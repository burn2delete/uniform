"""Phase 14 testing, verification, and conformance validation."""

from __future__ import annotations

import hashlib
import json
from pathlib import Path
from typing import Any


DOCUMENT_COMPONENTS = {
    "TEST1": "language_conformance",
    "TEST2": "compiler_tests",
    "TEST3": "runtime_tests",
    "TEST4": "profile_compliance",
    "TEST5": "safety_conformance",
    "TEST6": "backend_conformance",
    "TEST7": "stdlib_tests",
    "TEST8": "ai_workflow_eval",
    "TEST9": "fuzz_property_suite",
    "TEST10": "differential_report",
    "TEST11": "formal_proof_report",
    "TEST12": "performance_regression",
    "TEST13": "self_hosting_validation",
}


class ConformanceSystemError(Exception):
    def __init__(
        self,
        code: str,
        message: str,
        *,
        record: dict[str, Any] | None = None,
        source: str,
        missing_fact: str,
        remediation: str,
    ) -> None:
        super().__init__(message)
        self.code = code
        self.message = message
        self.record = record or {}
        self.source = source
        self.missing_fact = missing_fact
        self.remediation = remediation

    def to_diagnostic(self) -> dict[str, Any]:
        return {
            "id": self.code,
            "message": self.message,
            "document": self.record.get("document"),
            "suite_id": self.record.get("suite_id"),
            "fixture_id": self.record.get("fixture_id"),
            "artifact_id": self.record.get("artifact_id"),
            "profile": self.record.get("profile"),
            "target": self.record.get("target"),
            "backend": self.record.get("backend"),
            "runtime": self.record.get("runtime"),
            "source_span": self.record.get("source_span", {"source": self.source}),
            "missing_fact": self.missing_fact,
            "remediation": self.remediation,
            "analyzer_stage": "phase14-conformance-validation",
        }


def validate_conformance_system_file(path: Path) -> dict[str, Any]:
    return validate_conformance_system_manifest(load_manifest_file(path), str(path))


def conformance_system_diagnostic(path: Path) -> dict[str, Any] | None:
    try:
        validate_conformance_system_file(path)
    except ConformanceSystemError as exc:
        return exc.to_diagnostic()
    return None


def validate_conformance_system_manifest(manifest: dict[str, Any], source: str) -> dict[str, Any]:
    if manifest.get("kind") != "conformance-system-input":
        raise_error("TEST1001", "conformance input has wrong kind", {}, source, "conformance-system-input")
    components = {doc: require_dict(manifest, key, "TEST1001", source) for doc, key in DOCUMENT_COMPONENTS.items()}
    validate_language_conformance(components["TEST1"], source)
    validate_compiler_tests(components["TEST2"], source)
    validate_runtime_tests(components["TEST3"], source)
    validate_profile_compliance(components["TEST4"], source)
    validate_safety_conformance(components["TEST5"], source)
    validate_backend_conformance(components["TEST6"], source)
    validate_stdlib_tests(components["TEST7"], source)
    validate_ai_workflow_eval(components["TEST8"], source)
    validate_fuzz_property_suite(components["TEST9"], source)
    validate_differential_report(components["TEST10"], source)
    validate_formal_proof_report(components["TEST11"], source)
    validate_performance_regression(components["TEST12"], source)
    validate_self_hosting_validation(components["TEST13"], source)
    return {
        "kind": "conformance-system-artifact",
        "phase": "14",
        "conformance_harness": components["TEST1"],
        "fixture_manifest": components["TEST1"].get("fixture_index"),
        "golden_diagnostics": components["TEST2"].get("diagnostic_goldens"),
        "fuzz_property_suite": components["TEST9"],
        "differential_report": components["TEST10"],
        "formal_proof_report": components["TEST11"],
        "performance_regression_report": components["TEST12"],
        "document_contracts": components,
        "coverage_summary": {
            "documents": len(DOCUMENT_COMPONENTS),
            "tasks": 6,
            "suite_count": len(DOCUMENT_COMPONENTS),
            "status": ":passed",
        },
        "input_hash": artifact_hash(manifest),
        "diagnostics": [],
    }


