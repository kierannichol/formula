package org.formula.test;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.formula.ResolvedValue;
import org.formula.context.DataContext;

public record FormulaTestCase(String name,
                              String formula,
                              DataContext data,
                              @JsonProperty("expected_text") String expectedText,
                              @JsonProperty("expected_number") Number expectedNumber,
                              @JsonProperty("expected_boolean") Boolean expectedBoolean,
                              @JsonProperty("expected_error") String expectedError,
                              @JsonProperty("expected_name") String expectedName) {

    public void assertResult(ResolvedValue result) {
        if (expectedNumber != null && expectedNumber instanceof Integer i) {
            assertThat(result.asNumber()).isEqualTo(i);
        }
        if (expectedNumber != null && expectedNumber instanceof Double d) {
            assertThat(result.asDecimal()).isEqualTo(d);
        }
        if (expectedNumber != null && expectedNumber instanceof Float f) {
            assertThat(result.asDecimal()).isEqualTo(f);
        }
        if (expectedNumber != null && expectedNumber instanceof Long l) {
            assertThat(result.asNumber()).isEqualTo(l);
        }
        if (expectedText != null) {
            assertThat(result.asText()).isEqualTo(expectedText);
        }
        if (expectedBoolean != null) {
            assertThat(result.asBoolean()).isEqualTo(expectedBoolean);
        }
    }
}
