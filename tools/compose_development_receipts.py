#!/usr/bin/env python3
"""Compose successful, non-authoritative development verification receipts.

Composition is deliberately read-only.  It validates already-recorded command
results against one manifest and reports coverage; it does not run checks and
does not promote development evidence into authority.
"""

from __future__ import annotations

import argparse
import hashlib
import json
import math
from pathlib import Path
from pathlib import PurePosixPath
import re
import shlex
import sys
from typing import Any, Iterable, Mapping, Sequence

from verify_development import (
    DEFAULT_MANIFEST,
    ROOT,
    ManifestError,
    VerificationError,
    check_semantic_declaration,
    checks_by_id,
    dependencies_of,
    input_identities,
    load_manifest,
    topological_order,
    validate_manifest,
)


SCHEMA_VERSION = 1
MAX_RECEIPTS = 256
MAX_RECEIPT_BYTES = 8 * 1024 * 1024
MAX_TOTAL_RECEIPT_BYTES = 64 * 1024 * 1024
_CACHE_KEY = re.compile(r"[0-9a-f]{64}\Z")


class CompositionError(Exception):
    """Raised when receipt evidence cannot be safely composed."""


def _canonical(value: Any) -> str:
    return json.dumps(value, ensure_ascii=True, sort_keys=True, separators=(",", ":"), allow_nan=False)


def _sha256(value: Any) -> str:
    return hashlib.sha256(_canonical(value).encode("utf-8")).hexdigest()


def _strict_object(pairs: list[tuple[str, Any]]) -> dict[str, Any]:
    result: dict[str, Any] = {}
    for key, value in pairs:
        if key in result:
            raise CompositionError(f"duplicate JSON key: {key!r}")
        result[key] = value
    return result


def _reject_constant(value: str) -> Any:
    raise CompositionError(f"invalid JSON constant: {value}")


def _loads_strict(text: str, label: str) -> Any:
    try:
        return json.loads(text, object_pairs_hook=_strict_object, parse_constant=_reject_constant)
    except CompositionError:
        raise
    except json.JSONDecodeError as exc:
        raise CompositionError(f"cannot parse receipt {label}: {exc}") from exc


def load_receipt(path: Path | str) -> dict[str, Any]:
    """Read one bounded, strict JSON receipt."""

    receipt_path = Path(path)
    try:
        size = receipt_path.stat().st_size
        if size > MAX_RECEIPT_BYTES:
            raise CompositionError(f"receipt exceeds {MAX_RECEIPT_BYTES} bytes: {receipt_path}")
        value = _loads_strict(receipt_path.read_text(encoding="utf-8"), str(receipt_path))
    except CompositionError:
        raise
    except (OSError, UnicodeDecodeError) as exc:
        raise CompositionError(f"cannot read receipt {receipt_path}: {exc}") from exc
    if not isinstance(value, dict):
        raise CompositionError(f"receipt must be a JSON object: {receipt_path}")
    return value


def _manifest_command(check: Mapping[str, Any]) -> list[str]:
    command = check["command"]
    return shlex.split(command, posix=True) if isinstance(command, str) else list(command)


def _normalise_path(value: str) -> str:
    value = value.replace("\\", "/")
    while value.startswith("./"):
        value = value[2:]
    return value


def _binding(value: Any, label: str) -> dict[str, Any]:
    if not isinstance(value, dict) or set(value) != {"present", "sha256"}:
        raise CompositionError(f"{label} must contain exactly present and sha256")
    if value["present"] is not True or not isinstance(value["sha256"], str) or _CACHE_KEY.fullmatch(value["sha256"]) is None:
        raise CompositionError(f"{label} must be a present lowercase SHA-256 binding")
    return value


def _binding_map(value: Any, label: str) -> dict[str, Any]:
    if not isinstance(value, dict) or not all(isinstance(name, str) for name in value):
        raise CompositionError(f"{label} must be a string-keyed binding map")
    for name, binding in value.items():
        _binding(binding, f"{label}[{name!r}]")
    return value


