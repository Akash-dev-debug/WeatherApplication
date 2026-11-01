
package com.weatherapp.api;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.weatherapp.models.CitySuggestion;
import com.weatherapp.models.CurrentWeatherResponse;
import com.weatherapp.models.ForecastResponse;
import com.weatherapp.utils.ApiPerformanceMonitor;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

/**
 * Handles all communication with the OpenWeatherMap API.
 *
 * Responsibilities:
 * - Retrieve current weather information.
 * - Retrieve five-day weather forecasts.
 * - Search cities using the Geocoding API.
 * - Handle HTTP responses and network failures.
 * - Deserialize JSON responses using Jackson.
 * - Monitor API performance.
 *
 * @version 1.0.0
 */
public final class WeatherApiClient {

    private static final String CURRENT_WEATHER_ENDPOINT = "weather";

    private static final String FORECAST_ENDPOINT = "forecast";

    private static final String GEOCODING_BASE_URL =
            "https://api.openweathermap.org/geo/1.0/";

    private static final String ACCEPT_HEADER = "application/json";

    private final ApiConfiguration configuration;

    private final HttpClient httpClient;

    private final ObjectMapper objectMapper;

    private final ApiPerformanceMonitor performanceMonitor;

    /**
     * Creates a weather API client with a dedicated
     * performance monitor.
     *
     * @param configuration validated API configuration
     */
    public WeatherApiClient(ApiConfiguration configuration) {

        this(configuration, new ApiPerformanceMonitor());
    }

    /**
     * Creates a weather API client with an externally
     * supplied performance monitor.
     *
     * @param configuration validated API configuration
     * @param performanceMonitor performance monitoring component
     */
    public WeatherApiClient(
            ApiConfiguration configuration,
            ApiPerformanceMonitor performanceMonitor) {

        this.configuration = Objects.requireNonNull(
                configuration,
                "API configuration must not be null."
        );

        this.performanceMonitor = Objects.requireNonNull(
                performanceMonitor,
                "Performance monitor must not be null."
        );

        this.objectMapper = new ObjectMapper();

        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(configuration.connectTimeout())
                .followRedirects(HttpClient.Redirect.NEVER)
                .build();
    }

    /**
     * Retrieves current weather information for a city.
     *
     * @param city requested city name
     * @return current weather response
     * @throws IOException if a network or API error occurs
     * @throws InterruptedException if the request is interrupted
     */
    public CurrentWeatherResponse getCurrentWeather(String city)
            throws IOException, InterruptedException {

        URI uri = buildWeatherUri(
                CURRENT_WEATHER_ENDPOINT,
                city
        );

        return executeRequest(
                uri,
                CurrentWeatherResponse.class
        );
    }

    /**
     * Retrieves the five-day weather forecast for a city.
     *
     * @param city requested city name
     * @return forecast response
     * @throws IOException if a network or API error occurs
     * @throws InterruptedException if the request is interrupted
     */
    public ForecastResponse getForecast(String city)
            throws IOException, InterruptedException {

        URI uri = buildWeatherUri(
                FORECAST_ENDPOINT,
                city
        );

        return executeRequest(
                uri,
                ForecastResponse.class
        );
    }

    /**
     * Searches for cities using the OpenWeatherMap
     * Direct Geocoding API.
     *
     * Returns a maximum of five matching locations.
     *
     * @param query city name or partial search query
     * @return immutable list of matching city suggestions
     * @throws IOException if a network or API error occurs
     * @throws InterruptedException if the request is interrupted
     */
    public List<CitySuggestion> searchCities(String query)
            throws IOException, InterruptedException {

        Objects.requireNonNull(
                query,
                "City search query must not be null."
        );

        String normalizedQuery = query.trim();

        if (normalizedQuery.isBlank()) {
            throw new IllegalArgumentException(
                    "City search query must not be empty."
            );
        }

        if (normalizedQuery.length() > 100) {
            throw new IllegalArgumentException(
                    "City search query must not exceed 100 characters."
            );
        }

        String encodedQuery =
                encodeQueryParameter(normalizedQuery);

        String encodedApiKey =
                encodeQueryParameter(configuration.apiKey());

        URI uri = URI.create(
                GEOCODING_BASE_URL
                        + "direct?q=" + encodedQuery
                        + "&limit=5"
                        + "&appid=" + encodedApiKey
        );

        JavaType responseType =
                objectMapper.getTypeFactory()
                        .constructCollectionType(
                                List.class,
                                CitySuggestion.class
                        );

        List<CitySuggestion> suggestions =
                executeRequest(uri, responseType);

        return suggestions == null
                ? List.of()
                : List.copyOf(suggestions);
    }

