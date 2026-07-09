"""L19 interoperability and migration boundary validation."""

from __future__ import annotations

import copy
import json
from pathlib import Path
from typing import Any


REQUIRED_BOUNDARY_FIELDS = {
    "id",
    "kind",
    "foreign_source",
    "abi_or_protocol",
    "type_mapping",
    "effects",
    "capabilities",
    "ownership",
    "errors",
    "memory_behavior",
    "threading",
    "safety",
    "profiles",
    "targets",
    "version",
    "provider",
}

CONSTRAINED_PROFILES = {":core", ":kernel", ":firmware", ":hardware"}


class InteropError(Exception):
    def __init__(
        self,
        code: str,
        message: str,
        span: dict[str, Any],
        remediation: str,
        *,
        boundary_id: str | None = None,
        foreign_source: str | None = None,
        active_profile: str | None = None,
        provider_id: str | None = None,
        type_mapping: dict[str, Any] | None = None,
        ownership_facts: dict[str, Any] | None = None,
        effects: list[str] | None = None,
        capabilities: list[str] | None = None,
        suggested_step: str | None = None,
    ) -> None:
        super().__init__(message)
        self.code = code
        self.message = message
        self.span = span
        self.remediation = remediation
        self.boundary_id = boundary_id
        self.foreign_source = foreign_source
        self.active_profile = active_profile
        self.provider_id = provider_id
        self.type_mapping = type_mapping or {}
        self.ownership_facts = ownership_facts or {}
        self.effects = effects or []
        self.capabilities = capabilities or []
        self.suggested_step = suggested_step

    def to_diagnostic(self) -> dict[str, Any]:
        return {
            "id": self.code,
            "message": self.message,
            "boundary_id": self.boundary_id,
            "foreign_source": self.foreign_source,
            "active_profile": self.active_profile,
            "span": self.span,
            "provider_id": self.provider_id,
            "type_mapping": self.type_mapping,
            "ownership_facts": self.ownership_facts,
            "effects": self.effects,
            "capabilities": self.capabilities,
            "suggested_safe_wrapper_or_migration_step": self.suggested_step,
            "remediation": self.remediation,
            "analyzer_stage": "interop-migration",
        }


def validate_interop_manifest_file(path: Path) -> dict[str, Any]:
    manifest = load_manifest_file(path)
    return validate_interop_manifest(manifest, str(path))


def interop_manifest_diagnostic(path: Path) -> dict[str, Any] | None:
    try:
        validate_interop_manifest_file(path)
    except InteropError as exc:
        return exc.to_diagnostic()
    return None


def load_manifest_file(path: Path) -> dict[str, Any]:
    data = json.loads(path.read_text(encoding="utf-8"))
    if "extends" not in data:
        return data
    base = load_manifest_file(path.parent / data["extends"])
    result = copy.deepcopy(base)
    for pointer in data.get("delete", []):
        delete_pointer(result, pointer)
    for pointer, value in data.get("set", {}).items():
        set_pointer(result, pointer, value)
    return result