def _validate_command_identity(check: Mapping[str, Any], value: Any, root: str) -> dict[str, Any]:
    label = f"check {check['id']!r} command_identity"
    if not isinstance(value, dict) or set(value) != {"root", "argv", "executable", "runtime", "cwd", "env"}:
        raise CompositionError(f"{label} has invalid keys")
    if value["root"] != root:
        raise CompositionError(f"{label} root does not match the receipt root")
    argv = value["argv"]
    if not isinstance(argv, list) or not argv or not all(isinstance(item, str) for item in argv):
        raise CompositionError(f"{label} argv must be a nonempty string list")
    runtime = value["runtime"]
    runtime_keys = {
        "python", "python_version", "platform", "python_executable",
        "python_executable_sha256", "environment", "supervision_environment",
    }
    if not isinstance(runtime, dict) or set(runtime) != runtime_keys:
        raise CompositionError(f"{label} runtime has invalid keys")
    for field in ("python", "python_version", "platform", "python_executable"):
        if not isinstance(runtime[field], str) or not runtime[field]:
            raise CompositionError(f"{label} runtime {field} must be a nonempty string")
    python_hash = runtime["python_executable_sha256"]
    if python_hash is not None and (not isinstance(python_hash, str) or _CACHE_KEY.fullmatch(python_hash) is None):
        raise CompositionError(f"{label} runtime python_executable_sha256 is invalid")
    environment = _binding_map(runtime["environment"], f"{label} runtime environment")
    supervision = _binding_map(runtime["supervision_environment"], f"{label} runtime supervision_environment")
    if set(supervision) != {"_GRAVITY_VERIFIER_RUN"}:
        raise CompositionError(f"{label} runtime supervision_environment has invalid keys")
    manifest_env = _binding_map(value["env"], f"{label} env")
    expected_env = {
        str(name): {"present": True, "sha256": hashlib.sha256(str(raw).encode("utf-8")).hexdigest()}
        for name, raw in dict(check.get("env", {})).items()
    }
    if manifest_env != dict(sorted(expected_env.items())):
        raise CompositionError(f"{label} env does not match the manifest environment")
    if any(environment.get(name) != binding for name, binding in manifest_env.items()):
        raise CompositionError(f"{label} manifest env is inconsistent with runtime environment")
    declared_argv = _manifest_command(check)
    if len(argv) != len(declared_argv):
        raise CompositionError(f"{label} argv does not match the manifest command")
    for declared_token, actual_token in zip(declared_argv, argv):
        rooted_token = declared_token.replace("{root}", root)
        if "{python}" not in rooted_token:
            matches = actual_token == rooted_token
        else:
            prefix, suffix = rooted_token.split("{python}", 1)
            matches = actual_token.startswith(prefix) and actual_token.endswith(suffix)
            replacement = actual_token[len(prefix):len(actual_token) - len(suffix) if suffix else None]
            matches = matches and bool(replacement) and rooted_token.replace("{python}", replacement) == actual_token
        if not matches:
            raise CompositionError(f"{label} argv does not match the manifest command")
    if value["cwd"] != _normalise_path(str(check.get("cwd", "."))):
        raise CompositionError(f"{label} cwd does not match the manifest")
    executable = value["executable"]
    if not isinstance(executable, dict) or executable.get("requested") != argv[0]:
        raise CompositionError(f"{label} executable requested value is invalid")
    if executable.get("missing") is True:
        if set(executable) != {"requested", "resolved", "sha256", "missing"} or executable["resolved"] is not None or executable["sha256"] is not None:
            raise CompositionError(f"{label} missing executable shape is invalid")
    else:
        if set(executable) != {"requested", "resolved", "sha256", "size"}:
            raise CompositionError(f"{label} executable shape is invalid")
        if not isinstance(executable["resolved"], str) or not executable["resolved"]:
            raise CompositionError(f"{label} executable resolved path is invalid")
        if not isinstance(executable["sha256"], str) or _CACHE_KEY.fullmatch(executable["sha256"]) is None:
            raise CompositionError(f"{label} executable sha256 is invalid")
        if type(executable["size"]) is not int or executable["size"] < 0:
            raise CompositionError(f"{label} executable size is invalid")
    return value


