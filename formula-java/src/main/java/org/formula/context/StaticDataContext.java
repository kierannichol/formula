package org.formula.context;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.formula.Resolvable;
import org.formula.ResolvedValue;

public class StaticDataContext implements DataContext {
    private final Map<String, Resolvable> data;

    public static StaticDataContext empty() {
        return new StaticDataContext(new HashMap<>());
    }

    public static StaticDataContext of(Map<String, Resolvable> data) {
        return new StaticDataContext(data);
    }

    @Override
    public ResolvedValue get(String key) {
        return Optional.ofNullable(data.get(key))
                .map(resolvable -> resolvable.resolve(this))
                .orElse(ResolvedValue.none());
    }

    @Override
    public Stream<ResolvedValue> find(String pattern) {
        Predicate<String> patternFilter = Pattern.compile("^%s$".formatted(pattern
                        .replaceAll("\\*", ".*")))
                .asMatchPredicate();
        return keys()
                .filter(patternFilter)
                .map(this::get);
    }

    @Override
    public Stream<String> keys() {
        return data.keySet().stream();
    }

    public StaticDataContext set(String key, Resolvable value) {
        Map<String, Resolvable> copy = new HashMap<>(data);
        copy.put(key, value);
        return new StaticDataContext(copy);
    }

    public StaticDataContext set(String key, ResolvedValue value) {
        return set(key, Resolvable.just(value));
    }

    public StaticDataContext set(String key, String value) {
        return set(key, ResolvedValue.of(value));
    }

    public StaticDataContext set(String key, int value) {
        return set(key, ResolvedValue.of(value));
    }

    public StaticDataContext set(String key, double value) {
        return set(key, ResolvedValue.of(value));
    }

    public StaticDataContext set(String key, boolean value) {
        return set(key, ResolvedValue.of(value));
    }

    private StaticDataContext(Map<String, Resolvable> data) {
        this.data = data;
    }
}
