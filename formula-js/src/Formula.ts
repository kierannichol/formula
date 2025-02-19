import {DataContext} from "./DataContext";
import {FormulaOptimizer} from "./FormulaOptimizer";
import {Resolvable} from "./Resolvable";
import {ResolvedValue} from "./ResolvedValue";
import {Associativity, ShuntingYard} from "./ShuntingYard";
import {NamedResolvedValue} from "./NamedResolvedValue";
import {ResolvedRollValue} from "./ResolvedRollValue";

export class Formula {

  private static Parser = ShuntingYard.parser()
  .biOperator('-',
      4, Associativity.Left, (a: ResolvedValue) => ResolvedValue.of(-a.asNumber()),
      2, Associativity.Left, (a: ResolvedValue, b: ResolvedValue) => ResolvedValue.of(a.asNumber() - b.asNumber()))
  .operator('^', 4, Associativity.Right, 2, (a: ResolvedValue, b: ResolvedValue) => ResolvedValue.of(Math.pow(a.asNumber(), b.asNumber())))
  .operator('*', 3, Associativity.Left, 2, (a: ResolvedValue, b: ResolvedValue) => ResolvedValue.of(a.asNumber() * b.asNumber()))
  .operator('/', 3, Associativity.Left, 2, (a: ResolvedValue, b: ResolvedValue) => ResolvedValue.of(a.asNumber() / b.asNumber()))
  .operator('+', 2, Associativity.Left, 2, Formula.addReduceFn)
  .operator('!', 2, Associativity.Left, 1, (a: ResolvedValue) => ResolvedValue.of(!a.asBoolean()))
  .operator('<', 3, Associativity.Left, 2, (a: ResolvedValue, b: ResolvedValue) => ResolvedValue.of(a.asNumber() < b.asNumber()))
  .operator('<=', 3, Associativity.Left, 2, (a: ResolvedValue, b: ResolvedValue) => ResolvedValue.of(a.asNumber() <= b.asNumber()))
  .operator('>', 3, Associativity.Left, 2, (a: ResolvedValue, b: ResolvedValue) => ResolvedValue.of(a.asNumber() > b.asNumber()))
  .operator('>=', 3, Associativity.Left, 2, (a: ResolvedValue, b: ResolvedValue) => ResolvedValue.of(a.asNumber() >= b.asNumber()))
  .operator('==', 3, Associativity.Left, 2, (a: ResolvedValue, b: ResolvedValue) => ResolvedValue.of(a.equals(b)))
  .operator('!=', 3, Associativity.Left, 2, (a: ResolvedValue, b: ResolvedValue) => ResolvedValue.of(!a.equals(b)))
  .operator('AND', 1, Associativity.Left, 2, (a: ResolvedValue, b: ResolvedValue) => ResolvedValue.of(a.asBoolean() && b.asBoolean()))
  .operator('OR', 1, Associativity.Left, 2, (a: ResolvedValue, b: ResolvedValue) => ResolvedValue.of(a.asBoolean() || b.asBoolean()))
  .operator('d', 4, Associativity.Left, 2, (a: ResolvedValue, b: ResolvedValue) => new ResolvedRollValue(a.asNumber(), b.asNumber()))
  .term('true', () => ResolvedValue.of(true))
  .term('false', () => ResolvedValue.of(false))
  .term('null', () => ResolvedValue.None)
  .function('abs', 1, (a: ResolvedValue) => ResolvedValue.of(Math.abs(a.asNumber())))
  .function('min', 2, (a: ResolvedValue, b: ResolvedValue) => ResolvedValue.of(Math.min(a.asNumber(), b.asNumber())))
  .function('max', 2, (a: ResolvedValue, b: ResolvedValue) => ResolvedValue.of(Math.max(a.asNumber(), b.asNumber())))
  .function('floor', 1, (a: ResolvedValue) => ResolvedValue.of(Math.floor(a.asNumber())))
  .function('ceil', 1, (a: ResolvedValue) => ResolvedValue.of(Math.ceil(a.asNumber())))
  .function('signed', 1, (a: ResolvedValue) => ResolvedValue.of((a.asNumber() < 0 ? '' : '+') + a.asNumber()))
  .function('if', 3, (a: ResolvedValue, b: ResolvedValue, c: ResolvedValue) => a.asBoolean() ? b : c)
  .varargsFunction('concat', Formula.concatFn)
  .function('ordinal', 1, (a: ResolvedValue) => ResolvedValue.of(ordinal(a.asNumber())))
  .varargsFunction('any', args => ResolvedValue.of(args.some(arg => arg.asBoolean())))
  .varargsFunction('all', args => ResolvedValue.of(args.every(arg => arg.asBoolean())))
  .variable('@', '', Formula.variableFn)
  .variable('@{', '}', Formula.variableFn)
  .variable('min(@', ')', Formula.minFn)
  .variable('max(@', ')', Formula.maxFn)
  .variable('sum(@', ')', Formula.sumFn)
  .variable('sum(max(@', '))', Formula.sumMaxFn)
  .variable('sum(min(@', '))', Formula.sumMinFn)
  .comment('[', ']', (text, value) => new NamedResolvedValue(value, text))
  ;

