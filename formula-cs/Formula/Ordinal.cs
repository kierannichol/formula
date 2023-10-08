using System.Text.RegularExpressions;

namespace Formula;

public static class Ordinal
{
    private static readonly Regex Pattern = new(@"^(\\d+)(?:th|st|nd|rd)?$");
    private static readonly string[] Suffixes = { "th", "st", "nd", "rd" };

    public static string ToString(int n) {
        var v = n % 100;
        return n + (Suffix((v - 20) % 10) ?? Suffix(v) ?? Suffixes[0]);
    }

    public static int Parse(string str) {
        var matcher = Pattern.Match(str);
        if (!matcher.Success) {
            throw new ArgumentException(str + " is not an ordinal number");
        }
        return int.Parse(matcher.Groups[1].Value);
    }

    private static string? Suffix(int index) {
        if (index < Suffixes.Length && index >= 0) {
            return Suffixes[index];
        }
        return null;
    }
}