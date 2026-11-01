
package com.weatherapp.gui;

import com.weatherapp.cache.ExpiringCache;
import com.weatherapp.utils.ApiPerformanceMonitor;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Locale;
import java.util.Objects;

/**
 * Displays API and cache performance statistics.
 *
 * Features:
 * - API request statistics.
 * - Response latency and data transfer.
 * - Current weather cache statistics.
 * - Forecast cache statistics.
 *
 * @version 1.0.0
 */
public final class PerformancePanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private static final Color BACKGROUND_COLOR =
            new Color(245, 247, 250);

    private static final Color CARD_COLOR = Color.WHITE;

    private static final Color TEXT_COLOR =
            new Color(31, 41, 55);

    // API performance labels.
    private final JLabel totalRequestsValue = new JLabel("0");
    private final JLabel successfulRequestsValue = new JLabel("0");
    private final JLabel failedRequestsValue = new JLabel("0");
    private final JLabel averageLatencyValue = new JLabel("0.00 ms");
    private final JLabel successRateValue = new JLabel("0.00%");
    private final JLabel responseDataValue = new JLabel("0 B");

    // Current weather cache labels.
    private final JLabel currentCacheHitsValue = new JLabel("0");
    private final JLabel currentCacheMissesValue = new JLabel("0");
    private final JLabel currentCacheHitRateValue = new JLabel("0.00%");

    // Forecast cache labels.
    private final JLabel forecastCacheHitsValue = new JLabel("0");
    private final JLabel forecastCacheMissesValue = new JLabel("0");
    private final JLabel forecastCacheHitRateValue = new JLabel("0.00%");

    public PerformancePanel() {
        initializePanel();
        initializeComponents();
    }

    /**
     * Initializes the panel appearance.
     */
    private void initializePanel() {

        setLayout(new BorderLayout(12, 12));

        setBackground(BACKGROUND_COLOR);

        setBorder(
                new EmptyBorder(20, 15, 15, 15)
        );
    }

    /**
     * Creates the complete performance dashboard.
     */
    private void initializeComponents() {

        JLabel heading = new JLabel(
                "Application Performance Dashboard"
        );

        heading.setFont(
                new Font("SansSerif", Font.BOLD, 23)
        );

        heading.setForeground(TEXT_COLOR);

        JLabel subtitle = new JLabel(
                "API performance and cache efficiency statistics"
        );

        subtitle.setForeground(Color.GRAY);

        JPanel header = new JPanel();

        header.setOpaque(false);

        header.setLayout(
                new BoxLayout(header, BoxLayout.Y_AXIS)
        );

        header.add(heading);
        header.add(Box.createVerticalStrut(6));
        header.add(subtitle);

        JPanel metricsGrid =
                new JPanel(new GridLayout(3, 4, 12, 12));

        metricsGrid.setOpaque(false);

        // API performance metrics.
        metricsGrid.add(createMetricCard(
                "Total HTTP Requests",
                totalRequestsValue
        ));

        metricsGrid.add(createMetricCard(
                "Successful Requests",
                successfulRequestsValue
        ));

        metricsGrid.add(createMetricCard(
                "Failed Requests",
                failedRequestsValue
        ));

        metricsGrid.add(createMetricCard(
                "Average Response Time",
                averageLatencyValue
        ));

        metricsGrid.add(createMetricCard(
                "API Success Rate",
                successRateValue
        ));

        metricsGrid.add(createMetricCard(
                "Response Data Received",
                responseDataValue
        ));

        // Current weather cache metrics.
        metricsGrid.add(createMetricCard(
                "Current Cache Hits",
                currentCacheHitsValue
        ));

        metricsGrid.add(createMetricCard(
                "Current Cache Misses",
                currentCacheMissesValue
        ));

        metricsGrid.add(createMetricCard(
                "Current Cache Hit Rate",
                currentCacheHitRateValue
        ));

        // Forecast cache metrics.
        metricsGrid.add(createMetricCard(
                "Forecast Cache Hits",
                forecastCacheHitsValue
        ));

        metricsGrid.add(createMetricCard(
                "Forecast Cache Misses",
                forecastCacheMissesValue
        ));

        metricsGrid.add(createMetricCard(
                "Forecast Cache Hit Rate",
                forecastCacheHitRateValue
        ));

        add(header, BorderLayout.NORTH);

        add(metricsGrid, BorderLayout.CENTER);
    }

    /**
     * Creates a reusable metric card.
     */
    private JPanel createMetricCard(
            String title,
            JLabel valueLabel) {

        JPanel card =
                new JPanel(new BorderLayout(8, 12));

        card.setBackground(CARD_COLOR);

        card.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                new Color(225, 230, 237)
                        ),
                        new EmptyBorder(14, 14, 14, 14)
                )
        );

        JLabel titleLabel = new JLabel(title);

        titleLabel.setFont(
                new Font("SansSerif", Font.PLAIN, 12)
        );

        titleLabel.setForeground(Color.GRAY);

        valueLabel.setFont(
                new Font("SansSerif", Font.BOLD, 21)
        );

        valueLabel.setForeground(TEXT_COLOR);

        card.add(titleLabel, BorderLayout.NORTH);

        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }

    /**
     * Updates the performance dashboard.
     *
     * @param apiSnapshot API performance statistics
     * @param currentCache current weather cache statistics
     * @param forecastCache forecast cache statistics
     */
    public void updateMetrics(
            ApiPerformanceMonitor.MetricsSnapshot apiSnapshot,
            ExpiringCache.CacheStatistics currentCache,
            ExpiringCache.CacheStatistics forecastCache) {

        Objects.requireNonNull(apiSnapshot);
        Objects.requireNonNull(currentCache);
        Objects.requireNonNull(forecastCache);

        updateApiMetrics(apiSnapshot);

        updateCurrentCacheMetrics(currentCache);

        updateForecastCacheMetrics(forecastCache);
    }

    /**
     * Updates HTTP performance statistics.
     */
    private void updateApiMetrics(
            ApiPerformanceMonitor.MetricsSnapshot snapshot) {

        totalRequestsValue.setText(
                String.valueOf(snapshot.totalRequests())
        );

        successfulRequestsValue.setText(
                String.valueOf(snapshot.successfulRequests())
        );

        failedRequestsValue.setText(
                String.valueOf(snapshot.failedRequests())
        );

        averageLatencyValue.setText(
                String.format(
                        Locale.ROOT,
                        "%.2f ms",
                        snapshot.averageLatencyMillis()
                )
        );

        successRateValue.setText(
                String.format(
                        Locale.ROOT,
                        "%.2f%%",
                        snapshot.successRatePercentage()
                )
        );

        responseDataValue.setText(
                formatBytes(snapshot.totalResponseBytes())
        );
    }

    /**
     * Updates current weather cache statistics.
     */
    private void updateCurrentCacheMetrics(
            ExpiringCache.CacheStatistics statistics) {

        currentCacheHitsValue.setText(
                String.valueOf(statistics.hits())
        );

        currentCacheMissesValue.setText(
                String.valueOf(statistics.misses())
        );

        currentCacheHitRateValue.setText(
                formatPercentage(statistics.hitRatePercentage())
        );
    }

    /**
     * Updates forecast cache statistics.
     */
    private void updateForecastCacheMetrics(
            ExpiringCache.CacheStatistics statistics) {

        forecastCacheHitsValue.setText(
                String.valueOf(statistics.hits())
        );

        forecastCacheMissesValue.setText(
                String.valueOf(statistics.misses())
        );

        forecastCacheHitRateValue.setText(
                formatPercentage(statistics.hitRatePercentage())
        );
    }

    /**
     * Formats a percentage value.
     */
    private String formatPercentage(double percentage) {

        return String.format(
                Locale.ROOT,
                "%.2f%%",
                percentage
        );
    }

    /**
     * Formats response size into a readable unit.
     */
    private String formatBytes(long bytes) {

        if (bytes < 1024) {
            return bytes + " B";
        }

        if (bytes < 1024L * 1024) {

            return String.format(
                    Locale.ROOT,
                    "%.2f KB",
                    bytes / 1024.0
            );
        }

        return String.format(
                Locale.ROOT,
                "%.2f MB",
                bytes / (1024.0 * 1024.0)
        );
    }
}
