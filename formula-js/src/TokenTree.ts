import {AlphaCharacters, DigitCharacters, WordCharacters} from "./CharClasses";

export class ParseError extends Error {
  constructor(message:string,
              public readonly text: string,
              public readonly index: number) {
    super(message);
  }
}

export type TokenMapper = (token: string) => any;

export default class TokenTree {
  private readonly root: RootNode = new RootNode();

  ignoreWhitespaces(): TokenTree {
    return this.add(' ', _ => undefined);
  }

  add(matchers: string|NodeFactory|NodeExpression|Array<NodeFactory|NodeExpression>, mapper: TokenMapper): TokenTree {
    if (typeof matchers !== 'string') {
      matchers = asArray(matchers);
      return this.addBranch(matchers, mapper);
    }

    return this.addBranch([term(matchers)], mapper);
  }

  private addBranch(nodesOrExpressionsToAdd: Array<NodeFactory|NodeExpression>, mapper: TokenMapper): TokenTree {
    BasicNodeExpression.of(nodesOrExpressionsToAdd).chainTo(this.root, mapper);
    return this;
  }

  parse(text: string): any[] {
    const tokens: string[] = [];
    for (let i = 0; i < text.length; i++) {
      const matches = this.root.walk(text, i);
      if (matches.length > 0) {
        const match = matches[0];
        const token = match.get();
        if (token !== undefined) {
          tokens.push(token);
        }
        i = match.endIndex - 1;
      } else {
        throw new ParseError(TokenTree.generateParseErrorMessage(i, text, `did not expect character: '${text[i]}'`), text, i);
      }
    }
    return tokens;
  }

  private static generateParseErrorMessage(index: number, text: string, message: string) {
    let fullMessage = `Parse error at index ${index} of "${text}": ${message}`;
    fullMessage += `
${text}
${' '.repeat(index)}^`;
    return fullMessage;
  }
}

class TokenMatch {
  constructor(
      public readonly text: string,
      public readonly startIndex: number,
      public readonly endIndex: number,
      public readonly mapper: TokenMapper) {
  }

  get() {
    return this.mapper(this.text
        .substring(this.startIndex, this.endIndex));
  }
}

// NODE FACTORY ///////////////////////////////////////////////////////////////

abstract class NodeFactory {

  static match(matcher: TokenMatcher): MatcherNodeFactory {
    return new MatcherNodeFactory(matcher);
  }

  abstract node(): Node;
}

class MatcherNodeFactory extends NodeFactory {
  constructor(private readonly matcher: TokenMatcher) {
    super();
  }

  node(): Node {
    return new MatcherNode(this.matcher);
  }

  repeats(minLength: number = 0, maxLength: number|undefined = undefined): RepeatingNodeFactory {
    return new RepeatingNodeFactory(this.matcher, minLength, maxLength);
  }

  optional() {
    return this.repeats(0, 1);
  }

  not() {
    return new MatcherNodeFactory(new NotTokenMatcher(this.matcher));
  }
}

class AnyMatcherNodeFactory extends NodeFactory {
  private readonly matcher: TokenMatcher;

  constructor() {
    super();
    this.matcher = new AnyTokenMatcher();
  }

  node(): Node {
    return new MatcherNode(this.matcher);
  }

  except(...notAllowed: string[]) {
    return new MatcherNodeFactory(new NotTokenMatcher(new AnyOfTokenMatcher(notAllowed)));
  }

  repeats(minLength: number = 0, maxLength: number|undefined = undefined): RepeatingNodeFactory {
    return new RepeatingNodeFactory(this.matcher, minLength, maxLength);
  }

  optional() {
    return this.repeats(0, 1);
  }

  not() {
    return new MatcherNodeFactory(new NotTokenMatcher(this.matcher));
  }
}

class RepeatingNodeFactory extends NodeFactory {
  constructor(private readonly matcher: TokenMatcher,
              private readonly minLength: number,
              private readonly maxLength: number|undefined) {
    super();
  }

  node(): Node {
    return new RepeatingNode(this.matcher, this.minLength, this.maxLength);
  }
}
class AnyUntilNodeFactory extends NodeFactory {
  constructor(private readonly closeSequence: string,
              private readonly escapeSequence: string|undefined) {
    super();
  }

  node(): Node {
    return new AnyUntilNode(this.closeSequence, this.escapeSequence);
  }
}

// NODE EXPRESSION ////////////////////////////////////////////////////////////

export interface NodeExpression {
  chainTo(root: Node, mapper: TokenMapper, isRoot: boolean): Node;
}

class BasicNodeExpression implements NodeExpression {
  static of(nodesOrExpressions: Array<NodeFactory|NodeExpression>) {
    return new BasicNodeExpression(nodesOrExpressions);
  }

  private constructor(public readonly expressions: Array<NodeFactory|NodeExpression>) {}

