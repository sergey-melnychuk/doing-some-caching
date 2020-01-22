package io.github.sergey_melnychuk.cache;

import java.util.Comparator;
import java.util.Objects;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicLong;

public class SizeBoundEvictionPolicy<T> implements EvictionPolicy<T> {

    static class Entry<T> {
        final T value;
        final long index;

        Entry(T value, long index) {
            if (value == null) {
                throw new IllegalArgumentException("value can not be null.");
            }
            this.value = value;
            this.index = index;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Entry<?> entry = (Entry<?>) o;
            return value.equals(entry.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }
    }

    private final AtomicLong counter = new AtomicLong(0L);

    private final Comparator<Entry<T>> cmp = Comparator.comparing((Entry<T> e) -> e.index);
    private final ConcurrentSkipListSet<Entry<T>> queue = new ConcurrentSkipListSet<>(cmp);

    private final long maxSize;

    public SizeBoundEvictionPolicy(long maxSize) {
        this.maxSize = maxSize;
    }

    @Override
    public boolean keep(T value) {
        if (queue.size() < maxSize) {
            return true;
        }
        if (!value.equals(queue.first().value)) {
            return true;
        } else {
            queue.remove(new Entry<>(value, 0L));
            return false;
        }
    }

    @Override
    public void onGet(T value) {
        // nothing to do
    }

    @Override
    public void onPut(T value) {
        long index = counter.incrementAndGet();
        Entry<T> entry = new Entry<>(value, index);
        queue.remove(entry);
        queue.add(entry);
    }
}
