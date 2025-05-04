import {Formula} from "./Formula";
import * as fs from "node:fs";
import * as path from "node:path";
import {DataContext, MutableDataContext} from "./DataContext";
import {Resolvable} from "./Resolvable";
import {ResolvedValue} from "./ResolvedValue";

const YAML = require("yaml");

const dataContextTestCasesPath = path.resolve(__dirname, '..', '..', 'formula-test', 'data-context-test-cases.yml');
const dataContextTestCasesYaml = fs.readFileSync(dataContextTestCasesPath, "utf8");

const dataContextTestCases = YAML.parse(dataContextTestCasesYaml);

dataContextTestCases.forEach(testCase => {

  test(testCase.name, () => {
    const context = DataContext.of({});
    for (let actionText of testCase.actions) {
      const action = parseDataContextAction(actionText);
      action(context);
    }

    Object.keys(testCase.expected).forEach(key => {
      const expectedForKey = testCase.expected[key];
      const actualValue = context.get(key);
      expectValues(actualValue, expectedForKey);
    })
  });
});

function expectValues(actualValue: ResolvedValue, expectedForKey: any) {
  if ('expected_text' in expectedForKey) {
    expect(actualValue.asText()).toEqual(expectedForKey['expected_text']);
  }
  if ('expected_number' in expectedForKey) {
    expect(actualValue.asNumber()).toEqual(expectedForKey['expected_number']);
  }
  if ('expected_boolean' in expectedForKey) {
    expect(actualValue.asBoolean()).toEqual(expectedForKey['expected_boolean']);
  }
  if ('expected_list' in expectedForKey) {
    for (let i = 0; i < expectedForKey['expected_list'].length; i++) {
      expectValues(actualValue.asList()[i], expectedForKey['expected_list'][i]);
    }
  }
}

function toDataContext(data: any): DataContext {
  if (!data) return undefined;
  const parsed = {};
  Object.keys(data).forEach(key => {
    parsed[key] = parseDataValue(data[key]);
  });
  return DataContext.of(parsed);
}

function parseDataContextAction(text: string): (context: MutableDataContext) => any {
  return (context: MutableDataContext) => {
    const parts = text.split(' ');
    const action = parts[0].toUpperCase();
    const key = parts[1];
    const value = parts[2];

    switch (action) {
      case "SET":
        context.set(key, parseDataValue(value));
        break;
      case "PUSH":
        context.push(key, Resolvable.just(value));
        break;
    }
  };
}

function parseDataValue(value: any): string|number|boolean|ResolvedValue[]|Resolvable {
  if (typeof value === "string"
      && value.startsWith("{")
      && value.endsWith("}")) {
    return Formula.parse(value.slice(1, -1));
  }
  if (Array.isArray(value)) {
    return value.map(ResolvedValue.of);
  }
  return value;
}