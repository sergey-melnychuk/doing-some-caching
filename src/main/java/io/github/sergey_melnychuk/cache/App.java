package io.github.sergey_melnychuk.cache;

import java.time.Duration;
import java.util.stream.IntStream;

public class App {
    public static void main(String[] args) {
        EvictionPolicy<String> ep = EvictionPolicy.size(7).and(EvictionPolicy.ttl(Duration.ofMillis(1)));
        Cache<String, String> cache = new CacheImpl<>(ep);

        IntStream.range(0, 10).forEach(i -> {
            String key = String.valueOf(i);
            cache.put(key, "{}");
            System.out.println("insert " + key);
        });

        IntStream.range(0, 10).forEach(i -> {
            String key = String.valueOf(i);
            System.out.println("check " + key + " : " + cache.get(key).isPresent());
        });
    }
}
