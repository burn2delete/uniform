"""Phase 12 build, package, and artifact validation."""

from __future__ import annotations

import hashlib
import json
from pathlib import Path
from typing import Any


DOCUMENT_COMPONENTS = {
    "PKG1": "project_manifest",
    "PKG2": "build_graph",
    "PKG3": "artifact_manifest",
    "PKG4": "package_operation",
    "PKG5": "resolution_report",
    "PKG6": "capability_manifest",
    "PKG7": "reproducible_build",
    "PKG8": "package_safety",
    "PKG9": "registry_record",
    "PKG10": "provenance_record",
    "PKG11": "target_matrix",
    "PKG12": "release_verification",
}


class PackageArtifactError(Exception):
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
            "package_id": self.record.get("package_id"),
            "artifact_id": self.record.get("artifact_id"),
            "dependency_id": self.record.get("dependency_id"),
            "registry_id": self.record.get("registry_id"),
            "profile": self.record.get("profile"),
            "target": self.record.get("target"),
            "capability": self.record.get("capability"),
            "effect": self.record.get("effect"),
            "source_span": self.record.get("source_span", {"source": self.source}),
            "missing_fact": self.missing_fact,
            "remediation": self.remediation,
            "analyzer_stage": "phase12-package-artifact-validation",
        }


def validate_package_artifacts_file(path: Path) -> dict[str, Any]:
    return validate_package_artifacts_manifest(load_manifest_file(path), str(path))


def package_artifact_diagnostic(path: Path) -> dict[str, Any] | None:
    try:
        validate_package_artifacts_file(path)
    except PackageArtifactError as exc:
        return exc.to_diagnostic()
    return None


def validate_package_artifacts_manifest(manifest: dict[str, Any], source: str) -> dict[str, Any]:
    if manifest.get("kind") != "package-artifacts-input":
        raise_error("PKG1001", "package artifact input has wrong kind", {}, source, "package-artifacts-input")

    components = {doc: require_dict(manifest, key, "PKG1001", source) for doc, key in DOCUMENT_COMPONENTS.items()}
    validate_project_manifest(components["PKG1"], source)
    validate_build_graph(components["PKG2"], source)
    validate_artifact_manifest(components["PKG3"], source)
    validate_package_operation(components["PKG4"], source)
    validate_resolution_report(components["PKG5"], source)
    validate_capability_manifest(components["PKG6"], source)
    validate_reproducible_build(components["PKG7"], source)
    validate_package_safety(components["PKG8"], source)
    validate_registry_record(components["PKG9"], source)
    validate_provenance_record(components["PKG10"], source)
    validate_target_matrix(components["PKG11"], source)
    validate_release_verification(components["PKG12"], source)

    return {
        "kind": "package-artifacts-artifact",
        "phase": "12",
        "package": manifest.get("package"),
        "project_manifest": components["PKG1"],
        "lockfile": components["PKG5"].get("lockfile"),
        "build_graph": components["PKG2"],
        "package_manifest": components["PKG4"].get("package_manifest"),
        "capability_manifest": components["PKG6"],
        "sbom": components["PKG12"].get("sbom"),
        "signature_verification_report": components["PKG12"].get("verification_report"),
        "artifact_manifest": components["PKG3"],
        "reproducible_build": components["PKG7"],
        "provenance_record": components["PKG10"],
        "target_matrix": components["PKG11"],
        "document_contracts": components,
        "coverage_summary": {
            "documents": len(DOCUMENT_COMPONENTS),
            "tasks": 6,
            "package_id": components["PKG1"].get("package_id"),
            "artifact_id": components["PKG3"].get("artifact_id"),
            "status": ":passed",
        },
        "input_hash": artifact_hash(manifest),
        "diagnostics": [],
    }


