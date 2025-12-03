package org.formula;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import org.formula.context.DataContext;
import org.formula.parse.shuntingyard.Associativity;
import org.formula.parse.shuntingyard.Operator1;
import org.formula.parse.shuntingyard.Operator2;
import org.formula.parse.shuntingyard.ShuntingYardParser;
import org.formula.util.Ordinal;

public class FormulaParser {
    private final ShuntingYardParser parser = ShuntingYardParser.create();

    public static FormulaParser createDefault() {
        return new FormulaParser()
                .addNulls()
                .addLists()
                .addDefaultVariables()
                .addDefaultComments()
                .addBasicMath()
                .addBasicLogic()
                .addStringFunctions();
    }

    public FormulaParser addCustom(Consumer<ShuntingYardParser> modifyParserFn) {
        modifyParserFn.accept(parser);
        return this;
    }

    public Resolvable parse(String formulaText) {
        if (formulaText.isBlank()) {
            return Resolvable.empty();
        }
        return parser.parse(formulaText);
    }

    public FormulaParser addBasicMath() {
        parser
                .operator("^", 4, Associativity.RIGHT, (a, b) -> ResolvedValue.of(Math.pow(a.asDecimal(), b.asDecimal())))
                .operator("*", 3, Associativity.LEFT, (a, b) -> ResolvedValue.of(a.asDecimal() * b.asDecimal()))
                .operator("/", 3, Associativity.LEFT, (a, b) -> ResolvedValue.of(a.asDecimal() / b.asDecimal()))
                .operator("+", 2, Associativity.LEFT, FormulaParser::addReduceFn)
                .biOperator("-",
                        new Operator1("-", 4, Associativity.LEFT, a -> ResolvedValue.of(-a.asDecimal())),
                        new Operator2("-", 2, Associativity.LEFT, (a, b) -> ResolvedValue.of(a.asDecimal() - b.asDecimal())))
                .function("abs", (ResolvedValue a) -> ResolvedValue.of(Math.abs(a.asDecimal())))
                .function("min", FormulaParser::minFn)
                .function("max", FormulaParser::maxFn)
                .function("maxeach", FormulaParser::maxEachFn)
                .function("mineach", FormulaParser::minEachFn)
                .function("clamp", FormulaParser::clampFn)
                .function("floor", (ResolvedValue a) -> ResolvedValue.of(Math.floor(a.asDecimal())))
                .function("ceil", (ResolvedValue a) -> ResolvedValue.of(Math.ceil(a.asDecimal())))
                .function("sum", FormulaParser::sumFn);
        return this;
    }

    public FormulaParser addBasicLogic() {
        this.parser
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
                .function("if", (ResolvedValue a, ResolvedValue b, ResolvedValue c) -> a.asBoolean() ? b : c)
                .function("any", FormulaParser::anyFn)
                .function("all", FormulaParser::allFn);
        return this;
    }

    public FormulaParser addDefaultVariables() {
        this.parser
                .variable("@", FormulaParser::variableFn)
                .variable("@{", "}", FormulaParser::variableFn);
        return this;
    }

    public FormulaParser addDefaultComments() {
        this.parser
                .comment("[", "]", (value, comment) -> NamedResolvedValue.of(value, comment.substring(1, comment.length() - 1), "[", "]"));
        return this;
    }

    public FormulaParser addNulls() {
        this.parser
                .term("null", ResolvedValue::none);
        return this;
    }

    public FormulaParser addLists() {
        this.parser
                .operator(",", 1, Associativity.LEFT, FormulaParser::mergeLists);
        return this;
    }

    public FormulaParser addStringFunctions() {
        this.parser
                .function("signed", (ResolvedValue a) -> ResolvedValue.of((a.asNumber() < 0 ? "" : "+") + a.asNumber()))

                .function("concat", FormulaParser::concatFn)
                .function("ordinal", (ResolvedValue a) -> ResolvedValue.of(Ordinal.toString(a.asNumber())));
        return this;
    }

    private static ResolvedValue allFn(List<ResolvedValue> values) {
        for (ResolvedValue value : values) {
            if (!value.hasValue()) return ResolvedValue.FALSE;
            var valueAsList = value.asList();
            for (ResolvedValue el : valueAsList) {
                if (!el.hasValue() || !el.asBoolean()) {
                    return ResolvedValue.FALSE;
                }
            }
        }
        return ResolvedValue.TRUE;
    }

    private static ResolvedValue anyFn(List<ResolvedValue> values) {
        for (var arg : values) {
            if (!arg.hasValue()) {
                continue;
            }
            for (var el : arg.asList()) {
                if (el.hasValue() && el.asBoolean()) return ResolvedValue.TRUE;
            }
        }
        return ResolvedValue.FALSE;
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
                .map(FormulaParser::minFn)
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
        merged.addAll(a.hasValue() ? a.asList() : List.of(ResolvedValue.none()));
        merged.addAll(b.hasValue() ? b.asList() : List.of(ResolvedValue.none()));
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

    private static Optional<ResolvedValue> checkForNone(ResolvedValue a, ResolvedValue b) {
        if (!a.hasValue()) return Optional.of(b);
        if (!b.hasValue()) return Optional.of(a);
        return Optional.empty();
    }

    private FormulaParser() {}
}
