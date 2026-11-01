
package com.weatherapp.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Represents the current weather response returned by
 * the OpenWeatherMap Current Weather API.
 *
 * This record maps the API JSON response into an
 * immutable Java data model.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CurrentWeatherResponse(

        Coordinates coord,

        List<WeatherCondition> weather,

        String base,

        MainWeather main,

        Integer visibility,

        Wind wind,

        Clouds clouds,

        Long dt,

        SystemInfo sys,

        Integer timezone,

        Long id,

        String name,

        Integer cod

) {

    /**
     * Ensures the weather collection is never null.
     */
    public CurrentWeatherResponse {
        weather = weather == null ? List.of() : List.copyOf(weather);
    }

    /**
     * Represents geographical coordinates.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Coordinates(
            Double lon,
            Double lat
    ) {
    }

    /**
     * Represents weather conditions.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record WeatherCondition(
            Integer id,
            String main,
            String description,
            String icon
    ) {
    }

    /**
     * Represents temperature and atmospheric information.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record MainWeather(

            Double temp,

            @JsonProperty("feels_like")
            Double feelsLike,

            @JsonProperty("temp_min")
            Double tempMin,

            @JsonProperty("temp_max")
            Double tempMax,

            Integer pressure,

            Integer humidity,

            @JsonProperty("sea_level")
            Integer seaLevel,

            @JsonProperty("grnd_level")
            Integer groundLevel

    ) {
    }

    /**
     * Represents wind information.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Wind(
            Double speed,
            Integer deg,
            Double gust
    ) {
    }

    /**
     * Represents cloud information.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Clouds(
            Integer all
    ) {
    }

    /**
     * Represents country and sunrise/sunset information.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SystemInfo(
            Integer type,
            Integer id,
            String country,
            Long sunrise,
            Long sunset
    ) {
    }

    /**
     * Returns the primary weather description.
     *
     * @return weather description or Unknown
     */
    public String getWeatherDescription() {
        if (weather.isEmpty()) {
            return "Unknown";
        }

        String description = weather.get(0).description();

        return description == null || description.isBlank()
                ? "Unknown"
                : description;
    }
}
