package org.formula;

import static org.formula.Formula.*;
import static org.formula.parse.assertions.FormulaAssertions.assertFormula;
import static org.formula.parse.assertions.ResolvedValueAssertions.assertResolvedValue;

import org.formula.context.DataContext;
import org.formula.context.MutableDataContext;
import org.junit.jupiter.api.Test;

class FormulaTest {

    @Test
    void singleNegativeNumber() {
        var formula = parse("-9");
        assertResolvedValue(formula).hasValue(-9);
    }

    @Test
    void addTwoScalars() {
        var formula = parse("2 + 3");
        assertResolvedValue(formula).hasValue(5);
    }

    @Test
    void exponents() {
        var formula = parse("2^3");
        assertResolvedValue(formula).hasValue(8);
    }

    @Test
    void withBrackets() {
        var formula = parse("4 + 4 * 2 / ( 1 - 5 )");
        assertResolvedValue(formula).hasValue(2);
    }

    @Test
    void multipleDigitNumbers() {
        var formula = parse("12 + 100");
        assertResolvedValue(formula).hasValue(112);
    }

    @Test
    void absFunction() {
        var formula = parse("2 + abs(2 - 3) + 1");
        assertResolvedValue(formula).hasValue(4);
    }

    @Test
    void minFunction() {
        var formula = parse("1 + min(4, 2)");
        assertResolvedValue(formula).hasValue(3);
    }

    @Test
    void maxFunction() {
        var formula = parse("1 + max(4, 2)");
        assertResolvedValue(formula).hasValue(5);
    }

    @Test
    void complexMaxFunction() {
        var formula = parse("max(4 - 2, 2 / 2)");
        assertResolvedValue(formula).hasValue(2);
    }

    @Test
    void floorFunction() {
        var formula = parse("1 + floor(2.9)");
        assertResolvedValue(formula).hasValue(3);
    }

    @Test
    void ceilFunction() {
        var formula = parse("1 + ceil(2.9)");
        assertResolvedValue(formula).hasValue(4);
    }

    @Test
    void signedFunction() {
        assertResolvedValue(parse("signed(3)")).hasValue("+3");
        assertResolvedValue(parse("signed(-3)")).hasValue("-3");
        assertResolvedValue(parse("signed(3)")).hasValue(3);
        assertResolvedValue(parse("signed(-3)")).hasValue(-3);
    }

    @Test
    void negativeIntegers() {
        assertResolvedValue(parse("-4")).hasValue(-4);
        assertResolvedValue(parse("1-4")).hasValue(-3);
        assertResolvedValue(parse("(1)-4")).hasValue(-3);
        assertResolvedValue(parse("1-(4)")).hasValue(-3);
        assertResolvedValue(parse("(1-4)")).hasValue(-3);
    }

    @Test
    void trailingMinusInteger() {
        assertResolvedValue(parse("(5)-1")).hasValue(4);
    }

    @Test
    void multiplyNegativeInteger() {
        assertResolvedValue(parse("5*-2")).hasValue(-10);
    }

    @Test
    void simpleVariableFunction() {
        var formula = parse("@foo");
        var context = MutableDataContext.create().set("foo", 12);
        assertResolvedValue(formula.resolve(context)).hasValue(12);
    }

    @Test
    void variableMath() {
        var formula = parse("@foo + 2");
        var context = MutableDataContext.create().set("foo", 1);
        assertResolvedValue(formula.resolve(context)).hasValue(3);
    }

    @Test
    void variableReferenceFormula() {
        var formula = parse("@bar");
        var context = MutableDataContext.create()
                .set("foo", 4)
                .set("bar", parse("@foo"));
        assertResolvedValue(formula.resolve(context)).hasValue(4);
    }

    @Test
    void nullSameAsUndefined() {
        var formula = parse("null");
        assertResolvedValue(formula.resolve()).hasNoValue();
    }

    @Test
    void ifFormula() {
        var formula = parse("if(-2 < 0, 'A', 'B')");
        var context = DataContext.EMPTY;
        assertResolvedValue(formula.resolve(context)).hasValue("A");
    }

    @Test
    void elseFormula() {
        var formula = parse("concat(if(2 < 0, '-', '+'), 2)");
        var context = DataContext.EMPTY;
        assertResolvedValue(formula.resolve(context)).hasValue("+2");
    }

