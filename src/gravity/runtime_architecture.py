"""Phase 08 runtime architecture artifact validation."""

from __future__ import annotations

import hashlib
import json
from pathlib import Path
from typing import Any


REQUIRED_FAMILIES = {
    "no_runtime",
    "minimal_native",
    "managed",
    "memory",
    "concurrency",
    "distributed",
    "ai",
    "repl",
    "ffi",
    "capability",
    "observability",
}


class RuntimeArchitectureError(Exception):
    def __init__(
        self,
        code: str,
        message: str,
        *,
        record: dict[str, Any] | None = None,
        source: str,
        missing_fact: str,
        remediation: str,
    ) -> None:
        super().__init__(message)
        self.code = code
        self.message = message
        self.record = record or {}
        self.source = source
        self.missing_fact = missing_fact
        self.remediation = remediation

    def to_diagnostic(self) -> dict[str, Any]:
        return {
            "id": self.code,
            "message": self.message,
            "runtime_family": self.record.get("family"),
            "service": self.record.get("service"),
            "profile": self.record.get("profile"),
            "target": self.record.get("target"),
            "artifact_id": self.record.get("artifact_id") or self.record.get("id"),
            "source_span": self.record.get("source_span", {"source": self.source}),
            "missing_fact": self.missing_fact,
            "remediation": self.remediation,
            "analyzer_stage": "phase08-runtime-architecture-validation",
        }


def validate_runtime_file(path: Path) -> dict[str, Any]:
    return validate_runtime_manifest(load_manifest_file(path), str(path))


def runtime_diagnostic(path: Path) -> dict[str, Any] | None:
    try:
        validate_runtime_file(path)
    except RuntimeArchitectureError as exc:
        return exc.to_diagnostic()
    return None


def validate_runtime_manifest(manifest: dict[str, Any], source: str) -> dict[str, Any]:
    if manifest.get("kind") != "runtime-architecture-input":
        raise_error("R1-MANIFEST", "runtime architecture input has wrong kind", {}, source, "runtime-architecture-input")

    runtime_manifest = require_dict(manifest, "runtime_manifest", "R1-MANIFEST", source)
    validate_shared_runtime(runtime_manifest, source)
    family_selection = require_dict(manifest, "runtime_family_selection", "R1-SELECTION", source)
    validate_family_selection(family_selection, source)
    service_table = require_dict(manifest, "runtime_service_table", "R1-SERVICE", source)
    validate_service_table(service_table, source)
    families = require_dict(manifest, "runtime_families", "R1-MANIFEST", source)
    validate_runtime_families(families, source)
    conformance = require_dict(manifest, "runtime_conformance_report", "R1-MANIFEST", source)
    validate_conformance(conformance, source)

    return {
        "kind": "runtime-architecture-artifact",
        "phase": "08",
        "package": manifest.get("package"),
        "module": manifest.get("module"),
        "runtime_manifest": runtime_manifest,
        "runtime_family_selection": family_selection,
        "runtime_service_table": service_table,
        "no_runtime_manifest": families["no_runtime"],
        "minimal_native_runtime": families["minimal_native"],
        "managed_runtime": families["managed"],
        "memory_runtime": families["memory"],
        "concurrency_runtime": families["concurrency"],
        "distributed_runtime": families["distributed"],
        "ai_runtime": families["ai"],
        "repl_runtime": families["repl"],
        "ffi_runtime": families["ffi"],
        "capability_enforcement_report": families["capability"],
        "observability_event_schema": families["observability"],
        "runtime_conformance_report": conformance,
        "input_hash": artifact_hash(manifest),
        "diagnostics": [],
    }


def validate_shared_runtime(record: dict[str, Any], source: str) -> None:
    if not record.get("family") or not record.get("profile") or not record.get("target"):
        raise_error("R1-SELECTION", "runtime manifest lacks family, profile, or target", record, source, "runtime-family-profile-target")
    if record.get("hidden_forbidden_services"):
        raise_error("R1-FORBIDDEN", "runtime manifest hides forbidden service dependency", record, source, "forbidden-service-report")
    if record.get("capability_checks") is not True:
        raise_error("R1-CAPABILITY", "runtime manifest lacks capability enforcement", record, source, "capability-enforcement-table")
    if not record.get("diagnostics") or not record.get("startup_failure_model"):
        raise_error("R1-MANIFEST", "runtime manifest lacks diagnostics or startup/failure model", record, source, "diagnostics-startup-failure")


