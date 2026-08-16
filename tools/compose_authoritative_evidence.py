#!/usr/bin/env python3
"""Compose exact non-authoritative compiler evidence candidates.

Candidates are deterministic, isolated, and always non-authoritative.  The v1
promotion API revalidates one snapshot and records reviewed material, but it
also remains non-authoritative because this repository has no trusted admission
root.  Neither digest is a signature, and this tool never emits compiler-pass,
aggregate, release, full-self-hosting, or seed-retirement authority.
"""

from __future__ import annotations

import argparse
from dataclasses import dataclass
import hashlib
import json
import math
import os
from pathlib import Path, PurePosixPath
import platform
import re
import stat
import subprocess
import sys
from typing import Any, Iterable, Mapping, Sequence

import output_publication
import render_project_structure
import validate_project_structure
import verify_development


ROOT = Path(__file__).resolve().parents[1]
DEFAULT_CONTRACT = ROOT / "contracts" / "authoritative-evidence.json"
DEFAULT_PROJECT_STRUCTURE = ROOT / "contracts" / "project-structure.json"
DEFAULT_VERIFICATION_MANIFEST = ROOT / "tools" / "development_verification_manifest.json"
CONTRACT_RELATIVE = "contracts/authoritative-evidence.json"
PYTHON_TOOLING_RELATIVE = "contracts/python-tooling.json"
PROJECT_STRUCTURE_RELATIVE = "contracts/project-structure.json"
VERIFICATION_MANIFEST_RELATIVE = "tools/development_verification_manifest.json"
OUTPUT_PREFIX = "target/validation/"

CHILD_TOP_LEVEL_FIELDS = (
    "artifact", "elapsed-ms", "fresh-process-required?", "modules",
    "persistent-iteration-cache-used?", "proof-receipt-reuse-count",
    "proof-receipt-reuse-used?", "schema-version", "status",
)
CHILD_MODULE_FIELDS = (
    "artifact-id", "call-edge-count", "capability-proof-status", "contract-checks",
    "coverage-census", "elapsed-ms", "failed-checks", "form-count", "fragment-count",
    "function-record-count", "keyword-lookup-count", "module", "proof-transaction",
    "recursion-component-count", "resolution-count", "schema-version", "source-byte-count",
    "source-bytes-sha256", "source-path", "source-revision-id", "status",
    "verification-status",
)
COVERAGE_CENSUS_FIELDS = (
    "aggregate-authoritative?", "artifact", "authority-scope", "census-hash",
    "core-counts", "integrity", "module", "module-namespace", "request-counts",
    "request-schema-version", "schema-version", "scope", "sh06-status",
    "sh07-artifact-id", "source-binding", "source-revision-id", "task",
)
PROOF_TRANSACTION_FIELDS = (
    "artifact", "artifact-id", "check-catalog-bindings", "checked-core-revision",
    "cleanup-complete?", "construction-receipts-cleared?", "cross-epoch-reuse-count",
    "cross-epoch-reuse?", "failed-report-executions", "failed-report-reuse-count",
    "failed-report-reuse?", "final-snapshot-rechecked?", "maximum-receipts",
    "owner-thread-id", "phase-order", "phases", "retained-receipt-count",
    "schema-version", "source-snapshot", "status", "thread-confined?",
    "verification-report-id",
)
CONTRACT_CHECK_FIELDS = (
    "adapter-current?", "authoritative-coverage-census-current?",
    "capability-proof-complete?", "counts-precommitted-policy-current?",
    "coverage-census-policy-current?", "coverage-milestone-current?",
    "fresh-process-required?", "independent-count-oracle-policy-current?",
    "independent-verification-passed?", "iteration-cache-non-authoritative?",
    "proof-transaction-current?", "request-schema-current?",
    "required-core-product-counts-exact?", "required-core-products-present?",
    "required-request-products-present?", "required-verification-checks-present-and-passed?",
    "scope-current?", "source-revision-bound-to-bytes?", "source-snapshot-stable?",
    "target-source-reread-disabled?", "task-current?", "unsupported-claims-explicit?",
)
CRYPTOGRAPHIC_IDENTITY_KEYS = (
    "artifact-id", "census-hash", "function-names-hash", "function-shapes-hash",
    "functions-semantic-hash", "plan-semantic-hash", "sha256", "sh07-artifact-id",
    "source-bytes-sha256", "source-content-hash", "source-revision-id",
    "verification-report-id",
)
CRYPTOGRAPHIC_IDENTITY_MAP_KEYS = ("check-catalog-bindings", "public-function-hashes")

SCHEMA_VERSION = 1
SHA256 = re.compile(r"^sha256:[0-9a-f]{64}$")
BARE_SHA256 = re.compile(r"^[0-9a-f]{64}$")
SAFE_ID = re.compile(r"^[A-Za-z0-9][A-Za-z0-9._:/?-]*$")

DIAGNOSTICS = {
    "AE001": "strict data decoding failed",
    "AE002": "a configured bound was exceeded",
    "AE003": "a path escaped or violated its required class",
    "AE004": "a file was unsafe, missing, or changed during inspection",
    "AE005": "an evidence schema was malformed or unsupported",
    "AE006": "a content identity was malformed or did not recompute",
    "AE007": "an exact scope or dependency closure did not match",
    "AE008": "current bytes or semantics differ from the candidate",
    "AE009": "SH07 freshness, runtime, source, output, or child evidence was incomplete",
    "AE010": "a non-authoritative reference was malformed or attempted authority",
    "AE011": "reviewed promotion admission was absent or inconsistent",
    "AE012": "an output path was outside isolated validation output",
    "AE013": "the child EDN output or identity projection was malformed",
    "AE014": "the normative evidence contract was malformed or inconsistent",
}


class EvidenceError(ValueError):
    """A stable, fail-closed evidence diagnostic."""

    def __init__(self, code: str, detail: str):
        if code not in DIAGNOSTICS:
            raise ValueError(f"unknown authoritative-evidence diagnostic {code!r}")
        self.code = code
        self.detail = detail
        super().__init__(f"{code}: {DIAGNOSTICS[code]}: {detail}")


def _fail(code: str, detail: str) -> None:
    raise EvidenceError(code, detail)


def _exact(value: Any, fields: set[str], label: str, code: str = "AE005") -> Mapping[str, Any]:
    if not isinstance(value, Mapping):
        _fail(code, f"{label} must be an object")
    missing = sorted(fields.difference(value))
    unknown = sorted(set(value).difference(fields))
    if missing or unknown:
        _fail(code, f"{label} has missing={missing} unknown={unknown}")
    return value


def _canonical(value: Any) -> bytes:
    try:
        return json.dumps(
            value,
            ensure_ascii=True,
            allow_nan=False,
            sort_keys=True,
            separators=(",", ":"),
        ).encode("utf-8")
    except (TypeError, ValueError, RecursionError) as exc:
        _fail("AE001", f"canonical JSON failed: {exc}")


def _sha256_bytes(data: bytes) -> str:
    return "sha256:" + hashlib.sha256(data).hexdigest()


def _semantic_sha256(value: Any, domain: str) -> str:
    return _sha256_bytes(
        _canonical({"domain": domain, "schema_version": SCHEMA_VERSION, "value": value})
    )


def _plain_json_sha256(value: Any) -> str:
    return _sha256_bytes(_canonical(value))


def _bounded_value(value: Any, *, maximum_nodes: int, maximum_depth: int, label: str) -> Any:
    nodes = 0
    stack: list[tuple[Any, int, bool]] = [(value, 1, False)]
    active: set[int] = set()
    # JSON decoding cannot create cycles.  The active set also protects public
    # APIs when callers pass in-memory objects instead of decoded JSON.
    while stack:
        current, depth, exiting = stack.pop()
        if exiting:
            active.remove(id(current))
            continue
        nodes += 1
        if nodes > maximum_nodes:
            _fail("AE002", f"{label} exceeds {maximum_nodes} nodes")
        if depth > maximum_depth:
            _fail("AE002", f"{label} exceeds depth {maximum_depth}")
        if current is None or isinstance(current, (bool, int, str)):
            continue
        if isinstance(current, float):
            if not math.isfinite(current):
                _fail("AE001", f"{label} contains a non-finite number")
            continue
        if isinstance(current, list):
            marker = id(current)
            if marker in active:
                _fail("AE001", f"{label} contains a cycle")
            active.add(marker)
            stack.append((current, depth, True))
            stack.extend((item, depth + 1, False) for item in current)
            continue
        if isinstance(current, Mapping):
            if not all(isinstance(key, str) for key in current):
                _fail("AE001", f"{label} object keys must be strings")
            marker = id(current)
            if marker in active:
                _fail("AE001", f"{label} contains a cycle")
            active.add(marker)
            stack.append((current, depth, True))
            stack.extend((item, depth + 1, False) for item in current.values())
            continue
        _fail("AE001", f"{label} contains unsupported type {type(current).__name__}")
    return value


def _strict_object(pairs: list[tuple[str, Any]]) -> dict[str, Any]:
    result: dict[str, Any] = {}
    for key, value in pairs:
        if key in result:
            _fail("AE001", f"duplicate JSON key {key!r}")
        result[key] = value
    return result


def _reject_constant(value: str) -> Any:
    _fail("AE001", f"invalid JSON constant {value}")


def _normalised_relative(path: str | Path, *, label: str) -> str:
    raw = os.fspath(path)
    if not isinstance(raw, str) or not raw or "\x00" in raw or "\\" in raw:
        _fail("AE003", f"{label} must be a nonempty POSIX path")
    if raw.startswith("/") or raw.startswith("./") or "//" in raw or raw.endswith("/"):
        _fail("AE003", f"{label} must be canonical and repository-relative: {raw!r}")
    parsed = PurePosixPath(raw)
    if parsed.is_absolute() or any(part in {"", ".", ".."} for part in parsed.parts):
        _fail("AE003", f"{label} contains traversal: {raw!r}")
    if parsed.as_posix() != raw:
        _fail("AE003", f"{label} is not canonically spelled: {raw!r}")
    return raw


def _root_path(root: Path | str) -> Path:
    try:
        resolved = Path(root).resolve(strict=True)
        info = os.lstat(resolved)
    except OSError as exc:
        _fail("AE003", f"repository root is unavailable: {exc}")
    if not stat.S_ISDIR(info.st_mode):
        _fail("AE003", f"repository root is not a directory: {resolved}")
    return resolved


def _relative_to_root(root: Path, path: Path | str, *, label: str) -> str:
    raw = Path(path)
    if raw.is_absolute():
        try:
            raw = raw.relative_to(root)
        except ValueError:
            _fail("AE003", f"{label} is outside the repository: {path}")
    return _normalised_relative(raw.as_posix(), label=label)


def _directory_flags() -> int:
    if not getattr(os, "O_NOFOLLOW", 0) or not getattr(os, "O_DIRECTORY", 0):
        _fail("AE004", "secure no-follow directory reads are unavailable")
    return os.O_RDONLY | os.O_DIRECTORY | os.O_NOFOLLOW | getattr(os, "O_CLOEXEC", 0)


def _open_regular_relative(root: Path, relative: str, maximum_bytes: int) -> tuple[bytes, os.stat_result]:
    relative = _normalised_relative(relative, label="input path")
    owner = os.geteuid()
    parent = os.open(root, _directory_flags())
    try:
        parts = PurePosixPath(relative).parts
        for component in parts[:-1]:
            try:
                child = os.open(component, _directory_flags(), dir_fd=parent)
            except OSError as exc:
                _fail("AE004", f"cannot safely traverse {relative!r}: {exc}")
            os.close(parent)
            parent = child
        flags = os.O_RDONLY | os.O_NOFOLLOW | getattr(os, "O_CLOEXEC", 0)
        try:
            descriptor = os.open(parts[-1], flags, dir_fd=parent)
        except OSError as exc:
            _fail("AE004", f"cannot safely open {relative!r}: {exc}")
        try:
            before = os.fstat(descriptor)
            if not stat.S_ISREG(before.st_mode):
                _fail("AE004", f"input is not a regular file: {relative}")
            if before.st_nlink != 1:
                _fail("AE004", f"input is hard-linked: {relative}")
            if before.st_uid != owner:
                _fail("AE004", f"input is not owned by the current user: {relative}")
            if before.st_size > maximum_bytes:
                _fail("AE002", f"input exceeds {maximum_bytes} bytes: {relative}")
            chunks: list[bytes] = []
            total = 0
            while True:
                block = os.read(descriptor, min(1024 * 1024, maximum_bytes + 1 - total))
                if not block:
                    break
                total += len(block)
                if total > maximum_bytes:
                    _fail("AE002", f"input exceeds {maximum_bytes} bytes: {relative}")
                chunks.append(block)
            after = os.fstat(descriptor)
            coherent = (
                before.st_dev,
                before.st_ino,
                before.st_mode,
                before.st_uid,
                before.st_nlink,
                before.st_size,
                before.st_mtime_ns,
                before.st_ctime_ns,
            ) == (
                after.st_dev,
                after.st_ino,
                after.st_mode,
                after.st_uid,
                after.st_nlink,
                after.st_size,
                after.st_mtime_ns,
                after.st_ctime_ns,
            )
            if not coherent or total != before.st_size:
                _fail("AE004", f"input changed while it was read: {relative}")
            return b"".join(chunks), after
        finally:
            os.close(descriptor)
    finally:
        os.close(parent)


