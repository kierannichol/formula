package org.formula;

import static org.formula.Formula.parse;
import static org.formula.parse.assertions.ResolvedValueAssertions.assertResolvedValue;

import org.formula.context.MutableDataContext;
import org.junit.jupiter.api.Test;

public class FormulaLogicTest {

    @Test
    void numberEquals() {
        assertResolvedValue(parse("5==5")).hasValue(true);
        assertResolvedValue(parse("5==6")).hasValue(false);
        assertResolvedValue(parse("5==@five"), MutableDataContext.create().set("five", 5)).hasValue(true);
        assertResolvedValue(parse("5==@five"), MutableDataContext.create().set("five", "5")).hasValue(true);
    }

    @Test
    void textEquals() {
        assertResolvedValue(parse("'ABC'=='ABC'")).hasValue(true);
        assertResolvedValue(parse("'ABC'=='XYZ'")).hasValue(false);
        assertResolvedValue(parse("'5'==@five"), MutableDataContext.create().set("five", 5)).hasValue(true);
        assertResolvedValue(parse("'5'==@five"), MutableDataContext.create().set("five", "5")).hasValue(true);
    }
}
