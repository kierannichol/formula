class OptimizeTestCase:
    def __init__(self, yaml: dict):
        self.name = yaml.get('name')
        self.formula = yaml.get('formula')
        self.expected_formula = yaml.get('expected_formula')

    def __repr__(self):
        return self.name
