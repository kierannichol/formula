from formula.data_context import DataContext


class Resolvable:
    def resolve(self, context: DataContext | None = None) -> any: ...

    def as_formula(self) -> str: ...


class ResolvableList(Resolvable):
    def push(self, item: Resolvable): ...