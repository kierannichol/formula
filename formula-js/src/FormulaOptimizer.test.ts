import {Formula} from "./Formula";

const fs = require("fs");
const path = require("path");

const file = path.join(__dirname, "..", "..", "formula-test", "optimize-test-cases.csv");
const content = fs.readFileSync(file, "utf8", function(err: any, data: any) {
  return data;
});

let index = 0;
for (let row of content.split("\n")) {
  if (index++ === 0) {
    // Skip header line
    continue;
  }
  const columns = row.split("|");
  const displayName = columns[0].trim();
  const given = columns[1].trim();
  const expected = columns[2].trim();

  test(displayName, () => {
    let formula = Formula.optimize(given)
    expect(formula).toBe(expected);
  });
}