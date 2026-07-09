"""SAFE1 safety outcome classification for typed/effected core artifacts."""

from __future__ import annotations

import hashlib
import json
from pathlib import Path
from typing import Any


ALLOWED_OUTCOMES = {":proven-safe", ":runtime-checked", ":rejected", ":unsafe-island"}
SAFE_CLAIM_MODES = {":safe", ":safe-optimized"}
UNSAFE_ISLAND_MODES = {":audited-unsafe", ":systems", ":trusted-runtime", ":unsafe"}
WEAKER_DEPENDENCY_MODES = {":audited-unsafe", ":systems", ":trusted-runtime", ":experimental", ":unsafe"}
REQUIRED_OPERATION_FIELDS = {
    "id",
    "kind",
    "source_span",
    "active_profile",
    "safety_mode",
    "pipeline_stage",
    "type_context",
    "effect_context",
    "capability_context",
    "profile_facts",
    "danger_dimensions",
}
REQUIRED_UNSAFE_AUDIT_FIELDS = {
    "id",
    "operation",
    "reason",
    "owner",
    "source_span",
    "active_profile",
    "target",
    "effects",
    "capabilities",
    "preconditions",
    "postconditions",
    "invariants",
    "evidence",
    "safe_boundary",
    "review",
    "review_policy",
}


class SafetyClassificationError(Exception):
    def __init__(
        self,
        code: str,
        message: str,
        *,
        operation: dict[str, Any] | None = None,
        source: str,
        missing_fact: str,
        specialized_rule: str = "SAFE1",
        remediation: str,
    ) -> None:
        super().__init__(message)
        self.code = code
        self.message = message
        self.operation = operation or {}
        self.source = source
        self.missing_fact = missing_fact
        self.specialized_rule = specialized_rule
        self.remediation = remediation

    def to_diagnostic(self) -> dict[str, Any]:
        return {
            "id": self.code,
            "message": self.message,
            "operation_id": self.operation.get("id"),
            "operation_kind": self.operation.get("kind"),
            "span": self.operation.get("source_span", {"source": self.source}),
            "generated_origin": self.operation.get("generated_origin", []),
            "active_profile": self.operation.get("active_profile"),
            "safety_mode": self.operation.get("safety_mode"),
            "missing_fact": self.missing_fact,
            "specialized_rule": self.specialized_rule,
            "remediation": self.remediation,
            "analyzer_stage": "safety-classifier",
        }


def analyze_safety_file(path: Path) -> dict[str, Any]:
    return analyze_safety_manifest(load_manifest_file(path), str(path))


def safety_manifest_diagnostic(path: Path) -> dict[str, Any] | None:
    try:
        analyze_safety_file(path)
    except SafetyClassificationError as exc:
        return exc.to_diagnostic()
    return None


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
    for pointer, value in manifest.get("append", {}).items():
        target = get_pointer(result, pointer)
        if not isinstance(target, list):
            raise ValueError(f"append target is not a list: {pointer}")
        target.append(value)
    return result


