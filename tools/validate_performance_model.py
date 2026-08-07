#!/usr/bin/env python3
"""Validate the Phase 04 performance model contract and fixtures."""

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

from gravity.performance import performance_diagnostic, validate_performance_file  # noqa: E402


FIXTURE_DIR = ROOT / "docs/artifacts/phase-04/fixtures/performance"
DEFAULT_ACCEPTED = FIXTURE_DIR / "accepted-performance-model.json"
NEGATIVE_FIXTURES = {
    "rejected-perf1-claim.json": "PERF1-CLAIM",
    "rejected-perf2-residual.json": "PERF2-RESIDUAL",
    "rejected-perf3-key.json": "PERF3-KEY",
    "rejected-perf4-abi.json": "PERF4-ABI",
    "rejected-perf5-safety-gate.json": "PERF5-SAFETY-GATE",
    "rejected-perf6-stale.json": "PERF6-STALE",
    "rejected-perf7-fallback.json": "PERF7-FALLBACK",
    "rejected-perf8-lane.json": "PERF8-LANE",
    "rejected-perf9-loop.json": "PERF9-LOOP",
    "rejected-perf10-proof.json": "PERF10-PROOF-MISSING",
}


def require(condition: bool, message: str) -> None:
    if not condition:
        raise AssertionError(message)


def validate_accepted(path: Path) -> dict:
    artifact = validate_performance_file(path)
    require(artifact["kind"] == "performance-model-artifact", "artifact kind mismatch")
    for key in [
        "performance_contract_manifest",
        "optimization_decision_log",
        "performance_claim_records",
        "target_feature_reports",
        "layout_records",
        "benchmark_reports",
        "proof_index",
        "generated_variant_manifest",
        "zero_cost_conformance_results",
        "specialization_records",
        "pgo_decision_log",
        "autotuning_selection_certificates",
        "simd_cache_records",
        "realtime_latency_reports",
        "check_elision_certificates",
        "conformance_matrix",
    ]:
        require(artifact[key], f"{key} missing")
    require(len(artifact["check_elision_certificates"]) >= 15, "expected check-elision evidence for policy and non-policy classes")
    require(not artifact["diagnostics"], "accepted artifact should not contain diagnostics")
    return artifact


def validate_negative_fixtures() -> list[dict[str, str]]:
    observed = []
    for filename, expected in NEGATIVE_FIXTURES.items():
        path = FIXTURE_DIR / filename
        diagnostic = performance_diagnostic(path)
        require(diagnostic is not None, f"{filename} did not produce a diagnostic")
        require(diagnostic["id"] == expected, f"{filename} produced {diagnostic['id']} instead of {expected}")
        for key in ["source_span", "missing_evidence", "remediation", "analyzer_stage"]:
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
        print(f"performance validation failed: {exc}", file=sys.stderr)
        return 1

    if args.artifact_out:
        atomic_write_json(args.artifact_out, artifact)

    print(
        "performance validation passed: "
        f"{len(artifact['performance_claim_records'])} claim, "
        f"{len(artifact['optimization_decision_log'])} optimization decisions, "
        f"{len(diagnostics)} rejected fixtures"
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