def _validate_inputs(check: Mapping[str, Any], value: Any, root: Path, expected: dict[str, dict[str, Any]]) -> dict[str, Any]:
    label = f"check {check['id']!r} inputs"
    if not isinstance(value, dict) or set(value) != {"declared", "files", "sha256"}:
        raise CompositionError(f"{label} has invalid keys")
    declared = [_normalise_path(item) for item in list(check.get("inputs", [])) + list(check.get("tool_inputs", []))]
    if value["declared"] != declared:
        raise CompositionError(f"{label} declared paths do not match the manifest")
    files = value["files"]
    if not isinstance(files, list):
        raise CompositionError(f"{label} files must be a list")
    paths: list[str] = []
    for record in files:
        if not isinstance(record, dict) or "exists" not in record:
            raise CompositionError(f"{label} contains a malformed file record")
        path = record.get("path")
        if (
            not isinstance(path, str) or not path or path != _normalise_path(path)
            or path.startswith("/") or path.endswith("/") or "//" in path or "\\" in path
            or any(part in {".", ".."} for part in path.split("/"))
        ):
            raise CompositionError(f"{label} file path is invalid")
        parsed = PurePosixPath(path)
        if parsed.is_absolute() or ".." in parsed.parts:
            raise CompositionError(f"{label} file path escapes the repository")
        paths.append(path)
        if record["exists"] is True:
            if set(record) != {"path", "exists", "sha256", "size"}:
                raise CompositionError(f"{label} existing file shape is invalid")
            if not isinstance(record["sha256"], str) or _CACHE_KEY.fullmatch(record["sha256"]) is None:
                raise CompositionError(f"{label} file sha256 is invalid")
            if type(record["size"]) is not int or record["size"] < 0:
                raise CompositionError(f"{label} file size is invalid")
        elif record["exists"] is False:
            if set(record) != {"path", "exists", "sha256"} or record["sha256"] is not None:
                raise CompositionError(f"{label} missing file shape is invalid")
        else:
            raise CompositionError(f"{label} file exists must be boolean")
    if paths != sorted(set(paths)):
        raise CompositionError(f"{label} files must be sorted and unique")
    expected_digest = _sha256(files)
    if value["sha256"] != expected_digest:
        raise CompositionError(f"{label} sha256 does not match its file records")
    check_id = str(check["id"])
    if check_id not in expected:
        try:
            expected[check_id] = input_identities(check, root)
        except VerificationError as exc:
            raise CompositionError(f"cannot validate current inputs for check {check_id!r}: {exc}") from exc
    if value != expected[check_id]:
        raise CompositionError(f"{label} does not match current declared inputs")
    return value


def _declaration(check: Mapping[str, Any], manifest: Mapping[str, Any]) -> dict[str, Any]:
    semantic = check_semantic_declaration(check, manifest)
    return {
        "id": check["id"],
        "lane": check["lane"],
        "command": _manifest_command(check),
        "depends_on": dependencies_of(check),
        "lock": check.get("lock"),
        "exclusive": bool(check.get("exclusive", False)),
        "cost": check.get("cost", "cheap"),
        "fresh": bool(check.get("fresh", False)),
        "timeout_seconds": semantic["timeout_seconds"],
        "resource": semantic["resource"],
    }


def _record_declaration(record: Mapping[str, Any]) -> dict[str, Any]:
    if "fresh" not in record or type(record["fresh"]) is not bool:
        raise CompositionError("check record fresh declaration metadata must be boolean")
    if "timeout_seconds" not in record:
        raise CompositionError("check record is missing timeout_seconds declaration metadata")
    timeout = record["timeout_seconds"]
    if timeout is not None and (
        type(timeout) is not float or not math.isfinite(timeout) or timeout <= 0
    ):
        raise CompositionError(
            "check record timeout_seconds declaration metadata must be null or a finite positive float"
        )
    return {
        key: record.get(key)
        for key in (
            "id", "lane", "command", "depends_on", "lock", "exclusive", "cost", "fresh", "timeout_seconds", "resource"
        )
    }


