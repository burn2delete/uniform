"""Phase 06 compiler architecture artifact validation."""

from __future__ import annotations

import hashlib
import json
from pathlib import Path
from typing import Any


CANONICAL_PIPELINE = [
    ":read-source",
    ":build-syntax",
    ":macro-expand",
    ":resolve-names",
    ":lower-to-core",
    ":type-check",
    ":effect-check",
    ":profile-validate",
    ":safety-analyze",
    ":build-mir",
    ":verify-mir",
    ":optimize-mir",
    ":lower-domain-ir",
    ":verify-domain-ir",
    ":lower-target",
    ":emit-artifacts",
]
REQUIRED_PASS_FIELDS = {
    "pass",
    "input",
    "output",
    "requires",
    "preserves",
    "invalidates",
    "regenerates",
    "emits",
    "profiles",
}
REQUIRED_DIAGNOSTIC_FIELDS = {
    "rule",
    "severity",
    "stage",
    "primary",
    "origin_chain",
    "profile",
    "target",
    "facts",
    "remediation",
}


class CompilerArchitectureError(Exception):
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
            "stage": self.record.get("stage") or self.record.get("pass"),
            "artifact_id": self.record.get("artifact_id") or self.record.get("id") or self.record.get("module"),
            "profile": self.record.get("profile"),
            "target": self.record.get("target"),
            "source_span": self.record.get("source_span", {"source": self.source}),
            "missing_fact": self.missing_fact,
            "remediation": self.remediation,
            "analyzer_stage": "phase06-compiler-architecture-validation",
        }


def validate_compiler_file(path: Path) -> dict[str, Any]:
    return validate_compiler_manifest(load_manifest_file(path), str(path))


def compiler_diagnostic(path: Path) -> dict[str, Any] | None:
    try:
        validate_compiler_file(path)
    except CompilerArchitectureError as exc:
        return exc.to_diagnostic()
    return None


def validate_compiler_manifest(manifest: dict[str, Any], source: str) -> dict[str, Any]:
    if manifest.get("kind") != "compiler-architecture-input":
        raise_error("C1-MANIFEST", "compiler architecture input has wrong kind", {}, source, "compiler-architecture-input")

    pipeline = require_dict(manifest, "pipeline_manifest", "C1-MANIFEST", source)
    validate_pipeline(pipeline, source)
    reader = require_dict(manifest, "reader", "C2-HASH", source)
    validate_reader(reader, source)
    syntax = require_list(manifest, "syntax_objects", "C3-SHAPE", source)
    for item in syntax:
        validate_syntax_object(item, source)
    macro = require_dict(manifest, "macro_expansion", "C4-TRACE", source)
    validate_macro_expansion(macro, source)
    namespace = require_dict(manifest, "namespace_analysis", "C5-UNRESOLVED", source)
    validate_namespace(namespace, source)
    core = require_dict(manifest, "core_ast", "C6-CORE-SHAPE", source)
    validate_core(core, source)
    typed = require_dict(manifest, "typed_core", "C7-VERIFY", source)
    validate_typed_core(typed, source)
    effects = require_dict(manifest, "effect_graph", "C8-VERIFY", source)
    validate_effect_graph(effects, source)
    ownership = require_dict(manifest, "ownership_analysis", "C9-TRANSFER", source)
    validate_ownership(ownership, source)
    safety = require_dict(manifest, "safety_pipeline", "C10-NO-OUTCOME", source)
    validate_safety_pipeline(safety, source)
    mir = require_dict(manifest, "mir_module", "C11-MODULE", source)
    validate_mir(mir, source)
    domain = require_dict(manifest, "domain_ir", "C12-REGISTRATION", source)
    validate_domain_ir(domain, source)
    optimization = require_dict(manifest, "optimization_pipeline", "C13-CONTRACT", source)
    validate_optimization(optimization, source)
    lowering = require_dict(manifest, "target_lowering", "C14-INPUT", source)
    validate_target_lowering(lowering, source)
    diagnostics = require_dict(manifest, "diagnostic_system", "C15-SCHEMA", source)
    validate_diagnostics(diagnostics, source)
    incremental = require_dict(manifest, "incremental_compilation", "C16-KEY", source)
    validate_incremental(incremental, source)
    plugins = require_dict(manifest, "plugin_system", "C17-MANIFEST", source)
    validate_plugins(plugins, source)
    verification = require_dict(manifest, "verification_plan", "C18-RISK", source)
    validate_verification(verification, source)

    return {
        "kind": "compiler-architecture-artifact",
        "phase": "06",
        "package": manifest.get("package"),
        "module": manifest.get("module"),
        "pipeline_manifest": pipeline,
        "pass_contract_manifest": pipeline.get("pass_contracts", []),
        "compiler_diagnostic_registry": diagnostics,
        "reader_artifacts": reader,
        "syntax_object_stream": syntax,
        "macro_expansion_trace": macro,
        "namespace_analysis": namespace,
        "core_ast_module": core,
        "typed_core_module": typed,
        "effect_graph": effects,
        "ownership_analysis": ownership,
        "safety_pipeline": safety,
        "mir_module": mir,
        "domain_ir_modules": domain,
        "optimization_manifest": optimization,
        "target_lowering_manifest": lowering,
        "incremental_compilation": incremental,
        "plugin_system": plugins,
        "compiler_verification_plan": verification,
        "input_hash": artifact_hash(manifest),
        "diagnostics": [],
    }


