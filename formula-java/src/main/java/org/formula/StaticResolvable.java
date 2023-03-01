package org.formula;

import java.util.Optional;
import org.formula.context.DataContext;

public class StaticResolvable implements Resolvable {
    private final ResolvedValue resolved;

    public static Resolvable of(ResolvedValue value) {
        return new StaticResolvable(value);
    }

    @Override
    public ResolvedValue resolve() {
        return Optional.ofNullable(resolved).orElse(ResolvedValue.none());
    }

    @Override
    public ResolvedValue resolve(DataContext context) {
        return resolve();
    }

    private StaticResolvable(ResolvedValue resolved) {
        this.resolved = resolved;
    }
}
