package io.github.sergey_melnychuk.cache;

import io.github.sergey_melnychuk.cache.utils.ConstClock;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

class LastRecentlyUsedEvictionPolicyTest {
    private static ConstClock clock = new ConstClock();

    private static LastRecentlyUsedEvictionPolicy<String> makePolicy(long millis) {
        return new LastRecentlyUsedEvictionPolicy<>(Duration.ofMillis(millis), clock);
    }

    @Test void testEmpty() {
        LastRecentlyUsedEvictionPolicy<String> policy = makePolicy(10);
        policy.onGet("missing");
        assertThat(policy.seenForValue).isEmpty();
        assertThat(policy.keep("missing")).isFalse();
    }

    @Test void testPutOne() {
        final long now = 42L;
        clock.set(now);

        LastRecentlyUsedEvictionPolicy<String> policy = makePolicy(10);
        policy.onPut("hello");

        assertThat(policy.seenForValue).containsExactly(entry("hello", now));
        assertThat(policy.keep("hello")).isTrue();
    }

    @Test void testGetOne() {
        final long ttl = 20L;
        long now = 42L;
        clock.set(now);

        LastRecentlyUsedEvictionPolicy<String> policy = makePolicy(ttl);
        policy.onPut("hello");
        policy.onGet("hello");

        now += 10;
        clock.set(now);
        policy.onGet("hello");
        assertThat(policy.seenForValue).containsExactly(entry("hello", now));
        assertThat(policy.keep("hello")).isTrue();
    }

    @Test void testExpired() {
        final long ttl = 10L;
        long now = 42L;
        clock.set(now);

        LastRecentlyUsedEvictionPolicy<String> policy = makePolicy(ttl);
        policy.onPut("hello");

        now += 20;
        clock.set(now);
        policy.onGet("hello");
        assertThat(policy.seenForValue).isEmpty();
        assertThat(policy.keep("hello")).isFalse();
    }
}
