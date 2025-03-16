const M = 2147483647;
const A = 1103515245;
const C = 12345;

const MAX_SEED = Math.floor(0xFFFF / A) - C - 1;

export class FormulaRandom {
  private state: number;

  constructor(seed?: number) {
    this.state = (seed !== undefined
      ? seed
      : Math.floor(Math.random() * (M - 1))) % MAX_SEED;
  }

  public nextInt(): number {
    const result = (A * this.state + C) % M;
    this.state = result % MAX_SEED;
    return result;
  }

  public nextFloat(): number {
    return this.nextInt() / (M - 1);
  }

  public nextRange(start: number, end: number): number {
    var rangeSize = end - start;
    var randomUnder1 = this.nextInt() / M;
    return start + Math.floor(randomUnder1 * rangeSize);
  }
}