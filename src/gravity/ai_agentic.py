"""Phase 11 AI and agentic programming artifact validation."""

from __future__ import annotations

import hashlib
import json
from pathlib import Path
from typing import Any


DOCUMENT_COMPONENTS = {
    "A1": "ai_program_manifest",
    "A2": "model_manifest",
    "A3": "prompt_artifact",
    "A4": "tool_schema",
    "A5": "agent_manifest",
    "A6": "workflow_graph",
    "A7": "memory_policy",
    "A8": "policy_manifest",
    "A9": "evaluation_report",
    "A10": "human_review_manifest",
    "A11": "injection_defense",
}


class AIAgenticError(Exception):
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
            "agent_id": self.record.get("agent_id"),
            "model_id": self.record.get("model_id"),
            "tool_id": self.record.get("tool_id"),
            "workflow_id": self.record.get("workflow_id"),
            "prompt_id": self.record.get("prompt_id"),
            "policy_id": self.record.get("policy_id"),
            "source_span": self.record.get("source_span", {"source": self.source}),
            "missing_fact": self.missing_fact,
            "remediation": self.remediation,
            "analyzer_stage": "phase11-ai-agentic-validation",
        }


def validate_ai_agentic_file(path: Path) -> dict[str, Any]:
    return validate_ai_agentic_manifest(load_manifest_file(path), str(path))


def ai_agentic_diagnostic(path: Path) -> dict[str, Any] | None:
    try:
        validate_ai_agentic_file(path)
    except AIAgenticError as exc:
        return exc.to_diagnostic()
    return None


def validate_ai_agentic_manifest(manifest: dict[str, Any], source: str) -> dict[str, Any]:
    if manifest.get("kind") != "ai-agentic-input":
        raise_error("AI001", "AI agentic input has wrong kind", {}, source, "ai-agentic-input")

    components = {doc: require_dict(manifest, key, "AI001", source) for doc, key in DOCUMENT_COMPONENTS.items()}
    validate_ai_program(components["A1"], source)
    validate_model_manifest(components["A2"], source)
    validate_prompt_artifact(components["A3"], source)
    validate_tool_schema(components["A4"], source)
    validate_agent_manifest(components["A5"], source)
    validate_workflow_graph(components["A6"], source)
    validate_memory_policy(components["A7"], source)
    validate_policy_manifest(components["A8"], source)
    validate_evaluation_report(components["A9"], source)
    validate_human_review(components["A10"], source)
    validate_injection_defense(components["A11"], source)

    return {
        "kind": "ai-agentic-artifact",
        "phase": "11",
        "package": manifest.get("package"),
        "module": manifest.get("module"),
        "ai_program_manifest": components["A1"],
        "model_manifest": components["A2"],
        "prompt_artifact": components["A3"],
        "tool_schema": components["A4"],
        "agent_manifest": components["A5"],
        "workflow_graph": components["A6"],
        "memory_policy": components["A7"],
        "policy_manifest": components["A8"],
        "evaluation_report": components["A9"],
        "human_review_manifest": components["A10"],
        "injection_defense": components["A11"],
        "document_contracts": components,
        "coverage_summary": {
            "documents": len(DOCUMENT_COMPONENTS),
            "agent_id": components["A5"].get("agent_id"),
            "model_id": components["A2"].get("model_id"),
            "tool_id": components["A4"].get("tool_id"),
            "workflow_id": components["A6"].get("workflow_id"),
            "status": ":passed",
        },
        "input_hash": artifact_hash(manifest),
        "diagnostics": [],
    }


def validate_ai_program(record: dict[str, Any], source: str) -> None:
    require_fields(
        record,
        [
            "legal_profiles",
            "source_units",
            "ai_effects",
            "capability_requirements",
            "schema_references",
            "artifact_edges",
            "replay_modes",
            "generated_code_pipeline",
            "runtime_ledgers",
        ],
        "AI001",
        source,
    )
    if record.get("profile") not in record.get("legal_profiles", []):
        raise_error("AI001", "AI construct used outside a legal profile", record, source, "legal-ai-profile")
    if record.get("missing_model_identity"):
        raise_error("AI002", "model call lacks provider or model identity", record, source, "model-provider-identity")
    if record.get("model_output_direct_tool_argument"):
        raise_error("AI004", "model output drives tool call without schema/capability/human-review evidence", record, source, "tool-authorization")
    if record.get("generated_code_unchecked"):
        raise_error("AI006", "generated source bypasses compiler validation", record, source, "compiler-validation")


def validate_model_manifest(record: dict[str, Any], source: str) -> None:
    require_fields(
        record,
        [
            "provider_id",
            "model_id",
            "model_version",
            "supported_modes",
            "credential_policy",
            "context_limits",
            "structured_output",
            "tool_call_mode",
            "replay_policy",
            "retention_policy",
            "budget",
            "failure_classes",
        ],
        "A2002",
        source,
    )
    if not record.get("provider_capability"):
        raise_error("A2001", "model call lacks provider capability", record, source, "provider-capability")
    if record.get("unsafe_credential_binding"):
        raise_error("A2002", "model provider credential binding is unsafe", record, source, "credential-policy")
    if record.get("unsupported_mode"):
        raise_error("A2003", "model provider does not support requested mode", record, source, "supported-mode")
    if record.get("fallback_without_eval"):
        raise_error("A2004", "model fallback lacks policy or evaluation gate", record, source, "fallback-eval-gate")