def _open_regular_absolute(path: str, maximum_bytes: int, *, label: str) -> tuple[bytes, os.stat_result]:
    """Read an absolute file without following any path-component symlink."""

    if not isinstance(path, str) or not path.startswith("/") or "\x00" in path:
        _fail("AE004", f"{label} path must be absolute")
    parsed = PurePosixPath(path)
    if parsed.as_posix() != path or any(part in {"", ".", ".."} for part in parsed.parts[1:]):
        _fail("AE004", f"{label} path is not canonically spelled: {path!r}")
    components = parsed.parts[1:]
    if not components:
        _fail("AE004", f"{label} cannot name the filesystem root")
    owner = os.geteuid()
    parent = os.open("/", _directory_flags())
    try:
        for component in components[:-1]:
            try:
                child = os.open(component, _directory_flags(), dir_fd=parent)
            except OSError as exc:
                _fail("AE004", f"cannot safely traverse {label} {path!r}: {exc}")
            os.close(parent)
            parent = child
        flags = os.O_RDONLY | os.O_NOFOLLOW | getattr(os, "O_CLOEXEC", 0)
        try:
            descriptor = os.open(components[-1], flags, dir_fd=parent)
        except OSError as exc:
            _fail("AE004", f"cannot safely open {label} {path!r}: {exc}")
        try:
            before = os.fstat(descriptor)
            if not stat.S_ISREG(before.st_mode):
                _fail("AE004", f"{label} is not a regular file: {path}")
            if before.st_nlink != 1:
                _fail("AE004", f"{label} is hard-linked: {path}")
            if before.st_uid != owner:
                _fail("AE004", f"{label} is not owned by the current user: {path}")
            if before.st_size > maximum_bytes:
                _fail("AE002", f"{label} exceeds {maximum_bytes} bytes: {path}")
            chunks: list[bytes] = []
            total = 0
            while True:
                block = os.read(descriptor, min(1024 * 1024, maximum_bytes + 1 - total))
                if not block:
                    break
                total += len(block)
                if total > maximum_bytes:
                    _fail("AE002", f"{label} exceeds {maximum_bytes} bytes: {path}")
                chunks.append(block)
            after = os.fstat(descriptor)
            coherent = (
                before.st_dev, before.st_ino, before.st_mode, before.st_uid,
                before.st_nlink, before.st_size, before.st_mtime_ns, before.st_ctime_ns,
            ) == (
                after.st_dev, after.st_ino, after.st_mode, after.st_uid,
                after.st_nlink, after.st_size, after.st_mtime_ns, after.st_ctime_ns,
            )
            if not coherent or total != before.st_size:
                _fail("AE004", f"{label} changed while it was read: {path}")
            return b"".join(chunks), after
        finally:
            os.close(descriptor)
    finally:
        os.close(parent)


def _file_bytes(root: Path, path: Path | str, maximum_bytes: int) -> tuple[str, bytes, os.stat_result]:
    relative = _relative_to_root(root, path, label="input path")
    data, info = _open_regular_relative(root, relative, maximum_bytes)
    return relative, data, info


def _file_binding(root: Path, path: Path | str, maximum_bytes: int) -> tuple[dict[str, Any], bytes]:
    relative, data, info = _file_bytes(root, path, maximum_bytes)
    return {
        "path": relative,
        "size": info.st_size,
        "sha256": _sha256_bytes(data),
    }, data


def _strict_json_data(
    data: bytes,
    *,
    label: str,
    maximum_nodes: int,
    maximum_depth: int,
) -> Any:
    try:
        text = data.decode("utf-8", errors="strict")
        value = json.loads(
            text,
            object_pairs_hook=_strict_object,
            parse_constant=_reject_constant,
        )
    except EvidenceError:
        raise
    except (UnicodeError, json.JSONDecodeError, RecursionError, MemoryError) as exc:
        _fail("AE001", f"cannot decode {label}: {exc}")
    return _bounded_value(
        value,
        maximum_nodes=maximum_nodes,
        maximum_depth=maximum_depth,
        label=label,
    )


def _load_json_file(root: Path, path: Path | str, contract: Mapping[str, Any], label: str) -> tuple[dict[str, Any], dict[str, Any]]:
    maximum = int(contract["bounds"]["maximum_json_bytes"])
    binding, data = _file_binding(root, path, maximum)
    value = _strict_json_data(
        data,
        label=label,
        maximum_nodes=int(contract["bounds"]["maximum_json_nodes"]),
        maximum_depth=int(contract["bounds"]["maximum_depth"]),
    )
    if not isinstance(value, dict):
        _fail("AE005", f"{label} must be a JSON object")
    return value, binding


def _validate_contract_value(value: Mapping[str, Any]) -> Mapping[str, Any]:
    required = {
        "schema_version", "contract_id", "description", "normative_sources",
        "identity", "candidate", "promotion", "sh07", "reference_schemas",
        "bounds", "file_policy", "diagnostics", "nonclaims",
    }
    _exact(value, required, "authoritative-evidence contract", "AE014")
    if value.get("schema_version") != 1 or value.get("contract_id") != "gravity-authoritative-evidence-composition-v1":
        _fail("AE014", "unsupported authoritative-evidence contract identity")
    if value.get("diagnostics") != DIAGNOSTICS:
        _fail("AE014", "contract diagnostics differ from the tool diagnostics")
    candidate = value.get("candidate")
    promotion = value.get("promotion")
    identity = value.get("identity")
    if not isinstance(candidate, Mapping) or not isinstance(promotion, Mapping) or not isinstance(identity, Mapping):
        _fail("AE014", "contract candidate, promotion, and identity sections must be objects")
    _exact(candidate, {
        "aggregate_authoritative", "authoritative", "c2_cache_witness_authority",
        "context_is_authoritative", "development_composition_authority", "kind",
        "output_prefix", "release_authoritative", "required_bindings",
        "required_subject_fields", "seed_retirement_authoritative",
        "self_hosting_authoritative", "signature",
    }, "authoritative-evidence candidate contract", "AE014")
    _exact(promotion, {
        "aggregate_authoritative", "attestation_kind", "exact_subject_only", "kind",
        "may_set_authoritative", "output_prefix", "policy_kind", "release_authoritative",
        "release_blocked_without_signature", "required_revalidation",
        "seed_retirement_authoritative", "self_hosting_authoritative", "signature",
        "unconditional_blockers",
    }, "authoritative-evidence promotion contract", "AE014")
    _exact(identity, {
        "algorithm", "candidate_domain", "canonical_json", "child_edn_projection_domain",
        "digest_is_signature", "prefix", "promotion_domain", "semantic_file_domain",
        "signature",
    }, "authoritative-evidence identity contract", "AE014")
    false_candidate = (
        "authoritative", "aggregate_authoritative", "release_authoritative",
        "self_hosting_authoritative", "seed_retirement_authoritative",
        "development_composition_authority", "c2_cache_witness_authority",
    )
    if any(candidate.get(field) is not False for field in false_candidate) or candidate.get("signature") is not None:
        _fail("AE014", "candidate claim ceiling is not fail-closed")
    if promotion.get("may_set_authoritative") is not False:
        _fail("AE014", "Python promotion must remain non-authoritative")
    for field in (
        "aggregate_authoritative", "release_authoritative",
        "self_hosting_authoritative", "seed_retirement_authoritative",
    ):
        if promotion.get(field) is not False:
            _fail("AE014", f"promotion {field} must remain false")
    if promotion.get("signature") is not None or identity.get("digest_is_signature") is not False:
        _fail("AE014", "digest/signature distinction is not preserved")
    if (
        candidate.get("output_prefix") != OUTPUT_PREFIX
        or promotion.get("output_prefix") != OUTPUT_PREFIX
        or candidate.get("required_subject_fields") != ["module_ids"]
        or candidate.get("context_is_authoritative") is not False
    ):
        _fail("AE014", "contract attempts to redirect output or widen the evidence subject")
    if (
        identity.get("algorithm") != "sha256"
        or identity.get("prefix") != "sha256:"
        or identity.get("candidate_domain") != "gravity.authoritative-evidence/candidate/v1"
        or identity.get("promotion_domain") != "gravity.authoritative-evidence/promotion/v1"
        or identity.get("semantic_file_domain")
        != "gravity.authoritative-evidence/semantic-file/v1"
        or identity.get("child_edn_projection_domain")
        != "gravity.authoritative-evidence/sh07-child-edn-projection/v1"
        or identity.get("signature") is not None
        or candidate.get("kind") != "gravity/authoritative-evidence-candidate"
        or promotion.get("kind") != "gravity/reviewed-authoritative-evidence-candidate"
        or promotion.get("exact_subject_only") is not True
        or promotion.get("unconditional_blockers") != [
            "python_authority_ceiling_non_authoritative",
            "trusted_admission_root_missing",
            "evidence_subject_mapping_missing",
        ]
    ):
        _fail("AE014", "contract identity domains or kinds differ from pinned v1")
    sh07 = value.get("sh07")
    if not isinstance(sh07, Mapping):
        _fail("AE014", "contract SH07 section must be an object")
    _exact(sh07, {
        "checkpoint_runner_path", "checkpoint_schema", "child_module_fields",
        "child_runner_path", "child_top_level_fields", "compiler_paths", "completed_state",
        "contract_check_fields", "coverage_census_fields", "cryptographic_identity_keys",
        "cryptographic_identity_map_keys", "output_contract_checked_alone_is_sufficient",
        "proof_contract_path", "proof_transaction_fields", "required_authority_scope",
        "required_child_flags",
    }, "authoritative-evidence SH07 contract", "AE014")
    expected_lists = {
        "child_top_level_fields": CHILD_TOP_LEVEL_FIELDS,
        "child_module_fields": CHILD_MODULE_FIELDS,
        "coverage_census_fields": COVERAGE_CENSUS_FIELDS,
        "proof_transaction_fields": PROOF_TRANSACTION_FIELDS,
        "contract_check_fields": CONTRACT_CHECK_FIELDS,
        "cryptographic_identity_keys": CRYPTOGRAPHIC_IDENTITY_KEYS,
        "cryptographic_identity_map_keys": CRYPTOGRAPHIC_IDENTITY_MAP_KEYS,
    }
    for field, expected in expected_lists.items():
        if tuple(sh07.get(field, ())) != expected:
            _fail("AE014", f"contract SH07 {field} differs from the pinned v1 schema")
    if sh07.get("required_child_flags") != {
        "fresh-process-required?": True,
        "persistent-iteration-cache-used?": False,
    }:
        _fail("AE014", "contract SH07 child flags differ from the pinned v1 schema")
    if (
        sh07.get("checkpoint_schema") != "gravity/sh07-authoritative-module-checkpoints-v2"
        or sh07.get("completed_state") != "completed"
        or sh07.get("required_authority_scope")
        != "individual-existing-runner-outputs-only"
        or sh07.get("proof_contract_path")
        != "bootstrap/clojure/test/gravity/self_hosting/sh07_proof_contract.edn"
        or sh07.get("child_runner_path")
        != "bootstrap/clojure/test/gravity/self_hosting/sh07_authoritative_runner.clj"
        or sh07.get("checkpoint_runner_path") != "tools/run_sh07_authoritative_modules.py"
        or sh07.get("compiler_paths") != [
            "bootstrap/gravity/p15_s23/compiler.gravity",
            "bootstrap/gravity/p15_s23/emitter.gravity",
            "bootstrap/gravity/src/gravity/macro.gravity",
            "bootstrap/gravity/src/gravity/resolution.gravity",
            "bootstrap/gravity/src/gravity/checked_core.gravity",
        ]
        or sh07.get("output_contract_checked_alone_is_sufficient") is not False
    ):
        _fail("AE014", "contract SH07 paths or authority ceiling differ from pinned v1")
    expected_bounds = {
        "maximum_json_bytes": 16777216,
        "maximum_edn_bytes": 67108864,
        "maximum_total_reference_bytes": 67108864,
        "maximum_json_nodes": 100000,
        "maximum_edn_nodes": 250000,
        "maximum_depth": 96,
        "maximum_references_per_class": 64,
        "maximum_scope_ids_per_class": 512,
        "maximum_identity_records": 32768,
    }
    if value.get("bounds") != expected_bounds:
        _fail("AE014", "contract bounds differ from pinned v1 ceilings")
    bounds = value.get("bounds")
    if not isinstance(bounds, Mapping) or any(
        isinstance(item, bool) or not isinstance(item, int) or item <= 0
        for item in bounds.values()
    ):
        _fail("AE014", "contract bounds must be positive integers")
    return value


