"""Phase 04 performance contract and evidence validation."""

from __future__ import annotations

import hashlib
import json
from pathlib import Path
from typing import Any


REQUIRED_CLAIM_FIELDS = {
    "claim_id",
    "profile",
    "target",
    "runtime",
    "input_domain",
    "layout",
    "safety_mode",
    "benchmark",
    "proof_obligations",
    "artifacts",
    "target_fingerprint",
}
REQUIRED_TARGET_FINGERPRINT_FIELDS = {
    "hardware",
    "features",
    "os",
    "compiler",
    "backend",
    "runtime_provider",
    "memory_provider",
    "numeric_provider",
    "build_flags",
}
REQUIRED_PRESERVED_FACTS = {
    "types",
    "effects",
    "ownership",
    "capabilities",
    "initialization",
    "bounds",
    "taint",
    "numeric",
    "unsafe_audit",
    "profile",
}
REQUIRED_CONFORMANCE = {
    "zero_cost_families": {
        ":protocol-dispatch",
        ":generic-function",
        ":iterator-pipeline",
        ":record-wrapper",
        ":runtime-check-erasure",
    },
    "specialization_modes": {":type", ":const", ":shape", ":profile", ":target"},
    "layout_transformations": {":field-reorder", ":packing", ":alignment", ":aos-to-soa", ":hot-cold-split"},
    "benchmark_failure_cases": {":noise", ":drift", ":safety-gate-failure", ":baseline-review"},
    "pgo_statuses": {":accepted", ":advisory", ":stale", ":rejected", ":required-missing"},
    "autotuning_cases": {":candidate-expansion", ":candidate-rejection", ":guard-overlap", ":fallback"},
    "simd_cases": {":lane-proof", ":tail-handling", ":strict-fp-reject", ":volatile-reject", ":intrinsic-guard"},
    "realtime_cases": {":latency-budget", ":loop-bound", ":allocation-free", ":blocking-reject"},
    "check_classes": {
        ":bounds",
        ":integer-overflow",
        ":division-by-zero",
        ":shift-count",
        ":null-option",
        ":initialization",
        ":region-lifetime",
        ":borrow-alias",
        ":linear-resource",
        ":data-race",
        ":taint-sink",
        ":capability-effect",
        ":unsafe-audit",
        ":workflow-replay",
        ":ai-human-review",
    },
}
POLICY_CHECKS = {":capability-effect", ":unsafe-audit", ":workflow-replay", ":ai-human-review"}


class PerformanceValidationError(Exception):
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
            "claim_id": self.record.get("claim_id") or self.record.get("id"),
            "optimization_pass": self.record.get("pass_id") or self.record.get("optimization"),
            "profile": self.record.get("profile"),
            "target": self.record.get("target"),
            "source_span": self.record.get("source_span", {"source": self.source}),
            "artifact_node": self.record.get("artifact") or self.record.get("variant_id"),
            "missing_evidence": self.missing_fact,
            "remediation": self.remediation,
            "analyzer_stage": "phase04-performance-validation",
        }


def validate_performance_file(path: Path) -> dict[str, Any]:
    return validate_performance_manifest(load_manifest_file(path), str(path))


def performance_diagnostic(path: Path) -> dict[str, Any] | None:
    try:
        validate_performance_file(path)
    except PerformanceValidationError as exc:
        return exc.to_diagnostic()
    return None


