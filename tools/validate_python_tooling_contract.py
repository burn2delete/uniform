#!/usr/bin/env python3
"""Validate the fail-closed Python tooling-layer contract.

This checker is deliberately standard-library-only.  It classifies the Python
working inventory, checks the static import/effect graph, and verifies that the
contract does not turn tooling metadata into compiler or release authority.
Static AST inspection cannot establish behavior hidden behind dynamic imports
or dynamically selected call targets; the contract and tools README state that
limit explicitly.
"""

from __future__ import annotations

import argparse
import ast
import copy
import fnmatch
import hashlib
import json
from pathlib import Path, PurePosixPath
import re
import subprocess
import sys
from typing import Any, Mapping, Sequence


ROOT = Path(__file__).resolve().parents[1]
DEFAULT_CONTRACT = ROOT / "contracts" / "python-tooling.json"
SCHEMA_VERSION = 1
MAX_CONTRACT_BYTES = 2 * 1024 * 1024
MAX_SOURCE_BYTES = 4 * 1024 * 1024
MAX_AST_DEPTH = 256
SHA256_RE = re.compile(r"^sha256:[0-9a-f]{64}$")
ID_RE = re.compile(r"^[a-z][a-z0-9-]*$")

TOP_LEVEL_FIELDS = {
    "schema_version",
    "contract_id",
    "description",
    "scope",
    "enums",
    "policies",
    "components",
    "constraints",
    "readme_contract",
}
SCOPE_FIELDS = {
    "roots",
    "excluded_segments",
    "inventory_count",
    "inventory_sha256",
    "dependency_edge_count",
    "dependency_edge_sha256",
}
ENUM_FIELDS = {
    "categories",
    "roles",
    "effects",
    "output_classes",
    "authority_ceilings",
    "import_safety_modes",
    "execution_modes",
    "test_surfaces",
    "policy_kinds",
}
POLICY_FIELDS = {
    "id",
    "kind",
    "patterns",
    "authorizes_edits",
    "review_required",
    "blocking",
    "external_contract",
    "external_policy_id",
}
COMPONENT_FIELDS = {
    "id",
    "includes",
    "excludes",
    "category",
    "role",
    "allowed_dependency_categories",
    "effects",
    "output_classes",
    "authority_ceiling",
    "source_path_policy_refs",
    "output_path_policy_refs",
    "import_safety",
    "execution_mode",
    "test_surfaces",
}
CONSTRAINT_FIELDS = {
    "python_authority_granted",
    "allowed_dependency_cycles",
    "semantic_categories",
    "semantic_forbidden_effects",
    "semantic_forbidden_import_roots",
    "network_forbidden",
    "validator_pattern",
    "isolated_output_options",
    "output_publication_path",
    "isolated_output_classes",
    "isolated_output_policy_refs",
    "reviewed_source_roles",
    "reviewed_source_output_class",
    "reviewed_source_execution_mode",
    "reviewed_source_paths",
    "semantic_root",
    "tooling_root",
    "test_pattern",
    "unresolved_semantic_policy",
}
README_FIELDS = {"path", "required_statements"}

WRITE_CALLS = {
    "Path.write_text",
    "Path.write_bytes",
    "Path.mkdir",
    "Path.unlink",
    "Path.rename",
    "Path.replace",
    "Path.rmdir",
    "Path.touch",
    "Path.chmod",
    "Path.symlink_to",
    "Path.hardlink_to",
    "os.mkdir",
    "os.makedirs",
    "os.rmdir",
    "os.removedirs",
    "os.unlink",
    "os.remove",
    "os.rename",
    "os.replace",
    "os.write",
    "os.chmod",
    "os.fchmod",
    "os.truncate",
    "os.ftruncate",
    "os.symlink",
    "os.link",
    "os.mknod",
    "os.mkfifo",
    "tempfile.NamedTemporaryFile",
    "tempfile.TemporaryFile",
    "tempfile.SpooledTemporaryFile",
    "tempfile.TemporaryDirectory",
    "tempfile.mkstemp",
    "tempfile.mkdtemp",
    "shutil.copy",
    "shutil.copy2",
    "shutil.copyfile",
    "shutil.copytree",
    "shutil.make_archive",
    "shutil.unpack_archive",
    "shutil.move",
    "shutil.rmtree",
}
READ_CALLS = {
    "Path.read_text",
    "Path.read_bytes",
    "os.read",
    "json.load",
}
PROCESS_CALLS = {
    "subprocess.run",
    "subprocess.Popen",
    "subprocess.call",
    "subprocess.check_call",
    "subprocess.check_output",
    "os.system",
    "os.execv",
    "os.execve",
    "os.spawnv",
    "os.spawnve",
    "os.spawnvp",
    "os.spawnvpe",
    "os.posix_spawn",
    "os.posix_spawnp",
    "os.fork",
    "os.forkpty",
    "os.popen",
}
SIGNAL_CALLS = {
    "os.kill",
    "os.killpg",
    "signal.signal",
    "signal.pthread_kill",
}
CLOCK_CALLS = {
    "time.time",
    "time.monotonic",
    "time.perf_counter",
    "time.sleep",
    "datetime.now",
    "datetime.datetime.now",
}
METRIC_CALLS = {"resource.getrusage", "os.getloadavg"}
NETWORK_IMPORT_ROOTS = {
    "aiohttp",
    "http",
    "requests",
    "socket",
    "urllib",
    "urllib3",
}
class DuplicateKeyError(ValueError):
    """Raised when JSON contains an ambiguous duplicate key."""


def _object_no_duplicates(pairs: list[tuple[str, Any]]) -> dict[str, Any]:
    result: dict[str, Any] = {}
    for key, value in pairs:
        if key in result:
            raise DuplicateKeyError(f"duplicate JSON key {key!r}")
        result[key] = value
    return result


