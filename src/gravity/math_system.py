"""Phase 05 math, EFIR, EML, proof, and conformance validation."""

from __future__ import annotations

import hashlib
import json
from pathlib import Path
from typing import Any


REQUIRED_NUMERIC_FAMILIES = {
    ":fixed-int",
    ":big-int",
    ":ratio",
    ":real",
    ":floating",
    ":complex",
    ":interval",
    ":symbolic",
    ":quantity",
}
REQUIRED_FLOATING_FIELDS = {
    "manifest_id",
    "operation",
    "format",
    "rounding",
    "numeric_mode",
    "precision",
    "exceptions",
    "nan",
    "infinity",
    "signed_zero",
    "denormals",
    "status_flags",
    "fma",
    "reassociation",
    "determinism",
}
REQUIRED_MATH_DOCUMENTS = {f"MATH{index}" for index in range(1, 11)}


class MathValidationError(Exception):
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
            "document_id": self.record.get("document"),
            "fixture_id": self.record.get("fixture_id") or self.record.get("id"),
            "graph_id": self.record.get("graph_id") or self.record.get("target_efir"),
            "candidate_id": self.record.get("candidate_id") or self.record.get("selected"),
            "profile": self.record.get("profile"),
            "target": self.record.get("target"),
            "mode": self.record.get("numeric_mode") or self.record.get("mode"),
            "source_span": self.record.get("source_span", {"source": self.source}),
            "missing_fact": self.missing_fact,
            "remediation": self.remediation,
            "analyzer_stage": "phase05-math-validation",
        }


def validate_math_file(path: Path) -> dict[str, Any]:
    return validate_math_manifest(load_manifest_file(path), str(path))


def math_diagnostic(path: Path) -> dict[str, Any] | None:
    try:
        validate_math_file(path)
    except MathValidationError as exc:
        return exc.to_diagnostic()
    return None


def validate_math_manifest(manifest: dict[str, Any], source: str) -> dict[str, Any]:
    if manifest.get("kind") != "math-system-input":
        raise_error("MATH11-FIXTURE", "math input has the wrong artifact kind", {}, source, "math-system-input")

    numeric_tower = require_dict(manifest, "numeric_tower", "MATH1-FAMILY", source)
    validate_numeric_tower(numeric_tower, source)
    elementary = require_dict(manifest, "elementary_registry", "MATH2-DECLARATION", source)
    validate_elementary_registry(elementary, source)
    efir_graphs = require_list(manifest, "efir_graphs", "MATH3-NODE", source)
    for graph in efir_graphs:
        validate_efir_graph(graph, source)
    eml_traces = require_list(manifest, "eml_traces", "MATH4-EFIR", source)
    for trace in eml_traces:
        validate_eml_trace(trace, source)
    certificates = require_list(manifest, "approximation_certificates", "MATH5-CERT-SHAPE", source)
    for certificate in certificates:
        validate_approximation_certificate(certificate, source)
    interval_proofs = require_list(manifest, "interval_proofs", "MATH6-CLAIM", source)
    for proof in interval_proofs:
        validate_interval_proof(proof, source)
    numeric_modes = require_dict(manifest, "numeric_modes", "MATH7-MISSING", source)
    validate_numeric_modes(numeric_modes, source)
    floating = require_list(manifest, "floating_manifests", "MATH8-MANIFEST", source)
    for item in floating:
        validate_floating_manifest(item, source)
    rewrites = require_dict(manifest, "rewrite_system", "MATH9-RULE-SHAPE", source)
    validate_rewrite_system(rewrites, source)
    optimizations = require_list(manifest, "optimization_decisions", "MATH10-CANDIDATE", source)
    for decision in optimizations:
        validate_optimization_decision(decision, source)
    conformance = require_dict(manifest, "conformance_suite", "MATH11-FIXTURE", source)
    validate_conformance_suite(conformance, source)
    validate_domain_ir_registration(require_dict(manifest, "domain_ir_registration", "MATH3-RUNTIME", source), source)

    return {
        "kind": "math-system-artifact",
        "phase": "05",
        "package": manifest.get("package"),
        "module": manifest.get("module"),
        "numeric_kind_lattice": numeric_tower.get("families", []),
        "conversion_rule_table": numeric_tower.get("conversion_rules", []),
        "profile_support_matrix": numeric_tower.get("profile_support", []),
        "numeric_mode_table": numeric_modes,
        "elementary_function_registry": elementary.get("declarations", []),
        "provider_eligibility_reports": elementary.get("provider_eligibility_reports", []),
        "efir_graphs": efir_graphs,
        "eml_traces": eml_traces,
        "approximation_certificates": certificates,
        "interval_proof_artifacts": interval_proofs,
        "floating_manifests": floating,
        "rewrite_rule_registry": rewrites.get("rules", []),
        "rewrite_trace_reports": rewrites.get("traces", []),
        "elementary_optimization_decisions": optimizations,
        "math_conformance_report": conformance,
        "domain_ir_registration": manifest["domain_ir_registration"],
        "input_hash": artifact_hash(manifest),
        "diagnostics": [],
    }