def validate_prompt_artifact(record: dict[str, Any], source: str) -> None:
    require_fields(
        record,
        [
            "prompt_id",
            "input_schema",
            "output_schema",
            "authority_partitions",
            "taint_map",
            "tool_visibility",
            "provider_constraints",
            "repair_policy",
            "refusal_policy",
            "prompt_hash",
            "compatibility_version",
        ],
        "A3001",
        source,
    )
    if record.get("authority_violation"):
        raise_error("A3003", "untrusted content enters system or developer authority", record, source, "authority-partition")
    if record.get("missing_output_schema"):
        raise_error("A3002", "trusted structured output lacks output schema", record, source, "output-schema")
    if record.get("unbounded_repair"):
        raise_error("A3007", "prompt repair lacks bounded compatibility policy", record, source, "bounded-repair")
    if record.get("secret_in_prompt"):
        raise_error("A3008", "secret flows into rendered prompt without policy", record, source, "secret-redaction")


def validate_tool_schema(record: dict[str, Any], source: str) -> None:
    require_fields(
        record,
        [
            "tool_id",
            "version",
            "input_schema",
            "output_schema",
            "declared_effects",
            "capabilities",
            "side_effect_class",
            "idempotency",
            "retry_policy",
            "timeout_policy",
            "replay_behavior",
            "redaction_policy",
        ],
        "A4001",
        source,
    )
    if record.get("hidden_effect"):
        raise_error("A4002", "tool implementation performs undeclared effect", record, source, "declared-effect")
    if record.get("missing_capability_handle"):
        raise_error("A4003", "tool call lacks scoped capability handle", record, source, "capability-handle")
    if record.get("side_effect_class") in {":write", ":destructive", ":privileged"} and not record.get("human_review_policy"):
        raise_error("A4005", "privileged or write tool lacks human-review policy", record, source, "human-review-policy")
    if record.get("unsafe_retry"):
        raise_error("A4006", "non-idempotent tool retry lacks compensation", record, source, "retry-compensation")


def validate_agent_manifest(record: dict[str, Any], source: str) -> None:
    require_fields(
        record,
        [
            "agent_id",
            "version",
            "owner_package",
            "source_hash",
            "models",
            "prompts",
            "toolset",
            "memory_bindings",
            "policies",
            "human_review",
            "input_schema",
            "output_schema",
            "effects",
            "capabilities",
            "budgets",
            "deployment_class",
            "ledger_identity",
        ],
        "A5001",
        source,
    )
    if record.get("ambient_authority"):
        raise_error("A5002", "agent has ambient host authority", record, source, "tool-scoped-authority")
    if record.get("undeclared_tool_use"):
        raise_error("A5003", "agent uses a tool outside its manifest", record, source, "declared-toolset")
    if record.get("missing_policy"):
        raise_error("A5004", "agent lacks required policy", record, source, "policy-binding")
    if record.get("missing_eval_gate"):
        raise_error("A5005", "production agent lacks evaluation gate", record, source, "eval-gate")
    if not record.get("eval_gates"):
        raise_error("A5005", "agent lacks evaluation gate references", record, source, "eval-gates")


def validate_workflow_graph(record: dict[str, Any], source: str) -> None:
    require_fields(
        record,
        [
            "workflow_id",
            "nodes",
            "state_schema",
            "event_log_schema",
            "effect_capability_table",
            "retry_table",
            "compensation_table",
            "human_review_payload_schemas",
            "migration_compatibility",
            "budget_accounting",
        ],
        "A6004",
        source,
    )
    if not record.get("replay_mode"):
        raise_error("A6001", "AI workflow lacks replay mode", record, source, "replay-mode")
    if record.get("unrecorded_nondeterminism"):
        raise_error("A6002", "workflow has unrecorded nondeterminism", record, source, "replay-record")
    if record.get("unsafe_replay_side_effect"):
        raise_error("A6003", "workflow would repeat side effect during replay", record, source, "event-log-guard")
    if record.get("missing_compensation"):
        raise_error("A6005", "non-idempotent side effect lacks compensation", record, source, "idempotency-or-compensation")


def validate_memory_policy(record: dict[str, Any], source: str) -> None:
    require_fields(
        record,
        [
            "memory_id",
            "item_schema",
            "metadata_schema",
            "embedding_model",
            "vector_dimension",
            "read_capability",
            "write_capability",
            "partitioning",
            "retention_policy",
            "deletion_policy",
            "redaction_policy",
            "prompt_policy",
            "replay_policy",
            "retrieval_trace",
        ],
        "A7001",
        source,
    )
    if record.get("missing_capability"):
        raise_error("A7001", "memory access lacks scoped capability", record, source, "memory-capability")
    if record.get("invalid_item_schema"):
        raise_error("A7002", "memory item fails schema validation", record, source, "item-schema")
    if record.get("protected_embedding"):
        raise_error("A7003", "secret or no-store data is embedded without policy", record, source, "embedding-policy")
    if record.get("cross_tenant_retrieval"):
        raise_error("A7004", "cross-tenant retrieval is not allowed by policy", record, source, "tenant-partition")


