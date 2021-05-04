package com.kush.procol.index;

import java.util.AbstractCollection;
import java.util.stream.Stream;

import com.kush.procol.utils.IterableResult;

public abstract class AbstractIndexableCollection<T> extends AbstractCollection<T> implements IndexableCollection<T> {

    @Override
    public <K> void addIndex(Attribute attribute, Index<K, T> index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setIndexSelectionPolicy(IndexSelectionPolicy<T> policy) {
        throw new UnsupportedOperationException();
    }

    @Override
    public IndexResult<T> query(IndexQuery<T> query) {
        Stream<T> filteredStream = stream().filter(query.filter());
        return IndexResult.from(IterableResult.on(filteredStream));
    }
}
