"""Phase 07 backend architecture artifact validation."""

from __future__ import annotations

import hashlib
import json
from pathlib import Path
from typing import Any


REQUIRED_BACKENDS = {
    "c",
    "llvm",
    "wasm",
    "jvm",
    "js_ts",
    "mlir",
    "gpu",
    "hdl",
    "workflow_graph",
    "query_relational",
    "mobile",
}
COMMON_ARTIFACT_FIELDS = {
    "schema_version",
    "kind",
    "backend",
    "profile",
    "target",
    "content_hash",
    "source_provenance",
    "compiler_provenance",
    "dependencies",
    "effects",
    "capabilities",
    "safety",
    "proofs",
}


class BackendArchitectureError(Exception):
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
            "backend": self.record.get("backend"),
            "target": self.record.get("target") or self.record.get("target_id"),
            "profile": self.record.get("profile"),
            "artifact_id": self.record.get("artifact_id") or self.record.get("id"),
            "source_span": self.record.get("source_span", {"source": self.source}),
            "missing_fact": self.missing_fact,
            "remediation": self.remediation,
            "analyzer_stage": "phase07-backend-architecture-validation",
        }


def validate_backend_file(path: Path) -> dict[str, Any]:
    return validate_backend_manifest(load_manifest_file(path), str(path))


def backend_diagnostic(path: Path) -> dict[str, Any] | None:
    try:
        validate_backend_file(path)
    except BackendArchitectureError as exc:
        return exc.to_diagnostic()
    return None


def validate_backend_manifest(manifest: dict[str, Any], source: str) -> dict[str, Any]:
    if manifest.get("kind") != "backend-architecture-input":
        raise_error("B1-INPUT", "backend architecture input has wrong kind", {}, source, "backend-architecture-input")

    backend_interface = require_dict(manifest, "backend_interface", "B1-INPUT", source)
    validate_backend_interface(backend_interface, source)
    target_lowering = require_dict(manifest, "target_lowering_manifest", "B1-TARGET", source)
    validate_target_lowering(target_lowering, source)
    concrete = require_dict(manifest, "concrete_backends", "B1-UNSUPPORTED", source)
    validate_concrete_backends(concrete, source)
    artifacts = require_list(manifest, "backend_artifact_records", "B13-SCHEMA", source)
    validate_artifact_records(artifacts, source)
    abi_layout = require_dict(manifest, "abi_layout_report", "B1-ABI", source)
    validate_abi_layout(abi_layout, source)
    artifact_emission = require_dict(manifest, "artifact_emission", "B13-SCHEMA", source)
    validate_artifact_emission(artifact_emission, source)
    conformance = require_dict(manifest, "backend_conformance_report", "B14-COVERAGE", source)
    validate_conformance(conformance, source)

    return {
        "kind": "backend-architecture-artifact",
        "phase": "07",
        "package": manifest.get("package"),
        "module": manifest.get("module"),
        "backend_interface": backend_interface,
        "target_lowering_manifest": target_lowering,
        "concrete_backend_manifests": concrete,
        "backend_artifact_records": artifacts,
        "abi_layout_report": abi_layout,
        "artifact_emission": artifact_emission,
        "backend_conformance_report": conformance,
        "input_hash": artifact_hash(manifest),
        "diagnostics": [],
    }


def validate_backend_interface(record: dict[str, Any], source: str) -> None:
    if record.get("input_verified") is not True:
        raise_error("B1-INPUT", "backend accepted unverified or incomplete input", record, source, "verified-mir-or-domain-ir")
    required = {"profile", "target", "abi", "runtime", "effects", "capabilities", "safety", "proofs", "source_map"}
    if not required.issubset(set(record.get("required_manifests", []))):
        raise_error("B1-INPUT", "backend input packet lacks required manifests", record, source, ",".join(sorted(required)))
    if record.get("proofless_target_metadata"):
        raise_error("B1-PROOF", "target metadata lacks Gravity proof evidence", record, source, "proof-to-target-metadata")
    if not record.get("artifact_manifest") or not record.get("source_debug_map") or not record.get("conformance_record"):
        raise_error("B1-METADATA", "backend interface lacks emission metadata", record, source, "artifact-source-conformance-metadata")


