package org.formula.jackson.dataformat;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.formula.ResolvedValue;
import org.formula.context.DataContext;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DataContextDeserializerTest {
    private static ObjectMapper mapper;

    @BeforeAll
    public static void setUp() {
        mapper = new ObjectMapper();;
        mapper.registerModule(new FormulaModule());
    }

    @Test
    @DisplayName("empty")
    void empty() throws JsonProcessingException {
        String json = "{}";
        DataContext actual = mapper.readValue(json, DataContext.class);
        assertThat(actual.keys()).isEmpty();
    }

    @Test
    @DisplayName("single text value")
    void singleTextValue() throws JsonProcessingException {
        String json = "{\"a\":\"Test\"}";
        DataContext actual = mapper.readValue(json, DataContext.class);
        assertThat(actual.get("a")).isEqualTo(ResolvedValue.of("Test"));
    }

    @Test
    @DisplayName("single integer value")
    void singleIntegerValue() throws JsonProcessingException {
        String json = "{\"b\":42}";
        DataContext actual = mapper.readValue(json, DataContext.class);
        assertThat(actual.get("b")).isEqualTo(ResolvedValue.of(42));
    }

    @Test
    @DisplayName("single decimal value")
    void singleDecimalValue() throws JsonProcessingException {
        String json = "{\"c\":3.14}";
        DataContext actual = mapper.readValue(json, DataContext.class);
        assertThat(actual.get("c")).isEqualTo(ResolvedValue.of(3.14));
    }

    @Test
    @DisplayName("single boolean value: true")
    void singleBooleanValueTrue() throws JsonProcessingException {
        String json = "{\"d\":true}";
        DataContext actual = mapper.readValue(json, DataContext.class);
        assertThat(actual.get("d")).isEqualTo(ResolvedValue.TRUE);
    }

    @Test
    @DisplayName("single boolean value: false")
    void singleBooleanValueFalse() throws JsonProcessingException {
        String json = "{\"d\":false}";
        DataContext actual = mapper.readValue(json, DataContext.class);
        assertThat(actual.get("d")).isEqualTo(ResolvedValue.FALSE);
    }

    @Test
    @DisplayName("expression value")
    void expression() throws JsonProcessingException {
        String json = "{\"a\":\"Test\", \"formula\":\"{@a}\"}";
        DataContext actual = mapper.readValue(json, DataContext.class);
        assertThat(actual.get("formula")).isEqualTo(ResolvedValue.of("Test"));
    }
}