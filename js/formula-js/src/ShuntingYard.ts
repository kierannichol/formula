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
type OperandFunction<T> =
    ZeroOperandFunction<T>
    | OneOperandFunction<T>
    | TwoOperandFunction<T>
    | ThreeOperandFunction<T>;
type VarargsOperandFunction<T> = (n: T[]) => T;

function mapIntFunction(operands: number, fn: OperandFunction<number>): OperandFunction<ResolvedValue> {
  switch (operands) {
    case 0:
      return () => ResolvedValue.of((fn as ZeroOperandFunction<number>)());
    case 1:
      return (x?: ResolvedValue) => ResolvedValue.of((fn as OneOperandFunction<number>)(x?.asNumber() ?? 0));
    case 2:
      return (x?: ResolvedValue, y?: ResolvedValue) => ResolvedValue.of((fn as TwoOperandFunction<number>)(x?.asNumber() ?? 0, y?.asNumber() ?? 0));
    case 3:
      return (x?: ResolvedValue, y?: ResolvedValue, z?: ResolvedValue) => ResolvedValue.of((fn as ThreeOperandFunction<number>)(x?.asNumber() ?? 0, y?.asNumber() ?? 0, z?.asNumber() ?? 0));
    default:
      throw new Error(`Unsupported number of operands: ${operands}`);
  }
}

export enum Associativity {
  Left = 1,
  Right = 2
}

interface Node {

}

export abstract class Modifier {
  abstract apply(value: ResolvedValue): ResolvedValue;
}

class NoModifier extends Modifier {
  static readonly instance = new NoModifier();

  apply(value: ResolvedValue): ResolvedValue {
    return value;
  }
}

abstract class OperatorFunction implements Node {
  public readonly name: string;
  public readonly operands: number;
  private readonly fn: OperandFunction<ResolvedValue>;

  protected constructor(name: string, operands: number, fn: OperandFunction<ResolvedValue>) {
    this.name = name;
    this.operands = operands;
    this.fn = fn;
  }

  execute(modifier: Modifier, x?: ResolvedValue, y?: ResolvedValue, z?: ResolvedValue): ResolvedValue {
    const modifiedX = modifier.apply(x);
    const modifiedY = modifier.apply(y);
    const modifiedZ = modifier.apply(z);

    switch (this.operands) {
      case 0:
        return (this.fn as ZeroOperandFunction<ResolvedValue>)();
      case 1:
        return (this.fn as OneOperandFunction<ResolvedValue>)(modifiedX);
      case 2:
        return (this.fn as TwoOperandFunction<ResolvedValue>)(modifiedX, modifiedY);
      case 3:
        return (this.fn as ThreeOperandFunction<ResolvedValue>)(modifiedX, modifiedY, modifiedZ);
      default:
        throw new Error(`Unsupported number of operands: ${this.operands}`);
    }
  }
}

class Function extends OperatorFunction {
  constructor(name: string, operands: number, fn: OperandFunction<ResolvedValue>) {
    super(name, operands, fn);
  }
}

class VarargsFunction implements Node {
  constructor(public readonly name: string,
              private readonly fn: VarargsOperandFunction<ResolvedValue>) {
  }