def _load_contract(root: Path, contract_path: Path | str) -> tuple[Mapping[str, Any], dict[str, Any]]:
    # The contract's own ceiling is fixed so it can safely supply its bounds.
    relative = _relative_to_root(root, contract_path, label="authoritative-evidence contract")
    data, info = _open_regular_relative(root, relative, 2 * 1024 * 1024)
    value = _strict_json_data(
        data, label="authoritative-evidence contract", maximum_nodes=20000, maximum_depth=64
    )
    if not isinstance(value, dict):
        _fail("AE014", "authoritative-evidence contract must be an object")
    _validate_contract_value(value)
    return value, {"path": relative, "size": info.st_size, "sha256": _sha256_bytes(data)}


def _id_list(value: Iterable[str], *, label: str, maximum: int, allow_empty: bool = False) -> list[str]:
    if isinstance(value, (str, bytes)):
        _fail("AE007", f"{label} must be an iterable of ids, not a string")
    result = list(value)
    if (not allow_empty and not result) or len(result) > maximum:
        _fail("AE002" if len(result) > maximum else "AE007", f"{label} count is invalid")
    if not all(isinstance(item, str) and SAFE_ID.fullmatch(item) for item in result):
        _fail("AE007", f"{label} contains an invalid id")
    if result != sorted(set(result)):
        _fail("AE007", f"{label} must be sorted and unique")
    return result


def _dependency_closure(records: Mapping[str, Mapping[str, Any]], selected: Sequence[str], label: str) -> list[str]:
    unknown = sorted(set(selected).difference(records))
    if unknown:
        _fail("AE007", f"{label} contains unknown ids: {unknown}")
    closure = set(selected)
    pending = list(selected)
    while pending:
        current = pending.pop()
        for dependency in records[current].get("depends_on", []):
            if dependency not in records:
                _fail("AE007", f"{label} {current!r} has unknown dependency {dependency!r}")
            if dependency not in closure:
                closure.add(dependency)
                pending.append(dependency)
    return sorted(closure)


def _scope_and_closures(
    project: Mapping[str, Any],
    verification: Mapping[str, Any],
    *,
    pass_ids: Sequence[str],
    slice_ids: Sequence[str],
    artifact_ids: Sequence[str] | None,
    policy_ids: Sequence[str] | None,
    module_ids: Sequence[str],
    verification_check_ids: Sequence[str],
    maximum: int,
) -> tuple[dict[str, Any], dict[str, Any]]:
    passes = {item["id"]: item for item in project["canonical_passes"]}
    slices = {item["id"]: item for item in project["slices"]}
    artifacts = {item["id"]: item for item in project["artifacts"]}
    checks = verify_development.checks_by_id(verification)

    exact_passes = _id_list(pass_ids, label="pass_ids", maximum=maximum)
    exact_slices = _id_list(slice_ids, label="slice_ids", maximum=maximum)
    exact_modules = _id_list(module_ids, label="module_ids", maximum=maximum)
    exact_checks = _id_list(
        verification_check_ids, label="verification_check_ids", maximum=maximum
    )
    pass_closure = _dependency_closure(passes, exact_passes, "pass scope")
    slice_closure = _dependency_closure(slices, exact_slices, "slice scope")
    check_closure = _dependency_closure(checks, exact_checks, "verification scope")
    if exact_passes != pass_closure:
        _fail("AE007", f"pass scope is missing dependencies: {sorted(set(pass_closure) - set(exact_passes))}")
    if exact_slices != slice_closure:
        _fail("AE007", f"slice scope is missing dependencies: {sorted(set(slice_closure) - set(exact_slices))}")
    if exact_checks != check_closure:
        _fail("AE007", f"verification scope is missing dependencies: {sorted(set(check_closure) - set(exact_checks))}")

    derived_artifacts = sorted(
        {
            artifact
            for item_id in exact_passes
            for artifact in (
                list(passes[item_id].get("input_artifacts", []))
                + list(passes[item_id].get("output_artifacts", []))
            )
        }
        | {
            artifact
            for item_id in exact_slices
            for artifact in (
                list(slices[item_id].get("artifact_inputs", []))
                + list(slices[item_id].get("artifact_outputs", []))
            )
        }
    )
    if unknown_artifacts := sorted(set(derived_artifacts).difference(artifacts)):
        _fail("AE007", f"scope derives unknown artifacts: {unknown_artifacts}")
    exact_artifacts = (
        derived_artifacts
        if artifact_ids is None
        else _id_list(artifact_ids, label="artifact_ids", maximum=maximum)
    )
    if exact_artifacts != derived_artifacts:
        _fail("AE007", "artifact_ids do not exactly match pass/slice artifacts")

    derived_policies = sorted(
        {
            policy
            for item_id in exact_slices
            for policy in slices[item_id].get("path_policy_ids", [])
        }
    )
    exact_policies = (
        derived_policies
        if policy_ids is None
        else _id_list(policy_ids, label="policy_ids", maximum=maximum, allow_empty=True)
    )
    known_policies = {item["id"] for item in project["path_policy"]["policies"]}
    if unknown_policies := sorted(set(exact_policies).difference(known_policies)):
        _fail("AE007", f"policy_ids contain unknown policies: {unknown_policies}")
    if missing_policies := sorted(set(derived_policies).difference(exact_policies)):
        _fail("AE007", f"policy_ids omit selected slice policies: {missing_policies}")

    verification_order = verify_development.topological_order(verification, exact_checks)
    scope = {
        "policy_ids": exact_policies,
        "pass_ids": exact_passes,
        "slice_ids": exact_slices,
        "artifact_ids": exact_artifacts,
        "module_ids": exact_modules,
        "verification_check_ids": exact_checks,
    }
    closures = {
        "pass_dependency_closure": pass_closure,
        "slice_dependency_closure": slice_closure,
        "verification_dependency_closure": check_closure,
        "verification_topological_order": verification_order,
    }
    return scope, closures


def _claims() -> dict[str, Any]:
    return {
        "authoritative": False,
        "authority_scope": "none",
        "aggregate_authoritative": False,
        "release_authoritative": False,
        "self_hosting_authoritative": False,
        "seed_retirement_authoritative": False,
        "release_blocked": True,
        "signature": None,
        "digest_is_signature": False,
    }


def _repository_context(root: Path) -> dict[str, Any]:
    git_environment = {
        key: value for key, value in os.environ.items() if not key.startswith("GIT_")
    }
    try:
        result = subprocess.run(
            ["git", "-C", str(root), "rev-parse", "--show-toplevel", "HEAD"],
            capture_output=True,
            text=True,
            timeout=5,
            check=False,
            env=git_environment,
        )
    except (OSError, subprocess.SubprocessError):
        result = None
    lines = result.stdout.splitlines() if result is not None and result.returncode == 0 else []
    commit = lines[1].strip() if len(lines) == 2 else None
    try:
        top_level_matches = len(lines) == 2 and Path(lines[0]).resolve(strict=True) == root
    except OSError:
        top_level_matches = False
    if not top_level_matches:
        commit = None
    if not isinstance(commit, str) or re.fullmatch(r"[0-9a-f]{40,64}", commit) is None:
        commit = None
    return {
        "paths_are_project_relative": True,
        "head_commit": commit,
        "commit_bound": commit is not None,
        "canonical_top_level_validated": commit is not None,
        "physical_root_in_identity": False,
    }


def _manifest_bindings(
    root: Path,
    contract: Mapping[str, Any],
    project_path: Path | str,
    verification_path: Path | str,
) -> tuple[dict[str, Any], dict[str, Any], dict[str, Any], dict[str, Any]]:
    project, project_file = _load_json_file(root, project_path, contract, "project structure")
    project_errors = validate_project_structure.validate_manifest(project)
    if project_errors:
        _fail("AE005", "project structure validation failed: " + "; ".join(project_errors))
    try:
        verification, verification_file = _load_json_file(
            root, verification_path, contract, "verification manifest"
        )
        verify_development.validate_manifest(verification)
    except (verify_development.ManifestError, TypeError, ValueError) as exc:
        _fail("AE005", f"verification manifest validation failed: {exc}")
    project_binding = {
        "file": project_file,
        "manifest_id": project["manifest_id"],
        "schema_version": project["schema_version"],
        "semantic_sha256": _semantic_sha256(
            project, "gravity.authoritative-evidence/project-structure/v1"
        ),
        "static_contract_identity": render_project_structure.contract_identity_view(project),
    }
    verification_binding = {
        "file": verification_file,
        "name": verification.get("name"),
        "schema_version": verification["schema_version"],
        "semantic_sha256": _semantic_sha256(
            verification, "gravity.authoritative-evidence/verification-manifest/v1"
        ),
    }
    return project, project_binding, verification, verification_binding


def _python_tooling_ceiling(
    root: Path, contract: Mapping[str, Any]
) -> dict[str, Any]:
    tooling, binding = _load_json_file(
        root, PYTHON_TOOLING_RELATIVE, contract, "Python tooling contract"
    )
    if tooling.get("schema_version") != 1 or tooling.get("contract_id") != "gravity-python-tooling-v1":
        _fail("AE014", "Python tooling contract identity is unsupported")
    components = tooling.get("components")
    if not isinstance(components, list):
        _fail("AE014", "Python tooling components are missing")
    matches = [
        item for item in components
        if isinstance(item, Mapping) and item.get("id") == "authoritative-evidence-composition"
    ]
    if len(matches) != 1:
        _fail("AE014", "Python tooling evidence component is absent or duplicated")
    component = matches[0]
    expected = {
        "id": "authoritative-evidence-composition",
        "includes": ["tools/compose_authoritative_evidence.py"],
        "excludes": [],
        "category": "orchestration",
        "role": "nonauthoritative-evidence-composer",
        "allowed_dependency_categories": ["tooling-support", "orchestration", "validator"],
        "effects": ["filesystem-read", "filesystem-write", "process", "environment", "stdout"],
        "output_classes": ["stdout-diagnostic", "development-state-isolated"],
        "authority_ceiling": "non-authoritative-observation",
        "source_path_policy_refs": ["reviewed-central-routing"],
        "output_path_policy_refs": ["generated-evidence"],
        "import_safety": "guarded-cli",
        "execution_mode": "coordinator-reviewed-serial",
        "test_surfaces": ["import-smoke", "unit-test", "coordinator-review"],
    }
    if dict(component) != expected:
        _fail("AE014", "Python tooling evidence component differs from its exact non-authority ceiling")
    return {
        "file": binding,
        "contract_id": tooling["contract_id"],
        "semantic_sha256": _semantic_sha256(
            tooling, "gravity.authoritative-evidence/python-tooling-contract/v1"
        ),
        "component": expected,
        "authority_contribution": False,
    }


@dataclass(frozen=True)
class _EdnAtom:
    kind: str
    value: str


@dataclass(frozen=True)
class _EdnSequence:
    kind: str
    items: tuple[Any, ...]


