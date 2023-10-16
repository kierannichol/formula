export interface ResolvedValue {
  asText(): string;

  asNumber(): number;

  asBoolean(): boolean;

  hasValue(): boolean;

  equals(other: ResolvedValue): boolean;

  map(fn: (value: ResolvedValue) => ResolvedValue): ResolvedValue;
}

class TextValue implements ResolvedValue {
  constructor(private readonly value:string) {
  }

  asText(): string {
    return this.value;
  }

  asNumber(): number {
    return parseFloat(this.value);
  }

  asBoolean(): boolean {
    return !['false', 'no', '0', ''].includes(this.value.toLowerCase());
  }

  equals(other: ResolvedValue): boolean {
    return other.hasValue() && this.value === other.asText();
  }

  toString(): string {
    return this.asText();
  }

  map(fn: (value: ResolvedValue) => ResolvedValue): ResolvedValue {
    return fn(this);
  }

  hasValue(): boolean {
    return true;
  }
}

class NumberValue implements ResolvedValue {
  constructor(private readonly value:number) {
  }

  asText(): string {
    return this.value.toString();
  }

  asNumber(): number {
    return this.value;
  }

  asBoolean(): boolean {
    return this.value > 0;
  }

  equals(other: ResolvedValue): boolean {
    return other.hasValue() && this.value === other.asNumber();
  }

  toString(): string {
    return this.asText();
  }

  map(fn: (value: ResolvedValue) => ResolvedValue): ResolvedValue {
    return fn(this);
  }

  hasValue(): boolean {
    return true;
  }
}

class BooleanValue implements ResolvedValue {

  constructor(private readonly value:boolean) {
  }

  asBoolean(): boolean {
    return this.value;
  }

  asNumber(): number {
    return this.value ? 1 : 0;
  }

  asText(): string {
    return this.value ? "true" : "false";
  }

  map(fn: (value: ResolvedValue) => ResolvedValue): ResolvedValue {
    return fn(this);
  }

  hasValue(): boolean {
    return true;
  }

  equals(other: ResolvedValue): boolean {
    return other.hasValue() && this.value === other.asBoolean();
  }

  toString(): string {
    return this.asText();
  }
}

class NullValue implements ResolvedValue {
  static readonly Instance = new NullValue();

  asBoolean(): boolean {
    return false;
  }

  asNumber(): number {
    return 0;
  }

  asText(): string {
    return '';
  }

  map(_: (value: ResolvedValue) => ResolvedValue): ResolvedValue {
    return this;
  }

  hasValue(): boolean {
    return false;
  }

  equals(other: ResolvedValue): boolean {
    return !other.hasValue();
  }

  toString(): string {
    return this.asText();
  }
}

export abstract class ResolvedValue {
  static readonly True: ResolvedValue = new BooleanValue(true);
  static readonly False: ResolvedValue = new BooleanValue(false);
  static readonly None: ResolvedValue = NullValue.Instance;

  static of(value: string | number | boolean): ResolvedValue {
    if (value === undefined) {
      return NullValue.Instance;
    }

    if (typeof value === 'string') {
      return new TextValue(value);
    }
    if (typeof value === 'number') {
      return new NumberValue(value);
    }
    if (typeof value === 'boolean') {
      return value ? ResolvedValue.True : ResolvedValue.False;
    }
  }
}