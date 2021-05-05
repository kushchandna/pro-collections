package com.kush.procol;

import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;

import com.kush.commons.IterableResult;
import com.kush.procol.IndexSelectionPolicy.IndexOption;

public interface IndexableCollection<T> extends Collection<T> {

    <K> void addIndex(Attribute attribute, Index<K, T> index);

    IndexResult<T> query(IndexQuery<T> query);

    static <T> Optional<IterableResult<T>> getResult(Iterator<IndexOption<T>> indexOptions,
            IndexQuery<T> query) {
        IndexSelectionPolicy<T> indexSelectionPolicy = query.getIndexSelectionPolicy();
        return indexSelectionPolicy.getResult(indexOptions, query.getRangeSetProvider());
    }
}