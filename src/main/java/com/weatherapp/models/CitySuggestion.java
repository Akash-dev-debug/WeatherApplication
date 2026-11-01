
package com.weatherapp.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Objects;

/**
 * Represents a city returned by the OpenWeatherMap
 * Geocoding API.
 *
 * Contains the city name, geographical coordinates,
 * country and state information.
 *
 * @version 1.0.0
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CitySuggestion(
        String name,
        Double lat,
        Double lon,
        String country,
        String state
) {

    /**
     * Validates the geographical coordinates.
     */
    public CitySuggestion {

        Objects.requireNonNull(
                name,
                "City name must not be null."
        );

        if (name.isBlank()) {
            throw new IllegalArgumentException(
                    "City name must not be empty."
            );
        }

        if (lat == null || !Double.isFinite(lat)
                || lat < -90.0 || lat > 90.0) {

            throw new IllegalArgumentException(
                    "Invalid latitude."
            );
        }

        if (lon == null || !Double.isFinite(lon)
                || lon < -180.0 || lon > 180.0) {

            throw new IllegalArgumentException(
                    "Invalid longitude."
            );
        }
    }

    /**
     * Returns a human-readable city name.
     *
     * Example: Delhi, Delhi, IN
     *
     * @return formatted city location
     */
    public String displayName() {

        StringBuilder result = new StringBuilder(name);

        if (state != null && !state.isBlank()
                && !state.equalsIgnoreCase(name)) {

            result.append(", ").append(state);
        }

        if (country != null && !country.isBlank()) {

            result.append(", ").append(country);
        }

        return result.toString();
    }
}