    /**
     * Constructs a weather API request URI.
     *
     * @param endpoint API endpoint
     * @param city requested city
     * @return constructed request URI
     */
    private URI buildWeatherUri(
            String endpoint,
            String city) {

        Objects.requireNonNull(
                city,
                "City must not be null."
        );

        String normalizedCity = city.trim();

        if (normalizedCity.isBlank()) {
            throw new IllegalArgumentException(
                    "City name must not be empty."
            );
        }

        if (normalizedCity.length() > 100) {
            throw new IllegalArgumentException(
                    "City name must not exceed 100 characters."
            );
        }

        String encodedCity =
                encodeQueryParameter(normalizedCity);

        String encodedApiKey =
                encodeQueryParameter(configuration.apiKey());

        String query = "q=" + encodedCity
                + "&appid=" + encodedApiKey
                + "&units=metric";

        return URI.create(
                configuration.baseUri().toString()
                        + endpoint
                        + "?"
                        + query
        );
    }

    /**
     * Encodes a query parameter using UTF-8.
     *
     * @param value original parameter
     * @return URL-encoded parameter
     */
    private String encodeQueryParameter(String value) {

        return URLEncoder.encode(
                value,
                StandardCharsets.UTF_8
        ).replace("+", "%20");
    }

    /**
     * Executes an HTTP request for a Java response class.
     *
     * @param uri API request URI
     * @param responseType expected response class
     * @return deserialized response
     * @throws IOException if the request or processing fails
     * @throws InterruptedException if interrupted
     */
    private <T> T executeRequest(
            URI uri,
            Class<T> responseType)
            throws IOException, InterruptedException {

        JavaType javaType =
                objectMapper.getTypeFactory()
                        .constructType(responseType);

        return executeRequest(uri, javaType);
    }

    /**
     * Executes an HTTP GET request and deserializes
     * its JSON response using Jackson.
     *
     * Supports both individual objects and generic
     * collection responses.
     *
     * @param uri API request URI
     * @param responseType expected JSON response type
     * @return deserialized response
     * @throws IOException if networking or JSON processing fails
     * @throws InterruptedException if the request is interrupted
     */
    private <T> T executeRequest(
            URI uri,
            JavaType responseType)
            throws IOException, InterruptedException {

        Objects.requireNonNull(
                uri,
                "Request URI must not be null."
        );

        Objects.requireNonNull(
                responseType,
                "Response type must not be null."
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .timeout(configuration.requestTimeout())
                .header("Accept", ACCEPT_HEADER)
                .header("User-Agent", "WeatherApplication/1.0")
                .GET()
                .build();

        long startTime = System.nanoTime();

        long responseBytes = 0;

        try {

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString(
                            StandardCharsets.UTF_8
                    )
            );

            String responseBody = response.body();

            if (responseBody != null) {

                responseBytes = responseBody.getBytes(
                        StandardCharsets.UTF_8
                ).length;
            }

            int statusCode = response.statusCode();

            if (statusCode < 200 || statusCode >= 300) {

                throw new WeatherApiException(
                        statusCode,
                        getErrorMessage(statusCode)
                );
            }

            if (responseBody == null || responseBody.isBlank()) {

                throw new IOException(
                        "Weather service returned an empty response."
                );
            }

            T result = objectMapper.readValue(
                    responseBody,
                    responseType
            );

            performanceMonitor.recordRequest(
                    System.nanoTime() - startTime,
                    responseBytes,
                    true
            );

            return result;

        } catch (IOException | InterruptedException exception) {

            performanceMonitor.recordRequest(
                    System.nanoTime() - startTime,
                    responseBytes,
                    false
            );

            throw exception;
        }
    }

    /**
     * Converts HTTP status codes into understandable messages.
     *
     * @param statusCode HTTP response status
     * @return descriptive error message
     */
    private String getErrorMessage(int statusCode) {

        return switch (statusCode) {

            case 400 ->
                    "Invalid city name or API request.";

            case 401, 403 ->
                    "Invalid or unauthorized API key.";

            case 404 ->
                    "City not found. Please check the city name.";

            case 429 ->
                    "API request limit exceeded. Please try again later.";

            case 500, 502, 503, 504 ->
                    "Weather service is temporarily unavailable.";

            default ->
                    "Weather API request failed with HTTP status "
                            + statusCode + ".";
        };
    }

    /**
     * Returns an immutable snapshot of API performance statistics.
     *
     * @return performance metrics
     */
    public ApiPerformanceMonitor.MetricsSnapshot
            getPerformanceSnapshot() {

        return performanceMonitor.getSnapshot();
    }
}
