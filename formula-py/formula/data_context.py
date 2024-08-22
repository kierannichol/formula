import re
from typing import TypeAlias

from formula.resolvable import Resolvable
from formula.resolved_value import resolved_value

DataContextStateValue: TypeAlias = str | int | float | bool | Resolvable | None
DataContextState: TypeAlias = dict[str, DataContextStateValue]


class DataContext:
    def __init__(self, data: DataContextState):
        self._data = data

    def get(self, key: str) -> resolved_value:
        if key not in self._data:
            return resolved_value(None)
        value = self._data[key]
        if isinstance(value, Resolvable):
            return value.resolve(self)
        return resolved_value(value)

    def keys(self) -> list[str]:
        return list(self._data.keys())

    def set(self, key: str, value: DataContextStateValue):
        self._data[key] = value

    def search(self, pattern: str) -> list[resolved_value]:
        regex_pattern = pattern.replace('*', '.*?')
        regex = re.compile(f"^{regex_pattern}$")
        for key in self._data:
            match = regex.search(key)
            if match is not None:
                value = self._data[key]
                yield resolved_value(value)
