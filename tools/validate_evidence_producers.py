#!/usr/bin/env python3
"""Validate the bounded, non-authoritative Python evidence-producer inventory."""

from __future__ import annotations

import argparse
import ast
import copy
import fnmatch
import hashlib
import json
import os
from pathlib import Path, PurePosixPath
import re
import stat
import subprocess
import sys
from typing import Any, Mapping, Sequence


ROOT = Path(__file__).resolve().parents[1]
DEFAULT_CONTRACT = ROOT / "contracts" / "evidence-producers.json"
PYTHON_CONTRACT = ROOT / "contracts" / "python-tooling.json"
PROJECT_CONTRACT = ROOT / "contracts" / "project-structure.json"
MAX_JSON_BYTES = 2 * 1024 * 1024
MAX_SOURCE_BYTES = 4 * 1024 * 1024
MAX_JSON_DEPTH = 64
MAX_JSON_NODES = 100_000
MAX_PRODUCERS = 256
SHA256_RE = re.compile(r"^sha256:[0-9a-f]{64}$")
COMMIT_RE = re.compile(r"^[0-9a-f]{40}(?:[0-9a-f]{24})?$")
ID_RE = re.compile(r"^[a-z][a-z0-9-]*$")

TOP_FIELDS = {
    "schema_version", "contract_id", "description", "normative_sources",
    "inventory", "boundaries", "output_policies", "writers", "producers",
    "promotion_policy", "required_nonclaims",
}
INVENTORY_FIELDS = {"root_pattern", "excluded_patterns", "producer_count", "producer_sha256"}
BOUNDARY_FIELDS = {
    "filesystem_producers_only", "compiler_artifact_graph_path",
    "compiler_artifact_graph_is_filesystem_inventory", "publication_primitive_path",
    "publication_primitive_is_producer", "test_writers_are_producers",
    "temporary_scratch_is_producer", "python_can_mint_authority",
}
POLICY_FIELDS = {
    "id", "patterns", "python_tooling_policy", "project_structure_policy",
    "reviewed_source", "isolated_atomic_publication_required",
}
WRITER_FIELDS = {
    "id", "implementation", "mechanisms", "regular_nosymlink_current_owner",
    "atomic_replace", "provenance_added_by_writer",
}
PRODUCER_FIELDS = {
    "id", "sources", "commands", "cli_output_options", "input_patterns", "output_policy_refs",
    "output_patterns", "output_kinds", "schemas", "writer",
    "python_tooling_component", "authority_ceiling", "provenance_required",
    "review", "nonclaims",
}
SOURCE_FIELDS = {"includes", "excludes"}
REVIEW_FIELDS = {"generation_review_required", "direct_reviewed_source", "admission_review_required"}
PROMOTION_POLICY_FIELDS = {
    "record_schema", "tracked_record_patterns", "allowed_statuses",
    "admitted_required_fields", "required_passed_checks",
    "reviewer_must_differ_from_producer_author", "trusted_authority_boundary_present",
    "reviewed_commit_policy",
    "authoritative", "aggregate_authoritative", "release_authoritative",
    "digest_is_signature",
}
PROMOTION_RECORD_FIELDS = {
    "schema", "status", "producer_id", "producer_source", "reviewed_commit",
    "reviewer", "producer_author", "checks", "source_sha256", "output_sha256",
    "output_path", "producer_inventory_sha256", "authoritative", "aggregate_authoritative",
    "release_authoritative", "trusted_authority_boundary", "signature",
}
REQUIRED_PROVENANCE = {"producer-source", "command", "schema", "output-sha256"}
REQUIRED_NONCLAIMS = {"authority", "release"}
EXACT_REQUIRED_NONCLAIMS = [
    "Inventory membership is not evidence authority.",
    "A generated digest is an integrity identity, not a signature or trust root.",
    "A promotion is a review/admission record and cannot mint authority.",
    "Compiler artifact graph nodes are semantic dataflow concepts, not filesystem producer registrations.",
    "Python producers cannot establish aggregate authority, release authority, self-hosting completion, or seed retirement.",
]
EXPECTED_EXCLUDED_PATTERNS = [
    "tools/tests/**",
    "tools/output_publication.py",
    "tools/validate_gravity_toolchain.py",
    "tools/validate_w1_executable_carrier_interface.py",
    "tools/validate_artifact_census.py",
]
EXACT_OUTPUT_POLICY_PROFILES = {
    "generated-evidence": {
        "id": "generated-evidence",
        "patterns": ["target/**", "docs/artifacts/**"],
        "python_tooling_policy": "generated-evidence",
        "project_structure_policy": "generated-evidence",
        "reviewed_source": False,
        "isolated_atomic_publication_required": True,
    },
    "generated-coverage": {
        "id": "generated-coverage",
        "patterns": [
            "docs/artifacts/full-language/coverage/full-language-coverage-matrix.json",
            "docs/artifacts/full-language/coverage/full-language-coverage-gaps.json",
            "docs/artifacts/full-language/reports/full-language-coverage-matrix-report.md",
        ],
        "python_tooling_policy": "generated-coverage",
        "project_structure_policy": "generated-coverage",
        "reviewed_source": False,
        "isolated_atomic_publication_required": True,
    },
    "development-private-state": {
        "id": "development-private-state",
        "patterns": [".cpcache/**"],
        "python_tooling_policy": "development-private-state",
        "project_structure_policy": None,
        "reviewed_source": False,
        "isolated_atomic_publication_required": False,
    },
    "reviewed-document-source": {
        "id": "reviewed-document-source",
        "patterns": ["docs/**"],
        "python_tooling_policy": "unresolved-reviewed-document-source",
        "project_structure_policy": None,
        "reviewed_source": True,
        "isolated_atomic_publication_required": False,
    },
}
EXACT_WRITER_PROFILES = {
    "isolated-atomic-publication": {"id": "isolated-atomic-publication", "implementation": "tools/output_publication.py", "mechanisms": ["atomic_write_json", "atomic_write_text"], "regular_nosymlink_current_owner": True, "atomic_replace": True, "provenance_added_by_writer": False},
    "reviewed-source-rewrite": {"id": "reviewed-source-rewrite", "implementation": "pathlib.Path.write_text", "mechanisms": ["write_text"], "regular_nosymlink_current_owner": False, "atomic_replace": False, "provenance_added_by_writer": False},
    "development-receipt-writer": {"id": "development-receipt-writer", "implementation": "tools/measure_development_baseline.py#_write_receipt", "mechanisms": ["temporary-file-fsync-replace"], "regular_nosymlink_current_owner": False, "atomic_replace": True, "provenance_added_by_writer": True},
    "development-cache-writer": {"id": "development-cache-writer", "implementation": "tools/verify_development.py#_write_json", "mechanisms": ["dirfd-nofollow-fsync-replace"], "regular_nosymlink_current_owner": True, "atomic_replace": True, "provenance_added_by_writer": True},
    "sh07-checkpoint-writer": {"id": "sh07-checkpoint-writer", "implementation": "tools/run_sh07_authoritative_modules.py#atomic_json_write", "mechanisms": ["temporary-file-fsync-replace", "bounded-log"], "regular_nosymlink_current_owner": True, "atomic_replace": True, "provenance_added_by_writer": True},
    "heartbeat-state-writer": {"id": "heartbeat-state-writer", "implementation": "tools/run_with_heartbeat.py#atomic_json_write", "mechanisms": ["temporary-file-fsync-replace", "durable-log"], "regular_nosymlink_current_owner": False, "atomic_replace": True, "provenance_added_by_writer": True},
}
EXACT_PROMOTION_POLICY = {
    "record_schema": "gravity/evidence-promotion-record-v1",
    "tracked_record_patterns": ["docs/artifacts/evidence-promotions/*.json"],
    "allowed_statuses": ["candidate", "admitted", "rejected"],
    "admitted_required_fields": ["reviewed_commit", "reviewer", "producer_author", "checks", "source_sha256", "output_path", "output_sha256", "producer_inventory_sha256"],
    "required_passed_checks": ["producer-inventory", "current-input-digests", "current-output-digest", "review-provenance"],
    "reviewer_must_differ_from_producer_author": True,
    "reviewed_commit_policy": "existing-commit-reachable-ancestor-of-current-head",
    "trusted_authority_boundary_present": False,
    "authoritative": False,
    "aggregate_authoritative": False,
    "release_authoritative": False,
    "digest_is_signature": False,
}
NONPRODUCER_WRITE_EXCEPTIONS = {
    "tools/output_publication.py",
    "tools/validate_gravity_toolchain.py",
    "tools/validate_w1_executable_carrier_interface.py",
}
WRITE_CALL_NAMES = {
    "atomic_write_json", "atomic_write_text", "atomic_json_write", "_write_json",
    "_write_receipt", "write_text", "write_bytes",
}
CLI_OUTPUT_OPTIONS = {
    "--artifact-out", "--coverage-out", "--gaps", "--matrix", "--report",
    "--out", "--output", "--state-dir", "--log", "--status", "--cache", "--receipt",
}
PRODUCER_PROVENANCE_REQUIREMENTS = {
    "isolated-artifact-validators": {"producer-source", "command", "input-identities", "schema", "output-sha256"},
    "isolated-artifact-coverage-validators": {"producer-source", "command", "input-identities", "schema", "output-sha256"},
    "full-language-coverage-generator": {"producer-source", "command", "document-inventory", "input-identities", "schema", "output-sha256"},
    "authoritative-evidence-candidate-composer": {"producer-source", "command", "current-contract-identities", "input-identities", "schema", "output-sha256"},
    "gravity-document-generator": {"producer-source", "command", "reviewed-commit", "reviewer", "source-basis", "input-identities", "schema", "output-sha256"},
    "gravity-document-enricher": {"producer-source", "command", "reviewed-commit", "reviewer", "input-identities", "schema", "output-sha256"},
    "development-baseline-receipt": {"producer-source", "command", "runtime", "environment-bindings", "input-identities", "schema", "output-sha256"},
    "sh07-module-checkpoints": {"producer-source", "runner-identity", "command", "runtime", "input-identities", "dependency-identities", "child-output-identities", "schema", "output-sha256"},
    "long-running-command-heartbeat": {"producer-source", "command", "runtime", "environment-bindings", "input-identities", "status-identity", "schema", "output-sha256"},
    "development-verification-state": {"producer-source", "manifest-identity", "command", "input-identities", "dependency-results", "cache-keys", "schema", "output-sha256"},
}
PRODUCER_SCHEMA_REQUIREMENTS = {
    "isolated-artifact-validators": {"source-defined-json-kind-v1"},
    "isolated-artifact-coverage-validators": {"source-defined-json-kind-v1", "source-defined-document-coverage-kind-v1"},
    "full-language-coverage-generator": {"gravity/full-language-coverage-matrix", "gravity/full-language-coverage-gap-report", "markdown-report-v1"},
    "authoritative-evidence-candidate-composer": {"gravity/authoritative-evidence-candidate-v1", "gravity/reviewed-authority-promotion-candidate-v1"},
    "gravity-document-generator": {"gravity-canonical-document-v1", "gravity-document-inventory-v1"},
    "gravity-document-enricher": {"gravity-canonical-document-v1"},
    "development-baseline-receipt": {"gravity/development-performance-baseline-v1"},
    "sh07-module-checkpoints": {"gravity/sh07-authoritative-module-checkpoints-v2", "opaque-bounded-log-v1"},
    "long-running-command-heartbeat": {"opaque-durable-log-v1", "gravity/long-running-command-status-v1"},
    "development-verification-state": {"gravity/development-verification-cache-v1", "gravity/development-verification-receipt-v1"},
}
WRITER_CALL_REQUIREMENTS = {
    "isolated-atomic-publication": {"atomic_write_json", "atomic_write_text"},
    "reviewed-source-rewrite": {"write_text"},
    "development-receipt-writer": {"_write_receipt"},
    "development-cache-writer": {"_write_json"},
    "sh07-checkpoint-writer": {"atomic_json_write"},
    "heartbeat-state-writer": {"atomic_json_write"},
}


