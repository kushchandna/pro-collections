package com.kush.procol.index;

import com.kush.procol.utils.IterableResult;
import com.kush.procol.utils.ranges.RangeSet;

public interface Index<K, T> {

    IterableResult<T> find(RangeSet<K> ranges);
}
