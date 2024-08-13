package org.formula.jackson.dataformat;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.util.List;
import java.util.Map;
import org.formula.Resolvable;
import org.formula.ResolvedValue;
import org.formula.context.DataContext;

public class FormulaModule extends SimpleModule {
    private static final Version VERSION = new Version(1, 0, 0, null, null, null);

    public FormulaModule() {
        super("FormulaModule",
                VERSION,
                deserializers(),
                serializers());
    }

    private static List<JsonSerializer<?>> serializers() {
        return List.of(

        );
    }

    private static Map<Class<?>, JsonDeserializer<?>> deserializers() {
        return Map.of(
                ResolvedValue.class, new ResolvedValueDeserializer(),
                Resolvable.class, new ResolvableDeserializer(),
                DataContext.class, new DataContextDeserializer()
        );
    }
}
