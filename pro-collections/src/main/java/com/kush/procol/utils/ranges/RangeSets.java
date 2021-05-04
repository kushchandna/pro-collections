package com.kush.procol.utils.ranges;

import static java.util.Comparator.naturalOrder;

import java.util.Comparator;
import java.util.List;

public class RangeSets {

    private static final boolean IS_NULL_HIGH;

    static {
        boolean isNullHigh = false;
        try {
            isNullHigh = readIsNullHighProperty(isNullHigh);
        } catch (Exception e) {
        }
        IS_NULL_HIGH = isNullHigh;
    }

    private static boolean readIsNullHighProperty(boolean defaultIsNullHigh) throws Exception {
        return Boolean.parseBoolean(System.getProperty("isNullHigh", String.valueOf(defaultIsNullHigh)));
    }

    public static <T> RangeSet<T> on(Comparator<T> comparator, List<Range<T>> ranges) {
        RangeOperator<T> rangeOperator = new RangeOperator<>(comparator, IS_NULL_HIGH);
        RangeSet<T> rangeSet = RangeSet.empty(rangeOperator);
        for (Range<T> range : ranges) {
            rangeSet = rangeSet.union(RangeSet.withRange(rangeOperator, range));
        }
        return rangeSet;
    }

    public static <T extends Comparable<T>> RangeSet<T> on(List<Range<T>> ranges) {
        return RangeSets.on(naturalOrder(), ranges);
    }
}
