using Formula;
using FormulaTest.Assertions;

namespace FormulaTest;

using static ResolvedValueAssertions;

public class DataContextTest
{
    [Test, TestCaseSource(typeof(DataContextTestCaseSource), nameof(DataContextTestCaseSource.GetTestCases))]
    public void DataDrivenTestCases(DataContextTestCaseSource.Case data)
    {
        var context = data.Data != null ? new DataContext(data.Data) : new DataContext();

        foreach (var dataActionText in data.Actions)
        {
            Execute(dataActionText, context);
        }
        
        foreach (var (key, value) in data.Expected)
        {
            if (value.ExpectedText != null)
            {
                AssertResolvedValue(context.Get(key)).HasValue(value.ExpectedText);
            }
        }
    }

    private void Execute(string actionText, DataContext context)
    {
        var parts = actionText.Split(" ");
        if (parts is ["SET", _, _])
        {
            context.Set(parts[1], ParseValue(parts[2]));
        }
        if (parts is ["PUSH", _, _])
        {
            context.Push(parts[1], ParseValue(parts[2]));
        }
    }

    private static IResolvable ParseValue(string text)
    {
        if (text.StartsWith("{") && text.EndsWith("}"))
        {
            return Formula.Formula.Parse(text.Substring(1, text.Length - 2)) ?? Resolvable.Empty;
        }

        return Resolvable.Just(text);
    }
}