class _EdnReader:
    def __init__(self, data: bytes, *, maximum_nodes: int, maximum_depth: int):
        try:
            self.text = data.decode("utf-8", errors="strict")
        except UnicodeError as exc:
            _fail("AE013", f"child EDN is not UTF-8: {exc}")
        self.index = 0
        self.nodes = 0
        self.maximum_nodes = maximum_nodes
        self.maximum_depth = maximum_depth

    def _node(self, depth: int) -> None:
        self.nodes += 1
        if self.nodes > self.maximum_nodes:
            _fail("AE002", f"child EDN exceeds {self.maximum_nodes} nodes")
        if depth > self.maximum_depth:
            _fail("AE002", f"child EDN exceeds depth {self.maximum_depth}")

    def _skip(self) -> None:
        while self.index < len(self.text):
            character = self.text[self.index]
            if character.isspace() or character == ",":
                self.index += 1
            elif character == ";":
                newline = self.text.find("\n", self.index)
                self.index = len(self.text) if newline < 0 else newline + 1
            else:
                return

    def _string(self) -> str:
        start = self.index
        self.index += 1
        escaped = False
        while self.index < len(self.text):
            character = self.text[self.index]
            self.index += 1
            if escaped:
                escaped = False
            elif character == "\\":
                escaped = True
            elif character == '"':
                raw = self.text[start:self.index]
                try:
                    return json.loads(raw)
                except json.JSONDecodeError as exc:
                    _fail("AE013", f"invalid child EDN string: {exc}")
        _fail("AE013", "unterminated child EDN string")

    def _token(self) -> str:
        start = self.index
        delimiters = set("[]{}()\";,\t\r\n ")
        while self.index < len(self.text) and self.text[self.index] not in delimiters:
            self.index += 1
        token = self.text[start:self.index]
        if not token:
            _fail("AE013", f"unexpected child EDN token at offset {self.index}")
        return token

    def _collection(self, opener: str, closer: str, kind: str, depth: int) -> Any:
        assert self.text[self.index] == opener
        self.index += 1
        items: list[Any] = []
        while True:
            self._skip()
            if self.index >= len(self.text):
                _fail("AE013", f"unterminated child EDN {kind}")
            if self.text[self.index] == closer:
                self.index += 1
                break
            items.append(self.read(depth + 1))
        if kind == "map":
            if len(items) % 2:
                _fail("AE013", "child EDN map has an odd form count")
            result: dict[Any, Any] = {}
            for offset in range(0, len(items), 2):
                key = items[offset]
                try:
                    duplicated = key in result
                except TypeError:
                    _fail("AE013", "child EDN map contains an unhashable key")
                if duplicated:
                    _fail("AE013", f"child EDN map contains duplicate key {key!r}")
                result[key] = items[offset + 1]
            return result
        return _EdnSequence(kind, tuple(items))

    def read(self, depth: int = 1) -> Any:
        self._skip()
        self._node(depth)
        if self.index >= len(self.text):
            _fail("AE013", "unexpected end of child EDN")
        character = self.text[self.index]
        if character == "{":
            return self._collection("{", "}", "map", depth)
        if character == "[":
            return self._collection("[", "]", "vector", depth)
        if character == "(":
            return self._collection("(", ")", "list", depth)
        if character == '"':
            return self._string()
        if character == "#":
            if self.text.startswith("#{", self.index):
                self.index += 1
                return self._collection("{", "}", "set", depth)
            _fail("AE013", "tagged, discarded, regex, or namespaced child EDN is forbidden")
        if character in "'`~^@":
            _fail("AE013", f"reader macro {character!r} is forbidden in child EDN")
        token = self._token()
        if token == "nil":
            return None
        if token == "true":
            return True
        if token == "false":
            return False
        if token.startswith(":"):
            return _EdnAtom("keyword", token[1:])
        if token.startswith("\\"):
            return _EdnAtom("character", token[1:])
        if re.fullmatch(r"[-+]?\d+", token):
            try:
                return int(token)
            except ValueError:
                _fail("AE013", f"invalid child EDN integer {token!r}")
        if re.fullmatch(r"[-+]?(?:\d+\.\d*|\d*\.\d+)(?:[eE][-+]?\d+)?", token):
            try:
                number = float(token)
            except ValueError:
                _fail("AE013", f"invalid child EDN number {token!r}")
            if not math.isfinite(number):
                _fail("AE013", "child EDN contains a non-finite number")
            return number
        return _EdnAtom("symbol", token)

    def one(self) -> Any:
        value = self.read()
        self._skip()
        if self.index != len(self.text):
            _fail("AE013", f"child EDN has trailing forms at offset {self.index}")
        return value


def _edn_key(value: Any) -> str | None:
    return value.value if isinstance(value, _EdnAtom) and value.kind == "keyword" else None


def _edn_get(value: Any, key: str, *, label: str, required: bool = True) -> Any:
    if not isinstance(value, Mapping):
        _fail("AE013", f"{label} must be an EDN map")
    wanted = _EdnAtom("keyword", key)
    if wanted not in value:
        if required:
            _fail("AE013", f"{label} lacks :{key}")
        return None
    return value[wanted]


def _edn_keyword(value: Any) -> str | None:
    return value.value if isinstance(value, _EdnAtom) and value.kind == "keyword" else None


def _edn_path_token(value: Any) -> str:
    if isinstance(value, _EdnAtom):
        prefix = ":" if value.kind == "keyword" else value.kind + ":"
        return prefix + value.value
    if isinstance(value, str):
        return json.dumps(value, ensure_ascii=True)
    if isinstance(value, (bool, int)) or value is None:
        return json.dumps(value, ensure_ascii=True)
    if isinstance(value, _EdnSequence):
        body = ",".join(_edn_path_token(item) for item in value.items)
        return f"{value.kind}[{body}]"
    _fail("AE013", "child EDN catalog binding key cannot be projected")


def _edn_sequence(value: Any, kind: str | None = None) -> tuple[Any, ...]:
    if not isinstance(value, _EdnSequence) or (kind is not None and value.kind != kind):
        _fail("AE013", f"expected child EDN {kind or 'sequence'}")
    return value.items


def _identity_records(
    value: Any,
    identity_keys: Sequence[str],
    identity_map_keys: Sequence[str],
    maximum: int,
) -> tuple[list[dict[str, str]], list[str]]:
    records: list[dict[str, str]] = []
    deficiencies: list[str] = []
    scalar_keys = set(identity_keys)
    map_keys = set(identity_map_keys)

    def visit(current: Any, path: str, forced: bool = False) -> None:
        if isinstance(current, Mapping):
            for key, item in current.items():
                rendered = _edn_key(key)
                key_token = f":{rendered}" if rendered is not None else _edn_path_token(key)
                child_path = f"{path}/{key_token}"
                local = rendered.rsplit("/", 1)[-1] if rendered is not None else ""
                is_identity = local in scalar_keys or forced
                if is_identity:
                    if not isinstance(item, str) or SHA256.fullmatch(item) is None:
                        deficiencies.append(f"weak_identity:{child_path}")
                    else:
                        records.append({"path": child_path, "key": rendered or key_token, "value": item})
                        if len(records) > maximum:
                            _fail("AE002", f"child identity count exceeds {maximum}")
                if local in map_keys:
                    if not isinstance(item, Mapping):
                        deficiencies.append(f"malformed_identity_map:{child_path}")
                        continue
                    for binding_key, binding in item.items():
                        binding_name = _edn_path_token(binding_key)
                        if not isinstance(binding, str) or SHA256.fullmatch(binding) is None:
                            deficiencies.append(f"weak_identity:{child_path}/{binding_name}")
                            continue
                        binding_path = f"{child_path}/{binding_name}"
                        records.append({
                            "path": binding_path,
                            "key": {
                                "check-catalog-bindings": "check-catalog-binding",
                                "public-function-hashes": "public-function-hash",
                            }.get(local, local),
                            "value": binding,
                        })
                        if len(records) > maximum:
                            _fail("AE002", f"child identity count exceeds {maximum}")
                elif not is_identity:
                    visit(item, child_path)
        elif isinstance(current, _EdnSequence):
            for index, item in enumerate(current.items):
                visit(item, f"{path}/{current.kind}[{index}]")

    visit(value, "$")
    records.sort(key=lambda item: (item["path"], item["key"], item["value"]))
    if len({item["path"] for item in records}) != len(records):
        _fail("AE013", "child identity projection contains duplicate paths")
    return records, sorted(set(deficiencies))


def _exact_edn_keys(value: Any, expected: Sequence[str], label: str) -> None:
    if not isinstance(value, Mapping):
        _fail("AE013", f"{label} must be an EDN map")
    actual = {_edn_key(key) for key in value}
    if None in actual or actual != set(expected):
        missing = sorted(set(expected).difference(actual))
        unknown = sorted(str(item) for item in actual.difference(expected))
        _fail("AE013", f"{label} fields differ missing={missing} unknown={unknown}")


