"""SAFE16 safety conformance manifest validation."""

from __future__ import annotations

import hashlib
import json
from pathlib import Path
from typing import Any


REQUIRED_DOCUMENTS = {f"SAFE{index}" for index in range(1, 16)}


class SafetyConformanceError(Exception):
    def __init__(
        self,
        code: str,
        message: str,
        *,
        fixture: dict[str, Any] | None = None,
        source: str,
        missing_fact: str,
        remediation: str,
    ) -> None:
        super().__init__(message)
        self.code = code
        self.message = message
        self.fixture = fixture or {}
        self.source = source
        self.missing_fact = missing_fact
        self.remediation = remediation

    def to_diagnostic(self) -> dict[str, Any]:
        return {
            "id": self.code,
            "message": self.message,
            "fixture_id": self.fixture.get("id"),
            "document_id": self.fixture.get("document"),
            "profile": self.fixture.get("profile"),
            "target": self.fixture.get("target"),
            "expected_outcome": self.fixture.get("expected", {}).get("verdict"),
            "actual_outcome": self.fixture.get("actual", {}).get("verdict"),
            "missing_fact": self.missing_fact,
            "source_span": self.fixture.get("source_span", {"source": self.source}),
            "remediation": self.remediation,
            "analyzer_stage": "safety-conformance",
        }


def validate_safety_conformance_file(path: Path) -> dict[str, Any]:
    return validate_safety_conformance_manifest(load_manifest_file(path), str(path))


def safety_conformance_diagnostic(path: Path) -> dict[str, Any] | None:
    try:
        validate_safety_conformance_file(path)
    except SafetyConformanceError as exc:
        return exc.to_diagnostic()
    return None


def validate_safety_conformance_manifest(manifest: dict[str, Any], source: str) -> dict[str, Any]:
    if manifest.get("kind") != "safety-conformance-input":
        raise SafetyConformanceError(
            "SAFE16-FIXTURE",
            "safety conformance input has the wrong artifact kind",
            source=source,
            missing_fact="safety-conformance-input",
            remediation="Feed SAFE16 a safety-conformance-input artifact.",
        )
    families = manifest.get("fixture_families", [])
    documents = {family.get("document") for family in families}
    missing_documents = sorted(REQUIRED_DOCUMENTS - documents)
    if missing_documents:
        raise SafetyConformanceError(
            "SAFE16-FIXTURE",
            f"conformance corpus is missing documents: {missing_documents}",
            source=source,
            missing_fact=",".join(missing_documents),
            remediation="Add positive, negative, and artifact-inspection fixtures for every SAFE1-SAFE15 document.",
        )
    for family in families:
        validate_family(family, source)
    for fixture in manifest.get("fixture_results", []):
        validate_fixture_result(fixture, source)
    validate_report(manifest, source)

    return {
        "kind": "safety-conformance-artifact",
        "document": "SAFE16",
        "package": manifest.get("package"),
        "module": manifest.get("module"),
        "profile_matrix": manifest.get("profile_matrix", []),
        "input_hash": artifact_hash(manifest),
        "fixture_corpus": families,
        "expected_outcome_manifest": manifest.get("fixture_results", []),
        "diagnostic_match_records": collect(manifest, "diagnostic_matches"),
        "runtime_check_inspection_records": collect(manifest, "runtime_check_inspections"),
        "unsafe_audit_inspection_records": collect(manifest, "unsafe_audit_inspections"),
        "certificate_inspection_records": collect(manifest, "certificate_inspections"),
        "profile_matrix_reports": manifest.get("profile_matrix_reports", []),
        "backend_preservation_reports": manifest.get("backend_preservation_reports", []),
        "safety_conformance_summary": manifest.get("summary", {}),
        "diagnostics": [],
    }


def validate_family(family: dict[str, Any], source: str) -> None:
    if not family.get("positive") or not family.get("negative") or not family.get("artifact_inspections"):
        raise SafetyConformanceError(
            "SAFE16-FIXTURE",
            "fixture family lacks positive, negative, or artifact-inspection fixtures",
            fixture=family,
            source=source,
            missing_fact="positive-negative-artifact-fixtures",
            remediation="Every SAFE family must include positive, negative, and artifact-inspection fixtures.",
        )


def validate_fixture_result(fixture: dict[str, Any], source: str) -> None:
    if fixture.get("expected", {}).get("verdict") != fixture.get("actual", {}).get("verdict"):
        raise SafetyConformanceError(
            "SAFE16-OUTCOME",
            "fixture actual outcome does not match expected outcome",
            fixture=fixture,
            source=source,
            missing_fact="expected-actual-outcome-match",
            remediation="Fix the implementation or update the expected outcome with governing-document evidence.",
        )
    if fixture.get("expected", {}).get("diagnostic") and not (
        fixture.get("diagnostic_match", {}).get("id_matches") and fixture.get("diagnostic_match", {}).get("span_matches")
    ):
        raise SafetyConformanceError(
            "SAFE16-DIAGNOSTIC",
            "fixture diagnostic id or span did not match",
            fixture=fixture,
            source=source,
            missing_fact="diagnostic-id-span-match",
            remediation="Match diagnostics by structured id and source span.",
        )
    if fixture.get("expected", {}).get("artifacts") and not fixture.get("artifact_inspection", {}).get("passed"):
        raise SafetyConformanceError(
            "SAFE16-ARTIFACT",
            "required artifact inspection failed or is missing",
            fixture=fixture,
            source=source,
            missing_fact="artifact-inspection",
            remediation="Inspect emitted safety artifacts, not only compiler outcomes.",
        )
    if fixture.get("profile_matrix", {}).get("status") == ":mismatch":
        raise SafetyConformanceError(
            "SAFE16-PROFILE",
            "profile matrix expected and actual behavior differ",
            fixture=fixture,
            source=source,
            missing_fact="profile-matrix-match",
            remediation="Record profile-specific accept, reject, narrow, or delegate behavior correctly.",
        )
    if fixture.get("expected", {}).get("proof") and not fixture.get("certificate_inspection", {}).get("passed"):
        raise SafetyConformanceError(
            "SAFE16-CERTIFICATE",
            "proof or certificate inspection failed or is missing",
            fixture=fixture,
            source=source,
            missing_fact="certificate-inspection",
            remediation="Inspect SAFE15 proof and certificate records for proof-backed outcomes.",
        )
    if fixture.get("backend_preservation", {}).get("status") == ":mismatch":
        raise SafetyConformanceError(
            "SAFE16-BACKEND",
            "backend preservation report mismatched expected safety facts",
            fixture=fixture,
            source=source,
            missing_fact="backend-preservation-match",
            remediation="Retain checks or prove backend preservation before accepting optimized/backend fixtures.",
        )


def validate_report(manifest: dict[str, Any], source: str) -> None:
    summary = manifest.get("summary", {})
    if summary.get("machine_readable") is not True or summary.get("status") != ":passed":
        raise SafetyConformanceError(
            "SAFE16-REPORT",
            "conformance report is not machine-readable or did not pass",
            source=source,
            missing_fact="machine-readable-passing-report",
            remediation="Emit a machine-readable conformance report with pass/fail state and artifact ids.",
        )


def collect(manifest: dict[str, Any], key: str) -> list[dict[str, Any]]:
    records = []
    for fixture in manifest.get("fixture_results", []):
        for record in fixture.get(key, []):
            item = dict(record)
            item.setdefault("fixture_id", fixture.get("id"))
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
