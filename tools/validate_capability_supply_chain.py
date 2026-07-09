#!/usr/bin/env python3
"""Validate the Phase 02 P02-T05 capability and supply-chain safety checker."""

from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / "src"))

from gravity.capability_supply_chain import (  # noqa: E402
    capability_supply_chain_diagnostic,
    validate_capability_supply_chain_file,
)


FIXTURE_DIR = ROOT / "docs/artifacts/phase-02/fixtures/capability-supply-chain"
DEFAULT_ACCEPTED = FIXTURE_DIR / "accepted-capability-supply-chain.json"
NEGATIVE_FIXTURES = {
    "rejected-capability-missing.json": "SAFE10-MISSING",
    "rejected-capability-denied.json": "SAFE10-DENIED",
    "rejected-capability-scope.json": "SAFE10-SCOPE",
    "rejected-provider.json": "SAFE10-PROVIDER",
    "rejected-ambient.json": "SAFE10-AMBIENT",
    "rejected-phase.json": "SAFE10-PHASE",
    "rejected-secret-leak.json": "SAFE10-SECRET-LEAK",
    "rejected-attenuation.json": "SAFE10-ATTENUATION",
    "rejected-revocation.json": "SAFE10-REVOCATION",
    "rejected-runtime.json": "SAFE10-RUNTIME",
    "rejected-manifest.json": "SAFE14-MANIFEST",
    "rejected-build-effect.json": "SAFE14-BUILD-EFFECT",
    "rejected-runtime-capability.json": "SAFE14-RUNTIME-CAPABILITY",
    "rejected-lockfile.json": "SAFE14-LOCKFILE",
    "rejected-unsafe-summary.json": "SAFE14-UNSAFE-SUMMARY",
    "rejected-native-dep.json": "SAFE14-NATIVE-DEP",
    "rejected-generated.json": "SAFE14-GENERATED",
    "rejected-signature.json": "SAFE14-SIGNATURE",
    "rejected-authority-diff.json": "SAFE14-AUTHORITY-DIFF",
    "rejected-postinstall.json": "SAFE14-POSTINSTALL",
}


def require(condition: bool, message: str) -> None:
    if not condition:
        raise AssertionError(message)


def validate_accepted(path: Path) -> dict:
    artifact = validate_capability_supply_chain_file(path)
    require(artifact["kind"] == "capability-supply-chain-artifact", "artifact kind mismatch")
    require(artifact["documents"] == ["SAFE10", "SAFE14"], "document list mismatch")
    for key in [
        "capability_requirement_records",
        "grant_intersection_records",
        "provider_selection_records",
        "scope_check_records",
        "attenuation_revocation_records",
        "secret_redaction_records",
        "runtime_capability_check_records",
        "capability_usage_summary",
        "package_safety_manifests",
        "lockfile_dependency_graph_records",
        "build_effect_summaries",
        "runtime_capability_summaries",
        "unsafe_island_summaries",
        "native_dependency_abi_records",
        "generated_artifact_provenance",
        "signature_attestation_records",
        "transitive_authority_diffs",
        "supply_chain_conformance_reports",
    ]:
        require(artifact[key], f"{key} missing")
    require(not artifact["diagnostics"], "accepted artifact should not contain diagnostics")
    return artifact


def validate_negative_fixtures() -> list[dict[str, str]]:
    observed = []
    for filename, expected in NEGATIVE_FIXTURES.items():
        path = FIXTURE_DIR / filename
        diagnostic = capability_supply_chain_diagnostic(path)
        require(diagnostic is not None, f"{filename} did not produce a diagnostic")
        require(diagnostic["id"] == expected, f"{filename} produced {diagnostic['id']} instead of {expected}")
        for key in ["source_span", "missing_fact", "remediation", "analyzer_stage"]:
            require(key in diagnostic, f"{filename} diagnostic missing {key}")
        observed.append({"fixture": filename, "diagnostic": diagnostic["id"]})
    return observed


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--accepted", type=Path, default=DEFAULT_ACCEPTED)
    parser.add_argument("--artifact-out", type=Path)
    args = parser.parse_args()

    try:
        artifact = validate_accepted(args.accepted)
        diagnostics = validate_negative_fixtures()
    except AssertionError as exc:
        print(f"capability supply-chain validation failed: {exc}", file=sys.stderr)
        return 1

    if args.artifact_out:
        args.artifact_out.parent.mkdir(parents=True, exist_ok=True)
        args.artifact_out.write_text(json.dumps(artifact, indent=2, sort_keys=True) + "\n", encoding="utf-8")

    print(
        "capability supply-chain validation passed: "
        f"2 accepted records, {len(diagnostics)} rejected fixtures"
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
