export const SpaceCharacters = [ ' ' ];
export const BlankCharacters = [
  ...SpaceCharacters,
  '\t'
];

export const DigitCharacters = [ '1', '2', '3', '4', '5' , '6', '7', '8', '9', '0' ];
export const NumericCharacters = [ ...DigitCharacters, '.' ];
export const HexDigitCharacters = [
  ...DigitCharacters,
  'a', 'b', 'c', 'd', 'e', 'f',
  'A', 'B', 'C', 'D', 'E', 'F'
];

export const LowerCaseCharacters = [ 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z' ];
export const UpperCaseCharacters = [ 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z' ];

export const AlphaCharacters = [
  ...LowerCaseCharacters,
  ...UpperCaseCharacters
];

export const AlphaNumericCharacters = [
  ...AlphaCharacters,
  ...DigitCharacters
];

export const WordCharacters = [
  ...AlphaNumericCharacters,
  '_'
];