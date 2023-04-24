package org.formula.context;

import java.util.Optional;
import java.util.stream.Stream;
import org.formula.Resolvable;
import org.formula.ResolvedValue;

class EmptyDataContext implements DataContext {

    @Override
    public Optional<Resolvable> get(String key) {
        return Optional.empty();
    }

    @Override
    public ResolvedValue resolve(String key) {
        return ResolvedValue.none();
    }

    @Override
    public Stream<String> keys() {
        return Stream.empty();
    }
}
