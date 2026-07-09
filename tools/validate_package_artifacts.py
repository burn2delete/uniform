#!/usr/bin/env python3
"""Validate the Phase 12 build, package, and artifact contract."""

from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / "src"))

from gravity.package_artifacts import package_artifact_diagnostic, validate_package_artifacts_file  # noqa: E402


FIXTURE_DIR = ROOT / "docs/artifacts/phase-12/fixtures/package"
DEFAULT_ACCEPTED = FIXTURE_DIR / "accepted-package-artifacts.json"
NEGATIVE_FIXTURES = {
    "rejected-pkg1-release-lockfile.json": "PKG1006",
    "rejected-pkg2-undeclared-effect.json": "PKG2001",
    "rejected-pkg3-evidence-link.json": "PKG3005",
    "rejected-pkg4-download-verification.json": "PKG4001",
    "rejected-pkg5-capability-incompatible.json": "PKG5002",
    "rejected-pkg6-denied-authority.json": "PKG6004",
    "rejected-pkg7-uncontrolled-network.json": "PKG7003",
    "rejected-pkg8-unsafe-audit.json": "PKG8001",
    "rejected-pkg9-private-registry.json": "PKG9001",
    "rejected-pkg10-provenance.json": "PKG10001",
    "rejected-pkg11-implicit-host-target.json": "PKG11002",
    "rejected-pkg12-noncanonical-signature.json": "PKG12002",
}


def require(condition: bool, message: str) -> None:
    if not condition:
        raise AssertionError(message)


def validate_accepted(path: Path) -> dict:
    artifact = validate_package_artifacts_file(path)
    require(artifact["kind"] == "package-artifacts-artifact", "artifact kind mismatch")
    require(artifact["coverage_summary"]["documents"] == 12, "expected PKG1-PKG12 coverage")
    for key in [
        "project_manifest",
        "lockfile",
        "build_graph",
        "package_manifest",
        "capability_manifest",
        "sbom",
        "signature_verification_report",
        "artifact_manifest",
        "reproducible_build",
        "provenance_record",
        "target_matrix",
        "document_contracts",
    ]:
        require(artifact[key], f"{key} missing")
    require(not artifact["diagnostics"], "accepted artifact should not contain diagnostics")
    return artifact


def validate_negative_fixtures() -> list[dict[str, str]]:
    observed = []
    for filename, expected in NEGATIVE_FIXTURES.items():
        path = FIXTURE_DIR / filename
        diagnostic = package_artifact_diagnostic(path)
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
        print(f"package artifact validation failed: {exc}", file=sys.stderr)
        return 1

    if args.artifact_out:
        args.artifact_out.parent.mkdir(parents=True, exist_ok=True)
        args.artifact_out.write_text(json.dumps(artifact, indent=2, sort_keys=True) + "\n", encoding="utf-8")

    print(
        "package artifact validation passed: "
        f"{artifact['coverage_summary']['documents']} documents, "
        f"{len(diagnostics)} rejected fixtures"
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
