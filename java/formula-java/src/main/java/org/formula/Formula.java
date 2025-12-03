package org.formula;

public class Formula {
    private static final FormulaParser PARSER = FormulaParser.createDefault();

    public static Resolvable parse(String formula) {
        return PARSER.parse(formula);
    }
}
