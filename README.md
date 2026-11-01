
# Weather Application

A Java-based desktop Weather Application that retrieves real-time weather information and five-day forecasts using the OpenWeatherMap API.

The application is developed using Java Swing, Java HTTP Client, Jackson JSON processing and Maven.

## 1. Project Overview

The Weather Application allows users to search for cities and retrieve current weather conditions and forecast information through an external weather service.

It also provides weather alert detection, desktop popup notifications, data caching and API performance monitoring.

### Project Objectives

- Understand Java networking and HTTP communication.
- Integrate external REST APIs.
- Process JSON responses using Jackson.
- Develop a desktop application using Java Swing.
- Implement caching for improved performance.
- Handle network errors and request timeouts.
- Implement weather alert detection.
- Monitor API and cache performance.

## 2. Features

### Current Weather
- Search for weather information by city.
- Display current temperature.
- Display feels-like temperature.
- Display weather conditions.
- Display humidity and atmospheric pressure.
- Display wind speed and visibility.
- Display sunrise and sunset times.

### Five-Day Forecast
- Retrieve five-day weather forecast data.
- Display forecast temperature.
- Display weather conditions.
- Display humidity and wind speed.
- Display precipitation probability.
- Present forecast entries in a sortable table.

The forecast API provides entries at three-hour intervals.

### City Search
- Search for geographical locations.
- Retrieve city suggestions through the OpenWeatherMap Geocoding API.
- Display city, state and country information.
- Use a debounced search to reduce unnecessary requests.

### Weather Alerts
The application evaluates weather conditions using configurable application-defined thresholds.

Supported alert categories:

- Extreme heat.
- Extreme cold.
- Strong wind.
- High rainfall probability.

Detected alerts are displayed in the application and can generate desktop popup notifications.

These thresholds are advisory application rules, not official meteorological warnings.

### Data Caching
- Store current weather responses.
- Store forecast responses separately.
- Configure cache expiration.
- Limit maximum cache capacity.
- Remove expired entries.
- Track cache hits and misses.
- Calculate cache hit rate.

Current cache configuration:

| Setting | Value |
|---|---:|
| Cache duration | 10 minutes |
| Maximum current-weather entries | 100 |
| Maximum forecast entries | 100 |

### Performance Monitoring

The application tracks:

- Total HTTP requests.
- Successful requests.
- Failed requests.
- Average response time.
- API success percentage.
- Total response data received.
- Current weather cache statistics.
- Forecast cache statistics.

## 3. Technologies Used

| Technology | Purpose |
|---|---|
| Java 21 compatibility | Application development |
| Java Swing | Desktop interface |
| Java HTTP Client | HTTP communication |
| Apache Maven | Build and dependency management |
| Jackson | JSON processing |
| JUnit 5 | Unit testing |
| OpenWeatherMap | Weather data provider |

## 4. Project Architecture

The application follows a layered architecture.

```text
WeatherApplication/
│
├── pom.xml
├── README.md
├── .gitignore
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── weatherapp/
│   │   │           ├── app/
│   │   │           ├── api/
│   │   │           ├── models/
│   │   │           ├── service/
│   │   │           ├── cache/
│   │   │           ├── gui/
│   │   │           ├── alerts/
│   │   │           └── utils/
│   │   │
│   │   └── resources/
│   │
│   └── test/
│       └── java/
│           └── com/
│               └── weatherapp/
│                   ├── alerts/
│                   ├── cache/
│                   ├── models/
│                   └── utils/
│
├── docs/
└── test-data/
```

### Package Responsibilities

**app**
Contains the application entry point.

**api**
Handles external HTTP requests, API configuration and network exceptions.

**models**
Contains structured Java models for weather, forecast and geographical data.

**service**
Contains application-level weather retrieval, caching and city-search logic.

**cache**
Implements thread-safe in-memory data caching.

**gui**
Contains the Swing desktop interface and performance dashboard.

**alerts**
Contains weather alert detection and notification functionality.

**utils**
Contains formatting, unit conversion and performance monitoring utilities.

## 5. API Integration

The application uses OpenWeatherMap API services.

### Current Weather Endpoint

```text
https://api.openweathermap.org/data/2.5/weather
```

### Five-Day Forecast Endpoint

```text
https://api.openweathermap.org/data/2.5/forecast
```

### Direct Geocoding Endpoint

```text
https://api.openweathermap.org/geo/1.0/direct
```

The application uses HTTP GET requests over HTTPS and processes the returned JSON using Jackson.

## 6. Installation Requirements

Install the following software:

- JDK 21 or newer.
- Apache Maven 3.10 or compatible.
- Visual Studio Code or another Java IDE.
- Active internet connection.
- OpenWeatherMap account and API key.

## 7. API Configuration

The application reads its API key from a Windows environment variable.

The required variable name is:

```text
OPENWEATHER_API_KEY
```

### Windows Configuration

Open Command Prompt and execute:

```cmd
setx OPENWEATHER_API_KEY "YOUR_API_KEY"
```

Replace the placeholder with your actual API key.

Restart VS Code after configuring the environment variable.

Verify the configuration:

```cmd
if defined OPENWEATHER_API_KEY (echo API KEY CONFIGURED) else (echo API KEY MISSING)
```

Never upload your actual API key to GitHub.

## 8. Build and Run

Open the project directory in the terminal.

### Compile the application

```cmd
mvn clean compile
```

### Execute unit tests

```cmd
mvn clean test
```

### Launch the application

```cmd
mvn org.codehaus.mojo:exec-maven-plugin:3.5.0:java -Dexec.mainClass=com.weatherapp.app.Main
```

The application will open its desktop interface.

## 9. Testing

JUnit 5 is used for automated unit testing.

The test suite covers:

- Temperature conversion.
- Wind-speed conversion.
- Weather value formatting.
- Invalid input handling.
- Weather alert detection.
- Cache storage and retrieval.
- Cache expiration and eviction.
- City suggestion validation.
- JSON deserialization.

Execute all available tests using:

```cmd
mvn clean test
```

Maven generates test reports under:

```text
target/surefire-reports/
```

## 10. Error Handling

The application handles:

- Invalid API credentials.
- Unavailable cities.
- HTTP error responses.
- Network connection failures.
- Request timeouts.
- Invalid JSON responses.
- Missing or invalid user input.

Network operations are executed using SwingWorker to keep the graphical interface responsive.

## 11. Caching Strategy

Current weather and forecast data are stored in separate in-memory caches.

Each cache uses:

- ConcurrentHashMap.
- Time-based expiration.
- Maximum entry capacity.
- Cache hit and miss tracking.
- Oldest-entry eviction when capacity is reached.

Cached values are removed when they expire or when the cache is cleared.

## 12. Security Considerations

- API credentials are stored in environment variables.
- Sensitive configuration must not be committed to GitHub.
- HTTPS is used for API communication.
- Generated build files are excluded through .gitignore.

## 13. Future Improvements

Possible future enhancements include:

- Interactive weather charts.
- More detailed daily forecast summaries.
- Additional weather providers.
- Persistent caching.
- Improved location selection.
- More comprehensive integration testing.
- Enhanced desktop interface design.

## 14. Project Deliverables

The project aims to provide:

- Java desktop Weather Application.
- HTTP client with external API integration.
- JSON processing implementation.
- Weather data models.
- Caching system.
- Weather alert system.
- Desktop notification functionality.
- API performance monitoring.
- Automated unit tests.
- Technical documentation.

## 15. Author

**Project:** Weather Application with API Integration

**Technology:** Java, Swing, Maven, Jackson, REST API

---

*Developed as part of the Week 11 Networking & Web Basics project.*
