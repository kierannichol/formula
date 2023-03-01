package org.formula.parse.tree;

import java.util.Objects;

public class NotTokenMatcher implements TokenMatcher {
    private final TokenMatcher matcher;

    @Override
    public boolean matches(char c) {
        return !matcher.matches(c);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        NotTokenMatcher that = (NotTokenMatcher) o;
        return Objects.equals(matcher, that.matcher);
    }

    @Override
    public int hashCode() {
        return matcher != null ? matcher.hashCode() : 0;
    }

    public NotTokenMatcher(TokenMatcher matcher) {
        this.matcher = matcher;
    }
}
