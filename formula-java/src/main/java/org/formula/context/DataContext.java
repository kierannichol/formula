package org.formula.context;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.formula.Resolvable;
import org.formula.ResolvedValue;

public interface DataContext {
    DataContext EMPTY = new EmptyDataContext();

    Optional<Resolvable> get(String key);
    ResolvedValue resolve(String key);
    Stream<String> keys();

    default Stream<ResolvedValue> search(String pattern) {
        Predicate<String> patternFilter = Pattern.compile("^%s$".formatted(pattern
                        .replaceAll("\\*", ".*")))
                .asMatchPredicate();
        return keys()
                .filter(patternFilter)
                .map(this::resolve);
    }
}
