#!/usr/bin/env python3
"""Validate Phase 17 GOV document coverage evidence."""

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

from gravity.governance_document_coverage import validate_phase17_document_coverage_file  # noqa: E402


DEFAULT_ACCEPTED = ROOT / "docs/artifacts/phase-17/fixtures/document-coverage/accepted-governance-document-coverage.json"


def require(condition: bool, message: str) -> None:
    if not condition:
        raise AssertionError(message)


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--accepted", type=Path, default=DEFAULT_ACCEPTED)
    parser.add_argument("--artifact-out", type=Path)
    args = parser.parse_args()

    try:
        artifact = validate_phase17_document_coverage_file(args.accepted)
        require(artifact["kind"] == "phase17-governance-document-coverage-artifact", "artifact kind mismatch")
        require(artifact["coverage_summary"]["documents"] == 10, "expected GOV1-GOV10 coverage")
        require(artifact["coverage_summary"]["accepted_artifacts"] == 10, "expected 10 accepted artifacts")
        require(artifact["coverage_summary"]["rejected_diagnostics"] == 10, "expected 10 rejected diagnostics")
        require(not artifact["diagnostics"], "accepted coverage artifact should not contain diagnostics")
    except AssertionError as exc:
        print(f"Phase 17 document coverage validation failed: {exc}", file=sys.stderr)
        return 1

    if args.artifact_out:
        atomic_write_json(args.artifact_out, artifact)

    print(
        "Phase 17 document coverage validation passed: "
        f"{artifact['coverage_summary']['accepted_artifacts']} accepted artifacts, "
        f"{artifact['coverage_summary']['rejected_diagnostics']} rejected diagnostics"
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
