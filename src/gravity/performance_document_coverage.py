"""Phase 04 document-specific performance coverage validation."""

from __future__ import annotations

import hashlib
import json
from pathlib import Path
from typing import Any

from gravity.performance import performance_diagnostic, validate_performance_file


ROOT = Path(__file__).resolve().parents[2]
REQUIRED_DOCUMENTS = {f"PERF{index}" for index in range(1, 11)}


class PerformanceDocumentCoverageError(Exception):
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
            "analyzer_stage": "phase04-performance-document-coverage",
        }


def validate_phase04_document_coverage_file(path: Path) -> dict[str, Any]:
    return validate_phase04_document_coverage_manifest(load_json(path), str(path))


def validate_phase04_document_coverage_manifest(manifest: dict[str, Any], source: str) -> dict[str, Any]:
    if manifest.get("kind") != "phase04-performance-document-coverage-input":
        raise_error("PERF1-EVIDENCE", "document coverage input has the wrong kind", {}, source, "phase04-performance-document-coverage-input")
    records = manifest.get("documents", [])
    documents = {record.get("document") for record in records}
    missing = sorted(REQUIRED_DOCUMENTS - documents)
    if missing or len(records) != 10:
        raise_error("PERF1-EVIDENCE", "Phase 04 coverage must include PERF1 through PERF10", {}, source, ",".join(missing))
    accepted = []
    rejected = []
    for record in records:
        accepted.append(validate_accepted(record, source))
        rejected.append(validate_rejected(record, source))
    return {
        "kind": "phase04-performance-document-coverage-artifact",
        "phase": "04",
        "documents": sorted(documents, key=lambda item: int(item[4:])),
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
    artifact = validate_performance_file(fixture)
    for field in record.get("accepted", {}).get("required_artifact_fields", []):
        if not artifact.get(field):
            raise_error("PERF1-EVIDENCE", f"accepted artifact missing field {field}", record, source, field)
    coverage = record.get("coverage_claims", [])
    if not coverage:
        raise_error("PERF1-EVIDENCE", "coverage record lacks claims", record, source, "coverage-claims")
    return {
        "task_id": record.get("task_id"),
        "document": record.get("document"),
        "governing_doc": record.get("governing_doc"),
        "fixture": rel(fixture),
        "artifact_kind": artifact.get("kind"),
        "coverage": coverage,
        "artifact_hash": artifact_hash(artifact),
    }


def validate_rejected(record: dict[str, Any], source: str) -> dict[str, Any]:
    rejected = record.get("rejected", {})
    fixture = require_path(record, rejected.get("fixture"), source, "rejected-fixture")
    diagnostic = performance_diagnostic(fixture)
    expected = rejected.get("diagnostic")
    if diagnostic is None:
        raise_error("PERF1-EVIDENCE", "rejected fixture was accepted", record, source, "diagnostic")
    if diagnostic.get("id") != expected:
        raise_error("PERF1-EVIDENCE", f"rejected fixture produced {diagnostic.get('id')} instead of {expected}", record, source, "diagnostic-id")
    return {
        "task_id": record.get("task_id"),
        "document": record.get("document"),
        "fixture": rel(fixture),
        "diagnostic": diagnostic["id"],
        "missing_fact": diagnostic.get("missing_evidence"),
    }


def require_path(record: dict[str, Any], value: str | None, source: str, fact: str) -> Path:
    if not value:
        raise_error("PERF1-EVIDENCE", "coverage record lacks a path", record, source, fact)
    path = Path(value)
    path = path if path.is_absolute() else ROOT / path
    if not path.exists():
        raise_error("PERF1-EVIDENCE", f"coverage path does not exist: {value}", record, source, fact)
    return path


def raise_error(code: str, message: str, record: dict[str, Any], source: str, missing_fact: str) -> None:
    raise PerformanceDocumentCoverageError(
        code,
        message,
        record=record,
        source=source,
        missing_fact=missing_fact,
        remediation="Update the Phase 04 document coverage record, fixture, or owning performance validator evidence.",
    )


def load_json(path: Path) -> dict[str, Any]:
    return json.loads(path.read_text(encoding="utf-8"))


def rel(path: Path) -> str:
    try:
        return str(path.resolve().relative_to(ROOT))
    except ValueError:
        return str(path)


def artifact_hash(value: Any) -> str:
    data = json.dumps(value, sort_keys=True, separators=(",", ":"))
    return "sha256:" + hashlib.sha256(data.encode("utf-8")).hexdigest()
