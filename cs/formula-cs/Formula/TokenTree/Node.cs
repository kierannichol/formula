namespace Formula.TokenTree;

public abstract class Node<T>
{
    protected List<Node<T>>? Children;

    public Node<T> Add(Node<T> child) {
        Children ??= new List<Node<T>>();
        foreach (var existingChild in Children) {
            if (existingChild.Equals(child)) {
                return existingChild;
            }
        }
        Children.Add(child);
        return child;
    }

    public abstract void Walk(string text, int startIndex, int currentIndex, List<TokenMatch<T>> matches);

    protected void WalkChildren(string text, int startIndex, int currentIndex, List<TokenMatch<T>> matches) {
        if (Children == null)
        {
            return;
        }

        foreach (var child in Children)
        {
            child.Walk(text, startIndex, currentIndex, matches);
        }
    }
}