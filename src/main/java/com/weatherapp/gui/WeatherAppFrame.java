
package com.weatherapp.gui;

import com.weatherapp.alerts.WeatherAlert;
import com.weatherapp.alerts.WeatherAlertService;
import com.weatherapp.alerts.WeatherNotificationService;
import com.weatherapp.api.WeatherApiException;
import com.weatherapp.models.CitySuggestion;
import com.weatherapp.models.CurrentWeatherResponse;
import com.weatherapp.models.DailyForecastSummary;
import com.weatherapp.models.ForecastResponse;
import com.weatherapp.service.ForecastSummaryService;
import com.weatherapp.service.WeatherService;
import com.weatherapp.utils.WeatherFormatter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;

import java.awt.*;
import java.awt.event.ItemEvent;
import java.io.IOException;
import java.net.http.HttpTimeoutException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

/**
 * Main desktop window of the Weather Application.
 *
 * Features:
 * - Current weather dashboard.
 * - Interactive city search.
 * - Daily forecast summaries.
 * - Weather alert detection.
 * - Desktop notifications.
 * - API performance monitoring.
 * - Cache performance monitoring.
 * - Background network operations.
 *
 * @version 1.0.0
 */
public final class WeatherAppFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    private static final Color BACKGROUND_COLOR =
            new Color(245, 247, 250);

    private static final Color CARD_COLOR = Color.WHITE;

    private static final Color PRIMARY_COLOR =
            new Color(37, 99, 235);

    private static final Color TEXT_COLOR =
            new Color(31, 41, 55);

    private static final int SEARCH_DEBOUNCE_MS = 500;

    private static final int MIN_SEARCH_LENGTH = 2;

    private final WeatherService weatherService;

    private final WeatherAlertService alertService;

    private final ForecastSummaryService forecastSummaryService =
            new ForecastSummaryService();

    private final WeatherNotificationService notificationService =
            new WeatherNotificationService();

    private final PerformancePanel performancePanel =
            new PerformancePanel();

    private final JTextField cityField =
            new JTextField(18);

    private final JComboBox<CitySuggestion> citySuggestions =
            new JComboBox<>();

    private final JButton currentWeatherButton =
            new JButton("Get Weather");

    private final JButton forecastButton =
            new JButton("5-Day Forecast");

    private final JLabel statusLabel =
            new JLabel("Ready");

    private final JLabel locationLabel =
            new JLabel("Search for a city");

    private final JLabel temperatureValue =
            new JLabel("—");

    private final JLabel feelsLikeValue =
            new JLabel("—");

    private final JLabel conditionValue =
            new JLabel("—");

    private final JLabel humidityValue =
            new JLabel("—");

    private final JLabel windValue =
            new JLabel("—");

    private final JLabel pressureValue =
            new JLabel("—");

    private final JLabel visibilityValue =
            new JLabel("—");

    private final JLabel sunriseValue =
            new JLabel("—");

    private final JLabel sunsetValue =
            new JLabel("—");

    private final JTextArea alertsArea =
            new JTextArea();

    private final DefaultTableModel forecastTableModel =
            new DefaultTableModel(
                    new Object[]{
                            "Date",
                            "Minimum",
                            "Maximum",
                            "Conditions",
                            "Avg Humidity",
                            "Max Wind",
                            "Rain Probability",
                            "Samples"
                    },
                    0
            ) {

                private static final long serialVersionUID = 1L;

                @Override
                public boolean isCellEditable(
                        int row,
                        int column) {

                    return false;
                }
            };

    private final Timer citySearchTimer =
            new Timer(
                    SEARCH_DEBOUNCE_MS,
                    event -> searchCitySuggestions()
            );

    private CitySuggestion selectedCity;

    private boolean updatingCityField;

    private boolean updatingSuggestionsModel;

    private long citySearchRequestId;

    private String lastCitySearchQuery = "";

    /**
     * Creates the main application window.
     *
     * @param weatherService application weather service
     * @param alertService weather alert service
     */
    public WeatherAppFrame(
            WeatherService weatherService,
            WeatherAlertService alertService) {

        this.weatherService = Objects.requireNonNull(
                weatherService,
                "Weather service must not be null."
        );

        this.alertService = Objects.requireNonNull(
                alertService,
                "Alert service must not be null."
        );

        initializeWindow();
        initializeComponents();
        registerEventHandlers();
    }

    /**
     * Configures the main window.
     */
    private void initializeWindow() {

        setTitle("Weather Application");

        setDefaultCloseOperation(
                WindowConstants.EXIT_ON_CLOSE
        );

        setMinimumSize(
                new Dimension(900, 620)
        );

        setSize(1150, 760);

        setLocationRelativeTo(null);
    }

    /**
     * Initializes the complete application layout.
     */
    private void initializeComponents() {

        JPanel rootPanel =
                new JPanel(new BorderLayout(16, 16));

        rootPanel.setBackground(BACKGROUND_COLOR);

        rootPanel.setBorder(
                new EmptyBorder(20, 20, 20, 20)
        );

        rootPanel.add(
                createHeaderPanel(),
                BorderLayout.NORTH
        );

        rootPanel.add(
                createContentPanel(),
                BorderLayout.CENTER
        );

        rootPanel.add(
                createStatusPanel(),
                BorderLayout.SOUTH
        );

        setContentPane(rootPanel);
    }

    /**
     * Creates the application heading and search area.
     */
    private JPanel createHeaderPanel() {

        JPanel header =
                new JPanel(new BorderLayout(12, 12));

        header.setOpaque(false);

        JLabel title =
                new JLabel("Weather Dashboard");

        title.setFont(
                new Font("SansSerif", Font.BOLD, 26)
        );

        title.setForeground(TEXT_COLOR);

        JLabel subtitle =
                new JLabel(
                        "Live weather information and daily forecasts"
                );

        subtitle.setForeground(Color.GRAY);

        JPanel heading = new JPanel();

        heading.setOpaque(false);

        heading.setLayout(
                new BoxLayout(heading, BoxLayout.Y_AXIS)
        );

        heading.add(title);

        heading.add(Box.createVerticalStrut(5));

        heading.add(subtitle);

        configureCitySuggestions();

        cityField.setFont(
                new Font("SansSerif", Font.PLAIN, 14)
        );

        cityField.setToolTipText(
                "Enter at least two characters to search"
        );

        currentWeatherButton.setBackground(PRIMARY_COLOR);

        currentWeatherButton.setForeground(Color.WHITE);

        currentWeatherButton.setFocusPainted(false);

        forecastButton.setFocusPainted(false);

        JPanel searchPanel =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.LEFT,
                                8,
                                5
                        )
                );

        searchPanel.setOpaque(false);

        searchPanel.add(new JLabel("City:"));

        searchPanel.add(cityField);

        searchPanel.add(new JLabel("Suggestions:"));

        searchPanel.add(citySuggestions);

        searchPanel.add(currentWeatherButton);

        searchPanel.add(forecastButton);

        header.add(
                heading,
                BorderLayout.NORTH
        );

        header.add(
                searchPanel,
                BorderLayout.SOUTH
        );

        return header;
    }

    /**
     * Configures the searchable city suggestions dropdown.
     */
    private void configureCitySuggestions() {

        citySuggestions.setEditable(false);

        citySuggestions.setMaximumRowCount(5);

        citySuggestions.setPrototypeDisplayValue(
                new CitySuggestion(
                        "Example City",
                        0.0,
                        0.0,
                        "XX",
                        "Example State"
                )
        );

        citySuggestions.setToolTipText(
                "Select a matching city"
        );

        citySuggestions.setRenderer(
                new DefaultListCellRenderer() {

                    private static final long serialVersionUID = 1L;

                    @Override
                    public Component getListCellRendererComponent(
                            JList<?> list,
                            Object value,
                            int index,
                            boolean selected,
                            boolean focused) {

                        JLabel label = (JLabel)
                                super.getListCellRendererComponent(
                                        list,
                                        value,
                                        index,
                                        selected,
                                        focused
                                );

                        if (value instanceof CitySuggestion city) {
                            label.setText(city.displayName());
                        }

                        return label;
                    }
                }
        );
    }

    /**
     * Creates all four application tabs.
     */
    private JTabbedPane createContentPanel() {

        JTabbedPane tabs = new JTabbedPane();

        tabs.setFont(
                new Font("SansSerif", Font.PLAIN, 14)
        );

        tabs.addTab(
                "Current Weather",
                createCurrentWeatherPanel()
        );

        tabs.addTab(
                "Daily Forecast",
                createForecastPanel()
        );

        tabs.addTab(
                "Weather Alerts",
                createAlertsPanel()
        );

        tabs.addTab(
                "Performance",
                performancePanel
        );

        return tabs;
    }

    /**
     * Creates the current weather dashboard.
     */
    private JPanel createCurrentWeatherPanel() {

        JPanel panel =
                new JPanel(new BorderLayout(12, 16));

        panel.setBackground(BACKGROUND_COLOR);

        panel.setBorder(
                new EmptyBorder(16, 8, 8, 8)
        );

        locationLabel.setFont(
                new Font("SansSerif", Font.BOLD, 23)
        );

        locationLabel.setForeground(TEXT_COLOR);

        JPanel metrics =
                new JPanel(new GridLayout(3, 3, 12, 12));

        metrics.setOpaque(false);

        metrics.add(
                createMetricCard(
                        "Temperature",
                        temperatureValue
                )
        );

        metrics.add(
                createMetricCard(
                        "Feels Like",
                        feelsLikeValue
                )
        );

        metrics.add(
                createMetricCard(
                        "Conditions",
                        conditionValue
                )
        );

        metrics.add(
                createMetricCard(
                        "Humidity",
                        humidityValue
                )
        );

        metrics.add(
                createMetricCard(
                        "Wind Speed",
                        windValue
                )
        );

        metrics.add(
                createMetricCard(
                        "Pressure",
                        pressureValue
                )
        );

        metrics.add(
                createMetricCard(
                        "Visibility",
                        visibilityValue
                )
        );

        metrics.add(
                createMetricCard(
                        "Sunrise",
                        sunriseValue
                )
        );

        metrics.add(
                createMetricCard(
                        "Sunset",
                        sunsetValue
                )
        );

        panel.add(
                locationLabel,
                BorderLayout.NORTH
        );

        panel.add(
                metrics,
                BorderLayout.CENTER
        );

        return panel;
    }

    /**
     * Creates a reusable weather metric card.
     */
    private JPanel createMetricCard(
            String title,
            JLabel valueLabel) {

        JPanel card =
                new JPanel(new BorderLayout(5, 12));

        card.setBackground(CARD_COLOR);

        card.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                new Color(225, 230, 237)
                        ),
                        new EmptyBorder(14, 16, 14, 16)
                )
        );

        JLabel titleLabel = new JLabel(title);

        titleLabel.setForeground(Color.GRAY);

        titleLabel.setFont(
                new Font("SansSerif", Font.PLAIN, 13)
        );

        valueLabel.setForeground(TEXT_COLOR);

        valueLabel.setFont(
                new Font("SansSerif", Font.BOLD, 21)
        );

        card.add(
                titleLabel,
                BorderLayout.NORTH
        );

        card.add(
                valueLabel,
                BorderLayout.CENTER
        );

        return card;
    }

    /**
     * Creates the daily forecast summary table.
     */
    private JPanel createForecastPanel() {

        JPanel panel =
                new JPanel(new BorderLayout(10, 10));

        panel.setBackground(BACKGROUND_COLOR);

        panel.setBorder(
                new EmptyBorder(15, 10, 10, 10)
        );

        JLabel heading =
                new JLabel("Daily Weather Forecast");

        heading.setFont(
                new Font("SansSerif", Font.BOLD, 18)
        );

        JTable forecastTable =
                new JTable(forecastTableModel);

        forecastTable.setRowHeight(38);

        forecastTable.setFillsViewportHeight(true);

        forecastTable.setAutoCreateRowSorter(true);

        forecastTable.getTableHeader()
                .setReorderingAllowed(false);

        JScrollPane scrollPane =
                new JScrollPane(forecastTable);

        panel.add(
                heading,
                BorderLayout.NORTH
        );

        panel.add(
                scrollPane,
                BorderLayout.CENTER
        );

        return panel;
    }

    /**
     * Creates the weather alert panel.
     */
    private JPanel createAlertsPanel() {

        JPanel panel =
                new JPanel(new BorderLayout());

        panel.setBackground(BACKGROUND_COLOR);

        panel.setBorder(
                new EmptyBorder(15, 10, 10, 10)
        );

        alertsArea.setEditable(false);

        alertsArea.setLineWrap(true);

        alertsArea.setWrapStyleWord(true);

        alertsArea.setFont(
                new Font("SansSerif", Font.PLAIN, 14)
        );

        alertsArea.setText(
                "No weather information available yet."
        );

        panel.add(
                new JScrollPane(alertsArea),
                BorderLayout.CENTER
        );

        return panel;
    }

    /**
     * Creates the application status bar.
     */
    private JPanel createStatusPanel() {

        JPanel panel =
                new JPanel(new BorderLayout());

        panel.setOpaque(false);

        statusLabel.setForeground(Color.DARK_GRAY);

        panel.setBorder(
                new EmptyBorder(5, 0, 0, 0)
        );

        panel.add(
                statusLabel,
                BorderLayout.WEST
        );

        return panel;
    }

    /**
     * Registers GUI event handlers.
     */
    private void registerEventHandlers() {

        currentWeatherButton.addActionListener(
                event -> searchCurrentWeather()
        );

        forecastButton.addActionListener(
                event -> searchForecast()
        );

        cityField.addActionListener(
                event -> searchCurrentWeather()
        );

        cityField.getDocument().addDocumentListener(
                new DocumentListener() {

                    @Override
                    public void insertUpdate(DocumentEvent event) {
                        handleCityFieldChange();
                    }

                    @Override
                    public void removeUpdate(DocumentEvent event) {
                        handleCityFieldChange();
                    }

                    @Override
                    public void changedUpdate(DocumentEvent event) {
                        handleCityFieldChange();
                    }
                }
        );

        citySuggestions.addItemListener(event -> {

            if (updatingSuggestionsModel) {
                return;
            }

            if (event.getStateChange() == ItemEvent.SELECTED
                    && event.getItem() instanceof CitySuggestion city) {

                selectCitySuggestion(city);
            }
        });
    }

    /**
     * Handles user changes to the city search field.
     */
    private void handleCityFieldChange() {

        if (updatingCityField) {
            return;
        }

        selectedCity = null;

        citySearchRequestId++;

        citySearchTimer.restart();
    }

    /**
     * Searches for matching cities in a background thread.
     */
    private void searchCitySuggestions() {

        String query = cityField.getText().trim();

        if (query.length() < MIN_SEARCH_LENGTH) {

            lastCitySearchQuery = "";

            updateSuggestionModel(List.of());

            return;
        }

        if (query.equalsIgnoreCase(lastCitySearchQuery)) {
            return;
        }

        lastCitySearchQuery = query;

        long requestId = ++citySearchRequestId;

        new SwingWorker<List<CitySuggestion>, Void>() {

            @Override
            protected List<CitySuggestion> doInBackground()
                    throws Exception {

                return weatherService.searchCities(query);
            }

            @Override
            protected void done() {

                try {

                    List<CitySuggestion> suggestions = get();

                    if (requestId != citySearchRequestId) {
                        return;
                    }

                    updateSuggestionModel(suggestions);

                    if (!suggestions.isEmpty()
                            && cityField.isFocusOwner()
                            && cityField.isEnabled()) {

                        citySuggestions.showPopup();
                    }

                } catch (InterruptedException exception) {

                    Thread.currentThread().interrupt();

                } catch (ExecutionException exception) {

                    if (requestId == citySearchRequestId) {

                        statusLabel.setText(
                                "City suggestions unavailable."
                        );
                    }

                } catch (CancellationException exception) {

                    // No interface update is required.

                } finally {

                    refreshPerformanceMetrics();
                }
            }

        }.execute();
    }

    /**
     * Safely replaces the city suggestion dropdown model.
     */
    private void updateSuggestionModel(
            List<CitySuggestion> suggestions) {

        updatingSuggestionsModel = true;

        try {

            DefaultComboBoxModel<CitySuggestion> model =
                    new DefaultComboBoxModel<>();

            for (CitySuggestion city : suggestions) {
                model.addElement(city);
            }

            citySuggestions.setModel(model);

            citySuggestions.setSelectedIndex(-1);

            if (suggestions.isEmpty()
                    && citySuggestions.isPopupVisible()) {

                citySuggestions.hidePopup();
            }

        } finally {

            updatingSuggestionsModel = false;
        }
    }

    /**
     * Applies a selected geographical suggestion.
     */
    private void selectCitySuggestion(CitySuggestion city) {

        selectedCity = city;

        citySearchTimer.stop();

        updatingCityField = true;

        try {

            cityField.setText(city.displayName());

        } finally {

            updatingCityField = false;
        }

        citySuggestions.hidePopup();

        cityField.requestFocusInWindow();
    }

    /**
     * Stops pending suggestion searches before weather retrieval.
     */
    private void prepareWeatherSearch() {

        citySearchTimer.stop();

        citySearchRequestId++;
    }

    /**
     * Validates and returns the selected city query.
     */
    private String getValidatedCity() {

        if (selectedCity != null) {

            String country = selectedCity.country();

            if (country != null && !country.isBlank()) {

                return selectedCity.name() + "," + country;
            }

            return selectedCity.name();
        }

        String city = cityField.getText().trim();

        if (city.isBlank()) {

            statusLabel.setText(
                    "Please enter a city name."
            );

            cityField.requestFocusInWindow();

            return null;
        }

        if (city.length() > 100) {

            statusLabel.setText(
                    "City name is too long."
            );

            return null;
        }

        return city;
    }

    /**
     * Starts current weather retrieval.
     */
    private void searchCurrentWeather() {

        prepareWeatherSearch();

        String city = getValidatedCity();

        if (city == null) {
            return;
        }

        runInBackground(
                "Fetching current weather...",
                () -> weatherService.getCurrentWeather(city),
                this::displayCurrentWeather
        );
    }

    /**
     * Starts daily forecast retrieval.
     */
    private void searchForecast() {

        prepareWeatherSearch();

        String city = getValidatedCity();

        if (city == null) {
            return;
        }

        runInBackground(
                "Calculating daily forecast...",
                () -> weatherService.getForecast(city),
                this::displayForecast
        );
    }

    /**
     * Executes network operations outside the Swing
     * Event Dispatch Thread.
     */
    private <T> void runInBackground(
            String message,
            Callable<T> operation,
            Consumer<T> onSuccess) {

        setBusy(true);

        statusLabel.setText(message);

        new SwingWorker<T, Void>() {

            @Override
            protected T doInBackground() throws Exception {

                return operation.call();
            }

            @Override
            protected void done() {

                try {

                    T result = get();

                    onSuccess.accept(result);

                    statusLabel.setText(
                            "Data retrieved successfully."
                    );

                } catch (InterruptedException exception) {

                    Thread.currentThread().interrupt();

                    statusLabel.setText(
                            "Operation interrupted."
                    );

                } catch (ExecutionException exception) {

                    handleOperationError(exception.getCause());

                } catch (CancellationException exception) {

                    statusLabel.setText(
                            "Operation cancelled."
                    );

                } catch (RuntimeException exception) {

                    handleOperationError(exception);

                } finally {

                    try {

                        refreshPerformanceMetrics();

                    } finally {

                        setBusy(false);
                    }
                }
            }

        }.execute();
    }

    /**
     * Refreshes API and cache performance information.
     */
    private void refreshPerformanceMetrics() {

        performancePanel.updateMetrics(
                weatherService.getApiPerformanceSnapshot(),
                weatherService.getCurrentWeatherCacheStatistics(),
                weatherService.getForecastCacheStatistics()
        );
    }

    /**
     * Displays current weather data.
     */
    private void displayCurrentWeather(
            CurrentWeatherResponse weather) {

        if (weather == null) {

            statusLabel.setText(
                    "No weather data received."
            );

            return;
        }

        locationLabel.setText(
                weather.name() == null
                        ? "Current Weather"
                        : weather.name()
        );

        if (weather.main() != null) {

            temperatureValue.setText(
                    WeatherFormatter.formatTemperature(
                            weather.main().temp()
                    )
            );

            feelsLikeValue.setText(
                    WeatherFormatter.formatTemperature(
                            weather.main().feelsLike()
                    )
            );

            humidityValue.setText(
                    WeatherFormatter.formatHumidity(
                            weather.main().humidity()
                    )
            );

            pressureValue.setText(
                    WeatherFormatter.formatPressure(
                            weather.main().pressure()
                    )
            );

        } else {

            temperatureValue.setText("—");
            feelsLikeValue.setText("—");
            humidityValue.setText("—");
            pressureValue.setText("—");
        }

        conditionValue.setText(
                weather.getWeatherDescription()
        );

        windValue.setText(
                weather.wind() == null
                        ? "—"
                        : WeatherFormatter.formatWindSpeed(
                                weather.wind().speed()
                        )
        );

        visibilityValue.setText(
                WeatherFormatter.formatVisibility(
                        weather.visibility()
                )
        );

        if (weather.sys() != null) {

            sunriseValue.setText(
                    WeatherFormatter.formatLocalTime(
                            weather.sys().sunrise(),
                            weather.timezone()
                    )
            );

            sunsetValue.setText(
                    WeatherFormatter.formatLocalTime(
                            weather.sys().sunset(),
                            weather.timezone()
                    )
            );

        } else {

            sunriseValue.setText("—");
            sunsetValue.setText("—");
        }

        List<WeatherAlert> alerts =
                alertService.evaluateCurrentWeather(weather);

        displayAlerts(
                "CURRENT WEATHER ALERTS",
                alerts
        );
    }

    /**
     * Converts three-hour forecast entries into daily summaries
     * and displays the aggregated results.
     */
    private void displayForecast(ForecastResponse forecast) {

        forecastTableModel.setRowCount(0);

        if (forecast == null || forecast.list().isEmpty()) {

            statusLabel.setText(
                    "No forecast data available."
            );

            return;
        }

        List<DailyForecastSummary> summaries =
                forecastSummaryService.summarize(forecast);

        if (summaries.isEmpty()) {

            statusLabel.setText(
                    "No valid daily forecast entries available."
            );

            return;
        }

        for (DailyForecastSummary summary : summaries) {

            forecastTableModel.addRow(
                    new Object[]{

                            summary.date(),

                            WeatherFormatter.formatTemperature(
                                    summary.minimumTemperatureCelsius()
                            ),

                            WeatherFormatter.formatTemperature(
                                    summary.maximumTemperatureCelsius()
                            ),

                            summary.weatherDescription(),

                            WeatherFormatter.formatHumidity(
                                    summary.averageHumidity()
                            ),

                            WeatherFormatter.formatWindSpeed(
                                    summary.maximumWindSpeedMetersPerSecond()
                            ),

                            WeatherFormatter.formatProbability(
                                    summary.maximumPrecipitationProbability()
                            ),

                            summary.forecastSampleCount()
                    }
            );
        }

        List<WeatherAlert> alerts =
                alertService.evaluateForecast(forecast);

        displayAlerts(
                "FORECAST WEATHER ALERTS",
                alerts
        );
    }

    /**
     * Displays detected weather alerts and notifications.
     */
    private void displayAlerts(
            String heading,
            List<WeatherAlert> alerts) {

        StringBuilder output = new StringBuilder();

        output.append(heading)
                .append("\n\n");

        if (alerts.isEmpty()) {

            output.append(
                    "No application-defined alerts detected."
            );

        } else {

            for (WeatherAlert alert : alerts) {

                output.append("[")
                        .append(alert.severity())
                        .append("] ")
                        .append(alert.title())
                        .append("\n");

                output.append(alert.message())
                        .append("\n");

                output.append("Expected: ")
                        .append(alert.validTime())
                        .append("\n\n");
            }
        }

        alertsArea.setText(output.toString());

        alertsArea.setCaretPosition(0);

        notificationService.showAlerts(this, alerts);
    }

    /**
     * Displays understandable application errors.
     */
    private void handleOperationError(Throwable cause) {

        if (cause instanceof WeatherApiException exception) {

            statusLabel.setText(
                    "API Error: " + exception.getMessage()
            );

        } else if (cause instanceof HttpTimeoutException) {

            statusLabel.setText(
                    "Request timed out. Please try again."
            );

        } else if (cause instanceof IOException) {

            statusLabel.setText(
                    "Network error. Check your internet connection."
            );

        } else if (cause instanceof IllegalArgumentException) {

            statusLabel.setText(
                    "Invalid city or forecast information."
            );

        } else {

            statusLabel.setText(
                    "Unexpected application error."
            );
        }
    }

    /**
     * Enables or disables interactive controls during
     * weather operations.
     */
    private void setBusy(boolean busy) {

        cityField.setEnabled(!busy);

        citySuggestions.setEnabled(!busy);

        currentWeatherButton.setEnabled(!busy);

        forecastButton.setEnabled(!busy);

        setCursor(
                busy
                        ? Cursor.getPredefinedCursor(
                                Cursor.WAIT_CURSOR
                        )
                        : Cursor.getDefaultCursor()
        );
    }

    /**
     * Releases pending search timer resources.
     */
    @Override
    public void dispose() {

        citySearchTimer.stop();

        citySearchRequestId++;

        super.dispose();
    }
}
