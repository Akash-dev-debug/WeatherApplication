
package com.weatherapp.service;

import com.weatherapp.models.CurrentWeatherResponse;
import com.weatherapp.models.DailyForecastSummary;
import com.weatherapp.models.ForecastResponse;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ForecastSummaryService.
 *
 * Verifies daily grouping, temperature aggregation,
 * humidity averaging, wind speed and precipitation calculations.
 */
class ForecastSummaryServiceTest {

    private final ForecastSummaryService summaryService =
            new ForecastSummaryService();

    private static final ZoneOffset UTC = ZoneOffset.UTC;

    @Test
    void shouldAggregateForecastEntriesIntoDailySummary() {

        List<ForecastResponse.ForecastItem> entries = List.of(
                createForecastItem(
                        "2026-11-01T09:00:00Z",
                        25.0, 23.0, 27.0,
                        60, "clear sky", 3.6, 0.1
                ),
                createForecastItem(
                        "2026-11-01T12:00:00Z",
                        30.0, 28.0, 32.0,
                        50, "clear sky", 5.2, 0.8
                ),
                createForecastItem(
                        "2026-11-01T15:00:00Z",
                        28.0, 26.0, 30.0,
                        55, "few clouds", 4.0, 0.5
                )
        );

        ForecastResponse forecast =
                createForecast(entries, UTC);

        List<DailyForecastSummary> summaries =
                summaryService.summarize(forecast);

        assertEquals(1, summaries.size());

        DailyForecastSummary summary = summaries.get(0);

        assertEquals(
                LocalDate.of(2026, 11, 1),
                summary.date()
        );

        assertEquals(
                23.0,
                summary.minimumTemperatureCelsius(),
                0.001
        );

        assertEquals(
                32.0,
                summary.maximumTemperatureCelsius(),
                0.001
        );

        assertEquals(
                55,
                summary.averageHumidity()
        );

        assertEquals(
                5.2,
                summary.maximumWindSpeedMetersPerSecond(),
                0.001
        );

        assertEquals(
                0.8,
                summary.maximumPrecipitationProbability(),
                0.001
        );

        assertEquals(
                "clear sky",
                summary.weatherDescription()
        );

        assertEquals(
                3,
                summary.forecastSampleCount()
        );
    }

    @Test
    void shouldGroupForecastEntriesUsingCityLocalDate() {

        ZoneOffset indiaOffset =
                ZoneOffset.ofHoursMinutes(5, 30);

        List<ForecastResponse.ForecastItem> entries = List.of(
                createForecastItem(
                        "2026-11-01T20:00:00Z",
                        25.0, 23.0, 27.0,
                        60, "clear sky", 3.0, 0.1
                ),
                createForecastItem(
                        "2026-11-02T02:00:00Z",
                        28.0, 26.0, 30.0,
                        55, "few clouds", 4.0, 0.2
                ),
                createForecastItem(
                        "2026-11-02T20:00:00Z",
                        22.0, 20.0, 24.0,
                        65, "cloudy", 5.0, 0.3
                )
        );

        ForecastResponse forecast =
                createForecast(entries, indiaOffset);

        List<DailyForecastSummary> summaries =
                summaryService.summarize(forecast);

        assertEquals(2, summaries.size());

        assertEquals(
                LocalDate.of(2026, 11, 2),
                summaries.get(0).date()
        );

        assertEquals(
                2,
                summaries.get(0).forecastSampleCount()
        );

        assertEquals(
                LocalDate.of(2026, 11, 3),
                summaries.get(1).date()
        );
    }

    @Test
    void shouldReturnEmptyListForEmptyForecast() {

        ForecastResponse forecast =
                createForecast(List.of(), UTC);

        List<DailyForecastSummary> summaries =
                summaryService.summarize(forecast);

        assertNotNull(summaries);
        assertTrue(summaries.isEmpty());
    }

