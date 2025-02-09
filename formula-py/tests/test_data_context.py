from formula import DataContext, resolved_value


def test_push_new_key():
    context = DataContext({})
    context.push('key', 5)
    assert context.get('key') == resolved_value([
        resolved_value(5)
    ])


def test_push_key_has_scalar_value():
    context = DataContext({'key': 5})
    context.push('key', 6)
    assert context.get('key') == resolved_value([
        resolved_value(5),
        resolved_value(6)
    ])


def test_push_key_has_list_value():
    context = DataContext({'key': [5,6]})
    context.push('key', 7)
    assert context.get('key') == resolved_value([
        resolved_value(5),
        resolved_value(6),
        resolved_value(7)
    ])
