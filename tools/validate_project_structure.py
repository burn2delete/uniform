#!/usr/bin/env python3
"""Validate the machine-readable Gravity project structure contract.

The manifest is a coordination contract, not a compiler implementation.  It
keeps the D1 pipeline explicit, gives the D2/self-hosting slices a finite
dependency graph, and makes path ownership and generated evidence fail closed.
The validator intentionally uses only the Python standard library so it can be
run before the rest of the toolchain exists.
"""

from __future__ import annotations

import argparse
import fnmatch
import json
from pathlib import Path
import re
import sys
from typing import Any, Mapping, Sequence


ROOT = Path(__file__).resolve().parents[1]
DEFAULT_MANIFEST = ROOT / "contracts" / "project-structure.json"
NORMATIVE_OWNERSHIP = ROOT / "docs" / "self-hosting-slice-ownership.edn"
STAGE0_COMPONENT_CONTRACT = ROOT / "contracts" / "stage0-clojure-components.json"
MAX_OWNERSHIP_EDN_BYTES = 512 * 1024
MAX_STAGE0_COMPONENT_CONTRACT_BYTES = 512 * 1024
SCHEMA_VERSION = 1

STAGE0_COMPONENT_SCHEMA = "gravity/stage0-clojure-components-v1"
STAGE0_COMPONENT_KIND = "stage0-clojure-component-inventory"
STAGE0_COMPONENT_COUNTS = {
    "components": 44,
    "sources": 44,
    "tests": 44,
    "bootstrap_free_tests": 38,
    "compatibility_tests": 5,
    "coordinator_tests": 1,
}
STAGE0_COMPONENT_TOP_FIELDS = {
    "schema",
    "kind",
    "authority",
    "integration_owner",
    "source_root",
    "test_root",
    "counts",
    "canonical_pipeline_order",
    "stage0_extension_order",
    "authority_ceiling",
    "nonclaims",
    "components",
}
STAGE0_COMPONENT_FIELDS = {
    "id",
    "owner",
    "source",
    "test",
    "direct_source_dependencies",
    "direct_test_dependencies",
    "canonical_pass_refs",
    "mapping_kind",
    "authority",
    "contract_var",
    "stage0_group",
    "leaf_execution_group",
}
STAGE0_SOURCE_FIELDS = {"path", "namespace"}
STAGE0_TEST_FIELDS = {"path", "namespace", "lane", "execution_requires_bootstrap"}
STAGE0_AUTHORITY_FIELDS = {
    "ceiling",
    "compatibility_only",
    "canonical_authority",
    "self_hosted",
    "release",
    "seed_retirement",
}
STAGE0_TEST_LANES = {"bootstrap-free", "compatibility", "coordinator"}
STAGE0_MAPPING_KINDS = {
    "orchestrator",
    "primary",
    "supporting",
    "cross-cutting",
    "stage0-extension",
}
STAGE0_AUTHORITY_CEILINGS = {"none", "non-authoritative"}
STAGE0_GROUPS = {
    "coordinator",
    "foundation-reader",
    "c2-c3",
    "compiler",
    "compatibility-support",
}
STAGE0_LEAF_EXECUTION_GROUPS = {
    "foundation-reader",
    "c2-c3",
    "compiler",
}
STAGE0_LEAF_EXECUTION_GROUP_COMPONENT_IDS = {
    "foundation-reader": {
        "digest",
        "reader-cursor",
        "reader-diagnostic-policy",
        "reader-host-oracle",
        "reader-namespace",
        "source-unit",
        "syntax-object-stream",
        "syntax-origin",
    },
    "c2-c3": {
        "c2-artifact-identity",
        "c2-lexical-validation",
        "c2-reader-diagnostics",
        "c2-source-identity",
        "c3-artifact-identity",
        "c3-literal-projection",
        "c3-reader-integrity",
        "c3-syntax-construction",
        "c3-syntax-diagnostics",
        "c3-syntax-evidence",
        "c3-syntax-verification",
    },
    "compiler": {
        "c10-safety-analysis",
        "c11-mir",
        "c12-domain-ir",
        "c13-optimization",
        "c14-lowering",
        "c15-diagnostics",
        "c16-incremental",
        "c17-plugin",
        "c18-verification",
        "c4-macro-evidence",
        "c5-name-resolution",
        "c6-core-lowering",
        "c7-type-checker",
        "c8-effect-checker",
        "c9-ownership-checker",
        "compiler-verification-shared",
        "darwin-publication",
        "macro-expansion",
        "optimization-lowering",
    },
}
STAGE0_LEAF_EXECUTION_GROUP_COUNTS = {
    "foundation-reader": 8,
    "c2-c3": 11,
    "compiler": 19,
}
STAGE0_LEAF_EXECUTION_GROUP_BY_COMPONENT = {
    component_id: group
    for group, component_ids in STAGE0_LEAF_EXECUTION_GROUP_COMPONENT_IDS.items()
    for component_id in component_ids
}
STAGE0_BOOTSTRAP_COMPATIBILITY_TEST_COUNT = 5
STAGE0_COMPONENT_NONCLAIMS = (
    "canonical Gravity compiler authority",
    "self-hosted compiler authority",
    "release authority",
    "seed-retirement authority",
    "proof or conformance authority",
    "target support beyond the Stage0 Clojure/JVM subset",
)
STAGE0_EXTENSION_ORDER = (
    "read-source",
    "build-syntax",
    "macro-expand",
    "resolve-names",
    "lower-to-core",
    "type-check",
    "effect-check",
    "profile-validate",
    "capability-validate",
    "ownership-check",
    "safety-analyze",
    "build-mir",
    "verify-mir",
    "optimize-mir",
    "lower-domain-ir",
    "verify-domain-ir",
    "lower-target",
    "emit-artifacts",
    "record-package-provenance",
)
STAGE0_COMPATIBILITY_AUTHORITY_COMPONENT_IDS = {
    "c5-name-resolution",
    "c6-core-lowering",
    "c7-type-checker",
    "c8-effect-checker",
    "c9-ownership-checker",
    "c10-safety-analysis",
    "c11-mir",
    "c12-domain-ir",
    "c13-optimization",
    "c14-lowering",
    "c15-diagnostics",
    "c16-incremental",
    "c17-plugin",
    "c18-verification",
    "compiler-verification-shared",
    "optimization-lowering",
}

CANONICAL_PASS_IDS = (
    "source-forms",
    "reader",
    "syntax-objects",
    "macro-expansion",
    "core-ast",
    "name-resolution",
    "type-checking",
    "effect-checking",
    "profile-validation",
    "capability-validation",
    "ownership-lifetime",
    "safety-analysis",
    "gravity-mir",
    "domain-ir-lowering",
    "optimization",
    "target-lowering",
    "artifact-emission",
    "package-provenance",
)

REQUIRED_TOP_LEVEL = {
    "schema_version",
    "manifest_id",
    "alignment",
    "source_contracts",
    "canonical_passes",
    "artifacts",
    "slices",
    "ownership",
    "path_policy",
}

PASS_FIELDS = {
    "id",
    "name",
    "order",
    "depends_on",
    "input_artifacts",
    "output_artifacts",
    "requires",
    "preserves",
    "invalidates",
    "emits",
    "rejects",
    "owner",
    "cost",
    "authority",
}

PASS_COST_CLASSES = {"cheap", "bounded", "heavy", "serialized-heavy"}
AUTHORITY_LEVELS = {"none", "non-authoritative", "reviewed", "release"}
MAXIMUM_CLAIMS = {"contract-boundary-only", "coordination-only"}
SLICE_STATUSES = {"complete", "partial", "queued", "blocked"}
PATH_KINDS = {"reviewed", "generated", "reserved"}
ID_RE = re.compile(r"^[A-Za-z][A-Za-z0-9_.:-]*$")


class ManifestError(ValueError):
    """Raised by callers that prefer exceptions to an error list."""


def _strict_object_pairs(pairs: list[tuple[str, Any]]) -> dict[str, Any]:
    """Build JSON objects while rejecting duplicate keys.

    ``json.loads`` otherwise keeps the last value silently.  That is unsafe
    for a review contract because a duplicate key can hide an earlier owner,
    dependency, or authority claim.
    """

    result: dict[str, Any] = {}
    for key, value in pairs:
        if key in result:
            raise ValueError(f"duplicate JSON object key {key!r}")
        result[key] = value
    return result


def _reject_json_constant(value: str) -> Any:
    """Reject non-standard JSON constants such as NaN and Infinity."""

    raise ValueError(f"non-standard JSON constant {value!r} is not allowed")


def _strict_json_loads(text: str) -> Any:
    return json.loads(
        text,
        object_pairs_hook=_strict_object_pairs,
        parse_constant=_reject_json_constant,
    )


def _is_mapping(value: Any) -> bool:
    return isinstance(value, Mapping)


def _is_string_list(value: Any) -> bool:
    return isinstance(value, list) and all(isinstance(item, str) for item in value)


def _add_error(errors: list[str], location: str, message: str) -> None:
    errors.append(f"{location}: {message}")


def _validate_id(value: Any, location: str, errors: list[str]) -> bool:
    if not isinstance(value, str) or not value:
        _add_error(errors, location, "id must be a non-empty string")
        return False
    if not ID_RE.fullmatch(value):
        _add_error(errors, location, f"id has invalid characters: {value!r}")
        return False
    return True


def _required_mapping_fields(
    value: Any, fields: set[str], location: str, errors: list[str]
) -> bool:
    if not _is_mapping(value):
        _add_error(errors, location, "must be an object")
        return False
    missing = sorted(fields.difference(value))
    if missing:
        _add_error(errors, location, f"missing required fields: {', '.join(missing)}")
    return not missing


def _validate_string_list(
    value: Any, location: str, errors: list[str], *, allow_empty: bool = True
) -> bool:
    if not _is_string_list(value):
        _add_error(errors, location, "must be a list of strings")
        return False
    if not allow_empty and not value:
        _add_error(errors, location, "must not be empty")
        return False
    return True


