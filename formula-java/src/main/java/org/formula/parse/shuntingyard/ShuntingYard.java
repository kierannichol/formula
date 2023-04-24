package org.formula.parse.shuntingyard;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import org.formula.Resolvable;
import org.formula.ResolveException;
import org.formula.ResolvedValue;
import org.formula.context.DataContext;

public class ShuntingYard implements Resolvable {
    private final List<Object> stack;

    @Override
    public ResolvedValue resolve(DataContext context) {
        Deque<Object> localStack = new ArrayDeque<>();
        Deque<Object> stack = new ArrayDeque<>(this.stack);
        while (!stack.isEmpty()) {
            Object next = stack.removeFirst();
            if (next instanceof OperatorFunction0 func) {
                localStack.addFirst(func.execute());
            } else if (next instanceof OperatorFunction1 func) {
                ResolvedValue a = (ResolvedValue) checkedPopParameter(next, 1, localStack);
                localStack.addFirst(func.execute(a));
            } else if (next instanceof OperatorFunction2 func) {
                ResolvedValue b = (ResolvedValue) checkedPopParameter(next, 1, localStack);
                ResolvedValue a = (ResolvedValue) checkedPopParameter(next, 2, localStack);
                localStack.addFirst(func.execute(a, b));
            } else if (next instanceof OperatorFunction3 func) {
                ResolvedValue c = (ResolvedValue) checkedPopParameter(next, 1, localStack);
                ResolvedValue b = (ResolvedValue) checkedPopParameter(next, 2, localStack);
                ResolvedValue a = (ResolvedValue) checkedPopParameter(next, 3, localStack);
                localStack.addFirst(func.execute(a, b, c));
            } else if (next instanceof OperatorFunctionN func) {
                if (localStack.isEmpty()) {
                    throw new ResolveException("Missing arity count for \"" + func + "\"");
                }
                int arity = ((Arity) localStack.removeFirst()).arity();
                List<ResolvedValue> params = new ArrayList<>();
                while (arity-- > 0) {
                    params.add((ResolvedValue) checkedPopParameter(next, arity, localStack));
                }
                localStack.addFirst(func.execute(params));
            } else if (next instanceof Variable variable) {
                stack.addFirst(variable.get(context));
            } else if (next instanceof Comment comment) {
                localStack.addFirst(comment.fn().execute((ResolvedValue) localStack.removeFirst(), comment.text()));
            } else if (next instanceof ShuntingYard shuntingYard) {
                for (int i = shuntingYard.stack.size() - 1; i >= 0; i--) {
                    stack.addFirst(shuntingYard.stack.get(i));
                }
            } else {
                while (next instanceof Resolvable resolvable) {
                    next = resolvable.resolve(context);
                }
                localStack.addFirst(next);
            }
        }

        return (ResolvedValue) localStack.removeFirst();
    }

    private Object checkedPopParameter(Object func, int parameterIndex, Deque<Object> stack) {
        if (stack.isEmpty()) {
            throw new ResolveException("Missing parameter #" + parameterIndex + " for \"" + func + "\"");
        }
        return stack.removeFirst();
    }

    public ShuntingYard(List<Object> stack) {
        this.stack = stack;
    }
}
