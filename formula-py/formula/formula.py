import math

from formula.data_context import DataContext
from formula.resolvable import Resolvable
from formula.resolved_value import ResolvedValue
from formula.shunting_yard import ShuntingYard, ShuntingYardParser, Associativity

parser = (ShuntingYardParser()
          .decimal_bi_operator('-',
                               4, Associativity.LEFT, lambda x: -x,
                               2, Associativity.LEFT, lambda x, y: x - y)
          .operator('^', 4, Associativity.RIGHT, 2,
                    lambda params: ResolvedValue(
                        math.pow(params[0].as_number(), params[1].as_number())))
          .decimal_operator_2('*', 3, Associativity.LEFT, lambda x, y: x * y)
          .decimal_operator_2('/', 3, Associativity.LEFT, lambda x, y: x / y)
          .decimal_operator_2('+', 2, Associativity.LEFT, lambda x, y: x + y)
          .operator('!', 2, Associativity.LEFT, 1, lambda p: ResolvedValue(not p[0].as_boolean()))
          .decimal_operator_2('<', 3, Associativity.LEFT, lambda x, y: x < y)
          .decimal_operator_2('<=', 3, Associativity.LEFT, lambda x, y: x <= y)
          .decimal_operator_2('>', 3, Associativity.LEFT, lambda x, y: x > y)
          .decimal_operator_2('>=', 3, Associativity.LEFT, lambda x, y: x >= y)
          .decimal_operator_2('==', 3, Associativity.LEFT, lambda x, y: x == y)
          .decimal_operator_2('!=', 3, Associativity.LEFT, lambda x, y: x != y)
          .decimal_operator_2('AND', 1, Associativity.LEFT, lambda x, y: x and y)
          .decimal_operator_2('OR', 1, Associativity.LEFT, lambda x, y: x or y)
          .term('true', lambda: ResolvedValue(True))
          .term('false', lambda: ResolvedValue(False))
          .term('null', lambda: ResolvedValue(None))
          .decimal_function_1('abs', lambda x: math.fabs(x))
          .decimal_function_2('min', lambda x, y: min(x, y))
          .decimal_function_2('max', lambda x, y: max(x, y))
          .decimal_function_1('floor', lambda x: math.floor(x))
          .decimal_function_1('ceil', lambda x: math.ceil(x))
          .decimal_function_1('signed', lambda x: ('+' if x > 0 else '') + f'{x:g}')
          .function_3('if', lambda x, y, z: y if x.as_boolean() else z)
          .function_2('concat', lambda x, y: ResolvedValue(f'{x.as_text()}{y.as_text()}'))
          # ordinal
          .function_n('all', lambda args: ResolvedValue(all(x.as_boolean() for x in args)))
          .function_n('any', lambda args: ResolvedValue(any(x.as_boolean() for x in args)))
          )


class Formula(Resolvable):

    def __init__(self, shunting_yard: ShuntingYard):
        self.shunting_yard = shunting_yard

    def resolve(self, context: DataContext | None = None) -> ResolvedValue | None:
        return self.shunting_yard.resolve(context)

    def as_formula(self) -> str:
        return self.shunting_yard.original_formula

    @classmethod
    def parse(cls, formula: str):
        return Formula(parser.parse(formula))
