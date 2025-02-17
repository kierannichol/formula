package org.formula;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.formula.context.DataContext;

public class ResolvableList implements Resolvable {
    private final List<Resolvable> values = new ArrayList<>();

    public ResolvableList() {
    }

    public ResolvableList(Iterable<Resolvable> values) {
        values.forEach(this::add);
    }

    public ResolvableList add(Resolvable value) {
        values.add(value);
        return this;
    }

    @Override
    public ResolvedValue resolve(DataContext context) {
        return ResolvedValue.concat(values.stream()
                .map(resolvable -> resolvable.resolve(context)));
    }

    @Override
    public String asFormula() {
        StringBuilder builder = new StringBuilder();
        builder.append("<");
        int i = 0;
        for (Resolvable value : values) {
            if (i++ > 0) {
                builder.append(",");
            }
            builder.append(value.asFormula());
        }
        builder.append(">");
        return builder.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ResolvableList that)) {
            return false;
        }
        return Objects.equals(values, that.values);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(values);
    }

    @Override
    public String toString() {
        return asFormula();
    }
}
