package org.formula.parse.shuntingyard;

import java.util.List;
import org.formula.ResolvedValue;

public record FunctionN(String name, OperatorFunctionN fn) implements Function, OperatorFunctionN {

    public ResolvedValue execute(List<ResolvedValue> values) {
        return fn.execute(values);
    }

    @Override
    public String toString() {
        return name + "(0..n)";
    }
}
