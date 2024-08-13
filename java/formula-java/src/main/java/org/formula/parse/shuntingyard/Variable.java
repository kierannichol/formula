package org.formula.parse.shuntingyard;

import org.formula.Resolvable;
import org.formula.ResolvedValue;
import org.formula.context.DataContext;

public record Variable(String key, VariableResolver variableResolver) implements Node {

    public ResolvedValue get(DataContext context) {
        return variableResolver.resolve(context, key);
    }
}
