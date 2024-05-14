using Formula.Optimize;

namespace FormulaTest;

public class FormulaOptimizerTest
{
    [Test, TestCaseSource(typeof(DataDrivenTestCaseSource), nameof(DataDrivenTestCaseSource.GetTestCases))]
    public void DataDrivenTestCases(DataDrivenTestCaseSource.Case data)
    {
        var optimized = FormulaOptimizer.Optimize(data.Given);
        Assert.That(optimized, Is.EqualTo(data.Expected));
    }
}