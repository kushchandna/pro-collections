package com.kush.procol.indexes.queries.sql;

import static com.kush.commons.utils.MapUtils.intersect;
import static com.kush.commons.utils.MapUtils.union;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.kush.commons.ranges.RangeSet;
import com.kush.lib.expressions.Expression;
import com.kush.lib.expressions.ExpressionEvaluator;
import com.kush.lib.expressions.ExpressionEvaluatorFactory;
import com.kush.lib.expressions.ExpressionException;
import com.kush.lib.expressions.clauses.AndExpression;
import com.kush.lib.expressions.clauses.EqualsExpression;
import com.kush.lib.expressions.clauses.FieldExpression;
import com.kush.lib.expressions.clauses.GreaterThanEqualsExpression;
import com.kush.lib.expressions.clauses.GreaterThanExpression;
import com.kush.lib.expressions.clauses.InExpression;
import com.kush.lib.expressions.clauses.LessThanEqualsExpression;
import com.kush.lib.expressions.clauses.LessThanExpression;
import com.kush.lib.expressions.clauses.OrExpression;
import com.kush.lib.expressions.commons.BinomialExpression;
import com.kush.lib.expressions.types.TypedValue;
import com.kush.lib.expressions.utils.BaseExpressionHandler;
import com.kush.lib.expressions.utils.ExpressionUtils;

class FieldRangesFinder extends BaseExpressionHandler<Map<String, RangeSet<?>>> {

    private final ExpressionEvaluatorFactory<?> evaluatorFactory;

    public FieldRangesFinder(ExpressionEvaluatorFactory<?> evaluatorFactory) {
        this.evaluatorFactory = evaluatorFactory;
    }

    @Override
    protected Map<String, RangeSet<?>> handle(AndExpression expression) throws ExpressionException {
        return union(accept(expression.getLeft()), accept(expression.getRight()), (field, rangesLeft, rangesRight) -> {
            return intersectRangeSets(rangesLeft, rangesRight);
        });
    }

    @Override
    protected Map<String, RangeSet<?>> handle(OrExpression expression) throws ExpressionException {
        return intersect(accept(expression.getLeft()), accept(expression.getRight()), (field, rangesLeft, rangesRight) -> {
            return unionRangeSets(rangesLeft, rangesRight);
        });
    }

    @Override
    protected Map<String, RangeSet<?>> handle(EqualsExpression expression) throws ExpressionException {
        return processBinomialExpr(expression, RangeSetFactory::forEqualsOperation);
    }

    @Override
    protected Map<String, RangeSet<?>> handle(GreaterThanExpression expression) throws ExpressionException {
        return processBinomialExpr(expression, RangeSetFactory::forGreaterThanOperation);
    }

    @Override
    protected Map<String, RangeSet<?>> handle(GreaterThanEqualsExpression expression) throws ExpressionException {
        return processBinomialExpr(expression, RangeSetFactory::forGreaterThanEqualsOperation);
    }

    @Override
    protected Map<String, RangeSet<?>> handle(LessThanExpression expression) throws ExpressionException {
        return processBinomialExpr(expression, RangeSetFactory::forLessThanOperation);
    }

    @Override
    protected Map<String, RangeSet<?>> handle(LessThanEqualsExpression expression) throws ExpressionException {
        return processBinomialExpr(expression, RangeSetFactory::forLessThanEqualsOperation);
    }

    @Override
    protected Map<String, RangeSet<?>> handle(InExpression expression) throws ExpressionException {
        Expression target = expression.getTarget();
        if (!isField(target)) {
            return emptyMap();
        }
        List<TypedValue> inVals = new ArrayList<>(expression.getInExpressions().size());
        for (Expression inValExpr : expression.getInExpressions()) {
            if (!isConstant(inValExpr)) {
                return emptyMap();
            }
            inVals.add(constantValue(inValExpr));
        }
        return singletonMap(fieldName(target), RangeSetFactory.forInOperation(inVals));
    }

    @Override
    protected Map<String, RangeSet<?>> getDefaultValue() {
        return emptyMap();
    }

    private Map<String, RangeSet<?>> processBinomialExpr(BinomialExpression expression,
            Function<TypedValue, RangeSet<?>> rangeSetGenerator) throws ExpressionException {
        Expression left = expression.getLeft();
        Expression right = expression.getRight();
        if (isField(left) && isConstant(right)) {
            return singletonMap(fieldName(left), rangeSetGenerator.apply(constantValue(right)));
        } else if (isConstant(left) && isField(right)) {
            return singletonMap(fieldName(right), rangeSetGenerator.apply(constantValue(left)));
        } else {
            return emptyMap();
        }
    }

    private TypedValue constantValue(Expression expr) throws ExpressionException {
        ExpressionEvaluator<?> evaluator = evaluatorFactory.create(expr);
        return evaluator.getConstantValue().get();
    }

    private String fieldName(Expression expr) {
        return ((FieldExpression) expr).getFieldName();
    }

    private boolean isField(Expression expression) {
        return expression instanceof FieldExpression;
    }

    private boolean isConstant(Expression expression) throws ExpressionException {
        return ExpressionUtils.isConstant(expression, evaluatorFactory);
    }

    private <T> RangeSet<T> intersectRangeSets(RangeSet<?> rangeSet1, RangeSet<?> rangeSet2) {
        return FieldRangesFinder.<T>cast(rangeSet1).intersect(FieldRangesFinder.<T>cast(rangeSet2));
    }

    private <T> RangeSet<T> unionRangeSets(RangeSet<?> rangeSet1, RangeSet<?> rangeSet2) {
        return FieldRangesFinder.<T>cast(rangeSet1).union(FieldRangesFinder.<T>cast(rangeSet2));
    }

    @SuppressWarnings("unchecked")
    private static <T> RangeSet<T> cast(RangeSet<?> rangeSet) {
        return (RangeSet<T>) rangeSet;
    }
}
