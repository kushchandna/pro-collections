package com.kush.procol;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Predicate;

import com.kush.commons.ranges.RangeSet;
import com.kush.procol.indexes.policies.MostSelectiveIndexPolicy;

public interface IndexQuery<T> {

    Collection<Attribute> getFilteringAttributes();

    RangeSetProvider getRangeSetProvider();

    default IndexSelectionPolicy<T> getIndexSelectionPolicy() {
        return new MostSelectiveIndexPolicy<>();
    }

    default boolean skipFallbackFilter() {
        return false;
    }

    default Predicate<T> fallbackFilter() {
        return obj -> true;
    }

    public interface RangeSetProvider {

        <K> Optional<RangeSet<K>> getRanges(Attribute attribute);
    }
}
