package org.formula.test;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OptimizeTestCase(String name,
                               String formula,
                               @JsonProperty("expected_formula") String expectedFormula) {

}
