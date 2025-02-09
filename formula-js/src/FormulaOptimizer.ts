import {QuotedTextResolvedValue} from "./QuotedTextResolvedValue";
import {ResolvedValue} from "./ResolvedValue";
import {Associativity, ShuntingYard} from "./ShuntingYard";

function opFn(fn: (...stringArgs: string[]) => string) {
  return (...resolvedArgs: ResolvedValue[]) => ResolvedValue.of(fn(...resolvedArgs.map(format)));
}

function passthroughFn(fnName: string) {
  return (...resolvedArgs: ResolvedValue[]) => ResolvedValue.of(`${fnName}(${resolvedArgs.map(format).join(',')})`);
}

function format(resolvedArg: ResolvedValue): string {
  if (resolvedArg instanceof QuotedTextResolvedValue) {
    return resolvedArg.asQuotedText();
  }
  return resolvedArg.asText();
}

abstract class AbstractOptimizedFunction extends ResolvedValue {
  private static readonly UnsupportedException = new Error("Not available for optimization");

  asNumber(): number {
    throw AbstractOptimizedFunction.UnsupportedException;
  }

  asBoolean(): boolean {
    throw AbstractOptimizedFunction.UnsupportedException;
  }

  asList(): ResolvedValue[] {
    throw AbstractOptimizedFunction.UnsupportedException;
  }
}

class OptimizedAnyFunction extends AbstractOptimizedFunction {
  private readonly values: ResolvedValue[];

  constructor(values: ResolvedValue[]) {
    super();
    this.values = [];
    let hasTrue = false;
    let hasFalse = false;
    values.forEach(next => {
      if (next instanceof OptimizedAnyFunction) {
        this.values.push(...next.values);
        return;
      }
      if (next === ResolvedValue.False) {
        hasFalse = true;
        return;
      }
      if (next === ResolvedValue.True) {
        hasTrue = true;
        return;
      }
      this.values.push(next);
    });

    if (hasTrue) {
      this.values = [ResolvedValue.True];
    }
    if (this.values.length === 0) {
      this.values = [hasFalse ? ResolvedValue.False : ResolvedValue.True];
    }
  }

  asText(): string {
    if (this.values.length === 1) {
      return format(this.values[0]);
    }
    return `any(${this.values.map(format).join(',')})`;
  }
}

class OptimizedAllFunction extends AbstractOptimizedFunction {
  private readonly values: ResolvedValue[];

  constructor(values: ResolvedValue[]) {
    super();
    this.values = [];
    let hasFalse = false;
    values.forEach(next => {
      if (next instanceof OptimizedAllFunction) {
        this.values.push(...next.values);
        return;
      }
      if (next === ResolvedValue.True) {
        return;
      }
      if (next === ResolvedValue.False) {
        hasFalse = true;
        return;
      }
      this.values.push(next);
    });

    if (hasFalse) {
      this.values = [ResolvedValue.False];
    }
    if (this.values.length === 0) {
      this.values = [ResolvedValue.True];
    }
  }

  asText(): string {
    if (this.values.length === 1) {
      return format(this.values[0]);
    }
    return `all(${this.values.map(format).join(',')})`;
  }
}

class OptimizedMathFunction extends AbstractOptimizedFunction {

  static create(operator: string): (a: ResolvedValue, b: ResolvedValue) => ResolvedValue {
    return (a: ResolvedValue, b: ResolvedValue) => new OptimizedMathFunction(operator, a, b);
  }

  constructor(private readonly operator: string, private readonly a: ResolvedValue, private readonly b: ResolvedValue) {
    super();
  }

  asText(): string {
    return `(${this.asTextNoBrackets()})`;
  }

  asTextNoBrackets(): string {
    return this.format(this.a) + this.operator + this.format(this.b);
  }

  private format(v: ResolvedValue): string {
    if (v instanceof OptimizedMathFunction) {
      if ((v.operator == "+" || v.operator == "-") && (this.operator == "+" || this.operator == "-")) {
        return v.asTextNoBrackets();
      }
      if ((v.operator == "*" || v.operator == "/") && (this.operator == "*" || this.operator == "/")) {
        return v.asTextNoBrackets();
      }
    }
    return format(v);
  }
}

export class FormulaOptimizer {

  private static Parser = ShuntingYard.parser()
  .operator('^', 4, Associativity.Right, 2, OptimizedMathFunction.create('^'))
  .operator('*', 3, Associativity.Left, 2, OptimizedMathFunction.create('*'))
  .operator('/', 3, Associativity.Left, 2, OptimizedMathFunction.create('/'))
  .operator('+', 2, Associativity.Left, 2, OptimizedMathFunction.create('+'))
  .operator('-', 2, Associativity.Left, 2, OptimizedMathFunction.create('-'))
  .operator('!', 2, Associativity.Left, 1, opFn((a) => `!${a}`))
  .operator('<', 3, Associativity.Left, 2, opFn((a, b) => `${a}<${b}`))
  .operator('<=', 3, Associativity.Left, 2, opFn((a, b) => `${a}<=${b}`))
  .operator('>', 3, Associativity.Left, 2, opFn((a, b) => `${a}>${b}`))
  .operator('>=', 3, Associativity.Left, 2, opFn((a, b) => `${a}>=${b}`))
  .operator('==', 3, Associativity.Left, 2, opFn((a, b) => `${a}==${b}`))
  .operator('!=', 3, Associativity.Left, 2, opFn((a, b) => `${a}!=${b}`))
  .operator('AND', 1, Associativity.Left, 2, (a, b) => new OptimizedAllFunction([a, b]))
  .operator('OR', 1, Associativity.Left, 2, (a, b) => new OptimizedAnyFunction([a, b]))
  .operator('d', 4, Associativity.Left, 2, opFn((a, b) => `${a}d${b}`))
  .term('true', () => ResolvedValue.of(true))
  .term('false', () => ResolvedValue.of(false))
  .term('null', () => ResolvedValue.of('null'))
  .function('abs', 1, passthroughFn('abs'))
  .function('min', 2, passthroughFn('min'))
  .function('max', 2, passthroughFn('max'))
  .function('floor', 1, passthroughFn('floor'))
  .function('ceil', 1, passthroughFn('ceil'))
  .function('signed', 1, passthroughFn('signed'))
  .function('if', 3, passthroughFn('if'))
  .function('concat', 2, passthroughFn('concat'))
  .function('ordinal', 1, passthroughFn('ordinal'))
  .varargsFunction('any', (args) => new OptimizedAnyFunction(args))
  .varargsFunction('all', (args) => new OptimizedAllFunction(args))
  .variable('@', '', (state, key) => ResolvedValue.of(`@${key}`))
  .variable('@{', '}', (state, key) => ResolvedValue.of(`@{${key}}`))
  .variable('min(@', ')', (state, key) => ResolvedValue.of(`min(@${key})`))
  .variable('max(@', ')', (state, key) => ResolvedValue.of(`max(@${key})`))
  .variable('sum(@', ')', (state, key) => ResolvedValue.of(`sum(@${key})`))
  .comment('[', ']', (comment, subject) => new QuotedTextResolvedValue(subject, "", `[${comment}]`))

  static optimize(formula: string): string {
    const resolved = this.Parser.parse(formula).resolve();
    if (resolved instanceof OptimizedMathFunction) {
      return resolved.asTextNoBrackets();
    }
    return format(resolved);
  }
}