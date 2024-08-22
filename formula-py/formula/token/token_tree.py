import string
from typing import Callable, Self

from formula.token.utils import to_stream


class _TokenMatch:
    def __init__(self, text: str, start_index: int, end_index: int, map_fn: Callable[[str], any]) -> None:
        self.text = text
        self.start_index = start_index
        self.end_index = end_index
        self.map_fn = map_fn

    def map(self) -> any:
        return self.map_fn(self.text[self.start_index:self.end_index])

    def __repr__(self):
        return f"'{self.text[self.start_index:self.end_index]}'"


class _Node:
    def __init__(self):
        self._nodes = []

    def walk(self, text: str, start_index: int, current_index: int) -> list[_TokenMatch]:
        pass

    def is_same(self, other: type[Self]) -> bool:
        pass

    def add_branch(self, nodes: list[type[Self]]) -> None:
        if len(nodes) == 0:
            return
        current_node = self
        for next_node in nodes:
            found = False
            for existing in current_node._nodes:
                if existing.is_same(next_node):
                    found = True
                    next_node = existing

            if not found:
                current_node._nodes.append(next_node)
            current_node = next_node

    def _walk_children(self, text: str, start_index: int, current_index: int) -> list[_TokenMatch]:
        matches = []
        for child in self._nodes:
            matches += child.walk(text, start_index, current_index)
        return matches


class _LeafNode(_Node):
    def __init__(self):
        super().__init__()

    def _matches(self, text: str, start_index: int, current_index: int) -> int | None: pass

    def walk(self, text: str, start_index: int, current_index: int) -> list[_TokenMatch]:
        characters_matched = self._matches(text, start_index, current_index)
        if characters_matched is None:
            return []
        return self._walk_children(text, start_index, current_index + characters_matched)


class _NodeFactory:
    def create(self) -> _LeafNode: pass


class _MappedNode(_LeafNode):
    def __init__(self, node: _LeafNode, map_fn: Callable[[str], any]):
        super().__init__()
        self._node = node
        self._map_fn = map_fn

    def walk(self, text: str, start_index: int, current_index: int):
        characters_matched = self._matches(text, start_index, current_index)
        if characters_matched is None:
            return []
        matches = self._walk_children(text, start_index, current_index + characters_matched)
        matches += [_TokenMatch(text, start_index, current_index + characters_matched, self._map_fn)]
        return matches

    def _matches(self, text: str, start_index: int, current_index: int) -> int:
        return self._node._matches(text, start_index, current_index)

    def is_same(self, other: type[Self]) -> bool:
        return self._node.is_same(other)

    def __repr__(self):
        return self._node.__repr__()


class _RootNode(_Node):
    def __init__(self):
        super().__init__()

    def walk(self, text: str, start_index: int, current_index: int) -> list[_TokenMatch]:
        return self._walk_children(text, start_index, current_index)

    def is_same(self, other: type[Self]) -> bool:
        return False


class _RepeatingNode(_LeafNode):
    def __init__(self, node: _LeafNode, min_times: int, max_times: int | None) -> None:
        super().__init__()
        self._node = node
        self._min_times = min_times
        self._max_times = max_times

    def _matches(self, text: str, start_index: int, current_index: int) -> int | None:
        characters_matched = 0
        matched = self._node._matches(text, start_index, current_index)
        text_len = len(text)
        while matched is not None:
            characters_matched += matched
            if current_index + characters_matched >= text_len:
                break
            matched = self._node._matches(text, start_index, current_index + characters_matched)

        if characters_matched < self._min_times:
            return None
        if self._max_times is not None and characters_matched > self._max_times:
            return None
        return characters_matched

    def is_same(self, other: type[Self]) -> bool:
        return (isinstance(other, _RepeatingNode)
                and self._node.is_same(other._node)
                and self._min_times == other._min_times
                and self._max_times == other._max_times)

    def __repr__(self):
        return f"{self._node}[{self._min_times},{self._max_times}]"


class _RepeatingNodeFactory(_NodeFactory):
    def __init__(self, node: _NodeFactory, min_times: int, max_times: int | None):
        self._node = node
        self._min_times = min_times
        self._max_times = max_times

    def create(self) -> _Node:
        return _RepeatingNode(self._node.create(), self._min_times, self._max_times)

    def __repr__(self):
        return f"{self._node}[{self._min_times},{self._max_times}]"


class _OptionalNode(_LeafNode):
    def __init__(self, nodes: list[_LeafNode]):
        super().__init__()
        self._optional_nodes = nodes

    def _matches(self, text: str, start_index: int, current_index: int) -> int | None:
        characters_matched = 0
        text_len = len(text)
        for node in self._optional_nodes:
            if current_index + characters_matched > text_len:
                return None
            matched = node._matches(text, start_index, current_index + characters_matched)
            if matched is None:
                return 0
            characters_matched += matched
        return characters_matched

    def is_same(self, other: type[Self]) -> bool:
        return (isinstance(other, _OptionalNode)
                and self._optional_nodes == other._optional_nodes)

    def __repr__(self):
        return f"{self._nodes}?"


class _OptionalNodeFactory(_NodeFactory):
    def __init__(self, *nodes: _NodeFactory):
        super().__init__()
        self._nodes = nodes

    def create(self) -> _LeafNode:
        return _OptionalNode([x.create() for x in self._nodes])

    def __repr__(self):
        return f"{self._nodes}?"