def load_json(path: Path) -> dict[str, Any]:
    data = path.read_bytes()
    if len(data) > MAX_CONTRACT_BYTES:
        raise ValueError(f"{path}: exceeds {MAX_CONTRACT_BYTES} bytes")
    value = json.loads(data.decode("utf-8"), object_pairs_hook=_object_no_duplicates)
    if not isinstance(value, dict):
        raise ValueError(f"{path}: top level must be an object")
    return value


def _error(errors: list[str], location: str, message: str) -> None:
    errors.append(f"{location}: {message}")


def _exact_fields(
    value: Any, expected: set[str], location: str, errors: list[str]
) -> bool:
    if not isinstance(value, Mapping):
        _error(errors, location, "must be an object")
        return False
    missing = sorted(expected.difference(value))
    unknown = sorted(set(value).difference(expected))
    if missing:
        _error(errors, location, f"missing fields: {', '.join(missing)}")
    if unknown:
        _error(errors, location, f"unknown fields: {', '.join(unknown)}")
    return not missing and not unknown


def _string_list(
    value: Any,
    location: str,
    errors: list[str],
    *,
    allow_empty: bool = True,
    unique: bool = True,
) -> bool:
    if not isinstance(value, list) or not all(isinstance(item, str) for item in value):
        _error(errors, location, "must be a list of strings")
        return False
    if not allow_empty and not value:
        _error(errors, location, "must not be empty")
        return False
    if unique and len(value) != len(set(value)):
        _error(errors, location, "must not contain duplicates")
        return False
    return True


def _safe_relative_path(value: Any, location: str, errors: list[str]) -> bool:
    if not isinstance(value, str) or not value:
        _error(errors, location, "must be a non-empty repository-relative path")
        return False
    if "\\" in value or any(ord(character) < 32 for character in value):
        _error(errors, location, "must use safe POSIX separators")
        return False
    path = PurePosixPath(value)
    if path.is_absolute() or value.startswith("./") or ".." in path.parts:
        _error(errors, location, "must be normalized and repository-relative")
        return False
    if str(path) != value.rstrip("/"):
        _error(errors, location, "must be normalized")
        return False
    return True


def _safe_pattern(value: Any, location: str, errors: list[str]) -> bool:
    if not _safe_relative_path(value, location, errors):
        return False
    if "[" in value or "]" in value:
        _error(errors, location, "character-class globs are forbidden")
        return False
    return True


def _canonical_digest(lines: Sequence[str]) -> str:
    material = "".join(f"{line}\n" for line in sorted(lines)).encode("utf-8")
    return "sha256:" + hashlib.sha256(material).hexdigest()


def discover_python_inventory(root: Path) -> list[str]:
    """Return tracked plus intentional untracked Python candidates.

    Including untracked, non-ignored files makes the check useful before a
    coordinator stages a change.  Once admitted, the same set is the tracked
    inventory pinned by the contract digest.
    """

    completed = subprocess.run(
        [
            "git",
            "-C",
            str(root),
            "ls-files",
            "-z",
            "--cached",
            "--others",
            "--exclude-standard",
            "--",
            "tools",
            "src/gravity",
        ],
        check=True,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        timeout=10,
    )
    result = []
    for encoded in completed.stdout.split(b"\0"):
        if not encoded:
            continue
        raw = encoded.decode("utf-8", errors="strict")
        path = PurePosixPath(raw)
        if path.suffix != ".py" or "__pycache__" in path.parts:
            continue
        result.append(str(path))
    return sorted(set(result))


def _module_names(paths: Sequence[str]) -> dict[str, str]:
    names: dict[str, str] = {}
    for path in paths:
        pure = PurePosixPath(path)
        if path.startswith("src/"):
            module = str(pure)[4:-3].replace("/", ".")
        else:
            module = str(pure)[:-3].replace("/", ".")
        if module.endswith(".__init__"):
            module = module[: -len(".__init__")]
        names[module] = path
        if path.startswith("tools/"):
            # Existing command modules intentionally use a tools/ sys.path and
            # therefore import siblings by their basename.
            names.setdefault(pure.stem, path)
    return names


def _import_names(node: ast.AST, path: str) -> list[str]:
    if isinstance(node, ast.Import):
        return [alias.name for alias in node.names]
    if isinstance(node, ast.ImportFrom):
        base = node.module or ""
        if node.level and path.startswith("tools/"):
            base = f"tools.{base}" if base else "tools"
        return [base]
    return []


def _resolve_internal_dependencies(
    path: str, tree: ast.AST, module_names: Mapping[str, str]
) -> list[str]:
    dependencies: set[str] = set()
    for node in ast.walk(tree):
        for imported in _import_names(node, path):
            candidate = imported
            while candidate:
                target = module_names.get(candidate)
                if target is not None and target != path:
                    dependencies.add(target)
                    break
                candidate = candidate.rsplit(".", 1)[0] if "." in candidate else ""
    return sorted(dependencies)


def _call_name(node: ast.AST) -> str | None:
    if isinstance(node, ast.Name):
        return node.id
    if isinstance(node, ast.Attribute):
        parts: list[str] = []
        current: ast.AST = node
        while isinstance(current, ast.Attribute):
            parts.append(current.attr)
            current = current.value
        if isinstance(current, ast.Name):
            parts.append(current.id)
            return ".".join(reversed(parts))
        if isinstance(current, ast.Call) and isinstance(current.func, ast.Name):
            parts.append(current.func.id)
            return ".".join(reversed(parts))
    return None


