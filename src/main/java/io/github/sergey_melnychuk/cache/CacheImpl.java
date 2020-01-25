package io.github.sergey_melnychuk.cache;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class CacheImpl<K, V> implements Cache<K, V>, Evictable<K> {
    final EvictionPolicy<K> evictionPolicy;
    final ConcurrentHashMap<K, V> cache = new ConcurrentHashMap<>();

    public CacheImpl(EvictionPolicy<K> evictionPolicy) {
        if (evictionPolicy == null) {
            throw new IllegalArgumentException("Cache.evictionPolicy can not be null.");
        }
        this.evictionPolicy = evictionPolicy;
    }

    @Override
    public Optional<V> get(K key) {
        if (!cache.containsKey(key)) {
            return Optional.empty();
        }
        if (!evictionPolicy.keep(key)) {
            cache.remove(key);
            return Optional.empty();
        }
        V value = cache.get(key);
        evictionPolicy.onGet(key);
        return Optional.of(value);
    }

    @Override
    public void put(K key, V value) {
        cache.put(key, value);
        evictionPolicy.onPut(key);
    }

    @Override
    public Set<K> evict() {
        Set<K> evicted = cache.keySet().stream()
                .filter(k -> !evictionPolicy.keep(k))
                .collect(Collectors.toSet());
        evicted.forEach(cache::remove);
        return evicted;
    }
}
