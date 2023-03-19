import {DataContext} from "./DataContext";
import Parser from "./Parser";
import {QuotedTextResolvedValue} from "./QuotedTextResolvedValue";
import {Resolvable} from "./Resolvable";
import {ResolvedValue} from "./ResolvedValue";
import {ResolveError} from "./ResolveError";
import TokenTree, {alpha, decimal, integer, key, literal, optional, term} from "./TokenTree";

type ZeroOperandFunction<T> = () => T;
type OneOperandFunction<T> = (x: T) => T;
type TwoOperandFunction<T> = (x: T, y: T) => T;
type ThreeOperandFunction<T> = (x: T, y: T, z: T) => T;
type OperandFunction<T> = ZeroOperandFunction<T> | OneOperandFunction<T> | TwoOperandFunction<T> | ThreeOperandFunction<T>;
type VarargsOperandFunction<T> = (n: T[]) => T;

function mapIntFunction(operands: number, fn: OperandFunction<number>): OperandFunction<ResolvedValue> {
  switch (operands) {
    case 0: return () => ResolvedValue.of((fn as ZeroOperandFunction<number>)());
    case 1: return (x?: ResolvedValue) => ResolvedValue.of((fn as OneOperandFunction<number>)(x?.asNumber() ?? 0));
    case 2: return (x?: ResolvedValue, y?: ResolvedValue) => ResolvedValue.of((fn as TwoOperandFunction<number>)(x?.asNumber() ?? 0, y?.asNumber() ?? 0));
    case 3: return (x?: ResolvedValue, y?: ResolvedValue, z?: ResolvedValue) => ResolvedValue.of((fn as ThreeOperandFunction<number>)(x?.asNumber() ?? 0, y?.asNumber() ?? 0, z?.asNumber() ?? 0));
    default: throw new Error(`Unsupported number of operands: ${operands}`);
  }
}

export enum Associativity {
  Left = 1,
  Right = 2
}

interface Node {

}

abstract class OperatorFunction implements Node {
  public readonly name: string;
  public readonly operands: number;
  private readonly fn: OperandFunction<ResolvedValue>;

  abstract get associativity(): Associativity;

  protected constructor(name: string, operands: number, fn: OperandFunction<ResolvedValue>) {
    this.name = name;
    this.operands = operands;
    this.fn = fn;
  }

  execute(x?: ResolvedValue, y?: ResolvedValue, z?: ResolvedValue): ResolvedValue {
    switch (this.operands) {
      case 0:
        return (this.fn as ZeroOperandFunction<ResolvedValue>)();
      case 1:
        return (this.fn as OneOperandFunction<ResolvedValue>)(x as ResolvedValue);
      case 2:
        return (this.fn as TwoOperandFunction<ResolvedValue>)(x as ResolvedValue, y as ResolvedValue);
      case 3:
        return (this.fn as ThreeOperandFunction<ResolvedValue>)(x as ResolvedValue, y as ResolvedValue, z as ResolvedValue);
      default:
        throw new Error(`Unsupported number of operands: ${this.operands}`);
    }
  }
}

class Function extends OperatorFunction {
  constructor(name: string, operands: number, fn: OperandFunction<ResolvedValue>) {
    super(name, operands, fn);
  }

  get associativity(): Associativity {
    return Associativity.Left;
  }
}

class VarargsFunction implements Node {
  constructor(public readonly name: string,
              private readonly fn: VarargsOperandFunction<ResolvedValue>) {
  }
  execute(args: ResolvedValue[]): ResolvedValue {
    return this.fn(args);
  }
}

class Operator extends OperatorFunction {
  constructor(public readonly name: string,
              public readonly precedence: number,
              public readonly associativity: Associativity,
              operands: number, fn: OperandFunction<ResolvedValue>) {
    super(name, operands, fn);
  }
}

class Variable implements Node {
  constructor(public readonly key: string,
              private readonly resolver: (context: DataContext, key: string) => ResolvedValue|Resolvable|undefined) {}

  resolve(context: DataContext) {
    return this.resolver(context, this.key);
  }

  public toString(): string {
    return this.key;
  }
}

class Comment implements Node {
  constructor(public readonly comment: string, public readonly fn: (subject: ResolvedValue, comment: string) => ResolvedValue|Resolvable|undefined) {}
}

