import {Resolvable} from "./Resolvable";
import {ResolvedValue} from "./ResolvedValue";
import {ResolvedValueWithId} from "./ResolvedValueWithId";

export type DataContextState = { [key:string]:string|number|boolean|Resolvable; };

export interface DataContext {
  get(key: string): Resolvable|undefined;
  resolve(key: string): ResolvedValue | undefined;
  keys(): string[];
  search(pattern: string): ResolvedValueWithId[];
}

export interface ImmutableDataContext extends DataContext {
  replace(key: string, value: string|number|boolean): ImmutableDataContext;
}

export class ImmutableDataContext {
  static of(state: DataContextState): ImmutableDataContext {
    return new StaticImmutableDataContext(state);
  }
}

export interface MutableDataContext extends DataContext {
  set(key: string, value: string|number|boolean|Resolvable): void;
  remove(key: string): void;
  rename(key: string, to: string): void;
}

class EmptyDataContext implements DataContext {

  get(key: string): Resolvable | undefined {
    return undefined;
  }

  resolve(key: string): ResolvedValue | undefined {
    return undefined;
  }

  keys(): string[] {
    return [];
  }

  search(pattern: string): ResolvedValueWithId[] {
    return [];
  }
}

export class DataContext {
  public static Empty: DataContext = new EmptyDataContext();

  static of(state: DataContextState): MutableDataContext {
    return new StaticDataContext(state);
  }
}

export abstract class BaseDataContext implements DataContext {

  public resolve(key: string): ResolvedValue|undefined {
    return this.get(key)?.resolve(this);
  }

  public search(pattern: string): ResolvedValueWithId[] {
    return DataContextUtils.find(this, pattern);
  }

  abstract get(key: string): Resolvable | undefined;

  abstract keys(): string[];
}

class DataContextUtils {

  static find(context: DataContext, pattern: string): ResolvedValueWithId[] {
    const regex = new RegExp(this.escapeRegExp(pattern).replace(/\\\*/g, ".*?"));
    return context.keys()
    .filter((key: string) => regex.test(key))
    .map(key => {
      const value = context.resolve(key);
      if (value === undefined) {
        return undefined;
      }
      return new ResolvedValueWithId(key, value);
    })
    .filter(value => value !== undefined)
    .map(value => value as ResolvedValueWithId);
  }

  private static escapeRegExp(expression: string) {
    return expression.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
  }
}

class StaticDataContext extends BaseDataContext implements MutableDataContext {
  constructor(private readonly state: DataContextState) {
    super();
  }

  get(key: string): Resolvable | undefined {
    const result: string|number|boolean|Resolvable|undefined = this.state[key];
    if (result instanceof Resolvable) {
      return result;
    }
    return Resolvable.just(result);
  }

  resolve(key: string): ResolvedValue|undefined {
    const result: string|number|boolean|Resolvable|undefined = this.state[key];
    if (result instanceof Resolvable) {
      return result.resolve(this);
    }
    return ResolvedValue.of(result);
  }

  set(key: string, value: string|number|boolean|Resolvable): void {
    this.state[key] = value;
  }

  remove(key: string): void {
    delete this.state[key];
  }

  keys(): string[] {
    return Object.keys(this.state);
  }

  rename(key: string, to: string): void {
    if (key in this.state) {
      this.state[to] = this.state[key];
      delete this.state[key];
    }
  }
}

export class StaticImmutableDataContext extends BaseDataContext implements ImmutableDataContext {
  constructor(private readonly state: DataContextState = {}) {
    super();
  }

  get(key: string): Resolvable | undefined {
    const result: string|number|boolean|Resolvable|undefined = this.state[key];
    if (result instanceof Resolvable) {
      return result;
    }
    return Resolvable.just(result);
  }

  resolve(key: string): ResolvedValue|undefined {
    const result: string|number|boolean|Resolvable|undefined = this.state[key];
    if (result instanceof Resolvable) {
      return result.resolve(this);
    }
    return ResolvedValue.of(result);
  }

  replace(key: string, value: string|number): ImmutableDataContext {
    return new StaticImmutableDataContext({
      ...this.state,
      [key]: value
    });
  }

  keys(): string[] {
    return Object.keys(this.state);
  }
}