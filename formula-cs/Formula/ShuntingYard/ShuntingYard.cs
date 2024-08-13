namespace Formula.ShuntingYard;

public class ShuntingYard : IResolvable
{
    private readonly Stack<INode> _stack;
    
    public ResolvedValue Resolve(IDataContext context) {
        var localStack = new Stack<object>();
        var stack = new Stack<object>(_stack);
        while (stack.TryPop(out var next))
        {
            switch (next)
            {
                case IOperatorFunction0 func0:
                    localStack.Push(func0.Execute());
                    break;
                case IOperatorFunction1 func1:
                {
                    var a = (ResolvedValue) CheckedPopParameter(next, 1, localStack);
                    localStack.Push(func1.Execute(a));
                    break;
                }
                case IOperatorFunction2 func2:
                {
                    var b = (ResolvedValue) CheckedPopParameter(next, 1, localStack);
                    var a = (ResolvedValue) CheckedPopParameter(next, 2, localStack);
                    localStack.Push(func2.Execute(a, b));
                    break;
                }
                case IOperatorFunction3 func3:
                {
                    var c = (ResolvedValue) CheckedPopParameter(next, 1, localStack);
                    var b = (ResolvedValue) CheckedPopParameter(next, 2, localStack);
                    var a = (ResolvedValue) CheckedPopParameter(next, 3, localStack);
                    localStack.Push(func3.Execute(a, b, c));
                    break;
                }
                case IOperatorFunctionN func when localStack.Count == 0:
                    throw new ResolveException("Missing arity count for \"" + func + "\"");
                case IOperatorFunctionN func:
                {
                    var arity = ((Arity) localStack.Pop()).Value;
                    var parameters = new List<ResolvedValue>();
                    while (arity-- > 0) {
                        parameters.Add((ResolvedValue) CheckedPopParameter(next, arity, localStack));
                    }
                    localStack.Push(func.Execute(parameters));
                    break;
                }
                case Variable variable:
                    stack.Push(variable.Get(context));
                    break;
                case Comment comment:
                    localStack.Push(comment.Function((ResolvedValue) localStack.Pop(), comment.Text));
                    break;
                case ShuntingYard shuntingYard:
                    foreach (var otherNext in shuntingYard._stack)
                    {
                        stack.Push(otherNext);
                    }
                    break;
                default:
                {
                    var resolved = next;
                    while (resolved is IResolvable resolvable)
                    {
                        resolved = resolvable.Resolve(context);
                    }
                    localStack.Push(resolved);
                    break;
                }
            }
        }

        return (ResolvedValue) localStack.Pop();
    }

    private static object CheckedPopParameter(object func, int parameterIndex, Stack<object> stack) {
        if (stack.Count == 0) {
            throw new ResolveException("Missing parameter #" + parameterIndex + " for \"" + func + "\"");
        }
        return stack.Pop();
    }

    public ShuntingYard(Stack<INode> stack) {
        _stack = stack;
    }
}

public interface INode {}

public interface INamed
{
    public string Name { get; }
}

public interface IOperatorFunction0
{
    ResolvedValue Execute();
}

public interface IOperatorFunction1
{
    ResolvedValue Execute(ResolvedValue a1);
}

public interface IOperatorFunction2
{
    ResolvedValue Execute(ResolvedValue a1, ResolvedValue a2);
}

public interface IOperatorFunction3
{
    ResolvedValue Execute(ResolvedValue a1, ResolvedValue a2, ResolvedValue a3);
}

public interface IOperatorFunctionN
{
    ResolvedValue Execute(IEnumerable<ResolvedValue> values);
}

public enum Associativity
{
    Left,
    Right
}

public interface IFunction : INode, INamed {}

public interface IOperator : INode, INamed
{
    int Precedence { get; }
    Associativity Associativity { get; }
}

public record struct Arity(int Value) : INode;
public record struct Comment(string Text, Func<ResolvedValue, string, ResolvedValue> Function) : INode;

public readonly record struct Function0(string Name, Func<ResolvedValue> Function) : IFunction, IOperatorFunction0
{
    public ResolvedValue Execute()
    {
        return Function();
    }
    
    public override string ToString()
    {
        return Name;
    }
}

public readonly record struct Function1(string Name, Func<ResolvedValue, ResolvedValue> Function) : IFunction, IOperatorFunction1
{
    public ResolvedValue Execute(ResolvedValue a1)
    {
        return Function(a1);
    }
    
    public override string ToString()
    {
        return Name;
    }
}

public readonly record struct Function2(string Name, Func<ResolvedValue, ResolvedValue, ResolvedValue> Function) : IFunction, IOperatorFunction2
{
    public ResolvedValue Execute(ResolvedValue a1, ResolvedValue a2)
    {
        return Function(a1, a2);
    }
    
    public override string ToString()
    {
        return Name;
    }
}

public readonly record struct Function3(string Name, Func<ResolvedValue, ResolvedValue, ResolvedValue, ResolvedValue> Function) : IFunction, IOperatorFunction3
{
    public ResolvedValue Execute(ResolvedValue a1, ResolvedValue a2, ResolvedValue a3)
    {
        return Function(a1, a2, a3);
    }
    
    public override string ToString()
    {
        return Name;
    }
}

public readonly record struct FunctionN(string Name, Func<IEnumerable<ResolvedValue>, ResolvedValue> Function) : IFunction, IOperatorFunctionN
{
    public ResolvedValue Execute(IEnumerable<ResolvedValue> values)
    {
        return Function(values);
    }
    
    public override string ToString()
    {
        return Name;
    }
}

public readonly record struct Operator0
    (string Name, int Precedence, Associativity Associativity, Func<ResolvedValue> Function) : IOperator,
        IOperatorFunction0
{
    public ResolvedValue Execute()
    {
        return Function();
    }
    
    public override string ToString()
    {
        return Name;
    }
}

public readonly record struct Operator1
    (string Name, int Precedence, Associativity Associativity, Func<ResolvedValue, ResolvedValue> Function) : IOperator,
        IOperatorFunction1
{
    public ResolvedValue Execute(ResolvedValue a1)
    {
        return Function(a1);
    }
    
    public override string ToString()
    {
        return Name;
    }
}

public readonly record struct Operator2
    (string Name, int Precedence, Associativity Associativity, Func<ResolvedValue, ResolvedValue, ResolvedValue> Function) : IOperator,
        IOperatorFunction2
{
    public ResolvedValue Execute(ResolvedValue a1, ResolvedValue a2)
    {
        try
        {
            return Function(a1, a2);
        }
        catch (FormatException)
        {
            return ResolvedValue.NaN;
        }
    }

    public override string ToString()
    {
        return Name;
    }
}

public readonly record struct BiOperator
    (string Name, Operator1 UnaryOperator, Operator2 BinaryOperator) : INode;

internal class ResolveException : Exception
{
    public ResolveException(string message) : base(message)
    {
        
    }
}