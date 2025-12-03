import {FormulaOptimizer} from "./FormulaOptimizer";
import {Resolvable} from "./Resolvable";
import {FormulaParser} from "./FormulaParser";

export class Formula {
  private static Parser = FormulaParser.createDefault();

  static parse(formula: string | Resolvable): Resolvable {
    if (formula instanceof Resolvable) {
      return formula;
    }
    return this.Parser.parse(formula);
  }

  static optimize(formula: string): string {
    return FormulaOptimizer.optimize(formula);
  }
}