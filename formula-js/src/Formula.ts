import {DataContext} from "./DataContext";
import {FormulaOptimizer} from "./FormulaOptimizer";
import {Resolvable} from "./Resolvable";
import {ResolvedValue} from "./ResolvedValue";
import {Associativity, ShuntingYard} from "./ShuntingYard";

export class Formula {

  private static Parser = ShuntingYard.parser()
    .biOperator('-',
      4, Associativity.Left,(a: ResolvedValue) => ResolvedValue.of(-a.asNumber()),
      2, Associativity.Left,(a: ResolvedValue, b: ResolvedValue) => ResolvedValue.of(a.asNumber() - b.asNumber()))
    .operator('^', 4, Associativity.Right, 2, (a: ResolvedValue, b: ResolvedValue) => ResolvedValue.of(Math.pow(a.asNumber(), b.asNumber())))
    .operator('*', 3, Associativity.Left, 2, (a: ResolvedValue, b: ResolvedValue) => ResolvedValue.of(a.asNumber() * b.asNumber()))
    .operator('/', 3, Associativity.Left, 2, (a: ResolvedValue, b: ResolvedValue) => ResolvedValue.of(a.asNumber() / b.asNumber()))
    .operator('+', 2, Associativity.Left, 2, (a: ResolvedValue, b: ResolvedValue) => ResolvedValue.of(a.asNumber() + b.asNumber()))
    .operator('!', 2, Associativity.Left, 1, (a:ResolvedValue) => ResolvedValue.of(!a.asBoolean()))
    .operator('<', 3, Associativity.Left, 2, (a:ResolvedValue, b:ResolvedValue) => ResolvedValue.of(a.asNumber() < b.asNumber()))
    .operator('<=', 3, Associativity.Left, 2, (a:ResolvedValue, b:ResolvedValue) => ResolvedValue.of(a.asNumber() <= b.asNumber()))
    .operator('>', 3, Associativity.Left, 2, (a:ResolvedValue, b:ResolvedValue) => ResolvedValue.of(a.asNumber() > b.asNumber()))
    .operator('>=', 3, Associativity.Left, 2, (a:ResolvedValue, b:ResolvedValue) => ResolvedValue.of(a.asNumber() >= b.asNumber()))
    .operator('==', 3, Associativity.Left, 2, (a:ResolvedValue, b:ResolvedValue) => ResolvedValue.of(a.equals(b)))
    .operator('!=', 3, Associativity.Left, 2, (a:ResolvedValue, b:ResolvedValue) => ResolvedValue.of(!a.equals(b)))
    .operator('AND', 1, Associativity.Left, 2, (a:ResolvedValue, b:ResolvedValue) => ResolvedValue.of(a.asBoolean() && b.asBoolean()))
    .operator('OR', 1, Associativity.Left, 2, (a:ResolvedValue, b:ResolvedValue) => ResolvedValue.of(a.asBoolean() || b.asBoolean()))
    .term('true', () => ResolvedValue.of(true))
    .term('false', () => ResolvedValue.of(false))
    .function('abs', 1, (a: ResolvedValue) => ResolvedValue.of(Math.abs(a.asNumber())))
    .function('min', 2, (a: ResolvedValue, b: ResolvedValue) => ResolvedValue.of(Math.min(a.asNumber(), b.asNumber())))
    .function('max', 2, (a: ResolvedValue, b: ResolvedValue) => ResolvedValue.of(Math.max(a.asNumber(), b.asNumber())))
    .function('floor', 1, (a: ResolvedValue) => ResolvedValue.of(Math.floor(a.asNumber())))
    .function('ceil', 1, (a: ResolvedValue) => ResolvedValue.of(Math.ceil(a.asNumber())))
    .function('signed', 1, (a: ResolvedValue) => ResolvedValue.of((a.asNumber() < 0 ? '' : '+') + a.asNumber()))
    .function('if', 3, (a: ResolvedValue, b: ResolvedValue, c: ResolvedValue) => a.asBoolean() ? b : c)
    .function('concat', 2, (a: ResolvedValue, b: ResolvedValue) => ResolvedValue.of(a.asText() + b.asText()))
    .function('ordinal', 1, (a: ResolvedValue) => ResolvedValue.of(ordinal(a.asNumber())))
    .varargsFunction('any', args => ResolvedValue.of(args.some(arg => arg.asBoolean())))
    .varargsFunction('all', args => ResolvedValue.of(args.every(arg => arg.asBoolean())))
    .variable('@', '', (state, key) => state.get(key))
    .variable('min(@', ')', Formula.minFn)
    .variable('max(@', ')', Formula.maxFn)
    .variable('sum(@', ')', Formula.sumFn)
    .comment('[', ']')
  ;

  static parse(formula: string|Resolvable): Resolvable {
    if (formula instanceof Resolvable) {
      return formula;
    }
    return this.Parser.parse(formula);
  }

  static optimize(formula: string): string {
    return FormulaOptimizer.optimize(formula);
  }

  private static noneIfEmpty(array: ResolvedValue[]): ResolvedValue[] {
    return array.length > 0 ? array : [ ResolvedValue.None ];
  }

  private static minFn(state: DataContext, key: string) {
    return Formula.noneIfEmpty(state.search(key)).reduce((a, b) => a.asNumber() < b.asNumber() ? a : b);
  }

  private static maxFn(state: DataContext, key: string) {
    return Formula.noneIfEmpty(state.search(key)).reduce((a, b) => a.asNumber() > b.asNumber() ? a : b);
  }

  private static sumFn(state: DataContext, key: string) {
    return state.search(key).reduce((a, b) => ResolvedValue.of(a.asNumber() + b.asNumber()), ResolvedValue.None);
  }
}

function ordinal(n: number): string {
  let s = ["th", "st", "nd", "rd"];
  let v = n % 100;
  return n + (s[(v - 20) % 10] || s[v] || s[0]);
}