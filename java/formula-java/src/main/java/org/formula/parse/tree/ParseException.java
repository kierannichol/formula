package org.formula.parse.tree;

public class ParseException extends RuntimeException {
    private final String text;
    private final int index;

    public ParseException(String message, String text, int index) {
        super(message);
        this.text = text;
        this.index = index;
    }

    public String text() {
        return text;
    }

    public int index() {
        return index;
    }
}
