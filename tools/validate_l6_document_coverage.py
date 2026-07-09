#!/usr/bin/env python3
"""Validate document-specific coverage for L6 effect system rules."""

from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / "src"))

from gravity.typed_core import check_source_diagnostic, check_source_to_typed_core_artifact  # noqa: E402


TYPED_FIXTURE_DIR = ROOT / "docs/artifacts/phase-01/fixtures/typed"
L6_FIXTURE_DIR = ROOT / "docs/artifacts/phase-01/fixtures/l6"
ACCEPTED = TYPED_FIXTURE_DIR / "accepted-typed-effected-core.gravity"
NEGATIVE_FIXTURES = {
    "../typed/rejected-effect-undeclared.gravity": "L6-EFFECT-UNDECLARED",
    "rejected-effect-profile.gravity": "L6-EFFECT-PROFILE",
    "rejected-build-effect.gravity": "L6-BUILD-EFFECT",
    "rejected-replay-effect.gravity": "L6-REPLAY-EFFECT",
    "rejected-effect-unknown.gravity": "L6-EFFECT-UNKNOWN",
}


def require(condition: bool, message: str) -> None:
    if not condition:
        raise AssertionError(message)


def validate_accepted() -> dict:
    artifact = check_source_to_typed_core_artifact(ACCEPTED.read_text(encoding="utf-8"), str(ACCEPTED.relative_to(ROOT)))
    require(artifact["effect_environment"], "effect environment missing")
    require(artifact["namespace_effect_summary"]["inferred"], "namespace effect summary missing")
    require(artifact["module_effect_summary"]["escaping_effects"], "module effect summary missing")
    require(artifact["effect_legality_report"], "effect legality report missing")
    return artifact


def validate_negative_fixtures() -> list[dict[str, str]]:
    diagnostics = []
    for filename, expected in NEGATIVE_FIXTURES.items():
        path = (L6_FIXTURE_DIR / filename).resolve()
        diagnostic = check_source_diagnostic(path.read_text(encoding="utf-8"), str(path.relative_to(ROOT)))
        require(diagnostic is not None, f"{filename} did not produce a diagnostic")
        require(diagnostic["id"] == expected, f"{filename} produced {diagnostic['id']} instead of {expected}")
        diagnostics.append({"fixture": str(path.relative_to(ROOT)), "diagnostic": diagnostic["id"]})
    return diagnostics


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--artifact-out", type=Path)
    args = parser.parse_args()

    try:
        effect_artifact = validate_accepted()
        diagnostics = validate_negative_fixtures()
    except AssertionError as exc:
        print(f"L6 document coverage validation failed: {exc}", file=sys.stderr)
        return 1

    artifact = {
        "kind": "l6-document-coverage",
        "document": "L6",
        "accepted": [
            {
                "fixture": str(ACCEPTED.relative_to(ROOT)),
                "artifact_kind": effect_artifact["kind"],
                "coverage": ["effect environment", "namespace effect summary", "module effect summary", "effect legality report"],
            }
        ],
        "rejected": diagnostics,
    }
    if args.artifact_out:
        args.artifact_out.parent.mkdir(parents=True, exist_ok=True)
        args.artifact_out.write_text(json.dumps(artifact, indent=2, sort_keys=True) + "\n", encoding="utf-8")

    print("L6 document coverage validation passed: 1 accepted artifact, 5 rejected diagnostics")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
