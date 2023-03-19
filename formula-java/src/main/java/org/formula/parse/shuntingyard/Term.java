package org.formula.parse.shuntingyard;

import java.util.Optional;
import org.formula.QuotedTextResolvedValue;
import org.formula.Resolvable;
import org.formula.ResolvedValue;
import org.formula.context.DataContext;

public record Term(ResolvedValue value, String prefix, String suffix) implements Resolvable, Node {

    public static Term of(ResolvedValue value) {
        return new Term(value, null, null);
    }

    public static Term of(ResolvedValue value, String prefix, String suffix) {
        return new Term(value, prefix, suffix);
    }

    public static Term of(String value) {
        return of(ResolvedValue.of(value));
    }

    public static Term of(String value, String prefix, String suffix) {
        return new Term(ResolvedValue.of(value), prefix, suffix);
    }

    public static Term of(int value) {
        return of(ResolvedValue.of(value));
    }

    public static Term of(double value) {
        return of(ResolvedValue.of(value));
    }

    public static Term of(boolean value) {
        return of(ResolvedValue.of(value));
    }

    @Override
    public ResolvedValue resolve(DataContext context) {
        return Optional.ofNullable(value)
                .map(value -> prefix != null && suffix != null
                        ? QuotedTextResolvedValue.of(value, prefix, suffix)
                        : value)
                .orElse(ResolvedValue.none());
    }
}
