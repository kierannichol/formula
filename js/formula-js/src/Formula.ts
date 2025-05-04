import {DataContext} from "./DataContext";
import {FormulaOptimizer} from "./FormulaOptimizer";
import {Resolvable} from "./Resolvable";
import {ResolvedValue} from "./ResolvedValue";
import {Associativity, ShuntingYard} from "./ShuntingYard";
import {NamedResolvedValue} from "./NamedResolvedValue";

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
  .operator(',', 1, Associativity.Left, 2, (a: ResolvedValue, b: ResolvedValue) => ResolvedValue.of([...a.asList(), ...b.asList()]))
  .term('true', () => ResolvedValue.of(true))
  .term('false', () => ResolvedValue.of(false))
  .term('null', () => ResolvedValue.None)
  .function('abs', 1, (a: ResolvedValue) => ResolvedValue.of(Math.abs(a.asNumber())))
  .function('floor', 1, (a: ResolvedValue) => ResolvedValue.of(Math.floor(a.asNumber())))
  .function('ceil', 1, (a: ResolvedValue) => ResolvedValue.of(Math.ceil(a.asNumber())))
  .function('signed', 1, (a: ResolvedValue) => ResolvedValue.of((a.asNumber() < 0 ? '' : '+') + a.asNumber()))
  .function('if', 3, (a: ResolvedValue, b: ResolvedValue, c: ResolvedValue) => a.asBoolean() ? b : c)
  .varargsFunction('concat', Formula.concatFn)
  .function('ordinal', 1, (a: ResolvedValue) => ResolvedValue.of(ordinal(a.asNumber())))
  .varargsFunction('any', args => ResolvedValue.of(args.flatMap(arg => arg.asList()).some(arg => arg.asBoolean())))
  .varargsFunction('all', args => ResolvedValue.of(args.flatMap(arg => arg.asList()).every(arg => arg.asBoolean())))
  .variable('@', '', Formula.variableFn)
  .variable('@{', '}', Formula.variableFn)
  .function('sum', 1, Formula.sumFn)
  .function('clamp', 3, Formula.clampFn)
  .function('max', 1, Formula.maxFn)
  .function('maxeach', 1, Formula.maxEachFn)
  .function('mineach', 1, Formula.minEachFn)
  .function('min', 1, Formula.minFn)
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

  private static variableFn(state: DataContext, key: string) {
    if (key.includes('*')) {
      return ResolvedValue.of(state.search(key));
    }
    return state.get(key);
  }

  private static minFn(a: ResolvedValue): ResolvedValue {
    const list = a.asList();
    if (list.length < 2) return a;
    return list
    .reduce((a, b) => a.asNumber() < b.asNumber() ? a : b);
  }

  private static maxFn(a: ResolvedValue): ResolvedValue {
    const list = a.asList();
    if (list.length < 2) return a;
    return list
    .reduce((a, b) => a.asNumber() > b.asNumber() ? a : b);
  }

  private static maxEachFn(a: ResolvedValue): ResolvedValue {
    return ResolvedValue.of(a.asList()
    .map(Formula.maxFn));
  }

  private static minEachFn(a: ResolvedValue): ResolvedValue {
    return ResolvedValue.of(a.asList()
    .map(Formula.minFn));
  }

  private static sumFn(a: ResolvedValue) {
    const list = a.asList();
    if (list.length === 0) return ResolvedValue.of(0);
    if (list.length < 2) return a;
    return ResolvedValue.of(list
    .reduce((a, b) => a + Formula.sumFn(b).asNumber(), 0));
  }

  private static concatFn(args: ResolvedValue[]) {
    return ResolvedValue.of(
        args.flatMap(a => a.asList()));
  }

  private static addReduceFn(a: ResolvedValue, b: ResolvedValue): ResolvedValue {
    if ((a ?? ResolvedValue.None).equals(ResolvedValue.None) && (b ?? ResolvedValue.None).equals(ResolvedValue.None)) return ResolvedValue.None;
    return ResolvedValue.of(a.asNumber() + b.asNumber());
  }

  private static clampFn(a: ResolvedValue, min: ResolvedValue, max: ResolvedValue) {
    return ResolvedValue.of(Math.min(max.asNumber(), Math.max(min.asNumber(), a.asNumber())));
  }
}

function ordinal(n: number): string {
  let s = ["th", "st", "nd", "rd"];
  let v = n % 100;
  return n + (s[(v - 20) % 10] || s[v] || s[0]);
}