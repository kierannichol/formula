import {DataContext} from "./DataContext";
import {Formula} from "./Formula";
import {ResolvedValue} from "./ResolvedValue";

test('add two scalars', () => {
  let formula = Formula.parse("2 + 3")
  expect(formula.resolve()?.asNumber()).toBe(5);
});

test('exponents', () => {
  let formula = Formula.parse("2^3")
  expect(formula.resolve()?.asNumber()).toBe(8);
});

test ('with brackets', () => {
  let formula = Formula.parse('4 + 4 * 2 / ( 1 - 5 )')
  expect(formula.resolve()?.asNumber()).toBe(2);
})

test ('multiple digit numbers', () => {
  let formula = Formula.parse('12 + 100')
  expect(formula.resolve()?.asNumber()).toBe(112);
})

test ('abs()', () => {
  let formula = Formula.parse('2 + abs(2 - 3) + 1');
  expect(formula.resolve()?.asNumber()).toBe(4);
})

test ('min()', () => {
  let formula = Formula.parse('1 + min(4, 2)');
  expect(formula.resolve()?.asNumber()).toBe(3);
})

test ('max()', () => {
  let formula = Formula.parse('1 + max(4, 2)');
  expect(formula.resolve()?.asNumber()).toBe(5);
})

test ('complex max()', () => {
  let formula = Formula.parse('max(4 - 2, 2 / 2)');
  expect(formula.resolve()?.asNumber()).toBe(2);
})

test ('floor()', () => {
  let formula = Formula.parse('1 + floor(2.9)');
  expect(formula.resolve()?.asNumber()).toBe(3);
})

test ('ceil()', () => {
  let formula = Formula.parse('1 + ceil(2.9)');
  expect(formula.resolve()?.asNumber()).toBe(4);
})

test ('signed()', () => {
  expect(Formula.parse('signed(3)').resolve()?.asText()).toBe('+3');
  expect(Formula.parse('signed(-3)').resolve()?.asText()).toBe('-3');
  expect(Formula.parse('signed(3)').resolve()?.asNumber()).toBe(3);
  expect(Formula.parse('signed(-3)').resolve()?.asNumber()).toBe(-3);
})

test ('negative integers', () => {
  expect(Formula.parse('-4').resolve()?.asNumber()).toBe(-4);
  expect(Formula.parse('1-4').resolve()?.asNumber()).toBe(-3);
  expect(Formula.parse('(1)-4').resolve()?.asNumber()).toBe(-3);
  expect(Formula.parse('1-(4)').resolve()?.asNumber()).toBe(-3);
  expect(Formula.parse('(1-4)').resolve()?.asNumber()).toBe(-3);
})

test ('trailing minus integer', () => {
  let formula = Formula.parse('(5)-1');
  expect(formula.resolve()?.asNumber()).toBe(4);
})

test ('multiply negative integer', () => {
  let formula = Formula.parse('5*-2');
  expect(formula.resolve()?.asNumber()).toBe(-10);
})

test ('simple variable', () => {
  let formula = Formula.parse('@foo');
  let context = DataContext.of({ 'foo': 12 });
  expect(formula.resolve(context)?.asNumber()).toBe(12);
})

test ('variable math', () => {
  let formula = Formula.parse('@foo + 2');
  let context = DataContext.of({ 'foo': 1 });
  expect(formula.resolve(context)?.asNumber()).toBe(3);
})

test ('math with undefined variable', () => {
  expect(Formula.parse('@foo + 1').resolve()?.asNumber()).toBe(1);
  expect(Formula.parse('1 + @foo').resolve()?.asNumber()).toBe(1);
  expect(Formula.parse('@foo + @bar').resolve()?.asNumber()).toBe(0);
})

test ('variable references formula', () => {
  let formula = Formula.parse('@bar');
  let context = DataContext.of({
    'foo': 4,
    'bar': Formula.parse('@foo') });
  expect(formula.resolve(context)?.asNumber()).toBe(4);
})

test ('if formula', () => {
  let formula = Formula.parse('concat(if(-2 < 0, "-", "+"), 2)');
  let context = DataContext.Empty;
  expect(formula.resolve(context)?.asText()).toBe('-2');
})

test ('else formula', () => {
  let formula = Formula.parse('concat(if(2 < 0, "-", "+"), 2)');
  let context = DataContext.Empty;
  expect(formula.resolve(context)?.asText()).toBe('+2');
})

