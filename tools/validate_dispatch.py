#!/usr/bin/env python3
"""Validate the Phase 01 L8 dispatch analyzer."""

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

from gravity.dispatch import analyze_dispatch, analyze_dispatch_diagnostic  # noqa: E402


FIXTURE_DIR = ROOT / "docs/artifacts/phase-01/fixtures/l8"
ACCEPTED = FIXTURE_DIR / "accepted-dispatch.gravity"
NEGATIVE_FIXTURES = {
    "rejected-protocol-method.gravity": "L8-PROTOCOL-METHOD",
    "rejected-dispatch-ambiguous.gravity": "L8-DISPATCH-AMBIGUOUS",
    "rejected-dispatch-missing.gravity": "L8-DISPATCH-MISSING",
    "rejected-dynamic-forbidden.gravity": "L8-DYNAMIC-FORBIDDEN",
    "rejected-method-effect.gravity": "L8-METHOD-EFFECT",
    "rejected-host-dispatch.gravity": "L8-HOST-DISPATCH",
    "rejected-tool-dispatch.gravity": "L8-TOOL-DISPATCH",
}


def require(condition: bool, message: str) -> None:
    if not condition:
        raise AssertionError(message)


def validate_accepted() -> dict:
    artifact = analyze_dispatch(ACCEPTED.read_text(encoding="utf-8"), str(ACCEPTED.relative_to(ROOT)))
    require(artifact["kind"] == "dispatch-analysis-artifact", "artifact kind mismatch")
    require(artifact["protocol_table"], "protocol table missing")
    require(artifact["implementation_table"], "implementation table missing")
    require(artifact["method_signature_records"], "method signatures missing")
    require(artifact["dispatch_mode_records"], "dispatch records missing")
    require(artifact["interface_lowering_artifacts"], "interface artifacts missing")
    return artifact


def validate_negative_fixtures() -> list[dict[str, str]]:
    diagnostics = []
    for filename, expected in NEGATIVE_FIXTURES.items():
        path = FIXTURE_DIR / filename
        diagnostic = analyze_dispatch_diagnostic(path.read_text(encoding="utf-8"), str(path.relative_to(ROOT)))
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
        print(f"dispatch validation failed: {exc}", file=sys.stderr)
        return 1

    if args.artifact_out:
        atomic_write_json(args.artifact_out, artifact)

    print(f"dispatch validation passed: {len(artifact['protocol_table'])} protocols, {len(diagnostics)} rejected fixtures")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
