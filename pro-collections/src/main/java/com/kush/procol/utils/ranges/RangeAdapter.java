package com.kush.procol.utils.ranges;

public interface RangeAdapter<T, R> {

    Range<R> toRange(T value);

    T fromRange(Range<R> range);
}
