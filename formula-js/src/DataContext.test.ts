import {DataContext} from "./DataContext";
import {ResolvedValue} from "./ResolvedValue";

test('push to new key', () => {
  const context = DataContext.of({});
  context.push("key", 10);

  expect(context.get("key")?.asList()).toEqual([ResolvedValue.of(10)]);
})

test('push to key with scalar value', () => {
  const context = DataContext.of({ key: 5 });
  context.push("key", 10);

  expect(context.get("key")?.asList()).toEqual([
      ResolvedValue.of(5),
      ResolvedValue.of(10)
  ]);
})

test('push to key with list value', () => {
  const context = DataContext.of({ key: [5, 6] });
  context.push("key", 10);

  expect(context.get("key")?.asList()).toEqual([
    ResolvedValue.of(5),
    ResolvedValue.of(6),
    ResolvedValue.of(10)
  ]);
})