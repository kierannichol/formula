using Formula.TokenTree;

namespace Formula.ShuntingYard;

public class ShuntingYardParser
{
    private const string ParametersStartSymbol = "(";
    private const string ParametersEndSymbol = ")";
    private const string ParametersSeparatorSymbol = ";";
    private static readonly Literal ParametersStartToken = new Literal(ParametersStartSymbol);
    private static readonly Literal ParametersEndToken = new Literal(ParametersEndSymbol);
    private static readonly Literal ParametersSeparatorToken = new Literal(ParametersSeparatorSymbol);
    
    private readonly TokenTree<INode> _tokenTree;

    public static ShuntingYardParser Create()
    {
        return new ShuntingYardParser();
    }

    private ShuntingYardParser()
    {
        _tokenTree = TokenTree<INode>.Create()
            .IgnoreWhitespaces()
            .Add(NodeExpression.Integer, token => new Term(ResolvedValue.Of(int.Parse(token))))
            .Add(NodeExpression.Decimal, token => new Term(ResolvedValue.Of(double.Parse(token))))
            .Add(ParametersStartSymbol, _ => ParametersStartToken)
            .Add(ParametersEndSymbol, _ => ParametersEndToken)
            .Add(ParametersSeparatorSymbol, _ => ParametersSeparatorToken)
            .Add(NodeExpression.Literal("\"", "\"", "\\\""),
                quote => new Term(ResolvedValue.Of(quote.Substring(1, quote.Length - 2)), "\"", "\""))
            .Add(NodeExpression.Literal("'", "'", "\\'"),
                quote => new Term(ResolvedValue.Of(quote.Substring(1, quote.Length - 2)), "'", "'"));
    }

    public ShuntingYardParser Operator(string symbol, int precedence, Associativity associativity,
        Func<ResolvedValue> fn)
    {
        _tokenTree.Add(symbol, _ => new Operator0(symbol, precedence, associativity, fn));
        return this;
    }

    public ShuntingYardParser Operator(string symbol, int precedence, Associativity associativity,
        Func<ResolvedValue, ResolvedValue> fn)
    {
        _tokenTree.Add(symbol, _ => new Operator1(symbol, precedence, associativity, fn));
        return this;
    }

    public ShuntingYardParser Operator(string symbol, int precedence, Associativity associativity,
        Func<ResolvedValue, ResolvedValue, ResolvedValue> fn)
    {
        _tokenTree.Add(symbol, _ => new Operator2(symbol, precedence, associativity, fn));
        return this;
    }

    public ShuntingYardParser BiOperator(string symbol, Operator1 unaryOperator, Operator2 binaryOperator)
    {
        _tokenTree.Add(symbol, _ => new BiOperator(symbol, unaryOperator, binaryOperator));
        return this;
    }

    public ShuntingYardParser Function(string name, Func<ResolvedValue> fn)
    {
        _tokenTree.Add(name, _ => new Function0(name, fn));
        return this;
    }

    public ShuntingYardParser Function(string name, Func<ResolvedValue, ResolvedValue> fn)
    {
        _tokenTree.Add(name, _ => new Function1(name, fn));
        return this;
    }

    public ShuntingYardParser Function(string name, Func<ResolvedValue, ResolvedValue, ResolvedValue> fn)
    {
        _tokenTree.Add(name, _ => new Function2(name, fn));
        return this;
    }

    public ShuntingYardParser Function(string name, Func<ResolvedValue, ResolvedValue, ResolvedValue, ResolvedValue> fn)
    {
        _tokenTree.Add(name, _ => new Function3(name, fn));
        return this;
    }

    public ShuntingYardParser Function(string name, Func<IEnumerable<ResolvedValue>, ResolvedValue> fn)
    {
        _tokenTree.Add(name, _ => new FunctionN(name, fn));
        return this;
    }

    public ShuntingYardParser Variable(string identifier, VariableResolver variableResolver)
    {
        var variableExpression = NodeExpression.Of(
            NodeExpression.Term(identifier),
            NodeExpression.Alpha,
            NodeExpression.Optional(NodeExpression.Key)
        );
        _tokenTree.Add(variableExpression,
            key => new Variable(key, (context, k) => variableResolver(context, k[identifier.Length..])));
        return this;
    }