  execute(modifier: Modifier, args: ResolvedValue[]): ResolvedValue {
    return this.fn(args.map(modifier.apply));
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

class BiOperator implements Node {
  constructor(public readonly unary: Operator, public readonly binary: Operator) {
  }
}

class Variable implements Node {
  constructor(public readonly key: string,
              private readonly resolver: (context: DataContext, key: string) => ResolvedValue | Resolvable | undefined) {
  }

  resolve(context: DataContext): ResolvedValue | Resolvable {
    return this.resolver(context, this.key) ?? ResolvedValue.None;
  }

  public toString(): string {
    return this.key;
  }
}

class Comment implements Node {
  constructor(private readonly text: string,
              private readonly decorator: (text: string, value: ResolvedValue) => ResolvedValue) {
  }

  apply(previous: ResolvedValue) {
    return this.decorator(this.text, previous);
  }
}

class Term implements Node {
  constructor(private readonly resolver: (context: DataContext) => ResolvedValue | Resolvable | undefined,
              private readonly prefix: string | undefined = undefined,
              private readonly suffix: string | undefined = undefined) {
  }

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
    .add([integer], token => parseInt(token))
    .add([decimal], token => parseFloat(token))
    .add('(', token => token)
    .add(')', token => token)
    .add(';', token => token)
    .add(literal('"', '"', '\\"'), quote => new Term(() => ResolvedValue.of(quote.slice(1, -1)), "\"", "\""))
    .add(literal("'", "'", "\\'"), quote => new Term(() => ResolvedValue.of(quote.slice(1, -1)), "'", "'"))
  }

  operator(symbol: string, precedence: number, associativity: Associativity, operands: number, fn: OperandFunction<ResolvedValue>) {
    this.parser.add(symbol, _ => new Operator(symbol, precedence, associativity, operands, fn));
    return this;
  }

  biOperator(symbol: string,
             unaryPrecedence: number, unaryAssociativity: Associativity, unaryFn: OneOperandFunction<ResolvedValue>,
             binaryPrecedence: number, binaryAssociativity: Associativity, binaryFn: TwoOperandFunction<ResolvedValue>) {
    this.parser.add(symbol, _ => new BiOperator(
        new Operator(symbol, unaryPrecedence, unaryAssociativity, 1, unaryFn),
        new Operator(symbol, binaryPrecedence, binaryAssociativity, 2, binaryFn)
    ));
    return this;
  }

  intOperator(symbol: string, precedence: number, associativity: Associativity, operands: number, fn: OperandFunction<number>) {
    return this.operator(symbol, precedence, associativity, operands, mapIntFunction(operands, fn));
  }

  function(name: string, operands: number, fn: OperandFunction<ResolvedValue>) {
    this.parser.add(name, _ => new Function(name, operands, fn));
    return this;
  }

  modifier(name: string, mod: Modifier) {
    this.parser.add(name, _ => mod);
    return this;
  }

  varargsFunction(name: string, fn: VarargsOperandFunction<ResolvedValue>) {
    this.parser.add(name, _ => new VarargsFunction(name, fn));
    return this;
  }

  intFunction(name: string, operands: number, fn: OperandFunction<number>) {
    return this.function(name, operands, mapIntFunction(operands, fn));
  }

  variable(prefix: string, suffix: string, extractor: (context: DataContext, key: string) => ResolvedValue | Resolvable) {
    this.parser.add([term(prefix), alpha, optional(key), term(suffix)],
        key => new Variable(key, (context: DataContext, key: string) =>
            extractor(context, key.substring(prefix.length, key.length - suffix.length))));
    return this;
  }

  comment(prefix: string, suffix: string, decorator: (text: string, value: ResolvedValue) => ResolvedValue = (text, value) => value) {
    this.parser.add(literal(prefix, suffix),
        key => new Comment(key.substring(prefix.length, key.length - suffix.length), decorator));
    return this;
  }

  term(text: string, extractor: (context: DataContext) => ResolvedValue | Resolvable | undefined) {
    this.parser.add([term(text)],
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

    for (let i = 0; i < tokens.length; i++) {
      let token = tokens[i];
      let previous = i > 0 ? tokens[i - 1] : undefined;

      if (token instanceof BiOperator) {
        let operator = token;
        token = operator.binary;
        if (!previous || previous instanceof Operator || previous === '(' || previous === ';') {
          token = operator.unary;
        }
      }

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

      if (token instanceof Modifier) {
        operatorStack.push(token);
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

      if (token instanceof Term) {
        outputBuffer.push(token);
        continue;
      }

      if (token instanceof Comment) {
        outputBuffer.push(token);
        continue;
      }

      switch (token) {
        case ' ':
        case '{':
        case '}':
          // ignore
          break;
        case ';':
          arityStack[arityStack.length - 1]++;
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
          } else if (operatorStack[operatorStack.length-1] instanceof VarargsFunction) {
            let arity = arityStack.pop();
            arity = previous === '(' ? 0 : arity;
            outputBuffer.push(arity);
            outputBuffer.push(operatorStack.pop());
          } else if (operatorStack[operatorStack.length-1] instanceof Modifier) {
            outputBuffer.push(operatorStack.pop() as Modifier);
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

type OperatorStack = Array<Node | string | number | null | undefined>;

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

  resolve(context: DataContext = DataContext.Empty): ResolvedValue | undefined {
    let localStack: OperatorStack = [];
    let stack: OperatorStack = [...this.stack];
    let modifierStack: Modifier[] = [NoModifier.instance];
    while (stack.length > 0) {
      let next = stack.shift();

      if (next instanceof Modifier) {
        modifierStack.push(next);
        continue;
      }

      if (next instanceof OperatorFunction) {
        let func = next;
        if (func.operands === 0) {
          localStack.push(func.execute(modifierStack[modifierStack.length-1]));
        } else if (func.operands === 1) {
          let x = localStack.pop() as ResolvedValue;
          if (x === undefined) throw new ResolveError(`Missing parameter #1 for "${func.name}"`);
          localStack.push(func.execute(modifierStack[modifierStack.length-1], x));
        } else if (func.operands === 2) {
          let b = localStack.pop() as ResolvedValue;
          let a = localStack.pop() as ResolvedValue;
          if (b === undefined) throw new ResolveError(`Missing parameter #1 for "${func.name}"`)
          if (a === undefined) throw new ResolveError(`Missing parameter #2 for "${func.name}"`)
          localStack.push(func.execute(modifierStack[modifierStack.length-1], a, b));
        } else if (func.operands === 3) {
          let c = localStack.pop() as ResolvedValue;
          let b = localStack.pop() as ResolvedValue;
          let a = localStack.pop() as ResolvedValue;
          if (c === undefined) throw new ResolveError(`Missing parameter #1 for "${func.name}"`)
          if (b === undefined) throw new ResolveError(`Missing parameter #2 for "${func.name}"`)
          if (a === undefined) throw new ResolveError(`Missing parameter #3 for "${func.name}"`)
          localStack.push(func.execute(modifierStack[modifierStack.length-1], a, b, c));
        } else {
          throw new Error("Unsupported number of operands: " + func.operands);
        }
        continue;
      }

      if (next instanceof VarargsFunction) {
        let func = next;
        let params: ResolvedValue[] = [];
        let paramCount = localStack.pop() as number;
        while (paramCount-- > 0) {
          params.unshift(localStack.pop() as ResolvedValue);
        }
        const modifier = modifierStack[modifierStack.length-1];
        localStack.push(func.execute(modifier, params));
        continue;
      }

      if (next instanceof Comment) {
        const previous = localStack.pop();
        localStack.push(next.apply(previous as ResolvedValue));
        continue;
      }

      if (next instanceof Term) {
        next = next.resolve(context);
      }

      if (next instanceof Variable) {
        next = next.resolve(context);
        if (next instanceof ShuntingYard) {
          const nextStack = next.stack;
          for (let i = nextStack.length - 1; i >= 0; i--) {
            stack.unshift(nextStack[i]);
          }
          continue;
        }
      }

      while (next instanceof Resolvable) {
        next = next.resolve(context);
      }

      localStack.push(next);
    }

    return modifierStack[modifierStack.length-1].apply(localStack.pop() as ResolvedValue);
  }
}