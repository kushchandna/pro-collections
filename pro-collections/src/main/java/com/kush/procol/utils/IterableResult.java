package com.kush.procol.utils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.stream.Stream;

public interface IterableResult<T> extends Iterable<T> {

    Stream<T> stream();

    default long count() {
        return -1L;
    }

    default boolean isCountKnown() {
        return count() >= 0L;
    }

    default Stream<T> parallelStream() {
        return stream();
    }

    @Override
    default Iterator<T> iterator() {
        return stream().iterator();
    }

    default IterableResult<T> concat(IterableResult<T> result) {
        long count = -1L;
        if (isCountKnown() && result.isCountKnown()) {
            count = count() + result.count();
        }
        return new DefaultIterableResult<>(Stream.concat(stream(), result.stream()), count);
    }

    static <T> IterableResult<T> empty() {
        return new DefaultIterableResult<>(Stream.empty(), 0L);
    }

    static <T> IterableResult<T> on(Stream<T> stream) {
        return () -> stream;
    }

    static <T> IterableResult<T> on(Collection<T> collection) {
        return new DefaultIterableResult<T>(collection.stream(), collection.size()) {

            @Override
            public Stream<T> parallelStream() {
                return collection.parallelStream();
            }

            @Override
            public Iterator<T> iterator() {
                return collection.iterator();
            }
        };
    }

    @SafeVarargs
    static <T> IterableResult<T> on(T... values) {
        return new DefaultIterableResult<>(Arrays.stream(values), values.length);
    }

    static class DefaultIterableResult<T> implements IterableResult<T> {

        private final Stream<T> stream;
        private final long count;

        private DefaultIterableResult(Stream<T> stream, long count) {
            this.stream = stream;
            this.count = count;
        }

        @Override
        public Stream<T> stream() {
            return stream;
        }

        @Override
        public long count() {
            return count;
        }
    }
}