def _immutable(record: Mapping[str, Any]) -> dict[str, Any]:
    return {
        "declaration": _record_declaration(record),
        "command_identity": record.get("command_identity"),
        "inputs": record.get("inputs"),
    }


def _check_identity(
    manifest: Mapping[str, Any], check: Mapping[str, Any], record: Mapping[str, Any]
) -> dict[str, Any]:
    """Reconstruct the verifier cache identity without reading current inputs."""

    return {
        **check_semantic_declaration(check, manifest),
        "command": record.get("command_identity"),
        "inputs": record.get("inputs"),
    }


def _cache_key(manifest: Mapping[str, Any], identity: Mapping[str, Any]) -> str:
    payload = {"schema_version": manifest["schema_version"], "name": manifest.get("name"), "check": identity}
    return _sha256(payload)


def _manifest_identity(manifest: Mapping[str, Any], manifest_path: Path | None, root: Path) -> dict[str, Any] | None:
    if manifest_path is None:
        return None
    resolved = manifest_path.resolve()
    try:
        displayed = resolved.relative_to(root).as_posix()
    except ValueError:
        displayed = resolved.as_posix()
    return {"path": displayed, "sha256": _sha256(manifest)}


def _validate_manifest_identity(identity: Any, label: str) -> dict[str, Any]:
    if not isinstance(identity, dict) or set(identity) != {"path", "sha256"}:
        raise CompositionError(f"{label} manifest identity must contain exactly path and sha256")
    if identity["path"] is not None and not isinstance(identity["path"], str):
        raise CompositionError(f"{label} manifest path must be a string or null")
    digest = identity["sha256"]
    if not isinstance(digest, str) or _CACHE_KEY.fullmatch(digest) is None:
        raise CompositionError(f"{label} manifest sha256 must be 64 lowercase hexadecimal characters")
    return identity


def _validate_result_record(record: Mapping[str, Any], check: Mapping[str, Any]) -> str:
    check_id = str(check["id"])
    status = record.get("status")
    if status not in {"passed", "reused"}:
        raise CompositionError(f"check {check_id!r} status must be passed or reused")
    wanted_authority = "fresh-command-pass-non-authoritative" if status == "passed" else "non-authoritative"
    if record.get("authority") != wanted_authority:
        raise CompositionError(f"check {check_id!r} has invalid authority for status {status}")
    if status == "reused" and (check.get("fresh", False) or check.get("authority", "none") == "declared"):
        raise CompositionError(f"check {check_id!r} cannot be reused under its manifest declaration")
    if type(record.get("returncode")) is not int or record["returncode"] != 0:
        raise CompositionError(f"check {check_id!r} returncode must be the integer 0")
    for field in ("stdout", "stderr"):
        if not isinstance(record.get(field), str):
            raise CompositionError(f"check {check_id!r} {field} must be a string")
    for field in ("started_at", "finished_at"):
        if not isinstance(record.get(field), str) or not record[field]:
            raise CompositionError(f"check {check_id!r} {field} must be a nonempty string")
    duration = record.get("duration_ms")
    if isinstance(duration, bool) or not isinstance(duration, (int, float)) or not math.isfinite(duration) or duration < 0:
        raise CompositionError(f"check {check_id!r} duration_ms must be finite and nonnegative")
    return status


_OBSERVATION_KEYS = {
    "source", "sample_count", "sample_interval_seconds", "peak_rss_bytes",
    "peak_process_count", "telemetry_available", "telemetry_error",
    "declared_reserved_rss_bytes", "declared_reserved_processes",
    "rss_exceeded", "processes_exceeded", "authoritative",
}


