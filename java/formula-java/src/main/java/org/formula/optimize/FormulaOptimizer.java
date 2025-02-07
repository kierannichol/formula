package org.formula.optimize;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.formula.NamedResolvedValue;
import org.formula.QuotedTextResolvedValue;
import org.formula.ResolvedValue;
import org.formula.parse.shuntingyard.Associativity;
import org.formula.parse.shuntingyard.OperatorFunction1;
import org.formula.parse.shuntingyard.OperatorFunction2;
import org.formula.parse.shuntingyard.OperatorFunction3;
import org.formula.parse.shuntingyard.ShuntingYardParser;
import org.formula.util.Lambda1;
import org.formula.util.Lambda2;
import org.formula.util.Lambda3;

public class FormulaOptimizer {

    private static final ShuntingYardParser PARSER = ShuntingYardParser.create()
            .operator("^", 4, Associativity.RIGHT, opFn2((a, b) -> a + "^" + b))
            .operator("*", 3, Associativity.LEFT, (a,b) -> new MathFunction("*", a, b))
            .operator("/", 3, Associativity.LEFT, (a,b) -> new MathFunction("/", a, b))
            .operator("+", 2, Associativity.LEFT, (a,b) -> new MathFunction("+", a, b))
            .operator("-", 2, Associativity.LEFT, (a,b) -> new MathFunction("-", a, b))
            .operator("!", 2, Associativity.LEFT, opFn1((a) -> "!" + a))
            .operator("<", 3, Associativity.LEFT, opFn2((a, b) -> a + "<" + b))
            .operator("<=", 3, Associativity.LEFT, opFn2((a, b) -> a + "<=" + b))
            .operator(">", 3, Associativity.LEFT, opFn2((a, b) -> a + ">" + b))
            .operator(">=", 3, Associativity.LEFT, opFn2((a, b) -> a + ">=" + b))
            .operator("==", 3, Associativity.LEFT, opFn2((a, b) -> a + "==" + b))
            .operator("!=", 3, Associativity.LEFT, opFn2((a, b) -> a + "!=" + b))
            .operator("AND", 1, Associativity.LEFT, (a, b) -> new AllFunction(List.of(b, a)))
            .operator("OR", 1, Associativity.LEFT, (a, b) -> new AnyFunction(List.of(b, a)))
            .operator("d", 4, Associativity.LEFT, opFn2((a, b) -> a + "d" + b))
            .term("true", () -> ResolvedValue.of("true"))
            .term("false", () -> ResolvedValue.of("false"))
            .term("null", () -> ResolvedValue.of("null"))
            .function("abs", opFn1("abs(%s)"::formatted))
            .function("min", opFn2("min(%s,%s)"::formatted))
            .function("max", opFn2("max(%s,%s)"::formatted))
            .function("floor", opFn1("floor(%s)"::formatted))
            .function("ceil", opFn1("ceil(%s)"::formatted))
            .function("signed", opFn1("signed(%s)"::formatted))
            .function("if", opFn3("if(%s,%s,%s)"::formatted))
            .function("concat", opFn2("concat(%s,%s)"::formatted))
            .function("ordinal", opFn1("ordinal(%s)"::formatted))
            .function("any", AnyFunction::new)
            .function("all", AllFunction::new)
            .variable("@", (context, key) -> ResolvedValue.of("@%s".formatted(key)))
            .variable("@{", "}", (context, key) -> ResolvedValue.of("@{%s}".formatted(key)))
            .variable("min(@", ")", (context, key) -> ResolvedValue.of("min(@%s)".formatted(key)))
            .variable("max(@", ")", (context, key) -> ResolvedValue.of("max(@%s)".formatted(key)))
            .variable("sum(@", ")", (context, key) ->ResolvedValue.of("sum(@%s)".formatted(key)))
            .comment("[", "]", (value, comment) -> NamedResolvedValue.of(value, comment.substring(1, comment.length() - 1), "[", "]"))
            ;

    public static String optimize(String formulaText) {
        var resolved = FormulaOptimizer.PARSER.parse(formulaText).resolve();
        if (resolved instanceof MathFunction mf) {
            return mf.asTextNoBrackets();
        }
        return format(resolved);
    }

    private static String format(ResolvedValue value) {
        if (value instanceof QuotedTextResolvedValue quoted) {
            return quoted.asQuotedText();
        }
        if (value instanceof NamedResolvedValue named) {
            return named.toString();
        }
        return value.asText();
    }

