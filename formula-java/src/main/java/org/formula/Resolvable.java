package org.formula;

import org.formula.context.DataContext;

public interface Resolvable {

    static Resolvable just(String value) {
        return StaticResolvable.of(ResolvedValue.of(value));
    }

    static Resolvable just(int value) {
        return StaticResolvable.of(ResolvedValue.of(value));
    }

    static Resolvable just(boolean value) {
        return StaticResolvable.of(ResolvedValue.of(value));
    }

    static Resolvable just(ResolvedValue value) {
        return StaticResolvable.of(value);
    }

    static Resolvable empty() {
        return StaticResolvable.of(null);
    }

    default ResolvedValue resolve() {
        return resolve(DataContext.EMPTY);
    }

    ResolvedValue resolve(DataContext context);
}
