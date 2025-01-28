namespace Formula.ShuntingYard;

public delegate ResolvedValue VariableResolver(IDataContext context, string key);

public readonly record struct Variable(string Key, VariableResolver VariableResolver) : INode
{
    public ResolvedValue Get(IDataContext context)
    {
        return VariableResolver(context, Key);
    }
}