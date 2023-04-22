namespace FormulaTest;

using Formula;
using static ResolvedValueAssertions;

public class FormulaTest
{
    [Test]
    public void SingleNegativeNumber() {
        var formula = Formula.Parse("-9");
        AssertResolvedValue(formula).HasValue(-9);
    }

    [Test]
    public void AddTwoScalars() {
        var formula = Formula.Parse("2 + 3");
        AssertResolvedValue(formula).HasValue(5);
    }

    [Test]
    public void Exponents() {
        var formula = Formula.Parse("2^3");
        AssertResolvedValue(formula).HasValue(8);
    }

    [Test]
    public void WithBrackets() {
        var formula = Formula.Parse("4 + 4 * 2 / ( 1 - 5 )");
        AssertResolvedValue(formula).HasValue(2);
    }

    [Test]
    public void MultipleDigitNumbers() {
        var formula = Formula.Parse("12 + 100");
        AssertResolvedValue(formula).HasValue(112);
    }

    [Test]
    public void AbsFunction() {
        var formula = Formula.Parse("2 + abs(2 - 3) + 1");
        AssertResolvedValue(formula).HasValue(4);
    }

    [Test]
    public void MinFunction() {
        var formula = Formula.Parse("1 + min(4, 2)");
        AssertResolvedValue(formula).HasValue(3);
    }

    [Test]
    public void MaxFunction() {
        var formula = Formula.Parse("1 + max(4, 2)");
        AssertResolvedValue(formula).HasValue(5);
    }

    [Test]
    public void ComplexMaxFunction() {
        var formula = Formula.Parse("max(4 - 2, 2 / 2)");
        AssertResolvedValue(formula).HasValue(2);
    }

    [Test]
    public void FloorFunction() {
        var formula = Formula.Parse("1 + floor(2.9)");
        AssertResolvedValue(formula).HasValue(3);
    }

    [Test]
    public void CeilFunction() {
        var formula = Formula.Parse("1 + ceil(2.9)");
        AssertResolvedValue(formula).HasValue(4);
    }

    [Test]
    public void SignedFunction() {
        AssertResolvedValue(Formula.Parse("signed(3)")).HasValue("+3");
        AssertResolvedValue(Formula.Parse("signed(-3)")).HasValue("-3");
        AssertResolvedValue(Formula.Parse("signed(3)")).HasValue(3);
        AssertResolvedValue(Formula.Parse("signed(-3)")).HasValue(-3);
    }

    [Test]
    public void SimpleVariableFunction() {
        var formula = Formula.Parse("@foo");
        var context = new DataContext().Set("foo", 12);
        AssertResolvedValue(formula.Resolve(context)).HasValue(12);
    }

    [Test]
    public void VariableMath() {
        var formula = Formula.Parse("@foo + 2");
        var context = new DataContext().Set("foo", 1);
        AssertResolvedValue(formula.Resolve(context)).HasValue(3);
    }

    [Test]
    public void VariableReferenceFormula() {
        var formula = Formula.Parse("@bar");
        var context = new DataContext()
                .Set("foo", 4)
                .Set("bar", Formula.Parse("@foo"));
        AssertResolvedValue(formula.Resolve(context)).HasValue(4);
    }

    [Test]
    public void IfFormula() {
        var formula = Formula.Parse("if(-2 < 0, 'A', 'B')");
        var context = DataContext.Empty;
        AssertResolvedValue(formula.Resolve(context)).HasValue("A");
    }

    [Test]
    public void ElseFormula() {
        var formula = Formula.Parse("concat(if(2 < 0, '-', '+'), 2)");
        var context = DataContext.Empty;
        AssertResolvedValue(formula.Resolve(context)).HasValue("+2");
    }

    [Test]
    public void ModifierFormula() {
        var formula = Formula.Parse("concat(if((floor(@test_score/2) - 5) > 0, '+', ''), floor(@test_score/2) - 5)");
        var context = new DataContext()
                .Set("test_score", 12);
        AssertResolvedValue(formula.Resolve(context)).HasValue("+1");
    }

