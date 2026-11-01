
package com.weatherapp.models;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Immutable representation of an aggregated daily weather forecast.
 *
 * @version 1.0.0
 */
public record DailyForecastSummary(

        LocalDate date,

        Double minimumTemperatureCelsius,

        Double maximumTemperatureCelsius,

        String weatherDescription,

        Integer averageHumidity,

        Double maximumWindSpeedMetersPerSecond,

        Double maximumPrecipitationProbability,

        int forecastSampleCount

) {

    public DailyForecastSummary {

        Objects.requireNonNull(
                date,
                "Forecast date must not be null."
        );

        weatherDescription =
                weatherDescription == null
                        || weatherDescription.isBlank()
                        ? "Unknown"
                        : weatherDescription;

        if (forecastSampleCount <= 0) {
            throw new IllegalArgumentException(
                    "Forecast sample count must be positive."
            );
        }

        validateFinite(
                minimumTemperatureCelsius,
                "Minimum temperature"
        );

        validateFinite(
                maximumTemperatureCelsius,
                "Maximum temperature"
        );

        if (minimumTemperatureCelsius != null
                && maximumTemperatureCelsius != null
                && minimumTemperatureCelsius > maximumTemperatureCelsius) {

            throw new IllegalArgumentException(
                    "Minimum temperature cannot exceed maximum temperature."
            );
        }

        if (averageHumidity != null
                && (averageHumidity < 0 || averageHumidity > 100)) {

            throw new IllegalArgumentException(
                    "Average humidity must be between 0 and 100."
            );
        }

        validateNonNegativeFinite(
                maximumWindSpeedMetersPerSecond,
                "Maximum wind speed"
        );

        validateProbability(
                maximumPrecipitationProbability
        );
    }

    /**
     * Validates an optional finite numeric value.
     */
    private static void validateFinite(
            Double value,
            String fieldName) {

        if (value != null && !Double.isFinite(value)) {
            throw new IllegalArgumentException(
                    fieldName + " must be a finite number."
            );
        }
    }

    /**
     * Validates an optional non-negative finite value.
     */
    private static void validateNonNegativeFinite(
            Double value,
            String fieldName) {

        if (value != null
                && (!Double.isFinite(value) || value < 0)) {

            throw new IllegalArgumentException(
                    fieldName + " must be a finite non-negative number."
            );
        }
    }

    /**
     * Validates precipitation probability.
     */
    private static void validateProbability(Double probability) {

        if (probability != null
                && (!Double.isFinite(probability)
                || probability < 0.0
                || probability > 1.0)) {

            throw new IllegalArgumentException(
                    "Precipitation probability must be between 0 and 1."
            );
        }
    }
}
