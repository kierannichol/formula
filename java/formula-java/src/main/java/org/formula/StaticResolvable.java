package org.formula;

import java.util.Objects;
import java.util.Optional;
import org.formula.context.DataContext;

public class StaticResolvable implements Resolvable {
    private final ResolvedValue resolved;

    public static Resolvable of(ResolvedValue value) {
        if (value == null) {
            return Resolvable.empty();
        }
        return new StaticResolvable(value);
    }

    @Override
    public ResolvedValue resolve() {
        return resolved;
    }

    @Override
    public ResolvedValue resolve(DataContext context) {
        return resolve();
    }

    @Override
    public String asFormula() {
        return resolved.asText();
    }

    private StaticResolvable(ResolvedValue resolved) {
        this.resolved = resolved;
    }

    @Override
    public String toString() {
        return resolved.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        StaticResolvable that = (StaticResolvable) o;
        return Objects.equals(resolved, that.resolved);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(resolved);
    }
}
