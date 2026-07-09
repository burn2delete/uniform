"""SAFE10 capability and SAFE14 supply-chain safety validation."""

from __future__ import annotations

import hashlib
import json
from pathlib import Path
from typing import Any


class CapabilitySupplyChainError(Exception):
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
            "record_id": self.record.get("id"),
            "package_id": self.record.get("package_id"),
            "version": self.record.get("version"),
            "requested_capability": self.record.get("requested_capability"),
            "provider_id": self.record.get("provider_id"),
            "grant_id": self.record.get("grant_id"),
            "denied_policy_layer": self.record.get("denied_policy_layer"),
            "dependency_path": self.record.get("dependency_path", []),
            "scope": self.record.get("scope"),
            "phase": self.record.get("phase"),
            "active_profile": self.record.get("active_profile"),
            "source_span": self.record.get("source_span", {"source": self.source}),
            "missing_fact": self.missing_fact,
            "remediation": self.remediation,
            "analyzer_stage": "capability-supply-chain",
        }


def validate_capability_supply_chain_file(path: Path) -> dict[str, Any]:
    return validate_capability_supply_chain_manifest(load_manifest_file(path), str(path))


def capability_supply_chain_diagnostic(path: Path) -> dict[str, Any] | None:
    try:
        validate_capability_supply_chain_file(path)
    except CapabilitySupplyChainError as exc:
        return exc.to_diagnostic()
    return None


def validate_capability_supply_chain_manifest(manifest: dict[str, Any], source: str) -> dict[str, Any]:
    if manifest.get("kind") != "capability-supply-chain-input":
        raise CapabilitySupplyChainError(
            "SAFE14-MANIFEST",
            "capability supply-chain input has the wrong artifact kind",
            source=source,
            missing_fact="capability-supply-chain-input",
            remediation="Feed P02-T05 a capability-supply-chain-input artifact.",
        )
    for record in manifest.get("capability_checks", []):
        validate_capability_record(record, source)
    for record in manifest.get("package_checks", []):
        validate_package_record(record, source)

    return {
        "kind": "capability-supply-chain-artifact",
        "documents": ["SAFE10", "SAFE14"],
        "package": manifest.get("package"),
        "module": manifest.get("module"),
        "profile": manifest.get("profile"),
        "target": manifest.get("target"),
        "input_hash": artifact_hash(manifest),
        "capability_requirement_records": collect(manifest, "capability_requirement_records"),
        "grant_intersection_records": collect(manifest, "grant_intersection_records"),
        "provider_selection_records": collect(manifest, "provider_selection_records"),
        "scope_check_records": collect(manifest, "scope_check_records"),
        "attenuation_revocation_records": collect(manifest, "attenuation_revocation_records"),
        "secret_redaction_records": collect(manifest, "secret_redaction_records"),
        "runtime_capability_check_records": collect(manifest, "runtime_capability_check_records"),
        "capability_usage_summary": collect(manifest, "capability_usage_summary"),
        "package_safety_manifests": collect(manifest, "package_safety_manifests"),
        "lockfile_dependency_graph_records": collect(manifest, "lockfile_dependency_graph_records"),
        "build_effect_summaries": collect(manifest, "build_effect_summaries"),
        "runtime_capability_summaries": collect(manifest, "runtime_capability_summaries"),
        "unsafe_island_summaries": collect(manifest, "unsafe_island_summaries"),
        "native_dependency_abi_records": collect(manifest, "native_dependency_abi_records"),
        "generated_artifact_provenance": collect(manifest, "generated_artifact_provenance"),
        "signature_attestation_records": collect(manifest, "signature_attestation_records"),
        "transitive_authority_diffs": collect(manifest, "transitive_authority_diffs"),
        "supply_chain_conformance_reports": collect(manifest, "supply_chain_conformance_reports"),
        "diagnostics": [],
    }


