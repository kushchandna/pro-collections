package com.kush.procol.utils.ranges;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.kush.procol.utils.NullableOptional;

public final class RangeSet<T> implements Serializable {

    private static final long serialVersionUID = -5525123230651149267L;

    private final List<Range<T>> ranges;

    private final RangeOperator<T> rangeOperator;
    private final boolean isInvalid;

    // assumes specified lists are valid
    private RangeSet(RangeOperator<T> rangeOperator, boolean isInvalid, List<Range<T>> ranges) {
        if (isInvalid && !ranges.isEmpty()) {
            throw new IllegalStateException();
        }
        this.rangeOperator = rangeOperator;
        this.isInvalid = isInvalid;
        this.ranges = unmodifiableList(ranges);
    }

    public List<Range<T>> getRanges() {
        return unmodifiableList(ranges);
    }

    public boolean contains(T value) {
        return ranges.stream()
            .anyMatch(range -> rangeOperator.contains(range, value));
    }

    public RangeSet<T> union(RangeSet<T> rangeSet) {
        RangeSet<T> union = this;
        for (Range<T> range : rangeSet.getRanges()) {
            union = union.union(range);
        }
        return union;
    }

    public RangeSet<T> intersect(RangeSet<T> rangeSet) {
        RangeSet<T> intersection = empty(rangeOperator);
        for (Range<T> range : rangeSet.getRanges()) {
            RangeSet<T> rangeIntersection = this.intersect(range);
            intersection = intersection.union(rangeIntersection);
        }
        return intersection;
    }

    public boolean containsAllPointRanges() {
        return ranges.stream().allMatch(Range::isPointRange);
    }

    public List<RangeSet<T>> split() {
        return getRanges().stream()
            .map(range -> RangeSet.withRange(rangeOperator, range))
            .collect(toList());
    }

    public static <T> RangeSet<T> empty(RangeOperator<T> rangeOperator) {
        return new RangeSet<>(rangeOperator, false, emptyList());
    }

    public static <T> RangeSet<T> invalid(RangeOperator<T> rangeOperator) {
        return new RangeSet<>(rangeOperator, true, emptyList());
    }

    public static <T> RangeSet<T> universal(RangeOperator<T> rangeOperator) {
        return withRange(rangeOperator, Range.all());
    }

    public static <T> RangeSet<T> withRange(RangeOperator<T> rangeOperator, Range<T> range) {
        return withRanges(rangeOperator, asList(range));
    }

    // assumes specified lists are valid
    private static <T> RangeSet<T> withRanges(RangeOperator<T> rangeOperator, List<Range<T>> ranges) {
        return new RangeSet<>(rangeOperator, false, ranges);
    }

