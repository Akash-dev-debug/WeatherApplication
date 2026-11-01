
package com.weatherapp.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for WeatherUnitConverter.
 */
class WeatherUnitConverterTest {

    private static final double DELTA = 0.0001;

    @Test
    void shouldConvertCelsiusToFahrenheit() {
        assertEquals(
                77.0,
                WeatherUnitConverter.celsiusToFahrenheit(25.0),
                DELTA
        );
    }

    @Test
    void shouldConvertFahrenheitToCelsius() {
        assertEquals(
                25.0,
                WeatherUnitConverter.fahrenheitToCelsius(77.0),
                DELTA
        );
    }

    @Test
    void shouldConvertMetersPerSecondToKilometersPerHour() {
        assertEquals(
                36.0,
                WeatherUnitConverter
                        .metersPerSecondToKilometersPerHour(10.0),
                DELTA
        );
    }

    @Test
    void shouldConvertKilometersPerHourToMetersPerSecond() {
        assertEquals(
                10.0,
                WeatherUnitConverter
                        .kilometersPerHourToMetersPerSecond(36.0),
                DELTA
        );
    }

    @Test
    void shouldConvertMetersPerSecondToMilesPerHour() {
        assertEquals(
                22.36936,
                WeatherUnitConverter
                        .metersPerSecondToMilesPerHour(10.0),
                DELTA
        );
    }

    @Test
    void shouldConvertMilesPerHourToMetersPerSecond() {
        assertEquals(
                10.0,
                WeatherUnitConverter
                        .milesPerHourToMetersPerSecond(22.36936),
                DELTA
        );
    }

    @Test
    void shouldRejectNegativeWindSpeed() {
        assertThrows(
                IllegalArgumentException.class,
                () -> WeatherUnitConverter
                        .metersPerSecondToKilometersPerHour(-5.0)
        );
    }

    @Test
    void shouldRejectNonFiniteTemperature() {
        assertThrows(
                IllegalArgumentException.class,
                () -> WeatherUnitConverter
                        .celsiusToFahrenheit(Double.NaN)
        );
    }

    @Test
    void shouldRejectInfiniteWindSpeed() {
        assertThrows(
                IllegalArgumentException.class,
                () -> WeatherUnitConverter
                        .metersPerSecondToMilesPerHour(
                                Double.POSITIVE_INFINITY
                        )
        );
    }
}
