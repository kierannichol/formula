using Formula;
using YamlDotNet.Core;
using YamlDotNet.Core.Events;
using YamlDotNet.Serialization;

namespace FormulaTest.Yaml;

public class ResolvedValueDeserializer : INodeDeserializer
{
    public bool Deserialize(IParser reader, Type expectedType, Func<IParser, Type, object?> nestedObjectDeserializer, out object? value,
        ObjectDeserializer rootDeserializer)
    {
        if (expectedType != typeof(ResolvedValue))
        {
            value = null;
            return false;
        }
        
        var scalar = reader.Consume<Scalar>();
        value = ResolvedValue.Of(scalar.Value);
        return true;
    }
}