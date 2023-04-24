namespace Formula.ShuntingYard;

public delegate IResolvable VariableResolver(IDataContext context, string key);

public readonly record struct Variable(string Key, VariableResolver VariableResolver) : INode
{
    public IResolvable Get(IDataContext context)
    {
        return VariableResolver(context, Key);
    }
}