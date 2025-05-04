import {FormulaRandom} from "./FormulaRandom";

test('random int', () => {
  const seed = 10;
  const random = new FormulaRandom(seed);

  expect(random.nextInt()).toBe(297746560);
  expect(random.nextInt()).toBe(1120512893);
});

test('random int (different seed)', () => {
  const seed = 12345;
  const random = new FormulaRandom(seed);

  expect(random.nextInt()).toBe(1406938949);
  expect(random.nextInt()).toBe(506849219);
});

test('random float', () => {
  const seed = 10;
  const random = new FormulaRandom(seed);

  expect(random.nextFloat()).toBeCloseTo(0.13864);
  expect(random.nextFloat()).toBeCloseTo(0.52177);
});

test('random range', () => {
  const seed = 12345;
  const random = new FormulaRandom(seed);

  expect(random.nextRange(5, 10)).toBe(8);
  expect(random.nextRange(5, 10)).toBe(6);
});