    public ShuntingYardParser Variable(string prefix, string suffix, VariableResolver variableResolver)
    {
        var variableExpression = NodeExpression.Of(
            NodeExpression.Term(prefix),
            NodeExpression.Alpha,
            NodeExpression.Optional(NodeExpression.Key),
            NodeExpression.Term(suffix)
        );
        _tokenTree.Add(variableExpression,
            key => new Variable(key, (context, k) => variableResolver(context,
                k.Substring(prefix.Length, k.Length - prefix.Length - suffix.Length))));
        return this;
    }

    public ShuntingYardParser Comment(string prefix, string suffix, Func<ResolvedValue, string, ResolvedValue> fn)
    {
        var commentExpression = NodeExpression.Literal(prefix, suffix);
        _tokenTree.Add(commentExpression, token => new Comment(token, fn));
        return this;
    }

    public ShuntingYardParser Term(string text, Func<ResolvedValue> extractor)
    {
        var termExpression = NodeExpression.Term(text);
        _tokenTree.Add(termExpression, _ => new Term(extractor.Invoke()));
        return this;
    }

    public IResolvable Parse(string text)
    {
        var operatorStack = new Stack<INode>();
        var outputBuffer = new Stack<INode>();
        var arityStack = new Stack<int>();

        var tokens = _tokenTree.Parse(text);
        
        for (var i = 0; i < tokens.Count; i++)
        {
            var token = tokens[i];
            var previous = i > 0 ? tokens[i - 1] : null;

            if (token is BiOperator biOp)
            {
                token = biOp.BinaryOperator;
                if (previous is null or IOperator || previous.Equals(ParametersStartToken) || previous.Equals(ParametersSeparatorToken))
                {
                    token = biOp.UnaryOperator;
                }
            }
            
            if (token is IOperator op)
            {
                if (operatorStack.Count > 0)
                {
                    var top = operatorStack.Peek();
                    if (top is IOperator topOperator)
                    {
                        if (op.Precedence < topOperator.Precedence
                            || (op.Associativity == Associativity.Left
                                && op.Precedence == topOperator.Precedence))
                        {
                            operatorStack.Pop();
                            outputBuffer.Push(top);
                        }
                    }
                }

                operatorStack.Push(op);
                continue;
            }

            if (token is IFunction)
            {
                operatorStack.Push(token);
                arityStack.Push(1);
                continue;
            }

            if (token is Variable)
            {
                outputBuffer.Push(token);
                continue;
            }

            if (token is Term)
            {
                outputBuffer.Push(token);
                continue;
            }

            if (token is Comment)
            {
                outputBuffer.Push(token);
                continue;
            }

            if (token is Literal literal)
            {
                switch (literal.Value)
                {
                    case " ":
                    case "{":
                    case "}":
                        // ignore
                        break;
                    case ParametersSeparatorSymbol:
                        arityStack.Push(arityStack.Pop() + 1);
                        while (operatorStack.Count > 0)
                        {
                            var next = operatorStack.Pop();
                            if (next.Equals(ParametersStartToken))
                            {
                                operatorStack.Push(next);
                                break;
                            }

                            outputBuffer.Push(next);
                        }

                        break;
                    case ParametersStartSymbol:
                        operatorStack.Push(token);
                        break;
                    case ParametersEndSymbol:
                        while (operatorStack.Count > 0)
                        {
                            var next = operatorStack.Pop();
                            if (next.Equals(ParametersStartToken))
                            {
                                break;
                            }

                            outputBuffer.Push(next);
                        }

                        if (operatorStack.Count > 0 && operatorStack.Peek() is IFunction)
                        {
                            if (operatorStack.Peek() is FunctionN)
                            {
                                var arity = arityStack.Pop();
                                arity = previous != null && previous.Equals(ParametersStartToken) ? 0 : arity;
                                outputBuffer.Push(new Arity(arity));
                            }

                            outputBuffer.Push(operatorStack.Pop());
                        }

                        break;
                    default:
                        outputBuffer.Push(token);
                        break;
                }
            }
        }

        while (operatorStack.Count > 0)
        {
            outputBuffer.Push(operatorStack.Pop());
        }

        return new ShuntingYard(outputBuffer);
    }
}