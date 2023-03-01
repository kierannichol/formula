import { Resolvable } from "./Resolvable";

export default interface Parser {
  parse(text: string): Resolvable;
}