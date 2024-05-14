package org.formula;

import static org.formula.Formula.parse;
import static org.formula.parse.assertions.ResolvedValueAssertions.assertResolvedValue;

public class FormulaDataDrivenTest extends AbstractDataDrivenTestCase {

    @Override
    protected String getDataResourceName() {
        return "formula-test-cases.csv";
    }

    @Override
    protected void test(String given, String expected) {
        Integer expectedInteger = null;
        String expectedString = null;
        Double expectedDouble = null;
        Boolean expectedBoolean = null;

        if ("true".equals(expected)) {
            expectedBoolean = true;
        }
        else if ("false".equals(expected)) {
            expectedBoolean = false;
        }
        else if (asDouble(expected) != null && expected.contains(".")) {
            expectedDouble = asDouble(expected);
        }
        else if (asInteger(expected) != null) {
            expectedInteger = asInteger(expected);
        }
        else {
            expectedString = expected;
            if (expectedString.startsWith("\"") && expectedString.endsWith("\"")) {
                expectedString = expectedString.substring(1, expectedString.length() - 1);
            }
        }

        var formula = parse(given);

        if (expectedBoolean != null)
            assertResolvedValue(formula).hasValue(expectedBoolean);
        if (expectedDouble != null)
            assertResolvedValue(formula).hasValue(expectedDouble);
        if (expectedInteger != null)
            assertResolvedValue(formula).hasValue(expectedInteger);
        if (expectedString != null)
            assertResolvedValue(formula).hasValue(expectedString);
    }

    private static Double asDouble(String text) {
        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Integer asInteger(String text) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
