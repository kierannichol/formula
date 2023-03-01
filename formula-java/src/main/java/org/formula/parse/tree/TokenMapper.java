package org.formula.parse.tree;

@FunctionalInterface
public interface TokenMapper<T> {
    T map(String token);
}