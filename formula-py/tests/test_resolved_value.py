from formula.resolved_value import ResolvedValue


def test_text_as_text():
    assert ResolvedValue("Test").as_text() == "Test"
    assert ResolvedValue("1").as_text() == "1"
    assert ResolvedValue("true").as_text() == "true"


def test_text_as_int():
    assert ResolvedValue("0").as_number() == 0
    assert ResolvedValue("5").as_number() == 5
    assert ResolvedValue("-10").as_number() == -10


def test_text_as_bool():
    assert ResolvedValue("true").as_boolean() is True
    assert ResolvedValue("false").as_boolean() is False


def test_int_as_int():
    assert ResolvedValue(0).as_number() == 0
    assert ResolvedValue(5).as_number() == 5
    assert ResolvedValue(-20).as_number() == -20


def test_int_as_text():
    assert ResolvedValue(0).as_text() == "0"
    assert ResolvedValue(5).as_text() == "5"
    assert ResolvedValue(-20).as_text() == "-20"


def test_int_as_bool():
    assert ResolvedValue(0).as_boolean() is False
    assert ResolvedValue(5).as_boolean() is True
    assert ResolvedValue(-20).as_boolean() is True


def test_decimal_as_decimal():
    assert ResolvedValue(0.0).as_decimal() == 0.0
    assert ResolvedValue(5.2).as_decimal() == 5.2
    assert ResolvedValue(-20.1).as_decimal() == -20.1


def test_decimal_as_text():
    assert ResolvedValue(0.0).as_text() == '0.0'
    assert ResolvedValue(5.2).as_text() == '5.2'
    assert ResolvedValue(-20.1).as_text() == '-20.1'


def test_decimal_as_boolean():
    assert ResolvedValue(0.0).as_boolean() is False
    assert ResolvedValue(0.01).as_boolean() is True
    assert ResolvedValue(5.2).as_boolean() is True
    assert ResolvedValue(-20.1).as_boolean() is True


def test_boolean_as_boolean():
    assert ResolvedValue(True).as_boolean() is True
    assert ResolvedValue(False).as_boolean() is False


def test_boolean_as_text():
    assert ResolvedValue(True).as_text() == 'true'
    assert ResolvedValue(False).as_text() == 'false'


def test_boolean_as_number():
    assert ResolvedValue(True).as_number() == 1
    assert ResolvedValue(False).as_number() == 0


def test_boolean_as_decimal():
    assert ResolvedValue(True).as_decimal() == 1.0
    assert ResolvedValue(False).as_decimal() == 0.0