def _validate_child_edn(
    data: bytes,
    *,
    module: str,
    source_path: str,
    source_size: int,
    source_sha256: str,
    contract: Mapping[str, Any],
) -> dict[str, Any]:
    if len(data) > int(contract["bounds"]["maximum_edn_bytes"]):
        _fail("AE002", "child EDN output exceeds its byte bound")
    child = _EdnReader(
        data,
        maximum_nodes=int(contract["bounds"]["maximum_edn_nodes"]),
        maximum_depth=int(contract["bounds"]["maximum_depth"]),
    ).one()
    _exact_edn_keys(child, contract["sh07"]["child_top_level_fields"], "child proof run")
    if _edn_keyword(_edn_get(child, "artifact", label="child proof run")) != "gravity/sh07-authoritative-proof-run":
        _fail("AE013", "child proof run artifact kind is invalid")
    if _edn_get(child, "schema-version", label="child proof run") != 2:
        _fail("AE013", "child proof run schema version is invalid")
    if _edn_keyword(_edn_get(child, "status", label="child proof run")) != "passed":
        _fail("AE009", "child proof run did not pass")
    required_flags = contract["sh07"]["required_child_flags"]
    deficiencies: list[str] = [
        "outer_authority_scope_absent_v2",
        "module_census_scope_is_nonclaim",
    ]
    for key, expected in required_flags.items():
        if _edn_get(child, key, label="child proof run") != expected:
            _fail("AE009", f"child proof run {key} differs from {expected!r}")
    if _edn_get(child, "proof-receipt-reuse-used?", label="child proof run") is not False:
        deficiencies.append("proof_receipt_cache_used")
    if _edn_get(child, "proof-receipt-reuse-count", label="child proof run") != 0:
        deficiencies.append("proof_receipt_cache_count_nonzero")
    modules = _edn_sequence(_edn_get(child, "modules", label="child proof run"), "vector")
    if len(modules) != 1:
        _fail("AE009", "each checkpoint stdout must contain exactly one child module")
    result = modules[0]
    _exact_edn_keys(result, contract["sh07"]["child_module_fields"], f"child module {module}")
    expected_scalars = {
        "module": module,
        "source-path": source_path,
        "source-byte-count": source_size,
        "source-bytes-sha256": source_sha256,
        "source-revision-id": source_sha256,
    }
    for key, expected in expected_scalars.items():
        if _edn_get(result, key, label=f"child module {module}") != expected:
            _fail("AE009", f"child module {module} has stale or invalid {key}")
    if _edn_keyword(_edn_get(result, "status", label=f"child module {module}")) != "accepted":
        _fail("AE009", f"child module {module} was not accepted")
    if _edn_keyword(_edn_get(result, "verification-status", label=f"child module {module}")) != "passed":
        _fail("AE009", f"child module {module} verification did not pass")
    if _edn_keyword(_edn_get(result, "capability-proof-status", label=f"child module {module}")) != "complete":
        _fail("AE009", f"child module {module} capability proof is incomplete")
    if _edn_sequence(_edn_get(result, "failed-checks", label=f"child module {module}"), "vector"):
        _fail("AE009", f"child module {module} has failed checks")
    checks = _edn_get(result, "contract-checks", label=f"child module {module}")
    _exact_edn_keys(checks, contract["sh07"]["contract_check_fields"], "child contract checks")
    if not isinstance(checks, Mapping) or not checks or any(item is not True for item in checks.values()):
        _fail("AE009", f"child module {module} contract checks are incomplete")
    census = _edn_get(result, "coverage-census", label=f"child module {module}")
    _exact_edn_keys(census, contract["sh07"]["coverage_census_fields"], "coverage census")
    if (
        _edn_keyword(_edn_get(census, "artifact", label="coverage census"))
        != "gravity/sh07-authoritative-coverage-census"
        or _edn_get(census, "schema-version", label="coverage census") != 1
        or _edn_keyword(_edn_get(census, "authority-scope", label="coverage census"))
        != "individual-existing-runner-output-only"
        or _edn_get(census, "aggregate-authoritative?", label="coverage census") is not False
        or _edn_get(census, "module", label="coverage census") != module
    ):
        _fail("AE009", "coverage census identity or non-aggregate scope is invalid")
    source_binding = _edn_get(census, "source-binding", label="coverage census")
    _exact_edn_keys(
        source_binding, ("source-byte-count", "source-bytes-sha256"), "coverage source binding"
    )
    if (
        _edn_get(source_binding, "source-byte-count", label="coverage source binding")
        != source_size
        or _edn_get(source_binding, "source-bytes-sha256", label="coverage source binding")
        != source_sha256
    ):
        _fail("AE009", "coverage census source binding is stale")
    _exact_edn_keys(
        _edn_get(census, "request-counts", label="coverage census"),
        ("binding-count", "form-count", "fragment-count", "local-binding-count",
         "resolution-count", "root-form-count"),
        "coverage request counts",
    )
    _exact_edn_keys(
        _edn_get(census, "core-counts", label="coverage census"),
        ("call-count", "core-form-frequencies", "core-node-count", "definition-count",
         "keyword-lookup-count", "reference-count"),
        "coverage core counts",
    )
    integrity = _edn_get(census, "integrity", label="coverage census")
    _exact_edn_keys(
        integrity,
        ("form-id-order-exact?", "root-form-id-order-exact?", "source-revision-bound-to-bytes?",
         "source-snapshot-stable?", "target-source-reread-disabled?"),
        "coverage integrity",
    )
    if any(item is not True for item in integrity.values()):
        _fail("AE009", "coverage census integrity is incomplete")
    transaction = _edn_get(result, "proof-transaction", label=f"child module {module}")
    _exact_edn_keys(
        transaction, contract["sh07"]["proof_transaction_fields"], "child proof transaction"
    )
    transaction_invariants = {
        "cleanup-complete?": True,
        "construction-receipts-cleared?": True,
        "cross-epoch-reuse-count": 0,
        "cross-epoch-reuse?": False,
        "failed-report-executions": 0,
        "failed-report-reuse-count": 0,
        "failed-report-reuse?": False,
        "final-snapshot-rechecked?": True,
        "retained-receipt-count": 0,
        "schema-version": 1,
        "thread-confined?": True,
    }
    for key, expected in transaction_invariants.items():
        if _edn_get(transaction, key, label="child proof transaction") != expected:
            _fail("AE009", f"child proof transaction has invalid {key}")
    if (
        _edn_keyword(_edn_get(transaction, "artifact", label="child proof transaction"))
        != "gravity/sh07-proof-transaction-receipt"
        or _edn_keyword(_edn_get(transaction, "status", label="child proof transaction"))
        != "passed"
    ):
        _fail("AE009", "child proof transaction did not pass")
    catalog_bindings = _edn_get(transaction, "check-catalog-bindings", label="child proof transaction")
    expected_catalog = {
        _EdnSequence("vector", (_EdnAtom("keyword", "sh05"), _EdnAtom("keyword", "construction"))),
        _EdnSequence("vector", (_EdnAtom("keyword", "sh05"), _EdnAtom("keyword", "final"))),
        _EdnSequence("vector", (_EdnAtom("keyword", "sh06"), _EdnAtom("keyword", "final"))),
        _EdnSequence("vector", (_EdnAtom("keyword", "sh07"), _EdnAtom("keyword", "final"))),
    }
    if not isinstance(catalog_bindings, Mapping) or set(catalog_bindings) != expected_catalog:
        _fail("AE013", "child proof transaction check catalog is not exact")
    identities, identity_deficiencies = _identity_records(
        child,
        contract["sh07"]["cryptographic_identity_keys"],
        contract["sh07"]["cryptographic_identity_map_keys"],
        int(contract["bounds"]["maximum_identity_records"]),
    )
    deficiencies.extend(identity_deficiencies)
    if not identities:
        _fail("AE013", "child proof run has no cryptographic identity records")
    artifact_id = _edn_get(result, "artifact-id", label=f"child module {module}")
    if not isinstance(artifact_id, str) or SHA256.fullmatch(artifact_id) is None:
        deficiencies.append("artifact_id_not_cryptographic")
    linked = [
        _edn_get(census, "source-revision-id", label="coverage census"),
        _edn_get(census, "sh07-artifact-id", label="coverage census"),
        _edn_get(transaction, "artifact-id", label="proof transaction"),
    ]
    if linked != [source_sha256, artifact_id, artifact_id]:
        _fail("AE013", f"child module {module} source/artifact identity links disagree")
    module_prefix = "$/:modules/vector[0]"
    recomputable_paths = {
        f"{module_prefix}/:source-bytes-sha256",
        f"{module_prefix}/:source-revision-id",
        f"{module_prefix}/:coverage-census/:source-revision-id",
        f"{module_prefix}/:coverage-census/:source-binding/:source-bytes-sha256",
        f"{module_prefix}/:proof-transaction/:source-snapshot/:source-content-hash",
    }
    recomputed_paths: list[str] = []
    unrecomputed: list[dict[str, str]] = []
    for record in identities:
        if record["path"] in recomputable_paths and record["value"] == source_sha256:
            recomputed_paths.append(record["path"])
        else:
            unrecomputed.append(record)
    if unrecomputed:
        deficiencies.append("cryptographic_child_identity_incomplete")
    projection = {
        "module": module,
        "source_path": source_path,
        "source_size": source_size,
        "source_sha256": source_sha256,
        "artifact_id": artifact_id,
        "identity_records": identities,
        "recomputed_identity_paths": sorted(recomputed_paths),
        "unrecomputed_identity_records": unrecomputed,
        "cryptographic_identity_complete": not unrecomputed and not identity_deficiencies,
        "deficiencies": sorted(set(deficiencies)),
        "fresh_process": True,
        "persistent_iteration_cache_used": False,
        "proof_receipt_reuse_used": _edn_get(
            child, "proof-receipt-reuse-used?", label="child proof run"
        ),
        "proof_receipt_reuse_count": _edn_get(
            child, "proof-receipt-reuse-count", label="child proof run"
        ),
    }
    projection["projection_sha256"] = _semantic_sha256(
        projection, contract["identity"]["child_edn_projection_domain"]
    )
    return projection


def _entry_by_path(entries: Any, path: str, *, label: str) -> Mapping[str, Any]:
    if not isinstance(entries, list):
        _fail("AE009", f"{label} must be a list")
    matches = [item for item in entries if isinstance(item, Mapping) and item.get("path") == path]
    if len(matches) != 1:
        _fail("AE009", f"{label} must contain exactly one {path!r}")
    return matches[0]


def _match_file_entry(
    root: Path,
    entry: Mapping[str, Any],
    contract: Mapping[str, Any],
    label: str,
    *,
    allow_external: bool = False,
) -> dict[str, Any]:
    _exact(entry, {"path", "size", "sha256"}, label, "AE009")
    path = entry.get("path")
    if not isinstance(path, str):
        _fail("AE009", f"{label} path is not a repository file")
    maximum = int(contract["bounds"]["maximum_total_reference_bytes"])
    if path.startswith("external:"):
        if not allow_external:
            _fail("AE009", f"{label} path is not a repository file")
        absolute = path.removeprefix("external:")
        data, info = _open_regular_absolute(absolute, maximum, label=label)
        binding = {"path": path, "size": info.st_size, "sha256": _sha256_bytes(data)}
    else:
        binding, _data = _file_binding(root, path, maximum)
    if binding != dict(entry):
        _fail("AE008", f"current file differs from {label}: {path}")
    return binding


def _absolute_file_binding(path: str, contract: Mapping[str, Any], label: str) -> dict[str, Any]:
    data, info = _open_regular_absolute(
        path, int(contract["bounds"]["maximum_total_reference_bytes"]), label=label
    )
    return {"path": path, "size": info.st_size, "sha256": _sha256_bytes(data)}


def _project_absolute_binding(root: Path, binding: Mapping[str, Any]) -> dict[str, Any]:
    projected = dict(binding)
    try:
        projected["path"] = Path(str(binding["path"])).relative_to(root).as_posix()
        projected["path_class"] = "project-relative"
    except ValueError:
        projected["path_class"] = "external-absolute"
    return projected


def _launcher_binding(
    root: Path, path: str, recorded_sha256: str, contract: Mapping[str, Any]
) -> tuple[dict[str, Any], list[str]]:
    """Revalidate direct launchers; record legacy symlink identity as deficient."""

    try:
        metadata = os.lstat(path)
    except OSError as exc:
        _fail("AE004", f"SH07 resolved launcher is unavailable: {exc}")
    if stat.S_ISLNK(metadata.st_mode):
        try:
            link_target = os.readlink(path)
        except OSError as exc:
            _fail("AE004", f"SH07 launcher symlink cannot be inspected: {exc}")
        binding = {
            "path": path,
            "recorded_sha256": recorded_sha256,
            "symlink_target_text": link_target,
            "current_bytes_revalidated": False,
        }
        return (
            _project_absolute_binding(root, binding),
            ["runtime_launcher_canonical_target_unbound"],
        )
    binding = _absolute_file_binding(path, contract, "SH07 resolved launcher")
    if binding["sha256"] != recorded_sha256:
        _fail("AE008", "SH07 launcher bytes differ from the checkpoint")
    binding["current_bytes_revalidated"] = True
    return _project_absolute_binding(root, binding), []


def _current_classpath_directory(
    root: Path, directory: str, contract: Mapping[str, Any]
) -> list[dict[str, Any]]:
    path = Path(directory)
    if not path.is_absolute():
        _fail("AE009", f"classpath directory is not absolute: {directory!r}")
    try:
        relative_directory = path.relative_to(root)
    except ValueError:
        _fail("AE009", f"classpath directory is outside the repository: {directory}")
    directory_path = root / relative_directory
    try:
        metadata = os.lstat(directory_path)
    except OSError as exc:
        _fail("AE004", f"classpath directory is unavailable: {exc}")
    if stat.S_ISLNK(metadata.st_mode) or not stat.S_ISDIR(metadata.st_mode):
        _fail("AE004", f"classpath directory is unsafe: {directory}")
    entries: list[dict[str, Any]] = []
    for current, directory_names, file_names in os.walk(
        directory_path, topdown=True, followlinks=False
    ):
        current_path = Path(current)
        try:
            current_info = os.lstat(current_path)
            current_relative = current_path.relative_to(directory_path)
        except (OSError, ValueError) as exc:
            _fail("AE004", f"classpath directory traversal failed: {exc}")
        if stat.S_ISLNK(current_info.st_mode) or not stat.S_ISDIR(current_info.st_mode):
            _fail("AE004", f"classpath directory traversal is unsafe: {current_path}")
        for name in sorted(directory_names):
            child = current_path / name
            try:
                child_info = os.lstat(child)
            except OSError as exc:
                _fail("AE004", f"classpath directory entry is unavailable: {exc}")
            if stat.S_ISLNK(child_info.st_mode) or not stat.S_ISDIR(child_info.st_mode):
                _fail("AE004", f"classpath directory entry is unsafe: {child}")
        for name in sorted(file_names):
            child = current_path / name
            try:
                child_info = os.lstat(child)
            except OSError as exc:
                _fail("AE004", f"classpath file is unavailable: {exc}")
            if stat.S_ISLNK(child_info.st_mode) or not stat.S_ISREG(child_info.st_mode):
                _fail("AE004", f"classpath file is unsafe: {child}")
            if child.suffix == ".class":
                _fail("AE009", f"classpath contains an AOT shadow: {child}")
            if current_relative == Path(".") and name in {"data_readers.clj", "data_readers.cljc"}:
                binding, _data = _file_binding(
                    root, child, int(contract["bounds"]["maximum_total_reference_bytes"])
                )
                entries.append({
                    "path": child.relative_to(directory_path).as_posix(),
                    "size": binding["size"],
                    "sha256": binding["sha256"],
                })
    return sorted(entries, key=lambda item: item["path"])


