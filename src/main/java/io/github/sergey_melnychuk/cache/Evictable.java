package io.github.sergey_melnychuk.cache;

import java.util.Set;

public interface Evictable<T> {
    Set<T> evict();
}
