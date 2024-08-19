from typing import TypeAlias
import re

import formula
from formula import ResolvedValue

DataContextStateValue: TypeAlias = str | int | float | bool | formula.Resolvable | None
DataContextState: TypeAlias = dict[str, DataContextStateValue]


class DataContext:
    def __init__(self, data: DataContextState):
        self._data = data

    def get(self, key: str) -> ResolvedValue:
        value = self._data[key]
        if isinstance(value, formula.Resolvable):
            return value.resolve(self)
        return ResolvedValue(value)

    def keys(self) -> list[str]:
        return list(self._data.keys())

    def set(self, key: str, value: DataContextStateValue):
        self._data[key] = value

    def search(self, pattern: str) -> list[ResolvedValue]:
        regex_pattern = pattern.replace('\\*', '.*')
        regex = re.compile(f"^{regex_pattern}$")
        for key in self._data:
            value = self._data[key]
            match = regex.search(value)
            if match is not None:
                yield ResolvedValue(value)
