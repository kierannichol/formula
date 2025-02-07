from typing import Callable

from formula import resolved_value
from formula.resolved_value import NamedResolvedValue, QuotedTextResolvedValue, ResolvedValue
from formula.shunting_yard import ShuntingYardParser, Associativity


def optimize_formula(formula):
    optimized = __parser.parse(formula).resolve()
    if isinstance(optimized, _MathFunction):
        return optimized.as_text_no_brackets()
    return _format(optimized)


def _format(value: resolved_value) -> str:
    if isinstance(value, QuotedTextResolvedValue):
        return value.as_quoted_text()
    if isinstance(value, NamedResolvedValue):
        return f"{value.as_text()}[{value.as_name()}]"
    return value.as_text()


def _op_fn1(func: Callable[[str], str]) -> Callable[[list[resolved_value]], resolved_value]:
    return lambda p: resolved_value(func(_format(p[0])))


def _op_fn2(func: Callable[[str, str], str]) -> Callable[[list[resolved_value]], resolved_value]:
    return lambda p: resolved_value(func(_format(p[0]), _format(p[1])))


def _op_fn3(func: Callable[[str, str, str], str]) -> Callable[[list[resolved_value]], resolved_value]:
    return lambda p: resolved_value(func(_format(p[0]), _format(p[1]), _format(p[2])))


class _MathFunction(ResolvedValue):
    def __init__(self, operator: str, a: resolved_value, b: resolved_value):
        self._operator = operator
        self._a = a
        self._b = b

    def as_text_no_brackets(self):
        return f"{self._format(self._a)}{self._operator}{self._format(self._b)}"

    def as_text(self):
        return f"({self.as_text_no_brackets()})"

    def _format(self, value: resolved_value) -> str:
        if isinstance(value, _MathFunction):
            if (value._operator == '+' or value._operator == '-') and (self._operator == '+' or self._operator == '-'):
                return value.as_text_no_brackets()
            if (value._operator == '*' or value._operator == '/') and (self._operator == '*' or self._operator == '/'):
                return value.as_text_no_brackets()
        return _format(value)

    def __repr__(self):
        return f"{self._a}{self._operator}{self._b}"


class _AnyFunction(ResolvedValue):
    def __init__(self, values: list[resolved_value]):
        self._values = []
        has_false = False
        for value in values:
            if isinstance(value, _AnyFunction):
                self._values += value._values
                continue
            if value == resolved_value(False):
                has_false = True
                continue
            if value == resolved_value(True):
                self._values.clear()
                self._values.append(value)
                return
            self._values.append(value)
        if len(self._values) == 0:
            self._values.append(resolved_value(not has_false))

    def as_text(self) -> str:
        if len(self._values) == 1:
            return _format(self._values[0])
        return f"any({','.join([_format(x) for x in self._values])})"

    def __repr__(self):
        return self.as_text()


class _AllFunction(ResolvedValue):
    def __init__(self, values: list[resolved_value]):
        self._values = []
        for value in values:
            if isinstance(value, _AllFunction):
                self._values += value._values
                continue
            if value == resolved_value(True):
                continue
            if value == resolved_value(False):
                self._values.clear()
                self._values.append(value)
                return
            self._values.append(value)
        if len(self._values) == 0:
            self._values.append(resolved_value(True))

    def as_text(self) -> str:
        if len(self._values) == 1:
            return _format(self._values[0])
        return f"all({','.join([_format(x) for x in self._values])})"

    def __repr__(self):
        return self.as_text()


__parser = (ShuntingYardParser()
            .operator('^', 4, Associativity.RIGHT, 2, _op_fn2(lambda a, b: f"{a}^{b}"))
            .operator('*', 3, Associativity.LEFT, 2, lambda p: _MathFunction('*', p[0], p[1]))
            .operator('/', 3, Associativity.LEFT, 2, lambda p: _MathFunction('/', p[0], p[1]))
            .operator('+', 2, Associativity.LEFT, 2, lambda p: _MathFunction('+', p[0], p[1]))
            .operator('-', 2, Associativity.LEFT, 2, lambda p: _MathFunction('-', p[0], p[1]))
            .operator('!', 2, Associativity.LEFT, 1, _op_fn1(lambda a: f"!{a}"))
            .operator('<', 3, Associativity.LEFT, 2, _op_fn2(lambda a, b: f"{a}<{b}"))
            .operator('<=', 3, Associativity.LEFT, 2, _op_fn2(lambda a, b: f"{a}<={b}"))
            .operator('>', 3, Associativity.LEFT, 2, _op_fn2(lambda a, b: f"{a}>{b}"))
            .operator('>=', 3, Associativity.LEFT, 2, _op_fn2(lambda a, b: f"{a}>={b}"))
            .operator('==', 3, Associativity.LEFT, 2, _op_fn2(lambda a, b: f"{a}=={b}"))
            .operator('!=', 3, Associativity.LEFT, 2, _op_fn2(lambda a, b: f"{a}!={b}"))
            .operator('AND', 1, Associativity.LEFT, 2, lambda p: _AllFunction(p))
            .operator('OR', 1, Associativity.LEFT, 2, lambda p: _AnyFunction(p))
            .operator('d', 4, Associativity.LEFT, 2, _op_fn2(lambda a, b: f"{a}d{b}"))
            .term('true', lambda: resolved_value(True))
            .term('false', lambda: resolved_value(False))
            .term('null', lambda: resolved_value(None))
            .function('abs', 1, _op_fn1(lambda a: f"abs({a})"))
            .function('min', 2, _op_fn2(lambda a, b: f"min({a},{b})"))
            .function('max', 2, _op_fn2(lambda a, b: f"max({a},{b})"))
            .function('floor', 1, _op_fn1(lambda a: f"floor({a})"))
            .function('ceil', 1, _op_fn1(lambda a: f"ceil({a})"))
            .function('signed', 1, _op_fn1(lambda a: f"signed({a})"))
            .function('if', 3, _op_fn3(lambda a, b, c: f"if({a},{b},{c})"))
            .function('concat', 2, _op_fn2(lambda a, b: f"concat({a},{b})"))
            .function('ordinal', 1, _op_fn1(lambda a: f"ordinal({a})"))
            .function_n('any', lambda values: _AnyFunction(values))
            .function_n('all', lambda values: _AllFunction(values))
            .variable('@', '', lambda context, key: resolved_value(f"@{key}"))
            .variable('@{', '}', lambda context, key: resolved_value(f"@{{{key}}}"))
            .variable('min(@', ')', lambda context, key: resolved_value(f"min(@{key})"))
            .variable('max(@', ')', lambda context, key: resolved_value(f"max(@{key})"))
            .variable('sum(@', ')', lambda context, key: resolved_value(f"sum(@{key})"))
            .comment('[', ']', lambda text, value: NamedResolvedValue(value, text)))