def validate_performance_manifest(manifest: dict[str, Any], source: str) -> dict[str, Any]:
    if manifest.get("kind") != "performance-model-input":
        raise_error(
            "PERF1-CLAIM",
            "performance input has the wrong artifact kind",
            {},
            source,
            "performance-model-input",
        )

    claim = require_dict(manifest, "performance_claim", "PERF1-CLAIM", source)
    validate_claim(claim, source)
    decisions = require_list(manifest, "optimization_decisions", "PERF1-EVIDENCE", source)
    for decision in decisions:
        validate_optimization_decision(decision, source)
    zero_cost_claims = require_list(manifest, "zero_cost_claims", "PERF2-CLAIM", source)
    for item in zero_cost_claims:
        validate_zero_cost_claim(item, source)
    specializations = require_list(manifest, "specializations", "PERF3-KEY", source)
    for item in specializations:
        validate_specialization(item, source)
    layouts = require_list(manifest, "layouts", "PERF4-LAYOUT", source)
    for item in layouts:
        validate_layout(item, source)
    benchmarks = require_list(manifest, "benchmarks", "PERF5-MANIFEST", source)
    for item in benchmarks:
        validate_benchmark(item, source)
    pgo_records = require_list(manifest, "pgo_records", "PERF6-IDENTITY", source)
    for item in pgo_records:
        validate_pgo_record(item, source)
    autotuning_records = require_list(manifest, "autotuning_records", "PERF7-CANDIDATE-SPACE", source)
    for item in autotuning_records:
        validate_autotuning_record(item, source)
    simd_records = require_list(manifest, "simd_records", "PERF8-LANE", source)
    for item in simd_records:
        validate_simd_record(item, source)
    realtime_contracts = require_list(manifest, "realtime_contracts", "PERF9-BUDGET", source)
    for item in realtime_contracts:
        validate_realtime_contract(item, source)
    check_elisions = require_list(manifest, "check_elisions", "PERF10-PROOF-MISSING", source)
    for item in check_elisions:
        validate_check_elision(item, source)
    target_features = require_list(manifest, "target_feature_records", "PERF1-TARGET", source)
    for item in target_features:
        validate_target_feature_record(item, source)
    validate_conformance_matrix(require_dict(manifest, "conformance_matrix", "PERF1-EVIDENCE", source), source)

    return {
        "kind": "performance-model-artifact",
        "phase": "04",
        "package": manifest.get("package"),
        "module": manifest.get("module"),
        "performance_contract_manifest": claim,
        "optimization_decision_log": decisions,
        "performance_claim_records": [claim],
        "target_feature_reports": target_features,
        "layout_records": layouts,
        "benchmark_reports": benchmarks,
        "proof_index": manifest.get("proof_index", {}),
        "generated_variant_manifest": manifest.get("variant_manifests", []),
        "zero_cost_conformance_results": zero_cost_claims,
        "specialization_records": specializations,
        "pgo_decision_log": pgo_records,
        "autotuning_selection_certificates": autotuning_records,
        "simd_cache_records": simd_records,
        "realtime_latency_reports": realtime_contracts,
        "check_elision_certificates": check_elisions,
        "conformance_matrix": manifest["conformance_matrix"],
        "input_hash": artifact_hash(manifest),
        "diagnostics": [],
    }


def validate_claim(claim: dict[str, Any], source: str) -> None:
    missing = sorted(field for field in REQUIRED_CLAIM_FIELDS if not claim.get(field))
    if missing:
        raise_error(
            "PERF1-CLAIM",
            f"performance claim lacks required fields: {missing}",
            claim,
            source,
            ",".join(missing),
        )
    fingerprint = claim.get("target_fingerprint", {})
    missing_fingerprint = sorted(field for field in REQUIRED_TARGET_FINGERPRINT_FIELDS if not fingerprint.get(field))
    if missing_fingerprint:
        raise_error(
            "PERF1-TARGET",
            f"performance claim lacks target fingerprint fields: {missing_fingerprint}",
            claim,
            source,
            ",".join(missing_fingerprint),
        )
    if not claim.get("semantic_proof") or not claim.get("safety_proof"):
        raise_error(
            "PERF1-EVIDENCE",
            "performance claim lacks semantic or safety proof evidence",
            claim,
            source,
            "semantic-and-safety-proof",
        )


def validate_optimization_decision(record: dict[str, Any], source: str) -> None:
    preserved = set(record.get("preserved_facts", [])) | set(record.get("regenerated_facts", []))
    missing = sorted(REQUIRED_PRESERVED_FACTS - preserved)
    if missing:
        code = "PERF1-SAFETY"
        if "effects" in missing:
            code = "PERF1-EFFECT"
        elif "capabilities" in missing:
            code = "PERF1-CAPABILITY"
        elif "numeric" in missing:
            code = "PERF1-NUMERIC"
        raise_error(code, "optimization decision lost required facts", record, source, ",".join(missing))
    if record.get("lost_safety_facts"):
        raise_error(
            "PERF1-SAFETY",
            "optimization decision loses safety facts",
            record,
            source,
            ",".join(record["lost_safety_facts"]),
        )
    if record.get("erased_checks") and not record.get("proofs"):
        raise_error(
            "PERF1-SAFETY",
            "optimization erased checks without proof records",
            record,
            source,
            "proofs",
        )


