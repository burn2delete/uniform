#!/usr/bin/env python3
"""Validate Phase 06 document-specific compiler coverage."""

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

from gravity.compiler_document_coverage import validate_phase06_document_coverage_file  # noqa: E402


DEFAULT_ACCEPTED = ROOT / "docs/artifacts/phase-06/fixtures/document-coverage/accepted-compiler-document-coverage.json"


def require(condition: bool, message: str) -> None:
    if not condition:
        raise AssertionError(message)


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--accepted", type=Path, default=DEFAULT_ACCEPTED)
    parser.add_argument("--artifact-out", type=Path)
    args = parser.parse_args()

    try:
        artifact = validate_phase06_document_coverage_file(args.accepted)
        require(artifact["kind"] == "phase06-compiler-document-coverage-artifact", "artifact kind mismatch")
        require(artifact["coverage_summary"]["documents"] == 18, "expected C1-C18 coverage")
        require(artifact["coverage_summary"]["accepted_artifacts"] == 18, "expected eighteen accepted records")
        require(artifact["coverage_summary"]["rejected_diagnostics"] == 18, "expected eighteen rejected diagnostics")
        require(not artifact["diagnostics"], "accepted artifact should not contain diagnostics")
    except AssertionError as exc:
        print(f"Phase 06 document coverage validation failed: {exc}", file=sys.stderr)
        return 1

    if args.artifact_out:
        atomic_write_json(args.artifact_out, artifact)

    print(
        "Phase 06 document coverage validation passed: "
        f"{artifact['coverage_summary']['accepted_artifacts']} accepted artifacts, "
        f"{artifact['coverage_summary']['rejected_diagnostics']} rejected diagnostics"
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
