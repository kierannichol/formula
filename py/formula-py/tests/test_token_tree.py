import string

import pytest

from formula.resolved_value import resolved_value
from formula.token import token_tree

DIGITS = '0123456789'
ALPHA = string.ascii_letters


def test_single_node():
    assert (token_tree.create()
            .add_branch('A', lambda text: resolved_value(text))
            .parse('A') == [resolved_value('A')])


def test_simple_chain():
    parser = token_tree.create().add_branch('ABC', lambda text: resolved_value(text))
    assert parser.parse('ABC') == [resolved_value('ABC')]
    with pytest.raises(ValueError):
        parser.parse('A')
    with pytest.raises(ValueError):
        parser.parse('AB')
    with pytest.raises(ValueError):
        parser.parse('ABX')


def test_splitting_chain():
    parser = (token_tree.create()
              .add_branch('ABC', lambda text: resolved_value(text))
              .add_branch('A23', lambda text: resolved_value(text)))
    assert parser.parse('ABC') == [resolved_value('ABC')]
    assert parser.parse('A23') == [resolved_value('A23')]


def test_multiple_tokens():
    parser = (token_tree.create()
              .ignore_whitespace()
              .add_branch('ABC', lambda text: resolved_value(text))
              .add_branch('123', lambda text: resolved_value(text)))

    assert parser.parse('ABC 123') == [resolved_value('ABC'), resolved_value('123')]


def test_any_of_token():
    parser = (token_tree.create()
              .ignore_whitespace()
              .add_branch([token_tree.any_of(DIGITS)], lambda text: resolved_value(text)))

    assert parser.parse('1 2 3') == [resolved_value(1), resolved_value(2), resolved_value(3)]


def test_any_of_chain():
    parser = (token_tree.create()
              .ignore_whitespace()
              .add_branch([token_tree.any_of(DIGITS), token_tree.any_of(DIGITS)],
                          lambda text: resolved_value(text)))

    assert parser.parse('13 25 36') == [resolved_value(13), resolved_value(25), resolved_value(36)]


def test_repeated():
    parser = (token_tree.create()
              .ignore_whitespace()
              .add_branch([token_tree.any_of(DIGITS).repeats(1, 2)],
                          lambda text: resolved_value(text)))

    assert parser.parse('5') == [resolved_value(5)]
    assert parser.parse('73') == [resolved_value(73)]
    assert parser.parse('12 9 23') == [resolved_value(12), resolved_value(9), resolved_value(23)]


def test_optional_trailing_character():
    parser = (token_tree.create()
              .ignore_whitespace()
              .add_branch([token_tree.any_of(ALPHA).repeats(1), token_tree.any_of(DIGITS).optional()],
                          lambda text: resolved_value(text)))

    assert parser.parse('A5') == [resolved_value('A5')]
    assert parser.parse('ABC6') == [resolved_value('ABC6')]
    assert parser.parse('ABC') == [resolved_value('ABC')]
    with pytest.raises(ValueError):
        parser.parse('5')
    with pytest.raises(ValueError):
        parser.parse('ABC56')


def test_optional_leading_character():
    parser = (token_tree.create()
              .add_branch([token_tree.any_of('@').optional(),
                           token_tree.any_of(ALPHA).repeats(1),
                           token_tree.any_of(DIGITS).repeats(1)],
                          lambda text: resolved_value(text)))

    assert parser.parse('@A5') == [resolved_value('@A5')]
    assert parser.parse('A5') == [resolved_value('A5')]
    with pytest.raises(ValueError):
        parser.parse('@')
    with pytest.raises(ValueError):
        parser.parse('@@A5')


def test_parse_decimal():
    parser = (token_tree.create()
              .add_branch(token_tree.NUMBER, lambda text: resolved_value(text)))
    assert parser.parse('3.14') == [resolved_value(3.14)]


def test_number_token():
    parser = (token_tree.create()
              .add_branch(token_tree.NUMBER, lambda text: resolved_value(text)))

    assert parser.parse('1') == [resolved_value(1)]
    assert parser.parse('23') == [resolved_value(23)]
    assert parser.parse('54890') == [resolved_value(54890)]
    assert parser.parse('3.14') == [resolved_value(3.14)]
    with pytest.raises(ValueError):
        parser.parse('A')
    with pytest.raises(ValueError):
        parser.parse('5B')
    with pytest.raises(ValueError):
        parser.parse('2.')
    with pytest.raises(ValueError):
        parser.parse('.5')


def test_expression_order():
    parser = (token_tree.create()
              .ignore_whitespace()
              .add_branch(token_tree.DECIMAL, lambda text: float(text))
              .add_branch(token_tree.INTEGER, lambda text: str(text))
              )

    assert parser.parse('123 3.14 5 0.2') == ["123", 3.14, "5", 0.2]


def test_any_until():
    parser = (token_tree.create()
              .add_branch(token_tree.any_until('5'), lambda text: str(text))
              .add_branch('5', lambda text: str(text)))

    assert parser.parse('12345') == ['1234', '5']


def test_quoted_token():
    word = string.ascii_letters
    parser = (token_tree.create()
              .ignore_whitespace()
              .add_branch(token_tree.literal('"', '"'), lambda text: str(text))
              .add_branch(token_tree.any_of(word).repeats(1), lambda text: str(text)))

    assert parser.parse('one two "three four" five') == ["one", "two", "\"three four\"", "five"]


def test_quoted_token_with_escape():
    word = string.ascii_letters
    parser = (token_tree.create()
              .ignore_whitespace()
              .add_branch(token_tree.literal('"', '"', '\\"'), lambda text: str(text))
              .add_branch(token_tree.any_of(word).repeats(1), lambda text: str(text)))

    assert parser.parse('one two "three \\"four\\"" five') == ["one", "two", "\"three \\\"four\\\"\"", "five"]


def test_open_close_tag():
    word = string.ascii_letters
    parser = (token_tree.create()
              .ignore_whitespace()
              .add_branch(token_tree.literal('<open>', '<close>'), lambda text: str(text))
              .add_branch(token_tree.any_of(word).repeats(1), lambda text: str(text)))

    assert parser.parse('one two <open>three four<close> five') == ['one', 'two', '<open>three four<close>', 'five']
