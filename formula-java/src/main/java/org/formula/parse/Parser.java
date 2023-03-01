package org.formula.parse;

import org.formula.Resolvable;

public interface Parser {
    Resolvable parse(String text);
}
