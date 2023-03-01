package org.formula.parse.tree;

import java.util.Objects;
import java.util.stream.Stream;
import org.formula.util.StreamUtils;

public class AnyUntilNode<T> extends MappableNode<T> {
    private final String closeSequence;
    private final String escapeSequence;

    @Override
    public Stream<TokenMatch<T>> walk(String text, int startIndex, int currentIndex) {
        for (; currentIndex < text.length(); currentIndex++) {
            for (int i = 0; i < closeSequence.length(); i++) {
                if (text.charAt(currentIndex + i) != closeSequence.charAt(i)) {
                    break;
                }
                if (isEscaped(text, currentIndex)) {
                    break;
                }
                if (i == closeSequence.length() - 1) {
                    var matches = walkChildren(text, startIndex, currentIndex);
                    if (mapper == null || currentIndex <= startIndex) {
                        return matches;
                    }
                    var match = new TokenMatch<>(text, startIndex, currentIndex, mapper);
                    return StreamUtils.defaultIfEmpty(matches,
                            () -> match);
                }
            }
        }

        return this.mapper != null && currentIndex == text.length()
                ? Stream.of(new TokenMatch<>(text, startIndex, currentIndex, this.mapper))
                : Stream.empty();
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
