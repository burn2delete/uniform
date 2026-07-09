#!/usr/bin/env python3
"""Validate L14 facet-system coverage."""

from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / "src"))

from gravity.facets import facet_manifest_diagnostic, validate_facet_manifest_file  # noqa: E402


FIXTURE_DIR = ROOT / "docs/artifacts/phase-01/fixtures/l14"
ACCEPTED = FIXTURE_DIR / "accepted-facet-system.json"
NEGATIVE_FIXTURES = {
    "rejected-facet-not-active.json": "L14-FACET-NOT-ACTIVE",
    "rejected-facet-ambiguous.json": "L14-FACET-AMBIGUOUS",
    "rejected-profile.json": "L14-PROFILE",
    "rejected-build-effect.json": "L14-BUILD-EFFECT",
    "rejected-capability.json": "L14-CAPABILITY",
    "rejected-lowering.json": "L14-LOWERING",
    "rejected-domain-check.json": "L14-DOMAIN-CHECK",
    "rejected-generated-code.json": "L14-GENERATED-CODE",
    "rejected-ir-schema.json": "L14-IR-SCHEMA",
    "rejected-composition.json": "L14-COMPOSITION",
    "rejected-privacy-boundary.json": "L14-PRIVACY-BOUNDARY",
}


def require(condition: bool, message: str) -> None:
    if not condition:
        raise AssertionError(message)


def validate_accepted() -> dict:
    artifact = validate_facet_manifest_file(ACCEPTED)
    require(artifact["kind"] == "facet-system-artifact", "artifact kind mismatch")
    require(len(artifact["facet_manifests"]) >= 3, "representative facets missing")
    require(len(artifact["facet_activation_records"]) >= 3, "facet activation records missing")
    require(artifact["generated_gravity_forms"], "generated Gravity forms missing")
    require(artifact["domain_ir_records"], "domain IR records missing")
    require(artifact["composition_records"], "composition records missing")
    require(artifact["privacy_boundary_records"], "privacy boundary records missing")
    require(artifact["compatibility_migration_records"], "compatibility records missing")
    return artifact


def validate_negative_fixtures() -> list[dict[str, str]]:
    diagnostics = []
    for filename, expected in NEGATIVE_FIXTURES.items():
        path = FIXTURE_DIR / filename
        diagnostic = facet_manifest_diagnostic(path)
        require(diagnostic is not None, f"{filename} did not produce a diagnostic")
        require(diagnostic["id"] == expected, f"{filename} produced {diagnostic['id']} instead of {expected}")
        for key in [
            "facet_id",
            "facet_version",
            "active_profile",
            "span",
            "generated_origin_chain",
            "requested_effects",
            "requested_capabilities",
            "domain_rule",
            "remediation",
        ]:
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
        print(f"L14 facet validation failed: {exc}", file=sys.stderr)
        return 1

    if args.artifact_out:
        args.artifact_out.parent.mkdir(parents=True, exist_ok=True)
        args.artifact_out.write_text(json.dumps(artifact, indent=2, sort_keys=True) + "\n", encoding="utf-8")

    coverage = {
        "kind": "l14-document-coverage",
        "document": "L14",
        "accepted": [
            {
                "fixture": str(ACCEPTED.relative_to(ROOT)),
                "artifact_kind": artifact["kind"],
                "coverage": [
                    "facet manifests",
                    "namespace-scoped activation",
                    "generated Gravity validation",
                    "domain IR records",
                    "facet composition",
                    "privacy-boundary preservation",
                    "compatibility records",
                ],
            }
        ],
        "rejected": diagnostics,
    }
    if args.coverage_out:
        args.coverage_out.parent.mkdir(parents=True, exist_ok=True)
        args.coverage_out.write_text(json.dumps(coverage, indent=2, sort_keys=True) + "\n", encoding="utf-8")

    print("L14 facet validation passed: 1 accepted artifact, 11 rejected diagnostics")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
