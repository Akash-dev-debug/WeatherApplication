
package com.weatherapp.service;

import com.weatherapp.models.CurrentWeatherResponse;
import com.weatherapp.models.DailyForecastSummary;
import com.weatherapp.models.ForecastResponse;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 * Aggregates three-hour weather forecast entries into
 * daily weather summaries.
 *
 * Calculates:
 * - Minimum temperature.
 * - Maximum temperature.
 * - Representative weather condition.
 * - Average humidity.
 * - Maximum wind speed.
 * - Maximum precipitation probability.
 *
 * Forecast entries are grouped using the city's local
 * timezone offset supplied by the weather API.
 *
 * @version 1.0.0
 */
public final class ForecastSummaryService {

    /**
     * Aggregates the supplied forecast into daily summaries.
     *
     * @param forecast five-day forecast response
     * @return immutable, chronologically sorted daily summaries
     */
    public List<DailyForecastSummary> summarize(
            ForecastResponse forecast) {

        Objects.requireNonNull(
                forecast,
                "Forecast response must not be null."
        );

        if (forecast.list().isEmpty()) {
            return List.of();
        }

        ZoneOffset cityOffset = resolveCityOffset(forecast.city());

        Map<LocalDate, DailyAggregate> dailyAggregates =
                new TreeMap<>();

        for (ForecastResponse.ForecastItem item : forecast.list()) {

            if (item == null || item.dt() == null) {
                continue;
            }

            LocalDate forecastDate;

            try {

                forecastDate = Instant.ofEpochSecond(item.dt())
                        .atOffset(cityOffset)
                        .toLocalDate();

            } catch (DateTimeException | ArithmeticException exception) {

                // Ignore entries with invalid timestamps.
                continue;
            }

            DailyAggregate aggregate = dailyAggregates.computeIfAbsent(
                    forecastDate,
                    DailyAggregate::new
            );

            aggregate.add(item);
        }

        return dailyAggregates.values()
                .stream()
                .map(DailyAggregate::toSummary)
                .toList();
    }

    /**
     * Resolves the local timezone offset of the forecast city.
     *
     * UTC is used as a fallback if timezone information
     * is unavailable.
     */
    private ZoneOffset resolveCityOffset(
            ForecastResponse.City city) {

        if (city == null || city.timezone() == null) {
            return ZoneOffset.UTC;
        }

        try {

            return ZoneOffset.ofTotalSeconds(city.timezone());

        } catch (DateTimeException exception) {

            throw new IllegalArgumentException(
                    "Invalid city timezone offset in forecast response.",
                    exception
            );
        }
    }

    /**
     * Maintains aggregation statistics for an individual day.
     */
    private static final class DailyAggregate {

        private final LocalDate date;

        private Double minimumTemperature;

        private Double maximumTemperature;

        private Double maximumWindSpeed;

        private Double maximumPrecipitationProbability;

        private long humidityTotal;

        private int humidityCount;

        private int forecastSampleCount;

        private final Map<String, Integer> weatherDescriptions =
                new LinkedHashMap<>();

        private DailyAggregate(LocalDate date) {
            this.date = date;
        }

        /**
         * Adds one three-hour forecast entry to the daily aggregate.
         */
        private void add(ForecastResponse.ForecastItem item) {

            forecastSampleCount++;

            aggregateTemperature(item.main());

            aggregateHumidity(item.main());

            aggregateWind(item.wind());

            aggregatePrecipitation(item.pop());

            aggregateWeatherCondition(
                    item.getWeatherDescription()
            );
        }

        /**
         * Calculates the minimum and maximum temperatures.
         */
        private void aggregateTemperature(
                CurrentWeatherResponse.MainWeather main) {

            if (main == null) {
                return;
            }

            Double minimum = validTemperature(main.tempMin())
                    ? main.tempMin()
                    : main.temp();

            Double maximum = validTemperature(main.tempMax())
                    ? main.tempMax()
                    : main.temp();

            if (validTemperature(minimum)) {

                minimumTemperature = minimumTemperature == null
                        ? minimum
                        : Math.min(minimumTemperature, minimum);
            }

            if (validTemperature(maximum)) {

                maximumTemperature = maximumTemperature == null
                        ? maximum
                        : Math.max(maximumTemperature, maximum);
            }
        }

        /**
         * Calculates average humidity using valid measurements.
         */
        private void aggregateHumidity(
                CurrentWeatherResponse.MainWeather main) {

            if (main == null || main.humidity() == null) {
                return;
            }

            int humidity = main.humidity();

            if (humidity >= 0 && humidity <= 100) {

                humidityTotal += humidity;

                humidityCount++;
            }
        }

        /**
         * Finds the maximum valid wind speed for the day.
         */
        private void aggregateWind(
                CurrentWeatherResponse.Wind wind) {

            if (wind == null || wind.speed() == null) {
                return;
            }

            double speed = wind.speed();

            if (!Double.isFinite(speed) || speed < 0) {
                return;
            }

            maximumWindSpeed = maximumWindSpeed == null
                    ? speed
                    : Math.max(maximumWindSpeed, speed);
        }

        /**
         * Finds the maximum precipitation probability.
         */
        private void aggregatePrecipitation(Double probability) {

            if (probability == null
                    || !Double.isFinite(probability)
                    || probability < 0
                    || probability > 1) {
                return;
            }

            maximumPrecipitationProbability =
                    maximumPrecipitationProbability == null
                            ? probability
                            : Math.max(
                                    maximumPrecipitationProbability,
                                    probability
                            );
        }

        /**
         * Counts weather descriptions to determine
         * the representative condition for the day.
         */
        private void aggregateWeatherCondition(
                String description) {

            if (description == null
                    || description.isBlank()
                    || description.equalsIgnoreCase("Unknown")) {
                return;
            }

            weatherDescriptions.merge(
                    description,
                    1,
                    Integer::sum
            );
        }

        /**
         * Returns the most frequently occurring weather condition.
         *
         * In case of a tie, the first encountered condition is used.
         */
        private String getRepresentativeWeatherCondition() {

            String representative = "Unknown";

            int highestCount = 0;

            for (Map.Entry<String, Integer> entry
                    : weatherDescriptions.entrySet()) {

                if (entry.getValue() > highestCount) {

                    highestCount = entry.getValue();

                    representative = entry.getKey();
                }
            }

            return representative;
        }

        /**
         * Creates the immutable daily forecast summary.
         */
        private DailyForecastSummary toSummary() {

            Integer averageHumidity = humidityCount == 0
                    ? null
                    : (int) Math.round(
                            humidityTotal / (double) humidityCount
                    );

            return new DailyForecastSummary(
                    date,
                    minimumTemperature,
                    maximumTemperature,
                    getRepresentativeWeatherCondition(),
                    averageHumidity,
                    maximumWindSpeed,
                    maximumPrecipitationProbability,
                    forecastSampleCount
            );
        }

        /**
         * Checks whether a temperature is a valid finite value.
         */
        private boolean validTemperature(Double temperature) {

            return temperature != null
                    && Double.isFinite(temperature);
        }
    }
}
