
package com.weatherapp.app;

import com.weatherapp.alerts.WeatherAlertService;
import com.weatherapp.api.ApiConfiguration;
import com.weatherapp.api.WeatherApiClient;
import com.weatherapp.gui.WeatherAppFrame;
import com.weatherapp.service.WeatherService;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Application entry point.
 *
 * Initializes application services and launches the
 * Weather Application desktop interface.
 */
public final class Main {

    private static final Logger LOGGER =
            Logger.getLogger(Main.class.getName());

    private Main() {
        throw new UnsupportedOperationException(
                "Main class cannot be instantiated."
        );
    }

    /**
     * Starts the application on the Swing Event Dispatch Thread.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {

        LOGGER.info("Starting Weather Application...");

        SwingUtilities.invokeLater(Main::launchApplication);
    }

    /**
     * Initializes application dependencies and displays the GUI.
     */
    private static void launchApplication() {

        try {
            // Load and validate API credentials.
            ApiConfiguration configuration =
                    ApiConfiguration.fromEnvironment();

            // Initialize HTTP communication.
            WeatherApiClient apiClient =
                    new WeatherApiClient(configuration);

            // Initialize application services.
            WeatherService weatherService =
                    new WeatherService(apiClient);

            WeatherAlertService alertService =
                    new WeatherAlertService();

            // Create and display the desktop interface.
            WeatherAppFrame application =
                    new WeatherAppFrame(
                            weatherService,
                            alertService
                    );

            application.setVisible(true);

            LOGGER.info("Weather Application started successfully.");

        } catch (IllegalStateException exception) {

            LOGGER.log(
                    Level.SEVERE,
                    "Application configuration error.",
                    exception
            );

            JOptionPane.showMessageDialog(
                    null,
                    exception.getMessage(),
                    "Configuration Error",
                    JOptionPane.ERROR_MESSAGE
            );

        } catch (RuntimeException exception) {

            LOGGER.log(
                    Level.SEVERE,
                    "Application startup failed.",
                    exception
            );

            JOptionPane.showMessageDialog(
                    null,
                    "Unable to start the Weather Application.\n"
                            + "Please check the application configuration.",
                    "Startup Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}
