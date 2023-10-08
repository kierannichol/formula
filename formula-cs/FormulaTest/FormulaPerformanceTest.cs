using Formula;

namespace FormulaTest;

public class FormulaPerformanceTest
{
    private const int Iterations = 1000;
    private const string FormulaText = "@alpha AND (@beta OR @delta) AND @sigma AND (@omega >= 5)";
    
    [Test]
    public void ParsePerformance()
    {
        var startTime = DateTime.UtcNow;
        for (var i = 0; i < Iterations; i++)
        {
            Formula.Formula.Parse(FormulaText);
        }
        var endTime = DateTime.UtcNow;
        var total = endTime - startTime;
        var average = total / Iterations;
        
        Console.WriteLine($"Parse Average: {average.TotalMilliseconds} ms");
    }
    
    [Test]
    public void ResolvePerformance()
    {
        var formula = Formula.Formula.Parse(FormulaText);
        var context = new DataContext(new Dictionary<string, IResolvable>
        {
            ["alpha"] = Resolvable.Just("true"),
            ["beta"] = Formula.Formula.Parse("!@delta"),
            ["delta"] = Formula.Formula.Parse("true AND @alpha"),
            ["sigma"] = Formula.Formula.Parse("@alpha AND @beta"),
            ["omega"] = Resolvable.Just("22"),
        });
        for (var j = 0; j < 20000; j++)
        {
            context.Set($"key_{j}", $"value_{j}");
        }
        var startTime = DateTime.UtcNow;
        for (var i = 0; i < Iterations; i++)
        {
            formula.Resolve(context);
        }
        var endTime = DateTime.UtcNow;
        var total = endTime - startTime;
        var average = total / Iterations;
        
        Console.WriteLine($"Resolve Average: {average.TotalMilliseconds} ms");
    }
    
    [Test]
    public void DeepResolvePerformance()
    {
        const int depth = 1000;
        var formula = Formula.Formula.Parse($"@step_{depth}");
        var context = new DataContext(new Dictionary<string, IResolvable>
        {
            ["step_1"] = Resolvable.Just(1)
        });
        for (var j = 2; j <= depth; j++)
        {
            context.Set($"step_{j}", Formula.Formula.Parse($"@step_{j-1} + 1"));
        }
        var startTime = DateTime.UtcNow;
        for (var i = 0; i < Iterations; i++)
        {
            formula.Resolve(context);
        }
        var endTime = DateTime.UtcNow;
        var total = endTime - startTime;
        var average = total / Iterations;
        
        Console.WriteLine($"Deep Resolve Average: {average.TotalMilliseconds} ms");
    }
}