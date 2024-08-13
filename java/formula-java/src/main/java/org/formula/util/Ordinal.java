package org.formula.util;

import static java.lang.Integer.parseInt;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Ordinal {
    private static final Pattern pattern = Pattern.compile("^(\\d+)(?:th|st|nd|rd)?$");
    private static final String[] suffixes = new String[] { "th", "st", "nd", "rd" };

    public static String toString(int n) {
        int v = n % 100;
        return n + suffix((v - 20) % 10)
                .or(() -> suffix(v))
                .orElse(suffixes[0]);
    }

    public static int parseOrdinal(String str) {
        Matcher matcher = pattern.matcher(str);
        if (!matcher.find()) {
            throw new IllegalArgumentException(str + " is not an ordinal number");
        }
        return parseInt(matcher.group(1));
    }

    private static Optional<String> suffix(int index) {
        if (index < suffixes.length && index >= 0) {
            return Optional.of(suffixes[index]);
        }
        return Optional.empty();
    }
}
