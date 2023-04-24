package org.formula.parse.shuntingyard;

import org.formula.Resolvable;
import org.formula.context.DataContext;

public record Variable(String key, VariableResolver variableResolver) implements Node {

    public Resolvable get(DataContext context) {
        return variableResolver.resolve(context, key);
    }
}
