package org.formula.util;

@FunctionalInterface
public interface Lambda2<A,B,U> {
    U execute(A a, B b);
}
