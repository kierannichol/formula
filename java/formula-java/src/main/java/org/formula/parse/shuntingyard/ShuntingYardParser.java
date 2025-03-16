package org.formula.parse.shuntingyard;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Stack;
import java.util.function.Supplier;
import org.formula.Resolvable;
import org.formula.ResolvedValue;
import org.formula.parse.Parser;
import org.formula.parse.tree.NodeExpression;
import org.formula.parse.tree.TokenTree;
import org.formula.util.Lambda2;

public class ShuntingYardParser implements Parser {
    private static final String PARAMETERS_START_SYMBOL = "(";
    private static final String PARAMETERS_END_SYMBOL = ")";
    private static final String PARAMETERS_SEPARATOR_SYMBOL = ";";
    private static final Literal PARAMETERS_START_TOKEN = Literal.of(PARAMETERS_START_SYMBOL);
    private static final Literal PARAMETERS_END_TOKEN = Literal.of(PARAMETERS_END_SYMBOL);
    private static final Literal PARAMETERS_SEPARATOR_TOKEN = Literal.of(PARAMETERS_SEPARATOR_SYMBOL);

    private final TokenTree<Node> tokenTree;

    public static ShuntingYardParser create() {
        return new ShuntingYardParser();
    }

    public ShuntingYardParser() {
        tokenTree = TokenTree.<Node>create()
                .ignoreWhitespaces()
                .add(NodeExpression.INTEGER, token -> Term.of(Integer.parseInt(token)))
                .add(NodeExpression.DECIMAL, token -> Term.of(Double.parseDouble(token)))
                .add(PARAMETERS_START_SYMBOL, ignored -> PARAMETERS_START_TOKEN)
                .add(PARAMETERS_END_SYMBOL, ignored -> PARAMETERS_END_TOKEN)
                .add(PARAMETERS_SEPARATOR_SYMBOL, ignored -> PARAMETERS_SEPARATOR_TOKEN)
                .add(NodeExpression.literal("\"", "\"", "\\\""),
                        quote -> Term.of(quote.substring(1, quote.length() - 1), "\"", "\""))
                .add(NodeExpression.literal("'", "'", "\\'"),
                        quote -> Term.of(quote.substring(1, quote.length() - 1), "'", "'"));
    }

    public ShuntingYardParser biOperator(String symbol, Operator1 unaryOperator, Operator2 binaryOperator) {
        tokenTree.add(symbol, token -> new BiOperatorFunction(symbol, unaryOperator, binaryOperator));
        return this;
    }

    public ShuntingYardParser operator(String symbol, int precedence, Associativity associativity,
            OperatorFunction1 fn) {
        tokenTree.add(symbol, token -> new Operator1(symbol, precedence, associativity, fn));
        return this;
    }

    public ShuntingYardParser operator(String symbol, int precedence, Associativity associativity,
            OperatorFunction2 fn) {
        tokenTree.add(symbol, token -> new Operator2(symbol, precedence, associativity, fn));
        return this;
    }

    public ShuntingYardParser function(String name, OperatorFunction0 fn) {
        tokenTree.add(name, token -> new Function0(name, fn));
        return this;
    }

    public ShuntingYardParser function(String name, OperatorFunction1 fn) {
        tokenTree.add(name, token -> new Function1(name, fn));
        return this;
    }

    public ShuntingYardParser function(String name, OperatorFunction2 fn) {
        tokenTree.add(name, token -> new Function2(name, fn));
        return this;
    }

    public ShuntingYardParser function(String name, OperatorFunction3 fn) {
        tokenTree.add(name, token -> new Function3(name, fn));
        return this;
    }

    public ShuntingYardParser function(String name, OperatorFunctionN fn) {
        tokenTree.add(name, token -> new FunctionN(name, fn));
        return this;
    }

    public ShuntingYardParser variable(String identifier, VariableResolver variableResolver) {
        NodeExpression variableExpression = NodeExpression.of(
                NodeExpression.term(identifier),
                NodeExpression.ALPHA,
                NodeExpression.optional(NodeExpression.KEY)
        );
        tokenTree.add(variableExpression,
                key -> new Variable(key, (context, k) -> variableResolver.resolve(context, k.substring(identifier.length()))));
        return this;
    }