def _validate_id_list(
    value: Any, location: str, errors: list[str], known: set[str] | None = None
) -> None:
    if not _validate_string_list(value, location, errors):
        return
    if known is not None:
        for item in value:
            if item not in known:
                _add_error(errors, location, f"unknown dependency or reference {item!r}")


def _collect_ids(
    manifest: Mapping[str, Any], errors: list[str]
) -> dict[str, dict[str, int]]:
    """Collect entity IDs and report duplicates within each typed collection.

    Pass and artifact IDs intentionally share a few semantic names (for
    example ``gravity-mir``), so references are resolved in their declared
    namespace.  A duplicate inside one collection would make a dependency
    point at an arbitrary entity and is rejected.
    """

    locations = (
        ("source_contracts", "source contract"),
        ("canonical_passes", "pass"),
        ("artifacts", "artifact"),
        ("slices", "slice"),
        ("ownership.categories", "ownership category"),
        ("ownership.owners", "owner"),
        ("path_policy.policies", "path policy"),
    )
    seen: dict[str, dict[str, int]] = {}
    for collection, label in locations:
        value: Any = manifest
        for part in collection.split("."):
            if not _is_mapping(value):
                value = None
                break
            value = value.get(part)
        if not isinstance(value, list):
            continue
        for index, item in enumerate(value):
            if not _is_mapping(item) or not isinstance(item.get("id"), str):
                continue
            entity_id = item["id"]
            location = f"{collection}[{index}].id"
            namespaced_id = f"{label}:{entity_id}"
            prior = seen.setdefault(namespaced_id, {})
            if prior:
                _add_error(
                    errors,
                    location,
                    f"duplicate id {entity_id!r}; already used by "
                    + ", ".join(f"{kind}#{offset}" for kind, offset in prior.items()),
                )
            prior[label] = index
    return seen


def _find_cycles(graph: Mapping[str, Sequence[str]]) -> list[list[str]]:
    """Return one or more readable dependency cycles from a directed graph."""

    state: dict[str, int] = {}
    stack: list[str] = []
    cycles: list[list[str]] = []

    def visit(node: str) -> None:
        state[node] = 1
        stack.append(node)
        for dependency in graph.get(node, ()):
            if dependency not in graph:
                continue
            if state.get(dependency, 0) == 0:
                visit(dependency)
            elif state.get(dependency) == 1:
                start = stack.index(dependency)
                cycle = stack[start:] + [dependency]
                if cycle not in cycles:
                    cycles.append(cycle)
        stack.pop()
        state[node] = 2

    for node in graph:
        if state.get(node, 0) == 0:
            visit(node)
    return cycles


def _validate_dependency_graph(
    entries: Sequence[Any], key: str, location: str, errors: list[str]
) -> set[str]:
    ids = {
        item.get("id")
        for item in entries
        if _is_mapping(item) and isinstance(item.get("id"), str)
    }
    graph: dict[str, list[str]] = {}
    for index, item in enumerate(entries):
        if not _is_mapping(item) or not isinstance(item.get("id"), str):
            continue
        item_id = item["id"]
        dependency_value = item.get(key)
        if not _is_string_list(dependency_value):
            _add_error(errors, f"{location}[{index}].{key}", "must be a list of strings")
            graph[item_id] = []
            continue
        graph[item_id] = list(dependency_value)
        for dependency in dependency_value:
            if dependency not in ids:
                _add_error(
                    errors,
                    f"{location}[{index}].{key}",
                    f"unknown dependency {dependency!r}",
                )
    for cycle in _find_cycles(graph):
        _add_error(errors, location, "dependency cycle: " + " -> ".join(cycle))
    return ids


def _pattern_prefix(pattern: str) -> str:
    """Return the literal prefix before the first supported glob operator.

    Do not append a path separator at a wildcard boundary.  For example,
    ``sh*_*.clj`` has the prefix ``sh``, not ``sh/``; the former correctly
    intersects ``sh07_test.clj`` while the latter silently misses it.
    """

    match = re.search(r"[*?\[{]", pattern)
    return pattern if match is None else pattern[: match.start()]


def _has_pattern_operator(pattern: str) -> bool:
    return re.search(r"[*?\[{]", pattern) is not None


def _patterns_overlap(left: str, right: str) -> bool:
    """Conservatively determine whether two supported path patterns overlap.

    The manifest uses exact paths, directory prefixes, and a small glob subset
    (``*``, ``?``, character classes, and EDN-style brace alternatives).  A
    literal prefix is sufficient for a fail-closed intersection test: when
    either side has a glob, compatible prefixes may overlap even if suffixes
    later prove disjoint.  Returning a false positive is safe; returning a
    false negative would permit two owners to claim the same path.
    """

    left = left.replace("\\", "/")
    right = right.replace("\\", "/")
    left_has_operator = _has_pattern_operator(left)
    right_has_operator = _has_pattern_operator(right)
    left_prefix = _pattern_prefix(left)
    right_prefix = _pattern_prefix(right)
    if left == right:
        return True
    if not left_has_operator and not right_has_operator:
        # A trailing separator is a directory reservation; otherwise exact
        # files only overlap on identity.
        return (left.endswith("/") and right.startswith(left)) or (
            right.endswith("/") and left.startswith(right)
        )
    if not left_prefix or not right_prefix:
        return True
    if left_prefix == right_prefix:
        return True
    if left_prefix.startswith(right_prefix) or right_prefix.startswith(left_prefix):
        return True
    return False


def _validate_relative_pattern(pattern: Any, location: str, errors: list[str]) -> None:
    if not isinstance(pattern, str) or not pattern:
        _add_error(errors, location, "path pattern must be a non-empty string")
        return
    if pattern.startswith("/") or "\\" in pattern:
        _add_error(errors, location, "path pattern must be relative and use '/' separators")
    if any(part == ".." for part in pattern.split("/")):
        _add_error(errors, location, "path pattern may not escape the repository")
    try:
        pattern.encode("ascii")
    except UnicodeEncodeError:
        _add_error(errors, location, "path pattern must be ASCII")


def _extract_normative_string_vector(
    section: str, key: str, errors: list[str]
) -> list[str]:
    matches = list(re.finditer(rf":{re.escape(key)}\s*\[(.*?)\]", section, flags=re.DOTALL))
    if len(matches) != 1:
        errors.append(f"ownership EDN must contain exactly one :{key} vector")
        return []
    body = matches[0].group(1)
    values = re.findall(r'"([^"\\]*(?:\\.[^"\\]*)*)"', body)
    remainder = re.sub(r'"([^"\\]*(?:\\.[^"\\]*)*)"', "", body)
    if re.search(r"[^\s]", remainder):
        errors.append(f"ownership EDN :{key} vector contains unrecognized structure")
    return values


def _read_ascii_contract_text(
    path: Path, maximum_bytes: int, label: str
) -> tuple[str, list[str]]:
    """Read a bounded ASCII contract text without a permissive fallback."""

    try:
        raw = path.read_bytes()
    except OSError as exc:
        return "", [f"cannot read {label} {path}: {exc}"]
    if len(raw) > maximum_bytes:
        return "", [f"{label} exceeds {maximum_bytes} bytes"]
    try:
        text = raw.decode("utf-8")
    except UnicodeDecodeError as exc:
        return "", [f"{label} is not UTF-8: {exc}"]
    try:
        text.encode("ascii")
    except UnicodeEncodeError:
        return "", [f"{label} must be ASCII"]
    return text, []


def _clojure_ns_form(text: str, location: str) -> tuple[str | None, str, list[str]]:
    """Extract the first balanced Clojure ``ns`` form.

    Stage0 files keep their namespace declaration as the first form.  A small
    scanner is used instead of loading Clojure/JVM code, and it ignores
    comments and strings so names in documentation cannot become dependencies.
    """

    errors: list[str] = []
    index = 0
    length = len(text)
    while index < length:
        if text[index].isspace() or text[index] == ",":
            index += 1
            continue
        if text[index] == ";":
            newline = text.find("\n", index)
            index = length if newline < 0 else newline + 1
            continue
        break
    if not text.startswith("(ns", index) or (
        index + 3 < length and not text[index + 3].isspace() and text[index + 3] not in "([{}"
    ):
        return None, "", [f"{location}: first form must be an ns declaration"]
    start = index
    stack: list[str] = []
    in_string = False
    escaped = False
    in_comment = False
    while index < length:
        char = text[index]
        if in_comment:
            if char == "\n":
                in_comment = False
            index += 1
            continue
        if in_string:
            if escaped:
                escaped = False
            elif char == "\\":
                escaped = True
            elif char == '"':
                in_string = False
            index += 1
            continue
        if char == ";":
            in_comment = True
            index += 1
            continue
        if char == '"':
            in_string = True
            index += 1
            continue
        if char in "([{":
            stack.append(char)
        elif char in ")]}":
            expected = {')': '(', ']': '[', '}': '{'}[char]
            if not stack or stack[-1] != expected:
                errors.append(f"{location}: unbalanced ns declaration")
                return None, "", errors
            stack.pop()
            if not stack:
                return None, text[start:index + 1], errors
        index += 1
    if in_string:
        errors.append(f"{location}: unterminated string in ns declaration")
    else:
        errors.append(f"{location}: unterminated ns declaration")
    return None, "", errors


def _clojure_form_tokens(form: str) -> tuple[list[str], list[str]]:
    """Tokenize delimiters and symbols in a namespace form."""

    tokens: list[str] = []
    errors: list[str] = []
    index = 0
    length = len(form)
    delimiters = set("()[]{}")
    while index < length:
        char = form[index]
        if char.isspace() or char == ",":
            index += 1
            continue
        if char == ";":
            newline = form.find("\n", index)
            index = length if newline < 0 else newline + 1
            continue
        if char in delimiters:
            tokens.append(char)
            index += 1
            continue
        if char == '"':
            start = index
            index += 1
            escaped = False
            while index < length:
                current = form[index]
                if escaped:
                    escaped = False
                elif current == "\\":
                    escaped = True
                elif current == '"':
                    index += 1
                    break
                index += 1
            else:
                errors.append("namespace declaration contains an unterminated string")
                break
            tokens.append(form[start:index])
            continue
        start = index
        while index < length and not form[index].isspace() and form[index] not in delimiters and form[index] != ",":
            index += 1
        tokens.append(form[start:index])
    return tokens, errors


