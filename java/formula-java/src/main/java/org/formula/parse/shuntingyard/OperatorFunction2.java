package org.formula.parse.shuntingyard;

import org.formula.ResolvedValue;

public interface OperatorFunction2 {

    ResolvedValue execute(ResolvedValue a1, ResolvedValue a2);
}
