package org.formula.jackson.dataformat;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.formula.Formula;
import org.formula.Resolvable;
import org.formula.ResolvedValue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ResolvableDeserializerTest {
    private static ObjectMapper mapper;

    @BeforeAll
    public static void setUp() {
        mapper = new ObjectMapper();;
        mapper.registerModule(new FormulaModule());
    }

    @Test
    void text() throws JsonProcessingException {
        String json = "\"Test\"";
        Resolvable expected = Resolvable.just("Test");
        Resolvable actual = mapper.readValue(json, Resolvable.class);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void integer() throws JsonProcessingException {
        String json = "5";
        Resolvable expected = Resolvable.just(5);
        Resolvable actual = mapper.readValue(json, Resolvable.class);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void decimal() throws JsonProcessingException {
        String json = "5.2";
        Resolvable expected = Resolvable.just(5.2);
        Resolvable actual = mapper.readValue(json, Resolvable.class);

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
    void expression() throws JsonProcessingException {
        String json = "\"{@a}\"";
        Resolvable expected = Formula.parse("@a");
        Resolvable actual = mapper.readValue(json, Resolvable.class);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void listOfNumbers() throws JsonProcessingException {
        String json = "[1, 2, 3]";
        Resolvable expected = Resolvable.concat(Resolvable.just(1), Resolvable.just(2), Resolvable.just(3));
        Resolvable actual = mapper.readValue(json, Resolvable.class);

        assertThat(actual).isEqualTo(expected);
    }
}