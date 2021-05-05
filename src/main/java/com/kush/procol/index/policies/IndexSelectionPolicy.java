package com.kush.procol.index.policies;

import java.util.Iterator;
import java.util.Optional;

import com.kush.commons.IterableResult;
import com.kush.procol.index.Attribute;
import com.kush.procol.index.Index;
import com.kush.procol.index.IndexQuery.RangeSetProvider;

public interface IndexSelectionPolicy<T> {

    Optional<IterableResult<T>> getResult(Iterator<IndexOption<T>> indexOptions, RangeSetProvider rangeSetProvider);

    interface IndexOption<T> {

        <K> Index<K, T> getIndex();

        Attribute getAttribute();
    }

    static <T> IndexOption<T> option(Attribute attribute, Index<?, T> index) {
        return new IndexOption<T>() {

            @Override
            @SuppressWarnings("unchecked")
            public <K> Index<K, T> getIndex() {
                return (Index<K, T>) index;
            }

            @Override
            public Attribute getAttribute() {
                return attribute;
            }
        };
    }
}
