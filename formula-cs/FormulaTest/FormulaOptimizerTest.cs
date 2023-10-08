using Formula.Optimize;

namespace FormulaTest;

public class FormulaOptimizerTest
{
    [Test]
    public void Any() {
        var optimized = FormulaOptimizer.Optimize("any(any(@a, any(@b, @c)), @d)");
        Assert.AreEqual("any(@a,@b,@c,@d)", optimized);
    }

    [Test]
    public void All() {
        var optimized = FormulaOptimizer.Optimize("all(any(@a, all(@b)), @c, all(@d AND @e), @f)");
        Assert.AreEqual("all(any(@a,@b),@c,@d,@e,@f)", optimized);
    }

    [Test]
    public void BracketAdd() {
        var optimized = FormulaOptimizer.Optimize("@a + (@b + @c)");
        Assert.AreEqual("@a+@b+@c", optimized);
    }

    [Test]
    public void KeepsRequiredBrackets() {
        Assert.AreEqual("@a*(@b+@c+@d)/2", FormulaOptimizer.Optimize("@a * (@b + @c + @d)/2"));
        Assert.AreEqual("@a-(@b/@c)", FormulaOptimizer.Optimize("@a - (@b / @c)"));
        Assert.AreEqual("@a<(@b-@c)", FormulaOptimizer.Optimize("@a < (@b - @c)"));
    }

    [Test]
    public void Literals() {
        Assert.AreEqual("\"testing\"", FormulaOptimizer.Optimize("\"testing\""));
        Assert.AreEqual("any(@a,\"testing\")", FormulaOptimizer.Optimize("any(@a,\"testing\")"));
    }

    [Test]
    public void Comments() {
        Assert.AreEqual("(@a+@b)[testing]", FormulaOptimizer.Optimize("(@a+@b)[testing]"));
    }
}