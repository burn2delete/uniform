#!/usr/bin/env python3
"""Validate L15 capability-provider coverage."""

from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / "src"))

from gravity.providers import provider_manifest_diagnostic, validate_provider_manifest_file  # noqa: E402


FIXTURE_DIR = ROOT / "docs/artifacts/phase-01/fixtures/l15"
ACCEPTED = FIXTURE_DIR / "accepted-providers.json"
NEGATIVE_FIXTURES = {
    "rejected-capability-missing.json": "L15-CAPABILITY-MISSING",
    "rejected-provider-missing.json": "L15-PROVIDER-MISSING",
    "rejected-provider-ambiguous.json": "L15-PROVIDER-AMBIGUOUS",
    "rejected-profile.json": "L15-PROFILE",
    "rejected-scope.json": "L15-SCOPE",
    "rejected-phase.json": "L15-PHASE",
    "rejected-trust.json": "L15-TRUST",
    "rejected-replay.json": "L15-REPLAY",
    "rejected-secret.json": "L15-SECRET",
    "rejected-contract.json": "L15-CONTRACT",
    "rejected-revocation.json": "L15-REVOCATION",
}


def require(condition: bool, message: str) -> None:
    if not condition:
        raise AssertionError(message)


def validate_accepted() -> dict:
    artifact = validate_provider_manifest_file(ACCEPTED)
    require(artifact["kind"] == "capability-provider-artifact", "artifact kind mismatch")
    require(len(artifact["provider_declaration_records"]) >= 4, "provider declarations missing")
    require(artifact["grant_records"], "grant records missing")
    require(artifact["provider_selection_records"], "selection records missing")
    require(artifact["capability_scope_audit_logs"], "scope audit logs missing")
    require(artifact["compile_time_replay_records"], "compile-time replay records missing")
    require(artifact["runtime_provider_manifests"], "runtime provider manifests missing")
    require(all(result["status"] == ":passed" for result in artifact["provider_conformance_results"]), "conformance results did not pass")
    sources = {record["selection_source"] for record in artifact["provider_selection_records"]}
    require({":source-annotation", ":package-manifest", ":workspace-policy", ":profile-default", ":compiler-default"}.issubset(sources), "selection order sources missing")
    return artifact


def validate_negative_fixtures() -> list[dict[str, str]]:
    diagnostics = []
    for filename, expected in NEGATIVE_FIXTURES.items():
        path = FIXTURE_DIR / filename
        diagnostic = provider_manifest_diagnostic(path)
        require(diagnostic is not None, f"{filename} did not produce a diagnostic")
        require(diagnostic["id"] == expected, f"{filename} produced {diagnostic['id']} instead of {expected}")
        for key in [
            "requested_capability",
            "selected_provider",
            "grant_id",
            "scope",
            "phase",
            "active_profile",
            "target",
            "nearest_valid_provider",
            "nearest_valid_grant",
            "span",
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
        print(f"L15 provider validation failed: {exc}", file=sys.stderr)
        return 1

    if args.artifact_out:
        args.artifact_out.parent.mkdir(parents=True, exist_ok=True)
        args.artifact_out.write_text(json.dumps(artifact, indent=2, sort_keys=True) + "\n", encoding="utf-8")

    coverage = {
        "kind": "l15-document-coverage",
        "document": "L15",
        "accepted": [
            {
                "fixture": str(ACCEPTED.relative_to(ROOT)),
                "artifact_kind": artifact["kind"],
                "coverage": [
                    "provider declarations",
                    "grant records",
                    "deterministic provider selection",
                    "scope audit logs",
                    "compile-time replay records",
                    "runtime provider manifests",
                    "conformance results",
                    "safe replacement providers",
                ],
            }
        ],
        "rejected": diagnostics,
    }
    if args.coverage_out:
        args.coverage_out.parent.mkdir(parents=True, exist_ok=True)
        args.coverage_out.write_text(json.dumps(coverage, indent=2, sort_keys=True) + "\n", encoding="utf-8")

    print("L15 provider validation passed: 1 accepted artifact, 11 rejected diagnostics")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
