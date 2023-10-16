import { ResolvedValue } from "./ResolvedValue";

export class NamedResolvedValue implements ResolvedValue {

  constructor(private readonly value: ResolvedValue,
              private readonly name: string) {
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

  asName(): string {
    return this.name;
  }

  map(fn: (value: ResolvedValue) => ResolvedValue): ResolvedValue {
    return fn(this);
  }

  equals(other: ResolvedValue): boolean {
    return this.value.equals(other);
  }

  hasValue(): boolean {
    return this.value.hasValue();
  }

}