package org.formula.parse.tree;

public abstract class MappableNode<T> extends Node<T> {
    protected TokenMapper<T> mapper;

    public void mapper(TokenMapper<T> mapper) {
        this.mapper = mapper;
    }

    public TokenMapper<T> mapper() {
        return this.mapper;
    }
}