def validate_pipeline(record: dict[str, Any], source: str) -> None:
    if record.get("stages") != CANONICAL_PIPELINE:
        raise_error("C1-PIPELINE", "compiler pipeline ordering is invalid", record, source, "canonical-stage-order")
    if not record.get("artifact_graph") or not record.get("diagnostic_stream"):
        raise_error("C1-MANIFEST", "pipeline manifest lacks artifact graph or diagnostics", record, source, "artifact-graph-diagnostics")
    for contract in record.get("pass_contracts", []):
        missing = sorted(REQUIRED_PASS_FIELDS - set(contract))
        if missing:
            raise_error("C1-PASS-CONTRACT", "pass contract is incomplete", contract, source, ",".join(missing))
        lost = set(contract.get("required_downstream_facts", [])) - (
            set(contract.get("preserves", [])) | set(contract.get("regenerates", []))
        )
        if lost:
            raise_error("C1-EVIDENCE-DROP", "pass contract drops downstream facts", contract, source, ",".join(sorted(lost)))
    if record.get("backend_input") != ":verified-mir":
        raise_error("C1-UNCHECKED-BACKEND", "backend lowering input is unchecked", record, source, "verified-mir-or-domain-ir")


def validate_reader(record: dict[str, Any], source: str) -> None:
    if record.get("unstable_hash"):
        raise_error("C2-HASH", "reader artifact identity is unstable", record, source, "stable-reader-hash")
    if record.get("extension_effect_violation"):
        raise_error("C2-EXTENSION", "reader extension used ungranted build effects", record, source, "extension-build-grant")
    for token in record.get("tokens", []):
        if not token.get("span") or not token.get("raw"):
            raise_error("C2-DELIMITER", "reader token lacks raw spelling or span", token, source, "token-raw-span")


def validate_syntax_object(record: dict[str, Any], source: str) -> None:
    required = ["syntax_id", "form", "span", "origin", "namespace", "phase", "profile", "hygiene", "version"]
    missing = [field for field in required if not record.get(field)]
    if missing:
        code = "C3-ORIGIN" if "origin" in missing else "C3-SHAPE"
        raise_error(code, "syntax object lacks required fields", record, source, ",".join(missing))
    if record.get("accidental_capture"):
        raise_error("C3-CAPTURE", "syntax object contains accidental capture", record, source, "declared-capture")
    if record.get("stale_facts"):
        raise_error("C3-FACT-STALE", "syntax object uses stale facts", record, source, "fact-invalidation")


def validate_macro_expansion(record: dict[str, Any], source: str) -> None:
    if not record.get("trace_replayable"):
        raise_error("C4-TRACE", "macro expansion trace is unreplayable", record, source, "replayable-expansion-trace")
    if record.get("ungranted_build_effects"):
        raise_error("C4-BUILD-EFFECT", "macro uses ungranted build effects", record, source, "build-effect-grant")
    if record.get("illegal_capture"):
        raise_error("C4-CAPTURE", "macro expansion captured illegally", record, source, "capture-policy")
    if record.get("generated_unsafe_missing_metadata"):
        raise_error("C4-GENERATED-UNSAFE", "macro generated unsafe code without metadata", record, source, "SAFE6-metadata")


def validate_namespace(record: dict[str, Any], source: str) -> None:
    if record.get("unresolved_symbols"):
        raise_error("C5-UNRESOLVED", "namespace has unresolved symbols", record, source, ",".join(record["unresolved_symbols"]))
    if record.get("ambiguous_symbols"):
        raise_error("C5-AMBIGUOUS", "namespace has ambiguous symbols", record, source, ",".join(record["ambiguous_symbols"]))
    if record.get("rejected_edges"):
        raise_error("C5-CROSS-PROFILE", "namespace has rejected cross-profile edges", record, source, "profile-boundary")
    if not record.get("binding_table") or not record.get("dependency_graph"):
        raise_error("C5-UNRESOLVED", "namespace analysis lacks binding table or dependency graph", record, source, "binding-table-dependency-graph")


