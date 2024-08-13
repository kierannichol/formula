package org.formula.parse.shuntingyard;

import org.formula.ResolvedValue;

public record Operator1(String name, int precedence, Associativity associativity, OperatorFunction1 fn) implements
        Operator, OperatorFunction1 {

    @Override
    public ResolvedValue execute(ResolvedValue a1) {
        return fn.execute(a1);
    }

    @Override
    public String toString() {
        return name;
    }
}
