package org.formula.parse.shuntingyard;

import org.formula.ResolvedValue;

public interface OperatorFunction1 {

    ResolvedValue execute(ResolvedValue a1);
}