def _clojure_ns_requirements(
    text: str, location: str
) -> tuple[str | None, list[str], list[str]]:
    """Return an ns symbol and direct ``:require`` namespace symbols."""

    namespace, form, errors = _clojure_ns_form(text, location)
    if errors:
        return namespace, [], errors
    tokens, token_errors = _clojure_form_tokens(form)
    errors.extend(f"{location}: {error}" for error in token_errors)
    if len(tokens) < 3 or tokens[0] != "(" or tokens[1] != "ns":
        errors.append(f"{location}: malformed ns declaration")
        return None, [], errors
    namespace = tokens[2]
    if not namespace or namespace.startswith(("[", "(", ":")):
        errors.append(f"{location}: ns declaration has no namespace symbol")
        return None, [], errors

    # Find direct ``(:require ...)`` clauses in the outer ns list.  Nested
    # vectors such as ``:refer [foo]`` are deliberately ignored.
    requirements: list[str] = []
    index = 3
    outer_depth = 1
    while index < len(tokens):
        token = tokens[index]
        if token == "(" and index + 1 < len(tokens) and tokens[index + 1] == ":require" and outer_depth == 1:
            index += 2
            clause_depth = 1
            while index < len(tokens) and clause_depth:
                token = tokens[index]
                if token == "[" and clause_depth == 1:
                    if index + 1 >= len(tokens):
                        errors.append(f"{location}: malformed :require vector")
                        break
                    required_namespace = tokens[index + 1]
                    if required_namespace.startswith('"'):
                        errors.append(f"{location}: :require namespace must be a symbol")
                    else:
                        requirements.append(required_namespace)
                    vector_depth = 1
                    index += 1
                    while index < len(tokens) and vector_depth:
                        if tokens[index] == "[":
                            vector_depth += 1
                        elif tokens[index] == "]":
                            vector_depth -= 1
                        index += 1
                    continue
                if token == "(":
                    clause_depth += 1
                elif token == ")":
                    clause_depth -= 1
                index += 1
            continue
        if token == "(":
            outer_depth += 1
        elif token == ")":
            outer_depth -= 1
        index += 1
    if outer_depth != 0:
        errors.append(f"{location}: malformed outer ns declaration")
    return namespace, requirements, errors


def _component_file_dependencies(
    path: Path,
    location: str,
    namespace_to_component: Mapping[str, str],
    errors: list[str],
) -> tuple[str | None, list[str]]:
    try:
        text = path.read_text(encoding="utf-8")
    except OSError as exc:
        _add_error(errors, location, f"cannot read source/test file {path}: {exc}")
        return None, []
    namespace, required_namespaces, parse_errors = _clojure_ns_requirements(text, location)
    for parse_error in parse_errors:
        _add_error(errors, location, parse_error)
    dependencies: list[str] = []
    for required_namespace in required_namespaces:
        if not required_namespace.startswith("gravity."):
            continue
        component_id = namespace_to_component.get(required_namespace)
        if component_id is None:
            _add_error(
                errors,
                location,
                f"requires unregistered internal namespace {required_namespace!r}",
            )
            continue
        if component_id not in dependencies:
            dependencies.append(component_id)
    return namespace, sorted(dependencies)


def parse_normative_ownership(
    path: Path = NORMATIVE_OWNERSHIP,
) -> tuple[list[str], list[str], list[str], dict[str, str], list[str]]:
    """Project only coordinator ownership facts from the normative EDN.

    This is deliberately not a permissive EDN parser.  The parity contract
    depends on simple, stable vector shapes for coordinator routing, generated
    evidence, and integration surfaces, plus a string-to-keyword map under
    ``:module-owners``. Any missing marker, duplicate marker, non-ASCII input,
    or token outside those shapes is an error rather than a best-effort parse.
    """

    errors: list[str] = []
    try:
        raw = path.read_bytes()
    except OSError as exc:
        return [], [], [], {}, [f"cannot read normative ownership EDN {path}: {exc}"]
    if len(raw) > MAX_OWNERSHIP_EDN_BYTES:
        return [], [], [], {}, [f"normative ownership EDN exceeds {MAX_OWNERSHIP_EDN_BYTES} bytes"]
    try:
        text = raw.decode("utf-8")
    except UnicodeDecodeError as exc:
        return [], [], [], {}, [f"normative ownership EDN is not UTF-8: {exc}"]
    try:
        text.encode("ascii")
    except UnicodeEncodeError:
        errors.append("normative ownership EDN must be ASCII")
    non_string_text = re.sub(r'"([^"\\]*(?:\\.[^"\\]*)*)"', '""', text)
    if re.search(r"[#;]", non_string_text):
        errors.append(
            "normative ownership EDN may not use comments, reader discard, or tagged reader syntax"
        )
    if text.count(":schema") != 1 or ":schema :gravity/self-hosting-slice-ownership-v1" not in text:
        errors.append("normative ownership EDN schema marker is unrecognized")
    integration_owner_markers = re.findall(
        r"(?m)^\s*:integration-owner\s+:master-coordinator\s*$", text
    )
    if len(integration_owner_markers) != 1:
        errors.append("normative ownership EDN integration owner is unrecognized")

    coordinator_matches = list(re.finditer(r":coordinator-owned\s*\{", text))
    module_matches = list(re.finditer(r":module-owners\s*\{", text))
    if len(coordinator_matches) != 1:
        errors.append("normative ownership EDN must contain exactly one :coordinator-owned map")
    if len(module_matches) != 1:
        errors.append("normative ownership EDN must contain exactly one :module-owners map")
    if errors:
        return [], [], [], {}, errors

    coordinator_start = coordinator_matches[0].end()
    module_start = module_matches[0].start()
    if module_start <= coordinator_start:
        return [], [], [], {}, ["normative ownership EDN coordinator/module owner order is unrecognized"]
    coordinator_section = text[coordinator_start:module_start]
    central_routing = _extract_normative_string_vector(
        coordinator_section, "central-routing", errors
    )
    generated_evidence_prefixes = _extract_normative_string_vector(
        coordinator_section, "generated-evidence-prefixes", errors
    )
    integration_surfaces = _extract_normative_string_vector(
        coordinator_section, "integration-surfaces", errors
    )

    reserved_matches = list(re.finditer(r":reserved-leaf-modules\s*\{", text))
    if len(reserved_matches) != 1 or reserved_matches[0].start() <= module_matches[0].end():
        errors.append("normative ownership EDN must delimit :module-owners before :reserved-leaf-modules")
        return central_routing, generated_evidence_prefixes, integration_surfaces, {}, errors
    module_section = text[module_matches[0].end() : reserved_matches[0].start()]
    entry_re = re.compile(r'"([^"\\]*(?:\\.[^"\\]*)*)"\s+:([A-Za-z0-9_-]+)')
    entries = list(entry_re.finditer(module_section))
    module_owners: dict[str, str] = {}
    for match in entries:
        source_path, owner = match.groups()
        if source_path in module_owners:
            errors.append(f"normative ownership EDN repeats module owner path {source_path!r}")
        module_owners[source_path] = owner
    remainder = entry_re.sub("", module_section).replace("}", "")
    if not entries or re.search(r"[^\s]", remainder):
        errors.append("normative ownership EDN :module-owners map contains unrecognized structure")
    return central_routing, generated_evidence_prefixes, integration_surfaces, module_owners, errors


def _extract_edn_delimited_section(
    text: str, marker: str, opener: str, closer: str, errors: list[str]
) -> str:
    """Extract one balanced EDN section after a marker.

    The normative file is intentionally a tiny EDN projection.  We still
    track nested delimiters and strings so a malformed or truncated section
    fails closed instead of being accepted by a non-greedy regex.
    """

    matches = list(re.finditer(rf":{re.escape(marker)}\s*{re.escape(opener)}", text))
    if len(matches) != 1:
        errors.append(f"ownership EDN must contain exactly one :{marker} {opener} section")
        return ""
    start = matches[0].end()
    stack = [opener]
    in_string = False
    escaped = False
    index = start
    while index < len(text):
        char = text[index]
        if in_string:
            if escaped:
                escaped = False
            elif char == "\\":
                escaped = True
            elif char == '"':
                in_string = False
            index += 1
            continue
        if char == '"':
            in_string = True
            index += 1
            continue
        if char in "([{":
            stack.append(char)
        elif char in ")]}":
            if not stack or {')': '(', ']': '[', '}': '{'}[char] != stack[-1]:
                errors.append(f"ownership EDN :{marker} section has unbalanced delimiters")
                return ""
            stack.pop()
            if not stack:
                return text[start:index]
        index += 1
    errors.append(f"ownership EDN :{marker} section is unterminated")
    return ""


def _parse_edn_keyword_map(body: str, marker: str, errors: list[str]) -> dict[str, str]:
    entry_re = re.compile(r'"([^"\\]*(?:\\.[^"\\]*)*)"\s+:([A-Za-z0-9_-]+)')
    entries = list(entry_re.finditer(body))
    result: dict[str, str] = {}
    for match in entries:
        key, value = match.groups()
        if key in result:
            errors.append(f"ownership EDN :{marker} repeats key {key!r}")
        result[key] = value
    remainder = entry_re.sub("", body)
    if re.search(r"[^\s]", remainder):
        errors.append(f"ownership EDN :{marker} map contains unrecognized structure")
    if not entries:
        errors.append(f"ownership EDN :{marker} map is empty")
    return result


