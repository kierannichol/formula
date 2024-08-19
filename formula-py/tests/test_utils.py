from formula.token.utils import to_stream


def test_to_stream():
    assert to_stream('A') == ['A']
    assert to_stream(['A']) == ['A']
    assert to_stream(['A', 'B']) == ['A', 'B']
    assert to_stream(['A', ['B']]) == ['A', 'B']
    assert to_stream(['A', ['B', 'C']]) == ['A', 'B', 'C']
    assert to_stream(['A', ['B', 'C'], ['D', 'E'], 'F']) == ['A', 'B', 'C', 'D', 'E', 'F']
    assert to_stream(['A', ['B', 'C', ['D', 'E'], 'F']]) == ['A', 'B', 'C', 'D', 'E', 'F']