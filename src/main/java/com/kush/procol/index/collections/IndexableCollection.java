package com.kush.procol.index.collections;

import java.util.Collection;

import com.kush.procol.index.Attribute;
import com.kush.procol.index.Index;
import com.kush.procol.index.IndexQuery;
import com.kush.procol.index.IndexResult;
import com.kush.procol.index.policies.IndexSelectionPolicy;

public interface IndexableCollection<T> extends Collection<T> {

    <K> void addIndex(Attribute attribute, Index<K, T> index);

    void setIndexSelectionPolicy(IndexSelectionPolicy<T> policy);

    IndexResult<T> query(IndexQuery<T> query);
}