def _validate_resource_observation(
    record: Mapping[str, Any], resource: Mapping[str, Any], status: str
) -> dict[str, Any]:
    check_id = str(record.get("id"))
    value = record.get("resource_observation")
    if not isinstance(value, dict) or set(value) != _OBSERVATION_KEYS:
        raise CompositionError(f"check {check_id!r} resource_observation has invalid keys")
    if value["authoritative"] is not False:
        raise CompositionError(f"check {check_id!r} resource_observation must be non-authoritative")
    expected_rss = int(resource["reserved_rss_mb"]) * 1024 * 1024
    expected_processes = int(resource["reserved_processes"])
    declared_rss = value["declared_reserved_rss_bytes"]
    declared_processes = value["declared_reserved_processes"]
    if (
        type(declared_rss) is not int or declared_rss <= 0
        or type(declared_processes) is not int or declared_processes <= 0
        or declared_rss != expected_rss or declared_processes != expected_processes
    ):
        raise CompositionError(f"check {check_id!r} resource_observation reservation does not match the manifest")
    sample_count = value["sample_count"]
    if type(sample_count) is not int or sample_count < 0:
        raise CompositionError(f"check {check_id!r} resource_observation sample_count is invalid")
    available = value["telemetry_available"]
    if type(available) is not bool:
        raise CompositionError(f"check {check_id!r} resource_observation telemetry_available is invalid")
    if status == "reused":
        if value["source"] != "not-executed" or value["sample_interval_seconds"] is not None:
            raise CompositionError(f"check {check_id!r} reused resource_observation must be not-executed")
        expected = {
            "source": "not-executed", "sample_count": 0,
            "sample_interval_seconds": None, "peak_rss_bytes": None,
            "peak_process_count": None, "telemetry_available": False,
            "telemetry_error": None, "declared_reserved_rss_bytes": expected_rss,
            "declared_reserved_processes": expected_processes, "rss_exceeded": None,
            "processes_exceeded": None, "authoritative": False,
        }
        if value != expected:
            raise CompositionError(f"check {check_id!r} reused resource_observation must be not-executed")
        return value
    if value["source"] != "process-tree-sampling" or value["sample_interval_seconds"] != 0.25:
        raise CompositionError(f"check {check_id!r} passed resource_observation must be a process-tree 0.25s sample")
    if available:
        peaks = (value["peak_rss_bytes"], value["peak_process_count"])
        if (
            sample_count < 1
            or type(peaks[0]) is not int or peaks[0] <= 0
            or type(peaks[1]) is not int or peaks[1] < 1
        ):
            raise CompositionError(f"check {check_id!r} available resource_observation has invalid measurements")
        if value["telemetry_error"] is not None:
            raise CompositionError(f"check {check_id!r} available resource_observation cannot have an error")
        expected_exceeded = (peaks[0] > expected_rss, peaks[1] > expected_processes)
        if (
            type(value["rss_exceeded"]) is not bool
            or type(value["processes_exceeded"]) is not bool
            or (value["rss_exceeded"], value["processes_exceeded"]) != expected_exceeded
        ):
            raise CompositionError(f"check {check_id!r} resource_observation exceeded flags are invalid")
        if any(expected_exceeded):
            raise CompositionError(f"check {check_id!r} passed despite a resource reservation exceedance")
    else:
        if sample_count != 0 or value["peak_rss_bytes"] is not None or value["peak_process_count"] is not None:
            raise CompositionError(f"check {check_id!r} unavailable resource_observation invents measurements")
        if not isinstance(value["telemetry_error"], str) or not value["telemetry_error"]:
            raise CompositionError(f"check {check_id!r} unavailable resource_observation requires an error")
        if value["rss_exceeded"] is not None or value["processes_exceeded"] is not None:
            raise CompositionError(f"check {check_id!r} unavailable resource_observation invents exceeded flags")
    return value


