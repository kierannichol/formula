package org.formula.parse.shuntingyard;

import org.formula.ResolvedValue;

public record Function0(String name, OperatorFunction0 fn) implements Function, OperatorFunction0 {

    public ResolvedValue execute() {
        return fn.execute();
    }

    @Override
    public String toString() {
        return name + "()";
    }
}