def analyze_safety_manifest(manifest: dict[str, Any], source: str) -> dict[str, Any]:
    if manifest.get("kind") != "safety-classification-input":
        raise SafetyClassificationError(
            "SAFE1-NO-OUTCOME",
            "safety input has the wrong artifact kind",
            source=source,
            missing_fact="safety-classification-input",
            remediation="Feed the SAFE1 classifier a safety-classification-input artifact.",
        )

    validate_dependencies(manifest, source)
    operation_records = []
    runtime_checks = []
    unsafe_islands = []
    proof_references = []
    rejection_diagnostics = []
    generated_provenance = []
    optimization_justifications = []

    for operation in manifest.get("operations", []):
        classification = classify_operation(operation, source)
        operation_records.append(classification)
        if operation.get("runtime_check"):
            runtime_checks.append(runtime_check_record(operation))
        if operation.get("audit"):
            unsafe_islands.append(operation["audit"])
        if operation.get("proof"):
            proof_references.append(proof_record(operation))
        if operation.get("diagnostic"):
            rejection_diagnostics.append(operation["diagnostic"])
        if operation.get("generated_origin"):
            generated_provenance.append(
                {
                    "operation_id": operation["id"],
                    "operation_kind": operation["kind"],
                    "generated_origin": operation["generated_origin"],
                    "span": operation["source_span"],
                }
            )
        if operation.get("erased_check"):
            optimization_justifications.append(
                {
                    "operation_id": operation["id"],
                    "erased_check": operation["erased_check"],
                    "replacement_proof": operation.get("proof", {}).get("id"),
                    "source_span": operation["source_span"],
                }
            )

    return {
        "kind": "safety-classification-artifact",
        "document": "SAFE1",
        "package": manifest.get("package"),
        "module": manifest.get("module"),
        "profile": manifest.get("profile"),
        "target": manifest.get("target"),
        "safety_mode": manifest.get("safety_mode"),
        "source": manifest.get("source"),
        "typed_core_artifact_hash": manifest.get("typed_core_artifact_hash"),
        "input_hash": artifact_hash(manifest),
        "operations": operation_records,
        "runtime_checks": runtime_checks,
        "unsafe_island_audit_records": unsafe_islands,
        "proof_references": proof_references,
        "rejection_diagnostics": rejection_diagnostics,
        "profile_safety_capability_report": profile_report(manifest, operation_records),
        "generated_code_safety_provenance": generated_provenance,
        "optimization_check_erasure_justifications": optimization_justifications,
        "safety_certificate_inputs": certificate_inputs(proof_references, runtime_checks, unsafe_islands),
        "diagnostics": [],
    }


def classify_operation(operation: dict[str, Any], source: str) -> dict[str, Any]:
    missing = sorted(REQUIRED_OPERATION_FIELDS - set(operation))
    if missing:
        raise SafetyClassificationError(
            "SAFE1-NO-OUTCOME",
            f"safety operation is missing required classification facts: {missing}",
            operation=operation,
            source=source,
            missing_fact=",".join(missing),
            remediation="Preserve source span, profile, type, effect, capability, and danger facts from typed core.",
        )

    outcome = exactly_one_outcome(operation, source)
    if operation.get("generated") and not operation.get("generated_origin"):
        raise SafetyClassificationError(
            "SAFE1-GENERATED-PROVENANCE",
            "generated safety-sensitive code lacks an origin chain",
            operation=operation,
            source=source,
            missing_fact="generated-origin-chain",
            remediation="Record the macro, facet, schema generator, compiler extension, or AI tool that emitted the operation.",
        )
    if operation.get("erased_check") and not valid_optimization_proof(operation):
        raise SafetyClassificationError(
            "SAFE1-OPTIMIZATION-PROOF",
            "an optimization erased a safety check without a surviving replacement proof",
            operation=operation,
            source=source,
            missing_fact="check-erasure-proof",
            specialized_rule="SAFE1/D9",
            remediation="Attach a check-elision proof or keep the runtime check in the safety artifact.",
        )

    if outcome == ":proven-safe":
        validate_proof(operation, source)
    elif outcome == ":runtime-checked":
        validate_runtime_check(operation, source)
    elif outcome == ":rejected":
        validate_rejection(operation, source)
    elif outcome == ":unsafe-island":
        validate_unsafe_island(operation, source)

    return {
        "id": operation["id"],
        "kind": operation["kind"],
        "source_span": operation["source_span"],
        "generated_origin": operation.get("generated_origin", []),
        "active_profile": operation["active_profile"],
        "target": operation.get("target"),
        "safety_mode": operation["safety_mode"],
        "pipeline_stage": operation["pipeline_stage"],
        "danger_dimensions": operation["danger_dimensions"],
        "outcome": outcome,
        "type_context": operation["type_context"],
        "effect_context": operation["effect_context"],
        "capability_context": operation["capability_context"],
        "profile_facts": operation["profile_facts"],
        "specialized_rules": operation.get("specialized_rules", ["SAFE1"]),
        "artifact_boundary": operation.get("artifact_boundary", "safety-classification-artifact"),
    }


