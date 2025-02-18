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
        .Operator("d", 4, Associativity.Left, (a, b) => new ResolvedRollValue(a.AsNumber(), b.AsNumber()))
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
        .Function("concat", ConcatFunction)
        .Function("ordinal", a => ResolvedValue.Of(Ordinal.ToString(a.AsNumber())))
        .Function("any", AnyFunction)
        .Function("all", AllFunction)
        .Variable("@", FindVariable)
        .Variable("@{", "}", FindVariable)
        .Variable("min(@", ")", MinFunction)
        .Variable("max(@", ")", MaxFunction)
        .Variable("sum(@", ")", SumFunction)
        .Variable("sum(max(@", "))", SumMaxFunction)
        .Variable("sum(min(@", "))", SumMinFunction)
        .Comment("[", "]", (value, comment) => NamedResolvedValue.Of(value, comment.Substring(1, comment.Length - 2)));

    private static ResolvedValue ConcatFunction(IEnumerable<ResolvedValue> arg)
    {
        var collected = new List<ResolvedValue>();
        foreach (var resolvedValue in arg)
        {
            collected.AddRange(resolvedValue.AsList());
        }

        return new ResolvedListValue(collected);
    }

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
        return FindAndReduce(context, key, AddReduceFunction, ResolvedValue.Zero, out var reduced) > 0
            ? reduced
            : ResolvedValue.Zero;
    }

    private static ResolvedValue SumMaxFunction(IDataContext context, string key)
    {
        return FindAndReduce2(context, key, MaxReduceFunction, AddReduceFunction, ResolvedValue.Zero, out var reduced) >
               0
            ? reduced
            : ResolvedValue.Zero;
    }

    private static ResolvedValue SumMinFunction(IDataContext context, string key)
    {
        return FindAndReduce2(context, key, MinReduceFunction, AddReduceFunction, ResolvedValue.Zero, out var reduced) >
               0
            ? reduced
            : ResolvedValue.Zero;
    }

    private static ResolvedValue MaxFunction(IDataContext context, string key)
    {
        return FindAndReduce(context, key, MaxReduceFunction, ResolvedValue.None, out var reduced) > 0
            ? reduced
            : ResolvedValue.None;
    }

    private static ResolvedValue MinFunction(IDataContext context, string key)
    {
        return FindAndReduce(context, key, MinReduceFunction, ResolvedValue.None, out var reduced) > 0
            ? reduced
            : ResolvedValue.None;
    }

    private static ResolvedValue AddReduceFunction(ResolvedValue a, ResolvedValue b)
    {
        if (!a.HasValue && !b.HasValue)
        {
            return ResolvedValue.Zero;
        }

        return ResolvedValue.Of(a.AsDecimal() + b.AsDecimal());
    }

    private static ResolvedValue MaxReduceFunction(ResolvedValue a, ResolvedValue b)
    {
        return CheckForNone(a,b) ?? (a.AsDecimal() > b.AsDecimal() ? a : b);
    }

    private static ResolvedValue MinReduceFunction(ResolvedValue a, ResolvedValue b)
    {
        return CheckForNone(a,b) ?? (a.AsDecimal() < b.AsDecimal() ? a : b);
    }

    private static ResolvedValue? CheckForNone(ResolvedValue a, ResolvedValue b)
    {
        if (a is { HasValue: false }) return b;
        return b is { HasValue: false } ? a : null;
    }

    private static int FindAndReduce<T>(IDataContext context, string pattern,
        Func<T, ResolvedValue, T> reduceFunction, T initialValue, out T reduced)
    {
        reduced = initialValue;
        var count = 0;
        if (!pattern.Contains('*'))
        {
            var found = context.Get(pattern);
            foreach (var value in found.AsList())
            {
                reduced = reduceFunction(reduced, value);
                count++;
            }

            return count;
        }

        var formatted = pattern.Replace("*", ".*");
        var regex = new Regex($"^{formatted}$");
        foreach (var key in context.Keys())
        {
            if (!Predicate(key)) continue;
            var found = context.Get(key);
            foreach (var value in found.AsList())
            {
                reduced = reduceFunction(reduced, value);
                count++;
            }
        }

        return count;
        bool Predicate(string key) => regex.IsMatch(key);
    }

    private static int FindAndReduce2<T>(IDataContext context, string pattern,
        Func<ResolvedValue, ResolvedValue, ResolvedValue> reduceFunctionA,
        Func<T, ResolvedValue, T> reduceFunctionB,
        T initialValue, out T reduced)
    {
        reduced = initialValue;
        var count = 0;

        var formatted = pattern.Replace("*", ".*");
        var regex = new Regex($"^{formatted}$");
        foreach (var key in context.Keys())
        {
            if (!Predicate(key)) continue;
            var found = context.Get(key);
            var reducedA = ResolvedValue.None;
            foreach (var value in found.AsList())
            {
                reducedA = reduceFunctionA(reducedA, value);
            }

            reduced = reduceFunctionB(reduced, reducedA);
            count++;
        }

        return count;
        bool Predicate(string key) => regex.IsMatch(key);
    }
}