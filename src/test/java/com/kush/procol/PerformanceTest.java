package com.kush.procol;

import static java.lang.System.currentTimeMillis;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

import java.util.List;

import org.junit.Test;

import com.kush.lib.expressions.ExpressionEvaluatorFactory;
import com.kush.lib.expressions.ExpressionException;
import com.kush.lib.expressions.ExpressionFactory;
import com.kush.lib.expressions.aspect.Aspect;
import com.kush.lib.expressions.aspect.AspectFieldEvaluationFactory;
import com.kush.lib.expressions.aspect.Aspects;
import com.kush.lib.expressions.evaluators.DefaultExpressionEvaluatorFactory;
import com.kush.lib.expressions.evaluators.FieldExpressionEvaluatorFactory;
import com.kush.lib.expressions.factory.DefaultExpressionFactory;
import com.kush.lib.expressions.parsers.sql.SqlExpressionParser;
import com.kush.procol.collections.IndexableArrayList;
import com.kush.procol.indexes.IndexFactory;
import com.kush.procol.indexes.policies.FirstApplicableIndexPolicy;
import com.kush.procol.queries.sql.SqlFieldAttribute;
import com.kush.procol.queries.sql.SqlIndexQueryGenerator;

public class PerformanceTest {

    @Test
    public void testName() throws Exception {

        IndexableCollection<SampleObject> collection = new IndexableArrayList<>();
        int totalObjects = 10_000_000;
        int labels = 100;
        for (int i = 1; i <= totalObjects; i++) {
            collection.add(sample("Name" + i, "Label" + i % labels));
        }

        long start = currentTimeMillis();
        List<SampleObject> filteredObjects = collection.stream()
            .filter(o -> o.label.equals("Label1"))
            .collect(toList());
        long timeTaken = currentTimeMillis() - start;
        assertThat(filteredObjects, hasSize(totalObjects / labels));
        System.out.println("Without index: " + timeTaken);

        Index<String, SampleObject> index = IndexFactory.createIndexWithSortedKeys(o -> o.label);
        collection.addIndex(new SqlFieldAttribute("label"), index);

        IndexQuery<SampleObject> query = createQuery("label = 'Label1'");

        start = currentTimeMillis();
        filteredObjects = collection.query(query).stream().collect(toList());
        timeTaken = currentTimeMillis() - start;
        assertThat(filteredObjects, hasSize(totalObjects / labels));
        System.out.println("With index: " + timeTaken);
    }

    private IndexQuery<SampleObject> createQuery(String sql) throws ExpressionException {
        ExpressionFactory factory = new DefaultExpressionFactory();
        SqlExpressionParser parser = new SqlExpressionParser(factory);
        Aspect<SampleObject> aspect = Aspects.classBased(SampleObject.class);
        FieldExpressionEvaluatorFactory<SampleObject> fieldEvalFactory = new AspectFieldEvaluationFactory<>(aspect);
        ExpressionEvaluatorFactory<SampleObject> evalFactory = new DefaultExpressionEvaluatorFactory<>(fieldEvalFactory);
        IndexSelectionPolicy<SampleObject> policy = new FirstApplicableIndexPolicy<>();
        SqlIndexQueryGenerator<SampleObject> queryGenerator = new SqlIndexQueryGenerator<>(parser, evalFactory, policy);
        IndexQuery<SampleObject> query = queryGenerator.generate(sql);
        return query;
    }

    private static SampleObject sample(String name, String label) {
        SampleObject object = new SampleObject();
        object.name = name;
        object.label = label;
        return object;
    }

    private static class SampleObject {
        @SuppressWarnings("unused")
        String name;
        String label;
    }
}
