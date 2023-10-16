import { ResolvedValue } from "./ResolvedValue";

export class ResolvedValueWithId extends ResolvedValue {

  constructor(public readonly id: string, private readonly value: ResolvedValue) {
    super();
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

  hasValue(): boolean {
    return this.value.hasValue();
  }

  equals(other: ResolvedValue): boolean {
    return this.value.equals(other);
  }

}