def validate_numeric_tower(record: dict[str, Any], source: str) -> None:
    families = set(record.get("families", []))
    missing = sorted(REQUIRED_NUMERIC_FAMILIES - families)
    if missing:
        raise_error("MATH1-FAMILY", "numeric tower lacks required families", record, source, ",".join(missing))
    if not record.get("profile_support"):
        raise_error("MATH1-PROFILE", "numeric tower lacks profile support matrix", record, source, "profile-support")
    for rule in record.get("conversion_rules", []):
        if rule.get("lossy") and rule.get("mode") == ":implicit":
            raise_error("MATH1-NARROW", "lossy numeric conversion is implicit", rule, source, "explicit-conversion-mode")
        if rule.get("precision_loss") and not rule.get("precision_record"):
            raise_error("MATH1-PRECISION", "conversion loses precision without a precision record", rule, source, "precision-record")
        if rule.get("rounding_required") and not rule.get("rounding"):
            raise_error("MATH1-ROUNDING", "conversion lacks rounding policy", rule, source, "rounding-policy")
    for equality in record.get("symbolic_equalities", []):
        if equality.get("claim") == ":equal" and not equality.get("proof"):
            raise_error("MATH1-EQUALITY", "symbolic equality is claimed without proof", equality, source, "proof-or-certificate")


def validate_elementary_registry(record: dict[str, Any], source: str) -> None:
    for declaration in record.get("declarations", []):
        required = ["function", "domain", "codomain", "semantic_form", "branch_policy", "exceptional_values", "numeric_modes", "providers"]
        missing = [field for field in required if not declaration.get(field)]
        if missing:
            raise_error("MATH2-DECLARATION", "elementary declaration is incomplete", declaration, source, ",".join(missing))
    decisions = record.get("selection_decisions", [])
    if not decisions:
        raise_error("MATH2-PROVIDER", "elementary registry lacks provider selection decisions", record, source, "selection-decisions")
    for decision in decisions:
        if not decision.get("efir_graph") or not decision.get("selected_provider"):
            raise_error("MATH2-PROVIDER", "provider selection lacks EFIR graph or selected provider", decision, source, "efir-provider-link")
        if decision.get("selected_status") != ":eligible":
            raise_error("MATH2-PROVIDER", "selected math provider is not eligible", decision, source, "eligible-provider")
        if decision.get("requires_certificate") and not decision.get("certificate"):
            raise_error("MATH2-CERTIFICATE", "selected provider lacks required certificate", decision, source, "certificate")
        if decision.get("provider_effects_outside_profile"):
            raise_error("MATH2-EFFECT", "provider effects exceed profile policy", decision, source, "profile-legal-effects")


