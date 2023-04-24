package org.formula.parse.tree;

import java.util.List;
import java.util.Objects;

public class MatcherNode<T> extends MappableNode<T> {
    protected final TokenMatcher matcher;

    @Override
    public void walk(String text, int startIndex, int currentIndex, List<TokenMatch<T>> matches) {
        if (!matches(text, startIndex, currentIndex)) {
            return;
        }
        walkChildren(text, startIndex, currentIndex + 1, matches);
        if (mapper == null) {
            return;
        }
        var match = new TokenMatch<>(text, startIndex, currentIndex + 1, mapper);
        matches.add(match);
    }

    protected boolean matches(String text, int startIndex, int currentIndex) {
        if (currentIndex >= text.length()) {
            return false;
        }
        char current = text.charAt(currentIndex);
        return matcher.matches(current);
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        return getClass() == other.getClass()
                && Objects.equals(matcher, ((MatcherNode<T>) other).matcher);
    }

    public MatcherNode(TokenMatcher matcher) {
        this.matcher = matcher;
    }
}
