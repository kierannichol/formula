import math

from formula.data_context import DataContext
from formula.resolvable import Resolvable
from formula.resolved_value import resolved_value, NamedResolvedValue, ResolvedValue
from formula.shunting_yard import ShuntingYard, ShuntingYardParser, Associativity


class Formula(Resolvable):

    def __init__(self, shunting_yard: ShuntingYard):
        self.shunting_yard = shunting_yard

    def resolve(self, context: DataContext | None = None) -> ResolvedValue | None:
        return self.shunting_yard.resolve(context)

    def as_formula(self) -> str:
        return self.shunting_yard.original_formula

    @classmethod
    def parse(cls, formula: str):
        return Formula(_parser.parse(formula))

    def __repr__(self):
        return self.as_formula()


_parser = (ShuntingYardParser()
           .decimal_bi_operator('-',
                                4, Associativity.LEFT, lambda x: -x,
                                2, Associativity.LEFT, lambda x, y: x - y)
           .operator('^', 4, Associativity.RIGHT, 2,
                     lambda params: resolved_value(
                         math.pow(params[0].as_number(), params[1].as_number())))
           .decimal_operator_2('*', 3, Associativity.LEFT, lambda x, y: x * y)
           .decimal_operator_2('/', 3, Associativity.LEFT, lambda x, y: x / y)
           .decimal_operator_2('+', 2, Associativity.LEFT, lambda x, y: x + y)
           .operator('!', 2, Associativity.LEFT, 1, lambda p: resolved_value(not p[0].as_boolean()))
           .decimal_operator_2('<', 3, Associativity.LEFT, lambda x, y: x < y)
           .decimal_operator_2('<=', 3, Associativity.LEFT, lambda x, y: x <= y)
           .decimal_operator_2('>', 3, Associativity.LEFT, lambda x, y: x > y)
           .decimal_operator_2('>=', 3, Associativity.LEFT, lambda x, y: x >= y)
           .decimal_operator_2('==', 3, Associativity.LEFT, lambda x, y: x == y)
           .decimal_operator_2('!=', 3, Associativity.LEFT, lambda x, y: x != y)
           .decimal_operator_2('AND', 1, Associativity.LEFT, lambda x, y: x and y)
           .decimal_operator_2('OR', 1, Associativity.LEFT, lambda x, y: x or y)
           .operator(',', 1, Associativity.LEFT, 2, lambda p: resolved_value(p[0].as_list() + p[1].as_list()))
           .term('true', lambda: resolved_value(True))
           .term('false', lambda: resolved_value(False))
           .term('null', lambda: resolved_value(None))
           .function_1('max', lambda x: _max_fn(x))
           .function_1('min', lambda x: _min_fn(x))
           .function_1('sum', lambda x: _sum_fn(x))
           .function_3('clamp', lambda x, y, z: _clamp_fn(x, y, z))
           .function_1('maxeach', lambda x: _maxeach_fn(x))
           .function_1('mineach', lambda x: _mineach_fn(x))
           .decimal_function_1('abs', lambda x: math.fabs(x))
           .decimal_function_1('floor', lambda x: math.floor(x))
           .decimal_function_1('ceil', lambda x: math.ceil(x))
           .decimal_function_1('signed', lambda x: ('+' if x > 0 else '') + f'{x:g}')
           .function_3('if', lambda x, y, z: y if x.as_boolean() else z)
           .function_n('concat', lambda args: _concat_fn(args))
           .function_1('ordinal', lambda x: resolved_value(_ordinal(x.as_number())))
           .function_n('all', lambda args: _all_fn(args))
           .function_n('any', lambda args: _any_fn(args))
           .variable('@', '', lambda context, key: _variable(context, key))
           .variable('@{', '}', lambda context, key: _variable(context, key))
           .comment('[', ']', lambda text, value: NamedResolvedValue(value, text)))


def _all_fn(args):
    for arg in args:
        for x in arg.as_list():
            if not x.as_boolean():
                return resolved_value(False)
    return resolved_value(True)


def _any_fn(args):
    for arg in args:
        for x in arg.as_list():
            if x.as_boolean():
                return resolved_value(True)
    return resolved_value(False)


def _variable(context: DataContext, key: str) -> ResolvedValue:
    if '*' in key:
        return resolved_value(context.search(key))
    return context.get(key)


def _ordinal(n: int) -> str:
    if 11 <= (n % 100) <= 13:
        suffix = 'th'
    else:
        suffix = ['th', 'st', 'nd', 'rd', 'th'][min(n % 10, 4)]
    return str(n) + suffix


def _min_fn(value: ResolvedValue):
    min_value: ResolvedValue | None = None
    for found in value.as_list():
        if min_value is None:
            min_value = found
        elif found.as_decimal() < min_value.as_decimal():
            min_value = found
    if min_value is None:
        return resolved_value(None)
    return min_value


def _max_fn(value: ResolvedValue):
    max_value: ResolvedValue | None = None
    for found in value.as_list():
        if max_value is None:
            max_value = found
        elif found.as_decimal() > max_value.as_decimal():
            max_value = found
    if max_value is None:
        return resolved_value(None)
    return max_value


def _maxeach_fn(x: ResolvedValue) -> ResolvedValue:
    found = []
    for next in x.as_list():
        found.append(_max_fn(next))
    return resolved_value(found)


def _mineach_fn(x: ResolvedValue) -> ResolvedValue:
    found = []
    for next in x.as_list():
        found.append(_min_fn(next))
    return resolved_value(found)


def _clamp_fn(value, min, max):
    if value.as_decimal() < min.as_decimal():
        return min
    if value.as_decimal() > max.as_decimal():
        return max
    return value


def _sum_fn(value: ResolvedValue):
    sum_value = 0
    list = value.as_list()
    if len(list) is 0:
        return resolved_value(0)
    if len(list) is 1:
        return value
    for value in value.as_list():
        sum_value += _sum_fn(value).as_decimal()
    return resolved_value(sum_value)


def _concat_fn(args):
    merged = []
    for arg in args:
        for value in arg.as_list():
            merged.append(value)
    return resolved_value(merged)
