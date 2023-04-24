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
            .Operator("-", 2, Associativity.Left, (a, b) => ResolvedValue.Of(a.AsDecimal() - b.AsDecimal()))
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
            ;

    public static IResolvable Parse(string formulaText)
    {
        return formulaText.Length == 0 
            ? Resolvable.Empty 
            : Parser.Parse(formulaText);
    }

    // public static string optimize(string formulaText) {
    //     return FormulaOptimizer.optimize(formulaText);
    // }

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

    private static IResolvable FindVariable(IDataContext context, string key)
    {
        return context.TryGet(key, out var found) ? found : Resolvable.Empty;
    }

    private static IResolvable SumFunction(IDataContext context, string key)
    {
        ResolvedValue Add(ResolvedValue a, ResolvedValue b) => ResolvedValue.Of(a.AsDecimal() + b.AsDecimal());
        FindAndReduce(context, key, Add, ResolvedValue.Zero, out var reduced);
        return Resolvable.Just(reduced);
    }
    
    private static IResolvable MaxFunction(IDataContext context, string key)
    {
        ResolvedValue Max(ResolvedValue a, ResolvedValue b) => a.AsDecimal() > b.AsDecimal() ? a : b;
        FindAndReduce(context, key, Max, ResolvedValue.Zero, out var reduced);
        return Resolvable.Just(reduced);
    }
    
    private static IResolvable MinFunction(IDataContext context, string key)
    {
        var maxValue = ResolvedValue.Of(int.MaxValue);
        ResolvedValue Min(ResolvedValue a, ResolvedValue b) => a.AsDecimal() < b.AsDecimal() ? a : b;
        return FindAndReduce(context, key, Min, maxValue, out var reduced) > 0 ? Resolvable.Just(reduced) : Resolvable.Empty;
    }

    private static int FindAndReduce<T>(IDataContext context, string pattern,
        Func<T, ResolvedValue, T> reduceFunction, T initialValue, out T reduced)
    {
        var formatted = pattern.Replace("*", ".*");
        var regex = new Regex($"^{formatted}$");
        bool Predicate(string key) => regex.IsMatch(key);
        reduced = initialValue;
        var count = 0;
        foreach (var key in context.Keys())
        {
            if (!Predicate(key)) continue;
            reduced = reduceFunction(reduced, context.Resolve(key));
            count++;
        }

        return count;
    }
}