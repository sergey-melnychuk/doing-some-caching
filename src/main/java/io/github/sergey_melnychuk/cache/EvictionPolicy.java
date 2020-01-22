package io.github.sergey_melnychuk.cache;

public interface EvictionPolicy<T> {

    boolean keep(T value);

    void onGet(T value);

    void onPut(T value);

    default <U extends T> EvictionPolicy<U> chain(EvictionPolicy<U> rhs) {
        EvictionPolicy<T> lhs = this;
        return new EvictionPolicy<U>() {
            @Override
            public boolean keep(U value) {
                return lhs.keep(value) && rhs.keep(value);
            }

            @Override
            public void onGet(U value) {
                lhs.onGet(value);
                rhs.onGet(value);
            }

            @Override
            public void onPut(U value) {
                lhs.onGet(value);
                rhs.onGet(value);
            }
        };
    }
}