def validate_zero_cost_claim(record: dict[str, Any], source: str) -> None:
    required = ["abstraction", "equivalent_form", "expected_erased_costs", "erased_costs", "before_ir", "after_ir", "proofs"]
    missing = [field for field in required if not record.get(field)]
    if missing:
        raise_error("PERF2-CLAIM", "zero-cost claim is incomplete", record, source, ",".join(missing))
    if record.get("claimed_zero_cost") and record.get("residual_costs"):
        raise_error(
            "PERF2-RESIDUAL",
            "residual work contradicts a zero-cost claim",
            record,
            source,
            ",".join(record.get("residual_costs", [])),
        )
    hidden = set(record.get("hidden_costs", []))
    if hidden:
        code_by_cost = {
            ":allocation": "PERF2-ALLOCATION",
            ":boxing": "PERF2-BOXING",
            ":dynamic-dispatch": "PERF2-DISPATCH",
            ":reflection": "PERF2-REFLECTION",
            ":runtime-check": "PERF2-CHECK",
        }
        code = code_by_cost.get(sorted(hidden)[0], "PERF2-RESIDUAL")
        raise_error(code, "zero-cost claim hides residual work", record, source, ",".join(sorted(hidden)))


def validate_specialization(record: dict[str, Any], source: str) -> None:
    key = record.get("key", {})
    behavior_facts = set(record.get("behavior_facts", []))
    missing_key = sorted(fact for fact in behavior_facts if fact not in key)
    if missing_key:
        raise_error("PERF3-KEY", "specialization key omits behavior-affecting facts", record, source, ",".join(missing_key))
    if not record.get("guard"):
        raise_error("PERF3-GUARD", "specialized variant lacks guard predicate", record, source, "guard")
    partial = record.get("partial_evaluation", {})
    ungranted = sorted(set(partial.get("build_effects", [])) - set(partial.get("grants", [])))
    if ungranted:
        raise_error("PERF3-EFFECT", "partial evaluation uses ungranted build effects", record, source, ",".join(ungranted))
    if partial and not partial.get("hermetic"):
        raise_error("PERF3-HERMETIC", "partial evaluation is not hermetic or replayable", record, source, "hermetic-replay")
    if not record.get("source_map"):
        raise_error("PERF3-SOURCE-MAP", "specialized artifact lacks source map", record, source, "source-map")
    missing_cache = sorted(set(record.get("invalidation_inputs", [])) - set(record.get("cache_key_inputs", [])))
    if missing_cache:
        raise_error("PERF3-CACHE", "specialization cache omits invalidation inputs", record, source, ",".join(missing_cache))
    if record.get("erased_checks") and not record.get("proof_id"):
        raise_error("PERF3-PROOF", "specialization erased checks without specialized proof", record, source, "proof-id")


def validate_layout(record: dict[str, Any], source: str) -> None:
    required = ["type", "profile", "target", "layout", "fields", "alignment", "abi", "proofs"]
    missing = [field for field in required if not record.get(field)]
    if missing:
        raise_error("PERF4-LAYOUT", "layout manifest is incomplete", record, source, ",".join(missing))
    if record.get("changes_public_boundary") and not record.get("adapters"):
        raise_error("PERF4-ABI", "layout transformation crosses a fixed boundary without an adapter", record, source, "boundary-adapter")
    if record.get("transformation") and not {":alias-proof", ":ownership-proof"} <= set(record.get("proofs", [])):
        raise_error("PERF4-ALIAS", "layout transformation lacks alias or ownership proof", record, source, "alias-ownership-proof")
    if record.get("address_identity_observable") and ":no-observable-address-identity" not in record.get("proofs", []) and not record.get("unsafe_audit"):
        raise_error("PERF4-ADDRESS", "layout transformation changes observable address identity", record, source, "address-identity-proof")
    if record.get("packed") and not record.get("access_safety_facts"):
        raise_error("PERF4-PACKED", "packed layout lacks access-safety facts", record, source, "access-safety-facts")
    if record.get("alignment") and not record.get("target_support", {}).get("alignment_supported", False):
        raise_error("PERF4-ALIGN", "layout alignment is unsupported by target", record, source, "target-alignment-support")
    cache = record.get("cache_claim", {})
    if cache and (not cache.get("target_fingerprint") or not cache.get("benchmark")):
        raise_error("PERF4-CACHE", "cache claim lacks target fingerprint or benchmark evidence", record, source, "cache-target-benchmark")


