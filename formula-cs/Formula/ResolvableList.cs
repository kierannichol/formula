namespace Formula;

public class ResolvableList : IResolvable
{
    private readonly List<IResolvable?> _values = new();

    public void Push(IResolvable? value)
    {
        _values.Add(value);
    }
    
    public ResolvedValue Resolve(IDataContext context)
    {
        var resolved = new List<ResolvedValue>();
        foreach (var resolvable in _values)
        {
            resolved.Add(resolvable.Resolve(context));
        }
        return new ResolvedListValue(resolved);
    }
}