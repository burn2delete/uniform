"""L1 Gravity reader for source forms and syntax objects."""

from __future__ import annotations

from dataclasses import dataclass, field
from typing import Any


DELIMITERS = set("()[]{}\"';`~^@")
OPEN_TO_CLOSE = {"(": ")", "[": "]", "{": "}"}
CLOSE_TO_OPEN = {")": "(", "]": "[", "}": "{"}
SPECIAL_FORMS = {"quote", "syntax-quote", "unquote", "splice-unquote", "deref"}
VALID_STRING_ESCAPES = {
    "n": "\n",
    "r": "\r",
    "t": "\t",
    "\"": "\"",
    "\\": "\\",
}


@dataclass(frozen=True)
class SourcePosition:
    offset: int
    line: int
    column: int


@dataclass(frozen=True)
class ReaderExtension:
    tag: str
    required_build_effects: frozenset[str] = frozenset()


@dataclass(frozen=True)
class ReaderPolicy:
    registered_extensions: dict[str, ReaderExtension] = field(default_factory=dict)
    build_effect_grants: frozenset[str] = frozenset()


class ReaderError(Exception):
    def __init__(self, code: str, message: str, span: dict[str, Any], state: str, remediation: str):
        super().__init__(message)
        self.code = code
        self.message = message
        self.span = span
        self.state = state
        self.remediation = remediation

    def to_diagnostic(self, source: str) -> dict[str, Any]:
        start = max(0, int(self.span.get("start_byte", 0)))
        end = min(len(source), max(start + 1, int(self.span.get("end_byte", start + 1))))
        return {
            "id": self.code,
            "message": self.message,
            "span": self.span,
            "excerpt": source[start:end],
            "reader_state": self.state,
            "remediation": self.remediation,
        }


