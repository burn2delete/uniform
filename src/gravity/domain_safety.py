"""SAFE7, SAFE8, SAFE9, and SAFE11 domain safety validation."""

from __future__ import annotations

import hashlib
import json
from pathlib import Path
from typing import Any


class DomainSafetyError(Exception):
    def __init__(
        self,
        code: str,
        message: str,
        *,
        operation: dict[str, Any] | None = None,
        source: str,
        missing_fact: str,
        remediation: str,
    ) -> None:
        super().__init__(message)
        self.code = code
        self.message = message
        self.operation = operation or {}
        self.source = source
        self.missing_fact = missing_fact
        self.remediation = remediation

    def to_diagnostic(self) -> dict[str, Any]:
        return {
            "id": self.code,
            "message": self.message,
            "operation_id": self.operation.get("id"),
            "domain": self.operation.get("domain"),
            "span": self.operation.get("source_span", {"source": self.source}),
            "active_profile": self.operation.get("active_profile"),
            "target": self.operation.get("target"),
            "missing_fact": self.missing_fact,
            "effect_context": self.operation.get("effect_context", {}),
            "capability_context": self.operation.get("capability_context", {}),
            "remediation": self.remediation,
            "analyzer_stage": "domain-safety",
        }


def validate_domain_safety_file(path: Path) -> dict[str, Any]:
    return validate_domain_safety_manifest(load_manifest_file(path), str(path))


def domain_safety_diagnostic(path: Path) -> dict[str, Any] | None:
    try:
        validate_domain_safety_file(path)
    except DomainSafetyError as exc:
        return exc.to_diagnostic()
    return None


def validate_domain_safety_manifest(manifest: dict[str, Any], source: str) -> dict[str, Any]:
    if manifest.get("kind") != "domain-safety-input":
        raise DomainSafetyError(
            "SAFE7-DECLARATION",
            "domain safety input has the wrong artifact kind",
            source=source,
            missing_fact="domain-safety-input",
            remediation="Feed P02-T04 a domain-safety-input artifact.",
        )
    operations = manifest.get("operations", [])
    for operation in operations:
        validate_operation(operation, source)

    return {
        "kind": "domain-safety-artifact",
        "documents": ["SAFE7", "SAFE8", "SAFE9", "SAFE11"],
        "package": manifest.get("package"),
        "module": manifest.get("module"),
        "profile": manifest.get("profile"),
        "target": manifest.get("target"),
        "input_hash": artifact_hash(manifest),
        "foreign_declaration_records": collect(operations, "foreign_declaration_records"),
        "abi_protocol_records": collect(operations, "abi_protocol_records"),
        "type_mapping_records": collect(operations, "type_mapping_records"),
        "ffi_ownership_lifetime_maps": collect(operations, "ffi_ownership_lifetime_maps"),
        "safe_wrapper_audit_records": collect(operations, "safe_wrapper_audit_records"),
        "error_translation_maps": collect(operations, "error_translation_maps"),
        "callback_safety_records": collect(operations, "callback_safety_records"),
        "generated_binding_provenance": collect(operations, "generated_binding_provenance"),
        "ffi_conformance_reports": collect(operations, "ffi_conformance_reports"),
        "concurrency_graph": collect(operations, "concurrency_graph"),
        "task_capture_records": collect(operations, "task_capture_records"),
        "ownership_transfer_records": collect(operations, "ownership_transfer_records"),
        "shared_state_access_records": collect(operations, "shared_state_access_records"),
        "synchronization_proof_records": collect(operations, "synchronization_proof_records"),
        "atomic_memory_order_records": collect(operations, "atomic_memory_order_records"),
        "blocking_cancellation_records": collect(operations, "blocking_cancellation_records"),
        "backend_memory_order_preservation_records": collect(operations, "backend_memory_order_preservation_records"),
        "numeric_mode_records": collect(operations, "numeric_mode_records"),
        "numeric_check_records": collect(operations, "numeric_check_records"),
        "range_interval_proof_records": collect(operations, "range_interval_proof_records"),
        "floating_point_mode_records": collect(operations, "floating_point_mode_records"),
        "elementary_function_approximation_records": collect(operations, "elementary_function_approximation_records"),
        "relaxed_numeric_mode_approvals": collect(operations, "relaxed_numeric_mode_approvals"),
        "numeric_optimization_proof_records": collect(operations, "numeric_optimization_proof_records"),
        "backend_numeric_lowering_records": collect(operations, "backend_numeric_lowering_records"),
        "taint_source_records": collect(operations, "taint_source_records"),
        "taint_flow_records": collect(operations, "taint_flow_records"),
        "validator_sanitizer_contracts": collect(operations, "validator_sanitizer_contracts"),
        "residual_constraint_records": collect(operations, "residual_constraint_records"),
        "sink_authorization_records": collect(operations, "sink_authorization_records"),
        "secret_redaction_records": collect(operations, "secret_redaction_records"),
        "generated_code_taint_propagation_records": collect(operations, "generated_code_taint_propagation_records"),
        "taint_conformance_reports": collect(operations, "taint_conformance_reports"),
        "diagnostics": [],
    }