def validate_efir_graph(graph: dict[str, Any], source: str) -> None:
    required = ["graph_id", "source_anchors", "nodes", "domain", "codomain", "numeric_mode", "precision", "branch_policy", "proof_obligations", "semantic_anchor"]
    missing = [field for field in required if not graph.get(field)]
    if missing:
        code = "MATH3-NODE"
        if "domain" in missing:
            code = "MATH3-DOMAIN"
        elif "codomain" in missing:
            code = "MATH3-CODOMAIN"
        elif "branch_policy" in missing:
            code = "MATH3-BRANCH"
        elif "precision" in missing:
            code = "MATH3-PRECISION"
        elif "source_anchors" in missing:
            code = "MATH3-SOURCE"
        raise_error(code, "EFIR graph is incomplete", graph, source, ",".join(missing))
    if graph.get("rewrite_claim") and not graph.get("rewrite_proof"):
        raise_error("MATH3-REWRITE", "EFIR rewrite claims equality without proof", graph, source, "rewrite-proof")
    if graph.get("runtime_selection") and graph.get("runtime_selection", {}).get("efir_anchor") != graph.get("graph_id"):
        raise_error("MATH3-RUNTIME", "runtime selection is not tied to EFIR graph", graph, source, "runtime-efir-anchor")


def validate_eml_trace(trace: dict[str, Any], source: str) -> None:
    required = ["artifact_id", "basis", "source_efir", "node_map", "domain", "branch_policy", "numeric_mode", "precision", "normalization_trace", "search_manifest", "candidates"]
    missing = [field for field in required if not trace.get(field)]
    if missing:
        raise_error("MATH4-EFIR", "EML artifact is missing semantic fields", trace, source, ",".join(missing))
    if trace.get("basis") != ":exp-minus-log":
        raise_error("MATH4-BASIS", "EML artifact uses unsupported basis", trace, source, "exp-minus-log")
    if not trace.get("trace_replayable"):
        raise_error("MATH4-TRACE", "EML normalization trace is not replayable", trace, source, "replayable-trace")
    manifest = trace.get("search_manifest", {})
    if not manifest.get("fuel") or not manifest.get("deterministic"):
        raise_error("MATH4-SEARCH", "EML search is unbounded or nondeterministic", trace, source, "bounded-deterministic-search")
    for candidate in trace.get("candidates", []):
        if candidate.get("used_for_lowering") and candidate.get("state") not in {":proved", ":bounded"}:
            raise_error("MATH4-CANDIDATE", "EML candidate was used before proof acceptance", candidate, source, "proved-or-bounded-candidate")
        if candidate.get("complex_intermediates") and not candidate.get("branch_policy"):
            raise_error("MATH4-COMPLEX", "complex EML candidate lacks branch policy", candidate, source, "branch-policy")


def validate_approximation_certificate(record: dict[str, Any], source: str) -> None:
    required = ["certificate_id", "target_efir", "function", "domain", "codomain", "numeric_mode", "precision", "branch_policy", "implementation", "error_proof", "target_assumptions", "checker"]
    missing = [field for field in required if not record.get(field)]
    if missing:
        raise_error("MATH5-CERT-SHAPE", "approximation certificate is malformed", record, source, ",".join(missing))
    precision = record.get("precision", {})
    error = record.get("error_proof", {})
    if error.get("combined", 0) > precision.get("absolute_error_max", float("inf")):
        raise_error("MATH5-APPROX-ERROR", "approximation error exceeds precision contract", record, source, "combined-error-bound")
    if error.get("roundoff") is None:
        raise_error("MATH5-ROUNDOFF", "certificate lacks separate roundoff bound", record, source, "roundoff-bound")
    checker = record.get("checker", {})
    if not checker.get("independent") or not checker.get("replayable"):
        raise_error("MATH5-CHECKER", "certificate checker is not independent or replayable", record, source, "independent-checker-replay")
    if record.get("target_assumptions", {}).get("satisfied") is not True:
        raise_error("MATH5-TARGET", "certificate target assumptions are not satisfied", record, source, "target-assumptions")