class DuplicateKeyError(ValueError):
    pass


def _object_no_duplicates(pairs: list[tuple[str, Any]]) -> dict[str, Any]:
    result: dict[str, Any] = {}
    for key, value in pairs:
        if key in result:
            raise DuplicateKeyError(f"duplicate JSON key {key!r}")
        result[key] = value
    return result


def _reject_constant(value: str) -> None:
    raise ValueError(f"non-finite JSON number {value!r}")


def _bounded(value: Any) -> None:
    count = 0
    stack = [(value, 1)]
    while stack:
        item, depth = stack.pop()
        count += 1
        if count > MAX_JSON_NODES:
            raise ValueError(f"JSON node count exceeds {MAX_JSON_NODES}")
        if depth > MAX_JSON_DEPTH:
            raise ValueError(f"JSON depth exceeds {MAX_JSON_DEPTH}")
        if isinstance(item, Mapping):
            stack.extend((key, depth + 1) for key in item)
            stack.extend((child, depth + 1) for child in item.values())
        elif isinstance(item, list):
            stack.extend((child, depth + 1) for child in item)


def load_json(path: Path) -> dict[str, Any]:
    data = path.read_bytes()
    if len(data) > MAX_JSON_BYTES:
        raise ValueError(f"{path}: exceeds {MAX_JSON_BYTES} bytes")
    value = json.loads(
        data.decode("utf-8"), object_pairs_hook=_object_no_duplicates,
        parse_constant=_reject_constant,
    )
    if not isinstance(value, dict):
        raise ValueError(f"{path}: top level must be an object")
    _bounded(value)
    return value


def _error(errors: list[str], code: str, location: str, message: str) -> None:
    errors.append(f"{code} {location}: {message}")


def _exact(value: Any, fields: set[str], location: str, errors: list[str]) -> bool:
    if not isinstance(value, Mapping):
        _error(errors, "EP001", location, "must be an object")
        return False
    missing = sorted(fields.difference(value))
    unknown = sorted(set(value).difference(fields))
    if missing:
        _error(errors, "EP001", location, f"missing fields: {', '.join(missing)}")
    if unknown:
        _error(errors, "EP001", location, f"unknown fields: {', '.join(unknown)}")
    return not missing and not unknown


def _strings(value: Any, location: str, errors: list[str], *, empty: bool = False) -> bool:
    if not isinstance(value, list) or not all(isinstance(item, str) for item in value):
        _error(errors, "EP001", location, "must be a list of strings")
        return False
    if not empty and not value:
        _error(errors, "EP001", location, "must not be empty")
    if len(set(value)) != len(value):
        _error(errors, "EP001", location, "must not contain duplicates")
    return True


def _safe_pattern(value: str, location: str, errors: list[str]) -> bool:
    path = PurePosixPath(value)
    if not value or value.startswith("/") or "\\" in value or "\x00" in value or ".." in path.parts:
        _error(errors, "EP002", location, "must be a safe repository-relative pattern")
        return False
    return True


def _literal_prefix(pattern: str) -> str:
    positions = [index for token in "*[?" if (index := pattern.find(token)) >= 0]
    return pattern[: min(positions) if positions else len(pattern)]


def _pattern_within(child: str, parent: str) -> bool:
    child_prefix = _literal_prefix(child)
    parent_prefix = _literal_prefix(parent)
    return child_prefix.startswith(parent_prefix)


