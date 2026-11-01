
package com.weatherapp.models;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests JSON deserialization using sample API responses.
 *
 * These tests do not require an internet connection
 * or an active OpenWeatherMap API key.
 */
class WeatherJsonDeserializationTest {

    private final ObjectMapper objectMapper =
            new ObjectMapper();

    private static final Path TEST_DATA_DIRECTORY =
            Path.of("test-data");

    /**
     * Tests current weather JSON deserialization.
     */
    @Test
    void shouldDeserializeCurrentWeatherSample()
            throws IOException {

        Path file = TEST_DATA_DIRECTORY.resolve(
                "current-weather-sample.json"
        );

        try (var reader = Files.newBufferedReader(file)) {

            CurrentWeatherResponse weather =
                    objectMapper.readValue(
                            reader,
                            CurrentWeatherResponse.class
                    );

            assertNotNull(weather);

            assertEquals("Delhi", weather.name());

            assertEquals(200, weather.cod());

            assertNotNull(weather.coord());

            assertEquals(
                    28.6139,
                    weather.coord().lat(),
                    0.0001
            );

            assertEquals(
                    28.5,
                    weather.main().temp(),
                    0.001
            );

            assertEquals(
                    45,
                    weather.main().humidity()
            );

            assertEquals(
                    "clear sky",
                    weather.getWeatherDescription()
            );

            assertEquals(
                    3.6,
                    weather.wind().speed(),
                    0.001
            );

            assertEquals(
                    "IN",
                    weather.sys().country()
            );
        }
    }

    /**
     * Tests five-day forecast JSON deserialization.
     */
    @Test
    void shouldDeserializeForecastSample()
            throws IOException {

        Path file = TEST_DATA_DIRECTORY.resolve(
                "forecast-sample.json"
        );

        try (var reader = Files.newBufferedReader(file)) {

            ForecastResponse forecast =
                    objectMapper.readValue(
                            reader,
                            ForecastResponse.class
                    );

            assertNotNull(forecast);

            assertEquals("200", forecast.cod());

            assertEquals(2, forecast.cnt());

            assertNotNull(forecast.list());

            assertEquals(2, forecast.list().size());

            assertEquals(
                    "Delhi",
                    forecast.city().name()
            );

            ForecastResponse.ForecastItem firstItem =
                    forecast.list().get(0);

            assertEquals(
                    28.5,
                    firstItem.main().temp(),
                    0.001
            );

            assertEquals(
                    "clear sky",
                    firstItem.getWeatherDescription()
            );

            ForecastResponse.ForecastItem secondItem =
                    forecast.list().get(1);

            assertEquals(
                    0.8,
                    secondItem.pop(),
                    0.001
            );

            assertEquals(
                    "few clouds",
                    secondItem.getWeatherDescription()
            );

            assertNotNull(secondItem.rain());

            assertEquals(
                    1.5,
                    secondItem.rain().amountLastThreeHours(),
                    0.001
            );
        }
    }

    /**
     * Tests city suggestions JSON array deserialization.
     */
    @Test
    void shouldDeserializeCitySuggestionsSample()
            throws IOException {

        Path file = TEST_DATA_DIRECTORY.resolve(
                "city-suggestions-sample.json"
        );

        try (var reader = Files.newBufferedReader(file)) {

            List<CitySuggestion> cities =
                    objectMapper.readValue(
                            reader,
                            new TypeReference<List<CitySuggestion>>() {}
                    );

            assertNotNull(cities);

            assertEquals(3, cities.size());

            CitySuggestion firstCity = cities.get(0);

            assertEquals(
                    "Delhi",
                    firstCity.name()
            );

            assertEquals(
                    "IN",
                    firstCity.country()
            );

            assertEquals(
                    "Delhi, IN",
                    firstCity.displayName()
            );

            CitySuggestion thirdCity = cities.get(2);

            assertEquals(
                    "Dehradun",
                    thirdCity.name()
            );

            assertEquals(
                    "Uttarakhand",
                    thirdCity.state()
            );

            assertEquals(
                    "Dehradun, Uttarakhand, IN",
                    thirdCity.displayName()
            );
        }
    }
}
