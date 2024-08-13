package org.formula.context;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import org.formula.Resolvable;
import org.formula.ResolvedValue;

class StaticDataContext implements MutableDataContext {
    private final Map<String, Resolvable> data;

    public static StaticDataContext of(Map<String, Resolvable> data) {
        return new StaticDataContext(new HashMap<>(data));
    }

    @Override
    public ResolvedValue get(String key) {
        Resolvable resolvable = data.get(key);
        if (resolvable == null) {
            return ResolvedValue.none();
        }

        return resolvable.resolve(this);
    }

    @Override
    public Stream<String> keys() {
        return data.keySet().stream();
    }

    public StaticDataContext set(String key, Resolvable value) {
        data.put(key, value);
        return this;
    }

    @Override
    public String toString() {
        return data.toString();
    }

    StaticDataContext() {
        this(new HashMap<>());
    }

    StaticDataContext(Map<String, Resolvable> data) {
        this.data = data;
    }
}
