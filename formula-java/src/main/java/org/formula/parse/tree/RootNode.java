package org.formula.parse.tree;

import java.util.stream.Stream;

public class RootNode<T> extends Node<T> {

    public Stream<TokenMatch<T>> walk(String text, int startIndex) {
        return walk(text, startIndex, startIndex);
    }

    @Override
    public Stream<TokenMatch<T>> walk(String text, int startIndex, int currentIndex) {
        return walkChildren(text, startIndex, currentIndex);
    }

    @Override
    public boolean equals(Object other) {
        return false;
    }
}