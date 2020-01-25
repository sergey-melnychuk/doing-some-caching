package io.github.sergey_melnychuk.cache;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class EvictionPolicyTest {

    @Test void testComposition() {
        EvictionPolicy<String> lhs = mock(EvictionPolicy.class);
        EvictionPolicy<String> rhs = mock(EvictionPolicy.class);
        when(lhs.keep("keep")).thenReturn(true);
        when(lhs.and(rhs)).thenCallRealMethod();

        EvictionPolicy<String> policy = lhs.and(rhs);

        policy.onPut("put");
        policy.onGet("get");
        policy.keep("keep");

        verify(lhs).and(rhs);

        verify(lhs).onPut("put");
        verify(lhs).onGet("get");
        verify(lhs).keep("keep");

        verify(rhs).onPut("put");
        verify(rhs).onGet("get");
        verify(rhs).keep("keep");

        verifyNoMoreInteractions(lhs, rhs);
    }

    @Test void testSize() {
        assertThat(EvictionPolicy.size(42L)).isInstanceOf(SizeBoundEvictionPolicy.class);
    }

    @Test void testLRU() {
        assertThat(EvictionPolicy.lru(Duration.ofMillis(42L))).isInstanceOf(LastRecentlyUsedEvictionPolicy.class);
    }

    @Test void testTTL() {
        assertThat(EvictionPolicy.ttl(Duration.ofMillis(42L))).isInstanceOf(ExpirationEvictionPolicy.class);
    }

}
