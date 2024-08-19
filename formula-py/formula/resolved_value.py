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
            return f'{self.value:g}'
        return str(self.value)

    def as_number(self) -> int:
        return int(self.value) if self.value is not None else 0

    def as_decimal(self) -> float:
        return float(self.value) if self.value is not None else 0.0

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

    def __eq__(self, __value):
        return isinstance(__value, ResolvedValue) and self.value == __value.value

    def __hash__(self):
        return hash(self.value)

    def __repr__(self):
        return str(self.value)


class QuotedTextResolvedValue(ResolvedValue):
    def __init__(self, value: ResolvedValue, prefix: str, suffix: str) -> None:
        super().__init__(value.value)
        self.prefix: str = prefix
        self.suffix: str = suffix

    def as_quoted_text(self) -> str:
        return self.prefix + self.as_text() + self.suffix