def validate_policy_manifest(record: dict[str, Any], source: str) -> None:
    require_fields(
        record,
        [
            "policy_id",
            "allow_effects",
            "deny_effects",
            "data_class_rules",
            "taint_rules",
            "human_review_rules",
            "budget_rules",
            "provider_fallback_rules",
            "generated_code_rules",
            "deployment_promotion_rules",
            "decision_ledger",
        ],
        "A8001",
        source,
    )
    if record.get("missing_policy"):
        raise_error("A8001", "production AI agent lacks explicit policy", record, source, "policy-manifest")
    if record.get("tainted_output_without_validation"):
        raise_error("A8004", "AI output is used as trusted data without validation", record, source, "taint-validation")
    if record.get("fallback_without_eval"):
        raise_error("A8005", "provider fallback lacks required evaluation evidence", record, source, "fallback-eval")
    if record.get("generated_code_unchecked"):
        raise_error("A8006", "generated code bypasses compiler validation", record, source, "generated-code-validation")


def validate_evaluation_report(record: dict[str, Any], source: str) -> None:
    require_fields(
        record,
        [
            "eval_id",
            "subject_identity",
            "dataset_identity",
            "metric_definitions",
            "thresholds",
            "provider_identities",
            "run_environment",
            "safety_probe_results",
            "budget_summary",
            "variance_analysis",
            "release_gate_status",
        ],
        "A9001",
        source,
    )
    if record.get("missing_eval_gate"):
        raise_error("A9001", "production promotion lacks required eval report", record, source, "eval-report")
    if record.get("stale_subject"):
        raise_error("A9002", "eval report subject hash is stale", record, source, "subject-identity")
    if record.get("missing_safety_probe"):
        raise_error("A9005", "required AI safety probe is missing", record, source, "safety-probe")
    if record.get("redaction_violation"):
        raise_error("A9008", "eval output storage violates redaction policy", record, source, "redacted-eval-output")


def validate_human_review(record: dict[str, Any], source: str) -> None:
    require_fields(
        record,
        [
            "human_review_id",
            "action_schema",
            "required_role",
            "evidence_schema",
            "payload_hash_rule",
            "expiry_rule",
            "denial_branch",
            "timeout_branch",
            "replay_rule",
            "audit_storage_policy",
            "redaction_policy",
        ],
        "A10001",
        source,
    )
    if record.get("missing_review"):
        raise_error("A10001", "human-review-required action lacks review record", record, source, "human-review-record")
    if record.get("wrong_role"):
        raise_error("A10002", "human-review actor lacks required role", record, source, "reviewer-role")
    if record.get("payload_hash_mismatch"):
        raise_error("A10005", "reviewed payload hash does not match executed payload", record, source, "payload-hash-match")
    if record.get("emergency_bypass_invalid"):
        raise_error("A10008", "emergency bypass lacks declared policy", record, source, "emergency-policy")


def validate_injection_defense(record: dict[str, Any], source: str) -> None:
    require_fields(
        record,
        [
            "authority_levels",
            "prompt_partition_map",
            "taint_rules",
            "tool_authorization_table",
            "policy_denial_fixtures",
            "defense_probe_requirements",
            "generated_code_validation",
            "runtime_monitors",
            "incident_bundle_schema",
        ],
        "A11001",
        source,
    )
    if record.get("authority_partition_violation"):
        raise_error("A11001", "untrusted data enters privileged prompt authority", record, source, "authority-partition")
    if record.get("model_text_authorizes_tool"):
        raise_error("A11002", "tool call is justified only by model text", record, source, "manifest-policy-capability-review")
    if record.get("memory_injection"):
        raise_error("A11003", "retrieved memory attempts to enter tool policy scope", record, source, "memory-data-authority")
    if record.get("missing_injection_eval"):
        raise_error("A11006", "privileged agent lacks injection-defense eval probe", record, source, "injection-defense-probe")


def require_fields(record: dict[str, Any], fields: list[str], code: str, source: str) -> None:
    for field in fields:
        if not record.get(field):
            raise_error(code, "manifest lacks required field", record, source, field)


def require_dict(manifest: dict[str, Any], key: str, code: str, source: str) -> dict[str, Any]:
    value = manifest.get(key)
    if not isinstance(value, dict) or not value:
        raise_error(code, "manifest lacks required object", {}, source, key)
    return value


def raise_error(code: str, message: str, record: dict[str, Any], source: str, missing_fact: str) -> None:
    raise AIAgenticError(
        code,
        message,
        record=record,
        source=source,
        missing_fact=missing_fact,
        remediation="Update the Phase 11 AI manifest, policy, replay record, fixture, or diagnostic evidence.",
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
