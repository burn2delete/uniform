"""SAFE6 unsafe island audit and policy validation."""

from __future__ import annotations

import hashlib
import json
from pathlib import Path
from typing import Any


REVIEW_GATED_POLICIES = {":local-review", ":domain-review", ":certificate-required"}
REQUIRED_ISLAND_FIELDS = {
    "id",
    "operation",
    "source_span",
    "package",
    "namespace",
    "active_profile",
    "target",
    "reason",
    "owner",
    "effects",
    "capabilities",
    "invariants",
    "preconditions",
    "postconditions",
    "evidence",
    "safe_boundary",
    "review",
    "policy",
    "source_version",
}


class UnsafeAuditError(Exception):
    def __init__(
        self,
        code: str,
        message: str,
        *,
        island: dict[str, Any] | None = None,
        source: str,
        missing_evidence: str,
        remediation: str,
        details: dict[str, Any] | None = None,
    ) -> None:
        super().__init__(message)
        self.code = code
        self.message = message
        self.island = island or {}
        self.source = source
        self.missing_evidence = missing_evidence
        self.remediation = remediation
        self.details = details or {}

    def to_diagnostic(self) -> dict[str, Any]:
        diagnostic = {
            "id": self.code,
            "message": self.message,
            "unsafe_island_id": self.island.get("id"),
            "span": self.island.get("source_span", {"source": self.source}),
            "generated_origin": self.island.get("generated_origin", []),
            "owner": self.island.get("owner"),
            "policy": self.island.get("policy"),
            "active_profile": self.island.get("active_profile"),
            "operation_family": self.island.get("operation"),
            "effects": self.island.get("effects", []),
            "capabilities": self.island.get("capabilities", []),
            "missing_evidence": self.missing_evidence,
            "remediation": self.remediation,
            "analyzer_stage": "unsafe-audit",
        }
        diagnostic.update(self.details)
        return diagnostic


def validate_unsafe_audit_file(path: Path) -> dict[str, Any]:
    return validate_unsafe_audit_manifest(load_manifest_file(path), str(path))


def unsafe_audit_diagnostic(path: Path) -> dict[str, Any] | None:
    try:
        validate_unsafe_audit_file(path)
    except UnsafeAuditError as exc:
        return exc.to_diagnostic()
    return None


def validate_unsafe_audit_manifest(manifest: dict[str, Any], source: str) -> dict[str, Any]:
    if manifest.get("kind") != "unsafe-audit-input":
        raise UnsafeAuditError(
            "SAFE6-MISSING-METADATA",
            "unsafe audit input has the wrong artifact kind",
            source=source,
            missing_evidence="unsafe-audit-input",
            remediation="Feed SAFE6 an unsafe-audit-input artifact.",
        )
    wrappers = {wrapper["name"]: wrapper for wrapper in manifest.get("safe_wrappers", [])}
    declared_capabilities = set(manifest.get("declared_capabilities", []))

    for island in manifest.get("islands", []):
        validate_island(island, manifest, wrappers, declared_capabilities, source)
    validate_dependencies(manifest, source)

    return {
        "kind": "unsafe-audit-artifact",
        "document": "SAFE6",
        "package": manifest.get("package"),
        "module": manifest.get("module"),
        "profile": manifest.get("profile"),
        "target": manifest.get("target"),
        "input_hash": artifact_hash(manifest),
        "unsafe_island_records": [island_record(island) for island in manifest.get("islands", [])],
        "safe_wrapper_records": manifest.get("safe_wrappers", []),
        "unsafe_operation_inventory": operation_inventory(manifest.get("islands", [])),
        "review_status_records": [review_record(island) for island in manifest.get("islands", [])],
        "invariant_and_proof_links": invariant_links(manifest.get("islands", [])),
        "generated_unsafe_provenance_records": [
            generated_record(island) for island in manifest.get("islands", []) if island.get("generated")
        ],
        "policy_decision_records": policy_records(manifest),
        "unsafe_dependency_summaries": manifest.get("dependency_summaries", []),
        "release_audit_report": {
            "unsafe_island_count": len(manifest.get("islands", [])),
            "safe_wrapper_count": len(manifest.get("safe_wrappers", [])),
            "policy": manifest.get("policy", {}),
            "status": ":accepted",
        },
        "diagnostics": [],
    }


