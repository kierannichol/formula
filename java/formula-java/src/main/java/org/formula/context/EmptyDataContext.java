package org.formula.context;

import java.util.stream.Stream;
import org.formula.ResolvedValue;

class EmptyDataContext implements DataContext {

    @Override
    public ResolvedValue get(String key) {
        return ResolvedValue.none();
    }

    @Override
    public Stream<String> keys() {
        return Stream.empty();
    }
}