def validate_operation(operation: dict[str, Any], source: str) -> None:
    domain = operation.get("domain")
    if domain == "ffi":
        validate_ffi(operation, source)
    elif domain == "concurrency":
        validate_concurrency(operation, source)
    elif domain == "numeric":
        validate_numeric(operation, source)
    elif domain == "taint":
        validate_taint(operation, source)
    else:
        raise DomainSafetyError(
            "SAFE7-DECLARATION",
            "domain safety operation lacks a recognized domain",
            operation=operation,
            source=source,
            missing_fact="domain",
            remediation="Use ffi, concurrency, numeric, or taint domain records.",
        )


def validate_ffi(operation: dict[str, Any], source: str) -> None:
    ffi = operation.get("ffi", {})
    required = {
        "boundary_id",
        "foreign_source",
        "provider_id",
        "abi_or_protocol",
        "type_mapping",
        "ownership",
        "lifetime",
        "error_translation",
        "effects",
        "capabilities",
        "supported_profiles",
        "wrapper",
        "audit_record",
    }
    missing = sorted(key for key in required if key not in ffi or ffi.get(key) in (None, [], {}))
    if missing:
        raise_error("SAFE7-DECLARATION", operation, source, ",".join(missing), "Complete the foreign declaration metadata.")
    if ffi.get("raw_call_in_safe"):
        raise_error("SAFE7-RAW-CALL", operation, source, "safe-wrapper", "Call a safe wrapper rather than raw foreign code.")
    if not ffi["type_mapping"].get("complete") or ffi["type_mapping"].get("unsafe"):
        raise_error("SAFE7-TYPE-MAP", operation, source, "safe-type-mapping", "Record complete ABI-safe type mapping.")
    if not ffi["ownership"].get("complete") or not ffi["ownership"].get("allocator_identity"):
        raise_error("SAFE7-OWNERSHIP", operation, source, "ownership-and-allocator", "Declare ownership transfer and allocator identity.")
    if not ffi["lifetime"].get("valid"):
        raise_error("SAFE7-LIFETIME", operation, source, "foreign-lifetime", "Bound the foreign value lifetime or reject the boundary.")
    if not ffi["error_translation"].get("complete"):
        raise_error("SAFE7-ERROR-MAP", operation, source, "error-translation", "Translate foreign failure into declared Gravity behavior.")
    if ffi.get("callback", {}).get("valid") is False:
        raise_error("SAFE7-CALLBACK", operation, source, "callback-contract", "Declare callback capture, lifetime, threading, reentrancy, and release behavior.")
    if any(capability not in operation.get("capability_context", {}).get("declared", []) for capability in ffi["capabilities"]):
        raise_error("SAFE7-CAPABILITY", operation, source, "ffi-capability", "Grant the FFI capability through provider policy.")
    if operation.get("active_profile") not in ffi["supported_profiles"]:
        raise_error("SAFE7-HOST-PROFILE", operation, source, "supported-profile", "Use the interop boundary only in profiles it declares.")
    if ffi.get("generated") and not (ffi.get("generated_provenance") and ffi.get("audit_record")):
        raise_error("SAFE7-GENERATED", operation, source, "generated-binding-provenance", "Preserve binding generator provenance and unsafe audit metadata.")


