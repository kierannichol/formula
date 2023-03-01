package org.formula.parse.shuntingyard;

import org.formula.Resolvable;
import org.formula.ResolvedValue;
import org.formula.context.DataContext;

public record Variable(String key, Resolver resolver) implements Resolvable, Node {

    @Override
    public ResolvedValue resolve(DataContext context) {
        return resolver.resolve(context, key);
    }
}
