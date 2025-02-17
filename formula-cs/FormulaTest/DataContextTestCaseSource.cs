using Formula;
using FormulaTest.Yaml;
using YamlDotNet.Serialization;
using YamlDotNet.Serialization.NamingConventions;

namespace FormulaTest;

public class DataContextTestCaseSource
{
    private static readonly string TestCaseFilePath = Path.Combine(TestContext.CurrentContext.TestDirectory, "..", "..",
        "..", "..", "..", "formula-test", "data-context-test-cases.yml");
    
    public static IEnumerable<TestCaseData> GetTestCases()
    {
        using TextReader input = File.OpenText(TestCaseFilePath);
        
        var deserializer = new DeserializerBuilder()
            .WithNamingConvention(UnderscoredNamingConvention.Instance)
            .WithNodeDeserializer(new FloatNodeDeserializer())
            .WithNodeDeserializer(new ResolvableDeserializer())
            .WithNodeDeserializer(new ResolvedValueDeserializer())
            .WithNodeDeserializer(new DataContextDeserializer())
            .Build();

        return deserializer
            .Deserialize<List<Case>>(input)
            .ConvertAll(testCase => new TestCaseData(testCase).SetName(testCase.Name));
    }
    
    public struct Case
    {
        public string Name;
        public Dictionary<string, IResolvable?>? Data;
        public List<string> Actions;
        public Dictionary<string, ExpectedValues> Expected;
    }
}