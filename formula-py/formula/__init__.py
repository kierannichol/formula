from formula.formula import Formula
from formula.data_context import DataContext
from formula.resolvable import Resolvable
from formula.resolved_value import ResolvedValue


def parse(formula_text: str) -> resolvable.Resolvable:
    return Formula.parse(formula_text)
