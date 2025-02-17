package org.formula;

import org.formula.context.DataContext;

class EmptyResolvable implements Resolvable {
    public static final Resolvable INSTANCE = new EmptyResolvable();

    @Override
    public ResolvedValue resolve() {
        return ResolvedValue.none();
    }

    @Override
    public ResolvedValue resolve(DataContext context) {
        return resolve();
    }

    @Override
    public String asFormula() {
        return "";
    }

    private EmptyResolvable() {
    }
}
