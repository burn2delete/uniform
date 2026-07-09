"""Phase 13 tooling and developer-experience validation."""

from __future__ import annotations

import hashlib
import json
from pathlib import Path
from typing import Any


DOCUMENT_COMPONENTS = {
    "T1": "cli_contract",
    "T2": "repl_session",
    "T3": "formatter_report",
    "T4": "lint_report",
    "T5": "lsp_capability_matrix",
    "T6": "debugger_trace",
    "T7": "documentation_artifact",
    "T8": "dev_server_session",
    "T9": "registry_ux_record",
    "T10": "ir_inspector_bundle",
    "T11": "profiler_report",
    "T12": "safety_audit_report",
    "T13": "ai_tooling_record",
}


class ToolingExperienceError(Exception):
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
            "tool_id": self.record.get("tool_id"),
            "command": self.record.get("command"),
            "session_id": self.record.get("session_id"),
            "artifact_id": self.record.get("artifact_id"),
            "profile": self.record.get("profile"),
            "target": self.record.get("target"),
            "capability": self.record.get("capability"),
            "source_span": self.record.get("source_span", {"source": self.source}),
            "missing_fact": self.missing_fact,
            "remediation": self.remediation,
            "analyzer_stage": "phase13-tooling-validation",
        }


def validate_tooling_experience_file(path: Path) -> dict[str, Any]:
    return validate_tooling_experience_manifest(load_manifest_file(path), str(path))


def tooling_experience_diagnostic(path: Path) -> dict[str, Any] | None:
    try:
        validate_tooling_experience_file(path)
    except ToolingExperienceError as exc:
        return exc.to_diagnostic()
    return None


def validate_tooling_experience_manifest(manifest: dict[str, Any], source: str) -> dict[str, Any]:
    if manifest.get("kind") != "tooling-experience-input":
        raise_error("T1006", "tooling input has wrong kind", {}, source, "tooling-experience-input")
    components = {doc: require_dict(manifest, key, "T1006", source) for doc, key in DOCUMENT_COMPONENTS.items()}
    validate_cli_contract(components["T1"], source)
    validate_repl_session(components["T2"], source)
    validate_formatter_report(components["T3"], source)
    validate_lint_report(components["T4"], source)
    validate_lsp_matrix(components["T5"], source)
    validate_debugger_trace(components["T6"], source)
    validate_documentation_artifact(components["T7"], source)
    validate_dev_server_session(components["T8"], source)
    validate_registry_ux(components["T9"], source)
    validate_ir_inspector(components["T10"], source)
    validate_profiler_report(components["T11"], source)
    validate_safety_audit(components["T12"], source)
    validate_ai_tooling(components["T13"], source)
    return {
        "kind": "tooling-experience-artifact",
        "phase": "13",
        "package": manifest.get("package"),
        "cli_command_set": components["T1"],
        "repl_session_artifact": components["T2"],
        "formatter_fixture": components["T3"],
        "linter_diagnostic_report": components["T4"],
        "lsp_capability_matrix": components["T5"],
        "debugger_trace": components["T6"],
        "tooling_ui_data_model": {
            "documentation": components["T7"],
            "dev_server": components["T8"],
            "registry": components["T9"],
            "ir_inspector": components["T10"],
            "profiler": components["T11"],
            "safety_audit": components["T12"],
            "ai_tooling": components["T13"],
        },
        "document_contracts": components,
        "coverage_summary": {
            "documents": len(DOCUMENT_COMPONENTS),
            "tasks": 6,
            "command_count": len(components["T1"].get("commands", [])),
            "status": ":passed",
        },
        "input_hash": artifact_hash(manifest),
        "diagnostics": [],
    }


