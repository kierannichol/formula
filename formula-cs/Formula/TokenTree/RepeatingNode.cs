namespace Formula.TokenTree;

internal class RepeatingNode<T> : MatcherNode<T>
{
    private readonly int _minLength;
    private readonly int _maxLength;

    public RepeatingNode(ITokenMatcher matcher, int minLength, int maxLength) : base(matcher) {
        _minLength = minLength;
        _maxLength = maxLength;
    }
    
    public override void Walk(string text, int startIndex, int currentIndex, List<TokenMatch<T>> matches) {
        var minLength = _minLength;
        var maxLength = CalculateMaxLength(text, currentIndex);
        var length = 0;

        while (length < maxLength && currentIndex < text.Length) {
            char next = text[currentIndex];
            if (!Matcher.Matches(next)) 
            {
                break;
            }
            currentIndex++;
            length++;
        }

        if (length < minLength)
        {
            return;
        }

        WalkChildren(text, startIndex, currentIndex, matches);
        if (length > maxLength || Mapper == null) {
            return;
        }

        var match = new TokenMatch<T>(text, startIndex, currentIndex, Mapper);
        matches.Add(match);
    }
    
    public override bool Equals(object? other) {
        return other is RepeatingNode<T> node && Equals(node);
    }

    private bool Equals(RepeatingNode<T> other)
    {
        return base.Equals(other) && _minLength == other._minLength && _maxLength == other._maxLength;
    }

    public override int GetHashCode()
    {
        return HashCode.Combine(base.GetHashCode(), _minLength, _maxLength);
    }

    private int CalculateMaxLength(String text, int currentIndex) {
        int maxTextLength = text.Length - currentIndex;
        return Math.Min(_maxLength, maxTextLength);
    }
}