package org.formula;

import org.formula.util.RollUtils;

public class ResolvedRollValue extends ResolvedValue {

    private final int count;
    private final int sides;

    public static ResolvedRollValue of(int count, int sides) {
        return new ResolvedRollValue(count, sides);
    }

    private ResolvedRollValue(int count, int sides) {
        this.count = count;
        this.sides = sides;
    }

    @Override
    public String toString() {
        return asText();
    }

    @Override
    public String asText() {
        return count + "d" + sides;
    }

    @Override
    public int asNumber() {
        return (int) asDecimal();
    }

    @Override
    public double asDecimal() {
        return RollUtils.average(count, sides);
    }

    @Override
    public boolean asBoolean() {
        return count > 0 && sides > 0;
    }
}