def parse_stage0_component_ownership(
    path: Path = NORMATIVE_OWNERSHIP,
) -> tuple[dict[str, str], list[str], list[str]]:
    """Project the Stage0 reserved-leaf and compatibility-test EDN facts.

    The existing :func:`parse_normative_ownership` return shape is preserved;
    this focused projection supplies the two new Stage0 maps/vectors without
    exposing a permissive general EDN reader.
    """

    errors: list[str] = []
    text, read_errors = _read_ascii_contract_text(
        path, MAX_OWNERSHIP_EDN_BYTES, "normative ownership EDN"
    )
    errors.extend(read_errors)
    if read_errors:
        return {}, [], errors
    non_string_text = re.sub(r'"([^"\\]*(?:\\.[^"\\]*)*)"', '""', text)
    if re.search(r"[#;]", non_string_text):
        errors.append(
            "normative ownership EDN may not use comments, reader discard, or tagged reader syntax"
        )
    reserved_body = _extract_edn_delimited_section(
        text, "reserved-leaf-modules", "{", "}", errors
    )
    reserved = _parse_edn_keyword_map(reserved_body, "reserved-leaf-modules", errors) if reserved_body else {}
    compatibility_body = _extract_edn_delimited_section(
        text, "bootstrap-compatibility-tests", "[", "]", errors
    )
    compatibility: list[str] = []
    if compatibility_body:
        values = re.findall(r'"([^"\\]*(?:\\.[^"\\]*)*)"', compatibility_body)
        remainder = re.sub(r'"([^"\\]*(?:\\.[^"\\]*)*)"', "", compatibility_body)
        if re.search(r"[^\s]", remainder):
            errors.append(
                "ownership EDN :bootstrap-compatibility-tests vector contains unrecognized structure"
            )
        compatibility = values
        if len(values) != len(set(values)):
            errors.append("ownership EDN :bootstrap-compatibility-tests vector repeats a path")
    return reserved, compatibility, errors


# Descriptive alias for callers that use the projection terminology.
parse_stage0_ownership_projection = parse_stage0_component_ownership


def load_stage0_component_contract(
    path: Path = STAGE0_COMPONENT_CONTRACT,
) -> Any:
    """Load the fixed reviewed Stage0 component inventory strictly."""

    try:
        raw = path.read_bytes()
    except OSError as exc:
        raise ManifestError(f"cannot read Stage0 component contract {path}: {exc}") from exc
    if len(raw) > MAX_STAGE0_COMPONENT_CONTRACT_BYTES:
        raise ManifestError(
            f"Stage0 component contract {path} exceeds {MAX_STAGE0_COMPONENT_CONTRACT_BYTES} bytes"
        )
    try:
        text = raw.decode("utf-8")
    except UnicodeDecodeError as exc:
        raise ManifestError(f"Stage0 component contract {path} is not UTF-8: {exc}") from exc
    try:
        return _strict_json_loads(text)
    except (TypeError, ValueError, json.JSONDecodeError) as exc:
        raise ManifestError(f"invalid strict JSON in Stage0 component contract {path}: {exc}") from exc


def _stage0_component_id_from_source_path(path: str) -> str | None:
    match = re.fullmatch(r"bootstrap/clojure/src/gravity/([A-Za-z0-9_]+)\.clj", path)
    if not match:
        return None
    return match.group(1).replace("_", "-")


def _stage0_test_path_for_source(path: str) -> str | None:
    component_id = _stage0_component_id_from_source_path(path)
    if component_id is None:
        return None
    source_stem = path.rsplit("/", 1)[-1][:-4]
    return f"bootstrap/clojure/test/gravity/{source_stem}_test.clj"


def _stage0_path_claims(
    policies: Sequence[Any], path: str
) -> list[tuple[str, str]]:
    claims: list[tuple[str, str]] = []
    for policy in policies:
        if not _is_mapping(policy) or not _is_string_list(policy.get("patterns")):
            continue
        if any(_path_pattern_matches(pattern, path) for pattern in policy["patterns"]):
            policy_id = policy.get("id")
            owner = policy.get("owner")
            if isinstance(policy_id, str) and isinstance(owner, str):
                claims.append((policy_id, owner))
    return claims


