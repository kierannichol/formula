package org.formula.parse.assertions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.formula.parse.assertions.ResolvedValueAssertions.assertResolvedValue;
import static org.junit.jupiter.api.Assertions.fail;

import org.formula.NamedResolvedValue;
import org.formula.ResolvedValue;
import org.formula.context.DataContext;
import org.formula.Formula;

public class FormulaAssertions {
    private final String formula;
    private final DataContext context;

    public static FormulaAssertions assertFormula(String formula) {
        return new FormulaAssertions(formula, DataContext.EMPTY);
    }

    public FormulaAssertions withContext(DataContext context) {
        return new FormulaAssertions(formula, context);
    }

    public FormulaAssertions resolvesTo(String expected) {
        assertResolvedValue(Formula.parse(formula).resolve(context)).hasValue(expected);
        return this;
    }

    public FormulaAssertions resolvesTo(int expected) {
        assertResolvedValue(Formula.parse(formula).resolve(context)).hasValue(expected);
        return this;
    }

    public FormulaAssertions resolvesTo(double expected) {
        assertResolvedValue(Formula.parse(formula).resolve(context)).hasValue(expected);
        return this;
    }

    public FormulaAssertions resolvesTo(boolean expected) {
        assertResolvedValue(Formula.parse(formula).resolve(context)).hasValue(expected);
        return this;
    }

    public FormulaAssertions isNamed(String expected) {
        ResolvedValue resolved = Formula.parse(formula).resolve(context);
        if (resolved instanceof NamedResolvedValue namedValue) {
            assertThat(namedValue.asName()).isEqualTo(expected);
            return this;
        }
        fail("ResolvedValue was not a NamedResolvedValue");
        return this;
    }

    private FormulaAssertions(String formula, DataContext context) {
        this.formula = formula;
        this.context = context;
    }
}
