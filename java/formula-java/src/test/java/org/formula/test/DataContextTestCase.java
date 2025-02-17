package org.formula.test;

import java.util.List;
import java.util.Map;
import org.formula.Resolvable;

public record DataContextTestCase(String name,
                                  Map<String, Resolvable> data,
                                  List<String> actions,
                                  Map<String, ExpectedValues> expected) {
}
