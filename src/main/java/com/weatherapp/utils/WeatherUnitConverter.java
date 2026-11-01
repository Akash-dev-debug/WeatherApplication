
package com.weatherapp.utils;

/**
 * Provides temperature and wind-speed unit conversions.
 *
 * This utility class contains stateless conversion methods
 * and does not require object instantiation.
 */
public final class WeatherUnitConverter {

    private static final double FAHRENHEIT_OFFSET = 32.0;
    private static final double CELSIUS_SCALE = 5.0 / 9.0;
    private static final double KILOMETERS_PER_METER = 3.6;
    private static final double METERS_PER_SECOND_TO_MPH =
            2.2369362920544;

    private WeatherUnitConverter() {
        throw new UnsupportedOperationException(
                "Utility classes cannot be instantiated."
        );
    }

    /**
     * Converts Celsius to Fahrenheit.
     *
     * Formula: (C × 9/5) + 32
     */
    public static double celsiusToFahrenheit(double celsius) {

        requireFinite(celsius, "Temperature");

        return (celsius / CELSIUS_SCALE)
                + FAHRENHEIT_OFFSET;
    }

    /**
     * Converts Fahrenheit to Celsius.
     *
     * Formula: (F - 32) × 5/9
     */
    public static double fahrenheitToCelsius(double fahrenheit) {

        requireFinite(fahrenheit, "Temperature");

        return (fahrenheit - FAHRENHEIT_OFFSET)
                * CELSIUS_SCALE;
    }

    /**
     * Converts metres per second to kilometres per hour.
     *
     * Formula: m/s × 3.6
     */
    public static double metersPerSecondToKilometersPerHour(
            double metersPerSecond) {

        requireValidWindSpeed(metersPerSecond);

        return metersPerSecond * KILOMETERS_PER_METER;
    }

    /**
     * Converts kilometres per hour to metres per second.
     *
     * Formula: km/h ÷ 3.6
     */
    public static double kilometersPerHourToMetersPerSecond(
            double kilometersPerHour) {

        requireValidWindSpeed(kilometersPerHour);

        return kilometersPerHour / KILOMETERS_PER_METER;
    }

    /**
     * Converts metres per second to miles per hour.
     */
    public static double metersPerSecondToMilesPerHour(
            double metersPerSecond) {

        requireValidWindSpeed(metersPerSecond);

        return metersPerSecond * METERS_PER_SECOND_TO_MPH;
    }

    /**
     * Converts miles per hour to metres per second.
     */
    public static double milesPerHourToMetersPerSecond(
            double milesPerHour) {

        requireValidWindSpeed(milesPerHour);

        return milesPerHour / METERS_PER_SECOND_TO_MPH;
    }

    /**
     * Validates that a numeric value is finite.
     */
    private static void requireFinite(
            double value,
            String fieldName) {

        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException(
                    fieldName + " must be a finite number."
            );
        }
    }

    /**
     * Validates wind-speed values.
     * Wind speed cannot be negative.
     */
    private static void requireValidWindSpeed(double speed) {

        requireFinite(speed, "Wind speed");

        if (speed < 0) {
            throw new IllegalArgumentException(
                    "Wind speed cannot be negative."
            );
        }
    }
}