def _validate_stage0_component_contract(
    manifest: Mapping[str, Any],
    errors: list[str],
    contract: Any | None = None,
) -> None:
    """Validate the fixed Stage0 component inventory and all projections."""

    if contract is None:
        try:
            contract = load_stage0_component_contract()
        except ManifestError as exc:
            _add_error(errors, "stage0 component contract", str(exc))
            return
    if not _is_mapping(contract):
        _add_error(errors, "stage0 component contract", "must be a JSON object")
        return

    missing = sorted(STAGE0_COMPONENT_TOP_FIELDS.difference(contract))
    extra = sorted(set(contract).difference(STAGE0_COMPONENT_TOP_FIELDS))
    if missing:
        _add_error(errors, "stage0 component contract", "missing required fields: " + ", ".join(missing))
    if extra:
        _add_error(errors, "stage0 component contract", "unknown fields: " + ", ".join(extra))
    if contract.get("schema") != STAGE0_COMPONENT_SCHEMA:
        _add_error(errors, "stage0 component contract.schema", f"must be {STAGE0_COMPONENT_SCHEMA!r}")
    if contract.get("kind") != STAGE0_COMPONENT_KIND:
        _add_error(errors, "stage0 component contract.kind", f"must be {STAGE0_COMPONENT_KIND!r}")
    if contract.get("authority") is not False:
        _add_error(errors, "stage0 component contract.authority", "must be false")
    if contract.get("integration_owner") != "master-coordinator":
        _add_error(errors, "stage0 component contract.integration_owner", "must be 'master-coordinator'")
    if contract.get("source_root") != "bootstrap/clojure/src":
        _add_error(errors, "stage0 component contract.source_root", "must be bootstrap/clojure/src")
    if contract.get("test_root") != "bootstrap/clojure/test":
        _add_error(errors, "stage0 component contract.test_root", "must be bootstrap/clojure/test")
    if contract.get("authority_ceiling") != "non-authoritative":
        _add_error(errors, "stage0 component contract.authority_ceiling", "must be non-authoritative")
    if contract.get("nonclaims") != list(STAGE0_COMPONENT_NONCLAIMS):
        _add_error(errors, "stage0 component contract.nonclaims", "must match the reviewed nonclaims exactly")
    if contract.get("canonical_pipeline_order") != list(CANONICAL_PASS_IDS):
        _add_error(errors, "stage0 component contract.canonical_pipeline_order", "must match canonical pass order exactly")
    if contract.get("stage0_extension_order") != list(STAGE0_EXTENSION_ORDER):
        _add_error(errors, "stage0 component contract.stage0_extension_order", "must match Stage0 extension order exactly")
    if contract.get("counts") != STAGE0_COMPONENT_COUNTS:
        _add_error(errors, "stage0 component contract.counts", f"must equal {STAGE0_COMPONENT_COUNTS!r}")

    components = contract.get("components")
    if not isinstance(components, list):
        _add_error(errors, "stage0 component contract.components", "must be a list")
        return
    if len(components) != STAGE0_COMPONENT_COUNTS["components"]:
        _add_error(errors, "stage0 component contract.components", "must contain exactly 44 components")

    # Every source file in this slice is a direct child of the reviewed source
    # root.  The test root also contains runner infrastructure; only the
    # paired *_test.clj files are part of this inventory.
    source_root = ROOT / "bootstrap/clojure/src/gravity"
    test_root = ROOT / "bootstrap/clojure/test/gravity"
    actual_source_paths = {
        path.relative_to(ROOT).as_posix()
        for path in source_root.glob("*.clj")
        if path.is_file()
    }
    actual_test_paths = {
        path.relative_to(ROOT).as_posix()
        for path in test_root.glob("*_test.clj")
        if path.is_file()
    }

    component_by_id: dict[str, Mapping[str, Any]] = {}
    source_namespace_to_id: dict[str, str] = {}
    test_namespace_to_id: dict[str, str] = {}
    source_paths: list[str] = []
    test_paths: list[str] = []
    leaf_execution_groups: list[Any] = []
    for index, component in enumerate(components):
        location = f"stage0 component contract.components[{index}]"
        if not _is_mapping(component):
            _add_error(errors, location, "must be an object")
            continue
        missing_fields = sorted(STAGE0_COMPONENT_FIELDS.difference(component))
        extra_fields = sorted(set(component).difference(STAGE0_COMPONENT_FIELDS))
        if missing_fields:
            _add_error(errors, location, "missing required fields: " + ", ".join(missing_fields))
        if extra_fields:
            _add_error(errors, location, "unknown fields: " + ", ".join(extra_fields))
        component_id = component.get("id")
        if not _validate_id(component_id, f"{location}.id", errors):
            continue
        if component_id in component_by_id:
            _add_error(errors, f"{location}.id", f"duplicate component id {component_id!r}")
        component_by_id[component_id] = component
        source = component.get("source")
        test = component.get("test")
        if not _required_mapping_fields(source, STAGE0_SOURCE_FIELDS, f"{location}.source", errors):
            source = {}
        if not _required_mapping_fields(test, STAGE0_TEST_FIELDS, f"{location}.test", errors):
            test = {}
        if _is_mapping(source) and set(source) != STAGE0_SOURCE_FIELDS:
            _add_error(errors, f"{location}.source", "must contain exactly path and namespace")
        if _is_mapping(test) and set(test) != STAGE0_TEST_FIELDS:
            _add_error(errors, f"{location}.test", "must contain exactly path, namespace, lane, and execution_requires_bootstrap")
        source_path = source.get("path") if _is_mapping(source) else None
        test_path = test.get("path") if _is_mapping(test) else None
        source_namespace = source.get("namespace") if _is_mapping(source) else None
        test_namespace = test.get("namespace") if _is_mapping(test) else None
        for value, field in ((source_path, "source.path"), (test_path, "test.path"), (source_namespace, "source.namespace"), (test_namespace, "test.namespace")):
            if not isinstance(value, str) or not value:
                _add_error(errors, f"{location}.{field}", "must be a non-empty string")
        if isinstance(source_path, str):
            source_paths.append(source_path)
            expected_id = _stage0_component_id_from_source_path(source_path)
            if expected_id != component_id:
                _add_error(errors, f"{location}.id", f"must match source path stem {expected_id!r}")
            if not source_path.startswith("bootstrap/clojure/src/"):
                _add_error(errors, f"{location}.source.path", "must be under the reviewed source root")
            if source_path_namespace := source_namespace:
                if source_path_namespace in source_namespace_to_id:
                    _add_error(errors, f"{location}.source.namespace", f"duplicate source namespace {source_path_namespace!r}")
                source_namespace_to_id[source_path_namespace] = component_id
        if isinstance(test_path, str):
            test_paths.append(test_path)
            expected_test_path = _stage0_test_path_for_source(source_path) if isinstance(source_path, str) else None
            if test_path != expected_test_path:
                _add_error(errors, f"{location}.test.path", f"must be paired test path {expected_test_path!r}")
            if not test_path.startswith("bootstrap/clojure/test/"):
                _add_error(errors, f"{location}.test.path", "must be under the reviewed test root")
            if isinstance(test_namespace, str):
                if test_namespace in test_namespace_to_id:
                    _add_error(errors, f"{location}.test.namespace", f"duplicate test namespace {test_namespace!r}")
                test_namespace_to_id[test_namespace] = component_id

        owner = component.get("owner")
        if not isinstance(owner, str) or not owner:
            _add_error(errors, f"{location}.owner", "must be a non-empty string")
        for dependency_field in ("direct_source_dependencies", "direct_test_dependencies", "canonical_pass_refs"):
            value = component.get(dependency_field)
            if not _is_string_list(value):
                _add_error(errors, f"{location}.{dependency_field}", "must be a list of strings")
            elif dependency_field != "canonical_pass_refs" and value != sorted(set(value)):
                _add_error(errors, f"{location}.{dependency_field}", "must be sorted and contain no duplicates")
        canonical_refs = component.get("canonical_pass_refs")
        if _is_string_list(canonical_refs):
            unknown_refs = sorted(set(canonical_refs).difference(CANONICAL_PASS_IDS))
            if unknown_refs:
                _add_error(errors, f"{location}.canonical_pass_refs", "unknown canonical pass reference(s): " + ", ".join(unknown_refs))
            expected_ref_order = sorted(canonical_refs, key=lambda ref: CANONICAL_PASS_IDS.index(ref) if ref in CANONICAL_PASS_IDS else len(CANONICAL_PASS_IDS))
            if canonical_refs != expected_ref_order:
                _add_error(errors, f"{location}.canonical_pass_refs", "must follow canonical pipeline order")
        if component.get("mapping_kind") not in STAGE0_MAPPING_KINDS:
            _add_error(errors, f"{location}.mapping_kind", f"unknown mapping kind {component.get('mapping_kind')!r}")
        if component.get("stage0_group") not in STAGE0_GROUPS:
            _add_error(errors, f"{location}.stage0_group", f"unknown Stage0 group {component.get('stage0_group')!r}")
        leaf_execution_group = component.get("leaf_execution_group")
        leaf_execution_groups.append(leaf_execution_group)
        valid_leaf_execution_group = (
            isinstance(leaf_execution_group, str)
            and leaf_execution_group in STAGE0_LEAF_EXECUTION_GROUPS
        )
        if leaf_execution_group is not None and not valid_leaf_execution_group:
            _add_error(
                errors,
                f"{location}.leaf_execution_group",
                f"unknown leaf execution group {leaf_execution_group!r}",
            )
        expected_leaf_execution_group = STAGE0_LEAF_EXECUTION_GROUP_BY_COMPONENT.get(component_id)
        if leaf_execution_group != expected_leaf_execution_group:
            _add_error(
                errors,
                f"{location}.leaf_execution_group",
                f"must be {expected_leaf_execution_group!r} for component {component_id!r}",
            )
        if component.get("contract_var") is not None and not isinstance(component.get("contract_var"), str):
            _add_error(errors, f"{location}.contract_var", "must be a string or null")
        authority = component.get("authority")
        if not _required_mapping_fields(authority, STAGE0_AUTHORITY_FIELDS, f"{location}.authority", errors):
            authority = {}
        if set(authority) != STAGE0_AUTHORITY_FIELDS:
            _add_error(errors, f"{location}.authority", "must contain exactly the reviewed authority fields")
        if authority.get("ceiling") not in STAGE0_AUTHORITY_CEILINGS:
            _add_error(errors, f"{location}.authority.ceiling", f"unknown ceiling {authority.get('ceiling')!r}")
        for field in ("compatibility_only", "canonical_authority", "self_hosted", "release", "seed_retirement"):
            if not isinstance(authority.get(field), bool):
                _add_error(errors, f"{location}.authority.{field}", "must be boolean")
        for field in ("canonical_authority", "self_hosted", "release", "seed_retirement"):
            if authority.get(field) is not False:
                _add_error(errors, f"{location}.authority.{field}", "must be false")

        lane = test.get("lane") if _is_mapping(test) else None
        execution_requires_bootstrap = test.get("execution_requires_bootstrap") if _is_mapping(test) else None
        if lane not in STAGE0_TEST_LANES:
            _add_error(errors, f"{location}.test.lane", f"unknown test lane {lane!r}")
        if (lane == "bootstrap-free") != valid_leaf_execution_group:
            _add_error(
                errors,
                f"{location}.leaf_execution_group",
                "must be non-null exactly for bootstrap-free tests",
            )
        if not isinstance(execution_requires_bootstrap, bool):
            _add_error(errors, f"{location}.test.execution_requires_bootstrap", "must be boolean")
        elif execution_requires_bootstrap != (lane in {"compatibility", "coordinator"}):
            _add_error(errors, f"{location}.test.execution_requires_bootstrap", "does not match test lane")
        expected_compatibility_authority = (
            component_id in STAGE0_COMPATIBILITY_AUTHORITY_COMPONENT_IDS
            or (lane == "compatibility")
        )
        if authority.get("compatibility_only") != expected_compatibility_authority:
            _add_error(
                errors,
                f"{location}.authority.compatibility_only",
                f"must be {expected_compatibility_authority} for the reviewed namespace contract",
            )
        expected_ceiling = "non-authoritative" if expected_compatibility_authority or lane in {"compatibility", "coordinator"} else "none"
        if authority.get("ceiling") != expected_ceiling:
            _add_error(errors, f"{location}.authority.ceiling", f"must be {expected_ceiling!r}")

    if source_paths != sorted(source_paths):
        _add_error(errors, "stage0 component contract.components", "components must be sorted by id")
    ids = list(component_by_id)
    if ids != sorted(ids):
        _add_error(errors, "stage0 component contract.components", "components must be sorted lexically by id")
    if len(set(source_paths)) != len(source_paths):
        _add_error(errors, "stage0 component contract", "source paths must be unique")
    if len(set(test_paths)) != len(test_paths):
        _add_error(errors, "stage0 component contract", "test paths must be unique")
    if source_paths and actual_source_paths != set(source_paths):
        missing_paths = sorted(set(source_paths).difference(actual_source_paths))
        extra_paths = sorted(actual_source_paths.difference(source_paths))
        if missing_paths:
            _add_error(errors, "stage0 component contract", "missing source files: " + ", ".join(missing_paths))
        if extra_paths:
            _add_error(errors, "stage0 component contract", "unregistered source files: " + ", ".join(extra_paths))
    if test_paths and actual_test_paths != set(test_paths):
        missing_paths = sorted(set(test_paths).difference(actual_test_paths))
        extra_paths = sorted(actual_test_paths.difference(test_paths))
        if missing_paths:
            _add_error(errors, "stage0 component contract", "missing component test files: " + ", ".join(missing_paths))
        if extra_paths:
            _add_error(errors, "stage0 component contract", "unregistered component test files: " + ", ".join(extra_paths))

    # Parse every paired file's actual namespace and static :require edges.
    all_namespace_to_id = {**source_namespace_to_id, **test_namespace_to_id}
    for component_id, component in sorted(component_by_id.items()):
        location = f"stage0 component {component_id!r}"
        source = component.get("source", {})
        test = component.get("test", {})
        source_path = source.get("path") if _is_mapping(source) else None
        test_path = test.get("path") if _is_mapping(test) else None
        if not isinstance(source_path, str) or not isinstance(test_path, str):
            continue
        source_file = ROOT / source_path
        test_file = ROOT / test_path
        actual_source_namespace, actual_source_deps = _component_file_dependencies(
            source_file, f"{location}.source", all_namespace_to_id, errors
        )
        actual_test_namespace, actual_test_deps = _component_file_dependencies(
            test_file, f"{location}.test", all_namespace_to_id, errors
        )
        expected_source_namespace = source.get("namespace")
        expected_test_namespace = test.get("namespace")
        if actual_source_namespace != expected_source_namespace:
            _add_error(errors, f"{location}.source.namespace", f"does not match file ns {actual_source_namespace!r}")
        if actual_test_namespace != expected_test_namespace:
            _add_error(errors, f"{location}.test.namespace", f"does not match file ns {actual_test_namespace!r}")
        declared_source_deps = component.get("direct_source_dependencies")
        if isinstance(declared_source_deps, list) and actual_source_deps != declared_source_deps:
            _add_error(errors, f"{location}.direct_source_dependencies", f"must equal static ns :require dependencies {actual_source_deps!r}")
        expected_test_deps = sorted(set(actual_test_deps) | {component_id})
        execution_requires_bootstrap = bool(test.get("execution_requires_bootstrap")) if _is_mapping(test) else False
        if execution_requires_bootstrap:
            expected_test_deps = sorted(set(expected_test_deps) | {"bootstrap"})
        declared_test_deps = component.get("direct_test_dependencies")
        if isinstance(declared_test_deps, list) and expected_test_deps != declared_test_deps:
            _add_error(errors, f"{location}.direct_test_dependencies", f"must equal static test dependencies plus execution edges {expected_test_deps!r}")

    # The leaf source graph is acyclic and only the coordinator may depend on
    # the bootstrap orchestrator.
    source_graph: dict[str, list[str]] = {}
    for component_id, component in component_by_id.items():
        dependencies = component.get("direct_source_dependencies")
        if not _is_string_list(dependencies):
            source_graph[component_id] = []
            continue
        source_graph[component_id] = list(dependencies)
        for dependency in dependencies:
            if dependency not in component_by_id:
                _add_error(errors, f"stage0 component {component_id!r}.direct_source_dependencies", f"unknown component dependency {dependency!r}")
            if component_id != "bootstrap" and dependency == "bootstrap":
                _add_error(errors, f"stage0 component {component_id!r}.direct_source_dependencies", "leaf source components may not depend on bootstrap")
    for cycle in _find_cycles(source_graph):
        _add_error(errors, "stage0 component source graph", "dependency cycle: " + " -> ".join(cycle))

    lanes = [component.get("test", {}).get("lane") for component in component_by_id.values() if _is_mapping(component.get("test"))]
    if lanes.count("bootstrap-free") != STAGE0_COMPONENT_COUNTS["bootstrap_free_tests"]:
        _add_error(errors, "stage0 component test lanes", "must contain exactly 38 bootstrap-free tests")
    if lanes.count("compatibility") != STAGE0_COMPONENT_COUNTS["compatibility_tests"]:
        _add_error(errors, "stage0 component test lanes", "must contain exactly 5 compatibility tests")
    if lanes.count("coordinator") != STAGE0_COMPONENT_COUNTS["coordinator_tests"]:
        _add_error(errors, "stage0 component test lanes", "must contain exactly 1 coordinator test")
    for group, expected_count in STAGE0_LEAF_EXECUTION_GROUP_COUNTS.items():
        actual_count = leaf_execution_groups.count(group)
        if actual_count != expected_count:
            _add_error(
                errors,
                "stage0 component leaf execution groups",
                f"{group!r} must contain exactly {expected_count} components, found {actual_count}",
            )
    non_null_execution_group_count = sum(
        group in STAGE0_LEAF_EXECUTION_GROUPS for group in leaf_execution_groups
    )
    if non_null_execution_group_count != STAGE0_COMPONENT_COUNTS["bootstrap_free_tests"]:
        _add_error(
            errors,
            "stage0 component leaf execution groups",
            "must contain exactly 38 grouped bootstrap-free components",
        )

    reserved, compatibility_tests, ownership_errors = parse_stage0_component_ownership()
    for ownership_error in ownership_errors:
        _add_error(errors, "stage0 ownership projection", ownership_error)
    if not ownership_errors:
        expected_reserved = {
            component_id: component.get("owner")
            for component_id, component in component_by_id.items()
            if isinstance(component.get("owner"), str)
        }
        if reserved != expected_reserved:
            _add_error(errors, "stage0 ownership projection", "reserved-leaf-modules must equal component id -> owner exactly")
        expected_compatibility_tests = sorted(
            component["test"]["path"]
            for component in component_by_id.values()
            if _is_mapping(component.get("test"))
            and component["test"].get("lane") == "compatibility"
        )
        if compatibility_tests != expected_compatibility_tests:
            _add_error(errors, "stage0 ownership projection", "bootstrap-compatibility-tests must equal compatibility leaf test paths in lexical order")

    ownership = manifest.get("ownership")
    module_paths = ownership.get("module_paths") if _is_mapping(ownership) else None
    path_policies = manifest.get("path_policy", {}).get("policies") if _is_mapping(manifest.get("path_policy")) else None
    if not _is_mapping(module_paths):
        _add_error(errors, "stage0 ownership projection", "project module_paths are unavailable")
        module_paths = {}
    if not isinstance(path_policies, list):
        _add_error(errors, "stage0 ownership projection", "project path policies are unavailable")
        path_policies = []
    stage_paths: set[str] = set()
    for component_id, component in component_by_id.items():
        owner = component.get("owner")
        for section_name in ("source", "test"):
            section = component.get(section_name)
            path = section.get("path") if _is_mapping(section) else None
            if not isinstance(path, str):
                continue
            stage_paths.add(path)
            if module_paths.get(path) != owner:
                _add_error(errors, "stage0 ownership projection", f"project module_paths owner mismatch for {path!r}: expected {owner!r}")
            if reserved.get(component_id) != owner:
                _add_error(errors, "stage0 ownership projection", f"reserved owner mismatch for {component_id!r}")
            claims = _stage0_path_claims(path_policies, path)
            if len(claims) != 1:
                _add_error(errors, "stage0 ownership projection", f"{path!r} must have exactly one path-policy claim, found {claims!r}")
            elif claims[0][1] != owner:
                _add_error(errors, "stage0 ownership projection", f"path-policy owner mismatch for {path!r}: {claims[0][1]!r} vs {owner!r}")
    extra_stage_module_paths = sorted(
        path for path in module_paths
        if (path.startswith("bootstrap/clojure/src/") or path.startswith("bootstrap/clojure/test/"))
        and path not in stage_paths
    )
    for path in extra_stage_module_paths:
        _add_error(errors, "stage0 ownership projection", f"project has unregistered Stage0 module path {path!r}")
    missing_stage_module_paths = sorted(stage_paths.difference(module_paths))
    for path in missing_stage_module_paths:
        _add_error(errors, "stage0 ownership projection", f"project is missing Stage0 module path {path!r}")

    source_contracts = manifest.get("source_contracts")
    fixed_source_contract = {
        "id": "STAGE0-CLOJURE-COMPONENTS",
        "path": "contracts/stage0-clojure-components.json",
        "role": "reviewed Stage0 Clojure component inventory and ownership",
    }
    if not isinstance(source_contracts, list) or source_contracts.count(fixed_source_contract) != 1:
        _add_error(errors, "stage0 ownership projection", "project source_contracts must contain the fixed Stage0 component contract exactly once")


