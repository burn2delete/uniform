"""Phase 15 bootstrap and self-hosting validation."""

from __future__ import annotations

import hashlib
import json
from pathlib import Path
from typing import Any


ROOT = Path(__file__).resolve().parents[2]

DOCUMENT_COMPONENTS = {
    "BOOT1": "bootstrap_strategy",
    "BOOT2": "seed_compiler",
    "BOOT3": "self_hosted_plan",
    "BOOT4": "compiler_coding_standard",
    "BOOT5": "stage_compatibility",
    "BOOT6": "trusting_trust",
    "BOOT7": "equivalence_validation",
    "BOOT8": "bootstrap_provenance",
}


class BootstrapSelfHostingError(Exception):
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
            "artifact_id": self.record.get("artifact_id"),
            "stage": self.record.get("stage"),
            "module": self.record.get("module"),
            "compiler": self.record.get("compiler_artifact_id") or self.record.get("compiler"),
            "source_span": self.record.get("source_span", {"source": self.source}),
            "missing_fact": self.missing_fact,
            "remediation": self.remediation,
            "analyzer_stage": "phase15-bootstrap-self-hosting-validation",
        }


def validate_bootstrap_self_hosting_file(path: Path) -> dict[str, Any]:
    return validate_bootstrap_self_hosting_manifest(load_manifest_file(path), str(path))


def bootstrap_self_hosting_diagnostic(path: Path) -> dict[str, Any] | None:
    try:
        validate_bootstrap_self_hosting_file(path)
    except BootstrapSelfHostingError as exc:
        return exc.to_diagnostic()
    return None


def validate_bootstrap_self_hosting_manifest(manifest: dict[str, Any], source: str) -> dict[str, Any]:
    if manifest.get("kind") != "bootstrap-self-hosting-input":
        raise_error("BOOT1001", "bootstrap input has wrong kind", {}, source, "bootstrap-self-hosting-input")

    components = {doc: require_dict(manifest, key, "BOOT1001", source) for doc, key in DOCUMENT_COMPONENTS.items()}
    validate_bootstrap_strategy(components["BOOT1"], source)
    validate_seed_compiler(components["BOOT2"], source)
    validate_self_hosted_plan(components["BOOT3"], source)
    validate_compiler_coding_standard(components["BOOT4"], source)
    validate_stage_compatibility(components["BOOT5"], source)
    validate_trusting_trust(components["BOOT6"], source)
    validate_equivalence_validation(components["BOOT7"], source)
    validate_bootstrap_provenance(components["BOOT8"], source)

    return {
        "kind": "bootstrap-self-hosting-artifact",
        "phase": "15",
        "bootstrap_stage_matrix": components["BOOT1"].get("stage_manifests"),
        "seed_compiler_manifest": components["BOOT2"],
        "self_hosted_component_manifest": components["BOOT3"].get("module_manifests"),
        "compiler_coding_standard_report": components["BOOT4"],
        "stage_compatibility_matrix": components["BOOT5"],
        "trusting_trust_report": components["BOOT6"],
        "equivalence_report": components["BOOT7"],
        "bootstrap_provenance_record": components["BOOT8"],
        "document_contracts": components,
        "coverage_summary": {
            "documents": len(DOCUMENT_COMPONENTS),
            "tasks": 6,
            "stage_count": len(components["BOOT1"].get("stages", [])),
            "status": ":passed",
        },
        "input_hash": artifact_hash(manifest),
        "diagnostics": [],
    }


def validate_bootstrap_strategy(record: dict[str, Any], source: str) -> None:
    require_fields(
        record,
        [
            "artifact_id",
            "stages",
            "stage_manifests",
            "trusted_inputs",
            "produced_artifacts",
            "supported_profiles",
            "supported_backends",
            "conformance_reports",
            "equivalence_reports",
            "tcb_deltas",
            "locked_dependencies",
            "compiler_lineage",
            "stage_gaps",
            "release_gates",
            "unsafe_audit_records",
        ],
        "BOOT1001",
        source,
    )
    if record.get("missing_stage_evidence") or not record.get("conformance_reports"):
        raise_error("BOOT1001", "bootstrap stage lacks conformance evidence", record, source, "stage-evidence")
    if record.get("undocumented_stage_gap"):
        raise_error("BOOT1002", "bootstrap stage gap is undocumented", record, source, "stage-gap")
    if record.get("missing_compiler_lineage"):
        raise_error("BOOT1003", "bootstrap artifact lacks compiler lineage", record, source, "compiler-lineage")
    if record.get("unexplained_stage_drift"):
        raise_error("BOOT1004", "bootstrap stage drift is unexplained", record, source, "stage-drift")
    if record.get("unlocked_dependency") or not record.get("locked_dependencies"):
        raise_error("BOOT1005", "bootstrap build uses unlocked dependencies", record, source, "locked-dependencies")
    if record.get("unsafe_audit_gap"):
        raise_error("BOOT1006", "compiler unsafe code lacks audit records", record, source, "unsafe-audit")


