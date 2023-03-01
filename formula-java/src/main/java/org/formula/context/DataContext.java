package org.formula.context;

import java.util.stream.Stream;
import org.formula.ResolvedValue;

public interface DataContext {

    StaticDataContext EMPTY = StaticDataContext.empty();

    ResolvedValue get(String key);
    Stream<ResolvedValue> find(String pattern);
    Stream<String> keys();
}
