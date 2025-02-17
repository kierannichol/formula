package org.formula.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import org.formula.jackson.dataformat.FormulaModule;

public class TestCaseLoader {
    private static final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

    static {
        mapper.registerModule(new FormulaModule());
    }

    public static <T> List<T> load(Class<? extends T> testCaseType, String resourceName) {
        try (var resourceStream = ClassLoader.getSystemResourceAsStream(resourceName)) {
            return mapper.readValue(resourceStream, mapper.getTypeFactory().constructCollectionLikeType(List.class, testCaseType));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