def validate_cli_contract(record: dict[str, Any], source: str) -> None:
    require_fields(record, ["commands", "exit_codes", "json_output", "diagnostic_routing", "artifact_outputs", "capability_prompts", "secret_redaction", "golden_fixtures"], "T1006", source)
    if record.get("missing_profile_target"):
        raise_error("T1001", "command inferred profile or target when explicit selection is required", record, source, "profile-target-option")
    if record.get("unsupported_output_format"):
        raise_error("T1002", "unsupported output format", record, source, "structured-output-format")
    if record.get("authority_denial"):
        raise_error("T1003", "operation requires authority not granted to command", record, source, "capability-grant")
    if record.get("artifact_verification_failure"):
        raise_error("T1004", "release command lacks artifact verification", record, source, "artifact-verification")
    if record.get("secret_leak"):
        raise_error("T1005", "CLI output exposes a secret", record, source, "secret-redaction")
    if record.get("invalid_usage"):
        raise_error("T1006", "invalid command usage", record, source, "command-usage")
    if record.get("unstable_plugin_output"):
        raise_error("T1007", "plugin lacks stable structured output", record, source, "stable-plugin-output")
    if record.get("hidden_network"):
        raise_error("T1008", "command performs hidden network access", record, source, "network-policy")


def validate_repl_session(record: dict[str, Any], source: str) -> None:
    require_fields(record, ["session_id", "project_manifest_hash", "lockfile_hash", "profile", "target", "namespace", "capability_grants", "evaluation_history", "diagnostics", "transcript_redacted"], "T2001", source)
    if record.get("missing_profile_target"):
        raise_error("T2001", "REPL effectful evaluation lacks profile or target", record, source, "repl-profile-target")
    if record.get("missing_capability"):
        raise_error("T2002", "REPL runtime effect lacks capability grant", record, source, "repl-capability-grant")
    if record.get("unsafe_without_policy"):
        raise_error("T2003", "unsafe REPL operation lacks audit policy", record, source, "unsafe-policy")
    if record.get("transcript_secret_leak"):
        raise_error("T2004", "REPL transcript leaks a secret", record, source, "transcript-redaction")
    if record.get("semantic_mismatch"):
        raise_error("T2005", "REPL result diverges from compiled semantics", record, source, "compiled-semantics")
    if record.get("replay_violation"):
        raise_error("T2006", "REPL replay repeats live nondeterministic effects", record, source, "recorded-effects")
    if record.get("invalid_hot_replacement"):
        raise_error("T2007", "hot replacement invalidates checked assumptions", record, source, "hot-load-assumptions")


def validate_formatter_report(record: dict[str, Any], source: str) -> None:
    require_fields(record, ["formatter_version", "configuration_hash", "reader_round_trip", "changed_files", "diff_output", "json_report", "comments_preserved", "metadata_preserved"], "T3001", source)
    if record.get("parse_failure"):
        raise_error("T3001", "formatter input cannot be parsed by reader", record, source, "reader-parse")
    if not record.get("reader_round_trip") or record.get("round_trip_mismatch"):
        raise_error("T3002", "formatted output changes reader output", record, source, "reader-round-trip")
    if record.get("generated_write_denial"):
        raise_error("T3003", "formatter attempted generated-source write without policy", record, source, "generated-source-policy")
    if record.get("invalid_configuration"):
        raise_error("T3004", "formatter configuration conflicts with edition", record, source, "formatter-configuration")
    if record.get("comment_metadata_loss") or not record.get("comments_preserved") or not record.get("metadata_preserved"):
        raise_error("T3005", "formatter moved or lost comments or metadata", record, source, "comment-metadata-preservation")
    if record.get("semantic_refactor"):
        raise_error("T3006", "formatter attempted semantic refactor", record, source, "formatting-only")


def validate_lint_report(record: dict[str, Any], source: str) -> None:
    require_fields(record, ["rules", "diagnostics", "baseline", "rule_metadata", "compiler_facts", "profile_target_applicability", "json_export"], "T4001", source)
    if record.get("unknown_rule"):
        raise_error("T4001", "lint configuration names unknown rule", record, source, "known-rule-id")
    if record.get("stale_baseline"):
        raise_error("T4002", "lint baseline hides changed source diagnostic", record, source, "fresh-baseline")
    if record.get("unsafe_autofix"):
        raise_error("T4003", "lint auto-fix alters semantics without validation", record, source, "validated-autofix")
    if record.get("unavailable_compiler_fact"):
        raise_error("T4004", "lint claimed compiler facts unavailable for target", record, source, "compiler-fact")
    if record.get("release_blocking_lint"):
        raise_error("T4005", "release policy maps lint to blocker", record, source, "lint-release-policy")
    if record.get("invalid_rule_configuration"):
        raise_error("T4006", "lint rule configuration is invalid", record, source, "rule-configuration")


