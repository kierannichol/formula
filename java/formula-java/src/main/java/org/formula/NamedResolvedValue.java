package org.formula;

public class NamedResolvedValue extends ResolvedValue {
    private final ResolvedValue value;
    private final String name;
    private final String prefix;
    private final String suffix;

    public static NamedResolvedValue of(ResolvedValue value, String name, String prefix, String suffix) {
        return new NamedResolvedValue(value, name, prefix, suffix);
    }

    private NamedResolvedValue(ResolvedValue value, String name, String prefix, String suffix) {
        this.value = value;
        this.name = name;
        this.prefix = prefix;
        this.suffix = suffix;
    }

    @Override
    public String asText() {
        return value.asText();
    }

    public String asName() {
        return name;
    }

    @Override
    public int asNumber() {
        return value.asNumber();
    }

    @Override
    public double asDecimal() {
        return value.asDecimal();
    }

    @Override
    public boolean asBoolean() {
        return value.asBoolean();
    }

    @Override
    public String toString() {
        return value.asText() + prefix + asName() + suffix;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ResolvedValue that)) {
            return false;
        }

        return value.equals(that);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