def _merge_resource_observations(
    first: Mapping[str, Any], second: Mapping[str, Any]
) -> dict[str, Any]:
    """Conservatively compose duplicate observations without hiding maxima."""

    if first["source"] == "not-executed":
        return dict(second)
    if second["source"] == "not-executed":
        return dict(first)
    reserved_rss = first["declared_reserved_rss_bytes"]
    reserved_processes = first["declared_reserved_processes"]
    available = [item for item in (first, second) if item["telemetry_available"]]
    had_unavailable = any(
        not item["telemetry_available"] or item["telemetry_error"] is not None
        for item in (first, second)
    )
    if available:
        peak_rss = max(int(item["peak_rss_bytes"]) for item in available)
        peak_processes = max(int(item["peak_process_count"]) for item in available)
        error = (
            "one or more composed observations reported unavailable telemetry"
            if had_unavailable else None
        )
        rss_exceeded: bool | None = peak_rss > reserved_rss
        processes_exceeded: bool | None = peak_processes > reserved_processes
    else:
        peak_rss = None
        peak_processes = None
        error = "all composed observations reported unavailable telemetry"
        rss_exceeded = None
        processes_exceeded = None
    return {
        "source": "composed-process-tree-maxima",
        # Duplicate receipts can repeat one execution, so summing would
        # overstate evidence. The maximum is deterministic and conservative.
        "sample_count": max(int(first["sample_count"]), int(second["sample_count"])),
        "sample_interval_seconds": 0.25,
        "peak_rss_bytes": peak_rss,
        "peak_process_count": peak_processes,
        "telemetry_available": bool(available),
        "telemetry_error": error,
        "declared_reserved_rss_bytes": reserved_rss,
        "declared_reserved_processes": reserved_processes,
        "rss_exceeded": rss_exceeded,
        "processes_exceeded": processes_exceeded,
        "authoritative": False,
    }


