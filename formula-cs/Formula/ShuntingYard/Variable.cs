namespace Formula.ShuntingYard;

public delegate ResolvedValue VariableResolver(IDataContext context, string key);

public readonly record struct Variable(string Key, VariableResolver VariableResolver) : IResolvable, INode
{
    public ResolvedValue Resolve(IDataContext context)
    {
        return VariableResolver(context, Key);
    }
}