def validate_lsp_matrix(record: dict[str, Any], source: str) -> None:
    require_fields(record, ["server_id", "compiler_state", "diagnostics_match_cli", "hover_facts", "completion_constraints", "code_action_records", "rename_boundaries", "trace_redacted"], "T5001", source)
    if not record.get("diagnostics_match_cli") or record.get("diagnostic_mismatch"):
        raise_error("T5001", "LSP diagnostics conflict with compiler diagnostics", record, source, "compiler-diagnostic-match")
    if record.get("unsafe_code_action"):
        raise_error("T5002", "code action bypasses compiler checks", record, source, "checked-code-action")
    if record.get("ambiguous_rename"):
        raise_error("T5003", "rename crosses ambiguous macro-generated binding", record, source, "rename-identity")
    if record.get("profile_illegal_completion"):
        raise_error("T5004", "completion includes symbol illegal in active profile", record, source, "profile-legal-completion")
    if record.get("trace_secret_leak"):
        raise_error("T5005", "LSP trace contains secret", record, source, "trace-redaction")
    if record.get("generated_edit_denial"):
        raise_error("T5006", "generated-file edit is not permitted by source artifact", record, source, "generated-file-edit-policy")


def validate_debugger_trace(record: dict[str, Any], source: str) -> None:
    require_fields(record, ["session_id", "artifact_id", "profile", "target", "debug_data_version", "breakpoints", "stack_frames", "variable_reports", "policy_denials", "source_map_validation"], "T6001", source)
    if record.get("metadata_mismatch"):
        raise_error("T6001", "debug metadata does not match artifact", record, source, "debug-metadata")
    if record.get("breakpoint_mapping_failure"):
        raise_error("T6002", "breakpoint cannot map to source or artifact location", record, source, "breakpoint-map")
    if record.get("debug_authority_denial"):
        raise_error("T6003", "state mutation lacks debug authority", record, source, "debug-authority")
    if record.get("redacted_value_access"):
        raise_error("T6004", "debugger attempted to inspect redacted value", record, source, "redacted-value")
    if record.get("unsafe_replay_side_effect"):
        raise_error("T6005", "workflow replay would repeat side effects", record, source, "replay-side-effect")
    if record.get("ai_trace_redaction_violation"):
        raise_error("T6006", "AI trace exposes secret or no-store data", record, source, "ai-trace-redaction")
    if record.get("optimized_away_value"):
        raise_error("T6007", "optimized-away value was claimed as live", record, source, "optimized-away-state")


def validate_documentation_artifact(record: dict[str, Any], source: str) -> None:
    require_fields(record, ["artifact_id", "source_hash", "compiler_version", "package_version", "api_signature_index", "effect_capability_docs", "schema_links", "example_validation_report", "structured_docs", "redacted"], "T7001", source)
    if record.get("stale_docs"):
        raise_error("T7001", "generated docs are stale against source hash", record, source, "source-hash")
    if record.get("missing_effect_docs"):
        raise_error("T7002", "effectful public API lacks effect or capability docs", record, source, "effect-capability-docs")
    if record.get("failing_example"):
        raise_error("T7003", "runnable example failed checks", record, source, "example-validation")
    if record.get("missing_unsafe_link"):
        raise_error("T7004", "unsafe API lacks audit metadata link", record, source, "unsafe-audit-link")
    if record.get("protected_data_leak"):
        raise_error("T7005", "generated docs contain protected data", record, source, "doc-redaction")
    if record.get("generated_doc_mismatch"):
        raise_error("T7006", "AI-generated docs contradict compiler facts", record, source, "source-fact-match")
    if record.get("missing_schema_link"):
        raise_error("T7007", "schema docs lack schema artifact reference", record, source, "schema-artifact-link")


