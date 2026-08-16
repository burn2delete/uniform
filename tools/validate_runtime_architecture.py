#!/usr/bin/env python3
"""Validate the Phase 08 runtime architecture contract and fixtures."""

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

from gravity.runtime_architecture import runtime_diagnostic, validate_runtime_file  # noqa: E402


FIXTURE_DIR = ROOT / "docs/artifacts/phase-08/fixtures/runtime"
DEFAULT_ACCEPTED = FIXTURE_DIR / "accepted-runtime-architecture.json"
NEGATIVE_FIXTURES = {
    "rejected-r1-forbidden.json": "R1-FORBIDDEN",
    "rejected-r2-hidden-service.json": "R2-HIDDEN-SERVICE",
    "rejected-r3-capability.json": "R3-CAPABILITY",
    "rejected-r4-null.json": "R4-NULL",
    "rejected-r5-lifetime.json": "R5-LIFETIME",
    "rejected-r6-race.json": "R6-RACE",
    "rejected-r7-replay.json": "R7-REPLAY",
    "rejected-r8-tool.json": "R8-TOOL",
    "rejected-r9-checks.json": "R9-CHECKS",
    "rejected-r10-pointer.json": "R10-POINTER",
    "rejected-r11-ambient.json": "R11-AMBIENT",
    "rejected-r12-secret.json": "R12-SECRET",
}


def require(condition: bool, message: str) -> None:
    if not condition:
        raise AssertionError(message)


def validate_accepted(path: Path) -> dict:
    artifact = validate_runtime_file(path)
    require(artifact["kind"] == "runtime-architecture-artifact", "artifact kind mismatch")
    for key in [
        "runtime_manifest",
        "runtime_family_selection",
        "runtime_service_table",
        "no_runtime_manifest",
        "minimal_native_runtime",
        "managed_runtime",
        "memory_runtime",
        "concurrency_runtime",
        "distributed_runtime",
        "ai_runtime",
        "repl_runtime",
        "ffi_runtime",
        "capability_enforcement_report",
        "observability_event_schema",
        "runtime_conformance_report",
    ]:
        require(artifact[key], f"{key} missing")
    require(not artifact["diagnostics"], "accepted artifact should not contain diagnostics")
    return artifact


def validate_negative_fixtures() -> list[dict[str, str]]:
    observed = []
    for filename, expected in NEGATIVE_FIXTURES.items():
        path = FIXTURE_DIR / filename
        diagnostic = runtime_diagnostic(path)
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
        print(f"runtime architecture validation failed: {exc}", file=sys.stderr)
        return 1

    if args.artifact_out:
        atomic_write_json(args.artifact_out, artifact)

    print(
        "runtime architecture validation passed: "
        f"{len(artifact['runtime_family_selection']['families'])} runtime families, "
        f"{len(diagnostics)} rejected fixtures"
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
