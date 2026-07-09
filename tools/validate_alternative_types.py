#!/usr/bin/env python3
"""Validate L17 alternative type-system coverage."""

from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / "src"))

from gravity.alternative_types import alternative_type_manifest_diagnostic, validate_alternative_type_manifest_file  # noqa: E402


FIXTURE_DIR = ROOT / "docs/artifacts/phase-01/fixtures/l17"
ACCEPTED = FIXTURE_DIR / "accepted-alternative-type.json"
NEGATIVE_FIXTURES = {
    "rejected-provider.json": "L17-PROVIDER",
    "rejected-lowering.json": "L17-LOWERING",
    "rejected-soundness.json": "L17-SOUNDNESS",
    "rejected-effect-erasure.json": "L17-EFFECT-ERASURE",
    "rejected-capability-erasure.json": "L17-CAPABILITY-ERASURE",
    "rejected-ownership-fact.json": "L17-OWNERSHIP-FACT",
    "rejected-gradual-boundary.json": "L17-GRADUAL-BOUNDARY",
    "rejected-unsafe-cast.json": "L17-UNSAFE-CAST",
    "rejected-domain-fact.json": "L17-DOMAIN-FACT",
    "rejected-diagnostic-map.json": "L17-DIAGNOSTIC-MAP",
}


def require(condition: bool, message: str) -> None:
    if not condition:
        raise AssertionError(message)


def validate_accepted() -> dict:
    artifact = validate_alternative_type_manifest_file(ACCEPTED)
    require(artifact["kind"] == "alternative-type-system-artifact", "artifact kind mismatch")
    require(artifact["provider_declaration"]["kind"] == ":type-system", "provider declaration missing")
    require(artifact["typed_core"]["lowering_status"] == ":accepted", "typed core was not accepted")
    require(artifact["fact_export_schema"], "fact export schema missing")
    require(artifact["proof_refinement_artifacts"], "proof artifacts missing")
    require(artifact["cast_runtime_check_records"], "runtime check records missing")
    require(artifact["type_diagnostic_mapping_records"], "diagnostic mapping records missing")
    require(artifact["reference_compatibility_report"]["result"] == ":compatible", "reference compatibility missing")
    require(artifact["profile_soundness_evidence"], "profile soundness evidence missing")
    return artifact


def validate_negative_fixtures() -> list[dict[str, str]]:
    diagnostics = []
    for filename, expected in NEGATIVE_FIXTURES.items():
        path = FIXTURE_DIR / filename
        diagnostic = alternative_type_manifest_diagnostic(path)
        require(diagnostic is not None, f"{filename} did not produce a diagnostic")
        require(diagnostic["id"] == expected, f"{filename} produced {diagnostic['id']} instead of {expected}")
        for key in ["provider_id", "provider_version", "active_profile", "span", "generated_origin_chain", "type_expression", "effect_set", "capability_set", "relevant_proof_id", "core_rule"]:
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
        print(f"L17 alternative type validation failed: {exc}", file=sys.stderr)
        return 1

    if args.artifact_out:
        args.artifact_out.parent.mkdir(parents=True, exist_ok=True)
        args.artifact_out.write_text(json.dumps(artifact, indent=2, sort_keys=True) + "\n", encoding="utf-8")

    coverage = {
        "kind": "l17-document-coverage",
        "document": "L17",
        "accepted": [
            {
                "fixture": str(ACCEPTED.relative_to(ROOT)),
                "artifact_kind": artifact["kind"],
                "coverage": [
                    "type provider declaration",
                    "typed core lowering",
                    "fact export schema",
                    "proof artifacts",
                    "runtime check records",
                    "diagnostic mapping",
                    "reference compatibility",
                    "profile soundness",
                ],
            }
        ],
        "rejected": diagnostics,
    }
    if args.coverage_out:
        args.coverage_out.parent.mkdir(parents=True, exist_ok=True)
        args.coverage_out.write_text(json.dumps(coverage, indent=2, sort_keys=True) + "\n", encoding="utf-8")

    print("L17 alternative type validation passed: 1 accepted artifact, 10 rejected diagnostics")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
