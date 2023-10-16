import { ResolvedValue } from "./ResolvedValue";

export class QuotedTextResolvedValue implements ResolvedValue {

  constructor(private readonly value: ResolvedValue,
              private readonly prefix: string,
              private readonly suffix: string) {
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

  asQuotedText(): string {
    return this.prefix + this.asText() + this.suffix;
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