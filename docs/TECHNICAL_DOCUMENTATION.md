
# Technical Documentation
## Weather Application with API Integration

Version: 1.0.0

Technology: Java, Swing, Maven, Jackson and OpenWeatherMap API.

---

# 1. Introduction

The Weather Application is a Java-based desktop application designed to retrieve and display current weather information and five-day weather forecasts.

The application communicates with OpenWeatherMap through HTTP requests and processes JSON responses using the Jackson library.

It also implements caching, weather alert detection, desktop notifications and API performance monitoring.

## 1.1 Objectives

The main objectives are:

- Understand networking concepts in Java.
- Implement HTTP communication.
- Integrate an external REST API.
- Process JSON responses.
- Develop a graphical desktop interface.
- Implement an efficient caching mechanism.
- Handle network failures and timeouts.
- Monitor application performance.

---

# 2. System Architecture

The application follows a layered architecture to separate user interface components, business logic, external communication and data processing.

## 2.1 Architecture Components

### Presentation Layer

Package: `com.weatherapp.gui`

Responsibilities:

- Display weather information.
- Accept city search input.
- Display forecast information.
- Display weather alerts.
- Show API performance statistics.
- Display desktop notifications through the notification service.

Main classes:

- WeatherAppFrame
- PerformancePanel

### Application Service Layer

Package: `com.weatherapp.service`

Responsibilities:

- Coordinate weather retrieval.
- Communicate with the API client.
- Manage cached weather responses.
- Provide city-search functionality.
- Expose cache and API performance statistics.

Main class:

- WeatherService

### API Communication Layer

Package: `com.weatherapp.api`

Responsibilities:

- Create HTTP requests.
- Communicate with OpenWeatherMap.
- Handle HTTP responses.
- Process HTTP status codes.
- Handle request timeouts and network failures.
- Deserialize JSON responses.

Main classes:

- ApiConfiguration
- WeatherApiClient
- WeatherApiException

### Data Model Layer

Package: `com.weatherapp.models`

Responsibilities:

- Represent current weather information.
- Represent five-day forecast information.
- Represent geographical coordinates.
- Represent city suggestions.

Main classes:

- CurrentWeatherResponse
- ForecastResponse
- CitySuggestion

### Caching Layer

Package: `com.weatherapp.cache`

Responsibilities:

- Store weather information temporarily.
- Retrieve previously requested weather data.
- Remove expired entries.
- Maintain maximum cache capacity.
- Record cache hits and misses.

Main class:

- ExpiringCache

### Alert Layer

Package: `com.weatherapp.alerts`

Responsibilities:

- Detect application-defined weather conditions.
- Generate weather alerts.
- Display desktop popup notifications.

Main classes:

- WeatherAlert
- WeatherAlertService
- WeatherNotificationService

### Utility Layer

Package: `com.weatherapp.utils`

Responsibilities:

- Format weather values.
- Convert temperature and wind-speed units.
- Measure API performance.

Main classes:

- WeatherFormatter
- WeatherUnitConverter
- ApiPerformanceMonitor

---

# 3. API Integration

The application uses OpenWeatherMap as its external weather data provider.

Three API endpoints are implemented.

## 3.1 Current Weather API

Endpoint:

```text
https://api.openweathermap.org/data/2.5/weather
```

Purpose:

Retrieves the current weather information for a specified city.

Request parameters:

| Parameter | Description |
|---|---|
| q | City name |
| appid | API authentication key |
| units | Temperature measurement system |

The application uses metric units.

## 3.2 Five-Day Forecast API

Endpoint:

```text
https://api.openweathermap.org/data/2.5/forecast
```

Purpose:

Retrieves weather forecast information for five days.

The API provides forecast entries at three-hour intervals.

The application displays these entries in a sortable table.

## 3.3 Direct Geocoding API

Endpoint:

```text
https://api.openweathermap.org/geo/1.0/direct
```

Purpose:

Searches for geographical locations based on city names.

