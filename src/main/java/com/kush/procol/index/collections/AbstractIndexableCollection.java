package com.kush.procol.index.collections;

import java.util.AbstractCollection;
import java.util.stream.Stream;

import com.kush.commons.IterableResult;
import com.kush.procol.index.Attribute;
import com.kush.procol.index.Index;
import com.kush.procol.index.IndexQuery;
import com.kush.procol.index.IndexResult;
import com.kush.procol.index.IndexableCollection;

public abstract class AbstractIndexableCollection<T> extends AbstractCollection<T> implements IndexableCollection<T> {

    @Override
    public <K> void addIndex(Attribute attribute, Index<K, T> index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public IndexResult<T> query(IndexQuery<T> query) {
        Stream<T> filteredStream = stream().filter(query.filter());
        return IndexResult.from(IterableResult.on(filteredStream));
    }
}