def validate_target_lowering(record: dict[str, Any], source: str) -> None:
    targets = set(record.get("eligible_targets", []))
    if not REQUIRED_BACKENDS.issubset(targets):
        raise_error("B1-TARGET", "target lowering matrix does not cover all Phase 7 backends", record, source, "phase07-target-matrix")
    for item in record.get("lowering_requests", []):
        if item.get("input_verified") is not True:
            raise_error("B1-INPUT", "target lowering request has unverified input", item, source, "verified-input")
        if item.get("missing_capability"):
            raise_error("B1-CAPABILITY", "target lowering request lacks capability evidence", item, source, item["missing_capability"])


def validate_concrete_backends(record: dict[str, Any], source: str) -> None:
    missing = sorted(REQUIRED_BACKENDS - set(record))
    if missing:
        raise_error("B1-UNSUPPORTED", "concrete backend registry is incomplete", record, source, ",".join(missing))
    validate_c_backend(record["c"], source)
    validate_llvm_backend(record["llvm"], source)
    validate_wasm_backend(record["wasm"], source)
    validate_jvm_backend(record["jvm"], source)
    validate_js_ts_backend(record["js_ts"], source)
    validate_mlir_backend(record["mlir"], source)
    validate_gpu_backend(record["gpu"], source)
    validate_hdl_backend(record["hdl"], source)
    validate_workflow_backend(record["workflow_graph"], source)
    validate_query_backend(record["query_relational"], source)
    validate_mobile_backend(record["mobile"], source)


def validate_c_backend(record: dict[str, Any], source: str) -> None:
    if not record.get("dialect"):
        raise_error("B2-DIALECT", "C backend lacks a declared dialect", record, source, "c-dialect")
    if record.get("relies_on_undefined_behavior"):
        raise_error("B2-UB", "C lowering relies on undefined behavior", record, source, "no-c-undefined-behavior")
    if not record.get("abi_layout_pinned"):
        raise_error("B2-ABI", "C ABI or layout is not pinned", record, source, "abi-layout-manifest")
    if not record.get("pointer_provenance"):
        raise_error("B2-POINTER", "C pointer lowering lacks provenance", record, source, "pointer-provenance")
    if record.get("hidden_runtime_dependency"):
        raise_error("B2-RUNTIME", "C backend has hidden runtime dependency", record, source, "runtime-helper-manifest")


def validate_llvm_backend(record: dict[str, Any], source: str) -> None:
    if not record.get("target_record") or not record.get("data_layout"):
        raise_error("B3-TARGET", "LLVM target or data layout is incomplete", record, source, "target-data-layout")
    if record.get("proofless_metadata"):
        raise_error("B3-METADATA", "LLVM metadata or attributes lack Gravity proof", record, source, "proof-gated-llvm-metadata")
    if record.get("implicit_ub"):
        raise_error("B3-UB", "LLVM lowering exposes undefined behavior or poison", record, source, "defined-safe-gravity")
    if record.get("pass_pipeline", {}).get("verifier") != ":passed":
        raise_error("B3-PASS", "LLVM pass pipeline failed verifier", record, source, "llvm-verifier")


def validate_wasm_backend(record: dict[str, Any], source: str) -> None:
    if not record.get("target_features"):
        raise_error("B4-TARGET", "Wasm target features are missing", record, source, "wasm-target-features")
    if record.get("ambient_imports"):
        raise_error("B4-IMPORT", "Wasm import lacks effect, capability, schema, or provider record", record, source, "import-capability-schema")
    if not record.get("canonical_abi"):
        raise_error("B4-CANONICAL-ABI", "Wasm canonical ABI record is missing", record, source, "canonical-abi")
    if record.get("async_component_missing_metadata"):
        raise_error("B4-WASI-ASYNC", "Wasm async component lacks ownership, cancellation, backpressure, or replay metadata", record, source, "async-component-metadata")


def validate_jvm_backend(record: dict[str, Any], source: str) -> None:
    if not record.get("target_record"):
        raise_error("B5-TARGET", "JVM target record is missing", record, source, "jvm-target-record")
    if record.get("unchecked_null_flow"):
        raise_error("B5-NULL", "JVM null enters safe Gravity unchecked", record, source, "nullability-map")
    if record.get("untranslated_exception"):
        raise_error("B5-EXCEPTION", "JVM exception is untranslated", record, source, "exception-map")
    if record.get("gc_only_linear_cleanup"):
        raise_error("B5-RESOURCE", "linear resource relies only on GC cleanup", record, source, "deterministic-cleanup")