The application retrieves:

- City name.
- Latitude.
- Longitude.
- Country.
- State.

A maximum of five matching locations is requested.

## 3.4 HTTP Communication

Java's built-in HttpClient is used for network communication.

The client is configured with:

- HTTP/2 support.
- 10-second connection timeout.
- 15-second request timeout.
- HTTPS API endpoints.
- JSON Accept header.

The application uses HTTP GET requests to retrieve weather information.

---

# 4. JSON Processing

Jackson is used to deserialize JSON responses into structured Java objects.

The main JSON-processing component is:

```java
ObjectMapper objectMapper = new ObjectMapper();
```

## 4.1 Current Weather Processing

The current weather JSON response is mapped into:

CurrentWeatherResponse.

Nested models represent:

- Coordinates.
- Weather conditions.
- Temperature information.
- Wind information.
- Cloud information.
- Sunrise and sunset information.

## 4.2 Forecast Processing

Forecast JSON responses are mapped into:

ForecastResponse.

It contains:

- Forecast entries.
- Temperature information.
- Weather conditions.
- Wind information.
- Precipitation probability.
- City information.

## 4.3 City Search Processing

The Geocoding API returns a JSON array.

Jackson uses JavaType to convert this array into:

```java
List<CitySuggestion>
```

Unknown JSON properties are ignored using Jackson annotations.

This makes the application less dependent on receiving every optional API field.

---

# 5. Caching Strategy

The application implements an in-memory caching mechanism.

The implementation uses:

```java
ConcurrentHashMap
```

This supports concurrent access to cached information.

## 5.1 Cache Configuration

| Configuration | Value |
|---|---:|
| Cache duration | 10 minutes |
| Maximum current weather entries | 100 |
| Maximum forecast entries | 100 |
| Storage | In-memory |
| Cache implementation | ConcurrentHashMap |

Current weather and forecast information are maintained in separate caches.

## 5.2 Cache Working Principle

When a user searches for weather information:

1. The application checks the cache.
2. If valid data exists, the cached information is returned.
3. If the information does not exist, an API request is performed.
4. The successful API response is stored in the cache.
5. When an entry expires, it is removed during cache operations.

## 5.3 Cache Performance

The application records:

- Cache hits.
- Cache misses.
- Cache hit percentage.
- Current number of entries.
- Maximum cache capacity.

Cache hit rate formula:

```text
Cache Hit Rate = (Cache Hits / Total Cache Lookups) × 100
```

The cache uses oldest-entry eviction when its maximum capacity is reached.

Important: The cache is stored in memory, so cached information is not preserved after the application closes.

---

# 6. Weather Alert System

The application includes a weather alert detection service.

The alert service evaluates current weather and forecast information.

## 6.1 Alert Categories

| Alert | Application Threshold | Severity |
|---|---|---|
| Extreme Heat | 40°C or above | Severe |
| Extreme Cold | 0°C or below | High |
| Strong Wind | 15 m/s or above | High |
| High Rain Probability | 70% or above | Moderate |

These thresholds are application-defined advisory rules and do not represent official meteorological warnings.

## 6.2 Alert Processing

The alert service:

1. Receives weather information.
2. Checks temperature conditions.
3. Checks wind speed.
4. Checks forecast precipitation probability.
5. Creates applicable weather alerts.
6. Returns an immutable list of alerts.

## 6.3 Desktop Notifications

The WeatherNotificationService displays popup notifications when alerts are detected.

Features:

- Non-blocking popup windows.
- Different notification types based on severity.
- Automatic dismissal after 10 seconds.
- Limited number of alerts per popup.

---

# 7. City Search Functionality

The application provides a geographical city-search feature.

It uses the OpenWeatherMap Direct Geocoding API.

## 7.1 Search Process

1. The user enters a city name.
2. The application waits for approximately 500 milliseconds after typing stops.
3. The search query is sent to the Geocoding API in a background thread.
4. Matching locations are retrieved.
5. Results are displayed in a dropdown.
6. The user selects a location.
7. The selected city and country code are used for weather retrieval.

