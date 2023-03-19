import {DataContext} from "./DataContext";
import {FormulaOptimizer} from "./FormulaOptimizer";
import {Resolvable} from "./Resolvable";
import {ResolvedValue} from "./ResolvedValue";
import {Associativity, ShuntingYard} from "./ShuntingYard";

export class Formula extends Resolvable {

  private static Parser = ShuntingYard.parser()
    .operator('^', 4, Associativity.Right, 2, (a: ResolvedValue, b: ResolvedValue) => ResolvedValue.of(Math.pow(a.asNumber(), b.asNumber())))
    .operator('*', 3, Associativity.Left, 2, (a: ResolvedValue, b: ResolvedValue) => ResolvedValue.of(a.asNumber() * b.asNumber()))
    .operator('/', 3, Associativity.Left, 2, (a: ResolvedValue, b: ResolvedValue) => ResolvedValue.of(a.asNumber() / b.asNumber()))
    .operator('+', 2, Associativity.Left, 2, (a: ResolvedValue, b: ResolvedValue) => ResolvedValue.of(a.asNumber() + b.asNumber()))
    .operator('-', 2, Associativity.Left, 2, (a: ResolvedValue, b: ResolvedValue) => ResolvedValue.of((a?.asNumber() ?? 0) - b.asNumber()))
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
    .variable('min(@', ')', (state, key) => Formula.noneIfEmpty(state.find(key)).reduce((a, b) => a.asNumber() < b.asNumber() ? a : b))
    .variable('max(@', ')', (state, key) => Formula.noneIfEmpty(state.find(key)).reduce((a, b) => a.asNumber() > b.asNumber() ? a : b))
    .variable('sum(@', ')', (state, key) => state.find(key).reduce((a, b) => ResolvedValue.of(a.asNumber() + b.asNumber()), ResolvedValue.none()));

  static parse(formula: string|Resolvable): Formula {
    if (formula instanceof Formula) {
      return formula;
    }
    if (formula instanceof Resolvable) {
      return new Formula(formula);
    }
    return new Formula(this.Parser.parse(formula));
  }

  static optimize(formula: string): string {
    return FormulaOptimizer.optimize(formula);
  }

  resolve(context?: DataContext | undefined): ResolvedValue | undefined {
    return this.resolvable.resolve(context);
  }

  asFormula(): string {
    return this.resolvable.asFormula();
  }

  private constructor(private readonly resolvable: Resolvable) {
    super();
  }

  private static noneIfEmpty(array: ResolvedValue[]): ResolvedValue[] {
    return array.length > 0 ? array : [ ResolvedValue.none() ];
  }
}

function ordinal(n: number): string {
  let s = ["th", "st", "nd", "rd"];
  let v = n % 100;
  return n + (s[(v - 20) % 10] || s[v] || s[0]);
}