def _static_aliases(tree: ast.AST) -> dict[str, str]:
    aliases: dict[str, str] = {}
    for node in ast.iter_child_nodes(tree):
        if isinstance(node, ast.Import):
            for item in node.names:
                aliases[item.asname or item.name.split(".", 1)[0]] = item.name
        elif isinstance(node, ast.ImportFrom) and node.module:
            for item in node.names:
                if item.name != "*":
                    aliases[item.asname or item.name] = f"{node.module}.{item.name}"
        elif isinstance(node, (ast.Assign, ast.AnnAssign)):
            value = node.value
            targets = node.targets if isinstance(node, ast.Assign) else [node.target]
            if value is None or len(targets) != 1 or not isinstance(targets[0], ast.Name):
                continue
            rendered = _call_name(value)
            if rendered:
                first, separator, rest = rendered.partition(".")
                base = aliases.get(first, first)
                aliases[targets[0].id] = base + (separator + rest if separator else "")
    return aliases


def _normalized_call_name(
    node: ast.Call, aliases: Mapping[str, str] | None = None
) -> str | None:
    name = _call_name(node.func)
    if name is None:
        return None
    first, separator, rest = name.partition(".")
    if aliases and first in aliases:
        name = aliases[first] + (separator + rest if separator else "")
    if name.endswith(tuple(f".{suffix}" for suffix in ("read_text", "read_bytes"))):
        return "Path." + name.rsplit(".", 1)[1]
    if name.endswith(
        tuple(
            f".{suffix}"
            for suffix in (
                "write_text",
                "write_bytes",
                "mkdir",
                "unlink",
                "rename",
                "rmdir",
                "touch",
                "chmod",
                "symlink_to",
                "hardlink_to",
            )
        )
    ):
        return "Path." + name.rsplit(".", 1)[1]
    return name


def _open_effect(node: ast.Call) -> str:
    mode: str | None = None
    if len(node.args) >= 2 and isinstance(node.args[1], ast.Constant):
        mode = node.args[1].value if isinstance(node.args[1].value, str) else None
    for keyword in node.keywords:
        if keyword.arg == "mode" and isinstance(keyword.value, ast.Constant):
            mode = keyword.value.value if isinstance(keyword.value.value, str) else None
    return "filesystem-write" if mode and any(mark in mode for mark in "wax+") else "filesystem-read"


def observed_effects(
    tree: ast.AST, *, aliases: Mapping[str, str] | None = None
) -> set[str]:
    effects: set[str] = set()
    known_aliases = dict(aliases) if aliases is not None else _static_aliases(tree)
    for node in ast.walk(tree):
        if isinstance(node, (ast.Import, ast.ImportFrom)):
            for imported in _import_names(node, ""):
                if imported.split(".", 1)[0] in NETWORK_IMPORT_ROOTS:
                    effects.add("network")
        if isinstance(node, ast.Attribute):
            rendered = _call_name(node) or ""
            first, separator, rest = rendered.partition(".")
            if first in known_aliases:
                rendered = known_aliases[first] + (separator + rest if separator else "")
            if rendered.startswith("os.environ"):
                effects.add("environment")
        if not isinstance(node, ast.Call):
            continue
        name = _normalized_call_name(node, known_aliases)
        if name is None:
            continue
        if name in {"open", "Path.open"} or name.endswith(".open"):
            effects.add(_open_effect(node))
            if name == "os.open":
                # Integer flag expressions are intentionally not interpreted;
                # conservatively include write authority.
                effects.add("filesystem-write")
        if name in READ_CALLS:
            effects.add("filesystem-read")
        if name in WRITE_CALLS:
            effects.add("filesystem-write")
        if name in PROCESS_CALLS:
            effects.add("process")
        if name in SIGNAL_CALLS:
            effects.add("signal")
        if name in CLOCK_CALLS or name.endswith(".now"):
            effects.add("clock")
        if name in METRIC_CALLS:
            effects.add("host-metrics")
        if name in {"os.getenv", "os.getlogin", "platform.platform", "platform.system"}:
            effects.add("environment")
        if name == "print" or name in {"sys.stdout.write", "sys.stderr.write"}:
            effects.add("stdout")
    return effects


def _is_main_guard(node: ast.stmt) -> bool:
    if not isinstance(node, ast.If) or not isinstance(node.test, ast.Compare):
        return False
    comparison = node.test
    if len(comparison.ops) != 1 or not isinstance(comparison.ops[0], ast.Eq):
        return False
    if len(comparison.comparators) != 1:
        return False
    pair = (comparison.left, comparison.comparators[0])
    return any(
        isinstance(left, ast.Name)
        and left.id == "__name__"
        and isinstance(right, ast.Constant)
        and right.value == "__main__"
        for left, right in (pair, tuple(reversed(pair)))
    )


def _has_main_guard(tree: ast.Module) -> bool:
    return any(_is_main_guard(node) for node in tree.body)


def _top_level_observed_effects(tree: ast.Module) -> set[str]:
    shell = ast.Module(body=[], type_ignores=[])
    for node in tree.body:
        if isinstance(
            node,
            (
                ast.FunctionDef,
                ast.AsyncFunctionDef,
                ast.ClassDef,
                ast.Import,
                ast.ImportFrom,
            ),
        ):
            continue
        if _is_main_guard(node):
            continue
        shell.body.append(copy.deepcopy(node))
    return observed_effects(shell, aliases=_static_aliases(tree))


def _argument_options(tree: ast.AST) -> set[str]:
    options: set[str] = set()
    for node in ast.walk(tree):
        if not isinstance(node, ast.Call):
            continue
        name = _call_name(node.func)
        if name is None or not name.endswith("add_argument"):
            continue
        for argument in node.args:
            if isinstance(argument, ast.Constant) and isinstance(argument.value, str):
                if argument.value.startswith("--"):
                    options.add(argument.value)
    return options