def validate_dev_server_session(record: dict[str, Any], source: str) -> None:
    require_fields(record, ["session_id", "project", "profile", "target", "capability_grants", "incremental_updates", "diagnostic_stream", "artifact_events", "runtime_log", "bug_report_redacted"], "T8001", source)
    if record.get("missing_profile_target"):
        raise_error("T8001", "dev server launch lacks profile or target", record, source, "dev-profile-target")
    if record.get("capability_denial"):
        raise_error("T8002", "runtime effect is not granted to dev session", record, source, "dev-capability-grant")
    if record.get("unsafe_hot_reload"):
        raise_error("T8003", "hot reload invalidates checked assumptions", record, source, "safe-hot-reload")
    if record.get("secret_redaction_violation"):
        raise_error("T8004", "dev diagnostics, logs, or status API leaks secret", record, source, "dev-redaction")
    if record.get("dev_release_contamination"):
        raise_error("T8005", "dev-only artifact used as release artifact", record, source, "release-artifact-boundary")
    if record.get("replay_gap"):
        raise_error("T8006", "replay-sensitive workflow has unrecorded nondeterminism", record, source, "replay-record")
    if record.get("endpoint_auth_failure"):
        raise_error("T8007", "remote dev endpoint lacks required auth", record, source, "dev-endpoint-auth")


def validate_registry_ux(record: dict[str, Any], source: str) -> None:
    require_fields(record, ["package_detail_json", "human_view", "update_diff", "verification_report", "search_results", "access_denials", "policy_compatibility"], "T9001", source)
    if record.get("hidden_capability_diff"):
        raise_error("T9001", "registry update diff hides capability expansion", record, source, "capability-diff")
    if record.get("policy_incompatible_recommendation"):
        raise_error("T9002", "registry recommends package incompatible with active policy", record, source, "policy-compatibility")
    if record.get("private_metadata_leak"):
        raise_error("T9003", "registry UX leaks private package metadata", record, source, "private-metadata")
    if record.get("unverifiable_badge"):
        raise_error("T9004", "verification badge lacks machine-verifiable report", record, source, "verification-report")
    if record.get("search_filter_violation"):
        raise_error("T9005", "registry search ignored profile or target filter", record, source, "search-filter")
    if record.get("latent_state_omission"):
        raise_error("T9006", "latent package UI omits state or review evidence", record, source, "latent-review-state")


def validate_ir_inspector(record: dict[str, Any], source: str) -> None:
    require_fields(record, ["stage_id", "compiler_version", "project_hash", "profile", "target", "stage_views", "source_span_maps", "pass_diff_reports", "preservation_reports", "redacted"], "T10001", source)
    if record.get("missing_stage_identity"):
        raise_error("T10001", "stage export lacks profile, target, or stage id", record, source, "stage-identity")
    if record.get("lost_source_origin"):
        raise_error("T10002", "IR view drops source origin", record, source, "source-origin")
    if record.get("missing_preservation_evidence"):
        raise_error("T10003", "optimization diff removed checks without preservation evidence", record, source, "preservation-evidence")
    if record.get("omitted_target_assumption"):
        raise_error("T10004", "backend view omits target assumptions", record, source, "target-assumption")
    if record.get("redaction_failure"):
        raise_error("T10005", "explorer bundle contains secret", record, source, "explorer-redaction")
    if record.get("generated_origin_gap"):
        raise_error("T10006", "generated code view loses origin chain", record, source, "generated-origin")