def validate_project_manifest(record: dict[str, Any], source: str) -> None:
    require_fields(
        record,
        [
            "package_id",
            "version",
            "edition",
            "source_roots",
            "generated_source_roots",
            "profiles",
            "targets",
            "entrypoints",
            "dependencies",
            "registries",
            "effects",
            "capabilities",
            "artifacts",
            "policy",
            "canonical_hash",
            "offline_parse",
        ],
        "PKG1001",
        source,
    )
    if not record.get("offline_parse"):
        raise_error("PKG1001", "project configuration requires effectful code execution", record, source, "offline-readable-project")
    if record.get("unknown_profile") or record.get("unknown_target_pair"):
        raise_error("PKG1002", "project declares unknown profile or target pair", record, source, "profile-target-legality")
    if record.get("unresolved_entrypoint"):
        raise_error("PKG1003", "project entrypoint cannot be resolved", record, source, "entrypoint-resolution")
    if record.get("dependency_outside_registry"):
        raise_error("PKG1004", "dependency is outside declared registry or local path grants", record, source, "registry-policy")
    if record.get("undeclared_authority"):
        raise_error("PKG1005", "project or dependency introduced an undeclared effect or capability", record, source, "declared-authority")
    if record.get("release_requires_lockfile") and not record.get("lockfile_complete"):
        raise_error("PKG1006", "release build lacks a complete lockfile", record, source, "release-lockfile")
    if record.get("unsafe_policy_mismatch"):
        raise_error("PKG1007", "unsafe forms conflict with package unsafe policy", record, source, "unsafe-policy")
    if record.get("missing_artifact_kind"):
        raise_error("PKG1008", "artifact plan is missing a required kind", record, source, "artifact-plan-kind")
    require_non_empty(record, "profiles", "PKG1002", source)
    require_non_empty(record, "targets", "PKG1002", source)


def validate_build_graph(record: dict[str, Any], source: str) -> None:
    require_fields(
        record,
        [
            "graph_id",
            "nodes",
            "project_manifest_hash",
            "lockfile_hash",
            "compiler_id",
            "policy_hash",
            "cache_keys",
            "generated_source_provenance",
            "release_evidence",
        ],
        "PKG2001",
        source,
    )
    if record.get("undeclared_effect"):
        raise_error("PKG2001", "build step uses an undeclared effect", record, source, "declared-build-effect")
    if record.get("invalid_cache_reuse"):
        raise_error("PKG2002", "cache hit ignored policy, compiler, profile, target, or lockfile input", record, source, "cache-key")
    if record.get("missing_generated_provenance"):
        raise_error("PKG2003", "generated source has no provenance edge", record, source, "generated-source-provenance")
    if record.get("graph_cycle"):
        raise_error("PKG2004", "build graph cycle lacks bootstrap fixed-point metadata", record, source, "bootstrap-cycle-metadata")
    if record.get("target_matrix_failure"):
        raise_error("PKG2005", "target matrix build did not complete for required pair", record, source, "target-matrix-evidence")
    if record.get("missing_release_evidence"):
        raise_error("PKG2006", "release artifact lacks required evidence", record, source, "release-evidence")
    if record.get("unauthorized_network"):
        raise_error("PKG2007", "build step attempted unauthorized network access", record, source, "build-network-grant")
    if record.get("plugin_authority_violation"):
        raise_error("PKG2008", "build plugin used authority outside declared effects", record, source, "plugin-authority")
    for node in record.get("nodes", []):
        require_fields(node, ["node_id", "inputs", "outputs", "effects", "cache_key", "tool_id"], "PKG2001", source)


def validate_artifact_manifest(record: dict[str, Any], source: str) -> None:
    require_fields(
        record,
        [
            "artifact_id",
            "kind",
            "schema_version",
            "package_id",
            "source_graph_hash",
            "project_manifest_hash",
            "lockfile_hash",
            "compiler_id",
            "profile",
            "target",
            "content_hash",
            "dependency_graph_hash",
            "evidence",
            "capability_summary",
            "provenance",
            "canonical",
        ],
        "PKG3001",
        source,
    )
    if record.get("missing_manifest"):
        raise_error("PKG3001", "artifact has no manifest", record, source, "artifact-manifest")
    if record.get("schema_mismatch"):
        raise_error("PKG3002", "artifact manifest schema version is unknown or unsupported", record, source, "schema-version")
    if record.get("missing_identity_field"):
        raise_error("PKG3003", "artifact manifest is missing an identity field", record, source, "artifact-identity")
    if record.get("content_hash_mismatch"):
        raise_error("PKG3004", "artifact content hash mismatch", record, source, "content-hash")
    if record.get("missing_evidence_link"):
        raise_error("PKG3005", "artifact safety or proof claim lacks evidence link", record, source, "evidence-link")
    if record.get("unknown_kind"):
        raise_error("PKG3006", "artifact kind is unknown", record, source, "artifact-kind-schema")
    if not record.get("canonical") or record.get("noncanonical_signature_input"):
        raise_error("PKG3007", "artifact signing input is not canonical", record, source, "canonical-manifest")
    if record.get("unverified_consumer"):
        raise_error("PKG3008", "artifact consumer ignored required evidence", record, source, "consumer-verification")


