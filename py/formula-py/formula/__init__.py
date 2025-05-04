import formula.resolvable
from formula.resolved_value import resolved_value, ResolvedValue, QuotedTextResolvedValue, NamedResolvedValue
from formula.resolvable import Resolvable
from formula.data_context import DataContext
from formula.formula import Formula
from formula.formula_optimizer import optimize_formula
from formula.resolve_error import ResolveError


def parse(formula_text: str) -> Resolvable:
    return Formula.parse(formula_text)


def optimize(formula_text: str) -> str:
    return optimize_formula(formula_text)
