import math

import formula
from formula import resolved_value, DataContext


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


class FormulaTestCase:
    def __init__(self, yaml: dict):
        self.name = yaml.get('name')
        self.formula = yaml.get('formula')
        self.data = _parse_data(yaml.get('data'))
        self.expected_text = yaml.get('expected_text')
        self.expected_number = yaml.get('expected_number')
        if self.expected_number == 'NaN':
            self.expected_number = math.nan
        self.expected_boolean = yaml.get('expected_boolean')
        self.expected_error = yaml.get('expected_error')
        self.expected_list = yaml.get('expected_list')

    def __repr__(self):
        return self.name
