package org.formula.parse.shuntingyard;

import org.formula.ResolvedValue;

public interface OperatorFunction3 {

    ResolvedValue execute(ResolvedValue a1, ResolvedValue a2, ResolvedValue a3);
}
