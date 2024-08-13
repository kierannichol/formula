package org.formula.jackson.dataformat;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.IOException;
import org.formula.Formula;
import org.formula.Resolvable;
import org.formula.ResolvedValue;

public class ResolvableDeserializer extends StdDeserializer<Resolvable> {

    protected ResolvableDeserializer() {
        super(Resolvable.class);
    }

    @Override
    public Resolvable deserialize(JsonParser jp, DeserializationContext deserializationContext)
            throws IOException {
        var currentToken = jp.currentToken();
        if (currentToken == JsonToken.VALUE_STRING) {
            String currentString = jp.getText();
            if (currentString.startsWith("{") && currentString.endsWith("}")) {
                String formula = currentString.substring(1, currentString.length() - 1);
                return Formula.parse(formula);
            }
        }
        return Resolvable.just(deserializationContext.readValue(jp, ResolvedValue.class));
    }

    @Override
    public Resolvable getNullValue(DeserializationContext ctxt) {
        return Resolvable.empty();
    }
}
