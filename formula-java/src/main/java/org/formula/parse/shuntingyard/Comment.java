package org.formula.parse.shuntingyard;

import org.formula.ResolvedValue;
import org.formula.util.Lambda2;

public record Comment(String text, Lambda2<ResolvedValue, String, ResolvedValue> fn) implements Node {

    public static Comment of(String text, Lambda2<ResolvedValue, String, ResolvedValue> fn) {
        return new Comment(text, fn);
    }
}
