package io.github.sergey_melnychuk.cache;

import io.github.sergey_melnychuk.cache.utils.ConstClock;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

class ExpirationEvictionPolicyTest {
    private static ConstClock clock = new ConstClock();

    private static ExpirationEvictionPolicy<String> makePolicy(long millis) {
        return new ExpirationEvictionPolicy<>(Duration.ofMillis(millis), clock);
    }

    @Test void testEmpty() {
        ExpirationEvictionPolicy<String> policy = makePolicy(10);
        assertThat(policy.timeForEntry).isEmpty();
        assertThat(policy.keep("missing")).isFalse();
    }

    @Test void testPutOne() {
        final long now = 42L;
        clock.set(now);

        ExpirationEvictionPolicy<String> policy = makePolicy(10);
        policy.onPut("hello");
        policy.onGet("hello");

        assertThat(policy.timeForEntry).containsExactly(entry("hello", now));
        assertThat(policy.keep("hello")).isTrue();
        assertThat(policy.keep("missing")).isFalse();
    }

    @Test void testGetExpired() {
        final long ttl = 10;
        long now = 42L;
        clock.set(now);

        ExpirationEvictionPolicy<String> policy = makePolicy(ttl);
        policy.onPut("hello");
        policy.onGet("hello");

        now += 2 * ttl;
        clock.set(now);
        policy.onGet("hello");

        assertThat(policy.timeForEntry).isEmpty();
        assertThat(policy.keep("hello")).isFalse();
        assertThat(policy.keep("missing")).isFalse();
    }

}
