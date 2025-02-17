package org.formula;

import java.util.List;
import org.formula.context.DataContext;

public interface Resolvable {

    static Resolvable just(String value) {
        return StaticResolvable.of(ResolvedValue.of(value));
    }

    static Resolvable just(int value) {
        return StaticResolvable.of(ResolvedValue.of(value));
    }

    static Resolvable just(double value) {
        return StaticResolvable.of(ResolvedValue.of(value));
    }

    static Resolvable just(boolean value) {
        return StaticResolvable.of(ResolvedValue.of(value));
    }

    static Resolvable just(ResolvedValue value) {
        return StaticResolvable.of(value);
    }

    static Resolvable empty() {
        return EmptyResolvable.INSTANCE;
    }

    default ResolvedValue resolve() {
        return resolve(DataContext.EMPTY);
    }

    ResolvedValue resolve(DataContext context);

    String asFormula();

    static Resolvable concat(Resolvable... values) {
        return concat(List.of(values));
    }

    static Resolvable concat(Iterable<Resolvable> values) {
        return new ResolvableList(values);
    }
}
