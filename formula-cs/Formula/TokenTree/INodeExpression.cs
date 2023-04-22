namespace Formula.TokenTree;

using static CharacterClass;

public interface INodeExpression
{
    Node<T> ChainTo<T>(Node<T> root);
}

public static class NodeExpression
{
    public static readonly INodeExpression Digit = AnyOf(DigitCharacters);
    public static readonly INodeExpression Digits = AnyOf(DigitCharacters).Repeats(1);
    public static readonly INodeExpression Integer = Of(Just('-').Optional(), Digits);
    public static readonly INodeExpression Key = AnyOf(WordCharacters + ":.#*").Repeats(1);
    public static readonly INodeExpression Word = AnyOf(WordCharacters).Repeats(1);
    public static readonly INodeExpression Alpha = AnyOf(AlphaCharacters);
    public static readonly INodeExpression Number = Of(Integer, Optional(Just('.'), Digits));
    public static readonly INodeExpression Decimal = Of(Integer, Just('.'), Digits);

    public static INodeExpression Of(params INodeExpression[] expressions) {
        return CompositeNodeExpression.Of(expressions);
    }

    public static MatcherNodeExpression AnyOf(IEnumerable<char> allowed) {
        return Matches(AnyOfTokenMatcher.Of(allowed));
    }

    public static AnyUntilNodeExpression AnyUntil(string closeSequence, string? escapeSequence = null) {
        return AnyUntilNodeExpression.Of(closeSequence, escapeSequence);
    }

    public static INodeExpression Literal(string openSequence, string closeSequence, string escapeSequence) {
        return Of(Term(openSequence), AnyUntil(closeSequence, escapeSequence), Term(closeSequence));
    }

    public static INodeExpression Literal(string openSequence, string closeSequence) {
        return Of(Term(openSequence), AnyUntil(closeSequence), Term(closeSequence));
    }

    public static INodeExpression Term(IEnumerable<char> term)
    {
        var expressions = new List<INodeExpression>();
        foreach (var c in term)
        {
            expressions.Add(Just(c));
        }
        return CompositeNodeExpression.Of(expressions);
    }

    public static INodeExpression Optional(params INodeExpression[] expressions) {
        return OptionalNodeExpression.Of(expressions);
    }

    public static MatcherNodeExpression Just(char c) {
        return Matches(CharacterTokenMatcher.Of(c));
    }

    public static MatcherNodeExpression Matches(ITokenMatcher matcher) {
        return new MatcherNodeExpression(matcher);
    }
}

internal class CompositeNodeExpression : INodeExpression
{
    private readonly IEnumerable<INodeExpression> _expressions;

    public static CompositeNodeExpression Of(IEnumerable<INodeExpression> expressions) {
        return new CompositeNodeExpression(expressions);
    }

    public Node<T> ChainTo<T>(Node<T> root)
    {
        var node = root;
        foreach (var toAdd in _expressions) {
            node = toAdd.ChainTo(node);
        }
        return node;
    }

    private CompositeNodeExpression(IEnumerable<INodeExpression> expressions) {
        _expressions = expressions;
    }
}

public class MatcherNodeExpression : INodeExpression
{
    private readonly ITokenMatcher _matcher;

    public Node<T> ChainTo<T>(Node<T> root)
    {
        return root.Add(new MatcherNode<T>(_matcher));
    }

    public INodeExpression Repeats(int minLength = 0, int maxLength = int.MaxValue) {
        return new RepeatingNodeExpression(_matcher, minLength, maxLength);
    }

    public INodeExpression Optional() {
        return Repeats(0, 1);
    }

    public MatcherNodeExpression Not() {
        return new MatcherNodeExpression(new NotTokenMatcher(_matcher));
    }

    public MatcherNodeExpression(ITokenMatcher matcher) {
        _matcher = matcher;
    }
}

internal class RepeatingNodeExpression : INodeExpression
{
    private readonly ITokenMatcher _matcher;
    private readonly int _minLength;
    private readonly int _maxLength;
    
    public RepeatingNodeExpression(ITokenMatcher matcher, int minLength, int maxLength)
    {
        _matcher = matcher;
        _minLength = minLength;
        _maxLength = maxLength;
    }

    public Node<T> ChainTo<T>(Node<T> root)
    {
        return root.Add(new RepeatingNode<T>(_matcher, _minLength, _maxLength));
    }
}