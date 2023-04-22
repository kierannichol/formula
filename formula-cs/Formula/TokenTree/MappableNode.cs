namespace Formula.TokenTree;

public abstract class MappableNode<T> : Node<T>
{
    public TokenMapper<T>? Mapper { get; set; }
}