class Term implements Node {
  constructor(private readonly resolver: (context: DataContext) => ResolvedValue|Resolvable|undefined,
              private readonly prefix: string|undefined = undefined,
              private readonly suffix: string|undefined = undefined) {}

  resolve(context: DataContext) {
    return (this.prefix && this.suffix)
      ? this.resolver(context).map(resolved => new QuotedTextResolvedValue(resolved, this.prefix, this.suffix))
      : this.resolver(context);
  }
}

export class ShuntingYardParser implements Parser {
  private readonly parser: TokenTree;
  private bracketFn: (value: ResolvedValue) => ResolvedValue = value => value;

  constructor() {
    this.parser = new TokenTree()
      .ignoreWhitespaces()
      .add([ integer ], token => parseInt(token))
      .add([ decimal ], token => parseFloat(token))
      .add('(', token => token)
      .add(')', token => token)
      .add(',', token => token)
      .add(literal('"', '"', '\\"'), quote => new Term(() => ResolvedValue.of(quote.slice(1,-1)), "\"", "\""))
      .add(literal("'", "'", "\\'"), quote => new Term(() => ResolvedValue.of(quote.slice(1,-1)), "'", "'"))
  }

  operator(symbol: string, precedence: number, associativity: Associativity, operands: number, fn: OperandFunction<ResolvedValue>) {
    this.parser.add(symbol, _ => new Operator(symbol, precedence, associativity, operands, fn));
    return this;
  }

  intOperator(symbol: string, precedence: number, associativity: Associativity, operands: number, fn: OperandFunction<number>) {
    return this.operator(symbol, precedence, associativity, operands, mapIntFunction(operands, fn));
  }

  function(name: string, operands: number, fn: OperandFunction<ResolvedValue>) {
    this.parser.add(name, _ => new Function(name, operands, fn));
    return this;
  }

  varargsFunction(name: string, fn: VarargsOperandFunction<ResolvedValue>) {
    this.parser.add(name, _ => new VarargsFunction(name, fn));
    return this;
  }

  intFunction(name: string, operands: number, fn: OperandFunction<number>) {
    return this.function(name, operands, mapIntFunction(operands, fn));
  }

  // variable(identifier: string, extractor: (context: DataContext, key: string) => ResolvedValue|Resolvable|undefined) {
  //   this.parser.add([ term(identifier), alpha, optional(key) ],
  //           key => new Variable(key, (context: DataContext, key: string) =>
  //               extractor(context, key.substring(identifier.length))));
  //   return this;
  // }

  variable(prefix: string, suffix: string, extractor: (context: DataContext, key: string) => ResolvedValue|Resolvable|undefined) {
    this.parser.add([ term(prefix), alpha, optional(key), term(suffix) ],
        key => new Variable(key, (context: DataContext, key: string) =>
            extractor(context, key.substring(prefix.length, key.length - suffix.length))));
    return this;
  }

  comment(prefix: string, suffix: string, fn: (subject: ResolvedValue, comment: string) => ResolvedValue|Resolvable|undefined) {
    this.parser.add([ literal(prefix, suffix, "\\"+prefix) ],
        token => new Comment(token, fn));
    return this;
  }

  term(text: string, extractor: (context: DataContext) => ResolvedValue|Resolvable|undefined) {
    this.parser.add([ term(text) ],
        key => new Variable(key, (context: DataContext) =>
            extractor(context)));
    return this;
  }

  brackets(mapFn: (value: ResolvedValue) => ResolvedValue) {
    this.bracketFn = mapFn;
    return this;
  }

