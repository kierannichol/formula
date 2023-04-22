using Formula;

namespace FormulaTest;

public class FormulaPerformanceTest
{
    private const int Iterations = 1000;
    private const string FormulaText = "@alpha AND (@beta OR @delta) AND @sigma AND (@omega >= 5)";
    
    [Test]
    public void ParsePerformance()
    {
        var startTime = DateTime.Now;
        for (var i = 0; i < Iterations; i++)
        {
            Formula.Formula.Parse(FormulaText);
        }
        var endTime = DateTime.Now;
        var total = endTime - startTime;
        var average = total / Iterations;
        
        Console.WriteLine($"Parse Total: {total.TotalMilliseconds} ms");
        Console.WriteLine($"Parse Average: {average.TotalMilliseconds} ms");
    }
    
    [Test]
    public void ResolvePerformance()
    {
        var formula = Formula.Formula.Parse(FormulaText);
        var context = DataContext.Of(new Dictionary<string, IResolvable>
        {
            ["alpha"] = Resolvable.Just("true"),
            ["beta"] = Resolvable.Just(1),
            ["delta"] = Resolvable.Just(0),
            ["sigma"] = Resolvable.Just("Not a member"),
            ["omega"] = Resolvable.Just("22"),
        });
        for (var j = 0; j < 20000; j++)
        {
            context.Set($"key_{j}", $"value_{j}");
        }
        var startTime = DateTime.Now;
        for (var i = 0; i < Iterations; i++)
        {
            formula.Resolve(context);
        }
        var endTime = DateTime.Now;
        var total = endTime - startTime;
        var average = total / Iterations;
        
        Console.WriteLine($"Resolve Total: {total.TotalMilliseconds} ms");
        Console.WriteLine($"Resolve Average: {average.TotalMilliseconds} ms");
    }
}