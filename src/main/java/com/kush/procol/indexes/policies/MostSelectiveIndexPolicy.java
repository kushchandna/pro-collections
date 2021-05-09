package com.kush.procol.indexes.policies;

import java.util.Iterator;
import java.util.Optional;

import com.kush.commons.IterableResult;
import com.kush.procol.indexes.IndexSelectionPolicy;
import com.kush.procol.indexes.IndexQuery.RangeSetProvider;

public class MostSelectiveIndexPolicy extends IndexSelectionPolicy {

    @Override
    public <T> Optional<IterableResult<T>> getResult(Iterator<IndexOption<T>> indexOptions, RangeSetProvider rangeSetProvider) {
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
}