test ('equals formula', () => {
  expect(Formula.parse('5==5').resolve()?.asBoolean()).toBe(true);
  expect(Formula.parse('5==6').resolve()?.asBoolean()).toBe(false);
  expect(Formula.parse('5=="5"').resolve()?.asBoolean()).toBe(true);
  expect(Formula.parse('5==@five').resolve(DataContext.of({'five':5}))?.asBoolean()).toBe(true);
  expect(Formula.parse('5==@five').resolve(DataContext.of({'five':'5'}))?.asBoolean()).toBe(true);
  expect(Formula.parse('"ABC"=="ABC"').resolve()?.asBoolean()).toBe(true);
  expect(Formula.parse('"ABC"=="XYZ"').resolve()?.asBoolean()).toBe(false);
  expect(Formula.parse('"10"==10').resolve()?.asBoolean()).toBe(true);
})

test ('modifier formula', () => {
  let formula = Formula.parse(`concat(if((floor(@test_score/2) - 5) > 0, "+", ""), floor(@test_score/2) - 5))`);
  let context = DataContext.of({
    'test_score': 12
  });
  expect(formula.resolve(context)?.asText()).toBe('+1');
})

test ('min(wildcard)', () => {
  let formula = Formula.parse('min(@key_*)');
  let context = DataContext.of({
    'other': 2,
    'key_1': 4,
    'key_2': 3,
    'key_3': 5
  });
  expect(formula.resolve(context)?.asNumber()).toBe(3);
})

test ('max(wildcard)', () => {
  let formula = Formula.parse('max(@key_*)');
  let context = DataContext.of({
    'other': 2,
    'key_1': 4,
    'key_2': 3,
    'key_3': 5
  });
  expect(formula.resolve(context)?.asNumber()).toBe(5);
})

test ('sum(wildcard)', () => {
  let formula = Formula.parse('sum(@key_*)');
  let context = DataContext.of({
    'other': 2,
    'key_1': 4,
    'key_2': 3,
    'key_3': 5
  });
  expect(formula.resolve(context)?.asNumber()).toBe(12);
})

test ('sum(a:wildcard:b)', () => {
  let formula = Formula.parse('sum(@ability:*:lay_on_hands)');
  let context = DataContext.of({
    'ability:paladin:lay_on_hands': 1,
    // 'base:second:target': 1,
  });
  expect(formula.resolve(context)?.asNumber()).toBe(1);
})

test ('with comment', () => {
  let formula = Formula.parse('(4[Four] + 2[Two])')
  expect(formula.resolve()?.asNumber()).toBe(6);
})

test ('zero not same as undefined', () => {
  let formula = Formula.parse('0')
  expect(formula.resolve()?.asText()).toBe('0');
})

test ('null is same as undefined', () => {
  let formula = Formula.parse('null')
  expect(formula.resolve()?.asText()).toBe("");
})

test('parse performance test', () => {
  const iterations = 1000;
  let startTime = performance.now();
  for (let i = 0; i < iterations; i++) {
    Formula.parse("@alpha AND (@beta OR @delta) AND @sigma AND (@omega >= 5)");
  }
  let endTime = performance.now();
  let total = endTime - startTime;
  let average = total / iterations * 1000;
  console.log(`Average Parse: ${average.toFixed(2)} µs`);
})

test('resolve performance test', () => {
  const iterations = 1000;
  const formula = Formula.parse("@alpha AND (@beta OR @delta) AND @sigma AND (@omega >= 5)");
  const context = DataContext.of({
    "alpha": "true",
    "beta": 1,
    "delta": 0,
    "sigma": "Not a number",
    "omega": "22"
  });
  for (let j = 0; j < 200; j++) {
    context.set(`key_${j}`, `value_${j}`);
  }
  let startTime = performance.now();
  for (let i = 0; i < iterations; i++) {
    formula.resolve(context);
  }
  let endTime = performance.now();
  let total = endTime - startTime;
  let average = total / iterations * 1000;
  console.log(`Average Resolve: ${average.toFixed(2)} µs`);
})

test('deep resolve performance test', () => {
  const iterations = 1000;
  const depth = 1000;
  const formula = Formula.parse(`@step_${depth}`);
  const context = DataContext.of({
    "step_1": 1
  });
  for (let j = 2; j <= depth; j++) {
    context.set(`step_${j}`, Formula.parse(`@step_${j-1} + 1`));
  }
  let startTime = performance.now();
  let result = ResolvedValue.None;
  for (let i = 0; i < iterations; i++) {
    result = formula.resolve(context);
  }
  let endTime = performance.now();
  let total = endTime - startTime;
  let average = total / iterations * 1000;
  expect(result.asNumber()).toBe(depth);
  console.log(`Average Deep Resolve: ${average.toFixed(2)} µs`);
})