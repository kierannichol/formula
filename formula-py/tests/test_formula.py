from pathlib import Path

import pytest
import yaml

import formula
from formula import DataContext, ResolveError
from tests.formula_test_case import FormulaTestCase

FORMULA_TEST_CASES_PATH = Path(__file__).parent.parent.parent / "formula-test" / "formula-test-cases.yml"


def get_test_cases() -> None:
    with open(FORMULA_TEST_CASES_PATH, 'r') as file:
        test_cases = yaml.safe_load(file)
        for test_case in test_cases:
            yield FormulaTestCase(test_case)


@pytest.mark.parametrize("test_case", get_test_cases(), ids=lambda test_case: test_case.name)
def test_formula(test_case: FormulaTestCase) -> None:
    try:
        parsed = formula.parse(test_case.formula)
        context = DataContext(test_case.data)
        resolved = parsed.resolve(context)
        if test_case.expected_text is not None:
            assert resolved.as_text() == test_case.expected_text
        if test_case.expected_number is not None:
            assert resolved.as_decimal() == test_case.expected_number
        if test_case.expected_boolean is not None:
            assert resolved.as_boolean() == test_case.expected_boolean
    except ResolveError as e:
        if test_case.expected_error is not None:
            assert e.message == test_case.expected_error
        else:
            raise e
