package org.formula.context;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import org.formula.Resolvable;
import org.formula.ResolvedValue;

class StaticDataContext implements MutableDataContext {
    private final Map<String, Resolvable> data;

    public static StaticDataContext of(Map<String, Resolvable> data) {
        return new StaticDataContext(new HashMap<>(data));
    }

    @Override
    public Optional<Resolvable> get(String key) {
        return Optional.ofNullable(data.get(key));
    }

    @Override
    public ResolvedValue resolve(String key) {
        return Optional.ofNullable(data.get(key))
                .map(resolvable -> resolvable.resolve(this))
                .orElse(ResolvedValue.none());
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