def _import_roots(tree: ast.AST, path: str) -> set[str]:
    roots: set[str] = set()
    for node in ast.walk(tree):
        for name in _import_names(node, path):
            if name:
                roots.add(name.split(".", 1)[0])
    return roots


def _component_matches(component: Mapping[str, Any], path: str) -> bool:
    included = any(fnmatch.fnmatchcase(path, pattern) for pattern in component["includes"])
    excluded = any(fnmatch.fnmatchcase(path, pattern) for pattern in component["excludes"])
    return included and not excluded


def _cycle(graph: Mapping[str, Sequence[str]]) -> list[str] | None:
    visited: set[str] = set()
    active: list[str] = []

    def visit(node: str) -> list[str] | None:
        if node in active:
            start = active.index(node)
            return active[start:] + [node]
        if node in visited:
            return None
        active.append(node)
        for dependency in graph.get(node, []):
            found = visit(dependency)
            if found:
                return found
        active.pop()
        visited.add(node)
        return None

    for node in sorted(graph):
        found = visit(node)
        if found:
            return found
    return None


def _validate_enums(contract: Mapping[str, Any], errors: list[str]) -> dict[str, set[str]]:
    raw = contract.get("enums")
    if not _exact_fields(raw, ENUM_FIELDS, "enums", errors):
        return {}
    assert isinstance(raw, Mapping)
    enums: dict[str, set[str]] = {}
    for name in sorted(ENUM_FIELDS):
        value = raw.get(name)
        if _string_list(value, f"enums.{name}", errors, allow_empty=False):
            assert isinstance(value, list)
            for index, item in enumerate(value):
                if not ID_RE.fullmatch(item):
                    _error(errors, f"enums.{name}[{index}]", "must be a kebab-case id")
            enums[name] = set(value)
    return enums


def _validate_scope(contract: Mapping[str, Any], errors: list[str]) -> None:
    scope = contract.get("scope")
    if not _exact_fields(scope, SCOPE_FIELDS, "scope", errors):
        return
    assert isinstance(scope, Mapping)
    if _string_list(scope.get("roots"), "scope.roots", errors, allow_empty=False):
        for index, path in enumerate(scope["roots"]):
            _safe_relative_path(path, f"scope.roots[{index}]", errors)
    _string_list(scope.get("excluded_segments"), "scope.excluded_segments", errors)
    for name in ("inventory_count", "dependency_edge_count"):
        value = scope.get(name)
        if not isinstance(value, int) or isinstance(value, bool) or value < 0:
            _error(errors, f"scope.{name}", "must be a non-negative integer")
    for name in ("inventory_sha256", "dependency_edge_sha256"):
        value = scope.get(name)
        if not isinstance(value, str) or not SHA256_RE.fullmatch(value):
            _error(errors, f"scope.{name}", "must be a lowercase sha256 identity")


def _project_policies(root: Path, errors: list[str]) -> dict[str, Mapping[str, Any]]:
    try:
        project = load_json(root / "contracts" / "project-structure.json")
        policies = project["path_policy"]["policies"]
    except (OSError, ValueError, KeyError, TypeError) as exc:
        _error(errors, "policies", f"cannot read project structure policies: {exc}")
        return {}
    result: dict[str, Mapping[str, Any]] = {}
    if not isinstance(policies, list):
        _error(errors, "policies", "project structure policies must be a list")
        return result
    for policy in policies:
        if isinstance(policy, Mapping) and isinstance(policy.get("id"), str):
            result[policy["id"]] = policy
    return result


def _validate_policies(
    contract: Mapping[str, Any],
    enums: Mapping[str, set[str]],
    root: Path,
    errors: list[str],
) -> dict[str, Mapping[str, Any]]:
    raw = contract.get("policies")
    if not isinstance(raw, list):
        _error(errors, "policies", "must be a list")
        return {}
    external = _project_policies(root, errors)
    result: dict[str, Mapping[str, Any]] = {}
    for index, policy in enumerate(raw):
        location = f"policies[{index}]"
        if not _exact_fields(policy, POLICY_FIELDS, location, errors):
            continue
        assert isinstance(policy, Mapping)
        identifier = policy.get("id")
        if not isinstance(identifier, str) or not ID_RE.fullmatch(identifier):
            _error(errors, f"{location}.id", "must be a kebab-case id")
            continue
        if identifier in result:
            _error(errors, f"{location}.id", f"duplicate policy {identifier!r}")
        result[identifier] = policy
        if policy.get("kind") not in enums.get("policy_kinds", set()):
            _error(errors, f"{location}.kind", "must be a declared policy kind")
        patterns = policy.get("patterns")
        if _string_list(patterns, f"{location}.patterns", errors, allow_empty=False):
            assert isinstance(patterns, list)
            for pattern_index, pattern in enumerate(patterns):
                _safe_pattern(pattern, f"{location}.patterns[{pattern_index}]", errors)
        for name in ("authorizes_edits", "review_required", "blocking"):
            if not isinstance(policy.get(name), bool):
                _error(errors, f"{location}.{name}", "must be boolean")
        external_contract = policy.get("external_contract")
        external_id = policy.get("external_policy_id")
        if (external_contract is None) != (external_id is None):
            _error(errors, location, "external_contract and external_policy_id must appear together")
        if external_contract is not None:
            if external_contract != "contracts/project-structure.json":
                _error(errors, f"{location}.external_contract", "unsupported external contract")
            target = external.get(external_id)
            if target is None:
                _error(errors, f"{location}.external_policy_id", f"unknown external policy {external_id!r}")
            elif target.get("kind") != policy.get("kind"):
                _error(errors, location, "external policy kind differs")
            elif isinstance(patterns, list):
                external_patterns = target.get("patterns", [])
                for pattern in patterns:
                    if pattern not in external_patterns:
                        _error(errors, f"{location}.patterns", f"pattern {pattern!r} is not declared by external policy")
        if policy.get("kind") == "unresolved":
            if policy.get("authorizes_edits") is not False:
                _error(errors, location, "unresolved policy must not authorize edits")
            if policy.get("blocking") is not True or policy.get("review_required") is not True:
                _error(errors, location, "unresolved policy must be blocking and review-required")
    return result