  parse(formula: string): ShuntingYard {
    let outputBuffer: OperatorStack = [];
    let operatorStack: OperatorStack = [];
    let arityStack: number[] = [];

    const tokens = this.parser.parse(formula);

    for (let token of tokens) {
      if (token instanceof Operator) {
        let operator = token;
        let top = operatorStack[operatorStack.length-1];
        if (top instanceof Operator) {
          if ((operator.precedence < top.precedence)
              || (operator.associativity === Associativity.Left
                  && operator.precedence === top.precedence)) {
            operatorStack.pop();
            outputBuffer.push(top);
          }
        }

        operatorStack.push(operator);
        continue;
      }

      if (token instanceof Function) {
        operatorStack.push(token);
        arityStack.push(1);
        continue;
      }

      if (token instanceof VarargsFunction) {
        operatorStack.push(token);
        arityStack.push(1);
        continue;
      }

      if (token instanceof Variable) {
        outputBuffer.push(token);
        continue;
      }

      if (token instanceof Comment) {
        outputBuffer.push(token);
        continue;
      }

      if (token instanceof Term) {
        outputBuffer.push(token);
        continue;
      }

      switch (token) {
        case ' ':
        case '{':
        case '}':
          // ignore
          break;
        case ',':
          arityStack[arityStack.length-1]++;
          while (operatorStack.length > 0) {
            let next = operatorStack.pop();
            if (next === '(') {
              operatorStack.push(next);
              break;
            }
            outputBuffer.push(next);
          }
          break;
        case '(':
          operatorStack.push(token);
          break;
        case ')':
          while (operatorStack.length > 0) {
            let next = operatorStack.pop();
            if (next === '(') {
              break;
            }
            outputBuffer.push(next);
          }

          if (operatorStack[operatorStack.length-1] instanceof Function) {
            const paramCount = arityStack.pop();
            const func = operatorStack.pop() as Function;
            if (paramCount !== func.operands) {
              throw new Error(`${func.name} expected ${func.operands} parameters, but got ${paramCount}`);
            }
            outputBuffer.push(func);
          }
          else if (operatorStack[operatorStack.length-1] instanceof VarargsFunction) {
            outputBuffer.push(arityStack.pop());
            outputBuffer.push(operatorStack.pop());
          }
          break;
        default:
          outputBuffer.push(ResolvedValue.of(token));
      }
    }

    while (operatorStack.length > 0) {
      outputBuffer.push(operatorStack.pop());
    }

    return new ShuntingYard(outputBuffer, formula);
  }
}

type OperatorStack = Array<Node|string|number|null|undefined>;

export class ShuntingYard extends Resolvable {
  private readonly originalFormula: string;
  private readonly stack: OperatorStack;

  static parser(): ShuntingYardParser {
    return new ShuntingYardParser();
  }

  constructor(stack: OperatorStack, originalFormula: string) {
    super();
    this.stack = stack;
    this.originalFormula = originalFormula;
  }

  asFormula(): string {
    return this.originalFormula;
  }

  resolve(context: DataContext = DataContext.Empty): ResolvedValue|undefined {
    let stack: OperatorStack = [];
    for (let i = 0; i < this.stack.length; i++) {
      let next = this.stack[i];

      if (next instanceof OperatorFunction) {
        let func = next;
        if (func.operands === 0) {
          stack.push(func.execute());
        }
        else if (func.operands === 1) {
          let x = stack.pop() as ResolvedValue;
          if (x === undefined) throw new ResolveError(`Error executing function, ${func.name}, because parameter was undefined`);
          stack.push(func.execute(x));
        }
        else if (func.operands === 2) {
          let b = stack.pop() as ResolvedValue;
          let a = stack.pop() as ResolvedValue;
          if (a === undefined) throw new ResolveError(`Error executing function, ${func.name}, because first parameter was undefined`);
          if (b === undefined) throw new ResolveError(`Error executing function, ${func.name}, because second parameter was undefined`);
          stack.push(func.execute(a, b));
        }
        else if (func.operands === 3) {
          let c = stack.pop() as ResolvedValue;
          let b = stack.pop() as ResolvedValue;
          let a = stack.pop() as ResolvedValue;
          if (a === undefined) throw new ResolveError(`Error executing function, ${func.name}, because first parameter was undefined`);
          if (b === undefined) throw new ResolveError(`Error executing function, ${func.name}, because second parameter was undefined`);
          if (c === undefined) throw new ResolveError(`Error executing function, ${func.name}, because third parameter was undefined`);
          stack.push(func.execute(a, b, c));
        }
        else {
          throw new Error("Unsupported number of operands: " + func.operands);
        }
        continue;
      }

      if (next instanceof VarargsFunction) {
        let func = next;
        let params: ResolvedValue[] = [];
        let paramCount = stack.pop() as number;
        while (paramCount-- > 0) {
          params.push(stack.pop() as ResolvedValue);
        }
        stack.push(func.execute(params));
        continue;
      }

      if (next instanceof Comment) {
        stack.push(next.fn(stack.pop() as ResolvedValue, next.comment));
        continue;
      }

      if (next instanceof Variable || next instanceof Term) {
        next = next.resolve(context);
        while (next instanceof Resolvable) {
          next = next.resolve(context);
        }
      }

      stack.push(next);
    }

    return stack.pop() as ResolvedValue;
  }
}