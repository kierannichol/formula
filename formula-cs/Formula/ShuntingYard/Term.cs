namespace Formula.ShuntingYard;

public readonly record struct Term(ResolvedValue Value, string Prefix = "", string Suffix = "") : IResolvable, INode
{
    public ResolvedValue Resolve(IDataContext context)
    {
        return Prefix.Length > 0 || Suffix.Length > 0
            ? QuotedTextResolvedValue.Of(Value, Prefix, Suffix)
            : Value;
        // return Optional.ofNullable(value)
        //     .map(value -> prefix != null && suffix != null
        //         ? QuotedTextResolvedValue.of(value, prefix, suffix)
        //         : value)
        //     .orElse(ResolvedValue.none());
    }
}