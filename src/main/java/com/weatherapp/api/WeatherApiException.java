
package com.weatherapp.api;

/**
 * Represents an error returned by the weather API.
 *
 * Extends IOException so callers can handle API failures
 * alongside other network-related errors.
 */
public final class WeatherApiException extends java.io.IOException {

    private static final long serialVersionUID = 1L;

    private final int statusCode;

    /**
     * Creates an exception for an unsuccessful API response.
     *
     * @param statusCode HTTP response status code
     * @param message descriptive error message
     */
    public WeatherApiException(int statusCode, String message) {
        super(message);

        if (statusCode < 100 || statusCode > 599) {
            throw new IllegalArgumentException(
                    "Invalid HTTP status code: " + statusCode
            );
        }

        this.statusCode = statusCode;
    }

    /**
     * Returns the HTTP status code associated with the error.
     *
     * @return HTTP status code
     */
    public int getStatusCode() {
        return statusCode;
    }
}
