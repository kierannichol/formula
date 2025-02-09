from pathlib import Path

import pytest
import yaml

import formula
from formula import DataContext, ResolveError, resolved_value
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
            assert resolved.as_text() == test_case.expected_text, (
                "expected text {}, got {}".format(test_case.expected_text, resolved.as_text))
        if test_case.expected_number is not None:
            assert resolved.as_decimal() == test_case.expected_number, (
                "expected number {}, got {}".format(test_case.expected_number, resolved.as_number))
        if test_case.expected_boolean is not None:
            assert resolved.as_boolean() == test_case.expected_boolean, (
                "expected boolean {}, got {}".format(test_case.expected_boolean, resolved.as_boolean))
        if test_case.expected_list is not None:
            assert resolved.as_list() == list(map(resolved_value, test_case.expected_list)), (
                "expected list {}, got {}".format(repr(test_case.expected_list), repr(resolved.as_list())))
    except ResolveError as e:
        if test_case.expected_error is not None:
            assert e.message == test_case.expected_error, (
                "expected error {}, got {}".format(test_case.expected_error, e.message))
        else:
            raise e
