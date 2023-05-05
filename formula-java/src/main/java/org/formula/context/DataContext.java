package org.formula.context;

import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.formula.ResolvedValue;

public interface DataContext {
    DataContext EMPTY = new EmptyDataContext();

    ResolvedValue get(String key);
    Stream<String> keys();

    default Stream<ResolvedValue> search(String pattern) {
        Predicate<String> patternFilter = Pattern.compile("^%s$".formatted(pattern
                        .replaceAll("\\*", ".*")))
                .asMatchPredicate();
        return keys()
                .filter(patternFilter)
                .map(this::get);
    }
}
