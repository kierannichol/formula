using System.Text.RegularExpressions;
using Formula.ShuntingYard;

namespace Formula;

public static class Formula
{
    private static readonly ShuntingYardParser Parser = ShuntingYardParser.Create()
            .Operator("^", 4, Associativity.Right, (a, b) => ResolvedValue.Of(Math.Pow(a.AsDecimal(), b.AsDecimal())))
            .Operator("*", 3, Associativity.Left, (a, b) => ResolvedValue.Of(a.AsDecimal() * b.AsDecimal()))
            .Operator("/", 3, Associativity.Left, (a, b) => ResolvedValue.Of(a.AsDecimal() / b.AsDecimal()))
            .Operator("+", 2, Associativity.Left, (a, b) => ResolvedValue.Of(a.AsDecimal() + b.AsDecimal()))
            .BiOperator("-", 
                new Operator1("-", 4, Associativity.Left, a => ResolvedValue.Of(-a.AsDecimal())),
                new Operator2("-", 2, Associativity.Left, (a, b) => ResolvedValue.Of(a.AsDecimal() - b.AsDecimal())))
            .Operator("!", 2, Associativity.Left, a => ResolvedValue.Of(!a.AsBoolean()))
            .Operator("<", 3, Associativity.Left, (a, b) => ResolvedValue.Of(a.AsDecimal() < b.AsDecimal()))
            .Operator("<=", 3, Associativity.Left, (a, b) => ResolvedValue.Of(a.AsDecimal() <= b.AsDecimal()))
            .Operator(">", 3, Associativity.Left, (a, b) => ResolvedValue.Of(a.AsDecimal() > b.AsDecimal()))
            .Operator(">=", 3, Associativity.Left, (a, b) => ResolvedValue.Of(a.AsDecimal() >= b.AsDecimal()))
            .Operator("==", 3, Associativity.Left, (a, b) => ResolvedValue.Of(a.Equals(b)))
            .Operator("!=", 3, Associativity.Left, (a, b) => ResolvedValue.Of(!a.Equals(b)))
            .Operator("AND", 1, Associativity.Left, (a, b) => ResolvedValue.Of(a.AsBoolean() && b.AsBoolean()))
            .Operator("OR", 1, Associativity.Left, (a, b) => ResolvedValue.Of(a.AsBoolean() || b.AsBoolean()))
            .Term("true", () => ResolvedValue.Of(true))
            .Term("false", () => ResolvedValue.Of(false))
            .Term("null", () => ResolvedValue.None)
            .Function("abs", a => ResolvedValue.Of(Math.Abs(a.AsDecimal())))
            .Function("min", (a, b) => ResolvedValue.Of(Math.Min(a.AsDecimal(), b.AsDecimal())))
            .Function("max", (a, b) => ResolvedValue.Of(Math.Max(a.AsDecimal(), b.AsDecimal())))
            .Function("floor", a => ResolvedValue.Of(Math.Floor(a.AsDecimal())))
            .Function("ceil", a => ResolvedValue.Of(Math.Ceiling(a.AsDecimal())))
            .Function("signed", a => ResolvedValue.Of((a.AsNumber() < 0 ? "" : "+") + a.AsNumber()))
            .Function("if", (a, b, c) => a.AsBoolean() ? b : c)
            .Function("concat", (a, b) => ResolvedValue.Of(a.AsText() + b.AsText()))
            .Function("ordinal", a => ResolvedValue.Of(Ordinal.ToString(a.AsNumber())))
            .Function("any", AnyFunction)
            .Function("all", AllFunction)
            .Variable("@", FindVariable)
            .Variable("@{", "}", FindVariable)
            .Variable("min(@", ")", MinFunction)
            .Variable("max(@", ")", MaxFunction)
            .Variable("sum(@", ")", SumFunction)
            .Comment("[", "]", (value, comment) => NamedResolvedValue.Of(value, comment.Substring(1, comment.Length - 2)))
            ;

    public static IResolvable Parse(string formulaText)
    {
        return formulaText.Length == 0 
            ? Resolvable.Empty 
            : Parser.Parse(formulaText);
    }

    private static ResolvedValue AnyFunction(IEnumerable<ResolvedValue> values)
    {
        foreach (var value in values)
        {
            if (value.AsBoolean())
            {
                return ResolvedValue.True;
            }
        }

        return ResolvedValue.False;
    }
    
    private static ResolvedValue AllFunction(IEnumerable<ResolvedValue> values)
    {
        foreach (var value in values)
        {
            if (!value.AsBoolean())
            {
                return ResolvedValue.False;
            }
        }

        return ResolvedValue.True;
    }

    private static ResolvedValue FindVariable(IDataContext context, string key)
    {
        return context.Get(key);
    }

    private static ResolvedValue SumFunction(IDataContext context, string key)
    {
        FindAndReduce(context, key, AddFunction, ResolvedValue.Zero, out var reduced);
        return reduced;
    }

    private static ResolvedValue AddFunction(ResolvedValue a, ResolvedValue b)
    {
        if (a.Equals(ResolvedValue.None) && b.Equals(ResolvedValue.None))
        {
            return ResolvedValue.None;
        }
        return ResolvedValue.Of(a.AsDecimal() + b.AsDecimal());
    }
    
    private static ResolvedValue MaxFunction(IDataContext context, string key)
    {
        FindAndReduce(context, key, Max, ResolvedValue.None, out var reduced);
        return reduced;
        ResolvedValue Max(ResolvedValue a, ResolvedValue b) => a.AsDecimal() > b.AsDecimal() ? a : b;
    }
    
    private static ResolvedValue MinFunction(IDataContext context, string key)
    {
        var maxValue = ResolvedValue.Of(int.MaxValue);
        ResolvedValue Min(ResolvedValue a, ResolvedValue b) => a.AsDecimal() < b.AsDecimal() ? a : b;
        return FindAndReduce(context, key, Min, maxValue, out var reduced) > 0 ? reduced : ResolvedValue.None;
    }

    private static int FindAndReduce<T>(IDataContext context, string pattern,
        Func<T, ResolvedValue, T> reduceFunction, T initialValue, out T reduced)
    {
        var formatted = pattern.Replace("*", ".*");
        var regex = new Regex($"^{formatted}$");
        reduced = initialValue;
        var count = 0;
        foreach (var key in context.Keys())
        {
            if (!Predicate(key)) continue;
            reduced = reduceFunction(reduced, context.Get(key));
            count++;
        }

        return count;
        bool Predicate(string key) => regex.IsMatch(key);
    }
}