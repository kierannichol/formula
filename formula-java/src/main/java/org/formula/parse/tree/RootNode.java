package org.formula.parse.tree;

import java.util.List;

public class RootNode<T> extends Node<T> {

    public void walk(String text, int startIndex, List<TokenMatch<T>> matches) {
        walk(text, startIndex, startIndex, matches);
    }

    @Override
    public void walk(String text, int startIndex, int currentIndex, List<TokenMatch<T>> matches) {
        walkChildren(text, startIndex, currentIndex, matches);
    }

    @Override
    public boolean equals(Object other) {
        return false;
    }
}