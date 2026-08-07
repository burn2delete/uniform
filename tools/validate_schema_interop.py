#!/usr/bin/env python3
"""Validate the Phase 10 schema, data, and interop contract."""

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

from gravity.schema_interop import schema_interop_diagnostic, validate_schema_interop_file  # noqa: E402


FIXTURE_DIR = ROOT / "docs/artifacts/phase-10/fixtures/schema"
DEFAULT_ACCEPTED = FIXTURE_DIR / "accepted-schema-interop.json"
NEGATIVE_FIXTURES = {
    "rejected-s1-projection.json": "S1-PROJECTION",
    "rejected-s2-taint.json": "S2-TAINT",
    "rejected-s3-hash.json": "S3-HASH",
    "rejected-s4-resolver.json": "S4-RESOLVER",
    "rejected-s5-schema.json": "S5-SCHEMA",
    "rejected-s6-data-loss.json": "S6-DATA-LOSS",
    "rejected-s7-pointer.json": "S7-POINTER",
    "rejected-s8-secret.json": "S8-SECRET",
    "rejected-s9-evidence.json": "S9-EVIDENCE",
}


def require(condition: bool, message: str) -> None:
    if not condition:
        raise AssertionError(message)


def validate_accepted(path: Path) -> dict:
    artifact = validate_schema_interop_file(path)
    require(artifact["kind"] == "schema-interop-artifact", "artifact kind mismatch")
    require(artifact["coverage_summary"]["documents"] == 9, "expected S1-S9 coverage")
    require(artifact["coverage_summary"]["generated_artifacts"] == 8, "expected eight generated artifact families")
    for key in [
        "source_schema_ir",
        "validator_artifact",
        "serialization_fixture",
        "canonical_format",
        "graphql_generation",
        "openapi_generation",
        "database_mapping",
        "binary_abi_schema",
        "typed_config",
        "artifact_schema_registry",
        "document_contracts",
    ]:
        require(artifact[key], f"{key} missing")
    require(not artifact["diagnostics"], "accepted artifact should not contain diagnostics")
    return artifact


def validate_negative_fixtures() -> list[dict[str, str]]:
    observed = []
    for filename, expected in NEGATIVE_FIXTURES.items():
        path = FIXTURE_DIR / filename
        diagnostic = schema_interop_diagnostic(path)
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
        print(f"schema interop validation failed: {exc}", file=sys.stderr)
        return 1

    if args.artifact_out:
        atomic_write_json(args.artifact_out, artifact)

    print(
        "schema interop validation passed: "
        f"{artifact['coverage_summary']['documents']} documents, "
        f"{len(diagnostics)} rejected fixtures"
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
