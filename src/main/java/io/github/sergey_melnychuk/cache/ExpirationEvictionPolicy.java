package io.github.sergey_melnychuk.cache;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class ExpirationEvictionPolicy<T> implements EvictionPolicy<T> {
    private final ConcurrentHashMap<T, Long> timeForEntry = new ConcurrentHashMap<>();
    private final Duration ttl;
    private final Supplier<Long> clock;

    public ExpirationEvictionPolicy(Duration ttl, Supplier<Long> clock) {
        this.ttl = ttl;
        this.clock = clock;
    }

    public ExpirationEvictionPolicy(Duration ttl) {
        this(ttl, System::currentTimeMillis);
    }

    @Override
    public boolean keep(T value) {
        boolean keep = timeForEntry.getOrDefault(value, 0L) < clock.get() - ttl.toMillis();
        if (!keep) {
            timeForEntry.remove(value);
        }
        return keep;
    }

    @Override
    public void onGet(T value) {
        // nothing to do
    }

    @Override
    public void onPut(T value) {
        timeForEntry.put(value, clock.get());
    }
}
