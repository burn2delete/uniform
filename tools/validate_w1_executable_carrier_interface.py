#!/usr/bin/env python3
"""Fail-closed validator for the W1 Linux executable-carrier interface.

The validator recognizes two deliberately different envelopes:

* development evidence produced by an emulated ``linux/amd64`` Docker run;
* the final interface shape, which remains inadmissible until a separate
  non-JVM replay contract is frozen and independently reviewed.

It does not run a compiler or infer facts from prose.  All identities are
lower-case, full SHA-256 values over canonical JSON or explicitly supplied
bytes.  Unknown keys, target aliases, C/Darwin residue, Stage9 evidence,
status-only verification, and authority overclaims fail closed.  The current
Clojure/JVM predicate is integration-only evidence and cannot authorize the
later replay contract.
"""

from __future__ import annotations

import argparse
import base64
import binascii
import copy
import hashlib
import json
from pathlib import Path
import re
import sys
from typing import Any, Mapping, Sequence


ROOT = Path(__file__).resolve().parents[1]
DEFAULT_ARTIFACT = ROOT / "docs/artifacts/workstreams/w1/w1-executable-carrier-interface.json"
DEVELOPMENT_ARTIFACT = ROOT / (
    "docs/artifacts/workstreams/w1/development/"
    "w1-executable-carrier-linux-amd64-emulated-evidence.json"
)

FINAL_ARTIFACT_KIND = "gravity/w1-executable-carrier-interface"
DEVELOPMENT_ARTIFACT_KIND = "gravity/w1-executable-carrier-linux-amd64-emulated-development-evidence"
FINAL_DOCUMENT_SCHEMA = "gravity.w1.executable-carrier-interface-document/v1"
DEVELOPMENT_DOCUMENT_SCHEMA = "gravity.w1.executable-carrier-development-evidence/v1"
INTERFACE_KIND = ":w1/executable-c13-c14-b1-llvm-x86_64-linux-backend"
INTERFACE_SCHEMA = "gravity.w1.executable-carrier-interface/v1"
TARGET = ":llvm-x86_64-linux"
PROFILE = ":hosted"

C13_KIND = ":gravity/c13-bounded-identity-optimized-mir"
C14_KIND = ":gravity/c14-bounded-llvm-lowering-record"
B1_KIND = ":gravity/b1-verified-backend-input-packet"
B3_KIND = ":gravity/p15-s23-b3-authenticated-llvm-x86_64-linux-artifact"
B3_SCHEMA_VERSION = 1
EMISSION_KIND = ":gravity/b3-llvm-x86_64-linux-elf-emission"
VERIFIER_PREDICATE = "gravity.bootstrap/p15-s23-stage2-b3-llvm-verify!"
VERIFIER_PREDICATE_VERSION = 1
CONSUMER_CONTRACT = ":gravity/p15-public-native-admission"
CONSUMER_CONTRACT_VERSION = 1

PINNED_IMAGE = "silkeh/clang@sha256:ae2f3deffd84470fbb2904cfb990db208a5f9880b4bcf9d3eae080a50a8900b4"
IMAGE_PLATFORM = "linux/amd64"
PULL_POLICY = "--pull=never"
LLVM_VERSION = "20.1.8"
LLVM_TOOLS = (
    "clang", "ld.lld", "llc", "llvm-as", "llvm-dis", "llvm-objdump",
    "llvm-readobj", "opt",
)

# The target identity is intentionally repeated here rather than inferred
# from an LLVM spelling.  C11/C13 ingress names the public target, while C14
# keeps its internal backend request as ``:llvm``.  The JSON handoff must bind
# the canonical target at every externally visible boundary.
LLVM_TRIPLE = "x86_64-unknown-linux-gnu"
LLVM_DATA_LAYOUT = "e-m:e-p270:32:32-p271:32:32-p272:64:64-i64:64-i128:128-f80:128-n8:16:32:64-S128"
LLVM_ABI = "SysV"
LLVM_ARCHITECTURE = "x86_64"
LLVM_OBJECT_FORMAT = "ELF"
# This is deliberately an integration-only command.  It names the SH17 test
# namespace and its opt-in development-tools gate; it is not a public or
# non-JVM replay command.  No final argv is frozen here.
INTEGRATION_VERIFIER_COMMAND = [
    "env", "GRAVITY_RUN_LINUX_DEVELOPMENT_TOOLS=1", "clojure", "-M:test", "--namespace",
    "gravity.self-hosting.sh17-c13-c14-b1-linux-llvm-backend-continuity-test",
]
REVIEW_KIND = "gravity/w1-independent-sol-review"
REVIEW_SCHEMA = "gravity.w1.independent-sol-review/v1"
REPLAY_CONTRACT_UNFROZEN = "W1-REPLAY-CONTRACT-UNFROZEN"
FINAL_NON_JVM_REPLAY_MISSING = "W1-FINAL-NON-JVM-REPLAY-MISSING"
SOURCE_ID_PREIMAGE_UNRESOLVED = "W1-SOURCE-ID-PREIMAGE-UNRESOLVED"
C13_SEMANTIC_PREIMAGE_UNRESOLVED = "W1-C13-SEMANTIC-PREIMAGE-UNRESOLVED"

KEYWORD_RE = re.compile(r"^:[a-z0-9][a-z0-9/_?-]*$")

CANONICALIZATION = "json-sort-keys-compact-utf8"
GRAVITY_MIR_DIGEST_ALGORITHM = "gravity-p15-s23-c11-mir-digest/v1"
TARGET_FINGERPRINT_ALGORITHM = GRAVITY_MIR_DIGEST_ALGORITHM
TARGET_FINGERPRINT_KIND = ":gravity/c14-bounded-llvm-target-fingerprint"
SELF_ID_SENTINEL = "__w1_self_id_excluded__"
HASH_RE = re.compile(r"^sha256:[0-9a-f]{64}$")
GIT_RE = re.compile(r"^[0-9a-f]{40}$")

TOP_COMMON = {
    "artifact_kind", "document_schema", "schema_version", "mode",
    "interface_kind", "interface_schema", "evidence_identity", "producer",
    "inputs", "carrier", "lowering", "backend_input", "backend", "emission",
    "transcripts", "diagnostics", "provenance", "verification",
    "environment", "authority", "review",
}
TOP_DEVELOPMENT = TOP_COMMON | {"consumer_handoff_candidate"}
TOP_FINAL = TOP_COMMON | {"consumer_handoff"}

SECTION_KEYS = {
    "inputs": {"id", "source", "fixtures"},
    "carrier": {"id", "artifact_id", "kind", "schema_version", "status", "profile", "target", "source_id", "semantic_id", "effects", "capabilities", "safety", "content", "content_hash"},
    "lowering": {"id", "artifact_id", "kind", "schema_version", "status", "profile", "target", "backend", "input_artifact_id", "record", "content_hash"},
    "backend_input": {"id", "artifact_id", "kind", "schema_version", "status", "profile", "target", "input_artifact_id", "lowering_artifact_id", "packet", "content_hash"},
    "backend": {"id", "artifact_id", "kind", "schema_version", "status", "profile", "target", "input_artifact_id", "llvm_version", "module", "content_hash"},
    "emission": {"id", "artifact_id", "kind", "schema_version", "status", "profile", "target", "backend_artifact_id", "object", "executable"},
    "transcripts": {"id", "positive", "negative"},
    "diagnostics": {"id", "accepted", "rejected"},
    "provenance": {"id", "lineage", "edges"},
    "verification": {"id", "predicate", "predicate_version", "command", "status", "replay", "replay_contract_frozen", "diagnostic_ids"},
    "authority": {"id", "development_only", "emulated", "authoritative", "native_replay", "public_route", "clojure_seed_boundary", "self_hosted", "release", "inventory"},
    "review": {"id", "status", "reviewer_class", "reviewed_commit", "reviewed_tree", "review_artifact_id", "review_path", "review_kind", "review_schema", "review_content_sha256", "independent"},
}

