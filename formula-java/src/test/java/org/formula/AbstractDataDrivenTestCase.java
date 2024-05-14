package org.formula;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

public abstract class AbstractDataDrivenTestCase {

    protected abstract String getDataResourceName();

    protected abstract void test(String given, String expected);

    @TestFactory
    @DisplayName("data-driven test cases")
    Stream<DynamicTest> dataDrivenTestCases() {
        InputStream is = ClassLoader.getSystemResourceAsStream(getDataResourceName());
        assert is != null;
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        return reader.lines()
                .skip(1)
                .map(line -> {
                    List<String> parts = splitCsv(line);
                    String displayName = parts.get(0);
                    String given = parts.get(1);
                    String expected = parts.get(2);

                    return DynamicTest.dynamicTest(displayName, () -> {
                        test(given, expected);
                    });
                });
    }

    private static List<String> splitCsv(String csv) {
        return Stream.of(csv.split("\\|"))
                .map(String::trim)
                .collect(Collectors.toList());
    }
}
