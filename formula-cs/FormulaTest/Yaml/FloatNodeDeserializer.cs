using YamlDotNet.Core;
using YamlDotNet.Core.Events;
using YamlDotNet.Serialization;

namespace FormulaTest.Yaml;

public class FloatNodeDeserializer : INodeDeserializer
{
    private static readonly Dictionary<Tuple<Type, string>, object> SpecialFloats =
        new()
        {
            { Tuple.Create(typeof(int?), "NaN"), null },
            { Tuple.Create(typeof(float), "NaN"), float.NaN },
            { Tuple.Create(typeof(double), "NaN"), double.NaN },
        };

    public bool Deserialize(IParser reader, Type expectedType, Func<IParser, Type, object?> nestedObjectDeserializer, out object? value,
        ObjectDeserializer rootDeserializer)
    {
        reader.Accept(out Scalar scalar);
        if (scalar == null) 
        {
            value = null;
            return false;
        }

        var found = SpecialFloats.TryGetValue(
            Tuple.Create(expectedType, scalar.Value),
            out value);

        if (found) 
        {
            reader.TryConsume(out scalar);
        }
        return found;
    }
}