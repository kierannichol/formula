package org.formula.parse.tree;

import java.util.List;
import java.util.Objects;

public class RepeatingNode<T> extends MatcherNode<T> {
    private final int minLength;
    private final int maxLength;

    public RepeatingNode(TokenMatcher matcher, int minLength, int maxLength) {
        super(matcher);
        this.minLength = minLength;
        this.maxLength = maxLength;
    }

    @Override
    public void walk(String text, int startIndex, int currentIndex, List<TokenMatch<T>> matches) {
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
            return;
        }

        walkChildren(text, startIndex, currentIndex, matches);
        if (length > maxLength || mapper == null) {
            return;
        }

        TokenMatch<T> match = new TokenMatch<>(text, startIndex, currentIndex, mapper);
        matches.add(match);
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