def validate_benchmark(record: dict[str, Any], source: str) -> None:
    required = ["benchmark_id", "profile", "target", "workload", "metric", "warmup", "samples", "statistics", "environment_fingerprint", "acceptance", "baseline"]
    missing = [field for field in required if not record.get(field)]
    if missing:
        raise_error("PERF5-MANIFEST", "benchmark manifest is incomplete", record, source, ",".join(missing))
    if not record.get("environment_fingerprint"):
        raise_error("PERF5-FINGERPRINT", "benchmark lacks environment fingerprint", record, source, "environment-fingerprint")
    gates = record.get("gates", {})
    if gates.get("safety") is not True:
        raise_error("PERF5-SAFETY-GATE", "benchmark ran before safety gates passed", record, source, "safety-gate")
    if gates.get("correctness") is not True:
        raise_error("PERF5-CORRECTNESS-GATE", "benchmark ran before correctness gates passed", record, source, "correctness-gate")
    if int(record.get("samples", 0)) < 20 or record.get("sample_summary", {}).get("variance", 0) > record.get("acceptance", {}).get("max_variance", 1):
        raise_error("PERF5-NOISE", "benchmark has insufficient samples or unstable variance", record, source, "samples-variance")
    if record.get("regression", {}).get("percent", 0) > record.get("acceptance", {}).get("max_regression_percent", 0) and not record.get("regression", {}).get("accepted"):
        raise_error("PERF5-REGRESSION", "benchmark exceeds regression threshold", record, source, "regression-review")
    baseline = record.get("baseline", {})
    if not baseline.get("reviewed"):
        raise_error("PERF5-BASELINE", "benchmark baseline update lacks review", record, source, "baseline-review")


def validate_pgo_record(record: dict[str, Any], source: str) -> None:
    identity = record.get("identity", {})
    required = ["id", "source_hash", "typed_core_hash", "mir_hash", "compiler", "profile", "target", "workload", "provider_versions"]
    missing = [field for field in required if not identity.get(field)]
    if missing:
        raise_error("PERF6-IDENTITY", "PGO data identity is incomplete", record, source, ",".join(missing))
    if record.get("status") == ":required-missing":
        raise_error("PERF6-DATA-MISSING", "required PGO data is missing", record, source, "profile-data")
    if record.get("status") == ":stale" and record.get("build_mode") == ":release":
        raise_error("PERF6-STALE", "stale PGO data drives a release optimization", record, source, "fresh-profile-data")
    privacy = record.get("privacy", {})
    if privacy.get("raw_sensitive_values") or not privacy.get("redacted"):
        raise_error("PERF6-PRIVACY", "PGO trace data violates privacy policy", record, source, "redacted-aggregate-data")
    for decision in record.get("decisions", []):
        if not decision.get("pass_id") or not decision.get("decision_log") or not decision.get("preserved_facts"):
            raise_error("PERF6-DECISION", "PGO decision lacks a pass decision log", decision, source, "decision-log")
        if not decision.get("reproducible"):
            raise_error("PERF6-REPRO", "PGO decision is not reproducible from inputs", decision, source, "reproducibility-record")


def validate_autotuning_record(record: dict[str, Any], source: str) -> None:
    if not record.get("candidate_space") or not record.get("candidates"):
        raise_error("PERF7-CANDIDATE-SPACE", "autotuning record lacks candidate-space declaration", record, source, "candidate-space")
    for candidate in record.get("candidates", []):
        if candidate.get("benchmarked") and candidate.get("evidence_status") != ":accepted":
            raise_error("PERF7-CANDIDATE-REJECTED", "invalid candidate was benchmarked", candidate, source, "candidate-evidence")
    guard_table = record.get("guard_table", {})
    if not guard_table.get("guards"):
        raise_error("PERF7-GUARD", "multiversion record lacks explicit guards", record, source, "variant-guards")
    if guard_table.get("overlap"):
        raise_error("PERF7-GUARD", "multiversion guards overlap", record, source, "non-overlapping-guards")
    if not guard_table.get("fallback"):
        raise_error("PERF7-FALLBACK", "multiversion guard table lacks valid fallback", record, source, "fallback")
    selected = record.get("selected_variant", {})
    if not selected.get("benchmark_comparison") or not selected.get("reason"):
        raise_error("PERF7-SELECTION", "selected variant lacks comparison evidence", record, source, "benchmark-comparison")
    if set(record.get("required_certificates", [])) - set(selected.get("certificates", [])):
        raise_error("PERF7-CERTIFICATE", "selected variant lacks required certificates", record, source, "variant-certificates")
    if not record.get("dispatch_overhead", {}).get("accounted"):
        raise_error("PERF7-DISPATCH", "dispatch overhead is not accounted for", record, source, "dispatch-overhead")
    if not record.get("reproducibility", {}).get("reproducible"):
        raise_error("PERF7-REPRO", "autotuning selection is not reproducible", record, source, "reproducibility")