def validate_family_selection(record: dict[str, Any], source: str) -> None:
    selected = set(record.get("families", []))
    missing = sorted(REQUIRED_FAMILIES - selected)
    if missing:
        raise_error("R1-SELECTION", "runtime family selection is incomplete", record, source, ",".join(missing))
    if record.get("profile_incompatible_family"):
        raise_error("R1-SELECTION", "runtime family is incompatible with profile", record, source, "profile-family-compatibility")


def validate_service_table(record: dict[str, Any], source: str) -> None:
    for bucket in ["linked", "generated", "delegated", "external", "forbidden"]:
        if bucket not in record:
            raise_error("R1-SERVICE", "runtime service table lacks classification bucket", record, source, bucket)
    if record.get("delegated_without_adapter"):
        raise_error("R1-HOST", "delegated runtime service lacks typed adapter", record, source, "typed-host-adapter")
    if record.get("nondeterminism_without_replay"):
        raise_error("R1-REPLAY", "runtime nondeterminism lacks replay record", record, source, "replay-record")


def validate_runtime_families(record: dict[str, Any], source: str) -> None:
    missing = sorted(REQUIRED_FAMILIES - set(record))
    if missing:
        raise_error("R1-MANIFEST", "runtime family registry is incomplete", record, source, ",".join(missing))
    validate_no_runtime(record["no_runtime"], source)
    validate_minimal_native(record["minimal_native"], source)
    validate_managed(record["managed"], source)
    validate_memory(record["memory"], source)
    validate_concurrency(record["concurrency"], source)
    validate_distributed(record["distributed"], source)
    validate_ai(record["ai"], source)
    validate_repl(record["repl"], source)
    validate_ffi(record["ffi"], source)
    validate_capability(record["capability"], source)
    validate_observability(record["observability"], source)


def validate_no_runtime(record: dict[str, Any], source: str) -> None:
    if record.get("runtime") != ":none":
        raise_error("R2-MANIFEST", "no-runtime artifact does not declare runtime none", record, source, "runtime-none")
    if record.get("hidden_service"):
        raise_error("R2-HIDDEN-SERVICE", "no-runtime artifact depends on forbidden runtime service", record, source, "forbidden-service")
    if not record.get("startup") or not record.get("memory_map") or not record.get("failure_policy"):
        raise_error("R2-STARTUP", "no-runtime artifact lacks startup, memory, or failure records", record, source, "startup-memory-failure")
    if record.get("heap_without_provider"):
        raise_error("R2-MEMORY", "no-runtime heap use lacks static or target provider", record, source, "static-or-target-allocator")


def validate_minimal_native(record: dict[str, Any], source: str) -> None:
    if not record.get("linked_services") or not record.get("panic_policy") or not record.get("allocator_provider"):
        raise_error("R3-SERVICE", "minimal native runtime lacks service, panic, or allocator records", record, source, "native-service-records")
    if record.get("helper_effect_without_capability"):
        raise_error("R3-CAPABILITY", "native runtime helper performs undeclared authority-bearing effect", record, source, "helper-capability")
    if record.get("allocator_policy_violation"):
        raise_error("R3-ALLOCATOR", "native allocator violates memory policy", record, source, "allocator-policy")
    if record.get("debug_in_release"):
        raise_error("R3-DEBUG", "debug-only service leaked into release artifact", record, source, "release-debug-policy")


def validate_managed(record: dict[str, Any], source: str) -> None:
    if not record.get("host") or not record.get("adapters"):
        raise_error("R4-HOST", "managed runtime lacks host target or adapters", record, source, "host-adapter-record")
    if record.get("unchecked_null"):
        raise_error("R4-NULL", "managed host null or undefined value is unchecked", record, source, "null-translation-map")
    if record.get("untranslated_exception"):
        raise_error("R4-EXCEPTION", "managed host exception is untranslated", record, source, "exception-map")
    if record.get("gc_only_cleanup"):
        raise_error("R4-RESOURCE", "managed runtime relies on GC for linear cleanup", record, source, "deterministic-cleanup")


