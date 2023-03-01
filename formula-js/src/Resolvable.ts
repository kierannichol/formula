import {DataContext} from "./DataContext";
import {ResolvedValue} from "./ResolvedValue";

export abstract class Resolvable {

  static just(value: string|number|boolean|ResolvedValue): Resolvable {
    if (value instanceof ResolvedValue) {
      return new Resolvable.StaticResolvable(value);
    }
    return new Resolvable.StaticResolvable(ResolvedValue.of(value));
  }

  abstract asFormula(): string;
  abstract resolve(context: DataContext): ResolvedValue;

  private static StaticResolvable = class extends Resolvable {
    constructor(private readonly resolved: ResolvedValue) {
      super();
    }

    resolve(context?: DataContext): ResolvedValue | undefined {
      return this.resolved;
    }

    asFormula(): string {
      return this.resolved.asText();
    }
  }

  static readonly True: Resolvable = new Resolvable.StaticResolvable(ResolvedValue.True);
  static readonly False: Resolvable = new Resolvable.StaticResolvable(ResolvedValue.False);
}