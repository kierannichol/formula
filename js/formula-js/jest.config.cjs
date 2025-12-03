const nodeModulesToIgnoreTransform = [
]

module.exports = {
  clearMocks: true,

  globals: {
    window: {}
  },

  preset: "ts-jest",

  moduleDirectories: [
    'node_modules'
  ],

  moduleFileExtensions: ['ts', 'tsx', 'js', 'json', 'node', 'd.ts'],

  transform: {
    "node_modules/variables/.+\\.(j|t)sx?$": "ts-jest"
  },

  testEnvironment: 'jsdom',

  testMatch: [
    '**/*.test.ts?(x)',
  ],

  testPathIgnorePatterns: [
    '/node_modules/',
  ],

  transformIgnorePatterns: [
    'node_modules/(?!('+ nodeModulesToIgnoreTransform.join('|')+'))'
  ],
};