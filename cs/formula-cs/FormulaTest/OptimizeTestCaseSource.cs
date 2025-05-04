using YamlDotNet.Serialization;
using YamlDotNet.Serialization.NamingConventions;

namespace FormulaTest;

public class OptimizeTestCaseSource
{
    private static readonly string TestCaseFilePath = Path.Combine(TestContext.CurrentContext.TestDirectory, "..", "..",
        "..", "..", "..", "formula-test", "optimize-test-cases.yml");
    public static IEnumerable<TestCaseData> GetTestCases()
    {
        using TextReader input = File.OpenText(TestCaseFilePath);
        
        var deserializer = new DeserializerBuilder()
            .WithNamingConvention(UnderscoredNamingConvention.Instance)
            .Build();

        return deserializer
            .Deserialize<List<Case>>(input)
            .ConvertAll(testCase => new TestCaseData(testCase).SetName(testCase.Name));
    }
    
    public struct Case
    {
        public string Name;
        public string Formula;
        public string ExpectedFormula;
    }
}