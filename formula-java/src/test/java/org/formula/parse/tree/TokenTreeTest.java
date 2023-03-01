package org.formula.parse.tree;

import static org.formula.parse.tree.CharacterClass.ALPHA_CHARACTERS;
import static org.formula.parse.tree.CharacterClass.DIGIT_CHARACTERS;
import static org.formula.parse.tree.CharacterClass.WORD_CHARACTERS;
import static org.formula.parse.tree.NodeExpression.DECIMAL;
import static org.formula.parse.tree.NodeExpression.INTEGER;
import static org.formula.parse.tree.NodeExpression.NUMBER;
import static org.formula.parse.tree.NodeExpression.WORD;
import static org.formula.parse.tree.NodeExpression.anyOf;
import static org.formula.parse.tree.NodeExpression.literal;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.formula.ResolvedValue;
import org.junit.jupiter.api.Test;

class TokenTreeTest {

    @Test
    void emptyTree() {
        assertThrows(ParseException.class, () ->
                TokenTree.<ResolvedValue>create().parse("Text"));
    }

    @Test
    void singleNode() {
        var tree = TokenTree.<ResolvedValue>create()
                .add("A", ResolvedValue::of);
        assertThat(tree.parse("A")).contains(ResolvedValue.of("A"));
    }

    @Test
    void simpleChain() {
        var tree = TokenTree.<ResolvedValue>create()
                .add("ABC", ResolvedValue::of);
        assertThat(tree.parse("ABC")).contains(ResolvedValue.of("ABC"));
        assertThrows(ParseException.class, () -> TokenTree.<ResolvedValue>create().parse("A"));
        assertThrows(ParseException.class, () -> TokenTree.<ResolvedValue>create().parse("AB"));
        assertThrows(ParseException.class, () -> TokenTree.<ResolvedValue>create().parse("ABX"));
    }

    @Test
    void splittingChain() {
        var tree = TokenTree.<ResolvedValue>create()
                .add("ABC", ResolvedValue::of)
                .add("A23", ResolvedValue::of);
        assertThat(tree.parse("ABC")).contains(ResolvedValue.of("ABC"));
        assertThat(tree.parse("A23")).contains(ResolvedValue.of("A23"));
    }

    @Test
    void multipleTokens() {
        var tree = TokenTree.<ResolvedValue>create()
                .ignoreWhitespaces()
                .add("ABC", ResolvedValue::of)
                .add("123", ResolvedValue::of);
        assertThat(tree.parse("ABC 123"))
                .containsExactly(ResolvedValue.of("ABC"), ResolvedValue.of("123"));
    }

    @Test
    void anyOfToken() {
        var tree = TokenTree.<ResolvedValue>create()
                .ignoreWhitespaces()
                .add(anyOf(DIGIT_CHARACTERS), token -> ResolvedValue.of(Integer.parseInt(token)));
        assertThat(tree.parse("1 2 3"))
                .containsExactly(ResolvedValue.of(1), ResolvedValue.of(2), ResolvedValue.of(3));
    }

    @Test
    void anyOfChain() {
        var tree = TokenTree.<ResolvedValue>create()
                .ignoreWhitespaces()
                .add(List.of(anyOf(DIGIT_CHARACTERS), anyOf(DIGIT_CHARACTERS)), token -> ResolvedValue.of(Integer.parseInt(token)));
        assertThat(tree.parse("13 25 36"))
                .containsExactly(ResolvedValue.of(13), ResolvedValue.of(25), ResolvedValue.of(36));
    }

    @Test
    void repeated() {
        var tree = TokenTree.<ResolvedValue>create()
                .ignoreWhitespaces()
                .add(anyOf(DIGIT_CHARACTERS).repeats(1, 2), token -> ResolvedValue.of(Integer.parseInt(token)));
        assertThat(tree.parse("5")).containsExactly(ResolvedValue.of(5));
        assertThat(tree.parse("73")).containsExactly(ResolvedValue.of(73));
        assertThat(tree.parse("12 9 23")).containsExactly(ResolvedValue.of(12), ResolvedValue.of(9), ResolvedValue.of(23));
    }