def validate_package_operation(record: dict[str, Any], source: str) -> None:
    require_fields(
        record,
        [
            "operation",
            "package_id",
            "version",
            "registry",
            "effects",
            "lockfile_diff",
            "package_manifest",
            "verification_report",
            "capability_diff",
            "safety_diff",
            "provenance_diff",
            "machine_readable",
        ],
        "PKG4001",
        source,
    )
    if record.get("download_verification_failure"):
        raise_error("PKG4001", "package download verification failed", record, source, "download-verification")
    if record.get("lockfile_metadata_omission"):
        raise_error("PKG4002", "lockfile write omitted capability or provenance summary", record, source, "lockfile-metadata")
    if record.get("capability_expansion_unreviewed"):
        raise_error("PKG4003", "package update adds capabilities without required review", record, source, "capability-review")
    if record.get("publish_policy_failure"):
        raise_error("PKG4004", "publish operation violates release policy", record, source, "publish-policy")
    if record.get("plugin_effect_violation"):
        raise_error("PKG4005", "package manager plugin has undeclared effects", record, source, "plugin-effect")
    if record.get("offline_cache_incomplete"):
        raise_error("PKG4006", "offline install cache is incomplete", record, source, "offline-cache")
    if record.get("credential_leak"):
        raise_error("PKG4007", "registry credential leaked to log or artifact", record, source, "credential-redaction")
    if record.get("yank_metadata_violation"):
        raise_error("PKG4008", "yank removed metadata required for rebuild", record, source, "historical-metadata")


def validate_resolution_report(record: dict[str, Any], source: str) -> None:
    require_fields(
        record,
        [
            "report_id",
            "canonical_inputs",
            "selected_graph",
            "lockfile",
            "capability_diff",
            "target_variant_table",
            "provenance_summary",
            "offline_resolution_proof",
            "solver_tiebreakers",
        ],
        "PKG5001",
        source,
    )
    if record.get("unsatisfied_constraint"):
        raise_error("PKG5001", "version constraint is unsatisfied", record, source, "version-constraint")
    if record.get("capability_incompatible"):
        raise_error("PKG5002", "dependency requires a denied capability", record, source, "capability-compatibility")
    if record.get("target_variant_missing"):
        raise_error("PKG5003", "dependency artifact variant does not support target", record, source, "target-variant")
    if record.get("lockfile_incomplete"):
        raise_error("PKG5004", "lockfile is incomplete for release or offline resolution", record, source, "complete-lockfile")
    if record.get("private_registry_denied"):
        raise_error("PKG5005", "private registry dependency lacks explicit grant", record, source, "private-registry-grant")
    if record.get("revoked_package"):
        raise_error("PKG5006", "selected package is revoked or yanked under policy", record, source, "revocation-policy")
    if record.get("nondeterministic_solver_input"):
        raise_error("PKG5007", "resolver inputs are nondeterministic", record, source, "canonical-solver-input")
    if record.get("feature_conflict"):
        raise_error("PKG5008", "feature selection conflict is unresolved", record, source, "feature-conflict")


def validate_capability_manifest(record: dict[str, Any], source: str) -> None:
    require_fields(
        record,
        [
            "package_id",
            "effects",
            "capabilities",
            "denied_effects",
            "denied_capabilities",
            "per_target",
            "dependency_summaries",
            "deployment_grants",
            "runtime_handles",
            "audit_event_schema",
            "sbom_fields",
        ],
        "PKG6001",
        source,
    )
    if record.get("missing_capability_summary"):
        raise_error("PKG6001", "package artifact lacks capability summary", record, source, "capability-summary")
    if record.get("effect_without_capability"):
        raise_error("PKG6002", "runtime effect lacks capability derivation", record, source, "effect-capability-derivation")
    if record.get("authority_expansion"):
        raise_error("PKG6003", "effect or capability expansion lacks policy review", record, source, "authority-diff")
    if record.get("denied_authority"):
        raise_error("PKG6004", "denied effect or capability was requested", record, source, "denied-authority")
    if record.get("ambient_authority"):
        raise_error("PKG6005", "ambient authority is not represented by a runtime handle", record, source, "runtime-handle")
    if record.get("invalid_deployment_grant"):
        raise_error("PKG6006", "deployment granted a capability not requested by package", record, source, "deployment-grant")
    if record.get("sbom_capability_omission"):
        raise_error("PKG6007", "SBOM omits capability fields", record, source, "sbom-capability-field")
    if record.get("source_manifest_mismatch"):
        raise_error("PKG6008", "capability summary differs from effect analysis", record, source, "source-manifest-match")