def validate_simd_record(record: dict[str, Any], source: str) -> None:
    proofs = set(record.get("proofs", []))
    for proof, code in [
        (":lane-independence", "PERF8-LANE"),
        (":alias-proof", "PERF8-ALIAS"),
        (":bounds-proof", "PERF8-BOUNDS"),
        (":alignment-proof", "PERF8-ALIGN"),
    ]:
        if proof not in proofs:
            raise_error(code, "SIMD/vectorization record lacks required proof", record, source, proof)
    if not record.get("tail_handling"):
        raise_error("PERF8-TAIL", "SIMD record lacks tail handling", record, source, "tail-handling")
    if record.get("numeric_mode") == ":strict-f32" and record.get("reassociates"):
        raise_error("PERF8-NUMERIC", "strict numeric mode was reassociated", record, source, "numeric-mode-proof")
    if record.get("elementary_function") and not record.get("math_certificate"):
        raise_error("PERF8-MATH", "vector elementary function lacks math certificate", record, source, "math-certificate")
    if record.get("reorders_volatile"):
        raise_error("PERF8-VOLATILE", "cache transformation reorders volatile or synchronized access", record, source, "ordering-proof")
    for intrinsic in record.get("intrinsics", []):
        if not intrinsic.get("feature") and not intrinsic.get("fallback"):
            raise_error("PERF8-INTRINSIC", "intrinsic lacks target feature guard or fallback", record, source, "intrinsic-feature-fallback")
    cache = record.get("cache_transform", {})
    if cache and not cache.get("evidence"):
        raise_error("PERF8-CACHE", "cache transformation lacks evidence", record, source, "cache-evidence")


def validate_realtime_contract(record: dict[str, Any], source: str) -> None:
    required = ["contract_id", "profile", "target", "budget", "allocation", "blocking", "bounds", "preemption", "evidence", "failure_mode"]
    missing = [field for field in required if record.get(field) in (None, {}, [])]
    if missing:
        raise_error("PERF9-BUDGET", "latency contract is incomplete", record, source, ",".join(missing))
    budget = record.get("budget", {})
    if budget.get("max_us") is None or budget.get("jitter_us") is None:
        raise_error("PERF9-BUDGET", "latency contract lacks budget values", record, source, "latency-budget")
    bounds = record.get("bounds", {})
    if record.get("unbounded_loops") or bounds.get("iterations") in (None, ":unbounded"):
        raise_error("PERF9-LOOP", "realtime path contains unbounded loop", record, source, "loop-bound")
    if bounds.get("recursion") not in (False, 0, None):
        raise_error("PERF9-RECURSION", "realtime path contains unbounded recursion", record, source, "recursion-bound")
    if record.get("allocation") in {":heap", ":unbounded"}:
        raise_error("PERF9-ALLOC", "realtime path uses forbidden or unbounded allocation", record, source, "allocation-bound")
    runtime_services = set(record.get("runtime_services", []))
    if ":gc" in runtime_services:
        raise_error("PERF9-GC", "deterministic path depends on GC", record, source, "gc-absence-or-bound")
    if record.get("blocking") == ":unbounded":
        raise_error("PERF9-BLOCKING", "deterministic path has unbounded blocking", record, source, "blocking-bound")
    if record.get("locks", {}).get("bounded") is False:
        raise_error("PERF9-LOCK", "deterministic path has unbounded lock behavior", record, source, "lock-bound")
    if not record.get("preemption", {}).get("interrupt_policy"):
        raise_error("PERF9-PREEMPTION", "realtime path lacks interrupt/preemption assumptions", record, source, "preemption-policy")
    if not (record.get("worst_case_path") or record.get("empirical_bound")):
        raise_error("PERF9-EVIDENCE", "latency contract lacks worst-case or bounded empirical evidence", record, source, "latency-evidence")