    @Test
    void optionalTrailingCharacter() {
        var tree = TokenTree.<ResolvedValue>create()
                .ignoreWhitespaces()
                .add(List.of(anyOf(ALPHA_CHARACTERS).repeats(1), anyOf(DIGIT_CHARACTERS).optional()), ResolvedValue::of);
        assertThat(tree.parse("A5")).containsExactly(ResolvedValue.of("A5"));
        assertThat(tree.parse("ABC6")).containsExactly(ResolvedValue.of("ABC6"));
        assertThat(tree.parse("ABC")).containsExactly(ResolvedValue.of("ABC"));
        assertThrows(ParseException.class, () -> TokenTree.<ResolvedValue>create().parse("5"));
        assertThrows(ParseException.class, () -> TokenTree.<ResolvedValue>create().parse("ABC56"));
    }

    @Test
    void optionalLeadingCharacter() {
        var tree = TokenTree.<ResolvedValue>create()
                .ignoreWhitespaces()
                .add(List.of(anyOf("@").optional(), anyOf(WORD_CHARACTERS).repeats(1)), ResolvedValue::of);
        assertThat(tree.parse("@A5")).containsExactly(ResolvedValue.of("@A5"));
        assertThat(tree.parse("A5")).containsExactly(ResolvedValue.of("A5"));
        assertThrows(ParseException.class, () -> TokenTree.<ResolvedValue>create().parse("@"));
        assertThrows(ParseException.class, () -> TokenTree.<ResolvedValue>create().parse("@@A5"));
    }

    @Test
    void numberToken() {
        var tree = TokenTree.<ResolvedValue>create()
                .ignoreWhitespaces()
                .add(NUMBER, token -> ResolvedValue.of(Double.parseDouble(token)));
        assertThat(tree.parse("1")).containsExactly(ResolvedValue.of(1.0));
        assertThat(tree.parse("23")).containsExactly(ResolvedValue.of(23.0));
        assertThat(tree.parse("54890")).containsExactly(ResolvedValue.of(54890.0));
        assertThat(tree.parse("3.14")).containsExactly(ResolvedValue.of(3.14));
        assertThrows(ParseException.class, () -> TokenTree.<ResolvedValue>create().parse("A"));
        assertThrows(ParseException.class, () -> TokenTree.<ResolvedValue>create().parse("5B"));
        assertThrows(ParseException.class, () -> TokenTree.<ResolvedValue>create().parse("2."));
        assertThrows(ParseException.class, () -> TokenTree.<ResolvedValue>create().parse(".5"));
    }

    @Test
    void expressionOrder() {
        var tree = TokenTree.<ResolvedValue>create()
                .ignoreWhitespaces()
                .add(INTEGER, ResolvedValue::of)
                .add(DECIMAL, token -> ResolvedValue.of(Double.parseDouble(token)));
        assertThat(tree.parse("123 3.14 5 0.2")).containsExactly(
                ResolvedValue.of("123"),
                ResolvedValue.of(3.14),
                ResolvedValue.of("5"),
                ResolvedValue.of(0.2));
    }

    @Test
    void quotedToken() {
        var tree = TokenTree.<ResolvedValue>create()
                .ignoreWhitespaces()
                .add(literal("\"", "\""), ResolvedValue::of)
                .add(WORD, ResolvedValue::of);

        assertThat(tree.parse("one two \"three four\" five")).containsExactly(
                ResolvedValue.of("one"),
                ResolvedValue.of("two"),
                ResolvedValue.of("\"three four\""),
                ResolvedValue.of("five"));
    }

    @Test
    void openCloseTagToken() {
        var tree = TokenTree.<ResolvedValue>create()
                .ignoreWhitespaces()
                .add(literal("<open>", "<close>"), ResolvedValue::of)
                .add(WORD, ResolvedValue::of);

        assertThat(tree.parse("one two <open>three four<close> five"))
                .containsExactly(
                        ResolvedValue.of("one"),
                        ResolvedValue.of("two"),
                        ResolvedValue.of("<open>three four<close>"),
                        ResolvedValue.of("five"));
    }
}