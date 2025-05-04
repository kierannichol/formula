using Formula;
using YamlDotNet.Core;
using YamlDotNet.Core.Events;
using YamlDotNet.Serialization;

namespace FormulaTest.Yaml;

public class DataContextDeserializer : INodeDeserializer
{
    public bool Deserialize(IParser reader, Type expectedType, Func<IParser, Type, object?> nestedObjectDeserializer, out object? value,
        ObjectDeserializer rootDeserializer)
    {
        if (expectedType != typeof(DataContext))
        {
            value = null;
            return false;
        }
        var result = rootDeserializer.Invoke(typeof(Dictionary<string, IResolvable>));
        if (result == null)
        {
            value = null;
            return true;
        }
        value = new DataContext((Dictionary<string, IResolvable?>) result);
        return true;
    }
}