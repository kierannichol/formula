package org.formula;

// const M = 0x80000000;
//const A = 1103515245;
//const C = 12345;

public class FormulaRandom {
    private static final long M = 2147483647L;
    private static final long A = 1103515245L;
    private static final long C = 12345L;
    private static final long MAX_SEED = Math.floorDiv(0xFFFFL, A) - C - 1;
    private long state;

    public FormulaRandom() {
        this((long) Math.floor(Math.random() * (M - 1)));
    }

    public FormulaRandom(long seed) {
        this.state = seed % MAX_SEED;
    }

    public long nextInt() {
        var result = (A * this.state + C) % M;
        this.state = result % MAX_SEED;
        return result;
    }

    public float nextFloat() {
        return this.nextInt() / (float)(M - 1);
    }
}
