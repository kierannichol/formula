package org.formula;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public abstract class ResolvedValue implements Comparable<ResolvedValue> {
    public static final ResolvedValue TRUE = of(true);
    public static final ResolvedValue FALSE = of(false);
    public static final ResolvedValue ZERO = of(0);

    public static ResolvedValue of(String value) {
        if (value == null) {
            return NullResolvedValue.INSTANCE;
        }
        return new TextResolvedValue(value);
    }

    public static ResolvedValue of(int value) {
        return new NumberResolvedValue(value);
    }

    public static ResolvedValue of(double value) {
        return new DecimalResolvedValue(value);
    }

    public static ResolvedValue of(boolean value) {
        return value
                ? BooleanTrueResolvedValue.INSTANCE
                : BooleanFalseResolvedValue.INSTANCE;
    }

    public static ResolvedValue none() {
        return NullResolvedValue.INSTANCE;
    }

    public abstract String asText();
    public abstract int asNumber();
    public abstract double asDecimal();
    public abstract boolean asBoolean();

    public boolean hasValue() {
        return true;
    }

    @Override
    public int compareTo(ResolvedValue o) {
        return Double.compare(asDecimal(), o.asDecimal());
    }

    

    private static class TextResolvedValue extends ResolvedValue {
        private static final List<String> FALSE_STRING_VALUES = List.of("false", "no", "0", "");
        private final String value;

        @Override
        public String asText() {
            return value;
        }

        @Override
        public int asNumber() {
            return Integer.parseInt(value);
        }

        @Override
        public double asDecimal() {
            try {
                return Double.parseDouble(value);
            } catch (NumberFormatException e) {
                throw new ResolveException("Cannot convert '%s' to a number".formatted(value));
            }
        }

        @Override
        public boolean asBoolean() {
            return FALSE_STRING_VALUES.contains(value.toLowerCase(Locale.ROOT));
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof ResolvedValue that)) {
                return false;
            }

            String thatValue = that.asText();
            return that.hasValue() && Objects.equals(value, thatValue);
        }

        @Override
        public int hashCode() {
            return value != null ? value.hashCode() : 0;
        }

        @Override
        public String toString() {
            return "\"" + value + "\"";
        }

        private TextResolvedValue(String value) {
            this.value = value;
        }
    }

    private static class NumberResolvedValue extends ResolvedValue {
        private final int value;

        @Override
        public String asText() {
            return Integer.toString(value);
        }

        @Override
        public int asNumber() {
            return value;
        }

        @Override
        public double asDecimal() {
            return value;
        }

        @Override
        public boolean asBoolean() {
            return value != 0;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof ResolvedValue that)) {
                return false;
            }

            int thatValue = that.asNumber();
            return that.hasValue() && Objects.equals(value, thatValue);
        }

        @Override
        public int hashCode() {
            return Integer.hashCode(value);
        }

        @Override
        public String toString() {
            return Integer.toString(value, 10);
        }

        private NumberResolvedValue(int value) {
            this.value = value;
        }
    }

    private static class DecimalResolvedValue extends ResolvedValue {
        private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.#");

        private final double value;

        @Override
        public String asText() {
            return DECIMAL_FORMAT.format(value);
        }

        @Override
        public int asNumber() {
            return (int) value;
        }

        @Override
        public double asDecimal() {
            return value;
        }

        @Override
        public boolean asBoolean() {
            return value != 0;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof ResolvedValue that)) {
                return false;
            }

            double thatValue = that.asDecimal();
            return that.hasValue() && Objects.equals(value, thatValue);
        }

        @Override
        public int hashCode() {
            return Double.hashCode(value);
        }

        @Override
        public String toString() {
            return asText();
        }

        private DecimalResolvedValue(double value) {
            this.value = value;
        }
    }

    private static class BooleanTrueResolvedValue extends ResolvedValue {
        public static final BooleanTrueResolvedValue INSTANCE = new BooleanTrueResolvedValue();

        @Override
        public String asText() {
            return "true";
        }

        @Override
        public int asNumber() {
            return 1;
        }

        @Override
        public double asDecimal() {
            return 1.0;
        }

        @Override
        public boolean asBoolean() {
            return true;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof ResolvedValue that)) {
                return false;
            }

            return this.hasValue() && that.asBoolean();
        }

        @Override
        public int hashCode() {
            return Boolean.hashCode(true);
        }

        @Override
        public String toString() {
            return "true";
        }

        private BooleanTrueResolvedValue() {
        }
    }

    private static class BooleanFalseResolvedValue extends ResolvedValue {
        public static final BooleanFalseResolvedValue INSTANCE = new BooleanFalseResolvedValue();

        @Override
        public String asText() {
            return "false";
        }

        @Override
        public int asNumber() {
            return 0;
        }

        @Override
        public double asDecimal() {
            return 0.0;
        }

        @Override
        public boolean asBoolean() {
            return false;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof ResolvedValue that)) {
                return false;
            }

            return this.hasValue() && !that.asBoolean();
        }

        @Override
        public int hashCode() {
            return Boolean.hashCode(false);
        }

        @Override
        public String toString() {
            return "false";
        }

        private BooleanFalseResolvedValue() {
        }
    }

    private static class NullResolvedValue extends ResolvedValue {
        public static final NullResolvedValue INSTANCE = new NullResolvedValue();

        @Override
        public String asText() {
            return "";
        }

        @Override
        public int asNumber() {
            return 0;
        }

        @Override
        public double asDecimal() {
            return 0;
        }

        @Override
        public boolean asBoolean() {
            return false;
        }

        @Override
        public boolean hasValue() {
            return false;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            return o != null && getClass() == o.getClass();
        }

        @Override
        public int hashCode() {
            return 0;
        }

        @Override
        public String toString() {
            return "null";
        }

        private NullResolvedValue() {
        }
    }
}
