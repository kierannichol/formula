package org.formula.optimize;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.formula.test.FormulaTestCase;
import org.formula.test.OptimizeTestCase;
import org.formula.test.TestCaseLoader;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

public class FormulaOptimizerTest {

    @TestFactory
    @DisplayName("data-driven test cases")
    Stream<DynamicTest> dataDrivenTestCases() {
        return TestCaseLoader.load(OptimizeTestCase.class, "optimize-test-cases.yml").stream()
                .map(testCase -> DynamicTest.dynamicTest(testCase.name(), () -> {
                    validateTestCase(testCase);
                }));
    }

    private void validateTestCase(OptimizeTestCase testCase) {
        var optimized = FormulaOptimizer.optimize(testCase.formula());
        assertThat(optimized).isEqualTo(testCase.expectedFormula());
    }
}
