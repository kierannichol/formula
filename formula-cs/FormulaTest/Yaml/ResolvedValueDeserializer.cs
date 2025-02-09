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

        if (reader.TryConsume(out SequenceStart? _))
        {
            var values = new List<ResolvedValue>();
            while (!reader.TryConsume(out SequenceEnd? _))
            {
                values.Add(ResolvedValue.Of(reader.Consume<Scalar>().Value));
            }

            value = ResolvedValue.Of(values);
            return true;
        }
        
        var scalar = reader.Consume<Scalar>();
        value = ResolvedValue.Of(scalar.Value);
        return true;
    }
}