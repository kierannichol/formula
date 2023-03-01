package org.formula.parse.tree;

import static org.formula.parse.tree.CharacterClass.BLANK_CHARACTERS;

import java.util.ArrayList;
import java.util.List;

public class TokenTree<T> {
    private final RootNode<T> root = new RootNode<>();

    public static <T> TokenTree<T> create() {
        return new TokenTree<>();
    }

    public TokenTree<T> ignoreWhitespaces() {
        return add(NodeExpression.anyOf(BLANK_CHARACTERS), c -> null);
    }

    public TokenTree<T> add(CharSequence allowed, TokenMapper<T> mapper) {
        return addBranch(
            NodeExpression.term(allowed),
            mapper);
    }

    public TokenTree<T> add(NodeExpression expression, TokenMapper<T> mapper) {
        return addBranch(
                expression,
                mapper);
    }

    public TokenTree<T> add(List<NodeExpression> expressions, TokenMapper<T> mapper) {
        return addBranch(
                CompositeNodeExpression.of(expressions),
                mapper);
    }

    public List<T> parse(String text) {
        List<T> tokens = new ArrayList<>();
        for (int i = 0; i < text.length(); i++) {
            List<TokenMatch<T>> matches = root.walk(text, i).toList();
            if (!matches.isEmpty()) {
                var match = matches.get(0);
                var token = match.get();
                if (token != null) {
                    tokens.add(token);
                }
                i = match.endIndex() - 1;
            } else {
                throw new ParseException(
                        generateParseErrorMessage(i, text, "did not expect character: '" + text.charAt(i) + "'"),
                        text, i);
            }
        }
        return tokens;
    }

    private TokenTree<T> addBranch(NodeExpression expression, TokenMapper<T> mapper) {
        Node<T> node = expression.chainTo(root);

        MatcherNode<T> matcherNode = (MatcherNode<T>) node;
        if (matcherNode.mapper() != null) {
            throw new RuntimeException("Conflicting tokens");
        }
        matcherNode.mapper(mapper);
        return this;
    }

    private static String generateParseErrorMessage(int index, String text, String message) {
        return String.format("Parse error at index %d of \"%s\": %s", index, text, message) + "\n"
                + text
                + "\n"
                + " ".repeat(index)
                + "^";
    }
}
