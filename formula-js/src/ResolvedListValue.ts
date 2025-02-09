import {ResolvedValue} from "./ResolvedValue";

export class ResolvedListValue implements ResolvedValue {
  constructor(private readonly values: ResolvedValue[]) {
  }

  asText(): string {
    return this.latest().asText();
  }

  asNumber(): number {
    return this.latest().asNumber();
  }

  asBoolean(): boolean {
    return this.latest().asBoolean();
  }

  asList(): ResolvedValue[] {
    return this.values;
  }

  equals(other: ResolvedValue): boolean {
    const otherList = other.asList();
    if (otherList.length !== this.values.length) {
      return false;
    }
    for (let i = 0; i < this.values.length; i++) {
      if (!this.values[i].equals(otherList[i])) {
        return false;
      }
    }
    return true;
  }

  toString(): string {
    return this.values.toString();
  }

  map(fn: (value: ResolvedValue) => ResolvedValue): ResolvedValue {
    return fn(this);
  }

  hasValue(): boolean {
    return true;
  }

  private latest(): ResolvedValue {
    if (this.values.length == 0) {
      return ResolvedValue.None;
    }
    return this.values[this.values.length - 1];
  }
}