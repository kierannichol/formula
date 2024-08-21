import enum
import string
from typing import Callable, List, Self, TypeAlias

from formula.data_context import DataContext
from formula.resolvable import Resolvable
from formula.resolve_error import ResolveError
from formula.resolved_value import ResolvedValue, QuotedTextResolvedValue
from formula.token import token_tree

_TokenMapper: TypeAlias = Callable[[list[ResolvedValue]], ResolvedValue]
_DataResolver: TypeAlias = Callable[[DataContext], ResolvedValue]
_VariableResolver: TypeAlias = Callable[[DataContext, str], ResolvedValue]


class Associativity(enum.IntEnum):
    LEFT = 1
    RIGHT = 2


class Node:
    pass


class OperatorFunction(Node):
    def __init__(self, name: str, operands: int | None, fn: _TokenMapper):
        super().__init__()
        self.name = name
        self.operands = operands
        self.fn = fn

    def execute(self, args: list[ResolvedValue]):
        if self.operands is not None and self.operands != len(args):
            raise Exception(f"Invalid number of operands; expected {self.operands} but got {len(args)}")
        return self.fn(args)

    def __repr__(self):
        return self.name


class Operator(OperatorFunction):
    def __init__(self,
                 name: str,
                 operands: int,
                 precedence: int,
                 associativity: Associativity,
                 fn: _TokenMapper):
        super().__init__(name, operands, fn)
        self.precedence = precedence
        self.associativity = associativity


class BiOperator(Node):
    def __init__(self, unary: Operator, binary: Operator):
        super().__init__()
        self.unary = unary
        self.binary = binary


class Function(OperatorFunction):
    def __init__(self, name: str, operands: int, fn: _TokenMapper):
        super().__init__(name, operands, fn)


class VarargsFunction(OperatorFunction):
    def __init__(self, name: str, fn: _TokenMapper):
        super().__init__(name, None, fn)


class Variable(Node):
    def __init__(self, key: str, resolve_fn: _VariableResolver):
        super().__init__()
        self.key = key
        self.resolve_fn = resolve_fn

    def resolve(self, context: DataContext) -> ResolvedValue:
        return self.resolve_fn(context, self.key)

    def __str__(self):
        return self.key


class Comment(Node):
    def __init__(self, text: str, decorate_fn: Callable[[str, ResolvedValue], ResolvedValue]):
        super().__init__()
        self.text = text
        self.decorate_fn = decorate_fn

    def apply(self, previous: ResolvedValue):
        return self.decorate_fn(self.text, previous)


class Term(Node):
    def __init__(self, resolve_fn: Callable[[], ResolvedValue], prefix: str | None = None, suffix: str | None = None):
        super().__init__()
        self.prefix = prefix
        self.suffix = suffix
        self.resolve_fn = resolve_fn

    def resolve(self) -> ResolvedValue:
        resolved = self.resolve_fn()
        if self.prefix is not None and self.suffix is not None:
            return QuotedTextResolvedValue(resolved, self.prefix, self.suffix)
        return resolved

    def __repr__(self):
        return str(self.resolve())


