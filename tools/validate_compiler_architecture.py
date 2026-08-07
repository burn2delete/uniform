#!/usr/bin/env python3
"""Validate the Phase 06 compiler architecture contract and fixtures."""

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

from gravity.compiler_architecture import compiler_diagnostic, validate_compiler_file  # noqa: E402


FIXTURE_DIR = ROOT / "docs/artifacts/phase-06/fixtures/compiler"
DEFAULT_ACCEPTED = FIXTURE_DIR / "accepted-compiler-architecture.json"
NEGATIVE_FIXTURES = {
    "rejected-c1-pipeline.json": "C1-PIPELINE",
    "rejected-c2-hash.json": "C2-HASH",
    "rejected-c3-origin.json": "C3-ORIGIN",
    "rejected-c4-trace.json": "C4-TRACE",
    "rejected-c5-unresolved.json": "C5-UNRESOLVED",
    "rejected-c6-lowering.json": "C6-LOWERING-GAP",
    "rejected-c7-verify.json": "C7-VERIFY",
    "rejected-c8-capability.json": "C8-CAPABILITY",
    "rejected-c9-linear.json": "C9-LINEAR-LEAK",
    "rejected-c10-outcome.json": "C10-NO-OUTCOME",
    "rejected-c11-type.json": "C11-TYPE",
    "rejected-c12-anchor.json": "C12-ANCHOR",
    "rejected-c13-proof.json": "C13-PROOF",
    "rejected-c14-input.json": "C14-INPUT",
    "rejected-c15-redaction.json": "C15-REDACTION",
    "rejected-c16-proof.json": "C16-PROOF",
    "rejected-c17-capability.json": "C17-CAPABILITY",
    "rejected-c18-evidence.json": "C18-EVIDENCE",
}


def require(condition: bool, message: str) -> None:
    if not condition:
        raise AssertionError(message)


def validate_accepted(path: Path) -> dict:
    artifact = validate_compiler_file(path)
    require(artifact["kind"] == "compiler-architecture-artifact", "artifact kind mismatch")
    for key in [
        "pipeline_manifest",
        "pass_contract_manifest",
        "compiler_diagnostic_registry",
        "reader_artifacts",
        "syntax_object_stream",
        "macro_expansion_trace",
        "namespace_analysis",
        "core_ast_module",
        "typed_core_module",
        "effect_graph",
        "ownership_analysis",
        "safety_pipeline",
        "mir_module",
        "domain_ir_modules",
        "optimization_manifest",
        "target_lowering_manifest",
        "incremental_compilation",
        "plugin_system",
        "compiler_verification_plan",
    ]:
        require(artifact[key], f"{key} missing")
    require(not artifact["diagnostics"], "accepted artifact should not contain diagnostics")
    return artifact


def validate_negative_fixtures() -> list[dict[str, str]]:
    observed = []
    for filename, expected in NEGATIVE_FIXTURES.items():
        path = FIXTURE_DIR / filename
        diagnostic = compiler_diagnostic(path)
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
        print(f"compiler architecture validation failed: {exc}", file=sys.stderr)
        return 1

    if args.artifact_out:
        atomic_write_json(args.artifact_out, artifact)

    print(
        "compiler architecture validation passed: "
        f"{len(artifact['pass_contract_manifest'])} pass contracts, "
        f"{len(diagnostics)} rejected fixtures"
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
