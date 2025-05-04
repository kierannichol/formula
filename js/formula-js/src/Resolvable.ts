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
  abstract resolve(context?: DataContext): ResolvedValue|undefined;

  map(fn: (resolved: ResolvedValue) => ResolvedValue): Resolvable {
    return new Resolvable.MappedResolvable(this, fn);
  }

  private static StaticResolvable = class extends Resolvable {
    constructor(private readonly resolved: ResolvedValue) {
      super();
    }

    resolve(context?: DataContext): ResolvedValue {
      return this.resolved;
    }

    asFormula(): string {
      return this.resolved.asText();
    }
  }

  private static MappedResolvable = class extends Resolvable {
    constructor(private readonly resolvable: Resolvable,
                private readonly mapFn: (resolved: ResolvedValue) => ResolvedValue) {
      super();
    }

    resolve(context?: DataContext): ResolvedValue {
      return this.mapFn(this.resolvable.resolve(context));
    }

    asFormula(): string {
      return this.resolvable.asFormula();
    }
  }

  static readonly True: Resolvable = new Resolvable.StaticResolvable(ResolvedValue.True);
  static readonly False: Resolvable = new Resolvable.StaticResolvable(ResolvedValue.False);
  static readonly None: Resolvable = new Resolvable.StaticResolvable(ResolvedValue.None);
}