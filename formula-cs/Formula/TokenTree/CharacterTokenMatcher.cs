namespace Formula.TokenTree;

internal class CharacterTokenMatcher : ITokenMatcher
{
    private readonly char _character;
    
    public static CharacterTokenMatcher Of(char character) {
        return new CharacterTokenMatcher(character);
    }

    public bool Matches(char c) {
        return c == _character;
    }
    
    public override bool Equals(object? o)
    {
        return o is CharacterTokenMatcher other && Equals(other);
    }

    private bool Equals(CharacterTokenMatcher other)
    {
        return _character == other._character;
    }

    public override int GetHashCode()
    {
        return _character.GetHashCode();
    }

    private CharacterTokenMatcher(char character) {
        _character = character;
    }
}