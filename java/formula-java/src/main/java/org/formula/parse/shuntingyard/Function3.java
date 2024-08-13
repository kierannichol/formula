package org.formula.parse.shuntingyard;

import org.formula.ResolvedValue;

public record Function3(String name, OperatorFunction3 fn) implements Function, OperatorFunction3 {

    public ResolvedValue execute(ResolvedValue a1, ResolvedValue a2, ResolvedValue a3) {
        return fn.execute(a1, a2, a3);
    }

    @Override
    public String toString() {
        return name + "(a, b, c)";
    }
}
