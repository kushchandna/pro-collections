package com.kush.procol.index;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Predicate;

import com.kush.commons.ranges.RangeSet;
import com.kush.procol.index.policies.MostSelectiveIndexPolicy;

public interface IndexQuery<T> {

    Collection<Attribute> getFilteringAttributes();

    RangeSetProvider getRangeSetProvider();

    default IndexSelectionPolicy<T> getIndexSelectionPolicy() {
        return new MostSelectiveIndexPolicy<>();
    }

    default Predicate<T> filter() {
        return obj -> true;
    }

    interface RangeSetProvider {

        <K> Optional<RangeSet<K>> getRanges(Attribute attribute);
    }
}