def _path_pattern_matches(pattern: str, path: str) -> bool:
    if pattern.endswith("/"):
        return path.startswith(pattern)
    if _has_pattern_operator(pattern):
        return fnmatch.fnmatchcase(path, pattern)
    return pattern == path


def _validate_normative_ownership_parity(
    manifest: Mapping[str, Any], errors: list[str]
) -> None:
    """Ensure coordinator paths in the normative EDN have the same owner here."""

    (
        central_routing,
        generated_evidence_prefixes,
        integration_surfaces,
        module_owners,
        parse_errors,
    ) = parse_normative_ownership()
    for parse_error in parse_errors:
        _add_error(errors, "normative ownership parity", parse_error)
    if parse_errors:
        return
    module_paths = manifest.get("ownership", {}).get("module_paths") if _is_mapping(manifest.get("ownership")) else None
    if not _is_mapping(module_paths):
        _add_error(errors, "normative ownership parity", "manifest module owner projection is unavailable")
        return
    for module_path, normative_owner in sorted(module_owners.items()):
        manifest_owner = module_paths.get(module_path)
        if manifest_owner is None:
            _add_error(errors, "normative ownership parity", f"missing manifest module owner for {module_path!r}")
        elif manifest_owner != normative_owner:
            _add_error(
                errors,
                "normative ownership parity",
                f"module owner mismatch for {module_path!r}: normative {normative_owner!r}, manifest {manifest_owner!r}",
            )
    extra_module_paths = sorted(set(module_paths).difference(module_owners))
    for module_path in extra_module_paths:
        _add_error(errors, "normative ownership parity", f"manifest has non-normative module owner path {module_path!r}")
    expected_paths = set(integration_surfaces)
    expected_paths.update(
        path for path, owner in module_owners.items() if owner == "master-coordinator"
    )
    if not expected_paths:
        _add_error(errors, "normative ownership parity", "normative coordinator path projection is empty")
        return
    path_policy = manifest.get("path_policy")
    policies = path_policy.get("policies") if _is_mapping(path_policy) else None
    if not isinstance(policies, list):
        _add_error(errors, "normative ownership parity", "manifest path policies are unavailable")
        return
    policies_by_id = {
        policy.get("id"): policy
        for policy in policies
        if _is_mapping(policy) and isinstance(policy.get("id"), str)
    }
    for policy_id, normative_patterns, required_semantics in (
        (
            "reviewed-central-routing",
            central_routing,
            {
                "owner": "master-coordinator",
                "kind": "reviewed",
                "editable": True,
                "review_required": True,
                "reviewer": "master-coordinator",
            },
        ),
        (
            "generated-evidence",
            generated_evidence_prefixes,
            {
                "owner": "master-coordinator",
                "kind": "generated",
                "editable": False,
                "review_required": True,
            },
        ),
    ):
        policy = policies_by_id.get(policy_id)
        manifest_patterns = policy.get("patterns") if _is_mapping(policy) else None
        if manifest_patterns != normative_patterns:
            _add_error(
                errors,
                "normative ownership parity",
                f"policy {policy_id!r} patterns differ from normative ownership: "
                f"normative {normative_patterns!r}, manifest {manifest_patterns!r}",
            )
        if _is_mapping(policy):
            for field, expected_value in required_semantics.items():
                if policy.get(field) != expected_value:
                    _add_error(
                        errors,
                        "normative ownership parity",
                        f"policy {policy_id!r} must set {field!r} to {expected_value!r}",
                    )
            if policy_id == "generated-evidence" and not isinstance(policy.get("generator"), str):
                _add_error(
                    errors,
                    "normative ownership parity",
                    "policy 'generated-evidence' must name its coordinator-owned generator",
                )
    for expected_path in sorted(expected_paths):
        claims: list[tuple[str, str]] = []
        for policy in policies:
            if not _is_mapping(policy) or not _is_string_list(policy.get("patterns")):
                continue
            if any(_path_pattern_matches(pattern, expected_path) for pattern in policy["patterns"]):
                owner = policy.get("owner")
                if isinstance(owner, str):
                    claims.append((policy.get("id", "<unknown>"), owner))
        if not claims:
            _add_error(errors, "normative ownership parity", f"missing manifest claim for coordinator path {expected_path!r}")
            continue
        non_coordinator = sorted({owner for _, owner in claims if owner != "master-coordinator"})
        if non_coordinator:
            _add_error(
                errors,
                "normative ownership parity",
                f"coordinator path {expected_path!r} is claimed by non-coordinator owner(s): {', '.join(non_coordinator)}",
            )


def _validate_cost(value: Any, location: str, errors: list[str]) -> None:
    if not _is_mapping(value):
        _add_error(errors, location, "cost must be an object")
        return
    cost_class = value.get("class")
    if cost_class not in PASS_COST_CLASSES:
        _add_error(errors, location, f"unknown cost class {cost_class!r}")
    if not isinstance(value.get("lane"), str) or not value.get("lane"):
        _add_error(errors, location, "cost lane must be a non-empty string")
    if not isinstance(value.get("resource"), str) or not value.get("resource"):
        _add_error(errors, location, "cost resource must be a non-empty string")


