package org.formula.parse.shuntingyard;

import org.formula.ResolvedValue;

public record Operator0(String name, int precedence, Associativity associativity, OperatorFunction0 fn) implements Operator, OperatorFunction0 {

    @Override
    public ResolvedValue execute() {
        return fn.execute();
    }

    @Override
    public String toString() {
        return name;
    }
}
