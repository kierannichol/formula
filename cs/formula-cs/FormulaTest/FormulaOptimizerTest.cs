using Formula.Optimize;

namespace FormulaTest;

public class FormulaOptimizerTest
{
    [Test, TestCaseSource(typeof(OptimizeTestCaseSource), nameof(OptimizeTestCaseSource.GetTestCases))]
    public void DataDrivenTestCases(OptimizeTestCaseSource.Case data)
    {
        var optimized = FormulaOptimizer.Optimize(data.Formula);
        Assert.That(optimized, Is.EqualTo(data.ExpectedFormula));
    }
}