def _validate_authority(value: Any, location: str, errors: list[str]) -> None:
    if not _is_mapping(value):
        _add_error(errors, location, "authority must be an object")
        return
    if "level" in value:
        _add_error(errors, location, "use required_level; level would imply current authority")
    if value.get("required_level") not in AUTHORITY_LEVELS:
        _add_error(errors, location, f"unknown required authority level {value.get('required_level')!r}")
    if value.get("maximum_claim") not in MAXIMUM_CLAIMS:
        _add_error(errors, location, f"unknown maximum claim {value.get('maximum_claim')!r}")
    if not isinstance(value.get("scope"), str) or not value.get("scope"):
        _add_error(errors, location, "authority scope must be a non-empty string")
    _validate_string_list(value.get("evidence"), f"{location}.evidence", errors, allow_empty=False)
    if not isinstance(value.get("human_review_required"), bool):
        _add_error(errors, location, "human_review_required must be boolean")


def _validate_passes(
    manifest: Mapping[str, Any],
    pass_ids: set[str],
    artifact_ids: set[str],
    owner_ids: set[str],
    errors: list[str],
) -> None:
    passes = manifest.get("canonical_passes")
    if not isinstance(passes, list):
        _add_error(errors, "canonical_passes", "must be a list")
        return
    _validate_dependency_graph(passes, "depends_on", "canonical_passes", errors)
    actual_order: list[str] = []
    for index, item in enumerate(passes):
        location = f"canonical_passes[{index}]"
        if not _required_mapping_fields(item, PASS_FIELDS, location, errors):
            continue
        pass_id = item.get("id")
        if not _validate_id(pass_id, f"{location}.id", errors):
            continue
        actual_order.append(pass_id)
        if not isinstance(item.get("order"), int) or isinstance(item.get("order"), bool):
            _add_error(errors, f"{location}.order", "must be an integer")
        elif item["order"] != index + 1:
            _add_error(
                errors,
                f"{location}.order",
                f"must be canonical position {index + 1}, got {item['order']}",
            )
        for field in ("input_artifacts", "output_artifacts"):
            _validate_id_list(item.get(field), f"{location}.{field}", errors, artifact_ids)
        for field in ("requires", "preserves", "invalidates", "emits", "rejects"):
            _validate_string_list(item.get(field), f"{location}.{field}", errors)
        if item.get("owner") not in owner_ids:
            _add_error(errors, f"{location}.owner", f"unknown owner {item.get('owner')!r}")
        _validate_cost(item.get("cost"), f"{location}.cost", errors)
        _validate_authority(item.get("authority"), f"{location}.authority", errors)
    if actual_order != list(CANONICAL_PASS_IDS):
        _add_error(
            errors,
            "canonical_passes",
            "canonical ordering must be exactly " + " -> ".join(CANONICAL_PASS_IDS),
        )
    if pass_ids != set(CANONICAL_PASS_IDS):
        missing = sorted(set(CANONICAL_PASS_IDS).difference(pass_ids))
        unexpected = sorted(pass_ids.difference(CANONICAL_PASS_IDS))
        if missing:
            _add_error(errors, "canonical_passes", "missing canonical passes: " + ", ".join(missing))
        if unexpected:
            _add_error(errors, "canonical_passes", "unexpected passes: " + ", ".join(unexpected))


def _validate_artifacts(
    manifest: Mapping[str, Any],
    pass_ids: set[str],
    artifact_ids: set[str],
    errors: list[str],
) -> None:
    artifacts = manifest.get("artifacts")
    if not isinstance(artifacts, list):
        _add_error(errors, "artifacts", "must be a list")
        return
    for index, item in enumerate(artifacts):
        location = f"artifacts[{index}]"
        required = {"id", "name", "kind", "produced_by", "consumed_by", "provenance_required"}
        if not _required_mapping_fields(item, required, location, errors):
            continue
        _validate_id(item.get("id"), f"{location}.id", errors)
        for field in ("produced_by", "consumed_by"):
            _validate_id_list(item.get(field), f"{location}.{field}", errors, pass_ids)
        if not isinstance(item.get("name"), str) or not item.get("name"):
            _add_error(errors, f"{location}.name", "must be a non-empty string")
        if not isinstance(item.get("kind"), str) or not item.get("kind"):
            _add_error(errors, f"{location}.kind", "must be a non-empty string")
        if not isinstance(item.get("provenance_required"), bool):
            _add_error(errors, f"{location}.provenance_required", "must be boolean")
    # Every canonical pass must publish an artifact boundary, and every
    # artifact must be reachable from at least one pass unless it is source.
    for artifact in artifacts if isinstance(artifacts, list) else []:
        if not _is_mapping(artifact) or not isinstance(artifact.get("id"), str):
            continue
        if not artifact.get("produced_by") and artifact.get("id") != "source-forms":
            _add_error(errors, f"artifacts[{artifact.get('id')}].produced_by", "non-source artifact needs a producer")


def _validate_artifact_continuity(
    manifest: Mapping[str, Any], errors: list[str]
) -> None:
    """Ensure each pass boundary agrees with the artifact producer/consumer graph."""

    passes = manifest.get("canonical_passes")
    artifacts = manifest.get("artifacts")
    if not isinstance(passes, list) or not isinstance(artifacts, list):
        return
    pass_by_id = {
        item.get("id"): item
        for item in passes
        if _is_mapping(item) and isinstance(item.get("id"), str)
    }
    artifact_by_id = {
        item.get("id"): item
        for item in artifacts
        if _is_mapping(item) and isinstance(item.get("id"), str)
    }
    for pass_id, item in pass_by_id.items():
        for artifact_id in item.get("input_artifacts", []):
            artifact = artifact_by_id.get(artifact_id)
            if artifact is not None and pass_id not in artifact.get("consumed_by", []):
                _add_error(
                    errors,
                    f"canonical pass {pass_id!r}",
                    f"input artifact {artifact_id!r} does not list this pass as a consumer",
                )
        for artifact_id in item.get("output_artifacts", []):
            artifact = artifact_by_id.get(artifact_id)
            if artifact is not None and pass_id not in artifact.get("produced_by", []):
                _add_error(
                    errors,
                    f"canonical pass {pass_id!r}",
                    f"output artifact {artifact_id!r} does not list this pass as a producer",
                )
    for artifact_id, artifact in artifact_by_id.items():
        for producer in artifact.get("produced_by", []):
            pass_item = pass_by_id.get(producer)
            if pass_item is not None and artifact_id not in pass_item.get("output_artifacts", []):
                _add_error(
                    errors,
                    f"artifact {artifact_id!r}",
                    f"producer {producer!r} does not publish this artifact",
                )
        for consumer in artifact.get("consumed_by", []):
            pass_item = pass_by_id.get(consumer)
            if pass_item is not None and artifact_id not in pass_item.get("input_artifacts", []):
                _add_error(
                    errors,
                    f"artifact {artifact_id!r}",
                    f"consumer {consumer!r} does not declare this artifact as an input",
                )


def _validate_slices(
    manifest: Mapping[str, Any],
    artifact_ids: set[str],
    owner_ids: set[str],
    policy_ids: set[str],
    errors: list[str],
) -> None:
    slices = manifest.get("slices")
    if not isinstance(slices, list):
        _add_error(errors, "slices", "must be a list")
        return
    slice_ids = _validate_dependency_graph(slices, "depends_on", "slices", errors)
    for index, item in enumerate(slices):
        location = f"slices[{index}]"
        required = {"id", "title", "depends_on", "status", "owner", "artifact_inputs", "artifact_outputs", "cost", "authority", "path_policy_ids"}
        if not _required_mapping_fields(item, required, location, errors):
            continue
        _validate_id(item.get("id"), f"{location}.id", errors)
        if not isinstance(item.get("title"), str) or not item.get("title"):
            _add_error(errors, f"{location}.title", "must be a non-empty string")
        if item.get("status") not in SLICE_STATUSES:
            _add_error(errors, f"{location}.status", f"unknown slice status {item.get('status')!r}")
        if item.get("owner") not in owner_ids:
            _add_error(errors, f"{location}.owner", f"unknown owner {item.get('owner')!r}")
        for field in ("artifact_inputs", "artifact_outputs"):
            _validate_id_list(item.get(field), f"{location}.{field}", errors, artifact_ids)
        _validate_id_list(item.get("path_policy_ids"), f"{location}.path_policy_ids", errors, policy_ids)
        _validate_cost(item.get("cost"), f"{location}.cost", errors)
        _validate_authority(item.get("authority"), f"{location}.authority", errors)
    expected = {f"SH-{index:02d}" for index in range(30)}
    missing = sorted(expected.difference(slice_ids))
    unexpected = sorted(slice_ids.difference(expected))
    if missing:
        _add_error(errors, "slices", "missing backlog slices: " + ", ".join(missing))
    if unexpected:
        _add_error(errors, "slices", "unexpected slice ids: " + ", ".join(unexpected))


