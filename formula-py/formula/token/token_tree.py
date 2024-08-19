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


class _Node:
    def __init__(self):
        self._nodes = []

    def walk(self, text: str, start_index: int, current_index: int) -> list[_TokenMatch]: pass

    def is_same(self, other: type[Self]) -> bool: pass

    def add_branch(self, nodes: list[type[Self]]) -> None:
        if len(nodes) == 0:
            return
        first_node = nodes[0]
        remaining_nodes = nodes[1:]
        for existing in self._nodes:
            if existing.is_same(first_node):
                existing.add_branch(remaining_nodes)
        if len(remaining_nodes) > 0:
            first_node.add_branch(remaining_nodes)
        self._nodes.append(first_node)

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


class _MappedNode(_LeafNode):
    def __init__(self, node: _LeafNode, map_fn: Callable[[str], any]):
        super().__init__()
        self._node = node
        self._map_fn = map_fn

    def walk(self, text: str, start_index: int, current_index: int):
        characters_matched = self._matches(text, start_index, current_index)
        if characters_matched is None:
            return []
        matches = [_TokenMatch(text, start_index, current_index + characters_matched, self._map_fn)]
        matches += self._walk_children(text, start_index, current_index)
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
        return f"{self._nodes}({self._min_times,self._max_times})"


class _OptionalNode(_LeafNode):
    def __init__(self, *nodes: _LeafNode):
        super().__init__()
        self._nodes = nodes

    def _matches(self, text: str, start_index: int, current_index: int) -> int | None:
        characters_matched = 0
        text_len = len(text)
        for node in self._nodes:
            if current_index + characters_matched > text_len:
                return None
            matched = node._matches(text, start_index, current_index + characters_matched)
            if matched is None:
                return 0
            characters_matched += matched
        return characters_matched

    def is_same(self, other: type[Self]) -> bool:
        return (isinstance(other, _OptionalNode)
                and self._nodes == other._nodes)

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
        return f"<{self._char}>"


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
        return f"<{self._allowed}>"

    def repeats(self, min_times: int, max_times: int | None = None):
        return _RepeatingNode(self, min_times, max_times)

    def optional(self):
        return _RepeatingNode(self, 0, 1)


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

    def add_branch(self, branch_nodes: str | _Node | list[_Node], map_fn: Callable[[str], any]) -> Self:
        nodes = []
        if isinstance(branch_nodes, _Node):
            branch_nodes = [branch_nodes]
        if isinstance(branch_nodes, list):
            branch_nodes = to_stream(branch_nodes)
        branch_nodes_len = len(branch_nodes)
        for i in range(branch_nodes_len):
            if isinstance(branch_nodes[i], str):
                current_node = _CharacterNode(branch_nodes[i])
            else:
                current_node = branch_nodes[i]
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


def any_of(characters: str) -> _AnyOfNode:
    return _AnyOfNode(list(characters))


def any_until(characters: str, escaped: str | None = None):
    return _AnyUntilNode(characters, escaped)


def term(text: str) -> list[_Node]:
    nodes = []
    for char in text:
        nodes.append(_CharacterNode(char))
    return nodes


def literal(open_token: str, close_token: str, escaped_token: str | None = None) -> list[_Node]:
    return [term(open_token), any_until(close_token, escaped_token), term(close_token)]


def optional(*nodes: _LeafNode) -> _OptionalNode:
    return _OptionalNode(*nodes)


INTEGER = any_of('0123456789').repeats(1)
DECIMAL = [INTEGER, _CharacterNode('.'), INTEGER]
NUMBER = [INTEGER, optional(_CharacterNode('.'), INTEGER)]
