#!/usr/bin/env python3
"""Validate the Phase 02 P02-T03 unsafe island audit checker."""

from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / "src"))

from gravity.unsafe_audit import unsafe_audit_diagnostic, validate_unsafe_audit_file  # noqa: E402


FIXTURE_DIR = ROOT / "docs/artifacts/phase-02/fixtures/unsafe-audit"
DEFAULT_ACCEPTED = FIXTURE_DIR / "accepted-unsafe-audit.json"
NEGATIVE_FIXTURES = {
    "rejected-forbidden-policy.json": "SAFE6-UNSAFE-FORBIDDEN",
    "rejected-missing-metadata.json": "SAFE6-MISSING-METADATA",
    "rejected-missing-owner.json": "SAFE6-MISSING-OWNER",
    "rejected-missing-invariant.json": "SAFE6-MISSING-INVARIANT",
    "rejected-missing-boundary.json": "SAFE6-MISSING-BOUNDARY",
    "rejected-review-required.json": "SAFE6-REVIEW-REQUIRED",
    "rejected-generated-unsafe.json": "SAFE6-GENERATED-UNSAFE",
    "rejected-capability.json": "SAFE6-CAPABILITY",
    "rejected-dependency.json": "SAFE6-DEPENDENCY",
    "rejected-certificate.json": "SAFE6-CERTIFICATE",
}


def require(condition: bool, message: str) -> None:
    if not condition:
        raise AssertionError(message)


def validate_accepted(path: Path) -> dict:
    artifact = validate_unsafe_audit_file(path)
    require(artifact["kind"] == "unsafe-audit-artifact", "artifact kind mismatch")
    require(artifact["document"] == "SAFE6", "document mismatch")
    require(len(artifact["unsafe_island_records"]) == 2, "expected two unsafe islands")
    require(artifact["safe_wrapper_records"], "safe wrapper records missing")
    require(artifact["unsafe_operation_inventory"], "unsafe operation inventory missing")
    require(artifact["review_status_records"], "review status records missing")
    require(artifact["invariant_and_proof_links"], "invariant links missing")
    require(artifact["generated_unsafe_provenance_records"], "generated unsafe provenance missing")
    require(artifact["policy_decision_records"], "policy records missing")
    require(artifact["unsafe_dependency_summaries"], "dependency summaries missing")
    require(artifact["release_audit_report"]["status"] == ":accepted", "release audit status mismatch")
    require(not artifact["diagnostics"], "accepted artifact should not contain diagnostics")
    return artifact


def validate_negative_fixtures() -> list[dict[str, str]]:
    observed = []
    for filename, expected in NEGATIVE_FIXTURES.items():
        path = FIXTURE_DIR / filename
        diagnostic = unsafe_audit_diagnostic(path)
        require(diagnostic is not None, f"{filename} did not produce a diagnostic")
        require(diagnostic["id"] == expected, f"{filename} produced {diagnostic['id']} instead of {expected}")
        for key in ["span", "policy", "active_profile", "operation_family", "effects", "capabilities", "missing_evidence", "remediation"]:
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
        print(f"unsafe audit validation failed: {exc}", file=sys.stderr)
        return 1

    if args.artifact_out:
        args.artifact_out.parent.mkdir(parents=True, exist_ok=True)
        args.artifact_out.write_text(json.dumps(artifact, indent=2, sort_keys=True) + "\n", encoding="utf-8")

    print(
        "unsafe audit validation passed: "
        f"{len(artifact['unsafe_island_records'])} unsafe islands, {len(diagnostics)} rejected fixtures"
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
