package org.formula;

public class QuotedTextResolvedValue extends ResolvedValue {
    private final ResolvedValue text;
    private final String startQuote;
    private final String endQuote;

    public static QuotedTextResolvedValue of(String text, String startQuote, String endQuote) {
        return of(ResolvedValue.of(text), startQuote, endQuote);
    }

    public static QuotedTextResolvedValue of(ResolvedValue value, String startQuote, String endQuote) {
        return new QuotedTextResolvedValue(value, startQuote, endQuote);
    }

    private QuotedTextResolvedValue(ResolvedValue text, String startQuote, String endQuote) {
        this.text = text;
        this.startQuote = startQuote != null ? startQuote : "";
        this.endQuote = endQuote != null ? endQuote : "";
    }

    @Override
    public String asText() {
        return text.asText();
    }

    public String asQuotedText() {
        return startQuote + text.asText() + endQuote;
    }

    @Override
    public int asNumber() {
        return text.asNumber();
    }

    @Override
    public double asDecimal() {
        return text.asDecimal();
    }

    @Override
    public boolean asBoolean() {
        return text.asBoolean();
    }

    @Override
    public String toString() {
        return asQuotedText();
    }
}
