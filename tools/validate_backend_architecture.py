#!/usr/bin/env python3
"""Validate the Phase 07 backend architecture contract and fixtures."""

from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / "src"))

from gravity.backend_architecture import backend_diagnostic, validate_backend_file  # noqa: E402


FIXTURE_DIR = ROOT / "docs/artifacts/phase-07/fixtures/backend"
DEFAULT_ACCEPTED = FIXTURE_DIR / "accepted-backend-architecture.json"
NEGATIVE_FIXTURES = {
    "rejected-b1-input.json": "B1-INPUT",
    "rejected-b2-ub.json": "B2-UB",
    "rejected-b3-metadata.json": "B3-METADATA",
    "rejected-b4-import.json": "B4-IMPORT",
    "rejected-b5-null.json": "B5-NULL",
    "rejected-b6-global.json": "B6-GLOBAL",
    "rejected-b7-metadata.json": "B7-METADATA",
    "rejected-b8-host-effect.json": "B8-HOST-EFFECT",
    "rejected-b9-width.json": "B9-WIDTH",
    "rejected-b10-replay.json": "B10-REPLAY",
    "rejected-b11-taint.json": "B11-TAINT",
    "rejected-b12-permission.json": "B12-PERMISSION",
    "rejected-b13-evidence.json": "B13-EVIDENCE",
    "rejected-b14-coverage.json": "B14-COVERAGE",
}


def require(condition: bool, message: str) -> None:
    if not condition:
        raise AssertionError(message)


def validate_accepted(path: Path) -> dict:
    artifact = validate_backend_file(path)
    require(artifact["kind"] == "backend-architecture-artifact", "artifact kind mismatch")
    for key in [
        "backend_interface",
        "target_lowering_manifest",
        "concrete_backend_manifests",
        "backend_artifact_records",
        "abi_layout_report",
        "artifact_emission",
        "backend_conformance_report",
    ]:
        require(artifact[key], f"{key} missing")
    require(len(artifact["concrete_backend_manifests"]) == 11, "expected eleven concrete backend manifests")
    require(len(artifact["backend_artifact_records"]) >= 11, "expected backend artifact records")
    require(not artifact["diagnostics"], "accepted artifact should not contain diagnostics")
    return artifact


def validate_negative_fixtures() -> list[dict[str, str]]:
    observed = []
    for filename, expected in NEGATIVE_FIXTURES.items():
        path = FIXTURE_DIR / filename
        diagnostic = backend_diagnostic(path)
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
        print(f"backend architecture validation failed: {exc}", file=sys.stderr)
        return 1

    if args.artifact_out:
        args.artifact_out.parent.mkdir(parents=True, exist_ok=True)
        args.artifact_out.write_text(json.dumps(artifact, indent=2, sort_keys=True) + "\n", encoding="utf-8")

    print(
        "backend architecture validation passed: "
        f"{len(artifact['concrete_backend_manifests'])} backend manifests, "
        f"{len(diagnostics)} rejected fixtures"
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
