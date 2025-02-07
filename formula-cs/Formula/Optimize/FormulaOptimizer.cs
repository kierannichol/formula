using System.Text;
using Formula.ShuntingYard;

namespace Formula.Optimize;

public class FormulaOptimizer
{
    private static readonly ShuntingYardParser Parser = ShuntingYardParser.Create()
            .Operator("^", 4, Associativity.Right, OpFn2((a, b) => a + "^" + b))
            .Operator("*", 3, Associativity.Left, (a,b) => new MathFunction("*", a, b))
            .Operator("/", 3, Associativity.Left, (a,b) => new MathFunction("/", a, b))
            .Operator("+", 2, Associativity.Left, (a,b) => new MathFunction("+", a, b))
            .Operator("-", 2, Associativity.Left, (a,b) => new MathFunction("-", a, b))
            .Operator("!", 2, Associativity.Left, OpFn1((a) => "!" + a))
            .Operator("<", 3, Associativity.Left, OpFn2((a, b) => a + "<" + b))
            .Operator("<=", 3, Associativity.Left, OpFn2((a, b) => a + "<=" + b))
            .Operator(">", 3, Associativity.Left, OpFn2((a, b) => a + ">" + b))
            .Operator(">=", 3, Associativity.Left, OpFn2((a, b) => a + ">=" + b))
            .Operator("==", 3, Associativity.Left, OpFn2((a, b) => a + "==" + b))
            .Operator("!=", 3, Associativity.Left, OpFn2((a, b) => a + "!=" + b))
            .Operator("AND", 1, Associativity.Left, (a, b) => new AllFunction(new List<ResolvedValue>(new[] {b, a})))
            .Operator("OR", 1, Associativity.Left, (a, b) => new AnyFunction(new List<ResolvedValue>(new[] {b, a})))
            .Operator("d", 4, Associativity.Left, OpFn2((a, b) => a + "d" + b))
            .Term("true", () => ResolvedValue.Of("true"))
            .Term("false", () => ResolvedValue.Of("false"))
            .Term("null", () => ResolvedValue.Of("null"))
            .Function("abs", OpFn1(a => $"abs({a})"))
            .Function("min", OpFn2((a, b) => $"min({a},{b})"))
            .Function("max", OpFn2((a, b) => $"max({a},{b})"))
            .Function("floor", OpFn1(a => $"floor({a})"))
            .Function("ceil", OpFn1(a => $"ceil({a})"))
            .Function("signed", OpFn1(a => $"signed({a})"))
            .Function("if", OpFn3((a, b, c) => $"if({a},{b},{c})"))
            .Function("concat", OpFn2((a, b) => $"concat({a},{b})"))
            .Function("ordinal", OpFn1(a => $"ordinal({a})"))
            .Function("any", a => new AnyFunction(new List<ResolvedValue>(a)))
            .Function("all", a => new AllFunction(new List<ResolvedValue>(a)))
            .Variable("@", (_, key) => ResolvedValue.Of($"@{key}"))
            .Variable("@{", "}", (_, key) => ResolvedValue.Of($"@{{{key}}}"))
            .Variable("min(@", ")", (_, key) => ResolvedValue.Of($"min(@{key})"))
            .Variable("max(@", ")", (_, key) => ResolvedValue.Of($"max(@{key})"))
            .Variable("sum(@", ")", (_, key) => ResolvedValue.Of($"sum(@{key})"))
            .Comment("[", "]", (value, comment) => NamedResolvedValue.Of(value, comment.Substring(1, comment.Length - 2)))
            ;

    public static string Optimize(string formulaText) {
        var resolved = Parser.Parse(formulaText).Resolve();
        if (resolved is MathFunction mf) {
            return mf.AsTextNoBrackets();
        }
        return Format(resolved);
    }

    private static string Format(ResolvedValue value)
    {
        return value switch
        {
            QuotedTextResolvedValue quoted => quoted.AsQuotedText(),
            NamedResolvedValue named => value.AsText() + "[" + named.AsName() + "]",
            _ => value.AsText()
        };
    }

    private static Func<ResolvedValue, ResolvedValue> OpFn1(Func<string, string> fn) {
        return a => ResolvedValue.Of(fn.Invoke(Format(a)));
    }

