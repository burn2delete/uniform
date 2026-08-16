#!/usr/bin/env python3
"""Validate Phase 03 profile manifests, compatibility, and compliance fixtures."""

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

from gravity.profiles import profile_diagnostic, validate_profile_file  # noqa: E402


FIXTURE_DIR = ROOT / "docs/artifacts/phase-03/fixtures/profiles"
DEFAULT_ACCEPTED = FIXTURE_DIR / "accepted-profile-system.json"
NEGATIVE_FIXTURES = {
    "rejected-p1-missing-profile.json": "P1-MISSING-PROFILE",
    "rejected-p2-core-effect.json": "P2-EFFECT",
    "rejected-p3-build-effect.json": "P3-BUILD-EFFECT",
    "rejected-p4-raw-memory.json": "P4-RAW-MEMORY",
    "rejected-p5-hidden-alloc.json": "P5-ALLOC",
    "rejected-p6-unbounded-alloc.json": "P6-ALLOC",
    "rejected-p7-hidden-alloc.json": "P7-HIDDEN-ALLOC",
    "rejected-p8-tag-loss.json": "P8-TAG",
    "rejected-p9-replay.json": "P9-REPLAY",
    "rejected-p10-prompt.json": "P10-PROMPT",
    "rejected-p11-host-effect.json": "P11-HOST-EFFECT",
    "rejected-p12-nondeterminism.json": "P12-NONDETERMINISM",
    "rejected-p13-direct-import.json": "P13-DIRECT",
}


def require(condition: bool, message: str) -> None:
    if not condition:
        raise AssertionError(message)


def validate_accepted(path: Path) -> dict:
    artifact = validate_profile_file(path)
    require(artifact["kind"] == "profile-system-artifact", "artifact kind mismatch")
    require(len(artifact["profile_manifests"]) == 11, "expected eleven standard profile manifests")
    for key in [
        "effect_permission_table",
        "capability_permission_table",
        "memory_regime_records",
        "runtime_assumption_records",
        "cross_profile_dependency_graph",
        "profile_compatibility_matrix",
        "backend_eligibility_reports",
        "profile_conformance_fixture_results",
    ]:
        require(artifact[key], f"{key} missing")
    require(not artifact["diagnostics"], "accepted artifact should not contain diagnostics")
    return artifact


def validate_negative_fixtures() -> list[dict[str, str]]:
    observed = []
    for filename, expected in NEGATIVE_FIXTURES.items():
        path = FIXTURE_DIR / filename
        diagnostic = profile_diagnostic(path)
        require(diagnostic is not None, f"{filename} did not produce a diagnostic")
        require(diagnostic["id"] == expected, f"{filename} produced {diagnostic['id']} instead of {expected}")
        for key in ["active_profile", "source_span", "missing_fact", "remediation"]:
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
        print(f"profile validation failed: {exc}", file=sys.stderr)
        return 1

    if args.artifact_out:
        atomic_write_json(args.artifact_out, artifact)

    print(
        "profile validation passed: "
        f"{len(artifact['profile_manifests'])} profile manifests, {len(diagnostics)} rejected fixtures"
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