HANDOFF_KEYS = {"contract", "contract-version", "workstream", "interface-kind", "interface-schema", "artifact-id", "producer-commit", "producer-tree", "verifier", "review", "bindings", "claims"}
HANDOFF_VERIFIER_KEYS = {"predicate", "predicate-version", "replay-artifact-id", "replay-content-hash", "status"}
HANDOFF_REVIEW_KEYS = {"status", "reviewer-class", "reviewed-commit", "review-artifact-id"}
HANDOFF_BINDING_KEYS = {"carrier-artifact-id", "carrier-content-hash", "carrier-schema", "source-id", "semantic-id", "profile", "target", "effects", "capabilities", "safety", "accepted-diagnostic-ids", "rejected-diagnostic-ids", "provenance-edges"}
HANDOFF_CLAIM_KEYS = {"public-route?", "clojure-seed-boundary?", "self-hosted?", "release?"}

DEV_ENV_KEYS = {"execution", "image", "image_platform", "pull_policy", "llvm_version", "tools", "docker", "emulation"}
FINAL_ENV_KEYS = {"execution", "os", "arch", "abi", "llvm_version", "tools", "kernel_id", "machine_id"}
TOOL_KEYS = set(LLVM_TOOLS)
DOCKER_KEYS = {"engine_version", "engine_id", "host_id", "host_os", "host_arch"}
EMULATION_KEYS = {"kind", "qemu_id", "binfmt_id"}
TRANSCRIPT_KEYS = {"id", "path", "content_sha256", "exit_code", "diagnostic_ids"}
REPLAY_KEYS = {"artifact_id", "content_hash", "transcript_id", "native"}
EDGE_KEYS = {"from_id", "to_id", "kind", "target"}
BLOB_KEYS = {"bytes_base64", "byte_count", "content_sha256", "format", "architecture", "abi"}
INPUT_FIXTURE_KEYS = {"path", "content_sha256"}
AUTHORITY_INVENTORY_KEYS = {"clojure", "jvm", "process", "file", "public_wrapper"}
AUTHORITY_INVENTORY = {
    "clojure": ":integration-only",
    "jvm": ":integration-only",
    "process": ":integration-only",
    "file": ":integration-only",
    "public_wrapper": ":none",
}
C14_RECORD_KEYS = {"source", "target", "target_fingerprint_algorithm"}
B1_PACKET_KEYS = {"lowering", "abi", "target"}
B3_MODULE_KEYS = {"llvm_ir", "input"}
C13_CONTENT_KEYS = {
    "mir", "source_id", "semantic_id", "semantic_id_algorithm",
    "semantic_id_preimage_edn",
}
INPUT_SOURCE_KEYS = {
    "path", "content_sha256", "source_id", "source_id_algorithm",
    "source_id_preimage_edn",
}
TARGET_RECORD_KEYS = {
    "request", "canonical_target", "triple", "data_layout", "cpu",
    "features", "object_format", "architecture", "relocation_model",
    "code_model", "optimization_level", "minimum_os_version",
    "sanitizers", "instrumentation", "backend", "tier", "exposure",
    "source_declaration_target", "requested_lowering_target", "selection",
    "reason", "direct_source_declared_llvm", "profile_eligibility",
    "fingerprint",
}
TARGET_RECORD = {
    "request": ":llvm",
    "canonical_target": TARGET,
    "triple": LLVM_TRIPLE,
    "data_layout": LLVM_DATA_LAYOUT,
    "cpu": "generic",
    "features": "",
    "object_format": ":elf",
    "architecture": ":x86_64",
    "relocation_model": ":pic",
    "code_model": ":small",
    "optimization_level": ":O0",
    "minimum_os_version": ":not-applicable",
    "sanitizers": [],
    "instrumentation": [],
    "backend": ":gravity.backend/llvm",
    "tier": ":experimental",
    "exposure": ":internal",
    "source_declaration_target": ":jvm",
    "requested_lowering_target": ":llvm",
    "selection": ":explicit-bootstrap-seed-target-override",
    "reason": ":checked-core-seed-contract",
    "direct_source_declared_llvm": False,
    "profile_eligibility": [PROFILE],
}

FORBIDDEN_MARKERS = (
    "stage9", "stage_9", "sh16", "darwin", "mach-o", "macho", "arm64",
    "aarch64", ":llvm-x86-64-linux", ":llvm\"", ":llvm'",
    "bounded-c-lowering", "b2-c", "c17-gate-b", "gate-b",
)


class ValidationError(ValueError):
    def __init__(self, errors: Sequence[str]):
        self.errors = tuple(errors)
        super().__init__("; ".join(errors))


def canonical_json(value: Any) -> bytes:
    return json.dumps(value, ensure_ascii=False, allow_nan=False, sort_keys=True, separators=(",", ":")).encode("utf-8")


def canonical_sha256(value: Any) -> str:
    return "sha256:" + hashlib.sha256(canonical_json(value)).hexdigest()


class _EdnKeyword(str):
    """Internal marker used by the exact Gravity canonical-EDN projection."""


def _gravity_canonical_edn(value: Any) -> str:
    """Serialize the bounded target preimage like c-backend-canonical-value/pr-str."""
    if isinstance(value, _EdnKeyword):
        return str(value)
    if isinstance(value, str):
        return json.dumps(value, ensure_ascii=False, separators=(",", ":"))
    if value is True:
        return "true"
    if value is False:
        return "false"
    if value is None:
        return "nil"
    if isinstance(value, list):
        return "[" + " ".join(_gravity_canonical_edn(item) for item in value) + "]"
    if isinstance(value, Mapping):
        entries = sorted(value.items(), key=lambda item: _gravity_canonical_edn(item[0]))
        return "{" + ", ".join(
            _gravity_canonical_edn(key) + " " + _gravity_canonical_edn(child)
            for key, child in entries
        ) + "}"
    raise TypeError(f"unsupported canonical EDN value {type(value).__name__}")


def gravity_mir_digest_preimage(canonical_edn_preimage: str) -> str:
    """Reproduce p15-s23-c11-mir-digest from its exact canonical EDN bytes."""
    return "sha256:" + hashlib.sha256(canonical_edn_preimage.encode("utf-8")).hexdigest()


def target_fingerprint(record: Mapping[str, Any]) -> str:
    """Reproduce the P15-S23 C14 target fingerprint, never a constant lookup."""
    target: dict[_EdnKeyword, Any] = {}
    for key, value in record.items():
        if key == "fingerprint":
            continue
        edn_key = key.replace("_", "-")
        if key == "direct_source_declared_llvm":
            edn_key += "?"
        if isinstance(value, str) and value.startswith(":"):
            edn_value: Any = _EdnKeyword(value)
        elif isinstance(value, list):
            edn_value = [
                _EdnKeyword(item) if isinstance(item, str) and item.startswith(":") else item
                for item in value
            ]
        else:
            edn_value = value
        target[_EdnKeyword(":" + edn_key)] = edn_value
    preimage = {
        _EdnKeyword(":kind"): _EdnKeyword(TARGET_FINGERPRINT_KIND),
        _EdnKeyword(":target"): target,
    }
    return gravity_mir_digest_preimage(_gravity_canonical_edn(preimage))


def section(payload: Mapping[str, Any]) -> dict[str, Any]:
    """Return a canonical section.  Exported for producers and tests."""
    result = copy.deepcopy(dict(payload))
    result["id"] = canonical_sha256(result)
    return result


def artifact_id(kind: str, inputs: Sequence[str], content_hash: str) -> str:
    """Recompute an artifact id from its exact kind, ordered inputs, and bytes/content."""
    return canonical_sha256({"kind": kind, "inputs": list(inputs), "content_hash": content_hash})


def canonical_evidence_payload(document: Mapping[str, Any]) -> dict[str, Any]:
    payload = copy.deepcopy(dict(document))
    evidence = payload.get("evidence_identity")
    if isinstance(evidence, dict):
        evidence["self_id"] = SELF_ID_SENTINEL
    handoff = payload.get("consumer_handoff")
    if isinstance(handoff, dict):
        handoff["artifact-id"] = SELF_ID_SENTINEL
    return payload


