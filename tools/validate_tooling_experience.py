#!/usr/bin/env python3
"""Validate the Phase 13 tooling and developer-experience contract."""

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

from gravity.tooling_experience import tooling_experience_diagnostic, validate_tooling_experience_file  # noqa: E402


FIXTURE_DIR = ROOT / "docs/artifacts/phase-13/fixtures/tooling"
DEFAULT_ACCEPTED = FIXTURE_DIR / "accepted-tooling-experience.json"
NEGATIVE_FIXTURES = {
    "rejected-t1-authority-denial.json": "T1003",
    "rejected-t2-missing-capability.json": "T2002",
    "rejected-t3-round-trip.json": "T3002",
    "rejected-t4-unsafe-autofix.json": "T4003",
    "rejected-t5-diagnostic-mismatch.json": "T5001",
    "rejected-t6-redacted-access.json": "T6004",
    "rejected-t7-stale-docs.json": "T7001",
    "rejected-t8-hot-reload.json": "T8003",
    "rejected-t9-hidden-capability-diff.json": "T9001",
    "rejected-t10-lost-origin.json": "T10002",
    "rejected-t11-check-elision.json": "T11003",
    "rejected-t12-unsafe-island.json": "T12001",
    "rejected-t13-generated-source.json": "T13002",
}


def require(condition: bool, message: str) -> None:
    if not condition:
        raise AssertionError(message)


def validate_accepted(path: Path) -> dict:
    artifact = validate_tooling_experience_file(path)
    require(artifact["kind"] == "tooling-experience-artifact", "artifact kind mismatch")
    require(artifact["coverage_summary"]["documents"] == 13, "expected T1-T13 coverage")
    for key in [
        "cli_command_set",
        "repl_session_artifact",
        "formatter_fixture",
        "linter_diagnostic_report",
        "lsp_capability_matrix",
        "debugger_trace",
        "tooling_ui_data_model",
        "document_contracts",
    ]:
        require(artifact[key], f"{key} missing")
    require(not artifact["diagnostics"], "accepted artifact should not contain diagnostics")
    return artifact


def validate_negative_fixtures() -> list[dict[str, str]]:
    observed = []
    for filename, expected in NEGATIVE_FIXTURES.items():
        diagnostic = tooling_experience_diagnostic(FIXTURE_DIR / filename)
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
        print(f"tooling experience validation failed: {exc}", file=sys.stderr)
        return 1

    if args.artifact_out:
        atomic_write_json(args.artifact_out, artifact)

    print(
        "tooling experience validation passed: "
        f"{artifact['coverage_summary']['documents']} documents, "
        f"{len(diagnostics)} rejected fixtures"
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
