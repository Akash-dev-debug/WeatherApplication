
package com.weatherapp.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ApiPerformanceMonitor.
 *
 * Verifies request counting, success rate, response latency,
 * transferred data and invalid input handling.
 */
class ApiPerformanceMonitorTest {

    private ApiPerformanceMonitor monitor;

    @BeforeEach
    void setUp() {
        monitor = new ApiPerformanceMonitor();
    }

    @Test
    void shouldStartWithZeroStatistics() {

        ApiPerformanceMonitor.MetricsSnapshot snapshot =
                monitor.getSnapshot();

        assertEquals(0, snapshot.totalRequests());
        assertEquals(0, snapshot.successfulRequests());
        assertEquals(0, snapshot.failedRequests());
        assertEquals(0.0, snapshot.averageLatencyMillis());
        assertEquals(0, snapshot.totalResponseBytes());
        assertEquals(0.0, snapshot.successRatePercentage());
    }

    @Test
    void shouldRecordSuccessfulRequest() {

        monitor.recordRequest(
                500_000_000L,
                2048,
                true
        );

        ApiPerformanceMonitor.MetricsSnapshot snapshot =
                monitor.getSnapshot();

        assertEquals(1, snapshot.totalRequests());
        assertEquals(1, snapshot.successfulRequests());
        assertEquals(0, snapshot.failedRequests());
        assertEquals(500.0, snapshot.averageLatencyMillis());
        assertEquals(2048, snapshot.totalResponseBytes());
        assertEquals(100.0, snapshot.successRatePercentage());
    }

    @Test
    void shouldRecordFailedRequest() {

        monitor.recordRequest(
                200_000_000L,
                512,
                false
        );

        ApiPerformanceMonitor.MetricsSnapshot snapshot =
                monitor.getSnapshot();

        assertEquals(1, snapshot.totalRequests());
        assertEquals(0, snapshot.successfulRequests());
        assertEquals(1, snapshot.failedRequests());
        assertEquals(200.0, snapshot.averageLatencyMillis());
        assertEquals(512, snapshot.totalResponseBytes());
        assertEquals(0.0, snapshot.successRatePercentage());
    }

    @Test
    void shouldCalculateAverageLatencyAndSuccessRate() {

        monitor.recordRequest(
                400_000_000L,
                1024,
                true
        );

        monitor.recordRequest(
                600_000_000L,
                2048,
                true
        );

        monitor.recordRequest(
                200_000_000L,
                512,
                false
        );

        ApiPerformanceMonitor.MetricsSnapshot snapshot =
                monitor.getSnapshot();

        assertEquals(3, snapshot.totalRequests());

        assertEquals(2, snapshot.successfulRequests());

        assertEquals(1, snapshot.failedRequests());

        assertEquals(
                400.0,
                snapshot.averageLatencyMillis(),
                0.001
        );

        assertEquals(3584, snapshot.totalResponseBytes());

        assertEquals(
                66.6667,
                snapshot.successRatePercentage(),
                0.001
        );
    }

    @Test
    void shouldRejectNegativeRequestDuration() {

        assertThrows(
                IllegalArgumentException.class,
                () -> monitor.recordRequest(-1, 100, true)
        );

        assertEquals(
                0,
                monitor.getSnapshot().totalRequests()
        );
    }

    @Test
    void shouldRejectNegativeResponseSize() {

        assertThrows(
                IllegalArgumentException.class,
                () -> monitor.recordRequest(1000, -1, true)
        );

        assertEquals(
                0,
                monitor.getSnapshot().totalRequests()
        );
    }

    @Test
    void shouldReturnIndependentStatisticsSnapshots() {

        monitor.recordRequest(
                100_000_000L,
                100,
                true
        );

        ApiPerformanceMonitor.MetricsSnapshot firstSnapshot =
                monitor.getSnapshot();

        monitor.recordRequest(
                200_000_000L,
                200,
                true
        );

        ApiPerformanceMonitor.MetricsSnapshot secondSnapshot =
                monitor.getSnapshot();

        assertEquals(1, firstSnapshot.totalRequests());

        assertEquals(2, secondSnapshot.totalRequests());
    }
}
