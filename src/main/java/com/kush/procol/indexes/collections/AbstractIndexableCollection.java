package com.kush.procol.indexes.collections;

import static com.kush.commons.utils.ObjectUtils.executeIfTrue;

import java.util.Collection;
import java.util.Iterator;

import com.kush.procol.indexes.BaseIndexableCollection;
import com.kush.procol.indexes.Index;

/*
 * TODO this class can be replaced with more specific implementations of collections for supporting all interfaces of Sets, Lists etc
 */
public abstract class AbstractIndexableCollection<T, COL extends Collection<T>> extends BaseIndexableCollection<T> {

    protected final COL collection;

    public AbstractIndexableCollection(COL collection) {
        this.collection = collection;
    }

    @Override
    protected <K> void initializeIndex(Index<K, T> index) {
        collection.forEach(obj -> updateIndex(index, null, obj));
    }

    @Override
    public final boolean add(T e) {
        return executeIfTrue(collection.add(e), () -> onUpdate(null, e));
    }

    @Override
    @SuppressWarnings("unchecked")
    public final boolean remove(Object o) {
        return executeIfTrue(collection.remove(o), () -> onUpdate((T) o, null));
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
