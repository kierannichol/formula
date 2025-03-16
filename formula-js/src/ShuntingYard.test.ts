import {DataContext} from "./DataContext";
import {ResolvedValue} from "./ResolvedValue";
import {Associativity, ShuntingYard} from "./ShuntingYard";

const parser = ShuntingYard.parser()
.intOperator('^', 4, Associativity.Right, 2, Math.pow)
.intOperator('*', 3, Associativity.Left, 2, (a: number, b: number) => a * b)
.intOperator('/', 3, Associativity.Left, 2, (a: number, b: number) => a / b)
.intOperator('+', 2, Associativity.Left, 2, (a: number, b: number) => a + b)
.intOperator('-', 2, Associativity.Left, 2, (a: number, b: number) => a - b)
.intFunction('abs', 1, Math.abs)
.intFunction('floor', 1, Math.floor)
.intFunction('ceil', 1, Math.ceil)
.function('max', 1, (a:ResolvedValue) => a.asList().reduce((a, b) => a.asNumber() > b.asNumber() ? a : b))
.function('min', 1, (a:ResolvedValue) => a.asList().reduce((a, b) => a.asNumber() < b.asNumber() ? a : b))
.varargsFunction('any', args => ResolvedValue.of(args.flatMap(arg => arg.asList()).some(arg => arg.asBoolean())))
.varargsFunction('all', args => ResolvedValue.of(args.flatMap(arg => arg.asList()).every(arg => arg.asBoolean())))
.operator('AND', 1, Associativity.Left, 2, (a: ResolvedValue, b: ResolvedValue) => ResolvedValue.of(a.asBoolean() && b.asBoolean()))
.operator('OR', 1, Associativity.Left, 2, (a: ResolvedValue, b: ResolvedValue) => ResolvedValue.of(a.asBoolean() || b.asBoolean()))
.operator(',', 1, Associativity.Left, 2, (a: ResolvedValue, b: ResolvedValue) => ResolvedValue.of([...a.asList(), ...b.asList()]))
.term('true', () => ResolvedValue.of(true))
.term('false', () => ResolvedValue.of(false))
.variable('@', '', (state, key) => state.get(key));

test('basic addition', () => {
  expect(parser.parse("5 + 3")?.resolve(DataContext.Empty)?.asNumber()).toBe(8);
});

test('brackets', () => {
  expect(parser.parse("5 + (2 + 1) + 3")?.resolve(DataContext.Empty)?.asNumber()).toBe(11);
});

test('exponents', () => {
  let formula = parser.parse("2^3");
  expect(formula.resolve()?.asNumber()).toBe(8);
});

test('with brackets', () => {
  let formula = parser.parse('4 + 4 * 2 / ( 1 - 5 )')
  expect(formula.resolve()?.asNumber()).toBe(2);
})

// test ('err: multiple operators in a row', () => {
//   let formula = Formula.parse('2 + + 3')
//   expect(formula.resolve()?.asNumber()).toBeNaN();
// })

test('multiple digit numbers', () => {
  let formula = parser.parse('12 + 100')
  expect(formula.resolve()?.asNumber()).toBe(112);
})

test('abs()', () => {
  let formula = parser.parse('2 + abs(2 - 3) + 1');
  expect(formula.resolve()?.asNumber()).toBe(4);
})

test('min()', () => {
  let formula = parser.parse('1 + min(4, 2)');
  expect(formula.resolve()?.asNumber()).toBe(3);
})

test('max()', () => {
  let formula = parser.parse('1 + max(4, 2)');
  expect(formula.resolve()?.asNumber()).toBe(5);
})

test('floor()', () => {
  let formula = parser.parse('1 + floor(2.9)');
  expect(formula.resolve()?.asNumber()).toBe(3);
})

test('ceil()', () => {
  let formula = parser.parse('1 + ceil(2.9)');
  expect(formula.resolve()?.asNumber()).toBe(4);
})

test('simple variable', () => {
  let formula = parser.parse('@foo');
  let context = DataContext.of({'foo': 12});
  expect(formula.resolve(context)?.asNumber()).toBe(12);
})

test('variable math', () => {
  let formula = parser.parse('@foo + 2');
  let context = DataContext.of({'foo': 1});
  expect(formula.resolve(context)?.asNumber()).toBe(3);
})

test('variable references formula', () => {
  let formula = parser.parse('@bar');
  let context = DataContext.of({
    'foo': 4,
    'bar': parser.parse('@foo')
  });
  expect(formula.resolve(context)?.asNumber()).toBe(4);
})


test('variable math back to formula', () => {
  let formula = parser.parse('@foo + 2');
  expect(formula.asFormula()).toBe('@foo + 2')
})


test('any function with two parameters', () => {
  let formula = parser.parse('any(@a, @b)');
  expect(formula.resolve(DataContext.of({'a': 0, 'b': 0}))?.asBoolean()).toBe(false);
  expect(formula.resolve(DataContext.of({'a': 0, 'b': 1}))?.asBoolean()).toBe(true);
  expect(formula.resolve(DataContext.of({'a': 1, 'b': 0}))?.asBoolean()).toBe(true);
  expect(formula.resolve(DataContext.of({'a': 1, 'b': 1}))?.asBoolean()).toBe(true);
})

test('all function with two parameters', () => {
  let formula = parser.parse('all(@a, @b)');
  expect(formula.resolve(DataContext.of({'a': 0, 'b': 0}))?.asBoolean()).toBe(false);
  expect(formula.resolve(DataContext.of({'a': 0, 'b': 1}))?.asBoolean()).toBe(false);
  expect(formula.resolve(DataContext.of({'a': 1, 'b': 0}))?.asBoolean()).toBe(false);
  expect(formula.resolve(DataContext.of({'a': 1, 'b': 1}))?.asBoolean()).toBe(true);
})

test('all with nested any function', () => {
  let formula = parser.parse('all(@a, any(@b, @c), 1)');
  expect(formula.resolve(DataContext.of({'a': 0, 'b': 0, 'c': 0}))?.asBoolean()).toBe(false);
  expect(formula.resolve(DataContext.of({'a': 1, 'b': 0, 'c': 0}))?.asBoolean()).toBe(false);
  expect(formula.resolve(DataContext.of({'a': 1, 'b': 1, 'c': 0}))?.asBoolean()).toBe(true);
  expect(formula.resolve(DataContext.of({'a': 1, 'b': 0, 'c': 1}))?.asBoolean()).toBe(true);
  expect(formula.resolve(DataContext.of({'a': 1, 'b': 1, 'c': 1}))?.asBoolean()).toBe(true);
})

test('all with nested any function (different order)', () => {
  let formula = parser.parse('all(any(@b, @c), @a, 1)');
  expect(formula.resolve(DataContext.of({'a': 0, 'b': 0, 'c': 0}))?.asBoolean()).toBe(false);
  expect(formula.resolve(DataContext.of({'a': 1, 'b': 0, 'c': 0}))?.asBoolean()).toBe(false);
  expect(formula.resolve(DataContext.of({'a': 1, 'b': 1, 'c': 0}))?.asBoolean()).toBe(true);
  expect(formula.resolve(DataContext.of({'a': 1, 'b': 0, 'c': 1}))?.asBoolean()).toBe(true);
  expect(formula.resolve(DataContext.of({'a': 1, 'b': 1, 'c': 1}))?.asBoolean()).toBe(true);
})