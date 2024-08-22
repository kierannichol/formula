from typing import Callable

from formula import ResolvedValue
from formula.resolved_value import NamedResolvedValue, QuotedTextResolvedValue
from formula.shunting_yard import ShuntingYardParser, Associativity


def optimize_formula(formula):
    optimized = __parser.parse(formula).resolve()
    if isinstance(optimized, MathFunction):
        return optimized.as_text_no_brackets()
    return _format(optimized)


def _format(value: ResolvedValue) -> str:
    if isinstance(value, QuotedTextResolvedValue):
        return value.as_quoted_text()
    if isinstance(value, NamedResolvedValue):
        return f"{value.as_text()}[{value.as_name()}]"
    return value.as_text()


def _op_fn1(func: Callable[[str], str]) -> Callable[[list[ResolvedValue]], ResolvedValue]:
    return lambda p: ResolvedValue(func(_format(p[0])))


def _op_fn2(func: Callable[[str, str], str]) -> Callable[[list[ResolvedValue]], ResolvedValue]:
    return lambda p: ResolvedValue(func(_format(p[0]), _format(p[1])))


def _op_fn3(func: Callable[[str, str, str], str]) -> Callable[[list[ResolvedValue]], ResolvedValue]:
    return lambda p: ResolvedValue(func(_format(p[0]), _format(p[1]), _format(p[2])))


class MathFunction(ResolvedValue):
    def __init__(self, operator: str, a: ResolvedValue, b: ResolvedValue):
        super().__init__(None)
        self._operator = operator
        self._a = a
        self._b = b

    def as_text_no_brackets(self):
        return f"{self._format(self._a)}{self._operator}{self._format(self._b)}"

    def as_text(self):
        return f"({self.as_text_no_brackets()})"

    def _format(self, value: ResolvedValue) -> str:
        if isinstance(value, MathFunction):
            if (value._operator == '+' or value._operator == '-') and (self._operator == '+' or self._operator == '-'):
                return value.as_text_no_brackets()
            if (value._operator == '*' or value._operator == '/') and (self._operator == '*' or self._operator == '/'):
                return value.as_text_no_brackets()
        return _format(value)

    def __repr__(self):
        return f"{self._a}{self._operator}{self._b}"


class AnyFunction(ResolvedValue):
    def __init__(self, values: list[ResolvedValue]):
        super().__init__(None)
        self._values = []
        has_false = False
        for value in values:
            if isinstance(value, AnyFunction):
                self._values += value._values
                continue
            if value == ResolvedValue(False):
                has_false = True
                continue
            if value == ResolvedValue(True):
                self._values.clear()
                self._values.append(value)
                return
            self._values.append(value)
        if len(self._values) == 0:
            self._values.append(ResolvedValue(not has_false))

    def as_text(self) -> str:
        if len(self._values) == 1:
            return _format(self._values[0])
        return f"any({','.join([_format(x) for x in self._values])})"

    def __repr__(self):
        return self.as_text()


class AllFunction(ResolvedValue):
    def __init__(self, values: list[ResolvedValue]):
        super().__init__(None)
        self._values = []
        for value in values:
            if isinstance(value, AllFunction):
                self._values += value._values
                continue
            if value == ResolvedValue(True):
                continue
            if value == ResolvedValue(False):
                self._values.clear()
                self._values.append(value)
                return
            self._values.append(value)
        if len(self._values) == 0:
            self._values.append(ResolvedValue(True))

    def as_text(self) -> str:
        if len(self._values) == 1:
            return _format(self._values[0])
        return f"all({','.join([_format(x) for x in self._values])})"

    def __repr__(self):
        return self.as_text()


def _optimized_math_fn(operator: str) -> Callable[[list[ResolvedValue]], ResolvedValue]:
    return _op

__parser = (ShuntingYardParser()
            .operator('^', 4, Associativity.RIGHT, 2, _op_fn2(lambda a, b: f"{a}^{b}"))
            .operator('*', 3, Associativity.LEFT, 2, lambda p: MathFunction('*', p[0], p[1]))
            .operator('/', 3, Associativity.LEFT, 2, lambda p: MathFunction('/', p[0], p[1]))
            .operator('+', 2, Associativity.LEFT, 2, lambda p: MathFunction('+', p[0], p[1]))
            .operator('-', 2, Associativity.LEFT, 2, lambda p: MathFunction('-', p[0], p[1]))
            .operator('!', 2, Associativity.LEFT, 1, _op_fn1(lambda a: f"!{a}"))
            .operator('<', 3, Associativity.LEFT, 2, _op_fn2(lambda a, b: f"{a}<{b}"))
            .operator('<=', 3, Associativity.LEFT, 2, _op_fn2(lambda a, b: f"{a}<={b}"))
            .operator('>', 3, Associativity.LEFT, 2, _op_fn2(lambda a, b: f"{a}>{b}"))
            .operator('>=', 3, Associativity.LEFT, 2, _op_fn2(lambda a, b: f"{a}>={b}"))
            .operator('==', 3, Associativity.LEFT, 2, _op_fn2(lambda a, b: f"{a}=={b}"))
            .operator('!=', 3, Associativity.LEFT, 2, _op_fn2(lambda a, b: f"{a}!={b}"))
            .operator('AND', 1, Associativity.LEFT, 2, lambda p: AllFunction(p))
            .operator('OR', 1, Associativity.LEFT, 2, lambda p: AnyFunction(p))
            .term('true', lambda: ResolvedValue(True))
            .term('false', lambda: ResolvedValue(False))
            .term('null', lambda: ResolvedValue(None))
            .function('abs', 1, _op_fn1(lambda a: f"abs({a})"))
            .function('min', 2, _op_fn2(lambda a, b: f"min({a},{b})"))
            .function('max', 2, _op_fn2(lambda a, b: f"max({a},{b})"))
            .function('floor', 1, _op_fn1(lambda a: f"floor({a})"))
            .function('ceil', 1, _op_fn1(lambda a: f"ceil({a})"))
            .function('signed', 1, _op_fn1(lambda a: f"signed({a})"))
            .function('if', 3, _op_fn3(lambda a, b, c: f"if({a},{b},{c})"))
            .function('concat', 2, _op_fn2(lambda a, b: f"concat({a},{b})"))
            .function('ordinal', 1, _op_fn1(lambda a: f"ordinal({a})"))
            .function_n('any', lambda values: AnyFunction(values))
            .function_n('all', lambda values: AllFunction(values))
            .variable('@', '', lambda context, key: ResolvedValue(f"@{key}"))
            .variable('@{', '}', lambda context, key: ResolvedValue(f"@{{{key}}}"))
            .variable('min(@', ')', lambda context, key: ResolvedValue(f"min(@{key})"))
            .variable('max(@', ')', lambda context, key: ResolvedValue(f"max(@{key})"))
            .variable('sum(@', ')', lambda context, key: ResolvedValue(f"sum(@{key})"))
            .comment('[', ']', lambda text, value: NamedResolvedValue(value, text)))
