#!/usr/bin/env python3
"""Validate the Phase 14 testing, verification, and conformance contract."""

from __future__ import annotations

import argparse
import sys
from pathlib import Path

if __package__:
    from .output_publication import atomic_write_json
else:
    from output_publication import atomic_write_json


ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / "src"))

from gravity.conformance_system import conformance_system_diagnostic, validate_conformance_system_file  # noqa: E402


FIXTURE_DIR = ROOT / "docs/artifacts/phase-14/fixtures/conformance"
DEFAULT_ACCEPTED = FIXTURE_DIR / "accepted-conformance-system.json"
NEGATIVE_FIXTURES = {
    "rejected-test1-metadata.json": "TEST1001",
    "rejected-test2-preserved-fact.json": "TEST2002",
    "rejected-test3-capability.json": "TEST3002",
    "rejected-test4-profile-target.json": "TEST4001",
    "rejected-test5-unsafe-audit.json": "TEST5002",
    "rejected-test6-artifact-manifest.json": "TEST6004",
    "rejected-test7-untested-api.json": "TEST7001",
    "rejected-test8-replay-trace.json": "TEST8003",
    "rejected-test9-seed.json": "TEST9001",
    "rejected-test10-divergence.json": "TEST10002",
    "rejected-test11-proof.json": "TEST11003",
    "rejected-test12-semantic-gate.json": "TEST12003",
    "rejected-test13-provenance.json": "TEST13002",
}


def require(condition: bool, message: str) -> None:
    if not condition:
        raise AssertionError(message)


def validate_accepted(path: Path) -> dict:
    artifact = validate_conformance_system_file(path)
    require(artifact["kind"] == "conformance-system-artifact", "artifact kind mismatch")
    require(artifact["coverage_summary"]["documents"] == 13, "expected TEST1-TEST13 coverage")
    for key in [
        "conformance_harness",
        "fixture_manifest",
        "golden_diagnostics",
        "fuzz_property_suite",
        "differential_report",
        "formal_proof_report",
        "performance_regression_report",
        "document_contracts",
    ]:
        require(artifact[key], f"{key} missing")
    require(not artifact["diagnostics"], "accepted artifact should not contain diagnostics")
    return artifact


def validate_negative_fixtures() -> list[dict[str, str]]:
    observed = []
    for filename, expected in NEGATIVE_FIXTURES.items():
        diagnostic = conformance_system_diagnostic(FIXTURE_DIR / filename)
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
        print(f"conformance system validation failed: {exc}", file=sys.stderr)
        return 1

    if args.artifact_out:
        atomic_write_json(args.artifact_out, artifact)

    print(
        "conformance system validation passed: "
        f"{artifact['coverage_summary']['documents']} documents, "
        f"{len(diagnostics)} rejected fixtures"
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
