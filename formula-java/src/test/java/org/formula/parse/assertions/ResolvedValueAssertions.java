package org.formula.parse.assertions;

import static org.assertj.core.api.Assertions.assertThat;

import org.formula.Resolvable;
import org.formula.ResolvedValue;
import org.formula.context.DataContext;

public class ResolvedValueAssertions {
    private final ResolvedValue value;

    public static ResolvedValueAssertions assertResolvedValue(Resolvable resolvable) {
        return new ResolvedValueAssertions(resolvable.resolve());
    }

    public static ResolvedValueAssertions assertResolvedValue(Resolvable resolvable, DataContext context) {
        return new ResolvedValueAssertions(resolvable.resolve(context));
    }

    public static ResolvedValueAssertions assertResolvedValue(ResolvedValue value) {
        return new ResolvedValueAssertions(value);
    }

    public ResolvedValueAssertions hasValue(String expected) {
        assertThat(value.asText()).isEqualTo(expected);
        return this;
    }

    public ResolvedValueAssertions hasValue(int expected) {
        assertThat(value.asNumber()).isEqualTo(expected);
        return this;
    }

    public ResolvedValueAssertions hasValue(double expected) {
        assertThat(value.asDecimal()).isEqualTo(expected);
        return this;
    }

    public ResolvedValueAssertions hasValue(boolean expected) {
        assertThat(value.asBoolean()).isEqualTo(expected);
        return this;
    }

    public ResolvedValueAssertions hasNoValue() {
        assertThat(value).isEqualTo(ResolvedValue.none());
        return this;
    }

    private ResolvedValueAssertions(ResolvedValue value) {
        this.value = value;
    }
}
