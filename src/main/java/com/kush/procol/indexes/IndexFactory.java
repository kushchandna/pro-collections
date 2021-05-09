package com.kush.procol.indexes;

import static java.util.Comparator.naturalOrder;

import java.util.Comparator;
import java.util.function.Function;

import com.kush.procol.Index;

public class IndexFactory {

    public static <K, T> Index<K, T> createIndexWithSortedKeys(Function<T, K> keyGetter, Comparator<K> comparator) {
        return new SortedKeyBasedIndex<>(comparator, keyGetter);
    }

    public static <K extends Comparable<K>, T> Index<K, T> createIndexWithSortedKeys(Function<T, K> keyGetter) {
        return new SortedKeyBasedIndex<>(naturalOrder(), keyGetter);
    }

    public static <K, T> Index<K, T> createIndexWithHashedKeys(Function<T, K> keyGetter) {
        return new HashBasedIndex<>(keyGetter);
    }
}
