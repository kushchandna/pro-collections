package com.kush.procol.index.policies;

import java.util.Iterator;
import java.util.Optional;

import com.kush.commons.IterableResult;
import com.kush.commons.ranges.RangeSet;
import com.kush.procol.index.Index;
import com.kush.procol.index.IndexQuery.RangeSetProvider;

public class MostSelectiveIndexPolicy<T> implements IndexSelectionPolicy<T> {

    @Override
    public Optional<IterableResult<T>> getResult(Iterator<IndexOption<T>> indexOptions, RangeSetProvider rangeSetProvider) {
        long minObjects = Long.MAX_VALUE;
        IterableResult<T> bestResult = null;
        while (indexOptions.hasNext()) {
            IndexSelectionPolicy.IndexOption<T> indexOption = indexOptions.next();
            Optional<IterableResult<T>> result = getResult(indexOption, rangeSetProvider);
            if (!result.isPresent()) {
                continue;
            }
            long count = result.get().count();
            if (result != null && minObjects > count) {
                bestResult = result.get();
                minObjects = count;
            }
        }
        return Optional.ofNullable(bestResult);
    }

    private <K> Optional<IterableResult<T>> getResult(IndexOption<T> option, RangeSetProvider rangeSetProvider) {
        Index<K, T> index = option.getIndex();
        Optional<RangeSet<K>> ranges = rangeSetProvider.getRanges(option.getAttribute());
        return ranges.map(index::find);
    }
}
