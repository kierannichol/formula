namespace Formula.TokenTree;

internal class MatcherNode<T> : MappableNode<T>
{
    protected readonly ITokenMatcher Matcher;

    public MatcherNode(ITokenMatcher matcher)
    {
        Matcher = matcher;
    }

    public override void Walk(string text, int startIndex, int currentIndex, List<TokenMatch<T>> matches)
    {
        if (!Matches(text, startIndex, currentIndex))
        {
            return;
        }
        
        WalkChildren(text, startIndex, currentIndex + 1, matches);
        if (Mapper == null)
        {
            return;
        }
        var match = new TokenMatch<T>(text, startIndex, currentIndex + 1, Mapper);
        matches.Add(match);
    }
    
    protected bool Matches(string text, int startIndex, int currentIndex) {
        if (currentIndex >= text.Length) {
            return false;
        }
        var current = text[currentIndex];
        return Matcher.Matches(current);
    }
    
    public override bool Equals(object? other)
    {
        return other is MatcherNode<T> node && Equals(node);
    }

    private bool Equals(MatcherNode<T> other)
    {
        return Matcher.Equals(other.Matcher);
    }

    public override int GetHashCode()
    {
        return Matcher.GetHashCode();
    }
}