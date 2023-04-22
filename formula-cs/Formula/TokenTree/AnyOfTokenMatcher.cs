namespace Formula.TokenTree;

public class AnyOfTokenMatcher : ITokenMatcher
{
    private readonly IEnumerable<char> _allowed;

    public static AnyOfTokenMatcher Of(IEnumerable<char> allowed) {
        return new AnyOfTokenMatcher(allowed);
    }

    public bool Matches(char c) {
        foreach (var value in _allowed) {
            if (value == c) {
                return true;
            }
        }
        return false;
    }
    
    public override bool Equals(object? o) 
    {
        return o is AnyOfTokenMatcher other && Equals(other);
    }

    private bool Equals(AnyOfTokenMatcher other)
    {
        return _allowed.Equals(other._allowed);
    }

    public override int GetHashCode()
    {
        return _allowed.GetHashCode();
    }

    private AnyOfTokenMatcher(IEnumerable<char> allowed) {
        _allowed = allowed;
    }
}