def validate_memory(record: dict[str, Any], source: str) -> None:
    if not record.get("provider_manifests"):
        raise_error("R5-PROVIDER", "memory runtime lacks provider manifests", record, source, "memory-provider-manifest")
    if record.get("allocation_in_no_alloc_region"):
        raise_error("R5-ALLOC", "memory runtime allocates in no-allocation region", record, source, "no-allocation-policy")
    if record.get("region_escape"):
        raise_error("R5-LIFETIME", "region or arena value escapes valid lifetime", record, source, "region-lifetime")
    if record.get("proofless_check_elision"):
        raise_error("R5-PROOF", "runtime memory check was elided without proof", record, source, "check-elision-proof")


def validate_concurrency(record: dict[str, Any], source: str) -> None:
    if not record.get("scheduler") or not record.get("task_tree"):
        raise_error("R6-SCHEDULER", "concurrency runtime lacks scheduler or task tree", record, source, "scheduler-task-tree")
    if record.get("unsynchronized_shared_state"):
        raise_error("R6-RACE", "shared mutable state lacks synchronization or transfer evidence", record, source, "sync-or-transfer-proof")
    if record.get("orphan_task"):
        raise_error("R6-TASK", "task lacks parent scope or lifecycle owner", record, source, "task-lifecycle-owner")
    if record.get("unsupported_atomic"):
        raise_error("R6-ATOMIC", "atomic memory order or scope unsupported", record, source, "atomic-support")


def validate_distributed(record: dict[str, Any], source: str) -> None:
    if not record.get("topology") or not record.get("schemas"):
        raise_error("R7-SCHEMA", "distributed runtime lacks topology or schemas", record, source, "topology-schemas")
    if record.get("unrecorded_nondeterminism"):
        raise_error("R7-REPLAY", "distributed replay has unrecorded nondeterminism", record, source, "event-log-replay")
    if record.get("side_effect_without_idempotency"):
        raise_error("R7-IDEMPOTENCY", "distributed side effect lacks idempotency", record, source, "idempotency-record")
    if record.get("unsafe_log_upgrade"):
        raise_error("R7-MIGRATION", "distributed event-log or schema upgrade lacks migration evidence", record, source, "migration-policy")


def validate_ai(record: dict[str, Any], source: str) -> None:
    if not record.get("model_call_ledger") or not record.get("policy_graph"):
        raise_error("R8-MODEL", "AI runtime lacks model ledger or policy graph", record, source, "model-ledger-policy")
    if record.get("tool_policy_gap"):
        raise_error("R8-TOOL", "AI tool call lacks schema, capability, human-review, timeout, or retry policy", record, source, "tool-policy")
    if record.get("tainted_output_to_sink"):
        raise_error("R8-TAINT", "AI output reaches trusted sink without validation", record, source, "validated-output")
    if record.get("live_call_in_replay"):
        raise_error("R8-REPLAY", "AI runtime issues live call in replay-required segment", record, source, "recorded-model-tool-output")


def validate_repl(record: dict[str, Any], source: str) -> None:
    if not record.get("session_manifest") or not record.get("transcript"):
        raise_error("R9-SESSION", "REPL runtime lacks session manifest or transcript", record, source, "session-transcript")
    if record.get("bypasses_compiler_checks"):
        raise_error("R9-CHECKS", "interactive evaluation bypasses compiler checks", record, source, "normal-compiler-pipeline")
    if record.get("forbidden_profile"):
        raise_error("R9-PROFILE", "interactive evaluation used in forbidden profile", record, source, "interactive-profile-eligibility")
    if record.get("unhermetic_state"):
        raise_error("R9-HERMETICITY", "build-affecting session state is untracked", record, source, "session-artifact")


def validate_ffi(record: dict[str, Any], source: str) -> None:
    if not record.get("binding_manifest") or not record.get("symbol_resolution"):
        raise_error("R10-BINDING", "FFI runtime lacks binding or symbol resolution records", record, source, "ffi-binding-symbol")
    if record.get("pointer_without_lifetime"):
        raise_error("R10-POINTER", "foreign pointer or handle lacks lifetime and ownership policy", record, source, "pointer-lifetime-ownership")
    if record.get("foreign_effect_without_capability"):
        raise_error("R10-CAPABILITY", "foreign effect lacks runtime authority", record, source, "foreign-capability")
    if record.get("callback_adapter_gap"):
        raise_error("R10-CALLBACK", "foreign callback lacks scheduler, taint, error, or capability adapter", record, source, "callback-adapter")


