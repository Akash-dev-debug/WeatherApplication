
package com.weatherapp.service;

import com.weatherapp.api.WeatherApiClient;
import com.weatherapp.cache.ExpiringCache;
import com.weatherapp.models.CitySuggestion;
import com.weatherapp.models.CurrentWeatherResponse;
import com.weatherapp.models.ForecastResponse;
import com.weatherapp.utils.ApiPerformanceMonitor;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/**
 * Application service responsible for retrieving weather information,
 * managing cached data and searching geographical locations.
 *
 * This class separates application business logic from
 * API communication and cache implementation.
 *
 * @version 1.0.0
 */
public final class WeatherService {

    private static final Duration CACHE_DURATION =
            Duration.ofMinutes(10);

    private static final int MAX_CACHED_CITIES = 100;

    private final WeatherApiClient apiClient;

    private final ExpiringCache<String, CurrentWeatherResponse>
            currentWeatherCache;

    private final ExpiringCache<String, ForecastResponse>
            forecastCache;

    /**
     * Creates the weather service.
     *
     * @param apiClient configured weather API client
     */
    public WeatherService(WeatherApiClient apiClient) {

        this.apiClient = Objects.requireNonNull(
                apiClient,
                "Weather API client must not be null."
        );

        this.currentWeatherCache = new ExpiringCache<>(
                CACHE_DURATION,
                MAX_CACHED_CITIES
        );

        this.forecastCache = new ExpiringCache<>(
                CACHE_DURATION,
                MAX_CACHED_CITIES
        );
    }

    /**
     * Retrieves current weather for a city.
     *
     * Checks the cache before making an API request.
     * Successful responses are stored in the cache.
     *
     * @param city requested city name
     * @return current weather information
     * @throws IOException if a network or API error occurs
     * @throws InterruptedException if the request is interrupted
     */
    public CurrentWeatherResponse getCurrentWeather(String city)
            throws IOException, InterruptedException {

        String cacheKey = normalizeCity(city);

        Optional<CurrentWeatherResponse> cachedWeather =
                currentWeatherCache.get(cacheKey);

        if (cachedWeather.isPresent()) {
            return cachedWeather.get();
        }

        CurrentWeatherResponse weather =
                apiClient.getCurrentWeather(city.trim());

        if (weather == null) {
            throw new IOException(
                    "The weather API returned no current weather data."
            );
        }

        currentWeatherCache.put(cacheKey, weather);

        return weather;
    }

    /**
     * Retrieves the five-day weather forecast for a city.
     *
     * Checks the forecast cache before making an API request.
     *
     * @param city requested city name
     * @return five-day forecast information
     * @throws IOException if a network or API error occurs
     * @throws InterruptedException if the request is interrupted
     */
    public ForecastResponse getForecast(String city)
            throws IOException, InterruptedException {

        String cacheKey = normalizeCity(city);

        Optional<ForecastResponse> cachedForecast =
                forecastCache.get(cacheKey);

        if (cachedForecast.isPresent()) {
            return cachedForecast.get();
        }

        ForecastResponse forecast =
                apiClient.getForecast(city.trim());

        if (forecast == null) {
            throw new IOException(
                    "The weather API returned no forecast data."
            );
        }

        forecastCache.put(cacheKey, forecast);

        return forecast;
    }

    /**
     * Searches for matching cities using the OpenWeatherMap
     * Direct Geocoding API.
     *
     * Returns structured city information including geographical
     * coordinates, country and state.
     *
     * City suggestions are not cached in this version.
     *
     * @param query city name or search query
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

        return List.copyOf(
                apiClient.searchCities(normalizedQuery)
        );
    }

    /**
     * Removes cached current weather and forecast information
     * for a particular city.
     *
     * @param city city whose cached information must be removed
     */
    public void invalidateCity(String city) {

        String cacheKey = normalizeCity(city);

        currentWeatherCache.invalidate(cacheKey);

        forecastCache.invalidate(cacheKey);
    }

    /**
     * Clears all cached weather information.
     */
    public void clearCache() {

        currentWeatherCache.clear();

        forecastCache.clear();
    }

    /**
     * Returns the number of valid current-weather cache entries.
     *
     * @return current cache size
     */
    public int getCurrentWeatherCacheSize() {

        return currentWeatherCache.size();
    }

    /**
     * Returns the number of valid forecast cache entries.
     *
     * @return forecast cache size
     */
    public int getForecastCacheSize() {

        return forecastCache.size();
    }

    /**
     * Returns current-weather cache performance statistics.
     *
     * @return current-weather cache statistics
     */
    public ExpiringCache.CacheStatistics
            getCurrentWeatherCacheStatistics() {

        return currentWeatherCache.getStatistics();
    }

    /**
     * Returns forecast cache performance statistics.
     *
     * @return forecast cache statistics
     */
    public ExpiringCache.CacheStatistics
            getForecastCacheStatistics() {

        return forecastCache.getStatistics();
    }

    /**
     * Returns API performance statistics.
     *
     * @return immutable API performance snapshot
     */
    public ApiPerformanceMonitor.MetricsSnapshot
            getApiPerformanceSnapshot() {

        return apiClient.getPerformanceSnapshot();
    }

    /**
     * Normalizes city names to create consistent cache keys.
     *
     * @param city requested city name
     * @return normalized city name
     */
    private String normalizeCity(String city) {

        Objects.requireNonNull(
                city,
                "City name must not be null."
        );

        String normalizedCity = city.trim()
                .replaceAll("\\s+", " ")
                .toLowerCase(Locale.ROOT);

        if (normalizedCity.isBlank()) {
            throw new IllegalArgumentException(
                    "City name must not be empty."
            );
        }

        return normalizedCity;
    }
}
