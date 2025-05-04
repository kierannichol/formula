from formula import Resolvable, ResolvedValue
from formula.resolved_value import resolved_value


class StaticResolvable(Resolvable):

    def __init__(self, value: str | int | float | bool | ResolvedValue):
        self.value = resolved_value(value)

    def resolve(self, context=None):
        return self.value

    def as_formula(self):
        return self.value.as_text()

