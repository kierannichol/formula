package org.formula.parse.shuntingyard;

import org.formula.ResolvedValue;

public record Operator3(String name, int precedence, Associativity associativity, OperatorFunction3 fn) implements Operator, OperatorFunction3 {

    @Override
    public ResolvedValue execute(ResolvedValue a1, ResolvedValue a2, ResolvedValue a3) {
        return fn.execute(a1, a2, a3);
    }

    @Override
    public String toString() {
        return name;
    }
}