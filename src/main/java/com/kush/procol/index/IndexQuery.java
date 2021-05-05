package com.kush.procol.index;

import java.util.function.Predicate;

import com.kush.lib.expressions.Expression;

public interface IndexQuery<T> {

    Expression getFilterExpression();

    Predicate<T> filter();
}
