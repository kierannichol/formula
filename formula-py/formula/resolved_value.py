from formula.resolve_error import ResolveError


class ResolvedValue:
    def as_text(self) -> str: pass
    def as_number(self) -> int: pass
    def as_decimal(self) -> float: pass
    def as_boolean(self) -> bool: pass
    def has_value(self) -> bool: pass
    def __eq__(self, other_value): pass
    def __hash__(self): pass
    def __repr__(self): pass

# class ResolvedValue:
#     def __init__(self, value: str | int | float | bool | None) -> None:
#         self.value: str | int | float | bool | None = value
#
#     def as_text(self) -> str:
#         if self.value is None:
#             return ''
#         if isinstance(self.value, bool):
#             if self.value is True:
#                 return 'true'
#             else:
#                 return 'false'
#         if isinstance(self.value, float):
#             if math.isnan(self.value):
#                 return 'NaN'
#             return f'{self.value:g}'
#         if isinstance(self.value, int) and math.isnan(self.value):
#             return 'NaN'
#         return str(self.value)
#
#     def as_number(self) -> int:
#         try:
#             return int(self.value) if self.value is not None else 0
#         except ValueError:
#             raise ResolveError(f"Cannot convert '{self.value}' to a number")
#
#     def as_decimal(self) -> float:
#         try:
#             return float(self.value) if self.value is not None else 0.0
#         except ValueError:
#             raise ResolveError(f"Cannot convert '{self.value}' to a number")
#
#     def as_boolean(self) -> bool:
#         if self.value is None:
#             return False
#         if isinstance(self.value, bool):
#             return self.value
#         if isinstance(self.value, str):
#             lower_case = self.value.lower()
#             if (lower_case == 'true'
#                     or lower_case == 'yes'):
#                 return True
#             if (lower_case == 'false'
#                     or lower_case == 'no'
#                     or lower_case == '0'
#                     or lower_case == ''):
#                 return False
#         if isinstance(self.value, int):
#             return self.value != 0
#         if isinstance(self.value, float):
#             return self.value != 0.0
#         return False
#
#     def has_value(self) -> bool:
#         return self.value is not None
#
#     def __eq__(self, other_value):
#         if not isinstance(other_value, ResolvedValue):
#             return False
#         if isinstance(self.value, str):
#             return self.value == other_value.as_text()
#         if isinstance(self.value, int):
#             return self.value == other_value.as_number()
#         if isinstance(self.value, float):
#             return math.isclose(self.value, other_value.as_decimal())
#         if isinstance(self.value, bool):
#             return self.value == other_value.as_boolean()
#         return self.value == other_value.value
#
#     def __hash__(self):
#         return hash(self.value)
#
#     def __repr__(self):
#         if self.value is None:
#             return 'null'
#         if isinstance(self.value, str):
#             return f"'{self.value}'"
#         return str(self.value)


class _TextResolvedValue(ResolvedValue):
    def __init__(self, value: str):
        self._value = value

    def as_text(self) -> str:
        return self._value

    def as_number(self) -> int:
        try:
            return int(self._value)
        except ValueError:
            raise ResolveError(f"Cannot convert '{self._value}' to a number")

    def as_decimal(self) -> float:
        try:
            return float(self._value)
        except ValueError:
            raise ResolveError(f"Cannot convert '{self._value}' to a number")

    def as_boolean(self) -> bool:
        lower_case = self._value.lower()
        if lower_case == 'true' or lower_case == 'yes':
            return True
        return False

    def has_value(self) -> bool:
        return True

    def __eq__(self, other_value):
        return isinstance(other_value, ResolvedValue) and self._value == other_value.as_text()

    def __hash__(self):
        return hash(self._value)

    def __repr__(self):
        return f"'{self._value}'"


class _NumberResolvedValue(ResolvedValue):
    def __init__(self, value: int):
        self._value = value

    def as_text(self) -> str:
        return str(self._value)

    def as_number(self) -> int:
        return self._value

    def as_decimal(self) -> float:
        return float(self._value)

    def as_boolean(self) -> bool:
        return self._value != 0

    def has_value(self) -> bool:
        return True

    def __eq__(self, other_value):
        return isinstance(other_value, ResolvedValue) and self._value == other_value.as_number()

    def __hash__(self):
        return hash(self._value)

    def __repr__(self):
        return str(self._value)


class _DecimalResolvedValue(ResolvedValue):
    def __init__(self, value: float):
        self._value = value

    def as_text(self) -> str:
        return f"{self._value:g}"

    def as_number(self) -> int:
        return int(self._value)

    def as_decimal(self) -> float:
        return self._value

    def as_boolean(self) -> bool:
        return self._value != 0.0

    def has_value(self) -> bool:
        return True

    def __eq__(self, other_value):
        return isinstance(other_value, ResolvedValue) and self._value == other_value.as_decimal()

    def __hash__(self):
        return hash(self._value)

    def __repr__(self):
        return str(self._value)


class _BooleanResolvedValue(ResolvedValue):
    def __init__(self, value: bool):
        self._value = value

    def as_text(self) -> str:
        return 'true' if self._value else 'false'

    def as_number(self) -> int:
        return 1 if self._value else 0

    def as_decimal(self) -> float:
        return 1.0 if self._value else 0.0

    def as_boolean(self) -> bool:
        return self._value

    def has_value(self) -> bool:
        return True

    def __eq__(self, other_value):
        return isinstance(other_value, ResolvedValue) and self._value == other_value.as_boolean()

    def __hash__(self):
        return hash(self._value)

    def __repr__(self):
        return self.as_text()


class _NullResolvedValue(ResolvedValue):
    def as_text(self) -> str:
        return ''

    def as_number(self) -> int:
        return 0

    def as_decimal(self) -> float:
        return 0.0

    def as_boolean(self) -> bool:
        return False

    def has_value(self) -> bool:
        return False

    def __eq__(self, other_value):
        return isinstance(other_value, _NullResolvedValue)

    def __hash__(self):
        return hash(0)

    def __repr__(self):
        return 'null'


class QuotedTextResolvedValue(ResolvedValue):
    def __init__(self, value: ResolvedValue, prefix: str, suffix: str) -> None:
        self._value = value
        self._prefix: str = prefix
        self._suffix: str = suffix

    def as_quoted_text(self) -> str:
        return self._prefix + self.as_text() + self._suffix

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
        return self._value.__repr__()


class NamedResolvedValue(ResolvedValue):
    def __init__(self, value: ResolvedValue, name: str):
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


def resolved_value(value: str | int | float | bool | None) -> ResolvedValue:
    if isinstance(value, str):
        return _TextResolvedValue(value)
    if isinstance(value, bool):
        return _BooleanResolvedValue(value)
    if isinstance(value, int):
        return _NumberResolvedValue(value)
    if isinstance(value, float):
        return _DecimalResolvedValue(value)
    return _NullResolvedValue()
