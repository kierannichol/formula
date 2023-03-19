import {Formula} from "./Formula";

test('any', () => {
  let formula = Formula.optimize("any(any(@a, any(@b, @c)), @d)")
  expect(formula).toBe("any(@a,@b,@c,@d)");
});

test('all', () => {
  let formula = Formula.optimize("all(any(@a, all(@b)), @c, all(@d AND @e), @f)")
  expect(formula).toBe("all(any(@a,@b),@c,@d,@e,@f)");
});

test('bracketAdd', () => {
  let formula = Formula.optimize("@a + (@b + @c)")
  expect(formula).toBe("@a+@b+@c");
});

test('keepsRequiredBrackets', () => {
  expect(Formula.optimize("@a * (@b + @c + @d)/2")).toBe("@a*(@b+@c+@d)/2");
  expect(Formula.optimize("@a - (@b / @c)")).toBe("@a-(@b/@c)");
  expect(Formula.optimize("@a < (@b - @c)")).toBe("@a<(@b-@c)");
});

test('literals', () => {
  expect(Formula.optimize("\"testing\"")).toBe("\"testing\"");
  expect(Formula.optimize("any(@a,\"testing\")")).toBe("any(@a,\"testing\")");
});

test('comments', () => {
  expect(Formula.optimize("(@a+@b)[testing]")).toBe("(@a+@b)[testing]");
});
