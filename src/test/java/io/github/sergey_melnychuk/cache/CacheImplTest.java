package io.github.sergey_melnychuk.cache;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class CacheImplTest {

    private CacheImpl<String, String> makeCache(EvictionPolicy<String> policy) {
        return new CacheImpl<>(policy);
    }

    @Test void testEmpty() {
        EvictionPolicy<String> policy = mock(EvictionPolicy.class);
        CacheImpl<String, String> cache = makeCache(policy);
        assertThat(cache.cache).isEmpty();
        verifyZeroInteractions(policy);
    }

    @Test void testNullPolicy() {
        assertThatThrownBy(() -> makeCache(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cache.evictionPolicy can not be null.");
    }

    @Test void testPut() {
        EvictionPolicy<String> policy = mock(EvictionPolicy.class);
        CacheImpl<String, String> cache = makeCache(policy);
        cache.put("key", "value");

        assertThat(cache.cache).containsExactly(entry("key", "value"));
        verify(policy).onPut("key");
        verifyNoMoreInteractions(policy);
    }

    @Test void testGetMissing() {
        EvictionPolicy<String> policy = mock(EvictionPolicy.class);
        CacheImpl<String, String> cache = makeCache(policy);
        Optional<String> opt = cache.get("key");

        assertThat(opt).isEmpty();
        assertThat(cache.cache).isEmpty();
        verifyNoMoreInteractions(policy);
    }

    @Test void testGetExisting() {
        EvictionPolicy<String> policy = mock(EvictionPolicy.class);
        when(policy.keep("key")).thenReturn(true);

        CacheImpl<String, String> cache = makeCache(policy);
        cache.put("key", "value");
        Optional<String> opt = cache.get("key");

        assertThat(opt).contains("value");
        assertThat(cache.cache).containsExactly(entry("key", "value"));
        verify(policy).keep("key");
        verify(policy).onPut("key");
        verify(policy).onGet("key");
        verifyNoMoreInteractions(policy);
    }

    @Test void testGetExpired() {
        EvictionPolicy<String> policy = mock(EvictionPolicy.class);
        when(policy.keep("key")).thenReturn(false);

        CacheImpl<String, String> cache = makeCache(policy);
        cache.put("key", "value");
        Optional<String> opt = cache.get("key");

        assertThat(opt).isEmpty();
        assertThat(cache.cache).isEmpty();
        verify(policy).keep("key");
        verify(policy).onPut("key");
        verifyNoMoreInteractions(policy);
    }

    @Test void testEviction() {
        EvictionPolicy<String> policy = mock(EvictionPolicy.class);
        when(policy.keep("keep")).thenReturn(true);
        when(policy.keep("drop")).thenReturn(false);

        CacheImpl<String, String> cache = makeCache(policy);
        cache.put("keep", "value");
        cache.put("drop", "value");

        assertThat(cache.evict()).containsExactly("drop");
        verify(policy).keep("keep");
        verify(policy).onPut("keep");
        verify(policy).keep("drop");
        verify(policy).onPut("drop");
        verifyNoMoreInteractions(policy);
    }
}
