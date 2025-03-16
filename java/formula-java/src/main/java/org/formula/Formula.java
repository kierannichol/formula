package org.formula;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.formula.context.DataContext;
import org.formula.optimize.FormulaOptimizer;
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
            .operator("+", 2, Associativity.LEFT, Formula::addReduceFn)
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
            .operator(",", 1, Associativity.LEFT, Formula::mergeLists)
            .term("true", () -> ResolvedValue.TRUE)
            .term("false", () -> ResolvedValue.FALSE)
            .term("null", ResolvedValue::none)
            .function("abs", (ResolvedValue a) -> ResolvedValue.of(Math.abs(a.asDecimal())))
            .function("min", Formula::minFn)
            .function("max", Formula::maxFn)
            .function("maxeach", Formula::maxEachFn)
            .function("mineach", Formula::minEachFn)
            .function("clamp", Formula::clampFn)
            .function("floor", (ResolvedValue a) -> ResolvedValue.of(Math.floor(a.asDecimal())))
            .function("ceil", (ResolvedValue a) -> ResolvedValue.of(Math.ceil(a.asDecimal())))
            .function("sum", Formula::sumFn)
            .function("signed", (ResolvedValue a) -> ResolvedValue.of((a.asNumber() < 0 ? "" : "+") + a.asNumber()))
            .function("if", (ResolvedValue a, ResolvedValue b, ResolvedValue c) -> a.asBoolean() ? b : c)
            .function("concat", Formula::concatFn)
            .function("ordinal", (ResolvedValue a) -> ResolvedValue.of(Ordinal.toString(a.asNumber())))
            .function("any", Formula::anyFn)
            .function("all", Formula::allFn)
            .variable("@", Formula::variableFn)
            .variable("@{", "}", Formula::variableFn)
            .comment("[", "]", (value, comment) -> NamedResolvedValue.of(value, comment.substring(1, comment.length() - 1), "[", "]"))
            ;

    private static ResolvedValue allFn(List<ResolvedValue> values) {
        return ResolvedValue.of(values.stream()
                .flatMap(value -> value.asList().stream())
                .allMatch(ResolvedValue::asBoolean));
    }

    private static ResolvedValue anyFn(List<ResolvedValue> values) {
        return ResolvedValue.of(values.stream()
                .flatMap(value -> value.asList().stream())
                .anyMatch(ResolvedValue::asBoolean));
    }

    private static ResolvedValue sumFn(ResolvedValue a) {
        var list = a.asList();
        if (list.isEmpty()) {
            return ResolvedValue.ZERO;
        }
        if (list.size() < 2) {
            return a;
        }
        return ResolvedValue.of(a.asList()
                .stream()
                .mapToDouble(value -> sumFn(value).asDecimal())
                .sum());
    }

    private static ResolvedValue maxEachFn(ResolvedValue a) {
        List<ResolvedValue> values = new ArrayList<>();
        a.asList().forEach(value -> values.add(maxFn(value)));
        return ResolvedValue.of(values);
    }

    private static ResolvedValue minEachFn(ResolvedValue a) {
        return ResolvedValue.of(a.asList().stream()
                .map(Formula::minFn)
                .toList());
    }

    private static ResolvedValue clampFn(ResolvedValue a, ResolvedValue min, ResolvedValue max) {
        if (a.asDecimal() < min.asDecimal()) {
            return min;
        }
        if (a.asDecimal() > max.asDecimal()) {
            return max;
        }
        return a;
    }

    private static ResolvedValue maxFn(ResolvedValue a) {
        return a.asList().stream()
                .max(Comparator.comparing(ResolvedValue::asDecimal))
                .orElse(ResolvedValue.none());
    }

    private static ResolvedValue minFn(ResolvedValue a) {
        return a.asList().stream()
                .min(Comparator.comparing(ResolvedValue::asDecimal))
                .orElse(ResolvedValue.none());
    }

    private static ResolvedValue mergeLists(ResolvedValue a, ResolvedValue b) {
        List<ResolvedValue> merged = new ArrayList<>();
        merged.addAll(a.asList());
        merged.addAll(b.asList());
        return ResolvedValue.of(merged);
    }

    private static ResolvedValue variableFn(DataContext context, String key) {
        if (key.contains("*")) {
            return ResolvedValue.of(context.search(key).toList());
        }
        return context.get(key);
    }

    private static ResolvedValue concatFn(List<ResolvedValue> values) {
        return ResolvedValue.concat(values);
    }

    private static ResolvedValue addReduceFn(ResolvedValue a, ResolvedValue b) {
        if (!a.hasValue() && !b.hasValue()) {
            return ResolvedValue.ZERO;
        }
        return ResolvedValue.of(a.asDecimal() + b.asDecimal());
    }

    private static ResolvedValue maxReduceFn(ResolvedValue a, ResolvedValue b) {
        return checkForNone(a,b).orElseGet(() -> a.asDecimal() > b.asDecimal() ? a : b);
    }

    private static ResolvedValue minReduceFn(ResolvedValue a, ResolvedValue b) {
        return checkForNone(a,b).orElseGet(() -> a.asDecimal() < b.asDecimal() ? a : b);
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

    private static Optional<ResolvedValue> checkForNone(ResolvedValue a, ResolvedValue b) {
        if (!a.hasValue()) return Optional.of(b);
        if (!b.hasValue()) return Optional.of(a);
        return Optional.empty();
    }

    private Formula() {}
}
