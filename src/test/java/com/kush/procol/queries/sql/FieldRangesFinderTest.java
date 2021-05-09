package com.kush.procol.queries.sql;

import static com.kush.commons.ranges.StringRangeAdapter.parseString;
import static com.kush.commons.utils.MapUtils.newHashMap;
import static com.kush.lib.expressions.types.Type.STRING;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasSize;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.kush.commons.ranges.Range;
import com.kush.commons.ranges.RangeSet;
import com.kush.commons.ranges.RangeSets;
import com.kush.lib.expressions.Expression;
import com.kush.lib.expressions.ExpressionEvaluatorFactory;
import com.kush.lib.expressions.ExpressionFactory;
import com.kush.lib.expressions.aspect.Aspect;
import com.kush.lib.expressions.aspect.AspectFieldEvaluationFactory;
import com.kush.lib.expressions.aspect.Aspects;
import com.kush.lib.expressions.evaluators.DefaultExpressionEvaluatorFactory;
import com.kush.lib.expressions.evaluators.FieldExpressionEvaluatorFactory;
import com.kush.lib.expressions.factory.DefaultExpressionFactory;
import com.kush.lib.expressions.parsers.sql.SqlExpressionParser;

public class FieldRangesFinderTest {

    private static Aspect<?> aspect;

    private FieldRangesFinder finder;

    @BeforeClass
    public static void suiteSetup() {
        aspect = Aspects.mapBased(newHashMap(
                "name", STRING,
                "label", STRING));
    }

    @Before
    public void testSetup() throws Exception {
        finder = createRangesFinder();
    }

    @Test
    public void simpleEqualsOperation() throws Exception {
        Map<String, RangeSet<?>> ranges = finder.process(sql("label = 'Red'"));
        assertThat(ranges, hasEntry("label", ranges("[Red - Red]")));
    }

    @Test
    public void simpleInOperation() throws Exception {
        Map<String, RangeSet<?>> ranges = finder.process(sql("label IN ('Red', 'Blue')"));
        assertThat(ranges, hasEntry("label", ranges("[Red - Red]", "[Blue - Blue]")));
    }

    @Test
    public void simpleGreaterThanOperation() throws Exception {
        Map<String, RangeSet<?>> ranges = finder.process(sql("label > 'Red'"));
        assertThat(ranges, hasEntry("label", ranges("(Red - *)")));
    }

    @Test
    public void simpleLessThanEqualsOperation() throws Exception {
        Map<String, RangeSet<?>> ranges = finder.process(sql("label <= 'Red'"));
        assertThat(ranges, hasEntry("label", ranges("(* - Red]")));
    }

    @Test
    public void operationWithAndOnDifferentFields() throws Exception {
        Map<String, RangeSet<?>> ranges = finder.process(sql("label = 'Red' AND name > 'Obj1'"));
        assertThat(ranges, hasEntry("label", ranges("[Red - Red]")));
        assertThat(ranges, hasEntry("name", ranges("(Obj1 - *)")));
    }

    @Test
    public void operationsWithAndOnSameFields() throws Exception {
        Map<String, RangeSet<?>> ranges = finder.process(sql("label <= 'Red' AND label > 'Blue'"));
        assertThat(ranges, hasEntry("label", ranges("(Blue - Red]")));
    }

    @Test
    public void operationWithOrOnDifferentFields() throws Exception {
        Map<String, RangeSet<?>> ranges = finder.process(sql("label = 'Red' OR name > 'Obj1'"));
        assertThat(ranges.entrySet(), hasSize(0));
    }

    @Test
    public void operationsWithOrOnSameFields() throws Exception {
        Map<String, RangeSet<?>> ranges = finder.process(sql("label >= 'Red' OR label = 'Blue'"));
        assertThat(ranges, hasEntry("label", ranges("[Blue - Blue]", "[Red - *)")));
    }

    private Expression sql(String sql) throws Exception {
        ExpressionFactory exprFactory = new DefaultExpressionFactory();
        SqlExpressionParser parser = new SqlExpressionParser(exprFactory);
        return parser.parse(sql);
    }

    private FieldRangesFinder createRangesFinder() {
        FieldExpressionEvaluatorFactory<?> fieldEvalFactory = new AspectFieldEvaluationFactory<>(aspect);
        ExpressionEvaluatorFactory<?> evalFactory = new DefaultExpressionEvaluatorFactory<>(fieldEvalFactory);
        return new FieldRangesFinder(evalFactory);
    }

    private static RangeSet<String> ranges(String... rangeTexts) {
        List<Range<String>> ranges = new ArrayList<>();
        for (String rangeText : rangeTexts) {
            Range<String> range = parseString(rangeText);
            ranges.add(range);
        }
        return RangeSets.on(ranges);
    }
}
