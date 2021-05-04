package com.kush.procol.index;

import java.util.Collection;

public interface IndexableCollection<T> extends Collection<T> {

    <K> void addIndex(Attribute attribute, Index<K, T> index);

    void setIndexSelectionPolicy(IndexSelectionPolicy<T> policy);

    IndexResult<T> query(IndexQuery<T> query);
}
