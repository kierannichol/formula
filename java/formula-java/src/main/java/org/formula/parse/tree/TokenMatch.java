package org.formula.parse.tree;

public record TokenMatch<T>(String text, int startIndex, int endIndex, TokenMapper<T> mapper) {
    T get() {
        return mapper.map(text
                .substring(startIndex, endIndex));
    }
}