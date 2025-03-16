package org.formula;

import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.data.Offset;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FormulaRandomTest {

    @Test
    @DisplayName("random int")
    void randomInt() {
        long seed = 10L;
        var random = new FormulaRandom(seed);

        assertThat(random.nextInt()).isEqualTo(297746560L);
        assertThat(random.nextInt()).isEqualTo(1120512893L);
    }

    @Test
    @DisplayName("random int (different seed)")
    void randomIntDifferentSeed() {
        long seed = 12345L;
        var random = new FormulaRandom(seed);

        assertThat(random.nextInt()).isEqualTo(1406938949L);
        assertThat(random.nextInt()).isEqualTo(506849219L);
    }

    @Test
    @DisplayName("random float")
    void randomFloat() {
        long seed = 10L;
        var random = new FormulaRandom(seed);

        assertThat(random.nextFloat()).isCloseTo(0.13864f, Offset.offset(0.005f));
        assertThat(random.nextFloat()).isCloseTo(0.52177f, Offset.offset(0.005f));
    }
}