def validate_island(
    island: dict[str, Any],
    manifest: dict[str, Any],
    wrappers: dict[str, dict[str, Any]],
    declared_capabilities: set[str],
    source: str,
) -> None:
    if manifest.get("policy", {}).get("unsafe") == ":forbidden" or island.get("policy") == ":forbidden":
        raise UnsafeAuditError(
            "SAFE6-UNSAFE-FORBIDDEN",
            "package or profile policy forbids unsafe code",
            island=island,
            source=source,
            missing_evidence="policy-allowance",
            remediation="Reject the unsafe island or compile under a reviewed unsafe policy.",
        )

    if not island.get("owner"):
        raise UnsafeAuditError(
            "SAFE6-MISSING-OWNER",
            "unsafe island has no accountable owner",
            island=island,
            source=source,
            missing_evidence="owner",
            remediation="Record the package, domain, or person accountable for the unsafe island.",
        )
    if not island.get("invariants"):
        raise UnsafeAuditError(
            "SAFE6-MISSING-INVARIANT",
            "unsafe island states no safety invariant",
            island=island,
            source=source,
            missing_evidence="invariant",
            remediation="State the invariant that keeps safe callers from observing invalid behavior.",
        )
    if not island.get("safe_boundary"):
        raise UnsafeAuditError(
            "SAFE6-MISSING-BOUNDARY",
            "unsafe island has no safe wrapper boundary",
            island=island,
            source=source,
            missing_evidence="safe-boundary",
            remediation="Expose unsafe internals only through a safe wrapper or explicit internal boundary.",
        )

    missing = sorted(REQUIRED_ISLAND_FIELDS - set(island))
    if missing:
        raise UnsafeAuditError(
            "SAFE6-MISSING-METADATA",
            f"unsafe island lacks required metadata: {missing}",
            island=island,
            source=source,
            missing_evidence=",".join(missing),
            remediation="Emit all SAFE6 island metadata before accepting unsafe code.",
        )

    wrapper = wrappers.get(island["safe_boundary"])
    if not wrapper or island["id"] not in wrapper.get("unsafe_islands", []) or wrapper.get("exposes_raw_authority"):
        raise UnsafeAuditError(
            "SAFE6-MISSING-BOUNDARY",
            "safe wrapper does not connect to the unsafe island or exposes raw authority",
            island=island,
            source=source,
            missing_evidence="verified-safe-wrapper",
            remediation="Connect the public API wrapper to the island and hide raw capabilities from callers.",
        )
    if island.get("generated") and missing_generated_provenance(island):
        raise UnsafeAuditError(
            "SAFE6-GENERATED-UNSAFE",
            "generated unsafe code lacks generator provenance",
            island=island,
            source=source,
            missing_evidence="generated-unsafe-provenance",
            remediation="Record generator identity, source form, provenance chain, invariants, and safe wrapper metadata.",
        )
    undeclared = sorted(set(island.get("capabilities", [])) - declared_capabilities)
    if undeclared:
        raise UnsafeAuditError(
            "SAFE6-CAPABILITY",
            "unsafe island uses undeclared authority",
            island=island,
            source=source,
            missing_evidence=",".join(undeclared),
            remediation="Declare and grant the capability through package, provider, build, or runtime policy.",
        )
    review = island.get("review", {})
    if island.get("policy") in REVIEW_GATED_POLICIES and (
        review.get("state") != ":approved" or review.get("expired") is True
    ):
        raise UnsafeAuditError(
            "SAFE6-REVIEW-REQUIRED",
            "unsafe island review is missing, pending, or expired",
            island=island,
            source=source,
            missing_evidence="approved-review",
            remediation="Attach an approved review tied to source version and unsafe island identity.",
        )
    if island.get("policy") == ":certificate-required" and not island.get("certificate"):
        raise UnsafeAuditError(
            "SAFE6-CERTIFICATE",
            "unsafe island requires a safety certificate",
            island=island,
            source=source,
            missing_evidence="safety-certificate",
            remediation="Attach a checkable certificate or reject the unsafe island under this policy.",
        )


