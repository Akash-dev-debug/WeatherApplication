
package com.weatherapp.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for WeatherFormatter.
 */
class WeatherFormatterTest {

    @Test
    void shouldFormatTemperature() {
        assertEquals(
                "28.6 °C",
                WeatherFormatter.formatTemperature(28.567)
        );
    }

    @Test
    void shouldHandleMissingTemperature() {
        assertEquals(
                "—",
                WeatherFormatter.formatTemperature(null)
        );
    }

    @Test
    void shouldFormatHumidity() {
        assertEquals(
                "65%",
                WeatherFormatter.formatHumidity(65)
        );
    }

    @Test
    void shouldRejectInvalidHumidity() {
        assertEquals(
                "—",
                WeatherFormatter.formatHumidity(120)
        );
    }

    @Test
    void shouldFormatWindSpeed() {
        assertEquals(
                "5.2 m/s",
                WeatherFormatter.formatWindSpeed(5.234)
        );
    }

    @Test
    void shouldHandleNegativeWindSpeed() {
        assertEquals(
                "—",
                WeatherFormatter.formatWindSpeed(-5.0)
        );
    }

    @Test
    void shouldFormatPressure() {
        assertEquals(
                "1012 hPa",
                WeatherFormatter.formatPressure(1012)
        );
    }

    @Test
    void shouldConvertVisibilityToKilometers() {
        assertEquals(
                "10.0 km",
                WeatherFormatter.formatVisibility(10000)
        );
    }

    @Test
    void shouldFormatRainProbability() {
        assertEquals(
                "70%",
                WeatherFormatter.formatProbability(0.70)
        );
    }

    @Test
    void shouldRejectInvalidRainProbability() {
        assertEquals(
                "—",
                WeatherFormatter.formatProbability(1.5)
        );
    }

    @Test
    void shouldFormatLocalTime() {
        assertEquals(
                "12:00 AM",
                WeatherFormatter.formatLocalTime(0L, 0)
        );
    }
}