def validate_reproducible_build(record: dict[str, Any], source: str) -> None:
    require_fields(
        record,
        [
            "recipe_id",
            "package_id",
            "source_hash",
            "project_hash",
            "lockfile_hash",
            "compiler_id",
            "environment",
            "target_matrix",
            "build_graph_hash",
            "allowed_external_inputs",
            "expected_artifact_ids",
            "output_hashes",
            "verification_report",
            "reproducible",
        ],
        "PKG7001",
        source,
    )
    if record.get("missing_recipe"):
        raise_error("PKG7001", "release reproducibility claim lacks build recipe", record, source, "build-recipe")
    if record.get("unlocked_dependency"):
        raise_error("PKG7002", "reproducible build used unlocked dependency", record, source, "locked-dependency")
    if record.get("uncontrolled_network"):
        raise_error("PKG7003", "reproducible build read network input outside declared inputs", record, source, "controlled-network-input")
    if record.get("unseeded_randomness"):
        raise_error("PKG7004", "reproducible build used unseeded randomness", record, source, "random-seed")
    if record.get("host_path_leak"):
        raise_error("PKG7005", "artifact leaks host path", record, source, "host-path-normalization")
    if record.get("generated_source_gap"):
        raise_error("PKG7006", "generated source lacks generator input hash", record, source, "generated-input-hash")
    if record.get("rebuild_hash_mismatch"):
        raise_error("PKG7007", "rebuild verification hash mismatch", record, source, "rebuild-hash")
    if record.get("invalid_release_claim"):
        raise_error("PKG7008", "non-reproducible artifact satisfies release gate", record, source, "reproducible-release-gate")


def validate_package_safety(record: dict[str, Any], source: str) -> None:
    require_fields(
        record,
        [
            "package_id",
            "unsafe_islands",
            "safe_wrappers",
            "ffi_boundaries",
            "privileged_effects",
            "capabilities",
            "taint_sinks",
            "proof_claims",
            "safety_tests",
            "review_state",
            "vulnerability_state",
            "schema_validated",
        ],
        "PKG8001",
        source,
    )
    if record.get("unsafe_without_audit"):
        raise_error("PKG8001", "unsafe forms lack audit metadata", record, source, "unsafe-audit-metadata")
    if record.get("safe_wrapper_gap"):
        raise_error("PKG8002", "safe API claim lacks wrapper evidence", record, source, "safe-wrapper-evidence")
    if record.get("ffi_assumption_omission"):
        raise_error("PKG8003", "FFI package omits ABI, ownership, lifetime, or error assumptions", record, source, "ffi-assumptions")
    if record.get("hidden_privileged_effect"):
        raise_error("PKG8004", "privileged effect lacks capability summary", record, source, "privileged-effect-summary")
    if record.get("missing_taint_sink"):
        raise_error("PKG8005", "taint sink is absent from safety metadata", record, source, "taint-sink")
    if record.get("revoked_proof_claim"):
        raise_error("PKG8006", "proof certificate has been revoked", record, source, "proof-revocation")
    if record.get("safety_diff_requires_review"):
        raise_error("PKG8007", "package update changed safety metadata without review", record, source, "safety-diff-review")
    if record.get("quarantined_dependency"):
        raise_error("PKG8008", "release graph includes quarantined dependency", record, source, "quarantine-policy")


def validate_registry_record(record: dict[str, Any], source: str) -> None:
    require_fields(
        record,
        [
            "registry_id",
            "visibility",
            "access_policy",
            "index_signature",
            "latent_package_states",
            "publish_record",
            "yank_or_revocation_record",
            "mirror_attestation",
            "registry_source_lock_entries",
        ],
        "PKG9001",
        source,
    )
    if record.get("private_access_denied"):
        raise_error("PKG9001", "private registry access lacks grant", record, source, "registry-read-grant")
    if record.get("private_metadata_leak"):
        raise_error("PKG9002", "public resolution leaked private package metadata", record, source, "private-metadata-redaction")
    if record.get("registry_signature_failure"):
        raise_error("PKG9003", "registry index signature failed verification", record, source, "registry-signature")
    if record.get("latent_resolution_denied"):
        raise_error("PKG9004", "latent package satisfied ordinary dependency constraint", record, source, "latent-package-grant")
    if record.get("generated_package_missing_provenance"):
        raise_error("PKG9005", "generated package candidate lacks generator provenance", record, source, "generated-package-provenance")
    if record.get("publish_without_review"):
        raise_error("PKG9006", "latent package was published without review transition", record, source, "latent-review-transition")
    if record.get("mirror_verification_failure"):
        raise_error("PKG9007", "mirror lacks signature preservation or attestation", record, source, "mirror-attestation")
    if record.get("lockfile_registry_omission"):
        raise_error("PKG9008", "lockfile dependency lacks registry source", record, source, "lockfile-registry-source")


