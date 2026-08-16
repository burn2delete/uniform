#!/usr/bin/env python3
"""Validate Phase 10 document-specific schema coverage."""

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

from gravity.schema_document_coverage import validate_phase10_document_coverage_file  # noqa: E402


DEFAULT_ACCEPTED = ROOT / "docs/artifacts/phase-10/fixtures/document-coverage/accepted-schema-document-coverage.json"


def require(condition: bool, message: str) -> None:
    if not condition:
        raise AssertionError(message)


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--accepted", type=Path, default=DEFAULT_ACCEPTED)
    parser.add_argument("--artifact-out", type=Path)
    args = parser.parse_args()

    try:
        artifact = validate_phase10_document_coverage_file(args.accepted)
        require(artifact["kind"] == "phase10-schema-document-coverage-artifact", "artifact kind mismatch")
        require(artifact["coverage_summary"]["documents"] == 9, "expected S1-S9 coverage")
        require(artifact["coverage_summary"]["accepted_artifacts"] == 9, "expected nine accepted records")
        require(artifact["coverage_summary"]["rejected_diagnostics"] == 9, "expected nine rejected diagnostics")
        require(not artifact["diagnostics"], "accepted artifact should not contain diagnostics")
    except AssertionError as exc:
        print(f"Phase 10 document coverage validation failed: {exc}", file=sys.stderr)
        return 1

    if args.artifact_out:
        atomic_write_json(args.artifact_out, artifact)

    print(
        "Phase 10 document coverage validation passed: "
        f"{artifact['coverage_summary']['accepted_artifacts']} accepted artifacts, "
        f"{artifact['coverage_summary']['rejected_diagnostics']} rejected diagnostics"
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