def validate_language_conformance(record: dict[str, Any], source: str) -> None:
    require_fields(record, ["suite_id", "fixture_index", "positive_fixtures", "negative_fixtures", "profiles", "targets", "golden_reader_output", "golden_typed_core_output", "diagnostic_json", "result_summary"], "TEST1001", source)
    if record.get("malformed_metadata"):
        raise_error("TEST1001", "fixture metadata is malformed", record, source, "fixture-metadata")
    if record.get("output_mismatch"):
        raise_error("TEST1002", "language output differs from golden", record, source, "language-output")
    if record.get("diagnostic_mismatch"):
        raise_error("TEST1003", "language diagnostic differs from golden", record, source, "diagnostic-json")
    if record.get("lost_syntax_metadata"):
        raise_error("TEST1004", "macro or syntax fixture lost source metadata", record, source, "syntax-metadata")
    if record.get("undeclared_capability"):
        raise_error("TEST1005", "fixture uses undeclared capability", record, source, "fixture-capability")
    if record.get("profile_portable_mixup"):
        raise_error("TEST1006", "profile-specific behavior merged into portable suite", record, source, "profile-portable-boundary")


def validate_compiler_tests(record: dict[str, Any], source: str) -> None:
    require_fields(record, ["suite_id", "stage_goldens", "preservation_reports", "pass_evidence", "diagnostic_goldens", "incremental_cache_traces", "plugin_denial_reports", "crash_reducers"], "TEST2001", source)
    if record.get("stage_golden_mismatch"):
        raise_error("TEST2001", "compiler stage golden mismatch", record, source, "stage-golden")
    if record.get("lost_preserved_fact"):
        raise_error("TEST2002", "compiler pass lost preserved fact", record, source, "preserved-fact")
    if record.get("unsound_optimization"):
        raise_error("TEST2003", "optimization accepted unsound rewrite", record, source, "optimization-evidence")
    if record.get("invalid_cache_reuse"):
        raise_error("TEST2004", "incremental cache reused invalid entry", record, source, "cache-invalidation")
    if record.get("plugin_authority_violation"):
        raise_error("TEST2005", "compiler plugin used ambient authority", record, source, "plugin-authority")
    if record.get("diagnostic_golden_mismatch"):
        raise_error("TEST2006", "compiler diagnostic golden mismatch", record, source, "diagnostic-golden")


def validate_runtime_tests(record: dict[str, Any], source: str) -> None:
    require_fields(record, ["suite_id", "runtime_families", "profile", "target", "artifact_id", "capability_decision_log", "memory_safety_report", "concurrency_trace", "replay_trace", "ai_ledger_report", "ffi_boundary_report", "observability_schema_report"], "TEST3001", source)
    if record.get("missing_runtime_family"):
        raise_error("TEST3001", "runtime fixture lacks runtime family", record, source, "runtime-family")
    if record.get("capability_enforcement_failure"):
        raise_error("TEST3002", "missing runtime grant did not deny operation", record, source, "capability-denial")
    if record.get("replay_violation"):
        raise_error("TEST3003", "runtime replay repeats side effects", record, source, "runtime-replay")
    if record.get("missing_ai_ledger"):
        raise_error("TEST3004", "AI runtime test lacks model/tool ledger", record, source, "ai-ledger")
    if record.get("observability_redaction_failure"):
        raise_error("TEST3005", "observability event leaked secret", record, source, "observability-redaction")
    if record.get("ffi_abi_mismatch"):
        raise_error("TEST3006", "FFI test lacks matching ABI identity", record, source, "ffi-abi")


def validate_profile_compliance(record: dict[str, Any], source: str) -> None:
    require_fields(record, ["suite_id", "profile_matrix", "target_matrix", "positive_results", "negative_results", "capability_legality_report", "runtime_service_legality_report", "artifact_delegation_report"], "TEST4001", source)
    if record.get("missing_profile_target"):
        raise_error("TEST4001", "profile fixture lacks profile or target", record, source, "profile-target")
    if record.get("accepted_forbidden_feature"):
        raise_error("TEST4002", "forbidden profile feature was accepted", record, source, "forbidden-profile-feature")
    if record.get("rejected_allowed_feature"):
        raise_error("TEST4003", "allowed profile feature was rejected", record, source, "allowed-profile-feature")
    if record.get("missing_delegation_artifact"):
        raise_error("TEST4004", "delegated profile behavior lacks artifact", record, source, "delegation-artifact")
    if record.get("multi_profile_conflict"):
        raise_error("TEST4005", "multi-profile compatibility conflict unresolved", record, source, "profile-compatibility")
    if record.get("severity_mismatch"):
        raise_error("TEST4006", "profile diagnostic severity differs from contract", record, source, "diagnostic-severity")


