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
        .Operator(",", 1, Associativity.Left, Formula.MergeLists)
        .Term("true", () => ResolvedValue.Of(true))
        .Term("false", () => ResolvedValue.Of(false))
        .Term("null", () => ResolvedValue.None)
        .Function("abs", a => ResolvedValue.Of(Math.Abs(a.AsDecimal())))
        .Function("min", MinFunction)
        .Function("max", MaxFunction)
        .Function("sum", SumFunction)
        .Function("clamp", ClampFunction)
        .Function("maxeach", MaxEachFunction)
        .Function("mineach", MinEachFunction)
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
        .Comment("[", "]", (value, comment) => NamedResolvedValue.Of(value, comment.Substring(1, comment.Length - 2)));

    private static ResolvedValue MaxEachFunction(ResolvedValue arg)
    {
        var values = new List<ResolvedValue>();
        foreach (var next in arg.AsList())
        {
            values.Add(MaxFunction(next));
        }
        return ResolvedValue.Of(values);
    }
    
    private static ResolvedValue MinEachFunction(ResolvedValue arg)
    {
        var values = new List<ResolvedValue>();
        foreach (var next in arg.AsList())
        {
            values.Add(MinFunction(next));
        }
        return ResolvedValue.Of(values);
    }

    private static ResolvedValue ClampFunction(ResolvedValue value, ResolvedValue min, ResolvedValue max)
    {
        if (value.AsDecimal() < min.AsDecimal())
        {
            return min;
        }

        if (value.AsDecimal() > max.AsDecimal())
        {
            return max;
        }

        return value;
    }

    private static ResolvedValue MinFunction(ResolvedValue arg)
    {
        ResolvedValue? min = null;
        foreach (var value in arg.AsList())
        {
            if (min == null || value.AsDecimal() < min.AsDecimal())
            {
                min = value;
            }
        }
        return min ?? ResolvedValue.None;
    }
    
    private static ResolvedValue MaxFunction(ResolvedValue arg)
    {
        ResolvedValue? max = null;
        foreach (var value in arg.AsList())
        {
            if (max == null || value.AsDecimal() > max.AsDecimal())
            {
                max = value;
            }
        }
        return max ?? ResolvedValue.None;
    }

    private static ResolvedValue MergeLists(ResolvedValue a, ResolvedValue b)
    {
        var values = new List<ResolvedValue>();
        values.AddRange(a.AsList());
        values.AddRange(b.AsList());
        return ResolvedValue.Of(values);
    }

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
            foreach (var next in value.AsList())
            {
                if (next.AsBoolean())
                {
                    return ResolvedValue.True;
                }
            }
        }

        return ResolvedValue.False;
    }

    private static ResolvedValue AllFunction(IEnumerable<ResolvedValue> values)
    {
        foreach (var value in values)
        {
            foreach (var next in value.AsList())
            {
                if (!next.AsBoolean())
                {
                    return ResolvedValue.False;
                }
            }
        }

        return ResolvedValue.True;
    }

    private static ResolvedValue FindVariable(IDataContext context, string key)
    {
        return key.Contains('*') 
            ? ResolvedValue.Of(Find(context, key)) 
            : context.Get(key);
    }

    private static ResolvedValue SumFunction(ResolvedValue value)
    {
        var list = value.AsList();
        if (list.Count == 0)
        {
            return ResolvedValue.Zero;
        }
        if (list.Count == 1)
        {
            return value;
        }
        
        var sum = 0.0;
        foreach (var next in value.AsList())
        {
            sum += SumFunction(next).AsDecimal();
        }
        return ResolvedValue.Of(sum);
    }
    
    private static List<ResolvedValue> Find(IDataContext context, string pattern)
    {
        var foundList = new List<ResolvedValue>();
        
        var formatted = pattern.Replace("*", ".*");
        var regex = new Regex($"^{formatted}$");
        foreach (var key in context.Keys())
        {
            if (!Predicate(key)) continue;
            var found = context.Get(key);
            foundList.Add(found);
        }

        return foundList;
        bool Predicate(string key) => regex.IsMatch(key);
    }
}