def compose_receipts(
    manifest: Mapping[str, Any] | Path | str,
    receipts: Iterable[Mapping[str, Any]],
    *,
    expected_ids: Iterable[str] | None = None,
    root: Path | str = ROOT,
) -> dict[str, Any]:
    """Validate and deterministically compose receipt objects."""

    manifest_path: Path | None = None
    if isinstance(manifest, (str, Path)):
        manifest_path = Path(manifest)
        manifest_value = load_manifest(manifest_path)
    else:
        manifest_value = dict(manifest)
        validate_manifest(manifest_value)
    root_path = Path(root).resolve()
    by_id = checks_by_id(manifest_value)
    all_order = topological_order(manifest_value)
    expected = set(all_order if expected_ids is None else expected_ids)
    unknown_expected = expected - set(by_id)
    if unknown_expected:
        raise CompositionError(f"unknown expected checks: {sorted(unknown_expected)}")

    receipt_values = list(receipts)
    if not receipt_values:
        raise CompositionError("at least one receipt is required")
    if len(receipt_values) > MAX_RECEIPTS:
        raise CompositionError(f"receipt count exceeds {MAX_RECEIPTS}")

    required_manifest = _manifest_identity(manifest_value, manifest_path, root_path)
    observed_manifest: dict[str, Any] | None = None
    records_by_id: dict[str, dict[str, Any]] = {}
    immutable_by_id: dict[str, dict[str, Any]] = {}
    immutable_by_key: dict[str, dict[str, Any]] = {}
    id_by_key: dict[str, str] = {}
    expected_inputs: dict[str, dict[str, Any]] = {}
    input_check_count = 0

    for receipt_index, receipt in enumerate(receipt_values):
        label = f"receipt {receipt_index + 1}"
        if not isinstance(receipt, Mapping):
            raise CompositionError(f"{label} must be an object")
        if receipt.get("kind") != "development-verification-receipt" or receipt.get("schema_version") != SCHEMA_VERSION:
            raise CompositionError(f"{label} is not a schema-v1 development verification receipt")
        if receipt.get("authoritative") is not False:
            raise CompositionError(f"{label} must have authoritative=false")
        if receipt.get("status") != "passed":
            raise CompositionError(f"{label} status must be passed")
        if receipt.get("root") != str(root_path):
            raise CompositionError(f"{label} root does not match {root_path}")
        identity = _validate_manifest_identity(receipt.get("manifest"), label)
        if observed_manifest is None:
            observed_manifest = identity
        elif identity != observed_manifest:
            raise CompositionError(f"{label} manifest identity conflicts with prior receipts")
        if required_manifest is not None and identity != required_manifest:
            raise CompositionError(f"{label} manifest identity does not match the supplied manifest")
        if required_manifest is None and identity.get("sha256") != _sha256(manifest_value):
            raise CompositionError(f"{label} manifest digest does not match the supplied manifest")
        checks = receipt.get("checks")
        if not isinstance(checks, list):
            raise CompositionError(f"{label} checks must be a list")
        preliminary_ids: set[str] = set()
        for record in checks:
            if isinstance(record, Mapping) and isinstance(record.get("id"), str):
                if record["id"] in preliminary_ids:
                    raise CompositionError(f"{label} contains duplicate check id {record['id']!r}")
                preliminary_ids.add(record["id"])
        if len(checks) > len(by_id):
            raise CompositionError(f"{label} check count exceeds the manifest check count")
        input_check_count += len(checks)
        receipt_check_ids: set[str] = set()
        for record in checks:
            if not isinstance(record, Mapping):
                raise CompositionError(f"{label} contains a non-object check record")
            check_id = record.get("id")
            if not isinstance(check_id, str) or check_id not in by_id:
                raise CompositionError(f"{label} contains unknown check {check_id!r}")
            if check_id in receipt_check_ids:
                raise CompositionError(f"{label} contains duplicate check id {check_id!r}")
            receipt_check_ids.add(check_id)
            if _record_declaration(record) != _declaration(by_id[check_id], manifest_value):
                raise CompositionError(f"check {check_id!r} declaration does not match the manifest")
            result_status = _validate_result_record(record, by_id[check_id])
            observation = _validate_resource_observation(
                record, check_semantic_declaration(by_id[check_id], manifest_value)["resource"], result_status
            )
            key = record.get("cache_key")
            if not isinstance(key, str) or _CACHE_KEY.fullmatch(key) is None:
                raise CompositionError(f"check {check_id!r} has an invalid cache_key")
            _validate_command_identity(by_id[check_id], record.get("command_identity"), str(root_path))
            _validate_inputs(by_id[check_id], record.get("inputs"), root_path, expected_inputs)
            immutable = _immutable(record)
            semantic_identity = _check_identity(manifest_value, by_id[check_id], record)
            unbound_identity = json.loads(_canonical(semantic_identity))
            unbound_identity["command"]["runtime"].pop("supervision_environment")
            marker = hashlib.sha256(("gravity-supervision:" + _canonical(unbound_identity)).encode("utf-8")).hexdigest()[:32]
            expected_supervision = {
                "_GRAVITY_VERIFIER_RUN": {"present": True, "sha256": hashlib.sha256(marker.encode("utf-8")).hexdigest()}
            }
            if semantic_identity["command"]["runtime"]["supervision_environment"] != expected_supervision:
                raise CompositionError(f"check {check_id!r} supervision binding does not match its semantic identity")
            if key != _cache_key(manifest_value, semantic_identity):
                raise CompositionError(f"check {check_id!r} cache_key does not match its semantic identity")
            if check_id in immutable_by_id:
                prior = records_by_id[check_id]
                if prior["cache_key"] != key or immutable_by_id[check_id] != immutable:
                    raise CompositionError(f"check {check_id!r} has conflicting semantic identity or cache_key")
            if key in immutable_by_key and (immutable_by_key[key] != immutable or id_by_key[key] != check_id):
                raise CompositionError(f"cache_key {key!r} has conflicting semantic identity")
            immutable_by_id[check_id] = immutable
            immutable_by_key[key] = immutable
            id_by_key[key] = check_id
            if check_id in records_by_id:
                prior_observation = records_by_id[check_id]["resource_observation"]
                observation = _merge_resource_observations(prior_observation, observation)
            records_by_id[check_id] = {
                **immutable["declaration"],
                "cache_key": key,
                "semantic_identity_sha256": _sha256(semantic_identity),
                "status": "satisfied",
                "authority": "non-authoritative",
                "resource_observation": observation,
            }

    present = set(records_by_id)
    for check_id in present:
        missing_dependencies = set(dependencies_of(by_id[check_id])) - present
        if missing_dependencies:
            raise CompositionError(f"check {check_id!r} is missing union dependencies: {sorted(missing_dependencies)}")
    present_order = topological_order(manifest_value, present) if present else []
    expected_order = [check_id for check_id in all_order if check_id in expected]
    missing_order = [check_id for check_id in expected_order if check_id not in present]
    observations = [records_by_id[check_id]["resource_observation"] for check_id in present_order]
    measured = [item for item in observations if item["telemetry_available"]]
    aggregate: dict[str, Any] = {
        "schema_version": SCHEMA_VERSION,
        "kind": "development-verification-composition",
        "manifest": observed_manifest,
        "root": str(root_path),
        "authoritative": False,
        "status": "complete" if not missing_order else "incomplete",
        "checks": [records_by_id[check_id] for check_id in present_order],
        "coverage": {
            "expected": expected_order,
            "present": present_order,
            "missing": missing_order,
            "receipt_count": len(receipt_values),
            "check_count": len(present_order),
            "input_check_count": input_check_count,
        },
        "resource_summary": {
            "authoritative": False,
            "check_count": len(observations),
            "executed_count": sum(item["source"] != "not-executed" for item in observations),
            "not_executed_count": sum(item["source"] == "not-executed" for item in observations),
            "composed_count": sum(item["source"] == "composed-process-tree-maxima" for item in observations),
            "composed_with_unavailable_count": sum(
                item["source"] == "composed-process-tree-maxima"
                and item["telemetry_error"] is not None
                for item in observations
            ),
            "telemetry_available_count": len(measured),
            "telemetry_unavailable_count": sum(
                item["source"] != "not-executed" and not item["telemetry_available"]
                for item in observations
            ),
            "total_sample_count": sum(item["sample_count"] for item in observations),
            "peak_rss_bytes": max((item["peak_rss_bytes"] for item in measured), default=None),
            "peak_process_count": max((item["peak_process_count"] for item in measured), default=None),
            "rss_exceeded_checks": sorted(
                records_by_id[check_id]["id"] for check_id in present_order
                if records_by_id[check_id]["resource_observation"]["rss_exceeded"] is True
            ),
            "processes_exceeded_checks": sorted(
                records_by_id[check_id]["id"] for check_id in present_order
                if records_by_id[check_id]["resource_observation"]["processes_exceeded"] is True
            ),
        },
    }
    aggregate["composition_sha256"] = _sha256(aggregate)
    return aggregate