def _runtime_projection(root: Path, runtime: Any, contract: Mapping[str, Any]) -> dict[str, Any]:
    runtime_fields = {
        "required", "operating_system", "operating_system_release", "architecture",
        "java_path", "java_sha256", "java_version", "clojure_sdescribe",
        "clojure_classpath", "clojure_classpath_entries", "clojure_classpath_errors",
        "clojure_config_files", "complete",
    }
    if not isinstance(runtime, Mapping):
        _fail("AE009", "SH07 runtime identity is malformed")
    _exact(runtime, runtime_fields, "SH07 runtime identity", "AE009")
    if runtime.get("required") is not True or runtime.get("complete") is not True:
        _fail("AE009", "SH07 runtime identity is incomplete")
    current_platform = (platform.system(), platform.release(), platform.machine())
    recorded_platform = (
        runtime.get("operating_system"), runtime.get("operating_system_release"),
        runtime.get("architecture"),
    )
    if recorded_platform != current_platform:
        _fail("AE008", "SH07 runtime platform differs from the current platform")
    java_hash = runtime.get("java_sha256")
    java_path = runtime.get("java_path")
    if (
        not isinstance(java_path, str)
        or not isinstance(java_hash, str)
        or SHA256.fullmatch(java_hash) is None
    ):
        _fail("AE009", "SH07 Java identity is not cryptographic")
    java_binding = _absolute_file_binding(java_path, contract, "SH07 Java runtime")
    if java_binding["sha256"] != java_hash:
        _fail("AE008", "SH07 Java runtime bytes differ from the checkpoint")
    for capture in ("java_version", "clojure_sdescribe", "clojure_classpath"):
        value = runtime.get(capture)
        if not isinstance(value, Mapping) or value.get("complete") is not True:
            _fail("AE009", f"SH07 runtime capture {capture} is incomplete")
    if runtime.get("clojure_classpath_errors") != []:
        _fail("AE009", "SH07 classpath identity contains errors")
    configs = runtime.get("clojure_config_files")
    if not isinstance(configs, list):
        _fail("AE009", "SH07 Clojure config identities are incomplete")
    current_configs: list[dict[str, Any]] = []
    for item in configs:
        if not isinstance(item, Mapping):
            _fail("AE009", "SH07 Clojure config identity is malformed")
        _exact(item, {"path", "sha256"}, "SH07 Clojure config identity", "AE009")
        if (
            not isinstance(item.get("path"), str)
            or not isinstance(item.get("sha256"), str)
            or SHA256.fullmatch(item["sha256"]) is None
        ):
            _fail("AE009", "SH07 Clojure config identity is weak")
        current = _absolute_file_binding(item["path"], contract, "SH07 Clojure config")
        if current["sha256"] != item["sha256"]:
            _fail("AE008", f"SH07 Clojure config bytes differ: {item['path']}")
        current_configs.append(_project_absolute_binding(root, current))
    classpath = runtime.get("clojure_classpath_entries")
    if not isinstance(classpath, list):
        _fail("AE009", "SH07 classpath projection is malformed")
    current_classpath: list[dict[str, Any]] = []
    for entry in classpath:
        if not isinstance(entry, Mapping) or entry.get("kind") not in {"file", "root-contained-directory"}:
            _fail("AE009", "SH07 classpath projection contains an unsupported entry")
        if entry["kind"] == "file":
            _exact(entry, {"path", "kind", "size", "sha256"}, "SH07 classpath file", "AE009")
            if (
                not isinstance(entry.get("path"), str)
                or not isinstance(entry.get("sha256"), str)
                or SHA256.fullmatch(entry["sha256"]) is None
            ):
                _fail("AE009", "SH07 classpath file identity is weak")
            current = _absolute_file_binding(entry["path"], contract, "SH07 classpath file")
            if current["size"] != entry.get("size") or current["sha256"] != entry["sha256"]:
                _fail("AE008", f"SH07 classpath bytes differ: {entry['path']}")
            current_classpath.append({
                **_project_absolute_binding(root, current), "kind": "file"
            })
        if entry["kind"] == "root-contained-directory":
            _exact(entry, {"path", "kind", "files"}, "SH07 classpath directory", "AE009")
            files = entry.get("files")
            if not isinstance(files, list) or any(
                not isinstance(item, Mapping)
                or not isinstance(item.get("sha256"), str)
                or SHA256.fullmatch(item["sha256"]) is None
                for item in files
            ):
                _fail("AE009", "SH07 classpath directory projection is incomplete")
            path = entry.get("path")
            if not isinstance(path, str):
                _fail("AE009", "SH07 classpath directory path is malformed")
            current_files = _current_classpath_directory(root, path, contract)
            if current_files != files:
                _fail("AE008", f"SH07 classpath directory bytes differ: {path}")
            projected_directory = _project_absolute_binding(root, {"path": path})
            current_classpath.append({
                **projected_directory, "kind": entry["kind"], "files": current_files
            })
    projection = dict(runtime)
    return {
        "complete": True,
        "semantic_sha256": _semantic_sha256(
            projection, "gravity.authoritative-evidence/sh07-runtime/v1"
        ),
        "java": _project_absolute_binding(root, java_binding),
        "classpath": current_classpath,
        "config_files": current_configs,
        "classpath_entry_count": len(classpath),
        "config_file_count": len(configs),
    }


def _checkpoint_projection(
    root: Path,
    checkpoint_path: Path | str,
    module_ids: Sequence[str],
    contract: Mapping[str, Any],
) -> dict[str, Any]:
    checkpoint, checkpoint_file = _load_json_file(root, checkpoint_path, contract, "SH07 checkpoint")
    if checkpoint.get("schema") != contract["sh07"]["checkpoint_schema"]:
        _fail("AE009", "SH07 checkpoint schema is unsupported")
    if checkpoint.get("state") != contract["sh07"]["completed_state"]:
        _fail("AE009", "SH07 checkpoint is not completed")
    if checkpoint.get("aggregate_authoritative") is not False:
        _fail("AE009", "SH07 checkpoint attempts aggregate authority")
    if checkpoint.get("authority_scope") != contract["sh07"]["required_authority_scope"]:
        _fail("AE009", "SH07 checkpoint authority scope differs")
    selected = checkpoint.get("selected_modules")
    if not isinstance(selected, list) or len(selected) != len(set(selected)) or sorted(selected) != list(module_ids):
        _fail("AE007", "SH07 selected modules differ from the exact module scope")
    if checkpoint.get("resumed_modules") != []:
        _fail("AE009", "SH07 checkpoint reused resumed module output")
    shared = checkpoint.get("shared_context")
    if not isinstance(shared, Mapping):
        _fail("AE009", "SH07 shared context is missing")
    shared_without_hash = {key: value for key, value in shared.items() if key != "sha256"}
    expected_shared = _plain_json_sha256(shared_without_hash)
    if shared.get("sha256") != expected_shared or checkpoint.get("shared_context_fingerprint") != expected_shared:
        _fail("AE006", "SH07 shared-context fingerprint does not recompute")
    base_command = shared.get("command")
    if not isinstance(base_command, list) or not base_command or not all(isinstance(item, str) for item in base_command):
        _fail("AE009", "SH07 base command is malformed")
    if shared.get("authoritative_module_catalog") is None:
        _fail("AE009", "SH07 module catalog binding is absent")
    environment_names = [
        "JAVA_HOME", "JAVA_OPTS", "JAVA_TOOL_OPTIONS", "_JAVA_OPTIONS",
        "JDK_JAVA_OPTIONS", "CLJ_JVM_OPTS", "CLJ_CONFIG",
    ]
    current_environment = {name: os.environ.get(name) for name in environment_names}
    if shared.get("environment") != current_environment:
        _fail("AE008", "SH07 runtime environment differs from the checkpoint")
    resolved_executable = shared.get("resolved_executable")
    resolved_executable_sha = shared.get("resolved_executable_sha256")
    if (
        not isinstance(resolved_executable, str)
        or not isinstance(resolved_executable_sha, str)
        or SHA256.fullmatch(resolved_executable_sha) is None
    ):
        _fail("AE009", "SH07 launcher identity is incomplete")
    executable_binding, launcher_deficiencies = _launcher_binding(
        root, resolved_executable, resolved_executable_sha, contract
    )
    runtime = _runtime_projection(root, shared.get("runtime"), contract)
    shared_files = shared.get("files")
    if not isinstance(shared_files, list) or not all(
        isinstance(entry, Mapping) for entry in shared_files
    ):
        _fail("AE009", "SH07 shared file projection is missing")
    current_shared_files = [
        _match_file_entry(root, entry, contract, "SH07 shared file", allow_external=True)
        for entry in shared_files
    ]
    if len({entry["path"] for entry in current_shared_files}) != len(current_shared_files):
        _fail("AE009", "SH07 shared file projection contains duplicate paths")
    proof_entry = _entry_by_path(
        shared_files, contract["sh07"]["proof_contract_path"], label="SH07 shared files"
    )
    child_runner_entry = _entry_by_path(
        shared_files, contract["sh07"]["child_runner_path"], label="SH07 shared files"
    )
    checkpoint_runner_entry = _entry_by_path(
        shared_files, contract["sh07"]["checkpoint_runner_path"], label="SH07 shared files"
    )
    compiler_entries = [
        _entry_by_path(shared_files, path, label="SH07 shared files")
        for path in contract["sh07"]["compiler_paths"]
    ]
    source_contracts = checkpoint.get("source_contracts")
    module_contexts = checkpoint.get("module_contexts")
    records = checkpoint.get("modules")
    catalog = shared.get("authoritative_module_catalog")
    if not all(isinstance(value, Mapping) for value in (source_contracts, module_contexts, records, catalog)):
        _fail("AE009", "SH07 module/source maps are incomplete")
    state_directory = PurePosixPath(checkpoint_file["path"]).parent.as_posix()
    modules: list[dict[str, Any]] = []
    deficiencies = list(launcher_deficiencies)
    for module in selected:
        source_path = catalog.get(module)
        source_contract = source_contracts.get(module)
        context = module_contexts.get(module)
        record = records.get(module)
        if not isinstance(source_path, str) or not isinstance(source_contract, Mapping):
            _fail("AE009", f"SH07 module {module!r} lacks a source contract")
        if not isinstance(context, Mapping) or not isinstance(record, Mapping):
            _fail("AE009", f"SH07 module {module!r} lacks context/result evidence")
        if source_contract.get("source_path") != source_path:
            _fail("AE009", f"SH07 module {module!r} source contract path differs")
        source_entry = _entry_by_path(context.get("files"), source_path, label=f"SH07 {module} context")
        current_source = _match_file_entry(root, source_entry, contract, f"SH07 module {module} source")
        if (
            source_contract.get("source_byte_count") != current_source["size"]
            or source_contract.get("source_bytes_sha256") != current_source["sha256"]
        ):
            _fail("AE008", f"SH07 module {module!r} source contract is stale")
        context_without_hash = {key: value for key, value in context.items() if key != "sha256"}
        context_hash = _plain_json_sha256(context_without_hash)
        if context.get("sha256") != context_hash or record.get("module_context_fingerprint") != context_hash:
            _fail("AE006", f"SH07 module {module!r} context fingerprint does not recompute")
        if record.get("module_context") != context:
            _fail("AE009", f"SH07 module {module!r} record/context differ")
        expected_command = [*base_command, "--fresh", module]
        if record.get("command") != expected_command:
            _fail("AE009", f"SH07 module {module!r} was not executed through --fresh")
        required_result = {
            "state": "passed", "exit_code": 0, "raw_child_exit_code": 0,
            "timed_out": False, "context_stable": True,
            "output_contract_checked": True,
            "shared_context_fingerprint_after": expected_shared,
        }
        for field, expected in required_result.items():
            if record.get(field) != expected:
                _fail("AE009", f"SH07 module {module!r} has invalid {field}")
        if record.get("stale_modules") != []:
            _fail("AE009", f"SH07 module {module!r} reports stale modules")
        stdout_relative = record.get("stdout_path")
        stderr_relative = record.get("stderr_path")
        expected_stdout = f"modules/{module}.stdout.log"
        expected_stderr = f"modules/{module}.stderr.log"
        if stdout_relative != expected_stdout or stderr_relative != expected_stderr:
            _fail("AE009", f"SH07 module {module!r} output paths are noncanonical")
        stdout_path = f"{state_directory}/{stdout_relative}"
        stderr_path = f"{state_directory}/{stderr_relative}"
        stdout_binding, stdout_data = _file_binding(
            root, stdout_path, int(contract["bounds"]["maximum_edn_bytes"])
        )
        stderr_binding, _stderr_data = _file_binding(
            root, stderr_path, int(contract["bounds"]["maximum_edn_bytes"])
        )
        if stdout_binding["sha256"] != record.get("stdout_sha256"):
            _fail("AE008", f"SH07 module {module!r} stdout changed")
        if stderr_binding["sha256"] != record.get("stderr_sha256"):
            _fail("AE008", f"SH07 module {module!r} stderr changed")
        child = _validate_child_edn(
            stdout_data,
            module=module,
            source_path=source_path,
            source_size=current_source["size"],
            source_sha256=current_source["sha256"],
            contract=contract,
        )
        deficiencies.extend(child["deficiencies"])
        modules.append({
            "module": module,
            "source": current_source,
            "source_contract": dict(source_contract),
            "module_context_sha256": context_hash,
            "command": expected_command,
            "fresh": True,
            "resumed": False,
            "iteration_cache_used": False,
            "proof_receipt_cache_used": child["proof_receipt_reuse_used"],
            "output_contract_checked": True,
            "stdout": stdout_binding,
            "stderr": stderr_binding,
            "child_edn": child,
        })
    child_projection_sha = _semantic_sha256(
        [{"module": item["module"], "projection_sha256": item["child_edn"]["projection_sha256"]} for item in modules],
        "gravity.authoritative-evidence/sh07-child-projection-set/v1",
    )
    return {
        "checkpoint": checkpoint_file,
        "schema": checkpoint["schema"],
        "state": "completed",
        "fresh": True,
        "resumed_modules": [],
        "aggregate_authoritative": False,
        "authority_scope": checkpoint["authority_scope"],
        "shared_context_sha256": expected_shared,
        "environment": current_environment,
        "resolved_executable": executable_binding,
        "runtime": runtime,
        "current_shared_files": current_shared_files,
        "proof_contract": _match_file_entry(root, proof_entry, contract, "SH07 proof contract"),
        "child_runner": _match_file_entry(root, child_runner_entry, contract, "SH07 child runner"),
        "checkpoint_runner": _match_file_entry(
            root, checkpoint_runner_entry, contract, "SH07 checkpoint runner"
        ),
        "compiler_sources": [
            _match_file_entry(root, item, contract, "SH07 compiler source")
            for item in compiler_entries
        ],
        "modules": modules,
        "child_edn_projection_sha256": child_projection_sha,
        "child_edn_projection_trusted_for_promotion": False,
        "deficiencies": sorted(set(deficiencies)),
    }