def validate_provenance_record(record: dict[str, Any], source: str) -> None:
    require_fields(
        record,
        [
            "artifact_id",
            "content_hash",
            "source_graph_hash",
            "project_manifest_hash",
            "lockfile_hash",
            "compiler_id",
            "builder_id",
            "build_recipe_hash",
            "dependency_graph_hash",
            "generated_source_ledger",
            "binary_blob_ledger",
            "evidence",
            "signing_links",
            "sbom_link",
            "revocation_status",
            "schema_version",
        ],
        "PKG10001",
        source,
    )
    if record.get("missing_provenance"):
        raise_error("PKG10001", "release artifact lacks provenance", record, source, "provenance-record")
    if record.get("unverified_dependency_provenance"):
        raise_error("PKG10002", "dependency provenance summary is missing or unverifiable", record, source, "dependency-provenance")
    if record.get("unknown_builder"):
        raise_error("PKG10003", "builder identity is unknown under trusted-builder policy", record, source, "trusted-builder")
    if record.get("untracked_generated_source"):
        raise_error("PKG10004", "generated source is untracked", record, source, "generated-source-ledger")
    if record.get("undeclared_binary_blob"):
        raise_error("PKG10005", "binary blob lacks source, hash, or policy", record, source, "binary-blob-policy")
    if record.get("revoked_input"):
        raise_error("PKG10006", "supply-chain input is revoked", record, source, "revocation-check")
    if record.get("artifact_mismatch"):
        raise_error("PKG10007", "provenance record is not linked to artifact manifest", record, source, "provenance-artifact-link")
    if record.get("unknown_schema"):
        raise_error("PKG10008", "provenance schema is unknown for release verification", record, source, "provenance-schema")
    if record.get("keyless_policy_gap"):
        raise_error("PKG10009", "keyless signing provenance is incomplete or policy-incompatible", record, source, "keyless-provenance")
    if record.get("transparency_log_gap"):
        raise_error("PKG10010", "transparency log evidence is missing or unverifiable", record, source, "transparency-log")


def validate_target_matrix(record: dict[str, Any], source: str) -> None:
    require_fields(
        record,
        [
            "package_id",
            "entries",
            "per_target_dependency_graph",
            "per_target_capabilities",
            "per_target_artifacts",
            "per_target_conformance",
            "release_support_table",
        ],
        "PKG11001",
        source,
    )
    if record.get("unsupported_pair"):
        raise_error("PKG11001", "profile/target pair is unsupported", record, source, "profile-target-support")
    if record.get("implicit_host_target"):
        raise_error("PKG11002", "release build depends on implicit host target", record, source, "explicit-target")
    if record.get("missing_dependency_variant"):
        raise_error("PKG11003", "dependency variant is missing for required target", record, source, "target-dependency-variant")
    if record.get("capability_mismatch"):
        raise_error("PKG11004", "capability legality was assumed across targets", record, source, "per-target-capability")
    if record.get("missing_target_identity"):
        raise_error("PKG11005", "artifact manifest lacks target or ABI identity", record, source, "target-identity")
    if record.get("missing_conformance"):
        raise_error("PKG11006", "release claim lacks per-target conformance evidence", record, source, "per-target-conformance")
    if record.get("illegal_fallback"):
        raise_error("PKG11007", "build fell back to different target without policy", record, source, "fallback-policy")
    if record.get("project_matrix_contradiction"):
        raise_error("PKG11008", "target matrix contradicts project profiles", record, source, "project-matrix-consistency")
    for entry in record.get("entries", []):
        require_fields(entry, ["profile", "backend", "target", "runtime", "support", "artifact_kinds"], "PKG11001", source)


