package org.formula.parse.tree;

import java.util.List;

public class CompositeNodeExpression implements NodeExpression {
    private final List<NodeExpression> expressions;

    public static CompositeNodeExpression of(List<NodeExpression> expressions) {
        return new CompositeNodeExpression(expressions);
    }

    @Override
    public <T> Node<T> chainTo(Node<T> root) {
        Node<T> node = root;
        for (NodeExpression toAdd : expressions) {
            node = toAdd.chainTo(node);
        }
        return node;
    }

    private CompositeNodeExpression(List<NodeExpression> expressions) {
        this.expressions = expressions;
    }
}
