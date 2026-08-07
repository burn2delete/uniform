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
MAX_OWNERSHIP_EDN_BYTES = 512 * 1024
SCHEMA_VERSION = 1

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


def parse_normative_ownership(
    path: Path = NORMATIVE_OWNERSHIP,
) -> tuple[list[str], dict[str, str], list[str]]:
    """Project only coordinator ownership facts from the normative EDN.

    This is deliberately not a permissive EDN parser.  The parity contract
    depends on two simple, stable shapes: a string vector under
    ``:integration-surfaces`` and a string-to-keyword map under
    ``:module-owners``.  Any missing marker, duplicate marker, non-ASCII input,
    or token outside those shapes is an error rather than a best-effort parse.
    """

    errors: list[str] = []
    try:
        raw = path.read_bytes()
    except OSError as exc:
        return [], {}, [f"cannot read normative ownership EDN {path}: {exc}"]
    if len(raw) > MAX_OWNERSHIP_EDN_BYTES:
        return [], {}, [f"normative ownership EDN exceeds {MAX_OWNERSHIP_EDN_BYTES} bytes"]
    try:
        text = raw.decode("utf-8")
    except UnicodeDecodeError as exc:
        return [], {}, [f"normative ownership EDN is not UTF-8: {exc}"]
    try:
        text.encode("ascii")
    except UnicodeEncodeError:
        errors.append("normative ownership EDN must be ASCII")
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
        return [], {}, errors

    coordinator_start = coordinator_matches[0].end()
    module_start = module_matches[0].start()
    if module_start <= coordinator_start:
        return [], {}, ["normative ownership EDN coordinator/module owner order is unrecognized"]
    coordinator_section = text[coordinator_start:module_start]
    integration_surfaces = _extract_normative_string_vector(
        coordinator_section, "integration-surfaces", errors
    )

    reserved_matches = list(re.finditer(r":reserved-leaf-modules\s*\{", text))
    if len(reserved_matches) != 1 or reserved_matches[0].start() <= module_matches[0].end():
        errors.append("normative ownership EDN must delimit :module-owners before :reserved-leaf-modules")
        return integration_surfaces, {}, errors
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
    return integration_surfaces, module_owners, errors


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

    integration_surfaces, module_owners, parse_errors = parse_normative_ownership()
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
    return errors


def load_manifest(path: Path = DEFAULT_MANIFEST) -> Any:
    """Load JSON from ``path`` with a useful manifest-specific error."""

    try:
        return json.loads(path.read_text(encoding="utf-8"))
    except OSError as exc:
        raise ManifestError(f"cannot read manifest {path}: {exc}") from exc
    except json.JSONDecodeError as exc:
        raise ManifestError(f"invalid JSON in {path}: {exc}") from exc


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
