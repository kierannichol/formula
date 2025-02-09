using Formula;
using YamlDotNet.Core;
using YamlDotNet.Core.Events;
using YamlDotNet.Serialization;

namespace FormulaTest.Yaml;

public class ResolvableDeserializer : INodeDeserializer
{
    public bool Deserialize(IParser reader, Type expectedType, Func<IParser, Type, object?> nestedObjectDeserializer, out object? value,
        ObjectDeserializer rootDeserializer)
    {
        if (expectedType != typeof(IResolvable))
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

            value = Resolvable.Just(ResolvedValue.Of(values));
            return true;
        }
        
        var scalar = reader.Consume<Scalar>();

        if (scalar.Value.StartsWith("{") && scalar.Value.EndsWith("}"))
        {
            value = Formula.Formula.Parse(scalar.Value.Substring(1, scalar.Value.Length - 2));
            return true;
        }
        
        value = Resolvable.Just(scalar.Value);
        return true;
    }
}