def validate_capability_record(record: dict[str, Any], source: str) -> None:
    checks = [
        ("available", False, "SAFE10-MISSING", "capability-grant", "Declare and grant the required capability."),
        ("policy_denied", True, "SAFE10-DENIED", "policy-allowance", "Change policy or remove the denied authority."),
        ("scope_exceeded", True, "SAFE10-SCOPE", "grant-scope", "Narrow the operation to the grant scope or request a broader approved grant."),
        ("provider_available", False, "SAFE10-PROVIDER", "provider", "Select a provider that satisfies the capability."),
        ("ambient_undeclared", True, "SAFE10-AMBIENT", "ambient-declaration", "Declare ambient authority in namespace, package, and deployment policy."),
        ("phase_mismatch", True, "SAFE10-PHASE", "phase-correct-grant", "Separate build and runtime authority."),
        ("secret_leak", True, "SAFE10-SECRET-LEAK", "secret-redaction", "Redact secrets and record only names or redaction policy in artifacts."),
        ("attenuation_expands", True, "SAFE10-ATTENUATION", "attenuation-subset", "Ensure derived capability scope is a subset of the parent."),
        ("revocation_unsupported", True, "SAFE10-REVOCATION", "revocation-support", "Use a profile with revocation or a static lifetime substitute."),
        ("runtime_failure_undeclared", True, "SAFE10-RUNTIME", "runtime-failure-behavior", "Declare runtime denial error or panic behavior."),
    ]
    for key, bad_value, code, fact, remediation in checks:
        if record.get(key) is bad_value:
            raise CapabilitySupplyChainError(
                code,
                f"capability check violates {code}",
                record=record,
                source=source,
                missing_fact=fact,
                remediation=remediation,
            )


def validate_package_record(record: dict[str, Any], source: str) -> None:
    checks = [
        ("manifest_complete", False, "SAFE14-MANIFEST", "package-safety-metadata", "Add runtime capabilities, build effects, unsafe, native, generated, and reproducibility metadata."),
        ("build_effects_granted", False, "SAFE14-BUILD-EFFECT", "build-effect-grant", "Declare and grant build authority with provider scope and replay records."),
        ("runtime_capabilities_approved", False, "SAFE14-RUNTIME-CAPABILITY", "runtime-capability-approval", "Approve dependency runtime authority in root package or deployment policy."),
        ("lockfile_pinned", False, "SAFE14-LOCKFILE", "pinned-lockfile", "Pin packages, providers, compiler, build grants, native deps, and generated artifact digests."),
        ("unsafe_summary_accepted", False, "SAFE14-UNSAFE-SUMMARY", "unsafe-summary", "Provide accepted unsafe summaries, safe wrappers, reviews, or certificates."),
        ("native_dep_complete", False, "SAFE14-NATIVE-DEP", "native-dependency-metadata", "Declare native source, digest, ABI, targets, link mode, license, and wrapper package."),
        ("generated_provenance_complete", False, "SAFE14-GENERATED", "generated-artifact-provenance", "Record generator id, source digests, build effects, grants, output digest, and checks."),
        ("signature_valid", False, "SAFE14-SIGNATURE", "signature-attestation", "Verify package digest, signature, manifest, and attestation before use."),
        ("authority_diff_approved", False, "SAFE14-AUTHORITY-DIFF", "authority-diff-approval", "Present and approve any transitive authority increase."),
        ("postinstall_declared", False, "SAFE14-POSTINSTALL", "postinstall-policy", "Deny hidden install-time execution or declare and grant it explicitly."),
    ]
    for key, bad_value, code, fact, remediation in checks:
        if record.get(key) is bad_value:
            raise CapabilitySupplyChainError(
                code,
                f"package safety check violates {code}",
                record=record,
                source=source,
                missing_fact=fact,
                remediation=remediation,
            )


def collect(manifest: dict[str, Any], key: str) -> list[dict[str, Any]]:
    records = []
    for section in ("capability_checks", "package_checks"):
        for item in manifest.get(section, []):
            for record in item.get(key, []):
                output = dict(record)
                output.setdefault("source_record", item.get("id"))
                records.append(output)
    return records


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