def _validate_components(
    contract: Mapping[str, Any],
    enums: Mapping[str, set[str]],
    policies: Mapping[str, Mapping[str, Any]],
    inventory: Sequence[str],
    errors: list[str],
) -> tuple[dict[str, Mapping[str, Any]], dict[str, Mapping[str, Any]]]:
    raw = contract.get("components")
    if not isinstance(raw, list):
        _error(errors, "components", "must be a list")
        return {}, {}
    components: dict[str, Mapping[str, Any]] = {}
    for index, component in enumerate(raw):
        location = f"components[{index}]"
        if not _exact_fields(component, COMPONENT_FIELDS, location, errors):
            continue
        assert isinstance(component, Mapping)
        identifier = component.get("id")
        if not isinstance(identifier, str) or not ID_RE.fullmatch(identifier):
            _error(errors, f"{location}.id", "must be a kebab-case id")
            continue
        if identifier in components:
            _error(errors, f"{location}.id", f"duplicate component {identifier!r}")
        components[identifier] = component
        for list_name in (
            "includes",
            "excludes",
            "allowed_dependency_categories",
            "effects",
            "output_classes",
            "source_path_policy_refs",
            "output_path_policy_refs",
            "test_surfaces",
        ):
            allow_empty = list_name not in {"includes", "source_path_policy_refs", "test_surfaces"}
            if _string_list(component.get(list_name), f"{location}.{list_name}", errors, allow_empty=allow_empty):
                if list_name in {"includes", "excludes"}:
                    for pattern_index, pattern in enumerate(component[list_name]):
                        _safe_pattern(pattern, f"{location}.{list_name}[{pattern_index}]", errors)
        enum_fields = {
            "category": "categories",
            "role": "roles",
            "authority_ceiling": "authority_ceilings",
            "import_safety": "import_safety_modes",
            "execution_mode": "execution_modes",
        }
        for field, enum_name in enum_fields.items():
            if component.get(field) not in enums.get(enum_name, set()):
                _error(errors, f"{location}.{field}", f"must be declared in enums.{enum_name}")
        list_enum_fields = {
            "allowed_dependency_categories": "categories",
            "effects": "effects",
            "output_classes": "output_classes",
            "test_surfaces": "test_surfaces",
        }
        for field, enum_name in list_enum_fields.items():
            values = component.get(field)
            if isinstance(values, list):
                for value in values:
                    if value not in enums.get(enum_name, set()):
                        _error(errors, f"{location}.{field}", f"unknown {enum_name} value {value!r}")
        for field in ("source_path_policy_refs", "output_path_policy_refs"):
            values = component.get(field)
            if isinstance(values, list):
                for value in values:
                    if value not in policies:
                        _error(errors, f"{location}.{field}", f"unknown policy {value!r}")
        output_classes = component.get("output_classes")
        if isinstance(output_classes, list) and "none" in output_classes and len(output_classes) != 1:
            _error(errors, f"{location}.output_classes", "none must be the only output class")
        source_refs = component.get("source_path_policy_refs")
        if isinstance(source_refs, list):
            for reference in source_refs:
                policy = policies.get(reference)
                if policy is not None and policy.get("kind") == "generated":
                    _error(errors, f"{location}.source_path_policy_refs", "generated policy cannot classify source")
                if policy is not None and policy.get("kind") == "unresolved":
                    if component.get("authority_ceiling") != "none":
                        _error(errors, location, "unresolved source policy requires authority ceiling none")
        output_refs = component.get("output_path_policy_refs")
        if isinstance(output_refs, list):
            for reference in output_refs:
                policy = policies.get(reference)
                if policy is not None and policy.get("authorizes_edits") is not False:
                    _error(errors, f"{location}.output_path_policy_refs", "output policy must not authorize edits")

    path_components: dict[str, Mapping[str, Any]] = {}
    for path in inventory:
        matches = [component for component in components.values() if _component_matches(component, path)]
        if len(matches) != 1:
            _error(errors, f"inventory[{path}]", f"must match exactly one component; matched {len(matches)}")
            continue
        component = matches[0]
        path_components[path] = component
        source_refs = component.get("source_path_policy_refs", [])
        covering = []
        for reference in source_refs if isinstance(source_refs, list) else []:
            policy = policies.get(reference)
            if policy and any(
                fnmatch.fnmatchcase(path, pattern) or path.startswith(pattern)
                for pattern in policy.get("patterns", [])
            ):
                covering.append(reference)
        if not covering:
            _error(errors, f"inventory[{path}]", "no referenced source policy covers path")
        constraints = contract.get("constraints", {})
        semantic_root = constraints.get("semantic_root") if isinstance(constraints, Mapping) else None
        tooling_root = constraints.get("tooling_root") if isinstance(constraints, Mapping) else None
        test_pattern = constraints.get("test_pattern") if isinstance(constraints, Mapping) else None
        if isinstance(semantic_root, str) and path.startswith(semantic_root.rstrip("/") + "/"):
            if component.get("category") not in {"semantic", "semantic-coverage"}:
                _error(errors, f"inventory[{path}]", "semantic-root path has a non-semantic category")
        if isinstance(tooling_root, str) and path.startswith(tooling_root.rstrip("/") + "/"):
            if component.get("source_path_policy_refs") != ["reviewed-central-routing"]:
                _error(errors, f"inventory[{path}]", "tooling-root path must retain reviewed-central-routing")
        if isinstance(test_pattern, str) and fnmatch.fnmatchcase(path, test_pattern):
            if component.get("category") != "test" or component.get("role") != "unit-test":
                _error(errors, f"inventory[{path}]", "test path must retain the unit-test classification")

    for identifier, component in components.items():
        if not any(_component_matches(component, path) for path in inventory):
            _error(errors, f"components[{identifier}]", "matches no inventory path")
    return components, path_components