    @Test
    void modifierFormula() {
        var formula = parse("concat(if((floor(@test_score/2) - 5) > 0, '+', ''), floor(@test_score/2) - 5)");
        var context = MutableDataContext.create()
                .set("test_score", 12);
        assertResolvedValue(formula.resolve(context)).hasValue("+1");
    }

    @Test
    void ordinal() {
        String[] expected = { "0th", "1st", "2nd", "3rd", "4th", "5th", "6th", "7th", "8th", "9th", "10th" };
        for (int i = 0; i < expected.length; i++) {
            assertFormula("ordinal(%d)".formatted(i))
                    .resolvesTo(expected[i]);
        }
    }

    @Test
    void wildcardMin() {
        var formula = parse("min(@key_*)");
        var context = MutableDataContext.create()
                .set("other", 2)
                .set("key_1", 4)
                .set("key_2", 3)
                .set("key_3", 5);
        assertResolvedValue(formula.resolve(context)).hasValue(3);
    }

    @Test
    void wildcardMax() {
        var formula = parse("max(@key_*)");
        var context = MutableDataContext.create()
                .set("other", 2)
                .set("key_1", 4)
                .set("key_2", 3)
                .set("key_3", 5);
        assertResolvedValue(formula.resolve(context)).hasValue(5);
    }

    @Test
    void wildcardSum() {
        var formula = parse("sum(@key_*)");
        var context = MutableDataContext.create()
                .set("other", 2)
                .set("key_1", 4)
                .set("key_2", 3)
                .set("key_3", 5);
        assertResolvedValue(formula.resolve(context)).hasValue(12);
    }

    @Test
    void anyFunction() {
        var formula = parse("any(@a, @b)");
        assertResolvedValue(formula.resolve(MutableDataContext.create().set("a", 0).set("b", 0))).hasValue(false);
        assertResolvedValue(formula.resolve(MutableDataContext.create().set("a", 0).set("b", 1))).hasValue(true);
        assertResolvedValue(formula.resolve(MutableDataContext.create().set("a", 1).set("b", 0))).hasValue(true);
        assertResolvedValue(formula.resolve(MutableDataContext.create().set("a", 1).set("b", 1))).hasValue(true);
    }

    @Test
    void allFunction() {
        var formula = parse("all(@a, @b)");
        assertResolvedValue(formula.resolve(MutableDataContext.create().set("a", 0).set("b", 0))).hasValue(false);
        assertResolvedValue(formula.resolve(MutableDataContext.create().set("a", 0).set("b", 1))).hasValue(false);
        assertResolvedValue(formula.resolve(MutableDataContext.create().set("a", 1).set("b", 0))).hasValue(false);
        assertResolvedValue(formula.resolve(MutableDataContext.create().set("a", 1).set("b", 1))).hasValue(true);
    }

    @Test
    void allWithNestedAny() {
        var formula = parse("all(@a, any(@b, @c), 1)");
        assertResolvedValue(formula.resolve(MutableDataContext.create().set("a", 0).set("b", 0).set("c", 0))).hasValue(false);
        assertResolvedValue(formula.resolve(MutableDataContext.create().set("a", 1).set("b", 0).set("c", 0))).hasValue(false);
        assertResolvedValue(formula.resolve(MutableDataContext.create().set("a", 1).set("b", 1).set("c", 0))).hasValue(true);
        assertResolvedValue(formula.resolve(MutableDataContext.create().set("a", 1).set("b", 0).set("c", 1))).hasValue(true);
        assertResolvedValue(formula.resolve(MutableDataContext.create().set("a", 1).set("b", 1).set("c", 1))).hasValue(true);
    }

    @Test
    void allWithNestedAnyDifferentOrder() {
        var formula = parse("all(any(@b, @c), @a, 1)");
        assertResolvedValue(formula.resolve(MutableDataContext.create().set("a", 0).set("b", 0).set("c", 0))).hasValue(false);
        assertResolvedValue(formula.resolve(MutableDataContext.create().set("a", 1).set("b", 0).set("c", 0))).hasValue(false);
        assertResolvedValue(formula.resolve(MutableDataContext.create().set("a", 1).set("b", 1).set("c", 0))).hasValue(true);
        assertResolvedValue(formula.resolve(MutableDataContext.create().set("a", 1).set("b", 0).set("c", 1))).hasValue(true);
        assertResolvedValue(formula.resolve(MutableDataContext.create().set("a", 1).set("b", 1).set("c", 1))).hasValue(true);
    }

    @Test
    void namedFormula() {
        assertFormula("(false)[Never True]").isNamed("Never True");
    }
}