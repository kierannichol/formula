import {Associativity, ShuntingYard, ShuntingYardParser} from "./ShuntingYard";
import {ResolvedValue} from "./ResolvedValue";
import {DataContext} from "./DataContext";
import {NamedResolvedValue} from "./NamedResolvedValue";
import {Resolvable} from "./Resolvable";

export class FormulaParser {
  private readonly parser = ShuntingYard.parser();

  static createDefault(): FormulaParser {
    return new FormulaParser()
    .addNulls()
    .addMathOperators()
    .addMathFunctions()
    .addLogicOperators()
    .addLogicFunctions()
    .addLists()
    .addVariables()
    .addOrdinalFunctions()
    .addStringFunctions()
    .addComments();
  }

  constructor() {
  }

  public addCustom(modifyParserFn: (parser: ShuntingYardParser) => void): FormulaParser {
    modifyParserFn(this.parser);
    return this;
  }

  public parse(formula: string): Resolvable {
    return this.parser.parse(formula);
  }

  public addNulls(): FormulaParser {
    this.parser
    .term('null', () => ResolvedValue.None);
    return this;
  }

  public addMathOperators(): FormulaParser {
    this.parser
    .biOperator('-',
        4, Associativity.Left, (a: ResolvedValue) => ResolvedValue.of(-a.asNumber()),
        2, Associativity.Left, (a: ResolvedValue, b: ResolvedValue) => ResolvedValue.of(a.asNumber() - b.asNumber()))
    .operator('^', 4, Associativity.Right, 2, (a: ResolvedValue, b: ResolvedValue) => ResolvedValue.of(Math.pow(a.asNumber(), b.asNumber())))
    .operator('*', 3, Associativity.Left, 2, (a: ResolvedValue, b: ResolvedValue) => ResolvedValue.of(a.asNumber() * b.asNumber()))
    .operator('/', 3, Associativity.Left, 2, (a: ResolvedValue, b: ResolvedValue) => ResolvedValue.of(a.asNumber() / b.asNumber()))
    .operator('+', 2, Associativity.Left, 2, FormulaParser.addReduceFn)
    .operator('!', 2, Associativity.Left, 1, (a: ResolvedValue) => ResolvedValue.of(!a.asBoolean()))
    .operator('<', 3, Associativity.Left, 2, (a: ResolvedValue, b: ResolvedValue) => ResolvedValue.of(a.asNumber() < b.asNumber()))
    .operator('<=', 3, Associativity.Left, 2, (a: ResolvedValue, b: ResolvedValue) => ResolvedValue.of(a.asNumber() <= b.asNumber()))
    .operator('>', 3, Associativity.Left, 2, (a: ResolvedValue, b: ResolvedValue) => ResolvedValue.of(a.asNumber() > b.asNumber()))
    .operator('>=', 3, Associativity.Left, 2, (a: ResolvedValue, b: ResolvedValue) => ResolvedValue.of(a.asNumber() >= b.asNumber()))
    .operator('==', 3, Associativity.Left, 2, (a: ResolvedValue, b: ResolvedValue) => ResolvedValue.of(a.equals(b)))
    .operator('!=', 3, Associativity.Left, 2, (a: ResolvedValue, b: ResolvedValue) => ResolvedValue.of(!a.equals(b)))
    return this;
  }

  public addMathFunctions(): FormulaParser {
    this.parser
    .function('abs', 1, (a: ResolvedValue) => ResolvedValue.of(Math.abs(a.asNumber())))
    .function('floor', 1, (a: ResolvedValue) => ResolvedValue.of(Math.floor(a.asNumber())))
    .function('ceil', 1, (a: ResolvedValue) => ResolvedValue.of(Math.ceil(a.asNumber())))
    .function('signed', 1, (a: ResolvedValue) => ResolvedValue.of((a.asNumber() < 0 ? '' : '+') + a.asNumber()))
    .function('sum', 1, FormulaParser.sumFn)
    .function('clamp', 3, FormulaParser.clampFn)
    .function('max', 1, FormulaParser.maxFn)
    .function('maxeach', 1, FormulaParser.maxEachFn)
    .function('mineach', 1, FormulaParser.minEachFn)
    .function('min', 1, FormulaParser.minFn)
    return this;
  }

  public addLogicOperators(): FormulaParser {
    this.parser
    .operator('AND', 1, Associativity.Left, 2, (a: ResolvedValue, b: ResolvedValue) => ResolvedValue.of(a.asBoolean() && b.asBoolean()))
    .operator('OR', 1, Associativity.Left, 2, (a: ResolvedValue, b: ResolvedValue) => ResolvedValue.of(a.asBoolean() || b.asBoolean()))
    .term('true', () => ResolvedValue.of(true))
    .term('false', () => ResolvedValue.of(false))
    return this;
  }

  public addLogicFunctions(): FormulaParser {
    this.parser
    .function('if', 3, (a: ResolvedValue, b: ResolvedValue, c: ResolvedValue) => a.asBoolean() ? b : c)
    .varargsFunction('any', FormulaParser.anyFn)
    .varargsFunction('all', FormulaParser.allFn);
    return this;
  }

  public addOrdinalFunctions(): FormulaParser {
    this.parser
    .function('ordinal', 1, (a: ResolvedValue) => ResolvedValue.of(ordinal(a.asNumber())));
    return this;
  }

  public addStringFunctions(): FormulaParser {
    this.parser
    .varargsFunction('concat', FormulaParser.concatFn);
    return this;
  }

  public addVariables(): FormulaParser {
    this.parser
    .variable('@', '', FormulaParser.variableFn)
    .variable('@{', '}', FormulaParser.variableFn);
    return this;
  }

  public addComments(): FormulaParser {
    this.parser
    .comment('[', ']', (text, value) => new NamedResolvedValue(value, text));
    return this;
  }

  public addLists(): FormulaParser {
    this.parser
    .operator(',', 1, Associativity.Left, 2, FormulaParser.mergeLists);
    return this;
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
    .map(FormulaParser.maxFn));
  }

  private static minEachFn(a: ResolvedValue): ResolvedValue {
    return ResolvedValue.of(a.asList()
    .map(FormulaParser.minFn));
  }

  private static sumFn(a: ResolvedValue) {
    const list = a.asList();
    if (list.length === 0) return ResolvedValue.of(0);
    if (list.length < 2) return a;
    return ResolvedValue.of(list
    .reduce((a, b) => a + FormulaParser.sumFn(b).asNumber(), 0));
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

  private static allFn(args: ResolvedValue[]): ResolvedValue {
    for (const arg of args) {
      if (!arg.hasValue()) return ResolvedValue.False;
      const argList = arg.asList();
      for (const el of argList) {
        if (!el.hasValue() || !el.asBoolean()) return ResolvedValue.False;
      }
    }
    return ResolvedValue.True;
  }

  private static anyFn(args: ResolvedValue[]): ResolvedValue {
    for (const arg of args) {
      if (!arg.hasValue()) continue;
      for (const el of arg.asList()) {
        if (el.hasValue() && el.asBoolean()) return ResolvedValue.True;
      }
    }
    return ResolvedValue.False;
  }

  private static mergeLists(a: ResolvedValue, b: ResolvedValue) {
    return ResolvedValue.of([
      ...(a.hasValue() ? a.asList() : [ResolvedValue.None]),
      ...(b.hasValue() ? b.asList() : [ResolvedValue.None])
    ])
  }
}

function ordinal(n: number): string {
  let s = ["th", "st", "nd", "rd"];
  let v = n % 100;
  return n + (s[(v - 20) % 10] || s[v] || s[0]);
}