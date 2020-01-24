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

        @Override
        public String toString() {
            return "Entry{value=" + value + ", index=" + index + '}';
        }
    }

    private static final Comparator<Entry<?>> COMPARATOR = Comparator.comparing((Entry<?> e) -> e.index);

    private final long maxSize;
    final ConcurrentSkipListSet<Entry<T>> queue = new ConcurrentSkipListSet<>(COMPARATOR);
    final AtomicLong counter = new AtomicLong(0L);

    public SizeBoundEvictionPolicy(long maxSize) {
        this.maxSize = maxSize;
    }

    @Override
    public boolean keep(T value) {
        if (value.equals(queue.first().value)) {
            return queue.size() <= maxSize;
        }
        return true;
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
        if (queue.size() > maxSize) {
            queue.pollFirst();
        }
    }
}
