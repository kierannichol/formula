package org.formula.parse.assertions;

import static org.formula.parse.assertions.ResolvedValueAssertions.assertResolvedValue;

import org.formula.context.DataContext;
import org.formula.parse.Formula;

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

    private FormulaAssertions(String formula, DataContext context) {
        this.formula = formula;
        this.context = context;
    }
}
