package com.kush.procol.index;

import java.util.Iterator;
import java.util.Optional;

import com.kush.commons.IterableResult;
import com.kush.commons.ranges.RangeSet;
import com.kush.procol.index.IndexQuery.RangeSetProvider;

public abstract class IndexSelectionPolicy<T> {

    protected abstract Optional<IterableResult<T>> getResult(Iterator<IndexOption<T>> indexOptions,
            RangeSetProvider rangeSetProvider);

    public interface IndexOption<T> {

        <K> Index<K, T> getIndex();

        Attribute getAttribute();
    }

    public static <T> IndexOption<T> option(Attribute attribute, Index<?, T> index) {
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

    protected final <K> Optional<IterableResult<T>> getResult(IndexOption<T> option, RangeSetProvider rangeSetProvider) {
        Index<K, T> index = option.getIndex();
        Optional<RangeSet<K>> ranges = rangeSetProvider.getRanges(option.getAttribute());
        return ranges.map(index::find);
    }

}
