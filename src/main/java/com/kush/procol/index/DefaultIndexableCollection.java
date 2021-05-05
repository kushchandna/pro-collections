package com.kush.procol.index;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DefaultIndexableCollection<T> extends AbstractIndexableCollection<T> {

    private final Map<Attribute, Index<?, T>> indexes = new LinkedHashMap<>();

    private final Collection<T> collection;

    public DefaultIndexableCollection(Collection<T> collection) {
        this.collection = collection;
    }

    @Override
    public <K> void addIndex(Attribute attribute, Index<K, T> index) {
        indexes.put(attribute, index);
    }

    @Override
    public void setIndexSelectionPolicy(IndexSelectionPolicy<T> policy) {
    }

    @Override
    public IndexResult<T> query(IndexQuery<T> query) {
        List<Attribute> attributes = query.getFilterAttributes();
        for (Attribute attribute : attributes) {
            Index<?, T> index = indexes.get(attribute);
            if (index != null) {
            }
        }
        return super.query(query);
    }

    @Override
    public Iterator<T> iterator() {
        return collection.iterator();
    }

    @Override
    public int size() {
        return collection.size();
    }
}
