#!/usr/bin/env python3
"""Validate Phase 00 D6/D8/D9 safety-performance gate artifacts."""

from __future__ import annotations

import argparse
import json
import re
import sys
from pathlib import Path
from typing import Any


ROOT = Path(__file__).resolve().parents[1]
DEFAULT_MODEL = ROOT / "docs/artifacts/phase-00/safety-performance-gate-model.json"

LEGAL_SAFETY_OUTCOMES = {
    ":proven-safe",
    ":runtime-checked",
    ":rejected",
    ":unsafe-island",
}
LEGAL_OPTIMIZATION_CHOICES = {
    "preserve-fact",
    "regenerate-fact",
    "retain-runtime-check",
    "reject-transformation",
}
REQUIRED_CLAIM_FIELDS = {
    "claim_id",
    "profile",
    "target",
    "backend",
    "runtime",
    "safety_mode",
    "input_domain",
    "layout",
    "effects",
    "capabilities",
    "erased_checks",
    "proof_records",
    "benchmark",
    "artifacts",
}
REQUIRED_BENCHMARK_FIELDS = {
    "harness",
    "baseline",
    "samples",
    "target_fingerprint",
    "compiler_identity",
    "source_hash",
    "optimization_manifest",
}
REQUIRED_PROOF_FIELDS = {
    "claim",
    "domain",
    "assumptions",
    "checker",
    "inputs",
    "result",
}
REQUIRED_UNSAFE_FIELDS = {
    "reason",
    "source_span",
    "profiles",
    "effects",
    "capabilities",
    "preconditions",
    "postconditions",
    "invariants",
    "safe_boundary",
    "evidence",
    "owner",
    "review",
    "re_review",
}
REQUIRED_MODEL_DIAGNOSTICS = {
    "D6-CLAIM-INCOMPLETE",
    "D6-CHECK-ELISION-UNPROVED",
    "D6-FAST-MATH-IMPLICIT",
    "D6-EFFECT-REORDER",
    "D6-TARGET-ASSUMPTION",
    "D8-UNCLASSIFIED-DANGER",
    "D8-CHECK-ERASED",
    "D8-UNSAFE-MISSING-METADATA",
    "D9-PROOF-MISSING",
    "D9-CERT-UNCHECKABLE",
    "D9-CHECK-ELISION-NO-PROOF",
}
DIAGNOSTIC_RE = re.compile(r"^[A-Z][A-Z0-9]*-[A-Z0-9][A-Z0-9-]*$")


def load_json(path: Path) -> Any:
    try:
        return json.loads(path.read_text(encoding="utf-8"))
    except json.JSONDecodeError as exc:
        return {"_json_error": f"{path}:{exc.lineno}:{exc.colno}: {exc.msg}"}


def non_empty(value: Any) -> bool:
    if isinstance(value, list):
        return bool(value)
    if isinstance(value, dict):
        return bool(value)
    if isinstance(value, str):
        return bool(value.strip())
    return value is not None


