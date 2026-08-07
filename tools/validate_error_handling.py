#!/usr/bin/env python3
"""Validate the Phase 01 L9 error handling analyzer."""

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

from gravity.errors import analyze_errors, analyze_errors_diagnostic  # noqa: E402


FIXTURE_DIR = ROOT / "docs/artifacts/phase-01/fixtures/l9"
ACCEPTED = FIXTURE_DIR / "accepted-error-handling.gravity"
NEGATIVE_FIXTURES = {
    "rejected-throw-effect.gravity": "L9-THROW-EFFECT",
    "rejected-unhandled.gravity": "L9-UNHANDLED",
    "rejected-panic-profile.gravity": "L9-PANIC-PROFILE",
    "rejected-host-error.gravity": "L9-HOST-ERROR",
    "rejected-ffi-error.gravity": "L9-FFI-ERROR",
    "rejected-workflow-error.gravity": "L9-WORKFLOW-ERROR",
    "rejected-ai-error.gravity": "L9-AI-ERROR",
}


def require(condition: bool, message: str) -> None:
    if not condition:
        raise AssertionError(message)


def validate_accepted() -> dict:
    artifact = analyze_errors(ACCEPTED.read_text(encoding="utf-8"), str(ACCEPTED.relative_to(ROOT)))
    require(artifact["kind"] == "error-handling-artifact", "artifact kind mismatch")
    require(artifact["error_type_declarations"], "error type declarations missing")
    require(artifact["function_thrown_error_effect_records"], "throw records missing")
    require(artifact["panic_lowering_records"], "panic records missing")
    require(artifact["safety_check_failure_records"], "safety check records missing")
    return artifact


def validate_negative_fixtures() -> list[dict[str, str]]:
    diagnostics = []
    for filename, expected in NEGATIVE_FIXTURES.items():
        path = FIXTURE_DIR / filename
        diagnostic = analyze_errors_diagnostic(path.read_text(encoding="utf-8"), str(path.relative_to(ROOT)))
        require(diagnostic is not None, f"{filename} did not produce a diagnostic")
        require(diagnostic["id"] == expected, f"{filename} produced {diagnostic['id']} instead of {expected}")
        diagnostics.append({"fixture": str(path.relative_to(ROOT)), "diagnostic": diagnostic["id"]})
    return diagnostics


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--artifact-out", type=Path)
    args = parser.parse_args()

    try:
        artifact = validate_accepted()
        diagnostics = validate_negative_fixtures()
    except AssertionError as exc:
        print(f"error handling validation failed: {exc}", file=sys.stderr)
        return 1

    if args.artifact_out:
        atomic_write_json(args.artifact_out, artifact)

    print(f"error handling validation passed: {len(artifact['function_thrown_error_effect_records'])} throw records, {len(diagnostics)} rejected fixtures")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
