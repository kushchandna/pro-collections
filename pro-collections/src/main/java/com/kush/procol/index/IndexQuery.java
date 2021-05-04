package com.kush.procol.index;

import java.util.List;
import java.util.function.Predicate;

import com.kush.procol.utils.ranges.RangeSet;

public interface IndexQuery<T> {

    Predicate<T> filter();

    List<Attribute> getFilterAttributes();

    <K> RangeSet<K> getRanges(Attribute attribute);
}
