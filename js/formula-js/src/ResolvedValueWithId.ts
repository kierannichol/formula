import {ResolvedValue} from "./ResolvedValue";

export class ResolvedValueWithId implements ResolvedValue {

  constructor(public readonly id: string, private readonly value: ResolvedValue) {
  }

  asBoolean(): boolean {
    return this.value.asBoolean();
  }

  asNumber(): number {
    return this.value.asNumber();
  }

  asText(): string {
    return this.value.asText();
  }

  asList(): ResolvedValue[] {
    return this.value.asList();
  }

  map(fn: (value: ResolvedValue) => ResolvedValue): ResolvedValue {
    return fn(this);
  }

  hasValue(): boolean {
    return this.value.hasValue();
  }

  equals(other: ResolvedValue): boolean {
    return this.value.equals(other);
  }

}