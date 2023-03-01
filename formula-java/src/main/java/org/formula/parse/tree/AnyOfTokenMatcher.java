package org.formula.parse.tree;

import java.util.Arrays;

public class AnyOfTokenMatcher implements TokenMatcher {
    private final char[] allowed;

    public static AnyOfTokenMatcher of(CharSequence allowed) {
        return new AnyOfTokenMatcher(allowed.toString().toCharArray());
    }

    public static AnyOfTokenMatcher of(char[] allowed) {
        return new AnyOfTokenMatcher(allowed);
    }

    @Override
    public boolean matches(char c) {
        for (char value : allowed) {
            if (value == c) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AnyOfTokenMatcher that = (AnyOfTokenMatcher) o;
        return Arrays.equals(allowed, that.allowed);
    }

    @Override
    public int hashCode() {
        return allowed != null ? Arrays.hashCode(allowed) : 0;
    }

    private AnyOfTokenMatcher(char[] allowed) {
        this.allowed = allowed;
    }
}
