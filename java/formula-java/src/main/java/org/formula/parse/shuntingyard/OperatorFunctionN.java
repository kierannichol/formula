package org.formula.parse.shuntingyard;

import java.util.List;
import org.formula.ResolvedValue;

public interface OperatorFunctionN {

    ResolvedValue execute(List<ResolvedValue> values);
}
