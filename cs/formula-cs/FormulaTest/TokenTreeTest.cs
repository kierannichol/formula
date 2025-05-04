using Formula;
using Formula.TokenTree;

namespace FormulaTest;

public class TokenTreeTest
{ 
    [Test]
    public void EmptyTree() {
        Assert.Throws(typeof(ParseException), () => 
            TokenTree<ResolvedValue>.Create().Parse("Text"));
    }

    [Test]
    public void SingleNode() {
        var tree = TokenTree<ResolvedValue>.Create()
                .Add("A", ResolvedValue.Of);
        Assert.That(tree.Parse("A"), Contains.Item(ResolvedValue.Of("A")));
    }

    [Test]
    public void SimpleChain() {
        var tree = TokenTree<ResolvedValue>.Create()
                .Add("ABC", ResolvedValue.Of);
        Assert.That(tree.Parse("ABC"), Contains.Item(ResolvedValue.Of("ABC")));
        Assert.Throws(typeof(ParseException), () => TokenTree<ResolvedValue>.Create().Parse("A"));
        Assert.Throws(typeof(ParseException), () => TokenTree<ResolvedValue>.Create().Parse("AB"));
        Assert.Throws(typeof(ParseException), () => TokenTree<ResolvedValue>.Create().Parse("ABX"));
    }

    [Test]
    public void SplittingChain() {
        var tree = TokenTree<ResolvedValue>.Create()
                .Add("ABC", ResolvedValue.Of)
                .Add("A23", ResolvedValue.Of);
        Assert.That(tree.Parse("ABC"), Contains.Item(ResolvedValue.Of("ABC")));
        Assert.That(tree.Parse("A23"), Contains.Item(ResolvedValue.Of("A23")));
    }
    
    [Test]
    public void MultipleTokens() {
        var tree = TokenTree<ResolvedValue>.Create()
                .IgnoreWhitespaces()
                .Add("ABC", ResolvedValue.Of)
                .Add("123", ResolvedValue.Of);
        Assert.That(tree.Parse("ABC 123"),
                Contains.Item(ResolvedValue.Of("ABC")).And.Contains(ResolvedValue.Of("123")));
    }
    
    [Test]
    public void AnyOfToken() {
        var tree = TokenTree<ResolvedValue>.Create()
                .IgnoreWhitespaces()
                .Add(NodeExpression.AnyOf(CharacterClass.DigitCharacters), token => ResolvedValue.Of(int.Parse(token)));
        Assert.That(tree.Parse("1 2 3"), 
            Contains.Item(ResolvedValue.Of(1)).And.Contains(ResolvedValue.Of(2)).And.Contains(ResolvedValue.Of(3)));
    }
    
    [Test]
    public void AnyOfChain() {
        var tree = TokenTree<ResolvedValue>.Create()
                .IgnoreWhitespaces()
                .Add(new [] { NodeExpression.AnyOf(CharacterClass.DigitCharacters), NodeExpression.AnyOf(CharacterClass.DigitCharacters) }, 
                    token => ResolvedValue.Of(int.Parse(token)));
        Assert.That(tree.Parse("13 25 36"),
                Contains.Item(ResolvedValue.Of(13)).And.Contains(ResolvedValue.Of(25)).And.Contains(ResolvedValue.Of(36)));
    }
    
    [Test]
    public void Repeated() {
        var tree = TokenTree<ResolvedValue>.Create()
                .IgnoreWhitespaces()
                .Add(NodeExpression.AnyOf(CharacterClass.DigitCharacters).Repeats(1, 2), 
                    token => ResolvedValue.Of(int.Parse(token)));
        Assert.That(tree.Parse("5"), Contains.Item(ResolvedValue.Of(5)));
        Assert.That(tree.Parse("73"), Contains.Item(ResolvedValue.Of(73)));
        Assert.That(tree.Parse("12 9 23"), Contains.Item(ResolvedValue.Of(12)).And.Contains(ResolvedValue.Of(9)).And.Contains(ResolvedValue.Of(23)));
    }
    
