#!/usr/bin/env python3
"""Validate the Phase 16 standard-library contract."""

from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / "src"))

from gravity.standard_library import standard_library_diagnostic, validate_standard_library_file  # noqa: E402


FIXTURE_DIR = ROOT / "docs/artifacts/phase-16/fixtures/standard-library"
DEFAULT_ACCEPTED = FIXTURE_DIR / "accepted-standard-library.json"
NEGATIVE_FIXTURES = {
    "rejected-std1-profile-metadata.json": "STD1001",
    "rejected-std2-host-state.json": "STD2002",
    "rejected-std3-allocation.json": "STD3001",
    "rejected-std4-text-boundary.json": "STD4002",
    "rejected-std5-certificate.json": "STD5003",
    "rejected-std6-borrow.json": "STD6002",
    "rejected-std7-race.json": "STD7001",
    "rejected-std8-capability.json": "STD8001",
    "rejected-std9-capability.json": "STD9001",
    "rejected-std10-validation.json": "STD10001",
    "rejected-std11-query.json": "STD11002",
    "rejected-std12-replay.json": "STD12001",
    "rejected-std13-ai-metadata.json": "STD13001",
    "rejected-std14-test-effect.json": "STD14001",
    "rejected-std15-generated-code.json": "STD15002",
    "rejected-std16-target-host.json": "STD16002",
    "rejected-std17-hardware-capability.json": "STD17001",
    "rejected-std18-algorithm.json": "STD18001",
    "rejected-std19-component-metadata.json": "STD19001",
    "rejected-std20-stability.json": "STD20001",
}


def require(condition: bool, message: str) -> None:
    if not condition:
        raise AssertionError(message)


def validate_accepted(path: Path) -> dict:
    artifact = validate_standard_library_file(path)
    require(artifact["kind"] == "standard-library-artifact", "artifact kind mismatch")
    require(artifact["coverage_summary"]["documents"] == 20, "expected STD1-STD20 coverage")
    for key in [
        "library_module_manifest",
        "api_stability_record",
        "safe_wrapper_audit",
        "library_conformance_fixture",
        "profile_support_matrix",
        "document_contracts",
    ]:
        require(artifact[key], f"{key} missing")
    require(not artifact["diagnostics"], "accepted artifact should not contain diagnostics")
    return artifact


def validate_negative_fixtures() -> list[dict[str, str]]:
    observed = []
    for filename, expected in NEGATIVE_FIXTURES.items():
        diagnostic = standard_library_diagnostic(FIXTURE_DIR / filename)
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
        print(f"standard library validation failed: {exc}", file=sys.stderr)
        return 1

    if args.artifact_out:
        args.artifact_out.parent.mkdir(parents=True, exist_ok=True)
        args.artifact_out.write_text(json.dumps(artifact, indent=2, sort_keys=True) + "\n", encoding="utf-8")

    print(
        "standard library validation passed: "
        f"{artifact['coverage_summary']['documents']} documents, "
        f"{len(diagnostics)} rejected fixtures"
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
