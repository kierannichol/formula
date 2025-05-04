import {ResolvedValue} from "./ResolvedValue";

test('none equals none', () => {
  const none = ResolvedValue.None;
  expect(none.equals(none)).toBe(true);
})

test('zero does not equal none', () => {
  const none = ResolvedValue.None;
  const zero = ResolvedValue.of(0);
  expect(zero.equals(none)).toBe(false);
  expect(none.equals(zero)).toBe(false);
})