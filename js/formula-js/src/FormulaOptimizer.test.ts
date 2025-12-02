import {Formula} from "./Formula";
import path from "node:path";
import fs from "node:fs";

const YAML = require("yaml");

const optimizeTestCasesPath = path.resolve(__dirname, '..', '..', '..', 'formula-test', 'optimize-test-cases.yml');
const optimizeTestCasesYaml = fs.readFileSync(optimizeTestCasesPath, "utf8");

const optimizeTestCases = YAML.parse(optimizeTestCasesYaml);

optimizeTestCases.forEach(testCase => {

  test(testCase.name, () => {
    try {
      let formula = Formula.optimize(testCase.formula);
      expect(formula).toBe(testCase.expected_formula);
    } catch (e) {
      if (testCase.expected_error) {
        expect(e.message).toBe(testCase.expected_error);
        return;
      }
      throw e;
    }
  });
});