def validate_concurrency(operation: dict[str, Any], source: str) -> None:
    c = operation.get("concurrency", {})
    checks = [
        ("data_race", "SAFE8-DATA-RACE", "synchronization"),
        ("invalid_task_capture", "SAFE8-TASK-CAPTURE", "bounded-task-capture"),
        ("use_after_move", "SAFE8-MOVE", "ownership-transfer-consumption"),
        ("unsafe_share", "SAFE8-SHARE", "concurrency-safe-representation"),
        ("lock_guard_invalid", "SAFE8-LOCK-GUARD", "lock-guard-lifetime"),
        ("atomic_order_invalid", "SAFE8-ATOMIC-ORDER", "atomic-memory-order"),
        ("fence_missing", "SAFE8-FENCE", "target-fence"),
        ("channel_invalid", "SAFE8-CHANNEL", "channel-contract"),
        ("actor_invalid", "SAFE8-ACTOR", "actor-message-contract"),
        ("workflow_replay_missing", "SAFE8-WORKFLOW-REPLAY", "workflow-replay-record"),
        ("backend_preservation_invalid", "SAFE8-BACKEND", "backend-sync-preservation"),
    ]
    for key, code, fact in checks:
        if c.get(key):
            raise_error(code, operation, source, fact, "Add proof, synchronization, replay, provider, or backend preservation evidence.")


def validate_numeric(operation: dict[str, Any], source: str) -> None:
    n = operation.get("numeric", {})
    checks = [
        ("overflow_unhandled", "SAFE9-OVERFLOW", "overflow-mode-or-proof"),
        ("div_zero_possible", "SAFE9-DIV-ZERO", "nonzero-divisor"),
        ("shift_invalid", "SAFE9-SHIFT", "valid-shift-count"),
        ("narrow_unchecked", "SAFE9-NARROW", "checked-narrowing"),
        ("float_mode_missing", "SAFE9-FLOAT-MODE", "floating-mode"),
        ("float_input_unhandled", "SAFE9-FLOAT-INPUT", "nan-infinity-policy"),
        ("elementary_domain_invalid", "SAFE9-ELEMENTARY-DOMAIN", "elementary-domain-proof"),
        ("approx_missing", "SAFE9-APPROX", "approximation-evidence"),
        ("relaxed_without_opt_in", "SAFE9-RELAXED", "source-relaxed-opt-in"),
        ("optimization_without_proof", "SAFE9-OPTIMIZATION", "mode-preserving-proof"),
        ("backend_invalid", "SAFE9-BACKEND", "backend-numeric-preservation"),
    ]
    for key, code, fact in checks:
        if n.get(key):
            raise_error(code, operation, source, fact, "Select an explicit mode, emit a runtime check, attach proof, or reject the numeric operation.")


def validate_taint(operation: dict[str, Any], source: str) -> None:
    t = operation.get("taint", {})
    checks = [
        ("tainted_sink", "SAFE11-TAINTED-SINK", "sink-authorization"),
        ("validator_invalid", "SAFE11-VALIDATOR", "validator-contract"),
        ("residual_invalid", "SAFE11-RESIDUAL", "accepted-residual-constraints"),
        ("parameterization_missing", "SAFE11-PARAMETERIZATION", "structured-parameterization"),
        ("deserialization_unvalidated", "SAFE11-DESERIALIZATION", "schema-validation"),
        ("secret_leak", "SAFE11-SECRET-LEAK", "secret-redaction"),
        ("prompt_injection", "SAFE11-PROMPT-INJECTION", "prompt-tool-policy"),
        ("generated_drops_taint", "SAFE11-GENERATED", "generated-taint-provenance"),
        ("foreign_erases_taint", "SAFE11-FOREIGN", "foreign-taint-metadata"),
        ("unsafe_clear_without_audit", "SAFE11-UNSAFE-CLEAR", "unsafe-taint-audit"),
    ]
    for key, code, fact in checks:
        if t.get(key):
            raise_error(code, operation, source, fact, "Preserve taint facts, validate for the sink, redact secrets, or require unsafe audit.")


def raise_error(code: str, operation: dict[str, Any], source: str, missing_fact: str, remediation: str) -> None:
    raise DomainSafetyError(
        code,
        f"{operation.get('domain')} safety operation violates {code}",
        operation=operation,
        source=source,
        missing_fact=missing_fact,
        remediation=remediation,
    )


def collect(operations: list[dict[str, Any]], key: str) -> list[dict[str, Any]]:
    records = []
    for operation in operations:
        for record in operation.get(key, []):
            item = dict(record)
            item.setdefault("operation_id", operation.get("id"))
            item.setdefault("source_span", operation.get("source_span"))
            records.append(item)
    return records


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
