namespace Formula;

public interface IDataContext
{
    bool TryGet(string key, out ResolvedValue value);
    ResolvedValue Get(string key);
    IEnumerable<string> Keys();
}

public class DataContext : IDataContext
{
    public static readonly IDataContext Empty = new EmptyDataContext();

    private readonly Dictionary<string, IResolvable> _data;
    
    public DataContext()
    {
        _data = new Dictionary<string, IResolvable>();
    }
    
    public DataContext(IDictionary<string, IResolvable> values)
    {
        _data = new Dictionary<string, IResolvable>(values);
    }

    public bool TryGet(string key, out ResolvedValue resolvable)
    {
        if (_data.TryGetValue(key, out var found))
        {
            resolvable = found.Resolve(this);
            return true;
        }
        resolvable = ResolvedValue.None;
        return false;
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
}

internal class EmptyDataContext : IDataContext
{
    public bool TryGet(string key, out ResolvedValue resolvable)
    {
        resolvable = ResolvedValue.None;
        return false;
    }

    public ResolvedValue Get(string key)
    {
        return ResolvedValue.None;
    }

    public IEnumerable<string> Keys()
    {
        return Enumerable.Empty<string>();
    }
}