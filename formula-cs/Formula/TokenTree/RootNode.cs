namespace Formula.TokenTree;

internal class RootNode<T> : Node<T> {

    public void Walk(string text, int startIndex, List<TokenMatch<T>> matches) {
        Walk(text, startIndex, startIndex, matches);
    }

    public override void Walk(string text, int startIndex, int currentIndex, List<TokenMatch<T>> matches)
    {
        WalkChildren(text, startIndex, currentIndex, matches);
    }

    public override bool Equals(object? obj)
    {
        return false;
    }

    public override int GetHashCode()
    {
        return 0;
    }
}