  chainTo(root: Node, mapper: TokenMapper, isRoot: boolean = true): Node {
    let node: Node = root;
    for (let toAdd of this.expressions) {
      if (toAdd instanceof NodeFactory) {
        node = node.add(toAdd.node());
      } else {
        node = toAdd.chainTo(node, mapper, false);
      }
    }
    if (isRoot) {
      let matcherNode = node as MatcherNode;
      if (matcherNode.mapper !== undefined) {
        throw new Error("Conflicting tokens");
      }
      matcherNode.mapper = mapper;
    }
    return node;
  }
}

class OptionalNodeExpression implements NodeExpression {
  static of(nodesOrExpressions: Array<Node|NodeExpression>) {
    return new OptionalNodeExpression(nodesOrExpressions);
  }

  private constructor(public readonly expressions: Array<Node|NodeExpression>) {}

  chainTo(root: Node, mapper: TokenMapper, isRoot: boolean = true): Node {
    let node: Node = root;
    (node as MatcherNode).mapper = mapper;
    for (let toAdd of this.expressions) {
      if (toAdd instanceof Node) {
        node = node.add(toAdd);
      } else {
        node = toAdd.chainTo(node, mapper, false);
      }
    }

    if (isRoot) {
      let matcherNode = node as MatcherNode;
      if (matcherNode.mapper !== undefined) {
        throw new Error("Conflicting tokens");
      }
      matcherNode.mapper = mapper;
    }
    return node;
  }
}

// NODES //////////////////////////////////////////////////////////////////////

export abstract class Node {
  static idCounter: number = 1;
  public id: number = Node.idCounter++;

  protected children: Node[]|undefined = undefined;

  add(child: Node): Node {
    if (this.children === undefined) {
      this.children = [];
    }
    for (let existingChild of this.children) {
      if (existingChild.equals(child)) {
        return existingChild;
      }
    }
    this.children.push(child);
    return child;
  }

  abstract walk(text: string, startIndex: number, currentIndex: number): TokenMatch[];

  protected walkChildren(text: string, startIndex: number, currentIndex: number): TokenMatch[] {
    if (this.children === undefined) {
      return [];
    }
    return this.children.flatMap(child =>
        child.walk(text, startIndex, currentIndex));
  }

  abstract equals(node: Node): boolean;
}

class RootNode extends Node {

  walk(text: string, startIndex: number = 0, currentIndex:number = startIndex): TokenMatch[] {
    return this.walkChildren(text, startIndex, currentIndex);
  }

  equals(node: Node): boolean {
    return false;
  }
}

class MatcherNode extends Node {
  public mapper: TokenMapper|undefined = undefined;

  constructor(private readonly matcher: TokenMatcher) {
    super();
  }

  walk(text: string, startIndex: number, currentIndex: number): TokenMatch[] {
    if (!this.matches(text, startIndex, currentIndex)) {
      return [];
    }

    const matches = this.walkChildren(text, startIndex, currentIndex+1);
    return matches.length > 0
        ? matches
        : this.mapper !== undefined
            ? [ new TokenMatch(text, startIndex, currentIndex + 1, this.mapper) ]
            : [];
  }

  protected matches(text: string, startIndex: number, currentIndex: number): boolean {
    const current = text[currentIndex];
    if (current === undefined) {
      return false;
    }
    return this.matcher.matches(current);
  }

  equals(node: Node): boolean {
    return node instanceof MatcherNode
        && this.matcher.equals(node.matcher);
  }
}

class RepeatingNode extends Node {
  private mapper: TokenMapper|undefined = undefined;

  constructor(
      private readonly matcher: TokenMatcher,
      private readonly minLength: number,
      private readonly maxLength: number|undefined
  ) {
    super();
  }

  walk(text: string, startIndex: number, currentIndex: number): TokenMatch[] {
    const minLength = this.minLength;
    const maxLength = this.calculateMaxLength(text, currentIndex);
    let length = 0;
    for (let next = text[currentIndex]; next !== undefined && this.matcher.matches(next) && length < maxLength; next = text[currentIndex]) {
      currentIndex++;
      length++;
    }

    if (length < minLength) {
      return [];
    }

    const matches = this.walkChildren(text, startIndex, currentIndex);
    if (length > maxLength) {
      return matches;
    }

    return matches.length > 0
        ? matches
        : this.mapper !== undefined
            ? [ new TokenMatch(text, startIndex, currentIndex, this.mapper) ]
            : [];
  }

  equals(node: Node): boolean {
    return node instanceof RepeatingNode
        && this.matcher.equals(node.matcher)
        && this.minLength === node.minLength
        && this.maxLength === node.maxLength;
  }

  private calculateMaxLength(text: string, currentIndex: number) {
    const maxTextLength = text.length - currentIndex;
    return this.maxLength !== undefined
        ? Math.min(this.maxLength, maxTextLength)
        : maxTextLength;
  }
}

class AnyUntilNode extends Node {
  public mapper: TokenMapper|undefined = undefined;

  constructor(private readonly closeSequence: string,
              private readonly escapeSequence: string|undefined) {
    super();
  }