    @Override
    public String toString() {
        if (isInvalid) {
            return "RangeSet <invalid>";
        } else {
            return String.format("RangeSet %s", String.valueOf(ranges));
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (isInvalid ? 1231 : 1237);
        result = prime * result + ranges.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        RangeSet<?> other = (RangeSet<?>) obj;
        if (isInvalid != other.isInvalid) {
            return false;
        }
        if (!ranges.equals(other.ranges)) {
            return false;
        }
        return true;
    }

    private RangeSet<T> union(Range<T> range) {
        if (rangeOperator.isEmpty(range)) {
            return this;
        }
        if (range.isAll()) {
            return RangeSet.universal(rangeOperator);
        }
        if (isInvalid) {
            return RangeSet.withRange(rangeOperator, range);
        }
        return unionWithoutNormalization(range).normalize();
    }

    private RangeSet<T> unionWithoutNormalization(Range<T> range) {
        NullableOptional<T> startingPoint = range.getStart();
        boolean startInclusive = range.isStartInclusive();

        int lastRangeIndexBeforeStart = -1;
        if (startingPoint.isPresent()) {
            for (int i = 0; i < ranges.size(); i++) {
                Range<T> existingRange = ranges.get(i);
                int startPos = rangeOperator.locate(existingRange, startingPoint.get());
                if (startPos > 0) {
                    lastRangeIndexBeforeStart = i;
                    continue;
                }
                if (startPos == 0) {
                    startingPoint = existingRange.getStart();
                    startInclusive = existingRange.isStartInclusive();
                }
                break;
            }
        }

        NullableOptional<T> endingPoint = range.getEnd();
        boolean endInclusive = range.isEndInclusive();

        int firstRangeIndexAfterEnd = ranges.size();
        if (endingPoint.isPresent()) {
            for (int i = ranges.size() - 1; i >= 0; i--) {
                Range<T> existingRange = ranges.get(i);
                int endPos = rangeOperator.locate(existingRange, endingPoint.get());
                if (endPos < 0) {
                    firstRangeIndexAfterEnd = i;
                    continue;
                }
                if (endPos == 0) {
                    endingPoint = existingRange.getEnd();
                    endInclusive = existingRange.isEndInclusive();
                }
                break;
            }
        }

        List<Range<T>> newRanges = new ArrayList<>(ranges.size() + 1);

        for (int i = 0; i <= lastRangeIndexBeforeStart; i++) {
            newRanges.add(ranges.get(i));
        }

        Range.Builder<T> rangeBuilder = Range.<T>builder();
        if (startingPoint.isPresent()) {
            rangeBuilder = rangeBuilder.startingFrom(startingPoint.get(), startInclusive);
        }
        if (endingPoint.isPresent()) {
            rangeBuilder = rangeBuilder.endingAt(endingPoint.get(), endInclusive);
        }
        newRanges.add(rangeBuilder.build());

        for (int i = firstRangeIndexAfterEnd; i < ranges.size(); i++) {
            newRanges.add(ranges.get(i));
        }

        return RangeSet.withRanges(rangeOperator, newRanges);
    }

    private RangeSet<T> normalize() {
        if (ranges.size() <= 1) {
            return this;
        }
        RangeSet<T> normalized = RangeSet.empty(rangeOperator);
        for (int i = 1; i < ranges.size(); i++) {

            Range<T> lastRange = ranges.get(i - 1);
            // lastRange's end is guaranteed to exist here as end value of a non-last range can not be empty

            Range<T> currentRange = ranges.get(i);
            // currentRange's start is guaranteed to exist here as start value of a non-first range can not be empty

            if (Objects.equals(lastRange.getEnd().get(), currentRange.getStart().get())
                    && (lastRange.isEndInclusive() || currentRange.isStartInclusive())) {

                Range.Builder<T> rangeBuilder = Range.<T>builder();
                if (lastRange.getStart().isPresent()) {
                    rangeBuilder = rangeBuilder.startingFrom(lastRange.getStart().get(), lastRange.isStartInclusive());
                }
                if (currentRange.getEnd().isPresent()) {
                    rangeBuilder = rangeBuilder.endingAt(currentRange.getEnd().get(), currentRange.isEndInclusive());
                }
                Range<T> mergedRange = rangeBuilder.build();
                normalized = normalized.union(mergedRange);

            } else {
                normalized = normalized
                    .unionWithoutNormalization(lastRange)
                    .unionWithoutNormalization(currentRange);

            }
        }
        return normalized;
    }

    private RangeSet<T> intersect(Range<T> range) {
        if (isInvalid) {
            return this;
        }
        if (range.isAll()) {
            return this;
        }
        if (rangeOperator.isEmpty(range)) {
            return invalid(rangeOperator);
        }

        List<Range<T>> ranges = new ArrayList<>(getRanges().size());
        for (Range<T> currentRange : getRanges()) {
            Optional<Range<T>> intersection = rangeOperator.intersect(currentRange, range);
            if (intersection.isPresent()) {
                ranges.add(intersection.get());
            }
        }

        return RangeSet.withRanges(rangeOperator, ranges);
    }
}
