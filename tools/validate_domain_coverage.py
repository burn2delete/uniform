#!/usr/bin/env python3
"""Validate the Phase 09 domain coverage contract and fixtures."""

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

from gravity.domain_coverage import domain_diagnostic, validate_domain_file  # noqa: E402


FIXTURE_DIR = ROOT / "docs/artifacts/phase-09/fixtures/domain"
DEFAULT_ACCEPTED = FIXTURE_DIR / "accepted-domain-coverage.json"
NEGATIVE_FIXTURES = {
    "rejected-dom1-width.json": "DOM1-WIDTH",
    "rejected-dom2-mmio.json": "DOM2-MMIO",
    "rejected-dom3-raw.json": "DOM3-RAW",
    "rejected-dom4-dma.json": "DOM4-DMA",
    "rejected-dom5-optimization.json": "DOM5-OPTIMIZATION",
    "rejected-dom6-taint.json": "DOM6-TAINT",
    "rejected-dom7-permission.json": "DOM7-PERMISSION",
    "rejected-dom8-schema.json": "DOM8-SCHEMA",
    "rejected-dom9-convergence.json": "DOM9-CONVERGENCE",
    "rejected-dom10-query.json": "DOM10-QUERY",
    "rejected-dom11-lineage.json": "DOM11-LINEAGE",
    "rejected-dom12-certificate.json": "DOM12-CERTIFICATE",
    "rejected-dom13-host-effect.json": "DOM13-HOST-EFFECT",
    "rejected-dom14-determinism.json": "DOM14-DETERMINISM",
    "rejected-dom15-boundary.json": "DOM15-BOUNDARY",
    "rejected-dom16-aa-profile.json": "DOM16-AA-PROFILE",
    "rejected-dom17-metadata.json": "DOM17-METADATA",
    "rejected-dom18-tool.json": "DOM18-TOOL",
    "rejected-dom19-zk-setup.json": "DOM19-ZK-SETUP",
    "rejected-dom20-taint.json": "DOM20-TAINT",
    "rejected-dom21-edge.json": "DOM21-EDGE",
    "rejected-p09-broad-claim.json": "P09-CLAIM",
}


def require(condition: bool, message: str) -> None:
    if not condition:
        raise AssertionError(message)


def validate_accepted(path: Path) -> dict:
    artifact = validate_domain_file(path)
    require(artifact["kind"] == "domain-coverage-artifact", "artifact kind mismatch")
    require(artifact["coverage_summary"]["documents"] == 21, "expected DOM1-DOM21 coverage")
    require(artifact["coverage_summary"]["domain_records"] == 21, "expected twenty-one domain records")
    require(artifact["coverage_summary"]["accepted_fixtures"] == 21, "expected twenty-one accepted fixtures")
    require(artifact["coverage_summary"]["rejected_fixtures"] == 21, "expected twenty-one rejected fixtures")
    require(artifact["coverage_summary"]["replacement_claims"] == 21, "expected twenty-one replacement claims")
    require(artifact["coverage_summary"]["conformance_records"] == 21, "expected twenty-one conformance records")
    require(not artifact["diagnostics"], "accepted artifact should not contain diagnostics")
    return artifact


def validate_negative_fixtures() -> list[dict[str, str]]:
    observed = []
    for filename, expected in NEGATIVE_FIXTURES.items():
        path = FIXTURE_DIR / filename
        diagnostic = domain_diagnostic(path)
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
        print(f"domain coverage validation failed: {exc}", file=sys.stderr)
        return 1

    if args.artifact_out:
        atomic_write_json(args.artifact_out, artifact)

    print(
        "domain coverage validation passed: "
        f"{artifact['coverage_summary']['domain_records']} domain records, "
        f"{len(diagnostics)} rejected fixtures"
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