def validate_seed_compiler(record: dict[str, Any], source: str) -> None:
    require_fields(
        record,
        [
            "artifact_id",
            "implemented_documents",
            "excluded_documents",
            "supported_profiles",
            "diagnostics",
            "provenance_record",
            "host_dependencies",
            "runtime_assumptions",
            "bootstrap_backend_artifact",
            "conformance_report",
            "metadata_for_stage_comparison",
        ],
        "BOOT2001",
        source,
    )
    if record.get("undeclared_seed_feature"):
        raise_error("BOOT2001", "seed compiler feature is not declared", record, source, "seed-feature")
    if record.get("unsupported_profile_accepted"):
        raise_error("BOOT2002", "unsupported profile was accepted by seed compiler", record, source, "profile-rejection")
    if record.get("missing_seed_provenance"):
        raise_error("BOOT2003", "seed compiler artifact lacks provenance", record, source, "seed-provenance")
    if record.get("unstable_seed_diagnostic"):
        raise_error("BOOT2004", "seed compiler diagnostic is unstable", record, source, "diagnostic-code")
    if record.get("untracked_host_dependency"):
        raise_error("BOOT2005", "seed compiler host dependency is untracked", record, source, "host-dependency")
    if record.get("semantic_mismatch"):
        raise_error("BOOT2006", "seed compiler contradicts upstream language specs", record, source, "semantic-contract")


def validate_self_hosted_plan(record: dict[str, Any], source: str) -> None:
    require_fields(
        record,
        [
            "artifact_id",
            "migrated_modules",
            "profile",
            "module_manifests",
            "stage_comparisons",
            "equivalence_reports",
            "diagnostic_compatibility_report",
            "provenance_records",
            "tcb_deltas",
            "unsafe_audit_records",
        ],
        "BOOT3001",
        source,
    )
    if record.get("profile") not in [":meta", ":bootstrap"]:
        raise_error("BOOT3002", "self-hosted compiler module is outside meta/bootstrap profile", record, source, "meta-profile")
    if record.get("missing_module_conformance"):
        raise_error("BOOT3001", "self-hosted module lacks conformance evidence", record, source, "module-conformance")
    if record.get("ambient_authority"):
        raise_error("BOOT3002", "self-hosted compiler module uses ambient authority", record, source, "ambient-authority")
    if record.get("diagnostic_drift"):
        raise_error("BOOT3003", "self-hosted diagnostics drift from accepted codes or spans", record, source, "diagnostic-compatibility")
    if record.get("generated_provenance_gap"):
        raise_error("BOOT3004", "generated compiler code lacks provenance", record, source, "generated-provenance")
    if record.get("unsafe_audit_gap"):
        raise_error("BOOT3005", "self-hosted compiler unsafe code lacks audit metadata", record, source, "unsafe-audit")
    if record.get("stale_stage_matrix"):
        raise_error("BOOT3006", "module migration did not update stage matrix", record, source, "stage-matrix")