class Reader:
    def __init__(self, source: str, source_path: str, policy: ReaderPolicy | None = None):
        self.source = source
        self.source_path = source_path
        self.policy = policy or ReaderPolicy()
        self.offset = 0
        self.line = 1
        self.column = 1

    def read_all(self) -> list[dict[str, Any]]:
        forms: list[dict[str, Any]] = []
        while True:
            self.skip_ws_and_comments()
            if self.eof:
                break
            forms.append(self.read_form())
        self.attach_namespace_context(forms)
        return forms

    @property
    def eof(self) -> bool:
        return self.offset >= len(self.source)

    def current(self) -> str:
        return "" if self.eof else self.source[self.offset]

    def peek(self, count: int = 1) -> str:
        return self.source[self.offset : self.offset + count]

    def position(self) -> SourcePosition:
        return SourcePosition(self.offset, self.line, self.column)

    def advance(self) -> str:
        char = self.current()
        self.offset += 1
        if char == "\n":
            self.line += 1
            self.column = 1
        else:
            self.column += 1
        return char

    def span_from(self, start: SourcePosition) -> dict[str, Any]:
        return {
            "source": self.source_path,
            "start_byte": start.offset,
            "end_byte": self.offset,
            "start_line": start.line,
            "start_column": start.column,
            "end_line": self.line,
            "end_column": self.column,
        }

    def error(self, code: str, message: str, start: SourcePosition, state: str, remediation: str) -> None:
        raise ReaderError(code, message, self.span_from(start), state, remediation)

    def skip_ws_and_comments(self) -> None:
        while not self.eof:
            char = self.current()
            if char.isspace():
                self.advance()
                continue
            if char == ";":
                while not self.eof and self.current() != "\n":
                    self.advance()
                continue
            break

    def read_form(self) -> dict[str, Any]:
        self.skip_ws_and_comments()
        start = self.position()
        if self.eof:
            self.error("L1-DELIMITER", "unexpected end of source", start, "form", "Add a complete form.")
        char = self.current()
        if char in CLOSE_TO_OPEN:
            self.advance()
            self.error("L1-DELIMITER", f"unexpected closing delimiter {char}", start, "form", "Remove the closing delimiter or add its opener.")
        if char in OPEN_TO_CLOSE:
            return self.read_collection(char)
        if char == "\"":
            return self.read_string()
        if char == "#":
            return self.read_hash_form()
        if char in {"'", "`", "^", "@", "~"}:
            return self.read_abbreviation()
        return self.read_atom()

    def read_collection(self, opener: str) -> dict[str, Any]:
        start = self.position()
        self.advance()
        closer = OPEN_TO_CLOSE[opener]
        items: list[dict[str, Any]] = []
        while True:
            self.skip_ws_and_comments()
            if self.eof:
                self.error("L1-DELIMITER", f"unclosed delimiter {opener}", start, "collection", f"Add closing delimiter {closer}.")
            if self.current() == closer:
                self.advance()
                break
            if self.current() in CLOSE_TO_OPEN:
                bad_start = self.position()
                bad = self.advance()
                self.error("L1-DELIMITER", f"mismatched closing delimiter {bad}", bad_start, "collection", f"Use {closer} to close {opener}.")
            items.append(self.read_form())

        if opener == "{":
            if len(items) % 2:
                self.error("L1-MAP-ARITY", "map literal contains an odd number of forms", start, "map", "Add a missing value or remove the dangling key.")
            return self.syntax("map", [{"key": items[i], "value": items[i + 1]} for i in range(0, len(items), 2)], start, "collection")
        kind = "list" if opener == "(" else "vector"
        return self.syntax(kind, items, start, "collection")

    def read_hash_form(self) -> dict[str, Any]:
        start = self.position()
        if self.peek(2) == "#{":
            self.advance()
            self.advance()
            items: list[dict[str, Any]] = []
            literal_keys: set[tuple[str, Any]] = set()
            while True:
                self.skip_ws_and_comments()
                if self.eof:
                    self.error("L1-DELIMITER", "unclosed set literal", start, "set", "Add closing delimiter }.")
                if self.current() == "}":
                    self.advance()
                    break
                item = self.read_form()
                key = literal_key(item)
                if key is not None:
                    if key in literal_keys:
                        self.error("L1-MAP-ARITY", "set literal contains a duplicate decidable literal", start, "set", "Remove the duplicate set element.")
                    literal_keys.add(key)
                items.append(item)
            return self.syntax("set", items, start, "collection")
        self.advance()
        tag_chars: list[str] = []
        while not self.eof and not self.current().isspace() and self.current() not in OPEN_TO_CLOSE and self.current() not in CLOSE_TO_OPEN:
            tag_chars.append(self.advance())
        tag = "".join(tag_chars)
        extension = self.policy.registered_extensions.get(tag)
        if extension is None:
            self.error("L1-READER-EXTENSION", "reader extension tag is not registered", start, "reader-extension", "Register the reader extension through build policy or remove the tag.")
        ungranted = sorted(extension.required_build_effects - self.policy.build_effect_grants)
        if ungranted:
            self.error(
                "L1-READER-EXTENSION",
                f"reader extension requires ungranted build effects: {ungranted}",
                start,
                "reader-extension",
                "Grant the reader extension build effects in build policy or remove the extension.",
            )
        self.skip_ws_and_comments()
        if self.eof:
            self.error("L1-READER-EXTENSION", "reader extension is not followed by a payload form", start, "reader-extension", "Add a payload form after the reader extension tag.")
        payload = self.read_form()
        return self.syntax("tagged", {"tag": tag, "form": payload}, start, "reader-extension")

    def read_string(self) -> dict[str, Any]:
        start = self.position()
        self.advance()
        value: list[str] = []
        while not self.eof:
            char = self.advance()
            if char == "\"":
                return self.syntax("string", "".join(value), start, "literal")
            if char == "\\":
                if self.eof:
                    self.error("L1-STRING", "string escape is incomplete", start, "string", "Complete the escape or close the string.")
                escape_start = self.position()
                escape = self.advance()
                if escape not in VALID_STRING_ESCAPES:
                    self.error("L1-STRING", f"invalid string escape \\{escape}", escape_start, "string", "Use a supported escape sequence.")
                value.append(VALID_STRING_ESCAPES[escape])
            else:
                value.append(char)
        self.error("L1-STRING", "string literal is not closed", start, "string", "Add a closing quote.")

    def read_abbreviation(self) -> dict[str, Any]:
        start = self.position()
        char = self.advance()
        if char == "~" and self.current() == "@":
            self.advance()
            return self.expand_abbreviation("splice-unquote", start)
        if char == "^":
            metadata = self.read_form()
            self.skip_ws_and_comments()
            if self.eof:
                self.error("L1-METADATA", "metadata is not followed by a form", start, "metadata", "Attach metadata to a following form.")
            form = self.read_form()
            if not valid_metadata(metadata):
                self.error("L1-METADATA", "metadata must be a symbol, keyword, or map with symbol or keyword keys", start, "metadata", "Use keyword or symbol metadata keys.")
            form["metadata"].append(metadata)
            form["reader_origin"].append({"kind": "metadata", "span": self.span_from(start)})
            return form
        mapping = {
            "'": "quote",
            "`": "syntax-quote",
            "~": "unquote",
            "@": "deref",
        }
        return self.expand_abbreviation(mapping[char], start)

    def expand_abbreviation(self, operator: str, start: SourcePosition) -> dict[str, Any]:
        form = self.read_form()
        operator_object = self.syntax("symbol", operator, start, "generated")
        result = self.syntax("list", [operator_object, form], start, "abbreviation")
        result["reader_origin"].append(
            {
                "kind": "abbreviation",
                "operator": operator,
                "original_span": self.span_from(start),
                "generated_origin": form["span"],
            }
        )
        return result

    def read_atom(self) -> dict[str, Any]:
        start = self.position()
        token_chars: list[str] = []
        while not self.eof:
            char = self.current()
            if char.isspace() or char in OPEN_TO_CLOSE or char in CLOSE_TO_OPEN or char == ";":
                break
            if char in {"\"", "'", "`", "^", "@", "~"}:
                break
            token_chars.append(self.advance())
        token = "".join(token_chars)
        if not token:
            self.error("L1-DELIMITER", "reader could not read token", start, "atom", "Use a valid token or delimiter.")
        kind, value = parse_atom(token)
        return self.syntax(kind, value, start, "literal" if kind not in {"symbol", "keyword"} else "symbolic")

    def syntax(self, kind: str, value: Any, start: SourcePosition, origin_kind: str) -> dict[str, Any]:
        return {
            "kind": kind,
            "value": value,
            "span": self.span_from(start),
            "metadata": [],
            "namespace_context": None,
            "profile_context": None,
            "reader_origin": [{"kind": origin_kind}],
        }

    def attach_namespace_context(self, forms: list[dict[str, Any]]) -> None:
        current_namespace: str | None = None
        current_profile: str | None = None
        for form in forms:
            if is_ns_form(form):
                validate_ns_form(form)
                current_namespace = form["value"][1]["value"]
                current_profile = extract_ns_profile(form)
            attach_context(form, current_namespace, current_profile)


