from formula import ResolvedValue


class ResolvedRollValue(ResolvedValue):
    def __init__(self, count: int, sides: int):
        self._count = count
        self._sides = sides

    def as_text(self) -> str:
        return f"{str(self._count)}d{str(self._sides)}"

    def as_number(self) -> int:
        return int(self.as_decimal())

    def as_decimal(self) -> float:
        return (self._count * (self._sides + 1)) / 2.0

    def as_boolean(self) -> bool:
        return self._count != 0.0 and self._sides != 0.0

    def has_value(self) -> bool:
        return True

    def __eq__(self, other_value):
        return isinstance(other_value, ResolvedValue) and self.as_decimal() == other_value.as_decimal()

    def __hash__(self):
        return hash(self._count) + hash(self._sides)

    def __repr__(self):
        return f"{str(self._count)}d{str(self._sides)}"