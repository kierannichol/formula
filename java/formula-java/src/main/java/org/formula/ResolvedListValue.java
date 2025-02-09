package org.formula;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

class ResolvedListValue extends ResolvedValue {

    private final List<ResolvedValue> values;

    ResolvedListValue(List<ResolvedValue> values) {
        this.values = new ArrayList<>(values);
    }

    @Override
    public String asText() {
        return latest().asText();
    }

    @Override
    public int asNumber() {
        return latest().asNumber();
    }

    @Override
    public double asDecimal() {
        return latest().asDecimal();
    }

    @Override
    public boolean asBoolean() {
        return latest().asBoolean();
    }

    @Override
    public List<ResolvedValue> asList() {
        return Collections.unmodifiableList(values);
    }

    private ResolvedValue latest() {
        if (values.isEmpty()) {
            return ResolvedValue.none();
        }
        return values.get(values.size() - 1);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ResolvedListValue that)) {
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
        return values.toString();
    }
}
