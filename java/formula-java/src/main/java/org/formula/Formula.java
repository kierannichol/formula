package org.formula;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.formula.context.DataContext;
import org.formula.optimize.FormulaOptimizer;
import org.formula.parse.shuntingyard.Associativity;
import org.formula.parse.shuntingyard.Operator1;
import org.formula.parse.shuntingyard.Operator2;
import org.formula.parse.shuntingyard.ShuntingYardParser;
import org.formula.util.Ordinal;
import org.formula.util.RollUtils;

public class Formula {

    private static final ShuntingYardParser PARSER = ShuntingYardParser.create()
            .operator("^", 4, Associativity.RIGHT, (a, b) -> ResolvedValue.of(Math.pow(a.asDecimal(), b.asDecimal())))
            .operator("*", 3, Associativity.LEFT, (a, b) -> ResolvedValue.of(a.asDecimal() * b.asDecimal()))
            .operator("/", 3, Associativity.LEFT, (a, b) -> ResolvedValue.of(a.asDecimal() / b.asDecimal()))
            .operator("+", 2, Associativity.LEFT, Formula::addFn)
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
            .operator("d", 4, Associativity.LEFT, (ResolvedValue a, ResolvedValue b) -> ResolvedRollValue.of(a.asNumber(), b.asNumber()))
            .term("true", () -> ResolvedValue.TRUE)
            .term("false", () -> ResolvedValue.FALSE)
            .term("null", ResolvedValue::none)
            .function("abs", (ResolvedValue a) -> ResolvedValue.of(Math.abs(a.asDecimal())))
            .function("min", (ResolvedValue a, ResolvedValue b) -> ResolvedValue.of(Math.min(a.asDecimal(), b.asDecimal())))
            .function("max", (ResolvedValue a, ResolvedValue b) -> ResolvedValue.of(Math.max(a.asDecimal(), b.asDecimal())))
            .function("floor", (ResolvedValue a) -> ResolvedValue.of(Math.floor(a.asDecimal())))
            .function("ceil", (ResolvedValue a) -> ResolvedValue.of(Math.ceil(a.asDecimal())))
            .function("signed", (ResolvedValue a) -> ResolvedValue.of((a.asNumber() < 0 ? "" : "+") + a.asNumber()))
            .function("if", (ResolvedValue a, ResolvedValue b, ResolvedValue c) -> a.asBoolean() ? b : c)
            .function("concat", Formula::concatFn)
            .function("ordinal", (ResolvedValue a) -> ResolvedValue.of(Ordinal.toString(a.asNumber())))
            .function("any", (List<ResolvedValue> values) -> ResolvedValue.of(values.stream().anyMatch(ResolvedValue::asBoolean)))
            .function("all", (List<ResolvedValue> values) -> ResolvedValue.of(values.stream().allMatch(ResolvedValue::asBoolean)))
            .variable("@", Formula::variableFn)
            .variable("@{", "}", Formula::variableFn)
            .variable("min(@", ")", Formula::minFn)
            .variable("max(@", ")", Formula::maxFn)
            .variable("sum(@", ")", Formula::sumFn)
            .comment("[", "]", (value, comment) -> NamedResolvedValue.of(value, comment.substring(1, comment.length() - 1), "[", "]"))
            ;

    private static ResolvedValue variableFn(DataContext context, String key) {
        return context.get(key);
    }

    private static ResolvedValue minFn(DataContext context, String key) {
        return context.search(key)
                .flatMap(a -> a.asList().stream())
                .reduce((a, b) -> a.asDecimal() < b.asDecimal() ? a : b)
                .orElse(ResolvedValue.none());
    }

    private static ResolvedValue sumFn(DataContext context, String key) {
        return context.search(key)
                .flatMap(a -> a.asList().stream())
                .reduce(Formula::addFn)
                .orElse(ResolvedValue.of(0));
    }

    private static ResolvedValue maxFn(DataContext context, String key) {
        return context.search(key)
                .flatMap(a -> a.asList().stream())
                .reduce((a, b) -> a.asDecimal() > b.asDecimal() ? a : b)
                .orElse(ResolvedValue.none());
    }

    private static ResolvedValue concatFn(List<ResolvedValue> values) {
        return ResolvedValue.concat(values);
    }

    private static ResolvedValue addFn(ResolvedValue a, ResolvedValue b) {
        if (a.equals(ResolvedValue.none()) && b.equals(ResolvedValue.none())) {
            return ResolvedValue.none();
        }
        return ResolvedValue.of(a.asDecimal() + b.asDecimal());
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
