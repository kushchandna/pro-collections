package com.kush.procol.indexes;

import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

import com.kush.commons.IterableResult;
import com.kush.commons.NullableOptional;
import com.kush.commons.ranges.Range;
import com.kush.commons.ranges.RangeSet;
import com.kush.procol.Index;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

class HashBasedIndex<K, T> extends Index<K, T> {

    private final Map<K, Collection<T>> indexedValues;

    public HashBasedIndex(Function<T, K> keyGetter) {
        super(keyGetter);
        indexedValues = new Object2ObjectOpenHashMap<>();
    }

    @Override
    protected IterableResult<T> find(RangeSet<K> rangeSet) {
        return IterableResult.onCollections(matchingListsStream(rangeSet).collect(toList()));
    }

    private Stream<Collection<T>> matchingListsStream(RangeSet<K> rangeSet) {
        if (rangeSet.containsAllPointRanges()) {
            return findForAllPointRanges(rangeSet);
        } else {
            return scanAllKeys(rangeSet);
        }
    }

    private Stream<Collection<T>> scanAllKeys(RangeSet<K> rangeSet) {
        return indexedValues.entrySet()
            .stream()
            .filter(entry -> rangeSet.contains(entry.getKey()))
            .map(entry -> entry.getValue());
    }

    private Stream<Collection<T>> findForAllPointRanges(RangeSet<K> rangeSet) {
        return rangeSet.getRanges()
            .stream()
            .map(Range::getPointValue)
            // guaranteed to be non-empty as all are point ranges
            .map(NullableOptional::get)
            .map(indexedValues::get)
            .filter(Objects::nonNull);
    }

    @Override
    protected void addToIndexedValues(K key, T object) {
        indexedValues.computeIfAbsent(key, k -> new ObjectOpenHashSet<>()).add(object);
    }

    @Override
    protected void removeFromIndexedValues(K key, T object) {
        indexedValues.computeIfPresent(key, (k, objects) -> {
            objects.remove(object);
            return objects.isEmpty() ? null : objects;
        });
    }

    @Override
    protected boolean areKeysEqual(K oldKey, K newKey) {
        return Objects.equals(oldKey, newKey);
    }
}
