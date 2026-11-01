
package com.weatherapp.alerts;

import com.weatherapp.models.CurrentWeatherResponse;
import com.weatherapp.models.ForecastResponse;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for WeatherAlertService.
 */
class WeatherAlertServiceTest {

    private final WeatherAlertService alertService =
            new WeatherAlertService();

    @Test
    void shouldDetectExtremeHeat() {

        CurrentWeatherResponse weather = createWeather(42.0, 5.0);

        List<WeatherAlert> alerts =
                alertService.evaluateCurrentWeather(weather);

        assertEquals(1, alerts.size());

        assertEquals(
                WeatherAlert.Type.EXTREME_HEAT,
                alerts.get(0).type()
        );

        assertEquals(
                WeatherAlert.Severity.SEVERE,
                alerts.get(0).severity()
        );
    }

    @Test
    void shouldDetectExtremeCold() {

        CurrentWeatherResponse weather = createWeather(-2.0, 5.0);

        List<WeatherAlert> alerts =
                alertService.evaluateCurrentWeather(weather);

        assertTrue(alerts.stream().anyMatch(
                alert -> alert.type()
                        == WeatherAlert.Type.EXTREME_COLD
        ));
    }

    @Test
    void shouldDetectStrongWind() {

        CurrentWeatherResponse weather = createWeather(25.0, 18.0);

        List<WeatherAlert> alerts =
                alertService.evaluateCurrentWeather(weather);

        assertTrue(alerts.stream().anyMatch(
                alert -> alert.type()
                        == WeatherAlert.Type.STRONG_WIND
        ));
    }

    @Test
    void shouldNotGenerateAlertsForNormalConditions() {

        CurrentWeatherResponse weather = createWeather(25.0, 5.0);

        List<WeatherAlert> alerts =
                alertService.evaluateCurrentWeather(weather);

        assertTrue(alerts.isEmpty());
    }

    @Test
    void shouldDetectHighRainProbability() {

        ForecastResponse.ForecastItem item =
                new ForecastResponse.ForecastItem(
                        1793534400L,
                        null,
                        List.of(),
                        null,
                        null,
                        null,
                        0.80,
                        null,
                        null,
                        null,
                        "2026-11-01 12:00:00"
                );

        ForecastResponse forecast = new ForecastResponse(
                "200",
                0.0,
                1,
                List.of(item),
                null
        );

        List<WeatherAlert> alerts =
                alertService.evaluateForecast(forecast);

        assertTrue(alerts.stream().anyMatch(
                alert -> alert.type()
                        == WeatherAlert.Type.HEAVY_RAINFALL
        ));
    }

    /**
     * Creates a sample current weather response for testing.
     */
    private CurrentWeatherResponse createWeather(
            double temperature,
            double windSpeed) {

        CurrentWeatherResponse.MainWeather main =
                new CurrentWeatherResponse.MainWeather(
                        temperature,
                        temperature,
                        temperature - 2,
                        temperature + 2,
                        1012,
                        60,
                        null,
                        null
                );

        CurrentWeatherResponse.Wind wind =
                new CurrentWeatherResponse.Wind(
                        windSpeed,
                        180,
                        null
                );

        return new CurrentWeatherResponse(
                null,
                List.of(),
                null,
                main,
                10000,
                wind,
                null,
                null,
                null,
                null,
                null,
                "Delhi",
                200
        );
    }
}
