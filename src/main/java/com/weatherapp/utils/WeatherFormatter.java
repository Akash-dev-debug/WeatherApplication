
package com.weatherapp.utils;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Provides consistent formatting for weather-related values.
 *
 * This utility class is stateless and cannot be instantiated.
 */
public final class WeatherFormatter {

    private static final String UNAVAILABLE = "—";

    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH);

    private WeatherFormatter() {
        throw new UnsupportedOperationException(
                "Utility classes cannot be instantiated."
        );
    }

    /**
     * Formats temperature in Celsius.
     *
     * @param temperature temperature value
     * @return formatted temperature
     */
    public static String formatTemperature(Double temperature) {

        if (temperature == null || !Double.isFinite(temperature)) {
            return UNAVAILABLE;
        }

        return String.format(Locale.ROOT, "%.1f °C", temperature);
    }

    /**
     * Formats humidity as a percentage.
     *
     * @param humidity humidity value between 0 and 100
     * @return formatted humidity
     */
    public static String formatHumidity(Integer humidity) {

        if (humidity == null || humidity < 0 || humidity > 100) {
            return UNAVAILABLE;
        }

        return humidity + "%";
    }

    /**
     * Formats wind speed in metres per second.
     *
     * @param windSpeed wind speed value
     * @return formatted wind speed
     */
    public static String formatWindSpeed(Double windSpeed) {

        if (windSpeed == null
                || !Double.isFinite(windSpeed)
                || windSpeed < 0) {
            return UNAVAILABLE;
        }

        return String.format(Locale.ROOT, "%.1f m/s", windSpeed);
    }

    /**
     * Formats atmospheric pressure.
     *
     * @param pressure pressure in hectopascals
     * @return formatted pressure
     */
    public static String formatPressure(Integer pressure) {

        if (pressure == null || pressure <= 0) {
            return UNAVAILABLE;
        }

        return pressure + " hPa";
    }

    /**
     * Converts visibility from metres to kilometres.
     *
     * @param visibility visibility in metres
     * @return formatted visibility
     */
    public static String formatVisibility(Integer visibility) {

        if (visibility == null || visibility < 0) {
            return UNAVAILABLE;
        }

        double kilometres = visibility / 1000.0;

        return String.format(
                Locale.ROOT,
                "%.1f km",
                kilometres
        );
    }

    /**
     * Converts precipitation probability into a percentage.
     *
     * OpenWeatherMap provides this value as a fraction
     * between 0.0 and 1.0.
     *
     * @param probability precipitation probability
     * @return formatted percentage
     */
    public static String formatProbability(Double probability) {

        if (probability == null
                || !Double.isFinite(probability)
                || probability < 0.0
                || probability > 1.0) {
            return UNAVAILABLE;
        }

        return Math.round(probability * 100) + "%";
    }

    /**
     * Formats an epoch timestamp into the city's local time.
     *
     * The API supplies the city's UTC offset in seconds.
     *
     * @param epochSeconds timestamp in seconds
     * @param timezoneOffsetSeconds city's UTC offset
     * @return formatted local time
     */
    public static String formatLocalTime(
            Long epochSeconds,
            Integer timezoneOffsetSeconds) {

        if (epochSeconds == null || timezoneOffsetSeconds == null) {
            return UNAVAILABLE;
        }

        try {

            ZoneOffset offset =
                    ZoneOffset.ofTotalSeconds(timezoneOffsetSeconds);

            return TIME_FORMATTER.format(
                    Instant.ofEpochSecond(epochSeconds).atOffset(offset)
            );

        } catch (DateTimeException | ArithmeticException exception) {

            return UNAVAILABLE;
        }
    }
}
