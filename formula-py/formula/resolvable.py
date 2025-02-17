from formula.resolved_value import resolved_value


class Resolvable:
    def resolve(self, context: any = None) -> any: pass

    def as_formula(self) -> str: pass


class ResolvableList(Resolvable):
    def __init__(self, items=None) -> None:
        if items is None:
            items = []
        self.items = items

    def push(self, item: Resolvable):
        if isinstance(item, ResolvableList):
            self.items.extend(item.items)
        else:
            self.items.append(item)

    def resolve(self, context=None):
        resolved = []
        for item in self.items:
            resolved.append(item.resolve(context))
        return resolved_value(resolved)

    def as_formula(self):
        return "<" + str(map(lambda item: item.as_formula(), self.items)) + ">"