def validate_dependencies(manifest: dict[str, Any], source: str) -> None:
    for dependency in manifest.get("dependency_summaries", []):
        if dependency.get("unsafe_island_count", 0) > 0 and not (
            dependency.get("certificates") or dependency.get("safe_wrappers_exported")
        ):
            island = {
                "id": dependency.get("id"),
                "operation": "dependency",
                "source_span": dependency.get("source_span", {"source": source}),
                "active_profile": manifest.get("profile"),
                "policy": manifest.get("policy", {}).get("unsafe"),
                "effects": dependency.get("effects", []),
                "capabilities": dependency.get("capabilities", []),
            }
            raise UnsafeAuditError(
                "SAFE6-DEPENDENCY",
                "dependency unsafe posture violates caller policy",
                island=island,
                source=source,
                missing_evidence="dependency-safe-wrapper-or-certificate",
                remediation="Require dependency safe wrappers, certificates, or reject the dependency.",
            )


def missing_generated_provenance(island: dict[str, Any]) -> bool:
    return not (island.get("generated_origin") and island.get("generator") and island.get("source_form"))


def island_record(island: dict[str, Any]) -> dict[str, Any]:
    return {
        "unsafe_island_id": island["id"],
        "operation": island["operation"],
        "source_span": island["source_span"],
        "generated_origin": island.get("generated_origin", []),
        "package": island["package"],
        "namespace": island["namespace"],
        "active_profile": island["active_profile"],
        "target": island["target"],
        "reason": island["reason"],
        "owner": island["owner"],
        "effects": island["effects"],
        "capabilities": island["capabilities"],
        "invariants": island["invariants"],
        "preconditions": island["preconditions"],
        "postconditions": island["postconditions"],
        "evidence": island["evidence"],
        "safe_boundary": island["safe_boundary"],
        "review": island["review"],
        "policy": island["policy"],
        "source_version": island["source_version"],
        "dependency_origin": island.get("dependency_origin"),
    }


def operation_inventory(islands: list[dict[str, Any]]) -> list[dict[str, Any]]:
    return [
        {
            "operation": island["operation"],
            "unsafe_island_id": island["id"],
            "active_profile": island["active_profile"],
            "effects": island["effects"],
            "capabilities": island["capabilities"],
        }
        for island in islands
    ]


def review_record(island: dict[str, Any]) -> dict[str, Any]:
    review = island["review"]
    return {
        "unsafe_island_id": island["id"],
        "state": review.get("state"),
        "policy": island["policy"],
        "reviewer": review.get("reviewer"),
        "review_date": review.get("review_date"),
        "source_version": island["source_version"],
        "expires": review.get("expires"),
    }


def invariant_links(islands: list[dict[str, Any]]) -> list[dict[str, Any]]:
    return [
        {
            "unsafe_island_id": island["id"],
            "invariants": island["invariants"],
            "evidence": island["evidence"],
            "certificate": island.get("certificate"),
        }
        for island in islands
    ]


def generated_record(island: dict[str, Any]) -> dict[str, Any]:
    return {
        "unsafe_island_id": island["id"],
        "generator": island["generator"],
        "source_form": island["source_form"],
        "generated_origin": island["generated_origin"],
        "safe_boundary": island["safe_boundary"],
    }


def policy_records(manifest: dict[str, Any]) -> list[dict[str, Any]]:
    return [
        {
            "scope": "package",
            "package": manifest.get("package"),
            "policy": manifest.get("policy", {}),
            "decision": ":accepted",
        }
    ]


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