def validate_model(model: Any) -> list[tuple[str, str]]:
    diagnostics: list[tuple[str, str]] = []
    if isinstance(model, dict) and "_json_error" in model:
        return [("D6-CLAIM-INCOMPLETE", model["_json_error"])]
    if not isinstance(model, dict):
        return [("D6-CLAIM-INCOMPLETE", "model must be a JSON object")]
    if model.get("kind") != "safety-performance-gate-model":
        diagnostics.append(("D6-CLAIM-INCOMPLETE", "kind must be safety-performance-gate-model"))

    if set(model.get("legal_safety_outcomes", [])) != LEGAL_SAFETY_OUTCOMES:
        diagnostics.append(("D8-UNCLASSIFIED-DANGER", "legal safety outcomes must be exactly the D8 four-outcome set"))
    if set(model.get("legal_optimization_choices", [])) != LEGAL_OPTIMIZATION_CHOICES:
        diagnostics.append(("D8-CHECK-ERASED", "legal optimization choices must preserve, regenerate, retain, or reject"))

    diagnostics_seen = set(model.get("diagnostics", []))
    missing_diagnostics = sorted(REQUIRED_MODEL_DIAGNOSTICS - diagnostics_seen)
    if missing_diagnostics:
        diagnostics.append(("D6-CLAIM-INCOMPLETE", f"missing model diagnostics: {missing_diagnostics}"))
    for diagnostic_id in diagnostics_seen:
        if not isinstance(diagnostic_id, str) or not DIAGNOSTIC_RE.match(diagnostic_id):
            diagnostics.append(("D6-CLAIM-INCOMPLETE", f"unstable diagnostic id {diagnostic_id!r}"))

    for field in ["claim_fields", "benchmark_fields", "proof_fields", "unsafe_island_fields", "gate_records"]:
        if not non_empty(model.get(field)):
            diagnostics.append(("D6-CLAIM-INCOMPLETE", f"model requires non-empty {field}"))

    if set(model.get("claim_fields", [])) != REQUIRED_CLAIM_FIELDS:
        diagnostics.append(("D6-CLAIM-INCOMPLETE", "claim_fields must match D6 performance claim record fields"))
    if set(model.get("benchmark_fields", [])) != REQUIRED_BENCHMARK_FIELDS:
        diagnostics.append(("D6-CLAIM-INCOMPLETE", "benchmark_fields must match D6 benchmark artifact fields"))
    if set(model.get("proof_fields", [])) != REQUIRED_PROOF_FIELDS:
        diagnostics.append(("D9-CERT-UNCHECKABLE", "proof_fields must match D9 certificate shape"))
    if set(model.get("unsafe_island_fields", [])) != REQUIRED_UNSAFE_FIELDS:
        diagnostics.append(("D8-UNSAFE-MISSING-METADATA", "unsafe_island_fields must match D8 unsafe island metadata"))

    for path in model.get("source_basis", []):
        if isinstance(path, str) and path.endswith(".md") and not (ROOT / path).exists():
            diagnostics.append(("D6-CLAIM-INCOMPLETE", f"source basis path does not exist: {path}"))
    return diagnostics


def validate_proofs(record_id: str, record: dict[str, Any], diagnostics: list[tuple[str, str]]) -> None:
    proof_records = record.get("proof_records")
    if not isinstance(proof_records, list) or not proof_records:
        diagnostics.append(("D9-PROOF-MISSING", f"{record_id} requires proof_records"))
        return
    for index, proof in enumerate(proof_records):
        if not isinstance(proof, dict):
            diagnostics.append(("D9-CERT-UNCHECKABLE", f"{record_id} proof {index} must be an object"))
            continue
        missing = sorted(REQUIRED_PROOF_FIELDS - set(proof))
        if missing:
            diagnostics.append(("D9-CERT-UNCHECKABLE", f"{record_id} proof {index} missing fields: {missing}"))