def _development_reference(value: Mapping[str, Any], binding: Mapping[str, Any]) -> dict[str, Any]:
    if (
        value.get("schema_version") != 1
        or value.get("kind") != "development-verification-composition"
        or value.get("authoritative") is not False
        or value.get("status") != "complete"
    ):
        _fail("AE010", f"development composition is incomplete or authoritative: {binding['path']}")
    digest = value.get("composition_sha256")
    if not isinstance(digest, str) or BARE_SHA256.fullmatch(digest) is None:
        _fail("AE010", f"development composition digest is malformed: {binding['path']}")
    payload = {key: item for key, item in value.items() if key != "composition_sha256"}
    if _plain_json_sha256(payload) != "sha256:" + digest:
        _fail("AE010", f"development composition digest does not recompute: {binding['path']}")
    return {
        "file": dict(binding),
        "composition_sha256": "sha256:" + digest,
        "authority_contribution": False,
    }


def _c2_reference(value: Mapping[str, Any], binding: Mapping[str, Any]) -> dict[str, Any]:
    required = {
        "schema_version", "kind", "authoritative", "aggregate_authoritative",
        "release_authority", "proof_authority", "semantic_key_id",
        "storage_key_id", "artifact_id", "boundary_projection_id",
        "observations", "witness_sha256",
    }
    _exact(value, required, f"C2 witness {binding['path']}", "AE010")
    if (
        value.get("schema_version") != 1
        or value.get("kind") != "gravity/c2-cache-witness"
        or any(value.get(field) is not False for field in (
            "authoritative", "aggregate_authoritative", "release_authority", "proof_authority"
        ))
    ):
        _fail("AE010", f"C2 cache witness violates its authority ceiling: {binding['path']}")
    for field in ("semantic_key_id", "storage_key_id", "artifact_id", "boundary_projection_id"):
        if not isinstance(value.get(field), str) or SHA256.fullmatch(value[field]) is None:
            _fail("AE010", f"C2 cache witness has weak {field}: {binding['path']}")
    observations = value.get("observations")
    if not isinstance(observations, list) or len(observations) != 2:
        _fail("AE010", f"C2 cache witness needs stored and hit observations: {binding['path']}")
    stored, hit = observations
    if not isinstance(stored, Mapping) or not isinstance(hit, Mapping):
        _fail("AE010", f"C2 cache observations must be objects: {binding['path']}")
    if stored.get("status") != "stored" or stored.get("reader_executed") is not True or stored.get("artifact_reused") is not False:
        _fail("AE010", f"C2 stored observation is invalid: {binding['path']}")
    if hit.get("status") != "hit" or hit.get("reader_executed") is not False or hit.get("artifact_reused") is not True:
        _fail("AE010", f"C2 hit observation is invalid: {binding['path']}")
    digest = value.get("witness_sha256")
    if not isinstance(digest, str) or SHA256.fullmatch(digest) is None:
        _fail("AE010", f"C2 witness digest is malformed: {binding['path']}")
    payload = {key: item for key, item in value.items() if key != "witness_sha256"}
    expected = _semantic_sha256(payload, "gravity.authoritative-evidence/c2-cache-witness/v1")
    if digest != expected:
        _fail("AE010", f"C2 witness digest does not recompute: {binding['path']}")
    return {"file": dict(binding), "witness_sha256": digest, "authority_contribution": False}


def _references(
    root: Path,
    contract: Mapping[str, Any],
    development_paths: Sequence[Path | str],
    c2_paths: Sequence[Path | str],
) -> dict[str, Any]:
    maximum_count = int(contract["bounds"]["maximum_references_per_class"])
    if len(development_paths) > maximum_count or len(c2_paths) > maximum_count:
        _fail("AE002", f"reference count exceeds {maximum_count}")
    total = 0
    development: list[dict[str, Any]] = []
    c2: list[dict[str, Any]] = []
    for path in development_paths:
        value, binding = _load_json_file(root, path, contract, "development composition")
        total += binding["size"]
        development.append(_development_reference(value, binding))
    for path in c2_paths:
        value, binding = _load_json_file(root, path, contract, "C2 cache witness")
        total += binding["size"]
        c2.append(_c2_reference(value, binding))
    if total > int(contract["bounds"]["maximum_total_reference_bytes"]):
        _fail("AE002", "reference bytes exceed their aggregate bound")
    development.sort(key=lambda item: item["file"]["path"])
    c2.sort(key=lambda item: item["file"]["path"])
    if len({item["file"]["path"] for item in development}) != len(development):
        _fail("AE010", "development composition references are duplicated")
    if len({item["file"]["path"] for item in c2}) != len(c2):
        _fail("AE010", "C2 cache witness references are duplicated")
    return {
        "development_compositions": development,
        "c2_cache_witnesses": c2,
        "authority_contribution": False,
    }


def _impact_relation(impact: Mapping[str, Any], scope: Mapping[str, Any]) -> dict[str, Any]:
    pairs = {
        "policy_ids": "impacted_policy_ids",
        "pass_ids": "impacted_passes",
        "slice_ids": "impacted_slices",
        "artifact_ids": "impacted_artifacts",
    }
    in_scope: dict[str, list[str]] = {}
    outside_scope: dict[str, list[str]] = {}
    for scope_key, impact_key in pairs.items():
        impacted = set(impact.get(impact_key, []))
        selected = set(scope[scope_key])
        in_scope[scope_key] = sorted(impacted.intersection(selected))
        outside_scope[scope_key] = sorted(impacted.difference(selected))
    return {
        "impact_complete": impact.get("impact_complete") is True,
        "in_scope": in_scope,
        "outside_exact_scope": outside_scope,
        "outside_scope_contributes_authority": False,
    }


def _build_candidate(
    *,
    root: Path,
    contract_path: Path | str,
    project_structure_path: Path | str,
    verification_manifest_path: Path | str,
    checkpoint_path: Path | str,
    changed_paths: Sequence[str],
    pass_ids: Sequence[str],
    slice_ids: Sequence[str],
    artifact_ids: Sequence[str] | None,
    policy_ids: Sequence[str] | None,
    module_ids: Sequence[str],
    verification_check_ids: Sequence[str],
    development_composition_paths: Sequence[Path | str],
    c2_cache_witness_paths: Sequence[Path | str],
) -> dict[str, Any]:
    if _relative_to_root(root, contract_path, label="authoritative-evidence contract") != CONTRACT_RELATIVE:
        _fail("AE014", f"authoritative-evidence contract is pinned to {CONTRACT_RELATIVE}")
    contract, contract_file = _load_contract(root, CONTRACT_RELATIVE)
    python_tooling = _python_tooling_ceiling(root, contract)
    if (
        _relative_to_root(root, project_structure_path, label="project structure")
        != PROJECT_STRUCTURE_RELATIVE
        or _relative_to_root(root, verification_manifest_path, label="verification manifest")
        != VERIFICATION_MANIFEST_RELATIVE
    ):
        _fail("AE014", "project structure and verification manifest are pinned to canonical paths")
    project, project_binding, verification, verification_binding = _manifest_bindings(
        root, contract, project_structure_path, verification_manifest_path
    )
    maximum_scope = int(contract["bounds"]["maximum_scope_ids_per_class"])
    scope, closures = _scope_and_closures(
        project,
        verification,
        pass_ids=pass_ids,
        slice_ids=slice_ids,
        artifact_ids=artifact_ids,
        policy_ids=policy_ids,
        module_ids=module_ids,
        verification_check_ids=verification_check_ids,
        maximum=maximum_scope,
    )
    normalised_changed = sorted(
        {_normalised_relative(path, label="changed path") for path in changed_paths}
    )
    if not normalised_changed:
        _fail("AE007", "at least one changed path is required")
    try:
        impact = render_project_structure.changed_path_impact_closure(project, normalised_changed)
    except (render_project_structure.RenderError, TypeError, ValueError) as exc:
        _fail("AE007", f"changed-path impact cannot be computed: {exc}")
    if impact.get("impact_complete") is not True:
        _fail("AE007", "changed-path impact is incomplete")
    impact_relation = _impact_relation(impact, scope)
    if any(impact_relation["outside_exact_scope"].values()):
        _fail("AE007", "changed-path impact escapes the exact dependency context")
    checkpoint = _checkpoint_projection(root, checkpoint_path, scope["module_ids"], contract)
    module_paths = {
        item["source"]["path"] for item in checkpoint["modules"]
    }
    if missing_module_sources := sorted(module_paths.difference(normalised_changed)):
        _fail(
            "AE007",
            f"every evidence-subject module source must be a changed path: {missing_module_sources}",
        )
    slices = {item["id"]: item for item in project["slices"]}
    selected_owners = {slices[item_id]["owner"] for item_id in scope["slice_ids"]}
    module_owners = project.get("ownership", {}).get("module_paths", {})
    for source_path in module_paths:
        owner = module_owners.get(source_path)
        if owner is None or owner not in selected_owners:
            _fail("AE007", f"module source {source_path!r} is not owned by the selected slice scope")
    references = _references(
        root, contract, development_composition_paths, c2_cache_witness_paths
    )
    slices_by_id = {item["id"]: item for item in project["slices"]}
    authority_ceilings = [{
        "slice_id": slice_id,
        "required_level": slices_by_id[slice_id]["authority"]["required_level"],
        "maximum_claim": slices_by_id[slice_id]["authority"]["maximum_claim"],
        "authority_contribution": False,
    } for slice_id in scope["slice_ids"]]
    evidence_subject = {"module_ids": scope["module_ids"]}
    context_nonclaims = {
        "policy_ids": scope["policy_ids"],
        "pass_ids": scope["pass_ids"],
        "slice_ids": scope["slice_ids"],
        "artifact_ids": scope["artifact_ids"],
        "verification_check_ids": scope["verification_check_ids"],
        "authority_ceilings": authority_ceilings,
        "authority_contribution": False,
    }
    payload: dict[str, Any] = {
        "schema_version": SCHEMA_VERSION,
        "kind": contract["candidate"]["kind"],
        "repository": _repository_context(root),
        "contract": {
            "file": contract_file,
            "contract_id": contract["contract_id"],
            "semantic_sha256": _semantic_sha256(
                contract, contract["identity"]["semantic_file_domain"]
            ),
        },
        "python_tooling": python_tooling,
        "claims": _claims(),
        "evidence_subject": evidence_subject,
        "context_nonclaims": context_nonclaims,
        "project_structure": project_binding,
        "verification_manifest": verification_binding,
        "closures": closures,
        "changed_path_impact": impact,
        "impact_scope_relation": impact_relation,
        "references": references,
        "sh07": checkpoint,
    }
    payload["candidate_sha256"] = _semantic_sha256(
        payload, contract["identity"]["candidate_domain"]
    )
    return payload


def _output_relative(root: Path, output_path: Path | str, contract: Mapping[str, Any]) -> str:
    relative = _relative_to_root(root, output_path, label="evidence output")
    if not relative.startswith(OUTPUT_PREFIX) or not relative.endswith(".json"):
        _fail("AE012", f"evidence output must be JSON below {OUTPUT_PREFIX}: {relative}")
    return relative


def _write_output(root: Path, output_path: Path | str, value: Mapping[str, Any], contract: Mapping[str, Any]) -> Path:
    relative = _output_relative(root, output_path, contract)
    if os.environ.get("GRAVITY_OUTPUT_ROOT"):
        _fail(
            "AE012",
            "GRAVITY_OUTPUT_ROOT is unsupported because evidence reads and writes must share one repository root",
        )
    try:
        return output_publication.atomic_write_json(relative, dict(value), repository_root=root)
    except (OSError, ValueError) as exc:
        _fail("AE012", f"isolated evidence publication failed: {exc}")


