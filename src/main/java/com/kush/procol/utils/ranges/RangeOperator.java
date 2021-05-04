package com.kush.procol.utils.ranges;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Comparator.nullsFirst;
import static java.util.Comparator.nullsLast;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import com.kush.procol.utils.NullableOptional;

public class RangeOperator<T> {

    private final Comparator<T> comparator;

    public RangeOperator(Comparator<T> comparator) {
        this.comparator = comparator;
    }

    public RangeOperator(Comparator<T> comparator, boolean isNullHigh) {
        this(isNullHigh ? nullsLast(comparator) : nullsFirst(comparator));
    }

    public boolean isEmpty(Range<T> range) {
        if (range.getStart().isAbsent() || range.getEnd().isAbsent()) {
            return false;
        }
        int comparision = comparator.compare(range.getStart().get(), range.getEnd().get());
        if (comparision == 0) {
            return !range.isStartInclusive() && !range.isEndInclusive();
        }
        return comparision > 0;
    }

    public boolean contains(Range<T> range, T key) {
        return locate(range, key) == 0;
    }

    public int locate(Range<T> range, T key) {
        checkRangeIsValid(range);
        if (range.isAll()) {
            return 0;
        }
        int startComparision = startComparator().compare(range.getStart(), NullableOptional.of(key));
        if (startComparision > 0) {
            return -1;
        }
        if (startComparision == 0 && !range.isStartInclusive()) {
            return -1;
        }
        int endComparision = endComparator().compare(range.getEnd(), NullableOptional.of(key));
        if (endComparision < 0) {
            return 1;
        }
        if (endComparision == 0 && !range.isEndInclusive()) {
            return 1;
        }
        return 0;
    }

    public void checkRangeIsValid(Range<T> range) {
        if (isEmpty(range)) {
            throw new IllegalArgumentException();
        }
    }

    public boolean areOverlapping(Range<T> range1, Range<T> range2) {
        return intersect(range1, range2).isPresent();
    }

    public Optional<Range<T>> intersect(Range<T> range1, Range<T> range2) {
        if (isEmpty(range1) || isEmpty(range2)) {
            return Optional.empty();
        }
        Range.Builder<T> rangeBuilder = Range.builder();

        NullableOptional<T> maxStart = max(range1.getStart(), range2.getStart(), startComparator());
        if (maxStart.isPresent()) {
            T startValue = maxStart.get();
            boolean isStartInclusive = contains(range1, startValue) && contains(range2, startValue);
            rangeBuilder = rangeBuilder.startingFrom(startValue, isStartInclusive);
        }

        NullableOptional<T> minEnd = min(range1.getEnd(), range2.getEnd(), endComparator());
        if (minEnd.isPresent()) {
            T endValue = minEnd.get();
            boolean isEndInclusive = contains(range1, endValue) && contains(range2, endValue);
            rangeBuilder = rangeBuilder.endingAt(endValue, isEndInclusive);
        }

        Range<T> intersection = rangeBuilder.build();
        return isEmpty(intersection) ? Optional.empty() : Optional.of(intersection);
    }

    public List<Range<T>> union(Range<T> range1, Range<T> range2) {
        boolean range1Empty = isEmpty(range1);
        boolean range2Empty = isEmpty(range2);
        if (range1Empty && range2Empty) {
            return emptyList();
        } else if (range1Empty) {
            return singletonList(range2);
        } else if (range2Empty) {
            return singletonList(range1);
        }

        if (areOverlapping(range1, range2)) {
            Range.Builder<T> rangeBuilder = Range.builder();

            NullableOptional<T> minStart = min(range1.getStart(), range2.getStart(), startComparator());
            if (minStart.isPresent()) {
                T startValue = minStart.get();
                boolean isStartInclusive = contains(range1, startValue) && contains(range2, startValue);
                rangeBuilder = rangeBuilder.startingFrom(startValue, isStartInclusive);
            }

            NullableOptional<T> maxEnd = max(range1.getEnd(), range2.getEnd(), endComparator());
            if (maxEnd.isPresent()) {
                T endValue = maxEnd.get();
                boolean isEndInclusive = contains(range1, endValue) && contains(range2, endValue);
                rangeBuilder = rangeBuilder.endingAt(endValue, isEndInclusive);
            }

            Range<T> union = rangeBuilder.build();
            return singletonList(union);
        }

        int startComparision = startComparator().compare(range1.getStart(), range2.getStart());
        if (startComparision > 0) {
            return asList(range2, range1);
        } else if (startComparision < 0) {
            return asList(range1, range2);
        }

        // both starts are same
        // one should be inclusive point range and other should have non-inclusive same start value
        if (range1.isStartInclusive()) {
            return asList(range1, range2);
        } else if (range2.isStartInclusive()) {
            return asList(range2, range1);
        } else {
            throw new IllegalStateException();
        }
    }

    private Comparator<NullableOptional<T>> startComparator() {
        return new RangePointComparator<>(comparator, false);
    }

    private Comparator<NullableOptional<T>> endComparator() {
        return new RangePointComparator<>(comparator, true);
    }

    private static <T> T max(T o1, T o2, Comparator<T> comparator) {
        int comparision = comparator.compare(o1, o2);
        return comparision < 0 ? o2 : o1;
    }

    private static <T> T min(T o1, T o2, Comparator<T> comparator) {
        int comparision = comparator.compare(o1, o2);
        return comparision > 0 ? o2 : o1;
    }

    private static class RangePointComparator<T> implements Comparator<NullableOptional<T>> {

        private final Comparator<T> valueComparator;
        private final boolean isEmptyHigh;

        public RangePointComparator(Comparator<T> valueComparator, boolean isEmptyHigh) {
            this.valueComparator = valueComparator;
            this.isEmptyHigh = isEmptyHigh;
        }

        @Override
        public int compare(NullableOptional<T> o1, NullableOptional<T> o2) {
            if (o1.isAbsent() && o2.isAbsent()) {
                return 0;

            } else if (o1.isAbsent()) {
                // isEmptyHigh true - o1 is larger than o2
                // isEmptyHigh false - o1 is smaller than o2
                return isEmptyHigh ? 1 : -1;

            } else if (o2.isAbsent()) {
                // isEmptyHigh true - o2 is larger than o1
                // isEmptyHigh false - o2 is smaller than o1
                return isEmptyHigh ? -1 : 1;

            } else {
                // both o1 and o2 contain a value
                return valueComparator.compare(o1.get(), o2.get());

            }
        }
    }
}