def _parse_sources(
    root: Path,
    inventory: Sequence[str],
    errors: list[str],
    source_overrides: Mapping[str, str] | None,
) -> dict[str, ast.Module]:
    trees: dict[str, ast.Module] = {}
    for path in inventory:
        try:
            candidate = root / path
            overridden = source_overrides is not None and path in source_overrides
            if not overridden:
                if candidate.is_symlink():
                    raise ValueError("symlinked Python source is forbidden")
                resolved = candidate.resolve(strict=True)
                resolved.relative_to(root.resolve())
            data = (
                source_overrides[path].encode("utf-8")
                if overridden and source_overrides is not None
                else candidate.read_bytes()
            )
            if len(data) > MAX_SOURCE_BYTES:
                raise ValueError(f"source exceeds {MAX_SOURCE_BYTES} bytes")
            source = data.decode("utf-8", errors="strict")
            tree = ast.parse(source, filename=path)
            stack: list[tuple[ast.AST, int]] = [(tree, 1)]
            while stack:
                node, depth = stack.pop()
                if depth > MAX_AST_DEPTH:
                    raise ValueError(f"AST depth exceeds {MAX_AST_DEPTH}")
                stack.extend((child, depth + 1) for child in ast.iter_child_nodes(node))
            trees[path] = tree
        except (OSError, UnicodeError, SyntaxError, ValueError, RecursionError, MemoryError) as exc:
            _error(errors, f"inventory[{path}]", f"cannot parse Python source: {exc}")
    return trees


def _validate_graph_and_ast(
    contract: Mapping[str, Any],
    inventory: Sequence[str],
    path_components: Mapping[str, Mapping[str, Any]],
    trees: Mapping[str, ast.Module],
    errors: list[str],
) -> None:
    scope = contract.get("scope", {})
    constraints = contract.get("constraints", {})
    module_names = _module_names(inventory)
    graph: dict[str, list[str]] = {}
    edge_lines: list[str] = []
    for path, tree in trees.items():
        dependencies = _resolve_internal_dependencies(path, tree, module_names)
        graph[path] = dependencies
        component = path_components.get(path)
        if component is None:
            continue
        allowed_categories = set(component.get("allowed_dependency_categories", []))
        for dependency in dependencies:
            target_component = path_components.get(dependency)
            edge_lines.append(f"{path}\t{dependency}")
            if target_component is None:
                _error(errors, f"dependencies[{path}]", f"unclassified target {dependency!r}")
            elif target_component.get("category") not in allowed_categories:
                _error(
                    errors,
                    f"dependencies[{path}]",
                    f"forbidden category edge to {dependency!r} ({target_component.get('category')})",
                )
        declared_effects = set(component.get("effects", []))
        observed = observed_effects(tree)
        undeclared = sorted(observed.difference(declared_effects))
        if undeclared:
            _error(errors, f"effects[{path}]", f"observed undeclared effects: {', '.join(undeclared)}")
        mode = component.get("import_safety")
        guard = _has_main_guard(tree)
        if mode in {"guarded-cli", "test-module"} and not guard:
            _error(errors, f"import-safety[{path}]", f"{mode} lacks __main__ guard")
        if mode == "library" and guard:
            _error(errors, f"import-safety[{path}]", "library must not expose a CLI main guard")
        top_effects = _top_level_observed_effects(tree)
        if top_effects:
            _error(errors, f"import-safety[{path}]", f"top-level effects: {', '.join(sorted(top_effects))}")

        category = component.get("category")
        if category in set(constraints.get("semantic_categories", [])):
            forbidden_effects = set(constraints.get("semantic_forbidden_effects", []))
            present_forbidden = sorted(declared_effects.intersection(forbidden_effects))
            if present_forbidden:
                _error(errors, f"semantic[{path}]", f"declares forbidden effects: {', '.join(present_forbidden)}")
            observed_forbidden = sorted(observed.intersection(forbidden_effects))
            if observed_forbidden:
                _error(errors, f"semantic[{path}]", f"observed forbidden effects: {', '.join(observed_forbidden)}")
            forbidden_roots = set(constraints.get("semantic_forbidden_import_roots", []))
            imported_forbidden = sorted(_import_roots(tree, path).intersection(forbidden_roots))
            if imported_forbidden:
                _error(errors, f"semantic[{path}]", f"imports forbidden roots: {', '.join(imported_forbidden)}")
        if constraints.get("network_forbidden") is True and "network" in observed:
            _error(errors, f"effects[{path}]", "network use is forbidden throughout this contract")

        imports = set(dependencies)
        publication_path = constraints.get("output_publication_path")
        output_classes = set(component.get("output_classes", []))
        output_refs = set(component.get("output_path_policy_refs", []))
        isolated_classes = output_classes.intersection(
            constraints.get("isolated_output_classes", [])
        )
        aliases = _static_aliases(tree)
        call_names = {
            name
            for node in ast.walk(tree)
            if isinstance(node, ast.Call)
            for name in [_normalized_call_name(node, aliases)]
            if name is not None
        }
        if isolated_classes:
            if publication_path not in imports:
                _error(errors, f"output-policy[{path}]", "isolated output lacks output_publication dependency")
            expected_policies = {
                "generated-evidence-isolated": "generated-evidence",
                "generated-coverage-isolated": "generated-coverage",
            }
            for output_class in sorted(isolated_classes):
                expected_policy = expected_policies.get(output_class)
                if expected_policy is None or expected_policy not in output_refs:
                    _error(errors, f"output-policy[{path}]", f"{output_class} lacks its matching output policy")
            if not any(
                name == "atomic_write_json"
                or name.endswith(".atomic_write_json")
                or name == "atomic_write_text"
                or name.endswith(".atomic_write_text")
                for name in call_names
            ):
                _error(errors, f"output-policy[{path}]", "isolated output never calls a shared atomic writer")
            if "filesystem-write" in observed:
                _error(errors, f"output-policy[{path}]", "isolated output performs direct filesystem writes")

        if fnmatch.fnmatchcase(path, str(constraints.get("validator_pattern", ""))):
            options = _argument_options(tree)
            isolated_options = set(constraints.get("isolated_output_options", []))
            has_output = bool(options.intersection(isolated_options))
            if has_output:
                if publication_path not in imports:
                    _error(errors, f"output-policy[{path}]", "output option lacks output_publication dependency")
                if not output_classes.intersection(constraints.get("isolated_output_classes", [])):
                    _error(errors, f"output-policy[{path}]", "output option lacks isolated output class")
                if not output_refs.intersection(constraints.get("isolated_output_policy_refs", [])):
                    _error(errors, f"output-policy[{path}]", "output option lacks isolated output policy")
            elif publication_path in imports:
                _error(errors, f"output-policy[{path}]", "output_publication import lacks a declared output option")

    if isinstance(scope, Mapping):
        if len(edge_lines) != scope.get("dependency_edge_count"):
            _error(errors, "scope.dependency_edge_count", f"expected {scope.get('dependency_edge_count')}, found {len(edge_lines)}")
        digest = _canonical_digest(edge_lines)
        if digest != scope.get("dependency_edge_sha256"):
            _error(errors, "scope.dependency_edge_sha256", f"expected {scope.get('dependency_edge_sha256')}, found {digest}")
    cycle_graph = {path: list(dependencies) for path, dependencies in graph.items()}
    allowed_cycles = constraints.get("allowed_dependency_cycles", [])
    if not isinstance(allowed_cycles, list):
        _error(errors, "constraints.allowed_dependency_cycles", "must be a list")
        allowed_cycles = []
    for index, cycle in enumerate(allowed_cycles):
        location = f"constraints.allowed_dependency_cycles[{index}]"
        if (
            not isinstance(cycle, list)
            or len(cycle) < 3
            or cycle[0] != cycle[-1]
            or any(not isinstance(path, str) for path in cycle)
        ):
            _error(errors, location, "must be a closed list of repository-relative Python paths")
            continue
        missing_edges = [
            f"{source} -> {target}"
            for source, target in zip(cycle, cycle[1:])
            if target not in graph.get(source, [])
        ]
        if missing_edges:
            _error(errors, location, "declares absent edges: " + ", ".join(missing_edges))
            continue
        cycle_graph[cycle[0]].remove(cycle[1])
    found_cycle = _cycle(cycle_graph)
    if found_cycle:
        _error(errors, "dependencies", "cycle: " + " -> ".join(found_cycle))