def _patterns_overlap(left: str, right: str) -> bool:
    left_prefix = _literal_prefix(left)
    right_prefix = _literal_prefix(right)
    return left_prefix.startswith(right_prefix) or right_prefix.startswith(left_prefix)


def _cli_options(tree: ast.Module) -> set[str]:
    options: set[str] = set()
    for node in ast.walk(tree):
        if not isinstance(node, ast.Call) or not isinstance(node.func, ast.Attribute) or node.func.attr != "add_argument":
            continue
        for argument in node.args:
            if isinstance(argument, ast.Constant) and isinstance(argument.value, str) and argument.value.startswith("--"):
                options.add(argument.value)
    return options.intersection(CLI_OUTPUT_OPTIONS)


def _digest_lines(paths: Sequence[str]) -> str:
    payload = "".join(f"{path}\n" for path in sorted(paths)).encode("utf-8")
    return "sha256:" + hashlib.sha256(payload).hexdigest()


def _sha256_bytes(value: bytes) -> str:
    return "sha256:" + hashlib.sha256(value).hexdigest()


def _read_current_regular(path: Path, root: Path) -> bytes:
    lexical_root = Path(os.path.abspath(root))
    lexical_path = Path(os.path.abspath(path))
    try:
        relative = lexical_path.relative_to(lexical_root)
    except ValueError as exc:
        raise ValueError("path is outside repository root") from exc
    root = lexical_root.resolve(strict=True)
    if not relative.parts or any(part in {"", ".", ".."} for part in relative.parts):
        raise ValueError("path is not canonically repository-relative")
    directory_flags = os.O_RDONLY | os.O_DIRECTORY | getattr(os, "O_CLOEXEC", 0) | getattr(os, "O_NOFOLLOW", 0)
    file_flags = os.O_RDONLY | getattr(os, "O_CLOEXEC", 0) | getattr(os, "O_NOFOLLOW", 0)
    directory_fd = os.open(root, directory_flags)
    try:
        for component in relative.parts[:-1]:
            next_fd = os.open(component, directory_flags, dir_fd=directory_fd)
            os.close(directory_fd)
            directory_fd = next_fd
        descriptor = os.open(relative.parts[-1], file_flags, dir_fd=directory_fd)
        try:
            info = os.fstat(descriptor)
            owner = getattr(os, "getuid", lambda: info.st_uid)()
            if not stat.S_ISREG(info.st_mode) or info.st_nlink != 1 or info.st_uid != owner:
                raise ValueError("must be a current-owner regular single-link file")
            if info.st_size > MAX_JSON_BYTES:
                raise ValueError(f"file exceeds {MAX_JSON_BYTES} bytes")
            blocks: list[bytes] = []
            total = 0
            while True:
                block = os.read(descriptor, min(1024 * 1024, MAX_JSON_BYTES + 1 - total))
                if not block:
                    break
                blocks.append(block)
                total += len(block)
                if total > MAX_JSON_BYTES:
                    raise ValueError(f"file exceeds {MAX_JSON_BYTES} bytes")
            return b"".join(blocks)
        finally:
            os.close(descriptor)
    except OSError as exc:
        raise ValueError(f"no-follow traversal rejected path: {exc}") from exc
    finally:
        os.close(directory_fd)


def _scrubbed_git_environment() -> dict[str, str]:
    environment = dict(os.environ)
    for name in list(environment):
        if name.startswith("GIT_"):
            environment.pop(name, None)
    environment["GIT_CONFIG_NOSYSTEM"] = "1"
    environment["GIT_CONFIG_GLOBAL"] = os.devnull
    return environment


def _verify_reviewed_commit(root: Path, commit: str) -> str | None:
    environment = _scrubbed_git_environment()
    try:
        top = subprocess.run(
            ["git", "-C", str(root), "rev-parse", "--show-toplevel"],
            check=False, capture_output=True, text=True, env=environment,
        )
        if top.returncode != 0 or Path(top.stdout.strip()).resolve() != root.resolve():
            return "repository root cannot be verified"
        exists = subprocess.run(
            ["git", "-C", str(root), "cat-file", "-e", f"{commit}^{{commit}}"],
            check=False, capture_output=True, text=True, env=environment,
        )
        if exists.returncode != 0:
            return "reviewed commit does not exist as a commit in this repository"
        reachable = subprocess.run(
            ["git", "-C", str(root), "merge-base", "--is-ancestor", commit, "HEAD"],
            check=False, capture_output=True, text=True, env=environment,
        )
        if reachable.returncode != 0:
            return "reviewed commit is not reachable as an ancestor of current HEAD"
    except OSError as exc:
        return f"cannot execute repository commit verification: {exc}"
    return None


def _call_name(node: ast.Call) -> str | None:
    target = node.func
    if isinstance(target, ast.Name):
        return target.id
    if isinstance(target, ast.Attribute):
        return target.attr
    return None


def _static_import_aliases(tree: ast.Module) -> dict[str, str]:
    aliases: dict[str, str] = {}
    for node in tree.body:
        if isinstance(node, ast.Import):
            for item in node.names:
                aliases[item.asname or item.name.split(".", 1)[0]] = item.name
        elif isinstance(node, ast.ImportFrom) and node.module:
            for item in node.names:
                if item.name != "*":
                    aliases[item.asname or item.name] = f"{node.module}.{item.name}"
    return aliases


def _qualified_call_name(node: ast.Call, aliases: Mapping[str, str]) -> str | None:
    target = node.func
    parts: list[str] = []
    while isinstance(target, ast.Attribute):
        parts.append(target.attr)
        target = target.value
    if isinstance(target, ast.Name):
        parts.append(target.id)
    else:
        return None
    name = ".".join(reversed(parts))
    first, separator, rest = name.partition(".")
    return aliases.get(first, first) + (separator + rest if separator else "")


