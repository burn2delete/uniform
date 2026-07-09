#!/usr/bin/env python3
"""Validate the Phase 02 P02-T01 SAFE1 safety outcome classifier."""

from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / "src"))

from gravity.safety import ALLOWED_OUTCOMES, analyze_safety_file, safety_manifest_diagnostic  # noqa: E402


FIXTURE_DIR = ROOT / "docs/artifacts/phase-02/fixtures/safety"
DEFAULT_ACCEPTED = FIXTURE_DIR / "accepted-safety-classification.json"
NEGATIVE_FIXTURES = {
    "rejected-no-outcome.json": "SAFE1-NO-OUTCOME",
    "rejected-ambiguous-outcome.json": "D8-UNCLASSIFIED-DANGER",
    "rejected-unknown-outcome.json": "D8-UNCLASSIFIED-DANGER",
    "rejected-proof-missing.json": "SAFE1-PROOF-MISSING",
    "rejected-runtime-check-missing.json": "SAFE1-CHECK-MISSING",
    "rejected-runtime-check-profile.json": "SAFE1-CHECK-ILLEGAL",
    "rejected-unsafe-mode-policy.json": "SAFE1-UNSAFE-POLICY",
    "rejected-unsafe-metadata.json": "SAFE1-UNSAFE-METADATA",
    "rejected-generated-provenance.json": "SAFE1-GENERATED-PROVENANCE",
    "rejected-optimization-proof.json": "SAFE1-OPTIMIZATION-PROOF",
    "rejected-dependency-mode.json": "SAFE1-DEPENDENCY-MODE",
}


def require(condition: bool, message: str) -> None:
    if not condition:
        raise AssertionError(message)


def validate_accepted(path: Path) -> dict:
    artifact = analyze_safety_file(path)
    require(artifact["kind"] == "safety-classification-artifact", "artifact kind mismatch")
    require(artifact["document"] == "SAFE1", "document mismatch")
    require(artifact["profile"] == ":native", "profile mismatch")
    require(artifact["safety_mode"] == ":audited-unsafe", "safety mode mismatch")
    require(not artifact["diagnostics"], "accepted artifact should not contain diagnostics")
    observed = {operation["outcome"] for operation in artifact["operations"]}
    require(observed == ALLOWED_OUTCOMES, f"expected all four outcomes, saw {sorted(observed)}")
    require(len(artifact["operations"]) == 4, "accepted fixture should classify four operations")
    require(len(artifact["runtime_checks"]) == 1, "runtime check record missing")
    require(len(artifact["unsafe_island_audit_records"]) == 1, "unsafe island audit record missing")
    require(len(artifact["rejection_diagnostics"]) == 1, "rejection diagnostic missing")
    require(artifact["proof_references"], "proof references missing")
    require(artifact["generated_code_safety_provenance"], "generated-code provenance missing")
    require(artifact["optimization_check_erasure_justifications"], "optimization proof record missing")
    require(artifact["safety_certificate_inputs"], "SAFE15 certificate inputs missing")
    for operation in artifact["operations"]:
        for key in ["source_span", "active_profile", "type_context", "effect_context", "capability_context", "profile_facts"]:
            require(operation.get(key), f"{operation['id']} missing {key}")
    return artifact


def validate_negative_fixtures() -> list[dict[str, str]]:
    observed = []
    for filename, expected in NEGATIVE_FIXTURES.items():
        path = FIXTURE_DIR / filename
        diagnostic = safety_manifest_diagnostic(path)
        require(diagnostic is not None, f"{filename} did not produce a diagnostic")
        require(diagnostic["id"] == expected, f"{filename} produced {diagnostic['id']} instead of {expected}")
        for key in ["span", "active_profile", "safety_mode", "missing_fact", "remediation", "analyzer_stage"]:
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
        print(f"safety classifier validation failed: {exc}", file=sys.stderr)
        return 1

    if args.artifact_out:
        args.artifact_out.parent.mkdir(parents=True, exist_ok=True)
        args.artifact_out.write_text(json.dumps(artifact, indent=2, sort_keys=True) + "\n", encoding="utf-8")

    print(
        "safety classifier validation passed: "
        f"{len(artifact['operations'])} classified operations, {len(diagnostics)} rejected fixtures"
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
