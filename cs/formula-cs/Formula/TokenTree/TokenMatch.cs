namespace Formula.TokenTree;

public readonly record struct TokenMatch<T>(string Text, int StartIndex, int EndIndex, TokenMapper<T> Mapper)
{
    public T? Get()
    {
        return Mapper(Text.Substring(StartIndex, EndIndex - StartIndex));
    }
}