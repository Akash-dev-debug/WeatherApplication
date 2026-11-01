
package com.weatherapp.api;

import java.net.URI;
import java.time.Duration;
import java.util.Objects;

/**
 * Central configuration for OpenWeatherMap API integration.
 *
 * Loads sensitive API credentials from environment variables
 * and provides shared network configuration.
 */
public final class ApiConfiguration {

    public static final String API_KEY_ENVIRONMENT_VARIABLE =
            "OPENWEATHER_API_KEY";

    private static final URI BASE_URI =
            URI.create("https://api.openweathermap.org/data/2.5/");

    private static final Duration CONNECT_TIMEOUT =
            Duration.ofSeconds(10);

    private static final Duration REQUEST_TIMEOUT =
            Duration.ofSeconds(15);

    private final String apiKey;

    private ApiConfiguration(String apiKey) {
        this.apiKey = Objects.requireNonNull(apiKey);
    }

    /**
     * Creates configuration using the system environment.
     *
     * @return validated API configuration
     * @throws IllegalStateException if the API key is missing
     */
    public static ApiConfiguration fromEnvironment() {

        String apiKey = System.getenv(API_KEY_ENVIRONMENT_VARIABLE);

        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "OpenWeatherMap API key is missing. "
                    + "Set the OPENWEATHER_API_KEY environment variable."
            );
        }

        apiKey = apiKey.trim();

        if (apiKey.equalsIgnoreCase("your_api_key_here")) {
            throw new IllegalStateException(
                    "Replace the placeholder with a valid API key."
            );
        }

        return new ApiConfiguration(apiKey);
    }

    /**
     * Returns the API key.
     * Package-private to restrict direct access to API components.
     */
    String apiKey() {
        return apiKey;
    }

    public URI baseUri() {
        return BASE_URI;
    }

    public Duration connectTimeout() {
        return CONNECT_TIMEOUT;
    }

    public Duration requestTimeout() {
        return REQUEST_TIMEOUT;
    }
}
