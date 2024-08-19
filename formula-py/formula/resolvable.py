from formula.data_context import DataContext
from formula.resolved_value import ResolvedValue


class Resolvable:
    def resolve(self, context: DataContext | None = None) -> ResolvedValue | None: pass
    def as_formula(self) -> str: pass
