package io.github.sergey_melnychuk.cache;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class LastRecentlyUsedEvictionPolicy<T> implements EvictionPolicy<T> {
    private final ConcurrentHashMap<T, Long> seenForValue = new ConcurrentHashMap<>();

    private final Duration ttl;
    private final Supplier<Long> clock;

    public LastRecentlyUsedEvictionPolicy(Duration ttl, Supplier<Long> clock) {
        this.ttl = ttl;
        this.clock = clock;
    }

    public LastRecentlyUsedEvictionPolicy(Duration ttl) {
        this(ttl, System::currentTimeMillis);
    }

    @Override
    public boolean keep(T value) {
        long now = clock.get();
        boolean keep = now - seenForValue.getOrDefault(value, 0L) < ttl.toMillis();
        if (!keep) {
            seenForValue.remove(value);
        }
        return keep;
    }

    @Override
    public void onGet(T value) {
        seenForValue.put(value, clock.get());
    }

    @Override
    public void onPut(T value) {
        seenForValue.put(value, clock.get());
    }
}