def _validate_constraints(
    contract: Mapping[str, Any],
    components: Mapping[str, Mapping[str, Any]],
    policies: Mapping[str, Mapping[str, Any]],
    errors: list[str],
) -> None:
    constraints = contract.get("constraints")
    if not _exact_fields(constraints, CONSTRAINT_FIELDS, "constraints", errors):
        return
    assert isinstance(constraints, Mapping)
    if constraints.get("python_authority_granted") is not False:
        _error(errors, "constraints.python_authority_granted", "must remain false")
    if not isinstance(constraints.get("allowed_dependency_cycles"), list):
        _error(errors, "constraints.allowed_dependency_cycles", "must be a list")
    if constraints.get("network_forbidden") is not True:
        _error(errors, "constraints.network_forbidden", "must remain true")
    for name in (
        "semantic_categories",
        "semantic_forbidden_effects",
        "semantic_forbidden_import_roots",
        "isolated_output_options",
        "isolated_output_classes",
        "isolated_output_policy_refs",
        "reviewed_source_roles",
        "reviewed_source_paths",
    ):
        _string_list(constraints.get(name), f"constraints.{name}", errors, allow_empty=False)
    for name in ("semantic_root", "tooling_root"):
        _safe_relative_path(constraints.get(name), f"constraints.{name}", errors)
    _safe_pattern(constraints.get("test_pattern"), "constraints.test_pattern", errors)
    unresolved_id = constraints.get("unresolved_semantic_policy")
    unresolved = policies.get(unresolved_id)
    if unresolved is None or unresolved.get("kind") != "unresolved":
        _error(errors, "constraints.unresolved_semantic_policy", "must name an unresolved policy")
    elif unresolved.get("authorizes_edits") is not False:
        _error(errors, "constraints.unresolved_semantic_policy", "must not authorize edits")

    semantic_categories = set(constraints.get("semantic_categories", []))
    for identifier, component in components.items():
        if component.get("category") not in semantic_categories:
            continue
        if component.get("source_path_policy_refs") != [unresolved_id]:
            _error(errors, f"components[{identifier}]", "semantic source must retain only the unresolved external ownership policy")
        if component.get("authority_ceiling") != "none":
            _error(errors, f"components[{identifier}]", "semantic ownership gap requires authority ceiling none")

    reviewed_roles = set(constraints.get("reviewed_source_roles", []))
    reviewed_paths = set(constraints.get("reviewed_source_paths", []))
    reviewed_output = constraints.get("reviewed_source_output_class")
    reviewed_mode = constraints.get("reviewed_source_execution_mode")
    for identifier, component in components.items():
        if component.get("role") not in reviewed_roles:
            continue
        if reviewed_output not in component.get("output_classes", []):
            _error(errors, f"components[{identifier}]", "reviewed-source generator lacks reviewed-source output class")
        if component.get("execution_mode") != reviewed_mode:
            _error(errors, f"components[{identifier}]", "reviewed-source generator must be coordinator-serialized")
        if component.get("authority_ceiling") != "none":
            _error(errors, f"components[{identifier}]", "reviewed-source generator cannot receive Python authority")
    for path in sorted(reviewed_paths):
        matches = [component for component in components.values() if _component_matches(component, path)]
        if len(matches) != 1 or matches[0].get("role") not in reviewed_roles:
            _error(errors, "constraints.reviewed_source_paths", f"{path!r} must retain reviewed-source-generator role")

    for identifier, component in components.items():
        ceiling = component.get("authority_ceiling")
        if ceiling not in {"none", "non-authoritative-observation"}:
            _error(errors, f"components[{identifier}].authority_ceiling", "Python contract cannot grant authority")