    private static OperatorFunction1 opFn1(Lambda1<String, String> fn) {
        return (ResolvedValue a) -> ResolvedValue.of(fn.execute(format(a)));
    }

    private static OperatorFunction2 opFn2(Lambda2<String, String, String> fn) {
        return (ResolvedValue a, ResolvedValue b) -> ResolvedValue.of(fn.execute(format(a), format(b)));
    }

    private static OperatorFunction3 opFn3(Lambda3<String, String, String, String> fn) {
        return (ResolvedValue a, ResolvedValue b, ResolvedValue c) -> ResolvedValue.of(fn.execute(format(a), format(b), format(c)));
    }

    private static class MathFunction extends AbstractOptimizedFunction {
        private final String operator;
        private final ResolvedValue a;
        private final ResolvedValue b;

        public MathFunction(String operator, ResolvedValue a, ResolvedValue b) {
            this.operator = operator;
            this.a = a;
            this.b = b;
        }

        @Override
        public String asText() {
            return "(" + asTextNoBrackets() + ")";
        }

        private String asTextNoBrackets() {
            return format(a)
                    + operator
                    + format(b);
        }

        private String format(ResolvedValue v) {
            if (v instanceof MathFunction mv) {
                return switch (operator) {
                    case "+", "-" -> switch (mv.operator) {
                        case "+", "-" -> mv.asTextNoBrackets();
                        default -> FormulaOptimizer.format(v);
                    };
                    case "*", "/" -> switch (mv.operator) {
                        case "*", "/" -> mv.asTextNoBrackets();
                        default -> FormulaOptimizer.format(v);
                    };
                    default -> FormulaOptimizer.format(v);
                };
            }
            return FormulaOptimizer.format(v);
        }
    }

    private static class AnyFunction extends AbstractOptimizedFunction {
        private final List<ResolvedValue> values;

        private AnyFunction(List<ResolvedValue> values) {
            this.values = new ArrayList<>();
            boolean hasFalse = false;
            for (ResolvedValue next : values) {
                if (next instanceof AnyFunction anyFn) {
                    this.values.addAll(0, anyFn.values);
                    continue;
                }
                if (next.equals(ResolvedValue.FALSE)) {
                    hasFalse = true;
                    continue;
                }
                if (next.equals(ResolvedValue.TRUE)) {
                    this.values.clear();
                    this.values.add(ResolvedValue.TRUE);
                    return;
                }
                this.values.add(0, next);
            }

            if (this.values.isEmpty()) {
                this.values.add(ResolvedValue.of(!hasFalse));
            }
        }

        @Override
        public String asText() {
            if (values.size() == 1) {
                return format(this.values.get(0));
            }
            return "any(" + values.stream()
                    .map(FormulaOptimizer::format)
                    .collect(Collectors.joining(",")) + ")";
        }
    }

    private static class AllFunction extends AbstractOptimizedFunction {
        private final List<ResolvedValue> values;

        private AllFunction(List<ResolvedValue> values) {
            this.values = new ArrayList<>();
            for (ResolvedValue next : values) {
                if (next instanceof AllFunction allFn) {
                    this.values.addAll(0, allFn.values);
                    continue;
                }
                if (next.equals(ResolvedValue.TRUE)) {
                    continue;
                }
                if (next.equals(ResolvedValue.FALSE)) {
                    this.values.clear();
                    this.values.add(ResolvedValue.FALSE);
                    return;
                }
                this.values.add(0, next);
            }

            if (this.values.isEmpty()) {
                this.values.add(ResolvedValue.TRUE);
            }
        }

        @Override
        public String asText() {
            if (values.size() == 1) {
                return format(this.values.get(0));
            }
            return "all(" + values.stream()
                    .map(FormulaOptimizer::format)
                    .collect(Collectors.joining(",")) + ")";
        }
    }


    private static abstract class AbstractOptimizedFunction extends ResolvedValue {

        private static final UnsupportedOperationException NOT_AVAILABLE_EXCEPTION = new UnsupportedOperationException(
                "Not available for optimization");

        @Override
        public int asNumber() {
            throw NOT_AVAILABLE_EXCEPTION;
        }

        @Override
        public double asDecimal() {
            throw NOT_AVAILABLE_EXCEPTION;
        }

        @Override
        public boolean asBoolean() {
            throw NOT_AVAILABLE_EXCEPTION;
        }
    }
}
