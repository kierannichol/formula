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


test('+1 keen vs +2', () => {
  testAttack(25, 2 - 3, 2 + 9, 18);
  testAttack(25, 1 - 3, 1 + 9 + 3.5, 18);
});

function testAttack(targetAC, attackBonus, damageBonus, critRange) {
  const strengthMod = 6;
  const bab = 8;

  const critMultiplier = 2;

  const attackMod = strengthMod + bab;
  const damageMod = strengthMod * 1.5;

  const attempts = 100000;
  let attacks = 0;
  let rounds = 0;
  let hitCount = 0.0;
  let damageSum = 0.0;
  let critCount = 0;
  for (let i = 0; i < attempts; i++) {
    const attackResult = attack(targetAC,
        attackBonus + attackMod,
        damageBonus + damageMod,
        critRange,
        critMultiplier);
    hitCount += attackResult.hit ? 1.0 : 0.0;
    damageSum += attackResult.damage;
    critCount += attackResult.crit ? 1 : 0;
    attacks++;

    const secondAttackResult = attack(targetAC,
        attackBonus + attackMod - 5,
        damageBonus + damageMod,
        critRange,
        critMultiplier);
    hitCount += secondAttackResult.hit ? 1.0 : 0.0;
    damageSum += secondAttackResult.damage;
    critCount += secondAttackResult.crit ? 1 : 0;
    attacks++;

    rounds++;
  }

  const averageHit = hitCount / attacks;
  const averageCrits = critCount / attacks;
  const averageDamagePerHit = damageSum / hitCount;
  const totalAverageDamage = damageSum / rounds;

  console.log(`Average Hits: ${averageHit * 100}%\nAverage Crits: ${averageCrits * 100}%\nAverage Damage per Hit: ${averageDamagePerHit}\nTotal average damage: ${totalAverageDamage}`)
}

function roll(dice, sides) {
  let total = 0;
  for (let i = 0; i < dice; i++) {
    total += Math.floor(Math.random() * sides) + 1;
  }
  return total;
}

function attack(targetAC, attackBonus, damageBonus, critRange, critMultiplier) {
  const attackRoll = roll(1, 20);
  const confirmRoll = roll(1, 20);

  const attackAmount = attackRoll + attackBonus;
  const confirmAmount = confirmRoll + attackBonus;

  const isHit = (attackRoll === 20 || attackAmount >= targetAC) && attackRoll !== 1;
  const isCrit = isHit && attackRoll >= critRange;
  const isCritConfirm = isCrit && confirmRoll !== 1 && (confirmAmount >= targetAC || confirmRoll === 20);

  const damageMultiplier = isHit
      ? isCritConfirm
            ? critMultiplier
            : 1
      : 0;

  let damageAmount = 0;

  for (let i = 0; i < damageMultiplier; i++) {
    damageAmount += roll(2, 4) + damageBonus;
  }

  return {
    hit: isHit,
    damage: damageAmount,
    crit: isCritConfirm,
  };
}