def validate_profiler_report(record: dict[str, Any], source: str) -> None:
    require_fields(record, ["report_id", "profile", "target", "artifact_id", "benchmark_id", "environment", "compiler_version", "samples", "comparison_policy", "check_elision_report", "capabilities"], "T11001", source)
    if record.get("missing_identity"):
        raise_error("T11001", "performance report lacks identity field", record, source, "performance-identity")
    if record.get("incompatible_comparison"):
        raise_error("T11002", "performance comparison inputs are incompatible", record, source, "benchmark-identity")
    if record.get("missing_check_elision_evidence"):
        raise_error("T11003", "check elision view lacks proof or analysis evidence", record, source, "check-elision-evidence")
    if record.get("unsupported_counter"):
        raise_error("T11004", "target counter is unsupported for target", record, source, "target-counter-support")
    if record.get("performance_regression"):
        raise_error("T11005", "performance regression exceeds threshold", record, source, "regression-threshold")
    if record.get("capability_overreach"):
        raise_error("T11006", "profiling session requires unrelated capability", record, source, "profiler-capability-scope")


def validate_safety_audit(record: dict[str, Any], source: str) -> None:
    require_fields(record, ["report_id", "unsafe_islands", "capability_graph", "taint_graph", "ffi_boundaries", "ai_safety", "package_safety", "proof_index", "missing_evidence_report", "redacted"], "T12001", source)
    if record.get("missing_unsafe_island"):
        raise_error("T12001", "audit omits known unsafe island", record, source, "unsafe-island-evidence")
    if record.get("wrapper_evidence_gap"):
        raise_error("T12002", "safe wrapper claim lacks evidence", record, source, "wrapper-evidence")
    if record.get("capability_graph_ambiguity"):
        raise_error("T12003", "capability graph collapses requested and granted states", record, source, "capability-state")
    if record.get("taint_sink_omission"):
        raise_error("T12004", "taint view omits sink", record, source, "taint-sink")
    if record.get("proof_checker_omission"):
        raise_error("T12005", "proof view omits checker identity", record, source, "proof-checker")
    if record.get("audit_evidence_gap"):
        raise_error("T12006", "audit pass hides missing required evidence", record, source, "audit-evidence")
    if record.get("redaction_failure"):
        raise_error("T12007", "audit export leaks protected data", record, source, "audit-redaction")


def validate_ai_tooling(record: dict[str, Any], source: str) -> None:
    require_fields(record, ["tool_id", "modes", "plan_artifact", "patch_artifact", "generated_source_provenance", "prompt_model_ledger", "tool_call_ledger", "validation_report", "human_review_record", "replay_trace"], "T13001", source)
    if record.get("invalid_patch_artifact"):
        raise_error("T13001", "AI patch lacks schema-valid patch artifact", record, source, "patch-artifact")
    if record.get("unchecked_generated_source"):
        raise_error("T13002", "generated source was not checked by compiler", record, source, "generated-source-checks")
    if record.get("hidden_tool_use"):
        raise_error("T13003", "AI trace includes hidden or unauthorized tool use", record, source, "declared-tool-use")
    if record.get("missing_human_review"):
        raise_error("T13004", "write action lacks required human review", record, source, "human-review")
    if record.get("prompt_authority_violation"):
        raise_error("T13005", "repository content treated as instruction authority", record, source, "prompt-authority")
    if record.get("missing_generated_test_provenance"):
        raise_error("T13006", "generated test lacks provenance", record, source, "generated-test-provenance")
    if record.get("missing_package_safety_diff"):
        raise_error("T13007", "package update proposal lacks capability and safety diff", record, source, "package-safety-diff")
    if record.get("incomplete_ai_trace"):
        raise_error("T13008", "AI trace omits model, prompt, tool, or policy records", record, source, "ai-trace")


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
    raise ToolingExperienceError(
        code,
        message,
        record=record,
        source=source,
        missing_fact=missing_fact,
        remediation="Update the Phase 13 tooling fixture, artifact view, diagnostic, or developer-experience evidence.",
    )


def load_manifest_file(path: Path) -> dict[str, Any]:
    raw = json.loads(path.read_text(encoding="utf-8"))
    if raw.get("kind") != "tooling-experience-fixture-patch":
        return raw
    base_value = raw.get("base")
    if not base_value:
        raise_error("T1006", "fixture patch lacks base manifest", {}, str(path), "base-manifest")
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