def validate_safety_conformance(record: dict[str, Any], source: str) -> None:
    require_fields(record, ["suite_id", "safety_outcomes", "positive_fixtures", "negative_fixtures", "unsafe_audit_report", "runtime_check_report", "proof_certificate_report", "capability_denial_report", "taint_flow_report", "check_elision_report"], "TEST5001", source)
    if record.get("safe_code_unsound"):
        raise_error("TEST5001", "safe-code fixture compiles to undefined behavior", record, source, "safe-code-soundness")
    if record.get("missing_unsafe_audit"):
        raise_error("TEST5002", "unsafe fixture lacks audit artifact", record, source, "unsafe-audit")
    if record.get("invalid_check_elision"):
        raise_error("TEST5003", "safety check removed without proof", record, source, "check-elision-proof")
    if record.get("capability_bypass"):
        raise_error("TEST5004", "capability bypass accepted at runtime", record, source, "capability-bypass")
    if record.get("taint_sink_violation"):
        raise_error("TEST5005", "taint sink lacks validation", record, source, "taint-validation")
    if record.get("ai_tool_safety_failure"):
        raise_error("TEST5006", "AI prompt or tool misuse was not denied", record, source, "ai-tool-denial")
    if record.get("missing_safety_span"):
        raise_error("TEST5007", "safety report omits source span", record, source, "safety-source-span")


def validate_backend_conformance(record: dict[str, Any], source: str) -> None:
    require_fields(record, ["suite_id", "backend_matrix", "profile", "target", "backend", "runtime", "artifact_manifests", "execution_outputs", "diagnostic_json", "source_map_report", "abi_layout_report", "differential_comparison"], "TEST6001", source)
    if record.get("identity_gap"):
        raise_error("TEST6001", "backend fixture lacks backend or target identity", record, source, "backend-target-identity")
    if record.get("semantic_mismatch"):
        raise_error("TEST6002", "lowering output changes observable semantics", record, source, "backend-semantics")
    if record.get("unsupported_diagnostic_mismatch"):
        raise_error("TEST6003", "unsupported operation diagnostic mismatch", record, source, "unsupported-diagnostic")
    if record.get("artifact_manifest_gap"):
        raise_error("TEST6004", "backend artifact lacks manifest", record, source, "artifact-manifest")
    if record.get("source_map_mismatch"):
        raise_error("TEST6005", "target source map cannot map diagnostics to Gravity spans", record, source, "source-map")
    if record.get("abi_layout_mismatch"):
        raise_error("TEST6006", "ABI or layout report mismatches target declaration", record, source, "abi-layout")
    if record.get("runtime_grant_gap"):
        raise_error("TEST6007", "backend runtime effect lacks grant", record, source, "runtime-grant")


def validate_stdlib_tests(record: dict[str, Any], source: str) -> None:
    require_fields(record, ["suite_id", "module_reports", "profile_availability_matrix", "capability_denial_reports", "property_test_reports", "documentation_example_reports", "stability_report"], "TEST7001", source)
    if record.get("untested_public_api"):
        raise_error("TEST7001", "public API lacks test or evidence", record, source, "public-api-test")
    if record.get("missing_capability_denial"):
        raise_error("TEST7002", "effectful API lacks missing-capability denial fixture", record, source, "capability-denial")
    if record.get("profile_availability_mismatch"):
        raise_error("TEST7003", "profile availability claim lacks fixture", record, source, "profile-availability")
    if record.get("unsafe_wrapper_gap"):
        raise_error("TEST7004", "unsafe wrapper API lacks safety test", record, source, "unsafe-wrapper-test")
    if record.get("doc_example_failure"):
        raise_error("TEST7005", "runnable documentation example failed", record, source, "doc-example")
    if record.get("stability_break"):
        raise_error("TEST7006", "breaking API change lacks stability handling", record, source, "stability-policy")


def validate_ai_workflow_eval(record: dict[str, Any], source: str) -> None:
    require_fields(record, ["suite_id", "eval_report", "dataset_manifest", "scored_outputs", "replay_traces", "tool_memory_ledgers", "human_review_records", "safety_probe_report", "budget_report", "release_gate_decision"], "TEST8001", source)
    if record.get("missing_eval_report"):
        raise_error("TEST8001", "AI or workflow release lacks eval report", record, source, "eval-report")
    if record.get("live_provider_policy_gap"):
        raise_error("TEST8002", "live-provider eval lacks budget or credential policy", record, source, "live-provider-policy")
    if record.get("missing_replay_trace"):
        raise_error("TEST8003", "workflow eval lacks replay trace", record, source, "workflow-replay")
    if record.get("structured_output_failure"):
        raise_error("TEST8004", "AI output failed schema validation", record, source, "structured-output-schema")
    if record.get("missing_safety_probe"):
        raise_error("TEST8005", "AI eval lacks safety probe", record, source, "safety-probe")
    if record.get("stale_eval_subject"):
        raise_error("TEST8006", "eval subject hash differs from release candidate", record, source, "eval-subject-hash")
    if record.get("human_review_gap"):
        raise_error("TEST8007", "human-review eval lacks denial or expiry case", record, source, "human-review-coverage")


