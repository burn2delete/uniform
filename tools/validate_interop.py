#!/usr/bin/env python3
"""Validate L19 interop and migration coverage."""

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

from gravity.interop import interop_manifest_diagnostic, validate_interop_manifest_file  # noqa: E402


FIXTURE_DIR = ROOT / "docs/artifacts/phase-01/fixtures/l19"
ACCEPTED = FIXTURE_DIR / "accepted-interop.json"
NEGATIVE_FIXTURES = {
    "rejected-boundary-incomplete.json": "L19-BOUNDARY-INCOMPLETE",
    "rejected-profile.json": "L19-PROFILE",
    "rejected-type-map.json": "L19-TYPE-MAP",
    "rejected-ownership.json": "L19-OWNERSHIP",
    "rejected-error-map.json": "L19-ERROR-MAP",
    "rejected-capability.json": "L19-CAPABILITY",
    "rejected-effect.json": "L19-EFFECT",
    "rejected-safe-wrapper.json": "L19-SAFE-WRAPPER",
    "rejected-schema-drift.json": "L19-SCHEMA-DRIFT",
    "rejected-migration-parity.json": "L19-MIGRATION-PARITY",
    "rejected-host-leak.json": "L19-HOST-LEAK",
}


def require(condition: bool, message: str) -> None:
    if not condition:
        raise AssertionError(message)


def validate_accepted() -> dict:
    artifact = validate_interop_manifest_file(ACCEPTED)
    require(artifact["kind"] == "interop-migration-artifact", "artifact kind mismatch")
    require(len(artifact["foreign_binding_declaration_records"]) >= 5, "representative boundaries missing")
    require(artifact["abi_protocol_schema_metadata"], "ABI/protocol/schema metadata missing")
    require(artifact["generated_binding_source_and_provenance"], "generated binding provenance missing")
    require(artifact["safe_wrapper_audit_records"], "safe wrapper audits missing")
    require(artifact["ownership_lifetime_maps"], "ownership maps missing")
    require(artifact["error_translation_maps"], "error maps missing")
    require(artifact["capability_effect_records"], "capability/effect records missing")
    require(artifact["migration_shim_records"], "migration records missing")
    require(all(report["status"] == ":passed" for report in artifact["incumbent_parity_test_reports"]), "parity reports did not pass")
    return artifact


def validate_negative_fixtures() -> list[dict[str, str]]:
    diagnostics = []
    for filename, expected in NEGATIVE_FIXTURES.items():
        path = FIXTURE_DIR / filename
        diagnostic = interop_manifest_diagnostic(path)
        require(diagnostic is not None, f"{filename} did not produce a diagnostic")
        require(diagnostic["id"] == expected, f"{filename} produced {diagnostic['id']} instead of {expected}")
        for key in ["boundary_id", "foreign_source", "active_profile", "span", "provider_id", "type_mapping", "ownership_facts", "effects", "capabilities", "suggested_safe_wrapper_or_migration_step"]:
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
        print(f"L19 interop validation failed: {exc}", file=sys.stderr)
        return 1

    if args.artifact_out:
        atomic_write_json(args.artifact_out, artifact)

    coverage = {
        "kind": "l19-document-coverage",
        "document": "L19",
        "accepted": [
            {
                "fixture": str(ACCEPTED.relative_to(ROOT)),
                "artifact_kind": artifact["kind"],
                "coverage": [
                    "foreign binding declarations",
                    "ABI/protocol/schema metadata",
                    "generated binding provenance",
                    "safe wrapper audits",
                    "ownership and error maps",
                    "capability and effect records",
                    "migration shims",
                    "parity reports",
                    "compatibility records",
                ],
            }
        ],
        "rejected": diagnostics,
    }
    if args.coverage_out:
        atomic_write_json(args.coverage_out, coverage)

    print("L19 interop validation passed: 1 accepted artifact, 11 rejected diagnostics")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