def validate_core(record: dict[str, Any], source: str) -> None:
    if record.get("lowering_gaps"):
        raise_error("C6-LOWERING-GAP", "surface form cannot lower to core or domain IR", record, source, ",".join(record["lowering_gaps"]))
    if record.get("lost_effects"):
        raise_error("C6-EFFECT-DROP", "core lowering lost effect or capability declarations", record, source, "effect-capability-preservation")
    if not record.get("nodes") or not record.get("evaluation_order") or not record.get("verifier", {}).get("passed"):
        raise_error("C6-VERIFY", "core AST verifier failed or lacks required records", record, source, "core-verifier")


def validate_typed_core(record: dict[str, Any], source: str) -> None:
    if record.get("missing_type_nodes"):
        raise_error("C7-VERIFY", "typed core has nodes without type facts", record, source, ",".join(record["missing_type_nodes"]))
    if record.get("dynamic_forbidden"):
        raise_error("C7-DYNAMIC", "dynamic boundary is forbidden by profile", record, source, "profile-legal-dynamic-boundary")
    if record.get("unchecked_casts"):
        raise_error("C7-CAST", "typed core has unchecked casts", record, source, "checked-or-unsafe-cast")
    if not record.get("function_signatures") or not record.get("constraints"):
        raise_error("C7-VERIFY", "typed core lacks signatures or constraints", record, source, "function-signatures-constraints")


def validate_effect_graph(record: dict[str, Any], source: str) -> None:
    if record.get("missing_capabilities"):
        raise_error("C8-CAPABILITY", "effect graph has missing capabilities", record, source, ",".join(record["missing_capabilities"]))
    if record.get("undeclared_effects"):
        raise_error("C8-UNDECLARED", "effect graph has undeclared effects", record, source, ",".join(record["undeclared_effects"]))
    if record.get("missing_ordering"):
        raise_error("C8-ORDER", "effect graph lacks ordering constraints", record, source, "effect-ordering")
    if not record.get("effect_nodes") or not record.get("capability_proofs"):
        raise_error("C8-VERIFY", "effect graph lacks nodes or capability proofs", record, source, "effect-nodes-capability-proofs")


def validate_ownership(record: dict[str, Any], source: str) -> None:
    if record.get("linear_leaks"):
        raise_error("C9-LINEAR-LEAK", "linear resource lacks terminal state", record, source, ",".join(record["linear_leaks"]))
    if record.get("use_after_move"):
        raise_error("C9-USE-AFTER-MOVE", "value used after move", record, source, "move-state")
    if record.get("borrow_escape"):
        raise_error("C9-BORROW-ESCAPE", "borrow escapes valid lifetime", record, source, "borrow-lifetime")
    if not record.get("ownership_graph") or not record.get("borrow_graph") or not record.get("linear_flow"):
        raise_error("C9-TRANSFER", "ownership artifact lacks required graphs", record, source, "ownership-borrow-linear-graphs")


def validate_safety_pipeline(record: dict[str, Any], source: str) -> None:
    if record.get("operations_without_outcome"):
        raise_error("C10-NO-OUTCOME", "safety-sensitive operation lacks outcome", record, source, ",".join(record["operations_without_outcome"]))
    for outcome in record.get("outcomes", []):
        if outcome.get("outcome") not in {":proven-safe", ":runtime-checked", ":rejected", ":unsafe-island"}:
            raise_error("C10-NO-OUTCOME", "invalid safety outcome", outcome, source, "SAFE1-outcome")
    if record.get("unsafe_missing_metadata"):
        raise_error("C10-UNSAFE", "unsafe island lacks metadata", record, source, "unsafe-metadata")


def validate_mir(record: dict[str, Any], source: str) -> None:
    required = ["module", "source_core", "profile", "target_request", "functions", "types", "effects", "ownership", "safety", "source_origin_map"]
    missing = [field for field in required if not record.get(field)]
    if missing:
        code = "C11-TYPE" if "types" in missing else "C11-MODULE"
        raise_error(code, "MIR module is incomplete", record, source, ",".join(missing))
    if record.get("target_specific_opcodes"):
        raise_error("C11-TARGET-LEAK", "generic MIR contains target-specific opcode", record, source, "target-independent-mir")
    if record.get("verifier", {}).get("passed") is not True:
        raise_error("C11-VERIFY", "MIR verifier failed", record, source, "mir-verifier-pass")


def validate_domain_ir(record: dict[str, Any], source: str) -> None:
    if record.get("missing_anchor"):
        raise_error("C12-ANCHOR", "domain IR lacks semantic anchor", record, source, "typed-core-or-mir-anchor")
    if not record.get("registrations") or not record.get("artifacts"):
        raise_error("C12-REGISTRATION", "domain IR registry lacks registrations or artifacts", record, source, "registrations-artifacts")
    for artifact in record.get("artifacts", []):
        if artifact.get("verifier", {}).get("result") != ":accepted":
            raise_error("C12-VERIFY", "domain verifier failed", artifact, source, "domain-verifier")
        if artifact.get("optimization") and not artifact.get("proofs"):
            raise_error("C12-PROOF", "domain optimization lacks proof or certificate", artifact, source, "domain-proof")


