namespace Formula.TokenTree;

public class AnyUntilNode<T> : MappableNode<T>
{
    private readonly string _closeSequence;
    private readonly string? _escapeSequence;
    
    public AnyUntilNode(string closeSequence, string? escapeSequence)
    {
        _closeSequence = closeSequence;
        _escapeSequence = escapeSequence;
    }

    public override void Walk(string text, int startIndex, int currentIndex, List<TokenMatch<T>> matches)
    {
        for (; currentIndex < text.Length; currentIndex++) 
        {
            for (var i = 0; i < _closeSequence.Length; i++) 
            {
                if (text[currentIndex + i] != _closeSequence[i]) 
                {
                    break;
                }
                if (IsEscaped(text, currentIndex)) 
                {
                    break;
                }

                if (i != _closeSequence.Length - 1) continue;
                WalkChildren(text, startIndex, currentIndex, matches);
                if (Mapper == null || currentIndex <= startIndex) 
                {
                    return;
                }
                var match = new TokenMatch<T>(text, startIndex, currentIndex, Mapper);
                matches.Add(match);
                return;
            }
        }

        if (Mapper != null && currentIndex == text.Length)
        {
            matches.Add(new TokenMatch<T>(text, startIndex, currentIndex, Mapper));
        }
    }
    
    private bool IsEscaped(string text, int index) {
        if (_escapeSequence == null) {
            return false;
        }

        for (int i = index - _escapeSequence.Length + 1, k = 0; k < _escapeSequence.Length; i++, k++) {
            if (i < 0 && i >= index) {
                return false;
            }
            if (text[i] != _escapeSequence[k]) {
                return false;
            }
        }
        return true;
    }

    public override bool Equals(object? obj)
    {
        return obj is AnyUntilNode<T> other && Equals(other);
    }

    private bool Equals(AnyUntilNode<T> other)
    {
        return _closeSequence.Equals(other._closeSequence)
               && (_escapeSequence?.Equals(other._escapeSequence) ?? _escapeSequence == other._escapeSequence);
    }

    public override int GetHashCode()
    {
        return _closeSequence.GetHashCode();
    }
}