def canonical_evidence_sha256(document: Mapping[str, Any]) -> str:
    return canonical_sha256(canonical_evidence_payload(document))


def _err(errors: list[str], path: str, message: str) -> None:
    errors.append(f"{path}: {message}")


def _object(value: Any, keys: set[str], path: str, errors: list[str]) -> bool:
    if not isinstance(value, Mapping):
        _err(errors, path, "must be an object")
        return False
    actual_keys = set(value)
    if any(not isinstance(key, str) for key in actual_keys):
        _err(errors, path, "all keys must be ASCII strings")
        return False
    if any(not key.isascii() for key in actual_keys):
        _err(errors, path, "all keys must be ASCII strings")
    missing, unknown = sorted(keys - actual_keys), sorted(actual_keys - keys)
    if missing:
        _err(errors, path, "missing keys: " + ", ".join(missing))
    if unknown:
        _err(errors, path, "unknown keys: " + ", ".join(unknown))
    return not missing and not unknown


def _hash(value: Any, path: str, errors: list[str]) -> bool:
    if not isinstance(value, str) or not HASH_RE.fullmatch(value) or value == "sha256:" + "0" * 64:
        _err(errors, path, "must be a nonzero sha256:<64 lowercase hex>")
        return False
    return True


def _identity_preimage(value: Any, path: str, errors: list[str]) -> str | None:
    """Accept exact, single-line canonical EDN bytes for a production digest."""
    if not isinstance(value, str) or not value or not value.isascii():
        _err(errors, path, "must be a non-empty ASCII canonical EDN preimage")
        return None
    if value != value.strip() or "\n" in value or "\r" in value or "\x00" in value:
        _err(errors, path, "must be exact single-line canonical EDN without padding")
        return None
    if value[0] not in "{[(":
        _err(errors, path, "must be an explicit collection preimage")
        return None
    return value


def _git(value: Any, path: str, errors: list[str]) -> bool:
    if not isinstance(value, str) or not GIT_RE.fullmatch(value):
        _err(errors, path, "must be 40 lowercase hexadecimal characters")
        return False
    return True


def _strings(value: Any, path: str, errors: list[str], *, nonempty: bool = False) -> bool:
    if not isinstance(value, list) or not all(isinstance(item, str) and item for item in value):
        _err(errors, path, "must be an array of non-empty strings")
        return False
    if nonempty and not value:
        _err(errors, path, "must not be empty")
    if value != sorted(set(value)):
        _err(errors, path, "must be sorted with no duplicates")
    return True


def _keywords(value: Any, path: str, errors: list[str], *, nonempty: bool = False) -> bool:
    """Validate a sorted EDN-keyword projection serialized as JSON strings."""
    if not _strings(value, path, errors, nonempty=nonempty):
        return False
    for index, item in enumerate(value):
        if KEYWORD_RE.fullmatch(item) is None:
            _err(errors, f"{path}[{index}]", "must be a lowercase EDN keyword spelling")
    return True


def _section(value: Any, name: str, errors: list[str]) -> bool:
    if not _object(value, SECTION_KEYS[name], name, errors):
        return False
    claimed = value.get("id")
    if _hash(claimed, f"{name}.id", errors):
        payload = dict(value)
        payload.pop("id")
        expected = canonical_sha256(payload)
        if claimed != expected:
            _err(errors, f"{name}.id", f"canonical mismatch; expected {expected}")
    return True


def _expect(value: Any, expected: Any, path: str, errors: list[str]) -> None:
    if value != expected:
        _err(errors, path, f"must equal {expected!r}")


def _scan_forbidden(value: Any, path: str, errors: list[str]) -> None:
    if isinstance(value, str):
        lowered = value.lower()
        for marker in FORBIDDEN_MARKERS:
            if marker in lowered:
                _err(errors, path, f"contains forbidden cross-target/evidence marker {marker!r}")
                return
    elif isinstance(value, Mapping):
        for key, child in value.items():
            _scan_forbidden(child, f"{path}.{key}", errors)
    elif isinstance(value, list):
        for index, child in enumerate(value):
            _scan_forbidden(child, f"{path}[{index}]", errors)


def _scan_non_circular_keys(value: Any, path: str, errors: list[str]) -> None:
    """Reject aliases that would smuggle payload-containing B/C into A."""
    if isinstance(value, Mapping):
        for key, child in value.items():
            if isinstance(key, str):
                normalized = key.lower().replace("_", "-")
                if any(token in normalized for token in ("payload-containing", "integrated-commit", "integrated-tree", "containing-commit", "containing-tree")):
                    _err(errors, f"{path}.{key}", "payload may contain reviewed A only; B/C identity is external")
            _scan_non_circular_keys(child, f"{path}.{key}", errors)
    elif isinstance(value, list):
        for index, child in enumerate(value):
            _scan_non_circular_keys(child, f"{path}[{index}]", errors)


def _repository_file(value: Any, path: str, content_hash: Any, errors: list[str]) -> None:
    if not isinstance(value, str) or not value:
        _err(errors, path, "must be a non-empty repository-relative path")
        return
    try:
        value.encode("ascii")
    except UnicodeEncodeError:
        _err(errors, path, "must contain ASCII only")
        return
    parts = value.split("/")
    if value.startswith("/") or "\\" in value or any(part in {"", ".", ".."} for part in parts):
        _err(errors, path, "must be normalized, repository-relative, and traversal-free")
        return
    candidate = ROOT.joinpath(*parts)
    try:
        candidate.resolve(strict=False).relative_to(ROOT.resolve())
    except ValueError:
        _err(errors, path, "must resolve inside the repository")
        return
    try:
        raw = candidate.read_bytes()
    except OSError as exc:
        _err(errors, path, f"cannot read exact repository file: {exc}")
        return
    actual = "sha256:" + hashlib.sha256(raw).hexdigest()
    if content_hash != actual:
        hash_path = path[:-4] + "content_sha256" if path.endswith("path") else path
        _err(errors, hash_path, f"does not match exact file bytes ({actual})")


def _blob(
    value: Any,
    path: str,
    *,
    expected_elf_types: set[int],
    errors: list[str],
) -> None:
    if not _object(value, BLOB_KEYS, path, errors):
        return
    _expect(value.get("format"), "ELF", f"{path}.format", errors)
    _expect(value.get("architecture"), "x86_64", f"{path}.architecture", errors)
    _expect(value.get("abi"), "SysV", f"{path}.abi", errors)
    encoded = value.get("bytes_base64")
    try:
        if not isinstance(encoded, str) or not encoded:
            raise ValueError
        raw = base64.b64decode(encoded, validate=True)
    except (ValueError, binascii.Error):
        _err(errors, f"{path}.bytes_base64", "must be non-empty canonical base64")
        return
    if base64.b64encode(raw).decode("ascii") != encoded:
        _err(errors, f"{path}.bytes_base64", "must use canonical padded base64")
    if value.get("byte_count") != len(raw):
        _err(errors, f"{path}.byte_count", f"does not match emitted bytes ({len(raw)})")
    actual = "sha256:" + hashlib.sha256(raw).hexdigest()
    if value.get("content_sha256") != actual:
        _err(errors, f"{path}.content_sha256", f"does not match emitted bytes ({actual})")
    # Labels and hashes do not establish target coherence. Recompute the
    # invariant ELF64/x86_64 fields from the authenticated bytes themselves.
    if len(raw) < 64:
        _err(errors, f"{path}.bytes_base64", "ELF bytes are shorter than the 64-byte ELF64 header")
        return
    if len(raw) == 64:
        _err(errors, f"{path}.bytes_base64", "ELF bytes must contain payload beyond the ELF64 header")
    if raw[:4] != b"\x7fELF":
        _err(errors, f"{path}.bytes_base64", "missing ELF magic")
    if raw[4] != 2:
        _err(errors, f"{path}.bytes_base64", "ELF class must be ELFCLASS64")
    if raw[5] != 1:
        _err(errors, f"{path}.bytes_base64", "ELF byte order must be little-endian")
    if raw[6] != 1:
        _err(errors, f"{path}.bytes_base64", "ELF identity version must be 1")
    elf_type = int.from_bytes(raw[16:18], "little")
    machine = int.from_bytes(raw[18:20], "little")
    if elf_type not in expected_elf_types:
        expected = ", ".join(str(item) for item in sorted(expected_elf_types))
        _err(errors, f"{path}.bytes_base64", f"ELF type {elf_type} is not one of {{{expected}}}")
    if machine != 62:
        _err(errors, f"{path}.bytes_base64", f"ELF machine must be EM_X86_64 (62), got {machine}")


