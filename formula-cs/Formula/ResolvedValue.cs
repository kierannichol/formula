using System.Globalization;

namespace Formula;

public abstract class ResolvedValue
{
    public static ResolvedValue True { get; } = new BooleanResolvedValue(true);
    public static ResolvedValue False { get; } = new BooleanResolvedValue(false);
    public static ResolvedValue None { get; } = new NoResolvedValue();
    public static ResolvedValue Zero { get; } = Of(0);
    public static ResolvedValue NaN { get; } = new NanResolvedValue();

    public static ResolvedValue Of(string value)
    {
        return new TextResolvedValue(value);
    }
    
    public static ResolvedValue Of(int value)
    {
        return new NumericResolvedValue(value);
    }
    
    public static ResolvedValue Of(double value)
    {
        return new DecimalResolvedValue(value);
    }
    
    public static ResolvedValue Of(bool value)
    {
        return value ? True : False;
    }

    public abstract string AsText();
    public abstract int AsNumber();
    public abstract double AsDecimal();
    public abstract bool AsBoolean();

    public virtual bool HasValue => true;
}

public class QuotedTextResolvedValue : ResolvedValue
{
    private readonly ResolvedValue _text;
    private readonly string _startQuote;
    private readonly string _endQuote;

    public static QuotedTextResolvedValue Of(string text, string startQuote, string endQuote)
    {
        return Of(ResolvedValue.Of(text), startQuote, endQuote);
    }
    
    public static QuotedTextResolvedValue Of(ResolvedValue value, string startQuote, string endQuote)
    {
        return new QuotedTextResolvedValue(value, startQuote, endQuote);
    }

    public override bool HasValue => _text.HasValue;
    
    public override string AsText()
    {
        return _text.AsText();
    }

    public string AsQuotedText()
    {
        return _startQuote + AsText() + _endQuote;
    }

    public override int AsNumber()
    {
        return _text.AsNumber();
    }

    public override double AsDecimal()
    {
        return _text.AsDecimal();
    }

    public override bool AsBoolean()
    {
        return _text.AsBoolean();
    }
    
    public override bool Equals(object? obj)
    {
        return _text.Equals(obj);
    }

    public override int GetHashCode()
    {
        return _text.GetHashCode();
    }

    private QuotedTextResolvedValue(ResolvedValue value, string startQuote, string endQuote)
    {
        _text = value;
        _startQuote = startQuote;
        _endQuote = endQuote;
    }
}

public class NamedResolvedValue : ResolvedValue
{
    private readonly ResolvedValue _value;
    private readonly string _name;

    public static NamedResolvedValue Of(string text, string name)
    {
        return Of(ResolvedValue.Of(text), name);
    }
    
    public static NamedResolvedValue Of(ResolvedValue value, string name)
    {
        return new NamedResolvedValue(value, name);
    }
    
    public override bool HasValue => _value.HasValue;

    public override string AsText()
    {
        return _value.AsText();
    }

    public string AsName()
    {
        return _name;
    }

    public override int AsNumber()
    {
        return _value.AsNumber();
    }

    public override double AsDecimal()
    {
        return _value.AsDecimal();
    }

    public override bool AsBoolean()
    {
        return _value.AsBoolean();
    }
    
    public override bool Equals(object? obj)
    {
        return _value.Equals(obj);
    }

    public override int GetHashCode()
    {
        return _value.GetHashCode();
    }

    private NamedResolvedValue(ResolvedValue value, string name)
    {
        _value = value;
        _name = name;
    }
}

internal class TextResolvedValue : ResolvedValue
{
    private static readonly string[] FalseStringValues = { "false", "no", "0", "" };
    
    private readonly string _value;

    public TextResolvedValue(string value)
    {
        _value = value;
    }

    public override string AsText()
    {
        return _value;
    }

    public override int AsNumber()
    {
        return int.Parse(_value);
    }

    public override double AsDecimal()
    {
        return double.Parse(_value);
    }

