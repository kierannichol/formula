package org.formula.parse.shuntingyard;

import org.formula.Resolvable;
import org.formula.context.DataContext;

public interface VariableResolver {
    Resolvable resolve(DataContext context, String key);
}
