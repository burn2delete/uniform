"""Phase 17 governance and evolution validation."""

from __future__ import annotations

import hashlib
import json
from pathlib import Path
from typing import Any


DOCUMENT_COMPONENTS = {
    "GOV1": "language_evolution",
    "GOV2": "compatibility_policy",
    "GOV3": "standard_library_governance",
    "GOV4": "security_review",
    "GOV5": "target_support",
    "GOV6": "rfc_process",
    "GOV7": "experiment_policy",
    "GOV8": "deprecation_stabilization",
    "GOV9": "unsafe_governance",
    "GOV10": "ecosystem_package_governance",
}

REJECTION_FLAGS = {
    "GOV1": ("missing_change_owner", "GOV1001", "change record lacks ownership or scope", "change-owner-scope"),
    "GOV2": ("missing_baseline", "GOV2001", "stable change lacks baseline compatibility analysis", "compatibility-baseline"),
    "GOV3": ("missing_stdlib_owner", "GOV3001", "standard module lacks owner, stability, or profile matrix", "standard-library-owner"),
    "GOV4": ("missing_security_review", "GOV4001", "security-impacting change lacks review record", "security-review"),
    "GOV5": ("missing_target_tier", "GOV5001", "target lacks tier, owner, or profile matrix", "target-tier-owner"),
    "GOV6": ("missing_rfc_owner", "GOV6001", "RFC lacks owner, scope, or affected documents", "rfc-owner-scope"),
    "GOV7": ("missing_experiment_metadata", "GOV7001", "experiment lacks owner, document, profile, expiry, or rollback metadata", "experiment-metadata"),
    "GOV8": ("missing_stabilization_evidence", "GOV8001", "stabilization lacks evidence or compatibility surfaces", "stabilization-evidence"),
    "GOV9": ("missing_unsafe_record", "GOV9001", "unsafe code lacks an island record", "unsafe-island-record"),
    "GOV10": ("missing_package_identity", "GOV10001", "package identity, owner, namespace, or version metadata is incomplete", "package-identity"),
}

COMMON_FIELDS = [
    "document",
    "artifact_id",
    "record_id",
    "owner",
    "state",
    "scope",
    "affected_surfaces",
    "review_gates",
    "evidence",
    "artifacts",
    "diagnostics",
    "decision",
    "provenance_record",
]


class GovernanceEvolutionError(Exception):
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
            "record_id": self.record.get("record_id"),
            "owner": self.record.get("owner"),
            "state": self.record.get("state"),
            "source_span": self.record.get("source_span", {"source": self.source}),
            "missing_fact": self.missing_fact,
            "remediation": self.remediation,
            "analyzer_stage": "phase17-governance-evolution-validation",
        }


def validate_governance_evolution_file(path: Path) -> dict[str, Any]:
    return validate_governance_evolution_manifest(load_manifest_file(path), str(path))


def governance_evolution_diagnostic(path: Path) -> dict[str, Any] | None:
    try:
        validate_governance_evolution_file(path)
    except GovernanceEvolutionError as exc:
        return exc.to_diagnostic()
    return None


def validate_governance_evolution_manifest(manifest: dict[str, Any], source: str) -> dict[str, Any]:
    if manifest.get("kind") != "governance-evolution-input":
        raise_error("GOV1001", "governance input has wrong kind", {}, source, "governance-evolution-input")

    components = {doc: require_dict(manifest, key, "GOV1001", source) for doc, key in DOCUMENT_COMPONENTS.items()}
    for document, record in components.items():
        validate_governance_record(document, record, source)

    return {
        "kind": "governance-evolution-artifact",
        "phase": "17",
        "language_change_record": components["GOV1"],
        "compatibility_report": components["GOV2"],
        "standard_library_governance_record": components["GOV3"],
        "security_review_record": components["GOV4"],
        "target_support_matrix": components["GOV5"],
        "rfc_record": components["GOV6"],
        "experiment_registry": components["GOV7"],
        "deprecation_plan": components["GOV8"],
        "unsafe_governance_audit": components["GOV9"],
        "ecosystem_package_governance_record": components["GOV10"],
        "document_contracts": components,
        "coverage_summary": {
            "documents": len(DOCUMENT_COMPONENTS),
            "tasks": 6,
            "status": ":passed",
        },
        "input_hash": artifact_hash(manifest),
        "diagnostics": [],
    }


def validate_governance_record(document: str, record: dict[str, Any], source: str) -> None:
    require_fields(record, COMMON_FIELDS, REJECTION_FLAGS[document][1], source)
    if record.get("document") != document:
        raise_error(REJECTION_FLAGS[document][1], f"record is for {record.get('document')} but expected {document}", record, source, "document-id")
    flag, code, message, missing_fact = REJECTION_FLAGS[document]
    if record.get(flag):
        raise_error(code, message, record, source, missing_fact)


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
    raise GovernanceEvolutionError(
        code,
        message,
        record=record,
        source=source,
        missing_fact=missing_fact,
        remediation="Update the Phase 17 governance record, review evidence, migration record, or provenance artifact.",
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


def artifact_hash(value: Any) -> str:
    data = json.dumps(value, sort_keys=True, separators=(",", ":"))
    return "sha256:" + hashlib.sha256(data.encode("utf-8")).hexdigest()
