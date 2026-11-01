
package com.weatherapp.alerts;

import com.weatherapp.models.CurrentWeatherResponse;
import com.weatherapp.models.ForecastResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.weatherapp.alerts.WeatherAlert.Severity;
import static com.weatherapp.alerts.WeatherAlert.Type;

/**
 * Evaluates weather information and generates application alerts.
 *
 * These thresholds are application-defined advisory rules,
 * not official meteorological warnings.
 */
public final class WeatherAlertService {

    private static final double HIGH_TEMPERATURE_CELSIUS = 40.0;
    private static final double LOW_TEMPERATURE_CELSIUS = 0.0;
    private static final double STRONG_WIND_METERS_PER_SECOND = 15.0;
    private static final double HIGH_RAIN_PROBABILITY = 0.70;

    /**
     * Analyzes current weather conditions.
     *
     * @param weather current weather response
     * @return immutable list of detected alerts
     */
    public List<WeatherAlert> evaluateCurrentWeather(
            CurrentWeatherResponse weather) {

        Objects.requireNonNull(
                weather,
                "Weather response must not be null."
        );

        List<WeatherAlert> alerts = new ArrayList<>();

        if (weather.main() != null) {
            evaluateTemperature(
                    alerts,
                    weather.main().temp(),
                    "Current conditions"
            );
        }

        if (weather.wind() != null) {
            evaluateWind(
                    alerts,
                    weather.wind().speed(),
                    "Current conditions"
            );
        }

        return List.copyOf(alerts);
    }

    /**
     * Analyzes forecast entries for potential weather risks.
     *
     * @param forecast five-day weather forecast
     * @return immutable list of detected alerts
     */
    public List<WeatherAlert> evaluateForecast(
            ForecastResponse forecast) {

        Objects.requireNonNull(
                forecast,
                "Forecast response must not be null."
        );

        List<WeatherAlert> alerts = new ArrayList<>();

        for (ForecastResponse.ForecastItem item : forecast.list()) {

            if (item == null) {
                continue;
            }

            String validTime = item.dateTimeText() == null
                    ? "Upcoming forecast"
                    : item.dateTimeText();

            if (item.main() != null) {
                evaluateTemperature(
                        alerts,
                        item.main().temp(),
                        validTime
                );
            }

            if (item.wind() != null) {
                evaluateWind(
                        alerts,
                        item.wind().speed(),
                        validTime
                );
            }

            evaluateRainfall(
                    alerts,
                    item.pop(),
                    validTime
            );
        }

        return List.copyOf(alerts);
    }

    /**
     * Evaluates extreme temperature conditions.
     */
    private void evaluateTemperature(
            List<WeatherAlert> alerts,
            Double temperature,
            String validTime) {

        if (temperature == null || !Double.isFinite(temperature)) {
            return;
        }

        if (temperature >= HIGH_TEMPERATURE_CELSIUS) {

            alerts.add(new WeatherAlert(
                    Severity.SEVERE,
                    Type.EXTREME_HEAT,
                    "Extreme Heat Warning",
                    String.format(
                            java.util.Locale.ROOT,
                            "Temperature may reach %.1f °C. "
                                    + "Stay hydrated and avoid prolonged "
                                    + "exposure to heat.",
                            temperature
                    ),
                    validTime
            ));

        } else if (temperature <= LOW_TEMPERATURE_CELSIUS) {

            alerts.add(new WeatherAlert(
                    Severity.HIGH,
                    Type.EXTREME_COLD,
                    "Low Temperature Warning",
                    String.format(
                            java.util.Locale.ROOT,
                            "Temperature may fall to %.1f °C. "
                                    + "Wear suitable warm clothing.",
                            temperature
                    ),
                    validTime
            ));
        }
    }

    /**
     * Evaluates strong wind conditions.
     */
    private void evaluateWind(
            List<WeatherAlert> alerts,
            Double windSpeed,
            String validTime) {

        if (windSpeed == null
                || !Double.isFinite(windSpeed)
                || windSpeed < STRONG_WIND_METERS_PER_SECOND) {
            return;
        }

        alerts.add(new WeatherAlert(
                Severity.HIGH,
                Type.STRONG_WIND,
                "Strong Wind Warning",
                String.format(
                        java.util.Locale.ROOT,
                        "Wind speed may reach %.1f m/s. "
                                + "Take care in exposed areas.",
                        windSpeed
                ),
                validTime
        ));
    }

    /**
     * Evaluates the probability of rainfall.
     */
    private void evaluateRainfall(
            List<WeatherAlert> alerts,
            Double probability,
            String validTime) {

        if (probability == null
                || !Double.isFinite(probability)
                || probability < HIGH_RAIN_PROBABILITY
                || probability > 1.0) {
            return;
        }

        alerts.add(new WeatherAlert(
                Severity.MODERATE,
                Type.HEAVY_RAINFALL,
                "High Rain Probability",
                String.format(
                        java.util.Locale.ROOT,
                        "Rain is forecast with a probability of %.0f%%. "
                                + "Consider carrying an umbrella.",
                        probability * 100
                ),
                validTime
        ));
    }
}
