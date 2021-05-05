package com.kush.procol.support;

import java.util.AbstractCollection;
import java.util.stream.Stream;

import com.kush.commons.IterableResult;
import com.kush.procol.Attribute;
import com.kush.procol.Index;
import com.kush.procol.IndexQuery;
import com.kush.procol.IndexResult;
import com.kush.procol.IndexableCollection;

public abstract class AbstractIndexableCollection<T> extends AbstractCollection<T> implements IndexableCollection<T> {

    @Override
    public <K> void addIndex(Attribute attribute, Index<K, T> index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public IndexResult<T> query(IndexQuery<T> query) {
        if (!query.skipFallbackFilter()) {
            Stream<T> filteredStream = stream().filter(query.fallbackFilter());
            return IndexResult.from(IterableResult.on(filteredStream));
        }
        return IndexResult.empty();
    }
}
