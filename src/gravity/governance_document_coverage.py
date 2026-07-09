"""Phase 17 document-specific governance coverage validation."""

from __future__ import annotations

import hashlib
import json
from pathlib import Path
from typing import Any

from gravity.governance_evolution import governance_evolution_diagnostic, validate_governance_evolution_file


ROOT = Path(__file__).resolve().parents[2]
REQUIRED_DOCUMENTS = [f"GOV{index}" for index in range(1, 11)]


class GovernanceDocumentCoverageError(Exception):
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
            "task_id": self.record.get("task_id"),
            "source_span": {"source": self.source},
            "missing_fact": self.missing_fact,
            "remediation": self.remediation,
            "analyzer_stage": "phase17-governance-document-coverage",
        }


def validate_phase17_document_coverage_file(path: Path) -> dict[str, Any]:
    return validate_phase17_document_coverage_manifest(load_json(path), str(path))


def validate_phase17_document_coverage_manifest(manifest: dict[str, Any], source: str) -> dict[str, Any]:
    if manifest.get("kind") != "phase17-governance-document-coverage-input":
        raise_error("GOV1001", "document coverage input has the wrong kind", {}, source, "phase17-governance-document-coverage-input")
    records = manifest.get("documents", [])
    documents = {record.get("document") for record in records}
    missing = sorted(set(REQUIRED_DOCUMENTS) - documents, key=document_sort_key)
    if missing or len(records) != 10:
        raise_error("GOV1001", "Phase 17 coverage must include GOV1 through GOV10", {}, source, ",".join(missing))
    accepted = []
    rejected = []
    for record in records:
        accepted.append(validate_accepted(record, source))
        rejected.append(validate_rejected(record, source))
    return {
        "kind": "phase17-governance-document-coverage-artifact",
        "phase": "17",
        "documents": sorted(documents, key=document_sort_key),
        "accepted": accepted,
        "rejected": rejected,
        "coverage_summary": {
            "documents": len(accepted),
            "accepted_artifacts": len(accepted),
            "rejected_diagnostics": len(rejected),
            "status": ":passed",
        },
        "input_hash": artifact_hash(manifest),
        "diagnostics": [],
    }


def validate_accepted(record: dict[str, Any], source: str) -> dict[str, Any]:
    fixture = require_path(record, record.get("accepted", {}).get("fixture"), source, "accepted-fixture")
    artifact = validate_governance_evolution_file(fixture)
    document = record.get("document")
    document_artifact = artifact["document_contracts"].get(document)
    if not document_artifact:
        raise_error("GOV1001", "accepted artifact does not include document", record, source, "document-contract")
    for field in record.get("accepted", {}).get("required_artifact_fields", []):
        if not artifact.get(field):
            raise_error("GOV1001", f"accepted artifact missing field {field}", record, source, field)
    coverage = record.get("coverage_claims", [])
    if not coverage:
        raise_error("GOV1001", "coverage record lacks claims", record, source, "coverage-claims")
    return {
        "task_id": record.get("task_id"),
        "document": document,
        "governing_doc": record.get("governing_doc"),
        "fixture": rel(fixture),
        "artifact_kind": artifact.get("kind"),
        "artifact_id": document_artifact.get("artifact_id"),
        "record_id": document_artifact.get("record_id"),
        "coverage": coverage,
        "artifact_hash": artifact_hash(document_artifact),
    }


def validate_rejected(record: dict[str, Any], source: str) -> dict[str, Any]:
    rejected = record.get("rejected", {})
    fixture = require_path(record, rejected.get("fixture"), source, "rejected-fixture")
    diagnostic = governance_evolution_diagnostic(fixture)
    expected = rejected.get("diagnostic")
    if diagnostic is None:
        raise_error("GOV1001", "rejected fixture was accepted", record, source, "diagnostic")
    if diagnostic.get("id") != expected:
        raise_error(
            "GOV1001",
            f"rejected fixture produced {diagnostic.get('id')} instead of {expected}",
            record,
            source,
            "diagnostic-id",
        )
    return {
        "task_id": record.get("task_id"),
        "document": record.get("document"),
        "fixture": rel(fixture),
        "diagnostic": diagnostic["id"],
        "missing_fact": diagnostic.get("missing_fact"),
    }


def require_path(record: dict[str, Any], value: str | None, source: str, fact: str) -> Path:
    if not value:
        raise_error("GOV1001", "coverage record lacks a path", record, source, fact)
    path = Path(value)
    path = path if path.is_absolute() else ROOT / path
    if not path.exists():
        raise_error("GOV1001", f"coverage path does not exist: {value}", record, source, fact)
    return path


def raise_error(code: str, message: str, record: dict[str, Any], source: str, missing_fact: str) -> None:
    raise GovernanceDocumentCoverageError(
        code,
        message,
        record=record,
        source=source,
        missing_fact=missing_fact,
        remediation="Update the Phase 17 document coverage record, fixture, or governance validator evidence.",
    )


def load_json(path: Path) -> dict[str, Any]:
    return json.loads(path.read_text(encoding="utf-8"))


def rel(path: Path) -> str:
    try:
        return str(path.resolve().relative_to(ROOT))
    except ValueError:
        return str(path)


def document_sort_key(document: str | None) -> int:
    if not document or not document.startswith("GOV"):
        return 0
    return int(document[3:])


def artifact_hash(value: Any) -> str:
    data = json.dumps(value, sort_keys=True, separators=(",", ":"))
    return "sha256:" + hashlib.sha256(data.encode("utf-8")).hexdigest()
