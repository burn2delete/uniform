#!/usr/bin/env python3
"""Validate the Phase 17 governance and evolution contract."""

from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / "src"))

from gravity.governance_evolution import governance_evolution_diagnostic, validate_governance_evolution_file  # noqa: E402


FIXTURE_DIR = ROOT / "docs/artifacts/phase-17/fixtures/governance"
DEFAULT_ACCEPTED = FIXTURE_DIR / "accepted-governance-evolution.json"
NEGATIVE_FIXTURES = {
    "rejected-gov1-owner.json": "GOV1001",
    "rejected-gov2-baseline.json": "GOV2001",
    "rejected-gov3-stdlib-owner.json": "GOV3001",
    "rejected-gov4-security-review.json": "GOV4001",
    "rejected-gov5-target-tier.json": "GOV5001",
    "rejected-gov6-rfc-owner.json": "GOV6001",
    "rejected-gov7-experiment-metadata.json": "GOV7001",
    "rejected-gov8-stabilization-evidence.json": "GOV8001",
    "rejected-gov9-unsafe-record.json": "GOV9001",
    "rejected-gov10-package-identity.json": "GOV10001",
}


def require(condition: bool, message: str) -> None:
    if not condition:
        raise AssertionError(message)


def validate_accepted(path: Path) -> dict:
    artifact = validate_governance_evolution_file(path)
    require(artifact["kind"] == "governance-evolution-artifact", "artifact kind mismatch")
    require(artifact["coverage_summary"]["documents"] == 10, "expected GOV1-GOV10 coverage")
    for key in [
        "language_change_record",
        "compatibility_report",
        "standard_library_governance_record",
        "security_review_record",
        "target_support_matrix",
        "rfc_record",
        "experiment_registry",
        "deprecation_plan",
        "unsafe_governance_audit",
        "ecosystem_package_governance_record",
        "document_contracts",
    ]:
        require(artifact[key], f"{key} missing")
    require(not artifact["diagnostics"], "accepted artifact should not contain diagnostics")
    return artifact


def validate_negative_fixtures() -> list[dict[str, str]]:
    observed = []
    for filename, expected in NEGATIVE_FIXTURES.items():
        diagnostic = governance_evolution_diagnostic(FIXTURE_DIR / filename)
        require(diagnostic is not None, f"{filename} did not produce a diagnostic")
        require(diagnostic["id"] == expected, f"{filename} produced {diagnostic['id']} instead of {expected}")
        for key in ["source_span", "missing_fact", "remediation", "analyzer_stage"]:
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
        print(f"governance evolution validation failed: {exc}", file=sys.stderr)
        return 1

    if args.artifact_out:
        args.artifact_out.parent.mkdir(parents=True, exist_ok=True)
        args.artifact_out.write_text(json.dumps(artifact, indent=2, sort_keys=True) + "\n", encoding="utf-8")

    print(
        "governance evolution validation passed: "
        f"{artifact['coverage_summary']['documents']} documents, "
        f"{len(diagnostics)} rejected fixtures"
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
