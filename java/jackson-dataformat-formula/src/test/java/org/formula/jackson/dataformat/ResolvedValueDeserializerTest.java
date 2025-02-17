package org.formula.jackson.dataformat;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.formula.Resolvable;
import org.formula.ResolvedValue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ResolvedValueDeserializerTest {
    private static ObjectMapper mapper;

    @BeforeAll
    public static void setUp() {
        mapper = new ObjectMapper();;
        mapper.registerModule(new FormulaModule());
    }

    @Test
    void text() throws JsonProcessingException {
        String json = "\"Test\"";
        ResolvedValue expected = ResolvedValue.of("Test");
        ResolvedValue actual = mapper.readValue(json, ResolvedValue.class);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void integer() throws JsonProcessingException {
        String json = "5";
        ResolvedValue expected = ResolvedValue.of(5);
        ResolvedValue actual = mapper.readValue(json, ResolvedValue.class);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void decimal() throws JsonProcessingException {
        String json = "5.2";
        ResolvedValue expected = ResolvedValue.of(5.2);
        ResolvedValue actual = mapper.readValue(json, ResolvedValue.class);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void booleanTrue() throws JsonProcessingException {
        String json = "true";
        ResolvedValue expected = ResolvedValue.TRUE;
        ResolvedValue actual = mapper.readValue(json, ResolvedValue.class);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void booleanFalse() throws JsonProcessingException {
        String json = "true";
        ResolvedValue expected = ResolvedValue.TRUE;
        ResolvedValue actual = mapper.readValue(json, ResolvedValue.class);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void nullValue() throws JsonProcessingException {
        String json = "null";
        ResolvedValue expected = ResolvedValue.none();
        ResolvedValue actual = mapper.readValue(json, ResolvedValue.class);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void listOfNumbers() throws JsonProcessingException {
        String json = "[1, 2, 3]";
        ResolvedValue expected = ResolvedValue.concat(ResolvedValue.of(1), ResolvedValue.of(2), ResolvedValue.of(3));
        ResolvedValue actual = mapper.readValue(json, ResolvedValue.class);

        assertThat(actual).isEqualTo(expected);
    }
}