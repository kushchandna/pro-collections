package com.kush.procol.index;

import com.kush.commons.IterableResult;
import com.kush.commons.ranges.RangeSet;

public interface Index<K, T> {

    IterableResult<T> find(RangeSet<K> ranges);
}
