namespace Formula.TokenTree;

internal class OptionalNodeExpression : INodeExpression
{
    private readonly IEnumerable<INodeExpression> _expressions;

    public static OptionalNodeExpression Of(IEnumerable<INodeExpression> expressions) {
        return new OptionalNodeExpression(expressions);
    }
    
    public Node<T> ChainTo<T>(Node<T> root)
    {
        var node = root;
        foreach (var toAdd in _expressions) {
            node = toAdd.ChainTo(node);
        }
        if (root is MappableNode<T> mappableRoot && node is MappableNode<T> mappableNode)
        {
            mappableRoot.Mapper = token =>
            {
                var mapper = mappableNode.Mapper;
                return mapper == null ? default : mapper(token);
            };
        }
        return node;
    }
    
    private OptionalNodeExpression(IEnumerable<INodeExpression> expressions)
    {
        _expressions = expressions;
    }
}