def validate_fuzz_property_suite(record: dict[str, Any], source: str) -> None:
    require_fields(record, ["suite_id", "seed_corpus", "generated_case_manifest", "property_report", "minimized_reproducers", "crash_report", "coverage_report", "promoted_regressions"], "TEST9001", source)
    if record.get("missing_seed"):
        raise_error("TEST9001", "fuzz target lacks seed or generator identity", record, source, "seed-generator")
    if record.get("unreproducible_failure"):
        raise_error("TEST9002", "fuzz failure lacks reproducible minimized case", record, source, "minimized-reproducer")
    if record.get("forbidden_live_effect"):
        raise_error("TEST9003", "generator produced live effect without policy", record, source, "live-effect-policy")
    if record.get("compiler_crash"):
        raise_error("TEST9004", "fuzz case crashed compiler where diagnostic expected", record, source, "crash-diagnostic")
    if record.get("missing_oracle"):
        raise_error("TEST9005", "property claim lacks oracle", record, source, "property-oracle")
    if record.get("missing_profile_target"):
        raise_error("TEST9006", "fuzz report lacks profile or target", record, source, "profile-target")


def validate_differential_report(record: dict[str, Any], source: str) -> None:
    require_fields(record, ["suite_id", "oracle_manifest", "observables", "profile", "target", "backend", "runtime", "numeric_mode", "comparison_report", "diagnostic_comparison", "artifact_comparison", "minimized_reproducer", "accepted_divergence"], "TEST10001", source)
    if record.get("missing_observable"):
        raise_error("TEST10001", "differential test lacks observable definition", record, source, "observable")
    if record.get("unexplained_divergence"):
        raise_error("TEST10002", "target divergence is unexplained", record, source, "accepted-divergence")
    if record.get("numeric_mode_omission"):
        raise_error("TEST10003", "numeric differential test lacks numeric mode", record, source, "numeric-mode")
    if record.get("live_nondeterminism"):
        raise_error("TEST10004", "AI or workflow differential test used live nondeterminism", record, source, "recorded-trace")
    if record.get("oracle_identity_gap"):
        raise_error("TEST10005", "oracle output lacks compiler or artifact version", record, source, "oracle-identity")
    if record.get("diagnostic_drift"):
        raise_error("TEST10006", "diagnostic category drift was ignored", record, source, "diagnostic-category")


def validate_formal_proof_report(record: dict[str, Any], source: str) -> None:
    require_fields(record, ["suite_id", "semantics_artifacts", "proof_objects", "certificate_manifests", "checker_reports", "assumption_manifests", "counterexamples", "proof_invalidation_reports"], "TEST11001", source)
    if record.get("missing_assumption"):
        raise_error("TEST11001", "proof artifact lacks assumptions", record, source, "proof-assumption")
    if record.get("stale_proof"):
        raise_error("TEST11002", "proof is stale against source, spec, compiler, or dependency", record, source, "proof-freshness")
    if record.get("uncheckable_claim"):
        raise_error("TEST11003", "release gate uses uncheckable proof claim", record, source, "machine-checkable-proof")
    if record.get("missing_math_certificate"):
        raise_error("TEST11004", "math equivalence lacks checked certificate", record, source, "math-certificate")
    if record.get("pass_linkage_gap"):
        raise_error("TEST11005", "optimization proof lacks exact pass input/output link", record, source, "pass-proof-link")
    if record.get("formal_obligation_unmet"):
        raise_error("TEST11006", "formal profile proof obligation is unmet", record, source, "formal-obligation")
    if record.get("unknown_checker"):
        raise_error("TEST11007", "proof checker identity is unknown", record, source, "checker-identity")


