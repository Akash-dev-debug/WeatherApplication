
package com.weatherapp.utils;

/**
 * Thread-safe performance monitor for API operations.
 *
 * Tracks request counts, successful and failed operations,
 * response latency and total response data transferred.
 */
public final class ApiPerformanceMonitor {

    private long totalRequests;
    private long successfulRequests;
    private long failedRequests;
    private long totalLatencyNanos;
    private long totalResponseBytes;

    /**
     * Records the result of an API request.
     *
     * @param durationNanos request duration in nanoseconds
     * @param responseBytes response size in bytes
     * @param successful whether the request completed successfully
     */
    public synchronized void recordRequest(
            long durationNanos,
            long responseBytes,
            boolean successful) {

        if (durationNanos < 0) {
            throw new IllegalArgumentException(
                    "Request duration cannot be negative."
            );
        }

        if (responseBytes < 0) {
            throw new IllegalArgumentException(
                    "Response size cannot be negative."
            );
        }

        totalRequests++;

        totalLatencyNanos += durationNanos;

        totalResponseBytes += responseBytes;

        if (successful) {
            successfulRequests++;
        } else {
            failedRequests++;
        }
    }

    /**
     * Returns an immutable snapshot of performance statistics.
     *
     * @return current performance metrics
     */
    public synchronized MetricsSnapshot getSnapshot() {

        double averageLatencyMillis = totalRequests == 0
                ? 0.0
                : totalLatencyNanos
                        / (double) totalRequests
                        / 1_000_000.0;

        double successRate = totalRequests == 0
                ? 0.0
                : successfulRequests
                        * 100.0 / totalRequests;

        return new MetricsSnapshot(
                totalRequests,
                successfulRequests,
                failedRequests,
                averageLatencyMillis,
                totalResponseBytes,
                successRate
        );
    }

    /**
     * Immutable representation of API performance statistics.
     */
    public record MetricsSnapshot(
            long totalRequests,
            long successfulRequests,
            long failedRequests,
            double averageLatencyMillis,
            long totalResponseBytes,
            double successRatePercentage
    ) {
    }
}
