namespace Formula;

public interface IResolvable
{
    ResolvedValue Resolve()
    {
        return Resolve(DataContext.Empty);
    }
    
    ResolvedValue Resolve(IDataContext context);
}

public static class Resolvable
{
    public static IResolvable Empty { get; } = new EmptyResolvable();
    public static IResolvable Just(ResolvedValue value)
    {
        return StaticResolvable.Of(value);
    }
    
    public static IResolvable Just(string value)
    {
        return Just(ResolvedValue.Of(value));
    }
    
    public static IResolvable Just(int value)
    {
        return Just(ResolvedValue.Of(value));
    }
    
    public static IResolvable Just(double value)
    {
        return Just(ResolvedValue.Of(value));
    }
    
    public static IResolvable Just(bool value)
    {
        return Just(ResolvedValue.Of(value));
    }
}

public class StaticResolvable : IResolvable
{
    private readonly ResolvedValue _value;

    public static IResolvable Of(ResolvedValue value)
    {
        return new StaticResolvable(value);
    }

    public ResolvedValue Resolve()
    {
        return _value;
    }

    public ResolvedValue Resolve(IDataContext context)
    {
        return Resolve();
    }
    
    private StaticResolvable(ResolvedValue value)
    {
        _value = value;
    }
}

internal class EmptyResolvable : IResolvable
{
    public ResolvedValue Resolve(IDataContext context)
    {
        return ResolvedValue.None;
    }
}