    private static Func<ResolvedValue, ResolvedValue, ResolvedValue> OpFn2(Func<string, string, string> fn) {
        return (a, b) => ResolvedValue.Of(fn.Invoke(Format(a), Format(b)));
    }

    private static Func<ResolvedValue, ResolvedValue, ResolvedValue, ResolvedValue> OpFn3(Func<string, string, string, string> fn) {
        return (a, b, c) => ResolvedValue.Of(fn.Invoke(Format(a), Format(b), Format(c)));
    }

    private class MathFunction : AbstractOptimizedFunction {
        private readonly string _operator;
        private readonly ResolvedValue _a;
        private readonly ResolvedValue _b;

        internal MathFunction(string op, ResolvedValue a, ResolvedValue b)
        {
            _operator = op;
            _a = a;
            _b = b;
        }
        
        public override string AsText() {
            return "(" + AsTextNoBrackets() + ")";
        }

        public string AsTextNoBrackets() {
            return FormatMath(_a)
                   + _operator
                   + FormatMath(_b);
        }

        private string FormatMath(ResolvedValue v) 
        {
            if (v is MathFunction mv)
            {
                return _operator switch
                {
                    "+" or "-" => mv._operator switch
                    {
                        "+" or "-" => mv.AsTextNoBrackets(),
                        _ => Format(v)
                    },
                    "*" or "/" => mv._operator switch
                    {
                        "*" or "/" => mv.AsTextNoBrackets(),
                        _ => Format(v)
                    },
                    _ => Format(v)
                };
            }
            return Format(v);
        }
    }

    private class AnyFunction : AbstractOptimizedFunction {
        private readonly List<ResolvedValue> _values;

        internal AnyFunction(List<ResolvedValue> values) {
            _values = new List<ResolvedValue>();
            var hasFalse = false;
            foreach (var next in values) 
            {
                if (next is AnyFunction anyFn) {
                    _values.InsertRange(0, anyFn._values);
                    continue;
                }

                if (next.Equals(False))
                {
                    hasFalse = true;
                    continue;
                }

                if (next.Equals(True))
                {
                    _values.Clear();
                    _values.Add(True);
                    return;
                }
                _values.Insert(0, next);
            }

            if (_values.Count == 0)
            {
                _values.Add(hasFalse ? False : True);
            }
        }
        
        public override string AsText() {
            if (_values.Count == 1) {
                return Format(_values[0]);
            }

            var builder = new StringBuilder();
            builder.Append("any(");
            for (var i = 0; i < _values.Count; i++)
            {
                var next = _values[i];
                if (i > 0)
                {
                    builder.Append(',');
                }

                builder.Append(Format(next));
            }

            builder.Append(')');
            return builder.ToString();
        }
    }

    private class AllFunction : AbstractOptimizedFunction 
    {
        private readonly List<ResolvedValue> _values;

        internal AllFunction(List<ResolvedValue> values) {
            _values = new List<ResolvedValue>();
            foreach (var next in values) 
            {
                if (next is AllFunction allFn) {
                    _values.InsertRange(0, allFn._values);
                    continue;
                }

                if (next.Equals(True))
                {
                    continue;
                }

                if (next.Equals(False))
                {
                    _values.Clear();
                    _values.Add(False);
                    return;
                }
                _values.Insert(0, next);
            }

            if (_values.Count == 0)
            {
                _values.Add(True);
            }
        }
        
        public override string AsText() 
        {
            if (_values.Count == 1) {
                return Format(_values[0]);
            }

            var builder = new StringBuilder();
            builder.Append("all(");
            for (var i = 0; i < _values.Count; i++)
            {
                var next = _values[i];
                if (i > 0)
                {
                    builder.Append(',');
                }

                builder.Append(Format(next));
            }

            builder.Append(')');
            return builder.ToString();
        }
    }


    private abstract class AbstractOptimizedFunction : ResolvedValue 
    {
        private static readonly InvalidOperationException NotAvailableException = new("Not available for optimization");
        
        public override int AsNumber() {
            throw NotAvailableException;
        }
        
        public override double AsDecimal() {
            throw NotAvailableException;
        }
        
        public override bool AsBoolean() {
            throw NotAvailableException;
        }
    }
}