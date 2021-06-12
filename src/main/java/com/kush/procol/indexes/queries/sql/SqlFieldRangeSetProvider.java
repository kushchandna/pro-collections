package com.kush.procol.indexes.queries.sql;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.kush.commons.ranges.RangeSet;
import com.kush.lib.expressions.Expression;
import com.kush.lib.expressions.ExpressionEvaluatorFactory;
import com.kush.lib.expressions.ExpressionException;
import com.kush.procol.indexes.Attribute;
import com.kush.procol.indexes.IndexQuery.RangeSetProvider;

class SqlFieldRangeSetProvider implements RangeSetProvider {

    private final Map<String, RangeSet<?>> fieldRanges = new HashMap<>();

    public SqlFieldRangeSetProvider(Expression sqlExpression, ExpressionEvaluatorFactory<?> evalFactory)
            throws ExpressionException {
        fieldRanges.putAll(process(sqlExpression, evalFactory));
    }

    @Override
    public <K> Optional<RangeSet<K>> getRanges(Attribute attribute) {
        if (attribute instanceof SqlFieldAttribute) {
            RangeSet<K> ranges = getRangesFromMap((SqlFieldAttribute) attribute);
            return Optional.ofNullable(ranges);
        }
        return Optional.empty();
    }

    Collection<String> getFields() {
        return fieldRanges.keySet();
    }

    @SuppressWarnings("unchecked")
    private <K> RangeSet<K> getRangesFromMap(SqlFieldAttribute attribute) {
        return (RangeSet<K>) fieldRanges.get(attribute.getName());
    }

    private Map<String, RangeSet<?>> process(Expression sqlExpression, ExpressionEvaluatorFactory<?> evalFactory)
            throws ExpressionException {
        FieldRangesFinder finder = new FieldRangesFinder(evalFactory);
        return finder.accept(sqlExpression);
    }
}
