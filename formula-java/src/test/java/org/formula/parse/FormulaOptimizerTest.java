package org.formula.parse;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class FormulaOptimizerTest {

    @Test
    void any() {
        var optimized = FormulaOptimizer.optimize("any(any(@a, any(@b, @c)), @d)");
        assertThat(optimized).isEqualTo("any(@a,@b,@c,@d)");
    }

    @Test
    void all() {
        var optimized = FormulaOptimizer.optimize("all(any(@a, all(@b)), @c, all(@d AND @e), @f)");
        assertThat(optimized).isEqualTo("all(any(@a,@b),@c,@d,@e,@f)");
    }

    @Test
    void bracketAdd() {
        var optimized = FormulaOptimizer.optimize("@a + (@b + @c)");
        assertThat(optimized).isEqualTo("@a+@b+@c");
    }

    @Test
    void keepsRequiredBrackets() {
        assertThat(FormulaOptimizer.optimize("@a * (@b + @c + @d)/2")).isEqualTo("@a*(@b+@c+@d)/2");
        assertThat(FormulaOptimizer.optimize("@a - (@b / @c)")).isEqualTo("@a-(@b/@c)");
        assertThat(FormulaOptimizer.optimize("@a < (@b - @c)")).isEqualTo("@a<(@b-@c)");
    }

    @Test
    void literals() {
        assertThat(FormulaOptimizer.optimize("\"testing\"")).isEqualTo("\"testing\"");
        assertThat(FormulaOptimizer.optimize("any(@a,\"testing\")")).isEqualTo("any(@a,\"testing\")");
    }

    @Test
    void comments() {
        assertThat(FormulaOptimizer.optimize("(@a+@b)[testing]")).isEqualTo("(@a+@b)[testing]");
    }
}