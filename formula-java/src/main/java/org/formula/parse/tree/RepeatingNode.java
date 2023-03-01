package org.formula.parse.tree;

import java.util.Objects;
import java.util.stream.Stream;
import org.formula.util.StreamUtils;

public class RepeatingNode<T> extends MatcherNode<T> {
    private final int minLength;
    private final int maxLength;

    public RepeatingNode(TokenMatcher matcher, int minLength, int maxLength) {
        super(matcher);
        this.minLength = minLength;
        this.maxLength = maxLength;
    }

    @Override
    public Stream<TokenMatch<T>> walk(String text, int startIndex, int currentIndex) {
        int minLength = this.minLength;
        int maxLength = calculateMaxLength(text, currentIndex);
        int length = 0;

        while (length < maxLength && currentIndex < text.length()) {
            char next = text.charAt(currentIndex);
            if (!matcher.matches(next)) {
                break;
            }
            currentIndex++;
            length++;
        }

        if (length < minLength) {
            return Stream.empty();
        }

        var matches = walkChildren(text, startIndex, currentIndex);
        if (length > maxLength || mapper == null) {
            return matches;
        }

        TokenMatch<T> match = new TokenMatch<>(text, startIndex, currentIndex, mapper);
        return StreamUtils.defaultIfEmpty(matches, () -> match);
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (getClass() != other.getClass()) {
            return false;
        }
        RepeatingNode<?> otherCasted = (RepeatingNode<?>) other;
        return Objects.equals(matcher, otherCasted.matcher)
                && Objects.equals(minLength, otherCasted.minLength)
                && Objects.equals(maxLength, otherCasted.maxLength);
    }

    private int calculateMaxLength(String text, int currentIndex) {
        int maxTextLength = text.length() - currentIndex;
        return Math.min(this.maxLength, maxTextLength);
    }
}