def compose_candidate(
    *,
    checkpoint_path: Path | str,
    changed_paths: Sequence[str],
    pass_ids: Sequence[str],
    slice_ids: Sequence[str],
    module_ids: Sequence[str],
    verification_check_ids: Sequence[str],
    output_path: Path | str,
    artifact_ids: Sequence[str] | None = None,
    policy_ids: Sequence[str] | None = None,
    development_composition_paths: Sequence[Path | str] = (),
    c2_cache_witness_paths: Sequence[Path | str] = (),
    root: Path | str = ROOT,
) -> dict[str, Any]:
    """Compose and atomically publish one non-authoritative exact candidate."""

    root_path = _root_path(root)
    candidate = _build_candidate(
        root=root_path,
        contract_path=CONTRACT_RELATIVE,
        project_structure_path=PROJECT_STRUCTURE_RELATIVE,
        verification_manifest_path=VERIFICATION_MANIFEST_RELATIVE,
        checkpoint_path=checkpoint_path,
        changed_paths=changed_paths,
        pass_ids=pass_ids,
        slice_ids=slice_ids,
        artifact_ids=artifact_ids,
        policy_ids=policy_ids,
        module_ids=module_ids,
        verification_check_ids=verification_check_ids,
        development_composition_paths=development_composition_paths,
        c2_cache_witness_paths=c2_cache_witness_paths,
    )
    contract, _binding = _load_contract(root_path, CONTRACT_RELATIVE)
    _write_output(root_path, output_path, candidate, contract)
    return candidate


def _candidate_inputs(candidate: Mapping[str, Any]) -> dict[str, Any]:
    _exact(candidate, {
        "schema_version", "kind", "repository", "contract", "python_tooling", "claims", "evidence_subject",
        "context_nonclaims",
        "project_structure", "verification_manifest", "closures", "changed_path_impact",
        "impact_scope_relation", "references", "sh07", "candidate_sha256",
    }, "candidate")
    subject = _exact(candidate["evidence_subject"], {"module_ids"}, "candidate evidence subject")
    context = _exact(candidate["context_nonclaims"], {
        "policy_ids", "pass_ids", "slice_ids", "artifact_ids", "verification_check_ids",
        "authority_ceilings", "authority_contribution",
    }, "candidate nonclaim context")
    references = _exact(candidate["references"], {
        "development_compositions", "c2_cache_witnesses", "authority_contribution",
    }, "candidate references")
    sh07 = candidate.get("sh07")
    if not isinstance(sh07, Mapping) or not isinstance(sh07.get("checkpoint"), Mapping):
        _fail("AE005", "candidate SH07 checkpoint binding is missing")
    changed = candidate.get("changed_path_impact")
    if not isinstance(changed, Mapping) or not isinstance(changed.get("changed_paths"), list):
        _fail("AE005", "candidate changed-path impact is missing")
    return {
        "contract_path": CONTRACT_RELATIVE,
        "project_structure_path": PROJECT_STRUCTURE_RELATIVE,
        "verification_manifest_path": VERIFICATION_MANIFEST_RELATIVE,
        "checkpoint_path": sh07["checkpoint"]["path"],
        "changed_paths": changed["changed_paths"],
        "pass_ids": context["pass_ids"],
        "slice_ids": context["slice_ids"],
        "artifact_ids": context["artifact_ids"],
        "policy_ids": context["policy_ids"],
        "module_ids": subject["module_ids"],
        "verification_check_ids": context["verification_check_ids"],
        "development_composition_paths": [item["file"]["path"] for item in references["development_compositions"]],
        "c2_cache_witness_paths": [item["file"]["path"] for item in references["c2_cache_witnesses"]],
    }


def validate_candidate(
    candidate: Mapping[str, Any] | Path | str,
    *,
    root: Path | str = ROOT,
) -> dict[str, Any]:
    """Rebuild a candidate from current bytes and require exact equality."""

    root_path = _root_path(root)
    candidate_binding: dict[str, Any] | None = None
    if isinstance(candidate, (str, Path)):
        # Load with the fixed outer ceiling, then use its bound contract for a
        # complete rebuild.  Candidate files must themselves live in the
        # isolated target/validation tree.
        relative = _relative_to_root(root_path, candidate, label="candidate")
        if not relative.startswith("target/validation/"):
            _fail("AE003", "candidate must be stored below target/validation")
        data, info = _open_regular_relative(root_path, relative, 16 * 1024 * 1024)
        candidate_value = _strict_json_data(
            data, label="candidate", maximum_nodes=100000, maximum_depth=96
        )
        candidate_binding = {"path": relative, "size": info.st_size, "sha256": _sha256_bytes(data)}
    else:
        candidate_value = _bounded_value(
            dict(candidate), maximum_nodes=100000, maximum_depth=96, label="candidate"
        )
    return _validate_candidate_snapshot(candidate_value, candidate_binding, root_path)


def _validate_candidate_snapshot(
    candidate_value: Any,
    candidate_binding: dict[str, Any] | None,
    root_path: Path,
) -> dict[str, Any]:
    """Validate one already-coherent candidate byte snapshot."""

    if not isinstance(candidate_value, Mapping):
        _fail("AE005", "candidate must be an object")
    inputs = _candidate_inputs(candidate_value)
    rebuilt = _build_candidate(root=root_path, **inputs)
    if dict(candidate_value) != rebuilt:
        original_digest = candidate_value.get("candidate_sha256")
        payload = {key: value for key, value in candidate_value.items() if key != "candidate_sha256"}
        contract, _binding = _load_contract(root_path, inputs["contract_path"])
        recomputed = _semantic_sha256(payload, contract["identity"]["candidate_domain"])
        if original_digest != recomputed:
            _fail("AE006", "candidate digest does not recompute")
        _fail("AE008", "candidate differs from a reconstruction over current bytes")
    return {
        "status": "valid",
        "candidate_sha256": rebuilt["candidate_sha256"],
        "evidence_subject": rebuilt["evidence_subject"],
        "context_nonclaims": rebuilt["context_nonclaims"],
        "child_edn_projection_sha256": rebuilt["sh07"]["child_edn_projection_sha256"],
        "candidate_file": candidate_binding,
        "candidate_value": rebuilt,
    }


def _reviewed_input(relative: str, prefixes: Sequence[str], label: str) -> None:
    if not any(relative.startswith(prefix) for prefix in prefixes):
        _fail("AE011", f"{label} must be reviewed source below one of {list(prefixes)}")


def promote_candidate(
    candidate_path: Path | str,
    *,
    policy_path: Path | str,
    attestation_path: Path | str,
    output_path: Path | str,
    root: Path | str = ROOT,
) -> dict[str, Any]:
    """Emit a reviewed-promotion candidate without minting Python authority."""

    root_path = _root_path(root)
    candidate_relative = _relative_to_root(root_path, candidate_path, label="candidate")
    if not candidate_relative.startswith(OUTPUT_PREFIX):
        _fail("AE003", "candidate must be stored below target/validation")
    candidate_data, candidate_info = _open_regular_relative(
        root_path, candidate_relative, 16 * 1024 * 1024
    )
    candidate = _strict_json_data(
        candidate_data, label="candidate", maximum_nodes=100000, maximum_depth=96
    )
    if not isinstance(candidate, dict):
        _fail("AE005", "candidate must be an object")
    candidate_binding = {
        "path": candidate_relative,
        "size": candidate_info.st_size,
        "sha256": _sha256_bytes(candidate_data),
    }
    validation = _validate_candidate_snapshot(candidate, candidate_binding, root_path)
    candidate = validation["candidate_value"]
    contract, _contract_binding = _load_contract(root_path, CONTRACT_RELATIVE)
    policy, policy_binding = _load_json_file(root_path, policy_path, contract, "promotion policy")
    attestation, attestation_binding = _load_json_file(
        root_path, attestation_path, contract, "reviewed attestation"
    )
    _reviewed_input(policy_binding["path"], ("contracts/", "docs/"), "promotion policy")
    _reviewed_input(attestation_binding["path"], ("contracts/", "docs/"), "reviewed attestation")
    sh07_deficiencies = list(candidate["sh07"].get("deficiencies", []))
    blockers = sorted(set(
        contract["promotion"]["unconditional_blockers"]
        + sh07_deficiencies
        + ["review_material_self_asserted_not_trusted"]
        + ([] if candidate["repository"]["commit_bound"] else ["repository_commit_unbound"])
    ))
    promotion: dict[str, Any] = {
        "schema_version": SCHEMA_VERSION,
        "kind": contract["promotion"]["kind"],
        "repository": candidate["repository"],
        "claims": _claims(),
        "evidence_subject": candidate["evidence_subject"],
        "context_nonclaims": candidate["context_nonclaims"],
        "candidate": {
            "file": validation["candidate_file"],
            "candidate_sha256": candidate["candidate_sha256"],
        },
        "review_material": {
            "policy": {"file": policy_binding, "semantic_sha256": _semantic_sha256(
                policy, "gravity.authoritative-evidence/untrusted-policy/v1"
            )},
            "attestation": {"file": attestation_binding, "semantic_sha256": _semantic_sha256(
                attestation, "gravity.authoritative-evidence/untrusted-attestation/v1"
            )},
            "trusted_admission": False,
            "authority_contribution": False,
        },
        "promotion_blocked": True,
        "blockers": blockers,
        "revalidation": {
            "status": "non-authoritative-blocked",
            "current_bytes": "runtime_launcher_canonical_target_unbound" not in sh07_deficiencies,
            "current_semantics": True,
            "dependency_closures": True,
            "fresh_process": True,
            "no_resumed_modules": True,
            "no_iteration_cache": True,
            "no_proof_receipt_cache": not any(
                item.startswith("proof_receipt_cache") for item in sh07_deficiencies
            ),
            "output_contract": True,
            "cryptographic_child_ids": "cryptographic_child_identity_incomplete" not in sh07_deficiencies,
            "trusted_child_edn_projection": False,
            "child_edn_projection_sha256": validation["child_edn_projection_sha256"],
        },
    }
    promotion["promotion_candidate_sha256"] = _semantic_sha256(
        promotion, contract["identity"]["promotion_domain"]
    )
    _write_output(root_path, output_path, promotion, contract)
    return promotion


def _parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--root", type=Path, default=ROOT)
    subparsers = parser.add_subparsers(dest="operation", required=True)

    compose = subparsers.add_parser("compose", help="compose a non-authoritative candidate")
    compose.add_argument("--checkpoint", required=True, type=Path)
    compose.add_argument("--changed-path", action="append", required=True)
    compose.add_argument("--pass", dest="pass_ids", action="append", required=True)
    compose.add_argument("--slice", dest="slice_ids", action="append", required=True)
    compose.add_argument("--module", dest="module_ids", action="append", required=True)
    compose.add_argument("--verification-check", dest="verification_check_ids", action="append", required=True)
    compose.add_argument("--artifact", dest="artifact_ids", action="append")
    compose.add_argument("--policy", dest="policy_ids", action="append")
    compose.add_argument("--development-composition", action="append", default=[])
    compose.add_argument("--c2-cache-witness", action="append", default=[])
    compose.add_argument("--out", required=True, type=Path)

    validate = subparsers.add_parser("validate", help="revalidate a candidate")
    validate.add_argument("candidate", type=Path)

    promote = subparsers.add_parser(
        "promote", help="compose a blocked non-authoritative reviewed-promotion candidate"
    )
    promote.add_argument("candidate", type=Path)
    promote.add_argument("--policy", required=True, type=Path)
    promote.add_argument("--attestation", required=True, type=Path)
    promote.add_argument("--out", required=True, type=Path)
    return parser


def main(argv: Sequence[str] | None = None) -> int:
    args = _parser().parse_args(argv)
    try:
        if args.operation == "compose":
            result = compose_candidate(
                checkpoint_path=args.checkpoint,
                changed_paths=sorted(set(args.changed_path)),
                pass_ids=sorted(set(args.pass_ids)),
                slice_ids=sorted(set(args.slice_ids)),
                module_ids=sorted(set(args.module_ids)),
                verification_check_ids=sorted(set(args.verification_check_ids)),
                artifact_ids=sorted(set(args.artifact_ids)) if args.artifact_ids else None,
                policy_ids=sorted(set(args.policy_ids)) if args.policy_ids else None,
                development_composition_paths=args.development_composition,
                c2_cache_witness_paths=args.c2_cache_witness,
                output_path=args.out,
                root=args.root,
            )
            print(result["candidate_sha256"])
        elif args.operation == "validate":
            result = validate_candidate(args.candidate, root=args.root)
            print(result["candidate_sha256"])
        else:
            result = promote_candidate(
                args.candidate,
                policy_path=args.policy,
                attestation_path=args.attestation,
                output_path=args.out,
                root=args.root,
            )
            print(result["promotion_candidate_sha256"])
        return 0
    except EvidenceError as exc:
        print(str(exc), file=sys.stderr)
        return 2


if __name__ == "__main__":
    raise SystemExit(main())
