#!/usr/bin/env python3
"""Validate document-specific coverage for L5 type system rules."""

from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / "src"))

from gravity.typed_core import check_source_diagnostic, check_source_to_typed_core_artifact  # noqa: E402


TYPED_FIXTURE_DIR = ROOT / "docs/artifacts/phase-01/fixtures/typed"
L5_FIXTURE_DIR = ROOT / "docs/artifacts/phase-01/fixtures/l5"
ACCEPTED = TYPED_FIXTURE_DIR / "accepted-typed-effected-core.gravity"
NEGATIVE_FIXTURES = {
    "rejected-annotation-required.gravity": "L5-ANNOTATION-REQUIRED",
    "rejected-cast-unsafe.gravity": "L5-CAST-UNSAFE",
    "rejected-uninit-read.gravity": "L5-UNINIT-READ",
    "rejected-linear-dup.gravity": "L5-LINEAR-DUP",
    "rejected-schema-weaken.gravity": "L5-SCHEMA-WEAKEN",
    "rejected-latent-effect-missing.gravity": "L5-LATENT-EFFECT-MISSING",
}


def require(condition: bool, message: str) -> None:
    if not condition:
        raise AssertionError(message)


def validate_accepted() -> dict:
    artifact = check_source_to_typed_core_artifact(ACCEPTED.read_text(encoding="utf-8"), str(ACCEPTED.relative_to(ROOT)))
    require(artifact["kind"] == "typed-effected-core-artifact", "typed artifact kind mismatch")
    require(artifact["type_facts"], "type facts missing")
    require(artifact["function_signature_table"], "function signatures missing")
    require(artifact["dynamic_boundary_records"], "dynamic boundary records missing")
    require(artifact["ownership_resource_type_facts"], "ownership/resource facts missing")
    require(artifact["function_latent_effect_table"], "latent effect table missing")
    return artifact


def validate_negative_fixtures() -> list[dict[str, str]]:
    diagnostics = []
    for filename, expected in NEGATIVE_FIXTURES.items():
        path = L5_FIXTURE_DIR / filename
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
        typed_artifact = validate_accepted()
        diagnostics = validate_negative_fixtures()
    except AssertionError as exc:
        print(f"L5 document coverage validation failed: {exc}", file=sys.stderr)
        return 1

    artifact = {
        "kind": "l5-document-coverage",
        "document": "L5",
        "accepted": [
            {
                "fixture": str(ACCEPTED.relative_to(ROOT)),
                "artifact_kind": typed_artifact["kind"],
                "coverage": ["type facts", "function signatures", "latent effects", "dynamic boundaries", "ownership/resource facts"],
            }
        ],
        "rejected": diagnostics,
    }
    if args.artifact_out:
        args.artifact_out.parent.mkdir(parents=True, exist_ok=True)
        args.artifact_out.write_text(json.dumps(artifact, indent=2, sort_keys=True) + "\n", encoding="utf-8")

    print("L5 document coverage validation passed: 1 accepted artifact, 6 rejected diagnostics")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
