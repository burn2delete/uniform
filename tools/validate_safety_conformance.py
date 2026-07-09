#!/usr/bin/env python3
"""Validate the Phase 02 P02-T06 safety conformance suite manifest."""

from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / "src"))

from gravity.safety_conformance import safety_conformance_diagnostic, validate_safety_conformance_file  # noqa: E402


FIXTURE_DIR = ROOT / "docs/artifacts/phase-02/fixtures/conformance"
DEFAULT_ACCEPTED = FIXTURE_DIR / "accepted-safety-conformance.json"
NEGATIVE_FIXTURES = {
    "rejected-fixture-family.json": "SAFE16-FIXTURE",
    "rejected-outcome.json": "SAFE16-OUTCOME",
    "rejected-diagnostic.json": "SAFE16-DIAGNOSTIC",
    "rejected-artifact.json": "SAFE16-ARTIFACT",
    "rejected-profile.json": "SAFE16-PROFILE",
    "rejected-certificate.json": "SAFE16-CERTIFICATE",
    "rejected-backend.json": "SAFE16-BACKEND",
    "rejected-report.json": "SAFE16-REPORT",
}


def require(condition: bool, message: str) -> None:
    if not condition:
        raise AssertionError(message)


def validate_accepted(path: Path) -> dict:
    artifact = validate_safety_conformance_file(path)
    require(artifact["kind"] == "safety-conformance-artifact", "artifact kind mismatch")
    require(artifact["document"] == "SAFE16", "document mismatch")
    require(len(artifact["fixture_corpus"]) == 15, "expected SAFE1-SAFE15 fixture families")
    for key in [
        "expected_outcome_manifest",
        "diagnostic_match_records",
        "runtime_check_inspection_records",
        "unsafe_audit_inspection_records",
        "certificate_inspection_records",
        "profile_matrix_reports",
        "backend_preservation_reports",
        "safety_conformance_summary",
    ]:
        require(artifact[key], f"{key} missing")
    require(artifact["safety_conformance_summary"]["status"] == ":passed", "summary did not pass")
    require(not artifact["diagnostics"], "accepted artifact should not contain diagnostics")
    return artifact


def validate_negative_fixtures() -> list[dict[str, str]]:
    observed = []
    for filename, expected in NEGATIVE_FIXTURES.items():
        path = FIXTURE_DIR / filename
        diagnostic = safety_conformance_diagnostic(path)
        require(diagnostic is not None, f"{filename} did not produce a diagnostic")
        require(diagnostic["id"] == expected, f"{filename} produced {diagnostic['id']} instead of {expected}")
        for key in ["document_id", "expected_outcome", "actual_outcome", "missing_fact", "remediation"]:
            require(key in diagnostic, f"{filename} diagnostic missing {key}")
        observed.append({"fixture": filename, "diagnostic": diagnostic["id"]})
    return observed


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--accepted", type=Path, default=DEFAULT_ACCEPTED)
    parser.add_argument("--artifact-out", type=Path)
    args = parser.parse_args()

    try:
        artifact = validate_accepted(args.accepted)
        diagnostics = validate_negative_fixtures()
    except AssertionError as exc:
        print(f"safety conformance validation failed: {exc}", file=sys.stderr)
        return 1

    if args.artifact_out:
        args.artifact_out.parent.mkdir(parents=True, exist_ok=True)
        args.artifact_out.write_text(json.dumps(artifact, indent=2, sort_keys=True) + "\n", encoding="utf-8")

    print(
        "safety conformance validation passed: "
        f"{len(artifact['fixture_corpus'])} fixture families, {len(diagnostics)} rejected fixtures"
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
