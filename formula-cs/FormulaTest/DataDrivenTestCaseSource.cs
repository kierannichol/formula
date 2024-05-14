using System.Data;

namespace FormulaTest;

public class DataDrivenTestCaseSource
{
    public static IEnumerable<TestCaseData> GetTestCases()
    {
        var lines = File.ReadLines(Path.Combine(TestContext.CurrentContext.TestDirectory, "..", "..", "..", "..", "..", "formula-test", "optimize-test-cases.csv")).Skip(1);

        foreach (var line in lines)
        {
            var parts = line.Split("|");
            yield return new TestCaseData(new Case(parts[1].Trim(), parts[2].Trim()))
                .SetName(parts[0].Trim());
        }
    }
    
    public class Case(string given, string expected)
    {
        public string Given { get; } = given;
        public string Expected { get; } = expected;
    }
}