
package com.weatherapp.cache;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ExpiringCache.
 */
class ExpiringCacheTest {

    @Test
    void shouldStoreAndRetrieveValue() {

        ExpiringCache<String, String> cache =
                new ExpiringCache<>(Duration.ofMinutes(1), 10);

        cache.put("Delhi", "Sunny");

        assertEquals(
                Optional.of("Sunny"),
                cache.get("Delhi")
        );
    }

    @Test
    void shouldRecordCacheHitAndMiss() {

        ExpiringCache<String, String> cache =
                new ExpiringCache<>(Duration.ofMinutes(1), 10);

        cache.put("Delhi", "Sunny");

        cache.get("Delhi");
        cache.get("Mumbai");

        ExpiringCache.CacheStatistics statistics =
                cache.getStatistics();

        assertEquals(1, statistics.hits());
        assertEquals(1, statistics.misses());
        assertEquals(50.0, statistics.hitRatePercentage(), 0.001);
    }

    @Test
    void shouldExpireCachedEntry() throws InterruptedException {

        ExpiringCache<String, String> cache =
                new ExpiringCache<>(Duration.ofMillis(100), 10);

        cache.put("Delhi", "Sunny");

        Thread.sleep(200);

        assertTrue(cache.get("Delhi").isEmpty());
    }

    @Test
    void shouldEvictOldestEntryWhenCapacityIsReached()
            throws InterruptedException {

        ExpiringCache<String, String> cache =
                new ExpiringCache<>(Duration.ofMinutes(1), 2);

        cache.put("Delhi", "Sunny");

        Thread.sleep(10);

        cache.put("Mumbai", "Cloudy");

        Thread.sleep(10);

        cache.put("Chennai", "Rainy");

        assertTrue(cache.get("Delhi").isEmpty());

        assertEquals(
                Optional.of("Cloudy"),
                cache.get("Mumbai")
        );

        assertEquals(
                Optional.of("Rainy"),
                cache.get("Chennai")
        );
    }

    @Test
    void shouldInvalidateSpecificEntry() {

        ExpiringCache<String, String> cache =
                new ExpiringCache<>(Duration.ofMinutes(1), 10);

        cache.put("Delhi", "Sunny");
        cache.put("Mumbai", "Cloudy");

        cache.invalidate("Delhi");

        assertTrue(cache.get("Delhi").isEmpty());

        assertEquals(
                Optional.of("Cloudy"),
                cache.get("Mumbai")
        );
    }

    @Test
    void shouldClearAllEntries() {

        ExpiringCache<String, String> cache =
                new ExpiringCache<>(Duration.ofMinutes(1), 10);

        cache.put("Delhi", "Sunny");
        cache.put("Mumbai", "Cloudy");

        cache.clear();

        assertEquals(0, cache.size());
    }

    @Test
    void shouldEnforceMaximumCapacity() {

        ExpiringCache<String, String> cache =
                new ExpiringCache<>(Duration.ofMinutes(1), 2);

        cache.put("Delhi", "Sunny");
        cache.put("Mumbai", "Cloudy");
        cache.put("Chennai", "Rainy");

        assertEquals(2, cache.size());

        assertEquals(
                2,
                cache.getStatistics().maximumEntries()
        );
    }
}