def validate_compiler_coding_standard(record: dict[str, Any], source: str) -> None:
    require_fields(
        record,
        [
            "artifact_id",
            "module_manifests",
            "effect_capability_declarations",
            "pass_preservation_report",
            "diagnostic_manifest",
            "deterministic_output_report",
            "unsafe_audit_report",
            "ambient_access_policy",
            "generated_artifact_provenance",
            "preservation_tests",
        ],
        "BOOT4001",
        source,
    )
    if record.get("undeclared_effect"):
        raise_error("BOOT4001", "compiler module has undeclared effects", record, source, "effect-declaration")
    if record.get("nondeterministic_output"):
        raise_error("BOOT4002", "compiler pass output is nondeterministic", record, source, "deterministic-output")
    if record.get("lost_preserved_fact"):
        raise_error("BOOT4003", "compiler pass lost a preserved fact", record, source, "preserved-fact")
    if record.get("ambient_host_access"):
        raise_error("BOOT4004", "compiler module reads ambient host state", record, source, "ambient-host-access")
    if record.get("unsafe_audit_gap"):
        raise_error("BOOT4005", "compiler unsafe island lacks audit record", record, source, "unsafe-audit")
    if record.get("diagnostic_code_gap"):
        raise_error("BOOT4006", "compiler diagnostic lacks stable code", record, source, "diagnostic-code")
    if record.get("missing_preservation_tests"):
        raise_error("BOOT4007", "compiler module lacks preservation tests", record, source, "preservation-tests")


def validate_stage_compatibility(record: dict[str, Any], source: str) -> None:
    require_fields(
        record,
        [
            "artifact_id",
            "version",
            "stages",
            "supported_features",
            "unsupported_features",
            "conformance_link_table",
            "profile_compliance_reports",
            "backend_conformance_reports",
            "stage_gap_report",
            "support_level_report",
            "release_readiness_summary",
            "matrix_change_record",
        ],
        "BOOT5001",
        source,
    )
    if record.get("missing_matrix_row"):
        raise_error("BOOT5001", "bootstrap stage lacks matrix row", record, source, "matrix-row")
    if record.get("unsupported_feature_claim"):
        raise_error("BOOT5002", "stage claims unsupported feature", record, source, "supported-feature")
    if record.get("missing_conformance_link") or not record.get("conformance_link_table"):
        raise_error("BOOT5003", "stage matrix row lacks conformance link", record, source, "conformance-link")
    if record.get("unreviewed_release_gap"):
        raise_error("BOOT5004", "release candidate has unreviewed stage gap", record, source, "release-gap-review")
    if record.get("unversioned_matrix_change"):
        raise_error("BOOT5005", "stage matrix change lacks version", record, source, "matrix-version")
    if record.get("implicit_support_ambiguity"):
        raise_error("BOOT5006", "stage support is implied instead of explicitly restated", record, source, "explicit-support")


def validate_trusting_trust(record: dict[str, Any], source: str) -> None:
    require_fields(
        record,
        [
            "artifact_id",
            "build_recipe",
            "environment_manifest",
            "locked_dependencies",
            "compiler_lineage",
            "rebuild_comparison_report",
            "diverse_rebuild_report",
            "accepted_delta_report",
            "revocation_check_report",
            "release_trust_summary",
            "network_policy",
        ],
        "BOOT6001",
        source,
    )
    if record.get("missing_environment_record") or not record.get("environment_manifest"):
        raise_error("BOOT6001", "bootstrap rebuild lacks controlled environment record", record, source, "environment-record")
    if record.get("compiler_lineage_gap"):
        raise_error("BOOT6002", "bootstrap stage artifact lacks compiler lineage", record, source, "compiler-lineage")
    if record.get("unexplained_hash_drift"):
        raise_error("BOOT6003", "bootstrap rebuild hash drift is unexplained", record, source, "hash-drift")
    if record.get("revoked_input"):
        raise_error("BOOT6004", "bootstrap input was revoked", record, source, "revocation-check")
    if record.get("diverse_identity_gap"):
        raise_error("BOOT6005", "diverse rebuild lacks independent toolchain identity", record, source, "diverse-toolchain")
    if record.get("uncontrolled_network_input"):
        raise_error("BOOT6006", "bootstrap rebuild used uncontrolled network input", record, source, "network-policy")


