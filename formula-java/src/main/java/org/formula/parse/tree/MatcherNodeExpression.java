package org.formula.parse.tree;

public class MatcherNodeExpression implements NodeExpression {
    private TokenMatcher matcher;

    @Override
    public <T> Node<T> chainTo(Node<T> root) {
        return root.add(new MatcherNode<>(matcher));
    }

    public NodeExpression repeats() {
        return repeats(0);
    }

    public NodeExpression repeats(int minLength) {
        return repeats(minLength, Integer.MAX_VALUE);
    }

    public NodeExpression repeats(int minLength, int maxLength) {
        return new RepeatingNodeExpression(this.matcher, minLength, maxLength);
    }

    public NodeExpression optional() {
        return repeats(0, 1);
    }

    public MatcherNodeExpression not() {
        return new MatcherNodeExpression(new NotTokenMatcher(matcher));
    }

    public MatcherNodeExpression(TokenMatcher matcher) {
        this.matcher = matcher;
    }
}
