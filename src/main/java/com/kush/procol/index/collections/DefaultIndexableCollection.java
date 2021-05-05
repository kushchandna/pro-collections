package com.kush.procol.index.collections;

import static com.kush.procol.index.IndexSelectionPolicy.option;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import com.kush.commons.IterableResult;
import com.kush.procol.index.Attribute;
import com.kush.procol.index.Index;
import com.kush.procol.index.IndexQuery;
import com.kush.procol.index.IndexResult;
import com.kush.procol.index.IndexSelectionPolicy.IndexOption;
import com.kush.procol.index.IndexableCollection;

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
    public IndexResult<T> query(IndexQuery<T> query) {
        Iterator<IndexOption<T>> options = query.getFilteringAttributes()
            .stream()
            .map(attr -> option(attr, indexes.get(attr)))
            .filter(opt -> opt.getIndex() != null)
            .iterator();
        Optional<IterableResult<T>> result = IndexableCollection.getResult(options, query);
        return IndexResult.from(result);
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
