package com.kush.procol.indexes.queries.sql;

import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.function.Predicate;

import com.kush.lib.expressions.Expression;
import com.kush.lib.expressions.ExpressionEvaluator;
import com.kush.lib.expressions.ExpressionEvaluatorFactory;
import com.kush.lib.expressions.ExpressionException;
import com.kush.lib.expressions.ExpressionParser;
import com.kush.lib.expressions.types.TypedValue;
import com.kush.procol.indexes.Attribute;
import com.kush.procol.indexes.IndexQuery;
import com.kush.procol.indexes.IndexSelectionPolicy;

public class SqlIndexQueryGenerator<T> {

    private final ExpressionParser<String> sqlParser;
    private final ExpressionEvaluatorFactory<T> evalFactory;
    private final IndexSelectionPolicy indexSelectionPolicy;

    public SqlIndexQueryGenerator(ExpressionParser<String> sqlParser, ExpressionEvaluatorFactory<T> evalFactory,
            IndexSelectionPolicy indexSelectionPolicy) {
        this.sqlParser = sqlParser;
        this.evalFactory = evalFactory;
        this.indexSelectionPolicy = indexSelectionPolicy;
    }

    public IndexQuery<T> generate(String sql) throws ExpressionException {
        Expression expression = sqlParser.parse(sql);
        SqlFieldRangeSetProvider rangeSetProvider = getRangeSetProvider(expression);
        ExpressionEvaluator<T> evaluator = evalFactory.create(expression);
        return new SqlIndexQuery(rangeSetProvider, evaluator);
    }

    private SqlFieldRangeSetProvider getRangeSetProvider(Expression expression) throws ExpressionException {
        return new SqlFieldRangeSetProvider(expression, evalFactory);
    }

    private final class SqlIndexQuery implements IndexQuery<T> {

        private final Collection<Attribute> attributes;
        private final SqlFieldRangeSetProvider rangeSetProvider;
        private final ExpressionEvaluator<T> evaluator;

        private SqlIndexQuery(SqlFieldRangeSetProvider rangeSetProvider, ExpressionEvaluator<T> evaluator) {
            attributes = rangeSetProvider.getFields()
                .stream()
                .map(SqlFieldAttribute::new)
                .collect(toList());
            this.rangeSetProvider = rangeSetProvider;
            this.evaluator = evaluator;
        }

        @Override
        public Collection<Attribute> getFilteringAttributes() {
            return attributes;
        }

        @Override
        public RangeSetProvider getRangeSetProvider() {
            return rangeSetProvider;
        }

        @Override
        public Predicate<T> fallbackFilter() {
            return this::evaluate;
        }

        @Override
        public IndexSelectionPolicy getIndexSelectionPolicy() {
            return indexSelectionPolicy;
        }

        private boolean evaluate(T object) {
            try {
                TypedValue value = evaluator.evaluate(object);
                return value.getBoolean();
            } catch (ExpressionException e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
        }
    }
}
