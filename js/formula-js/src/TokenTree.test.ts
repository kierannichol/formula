import TokenTree, {
  anyof,
  decimal,
  integer,
  literal,
  number, words
} from "./TokenTree";
import {
  AlphaCharacters,
  DigitCharacters,
  WordCharacters
} from "./CharClasses";

test('empty tree', () => {
  const tree = new TokenTree();
  expect(() => tree.parse('Test')).toThrowError();
});

test('single node', () => {
  const tree = new TokenTree()
      .add('A', token => token);
  expect(tree.parse('A')).toEqual(['A']);
});

test('simple chain', () => {
  const tree = new TokenTree()
  .add('ABC', token => token);
  expect(tree.parse('ABC')).toEqual(['ABC']);
  expect(() => tree.parse('A')).toThrowError();
  expect(() => tree.parse('AB')).toThrowError();
  expect(() => tree.parse('ABX')).toThrowError();
});

test('splitting chain', () => {
  const tree = new TokenTree()
  .add('ABC', token => token)
  .add('A23', token => token);
  expect(tree.parse('ABC')).toEqual(['ABC']);
  expect(tree.parse('A23')).toEqual(['A23']);
});

test('multiple tokens', () => {
  const tree = new TokenTree()
  .ignoreWhitespaces()
  .add('ABC', token => token)
  .add('123', token => token);
  expect(tree.parse('ABC 123')).toEqual(['ABC', '123']);
});

test('anyof()', () => {
  const tree = new TokenTree()
  .ignoreWhitespaces()
  .add(anyof(DigitCharacters), token => parseInt(token));
  expect(tree.parse('1 2 3')).toEqual([1, 2, 3]);
  expect(() => tree.parse('A')).toThrowError();
});

test('anyof() chain', () => {
  const tree = new TokenTree()
  .ignoreWhitespaces()
  .add([ anyof(DigitCharacters), anyof(DigitCharacters) ], token => parseInt(token));
  expect(tree.parse('13 25 36')).toEqual([13, 25, 36]);
  expect(() => tree.parse('4')).toThrowError();
});

test('repeated', () => {
  const tree = new TokenTree()
  .ignoreWhitespaces()
  .add([ anyof(DigitCharacters).repeats(1, 2) ], token => parseInt(token));
  expect(tree.parse('5')).toEqual([5]);
  expect(tree.parse('73')).toEqual([73]);
  expect(tree.parse('12 9 23')).toEqual([12, 9, 23]);
});

test('optional trailing character', () => {
  const tree = new TokenTree()
  .ignoreWhitespaces()
  .add([ anyof(AlphaCharacters).repeats(1), anyof(DigitCharacters).optional() ], token => token);
  expect(tree.parse('A5')).toEqual(['A5']);
  expect(tree.parse('ABC6')).toEqual(['ABC6']);
  expect(tree.parse('ABC')).toEqual(['ABC']);
  expect(() => tree.parse('5')).toThrowError();
  expect(() => tree.parse('ABC56')).toThrowError();
});

test('optional leading character', () => {
  const tree = new TokenTree()
  .ignoreWhitespaces()
  .add([ anyof('@').optional(), anyof(WordCharacters).repeats(1) ], token => token);
  expect(tree.parse('@A5')).toEqual(['@A5']);
  expect(tree.parse('A5')).toEqual(['A5']);
  expect(() => tree.parse('@')).toThrowError();
  expect(() => tree.parse('@@A5')).toThrowError();
});

test('number', () => {
  const tree = new TokenTree()
  .ignoreWhitespaces()
  .add([ number ], token => parseFloat(token));
  expect(tree.parse('1')).toEqual([1]);
  expect(tree.parse('23')).toEqual([23]);
  expect(tree.parse('54890')).toEqual([54890]);
  expect(tree.parse('3.14')).toEqual([3.14]);
  expect(() => tree.parse('A')).toThrowError();
  expect(() => tree.parse('5B')).toThrowError();

  expect(() => tree.parse('2.')).toThrowError();
  expect(() => tree.parse('.5')).toThrowError();
});

test('expression order', () => {
  const tree = new TokenTree()
  .ignoreWhitespaces()
  .add(integer, token => token)
  .add(decimal, token => parseFloat(token));
  expect(tree.parse('123 3.14 5 0.2')).toEqual(['123', 3.14, '5', 0.2]);
});

test('quoted token', () => {
  const tree = new TokenTree()
  .ignoreWhitespaces()
  .add(literal('"', '"'), quote => quote)
  .add(words, word => word);

  expect(tree.parse('one two "three four" five')).toEqual(['one', 'two', '"three four"', 'five']);
});

test('open/close tag token', () => {
  const tree = new TokenTree()
  .ignoreWhitespaces()
  .add(literal('<open>', '<close>'), quote => quote)
  .add(words, word => word);

  expect(tree.parse('one two <open>three four<close> five')).toEqual(['one', 'two', '<open>three four<close>', 'five']);
});