def validate_equivalence_validation(record: dict[str, Any], source: str) -> None:
    require_fields(
        record,
        [
            "artifact_id",
            "compiler_a",
            "compiler_b",
            "compared_artifacts",
            "comparison_modes",
            "accepted_deltas",
            "conformance_report",
            "diagnostic_comparison_report",
            "ir_comparison_report",
            "performance_bounds",
            "release_decision",
            "provenance_links",
        ],
        "BOOT7001",
        source,
    )
    if record.get("missing_compiler_identity"):
        raise_error("BOOT7001", "equivalence report lacks compiler identities", record, source, "compiler-identity")
    if record.get("artifact_drift"):
        raise_error("BOOT7002", "stage artifact drift is unexplained", record, source, "artifact-drift")
    if record.get("diagnostic_drift"):
        raise_error("BOOT7003", "self-hosted diagnostic drift is unexplained", record, source, "diagnostic-drift")
    if record.get("unreviewed_delta"):
        raise_error("BOOT7004", "accepted equivalence delta lacks review", record, source, "delta-review")
    if record.get("missing_stage_output"):
        raise_error("BOOT7005", "equivalence report lacks required stage output", record, source, "stage-output")
    if record.get("conformance_failure"):
        raise_error("BOOT7006", "stage conformance suite failed", record, source, "conformance-report")
    if record.get("performance_bound_failure"):
        raise_error("BOOT7007", "bootstrap performance bound failed", record, source, "performance-bound")


def validate_bootstrap_provenance(record: dict[str, Any], source: str) -> None:
    require_fields(
        record,
        [
            "artifact_id",
            "stage",
            "source_graph_hash",
            "compiler_artifact_id",
            "compiler_hash",
            "lockfile_hash",
            "build_recipe_hash",
            "environment_manifest_hash",
            "dependency_graph_hash",
            "conformance_report_links",
            "equivalence_report_links",
            "safety_report_links",
            "sbom_links",
            "signature_links",
            "builder_identity",
            "canonicalization",
            "revocation_check",
            "compiler_lineage_graph",
            "auditor_query_index",
        ],
        "BOOT8001",
        source,
    )
    if record.get("missing_provenance"):
        raise_error("BOOT8001", "bootstrap artifact lacks provenance record", record, source, "bootstrap-provenance")
    if record.get("compiler_lineage_gap"):
        raise_error("BOOT8002", "bootstrap provenance has compiler lineage gap", record, source, "compiler-lineage")
    if record.get("undeclared_cycle"):
        raise_error("BOOT8003", "bootstrap provenance contains undeclared cycle", record, source, "lineage-cycle")
    if record.get("missing_release_evidence"):
        raise_error("BOOT8004", "release candidate lacks required evidence links", record, source, "release-evidence")
    if record.get("noncanonical_signature"):
        raise_error("BOOT8005", "provenance signature covers noncanonical payload", record, source, "canonical-signature")
    if record.get("revoked_input"):
        raise_error("BOOT8006", "provenance contains revoked input", record, source, "revoked-input")
    if record.get("auditor_query_failure"):
        raise_error("BOOT8007", "auditor cannot traverse compiler lineage", record, source, "auditor-query")


def require_dict(manifest: dict[str, Any], key: str, code: str, source: str) -> dict[str, Any]:
    value = manifest.get(key)
    if not isinstance(value, dict):
        raise_error(code, f"manifest lacks {key}", {}, source, key)
    return value


def require_fields(record: dict[str, Any], fields: list[str], code: str, source: str) -> None:
    missing = [field for field in fields if field not in record or record.get(field) in [None, "", []]]
    if missing:
        raise_error(code, f"record lacks required fields: {', '.join(missing)}", record, source, ",".join(missing))


def raise_error(code: str, message: str, record: dict[str, Any], source: str, missing_fact: str) -> None:
    raise BootstrapSelfHostingError(
        code,
        message,
        record=record,
        source=source,
        missing_fact=missing_fact,
        remediation="Update the Phase 15 bootstrap manifest, fixture, provenance, or equivalence evidence.",
    )


def load_manifest_file(path: Path) -> dict[str, Any]:
    data = load_json(path)
    base_ref = data.pop("extends", None)
    if not base_ref:
        return data
    base_path = Path(base_ref)
    if not base_path.is_absolute():
        base_path = path.parent / base_path
    return deep_merge(load_manifest_file(base_path), data)


def load_json(path: Path) -> dict[str, Any]:
    return json.loads(path.read_text(encoding="utf-8"))


def deep_merge(base: dict[str, Any], overlay: dict[str, Any]) -> dict[str, Any]:
    merged = dict(base)
    for key, value in overlay.items():
        if isinstance(value, dict) and isinstance(merged.get(key), dict):
            merged[key] = deep_merge(merged[key], value)
        else:
            merged[key] = value
    return merged


def artifact_hash(value: Any) -> str:
    data = json.dumps(value, sort_keys=True, separators=(",", ":"))
    return "sha256:" + hashlib.sha256(data.encode("utf-8")).hexdigest()
