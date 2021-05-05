package com.kush.procol;

import static com.kush.procol.IndexSelectionPolicy.option;

import java.util.AbstractCollection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import com.kush.commons.IterableResult;
import com.kush.procol.IndexSelectionPolicy.IndexOption;

public abstract class BaseIndexableCollection<T> extends AbstractCollection<T> implements IndexableCollection<T> {

    protected final Map<Attribute, Index<?, T>> indexes = new LinkedHashMap<>();

    @Override
    public final <K> void addIndex(Attribute attribute, Index<K, T> index) {
        indexes.put(attribute, index);
        getInitialSnapshot().forEach(this::add);
    }

    @Override
    public boolean add(T e) {
        return update(null, e);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean remove(Object o) {
        return update((T) o, null);
    }

    @Override
    public void onUpdate(T oldObject, T newObject) {
        update(oldObject, newObject);
    }

    @Override
    public IndexResult<T> query(IndexQuery<T> query) {
        Optional<IterableResult<T>> result = executeQueryOnIndexes(query);
        if (result.isPresent()) {
            return IndexResult.from(result.get());
        }
        if (!query.skipFallbackFilter()) {
            return performFallbackQuery(query);
        }
        return IndexResult.empty();
    }

    private Optional<IterableResult<T>> executeQueryOnIndexes(IndexQuery<T> query) {
        Iterator<IndexOption<T>> options = query.getFilteringAttributes()
            .stream()
            .map(attr -> option(attr, indexes.get(attr)))
            .filter(opt -> opt.getIndex() != null)
            .iterator();
        return IndexableCollection.getResult(options, query);
    }

    private IndexResult<T> performFallbackQuery(IndexQuery<T> query) {
        Stream<T> filteredStream = stream().filter(query.fallbackFilter());
        return IndexResult.from(IterableResult.onStream(filteredStream));
    }

    private boolean update(T oldObject, T newObject) {
        boolean changed = false;
        for (Index<?, T> index : indexes.values()) {
            boolean changePerformed = index.onUpdate(oldObject, newObject);
            changed = changed || changePerformed;
        }
        return changed;
    }

    protected abstract Stream<T> getInitialSnapshot();
}
