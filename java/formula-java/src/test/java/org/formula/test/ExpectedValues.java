package org.formula.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.formula.ResolvedValue;

public record ExpectedValues(String expected_text,
                             Integer expected_number,
                             Boolean expected_boolean,
                             List<ExpectedValues> expected_list) {

    public void assertEqualsAll(String key, ResolvedValue actual) {
        if (expected_text != null) {
            assertThat(actual.asText())
                    .as("Expected key '%s' to have text value '%s', but was '%s'", key, expected_text, actual.asText())
                    .isEqualTo(expected_text);
        }
        if (expected_number != null) {
            assertThat(actual.asNumber())
                    .as("Expected key '%s' to have numeric value %d, but was %d", key, expected_number, actual.asNumber())
                    .isEqualTo(expected_number);
        }
        if (expected_boolean != null) {
            assertThat(actual.asBoolean())
                    .as("Expected key '%s' to have boolean value %s, but was %s", key, expected_boolean, actual.asBoolean())
                    .isEqualTo(expected_boolean);
        }
        if (expected_list != null) {
            assertThat(actual.asList()).hasSize(expected_list.size());
            for (int i = 0; i < expected_list.size(); i++) {
                expected_list.get(i).assertEqualsAll(key + "[" + i + "]", actual.asList().get(i));
            }
        }
    }
}