def _artifact_section(value: Mapping[str, Any], name: str, *, kind: str, inputs: list[str], content_hash: str, final: bool, errors: list[str]) -> None:
    _expect(value.get("kind"), kind, f"{name}.kind", errors)
    _expect(value.get("schema_version"), 1, f"{name}.schema_version", errors)
    _expect(value.get("status"), ":accepted" if final else ":development-observed", f"{name}.status", errors)
    _expect(value.get("profile"), PROFILE, f"{name}.profile", errors)
    _expect(value.get("target"), TARGET, f"{name}.target", errors)
    expected = artifact_id(kind, inputs, content_hash)
    if value.get("artifact_id") != expected:
        _err(errors, f"{name}.artifact_id", f"lineage/content mismatch; expected {expected}")


def _validate_inputs(document: Mapping[str, Any], errors: list[str]) -> None:
    """Recompute source and fixture identities from repository bytes.

    A transcript is not a source substitute: every transcript path must be
    represented by the explicit fixture inventory, while the source record is
    separately bound to the C13 source id.  This prevents a status-only
    record from quietly changing the input file under the same semantic id.
    """
    _section(document.get("inputs"), "inputs", errors)
    inputs = document.get("inputs")
    if not isinstance(inputs, Mapping):
        return
    source = inputs.get("source")
    if _object(source, INPUT_SOURCE_KEYS, "inputs.source", errors):
        _hash(source.get("content_sha256"), "inputs.source.content_sha256", errors)
        _hash(source.get("source_id"), "inputs.source.source_id", errors)
        _expect(
            source.get("source_id_algorithm"), GRAVITY_MIR_DIGEST_ALGORITHM,
            "inputs.source.source_id_algorithm", errors,
        )
        source_preimage = _identity_preimage(
            source.get("source_id_preimage_edn"),
            "inputs.source.source_id_preimage_edn", errors,
        )
        if source_preimage is not None:
            expected_source_id = gravity_mir_digest_preimage(source_preimage)
            if source.get("source_id") != expected_source_id:
                _err(
                    errors, "inputs.source.source_id",
                    f"production preimage mismatch; expected {expected_source_id}",
                )
            _err(
                errors, "inputs.source.source_id_preimage_edn",
                SOURCE_ID_PREIMAGE_UNRESOLVED
                + ": reduced JSON does not prove the preimage is the validated source record",
            )
        _repository_file(source.get("path"), "inputs.source.path", source.get("content_sha256"), errors)
        carrier = document.get("carrier")
        if isinstance(carrier, Mapping):
            _expect(source.get("source_id"), carrier.get("source_id"), "inputs.source.source_id", errors)
    fixtures = inputs.get("fixtures")
    if not isinstance(fixtures, list) or not fixtures:
        _err(errors, "inputs.fixtures", "must be a non-empty array")
        return
    seen: set[str] = set()
    fixture_pairs: set[tuple[str, str]] = set()
    for index, fixture in enumerate(fixtures):
        path = f"inputs.fixtures[{index}]"
        if not _object(fixture, INPUT_FIXTURE_KEYS, path, errors):
            continue
        fixture_path, content_hash = fixture.get("path"), fixture.get("content_sha256")
        _hash(content_hash, f"{path}.content_sha256", errors)
        _repository_file(fixture_path, f"{path}.path", content_hash, errors)
        if isinstance(fixture_path, str):
            if fixture_path in seen:
                _err(errors, f"{path}.path", "fixture paths must be unique")
            seen.add(fixture_path)
            fixture_pairs.add((fixture_path, content_hash))
    if fixtures != sorted(fixtures, key=lambda item: item.get("path", "") if isinstance(item, Mapping) else ""):
        _err(errors, "inputs.fixtures", "must be sorted by normalized path")
    transcripts = document.get("transcripts")
    if isinstance(transcripts, Mapping):
        for group in ("positive", "negative"):
            values = transcripts.get(group)
            if not isinstance(values, list):
                continue
            for index, transcript in enumerate(values):
                if not isinstance(transcript, Mapping):
                    continue
                pair = (transcript.get("path"), transcript.get("content_sha256"))
                if pair not in fixture_pairs:
                    _err(errors, f"transcripts.{group}[{index}]", "path/hash pair must be listed in inputs.fixtures")


