import math

from formula.resolve_error import ResolveError


class ResolvedValue:
    def __init__(self, value: str | int | float | bool | None) -> None:
        self.value: str | int | float | bool | None = value

    def as_text(self) -> str:
        if self.value is None:
            return ''
        if isinstance(self.value, bool):
            if self.value is True:
                return 'true'
            else:
                return 'false'
        if isinstance(self.value, float):
            if math.isnan(self.value):
                return 'NaN'
            return f'{self.value:g}'
        if isinstance(self.value, int) and math.isnan(self.value):
            return 'NaN'
        return str(self.value)

    def as_number(self) -> int:
        try:
            return int(self.value) if self.value is not None else 0
        except ValueError:
            raise ResolveError(f"Cannot convert '{self.value}' to a number")

    def as_decimal(self) -> float:
        try:
            return float(self.value) if self.value is not None else 0.0
        except ValueError:
            raise ResolveError(f"Cannot convert '{self.value}' to a number")

    def as_boolean(self) -> bool:
        if self.value is None:
            return False
        if isinstance(self.value, bool):
            return self.value
        if isinstance(self.value, str):
            lower_case = self.value.lower()
            if (lower_case == 'true'
                    or lower_case == 'yes'):
                return True
            if (lower_case == 'false'
                    or lower_case == 'no'
                    or lower_case == '0'
                    or lower_case == ''):
                return False
        if isinstance(self.value, int):
            return self.value != 0
        if isinstance(self.value, float):
            return self.value != 0.0
        return False

    def has_value(self) -> bool:
        return self.value is not None

    def __eq__(self, other_value):
        if not isinstance(other_value, ResolvedValue):
            return False
        if isinstance(self.value, str):
            return self.value == other_value.as_text()
        if isinstance(self.value, int):
            return self.value == other_value.as_number()
        if isinstance(self.value, float):
            return math.isclose(self.value, other_value.as_decimal())
        if isinstance(self.value, bool):
            return self.value == other_value.as_boolean()
        return self.value == other_value.value

    def __hash__(self):
        return hash(self.value)

    def __repr__(self):
        if self.value is None:
            return 'null'
        if isinstance(self.value, str):
            return f"'{self.value}'"
        return str(self.value)


class QuotedTextResolvedValue(ResolvedValue):
    def __init__(self, value: ResolvedValue, prefix: str, suffix: str) -> None:
        super().__init__(value.value)
        self.prefix: str = prefix
        self.suffix: str = suffix

    def as_quoted_text(self) -> str:
        return self.prefix + self.as_text() + self.suffix


class NamedResolvedValue(ResolvedValue):
    def __init__(self, value: ResolvedValue, name: str):
        super().__init__(None)
        self._name = name
        self._value = value

    def as_name(self) -> str:
        return self._name

    def as_text(self) -> str:
        return self._value.as_text()

    def as_number(self) -> int:
        return self._value.as_number()

    def as_decimal(self) -> float:
        return self._value.as_decimal()

    def as_boolean(self) -> bool:
        return self._value.as_boolean()

    def has_value(self) -> bool:
        return self._value.has_value()

    def __eq__(self, other_value):
        return self._value.__eq__(other_value)

    def __hash__(self):
        return self._value.__hash__()

    def __repr__(self):
        return f"{self._value.as_text()}[{self.as_name()}]"


