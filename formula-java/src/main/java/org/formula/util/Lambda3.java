package org.formula.util;

@FunctionalInterface
public interface Lambda3<A,B,C,U> {
    U execute(A a, B b, C c);
}
