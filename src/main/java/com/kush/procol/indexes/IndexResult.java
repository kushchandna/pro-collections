package com.kush.procol.indexes;

import java.util.Optional;
import java.util.stream.Stream;

import com.kush.commons.IterableResult;

public interface IndexResult<T> {

    Optional<IterableResult<T>> get();

    default Stream<T> stream() {
        return get().get().stream();
    }

    static <T> IndexResult<T> from(IterableResult<T> result) {
        return () -> Optional.of(result);
    }

    static <T> IndexResult<T> empty() {
        return () -> Optional.empty();
    }
}
