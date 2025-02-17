from pathlib import Path

import pytest
import yaml

from formula import DataContext
from tests.data_context_test_case import DataContextTestCase

DATA_CONTEXT_TEST_CASES_PATH = Path(__file__).parent.parent.parent / "formula-test" / "data-context-test-cases.yml"


def get_test_cases() -> None:
    with open(DATA_CONTEXT_TEST_CASES_PATH, 'r') as file:
        test_cases = yaml.safe_load(file)
        for test_case in test_cases:
            yield DataContextTestCase(test_case)


@pytest.mark.parametrize("test_case", get_test_cases(), ids=lambda test_case: test_case.name)
def test_formula(test_case: DataContextTestCase) -> None:
    context = DataContext(test_case.data)
    for action in test_case.actions:
        action(context)

    for (key, value) in test_case.expected.items():
        value.test(context.get(key))
