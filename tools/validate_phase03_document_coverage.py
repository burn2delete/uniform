#!/usr/bin/env python3
"""Validate document-specific coverage for Phase 03 profile documents."""

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

from gravity.profile_document_coverage import validate_phase03_document_coverage_file  # noqa: E402


DEFAULT_ACCEPTED = ROOT / "docs/artifacts/phase-03/fixtures/document-coverage/accepted-profile-document-coverage.json"


def require(condition: bool, message: str) -> None:
    if not condition:
        raise AssertionError(message)


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--accepted", type=Path, default=DEFAULT_ACCEPTED)
    parser.add_argument("--artifact-out", type=Path)
    args = parser.parse_args()

    try:
        artifact = validate_phase03_document_coverage_file(args.accepted)
        require(artifact["kind"] == "phase03-profile-document-coverage-artifact", "artifact kind mismatch")
        require(len(artifact["accepted"]) == 13, "expected P1-P13 accepted coverage")
        require(len(artifact["rejected"]) == 13, "expected P1-P13 rejected diagnostics")
        require(artifact["coverage_summary"]["status"] == ":passed", "coverage did not pass")
    except AssertionError as exc:
        print(f"Phase 03 document coverage validation failed: {exc}", file=sys.stderr)
        return 1

    if args.artifact_out:
        atomic_write_json(args.artifact_out, artifact)

    print("Phase 03 document coverage validation passed: 13 accepted artifacts, 13 rejected diagnostics")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
