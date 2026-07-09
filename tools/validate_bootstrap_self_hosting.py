#!/usr/bin/env python3
"""Validate the Phase 15 bootstrap and self-hosting contract."""

from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / "src"))

from gravity.bootstrap_self_hosting import (  # noqa: E402
    bootstrap_self_hosting_diagnostic,
    validate_bootstrap_self_hosting_file,
)


FIXTURE_DIR = ROOT / "docs/artifacts/phase-15/fixtures/bootstrap"
DEFAULT_ACCEPTED = FIXTURE_DIR / "accepted-bootstrap-self-hosting.json"
NEGATIVE_FIXTURES = {
    "rejected-boot1-stage-evidence.json": "BOOT1001",
    "rejected-boot2-profile.json": "BOOT2002",
    "rejected-boot3-ambient-authority.json": "BOOT3002",
    "rejected-boot4-preserved-fact.json": "BOOT4003",
    "rejected-boot5-conformance-link.json": "BOOT5003",
    "rejected-boot6-environment.json": "BOOT6001",
    "rejected-boot7-compiler-identity.json": "BOOT7001",
    "rejected-boot8-lineage.json": "BOOT8002",
}


def require(condition: bool, message: str) -> None:
    if not condition:
        raise AssertionError(message)


def validate_accepted(path: Path) -> dict:
    artifact = validate_bootstrap_self_hosting_file(path)
    require(artifact["kind"] == "bootstrap-self-hosting-artifact", "artifact kind mismatch")
    require(artifact["coverage_summary"]["documents"] == 8, "expected BOOT1-BOOT8 coverage")
    for key in [
        "bootstrap_stage_matrix",
        "seed_compiler_manifest",
        "self_hosted_component_manifest",
        "compiler_coding_standard_report",
        "stage_compatibility_matrix",
        "trusting_trust_report",
        "equivalence_report",
        "bootstrap_provenance_record",
        "document_contracts",
    ]:
        require(artifact[key], f"{key} missing")
    require(not artifact["diagnostics"], "accepted artifact should not contain diagnostics")
    return artifact


def validate_negative_fixtures() -> list[dict[str, str]]:
    observed = []
    for filename, expected in NEGATIVE_FIXTURES.items():
        diagnostic = bootstrap_self_hosting_diagnostic(FIXTURE_DIR / filename)
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
        print(f"bootstrap self-hosting validation failed: {exc}", file=sys.stderr)
        return 1

    if args.artifact_out:
        args.artifact_out.parent.mkdir(parents=True, exist_ok=True)
        args.artifact_out.write_text(json.dumps(artifact, indent=2, sort_keys=True) + "\n", encoding="utf-8")

    print(
        "bootstrap self-hosting validation passed: "
        f"{artifact['coverage_summary']['documents']} documents, "
        f"{len(diagnostics)} rejected fixtures"
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