def _validate_pipeline(document: Mapping[str, Any], *, final: bool, errors: list[str]) -> None:
    for name in ("carrier", "lowering", "backend_input", "backend", "emission"):
        _section(document.get(name), name, errors)
    carrier, lowering = document.get("carrier"), document.get("lowering")
    backend_input, backend, emission = document.get("backend_input"), document.get("backend"), document.get("emission")
    if not all(isinstance(item, Mapping) for item in (carrier, lowering, backend_input, backend, emission)):
        return

    content = carrier.get("content")
    if _object(content, C13_CONTENT_KEYS, "carrier.content", errors):
        _hash(content.get("source_id"), "carrier.content.source_id", errors)
        _hash(content.get("semantic_id"), "carrier.content.semantic_id", errors)
        _expect(
            content.get("semantic_id_algorithm"), GRAVITY_MIR_DIGEST_ALGORITHM,
            "carrier.content.semantic_id_algorithm", errors,
        )
        semantic_preimage = _identity_preimage(
            content.get("semantic_id_preimage_edn"),
            "carrier.content.semantic_id_preimage_edn", errors,
        )
        if semantic_preimage is not None:
            expected_semantic_id = gravity_mir_digest_preimage(semantic_preimage)
            if content.get("semantic_id") != expected_semantic_id:
                _err(
                    errors, "carrier.content.semantic_id",
                    f"production C13 preimage mismatch; expected {expected_semantic_id}",
                )
            _err(
                errors, "carrier.content.semantic_id_preimage_edn",
                C13_SEMANTIC_PREIMAGE_UNRESOLVED
                + ": reduced JSON does not carry the complete C13 stage-semantic record",
            )
        _expect(content.get("source_id"), carrier.get("source_id"), "carrier.content.source_id", errors)
        _expect(content.get("semantic_id"), carrier.get("semantic_id"), "carrier.content.semantic_id", errors)
    content_hash = canonical_sha256(content)
    if carrier.get("content_hash") != content_hash:
        _err(errors, "carrier.content_hash", f"canonical mismatch; expected {content_hash}")
    _artifact_section(carrier, "carrier", kind=C13_KIND, inputs=[carrier.get("source_id")], content_hash=content_hash, final=final, errors=errors)
    _hash(carrier.get("source_id"), "carrier.source_id", errors)
    _hash(carrier.get("semantic_id"), "carrier.semantic_id", errors)
    if carrier.get("source_id") == carrier.get("semantic_id"):
        _err(errors, "carrier", "source and semantic identities must be distinct")
    _keywords(carrier.get("effects"), "carrier.effects", errors)
    _keywords(carrier.get("capabilities"), "carrier.capabilities", errors)
    safety = carrier.get("safety")
    if not _object(safety, {"mode", "outcomes"}, "carrier.safety", errors):
        safety = None
    else:
        _expect(safety.get("mode"), ":safe", "carrier.safety.mode", errors)
        outcomes = safety.get("outcomes")
        if not _object(outcomes, {"proven_safe", "runtime_checked", "rejected", "unsafe_island"}, "carrier.safety.outcomes", errors):
            pass
        elif not all(isinstance(outcomes[key], int) and not isinstance(outcomes[key], bool) and outcomes[key] >= 0 for key in outcomes):
            _err(errors, "carrier.safety.outcomes", "all counts must be nonnegative integers")
        elif outcomes.get("unsafe_island") != 0:
            _err(errors, "carrier.safety.outcomes.unsafe_island", "safe executable carrier must not contain an unsafe island")

    record = lowering.get("record")
    if _object(record, C14_RECORD_KEYS, "lowering.record", errors):
        _expect(record.get("source"), carrier.get("artifact_id"), "lowering.record.source", errors)
        _expect(
            record.get("target_fingerprint_algorithm"),
            TARGET_FINGERPRINT_ALGORITHM,
            "lowering.record.target_fingerprint_algorithm", errors,
        )
        target = record.get("target")
        if _object(target, TARGET_RECORD_KEYS, "lowering.record.target", errors):
            expected_target = dict(TARGET_RECORD)
            observed_fingerprint = target.get("fingerprint")
            for key, expected_value in expected_target.items():
                _expect(target.get(key), expected_value, f"lowering.record.target.{key}", errors)
            expected_fingerprint = target_fingerprint(target)
            if observed_fingerprint != expected_fingerprint:
                _err(
                    errors, "lowering.record.target.fingerprint",
                    f"canonical Gravity target preimage mismatch; expected {expected_fingerprint}",
                )
    lowering_hash = canonical_sha256(record)
    if lowering.get("content_hash") != lowering_hash:
        _err(errors, "lowering.content_hash", f"canonical mismatch; expected {lowering_hash}")
    _expect(lowering.get("backend"), ":llvm", "lowering.backend", errors)
    _expect(lowering.get("input_artifact_id"), carrier.get("artifact_id"), "lowering.input_artifact_id", errors)
    _artifact_section(lowering, "lowering", kind=C14_KIND, inputs=[carrier.get("artifact_id")], content_hash=lowering_hash, final=final, errors=errors)

    packet = backend_input.get("packet")
    if _object(packet, B1_PACKET_KEYS, "backend_input.packet", errors):
        _expect(packet.get("lowering"), lowering.get("artifact_id"), "backend_input.packet.lowering", errors)
        _expect(packet.get("abi"), LLVM_ABI, "backend_input.packet.abi", errors)
        _expect(packet.get("target"), TARGET, "backend_input.packet.target", errors)
    packet_hash = canonical_sha256(packet)
    if backend_input.get("content_hash") != packet_hash:
        _err(errors, "backend_input.content_hash", f"canonical mismatch; expected {packet_hash}")
    _expect(backend_input.get("input_artifact_id"), carrier.get("artifact_id"), "backend_input.input_artifact_id", errors)
    _expect(backend_input.get("lowering_artifact_id"), lowering.get("artifact_id"), "backend_input.lowering_artifact_id", errors)
    _artifact_section(backend_input, "backend_input", kind=B1_KIND, inputs=[carrier.get("artifact_id"), lowering.get("artifact_id")], content_hash=packet_hash, final=final, errors=errors)

    module = backend.get("module")
    if _object(module, B3_MODULE_KEYS, "backend.module", errors):
        if not isinstance(module.get("llvm_ir"), str) or not module.get("llvm_ir"):
            _err(errors, "backend.module.llvm_ir", "must be a non-empty LLVM IR string")
        elif LLVM_TRIPLE not in module.get("llvm_ir"):
            _err(errors, "backend.module.llvm_ir", f"must bind target triple {LLVM_TRIPLE!r}")
        _expect(module.get("input"), backend_input.get("artifact_id"), "backend.module.input", errors)
    module_hash = canonical_sha256(module)
    if backend.get("content_hash") != module_hash:
        _err(errors, "backend.content_hash", f"canonical mismatch; expected {module_hash}")
    _expect(backend.get("input_artifact_id"), backend_input.get("artifact_id"), "backend.input_artifact_id", errors)
    _expect(backend.get("llvm_version"), LLVM_VERSION, "backend.llvm_version", errors)
    _artifact_section(backend, "backend", kind=B3_KIND, inputs=[backend_input.get("artifact_id")], content_hash=module_hash, final=final, errors=errors)

    _expect(emission.get("kind"), EMISSION_KIND, "emission.kind", errors)
    _expect(emission.get("schema_version"), 1, "emission.schema_version", errors)
    _expect(emission.get("status"), ":accepted" if final else ":development-observed", "emission.status", errors)
    _expect(emission.get("profile"), PROFILE, "emission.profile", errors)
    _expect(emission.get("target"), TARGET, "emission.target", errors)
    _expect(emission.get("backend_artifact_id"), backend.get("artifact_id"), "emission.backend_artifact_id", errors)
    _blob(emission.get("object"), "emission.object", expected_elf_types={1}, errors=errors)
    _blob(emission.get("executable"), "emission.executable", expected_elf_types={2, 3}, errors=errors)
    executable = emission.get("executable")
    if isinstance(executable, Mapping):
        expected = artifact_id(EMISSION_KIND, [backend.get("artifact_id")], executable.get("content_sha256"))
        if emission.get("artifact_id") != expected:
            _err(errors, "emission.artifact_id", f"lineage/bytes mismatch; expected {expected}")


def _validate_transcripts_and_diagnostics(document: Mapping[str, Any], errors: list[str]) -> None:
    _section(document.get("transcripts"), "transcripts", errors)
    transcripts = document.get("transcripts")
    if isinstance(transcripts, Mapping):
        for group in ("positive", "negative"):
            values = transcripts.get(group)
            if not isinstance(values, list) or not values:
                _err(errors, f"transcripts.{group}", "must be a non-empty array")
                continue
            ids: list[str] = []
            for index, item in enumerate(values):
                path = f"transcripts.{group}[{index}]"
                if not _object(item, TRANSCRIPT_KEYS, path, errors):
                    continue
                _hash(item.get("content_sha256"), f"{path}.content_sha256", errors)
                _repository_file(
                    item.get("path"), f"{path}.path",
                    item.get("content_sha256"), errors,
                )
                if not isinstance(item.get("exit_code"), int) or isinstance(item.get("exit_code"), bool):
                    _err(errors, f"{path}.exit_code", "must be an integer")
                elif group == "positive" and item.get("exit_code") != 0:
                    _err(errors, f"{path}.exit_code", "positive transcript must exit zero")
                elif group == "negative" and item.get("exit_code") == 0:
                    _err(errors, f"{path}.exit_code", "negative transcript must exit nonzero")
                _strings(item.get("diagnostic_ids"), f"{path}.diagnostic_ids", errors, nonempty=(group == "negative"))
                claimed = item.get("id")
                payload = dict(item); payload.pop("id", None)
                expected = canonical_sha256(payload)
                if claimed != expected:
                    _err(errors, f"{path}.id", f"canonical mismatch; expected {expected}")
                ids.append(claimed)
            if len(ids) != len(set(ids)):
                _err(errors, f"transcripts.{group}", "transcript ids must be distinct")

    _section(document.get("diagnostics"), "diagnostics", errors)
    diagnostics = document.get("diagnostics")
    if isinstance(diagnostics, Mapping):
        _strings(diagnostics.get("accepted"), "diagnostics.accepted", errors, nonempty=True)
        _strings(diagnostics.get("rejected"), "diagnostics.rejected", errors, nonempty=True)
        negative = transcripts.get("negative", []) if isinstance(transcripts, Mapping) else []
        observed = sorted({d for item in negative if isinstance(item, Mapping) for d in item.get("diagnostic_ids", [])})
        if diagnostics.get("rejected") != observed:
            _err(errors, "diagnostics.rejected", "must equal diagnostic ids in negative transcripts")
        positive = transcripts.get("positive", []) if isinstance(transcripts, Mapping) else []
        accepted = sorted({d for item in positive if isinstance(item, Mapping) for d in item.get("diagnostic_ids", [])})
        if diagnostics.get("accepted") != accepted:
            _err(errors, "diagnostics.accepted", "must equal diagnostic ids in positive transcripts")


