class FormulaTestCase:
    def __init__(self, yaml: dict):
        self.name = yaml.get('name')
        self.formula = yaml.get('formula')
        self.data = yaml.get('data')
        self.expected_text = yaml.get('expected_text')
        self.expected_number = yaml.get('expected_number')
        self.expected_boolean = yaml.get('expected_boolean')

    def __repr__(self):
        return self.name