    public override bool AsBoolean()
    {
        var lowerCase = _value.ToLowerInvariant();
        foreach (var falseString in FalseStringValues)
        {
            if (lowerCase.Equals(falseString))
            {
                return false;
            }
        }

        return true;
    }

    public override bool Equals(object? obj)
    {
        return obj is ResolvedValue { HasValue: true } other 
               && _value.Equals(other.AsText());
    }

    public override int GetHashCode()
    {
        return _value.GetHashCode();
    }

    public override string ToString()
    {
        return _value;
    }
}

internal class NumericResolvedValue : ResolvedValue
{
    private readonly int _value;

    public NumericResolvedValue(int value)
    {
        _value = value;
    }

    public override string AsText()
    {
        return _value.ToString();
    }

    public override int AsNumber()
    {
        return _value;
    }

    public override double AsDecimal()
    {
        return _value;
    }

    public override bool AsBoolean()
    {
        return _value > 0;
    }

    public override bool Equals(object? obj)
    {
        return obj is ResolvedValue { HasValue: true } other && _value == other.AsNumber();
    }

    public override int GetHashCode()
    {
        return _value.GetHashCode();
    }

    public override string ToString()
    {
        return _value.ToString();
    }
}

internal class DecimalResolvedValue : ResolvedValue
{
    private readonly double _value;

    public DecimalResolvedValue(double value)
    {
        _value = value;
    }

    public override string AsText()
    {
        return _value.ToString(CultureInfo.InvariantCulture);
    }

    public override int AsNumber()
    {
        return (int)_value;
    }

    public override double AsDecimal()
    {
        return _value;
    }

    public override bool AsBoolean()
    {
        return _value > 0.0;
    }

    public override bool Equals(object? obj)
    {
        return obj is ResolvedValue { HasValue: true } other && Math.Abs(_value - other.AsDecimal()) < 0.01;
    }

    public override int GetHashCode()
    {
        return _value.GetHashCode();
    }

    public override string ToString()
    {
        return _value.ToString(CultureInfo.InvariantCulture);
    }
}

internal class BooleanResolvedValue : ResolvedValue
{
    private const string TrueText = "true";
    private const string FalseText = "false";

    private readonly bool _value;

    public BooleanResolvedValue(bool value)
    {
        _value = value;
    }

    public override string AsText()
    {
        return _value ? TrueText : FalseText;
    }

    public override int AsNumber()
    {
        return _value ? 1 : 0;
    }

    public override double AsDecimal()
    {
        return _value ? 1.0 : 0.0;
    }

    public override bool AsBoolean()
    {
        return _value;
    }

    public override bool Equals(object? obj)
    {
        return obj is ResolvedValue { HasValue: true } other && _value == other.AsBoolean();
    }

    public override int GetHashCode()
    {
        return _value.GetHashCode();
    }

    public override string ToString()
    {
        return _value.ToString();
    }
}

internal class NoResolvedValue : ResolvedValue
{
    public override bool HasValue => false;
    
    public override string AsText()
    {
        return "";
    }

    public override int AsNumber()
    {
        return 0;
    }

    public override double AsDecimal()
    {
        return 0.0;
    }

    public override bool AsBoolean()
    {
        return false;
    }

    public override bool Equals(object? obj)
    {
        return obj is NoResolvedValue;
    }

    public override int GetHashCode()
    {
        return 0;
    }

    public override string ToString()
    {
        return "null";
    }
}

internal class NanResolvedValue : ResolvedValue
{
    public override bool HasValue => true;
    
    public override string AsText()
    {
        return "NaN";
    }

    public override int AsNumber()
    {
        throw new FormatException("NaN");
    }

    public override double AsDecimal()
    {
        return double.NaN;
    }

    public override bool AsBoolean()
    {
        return false;
    }

    public override bool Equals(object? obj)
    {
        return obj is NanResolvedValue;
    }

    public override int GetHashCode()
    {
        return double.NaN.GetHashCode();
    }

    public override string ToString()
    {
        return "NaN";
    }
}