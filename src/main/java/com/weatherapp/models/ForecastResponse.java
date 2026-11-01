
package com.weatherapp.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Represents the five-day weather forecast response
 * returned by the OpenWeatherMap Forecast API.
 *
 * The API provides forecast entries at three-hour intervals.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ForecastResponse(

        String cod,
        Double message,
        Integer cnt,
        List<ForecastItem> list,
        City city

) {

    public ForecastResponse {
        list = list == null ? List.of() : List.copyOf(list);
    }

    /**
     * Represents an individual forecast entry.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ForecastItem(

            Long dt,

            CurrentWeatherResponse.MainWeather main,

            List<CurrentWeatherResponse.WeatherCondition> weather,

            CurrentWeatherResponse.Clouds clouds,

            CurrentWeatherResponse.Wind wind,

            Integer visibility,

            Double pop,

            Precipitation rain,

            Precipitation snow,

            ForecastSystem sys,

            @JsonProperty("dt_txt")
            String dateTimeText

    ) {

        public ForecastItem {
            weather = weather == null ? List.of() : List.copyOf(weather);
        }

        /**
         * Returns the primary weather description.
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

    /**
     * Represents rainfall or snowfall during the forecast period.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Precipitation(

            @JsonProperty("3h")
            Double amountLastThreeHours

    ) {
    }

    /**
     * Represents day/night forecast information.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ForecastSystem(
            String pod
    ) {
    }

    /**
     * Represents the forecast location.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record City(

            Integer id,

            String name,

            CurrentWeatherResponse.Coordinates coord,

            String country,

            Integer population,

            Integer timezone,

            Long sunrise,

            Long sunset

    ) {
    }
}
