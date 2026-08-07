#!/usr/bin/env python3
"""Validate the Phase 02 P02-T02 memory safety checker."""

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

from gravity.memory_safety import analyze_memory_safety_file, memory_safety_diagnostic  # noqa: E402


FIXTURE_DIR = ROOT / "docs/artifacts/phase-02/fixtures/memory-safety"
DEFAULT_ACCEPTED = FIXTURE_DIR / "accepted-memory-safety.json"
NEGATIVE_FIXTURES = {
    "rejected-uninit.json": "SAFE2-UNINIT",
    "rejected-bounds.json": "SAFE2-BOUNDS",
    "rejected-lifetime.json": "SAFE2-LIFETIME",
    "rejected-profile-regime.json": "SAFE2-PROFILE",
    "rejected-raw-outside-unsafe.json": "SAFE2-RAW",
    "rejected-check-erase.json": "SAFE2-CHECK-ERASE",
    "rejected-use-after-move.json": "SAFE3-USE-AFTER-MOVE",
    "rejected-mut-alias.json": "SAFE3-MUT-ALIAS",
    "rejected-region-escape.json": "SAFE4-REGION-ESCAPE",
    "rejected-post-reset.json": "SAFE4-POST-RESET",
    "rejected-provider-contract.json": "SAFE4-PROVIDER",
    "rejected-linear-leak.json": "SAFE5-LEAK",
    "rejected-linear-double-close.json": "SAFE5-DOUBLE-CLOSE",
    "rejected-linear-wrong-provider.json": "SAFE5-WRONG-PROVIDER",
    "rejected-linear-branch.json": "SAFE5-BRANCH",
}


def require(condition: bool, message: str) -> None:
    if not condition:
        raise AssertionError(message)


def validate_accepted(path: Path) -> dict:
    artifact = analyze_memory_safety_file(path)
    require(artifact["kind"] == "memory-safety-artifact", "artifact kind mismatch")
    require(artifact["documents"] == ["SAFE2", "SAFE3", "SAFE4", "SAFE5"], "document list mismatch")
    require(not artifact["diagnostics"], "accepted artifact should not contain diagnostics")
    require(len(artifact["memory_safety_facts"]) == 6, "expected six accepted memory operations")
    require(artifact["runtime_check_records"], "runtime check records missing")
    require(artifact["allocation_release_map"], "allocation/release map missing")
    require(artifact["region_lifetime_graph"], "region lifetime graph missing")
    require(artifact["arena_generation_graph"], "arena generation graph missing")
    require(artifact["ownership_graph"], "ownership graph missing")
    require(artifact["borrow_graph"], "borrow graph missing")
    require(artifact["lifetime_interval_map"], "lifetime interval map missing")
    require(artifact["linear_resource_flow_graph"], "linear resource flow graph missing")
    require(artifact["acquire_terminal_operation_records"], "terminal operation records missing")
    require(artifact["exceptional_cleanup_records"], "exceptional cleanup records missing")
    require(artifact["cancellation_cleanup_records"], "cancellation cleanup records missing")
    require(artifact["unsafe_memory_audit_records"], "unsafe memory audit records missing")
    require(artifact["proof_records"], "proof records missing")
    return artifact


def validate_negative_fixtures() -> list[dict[str, str]]:
    observed = []
    for filename, expected in NEGATIVE_FIXTURES.items():
        path = FIXTURE_DIR / filename
        diagnostic = memory_safety_diagnostic(path)
        require(diagnostic is not None, f"{filename} did not produce a diagnostic")
        require(diagnostic["id"] == expected, f"{filename} produced {diagnostic['id']} instead of {expected}")
        for key in ["span", "active_profile", "memory_regime", "provider_id", "missing_fact", "remediation", "analyzer_stage"]:
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
        print(f"memory safety validation failed: {exc}", file=sys.stderr)
        return 1

    if args.artifact_out:
        atomic_write_json(args.artifact_out, artifact)

    print(
        "memory safety validation passed: "
        f"{len(artifact['memory_safety_facts'])} accepted operations, {len(diagnostics)} rejected fixtures"
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
