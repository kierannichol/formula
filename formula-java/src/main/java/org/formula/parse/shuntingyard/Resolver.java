package org.formula.parse.shuntingyard;

import org.formula.ResolvedValue;
import org.formula.context.DataContext;

public interface Resolver {
    ResolvedValue resolve(DataContext context, String key);
}