    [Test]
    public void OptionalTrailingCharacter()
    {
        var tree = TokenTree<ResolvedValue>.Create()
            .IgnoreWhitespaces()
            .Add(new[]
            {
                NodeExpression.AnyOf(CharacterClass.AlphaCharacters).Repeats(1),
                NodeExpression.AnyOf(CharacterClass.DigitCharacters).Optional()
            }, ResolvedValue.Of);
        Assert.That(tree.Parse("A5"), Contains.Item(ResolvedValue.Of("A5")));
        Assert.That(tree.Parse("ABC6"), Contains.Item(ResolvedValue.Of("ABC6")));
        Assert.That(tree.Parse("ABC"), Contains.Item(ResolvedValue.Of("ABC")));
        Assert.Throws(typeof(ParseException), () => TokenTree<ResolvedValue>.Create().Parse("5"));
        Assert.Throws(typeof(ParseException), () => TokenTree<ResolvedValue>.Create().Parse("ABC56"));
    }
    
    [Test]
    public void OptionalLeadingCharacter() {
        var tree = TokenTree<ResolvedValue>.Create()
                .IgnoreWhitespaces()
                .Add(new [] {
                    NodeExpression.AnyOf("@").Optional(), 
                    NodeExpression.AnyOf(CharacterClass.WordCharacters).Repeats(1)
                }, ResolvedValue.Of);
        Assert.That(tree.Parse("@A5"), Contains.Item(ResolvedValue.Of("@A5")));
        Assert.That(tree.Parse("A5"), Contains.Item(ResolvedValue.Of("A5")));
        Assert.Throws(typeof(ParseException), () => TokenTree<ResolvedValue>.Create().Parse("@"));
        Assert.Throws(typeof(ParseException), () => TokenTree<ResolvedValue>.Create().Parse("@@A5"));
    }
    
    [Test]
    public void NumberToken() {
        var tree = TokenTree<ResolvedValue>.Create()
                .IgnoreWhitespaces()
                .Add(NodeExpression.Number, token => ResolvedValue.Of(double.Parse(token)));
        Assert.That(tree.Parse("1"), Contains.Item(ResolvedValue.Of(1.0)));
        Assert.That(tree.Parse("23"), Contains.Item(ResolvedValue.Of(23.0)));
        Assert.That(tree.Parse("54890"), Contains.Item(ResolvedValue.Of(54890.0)));
        Assert.That(tree.Parse("3.14"), Contains.Item(ResolvedValue.Of(3.14)));
        Assert.Throws(typeof(ParseException), () => TokenTree<ResolvedValue>.Create().Parse("A"));
        Assert.Throws(typeof(ParseException), () => TokenTree<ResolvedValue>.Create().Parse("5B"));
        Assert.Throws(typeof(ParseException), () => TokenTree<ResolvedValue>.Create().Parse("2."));
        Assert.Throws(typeof(ParseException), () => TokenTree<ResolvedValue>.Create().Parse(".5"));
    }
    
    [Test]
    public void ExpressionOrder() {
        var tree = TokenTree<ResolvedValue>.Create()
                .IgnoreWhitespaces()
                .Add(NodeExpression.Integer, ResolvedValue.Of)
                .Add(NodeExpression.Decimal, token => ResolvedValue.Of(double.Parse(token)));
        Assert.That(tree.Parse("123 3.14 5 0.2"), Contains
            .Item(ResolvedValue.Of("123"))
            .And.Contains(ResolvedValue.Of(3.14))
            .And.Contains(ResolvedValue.Of("5"))
            .And.Contains(ResolvedValue.Of(0.2)));
    }
    
    [Test]
    public void QuotedToken() {
        var tree = TokenTree<ResolvedValue>.Create()
                .IgnoreWhitespaces()
                .Add(NodeExpression.Literal("\"", "\""), ResolvedValue.Of)
                .Add(NodeExpression.Word, ResolvedValue.Of);
    
        Assert.That(tree.Parse("one two \"three four\" five"), 
            Contains.Item(ResolvedValue.Of("one"))
                .And.Contains(ResolvedValue.Of("two"))
                .And.Contains(ResolvedValue.Of("\"three four\""))
                .And.Contains(ResolvedValue.Of("five")));
    }
    
    [Test]
    public void OpenCloseTagToken() {
        var tree = TokenTree<ResolvedValue>.Create()
                .IgnoreWhitespaces()
                .Add(NodeExpression.Literal("<open>", "<close>"), ResolvedValue.Of)
                .Add(NodeExpression.Word, ResolvedValue.Of);
    
        Assert.That(tree.Parse("one two <open>three four<close> five"),
                Contains
                    .Item(ResolvedValue.Of("one"))
                    .And.Contains(ResolvedValue.Of("two"))
                    .And.Contains(ResolvedValue.Of("<open>three four<close>"))
                    .And.Contains(ResolvedValue.Of("five")));
    }
}