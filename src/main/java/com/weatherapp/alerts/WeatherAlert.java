
package com.weatherapp.alerts;

import java.util.Objects;

/**
 * Immutable representation of a weather alert.
 */
public record WeatherAlert(
        Severity severity,
        Type type,
        String title,
        String message,
        String validTime
) {

    public WeatherAlert {
        Objects.requireNonNull(severity, "Severity must not be null.");
        Objects.requireNonNull(type, "Alert type must not be null.");
        Objects.requireNonNull(title, "Title must not be null.");
        Objects.requireNonNull(message, "Message must not be null.");

        if (title.isBlank() || message.isBlank()) {
            throw new IllegalArgumentException(
                    "Alert title and message must not be empty."
            );
        }

        validTime = validTime == null || validTime.isBlank()
                ? "Current conditions"
                : validTime;
    }

    /**
     * Defines the importance of an alert.
     */
    public enum Severity {
        MODERATE,
        HIGH,
        SEVERE
    }

    /**
     * Defines the category of weather alert.
     */
    public enum Type {
        EXTREME_HEAT,
        EXTREME_COLD,
        STRONG_WIND,
        HEAVY_RAINFALL
    }
}
