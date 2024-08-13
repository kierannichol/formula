package org.formula.parse.shuntingyard;

public record BiOperatorFunction(String symbol, Operator1 unaryOperator, Operator2 binaryOperator) implements Node {

}
