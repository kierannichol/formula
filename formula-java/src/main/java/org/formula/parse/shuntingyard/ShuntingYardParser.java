package org.formula.parse.shuntingyard;

import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import java.util.function.Supplier;
import org.formula.Resolvable;
import org.formula.ResolvedValue;
import org.formula.parse.Parser;
import org.formula.parse.tree.NodeExpression;
import org.formula.parse.tree.TokenTree;
import org.formula.util.Lambda2;

public class ShuntingYardParser implements Parser {

    private final TokenTree<Node> tokenTree;

    public static ShuntingYardParser create() {
        return new ShuntingYardParser();
    }

    public ShuntingYardParser() {
        tokenTree = TokenTree.<Node>create()
                .ignoreWhitespaces()
                .add(NodeExpression.INTEGER, token -> Term.of(Integer.parseInt(token)))
                .add(NodeExpression.DECIMAL, token -> Term.of(Double.parseDouble(token)))
                .add("(", Literal::of)
                .add(")", Literal::of)
                .add(",", Literal::of)
                .add(NodeExpression.literal("\"", "\"", "\\\""),
                        quote -> Term.of(quote.substring(1, quote.length() - 1), "\"", "\""))
                .add(NodeExpression.literal("'", "'", "\\'"),
                        quote -> Term.of(quote.substring(1, quote.length() - 1), "'", "'"));
    }

    public ShuntingYardParser operator(String symbol, int precedence, Associativity associativity,
            OperatorFunction0 fn) {
        tokenTree.add(symbol, token -> new Operator0(symbol, precedence, associativity, fn));
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

    public ShuntingYardParser variable(String idenfifier, VariableResolver variableResolver) {
        NodeExpression variableExpression = NodeExpression.of(
                NodeExpression.term(idenfifier),
                NodeExpression.ALPHA,
                NodeExpression.optional(NodeExpression.KEY)
        );
        tokenTree.add(variableExpression,
                key -> new Variable(key, (context, k) -> variableResolver.resolve(context, k.substring(idenfifier.length()))));
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

        for (Node token : tokens) {
            if (token instanceof Operator operator) {
                if (operatorStack.size() > 0) {
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
                    case ",":
                        arityStack.push(arityStack.pop() + 1);
                        while (operatorStack.size() > 0) {
                            Node next = operatorStack.pop();
                            if (next.equals(Literal.of("("))) {
                                operatorStack.push(next);
                                break;
                            }
                            outputBuffer.push(next);
                        }
                        break;
                    case "(":
                        operatorStack.push(token);
                        break;
                    case ")":
                        while (operatorStack.size() > 0) {
                            var next = operatorStack.pop();
                            if (next.equals(Literal.of("("))) {
                                break;
                            }
                            outputBuffer.push(next);
                        }

                        if (operatorStack.size() > 0 && operatorStack.peek() instanceof Function) {
                            if (operatorStack.peek() instanceof FunctionN) {
                                outputBuffer.push(new Arity(arityStack.pop()));
                            }
                            outputBuffer.push(operatorStack.pop());
                        }
                        break;
                    default:
                        outputBuffer.push(token);
                }
            }
        }

        while (operatorStack.size() > 0) {
            outputBuffer.push(operatorStack.pop());
        }
        return new ShuntingYard(Arrays.asList(outputBuffer.toArray()));
    }
}
