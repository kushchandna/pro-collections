package com.kush.procol.index.policies;

import java.util.Iterator;
import java.util.Optional;

import com.kush.commons.IterableResult;
import com.kush.commons.ranges.RangeSet;
import com.kush.procol.index.IndexQuery.RangeSetProvider;
import com.kush.procol.index.IndexSelectionPolicy;

public class FirstApplicableIndexPolicy<T> extends IndexSelectionPolicy<T> {

    @Override
    public Optional<IterableResult<T>> getResult(Iterator<IndexOption<T>> indexOptions, RangeSetProvider rangeSetProvider) {
        while (indexOptions.hasNext()) {
            IndexSelectionPolicy.IndexOption<T> indexOption = indexOptions.next();
            Optional<RangeSet<Object>> ranges = rangeSetProvider.getRanges(indexOption.getAttribute());
            if (ranges.isPresent()) {
                return getResult(indexOption, rangeSetProvider);
            }
        }
        return Optional.empty();
    }

}
