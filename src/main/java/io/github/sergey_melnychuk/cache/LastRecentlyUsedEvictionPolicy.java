package io.github.sergey_melnychuk.cache;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class LastRecentlyUsedEvictionPolicy<T> implements EvictionPolicy<T> {
    private final Duration ttl;
    private final Supplier<Long> clock;

    final ConcurrentHashMap<T, Long> seenForValue = new ConcurrentHashMap<>();

    public LastRecentlyUsedEvictionPolicy(Duration ttl, Supplier<Long> clock) {
        this.ttl = ttl;
        this.clock = clock;
    }

    public LastRecentlyUsedEvictionPolicy(Duration ttl) {
        this(ttl, System::currentTimeMillis);
    }

    @Override
    public boolean keep(T value) {
        long time = seenForValue.getOrDefault(value, 0L);
        boolean keep = clock.get() - time < ttl.toMillis();
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