def validate_js_ts_backend(record: dict[str, Any], source: str) -> None:
    if not record.get("target_record") or not record.get("typescript_declarations"):
        raise_error("B6-TARGET", "JS/TS target or declarations are incomplete", record, source, "js-ts-target-declarations")
    if record.get("ambient_globals"):
        raise_error("B6-GLOBAL", "JS/TS host global lacks effect and capability evidence", record, source, "host-global-capability")
    if record.get("unchecked_nullish_flow"):
        raise_error("B6-NULLISH", "JS/TS nullish value enters safe Gravity unchecked", record, source, "nullish-policy")
    if record.get("lossy_number_lowering"):
        raise_error("B6-NUMERIC", "JS/TS numeric lowering loses Gravity numeric semantics", record, source, "numeric-representation-map")


def validate_mlir_backend(record: dict[str, Any], source: str) -> None:
    if not record.get("dialect_registry"):
        raise_error("B7-DIALECT", "MLIR dialect registry is missing", record, source, "dialect-registry")
    if record.get("verifier") != ":passed":
        raise_error("B7-VERIFY", "MLIR verifier failed", record, source, "mlir-verifier")
    if record.get("metadata_loss"):
        raise_error("B7-METADATA", "MLIR lowering lost Gravity metadata", record, source, "metadata-preservation")
    if record.get("effect_change_without_repair"):
        raise_error("B7-EFFECT", "MLIR pass changed effects without repair", record, source, "effect-repair")


def validate_gpu_backend(record: dict[str, Any], source: str) -> None:
    if not record.get("host_device_boundary"):
        raise_error("B8-TARGET", "GPU host/device boundary is missing", record, source, "host-device-boundary")
    if record.get("host_effect_in_kernel"):
        raise_error("B8-HOST-EFFECT", "GPU kernel captures host-only effect", record, source, "gpu-profile-effect-boundary")
    if not record.get("transfer_graph"):
        raise_error("B8-TRANSFER", "GPU transfer graph is missing", record, source, "transfer-graph")
    if not record.get("sync_graph"):
        raise_error("B8-SYNC", "GPU synchronization graph is missing", record, source, "sync-graph")
    if record.get("uncertified_fast_math"):
        raise_error("B8-MATH", "GPU math lowering lacks numeric certificate", record, source, "math-certificate")


def validate_hdl_backend(record: dict[str, Any], source: str) -> None:
    if record.get("missing_fixed_widths"):
        raise_error("B9-WIDTH", "HDL signal or arithmetic lacks fixed width", record, source, "fixed-width-layout")
    if not record.get("clock_domains"):
        raise_error("B9-CLOCK", "HDL clock-domain report is missing", record, source, "clock-domains")
    if record.get("runtime_constructs"):
        raise_error("B9-RUNTIME", "HDL lowering contains runtime construct", record, source, "hardware-approved-primitive")
    if record.get("unsafe_cdc"):
        raise_error("B9-CDC", "HDL clock-domain crossing lacks proof or synchronizer", record, source, "cdc-proof")


def validate_workflow_backend(record: dict[str, Any], source: str) -> None:
    if not record.get("schema_bundle"):
        raise_error("B10-SCHEMA", "workflow graph lacks schemas", record, source, "workflow-step-schemas")
    if record.get("unrecorded_nondeterminism"):
        raise_error("B10-REPLAY", "workflow replay has unrecorded nondeterminism", record, source, "replay-event-log")
    if record.get("missing_idempotency"):
        raise_error("B10-IDEMPOTENCY", "workflow step lacks idempotency record", record, source, "idempotency-key")
    if record.get("missing_capability"):
        raise_error("B10-CAPABILITY", "workflow step lacks capability or human-review authority", record, source, "capability-policy")


def validate_query_backend(record: dict[str, Any], source: str) -> None:
    if not record.get("dialect_schema_map"):
        raise_error("B11-SCHEMA", "query backend lacks dialect or schema mapping", record, source, "dialect-schema-map")
    if record.get("tainted_string_query"):
        raise_error("B11-TAINT", "tainted input reaches executable SQL syntax", record, source, "prepared-binding")
    if record.get("write_without_capability"):
        raise_error("B11-CAPABILITY", "database write lacks capability", record, source, "database-write-capability")
    if record.get("unchecked_result_adapter"):
        raise_error("B11-RESULT", "query result adapter is unchecked", record, source, "typed-result-adapter")


