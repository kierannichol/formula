package org.formula.parse.tree;

import java.util.ArrayList;
import java.util.List;

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

    public abstract void walk(String text, int startIndex, int currentIndex, List<TokenMatch<T>> matches);

    protected void walkChildren(String text, int startIndex, int currentIndex, List<TokenMatch<T>> matches) {
        if (children == null) {
            return;
        }

        children.forEach(child -> child.walk(text, startIndex, currentIndex, matches));
    }
}