def parse_atom(token: str) -> tuple[str, Any]:
    if token == "nil":
        return "nil", None
    if token == "true":
        return "boolean", True
    if token == "false":
        return "boolean", False
    if token.startswith(":"):
        return "keyword", token
    if token.startswith("\\"):
        return "character", token[1:]
    try:
        if token.startswith(("0x", "-0x")):
            return "integer", int(token, 16)
        if token.startswith(("0b", "-0b")):
            return "integer", int(token, 2)
        if "/" in token and token.count("/") == 1 and all(part.lstrip("-").isdigit() for part in token.split("/")):
            return "ratio", token
        if any(marker in token for marker in [".", "e", "E"]):
            return "float", float(token)
        if token.lstrip("-").isdigit():
            return "integer", int(token)
    except ValueError:
        pass
    return "symbol", token


def literal_key(form: dict[str, Any]) -> tuple[str, Any] | None:
    if form["kind"] in {"nil", "boolean", "integer", "float", "ratio", "string", "character", "symbol", "keyword"}:
        return form["kind"], form["value"]
    return None


def valid_metadata(metadata: dict[str, Any]) -> bool:
    if metadata["kind"] in {"symbol", "keyword"}:
        return True
    if metadata["kind"] == "map":
        for entry in metadata["value"]:
            if entry["key"]["kind"] not in {"symbol", "keyword"}:
                return False
        return True
    return False


def is_ns_form(form: dict[str, Any]) -> bool:
    return (
        form["kind"] == "list"
        and len(form["value"]) >= 2
        and form["value"][0]["kind"] == "symbol"
        and form["value"][0]["value"] == "ns"
        and form["value"][1]["kind"] == "symbol"
    )


