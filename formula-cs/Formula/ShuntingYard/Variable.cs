namespace Formula.ShuntingYard;

public readonly record struct Variable(string Key, Resolver Resolver) : IResolvable, INode
{
    public ResolvedValue Resolve(IDataContext context)
    {
        return Resolver(context, Key);
    }
}