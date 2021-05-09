package com.kush.procol.indexes.queries.sql;

import java.util.Collection;

import com.kush.commons.ranges.Range;
import com.kush.commons.ranges.RangeSet;
import com.kush.commons.ranges.RangeSets;
import com.kush.lib.expressions.types.TypedValue;

class RangeSetFactory {

    public static <T extends Comparable<T>> RangeSet<T> forEqualsOperation(TypedValue value) {
        return getRangeSet(value, (rb, val) -> rb
            .startingFrom(val, true)
            .endingAt(val, true));
    }

    public static <T extends Comparable<T>> RangeSet<T> forGreaterThanOperation(TypedValue value) {
        return getRangeSet(value, (rb, val) -> rb
            .startingFrom(val, false));
    }

    public static <T extends Comparable<T>> RangeSet<T> forGreaterThanEqualsOperation(TypedValue value) {
        return getRangeSet(value, (rb, val) -> rb
            .startingFrom(val, true));
    }

    public static <T extends Comparable<T>> RangeSet<T> forLessThanOperation(TypedValue value) {
        return getRangeSet(value, (rb, val) -> rb
            .endingAt(val, false));
    }

    public static <T extends Comparable<T>> RangeSet<T> forLessThanEqualsOperation(TypedValue value) {
        return getRangeSet(value, (rb, val) -> rb
            .endingAt(val, true));
    }

    public static <T extends Comparable<T>> RangeSet<T> forInOperation(Collection<TypedValue> values) {
        RangeSet<T> rangeSet = RangeSets.empty();
        for (TypedValue value : values) {
            rangeSet = rangeSet.union(forEqualsOperation(value));
        }
        return rangeSet;
    }

    public static <T extends Comparable<T>> RangeSet<T> forBetweenOperation(TypedValue start, TypedValue end) {
        RangeSet<T> startRangeSet = forGreaterThanEqualsOperation(start);
        RangeSet<T> endRangeSet = forLessThanEqualsOperation(start);
        return startRangeSet.intersect(endRangeSet);
    }

    private static <T extends Comparable<T>> RangeSet<T> getRangeSet(TypedValue typedValue, RangeBuilderEnricher<T> enricher) {
        Range<T> range = getRange(typedValue, enricher);
        return RangeSets.on(range);
    }

    private static <T extends Comparable<T>> Range<T> getRange(TypedValue typedValue, RangeBuilderEnricher<T> enricher) {
        T value = getValue(typedValue);
        return enricher.enrich(Range.<T>builder(), value).build();
    }

    @SuppressWarnings("unchecked")
    private static <T extends Comparable<T>> T getValue(TypedValue typedValue) {
        return (T) typedValue.getObject();
    }

    private interface RangeBuilderEnricher<T> {

        Range.Builder<T> enrich(Range.Builder<T> builder, T value);
    }
}