def _validate_readme(
    contract: Mapping[str, Any], root: Path, errors: list[str], readme_text: str | None
) -> None:
    raw = contract.get("readme_contract")
    if not _exact_fields(raw, README_FIELDS, "readme_contract", errors):
        return
    assert isinstance(raw, Mapping)
    path = raw.get("path")
    if not _safe_relative_path(path, "readme_contract.path", errors):
        return
    statements = raw.get("required_statements")
    if not _string_list(statements, "readme_contract.required_statements", errors, allow_empty=False):
        return
    try:
        text = readme_text if readme_text is not None else (root / path).read_text(encoding="utf-8")
    except (OSError, UnicodeError) as exc:
        _error(errors, "readme_contract.path", f"cannot read README: {exc}")
        return
    for statement in statements:
        if text.count(statement) != 1:
            _error(errors, "readme_contract.required_statements", f"must contain exactly once: {statement!r}")


def validate_contract(
    contract: Mapping[str, Any],
    *,
    root: Path = ROOT,
    inventory: Sequence[str] | None = None,
    source_overrides: Mapping[str, str] | None = None,
    readme_text: str | None = None,
) -> list[str]:
    errors: list[str] = []
    _exact_fields(contract, TOP_LEVEL_FIELDS, "contract", errors)
    if contract.get("schema_version") != SCHEMA_VERSION:
        _error(errors, "schema_version", f"must equal {SCHEMA_VERSION}")
    identifier = contract.get("contract_id")
    if not isinstance(identifier, str) or not ID_RE.fullmatch(identifier):
        _error(errors, "contract_id", "must be a kebab-case id")
    if not isinstance(contract.get("description"), str) or not contract.get("description"):
        _error(errors, "description", "must be a non-empty string")
    _validate_scope(contract, errors)
    enums = _validate_enums(contract, errors)
    policies = _validate_policies(contract, enums, root, errors)
    raw_inventory = list(inventory if inventory is not None else discover_python_inventory(root))
    if len(raw_inventory) != len(set(raw_inventory)):
        _error(errors, "inventory", "must not contain duplicate paths")
    current_inventory = sorted(set(raw_inventory))
    for index, path in enumerate(current_inventory):
        _safe_relative_path(path, f"inventory[{index}]", errors)
        if any(character in path for character in "*?"):
            _error(errors, f"inventory[{index}]", "inventory paths must not contain glob metacharacters")
    scope = contract.get("scope", {})
    if isinstance(scope, Mapping):
        excluded = set(scope.get("excluded_segments", []))
        roots = tuple(f"{item.rstrip('/')}" for item in scope.get("roots", []) if isinstance(item, str))
        for path in current_inventory:
            pure = PurePosixPath(path)
            if roots and not any(path == root_path or path.startswith(root_path + "/") for root_path in roots):
                _error(errors, f"inventory[{path}]", "outside declared roots")
            if excluded.intersection(pure.parts):
                _error(errors, f"inventory[{path}]", "contains excluded segment")
        if len(current_inventory) != scope.get("inventory_count"):
            _error(errors, "scope.inventory_count", f"expected {scope.get('inventory_count')}, found {len(current_inventory)}")
        digest = _canonical_digest(current_inventory)
        if digest != scope.get("inventory_sha256"):
            _error(errors, "scope.inventory_sha256", f"expected {scope.get('inventory_sha256')}, found {digest}")
    components, path_components = _validate_components(
        contract, enums, policies, current_inventory, errors
    )
    trees = _parse_sources(root, current_inventory, errors, source_overrides)
    _validate_graph_and_ast(contract, current_inventory, path_components, trees, errors)
    _validate_constraints(contract, components, policies, errors)
    _validate_readme(contract, root, errors, readme_text)
    return sorted(set(errors))


def build_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--contract", type=Path, default=DEFAULT_CONTRACT)
    parser.add_argument("--root", type=Path, default=ROOT)
    return parser


def main(argv: Sequence[str] | None = None) -> int:
    args = build_parser().parse_args(argv)
    try:
        contract = load_json(args.contract)
        errors = validate_contract(contract, root=args.root.resolve())
    except (OSError, UnicodeError, ValueError, subprocess.SubprocessError) as exc:
        print(f"python tooling contract validation failed: {exc}", file=sys.stderr)
        return 1
    if errors:
        print("python tooling contract validation failed:", file=sys.stderr)
        for error in errors:
            print(f"- {error}", file=sys.stderr)
        return 1
    inventory = discover_python_inventory(args.root.resolve())
    print(f"python tooling contract validation passed: {len(inventory)} Python files, static non-authority contract")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
