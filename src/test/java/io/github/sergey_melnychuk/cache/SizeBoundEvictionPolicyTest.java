package io.github.sergey_melnychuk.cache;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

class SizeBoundEvictionPolicyTest {

    private static SizeBoundEvictionPolicy<String> makePolicy(long size) {
        return new SizeBoundEvictionPolicy<>(size);
    }

    @Test void testEmpty() {
        SizeBoundEvictionPolicy<String> policy = makePolicy(42);
        assertThat(policy.counter.get()).isEqualTo(0);
        assertThat(policy.indexForValue).isEmpty();
        assertThat(policy.orderedValues).isEmpty();
    }

    @Test void testPutOne() {
        SizeBoundEvictionPolicy<String> policy = makePolicy(1);
        policy.onPut("hello");

        assertThat(policy.counter.get()).isEqualTo(1);
        assertThat(policy.orderedValues).containsExactly(SizeBoundEvictionPolicy.Entry.of("hello", 1));
        assertThat(policy.indexForValue).containsExactly(entry("hello", 1L));

        assertThat(policy.keep("hello")).isTrue();
    }

    @Test void testPutExtra() {
        SizeBoundEvictionPolicy<String> policy = makePolicy(1);
        policy.onPut("hello");
        policy.onPut("hola");

        assertThat(policy.counter.get()).isEqualTo(2);
        assertThat(policy.orderedValues).containsExactly(SizeBoundEvictionPolicy.Entry.of("hola", 2));
        assertThat(policy.indexForValue).containsExactly(entry("hola", 2L));

        assertThat(policy.keep("hello")).isFalse();
        assertThat(policy.keep("hola")).isTrue();
    }

    @Test void testGetDoesNotChangeState() {
        SizeBoundEvictionPolicy<String> policy = makePolicy(1);
        policy.onPut("hello");
        policy.onGet("hello");
        policy.onGet("missing");

        assertThat(policy.counter.get()).isEqualTo(1);
        assertThat(policy.orderedValues).containsExactly(SizeBoundEvictionPolicy.Entry.of("hello", 1));
        assertThat(policy.indexForValue).containsExactly(entry("hello", 1L));

        assertThat(policy.keep("hello")).isTrue();
    }

}
