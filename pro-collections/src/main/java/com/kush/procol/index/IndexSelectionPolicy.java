package com.kush.procol.index;

public interface IndexSelectionPolicy<T> {

    IndexResult<T> getResult(Iterable<IndexOption<?, T>> indexOptions);

    interface IndexOption<K, T> {

        Index<K, T> getIndex();

        Attribute getAttribute();
    }
}