def validate_interval_proof(record: dict[str, Any], source: str) -> None:
    required = ["claim_id", "source", "claim", "domain", "branch_policy", "numeric_mode", "precision", "partition", "bound_ledger", "checker_transcript", "safe15_reference"]
    missing = [field for field in required if not record.get(field)]
    if missing:
        raise_error("MATH6-CLAIM", "interval proof is incomplete", record, source, ",".join(missing))
    if record.get("domain_exact") is not True:
        raise_error("MATH6-DOMAIN", "interval proof domain lacks exact endpoints", record, source, "exact-domain-endpoints")
    if record.get("outward_rounding_proved") is not True:
        raise_error("MATH6-ROUNDING", "interval proof lacks outward rounding proof", record, source, "outward-rounding-proof")
    partition = record.get("partition", {})
    if not partition.get("replayable"):
        raise_error("MATH6-PARTITION", "interval partition is not replayable", record, source, "replayable-partition")
    unresolved = partition.get("unresolved", [])
    if unresolved and not (record.get("residual_checks_allowed") and record.get("residual_check")):
        raise_error("MATH6-UNRESOLVED", "interval proof has unresolved cells without legal residual checks", record, source, "resolved-cells-or-residual-check")
    if not record.get("bound_ledger", {}).get("approximation") or not record.get("bound_ledger", {}).get("roundoff"):
        raise_error("MATH6-BOUND", "interval proof lacks separate approximation or roundoff bounds", record, source, "bound-ledger")


def validate_numeric_modes(record: dict[str, Any], source: str) -> None:
    if not record.get("mode_environment") or not record.get("precision_contracts"):
        raise_error("MATH7-MISSING", "numeric modes lack environment or precision contracts", record, source, "mode-environment-precision-contracts")
    for mode in record.get("mode_environment", []):
        if mode.get("target_default"):
            raise_error("MATH7-TARGET-DEFAULT", "numeric mode relies on implicit target defaults", mode, source, "explicit-target-contract")
        if mode.get("downgrade") and not mode.get("downgrade_authorized"):
            raise_error("MATH7-DOWNGRADE", "numeric mode downgrade lacks authorization", mode, source, "downgrade-authorization")
        if mode.get("mode") in {":certified-approx", ":faithful", ":correctly-rounded"} and not mode.get("precision"):
            raise_error("MATH7-PRECISION", "numeric mode lacks precision contract", mode, source, "precision-contract")


def validate_floating_manifest(record: dict[str, Any], source: str) -> None:
    missing = sorted(field for field in REQUIRED_FLOATING_FIELDS if not record.get(field))
    if missing:
        raise_error("MATH8-MANIFEST", "floating manifest is incomplete", record, source, ",".join(missing))
    if record.get("strict") and record.get("fma_transform") and not record.get("fma_proof"):
        raise_error("MATH8-FMA", "strict floating mode contracted FMA without proof", record, source, "fma-proof-or-relaxed-mode")
    if record.get("strict") and record.get("reassociation_transform") and not record.get("reassociation_proof"):
        raise_error("MATH8-REASSOC", "strict floating mode reassociated without proof", record, source, "reassociation-proof-or-relaxed-mode")
    if record.get("backend_lowering", {}).get("satisfies_manifest") is not True:
        raise_error("MATH8-BACKEND", "backend lowering cannot satisfy floating manifest", record, source, "manifest-preserving-lowering")


def validate_rewrite_system(record: dict[str, Any], source: str) -> None:
    if not record.get("rules") or not record.get("traces"):
        raise_error("MATH9-RULE-SHAPE", "rewrite system lacks rules or traces", record, source, "rules-traces")
    for rule in record.get("rules", []):
        required = ["rule_id", "pattern", "replacement", "domain", "branch_policy", "numeric_modes", "proof_status", "source"]
        missing = [field for field in required if not rule.get(field)]
        if missing:
            raise_error("MATH9-RULE-SHAPE", "rewrite rule is malformed", rule, source, ",".join(missing))
        if rule.get("used_for_code") and rule.get("proof_status") not in {":proved", ":bounded"}:
            raise_error("MATH9-PROOF", "unproved rewrite was used for code", rule, source, "proved-or-bounded-rule")
        if rule.get("tree_identity_equality"):
            raise_error("MATH9-EQUALITY", "tree identity was used as semantic equality", rule, source, "proof-artifact")
    if not record.get("termination", {}).get("bounded"):
        raise_error("MATH9-TERMINATION", "rewrite strategy is unbounded", record, source, "bounded-fuel-or-termination")
    egraph = record.get("egraph", {})
    if egraph and (not egraph.get("proof_replay") or egraph.get("uses_unproved_edge")):
        raise_error("MATH9-EGRAPH", "e-graph extraction lacks replay proof or uses unproved edge", record, source, "egraph-proof-replay")


