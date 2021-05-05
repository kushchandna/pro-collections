package com.kush.procol.index.collections;

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
import com.kush.procol.index.IndexQuery.RangeSetProvider;
import com.kush.procol.index.policies.IndexSelectionPolicy;
import com.kush.procol.index.policies.MostSelectiveIndexPolicy;
import com.kush.procol.index.policies.IndexSelectionPolicy.IndexOption;

public class DefaultIndexableCollection<T> extends AbstractIndexableCollection<T> {

    private final Map<Attribute, Index<?, T>> indexes = new LinkedHashMap<>();

    private final Collection<T> collection;

    private IndexSelectionPolicy<T> policy;

    public DefaultIndexableCollection(Collection<T> collection) {
        this.collection = collection;
        setIndexSelectionPolicy(new MostSelectiveIndexPolicy<>());
    }

    @Override
    public <K> void addIndex(Attribute attribute, Index<K, T> index) {
        indexes.put(attribute, index);
    }

    @Override
    public void setIndexSelectionPolicy(IndexSelectionPolicy<T> policy) {
        this.policy = policy;
    }

    @Override
    public IndexResult<T> query(IndexQuery<T> query) {
        Iterator<IndexOption<T>> options = query.getFilteringAttributes()
            .stream()
            .map(attr -> IndexSelectionPolicy.option(attr, indexes.get(attr)))
            .filter(opt -> opt.getIndex() != null)
            .iterator();
        RangeSetProvider rangeSetProvider = query.getRangeSetProvider();
        Optional<IterableResult<T>> result = policy.getResult(options, rangeSetProvider);
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