class _CharacterNode(_LeafNode):
    def __init__(self, char: str):
        super().__init__()
        self._char = char

    def _matches(self, text: str, start_index: int, current_index: int) -> int | None:
        if current_index == len(text):
            return None
        if text[current_index] == self._char:
            return 1
        return None

    def is_same(self, other: type[Self]) -> bool:
        return (isinstance(other, _CharacterNode)
                and self._char == other._char)

    def __repr__(self):
        return f"'{self._char}'"


class _CharacterNodeFactory(_NodeFactory):
    def __init__(self, char: str):
        super().__init__()
        self._char = char

    def create(self) -> _LeafNode:
        return _CharacterNode(self._char)

    def __repr__(self):
        return f"'{self._char}'"


class _AnyOfNode(_LeafNode):
    def __init__(self, allowed: list[str]):
        super().__init__()
        self._allowed = allowed

    def _matches(self, text: str, start_index: int, current_index: int) -> int | None:
        if current_index == len(text):
            return None
        if text[current_index] in self._allowed:
            return 1
        return None

    def is_same(self, other: type[Self]) -> bool:
        return (isinstance(other, _AnyOfNode)
                and self._allowed == other._allowed)

    def __repr__(self):
        return f"*"


class _AnyOfNodeFactory(_NodeFactory):
    def __init__(self, allowed: list[str]):
        super().__init__()
        self._allowed = allowed

    def create(self) -> _LeafNode:
        return _AnyOfNode(self._allowed)

    def repeats(self, min_times: int, max_times: int | None = None):
        return _RepeatingNodeFactory(self, min_times, max_times)

    def optional(self):
        return _OptionalNodeFactory(self)

    def __repr__(self):
        return f"*"


class _AnyUntilNode(_LeafNode):
    def __init__(self, until: str, escaped: str):
        super().__init__()
        self._until = until
        self._escaped = escaped

    def _matches(self, text: str, start_index: int, current_index: int) -> int | None:
        for working_index in range(current_index, len(text)):
            for k in range(len(self._until)):
                if text[working_index + k] != self._until[k]:
                    break
                if self._is_escaped(text, working_index):
                    break
                if k == len(self._until) - 1:
                    matched = working_index - current_index
                    if matched == 0:
                        matched = None
                    return matched
        return None

    def is_same(self, other: type[Self]) -> bool:
        return (isinstance(other, _AnyUntilNode)
                and self._until == other._until)

    def _is_escaped(self, text: str, index: int) -> bool:
        if self._escaped is None:
            return False
        found_index = text.find(self._escaped, index - len(self._escaped) + 1)
        if found_index == -1:
            return False
        return True

    def __repr__(self):
        return f"*{self._until}"


class _AnyUntilNodeFactory(_NodeFactory):
    def __init__(self, until: str, escaped: str):
        super().__init__()
        self._until = until
        self._escaped = escaped

    def create(self) -> _LeafNode:
        return _AnyUntilNode(self._until, self._escaped)

    def __repr__(self):
        return f"*{self._until}"


class TokenTree:
    def __init__(self):
        self._root = _RootNode()

    def ignore_whitespace(self) -> Self:
        self._root.add_branch([_MappedNode(_AnyOfNode(list(string.whitespace)), lambda char: None)])
        return self

    def parse(self, text: str) -> list[any]:
        tokens = []
        i = 0
        while i < len(text):
            matches = self._root.walk(text, i, i)
            if len(matches) > 0:
                mapped_token = matches[0].map()
                if mapped_token is not None:
                    tokens.append(mapped_token)
                i = matches[0].end_index
            else:
                raise ValueError(self._generate_parse_error(i, text, f"did not expect character {text[i]} of '{text}'"))
        return tokens

    def add_branch(self, branch_nodes: str | _NodeFactory | list[_NodeFactory], map_fn: Callable[[str], any]) -> Self:
        nodes = []
        if isinstance(branch_nodes, _NodeFactory):
            branch_nodes = [branch_nodes]
        if isinstance(branch_nodes, list):
            branch_nodes = to_stream(branch_nodes)
        branch_nodes_len = len(branch_nodes)
        for i in range(branch_nodes_len):
            current_node = branch_nodes[i]
            if isinstance(current_node, str):
                current_node = _CharacterNodeFactory(current_node).create()
            elif isinstance(current_node, _NodeFactory):
                current_node = current_node.create()
            if i == branch_nodes_len - 1:
                current_node = _MappedNode(current_node, map_fn)
            nodes.append(current_node)
        self._root.add_branch(nodes)
        return self

    @staticmethod
    def _generate_parse_error(index: int, text: str, message: str):
        tab = "\t"
        return f"Parse error at index {index} of '{text}': {message}\n{text}\n{tab.expandtabs(index)}^"


def create() -> TokenTree:
    return TokenTree()


def just(char: str) -> _NodeFactory:
    return _CharacterNodeFactory(char)


def any_of(characters: str) -> _AnyOfNodeFactory:
    return _AnyOfNodeFactory(list(characters))


def any_until(characters: str, escaped: str | None = None):
    return _AnyUntilNodeFactory(characters, escaped)


def term(text: str) -> list[_NodeFactory]:
    return [just(x) for x in text]


def literal(open_token: str, close_token: str, escaped_token: str | None = None) -> list[_NodeFactory]:
    return [*term(open_token), any_until(close_token, escaped_token), *term(close_token)]


def optional(*nodes: _NodeFactory) -> _NodeFactory:
    return _OptionalNodeFactory(*nodes)


INTEGER = any_of(string.digits).repeats(1)
DECIMAL = [INTEGER, just('.'), INTEGER]
NUMBER = [INTEGER, optional(just('.'), INTEGER)]
KEY = any_of(string.ascii_letters + string.digits + '_:.#*').repeats(1)
