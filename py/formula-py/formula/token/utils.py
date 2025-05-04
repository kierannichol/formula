from typing import TypeVar, List

T = TypeVar('T')


def to_stream(iterable: T | List[T | List[T]]) -> List[T]:
    output = []
    if isinstance(iterable, List):
        for item in iterable:
            output += to_stream(item)
    else:
        output.append(iterable)
    return output
