namespace Formula;

public class ResolvedRollValue : ResolvedValue
{
    private readonly int _count;
    private readonly int _sides;

    public ResolvedRollValue(int count, int sides)
    {
        _count = count;
        _sides = sides;
    }

    public override string AsText()
    {
        return _count + "d" + _sides;
    }

    public override int AsNumber()
    {
        return (int)AsDecimal();
    }

    public override double AsDecimal()
    {
        return (_count * (_sides + 1)) / 2.0;
    }

    public override bool AsBoolean()
    {
        return _count > 0 && _sides > 0;
    }

    public override bool Equals(object? obj)
    {
        return obj is ResolvedValue { HasValue: true } other && Math.Abs(AsDecimal() - other.AsDecimal()) < 0.01;
    }

    public override int GetHashCode()
    {
        return _count.GetHashCode() + _sides.GetHashCode();
    }

    public override string ToString()
    {
        return AsText();
    }
}