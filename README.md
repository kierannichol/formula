# Formula

[![Javascript](https://github.com/kierannichol/formula/actions/workflows/formula-js.yml/badge.svg?branch=main)](https://github.com/kierannichol/formula/actions/workflows/formula-js.yml)
[![Java](https://github.com/kierannichol/formula/actions/workflows/formula-java.yml/badge.svg?branch=main)](https://github.com/kierannichol/formula/actions/workflows/formula-java.yml)
[![C#](https://github.com/kierannichol/formula/actions/workflows/formula-cs.yml/badge.svg?branch=main)](https://github.com/kierannichol/formula/actions/workflows/formula-cs.yml)

## What this is trying to solve

Given a big bag of key-value pairs (called a DataContext in this project), we want to be able to quickly solve basic mathematical formulas referencing these values.

# Formula Format

| Description       |                      Data Context                       |      Formula      |   Result    |
|:------------------|:-------------------------------------------------------:|:-----------------:|:-----------:|
| Addition          |                        ```{}```                         |    ```5 + 2```    |  ``` 7 ```  |
| Brackets          |                        ```{}```                         | ```(2 + 3) * 5``` | ``` 25 ```  |
| Scalar Variables  |                  ``` { "foo": 12 } ```                  | ``` @foo / 2 ```  |  ``` 6 ```  |
| Formula Variables | ``` { "foo": 12, "bar": Formula.parse("@foo + 1") } ``` | ``` @bar + 1 ```  |  ```14```   |

