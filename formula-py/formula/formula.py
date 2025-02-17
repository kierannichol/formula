import math

from formula.data_context import DataContext
from formula.resolvable import Resolvable
from formula.resolved_value import resolved_value, NamedResolvedValue, ResolvedValue
from formula.shunting_yard import ShuntingYard, ShuntingYardParser, Associativity
from formula.resolved_roll_value import ResolvedRollValue


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
           .operator('d', 4, Associativity.LEFT, 2, lambda p: ResolvedRollValue(p[0].as_number(), p[1].as_number()))
           .term('true', lambda: resolved_value(True))
           .term('false', lambda: resolved_value(False))
           .term('null', lambda: resolved_value(None))
           .decimal_function_1('abs', lambda x: math.fabs(x))
           .decimal_function_2('min', lambda x, y: min(x, y))
           .decimal_function_2('max', lambda x, y: max(x, y))
           .decimal_function_1('floor', lambda x: math.floor(x))
           .decimal_function_1('ceil', lambda x: math.ceil(x))
           .decimal_function_1('signed', lambda x: ('+' if x > 0 else '') + f'{x:g}')
           .function_3('if', lambda x, y, z: y if x.as_boolean() else z)
           .function_n('concat', lambda args: _concat_fn(args))
           .function_1('ordinal', lambda x: resolved_value(_ordinal(x.as_number())))
           .function_n('all', lambda args: resolved_value(all(x.as_boolean() for x in args)))
           .function_n('any', lambda args: resolved_value(any(x.as_boolean() for x in args)))
           .variable('@', '', lambda context, key: context.get(key))
           .variable('@{', '}', lambda context, key: context.get(key))
           .variable('min(@', ')', lambda context, key: _min_fn(context, key))
           .variable('max(@', ')', lambda context, key: _max_fn(context, key))
           .variable('sum(@', ')', lambda context, key: _sum_fn(context, key))
           .comment('[', ']', lambda text, value: NamedResolvedValue(value, text)))


def _ordinal(n: int) -> str:
    if 11 <= (n % 100) <= 13:
        suffix = 'th'
    else:
        suffix = ['th', 'st', 'nd', 'rd', 'th'][min(n % 10, 4)]
    return str(n) + suffix


def _min_fn(context: DataContext, key: str):
    min_value: float | None = None
    for found in context.search(key):
        for value in found.as_list():
            if min_value is None:
                min_value = value.as_decimal()
            else:
                min_value = min(min_value, value.as_number())
    return resolved_value(min_value)


def _max_fn(context: DataContext, key: str):
    max_value: float | None = None
    for found in context.search(key):
        for value in found.as_list():
            if max_value is None:
                max_value = value.as_decimal()
            else:
                max_value = max(max_value, value.as_number())
    return resolved_value(max_value)


def _sum_fn(context: DataContext, key: str):
    sum_value = 0
    for found in context.search(key):
        for value in found.as_list():
            sum_value += value.as_number()
    return resolved_value(sum_value)


def _concat_fn(args):
    merged = []
    for arg in args:
        for value in arg.as_list():
            merged.append(value)
    return resolved_value(merged)