def validate_release_verification(record: dict[str, Any], source: str) -> None:
    require_fields(
        record,
        [
            "artifact_id",
            "signature",
            "payload",
            "sbom",
            "attestation",
            "keyless_identity",
            "transparency_log",
            "root_metadata",
            "verification_report",
            "policy_decision",
        ],
        "PKG12001",
        source,
    )
    if record.get("missing_signature"):
        raise_error("PKG12001", "release artifact requires a signature", record, source, "signature")
    if record.get("noncanonical_payload"):
        raise_error("PKG12002", "signature payload is not canonical", record, source, "canonical-signature-payload")
    if record.get("content_hash_mismatch"):
        raise_error("PKG12003", "signed content hash does not match artifact", record, source, "signed-content-hash")
    if record.get("sbom_dependency_omission"):
        raise_error("PKG12004", "SBOM omits transitive dependency", record, source, "sbom-transitive-dependency")
    if record.get("sbom_safety_capability_omission"):
        raise_error("PKG12005", "SBOM omits safety, capability, generated-source, or binary-blob summary", record, source, "sbom-safety-capability")
    if record.get("revoked_signing_material"):
        raise_error("PKG12006", "signing material is revoked", record, source, "signing-revocation")
    if record.get("unverified_consumer_use"):
        raise_error("PKG12007", "consumer used artifact before required verification", record, source, "consumer-verification")
    if record.get("incomplete_verification_report"):
        raise_error("PKG12008", "verification report omits failed checks", record, source, "verification-report")
    if record.get("missing_attestation"):
        raise_error("PKG12009", "required provenance attestation is missing", record, source, "provenance-attestation")
    if record.get("attestation_artifact_mismatch"):
        raise_error("PKG12010", "attestation is not bound to artifact subject hash", record, source, "attestation-subject")
    if record.get("untrusted_builder"):
        raise_error("PKG12011", "builder identity is unknown or untrusted", record, source, "builder-identity")
    if record.get("incomplete_source_material"):
        raise_error("PKG12012", "attestation omits required source material", record, source, "source-material")
    if record.get("unproved_hermetic_claim"):
        raise_error("PKG12013", "isolation, hermetic, or reproducible claim lacks evidence", record, source, "claim-evidence")
    if record.get("timestamp_gap"):
        raise_error("PKG12014", "timestamp or transparency log evidence is missing or unverifiable", record, source, "timestamp-evidence")
    if record.get("stale_attestation"):
        raise_error("PKG12015", "attestation is stale", record, source, "attestation-freshness")
    if record.get("policy_level_failure"):
        raise_error("PKG12016", "attestation policy level or verification track failed", record, source, "attestation-policy-level")
    if record.get("keyless_identity_gap"):
        raise_error("PKG12017", "keyless signing identity evidence is missing or invalid", record, source, "keyless-identity")
    if record.get("oidc_identity_mismatch"):
        raise_error("PKG12018", "OIDC issuer, subject, audience, claim, or certificate identity mismatch", record, source, "oidc-identity")
    if record.get("root_metadata_gap"):
        raise_error("PKG12019", "root metadata is missing, stale, or untrusted", record, source, "root-metadata")
    if record.get("transparency_log_failure"):
        raise_error("PKG12020", "transparency log inclusion, timestamp, checkpoint, or consistency evidence failed policy", record, source, "transparency-log-policy")


def require_dict(manifest: dict[str, Any], key: str, code: str, source: str) -> dict[str, Any]:
    value = manifest.get(key)
    if not isinstance(value, dict):
        raise_error(code, f"missing component {key}", {}, source, key)
    return value


def require_fields(record: dict[str, Any], fields: list[str], code: str, source: str) -> None:
    missing = [field for field in fields if field not in record]
    if missing:
        raise_error(code, "record is missing required fields", record, source, ",".join(missing))


def require_non_empty(record: dict[str, Any], field: str, code: str, source: str) -> None:
    if not record.get(field):
        raise_error(code, f"record field {field} must not be empty", record, source, field)


def raise_error(code: str, message: str, record: dict[str, Any], source: str, missing_fact: str) -> None:
    raise PackageArtifactError(
        code,
        message,
        record=record,
        source=source,
        missing_fact=missing_fact,
        remediation="Update the Phase 12 package, build, artifact, capability, provenance, or signing evidence.",
    )


def load_manifest_file(path: Path) -> dict[str, Any]:
    raw = json.loads(path.read_text(encoding="utf-8"))
    if raw.get("kind") != "package-artifacts-fixture-patch":
        return raw
    base_value = raw.get("base")
    if not base_value:
        raise_error("PKG1001", "fixture patch lacks base manifest", {}, str(path), "base-manifest")
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
