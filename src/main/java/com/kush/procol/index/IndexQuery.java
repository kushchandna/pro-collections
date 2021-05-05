package com.kush.procol.index;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Predicate;

import com.kush.commons.ranges.RangeSet;

public interface IndexQuery<T> {

    Collection<Attribute> getFilteringAttributes();

    RangeSetProvider getRangeSetProvider();

    Predicate<T> filter();

    interface RangeSetProvider {

        <K> Optional<RangeSet<K>> getRanges(Attribute attribute);
    }
}
