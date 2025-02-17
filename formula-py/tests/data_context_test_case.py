import formula
from formula import resolved_value
from tests.expected_values import ExpectedValues


def _parse_value(value: str):
    if isinstance(value, str) and value.startswith('{') and value.endswith('}'):
        return formula.parse(value[1:-1])
    if isinstance(value, list):
        return list(map(resolved_value, value))
    return value


def _parse_data(data: dict):
    if data is None:
        return {}
    for key in data.keys():
        value = data.get(key)
        if isinstance(value, str) and value.startswith('{') and value.endswith('}'):
            data[key] = formula.parse(value[1:-1])
        if isinstance(value, list):
            data[key] = list(map(resolved_value, value))
    return data


def _parse_action(action_text: str):
    parts = action_text.split(' ')
    if len(parts) == 3 and parts[0] == 'SET':
        func = lambda context: context.set(parts[1], _parse_value(parts[2]))
        return func
    if len(parts) == 3 and parts[0] == 'PUSH':
        func = lambda context: context.push(parts[1], _parse_value(parts[2]))
        return func


class DataContextTestCase:
    def __init__(self, yaml: dict):
        self.name = yaml.get('name')
        self.data = _parse_data(yaml.get('data'))
        self.actions = map(_parse_action, yaml.get('actions'))
        self.expected = {}
        for (key, value) in yaml.get('expected').items():
            self.expected[key] = ExpectedValues(value)

    def __repr__(self):
        return self.name
