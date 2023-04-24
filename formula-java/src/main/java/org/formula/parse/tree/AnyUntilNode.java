package org.formula.parse.tree;

import java.util.List;
import java.util.Objects;

public class AnyUntilNode<T> extends MappableNode<T> {
    private final String closeSequence;
    private final String escapeSequence;

    @Override
    public void walk(String text, int startIndex, int currentIndex, List<TokenMatch<T>> matches) {
        for (; currentIndex < text.length(); currentIndex++) {
            for (int i = 0; i < closeSequence.length(); i++) {
                if (text.charAt(currentIndex + i) != closeSequence.charAt(i)) {
                    break;
                }
                if (isEscaped(text, currentIndex)) {
                    break;
                }
                if (i == closeSequence.length() - 1) {
                    walkChildren(text, startIndex, currentIndex, matches);
                    if (mapper == null || currentIndex <= startIndex) {
                        return;
                    }
                    var match = new TokenMatch<>(text, startIndex, currentIndex, mapper);
                    matches.add(match);
                    return;
                }
            }
        }

        if (this.mapper != null && currentIndex == text.length()) {
            matches.add(new TokenMatch<>(text, startIndex, currentIndex, this.mapper));
        }
    }

    private boolean isEscaped(String text, int index) {
        if (escapeSequence == null) {
            return false;
        }

        for (int i = index - escapeSequence.length() + 1, k = 0; k < escapeSequence.length(); i++, k++) {
            if (i < 0 && i >= index) {
                return false;
            }
            if (text.charAt(i) != escapeSequence.charAt(k)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AnyUntilNode<T> that = (AnyUntilNode<T>) o;

        return Objects.equals(closeSequence, that.closeSequence);
    }

    @Override
    public int hashCode() {
        return closeSequence != null ? closeSequence.hashCode() : 0;
    }

    public AnyUntilNode(String closeSequence, String escapeSequence) {
        this.closeSequence = closeSequence;
        this.escapeSequence = escapeSequence;
    }
}