def _validate_ownership_and_paths(
    manifest: Mapping[str, Any], errors: list[str]
) -> tuple[set[str], set[str], dict[str, Mapping[str, Any]]]:
    ownership = manifest.get("ownership")
    if not _is_mapping(ownership):
        _add_error(errors, "ownership", "must be an object")
        return set(), set(), {}
    categories = ownership.get("categories")
    owners = ownership.get("owners")
    if not isinstance(categories, list):
        _add_error(errors, "ownership.categories", "must be a list")
        categories = []
    if not isinstance(owners, list):
        _add_error(errors, "ownership.owners", "must be a list")
        owners = []
    category_ids: set[str] = set()
    for index, item in enumerate(categories):
        location = f"ownership.categories[{index}]"
        if not _required_mapping_fields(item, {"id", "description"}, location, errors):
            continue
        if _validate_id(item.get("id"), f"{location}.id", errors):
            category_ids.add(item["id"])
        if not isinstance(item.get("description"), str) or not item.get("description"):
            _add_error(errors, f"{location}.description", "must be a non-empty string")
    owner_ids: set[str] = set()
    owner_policies: dict[str, list[str]] = {}
    for index, item in enumerate(owners):
        location = f"ownership.owners[{index}]"
        if not _required_mapping_fields(item, {"id", "category", "exclusive", "path_policy_ids"}, location, errors):
            continue
        if _validate_id(item.get("id"), f"{location}.id", errors):
            owner_ids.add(item["id"])
            owner_policies[item["id"]] = list(item.get("path_policy_ids", [])) if _is_string_list(item.get("path_policy_ids")) else []
        if item.get("category") not in category_ids:
            _add_error(errors, f"{location}.category", f"unknown ownership category {item.get('category')!r}")
        if not isinstance(item.get("exclusive"), bool):
            _add_error(errors, f"{location}.exclusive", "must be boolean")
        _validate_string_list(item.get("path_policy_ids"), f"{location}.path_policy_ids", errors)

    module_paths = ownership.get("module_paths")
    if not _is_mapping(module_paths):
        _add_error(errors, "ownership.module_paths", "must be an object")
    else:
        for source_path, owner in module_paths.items():
            location = f"ownership.module_paths[{source_path!r}]"
            _validate_relative_pattern(source_path, location, errors)
            if _has_pattern_operator(source_path):
                _add_error(errors, location, "module owner paths must be exact paths")
            if owner not in owner_ids:
                _add_error(errors, location, f"unknown owner {owner!r}")

    path_policy = manifest.get("path_policy")
    if not _is_mapping(path_policy):
        _add_error(errors, "path_policy", "must be an object")
        return owner_ids, set(), {}
    for field in ("reviewed_definition", "generated_definition", "reserved_definition"):
        if not isinstance(path_policy.get(field), str) or not path_policy.get(field):
            _add_error(errors, f"path_policy.{field}", "must be a non-empty string")
    policies = path_policy.get("policies")
    if not isinstance(policies, list):
        _add_error(errors, "path_policy.policies", "must be a list")
        return owner_ids, set(), {}
    policy_ids: set[str] = set()
    policy_by_id: dict[str, Mapping[str, Any]] = {}
    for index, item in enumerate(policies):
        location = f"path_policy.policies[{index}]"
        required = {"id", "kind", "owner", "patterns", "editable", "review_required", "allow_overlap"}
        if not _required_mapping_fields(item, required, location, errors):
            continue
        if _validate_id(item.get("id"), f"{location}.id", errors):
            policy_ids.add(item["id"])
            policy_by_id[item["id"]] = item
        kind = item.get("kind")
        if kind not in PATH_KINDS:
            _add_error(errors, f"{location}.kind", f"unknown path policy kind {kind!r}")
        owner = item.get("owner")
        if owner not in owner_ids:
            _add_error(errors, f"{location}.owner", f"unknown owner {owner!r}")
        if not _is_string_list(item.get("patterns")) or not item.get("patterns"):
            _add_error(errors, f"{location}.patterns", "must be a non-empty list of strings")
        else:
            for pattern_index, pattern in enumerate(item["patterns"]):
                _validate_relative_pattern(pattern, f"{location}.patterns[{pattern_index}]", errors)
        for field in ("editable", "review_required", "allow_overlap"):
            if not isinstance(item.get(field), bool):
                _add_error(errors, f"{location}.{field}", "must be boolean")
        if kind == "reviewed":
            if item.get("editable") is not True or item.get("review_required") is not True:
                _add_error(errors, location, "reviewed policy must be editable and review_required")
            if not isinstance(item.get("reviewer"), str) or not item.get("reviewer"):
                _add_error(errors, location, "reviewed policy needs a reviewer")
        elif kind == "generated":
            if item.get("editable") is not False:
                _add_error(errors, location, "generated policy must set editable=false")
            if not isinstance(item.get("generator"), str) or not item.get("generator"):
                _add_error(errors, location, "generated policy needs a generator")
        elif kind == "reserved":
            if not isinstance(item.get("reservation"), str) or not item.get("reservation"):
                _add_error(errors, location, "reserved policy needs a reservation description")

    for owner, references in owner_policies.items():
        for policy_id in references:
            if policy_id not in policy_ids:
                _add_error(errors, f"ownership owner {owner!r}", f"unknown path policy {policy_id!r}")
            elif policy_by_id[policy_id].get("owner") != owner:
                _add_error(
                    errors,
                    f"ownership owner {owner!r}",
                    f"path policy {policy_id!r} is owned by {policy_by_id[policy_id].get('owner')!r}",
                )
    referenced_by: dict[str, list[str]] = {}
    for owner, references in owner_policies.items():
        for policy_id in references:
            referenced_by.setdefault(policy_id, []).append(owner)
    for policy_id, owners_for_policy in referenced_by.items():
        if len(owners_for_policy) > 1:
            _add_error(errors, f"path policy {policy_id!r}", "claimed by multiple owners: " + ", ".join(owners_for_policy))

    # Different owners may never overlap. Same-owner overlap is only allowed
    # for intentionally nested generated policies with explicit opt-in.
    for left_index, left in enumerate(policies):
        if not _is_mapping(left) or not _is_string_list(left.get("patterns")):
            continue
        for right in policies[left_index + 1 :]:
            if not _is_mapping(right) or not _is_string_list(right.get("patterns")):
                continue
            for left_pattern in left["patterns"]:
                for right_pattern in right["patterns"]:
                    if not _patterns_overlap(left_pattern, right_pattern):
                        continue
                    same_owner = left.get("owner") == right.get("owner")
                    same_kind = left.get("kind") == right.get("kind")
                    explicitly_allowed = bool(left.get("allow_overlap")) and bool(right.get("allow_overlap"))
                    if not same_owner:
                        _add_error(
                            errors,
                            "path_policy.policies",
                            f"ownership overlap: {left.get('id')} ({left_pattern}) and {right.get('id')} ({right_pattern}) have different owners",
                        )
                    elif not (same_kind and explicitly_allowed):
                        _add_error(
                            errors,
                            "path_policy.policies",
                            f"unapproved overlap: {left.get('id')} ({left_pattern}) and {right.get('id')} ({right_pattern})",
                        )
    return owner_ids, policy_ids, policy_by_id


def validate_manifest(manifest: Any) -> list[str]:
    """Return all structural errors in a project structure manifest."""

    errors: list[str] = []
    if not _is_mapping(manifest):
        return ["manifest: must be a JSON object"]
    missing = sorted(REQUIRED_TOP_LEVEL.difference(manifest))
    if missing:
        _add_error(errors, "manifest", "missing required fields: " + ", ".join(missing))
    if manifest.get("schema_version") != SCHEMA_VERSION:
        _add_error(errors, "schema_version", f"must be {SCHEMA_VERSION}")
    _validate_id(manifest.get("manifest_id"), "manifest_id", errors)
    _collect_ids(manifest, errors)

    source_contracts = manifest.get("source_contracts")
    if not isinstance(source_contracts, list) or not source_contracts:
        _add_error(errors, "source_contracts", "must be a non-empty list")
    else:
        for index, item in enumerate(source_contracts):
            location = f"source_contracts[{index}]"
            if not _required_mapping_fields(item, {"id", "path", "role"}, location, errors):
                continue
            _validate_id(item.get("id"), f"{location}.id", errors)
            for field in ("path", "role"):
                if not isinstance(item.get(field), str) or not item.get(field):
                    _add_error(errors, f"{location}.{field}", "must be a non-empty string")
            _validate_relative_pattern(item.get("path"), f"{location}.path", errors)

    passes = manifest.get("canonical_passes")
    pass_ids = {
        item.get("id")
        for item in passes
        if isinstance(passes, list) and _is_mapping(item) and isinstance(item.get("id"), str)
    }
    artifacts = manifest.get("artifacts")
    artifact_ids = {
        item.get("id")
        for item in artifacts
        if isinstance(artifacts, list) and _is_mapping(item) and isinstance(item.get("id"), str)
    }
    owner_ids, policy_ids, _ = _validate_ownership_and_paths(manifest, errors)
    _validate_passes(manifest, pass_ids, artifact_ids, owner_ids, errors)
    _validate_artifacts(manifest, pass_ids, artifact_ids, errors)
    _validate_artifact_continuity(manifest, errors)
    _validate_slices(manifest, artifact_ids, owner_ids, policy_ids, errors)
    _validate_normative_ownership_parity(manifest, errors)
    _validate_stage0_component_contract(manifest, errors)
    return errors


def load_manifest(path: Path = DEFAULT_MANIFEST) -> Any:
    """Load JSON from ``path`` with a useful manifest-specific error."""

    try:
        raw = path.read_bytes()
        if len(raw) > MAX_OWNERSHIP_EDN_BYTES * 4:
            raise ManifestError(f"project structure manifest exceeds {MAX_OWNERSHIP_EDN_BYTES * 4} bytes")
        return _strict_json_loads(raw.decode("utf-8"))
    except OSError as exc:
        raise ManifestError(f"cannot read manifest {path}: {exc}") from exc
    except ManifestError:
        raise
    except (UnicodeDecodeError, TypeError, ValueError, json.JSONDecodeError) as exc:
        raise ManifestError(f"invalid strict JSON in {path}: {exc}") from exc


def main(argv: Sequence[str] | None = None) -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument(
        "manifest",
        nargs="?",
        type=Path,
        default=DEFAULT_MANIFEST,
        help="project structure JSON manifest (default: contracts/project-structure.json)",
    )
    args = parser.parse_args(argv)
    try:
        manifest = load_manifest(args.manifest)
    except ManifestError as exc:
        print(f"project structure validation failed: {exc}", file=sys.stderr)
        return 1
    errors = validate_manifest(manifest)
    if errors:
        print("project structure validation failed:", file=sys.stderr)
        for error in errors:
            print(f"- {error}", file=sys.stderr)
        return 1
    pass_count = len(manifest.get("canonical_passes", []))
    slice_count = len(manifest.get("slices", []))
    policy_count = len(manifest.get("path_policy", {}).get("policies", []))
    print(
        "project structure validation passed: "
        f"{pass_count} canonical passes, {slice_count} slices, {policy_count} path policies"
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
