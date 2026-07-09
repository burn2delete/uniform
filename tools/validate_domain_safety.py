#!/usr/bin/env python3
"""Validate the Phase 02 P02-T04 domain safety checker."""

from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / "src"))

from gravity.domain_safety import domain_safety_diagnostic, validate_domain_safety_file  # noqa: E402


FIXTURE_DIR = ROOT / "docs/artifacts/phase-02/fixtures/domain-safety"
DEFAULT_ACCEPTED = FIXTURE_DIR / "accepted-domain-safety.json"
NEGATIVE_FIXTURES = {
    "rejected-ffi-declaration.json": "SAFE7-DECLARATION",
    "rejected-ffi-raw-call.json": "SAFE7-RAW-CALL",
    "rejected-ffi-error-map.json": "SAFE7-ERROR-MAP",
    "rejected-ffi-generated.json": "SAFE7-GENERATED",
    "rejected-concurrency-data-race.json": "SAFE8-DATA-RACE",
    "rejected-concurrency-task-capture.json": "SAFE8-TASK-CAPTURE",
    "rejected-concurrency-atomic-order.json": "SAFE8-ATOMIC-ORDER",
    "rejected-concurrency-workflow-replay.json": "SAFE8-WORKFLOW-REPLAY",
    "rejected-numeric-overflow.json": "SAFE9-OVERFLOW",
    "rejected-numeric-narrow.json": "SAFE9-NARROW",
    "rejected-numeric-float-mode.json": "SAFE9-FLOAT-MODE",
    "rejected-numeric-optimization.json": "SAFE9-OPTIMIZATION",
    "rejected-taint-sink.json": "SAFE11-TAINTED-SINK",
    "rejected-taint-parameterization.json": "SAFE11-PARAMETERIZATION",
    "rejected-taint-secret-leak.json": "SAFE11-SECRET-LEAK",
    "rejected-taint-prompt-injection.json": "SAFE11-PROMPT-INJECTION",
}


def require(condition: bool, message: str) -> None:
    if not condition:
        raise AssertionError(message)


def validate_accepted(path: Path) -> dict:
    artifact = validate_domain_safety_file(path)
    require(artifact["kind"] == "domain-safety-artifact", "artifact kind mismatch")
    require(artifact["documents"] == ["SAFE7", "SAFE8", "SAFE9", "SAFE11"], "document list mismatch")
    for key in [
        "foreign_declaration_records",
        "abi_protocol_records",
        "type_mapping_records",
        "ffi_ownership_lifetime_maps",
        "safe_wrapper_audit_records",
        "error_translation_maps",
        "generated_binding_provenance",
        "concurrency_graph",
        "task_capture_records",
        "synchronization_proof_records",
        "atomic_memory_order_records",
        "backend_memory_order_preservation_records",
        "numeric_mode_records",
        "numeric_check_records",
        "range_interval_proof_records",
        "floating_point_mode_records",
        "elementary_function_approximation_records",
        "numeric_optimization_proof_records",
        "backend_numeric_lowering_records",
        "taint_source_records",
        "taint_flow_records",
        "validator_sanitizer_contracts",
        "sink_authorization_records",
        "secret_redaction_records",
        "generated_code_taint_propagation_records",
        "taint_conformance_reports",
    ]:
        require(artifact[key], f"{key} missing")
    require(not artifact["diagnostics"], "accepted artifact should not contain diagnostics")
    return artifact


def validate_negative_fixtures() -> list[dict[str, str]]:
    observed = []
    for filename, expected in NEGATIVE_FIXTURES.items():
        path = FIXTURE_DIR / filename
        diagnostic = domain_safety_diagnostic(path)
        require(diagnostic is not None, f"{filename} did not produce a diagnostic")
        require(diagnostic["id"] == expected, f"{filename} produced {diagnostic['id']} instead of {expected}")
        for key in ["span", "active_profile", "target", "missing_fact", "effect_context", "capability_context", "remediation"]:
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
        print(f"domain safety validation failed: {exc}", file=sys.stderr)
        return 1

    if args.artifact_out:
        args.artifact_out.parent.mkdir(parents=True, exist_ok=True)
        args.artifact_out.write_text(json.dumps(artifact, indent=2, sort_keys=True) + "\n", encoding="utf-8")

    print(
        "domain safety validation passed: "
        f"4 accepted domains, {len(diagnostics)} rejected fixtures"
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
