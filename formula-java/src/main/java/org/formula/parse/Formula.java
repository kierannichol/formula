package org.formula.parse;

import java.util.List;
import org.formula.Resolvable;
import org.formula.ResolvedValue;
import org.formula.context.DataContext;
import org.formula.parse.shuntingyard.Associativity;
import org.formula.parse.shuntingyard.Operator1;
import org.formula.parse.shuntingyard.Operator2;
import org.formula.parse.shuntingyard.ShuntingYardParser;
import org.formula.util.Ordinal;

public class Formula {

    private static final ShuntingYardParser PARSER = ShuntingYardParser.create()
            .operator("^", 4, Associativity.RIGHT, (a, b) -> ResolvedValue.of(Math.pow(a.asDecimal(), b.asDecimal())))
            .operator("*", 3, Associativity.LEFT, (a, b) -> ResolvedValue.of(a.asDecimal() * b.asDecimal()))
            .operator("/", 3, Associativity.LEFT, (a, b) -> ResolvedValue.of(a.asDecimal() / b.asDecimal()))
            .operator("+", 2, Associativity.LEFT, (a, b) -> ResolvedValue.of(a.asDecimal() + b.asDecimal()))
            .biOperator("-",
                    new Operator1("-", 4, Associativity.LEFT, a -> ResolvedValue.of(-a.asDecimal())),
                    new Operator2("-", 2, Associativity.LEFT, (a, b) -> ResolvedValue.of(a.asDecimal() - b.asDecimal())))
            .operator("!", 2, Associativity.LEFT, (ResolvedValue a) -> ResolvedValue.of(!a.asBoolean()))
            .operator("<", 3, Associativity.LEFT, (ResolvedValue a, ResolvedValue b) -> ResolvedValue.of(a.asDecimal() < b.asDecimal()))
            .operator("<=", 3, Associativity.LEFT, (ResolvedValue a, ResolvedValue b) -> ResolvedValue.of(a.asDecimal() <= b.asDecimal()))
            .operator(">", 3, Associativity.LEFT, (ResolvedValue a, ResolvedValue b) -> ResolvedValue.of(a.asDecimal() > b.asDecimal()))
            .operator(">=", 3, Associativity.LEFT, (ResolvedValue a, ResolvedValue b) -> ResolvedValue.of(a.asDecimal() >= b.asDecimal()))
            .operator("==", 3, Associativity.LEFT, (ResolvedValue a, ResolvedValue b) -> ResolvedValue.of(a.equals(b)))
            .operator("!=", 3, Associativity.LEFT, (ResolvedValue a, ResolvedValue b) -> ResolvedValue.of(!a.equals(b)))
            .operator("AND", 1, Associativity.LEFT, (ResolvedValue a, ResolvedValue b) -> ResolvedValue.of(a.asBoolean() && b.asBoolean()))
            .operator("OR", 1, Associativity.LEFT, (ResolvedValue a, ResolvedValue b) -> ResolvedValue.of(a.asBoolean() || b.asBoolean()))
            .term("true", () -> ResolvedValue.TRUE)
            .term("false", () -> ResolvedValue.FALSE)
            .function("abs", (ResolvedValue a) -> ResolvedValue.of(Math.abs(a.asDecimal())))
            .function("min", (ResolvedValue a, ResolvedValue b) -> ResolvedValue.of(Math.min(a.asDecimal(), b.asDecimal())))
            .function("max", (ResolvedValue a, ResolvedValue b) -> ResolvedValue.of(Math.max(a.asDecimal(), b.asDecimal())))
            .function("floor", (ResolvedValue a) -> ResolvedValue.of(Math.floor(a.asDecimal())))
            .function("ceil", (ResolvedValue a) -> ResolvedValue.of(Math.ceil(a.asDecimal())))
            .function("signed", (ResolvedValue a) -> ResolvedValue.of((a.asNumber() < 0 ? "" : "+") + a.asNumber()))
            .function("if", (ResolvedValue a, ResolvedValue b, ResolvedValue c) -> a.asBoolean() ? b : c)
            .function("concat", (ResolvedValue a, ResolvedValue b) -> ResolvedValue.of(a.asText() + b.asText()))
            .function("ordinal", (ResolvedValue a) -> ResolvedValue.of(Ordinal.toString(a.asNumber())))
            .function("any", (List<ResolvedValue> values) -> ResolvedValue.of(values.stream().anyMatch(ResolvedValue::asBoolean)))
            .function("all", (List<ResolvedValue> values) -> ResolvedValue.of(values.stream().allMatch(ResolvedValue::asBoolean)))
            .variable("@", Formula::variableFn)
            .variable("@{", "}", Formula::variableFn)
            .variable("min(@", ")", Formula::minFn)
            .variable("max(@", ")", Formula::maxFn)
            .variable("sum(@", ")", Formula::sumFn)
            ;

    private static ResolvedValue variableFn(DataContext context, String key) {
        return context.get(key);
    }

    private static ResolvedValue minFn(DataContext context, String key) {
        return context.search(key).reduce((a, b) -> a.asDecimal() < b.asDecimal() ? a : b)
                .orElse(ResolvedValue.none());
    }

    private static ResolvedValue sumFn(DataContext context, String key) {
        return context.search(key).reduce((a, b) -> ResolvedValue.of(a.asDecimal() + b.asDecimal()))
                .orElse(ResolvedValue.of(0));
    }

    private static ResolvedValue maxFn(DataContext context, String key) {
        return context.search(key).reduce((a, b) -> a.asDecimal() > b.asDecimal() ? a : b)
                .orElse(ResolvedValue.none());
    }

    public static Resolvable parse(String formulaText) {
        if (formulaText.isBlank()) {
            return Resolvable.empty();
        }
        return PARSER.parse(formulaText);
    }

    public static String optimize(String formulaText) {
        return FormulaOptimizer.optimize(formulaText);
    }

    private Formula() {}
}