def _proven_subprocess_pipe_writes(tree: ast.Module) -> tuple[set[int], set[int]]:
    """Return candidate and proven same-scope ``Popen.stdin`` writes."""
    aliases = _static_import_aliases(tree)
    bindings: dict[int, dict[str, set[str]]] = {}
    stdin_mutated: dict[int, set[str]] = {}
    call_scopes: dict[int, int] = {}
    scope_parents: dict[int, int | None] = {}
    scope_nodes: dict[int, ast.AST] = {}
    global_names: dict[int, set[str]] = {}
    nonlocal_names: dict[int, set[str]] = {}
    stack: list[ast.AST] = []

    def binding_scope(scope_id: int, name: str) -> int:
        if name in global_names.get(scope_id, set()):
            return id(tree)
        if name in nonlocal_names.get(scope_id, set()):
            current = scope_parents.get(scope_id)
            while current is not None:
                if isinstance(scope_nodes[current], (ast.FunctionDef, ast.AsyncFunctionDef, ast.Lambda)):
                    return current
                current = scope_parents.get(current)
        return scope_id

    def record(name: str, kind: str = "other") -> None:
        record_in(binding_scope(id(stack[-1]), name), name, kind)

    def record_in(scope_id: int, name: str, kind: str = "other") -> None:
        bindings.setdefault(scope_id, {}).setdefault(name, set()).add(kind)

    def target_names(target: ast.AST) -> list[str]:
        if isinstance(target, ast.Name):
            return [target.id]
        if isinstance(target, (ast.Tuple, ast.List)):
            return [name for item in target.elts for name in target_names(item)]
        return []

    def pattern_names(pattern: ast.pattern) -> list[str]:
        if isinstance(pattern, ast.MatchAs):
            return ([pattern.name] if pattern.name else []) + (
                pattern_names(pattern.pattern) if pattern.pattern else []
            )
        if isinstance(pattern, ast.MatchStar):
            return [pattern.name] if pattern.name else []
        if isinstance(pattern, ast.MatchMapping):
            return ([pattern.rest] if pattern.rest else []) + [
                name for child in pattern.patterns for name in pattern_names(child)
            ]
        if isinstance(pattern, ast.MatchSequence):
            return [name for child in pattern.patterns for name in pattern_names(child)]
        if isinstance(pattern, ast.MatchClass):
            return [
                name
                for child in [*pattern.patterns, *pattern.kwd_patterns]
                for name in pattern_names(child)
            ]
        if isinstance(pattern, ast.MatchOr):
            return [name for child in pattern.patterns for name in pattern_names(child)]
        return []

    module_aliases = {
        name: target for name, target in aliases.items() if target in {"os", "subprocess"}
    }
    os_alias_names = {name for name, target in module_aliases.items() if target == "os"}

    def declarations(body: Sequence[ast.AST]) -> tuple[set[str], set[str]]:
        found_global: set[str] = set()
        found_nonlocal: set[str] = set()

        class DeclarationVisitor(ast.NodeVisitor):
            def visit_Global(self, node: ast.Global) -> None:
                found_global.update(node.names)

            def visit_Nonlocal(self, node: ast.Nonlocal) -> None:
                found_nonlocal.update(node.names)

            def visit_FunctionDef(self, node: ast.FunctionDef) -> None:
                return None

            visit_AsyncFunctionDef = visit_FunctionDef
            visit_ClassDef = visit_FunctionDef
            visit_Lambda = visit_FunctionDef

        visitor = DeclarationVisitor()
        for child in body:
            visitor.visit(child)
        return found_global, found_nonlocal

    def imported_receiver(scope_id: int, name: str, module: str) -> bool:
        current: int | None = scope_id
        while current is not None:
            redirected = binding_scope(current, name)
            if redirected != current:
                current = redirected
                continue
            local = bindings.get(current, {}).get(name)
            if local is not None:
                return local == {f"module-import:{module}"}
            current = scope_parents.get(current)
            if current is not None and isinstance(scope_nodes[current], ast.ClassDef):
                current = scope_parents.get(current)
        return module_aliases.get(name) == module

    class Visitor(ast.NodeVisitor):
        def enter(self, node: ast.AST, body: Sequence[ast.AST]) -> None:
            scope_parents[id(node)] = id(stack[-1]) if stack else None
            scope_nodes[id(node)] = node
            stack.append(node)
            bindings.setdefault(id(node), {})
            stdin_mutated.setdefault(id(node), set())
            global_names[id(node)], nonlocal_names[id(node)] = declarations(body)
            if isinstance(node, (ast.FunctionDef, ast.AsyncFunctionDef, ast.Lambda)):
                args = node.args
                for item in [*args.posonlyargs, *args.args, *args.kwonlyargs]:
                    record(item.arg)
                if args.vararg:
                    record(args.vararg.arg)
                if args.kwarg:
                    record(args.kwarg.arg)
            for child in body:
                self.visit(child)
            stack.pop()

        def visit_Module(self, node: ast.Module) -> None:
            self.enter(node, node.body)

        def visit_FunctionDef(self, node: ast.FunctionDef) -> None:
            if stack:
                record(node.name)
            for expression in [*node.decorator_list, *node.args.defaults, *[item for item in node.args.kw_defaults if item], *[item.annotation for item in [*node.args.posonlyargs, *node.args.args, *node.args.kwonlyargs] if item.annotation], node.args.vararg.annotation if node.args.vararg else None, node.args.kwarg.annotation if node.args.kwarg else None, node.returns]:
                if expression is not None:
                    self.visit(expression)
            self.enter(node, node.body)

        visit_AsyncFunctionDef = visit_FunctionDef

        def visit_ClassDef(self, node: ast.ClassDef) -> None:
            if stack:
                record(node.name)
            for expression in [*node.decorator_list, *node.bases, *[item.value for item in node.keywords]]:
                self.visit(expression)
            self.enter(node, node.body)

        def visit_Lambda(self, node: ast.Lambda) -> None:
            for expression in [*node.args.defaults, *[item for item in node.args.kw_defaults if item]]:
                self.visit(expression)
            self.enter(node, [node.body])

        def visit_ListComp(self, node: ast.ListComp) -> None:
            self.visit(node.generators[0].iter)
            scope_parents[id(node)] = id(stack[-1])
            scope_nodes[id(node)] = node
            stack.append(node)
            bindings.setdefault(id(node), {})
            stdin_mutated.setdefault(id(node), set())
            global_names[id(node)] = set()
            nonlocal_names[id(node)] = set()
            for index, generator in enumerate(node.generators):
                self.assign([generator.target], None)
                if index:
                    self.visit(generator.iter)
                for condition in generator.ifs:
                    self.visit(condition)
            if isinstance(node, ast.DictComp):
                self.visit(node.key)
                self.visit(node.value)
            else:
                self.visit(node.elt)
            stack.pop()

        visit_SetComp = visit_ListComp
        visit_DictComp = visit_ListComp
        visit_GeneratorExp = visit_ListComp

        def visit_comprehension(self, node: ast.comprehension) -> None:
            self.assign([node.target], None)
            self.generic_visit(node)

        def visit_Call(self, node: ast.Call) -> None:
            scope_id = id(stack[-1])
            call_scopes[id(node)] = scope_id
            if isinstance(node.func, ast.Name) and node.func.id == "setattr" and node.args and isinstance(node.args[0], ast.Name):
                stdin_mutated[scope_id].add(node.args[0].id)
            self.generic_visit(node)

        def assign(self, targets: Sequence[ast.AST], value: ast.AST | None) -> None:
            kind = "other"
            if (
                isinstance(value, ast.Call)
                and isinstance(value.func, ast.Attribute)
                and value.func.attr == "Popen"
                and isinstance(value.func.value, ast.Name)
                and imported_receiver(
                    id(stack[-1]), value.func.value.id, "subprocess"
                )
            ):
                kind = "popen"
            for target in targets:
                for name in target_names(target):
                    record(name, kind)
                if isinstance(target, ast.Attribute) and isinstance(target.value, ast.Name):
                    stdin_mutated[id(stack[-1])].add(target.value.id)

        def visit_Assign(self, node: ast.Assign) -> None:
            self.assign(node.targets, node.value)
            self.generic_visit(node)

        def visit_AnnAssign(self, node: ast.AnnAssign) -> None:
            self.assign([node.target], node.value)
            self.generic_visit(node)

        def visit_AugAssign(self, node: ast.AugAssign) -> None:
            self.assign([node.target], None)
            self.generic_visit(node)

        def visit_Delete(self, node: ast.Delete) -> None:
            self.assign(node.targets, None)
            self.generic_visit(node)

        def visit_NamedExpr(self, node: ast.NamedExpr) -> None:
            target_scope = next(
                scope
                for scope in reversed(stack)
                if not isinstance(scope, (ast.ListComp, ast.SetComp, ast.DictComp, ast.GeneratorExp))
            )
            for name in target_names(node.target):
                record_in(binding_scope(id(target_scope), name), name)
            self.visit(node.value)

        def visit_Import(self, node: ast.Import) -> None:
            for item in node.names:
                name = item.asname or item.name.split(".", 1)[0]
                module = item.name if item.name in {"os", "subprocess"} else None
                if module == "os":
                    os_alias_names.add(name)
                record(name, f"module-import:{module}" if module else "other")

        def visit_ImportFrom(self, node: ast.ImportFrom) -> None:
            for item in node.names:
                if item.name != "*":
                    record(item.asname or item.name)

        def visit_For(self, node: ast.For) -> None:
            self.assign([node.target], None)
            self.generic_visit(node)

        visit_AsyncFor = visit_For

        def visit_With(self, node: ast.With) -> None:
            self.assign([item.optional_vars for item in node.items if item.optional_vars], None)
            self.generic_visit(node)

        visit_AsyncWith = visit_With

        def visit_ExceptHandler(self, node: ast.ExceptHandler) -> None:
            if node.name:
                record(node.name)
            self.generic_visit(node)

        def visit_match_case(self, node: ast.match_case) -> None:
            for name in pattern_names(node.pattern):
                record(name)
            if node.guard:
                self.visit(node.guard)
            for statement in node.body:
                self.visit(statement)

    Visitor().visit(tree)
    proven = {
        scope_id: {name for name, kinds in values.items() if kinds == {"popen"} and name not in stdin_mutated[scope_id]}
        for scope_id, values in bindings.items()
    }
    candidates: set[int] = set()
    result: set[int] = set()
    for node in ast.walk(tree):
        if not isinstance(node, ast.Call):
            continue
        scope_id = call_scopes.get(id(node), -1)
        if (
            isinstance(node.func, ast.Attribute)
            and node.func.attr == "write"
            and isinstance(node.func.value, ast.Name)
            and (
                node.func.value.id in os_alias_names
                or bindings[scope_id].get(node.func.value.id) == {"module-import:os"}
            )
        ):
            candidates.add(id(node))
        if _qualified_call_name(node, aliases) != "os.write" and id(node) not in candidates:
            continue
        if not node.args:
            continue
        if (
            not isinstance(node.func, ast.Attribute)
            or isinstance(scope_nodes.get(scope_id), ast.ClassDef)
            or not isinstance(node.func.value, ast.Name)
            or not imported_receiver(
                call_scopes.get(id(node), -1), node.func.value.id, "os"
            )
        ):
            continue
        fileno = node.args[0]
        if not isinstance(fileno, ast.Call) or not isinstance(fileno.func, ast.Attribute) or fileno.func.attr != "fileno":
            continue
        stdin = fileno.func.value
        if isinstance(stdin, ast.Attribute) and stdin.attr == "stdin" and isinstance(stdin.value, ast.Name):
            if stdin.value.id in proven.get(call_scopes.get(id(node), -1), set()):
                result.add(id(node))
    return candidates, result