    [Test]
    public void Ordinal() {
        string[] expected = { "0th", "1st", "2nd", "3rd", "4th", "5th", "6th", "7th", "8th", "9th", "10th" };
        for (var i = 0; i < expected.Length; i++) {
            var formula = Formula.Parse($"ordinal({i})");
            AssertResolvedValue(formula.Resolve()).HasValue(expected[i]);
        }
    }

    [Test]
    public void WildcardMin() {
        var formula = Formula.Parse("min(@key_*)");
        var context = new DataContext()
                .Set("other", 2)
                .Set("key_1", 4)
                .Set("key_2", 3)
                .Set("key_3", 5);
        AssertResolvedValue(formula.Resolve(context)).HasValue(3);
    }

    [Test]
    public void WildcardMax() {
        var formula = Formula.Parse("max(@key_*)");
        var context = new DataContext()
                .Set("other", 2)
                .Set("key_1", 4)
                .Set("key_2", 3)
                .Set("key_3", 5);
        AssertResolvedValue(formula.Resolve(context)).HasValue(5);
    }

    [Test]
    public void WildcardSum() {
        var formula = Formula.Parse("sum(@key_*)");
        var context = new DataContext()
                .Set("other", 2)
                .Set("key_1", 4)
                .Set("key_2", 3)
                .Set("key_3", 5);
        AssertResolvedValue(formula.Resolve(context)).HasValue(12);
    }

    [Test]
    public void AnyFunction() {
        var formula = Formula.Parse("any(@a, @b)");
        AssertResolvedValue(formula.Resolve(new DataContext().Set("a", 0).Set("b", 0))).HasValue(false);
        AssertResolvedValue(formula.Resolve(new DataContext().Set("a", 0).Set("b", 1))).HasValue(true);
        AssertResolvedValue(formula.Resolve(new DataContext().Set("a", 1).Set("b", 0))).HasValue(true);
        AssertResolvedValue(formula.Resolve(new DataContext().Set("a", 1).Set("b", 1))).HasValue(true);
    }

    [Test]
    public void AllFunction() {
        var formula = Formula.Parse("all(@a, @b)");
        AssertResolvedValue(formula.Resolve(new DataContext().Set("a", 0).Set("b", 0))).HasValue(false);
        AssertResolvedValue(formula.Resolve(new DataContext().Set("a", 0).Set("b", 1))).HasValue(false);
        AssertResolvedValue(formula.Resolve(new DataContext().Set("a", 1).Set("b", 0))).HasValue(false);
        AssertResolvedValue(formula.Resolve(new DataContext().Set("a", 1).Set("b", 1))).HasValue(true);
    }

    [Test]
    public void AllWithNestedAny() {
        var formula = Formula.Parse("all(@a, any(@b, @c), 1)");
        AssertResolvedValue(formula.Resolve(new DataContext().Set("a", 0).Set("b", 0).Set("c", 0))).HasValue(false);
        AssertResolvedValue(formula.Resolve(new DataContext().Set("a", 1).Set("b", 0).Set("c", 0))).HasValue(false);
        AssertResolvedValue(formula.Resolve(new DataContext().Set("a", 1).Set("b", 1).Set("c", 0))).HasValue(true);
        AssertResolvedValue(formula.Resolve(new DataContext().Set("a", 1).Set("b", 0).Set("c", 1))).HasValue(true);
        AssertResolvedValue(formula.Resolve(new DataContext().Set("a", 1).Set("b", 1).Set("c", 1))).HasValue(true);
    }

    [Test]
    public void AllWithNestedAnyDifferentOrder() {
        var formula = Formula.Parse("all(any(@b, @c), @a, 1)");
        AssertResolvedValue(formula.Resolve(new DataContext().Set("a", 0).Set("b", 0).Set("c", 0))).HasValue(false);
        AssertResolvedValue(formula.Resolve(new DataContext().Set("a", 1).Set("b", 0).Set("c", 0))).HasValue(false);
        AssertResolvedValue(formula.Resolve(new DataContext().Set("a", 1).Set("b", 1).Set("c", 0))).HasValue(true);
        AssertResolvedValue(formula.Resolve(new DataContext().Set("a", 1).Set("b", 0).Set("c", 1))).HasValue(true);
        AssertResolvedValue(formula.Resolve(new DataContext().Set("a", 1).Set("b", 1).Set("c", 1))).HasValue(true);
    }
}