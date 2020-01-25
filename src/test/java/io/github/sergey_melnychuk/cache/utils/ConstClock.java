package io.github.sergey_melnychuk.cache.utils;

import java.util.function.Supplier;

public class ConstClock implements Supplier<Long> {
    private long now = 0L;

    public void set(long now) {
        this.now = now;
    }

    @Override
    public Long get() {
        return now;
    }
}