class ShuntingYard:
    def __init__(self, stack: List[Node | str | int | float | None], original_formula: str):
        self._stack = stack
        self.original_formula = original_formula

    def resolve(self, context: DataContext | None):
        local_stack = list[any]()
        stack = list[any]()
        stack += self._stack

        while len(stack) > 0:
            next_token = stack.pop(0)
            if isinstance(next_token, OperatorFunction):
                operand_count = next_token.operands \
                    if next_token.operands is not None \
                    else self._pop_arity(local_stack, next_token)
                parameters = []
                for i in range(operand_count):
                    parameters.insert(0, self._checked_popped_param(next_token, i, local_stack))
                local_stack.insert(0, next_token.execute(parameters))
            elif isinstance(next_token, Term):
                stack.insert(0, next_token.resolve())
            elif isinstance(next_token, Variable):
                stack.insert(0, next_token.resolve(context))
            elif isinstance(next_token, Comment):
                local_stack.insert(0, next_token.apply(local_stack.pop(0)))
            else:
                if isinstance(next_token, Resolvable):
                    local_stack.insert(0, next_token.resolve(context))
                else:
                    local_stack.insert(0, next_token)

        return local_stack.pop(0) if len(local_stack) > 0 else ResolvedValue(None)

    @staticmethod
    def _checked_popped_param(func: any, index: int, stack: list):
        if len(stack) == 0:
            raise ResolveError(f"Missing parameter #{index+1} for \"{func}\"")
        return stack.pop(0)

    @staticmethod
    def _pop_arity(stack: list, func: OperatorFunction):
        if len(stack) == 0:
            raise ResolveError(f"Missing arity count for \"{func}\"")
        return stack.pop(0)


