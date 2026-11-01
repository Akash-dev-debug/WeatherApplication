
package com.weatherapp.models;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CitySuggestion.
 *
 * Verifies location formatting, JSON deserialization
 * and geographical coordinate validation.
 */
class CitySuggestionTest {

    private final ObjectMapper objectMapper =
            new ObjectMapper();

    @Test
    void shouldFormatCityWithStateAndCountry() {

        CitySuggestion city = new CitySuggestion(
                "Springfield",
                39.7817,
                -89.6501,
                "US",
                "Illinois"
        );

        assertEquals(
                "Springfield, Illinois, US",
                city.displayName()
        );
    }

    @Test
    void shouldAvoidDuplicatingCityAndState() {

        CitySuggestion city = new CitySuggestion(
                "Delhi",
                28.6139,
                77.2090,
                "IN",
                "Delhi"
        );

        assertEquals(
                "Delhi, IN",
                city.displayName()
        );
    }

    @Test
    void shouldFormatCityWithoutState() {

        CitySuggestion city = new CitySuggestion(
                "Mumbai",
                19.0760,
                72.8777,
                "IN",
                null
        );

        assertEquals(
                "Mumbai, IN",
                city.displayName()
        );
    }

    @Test
    void shouldFormatCityWithoutCountry() {

        CitySuggestion city = new CitySuggestion(
                "Mumbai",
                19.0760,
                72.8777,
                null,
                null
        );

        assertEquals(
                "Mumbai",
                city.displayName()
        );
    }

    @Test
    void shouldDeserializeCityFromJson() throws Exception {

        String json = """
                {
                    "name": "Delhi",
                    "lat": 28.6139,
                    "lon": 77.2090,
                    "country": "IN",
                    "state": "Delhi"
                }
                """;

        CitySuggestion city = objectMapper.readValue(
                json,
                CitySuggestion.class
        );

        assertEquals("Delhi", city.name());
        assertEquals("IN", city.country());
        assertEquals("Delhi", city.state());

        assertEquals(
                28.6139,
                city.lat(),
                0.0001
        );

        assertEquals(
                77.2090,
                city.lon(),
                0.0001
        );
    }

    @Test
    void shouldIgnoreUnknownJsonProperties() throws Exception {

        String json = """
                {
                    "name": "Mumbai",
                    "lat": 19.0760,
                    "lon": 72.8777,
                    "country": "IN",
                    "state": "Maharashtra",
                    "unknownField": "ignored"
                }
                """;

        CitySuggestion city = objectMapper.readValue(
                json,
                CitySuggestion.class
        );

        assertEquals("Mumbai", city.name());
        assertEquals("Maharashtra", city.state());
    }

    @Test
    void shouldRejectEmptyCityName() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new CitySuggestion(
                        " ",
                        28.6139,
                        77.2090,
                        "IN",
                        "Delhi"
                )
        );
    }

    @Test
    void shouldRejectInvalidLatitude() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new CitySuggestion(
                        "Delhi",
                        95.0,
                        77.2090,
                        "IN",
                        "Delhi"
                )
        );
    }

    @Test
    void shouldRejectInvalidLongitude() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new CitySuggestion(
                        "Delhi",
                        28.6139,
                        190.0,
                        "IN",
                        "Delhi"
                )
        );
    }

    @Test
    void shouldRejectNonFiniteCoordinates() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new CitySuggestion(
                        "Delhi",
                        Double.NaN,
                        77.2090,
                        "IN",
                        "Delhi"
                )
        );
    }
}
