package org.formula.test;

import org.formula.Formula;
import org.formula.Resolvable;
import org.formula.ResolvedValue;
import org.formula.context.MutableDataContext;

public abstract class DataContextAction {

    public abstract void execute(MutableDataContext context);

    public static DataContextAction parse(String text) {
        String[] parts = text.split(" ");
        if (parts.length == 3 && "SET".equals(parts[0])) {
            return new SetAction(parts[1], parseValue(parts[2]));
        }
        if (parts.length == 3 && "PUSH".equals(parts[0])) {
            return new PushAction(parts[1], ResolvedValue.of(parts[2]));
        }
        throw new IllegalArgumentException("Invalid action: " + text);
    }

    private static Resolvable parseValue(String text) {
        if (text.startsWith("{") && text.endsWith("}")) {
            return Formula.parse(text.substring(1, text.length() - 1));
        }
        return Resolvable.just(text);
    }

    private static class SetAction extends DataContextAction {
        private final String key;
        private final Resolvable value;

        public SetAction(String key, Resolvable value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public void execute(MutableDataContext context) {
            context.set(key, value);
        }
    }

    private static class PushAction extends DataContextAction {
        private final String key;
        private final ResolvedValue value;

        public PushAction(String key, ResolvedValue value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public void execute(MutableDataContext context) {
            context.push(key, value);
        }
    }
}