def validate_mobile_backend(record: dict[str, Any], source: str) -> None:
    if not record.get("platform_target"):
        raise_error("B12-TARGET", "mobile platform target is missing", record, source, "platform-target")
    if record.get("permissionless_api"):
        raise_error("B12-PERMISSION", "mobile platform API lacks permission or capability", record, source, "permission-capability")
    if record.get("lifecycle_gap"):
        raise_error("B12-LIFECYCLE", "mobile lifecycle assumption is unmodeled", record, source, "lifecycle-map")
    if record.get("thread_violation"):
        raise_error("B12-THREAD", "mobile UI/threading rule is violated", record, source, "threading-map")


def validate_artifact_records(records: list[dict[str, Any]], source: str) -> None:
    if len(records) < len(REQUIRED_BACKENDS):
        raise_error("B13-SCHEMA", "backend artifact record set is incomplete", {}, source, "backend-artifact-records")
    for record in records:
        missing = sorted(COMMON_ARTIFACT_FIELDS - set(record))
        if missing:
            raise_error("B13-SCHEMA", "backend artifact manifest lacks common fields", record, source, ",".join(missing))
        if record.get("stale_hash"):
            raise_error("B13-HASH", "backend artifact hash is stale or mismatched", record, source, "content-hash")
        if record.get("missing_evidence"):
            raise_error("B13-EVIDENCE", "backend artifact lacks safety, proof, effect, capability, or audit evidence", record, source, "artifact-evidence")


def validate_abi_layout(record: dict[str, Any], source: str) -> None:
    if not record.get("records"):
        raise_error("B1-ABI", "ABI/layout report lacks records", record, source, "abi-layout-records")
    for item in record.get("records", []):
        if not item.get("backend") or not item.get("layout") or not item.get("source"):
            raise_error("B1-ABI", "ABI/layout record is incomplete", item, source, "backend-layout-source")


def validate_artifact_emission(record: dict[str, Any], source: str) -> None:
    if record.get("missing_evidence"):
        raise_error("B13-EVIDENCE", "artifact emission lacks required evidence", record, source, "safety-proof-capability-effect-evidence")
    if not record.get("artifact_graph") or not record.get("content_hashes"):
        raise_error("B13-GRAPH", "artifact emission lacks graph or hashes", record, source, "artifact-graph-content-hashes")
    if record.get("release_without_conformance"):
        raise_error("B13-RELEASE", "release artifact lacks conformance evidence", record, source, "release-conformance-evidence")


def validate_conformance(record: dict[str, Any], source: str) -> None:
    targets = set(record.get("target_matrix", []))
    missing = sorted(REQUIRED_BACKENDS - targets)
    if missing:
        raise_error("B14-COVERAGE", "backend conformance matrix lacks required targets", record, source, ",".join(missing))
    if record.get("execution_only_claim"):
        raise_error("B14-METADATA", "backend conformance is execution-only", record, source, "metadata-preservation")
    if record.get("unrecorded_nondeterminism"):
        raise_error("B14-NONDETERMINISM", "conformance test has unrecorded nondeterminism", record, source, "nondeterminism-record")
    for result in record.get("negative_results", []):
        if result.get("actual_diagnostic") != result.get("expected_diagnostic"):
            raise_error("B14-NEGATIVE", "negative backend fixture produced wrong diagnostic", result, source, "expected-diagnostic-id")


def require_dict(manifest: dict[str, Any], key: str, code: str, source: str) -> dict[str, Any]:
    value = manifest.get(key)
    if not isinstance(value, dict) or not value:
        raise_error(code, f"manifest lacks required object {key}", {}, source, key)
    return value


def require_list(manifest: dict[str, Any], key: str, code: str, source: str) -> list[dict[str, Any]]:
    value = manifest.get(key)
    if not isinstance(value, list) or not value:
        raise_error(code, f"manifest lacks required list {key}", {}, source, key)
    return value


def raise_error(code: str, message: str, record: dict[str, Any], source: str, missing_fact: str) -> None:
    raise BackendArchitectureError(
        code,
        message,
        record=record,
        source=source,
        missing_fact=missing_fact,
        remediation="Update the backend manifest, target lowering record, artifact emission record, diagnostic fixture, or conformance evidence.",
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