    @Test
    void shouldHandleMissingWeatherMeasurements() {

        ForecastResponse.ForecastItem item =
                new ForecastResponse.ForecastItem(
                        Instant.parse(
                                "2026-11-01T09:00:00Z"
                        ).getEpochSecond(),
                        null,
                        List.of(),
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        "2026-11-01 09:00:00"
                );

        ForecastResponse forecast =
                createForecast(List.of(item), UTC);

        List<DailyForecastSummary> summaries =
                summaryService.summarize(forecast);

        assertEquals(1, summaries.size());

        DailyForecastSummary summary = summaries.get(0);

        assertNull(summary.minimumTemperatureCelsius());

        assertNull(summary.maximumTemperatureCelsius());

        assertNull(summary.averageHumidity());

        assertNull(summary.maximumWindSpeedMetersPerSecond());

        assertNull(summary.maximumPrecipitationProbability());

        assertEquals(
                "Unknown",
                summary.weatherDescription()
        );

        assertEquals(1, summary.forecastSampleCount());
    }

    @Test
    void shouldSortDailySummariesChronologically() {

        List<ForecastResponse.ForecastItem> entries = List.of(
                createForecastItem(
                        "2026-11-03T09:00:00Z",
                        22.0, 20.0, 24.0,
                        60, "cloudy", 4.0, 0.2
                ),
                createForecastItem(
                        "2026-11-01T09:00:00Z",
                        28.0, 26.0, 30.0,
                        50, "clear sky", 3.0, 0.1
                ),
                createForecastItem(
                        "2026-11-02T09:00:00Z",
                        25.0, 23.0, 27.0,
                        55, "few clouds", 5.0, 0.3
                )
        );

        ForecastResponse forecast =
                createForecast(entries, UTC);

        List<DailyForecastSummary> summaries =
                summaryService.summarize(forecast);

        assertEquals(3, summaries.size());

        assertEquals(
                LocalDate.of(2026, 11, 1),
                summaries.get(0).date()
        );

        assertEquals(
                LocalDate.of(2026, 11, 2),
                summaries.get(1).date()
        );

        assertEquals(
                LocalDate.of(2026, 11, 3),
                summaries.get(2).date()
        );
    }

    @Test
    void shouldRejectNullForecast() {

        assertThrows(
                NullPointerException.class,
                () -> summaryService.summarize(null)
        );
    }

    /**
     * Creates an individual forecast item for testing.
     */
    private ForecastResponse.ForecastItem createForecastItem(
            String timestamp,
            double temperature,
            double minimumTemperature,
            double maximumTemperature,
            int humidity,
            String description,
            double windSpeed,
            double probability) {

        CurrentWeatherResponse.MainWeather main =
                new CurrentWeatherResponse.MainWeather(
                        temperature,
                        temperature,
                        minimumTemperature,
                        maximumTemperature,
                        1012,
                        humidity,
                        null,
                        null
                );

        CurrentWeatherResponse.WeatherCondition condition =
                new CurrentWeatherResponse.WeatherCondition(
                        800,
                        "Clear",
                        description,
                        "01d"
                );

        CurrentWeatherResponse.Wind wind =
                new CurrentWeatherResponse.Wind(
                        windSpeed,
                        180,
                        null
                );

        return new ForecastResponse.ForecastItem(
                Instant.parse(timestamp).getEpochSecond(),
                main,
                List.of(condition),
                null,
                wind,
                10000,
                probability,
                null,
                null,
                null,
                timestamp
        );
    }

    /**
     * Creates a forecast response with the specified timezone.
     */
    private ForecastResponse createForecast(
            List<ForecastResponse.ForecastItem> entries,
            ZoneOffset timezone) {

        ForecastResponse.City city =
                new ForecastResponse.City(
                        1273294,
                        "Delhi",
                        new CurrentWeatherResponse.Coordinates(
                                77.2090,
                                28.6139
                        ),
                        "IN",
                        10000000,
                        timezone.getTotalSeconds(),
                        null,
                        null
                );

        return new ForecastResponse(
                "200",
                0.0,
                entries.size(),
                entries,
                city
        );
    }
}
