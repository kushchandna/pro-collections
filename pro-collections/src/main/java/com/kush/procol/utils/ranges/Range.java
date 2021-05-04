package com.kush.procol.utils.ranges;

import static java.util.Objects.requireNonNull;

import java.io.Serializable;
import java.util.Objects;

import com.kush.procol.utils.NullableOptional;

public final class Range<T> implements Serializable {

    private static final long serialVersionUID = 686409278295058167L;

    private final NullableOptional<T> start;
    private final boolean isStartInclusive;
    private final NullableOptional<T> end;
    private final boolean isEndInclusive;

    private Range(Range.Builder<T> builder) {
        requireNonNull(builder.start, "start");
        requireNonNull(builder.end, "end");
        this.start = builder.start;
        this.isStartInclusive = builder.isStartInclusive;
        this.end = builder.end;
        this.isEndInclusive = builder.isEndInclusive;
    }

    public NullableOptional<T> getStart() {
        return start;
    }

    public boolean isStartInclusive() {
        return isStartInclusive;
    }

    public NullableOptional<T> getEnd() {
        return end;
    }

    public boolean isEndInclusive() {
        return isEndInclusive;
    }

    public boolean isAll() {
        return start.isAbsent() && end.isAbsent();
    }

    public boolean isPointRange() {
        if (start.isPresent() && end.isPresent() && isStartInclusive && isEndInclusive) {
            return Objects.equals(start.get(), end.get());
        }
        return false;
    }

    public static <T> Range.Builder<T> builder() {
        return new Range.Builder<>();
    }

    public static <T> Range<T> all() {
        return Range.<T>builder()
            .build();
    }

    public static <T> Range<T> onValue(T value) {
        return Range.<T>builder()
            .startingFrom(value, true)
            .endingAt(value, true)
            .build();
    }

    public static <T> Range<T> between(T start, T end) {
        return Range.<T>builder()
            .startingFrom(start, true)
            .endingAt(end, true)
            .build();
    }

    @Override
    public String toString() {
        return StringRangeAdapter.write(this);
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + start.hashCode();
        if (start.isPresent()) {
            result = prime * result + (isStartInclusive ? 1231 : 1237);
        }
        result = prime * result + end.hashCode();
        if (end.isPresent()) {
            result = prime * result + (isEndInclusive ? 1231 : 1237);
        }
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
        Range<?> other = (Range<?>) obj;
        if (!start.equals(other.start)) {
            return false;
        }
        if (start.isPresent() && isStartInclusive != other.isStartInclusive) {
            return false;
        }
        if (!end.equals(other.end)) {
            return false;
        }
        if (end.isPresent() && isEndInclusive != other.isEndInclusive) {
            return false;
        }
        return true;
    }

    public static class Builder<T> {

        private NullableOptional<T> start = NullableOptional.empty();
        private boolean isStartInclusive = false;
        private NullableOptional<T> end = NullableOptional.empty();
        private boolean isEndInclusive = false;

        private Builder() {
        }

        public Range<T> build() {
            return new Range<>(this);
        }

        public Range.Builder<T> startingFrom(T value, boolean isInclusive) {
            start = NullableOptional.of(value);
            isStartInclusive = isInclusive;
            return this;
        }

        public Range.Builder<T> endingAt(T value, boolean isInclusive) {
            end = NullableOptional.of(value);
            isEndInclusive = isInclusive;
            return this;
        }
    }
}