def exactly_one_outcome(operation: dict[str, Any], source: str) -> str:
    outcomes = []
    if "outcome" in operation:
        outcomes.append(operation["outcome"])
    if "outcomes" in operation:
        outcomes.extend(operation["outcomes"])
    if not outcomes:
        raise SafetyClassificationError(
            "SAFE1-NO-OUTCOME",
            "safety-sensitive operation has no outcome",
            operation=operation,
            source=source,
            missing_fact="safety-outcome",
            remediation="Classify the operation as :proven-safe, :runtime-checked, :rejected, or :unsafe-island.",
        )
    if len(outcomes) != 1 or outcomes[0] not in ALLOWED_OUTCOMES:
        raise SafetyClassificationError(
            "D8-UNCLASSIFIED-DANGER",
            "safety-sensitive operation does not have exactly one legal outcome",
            operation=operation,
            source=source,
            missing_fact="single-legal-safety-outcome",
            specialized_rule="D8/SAFE1",
            remediation="Emit exactly one of :proven-safe, :runtime-checked, :rejected, or :unsafe-island.",
        )
    return outcomes[0]


def validate_proof(operation: dict[str, Any], source: str) -> None:
    proof = operation.get("proof")
    if not valid_proof(proof):
        raise SafetyClassificationError(
            "SAFE1-PROOF-MISSING",
            "operation claims :proven-safe without a valid proof reference",
            operation=operation,
            source=source,
            missing_fact="accepted-proof-reference",
            specialized_rule="SAFE1/D9",
            remediation="Attach a proof naming the claim, checker, inputs, assumptions, and accepted result.",
        )


def validate_runtime_check(operation: dict[str, Any], source: str) -> None:
    check = operation.get("runtime_check")
    if not isinstance(check, dict) or missing_keys(
        check,
        {
            "id",
            "condition",
            "failure_behavior",
            "type_context",
            "effect_context",
            "capability_context",
            "performance_classification",
            "artifact_record",
        },
    ):
        raise SafetyClassificationError(
            "SAFE1-CHECK-MISSING",
            "operation claims :runtime-checked without a complete runtime check record",
            operation=operation,
            source=source,
            missing_fact="runtime-check-record",
            remediation="Emit the condition, source span, contexts, profile-legal failure behavior, and artifact record.",
        )
    if check.get("failure_behavior") in {None, ":undefined"}:
        raise SafetyClassificationError(
            "SAFE1-CHECK-MISSING",
            "runtime check has undefined failure behavior",
            operation=operation,
            source=source,
            missing_fact="defined-failure-behavior",
            remediation="Lower the failure to a declared panic, error, or rejection path.",
        )
    if check.get("profile_legal") is not True or operation["active_profile"] not in check.get("profile_support", []):
        raise SafetyClassificationError(
            "SAFE1-CHECK-ILLEGAL",
            "runtime check requires unavailable profile support",
            operation=operation,
            source=source,
            missing_fact="profile-legal-runtime-check",
            remediation="Use a proof, reject the operation, or select a profile/runtime that supports the check.",
        )


def validate_rejection(operation: dict[str, Any], source: str) -> None:
    diagnostic = operation.get("diagnostic")
    if not isinstance(diagnostic, dict) or missing_keys(diagnostic, {"id", "missing_fact", "span", "remediation"}):
        raise SafetyClassificationError(
            "SAFE1-NO-OUTCOME",
            "operation claims :rejected without a structured diagnostic",
            operation=operation,
            source=source,
            missing_fact="rejection-diagnostic",
            remediation="Attach the diagnostic that stops compilation and names the missing fact.",
        )


def validate_unsafe_island(operation: dict[str, Any], source: str) -> None:
    if operation["safety_mode"] in SAFE_CLAIM_MODES or operation["safety_mode"] not in UNSAFE_ISLAND_MODES:
        raise SafetyClassificationError(
            "SAFE1-UNSAFE-POLICY",
            "unsafe island is illegal in the active safety mode",
            operation=operation,
            source=source,
            missing_fact="unsafe-policy-allowance",
            remediation="Reject the operation or compile it under an audited unsafe or systems policy with safe boundaries.",
        )
    audit = operation.get("audit")
    if not isinstance(audit, dict):
        missing = REQUIRED_UNSAFE_AUDIT_FIELDS
    else:
        missing = REQUIRED_UNSAFE_AUDIT_FIELDS - set(audit)
    if missing:
        raise SafetyClassificationError(
            "SAFE1-UNSAFE-METADATA",
            f"unsafe island lacks required audit metadata: {sorted(missing)}",
            operation=operation,
            source=source,
            missing_fact=",".join(sorted(missing)),
            remediation="Record owner, reason, invariant, evidence, review policy, effects, capabilities, and safe wrapper boundary.",
        )


