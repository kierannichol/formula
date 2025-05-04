import re

from formula.resolvable import Resolvable
from formula.resolved_value import resolved_value, ResolvedValue


class DataContext:
    def __init__(self, data: dict[str, str|int|float|bool|Resolvable|list[Resolvable]]):
        self._data = data

    def get(self, key: str) -> ResolvedValue:
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

    def search(self, pattern: str) -> list[ResolvedValue]:
        regex_pattern = re.escape(pattern).replace('\\*', '.*?')
        regex = re.compile(f"^{regex_pattern}$")
        found = []
        for key in self._data:
            match = regex.search(key)
            if match is not None:
                found.append(self.get(key))
        return found

    def _resolve_list(self, values) -> ResolvedValue:
        resolved = []
        for value in values:
            if isinstance(value, Resolvable):
                resolved.append(value.resolve(self))
            else:
                resolved.append(resolved_value(value))
        return resolved_value(resolved)