def validate_performance_regression(record: dict[str, Any], source: str) -> None:
    require_fields(record, ["suite_id", "benchmark_manifest", "raw_measurements", "performance_report", "comparison_report", "regression_decision", "semantic_gate_report", "safety_evidence_links"], "TEST12001", source)
    if record.get("missing_benchmark_identity"):
        raise_error("TEST12001", "benchmark result lacks environment or artifact identity", record, source, "benchmark-identity")
    if record.get("incompatible_comparison"):
        raise_error("TEST12002", "performance comparison artifacts are incompatible", record, source, "comparison-policy")
    if record.get("semantic_gate_failure"):
        raise_error("TEST12003", "performance pass accepted despite semantic gate failure", record, source, "semantic-gate")
    if record.get("unsafe_check_elision_speedup"):
        raise_error("TEST12004", "check-elision speedup lacks safety evidence", record, source, "check-elision-evidence")
    if record.get("unsupported_counter"):
        raise_error("TEST12005", "target counter unavailable on declared device", record, source, "target-counter")
    if record.get("opaque_ai_cost"):
        raise_error("TEST12006", "AI latency report collapses provider and tool costs", record, source, "ai-cost-breakdown")


def validate_self_hosting_validation(record: dict[str, Any], source: str) -> None:
    require_fields(record, ["suite_id", "stage_manifest", "stage_compiler_artifact", "conformance_report", "rebuild_log", "stage_comparison_report", "provenance_attestation", "tcb_delta", "unsafe_audit_report"], "TEST13001", source)
    if record.get("missing_stage_conformance"):
        raise_error("TEST13001", "stage advancement lacks conformance suite pass", record, source, "stage-conformance")
    if record.get("provenance_gap"):
        raise_error("TEST13002", "bootstrap artifact lacks provenance", record, source, "bootstrap-provenance")
    if record.get("unexplained_divergence"):
        raise_error("TEST13003", "stage comparison has unexplained output divergence", record, source, "stage-divergence")
    if record.get("diagnostic_regression"):
        raise_error("TEST13004", "self-hosted diagnostics lose spans or stable codes", record, source, "diagnostic-preservation")
    if record.get("unreproducible_compiler"):
        raise_error("TEST13005", "compiler artifact is unreproducible under policy", record, source, "compiler-reproducibility")
    if record.get("unsafe_audit_gap"):
        raise_error("TEST13006", "compiler unsafe code lacks audit metadata", record, source, "compiler-unsafe-audit")
    if record.get("missing_tcb_delta"):
        raise_error("TEST13007", "trusted computing base delta is missing", record, source, "tcb-delta")


def require_dict(manifest: dict[str, Any], key: str, code: str, source: str) -> dict[str, Any]:
    value = manifest.get(key)
    if not isinstance(value, dict):
        raise_error(code, f"missing component {key}", {}, source, key)
    return value


def require_fields(record: dict[str, Any], fields: list[str], code: str, source: str) -> None:
    missing = [field for field in fields if field not in record]
    if missing:
        raise_error(code, "record is missing required fields", record, source, ",".join(missing))


def raise_error(code: str, message: str, record: dict[str, Any], source: str, missing_fact: str) -> None:
    raise ConformanceSystemError(
        code,
        message,
        record=record,
        source=source,
        missing_fact=missing_fact,
        remediation="Update the Phase 14 conformance fixture, suite artifact, diagnostic, or proof evidence.",
    )


def load_manifest_file(path: Path) -> dict[str, Any]:
    raw = json.loads(path.read_text(encoding="utf-8"))
    if raw.get("kind") != "conformance-system-fixture-patch":
        return raw
    base_value = raw.get("base")
    if not base_value:
        raise_error("TEST1001", "fixture patch lacks base manifest", {}, str(path), "base-manifest")
    base_path = Path(base_value)
    if not base_path.is_absolute():
        base_path = Path(__file__).resolve().parents[2] / base_path
    base = load_manifest_file(base_path)
    patched = deep_merge(base, raw.get("patch", {}))
    patched.setdefault("fixture_patch", str(path))
    return patched


def deep_merge(base: dict[str, Any], patch: dict[str, Any]) -> dict[str, Any]:
    merged = dict(base)
    for key, value in patch.items():
        if isinstance(value, dict) and isinstance(merged.get(key), dict):
            merged[key] = deep_merge(merged[key], value)
        else:
            merged[key] = value
    return merged


def artifact_hash(value: Any) -> str:
    data = json.dumps(value, sort_keys=True, separators=(",", ":"))
    return "sha256:" + hashlib.sha256(data.encode("utf-8")).hexdigest()
