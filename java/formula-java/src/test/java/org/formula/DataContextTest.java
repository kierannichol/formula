package org.formula;

import java.util.stream.Stream;
import org.formula.context.MutableDataContext;
import org.formula.test.DataContextAction;
import org.formula.test.DataContextTestCase;
import org.formula.test.TestCaseLoader;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

class DataContextTest {

    @TestFactory
    @DisplayName("data-driven test cases")
    Stream<DynamicTest> dataDrivenTestCases() {
        return TestCaseLoader.load(DataContextTestCase.class, "data-context-test-cases.yml").stream()
                .map(testCase -> DynamicTest.dynamicTest(testCase.name(), () -> validateTestCase(testCase)));
    }

    private void validateTestCase(DataContextTestCase testCase) {
        MutableDataContext context = MutableDataContext.create();
        if (testCase.data() != null) {
            testCase.data().forEach(context::set);
        }

        for (String actionText : testCase.actions()) {
            DataContextAction.parse(actionText).execute(context);
        }

        testCase.expected().forEach((key, expected) -> {
            ResolvedValue actual = context.get(key);
            expected.assertEqualsAll(key, actual);
        });
    }


}