def validate_optimization_decision(record: dict[str, Any], source: str) -> None:
    required = ["decision_id", "efir", "candidates", "selected", "proofs", "fallback", "backend_lowering"]
    missing = [field for field in required if not record.get(field)]
    if missing:
        raise_error("MATH10-CANDIDATE", "elementary optimization decision is incomplete", record, source, ",".join(missing))
    selected = next((item for item in record.get("candidates", []) if item.get("id") == record.get("selected")), None)
    if not selected:
        raise_error("MATH10-CANDIDATE", "selected candidate is absent from candidate set", record, source, "selected-candidate")
    if selected and selected.get("status") != ":legal":
        raise_error("MATH10-PROVIDER", "selected candidate is not semantically legal", selected, source, "legal-candidate")
    if selected and selected.get("requires_proof") and not selected.get("proof"):
        raise_error("MATH10-PROOF", "selected candidate lacks required proof", selected, source, "proof-or-certificate")
    if record.get("correct_rounding_target") and not record.get("rounding_interval_ledger"):
        raise_error("MATH10-ROUNDING-INTERVAL", "correct-rounding target lacks interval-generation ledger", record, source, "rounding-interval-ledger")
    if record.get("autotune") and not record.get("autotune", {}).get("replayable"):
        raise_error("MATH10-AUTOTUNE", "autotune selection is not replayable", record, source, "autotune-replay")
    if not record.get("fallback"):
        raise_error("MATH10-FALLBACK", "elementary optimization lacks fallback", record, source, "fallback")


def validate_conformance_suite(record: dict[str, Any], source: str) -> None:
    documents = set(record.get("documents", []))
    missing_docs = sorted(REQUIRED_MATH_DOCUMENTS - documents)
    if missing_docs:
        raise_error("MATH11-FIXTURE", "math conformance suite lacks document coverage", record, source, ",".join(missing_docs))
    if not record.get("oracles"):
        raise_error("MATH11-ORACLE", "math conformance suite lacks oracles", record, source, "oracles")
    for fixture in record.get("fixtures", []):
        if not fixture.get("expected_artifacts"):
            raise_error("MATH11-ARTIFACT", "math fixture lacks expected artifacts", fixture, source, "expected-artifacts")
        expected = fixture.get("expected", {})
        actual = fixture.get("actual", {})
        if expected.get("compile") != actual.get("compile"):
            raise_error("MATH11-FIXTURE", "math fixture compile outcome mismatched", fixture, source, "expected-actual-compile")
        if expected.get("diagnostic") != actual.get("diagnostic"):
            raise_error("MATH11-DIAGNOSTIC", "math negative fixture diagnostic mismatched", fixture, source, "expected-diagnostic")


def validate_domain_ir_registration(record: dict[str, Any], source: str) -> None:
    required = ["domain", "owner_doc", "schema", "semantic_anchor", "entry_passes", "exit_passes", "verifier", "supported_profiles", "target_lowerings", "proof_obligations", "fallback"]
    missing = [field for field in required if not record.get(field)]
    if missing or record.get("domain") != ":efir":
        raise_error("MATH3-RUNTIME", "EFIR domain IR registration is incomplete", record, source, ",".join(missing or ["efir-domain"]))


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
    raise MathValidationError(
        code,
        message,
        record=record,
        source=source,
        missing_fact=missing_fact,
        remediation="Update the math artifact, EFIR/EML anchor, proof/certificate, provider record, or conformance fixture.",
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