  walk(text: string, startIndex: number, currentIndex: number): TokenMatch[] {

    for (; currentIndex < text.length; currentIndex++) {
      for (let i = 0; i < this.closeSequence.length; i++) {
        if (text[currentIndex + i] !== this.closeSequence[i]) {
          break;
        }

        if (this.isEscaped(text, currentIndex)) {
          break;
        }

        if (i === this.closeSequence.length - 1) {
          const matches = this.walkChildren(text, startIndex, currentIndex);
          return matches.length > 0
              ? matches
              : this.mapper !== undefined && currentIndex > startIndex
                  ? [new TokenMatch(text, startIndex, currentIndex, this.mapper)]
                  : [];
        }
      }
    }

    return this.mapper !== undefined && currentIndex === text.length
        ? [ new TokenMatch(text, startIndex, currentIndex, this.mapper) ]
        : [];
  }

  private isEscaped(text: string, index: number): boolean {
    if (this.escapeSequence === undefined) {
      return false;
    }
    for (let i = index - this.escapeSequence.length + 1, k = 0; k < this.escapeSequence.length; i++, k++) {
      if (i < 0 && i >= index) {
        return false;
      }
      if (text[i] !== this.escapeSequence[k]) {
        return false;
      }
    }
    return true;
  }

  equals(node: Node): boolean {
    return node instanceof AnyUntilNode
        && this.closeSequence === node.closeSequence;
  }
}

// TOKEN MATCHERS /////////////////////////////////////////////////////////////

interface TokenMatcher {
  matches(character: string): boolean;
  equals(other: TokenMatcher): boolean;
}

class CharacterTokenMatcher implements TokenMatcher {
  constructor(private readonly character: string) {
  }

  matches(character: string): boolean {
    return this.character === character;
  }

  equals(other: TokenMatcher): boolean {
    return other instanceof CharacterTokenMatcher
        && this.character === other.character;
  }
}

class AnyOfTokenMatcher implements TokenMatcher {
  constructor(private readonly allowed: string[]) {
  }

  matches(character: string): boolean {
    return this.allowed.includes(character);
  }

  equals(other: TokenMatcher): boolean {
    return other instanceof AnyOfTokenMatcher
        && shallowEquals(this.allowed, other.allowed);
  }
}

class AnyTokenMatcher implements TokenMatcher {
  matches(character: string): boolean {
    return character !== undefined;
  }

  equals(other: TokenMatcher): boolean {
    return other instanceof AnyOfTokenMatcher;
  }
}

class NotTokenMatcher implements TokenMatcher {
  constructor(private readonly matcher: TokenMatcher) {
  }

  matches(character: string): boolean {
    return !this.matcher.matches(character);
  }

  equals(other: TokenMatcher): boolean {
    return other instanceof NotTokenMatcher
        && this.matcher.equals(other.matcher);
  }
}

function shallowEquals(a: string[], b: string[]): boolean {
  if (a.length !== b.length) {
    return false;
  }
  for (let i = 0; i < a.length; i++) {
    if (a[i] !== b[i]) {
      return false;
    }
  }
  return true;
}

function asArray<T>(obj: T|T[]): T[] {
  return Array.isArray(obj) ? obj : [ obj ];
}

// Matchers

export function anyof(allowed: string|string[]): MatcherNodeFactory {
  if (Array.isArray(allowed) && allowed.length === 1) {
    allowed = allowed[0];
  }
  return NodeFactory.match(Array.isArray(allowed)
      ? new AnyOfTokenMatcher(allowed)
      : new CharacterTokenMatcher(allowed));
}

export function term(text: string): NodeExpression {
  return BasicNodeExpression.of(Array.from(text).map(character =>
      anyof(character)));
}

export function just(...expressions: Array<string|NodeFactory|NodeExpression>): NodeExpression {
  return BasicNodeExpression.of(expressions.map(expression => typeof expression === 'string' ? term(expression) : expression));
}

export function optional(...expressions: Array<string|NodeFactory|NodeExpression>): NodeExpression {
  return OptionalNodeExpression.of([just(...expressions)]);
}

export function literal(openSequence: string, closeSequence: string, escapeSequence: string|undefined = undefined): NodeExpression {
  return just(term(openSequence), anyUntil(closeSequence, escapeSequence), term(closeSequence));
}

export function anyUntil(closeSequence: string, escapeSequence: string|undefined = undefined): NodeExpression {
  return just(new AnyUntilNodeFactory(closeSequence, escapeSequence));
}

export const any: AnyMatcherNodeFactory = new AnyMatcherNodeFactory();
export const digit: MatcherNodeFactory = anyof(DigitCharacters);
export const digits: NodeExpression = just(digit.repeats(1));
export const words: NodeExpression = just(anyof(WordCharacters).repeats(1));
export const key: NodeExpression = just(anyof([...WordCharacters, ':', '.', '#', '*']).repeats(1));
export const integer: NodeExpression = just(anyof('-').repeats(0,1), digits);
export const decimal: NodeExpression = just(integer, '.', digits);
export const number: NodeExpression = just(integer, optional('.', digits) );
export const alpha: MatcherNodeFactory = anyof(AlphaCharacters);