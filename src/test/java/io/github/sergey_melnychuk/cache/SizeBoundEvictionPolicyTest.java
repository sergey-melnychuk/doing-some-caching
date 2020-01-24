package io.github.sergey_melnychuk.cache;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SizeBoundEvictionPolicyTest {

    private static SizeBoundEvictionPolicy<String> makePolicy(long size) {
        return new SizeBoundEvictionPolicy<>(size);
    }

    @Test void testEmpty() {
        SizeBoundEvictionPolicy<String> policy = makePolicy(42);
        assertThat(policy.counter.get()).isEqualTo(0);
        assertThat(policy.queue).isEmpty();
    }

    @Test void testPutOne() {
        SizeBoundEvictionPolicy<String> policy = makePolicy(1);
        policy.onPut("hello");

        assertThat(policy.counter.get()).isEqualTo(1);
        assertThat(policy.queue).containsExactly(new SizeBoundEvictionPolicy.Entry<>("hello", 1));

        assertThat(policy.keep("hello")).isTrue();
    }

    @Test void testPutExtra() {
        SizeBoundEvictionPolicy<String> policy = makePolicy(1);
        policy.onPut("hello");
        policy.onPut("hola");

        assertThat(policy.counter.get()).isEqualTo(2);
        assertThat(policy.queue).containsExactly(new SizeBoundEvictionPolicy.Entry<>("hola", 2));

        assertThat(policy.keep("hello")).isTrue();
        assertThat(policy.keep("hola")).isTrue();
    }

}