def _parse_source(path: str, root: Path, overrides: Mapping[str, str] | None) -> ast.Module:
    candidate = root / path
    if overrides is not None and path in overrides:
        data = overrides[path].encode("utf-8")
    else:
        if candidate.is_symlink():
            raise ValueError("symlinked source is forbidden")
        resolved = candidate.resolve(strict=True)
        resolved.relative_to(root.resolve())
        data = candidate.read_bytes()
    if len(data) > MAX_SOURCE_BYTES:
        raise ValueError(f"source exceeds {MAX_SOURCE_BYTES} bytes")
    return ast.parse(data.decode("utf-8", errors="strict"), filename=path)


def discover_python_paths(root: Path, overrides: Mapping[str, str] | None = None) -> list[str]:
    paths = {path.relative_to(root).as_posix() for path in (root / "tools").glob("*.py")}
    if overrides:
        paths.update(path for path in overrides if fnmatch.fnmatchcase(path, "tools/*.py"))
    return sorted(paths)


def discover_producers(
    root: Path, *, source_overrides: Mapping[str, str] | None = None,
    python_paths: Sequence[str] | None = None,
) -> list[str]:
    result: list[str] = []
    for path in python_paths if python_paths is not None else discover_python_paths(root, source_overrides):
        if path.startswith("tools/tests/") or path in NONPRODUCER_WRITE_EXCEPTIONS:
            continue
        try:
            tree = _parse_source(path, root, source_overrides)
        except (OSError, UnicodeError, SyntaxError, ValueError) as exc:
            raise ValueError(f"cannot parse {path}: {exc}") from exc
        calls = {_call_name(node) for node in ast.walk(tree) if isinstance(node, ast.Call)}
        aliases = _static_import_aliases(tree)
        os_write_calls, pipe_writes = _proven_subprocess_pipe_writes(tree)
        unproven_os_write = any(
            isinstance(node, ast.Call)
            and (
                _qualified_call_name(node, aliases) == "os.write"
                or id(node) in os_write_calls
            )
            and id(node) not in pipe_writes
            for node in ast.walk(tree)
        )
        if calls.intersection(WRITE_CALL_NAMES) or unproven_os_write:
            result.append(path)
    return sorted(result)


def _component_matches(component: Mapping[str, Any], path: str) -> bool:
    includes = component.get("includes", [])
    excludes = component.get("excludes", [])
    return any(fnmatch.fnmatchcase(path, pattern) for pattern in includes) and not any(
        fnmatch.fnmatchcase(path, pattern) for pattern in excludes
    )


def _load_alignment(root: Path, errors: list[str]) -> tuple[dict[str, Any], dict[str, Any]]:
    try:
        python = load_json(root / "contracts/python-tooling.json")
        project = load_json(root / "contracts/project-structure.json")
        return python, project
    except (OSError, ValueError) as exc:
        _error(errors, "EP003", "alignment", f"cannot load pinned contracts: {exc}")
        return {}, {}


def validate_promotion_record(
    record: Mapping[str, Any], contract: Mapping[str, Any], *,
    current_producer_inventory_sha256: str,
    current_source_sha256: str | None = None,
    current_output_sha256: str | None = None,
    root: Path = ROOT,
) -> list[str]:
    errors: list[str] = []
    policy = contract.get("promotion_policy", {})
    if not _exact(record, PROMOTION_RECORD_FIELDS, "promotion", errors):
        return errors
    if record.get("schema") != policy.get("record_schema"):
        _error(errors, "EP020", "promotion.schema", "does not match the pinned promotion schema")
    status = record.get("status")
    if status not in policy.get("allowed_statuses", []):
        _error(errors, "EP020", "promotion.status", "is not allowed")
    for field in ("authoritative", "aggregate_authoritative", "release_authoritative", "trusted_authority_boundary"):
        if record.get(field) is not False:
            _error(errors, "EP021", f"promotion.{field}", "must remain false")
    if record.get("signature") is not None:
        _error(errors, "EP021", "promotion.signature", "must be absent/null; a digest is not a signature")
    digest = record.get("producer_inventory_sha256")
    if digest != current_producer_inventory_sha256:
        _error(errors, "EP022", "promotion.producer_inventory_sha256", "is stale")
    if current_source_sha256 is not None and record.get("source_sha256") != current_source_sha256:
        _error(errors, "EP022", "promotion.source_sha256", "does not bind current source bytes")
    if current_output_sha256 is not None and record.get("output_sha256") != current_output_sha256:
        _error(errors, "EP022", "promotion.output_sha256", "does not bind current output bytes")
    for field in ("source_sha256", "output_sha256", "producer_inventory_sha256"):
        if not isinstance(record.get(field), str) or SHA256_RE.fullmatch(record[field]) is None:
            _error(errors, "EP020", f"promotion.{field}", "must be a lowercase sha256 identity")
    source = record.get("producer_source")
    output = record.get("output_path")
    if not isinstance(source, str) or not _safe_pattern(source, "promotion.producer_source", errors):
        _error(errors, "EP020", "promotion.producer_source", "must identify a safe producer source")
    if not isinstance(output, str) or not _safe_pattern(output, "promotion.output_path", errors):
        _error(errors, "EP020", "promotion.output_path", "must identify a safe logical output")
    producers = [item for item in contract.get("producers", []) if isinstance(item, Mapping) and item.get("id") == record.get("producer_id")]
    if len(producers) != 1:
        _error(errors, "EP024", "promotion.producer_id", "does not identify exactly one inventoried producer")
    elif isinstance(source, str) and isinstance(output, str):
        producer = producers[0]
        source_rules = producer.get("sources", {})
        if not (
            any(fnmatch.fnmatchcase(source, pattern) for pattern in source_rules.get("includes", []))
            and not any(fnmatch.fnmatchcase(source, pattern) for pattern in source_rules.get("excludes", []))
        ):
            _error(errors, "EP024", "promotion.producer_source", "is outside the selected producer source scope")
        if not any(fnmatch.fnmatchcase(output, pattern) for pattern in producer.get("output_patterns", [])):
            _error(errors, "EP024", "promotion.output_path", "is outside the selected producer output scope")
    if status == "admitted":
        for field in policy.get("admitted_required_fields", []):
            if record.get(field) in (None, "", [], {}):
                _error(errors, "EP023", f"promotion.{field}", "is required for admitted status")
        commit = record.get("reviewed_commit")
        if not isinstance(commit, str) or COMMIT_RE.fullmatch(commit) is None:
            _error(errors, "EP023", "promotion.reviewed_commit", "must be a full reviewed commit identity")
        else:
            commit_error = _verify_reviewed_commit(root, commit)
            if commit_error is not None:
                _error(errors, "EP025", "promotion.reviewed_commit", commit_error)
        reviewer = record.get("reviewer")
        author = record.get("producer_author")
        if not isinstance(reviewer, str) or not reviewer:
            _error(errors, "EP023", "promotion.reviewer", "must identify the reviewer")
        if policy.get("reviewer_must_differ_from_producer_author") is True and reviewer == author:
            _error(errors, "EP023", "promotion.reviewer", "must differ from producer_author")
        checks = record.get("checks")
        if not isinstance(checks, Mapping):
            _error(errors, "EP023", "promotion.checks", "must be an object of check id to passed status")
        else:
            for check in policy.get("required_passed_checks", []):
                if checks.get(check) != "passed":
                    _error(errors, "EP023", f"promotion.checks.{check}", "must be passed")
    return errors


