namespace Formula.TokenTree;

public class NotTokenMatcher : ITokenMatcher
{
    private readonly ITokenMatcher _matcher;
    
    public bool Matches(char c) {
        return !_matcher.Matches(c);
    }
    
    public override bool Equals(object? o)
    {
        return o is NotTokenMatcher matcher && Equals(matcher);
    }

    private bool Equals(NotTokenMatcher other)
    {
        return _matcher.Equals(other._matcher);
    }

    public override int GetHashCode()
    {
        return _matcher.GetHashCode();
    }

    public NotTokenMatcher(ITokenMatcher matcher) {
        _matcher = matcher;
    }
}