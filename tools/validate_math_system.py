#!/usr/bin/env python3
"""Validate the Phase 05 math system contract and fixtures."""

from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / "src"))

from gravity.math_system import math_diagnostic, validate_math_file  # noqa: E402


FIXTURE_DIR = ROOT / "docs/artifacts/phase-05/fixtures/math"
DEFAULT_ACCEPTED = FIXTURE_DIR / "accepted-math-system.json"
NEGATIVE_FIXTURES = {
    "rejected-math1-narrow.json": "MATH1-NARROW",
    "rejected-math2-provider.json": "MATH2-PROVIDER",
    "rejected-math3-branch.json": "MATH3-BRANCH",
    "rejected-math4-candidate.json": "MATH4-CANDIDATE",
    "rejected-math5-error.json": "MATH5-APPROX-ERROR",
    "rejected-math6-unresolved.json": "MATH6-UNRESOLVED",
    "rejected-math7-target-default.json": "MATH7-TARGET-DEFAULT",
    "rejected-math8-fma.json": "MATH8-FMA",
    "rejected-math9-equality.json": "MATH9-EQUALITY",
    "rejected-math10-proof.json": "MATH10-PROOF",
    "rejected-math11-diagnostic.json": "MATH11-DIAGNOSTIC",
}


def require(condition: bool, message: str) -> None:
    if not condition:
        raise AssertionError(message)


def validate_accepted(path: Path) -> dict:
    artifact = validate_math_file(path)
    require(artifact["kind"] == "math-system-artifact", "artifact kind mismatch")
    for key in [
        "numeric_kind_lattice",
        "conversion_rule_table",
        "profile_support_matrix",
        "numeric_mode_table",
        "elementary_function_registry",
        "provider_eligibility_reports",
        "efir_graphs",
        "eml_traces",
        "approximation_certificates",
        "interval_proof_artifacts",
        "floating_manifests",
        "rewrite_rule_registry",
        "rewrite_trace_reports",
        "elementary_optimization_decisions",
        "math_conformance_report",
        "domain_ir_registration",
    ]:
        require(artifact[key], f"{key} missing")
    require(not artifact["diagnostics"], "accepted artifact should not contain diagnostics")
    return artifact


def validate_negative_fixtures() -> list[dict[str, str]]:
    observed = []
    for filename, expected in NEGATIVE_FIXTURES.items():
        path = FIXTURE_DIR / filename
        diagnostic = math_diagnostic(path)
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
        print(f"math validation failed: {exc}", file=sys.stderr)
        return 1

    if args.artifact_out:
        args.artifact_out.parent.mkdir(parents=True, exist_ok=True)
        args.artifact_out.write_text(json.dumps(artifact, indent=2, sort_keys=True) + "\n", encoding="utf-8")

    print(
        "math validation passed: "
        f"{len(artifact['efir_graphs'])} EFIR graph, "
        f"{len(artifact['eml_traces'])} EML trace, "
        f"{len(diagnostics)} rejected fixtures"
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
