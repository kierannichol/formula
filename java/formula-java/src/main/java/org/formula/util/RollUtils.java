package org.formula.util;

public class RollUtils {

    public static double average(int count, int sides) {
        return (count * (sides + 1)) / 2.0;
    }

    private RollUtils() {}
}
