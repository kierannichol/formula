package org.formula.parse.shuntingyard;

import org.formula.ResolvedValue;

public record Function2(String name, OperatorFunction2 fn) implements Function, OperatorFunction2 {

    public ResolvedValue execute(ResolvedValue a1, ResolvedValue a2) {
        return fn.execute(a1, a2);
    }

    @Override
    public String toString() {
        return name + "(a, b)";
    }
}