def _validate_provenance(document: Mapping[str, Any], errors: list[str]) -> None:
    _section(document.get("provenance"), "provenance", errors)
    provenance = document.get("provenance")
    if not isinstance(provenance, Mapping):
        return
    lineage = provenance.get("lineage")
    expected_lineage_keys = {"source_id", "semantic_id", "c13_artifact_id", "c14_artifact_id", "b1_artifact_id", "b3_artifact_id", "emission_artifact_id"}
    _object(lineage, expected_lineage_keys, "provenance.lineage", errors)
    carrier, lowering = document.get("carrier"), document.get("lowering")
    backend_input, backend, emission = document.get("backend_input"), document.get("backend"), document.get("emission")
    if not all(isinstance(item, Mapping) for item in (carrier, lowering, backend_input, backend, emission)):
        return
    expected_lineage = {
        "source_id": carrier.get("source_id"), "semantic_id": carrier.get("semantic_id"),
        "c13_artifact_id": carrier.get("artifact_id"), "c14_artifact_id": lowering.get("artifact_id"),
        "b1_artifact_id": backend_input.get("artifact_id"), "b3_artifact_id": backend.get("artifact_id"),
        "emission_artifact_id": emission.get("artifact_id"),
    }
    if lineage != expected_lineage:
        _err(errors, "provenance.lineage", "must exactly bind the C13 -> C14 -> B1 -> B3 -> ELF chain")
    for name, value in expected_lineage.items():
        if name.endswith("_id"):
            _hash(value, f"provenance.lineage.{name}", errors)
    chain_ids = [
        carrier.get("artifact_id"), lowering.get("artifact_id"),
        backend_input.get("artifact_id"), backend.get("artifact_id"),
        emission.get("artifact_id"),
    ]
    if len(chain_ids) != len(set(chain_ids)):
        _err(errors, "provenance.lineage", "pipeline artifact ids must be distinct")
    expected_edges = [
        (carrier.get("source_id"), carrier.get("artifact_id"), "source-to-c13"),
        (carrier.get("artifact_id"), lowering.get("artifact_id"), "c13-to-c14"),
        (lowering.get("artifact_id"), backend_input.get("artifact_id"), "c14-to-b1"),
        (backend_input.get("artifact_id"), backend.get("artifact_id"), "b1-to-b3"),
        (backend.get("artifact_id"), emission.get("artifact_id"), "b3-to-elf"),
    ]
    edges = provenance.get("edges")
    if not isinstance(edges, list) or len(edges) != len(expected_edges):
        _err(errors, "provenance.edges", "must contain the exact five-edge Linux lineage")
        return
    for index, (edge, expected) in enumerate(zip(edges, expected_edges)):
        path = f"provenance.edges[{index}]"
        if not _object(edge, EDGE_KEYS, path, errors):
            continue
        _hash(edge.get("from_id"), f"{path}.from_id", errors)
        _hash(edge.get("to_id"), f"{path}.to_id", errors)
        _expect(edge.get("target"), TARGET, f"{path}.target", errors)
        if (edge.get("from_id"), edge.get("to_id"), edge.get("kind")) != expected:
            _err(errors, path, f"must equal lineage edge {expected!r}")


def _validate_environment(value: Any, *, final: bool, errors: list[str]) -> None:
    keys = FINAL_ENV_KEYS if final else DEV_ENV_KEYS
    if not _object(value, keys, "environment", errors):
        return
    _expect(value.get("llvm_version"), LLVM_VERSION, "environment.llvm_version", errors)
    tools = value.get("tools")
    if _object(tools, TOOL_KEYS, "environment.tools", errors):
        for tool in LLVM_TOOLS:
            _hash(tools.get(tool), f"environment.tools.{tool}", errors)
    if final:
        _expect(value.get("execution"), ":native-linux", "environment.execution", errors)
        _expect(value.get("os"), "linux", "environment.os", errors)
        _expect(value.get("arch"), "x86_64", "environment.arch", errors)
        _expect(value.get("abi"), "SysV", "environment.abi", errors)
        _hash(value.get("kernel_id"), "environment.kernel_id", errors)
        _hash(value.get("machine_id"), "environment.machine_id", errors)
    else:
        _expect(value.get("execution"), ":development-emulated", "environment.execution", errors)
        _expect(value.get("image"), PINNED_IMAGE, "environment.image", errors)
        _expect(value.get("image_platform"), IMAGE_PLATFORM, "environment.image_platform", errors)
        _expect(value.get("pull_policy"), PULL_POLICY, "environment.pull_policy", errors)
        docker = value.get("docker")
        if _object(docker, DOCKER_KEYS, "environment.docker", errors):
            if not isinstance(docker.get("engine_version"), str) or not docker.get("engine_version"):
                _err(errors, "environment.docker.engine_version", "must be non-empty")
            _hash(docker.get("engine_id"), "environment.docker.engine_id", errors)
            _hash(docker.get("host_id"), "environment.docker.host_id", errors)
            if not isinstance(docker.get("host_os"), str) or not docker.get("host_os"):
                _err(errors, "environment.docker.host_os", "must be a non-empty recorded identity")
            if not isinstance(docker.get("host_arch"), str) or not docker.get("host_arch"):
                _err(errors, "environment.docker.host_arch", "must be a non-empty recorded identity")
        emulation = value.get("emulation")
        if _object(emulation, EMULATION_KEYS, "environment.emulation", errors):
            _expect(emulation.get("kind"), "qemu-binfmt", "environment.emulation.kind", errors)
            _hash(emulation.get("qemu_id"), "environment.emulation.qemu_id", errors)
            _hash(emulation.get("binfmt_id"), "environment.emulation.binfmt_id", errors)


def _validate_verification(document: Mapping[str, Any], *, final: bool, require_authoritative: bool, errors: list[str]) -> None:
    _section(document.get("verification"), "verification", errors)
    verification = document.get("verification")
    if not isinstance(verification, Mapping):
        return
    _expect(verification.get("predicate"), VERIFIER_PREDICATE, "verification.predicate", errors)
    _expect(verification.get("predicate_version"), VERIFIER_PREDICATE_VERSION, "verification.predicate_version", errors)
    _expect(verification.get("replay_contract_frozen"), False, "verification.replay_contract_frozen", errors)
    if final or require_authoritative:
        _err(errors, "verification.replay_contract_frozen", REPLAY_CONTRACT_UNFROZEN)
    if final:
        _err(errors, "verification.replay", FINAL_NON_JVM_REPLAY_MISSING)
    if final:
        _expect(verification.get("command"), None, "verification.command", errors)
    elif verification.get("command") != INTEGRATION_VERIFIER_COMMAND:
        _err(errors, "verification.command", f"must equal the exact integration-only SH17 argv {INTEGRATION_VERIFIER_COMMAND!r}")
    _expect(verification.get("status"), ":passed" if final else ":development-observed", "verification.status", errors)
    _strings(verification.get("diagnostic_ids"), "verification.diagnostic_ids", errors, nonempty=True)
    replay = verification.get("replay")
    if _object(replay, REPLAY_KEYS, "verification.replay", errors):
        _hash(replay.get("artifact_id"), "verification.replay.artifact_id", errors)
        _hash(replay.get("content_hash"), "verification.replay.content_hash", errors)
        _hash(replay.get("transcript_id"), "verification.replay.transcript_id", errors)
        _expect(replay.get("native"), final, "verification.replay.native", errors)
        transcripts = document.get("transcripts")
        positive = transcripts.get("positive", []) if isinstance(transcripts, Mapping) else []
        positive_ids = {item.get("id") for item in positive if isinstance(item, Mapping)}
        if replay.get("transcript_id") not in positive_ids:
            _err(errors, "verification.replay.transcript_id", "must reference a positive transcript")


