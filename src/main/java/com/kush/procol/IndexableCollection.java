package com.kush.procol;

import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;

import com.kush.commons.IterableResult;
import com.kush.procol.IndexSelectionPolicy.IndexOption;

public interface IndexableCollection<T> extends Collection<T> {

    <K> void addIndex(Attribute attribute, Index<K, T> index);

    void onUpdate(T oldObject, T newObject);

    IndexResult<T> query(IndexQuery<T> query);

    static <T> Optional<IterableResult<T>> getResult(Iterator<IndexOption<T>> options, IndexQuery<T> query) {
        IndexSelectionPolicy indexSelectionPolicy = query.getIndexSelectionPolicy();
        return indexSelectionPolicy.getResult(options, query.getRangeSetProvider());
    }
}
