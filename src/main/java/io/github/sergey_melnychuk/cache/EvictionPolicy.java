package io.github.sergey_melnychuk.cache;

import java.time.Duration;

public interface EvictionPolicy<T> {

    boolean keep(T value);

    void onGet(T value);

    void onPut(T value);

    default <U extends T> EvictionPolicy<U> and(EvictionPolicy<U> rhs) {
        EvictionPolicy<T> lhs = this;
        return new EvictionPolicy<U>() {
            @Override
            public boolean keep(U value) {
                return lhs.keep(value) && rhs.keep(value);
            }

            @Override
            public void onGet(U value) {
                lhs.onGet(value);
                rhs.onGet(value);
            }

            @Override
            public void onPut(U value) {
                lhs.onPut(value);
                rhs.onPut(value);
            }
        };
    }

    static <T> EvictionPolicy<T> ttl(Duration ttl) {
        return new ExpirationEvictionPolicy<>(ttl, System::currentTimeMillis);
    }

    static <T> EvictionPolicy<T> lru(Duration ttl) {
        return new LastRecentlyUsedEvictionPolicy<>(ttl, System::currentTimeMillis);
    }

    static <T> EvictionPolicy<T> size(long size) {
        return new SizeBoundEvictionPolicy<>(size);
    }
}
