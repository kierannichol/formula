package org.formula.jackson.dataformat;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.IOException;
import org.formula.ResolvedValue;

public class ResolvedValueDeserializer extends StdDeserializer<ResolvedValue> {

    protected ResolvedValueDeserializer() {
        super(ResolvedValue.class);
    }

    @Override
    public ResolvedValue deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
            throws IOException {
        return switch (jsonParser.currentToken()) {
            case VALUE_NUMBER_INT -> ResolvedValue.of(jsonParser.getValueAsInt());
            case VALUE_NUMBER_FLOAT -> ResolvedValue.of(jsonParser.getValueAsDouble());
            case VALUE_STRING -> ResolvedValue.of(jsonParser.getValueAsString());
            case VALUE_TRUE -> ResolvedValue.of(Boolean.TRUE);
            case VALUE_FALSE -> ResolvedValue.of(Boolean.FALSE);
            default -> throw new JsonParseException(jsonParser,
                    "ResolvedValue must be a string, integer, or boolean; was " + jsonParser.currentToken(),
                    jsonParser.currentLocation());
        };
    }

    @Override
    public ResolvedValue getNullValue(DeserializationContext ctxt) throws JsonMappingException {
        return ResolvedValue.none();
    }
}
