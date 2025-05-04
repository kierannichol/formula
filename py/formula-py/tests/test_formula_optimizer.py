from pathlib import Path

import pytest
import yaml

import formula
from tests.optimize_test_case import OptimizeTestCase

FORMULA_TEST_CASES_PATH = Path(__file__).parent.parent.parent / "formula-test" / "optimize-test-cases.yml"


def get_test_cases() -> None:
    with open(FORMULA_TEST_CASES_PATH, 'r') as file:
        test_cases = yaml.safe_load(file)
        for test_case in test_cases:
            yield OptimizeTestCase(test_case)


@pytest.mark.parametrize("test_case", get_test_cases(), ids=lambda test_case: test_case.name)
def test_formula(test_case: OptimizeTestCase) -> None:
    actual = formula.optimize(test_case.formula)
    assert actual == test_case.expected_formula
