namespace Formula.TokenTree;

public class AnyUntilNodeExpression : INodeExpression
{
    private readonly string _closeSequence;
    private readonly string? _escapeSequence;

    public static AnyUntilNodeExpression Of(string closeSequence, string? escapeSequence = null) {
        return new AnyUntilNodeExpression(closeSequence, escapeSequence);
    }

    public Node<T> ChainTo<T>(Node<T> root)
    {
        return root.Add(new AnyUntilNode<T>(_closeSequence, _escapeSequence));
    }

    private AnyUntilNodeExpression(string closeSequence, string? escapeSequence = null) {
        _closeSequence = closeSequence;
        _escapeSequence = escapeSequence;
    }
}