def _validate_authority_review(document: Mapping[str, Any], *, final: bool, errors: list[str]) -> None:
    _section(document.get("authority"), "authority", errors)
    authority = document.get("authority")
    expected = {
        "development_only": not final, "emulated": not final,
        "authoritative": final, "native_replay": final,
        "public_route": False, "clojure_seed_boundary": True,
        "self_hosted": False, "release": False,
    }
    if isinstance(authority, Mapping):
        for key, value in expected.items():
            _expect(authority.get(key), value, f"authority.{key}", errors)
        inventory = authority.get("inventory")
        if _object(inventory, AUTHORITY_INVENTORY_KEYS, "authority.inventory", errors):
            _expect(inventory, AUTHORITY_INVENTORY, "authority.inventory", errors)

    _section(document.get("review"), "review", errors)
    review = document.get("review")
    if not isinstance(review, Mapping):
        return
    if final:
        _expect(review.get("status"), ":accepted", "review.status", errors)
        _expect(review.get("reviewer_class"), ":independent-sol", "review.reviewer_class", errors)
        _expect(review.get("independent"), True, "review.independent", errors)
        producer = document.get("producer")
        producer_commit = producer.get("commit") if isinstance(producer, Mapping) else None
        producer_tree = producer.get("tree") if isinstance(producer, Mapping) else None
        _expect(review.get("reviewed_commit"), producer_commit, "review.reviewed_commit", errors)
        _expect(review.get("reviewed_tree"), producer_tree, "review.reviewed_tree", errors)
        _expect(review.get("review_kind"), REVIEW_KIND, "review.review_kind", errors)
        _expect(review.get("review_schema"), REVIEW_SCHEMA, "review.review_schema", errors)
        review_path, review_hash = review.get("review_path"), review.get("review_content_sha256")
        _hash(review_hash, "review.review_content_sha256", errors)
        _repository_file(review_path, "review.review_path", review_hash, errors)
        expected_review = artifact_id(REVIEW_KIND, [review_path], review_hash)
        _expect(review.get("review_artifact_id"), expected_review, "review.review_artifact_id", errors)
        if _hash(review.get("review_artifact_id"), "review.review_artifact_id", errors):
            producer_artifacts = {
                document.get(name, {}).get("artifact_id")
                for name in ("carrier", "lowering", "backend_input", "backend", "emission")
                if isinstance(document.get(name), Mapping)
            }
            if review.get("review_artifact_id") in producer_artifacts:
                _err(errors, "review.review_artifact_id", "must be a distinct later review artifact, not a producer artifact")
    else:
        _expect(review.get("status"), ":pending", "review.status", errors)
        _expect(review.get("reviewer_class"), ":none", "review.reviewer_class", errors)
        _expect(review.get("independent"), False, "review.independent", errors)
        _expect(review.get("reviewed_commit"), None, "review.reviewed_commit", errors)
        _expect(review.get("reviewed_tree"), None, "review.reviewed_tree", errors)
        _expect(review.get("review_artifact_id"), None, "review.review_artifact_id", errors)
        _expect(review.get("review_path"), None, "review.review_path", errors)
        _expect(review.get("review_kind"), None, "review.review_kind", errors)
        _expect(review.get("review_schema"), None, "review.review_schema", errors)
        _expect(review.get("review_content_sha256"), None, "review.review_content_sha256", errors)


def _bindings(document: Mapping[str, Any]) -> dict[str, Any] | None:
    required = ("carrier", "backend", "emission", "diagnostics", "provenance")
    if not all(isinstance(document.get(name), Mapping) for name in required):
        return None
    carrier, backend, emission = document["carrier"], document["backend"], document["emission"]
    executable = emission.get("executable")
    if not isinstance(executable, Mapping):
        return None
    return {
        "carrier-artifact-id": backend.get("artifact_id"),
        "carrier-content-hash": backend.get("content_hash"),
        "carrier-schema": backend.get("schema_version"),
        "source-id": carrier.get("source_id"), "semantic-id": carrier.get("semantic_id"),
        "profile": carrier.get("profile"), "target": carrier.get("target"),
        "effects": carrier.get("effects"), "capabilities": carrier.get("capabilities"),
        "safety": carrier.get("safety"),
        "accepted-diagnostic-ids": document["diagnostics"].get("accepted"),
        "rejected-diagnostic-ids": document["diagnostics"].get("rejected"),
        "provenance-edges": {"artifact-kind": B3_KIND, "schema-version": B3_SCHEMA_VERSION},
    }


def _validate_handoff_provenance_binding(value: Any, path: str, errors: list[str]) -> None:
    """The frozen W4 field is a two-key producer descriptor, not edge data."""
    expected = {"artifact-kind": B3_KIND, "schema-version": B3_SCHEMA_VERSION}
    if _object(value, set(expected), path, errors):
        _expect(value, expected, path, errors)


def _validate_handoff(document: Mapping[str, Any], *, final: bool, errors: list[str]) -> None:
    claims = {"public-route?": False, "clojure-seed-boundary?": True, "self-hosted?": False, "release?": False}
    if not final:
        candidate = document.get("consumer_handoff_candidate")
        keys = {"contract", "contract-version", "workstream", "interface-kind", "interface-schema", "status", "bindings", "claims"}
        if not _object(candidate, keys, "consumer_handoff_candidate", errors):
            return
        _expect(candidate.get("contract"), CONSUMER_CONTRACT, "consumer_handoff_candidate.contract", errors)
        _expect(candidate.get("contract-version"), CONSUMER_CONTRACT_VERSION, "consumer_handoff_candidate.contract-version", errors)
        _expect(candidate.get("workstream"), ":w1", "consumer_handoff_candidate.workstream", errors)
        _expect(candidate.get("interface-kind"), INTERFACE_KIND, "consumer_handoff_candidate.interface-kind", errors)
        _expect(candidate.get("interface-schema"), INTERFACE_SCHEMA, "consumer_handoff_candidate.interface-schema", errors)
        _expect(candidate.get("status"), ":development-only", "consumer_handoff_candidate.status", errors)
        if _object(candidate.get("bindings"), HANDOFF_BINDING_KEYS, "consumer_handoff_candidate.bindings", errors):
            _validate_handoff_provenance_binding(candidate["bindings"].get("provenance-edges"), "consumer_handoff_candidate.bindings.provenance-edges", errors)
            if candidate.get("bindings") != _bindings(document):
                _err(errors, "consumer_handoff_candidate.bindings", "must exactly match recomputed W1 bindings")
        if _object(candidate.get("claims"), HANDOFF_CLAIM_KEYS, "consumer_handoff_candidate.claims", errors):
            _expect(candidate.get("claims"), claims, "consumer_handoff_candidate.claims", errors)
        return

    handoff = document.get("consumer_handoff")
    if not _object(handoff, HANDOFF_KEYS, "consumer_handoff", errors):
        return
    expected_simple = {
        "contract": CONSUMER_CONTRACT, "contract-version": CONSUMER_CONTRACT_VERSION,
        "workstream": ":w1", "interface-kind": INTERFACE_KIND,
        "interface-schema": INTERFACE_SCHEMA,
        "producer-commit": document.get("producer").get("commit") if isinstance(document.get("producer"), Mapping) else None,
        "producer-tree": document.get("producer").get("tree") if isinstance(document.get("producer"), Mapping) else None,
    }
    for key, value in expected_simple.items():
        _expect(handoff.get(key), value, f"consumer_handoff.{key}", errors)
    _hash(handoff.get("artifact-id"), "consumer_handoff.artifact-id", errors)
    _git(handoff.get("producer-commit"), "consumer_handoff.producer-commit", errors)
    _git(handoff.get("producer-tree"), "consumer_handoff.producer-tree", errors)
    verifier = handoff.get("verifier")
    if _object(verifier, HANDOFF_VERIFIER_KEYS, "consumer_handoff.verifier", errors):
        verification = document.get("verification")
        replay = verification.get("replay", {}) if isinstance(verification, Mapping) else {}
        expected = {"predicate": VERIFIER_PREDICATE, "predicate-version": 1, "replay-artifact-id": replay.get("artifact_id"), "replay-content-hash": replay.get("content_hash"), "status": ":passed"}
        _expect(verifier, expected, "consumer_handoff.verifier", errors)
    handoff_review = handoff.get("review")
    if _object(handoff_review, HANDOFF_REVIEW_KEYS, "consumer_handoff.review", errors):
        review = document.get("review")
        review = review if isinstance(review, Mapping) else {}
        expected = {"status": ":accepted", "reviewer-class": ":independent-sol", "reviewed-commit": review.get("reviewed_commit"), "review-artifact-id": review.get("review_artifact_id")}
        _expect(handoff_review, expected, "consumer_handoff.review", errors)
    if _object(handoff.get("bindings"), HANDOFF_BINDING_KEYS, "consumer_handoff.bindings", errors):
        _validate_handoff_provenance_binding(handoff["bindings"].get("provenance-edges"), "consumer_handoff.bindings.provenance-edges", errors)
        if handoff.get("bindings") != _bindings(document):
            _err(errors, "consumer_handoff.bindings", "must exactly match recomputed W1 bindings")
    if _object(handoff.get("claims"), HANDOFF_CLAIM_KEYS, "consumer_handoff.claims", errors):
        _expect(handoff.get("claims"), claims, "consumer_handoff.claims", errors)


