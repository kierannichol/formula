import re

from formula.resolvable import Resolvable
from formula.resolved_value import resolved_value, ResolvedValue


class DataContext:
    def __init__(self, data: dict[str, Resolvable]):
        self._data = data

    def get(self, key: str) -> resolved_value:
        if key not in self._data:
            return resolved_value(None)
        value = self._data[key]
        if isinstance(value, Resolvable):
            return value.resolve(self)
        if isinstance(value, list):
            return self._resolve_list(value)
        return resolved_value(value)

    def keys(self) -> list[str]:
        return list(self._data.keys())

    def set(self, key: str, value: Resolvable):
        self._data[key] = value

    def push(self, key: str, value: Resolvable):
        if key not in self._data:
            self._data[key] = [value]
        elif isinstance(self._data[key], list):
            self._data[key].append(value)
        else:
            existing = self._data[key]
            self._data[key] = [existing, value]

    def search(self, pattern: str) -> list[resolved_value]:
        regex_pattern = pattern.replace('*', '.*?')
        regex = re.compile(f"^{regex_pattern}$")
        for key in self._data:
            match = regex.search(key)
            if match is not None:
                value = self._data[key]
                if isinstance(value, Resolvable):
                    yield value.resolve(self)
                elif isinstance(value, list):
                    yield self._resolve_list(value)
                else:
                    yield resolved_value(value)

    def _resolve_list(self, values) -> ResolvedValue:
        resolved = []
        for value in values:
            if isinstance(value, Resolvable):
                resolved.append(value.resolve(self))
            else:
                resolved.append(resolved_value(value))
        return resolved_value(resolved)