def validate_dependencies(manifest: dict[str, Any], source: str) -> None:
    caller_mode = manifest.get("safety_mode")
    if caller_mode not in SAFE_CLAIM_MODES:
        return
    for dependency in manifest.get("dependencies", []):
        dependency_mode = dependency.get("safety_mode")
        if dependency_mode in WEAKER_DEPENDENCY_MODES and not (
            dependency.get("safety_certificate") or dependency.get("reviewed_unsafe_records")
        ):
            operation = {
                "id": dependency.get("id"),
                "kind": "dependency",
                "source_span": dependency.get("source_span", {"source": source}),
                "active_profile": manifest.get("profile"),
                "safety_mode": caller_mode,
            }
            raise SafetyClassificationError(
                "SAFE1-DEPENDENCY-MODE",
                "dependency safety mode is weaker than the caller's safe-code claim",
                operation=operation,
                source=source,
                missing_fact="dependency-safety-certificate-or-reviewed-unsafe-record",
                remediation="Require a safety certificate, reviewed unsafe records, or stop treating the dependency as safe.",
            )


def valid_optimization_proof(operation: dict[str, Any]) -> bool:
    proof = operation.get("proof")
    return valid_proof(proof) and proof.get("replaces_check") == operation.get("erased_check")


def valid_proof(proof: Any) -> bool:
    return (
        isinstance(proof, dict)
        and not missing_keys(proof, {"id", "claim", "checker", "inputs", "result"})
        and proof.get("result") == ":accepted"
        and isinstance(proof.get("inputs"), list)
        and bool(proof.get("inputs"))
    )


def runtime_check_record(operation: dict[str, Any]) -> dict[str, Any]:
    check = dict(operation["runtime_check"])
    check["operation_id"] = operation["id"]
    check["operation_kind"] = operation["kind"]
    check["source_span"] = operation["source_span"]
    check["active_profile"] = operation["active_profile"]
    check["safety_mode"] = operation["safety_mode"]
    return check


def proof_record(operation: dict[str, Any]) -> dict[str, Any]:
    proof = dict(operation["proof"])
    proof["operation_id"] = operation["id"]
    proof["operation_kind"] = operation["kind"]
    proof["source_span"] = operation["source_span"]
    return proof


def profile_report(manifest: dict[str, Any], operations: list[dict[str, Any]]) -> dict[str, Any]:
    effects = unique([effect for operation in operations for effect in operation["effect_context"].get("effects", [])])
    capabilities = unique(
        [capability for operation in operations for capability in operation["capability_context"].get("capabilities", [])]
    )
    return {
        "profile": manifest.get("profile"),
        "target": manifest.get("target"),
        "safety_mode": manifest.get("safety_mode"),
        "effects": effects,
        "capabilities": capabilities,
        "operation_count": len(operations),
        "outcomes": {outcome: sum(1 for operation in operations if operation["outcome"] == outcome) for outcome in sorted(ALLOWED_OUTCOMES)},
    }


def certificate_inputs(proofs: list[dict[str, Any]], checks: list[dict[str, Any]], audits: list[dict[str, Any]]) -> list[dict[str, Any]]:
    inputs = []
    for proof in proofs:
        inputs.append({"kind": "proof-reference", "id": proof["id"], "result": proof["result"], "checker": proof["checker"]})
    for check in checks:
        inputs.append({"kind": "runtime-check", "id": check["id"], "condition": check["condition"], "failure_behavior": check["failure_behavior"]})
    for audit in audits:
        inputs.append({"kind": "unsafe-audit", "id": audit["id"], "review": audit["review"], "owner": audit["owner"]})
    return inputs


def missing_keys(value: dict[str, Any], required: set[str]) -> list[str]:
    return sorted(key for key in required if key not in value or value.get(key) in (None, [], {}))


def unique(values: list[str]) -> list[str]:
    result = []
    for value in values:
        if value not in result:
            result.append(value)
    return result


def artifact_hash(value: Any) -> str:
    data = json.dumps(value, sort_keys=True, separators=(",", ":"))
    return "sha256:" + hashlib.sha256(data.encode("utf-8")).hexdigest()


def get_pointer(value: Any, pointer: str) -> Any:
    current = value
    for part in pointer_parts(pointer):
        current = current[int(part)] if isinstance(current, list) else current[part]
    return current


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
