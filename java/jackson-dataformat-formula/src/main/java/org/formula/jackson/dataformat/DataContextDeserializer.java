package org.formula.jackson.dataformat;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.formula.Resolvable;
import org.formula.context.DataContext;

public class DataContextDeserializer extends StdDeserializer<DataContext> {

    protected DataContextDeserializer() {
        super(DataContext.class);
    }

    @Override
    public DataContext deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
            throws IOException {
        Map<String, Resolvable> data = new HashMap<>();

        ObjectCodec codec = jsonParser.getCodec();
        TreeNode node = codec.readTree(jsonParser);
        Iterator<String> fieldNameIterator = node.fieldNames();
        while (fieldNameIterator.hasNext()) {
            String fieldName = fieldNameIterator.next();
            Resolvable value = codec.treeToValue(node.get(fieldName), Resolvable.class);
            data.put(fieldName, value);
        }

        return DataContext.of(data);
    }
}
