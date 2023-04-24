import {Resolvable} from "./Resolvable";
import {ResolvedValue} from "./ResolvedValue";

export type DataContextState = { [key:string]:string|number|boolean|Resolvable; };

export interface DataContext {
  get(key: string): Resolvable|undefined;
  resolve(key: string): ResolvedValue|undefined;
  find(pattern: string): ResolvedValue[];
  keys(): string[];
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

  resolve(key: string): undefined {
    return undefined;
  }

  keys(): string[] {
    return [];
  }

  find(pattern: string): ResolvedValue[] {
    return [];
  }

}

export class DataContext {
  public static Empty: DataContext = new EmptyDataContext();

  static of(state: DataContextState): MutableDataContext {
    return new StaticDataContext(state);
  }
}

class StaticDataContext implements MutableDataContext {
  constructor(private readonly state: DataContextState) {
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

  find(pattern: string): ResolvedValue[] {
    const regex = new RegExp(this.escapeRegExp(pattern).replace(/\\\*/g, ".*?"));
    return this.keys()
        .filter((key: string) => regex.test(key))
        .map(key => this.resolve(key))
        .filter(value => value !== undefined)
        .map(value => value as ResolvedValue);
  }

  private escapeRegExp(expression: string) {
    return expression.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
  }

  rename(key: string, to: string): void {
    if (key in this.state) {
      this.state[to] = this.state[key];
      delete this.state[key];
    }
  }
}

export class StaticImmutableDataContext implements ImmutableDataContext {
  constructor(private readonly state: DataContextState = {}) {
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

  find(pattern: string): ResolvedValue[] {
    const regex = new RegExp(pattern.replace(/\\\*/, ".*?"));
    return this.keys()
        .filter((key: string) => regex.test(key))
        .map(key => this.resolve(key))
        .filter(value => value !== undefined)
        .map(value => value as ResolvedValue);
  }
}