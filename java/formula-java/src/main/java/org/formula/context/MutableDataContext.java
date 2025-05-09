package org.formula.context;

import org.formula.Resolvable;
import org.formula.ResolvedValue;

public interface MutableDataContext extends DataContext {

    static MutableDataContext create() {
        return new StaticDataContext();
    }

    MutableDataContext set(String key, Resolvable value);

    default MutableDataContext set(String key, ResolvedValue value) {
        return set(key, Resolvable.just(value));
    }

    default MutableDataContext set(String key, String value) {
        return set(key, ResolvedValue.of(value));
    }

    default MutableDataContext set(String key, int value) {
        return set(key, ResolvedValue.of(value));
    }

    default MutableDataContext set(String key, double value) {
        return set(key, ResolvedValue.of(value));
    }

    default MutableDataContext set(String key, boolean value) {
        return set(key, ResolvedValue.of(value));
    }

    MutableDataContext push(String key, Resolvable value);

    default MutableDataContext push(String key, ResolvedValue value) {
        return set(key, ResolvedValue.concat(get(key), value));
    }

    default MutableDataContext push(String key, String value) {
        return push(key, ResolvedValue.of(value));
    }

    default MutableDataContext push(String key, int value) {
        return push(key, ResolvedValue.of(value));
    }

    default MutableDataContext push(String key, double value) {
        return push(key, ResolvedValue.of(value));
    }

    default MutableDataContext push(String key, boolean value) {
        return push(key, ResolvedValue.of(value));
    }
}