This approach reduces unnecessary API calls caused by sending a request after every keystroke.

---

# 8. Performance Monitoring

The application includes API performance monitoring.

The monitoring component is:

ApiPerformanceMonitor.

It maintains statistics for actual HTTP requests.

## 8.1 Performance Metrics

| Metric | Description |
|---|---|
| Total Requests | Number of HTTP requests |
| Successful Requests | Successfully processed responses |
| Failed Requests | Failed HTTP or response-processing operations |
| Average Latency | Average request-processing duration |
| Success Rate | Percentage of successful requests |
| Response Data | Total response-body bytes received |

## 8.2 Success Rate Formula

```text
Success Rate = (Successful Requests / Total Requests) × 100
```

## 8.3 Average Response Time

```text
Average Response Time =
Total Request Duration / Total Requests
```

Request duration is measured using System.nanoTime().

Cache hits do not create HTTP requests and therefore are not included in the API request count.

---

# 9. Error Handling

The application includes error-handling mechanisms for common network and API failures.

## 9.1 HTTP Error Responses

| Status Code | Description |
|---|---|
| 400 | Invalid API request |
| 401 | Authentication failure |
| 403 | Authorization failure |
| 404 | City not found |
| 429 | API request limit exceeded |
| 500 | Internal server error |
| 502 | Bad gateway |
| 503 | Service unavailable |
| 504 | Gateway timeout |

## 9.2 Network Error Handling

The application handles:

- Connection failures.
- Request timeouts.
- Interrupted requests.
- Empty API responses.
- Invalid JSON responses.
- Missing configuration.
- Invalid city input.

A custom WeatherApiException class represents HTTP API failures.

## 9.3 GUI Responsiveness

The application uses SwingWorker for network operations.

This prevents HTTP requests from blocking the Swing Event Dispatch Thread.

GUI updates are performed on the Event Dispatch Thread.

---

# 10. Security and Configuration

The OpenWeatherMap API key is stored outside the Java source code.

The application uses the following environment variable:

```text
OPENWEATHER_API_KEY
```

This prevents the actual credential from being hardcoded into application classes.

Additional security practices:

- HTTPS communication.
- Environment-based API credentials.
- Sensitive configuration excluded from Git.
- No API key committed to source control.

---

# 11. Unit Testing

JUnit 5 is used for automated unit testing.

The test suite covers the application's utility classes, caching logic, alert detection and city data model.

## 11.1 Test Categories

### WeatherUnitConverterTest

Tests temperature and wind-speed conversions.

### WeatherFormatterTest

Tests weather value formatting and missing-value handling.

### WeatherAlertServiceTest

Tests alert detection under different weather conditions.

### ExpiringCacheTest

Tests caching, expiration, eviction, invalidation and cache statistics.

### CitySuggestionTest

Tests city formatting, JSON deserialization and coordinate validation.

## 11.2 Execute Unit Tests

Run:

```cmd
mvn clean test
```

Maven generates test reports in:

```text
target/surefire-reports/
```

The final test results should be recorded after executing the complete test suite.

---

# 12. Limitations and Future Enhancements

The current implementation uses in-memory caching rather than persistent storage.

City suggestions are retrieved through the Geocoding API and are not currently cached.

Additional features can be implemented in future versions:

- Daily forecast summaries.
- Weather charts and visualizations.
- Persistent caching.
- Additional weather providers.
- Automated API integration tests.
- Improved notification management.
- Expanded performance analysis.
- Enhanced user interface customization.

---

# 13. Conclusion

The Weather Application demonstrates practical implementation of Java networking and web integration concepts.

It combines HTTP communication, REST API consumption, JSON processing, desktop GUI development, caching, exception handling and performance monitoring.

The project provides practical experience in designing and implementing a modular Java application that communicates with an external web service.