def validate_claim_record(record: dict[str, Any]) -> list[tuple[str, str]]:
    diagnostics: list[tuple[str, str]] = []
    record_id = str(record.get("claim_id", record.get("id", "record")))

    missing_claim_fields = sorted(REQUIRED_CLAIM_FIELDS - set(record))
    if missing_claim_fields:
        diagnostics.append(("D6-CLAIM-INCOMPLETE", f"{record_id} missing claim fields: {missing_claim_fields}"))

    safety_outcome = record.get("safety_outcome")
    if safety_outcome not in LEGAL_SAFETY_OUTCOMES:
        diagnostics.append(("D8-UNCLASSIFIED-DANGER", f"{record_id} has illegal or missing safety_outcome {safety_outcome!r}"))

    optimization_choice = record.get("optimization_choice")
    if optimization_choice not in LEGAL_OPTIMIZATION_CHOICES:
        diagnostics.append(("D8-CHECK-ERASED", f"{record_id} has illegal optimization_choice {optimization_choice!r}"))

    erased_checks = record.get("erased_checks", [])
    if erased_checks and optimization_choice not in {"preserve-fact", "regenerate-fact"}:
        diagnostics.append(("D6-CHECK-ELISION-UNPROVED", f"{record_id} erases checks without preserve or regenerate choice"))
    if erased_checks:
        proof_records = record.get("proof_records", [])
        if not proof_records:
            diagnostics.append(("D9-CHECK-ELISION-NO-PROOF", f"{record_id} erased checks without proof records"))
        else:
            validate_proofs(record_id, record, diagnostics)

    if record.get("drops_proof") is True:
        diagnostics.append(("D8-CHECK-ERASED", f"{record_id} drops proof evidence"))
    if record.get("fast_math") is True and not (record.get("numeric_mode") and record.get("approximation_certificate")):
        diagnostics.append(("D6-FAST-MATH-IMPLICIT", f"{record_id} uses fast math without explicit mode and certificate"))

    benchmark = record.get("benchmark")
    if not isinstance(benchmark, dict):
        diagnostics.append(("D6-CLAIM-INCOMPLETE", f"{record_id} requires benchmark artifact"))
    else:
        missing_benchmark = sorted(REQUIRED_BENCHMARK_FIELDS - set(benchmark))
        if missing_benchmark:
            diagnostics.append(("D6-CLAIM-INCOMPLETE", f"{record_id} benchmark missing fields: {missing_benchmark}"))

    if safety_outcome == ":unsafe-island":
        unsafe_island = record.get("unsafe_island")
        if not isinstance(unsafe_island, dict):
            diagnostics.append(("D8-UNSAFE-MISSING-METADATA", f"{record_id} requires unsafe_island metadata"))
        else:
            missing_unsafe = sorted(REQUIRED_UNSAFE_FIELDS - set(unsafe_island))
            if missing_unsafe:
                diagnostics.append(("D8-UNSAFE-MISSING-METADATA", f"{record_id} unsafe island missing fields: {missing_unsafe}"))

    if safety_outcome == ":rejected" and not record.get("rejection_diagnostic"):
        diagnostics.append(("D6-CHECK-ELISION-UNPROVED", f"{record_id} rejected transformation needs rejection_diagnostic"))

    return diagnostics


def validate_fixture(fixture: Any, model: dict[str, Any]) -> list[tuple[str, str]]:
    diagnostics = validate_model(model)
    if diagnostics:
        return diagnostics
    if isinstance(fixture, dict) and "_json_error" in fixture:
        return [("D6-CLAIM-INCOMPLETE", fixture["_json_error"])]
    if not isinstance(fixture, dict):
        return [("D6-CLAIM-INCOMPLETE", "fixture must be a JSON object")]
    if fixture.get("kind") != "safety-performance-gate-fixture":
        diagnostics.append(("D6-CLAIM-INCOMPLETE", "kind must be safety-performance-gate-fixture"))
    records = fixture.get("records")
    if not isinstance(records, list) or not records:
        return diagnostics + [("D6-CLAIM-INCOMPLETE", "fixture records must be non-empty")]
    for record in records:
        if not isinstance(record, dict):
            diagnostics.append(("D6-CLAIM-INCOMPLETE", "fixture record must be an object"))
            continue
        diagnostics.extend(validate_claim_record(record))
    return diagnostics


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("artifact", type=Path)
    parser.add_argument("--model", type=Path, default=DEFAULT_MODEL)
    parser.add_argument("--expect-failure")
    args = parser.parse_args()

    artifact = load_json(args.artifact)
    if isinstance(artifact, dict) and artifact.get("kind") == "safety-performance-gate-model":
        diagnostics = validate_model(artifact)
        output = "safety/performance gate model validation passed"
    else:
        model = load_json(args.model)
        diagnostics = validate_fixture(artifact, model)
        record_count = len(artifact.get("records", [])) if isinstance(artifact, dict) else 0
        output = f"safety/performance gate fixture validation passed: {record_count} records"

    if args.expect_failure:
        for code, message in diagnostics:
            if code == args.expect_failure:
                print(f"expected diagnostic observed: {code}")
                print(message)
                return 0
        observed = ", ".join(code for code, _ in diagnostics) or "none"
        print(
            f"expected diagnostic {args.expect_failure} was not observed; observed: {observed}",
            file=sys.stderr,
        )
        return 1

    if diagnostics:
        for code, message in diagnostics:
            print(f"{code}: {message}", file=sys.stderr)
        return 1

    print(output)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
