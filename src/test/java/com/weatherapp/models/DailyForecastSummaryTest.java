
package com.weatherapp.models;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DailyForecastSummary.
 */
class DailyForecastSummaryTest {

    private static final LocalDate TEST_DATE =
            LocalDate.of(2026, 11, 1);

    @Test
    void shouldCreateValidDailySummary() {

        DailyForecastSummary summary = createValidSummary();

        assertEquals(TEST_DATE, summary.date());
        assertEquals(20.0, summary.minimumTemperatureCelsius());
        assertEquals(30.0, summary.maximumTemperatureCelsius());
        assertEquals("clear sky", summary.weatherDescription());
        assertEquals(60, summary.averageHumidity());
        assertEquals(5.0, summary.maximumWindSpeedMetersPerSecond());
        assertEquals(0.7, summary.maximumPrecipitationProbability());
        assertEquals(8, summary.forecastSampleCount());
    }

    @Test
    void shouldReplaceMissingDescriptionWithUnknown() {

        DailyForecastSummary summary = new DailyForecastSummary(
                TEST_DATE,
                20.0,
                30.0,
                null,
                60,
                5.0,
                0.5,
                4
        );

        assertEquals("Unknown", summary.weatherDescription());
    }

    @Test
    void shouldReplaceBlankDescriptionWithUnknown() {

        DailyForecastSummary summary = new DailyForecastSummary(
                TEST_DATE,
                20.0,
                30.0,
                "   ",
                60,
                5.0,
                0.5,
                4
        );

        assertEquals("Unknown", summary.weatherDescription());
    }

    @Test
    void shouldRejectNullDate() {

        assertThrows(
                NullPointerException.class,
                () -> new DailyForecastSummary(
                        null,
                        20.0,
                        30.0,
                        "clear sky",
                        60,
                        5.0,
                        0.5,
                        4
                )
        );
    }

    @Test
    void shouldRejectInvalidSampleCount() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new DailyForecastSummary(
                        TEST_DATE,
                        20.0,
                        30.0,
                        "clear sky",
                        60,
                        5.0,
                        0.5,
                        0
                )
        );
    }

    @Test
    void shouldRejectMinimumTemperatureGreaterThanMaximum() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new DailyForecastSummary(
                        TEST_DATE,
                        35.0,
                        25.0,
                        "clear sky",
                        60,
                        5.0,
                        0.5,
                        4
                )
        );
    }

    @Test
    void shouldRejectNonFiniteTemperature() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new DailyForecastSummary(
                        TEST_DATE,
                        Double.NaN,
                        30.0,
                        "clear sky",
                        60,
                        5.0,
                        0.5,
                        4
                )
        );
    }

    @Test
    void shouldRejectHumidityAboveOneHundred() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new DailyForecastSummary(
                        TEST_DATE,
                        20.0,
                        30.0,
                        "clear sky",
                        120,
                        5.0,
                        0.5,
                        4
                )
        );
    }

    @Test
    void shouldRejectNegativeWindSpeed() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new DailyForecastSummary(
                        TEST_DATE,
                        20.0,
                        30.0,
                        "clear sky",
                        60,
                        -5.0,
                        0.5,
                        4
                )
        );
    }

    @Test
    void shouldRejectInfiniteWindSpeed() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new DailyForecastSummary(
                        TEST_DATE,
                        20.0,
                        30.0,
                        "clear sky",
                        60,
                        Double.POSITIVE_INFINITY,
                        0.5,
                        4
                )
        );
    }

    @Test
    void shouldRejectRainProbabilityAboveOne() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new DailyForecastSummary(
                        TEST_DATE,
                        20.0,
                        30.0,
                        "clear sky",
                        60,
                        5.0,
                        1.5,
                        4
                )
        );
    }

    @Test
    void shouldRejectNonFiniteRainProbability() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new DailyForecastSummary(
                        TEST_DATE,
                        20.0,
                        30.0,
                        "clear sky",
                        60,
                        5.0,
                        Double.NaN,
                        4
                )
        );
    }

    /**
     * Creates a valid summary for test cases.
     */
    private DailyForecastSummary createValidSummary() {

        return new DailyForecastSummary(
                TEST_DATE,
                20.0,
                30.0,
                "clear sky",
                60,
                5.0,
                0.7,
                8
        );
    }
}
