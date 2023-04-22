using System.Text.RegularExpressions;

namespace Formula;

public interface IDataContext
{
    ResolvedValue Get(string key);
    IEnumerable<string> Keys();
}

public class DataContext : IDataContext
{
    public static readonly IDataContext Empty = new EmptyDataContext();

    private readonly Dictionary<string, IResolvable> _data;

    public static DataContext Of(IDictionary<string, IResolvable> values)
    {
        return new DataContext(values);
    }

    public ResolvedValue Get(string key)
    {
        return _data.TryGetValue(key, out var found) 
            ? found.Resolve(this) 
            : ResolvedValue.None;
    }

    public IEnumerable<string> Keys()
    {
        return _data.Keys;
    }

    public DataContext Set(string key, IResolvable value)
    {
        _data[key] = value;
        return this;
    }

    public DataContext Set(string key, ResolvedValue value)
    {
        _data[key] = Resolvable.Just(value);
        return this;
    }

    public DataContext Set(string key, string value)
    {
        return Set(key, ResolvedValue.Of(value));
    }
    
    public DataContext Set(string key, int value)
    {
        return Set(key, ResolvedValue.Of(value));
    }
    
    public DataContext Set(string key, double value)
    {
        return Set(key, ResolvedValue.Of(value));
    }
    
    public DataContext Set(string key, bool value)
    {
        return Set(key, ResolvedValue.Of(value));
    }

    public DataContext()
    {
        _data = new Dictionary<string, IResolvable>();
    }
    
    public DataContext(IDictionary<string, IResolvable> values)
    {
        _data = new Dictionary<string, IResolvable>(values);
    }
}

internal class EmptyDataContext : IDataContext
{
    public ResolvedValue Get(string key)
    {
        return ResolvedValue.None;
    }

    public IEnumerable<string> Keys()
    {
        return Enumerable.Empty<string>();
    }
}