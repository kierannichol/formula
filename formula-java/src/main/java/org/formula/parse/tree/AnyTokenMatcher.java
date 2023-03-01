package org.formula.parse.tree;

public class AnyTokenMatcher implements TokenMatcher {
    public static AnyTokenMatcher INSTANCE = new AnyTokenMatcher();

    @Override
    public boolean matches(char c) {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        return this == o;
    }

    @Override
    public int hashCode() {
        return 1;
    }

    private AnyTokenMatcher() {
    }
}
