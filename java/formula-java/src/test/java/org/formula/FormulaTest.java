package org.formula;

import static org.assertj.core.api.Assertions.assertThat;
import static org.formula.Formula.*;
import static org.formula.parse.assertions.FormulaAssertions.assertFormula;
import static org.formula.parse.assertions.ResolvedValueAssertions.assertResolvedValue;

import java.nio.file.Path;
import java.util.stream.Stream;
import net.bytebuddy.asm.Advice.Argument;
import org.formula.context.DataContext;
import org.formula.context.MutableDataContext;
import org.formula.optimize.FormulaOptimizer;
import org.formula.test.FormulaTestCase;
import org.formula.test.OptimizeTestCase;
import org.formula.test.TestCaseLoader;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class FormulaTest {

    @TestFactory
    @DisplayName("data-driven test cases")
    Stream<DynamicTest> dataDrivenTestCases() {
        return TestCaseLoader.load(FormulaTestCase.class, "formula-test-cases.yml").stream()
                .map(testCase -> DynamicTest.dynamicTest(testCase.name(), () -> {
                    validateTestCase(testCase);
                }));
    }

    private void validateTestCase(FormulaTestCase testCase) {
        try {
            var formula = parse(testCase.formula());
            ResolvedValue resolved = formula.resolve(testCase.data());
            testCase.assertResult(resolved);

            if (testCase.expectedName() != null) {
                assertFormula(testCase.formula()).isNamed(testCase.expectedName());
            }
        } catch (Exception e) {
            if (testCase.expectedError() == null) {
                throw e;
            }

            assertThat(e.getMessage()).isEqualTo(testCase.expectedError());
        }
    }
}