def validate_ns_form(form: dict[str, Any]) -> None:
    allowed = {
        ":profile",
        ":profiles",
        ":target",
        ":targets",
        ":requires",
        ":imports",
        ":exports",
        ":effects",
        ":capabilities",
        ":safety",
        ":providers",
        ":doc",
        ":metadata",
    }
    for clause in form["value"][2:]:
        if clause["kind"] != "list" or not clause["value"]:
            raise ReaderError("L1-NS-SHAPE", "namespace clause must be a non-empty list", clause["span"], "namespace", "Use a list clause such as (:profile :hosted).")
        head = clause["value"][0]
        if head["kind"] != "keyword" or head["value"] not in allowed:
            raise ReaderError("L1-NS-SHAPE", "namespace clause head is not an allowed keyword", head["span"], "namespace", "Use a documented namespace clause keyword.")


def extract_ns_profile(form: dict[str, Any]) -> str | None:
    for clause in form["value"][2:]:
        if clause["kind"] == "list" and clause["value"] and clause["value"][0]["kind"] == "keyword" and clause["value"][0]["value"] == ":profile":
            if len(clause["value"]) >= 2 and clause["value"][1]["kind"] == "keyword":
                return clause["value"][1]["value"]
    return None


def attach_context(form: dict[str, Any], namespace: str | None, profile: str | None) -> None:
    form["namespace_context"] = namespace
    form["profile_context"] = profile
    if form["kind"] == "map":
        for entry in form["value"]:
            attach_context(entry["key"], namespace, profile)
            attach_context(entry["value"], namespace, profile)
        return
    if form["kind"] == "tagged":
        attach_context(form["value"]["form"], namespace, profile)
        return
    value = form.get("value")
    if isinstance(value, list):
        for item in value:
            if isinstance(item, dict):
                attach_context(item, namespace, profile)


def read_source(source: str, source_path: str = "<memory>", policy: ReaderPolicy | None = None) -> list[dict[str, Any]]:
    return Reader(source, source_path, policy=policy).read_all()


def read_source_to_artifact(source: str, source_path: str, policy: ReaderPolicy | None = None) -> dict[str, Any]:
    forms = read_source(source, source_path=source_path, policy=policy)
    return {
        "kind": "syntax-object-stream",
        "source": source_path,
        "form_count": len(forms),
        "reader_extension_registry": sorted((policy or ReaderPolicy()).registered_extensions),
        "reader_build_effect_grants": sorted((policy or ReaderPolicy()).build_effect_grants),
        "forms": forms,
    }


def read_source_diagnostic(source: str, source_path: str = "<memory>", policy: ReaderPolicy | None = None) -> dict[str, Any] | None:
    try:
        read_source(source, source_path=source_path, policy=policy)
    except ReaderError as exc:
        return exc.to_diagnostic(source)
    return None


def read_source_bytes_to_artifact(source_bytes: bytes, source_path: str, encoding: str = "utf-8", policy: ReaderPolicy | None = None) -> dict[str, Any]:
    source = decode_source_bytes(source_bytes, source_path, encoding)
    artifact = read_source_to_artifact(source, source_path, policy=policy)
    artifact["source_encoding"] = encoding
    return artifact


def read_source_bytes_diagnostic(source_bytes: bytes, source_path: str, encoding: str = "utf-8", policy: ReaderPolicy | None = None) -> dict[str, Any] | None:
    try:
        read_source_bytes_to_artifact(source_bytes, source_path, encoding=encoding, policy=policy)
    except ReaderError as exc:
        return exc.to_diagnostic("")
    return None


def decode_source_bytes(source_bytes: bytes, source_path: str, encoding: str) -> str:
    try:
        return source_bytes.decode(encoding)
    except UnicodeDecodeError as exc:
        span = {
            "source": source_path,
            "start_byte": exc.start,
            "end_byte": max(exc.end, exc.start + 1),
            "start_line": 1,
            "start_column": exc.start + 1,
            "end_line": 1,
            "end_column": max(exc.end, exc.start + 1) + 1,
        }
        raise ReaderError(
            "L1-SOURCE-ENCODING",
            f"source bytes cannot be decoded as {encoding}",
            span,
            "source-encoding",
            "Save the source using the project encoding or configure an accepted encoding policy.",
        ) from exc
