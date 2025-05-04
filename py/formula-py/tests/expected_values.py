from formula import ResolvedValue


class ExpectedValues:
    def __init__(self, yaml: dict):
        self.expected_text = yaml.get("expected_text")
        self.expected_number = yaml.get("expected_number")
        self.expected_boolean = yaml.get("expected_boolean")
        self.expected_list = yaml.get("expected_list")

    def test(self, actual: ResolvedValue):
        if self.expected_text is not None:
            assert actual.as_text() == self.expected_text
        if self.expected_number is not None:
            assert actual.as_number() == self.expected_number
        if self.expected_boolean is not None:
            assert actual.as_boolean() == self.expected_boolean
        if self.expected_list is not None:
            for i in range(len(self.expected_list)):
                ExpectedValues(self.expected_list[i]).test(actual.as_list()[i])