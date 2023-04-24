package org.formula.parse;

import static org.formula.parse.assertions.ResolvedValueAssertions.assertResolvedValue;

import org.formula.ResolvedValue;
import org.formula.context.MutableDataContext;
import org.junit.jupiter.api.Test;

public class FormulaPerformanceTest {
    private static final int ITERATIONS = 1000;
    private static final String FORMULA_TEXT = "@alpha AND (@beta OR @delta) AND @sigma AND (@omega >= 5)";

    @Test
    void parsePerformance() {
        var startTime = System.nanoTime();
        for (var i = 0; i < ITERATIONS; i++) {
            Formula.parse(FORMULA_TEXT);
        }
        var endTime = System.nanoTime();
        var total = endTime - startTime;

        System.out.printf("Parse Average: %.2f µs%n", total / ITERATIONS * 0.001);
    }

    @Test
    void resolvePerformance() {
        var formula = Formula.parse(FORMULA_TEXT);
        var context = MutableDataContext.create()
                .set("alpha", "true")
                .set("beta", Formula.parse("!@delta"))
                .set("delta", Formula.parse("true AND @alpha"))
                .set("sigma", Formula.parse("@alpha AND @beta"))
                .set("omega", "12");

        for (var j = 0; j < 20000; j++) {
            context.set("key_"+j, "value_"+j);
        }
        var startTime = System.nanoTime();
        for (var i = 0; i < ITERATIONS; i++) {
            formula.resolve(context);
        }
        var endTime = System.nanoTime();
        var total = endTime - startTime;

        System.out.printf("Resolve Average: %.2f µs%n", total / ITERATIONS * 0.001);
    }

    @Test
    void deepResolvePerformance() {
        final int depth = 1000;
        var formula = Formula.parse("@step_" + depth);
        var context = MutableDataContext.create()
                .set("step_1", 1);
        for (var j = 2; j <= depth; j++) {
            context = context.set("step_"+j, Formula.parse("@step_%d + 1".formatted(j - 1)));
        }
        var startTime = System.nanoTime();
        var result = ResolvedValue.none();
        for (var i = 0; i < ITERATIONS; i++) {
            result = formula.resolve(context);
        }
        var endTime = System.nanoTime();
        var total = endTime - startTime;

        assertResolvedValue(result).hasValue(depth);
        System.out.printf("Deep Resolve Average: %.2f µs%n", total / ITERATIONS * 0.001);
    }
}