def build_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(description="Compose non-authoritative development verification receipts.")
    parser.add_argument("receipts", nargs="+", type=Path, help="schema-v1 receipt JSON paths")
    parser.add_argument("--manifest", type=Path, default=DEFAULT_MANIFEST)
    parser.add_argument("--root", type=Path, default=ROOT)
    parser.add_argument("--expected-check", action="append", dest="expected_ids", help="expected check id; repeatable")
    return parser


def main(argv: Sequence[str] | None = None) -> int:
    args = build_parser().parse_args(argv)
    try:
        if len(args.receipts) > MAX_RECEIPTS:
            raise CompositionError(f"receipt count exceeds {MAX_RECEIPTS}")
        sizes = [(path, path.stat().st_size) for path in args.receipts]
        oversized = [path for path, size in sizes if size > MAX_RECEIPT_BYTES]
        if oversized:
            raise CompositionError(f"receipt exceeds {MAX_RECEIPT_BYTES} bytes: {oversized[0]}")
        total_size = sum(size for _path, size in sizes)
        if total_size > MAX_TOTAL_RECEIPT_BYTES:
            raise CompositionError(f"total receipt bytes exceed {MAX_TOTAL_RECEIPT_BYTES}")
        receipts = [load_receipt(path) for path in args.receipts]
        result = compose_receipts(args.manifest, receipts, expected_ids=args.expected_ids, root=args.root)
    except (CompositionError, ManifestError, OSError, ValueError) as exc:
        print(f"composition invalid: {exc}", file=sys.stderr)
        return 2
    print(_canonical(result))
    return 0 if result["status"] == "complete" else 1


if __name__ == "__main__":
    raise SystemExit(main())
