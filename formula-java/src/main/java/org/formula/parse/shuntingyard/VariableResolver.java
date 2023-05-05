package org.formula.parse.shuntingyard;

import org.formula.ResolvedValue;
import org.formula.context.DataContext;

public interface VariableResolver {
    ResolvedValue resolve(DataContext context, String key);
}
