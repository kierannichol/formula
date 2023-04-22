namespace Formula.TokenTree;

public class ParseException : Exception
{
    public string Text { get; }
    public int Index { get; }
    
    public ParseException(string? message, string text, int index) : base(message)
    {
        Text = text;
        Index = index;
    }
}