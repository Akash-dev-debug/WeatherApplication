
package com.weatherapp.cache;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

/**
 * Thread-safe in-memory cache with time-based expiration.
 *
 * Features:
 * - Configurable entry expiration.
 * - Maximum cache capacity.
 * - Automatic removal of expired entries.
 * - Thread-safe operations.
 * - Cache performance statistics.
 *
 * @param <K> cache key type
 * @param <V> cached value type
 */
public final class ExpiringCache<K, V> {

    private final ConcurrentHashMap<K, CacheEntry<V>> entries;

    private final LongAdder cacheHits = new LongAdder();
    private final LongAdder cacheMisses = new LongAdder();

    private final long ttlNanos;
    private final int maxEntries;

    /**
     * Creates a cache with a specified expiration duration
     * and maximum capacity.
     */
    public ExpiringCache(Duration timeToLive, int maxEntries) {

        Objects.requireNonNull(
                timeToLive,
                "Cache expiration duration must not be null."
        );

        if (timeToLive.isZero() || timeToLive.isNegative()) {
            throw new IllegalArgumentException(
                    "Cache expiration duration must be positive."
            );
        }

        if (maxEntries <= 0) {
            throw new IllegalArgumentException(
                    "Maximum cache capacity must be positive."
            );
        }

        try {
            this.ttlNanos = timeToLive.toNanos();
        } catch (ArithmeticException exception) {
            throw new IllegalArgumentException(
                    "Cache expiration duration is too large.",
                    exception
            );
        }

        if (ttlNanos <= 0 || ttlNanos > Long.MAX_VALUE / 2) {
            throw new IllegalArgumentException(
                    "Cache expiration duration is outside the supported range."
            );
        }

        this.maxEntries = maxEntries;
        this.entries = new ConcurrentHashMap<>();
    }

    /**
     * Retrieves a valid cached value.
     *
     * Every lookup is recorded as a hit or miss.
     */
    public Optional<V> get(K key) {

        Objects.requireNonNull(
                key,
                "Cache key must not be null."
        );

        CacheEntry<V> entry = entries.get(key);

        if (entry == null) {
            cacheMisses.increment();
            return Optional.empty();
        }

        if (entry.isExpired(System.nanoTime())) {

            entries.remove(key, entry);

            cacheMisses.increment();

            return Optional.empty();
        }

        cacheHits.increment();

        return Optional.of(entry.value());
    }

    /**
     * Stores a value in the cache.
     *
     * When capacity is reached, the oldest entry is evicted.
     */
    public synchronized void put(K key, V value) {

        Objects.requireNonNull(
                key,
                "Cache key must not be null."
        );

        Objects.requireNonNull(
                value,
                "Cached value must not be null."
        );

        long currentTime = System.nanoTime();

        removeExpiredEntries(currentTime);

        if (!entries.containsKey(key)
                && entries.size() >= maxEntries) {

            evictOldestEntry(currentTime);
        }

        entries.put(
                key,
                new CacheEntry<>(
                        value,
                        currentTime,
                        currentTime + ttlNanos
                )
        );
    }

    /**
     * Removes an individual cache entry.
     */
    public void invalidate(K key) {

        Objects.requireNonNull(
                key,
                "Cache key must not be null."
        );

        entries.remove(key);
    }

    /**
     * Clears all cached values.
     *
     * Performance counters are retained.
     */
    public void clear() {

        entries.clear();
    }

    /**
     * Returns the number of valid cached entries.
     */
    public int size() {

        removeExpiredEntries(System.nanoTime());

        return entries.size();
    }

    /**
     * Returns a snapshot of cache performance statistics.
     */
    public CacheStatistics getStatistics() {

        int currentEntries = size();

        long hits = cacheHits.sum();
        long misses = cacheMisses.sum();

        long totalLookups = hits + misses;

        double hitRate = totalLookups == 0
                ? 0.0
                : (hits * 100.0) / totalLookups;

        return new CacheStatistics(
                hits,
                misses,
                hitRate,
                currentEntries,
                maxEntries
        );
    }

    /**
     * Removes expired entries from the cache.
     */
    private void removeExpiredEntries(long currentTime) {

        entries.entrySet().removeIf(
                entry -> entry.getValue().isExpired(currentTime)
        );
    }

    /**
     * Evicts the oldest cache entry when capacity is reached.
     */
    private void evictOldestEntry(long currentTime) {

        Map.Entry<K, CacheEntry<V>> oldestEntry = null;

        long greatestAge = Long.MIN_VALUE;

        for (Map.Entry<K, CacheEntry<V>> entry
                : entries.entrySet()) {

            long age = currentTime
                    - entry.getValue().createdAtNanos();

            if (age > greatestAge) {

                greatestAge = age;
                oldestEntry = entry;
            }
        }

        if (oldestEntry != null) {

            entries.remove(
                    oldestEntry.getKey(),
                    oldestEntry.getValue()
            );
        }
    }

    /**
     * Immutable cache performance statistics.
     */
    public record CacheStatistics(
            long hits,
            long misses,
            double hitRatePercentage,
            int currentEntries,
            int maximumEntries
    ) {
    }

    /**
     * Immutable representation of a cached entry.
     */
    private record CacheEntry<V>(
            V value,
            long createdAtNanos,
            long expiresAtNanos
    ) {

        private boolean isExpired(long currentTime) {

            return currentTime - expiresAtNanos >= 0;
        }
    }
}
