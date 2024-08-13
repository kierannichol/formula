package org.formula.parse.shuntingyard;

public record Literal(String value) implements Node {

    public static Literal of(String value) {
        return new Literal(value);
    }
}