def validate_capability(record: dict[str, Any], source: str) -> None:
    if not record.get("capability_table") or not record.get("decision_log"):
        raise_error("R11-MANIFEST", "runtime capability enforcement lacks table or decision log", record, source, "capability-table-decision-log")
    if record.get("ambient_authority"):
        raise_error("R11-AMBIENT", "runtime action uses ambient authority", record, source, "scoped-capability-handle")
    if record.get("missing_grant"):
        raise_error("R11-GRANT", "runtime action lacks matching capability grant", record, source, "capability-grant")
    if record.get("tool_contract_violation"):
        raise_error("R11-TOOL", "runtime tool or plugin exceeds contract", record, source, "tool-contract")


def validate_observability(record: dict[str, Any], source: str) -> None:
    if not record.get("event_schemas") or not record.get("diagnostic_bundle"):
        raise_error("R12-SCHEMA", "observability runtime lacks event schemas or diagnostic bundle", record, source, "event-schema-diagnostic-bundle")
    if record.get("secret_leak"):
        raise_error("R12-SECRET", "runtime observability leaks secret or sensitive data", record, source, "redaction-policy")
    if record.get("sink_without_capability"):
        raise_error("R12-SINK", "observability sink lacks capability grant", record, source, "sink-capability")
    if record.get("semantic_change"):
        raise_error("R12-SEMANTICS", "observability changes ordering, replay, safety, or behavior", record, source, "semantic-neutrality")


def validate_conformance(record: dict[str, Any], source: str) -> None:
    covered = set(record.get("families", []))
    missing = sorted(REQUIRED_FAMILIES - covered)
    if missing:
        raise_error("R1-MANIFEST", "runtime conformance lacks required family coverage", record, source, ",".join(missing))
    if record.get("missing_replay_fixture"):
        raise_error("R7-REPLAY", "runtime conformance lacks required replay fixture", record, source, "replay-fixture")
    if record.get("missing_observability_fixture"):
        raise_error("R12-BUNDLE", "runtime conformance lacks observability bundle fixture", record, source, "observability-fixture")


def require_dict(manifest: dict[str, Any], key: str, code: str, source: str) -> dict[str, Any]:
    value = manifest.get(key)
    if not isinstance(value, dict) or not value:
        raise_error(code, f"manifest lacks required object {key}", {}, source, key)
    return value


def raise_error(code: str, message: str, record: dict[str, Any], source: str, missing_fact: str) -> None:
    raise RuntimeArchitectureError(
        code,
        message,
        record=record,
        source=source,
        missing_fact=missing_fact,
        remediation="Update the runtime manifest, service table, family record, capability decision, observability schema, or evidence fixture.",
    )


def load_manifest_file(path: Path) -> dict[str, Any]:
    manifest = json.loads(path.read_text(encoding="utf-8"))
    if "extends" not in manifest:
        return manifest
    base = load_manifest_file(path.parent / manifest["extends"])
    result = json.loads(json.dumps(base))
    for pointer in manifest.get("delete", []):
        delete_pointer(result, pointer)
    for pointer, value in manifest.get("set", {}).items():
        set_pointer(result, pointer, value)
    return result


def set_pointer(value: Any, pointer: str, replacement: Any) -> None:
    parent, key = pointer_parent(value, pointer)
    if isinstance(parent, list):
        parent[int(key)] = replacement
    else:
        parent[key] = replacement


def delete_pointer(value: Any, pointer: str) -> None:
    parent, key = pointer_parent(value, pointer)
    if isinstance(parent, list):
        del parent[int(key)]
    else:
        del parent[key]


def pointer_parent(value: Any, pointer: str) -> tuple[Any, str]:
    parts = pointer_parts(pointer)
    if not parts:
        raise ValueError("cannot address manifest root")
    current = value
    for part in parts[:-1]:
        current = current[int(part)] if isinstance(current, list) else current[part]
    return current, parts[-1]


def pointer_parts(pointer: str) -> list[str]:
    if not pointer.startswith("/"):
        raise ValueError(f"invalid JSON pointer: {pointer}")
    return [part.replace("~1", "/").replace("~0", "~") for part in pointer.split("/")[1:]]


def artifact_hash(value: Any) -> str:
    data = json.dumps(value, sort_keys=True, separators=(",", ":"))
    return "sha256:" + hashlib.sha256(data.encode("utf-8")).hexdigest()
