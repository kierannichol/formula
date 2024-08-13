using Formula;

namespace FormulaTest;

public class ResolvedValueAssertions
{
    private readonly ResolvedValue _value;

    public static ResolvedValueAssertions AssertResolvedValue(IResolvable resolvable)
    {
        return new ResolvedValueAssertions(resolvable.Resolve());
    }
    
    public static ResolvedValueAssertions AssertResolvedValue(ResolvedValue value)
    {
        return new ResolvedValueAssertions(value);
    }
    
    public ResolvedValueAssertions HasValue(string expected) {
        Assert.That(_value.AsText(), Is.EqualTo(expected));
        return this;
    }
    
    public ResolvedValueAssertions HasValue(int expected) {
        Assert.That(_value.AsNumber(), Is.EqualTo(expected));
        return this;
    }

    public ResolvedValueAssertions HasValue(double expected) {
        Assert.That(_value.AsDecimal(), Is.EqualTo(expected));
        return this;
    }

    public ResolvedValueAssertions HasValue(bool expected) {
        Assert.That(_value.AsBoolean(), Is.EqualTo(expected));
        return this;
    }

    public ResolvedValueAssertions HasNoValue() {
        Assert.That(_value, Is.EqualTo(ResolvedValue.None));
        return this;
    }

    private ResolvedValueAssertions(ResolvedValue value)
    {
        _value = value;
    }

    public void HasName(string expectedName)
    {
        if (_value is not NamedResolvedValue named)
        {
            Assert.Fail("Resolved value was not named");
            return;
        }
        Assert.That(named.AsName(), Is.EqualTo(expectedName));
    }
}