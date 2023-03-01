package org.formula.parse.tree;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public abstract class Node<T> {
    protected List<Node<T>> children;

    public Node<T> add(Node<T> child) {
        if (children == null) {
            children = new ArrayList<>();
        }
        for (Node<T> existingChild : children) {
            if (existingChild.equals(child)) {
                return existingChild;
            }
        }
        children.add(child);
        return child;
    }

    public abstract Stream<TokenMatch<T>> walk(String text, int startIndex, int currentIndex);

    protected Stream<TokenMatch<T>> walkChildren(String text, int startIndex, int currentIndex) {
        if (children == null) {
            return Stream.empty();
        }

        return children.stream()
                .flatMap(child -> child.walk(text, startIndex, currentIndex));
    }
}