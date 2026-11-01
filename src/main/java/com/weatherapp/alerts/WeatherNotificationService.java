
package com.weatherapp.alerts;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.Component;
import java.util.List;
import java.util.Objects;

/**
 * Displays temporary desktop notifications for weather alerts.
 *
 * Notifications are non-blocking and automatically dismissed
 * after ten seconds.
 */
public final class WeatherNotificationService {

    private static final int NOTIFICATION_DURATION_MS = 10_000;
    private static final int MAX_DISPLAYED_ALERTS = 4;

    private JDialog activeDialog;
    private Timer autoCloseTimer;

    /**
     * Displays notifications for detected weather alerts.
     *
     * This method is safe to call from any thread.
     *
     * @param parent parent component for positioning
     * @param alerts detected weather alerts
     */
    public void showAlerts(
            Component parent,
            List<WeatherAlert> alerts) {

        Objects.requireNonNull(alerts, "Alerts must not be null.");

        if (alerts.isEmpty()) {
            return;
        }

        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(
                    () -> showAlerts(parent, alerts)
            );
            return;
        }

        String message = buildNotificationMessage(alerts);

        int messageType = alerts.stream().anyMatch(
                alert -> alert.severity() == WeatherAlert.Severity.SEVERE
                        || alert.severity() == WeatherAlert.Severity.HIGH
        )
                ? JOptionPane.WARNING_MESSAGE
                : JOptionPane.INFORMATION_MESSAGE;

        closePreviousNotification();

        JOptionPane optionPane = new JOptionPane(
                message,
                messageType
        );

        activeDialog = optionPane.createDialog(
                parent,
                "Weather Alert Notification"
        );

        activeDialog.setModal(false);
        activeDialog.setDefaultCloseOperation(
                JDialog.DISPOSE_ON_CLOSE
        );

        activeDialog.setLocationRelativeTo(parent);

        autoCloseTimer = new Timer(
                NOTIFICATION_DURATION_MS,
                event -> closePreviousNotification()
        );

        autoCloseTimer.setRepeats(false);
        autoCloseTimer.start();

        activeDialog.setVisible(true);
    }

    /**
     * Builds a readable notification message.
     */
    private String buildNotificationMessage(
            List<WeatherAlert> alerts) {

        StringBuilder message = new StringBuilder();

        message.append("Weather conditions require your attention.\n\n");

        int displayedCount = Math.min(
                alerts.size(),
                MAX_DISPLAYED_ALERTS
        );

        for (int index = 0; index < displayedCount; index++) {

            WeatherAlert alert = alerts.get(index);

            message.append("[")
                    .append(alert.severity())
                    .append("] ")
                    .append(alert.title())
                    .append("\n");

            message.append(alert.message())
                    .append("\n\n");
        }

        int remaining = alerts.size() - displayedCount;

        if (remaining > 0) {
            message.append("And ")
                    .append(remaining)
                    .append(" additional alert(s).");
        }

        return message.toString();
    }

    /**
     * Closes the previous notification and stops its timer.
     */
    private void closePreviousNotification() {

        if (autoCloseTimer != null) {
            autoCloseTimer.stop();
            autoCloseTimer = null;
        }

        if (activeDialog != null) {
            activeDialog.dispose();
            activeDialog = null;
        }
    }
}
