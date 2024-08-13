package org.formula.format;

import org.formula.ResolvedValue;

public class FormattedResolvedValue extends ResolvedValue {
    private final ResolvedValue actual;
    private final String formatted;

    public FormattedResolvedValue(ResolvedValue actual, String formatted) {
        this.actual = actual;
        this.formatted = formatted;
    }

    @Override
    public String asText() {
        return this.actual.asText();
    }

    @Override
    public int asNumber() {
        return this.actual.asNumber();
    }

    @Override
    public double asDecimal() {
        return this.actual.asDecimal();
    }

    @Override
    public boolean asBoolean() {
        return this.actual.asBoolean();
    }

    public String asFormatted() {
        return this.formatted;
    }

    @Override
    public boolean equals(Object obj) {
        return false;
    }
}
