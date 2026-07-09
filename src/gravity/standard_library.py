"""Phase 16 standard library validation."""

from __future__ import annotations

import hashlib
import json
from pathlib import Path
from typing import Any


DOCUMENT_COMPONENTS = {
    "STD1": "standard_library_architecture",
    "STD2": "core_library",
    "STD3": "collections_library",
    "STD4": "text_library",
    "STD5": "math_library",
    "STD6": "memory_library",
    "STD7": "concurrency_library",
    "STD8": "io_filesystem_library",
    "STD9": "network_http_library",
    "STD10": "serialization_schema_library",
    "STD11": "database_query_library",
    "STD12": "workflow_library",
    "STD13": "ai_agent_library",
    "STD14": "testing_library",
    "STD15": "compiler_meta_library",
    "STD16": "platform_os_library",
    "STD17": "hardware_firmware_library",
    "STD18": "cryptography_library",
    "STD19": "ui_application_library",
    "STD20": "stability_policy",
}

REJECTION_FLAGS = {
    "STD1": ("missing_profile_metadata", "STD1001", "standard-library export lacks profile metadata", "profile-metadata"),
    "STD2": ("host_state_dependency", "STD2002", "core API depends on host state", "host-state"),
    "STD3": ("hidden_allocation", "STD3001", "collection operation would allocate in a profile that forbids hidden allocation", "allocation-policy"),
    "STD4": ("invalid_text_boundary", "STD4002", "text slicing uses an invalid boundary", "text-boundary"),
    "STD5": ("missing_certificate", "STD5003", "required approximation certificate is missing", "math-certificate"),
    "STD6": ("borrow_escape", "STD6002", "borrow escapes its lifetime", "borrow-lifetime"),
    "STD7": ("data_race", "STD7001", "shared mutable state lacks synchronization proof", "synchronization-proof"),
    "STD8": ("missing_filesystem_capability", "STD8001", "filesystem access lacks a capability", "filesystem-capability"),
    "STD9": ("missing_network_capability", "STD9001", "network access lacks a capability", "network-capability"),
    "STD10": ("decode_without_validation", "STD10001", "data is decoded without validation", "schema-validation"),
    "STD11": ("unparameterized_query", "STD11002", "textual query embeds untrusted data without parameterization", "query-parameterization"),
    "STD12": ("unrecorded_nondeterminism", "STD12001", "workflow replay code performs unrecorded nondeterminism", "workflow-replay-record"),
    "STD13": ("missing_ai_metadata", "STD13001", "model call lacks provider, model, schema, budget, or fallback metadata", "ai-call-metadata"),
    "STD14": ("undeclared_test_effect", "STD14001", "test uses an undeclared effect or capability", "test-effect-capability"),
    "STD15": ("unchecked_generated_code", "STD15002", "macro output bypasses ordinary compiler checks", "generated-code-checks"),
    "STD16": ("target_host_confusion", "STD16002", "code confuses build host, runtime host, and compilation target facts", "target-host-facts"),
    "STD17": ("missing_hardware_capability", "STD17001", "hardware access lacks target or device capability metadata", "hardware-capability"),
    "STD18": ("ambiguous_algorithm", "STD18001", "cryptographic algorithm, mode, provider, key, or nonce policy is ambiguous", "crypto-algorithm-policy"),
    "STD19": ("missing_component_metadata", "STD19001", "component lacks required props, state, event, or effect metadata", "ui-component-metadata"),
    "STD20": ("missing_stability_level", "STD20001", "standard-library module or export lacks a stability level", "stability-level"),
}

COMMON_FIELDS = [
    "document",
    "artifact_id",
    "module",
    "namespace",
    "profiles",
    "exports",
    "effect_capability_matrix",
    "allocation_behavior",
    "safe_wrapper_audit",
    "stability",
    "profile_support_matrix",
    "conformance_fixtures",
    "negative_fixtures",
    "docs_examples",
    "artifacts",
    "governance_record",
    "provenance_record",
]


class StandardLibraryError(Exception):
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
            "document": self.record.get("document"),
            "artifact_id": self.record.get("artifact_id"),
            "module": self.record.get("module"),
            "namespace": self.record.get("namespace"),
            "profile": self.record.get("profile"),
            "export": self.record.get("export"),
            "source_span": self.record.get("source_span", {"source": self.source}),
            "missing_fact": self.missing_fact,
            "remediation": self.remediation,
            "analyzer_stage": "phase16-standard-library-validation",
        }


