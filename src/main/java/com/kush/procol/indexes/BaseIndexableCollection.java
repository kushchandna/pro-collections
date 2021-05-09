package com.kush.procol.indexes;

import static com.kush.procol.indexes.IndexSelectionPolicy.option;

import java.util.AbstractCollection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import com.kush.commons.IterableResult;
import com.kush.procol.indexes.IndexSelectionPolicy.IndexOption;

public abstract class BaseIndexableCollection<T> extends AbstractCollection<T> implements IndexableCollection<T> {

    protected final Map<Attribute, Index<?, T>> indexes = new LinkedHashMap<>();

    @Override
    public final <K> void addIndex(Attribute attribute, Index<K, T> index) {
        indexes.put(attribute, index);
        initializeIndex(index);
    }

    @Override
    public void onUpdate(T oldObject, T newObject) {
        for (Index<?, T> index : indexes.values()) {
            updateIndex(index, oldObject, newObject);
        }
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

    protected final void updateIndex(Index<?, T> index, T oldObject, T newObject) {
        index.onUpdate(oldObject, newObject);
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

    protected abstract <K> void initializeIndex(Index<K, T> index);
}