def validate_optimization(record: dict[str, Any], source: str) -> None:
    if not record.get("pass_registry") or not record.get("decision_log"):
        raise_error("C13-CONTRACT", "optimization pipeline lacks pass registry or decision log", record, source, "pass-registry-decision-log")
    for decision in record.get("decision_log", []):
        if decision.get("requires_proof") and not decision.get("proofs_used"):
            raise_error("C13-PROOF", "optimization decision lacks required proof", decision, source, "proofs-used")
        if decision.get("erased_checks") and not decision.get("perf10_record"):
            raise_error("C13-CHECK-ELISION", "optimization erased checks outside PERF10", decision, source, "PERF10-record")
        if decision.get("verifier_result") != ":passed":
            raise_error("C13-VERIFY", "post-pass verifier failed", decision, source, "post-pass-verifier")


def validate_target_lowering(record: dict[str, Any], source: str) -> None:
    if record.get("input_verified") is not True:
        raise_error("C14-INPUT", "target lowering input is unverified or stale", record, source, "verified-mir-or-domain-ir")
    if not record.get("eligibility", {}).get("accepted"):
        raise_error("C14-TARGET", "target lowering eligibility failed", record, source, "target-eligibility")
    if record.get("proofless_metadata"):
        raise_error("C14-PROOF-METADATA", "target metadata lacks Gravity proof", record, source, "proof-to-metadata-map")
    if not record.get("artifact_manifest"):
        raise_error("C14-MANIFEST", "target lowering lacks emitted artifact manifest", record, source, "target-artifact-manifest")


def validate_diagnostics(record: dict[str, Any], source: str) -> None:
    for diagnostic in record.get("diagnostics", []):
        missing = sorted(REQUIRED_DIAGNOSTIC_FIELDS - set(diagnostic))
        if missing:
            raise_error("C15-SCHEMA", "diagnostic record is malformed", diagnostic, source, ",".join(missing))
        if diagnostic.get("leaks_secret"):
            raise_error("C15-REDACTION", "diagnostic leaks secret or private data", diagnostic, source, "redaction")
    if record.get("nondeterministic_order"):
        raise_error("C15-ORDER", "diagnostic stream order is nondeterministic", record, source, "deterministic-order")


def validate_incremental(record: dict[str, Any], source: str) -> None:
    if not record.get("cache_keys") or not record.get("dependency_graph"):
        raise_error("C16-KEY", "incremental compilation lacks cache keys or dependency graph", record, source, "cache-keys-dependency-graph")
    if record.get("stale_artifact_reuse"):
        raise_error("C16-STALE", "stale artifact was reused", record, source, "invalidation-trace")
    if record.get("stale_proof_reuse"):
        raise_error("C16-PROOF", "stale proof or certificate was reused", record, source, "proof-freshness")
    if record.get("speculative_publish"):
        raise_error("C16-SPECULATIVE", "speculative cache reuse reached publish boundary", record, source, "full-revalidation")


def validate_plugins(record: dict[str, Any], source: str) -> None:
    for plugin in record.get("plugins", []):
        if not plugin.get("manifest") or not plugin.get("api_compatible"):
            raise_error("C17-API", "plugin manifest is missing or API is incompatible", plugin, source, "plugin-manifest-api")
        if plugin.get("missing_capability"):
            raise_error("C17-CAPABILITY", "plugin lacks required compiler capability", plugin, source, "compiler-capability")
        if plugin.get("output_verified") is not True:
            raise_error("C17-OUTPUT", "plugin output failed verification", plugin, source, "plugin-output-verifier")
    if not record.get("execution_traces"):
        raise_error("C17-MANIFEST", "plugin system lacks execution traces", record, source, "plugin-execution-trace")


def validate_verification(record: dict[str, Any], source: str) -> None:
    for risk in record.get("pass_risks", []):
        if not risk.get("minimum_evidence"):
            raise_error("C18-RISK", "pass risk classification lacks minimum evidence", risk, source, "minimum-evidence")
        if risk.get("release_gate") == ":required" and not risk.get("evidence_present"):
            raise_error("C18-EVIDENCE", "required pass evidence is missing", risk, source, "required-evidence")
    if not record.get("trust_report") or not record.get("release_gate_report"):
        raise_error("C18-TRUST-REPORT", "compiler verification lacks trust or release gate report", record, source, "trust-release-report")


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
    raise CompilerArchitectureError(
        code,
        message,
        record=record,
        source=source,
        missing_fact=missing_fact,
        remediation="Update the compiler artifact, pass contract, verifier report, diagnostic record, or evidence fixture.",
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