def validate_interop_manifest(manifest: dict[str, Any], source: str) -> dict[str, Any]:
    if manifest.get("kind") != "interop-migration-manifest":
        raise InteropError(
            "L19-BOUNDARY-INCOMPLETE",
            "interop manifest has the wrong kind",
            span(source, ""),
            "Use kind interop-migration-manifest.",
        )
    boundaries = {boundary["id"]: boundary for boundary in manifest.get("boundaries", [])}
    for index, boundary in enumerate(manifest.get("boundaries", [])):
        validate_boundary(boundary, source, index)

    safe_wrappers = {wrapper["boundary_id"]: wrapper for wrapper in manifest.get("safe_wrappers", [])}
    for boundary in manifest.get("boundaries", []):
        if boundary.get("safety") == ":wrapped-safe":
            wrapper = safe_wrappers.get(boundary["id"])
            if not wrapper or not wrapper.get("invariants") or not wrapper.get("evidence"):
                raise boundary_error(
                    "L19-SAFE-WRAPPER",
                    "safe wrapper invariants are unproven",
                    "State wrapper invariants and attach proof, checks, or audit evidence.",
                    boundary,
                    source,
                    f"/safe_wrappers/{boundary['id']}",
                    suggested_step="keep the boundary unsafe or add wrapper evidence",
                )

    for index, binding in enumerate(manifest.get("generated_bindings", [])):
        if binding.get("source_digest") != binding.get("current_schema_digest"):
            boundary = boundaries.get(binding.get("boundary_id"), {})
            raise boundary_error(
                "L19-SCHEMA-DRIFT",
                "generated bindings no longer match source schemas",
                "Regenerate bindings from the current schema and record compatibility checks.",
                boundary,
                source,
                f"/generated_bindings/{index}",
                suggested_step="rerun schema generation under L12 build-effect policy",
            )

    for index, report in enumerate(manifest.get("parity_reports", [])):
        if report.get("status") != ":passed":
            boundary = boundaries.get(report.get("boundary_id"), {})
            raise boundary_error(
                "L19-MIGRATION-PARITY",
                "migration parity tests failed against incumbent behavior",
                "Fix the shim or record intentionally rejected behavior.",
                boundary,
                source,
                f"/parity_reports/{index}",
                suggested_step="update migration shim behavior or parity expectations",
            )

    for index, check in enumerate(manifest.get("call_checks", [])):
        boundary = boundaries.get(check.get("boundary_id"))
        if not boundary:
            continue
        active_profile = check.get("active_profile")
        if boundary.get("kind") == ":managed-host" and active_profile in CONSTRAINED_PROFILES and boundary.get("host_bridge", {}).get("leaks_host_behavior"):
            raise boundary_error(
                "L19-HOST-LEAK",
                "hosted behavior leaks into a portable or constrained profile",
                "Reject the host bridge or model the host runtime boundary explicitly.",
                boundary,
                source,
                f"/call_checks/{index}",
                active_profile=active_profile,
                suggested_step="replace the host bridge with a profile-supported boundary",
            )
        if active_profile not in boundary.get("profiles", []):
            raise boundary_error(
                "L19-PROFILE",
                "boundary is unsupported by the active profile",
                "Gate the boundary or select a profile-supported interop provider.",
                boundary,
                source,
                f"/call_checks/{index}",
                active_profile=active_profile,
                suggested_step="use a schema boundary that lowers to portable typed core",
            )
        missing_effects = sorted(set(boundary.get("effects", [])) - set(check.get("caller_effects", [])))
        if missing_effects:
            raise boundary_error(
                "L19-EFFECT",
                "foreign effects are missing from the caller",
                "Declare the foreign effects at the caller boundary.",
                boundary,
                source,
                f"/call_checks/{index}",
                active_profile=active_profile,
                effects=missing_effects,
                suggested_step="add the required effect declaration",
            )
        missing_capabilities = sorted(set(boundary.get("capabilities", [])) - set(check.get("capability_context", [])))
        if missing_capabilities:
            raise boundary_error(
                "L19-CAPABILITY",
                "boundary lacks required authority",
                "Pass a capability value or configure a provider grant.",
                boundary,
                source,
                f"/call_checks/{index}",
                active_profile=active_profile,
                capabilities=missing_capabilities,
                suggested_step="grant the boundary capability",
            )

    return {
        "kind": "interop-migration-artifact",
        "document": "L19",
        "foreign_binding_declaration_records": manifest.get("boundaries", []),
        "abi_protocol_schema_metadata": [
            {
                "boundary_id": boundary["id"],
                "kind": boundary.get("kind"),
                "abi_or_protocol": boundary.get("abi_or_protocol"),
                "version": boundary.get("version"),
            }
            for boundary in manifest.get("boundaries", [])
        ],
        "generated_binding_source_and_provenance": manifest.get("generated_bindings", []),
        "safe_wrapper_audit_records": manifest.get("safe_wrappers", []),
        "ownership_lifetime_maps": [boundary.get("ownership", {}) for boundary in manifest.get("boundaries", [])],
        "error_translation_maps": [boundary.get("errors", {}) for boundary in manifest.get("boundaries", [])],
        "capability_effect_records": [
            {"boundary_id": boundary["id"], "effects": boundary.get("effects", []), "capabilities": boundary.get("capabilities", [])}
            for boundary in manifest.get("boundaries", [])
        ],
        "migration_shim_records": manifest.get("migration_shims", []),
        "incumbent_parity_test_reports": manifest.get("parity_reports", []),
        "compatibility_deprecation_records": manifest.get("compatibility_records", []),
        "diagnostics": [],
    }