def validate_check_elision(record: dict[str, Any], source: str) -> None:
    if not record.get("proof_id"):
        raise_error("PERF10-PROOF-MISSING", "erased check lacks proof", record, source, "proof-id")
    if record.get("proof_dominates_use") is not True:
        raise_error("PERF10-DOMINANCE", "proof does not dominate erased check", record, source, "dominance")
    if record.get("invalidated_by") and not (record.get("regenerated_proof") or record.get("residual_checks")):
        raise_error("PERF10-INVALIDATED", "invalidated proof was not regenerated or protected by residual checks", record, source, "regenerated-proof-or-residual-check")
    if record.get("partial_condition") and not record.get("residual_checks"):
        raise_error("PERF10-RESIDUAL", "partial proof lacks residual check report", record, source, "residual-checks")
    check_class = record.get("check_class")
    if (record.get("policy_check") or check_class in POLICY_CHECKS) and not record.get("policy_artifact"):
        raise_error("PERF10-POLICY", "policy check was removed without equivalent policy artifact", record, source, "policy-artifact")
    if record.get("backend_preserved") is not True:
        raise_error("PERF10-BACKEND", "backend cannot preserve proof assumptions", record, source, "backend-preservation")
    if not record.get("certificate"):
        raise_error("PERF10-CERTIFICATE", "check-elision certificate is missing", record, source, "certificate")
    if not record.get("source_map"):
        raise_error("PERF10-SOURCEMAP", "check elision lost source mapping", record, source, "source-map")


def validate_target_feature_record(record: dict[str, Any], source: str) -> None:
    if not record.get("target") or not record.get("features") or not record.get("fingerprint") or not record.get("guards"):
        raise_error("PERF1-TARGET", "target feature record is incomplete", record, source, "target-features-fingerprint-guards")


def validate_conformance_matrix(matrix: dict[str, Any], source: str) -> None:
    for key, required in REQUIRED_CONFORMANCE.items():
        observed = set(matrix.get(key, []))
        missing = sorted(required - observed)
        if missing:
            raise_error("PERF1-EVIDENCE", "performance conformance matrix is incomplete", matrix, source, f"{key}:{','.join(missing)}")


def require_dict(manifest: dict[str, Any], key: str, code: str, source: str) -> dict[str, Any]:
    value = manifest.get(key)
    if not isinstance(value, dict) or not value:
        raise_error(code, f"manifest lacks required object {key}", {}, source, key)
    return value


def require_list(manifest: dict[str, Any], key: str, code: str, source: str) -> list[dict[str, Any]]:
    value = manifest.get(key)
    if not isinstance(value, list) or not value:
        raise_error(code, f"manifest lacks required list {key}", {}, source, key)
    return value


def raise_error(code: str, message: str, record: dict[str, Any], source: str, missing_fact: str) -> None:
    raise PerformanceValidationError(
        code,
        message,
        record=record,
        source=source,
        missing_fact=missing_fact,
        remediation="Update the performance artifact, proof/certificate, target fingerprint, or rejected diagnostic fixture.",
    )


def load_manifest_file(path: Path) -> dict[str, Any]:
    manifest = json.loads(path.read_text(encoding="utf-8"))
    if "extends" not in manifest:
        return manifest
    base = load_manifest_file(path.parent / manifest["extends"])
    result = json.loads(json.dumps(base))
    for pointer in manifest.get("delete", []):
        delete_pointer(result, pointer)
    for pointer, value in manifest.get("set", {}).items():
        set_pointer(result, pointer, value)
    return result


def set_pointer(value: Any, pointer: str, replacement: Any) -> None:
    parent, key = pointer_parent(value, pointer)
    if isinstance(parent, list):
        parent[int(key)] = replacement
    else:
        parent[key] = replacement


def delete_pointer(value: Any, pointer: str) -> None:
    parent, key = pointer_parent(value, pointer)
    if isinstance(parent, list):
        del parent[int(key)]
    else:
        del parent[key]


def pointer_parent(value: Any, pointer: str) -> tuple[Any, str]:
    parts = pointer_parts(pointer)
    if not parts:
        raise ValueError("cannot address manifest root")
    current = value
    for part in parts[:-1]:
        current = current[int(part)] if isinstance(current, list) else current[part]
    return current, parts[-1]


def pointer_parts(pointer: str) -> list[str]:
    if not pointer.startswith("/"):
        raise ValueError(f"invalid JSON pointer: {pointer}")
    return [part.replace("~1", "/").replace("~0", "~") for part in pointer.split("/")[1:]]


def artifact_hash(value: Any) -> str:
    data = json.dumps(value, sort_keys=True, separators=(",", ":"))
    return "sha256:" + hashlib.sha256(data.encode("utf-8")).hexdigest()