    public ShuntingYardParser variable(String prefix, String suffix, VariableResolver variableResolver) {
        NodeExpression variableExpression = NodeExpression.of(
                NodeExpression.term(prefix),
                NodeExpression.ALPHA,
                NodeExpression.optional(NodeExpression.KEY),
                NodeExpression.term(suffix)
        );
        tokenTree.add(variableExpression,
                key -> new Variable(key, (context, k) -> variableResolver.resolve(context,
                        k.substring(prefix.length(), k.length() - suffix.length()))));
        return this;
    }

    public ShuntingYardParser comment(String prefix, String suffix, Lambda2<ResolvedValue, String, ResolvedValue> fn) {
        NodeExpression commentExpression = NodeExpression.literal(prefix, suffix);
        tokenTree.add(commentExpression, token -> Comment.of(token, fn));
        return this;
    }

    public ShuntingYardParser term(String text, Supplier<ResolvedValue> extractor) {
        NodeExpression termExpression = NodeExpression.term(text);
        tokenTree.add(termExpression, key -> Term.of(extractor.get()));
        return this;
    }

    @Override
    public Resolvable parse(String text) {
        Stack<Node> operatorStack = new Stack<>();
        Stack<Node> outputBuffer = new Stack<>();
        Stack<Integer> arityStack = new Stack<>();

        List<Node> tokens = tokenTree.parse(text);

        for (int i = 0; i < tokens.size(); i++) {
            Node token = tokens.get(i);
            Node previous = i > 0 ? tokens.get(i-1) : null;

            if (token instanceof BiOperatorFunction func) {
                token = func.binaryOperator();
                if (previous == null || previous instanceof Operator
                        || Objects.equals(previous, PARAMETERS_START_TOKEN)
                        || Objects.equals(previous, PARAMETERS_SEPARATOR_TOKEN)) {
                    token = func.unaryOperator();
                }
            }

            if (token instanceof Operator operator) {
                if (!operatorStack.isEmpty()) {
                    Node top = operatorStack.peek();
                    if (top instanceof Operator topOperator) {
                        if ((operator.precedence() < topOperator.precedence())
                                || (operator.associativity() == Associativity.LEFT
                                && operator.precedence() == topOperator.precedence())) {
                            operatorStack.pop();
                            outputBuffer.push(top);
                        }
                    }
                }

                operatorStack.push(operator);
                continue;
            }

            if (token instanceof Function) {
                operatorStack.push(token);
                arityStack.push(1);
                continue;
            }

            if (token instanceof Variable) {
                outputBuffer.push(token);
                continue;
            }

            if (token instanceof Term) {
                outputBuffer.push(token);
                continue;
            }

            if (token instanceof Comment) {
                outputBuffer.push(token);
                continue;
            }

            if (token instanceof Literal literal) {
                switch (literal.value()) {
                    case " ":
                    case "{":
                    case "}":
                        // ignore
                        break;
                    case PARAMETERS_SEPARATOR_SYMBOL:
                        arityStack.push(arityStack.pop() + 1);
                        while (!operatorStack.isEmpty()) {
                            Node next = operatorStack.pop();
                            if (next.equals(PARAMETERS_START_TOKEN)) {
                                operatorStack.push(next);
                                break;
                            }
                            outputBuffer.push(next);
                        }
                        break;
                    case PARAMETERS_START_SYMBOL:
                        operatorStack.push(token);
                        break;
                    case PARAMETERS_END_SYMBOL:
                        while (!operatorStack.isEmpty()) {
                            var next = operatorStack.pop();
                            if (next.equals(PARAMETERS_START_TOKEN)) {
                                break;
                            }
                            outputBuffer.push(next);
                        }

                        if (!operatorStack.isEmpty() && operatorStack.peek() instanceof Function) {
                            if (operatorStack.peek() instanceof FunctionN) {
                                int arity = arityStack.pop();
                                arity = previous.equals(PARAMETERS_START_TOKEN) ? 0 : arity;
                                outputBuffer.push(new Arity(arity));
                            }
                            outputBuffer.push(operatorStack.pop());
                        }
                        break;
                    default:
                        outputBuffer.push(token);
                }
            }
        }

        while (!operatorStack.isEmpty()) {
            outputBuffer.push(operatorStack.pop());
        }
        return new ShuntingYard(Arrays.asList(outputBuffer.toArray()), text);
    }
}
