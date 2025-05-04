from formula.resolved_value import resolved_value


def test_text_as_text():
    assert resolved_value("Test").as_text() == "Test"
    assert resolved_value("1").as_text() == "1"
    assert resolved_value("true").as_text() == "true"


def test_text_as_int():
    assert resolved_value("0").as_number() == 0
    assert resolved_value("5").as_number() == 5
    assert resolved_value("-10").as_number() == -10


def test_text_as_bool():
    assert resolved_value("true").as_boolean() is True
    assert resolved_value("false").as_boolean() is False


def test_int_as_int():
    assert resolved_value(0).as_number() == 0
    assert resolved_value(5).as_number() == 5
    assert resolved_value(-20).as_number() == -20


def test_int_as_text():
    assert resolved_value(0).as_text() == "0"
    assert resolved_value(5).as_text() == "5"
    assert resolved_value(-20).as_text() == "-20"


def test_int_as_bool():
    assert resolved_value(0).as_boolean() is False
    assert resolved_value(5).as_boolean() is True
    assert resolved_value(-20).as_boolean() is True


def test_decimal_as_decimal():
    assert resolved_value(0.0).as_decimal() == 0.0
    assert resolved_value(5.2).as_decimal() == 5.2
    assert resolved_value(-20.1).as_decimal() == -20.1


def test_decimal_as_text():
    assert resolved_value(0.0).as_text() == '0'
    assert resolved_value(5.2).as_text() == '5.2'
    assert resolved_value(-20.1).as_text() == '-20.1'


def test_decimal_as_boolean():
    assert resolved_value(0.0).as_boolean() is False
    assert resolved_value(0.01).as_boolean() is True
    assert resolved_value(5.2).as_boolean() is True
    assert resolved_value(-20.1).as_boolean() is True


def test_boolean_as_boolean():
    assert resolved_value(True).as_boolean() is True
    assert resolved_value(False).as_boolean() is False


def test_boolean_as_text():
    assert resolved_value(True).as_text() == 'true'
    assert resolved_value(False).as_text() == 'false'


def test_boolean_as_number():
    assert resolved_value(True).as_number() == 1
    assert resolved_value(False).as_number() == 0


def test_boolean_as_decimal():
    assert resolved_value(True).as_decimal() == 1.0
    assert resolved_value(False).as_decimal() == 0.0
