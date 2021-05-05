package com.kush.procol;

import java.util.Optional;

import com.kush.commons.IterableResult;

public interface IndexResult<T> {

    Optional<IterableResult<T>> get();

    static <T> IndexResult<T> from(IterableResult<T> result) {
        return () -> Optional.of(result);
    }

    static <T> IndexResult<T> empty() {
        return () -> Optional.empty();
    }
}
