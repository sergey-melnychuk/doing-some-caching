package io.github.sergey_melnychuk.cache;

import java.util.Comparator;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicLong;

public class SizeBoundEvictionPolicy<T> implements EvictionPolicy<T> {

    static class Entry<T> {
        final T value;
        final long index;

        private Entry(T value, long index) {
            this.value = value;
            this.index = index;
        }

        static Entry<?> empty(long index) {
            return new Entry<>(null, index);
        }

        static <T> Entry<T> of(T value, long index) {
            return new Entry<>(value, index);
        }
    }

    private final long maxSize;
    final AtomicLong counter = new AtomicLong(0L);
    final ConcurrentHashMap<T, Long> indexForValue = new ConcurrentHashMap<>();
    final ConcurrentSkipListSet<Entry<T>> orderedValues =
            new ConcurrentSkipListSet<>(Comparator.comparing((Entry<?> e) -> e.index));

    public SizeBoundEvictionPolicy(long maxSize) {
        this.maxSize = maxSize;
    }

    @Override
    public boolean keep(T value) {
        return indexForValue.containsKey(value);
    }

    @Override
    public void onGet(T value) {
        // nothing to do
    }

    @Override
    public void onPut(T value) {
        long index = counter.incrementAndGet();
        long remove = Optional.ofNullable(indexForValue.put(value, index)).orElse(-1L);
        orderedValues.remove(Entry.empty(remove));
        orderedValues.add(Entry.of(value, index));
        if (orderedValues.size() > maxSize) {
            Optional.ofNullable(orderedValues.pollFirst())
                    .ifPresent(e -> indexForValue.remove(e.value));
        }
    }
}
