#!/usr/bin/env python3
"""Validate document-specific coverage for Phase 02 SAFE documents."""

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

from gravity.safety_document_coverage import validate_phase02_document_coverage_file  # noqa: E402


DEFAULT_ACCEPTED = ROOT / "docs/artifacts/phase-02/fixtures/document-coverage/accepted-safe-document-coverage.json"


def require(condition: bool, message: str) -> None:
    if not condition:
        raise AssertionError(message)


def validate_accepted(path: Path) -> dict:
    artifact = validate_phase02_document_coverage_file(path)
    require(artifact["kind"] == "phase02-safety-document-coverage-artifact", "artifact kind mismatch")
    require(len(artifact["documents"]) == 16, "expected SAFE1-SAFE16 coverage")
    require(len(artifact["accepted"]) == 16, "expected one accepted artifact per SAFE document")
    require(len(artifact["rejected"]) == 16, "expected one rejected diagnostic per SAFE document")
    require(artifact["coverage_summary"]["status"] == ":passed", "coverage did not pass")
    require(not artifact["diagnostics"], "accepted coverage should not contain diagnostics")
    return artifact


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--accepted", type=Path, default=DEFAULT_ACCEPTED)
    parser.add_argument("--artifact-out", type=Path)
    args = parser.parse_args()

    try:
        artifact = validate_accepted(args.accepted)
    except AssertionError as exc:
        print(f"Phase 02 document coverage validation failed: {exc}", file=sys.stderr)
        return 1

    if args.artifact_out:
        atomic_write_json(args.artifact_out, artifact)

    print("Phase 02 document coverage validation passed: 16 accepted artifacts, 16 rejected diagnostics")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