def validate_standard_library_file(path: Path) -> dict[str, Any]:
    return validate_standard_library_manifest(load_manifest_file(path), str(path))


def standard_library_diagnostic(path: Path) -> dict[str, Any] | None:
    try:
        validate_standard_library_file(path)
    except StandardLibraryError as exc:
        return exc.to_diagnostic()
    return None


def validate_standard_library_manifest(manifest: dict[str, Any], source: str) -> dict[str, Any]:
    if manifest.get("kind") != "standard-library-input":
        raise_error("STD1001", "standard-library input has wrong kind", {}, source, "standard-library-input")

    components = {doc: require_dict(manifest, key, "STD1001", source) for doc, key in DOCUMENT_COMPONENTS.items()}
    for document, record in components.items():
        validate_library_record(document, record, source)

    return {
        "kind": "standard-library-artifact",
        "phase": "16",
        "library_module_manifest": [components[doc] for doc in sorted(components, key=document_sort_key)],
        "api_stability_record": components["STD20"],
        "safe_wrapper_audit": collect_field(components, "safe_wrapper_audit"),
        "library_conformance_fixture": collect_field(components, "conformance_fixtures"),
        "profile_support_matrix": {doc: components[doc]["profile_support_matrix"] for doc in components},
        "document_contracts": components,
        "coverage_summary": {
            "documents": len(DOCUMENT_COMPONENTS),
            "tasks": 6,
            "module_count": len(DOCUMENT_COMPONENTS),
            "status": ":passed",
        },
        "input_hash": artifact_hash(manifest),
        "diagnostics": [],
    }


def validate_library_record(document: str, record: dict[str, Any], source: str) -> None:
    require_fields(record, COMMON_FIELDS, REJECTION_FLAGS[document][1], source)
    if record.get("document") != document:
        raise_error(REJECTION_FLAGS[document][1], f"record is for {record.get('document')} but expected {document}", record, source, "document-id")
    if not record.get("exports"):
        raise_error(REJECTION_FLAGS[document][1], "standard-library module has no export evidence", record, source, "exports")
    if record.get("stability") in [None, "", ":internal-without-record"]:
        raise_error("STD20001", "standard-library module lacks stability evidence", record, source, "stability")
    flag, code, message, missing_fact = REJECTION_FLAGS[document]
    if record.get(flag):
        raise_error(code, message, record, source, missing_fact)


def collect_field(components: dict[str, dict[str, Any]], field: str) -> dict[str, Any]:
    return {doc: record[field] for doc, record in components.items()}


def require_dict(manifest: dict[str, Any], key: str, code: str, source: str) -> dict[str, Any]:
    value = manifest.get(key)
    if not isinstance(value, dict):
        raise_error(code, f"manifest lacks {key}", {}, source, key)
    return value


def require_fields(record: dict[str, Any], fields: list[str], code: str, source: str) -> None:
    missing = [field for field in fields if field not in record or record.get(field) in [None, "", []]]
    if missing:
        raise_error(code, f"record lacks required fields: {', '.join(missing)}", record, source, ",".join(missing))


def raise_error(code: str, message: str, record: dict[str, Any], source: str, missing_fact: str) -> None:
    raise StandardLibraryError(
        code,
        message,
        record=record,
        source=source,
        missing_fact=missing_fact,
        remediation="Update the Phase 16 standard-library manifest, profile matrix, safety audit, stability record, or conformance evidence.",
    )


def load_manifest_file(path: Path) -> dict[str, Any]:
    data = load_json(path)
    base_ref = data.pop("extends", None)
    if not base_ref:
        return data
    base_path = Path(base_ref)
    if not base_path.is_absolute():
        base_path = path.parent / base_path
    return deep_merge(load_manifest_file(base_path), data)


def load_json(path: Path) -> dict[str, Any]:
    return json.loads(path.read_text(encoding="utf-8"))


def deep_merge(base: dict[str, Any], overlay: dict[str, Any]) -> dict[str, Any]:
    merged = dict(base)
    for key, value in overlay.items():
        if isinstance(value, dict) and isinstance(merged.get(key), dict):
            merged[key] = deep_merge(merged[key], value)
        else:
            merged[key] = value
    return merged


def document_sort_key(document: str) -> int:
    return int(document[3:])


def artifact_hash(value: Any) -> str:
    data = json.dumps(value, sort_keys=True, separators=(",", ":"))
    return "sha256:" + hashlib.sha256(data.encode("utf-8")).hexdigest()