def validate_contract(
    contract: Mapping[str, Any], *, root: Path = ROOT,
    source_overrides: Mapping[str, str] | None = None,
    python_paths: Sequence[str] | None = None,
    promotion_records: Sequence[Mapping[str, Any]] | None = None,
) -> list[str]:
    errors: list[str] = []
    if not _exact(contract, TOP_FIELDS, "contract", errors):
        return errors
    if contract.get("schema_version") != 1 or contract.get("contract_id") != "gravity/evidence-producers-v1":
        _error(errors, "EP001", "contract", "schema_version/contract_id must be the exact v1 values")
    _strings(contract.get("normative_sources"), "normative_sources", errors)
    if _strings(contract.get("required_nonclaims"), "required_nonclaims", errors):
        if contract.get("required_nonclaims") != EXACT_REQUIRED_NONCLAIMS:
            _error(errors, "EP012", "required_nonclaims", "must equal the pinned non-authority statements")

    inventory = contract.get("inventory")
    if _exact(inventory, INVENTORY_FIELDS, "inventory", errors):
        assert isinstance(inventory, Mapping)
        if inventory.get("root_pattern") != "tools/*.py":
            _error(errors, "EP001", "inventory.root_pattern", "must remain tools/*.py")
        if _strings(inventory.get("excluded_patterns"), "inventory.excluded_patterns", errors, empty=True):
            if inventory.get("excluded_patterns") != EXPECTED_EXCLUDED_PATTERNS:
                _error(errors, "EP015", "inventory.excluded_patterns", "must equal the pinned non-producer exclusions")
        if not isinstance(inventory.get("producer_count"), int) or isinstance(inventory.get("producer_count"), bool):
            _error(errors, "EP001", "inventory.producer_count", "must be an integer")
        if not isinstance(inventory.get("producer_sha256"), str) or SHA256_RE.fullmatch(inventory["producer_sha256"]) is None:
            _error(errors, "EP001", "inventory.producer_sha256", "must be a lowercase sha256 identity")

    boundaries = contract.get("boundaries")
    if _exact(boundaries, BOUNDARY_FIELDS, "boundaries", errors):
        assert isinstance(boundaries, Mapping)
        required_true = {"filesystem_producers_only"}
        required_false = set(BOUNDARY_FIELDS) - required_true - {"compiler_artifact_graph_path", "publication_primitive_path"}
        for field in required_true:
            if boundaries.get(field) is not True:
                _error(errors, "EP004", f"boundaries.{field}", "must remain true")
        for field in required_false:
            if boundaries.get(field) is not False:
                _error(errors, "EP004", f"boundaries.{field}", "must remain false")
        if boundaries.get("compiler_artifact_graph_path") != "contracts/project-structure.json#artifacts":
            _error(errors, "EP004", "boundaries.compiler_artifact_graph_path", "must pin the semantic compiler artifact graph")
        if boundaries.get("publication_primitive_path") != "tools/output_publication.py":
            _error(errors, "EP004", "boundaries.publication_primitive_path", "must pin the publication primitive")

    python_contract, project_contract = _load_alignment(root, errors)
    python_policies = {item.get("id"): item for item in python_contract.get("policies", []) if isinstance(item, Mapping)}
    project_policies = {
        item.get("id"): item
        for item in project_contract.get("path_policy", {}).get("policies", [])
        if isinstance(item, Mapping)
    }
    components = {item.get("id"): item for item in python_contract.get("components", []) if isinstance(item, Mapping)}

    policy_values = contract.get("output_policies")
    policies: dict[str, Mapping[str, Any]] = {}
    if not isinstance(policy_values, list):
        _error(errors, "EP001", "output_policies", "must be a list")
    else:
        for index, policy in enumerate(policy_values):
            location = f"output_policies[{index}]"
            if not _exact(policy, POLICY_FIELDS, location, errors):
                continue
            assert isinstance(policy, Mapping)
            identifier = policy.get("id")
            if not isinstance(identifier, str) or ID_RE.fullmatch(identifier) is None or identifier in policies:
                _error(errors, "EP001", f"{location}.id", "must be a unique kebab-case id")
                continue
            policies[identifier] = policy
            expected_policy = EXACT_OUTPUT_POLICY_PROFILES.get(identifier)
            if expected_policy is None or dict(policy) != expected_policy:
                _error(errors, "EP005", location, "must equal the pinned output-policy profile")
            if _strings(policy.get("patterns"), f"{location}.patterns", errors):
                for pi, pattern in enumerate(policy["patterns"]):
                    _safe_pattern(pattern, f"{location}.patterns[{pi}]", errors)
            for field in ("reviewed_source", "isolated_atomic_publication_required"):
                if not isinstance(policy.get(field), bool):
                    _error(errors, "EP001", f"{location}.{field}", "must be boolean")
            py_policy = python_policies.get(policy.get("python_tooling_policy"))
            if py_policy is None:
                _error(errors, "EP005", location, "references an unknown Python tooling policy")
            else:
                for pattern in policy.get("patterns", []):
                    if not any(_pattern_within(pattern, parent) for parent in py_policy.get("patterns", [])):
                        _error(errors, "EP005", f"{location}.patterns", f"{pattern!r} escapes Python tooling policy")
            project_id = policy.get("project_structure_policy")
            if project_id is not None:
                project_policy = project_policies.get(project_id)
                if project_policy is None:
                    _error(errors, "EP005", location, "references an unknown project-structure policy")
                else:
                    for pattern in policy.get("patterns", []):
                        if not any(_pattern_within(pattern, parent) for parent in project_policy.get("patterns", [])):
                            _error(errors, "EP005", f"{location}.patterns", f"{pattern!r} escapes project-structure policy")
        if set(policies) != set(EXACT_OUTPUT_POLICY_PROFILES):
            _error(errors, "EP005", "output_policies", "ids must exactly match the pinned output-policy profiles")

    writer_values = contract.get("writers")
    writers: dict[str, Mapping[str, Any]] = {}
    if not isinstance(writer_values, list):
        _error(errors, "EP001", "writers", "must be a list")
    else:
        for index, writer in enumerate(writer_values):
            location = f"writers[{index}]"
            if not _exact(writer, WRITER_FIELDS, location, errors):
                continue
            assert isinstance(writer, Mapping)
            identifier = writer.get("id")
            if not isinstance(identifier, str) or ID_RE.fullmatch(identifier) is None or identifier in writers:
                _error(errors, "EP001", f"{location}.id", "must be a unique kebab-case id")
                continue
            writers[identifier] = writer
            expected_writer = EXACT_WRITER_PROFILES.get(identifier)
            if expected_writer is None or dict(writer) != expected_writer:
                _error(errors, "EP006", location, "must equal the pinned writer profile")
            if not isinstance(writer.get("implementation"), str) or not writer["implementation"]:
                _error(errors, "EP006", f"{location}.implementation", "must identify an implementation")
            _strings(writer.get("mechanisms"), f"{location}.mechanisms", errors)
            for field in ("regular_nosymlink_current_owner", "atomic_replace", "provenance_added_by_writer"):
                if not isinstance(writer.get(field), bool):
                    _error(errors, "EP006", f"{location}.{field}", "must be boolean")
        if set(writers) != set(EXACT_WRITER_PROFILES):
            _error(errors, "EP006", "writers", "ids must exactly match the pinned writer profiles")

    all_paths = list(python_paths) if python_paths is not None else discover_python_paths(root, source_overrides)
    try:
        observed = discover_producers(root, source_overrides=source_overrides, python_paths=all_paths)
    except ValueError as exc:
        _error(errors, "EP007", "inventory", str(exc))
        observed = []
    declared_by_path: dict[str, list[str]] = {path: [] for path in all_paths}
    producer_values = contract.get("producers")
    producer_ids: set[str] = set()
    if not isinstance(producer_values, list) or len(producer_values) > MAX_PRODUCERS:
        _error(errors, "EP001", "producers", f"must be a list of at most {MAX_PRODUCERS} entries")
        producer_values = []
    for index, producer in enumerate(producer_values):
        location = f"producers[{index}]"
        if not _exact(producer, PRODUCER_FIELDS, location, errors):
            continue
        assert isinstance(producer, Mapping)
        identifier = producer.get("id")
        if not isinstance(identifier, str) or ID_RE.fullmatch(identifier) is None or identifier in producer_ids:
            _error(errors, "EP001", f"{location}.id", "must be a unique kebab-case id")
            continue
        producer_ids.add(identifier)
        sources = producer.get("sources")
        if not _exact(sources, SOURCE_FIELDS, f"{location}.sources", errors):
            continue
        assert isinstance(sources, Mapping)
        for field in ("includes", "excludes"):
            if _strings(sources.get(field), f"{location}.sources.{field}", errors, empty=(field == "excludes")):
                for pi, pattern in enumerate(sources[field]):
                    _safe_pattern(pattern, f"{location}.sources.{field}[{pi}]", errors)
        matched = [
            path for path in all_paths
            if any(fnmatch.fnmatchcase(path, pattern) for pattern in sources.get("includes", []))
            and not any(fnmatch.fnmatchcase(path, pattern) for pattern in sources.get("excludes", []))
        ]
        if not matched:
            _error(errors, "EP007", f"{location}.sources", "matches no current source")
        for path in matched:
            declared_by_path.setdefault(path, []).append(identifier)
        for field in ("commands", "input_patterns", "output_policy_refs", "output_patterns", "output_kinds", "schemas", "provenance_required", "nonclaims"):
            _strings(producer.get(field), f"{location}.{field}", errors)
        _strings(producer.get("cli_output_options"), f"{location}.cli_output_options", errors, empty=True)
        for pattern_index, pattern in enumerate(producer.get("input_patterns", []) + producer.get("output_patterns", [])):
            if not pattern.startswith("<"):
                _safe_pattern(pattern, f"{location}.patterns[{pattern_index}]", errors)
        refs = producer.get("output_policy_refs", [])
        for reference in refs:
            if reference not in policies:
                _error(errors, "EP008", f"{location}.output_policy_refs", f"unknown policy {reference!r}")
        for pattern in producer.get("output_patterns", []):
            if not any(
                any(_pattern_within(pattern, parent) for parent in policies[ref].get("patterns", []))
                for ref in refs if ref in policies
            ):
                _error(errors, "EP008", f"{location}.output_patterns", f"{pattern!r} is outside declared output policies")
            referenced_project_ids = {
                policies[ref].get("project_structure_policy") for ref in refs if ref in policies
            }
            overlapping_generated = {
                identifier for identifier, project_policy in project_policies.items()
                if project_policy.get("kind") == "generated"
                and any(_patterns_overlap(pattern, project_pattern) for project_pattern in project_policy.get("patterns", []))
            }
            overlapping_reviewed = {
                identifier for identifier, project_policy in project_policies.items()
                if project_policy.get("kind") == "reviewed"
                and any(_patterns_overlap(pattern, project_pattern) for project_pattern in project_policy.get("patterns", []))
            }
            reviewed_output = any(ref in policies and policies[ref].get("reviewed_source") is True for ref in refs)
            if reviewed_output and overlapping_generated:
                _error(errors, "EP008", f"{location}.output_patterns", f"reviewed output {pattern!r} overlaps generated project policy {sorted(overlapping_generated)}")
            if not reviewed_output and overlapping_reviewed:
                _error(errors, "EP008", f"{location}.output_patterns", f"generated output {pattern!r} overlaps reviewed project policy {sorted(overlapping_reviewed)}")
            if "generated-coverage" in overlapping_generated and "generated-coverage" not in referenced_project_ids:
                _error(errors, "EP008", f"{location}.output_patterns", f"coverage output {pattern!r} lacks generated-coverage policy")
        writer = writers.get(producer.get("writer"))
        if writer is None:
            _error(errors, "EP009", f"{location}.writer", "references an unknown writer")
        required_calls = WRITER_CALL_REQUIREMENTS.get(producer.get("writer"), set())
        for path in matched:
            try:
                tree = _parse_source(path, root, source_overrides)
            except (OSError, UnicodeError, SyntaxError, ValueError) as exc:
                _error(errors, "EP009", f"{location}.writer", f"cannot inspect {path}: {exc}")
                continue
            calls = {_call_name(node) for node in ast.walk(tree) if isinstance(node, ast.Call)}
            if required_calls and calls.isdisjoint(required_calls):
                _error(errors, "EP009", f"{location}.writer", f"{path!r} does not call the declared writer")
            commands = producer.get("commands", [])
            if not any(path in command or "{source}" in command for command in commands):
                _error(errors, "EP016", f"{location}.commands", f"does not bind source {path!r}")
            observed_options = _cli_options(tree)
            declared_options = set(producer.get("cli_output_options", []))
            if observed_options != declared_options:
                _error(
                    errors, "EP016", f"{location}.cli_output_options",
                    f"{path!r} declares {sorted(observed_options)}, contract declares {sorted(declared_options)}",
                )
        component = components.get(producer.get("python_tooling_component"))
        if component is None:
            _error(errors, "EP005", f"{location}.python_tooling_component", "references an unknown component")
        else:
            if producer.get("authority_ceiling") != component.get("authority_ceiling"):
                _error(errors, "EP010", f"{location}.authority_ceiling", "differs from Python tooling ceiling")
            component_refs = set(component.get("output_path_policy_refs", []))
            mapped_refs = {policies[ref].get("python_tooling_policy") for ref in refs if ref in policies}
            if not mapped_refs.issubset(component_refs):
                _error(errors, "EP005", f"{location}.output_policy_refs", "not declared by the Python tooling component")
            for path in matched:
                if not _component_matches(component, path):
                    _error(errors, "EP005", f"{location}.sources", f"{path!r} is outside the declared Python tooling component")
            isolated = set(component.get("output_classes", [])).intersection(
                {"generated-evidence-isolated", "generated-coverage-isolated"}
            )
            if isolated and producer.get("writer") != "isolated-atomic-publication":
                _error(errors, "EP009", f"{location}.writer", "isolated output must use output_publication")
        required_provenance = PRODUCER_PROVENANCE_REQUIREMENTS.get(identifier)
        if required_provenance is None:
            _error(errors, "EP011", f"{location}.provenance_required", "producer has no pinned provenance profile")
        elif not required_provenance.issubset(set(producer.get("provenance_required", []))):
            missing = sorted(required_provenance.difference(producer.get("provenance_required", [])))
            _error(errors, "EP011", f"{location}.provenance_required", f"lacks producer-specific provenance: {', '.join(missing)}")
        required_schemas = PRODUCER_SCHEMA_REQUIREMENTS.get(identifier)
        if required_schemas is None or set(producer.get("schemas", [])) != required_schemas:
            _error(errors, "EP017", f"{location}.schemas", f"must equal the pinned schema ids: {sorted(required_schemas or [])}")
        if not REQUIRED_NONCLAIMS.issubset(set(producer.get("nonclaims", []))):
            _error(errors, "EP012", f"{location}.nonclaims", "must disclaim authority and release")
        review = producer.get("review")
        if _exact(review, REVIEW_FIELDS, f"{location}.review", errors):
            assert isinstance(review, Mapping)
            if not all(isinstance(review.get(field), bool) for field in REVIEW_FIELDS):
                _error(errors, "EP013", f"{location}.review", "all review fields must be boolean")
            reviewed_refs = [ref for ref in refs if ref in policies and policies[ref].get("reviewed_source") is True]
            if review.get("direct_reviewed_source") is True:
                if reviewed_refs != ["reviewed-document-source"] or producer.get("writer") != "reviewed-source-rewrite":
                    _error(errors, "EP014", location, "direct reviewed-source generation has an invalid boundary")
                if review.get("generation_review_required") is not True or review.get("admission_review_required") is not True:
                    _error(errors, "EP014", f"{location}.review", "direct reviewed-source generation must require generation and admission review")
                if producer.get("python_tooling_component") != "reviewed-source-generators":
                    _error(errors, "EP014", f"{location}.python_tooling_component", "direct reviewed-source generation is limited to reviewed-source-generators")
            elif reviewed_refs:
                _error(errors, "EP014", location, "unreviewed direct reviewed-path generation is forbidden")

    observed_set = set(observed)
    declared_set = {path for path, owners in declared_by_path.items() if owners}
    for path in sorted(observed_set):
        owners = declared_by_path.get(path, [])
        if len(owners) != 1:
            _error(errors, "EP007", f"inventory[{path}]", f"producer must be covered exactly once; matched {len(owners)}")
    for path in sorted(declared_set - observed_set):
        _error(errors, "EP007", f"inventory[{path}]", "declared producer has no statically observed filesystem writer")
    if producer_ids != set(PRODUCER_PROVENANCE_REQUIREMENTS):
        _error(errors, "EP011", "producers", "producer ids must exactly match the pinned provenance profiles")
    if producer_ids != set(PRODUCER_SCHEMA_REQUIREMENTS):
        _error(errors, "EP017", "producers", "producer ids must exactly match the pinned schema profiles")
    if isinstance(inventory, Mapping):
        if inventory.get("producer_count") != len(observed):
            _error(errors, "EP015", "inventory.producer_count", f"expected {inventory.get('producer_count')}, found {len(observed)}")
        digest = _digest_lines(observed)
        if inventory.get("producer_sha256") != digest:
            _error(errors, "EP015", "inventory.producer_sha256", f"expected {inventory.get('producer_sha256')}, found {digest}")

    promotion_policy = contract.get("promotion_policy")
    if _exact(promotion_policy, PROMOTION_POLICY_FIELDS, "promotion_policy", errors):
        assert isinstance(promotion_policy, Mapping)
        if dict(promotion_policy) != EXACT_PROMOTION_POLICY:
            _error(errors, "EP023", "promotion_policy", "must equal the pinned promotion/admission policy")
        for field in ("tracked_record_patterns", "allowed_statuses", "admitted_required_fields", "required_passed_checks"):
            _strings(promotion_policy.get(field), f"promotion_policy.{field}", errors)
        for field in ("trusted_authority_boundary_present", "authoritative", "aggregate_authoritative", "release_authoritative", "digest_is_signature"):
            if promotion_policy.get(field) is not False:
                _error(errors, "EP021", f"promotion_policy.{field}", "must remain false")
        if promotion_policy.get("reviewer_must_differ_from_producer_author") is not True:
            _error(errors, "EP023", "promotion_policy.reviewer_must_differ_from_producer_author", "must remain true")
        if promotion_policy.get("reviewed_commit_policy") != "existing-commit-reachable-ancestor-of-current-head":
            _error(errors, "EP025", "promotion_policy.reviewed_commit_policy", "must require an existing ancestor of current HEAD")
    records: list[Mapping[str, Any]] = []
    if promotion_records is not None:
        records = list(promotion_records)
    elif isinstance(promotion_policy, Mapping):
        for pattern in promotion_policy.get("tracked_record_patterns", []):
            if not isinstance(pattern, str) or not _safe_pattern(pattern, "promotion_policy.tracked_record_patterns", errors):
                continue
            for path in sorted(root.glob(pattern)):
                try:
                    records.append(load_json(path))
                except (OSError, ValueError, UnicodeError) as exc:
                    _error(errors, "EP020", f"promotion_records[{path.relative_to(root).as_posix()}]", f"cannot load current record: {exc}")
    producer_digest = _digest_lines(observed)
    for index, record in enumerate(records):
        source_digest: str | None = None
        output_digest: str | None = None
        if isinstance(record, Mapping):
            source = record.get("producer_source")
            output = record.get("output_path")
            try:
                if isinstance(source, str):
                    source_digest = _sha256_bytes(_read_current_regular(root / source, root))
                if isinstance(output, str):
                    output_digest = _sha256_bytes(_read_current_regular(root / output, root))
            except (OSError, ValueError) as exc:
                _error(errors, "EP022", f"promotion_records[{index}]", f"cannot revalidate current source/output bytes: {exc}")
        for error in validate_promotion_record(
            record, contract,
            current_producer_inventory_sha256=producer_digest,
            current_source_sha256=source_digest,
            current_output_sha256=output_digest,
            root=root,
        ):
            errors.append(error.replace("promotion", f"promotion_records[{index}]", 1))
    return errors


def build_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--contract", type=Path, default=DEFAULT_CONTRACT)
    return parser


def main(argv: Sequence[str] | None = None) -> int:
    args = build_parser().parse_args(argv)
    try:
        contract = load_json(args.contract)
        errors = validate_contract(contract, root=ROOT)
    except (OSError, ValueError, UnicodeError) as exc:
        print(f"evidence producer validation failed: {exc}", file=sys.stderr)
        return 2
    if errors:
        for error in errors:
            print(error, file=sys.stderr)
        return 1
    print(f"evidence producer validation passed: {contract['inventory']['producer_count']} non-authoritative producers")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
