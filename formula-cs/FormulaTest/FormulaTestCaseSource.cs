using Formula;
using FormulaTest.Yaml;
using YamlDotNet.Serialization;
using YamlDotNet.Serialization.NamingConventions;

namespace FormulaTest;

public class FormulaTestCaseSource
{
    private static readonly string TestCaseFilePath = Path.Combine(TestContext.CurrentContext.TestDirectory, "..", "..",
        "..", "..", "..", "formula-test", "formula-test-cases.yml");
    public static IEnumerable<TestCaseData> GetTestCases()
    {
        using TextReader input = File.OpenText(TestCaseFilePath);
        
        var deserializer = new DeserializerBuilder()
            .WithNamingConvention(UnderscoredNamingConvention.Instance)
            .WithNodeDeserializer(new FloatNodeDeserializer())
            .WithNodeDeserializer(new ResolvableDeserializer())
            .WithNodeDeserializer(new DataContextDeserializer())
            .Build();

        return deserializer
            .Deserialize<List<Case>>(input)
            .ConvertAll(testCase => new TestCaseData(testCase).SetName(testCase.Name));
    }
    
    public struct Case
    {
        public string Name;
        public string Formula;
        public string? ExpectedText;
        public double? ExpectedNumber;
        public bool? ExpectedBoolean;
        public string? ExpectedError;
        public string? ExpectedName;
        public DataContext? Data;
    }
}