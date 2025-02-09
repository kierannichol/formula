import {ResolvedValue} from "./ResolvedValue";

export class ResolvedRollValue implements ResolvedValue {
  constructor(private readonly count: number,
              private readonly sides: number) {
  }

  asText(): string {
    return this.count + 'd' + this.sides;
  }

  asNumber(): number {
    return (this.count * (this.sides + 1)) / 2.0;
  }

  asBoolean(): boolean {
    return this.count > 0 && this.sides > 0;
  }

  asList(): ResolvedValue[] {
    return [this];
  }

  equals(other: ResolvedValue): boolean {
    return other.hasValue() && this.asText() === other.asText();
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