  static parse(formula: string | Resolvable): Resolvable {
    if (formula instanceof Resolvable) {
      return formula;
    }
    return this.Parser.parse(formula);
  }

  static optimize(formula: string): string {
    return FormulaOptimizer.optimize(formula);
  }

  private static noneIfEmpty(array: ResolvedValue[]): ResolvedValue[] {
    return array.length > 0 ? array : [];
  }

  private static variableFn(state: DataContext, key: string) {
    return state.get(key);
  }

  private static minFn(state: DataContext, key: string) {
    return Formula.noneIfEmpty(state.search(key))
    .flatMap(a => a.asList())
    .reduce(Formula.minReduceFn, ResolvedValue.None);
  }

  private static maxFn(state: DataContext, key: string) {
    return Formula.noneIfEmpty(state.search(key))
    .flatMap(a => a.asList())
    .reduce(Formula.maxReduceFn, ResolvedValue.None);
  }

  private static sumFn(state: DataContext, key: string) {
    return state.search(key)
    .flatMap(a => a.asList())
    .reduce(Formula.addReduceFn, ResolvedValue.of(0));
  }

  private static sumMaxFn(state: DataContext, key: string) {
    return state.search(key)
    .flatMap(a => a.asList().reduce(Formula.maxReduceFn))
    .reduce(Formula.addReduceFn, ResolvedValue.of(0));
  }

  private static sumMinFn(state: DataContext, key: string) {
    return state.search(key)
    .flatMap(a => a.asList().reduce(Formula.minReduceFn))
    .reduce(Formula.addReduceFn, ResolvedValue.of(0));
  }

  private static concatFn(args: ResolvedValue[]) {
    return ResolvedValue.of(
        args.flatMap(a => a.asList()));
  }

  private static addReduceFn(a: ResolvedValue, b: ResolvedValue): ResolvedValue {
    if ((a ?? ResolvedValue.None).equals(ResolvedValue.None) && (b ?? ResolvedValue.None).equals(ResolvedValue.None)) return ResolvedValue.None;
    return ResolvedValue.of(a.asNumber() + b.asNumber());
  }

  private static maxReduceFn(a: ResolvedValue, b: ResolvedValue): ResolvedValue {
    return Formula.notNone(a,b) ?? (a.asNumber() > b.asNumber() ? a : b);
  }

  private static minReduceFn(a: ResolvedValue, b: ResolvedValue): ResolvedValue {
    return Formula.notNone(a,b) ?? (a.asNumber() < b.asNumber() ? a : b);
  }

  private static notNone(a: ResolvedValue, b: ResolvedValue): ResolvedValue | null {
    if (!a.hasValue() && !b.hasValue()) return ResolvedValue.None;
    if (!a.hasValue()) return b;
    if (!b.hasValue()) return a;
    return null;
  }
}

function ordinal(n: number): string {
  let s = ["th", "st", "nd", "rd"];
  let v = n % 100;
  return n + (s[(v - 20) % 10] || s[v] || s[0]);
}