def _validate_artifact_impl(document: Any, *, require_authoritative: bool = False) -> list[str]:
    errors: list[str] = []
    if not isinstance(document, Mapping):
        return ["document: must be a JSON object"]
    # A forged opt-in must fail before any source, fixture, transcript, or
    # review path is opened.  No replay owner contract is frozen yet.
    preflight_verification = document.get("verification")
    if isinstance(preflight_verification, Mapping) and preflight_verification.get("replay_contract_frozen") is True:
        return [f"verification.replay_contract_frozen: {REPLAY_CONTRACT_UNFROZEN}"]
    _scan_non_circular_keys(document, "document", errors)
    mode = document.get("mode")
    final = mode == ":authoritative-native"
    if mode not in {":development-emulated", ":authoritative-native"}:
        _err(errors, "mode", "must be ':development-emulated' or ':authoritative-native'")
    expected_keys = TOP_FINAL if final else TOP_DEVELOPMENT
    _object(document, expected_keys, "document", errors)
    _expect(document.get("artifact_kind"), FINAL_ARTIFACT_KIND if final else DEVELOPMENT_ARTIFACT_KIND, "artifact_kind", errors)
    _expect(document.get("document_schema"), FINAL_DOCUMENT_SCHEMA if final else DEVELOPMENT_DOCUMENT_SCHEMA, "document_schema", errors)
    _expect(document.get("schema_version"), 1, "schema_version", errors)
    _expect(document.get("interface_kind"), INTERFACE_KIND, "interface_kind", errors)
    _expect(document.get("interface_schema"), INTERFACE_SCHEMA, "interface_schema", errors)
    if require_authoritative and not final:
        _err(errors, "mode", "--require-authoritative rejects development/emulated evidence")

    evidence = document.get("evidence_identity")
    if _object(evidence, {"self_id", "canonicalization"}, "evidence_identity", errors):
        _expect(evidence.get("canonicalization"), CANONICALIZATION, "evidence_identity.canonicalization", errors)
        if _hash(evidence.get("self_id"), "evidence_identity.self_id", errors):
            expected = canonical_evidence_sha256(document)
            if evidence.get("self_id") != expected:
                _err(errors, "evidence_identity.self_id", f"canonical mismatch; expected {expected}")
    producer = document.get("producer")
    if _object(producer, {"repository", "commit", "tree"}, "producer", errors):
        _expect(producer.get("repository"), "gravity", "producer.repository", errors)
        _git(producer.get("commit"), "producer.commit", errors)
        _git(producer.get("tree"), "producer.tree", errors)
        if producer.get("commit") == producer.get("tree"):
            _err(errors, "producer", "reviewed A commit and tree must be distinct object identities")

    _validate_inputs(document, errors)
    _validate_pipeline(document, final=final, errors=errors)
    _validate_transcripts_and_diagnostics(document, errors)
    _validate_provenance(document, errors)
    _validate_verification(document, final=final, require_authoritative=require_authoritative, errors=errors)
    _validate_environment(document.get("environment"), final=final, errors=errors)
    _validate_authority_review(document, final=final, errors=errors)
    _validate_handoff(document, final=final, errors=errors)
    # Development evidence records the host/emulation identity separately, and
    # a negative transcript may name the Stage9 rejection it exercised.  Scan
    # only semantic/executable claims; environment identity and diagnostic
    # names are evidence about what was rejected, not cross-target lineage.
    for name in ("carrier", "lowering", "backend_input", "backend", "emission", "provenance"):
        _scan_forbidden(document.get(name), name, errors)
    verification = document.get("verification")
    if isinstance(verification, Mapping):
        _scan_forbidden(verification.get("command"), "verification.command", errors)
    handoff_name = "consumer_handoff" if final else "consumer_handoff_candidate"
    handoff = document.get(handoff_name)
    if isinstance(handoff, Mapping):
        _scan_forbidden(handoff.get("interface-kind"), f"{handoff_name}.interface-kind", errors)
        bindings = handoff.get("bindings")
        if isinstance(bindings, Mapping):
            # Rejected diagnostic ids may intentionally name Stage9.  The
            # executable target and edge list are the cross-target claims.
            _scan_forbidden(bindings.get("target"), f"{handoff_name}.bindings.target", errors)
            _scan_forbidden(bindings.get("provenance-edges"), f"{handoff_name}.bindings.provenance-edges", errors)

    if final and isinstance(document.get("consumer_handoff"), Mapping):
        evidence = document.get("evidence_identity")
        self_id = evidence.get("self_id") if isinstance(evidence, Mapping) else None
        if document["consumer_handoff"].get("artifact-id") != self_id:
            _err(errors, "consumer_handoff.artifact-id", "must equal evidence_identity.self_id")
    return errors


def validate_artifact(document: Any, *, require_authoritative: bool = False) -> list[str]:
    """Validate an artifact and convert malformed canonical values to errors."""
    try:
        return _validate_artifact_impl(document, require_authoritative=require_authoritative)
    except (TypeError, ValueError, OverflowError) as exc:
        return [f"document: malformed canonical value: {exc}"]


def validate_or_raise(document: Any, *, require_authoritative: bool = False) -> None:
    errors = validate_artifact(document, require_authoritative=require_authoritative)
    if errors:
        raise ValidationError(errors)


def _strict_object_pairs(pairs: list[tuple[str, Any]]) -> dict[str, Any]:
    """Reject duplicate JSON members before Python's dict semantics erase them."""
    result: dict[str, Any] = {}
    for key, value in pairs:
        if key in result:
            raise ValueError(f"duplicate JSON object key: {key!r}")
        result[key] = value
    return result


def main(argv: Sequence[str] | None = None) -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("path", nargs="?", help="JSON artifact; defaults to the final canonical path")
    parser.add_argument("--artifact", dest="artifact_option", help="explicit JSON artifact path")
    parser.add_argument("--require-authoritative", action="store_true", help="reject development/emulated evidence")
    args = parser.parse_args(argv)
    if args.path and args.artifact_option:
        parser.error("use either positional path or --artifact, not both")
    path = Path(args.artifact_option or args.path) if (args.artifact_option or args.path) else DEFAULT_ARTIFACT
    try:
        document = json.loads(path.read_text(encoding="utf-8"), object_pairs_hook=_strict_object_pairs)
    except (OSError, UnicodeError, json.JSONDecodeError, ValueError) as exc:
        print(f"validation failed: {path}: {exc}")
        return 1
    errors = validate_artifact(document, require_authoritative=args.require_authoritative)
    if errors:
        for error in errors:
            print(f"validation failed: {error}")
        return 1
    print("validation passed: w1 executable carrier interface")
    return 0


if __name__ == "__main__":
    sys.exit(main())
