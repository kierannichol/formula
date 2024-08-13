package org.formula.parse.tree;

public class RepeatingNodeExpression implements NodeExpression {
    private final TokenMatcher matcher;
    private final int minLength;
    private final int maxLength;

    @Override
    public <T> Node<T> chainTo(Node<T> root) {
        return root.add(new RepeatingNode<>(matcher, minLength, maxLength));
    }

    public RepeatingNodeExpression(TokenMatcher matcher, int minLength, int maxLength) {
        this.matcher = matcher;
        this.minLength = minLength;
        this.maxLength = maxLength;
    }
}
