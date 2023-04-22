namespace Formula.TokenTree;

public static class CharacterClass
{
    private const string SpaceCharacters = " ";
    public static readonly string BlankCharacters = SpaceCharacters + "\t";
    public const string DigitCharacters = "1234567890";

    private static readonly string LowerCaseCharacters = new(new[] { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z' });
    private static readonly string UpperCaseCharacters = new(new[] { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z' });
    public static readonly string AlphaCharacters = LowerCaseCharacters + UpperCaseCharacters;
    private static readonly string AlphanumericCharacters = AlphaCharacters + DigitCharacters;
    public static readonly string WordCharacters = AlphanumericCharacters + "_";
}