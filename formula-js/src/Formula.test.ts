import {Formula} from "./Formula";
import * as fs from "node:fs";
import * as path from "node:path";
import {DataContext} from "./DataContext";
import {Resolvable} from "./Resolvable";

const YAML = require("yaml");

const formulaTestCasesPath = path.resolve(__dirname, '..', '..', 'formula-test', 'formula-test-cases.yml');
const formulaTestCasesYaml = fs.readFileSync(formulaTestCasesPath, "utf8");

const formulaTestCases = YAML.parse(formulaTestCasesYaml);

formulaTestCases.forEach(testCase => {

  test(testCase.name, () => {
    try {
      let formula = Formula.parse(testCase.formula);
      let resolved = formula.resolve(toDataContext(testCase.data));

      if (testCase.expected_number) {
        const expected = (typeof testCase.expected_number === 'string'
            && testCase.expected_number.toLowerCase() === "nan")
            ? NaN
            : testCase.expected_number;
        expect(resolved?.asNumber()).toBe(expected);
      }
      if (testCase.expected_text)
        expect(resolved?.asText()).toBe(testCase.expected_text);
      if (testCase.expected_boolean)
        expect(resolved?.asBoolean()).toBe(testCase.expected_boolean);
    } catch (e) {
      if (testCase.expected_error) {
        expect(e.message).toBe(testCase.expected_error);
        return;
      }
      throw e;
    }
  });
});

function toDataContext(data: any): DataContext {
  if (!data) return undefined;
  const parsed = {};
  Object.keys(data).forEach(key => {
    parsed[key] = parseDataValue(data[key]);
  });
  return DataContext.of(parsed);
}

function parseDataValue(value: any): string|number|boolean|Resolvable {
  if (typeof value === "string"
      && value.startsWith("{")
      && value.endsWith("}")) {
    return Formula.parse(value.slice(1, -1));
  }
  return value;
}