class ShuntingYardParser:
    def __init__(self):
        self._parser = (token_tree.create()
                        .ignore_whitespace()
                        .add_branch(token_tree.DECIMAL, lambda x: float(x))
                        .add_branch(token_tree.INTEGER, lambda x: int(x))
                        .add_branch('(', lambda x: x)
                        .add_branch(')', lambda x: x)
                        .add_branch(',', lambda x: x)
                        .add_branch(token_tree.literal('"', '"', '\\"'),
                                    lambda quote: Term(lambda: ResolvedValue(quote[1:-1]), '"', '"'))
                        .add_branch(token_tree.literal('\'', '\'', '\\\''),
                                    lambda quote: Term(lambda: ResolvedValue(quote[1:-1]), '\'', '\'')))

    def operator(self, symbol: str,
                 precedence: int,
                 associativity: Associativity,
                 operands: int,
                 fn: _TokenMapper) -> Self:
        self._parser.add_branch(symbol, lambda _: Operator(symbol, operands, precedence, associativity, fn))
        return self

    def decimal_operator_2(self,
                           symbol: str,
                           precedence: int,
                           associativity: Associativity,
                           func: Callable[[float, float], str | int | float | bool]) -> Self:
        self._parser.add_branch(symbol,
                                lambda _: Operator(symbol,
                                                   2,
                                                   precedence,
                                                   associativity,
                                                   lambda p: ResolvedValue(func(p[0].as_decimal(), p[1].as_decimal()))))
        return self

    def decimal_bi_operator(self,
                            symbol: str,
                            unary_precedence: int,
                            unary_associativity: Associativity,
                            unary_func: Callable[[float], any],
                            binary_precedence: int,
                            binary_associativity: Associativity,
                            binary_func: Callable[[float, float], any]) -> Self:
        self._parser.add_branch(token_tree.term(symbol), lambda _: BiOperator(
            Operator(symbol, 1, unary_precedence, unary_associativity,
                     lambda p: ResolvedValue(unary_func(p[0].as_decimal()))),
            Operator(symbol, 2, binary_precedence, binary_associativity,
                     lambda p: ResolvedValue(binary_func(p[0].as_decimal(), p[1].as_decimal())))
        ))
        return self

    def term(self, text: str, extractor: Callable[[], ResolvedValue]) -> Self:
        self._parser.add_branch(token_tree.term(text), lambda _: Term(extractor))
        return self

    def function_1(self, name, func: Callable[[ResolvedValue], ResolvedValue]) -> Self:
        self._parser.add_branch(token_tree.term(name), lambda _: Function(name, 1, lambda x: func(x[0])))
        return self

    def function_2(self, name, func: Callable[[ResolvedValue, ResolvedValue], ResolvedValue]) -> Self:
        self._parser.add_branch(token_tree.term(name), lambda _: Function(name, 2, lambda x: func(x[0], x[1])))
        return self

    def function_n(self, name, func: Callable[[list[ResolvedValue]], ResolvedValue]) -> Self:
        self._parser.add_branch(token_tree.term(name), lambda _: VarargsFunction(name, func))
        return self

    def variable(self, prefix: str, suffix: str, func: Callable[[DataContext, str], ResolvedValue]) -> Self:
        self._parser.add_branch(
            [
                *token_tree.term(prefix),
                token_tree.any_of(string.ascii_letters),
                token_tree.optional(token_tree.KEY),
                *token_tree.term(suffix)
            ],
            lambda key: Variable(key, lambda context, v_key: func(context, key[len(prefix):len(v_key) - len(suffix)])))
        return self

    def comment(self, prefix: str, suffix: str, func: Callable[[str, ResolvedValue], ResolvedValue]) -> Self:
        self._parser.add_branch(token_tree.literal(prefix, suffix),
                                lambda token: Comment(token[len(prefix):len(token) - len(suffix)], func))
        return self

    def decimal_function_1(self, name, func: Callable[[float], any]) -> Self:
        self._parser.add_branch(token_tree.term(name),
                                lambda _: Function(name, 1,
                                                   lambda x: ResolvedValue(func(x[0].as_decimal()))))
        return self

    def decimal_function_2(self, name, func: Callable[[float, float], any]) -> Self:
        self._parser.add_branch(token_tree.term(name),
                                lambda _: Function(name, 2,
                                                   lambda x: ResolvedValue(func(x[0].as_decimal(), x[1].as_decimal()))))
        return self

    def function_3(self, name, func: Callable[[ResolvedValue, ResolvedValue, ResolvedValue], ResolvedValue]):
        self._parser.add_branch(token_tree.term(name),
                                lambda _: Function(name, 3,
                                                   lambda x: func(x[0], x[1], x[2])))
        return self

    def parse(self, formula: str) -> ShuntingYard:
        output_buffer = list()
        operator_stack = list()
        arity_stack = list()
        tokens = self._parser.parse(formula)

        for i in range(len(tokens)):
            token = tokens[i]
            previous = tokens[i - 1] if i > 0 else None

            if isinstance(token, BiOperator):
                operator = token
                token = operator.binary
                if previous is None or isinstance(previous, Operator) or previous == '(' or previous == ',':
                    token = operator.unary

            if isinstance(token, Operator):
                if len(operator_stack) > 0:
                    top = operator_stack[-1]
                    if isinstance(top, Operator):
                        if (token.precedence < top.precedence
                                or (token.associativity == Associativity.LEFT and token.precedence == top.precedence)):
                            operator_stack.pop()
                            output_buffer.append(top)
                operator_stack.append(token)
                continue

            if isinstance(token, Function) or isinstance(token, VarargsFunction):
                operator_stack.append(token)
                arity_stack.append(1)
                continue

            if isinstance(token, Variable) or isinstance(token, Term) or isinstance(token, Comment):
                output_buffer.append(token)
                continue

            match token:
                case ' ' | '{' | '}':
                    pass
                case ',':
                    arity_stack[-1] += 1
                    while len(operator_stack) > 0:
                        top = operator_stack.pop()
                        if top == '(':
                            operator_stack.append(top)
                            break
                        output_buffer.append(top)
                case '(':
                    operator_stack.append(token)
                case ')':
                    while len(operator_stack) > 0:
                        top = operator_stack.pop()
                        if top == '(':
                            break
                        output_buffer.append(top)
                    if len(operator_stack) > 0:
                        if isinstance(operator_stack[-1], Function):
                            output_buffer.append(operator_stack.pop())
                        elif isinstance(operator_stack[-1], VarargsFunction):
                            arity = 0 if previous == '(' else arity_stack.pop()
                            output_buffer.append(arity)
                            output_buffer.append(operator_stack.pop())
                case _:
                    output_buffer.append(ResolvedValue(token))

        while len(operator_stack) > 0:
            output_buffer.append(operator_stack.pop())

        return ShuntingYard(output_buffer, formula)