def validate_boundary(boundary: dict[str, Any], source: str, index: int) -> None:
    missing = sorted(REQUIRED_BOUNDARY_FIELDS - set(boundary))
    if missing:
        raise boundary_error(
            "L19-BOUNDARY-INCOMPLETE",
            f"foreign declaration omits required metadata: {missing}",
            "Declare ABI/protocol, types, ownership, effects, capabilities, errors, memory, threading, safety, profiles, provider, and version.",
            boundary,
            source,
            f"/boundaries/{index}",
            suggested_step="complete the foreign declaration before importing it",
        )
    if boundary.get("type_mapping", {}).get("status") != ":checked":
        raise boundary_error(
            "L19-TYPE-MAP",
            "type mapping is missing, lossy, or unchecked",
            "Use explicit conversions that return Result or attach checked mapping evidence.",
            boundary,
            source,
            f"/boundaries/{index}/type_mapping",
            suggested_step="add a checked directional type mapping",
        )
    if not boundary.get("ownership", {}).get("complete"):
        raise boundary_error(
            "L19-OWNERSHIP",
            "transfer, borrow, release, or allocator facts are missing",
            "Record ownership transfer, nullability, allocator identity, and release behavior.",
            boundary,
            source,
            f"/boundaries/{index}/ownership",
            suggested_step="add an ownership and lifetime map",
        )
    if not boundary.get("errors", {}).get("complete"):
        raise boundary_error(
            "L19-ERROR-MAP",
            "foreign failure is not translated",
            "Map exceptions, panics, error codes, rejected promises, signals, and exits to Gravity errors.",
            boundary,
            source,
            f"/boundaries/{index}/errors",
            suggested_step="add error translation for all foreign failure modes",
        )


def boundary_error(
    code: str,
    message: str,
    remediation: str,
    boundary: dict[str, Any],
    source: str,
    pointer: str,
    *,
    active_profile: str | None = None,
    effects: list[str] | None = None,
    capabilities: list[str] | None = None,
    suggested_step: str | None = None,
) -> InteropError:
    return InteropError(
        code,
        message,
        span(source, pointer),
        remediation,
        boundary_id=boundary.get("id"),
        foreign_source=boundary.get("foreign_source"),
        active_profile=active_profile,
        provider_id=boundary.get("provider"),
        type_mapping=boundary.get("type_mapping", {}),
        ownership_facts=boundary.get("ownership", {}),
        effects=effects if effects is not None else boundary.get("effects", []),
        capabilities=capabilities if capabilities is not None else boundary.get("capabilities", []),
        suggested_step=suggested_step,
    )


def span(source: str, pointer: str) -> dict[str, str]:
    return {"source": source, "json_pointer": pointer or "/"}


def get_pointer(root: Any, pointer: str) -> Any:
    node = root
    for part in pointer_parts(pointer):
        node = node[int(part)] if isinstance(node, list) else node[part]
    return node


def set_pointer(root: Any, pointer: str, value: Any) -> None:
    parent, key = parent_pointer(root, pointer)
    if isinstance(parent, list):
        parent[int(key)] = value
    else:
        parent[key] = value


def delete_pointer(root: Any, pointer: str) -> None:
    parent, key = parent_pointer(root, pointer)
    if isinstance(parent, list):
        del parent[int(key)]
    else:
        del parent[key]


def parent_pointer(root: Any, pointer: str) -> tuple[Any, str]:
    parts = pointer_parts(pointer)
    if not parts:
        raise ValueError("cannot mutate root pointer")
    node = root
    for part in parts[:-1]:
        node = node[int(part)] if isinstance(node, list) else node[part]
    return node, parts[-1]


def pointer_parts(pointer: str) -> list[str]:
    if pointer in {"", "/"}:
        return []
    return [part.replace("~1", "/").replace("~0", "~") for part in pointer.lstrip("/").split("/")]
