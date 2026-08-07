#!/usr/bin/env python3
"""Validate L16 alternative macro-system coverage."""

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

from gravity.alternative_macros import alternative_macro_manifest_diagnostic, validate_alternative_macro_manifest_file  # noqa: E402


FIXTURE_DIR = ROOT / "docs/artifacts/phase-01/fixtures/l16"
ACCEPTED = FIXTURE_DIR / "accepted-alternative-macro.json"
NEGATIVE_FIXTURES = {
    "rejected-provider.json": "L16-PROVIDER",
    "rejected-equivalence.json": "L16-EQUIVALENCE",
    "rejected-syntax-object.json": "L16-SYNTAX-OBJECT",
    "rejected-hygiene.json": "L16-HYGIENE",
    "rejected-phase.json": "L16-PHASE",
    "rejected-build-effect.json": "L16-BUILD-EFFECT",
    "rejected-hermetic.json": "L16-HERMETIC",
    "rejected-cache.json": "L16-CACHE",
    "rejected-facet.json": "L16-FACET",
    "rejected-generated.json": "L16-GENERATED",
}


def require(condition: bool, message: str) -> None:
    if not condition:
        raise AssertionError(message)


def validate_accepted() -> dict:
    artifact = validate_alternative_macro_manifest_file(ACCEPTED)
    require(artifact["kind"] == "alternative-macro-provider-artifact", "artifact kind mismatch")
    require(artifact["provider_declaration"]["kind"] == ":macro-system", "macro provider declaration missing")
    require(artifact["expansion_trace"], "expansion trace missing")
    require(artifact["syntax_object_serialization"], "syntax object serialization missing")
    require(artifact["hygiene_and_capture_records"], "hygiene records missing")
    require(artifact["build_effect_trace"], "build effect trace missing")
    require(artifact["incremental_cache_decision"], "cache decision missing")
    require(artifact["reference_equivalence_report"]["result"] == ":equivalent", "reference equivalence missing")
    require(artifact["facet_dispatch_record"]["uses_l14_pipeline"], "facet dispatch did not preserve L14")
    return artifact


def validate_negative_fixtures() -> list[dict[str, str]]:
    diagnostics = []
    for filename, expected in NEGATIVE_FIXTURES.items():
        path = FIXTURE_DIR / filename
        diagnostic = alternative_macro_manifest_diagnostic(path)
        require(diagnostic is not None, f"{filename} did not produce a diagnostic")
        require(diagnostic["id"] == expected, f"{filename} produced {diagnostic['id']} instead of {expected}")
        for key in ["provider_id", "provider_version", "macro_symbol", "expansion_phase", "active_profile", "span", "generated_origin_chain", "build_effects", "equivalent_l4_rule"]:
            require(key in diagnostic, f"{filename} diagnostic missing {key}")
        diagnostics.append({"fixture": str(path.relative_to(ROOT)), "diagnostic": diagnostic["id"]})
    return diagnostics


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--artifact-out", type=Path)
    parser.add_argument("--coverage-out", type=Path)
    args = parser.parse_args()

    try:
        artifact = validate_accepted()
        diagnostics = validate_negative_fixtures()
    except AssertionError as exc:
        print(f"L16 alternative macro validation failed: {exc}", file=sys.stderr)
        return 1

    if args.artifact_out:
        atomic_write_json(args.artifact_out, artifact)

    coverage = {
        "kind": "l16-document-coverage",
        "document": "L16",
        "accepted": [
            {
                "fixture": str(ACCEPTED.relative_to(ROOT)),
                "artifact_kind": artifact["kind"],
                "coverage": [
                    "macro provider declaration",
                    "expansion trace",
                    "syntax object serialization",
                    "hygiene and capture records",
                    "build effect trace",
                    "incremental cache decision",
                    "reference equivalence",
                    "facet-aware dispatch",
                    "generated code validation",
                ],
            }
        ],
        "rejected": diagnostics,
    }
    if args.coverage_out:
        atomic_write_json(args.coverage_out, coverage)

    print("L16 alternative macro validation passed: 1 accepted artifact, 10 rejected diagnostics")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
