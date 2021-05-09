package com.kush.procol.indexes;

import java.util.function.Function;

import com.kush.commons.IterableResult;
import com.kush.commons.ranges.RangeSet;

public abstract class Index<K, T> {

    private final Function<T, K> keyGetter;

    public Index(Function<T, K> keyGetter) {
        this.keyGetter = keyGetter;
    }

    protected abstract IterableResult<T> find(RangeSet<K> ranges);

    protected abstract void addToIndexedValues(K newKey, T newObject);

    protected abstract void removeFromIndexedValues(K oldKey, T oldObject);

    protected abstract boolean areKeysEqual(K oldKey, K newKey);

    protected void onUpdate(T oldObject, T newObject) {
        K oldKey = oldObject == null ? null : keyGetter.apply(oldObject);
        K newKey = newObject == null ? null : keyGetter.apply(newObject);
        if (oldObject == newObject && areKeysEqual(oldKey, newKey)) {
            return;
        }
        if (oldObject != null) {
            removeFromIndexedValues(oldKey, oldObject);
        }
        if (newObject != null) {
            addToIndexedValues(newKey, newObject);
        }
    }

    protected final K getValue(T object) {
        return keyGetter.apply(object);
    }

    protected final Function<T, K> getKeyGetter() {
        return keyGetter;
    }
}
