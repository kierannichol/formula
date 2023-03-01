package org.formula.parse.shuntingyard;

import java.util.Optional;
import org.formula.Resolvable;
import org.formula.ResolvedValue;
import org.formula.context.DataContext;

public record Term(ResolvedValue value) implements Resolvable, Node {

    public static Term of(ResolvedValue value) {
        return new Term(value);
    }

    public static Term of(String value) {
        return new Term(ResolvedValue.of(value));
    }

    public static Term of(int value) {
        return new Term(ResolvedValue.of(value));
    }

    public static Term of(double value) {
        return new Term(ResolvedValue.of(value));
    }

    public static Term of(boolean value) {
        return new Term(ResolvedValue.of(value));
    }

    @Override
    public ResolvedValue resolve(DataContext context) {
        return Optional.ofNullable(value)
                .orElse(ResolvedValue.none());
    }
}
