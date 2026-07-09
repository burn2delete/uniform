"""L18 alternative memory-provider contract validation."""

from __future__ import annotations

import copy
import json
from pathlib import Path
from typing import Any


CONSTRAINED_PROFILES = {":kernel", ":firmware", ":hardware"}
SAFE_OUTCOMES = {":proven-safe", ":runtime-checked", ":rejected", ":unsafe-island"}


class AlternativeMemoryError(Exception):
    def __init__(
        self,
        code: str,
        message: str,
        span: dict[str, Any],
        remediation: str,
        *,
        provider_id: str | None = None,
        active_profile: str | None = None,
        generated_origin_chain: list[dict[str, Any]] | None = None,
        memory_family: str | None = None,
        lifetime_region: str | None = None,
        capability_scope: dict[str, Any] | None = None,
        required_proof_or_check: str | None = None,
    ) -> None:
        super().__init__(message)
        self.code = code
        self.message = message
        self.span = span
        self.remediation = remediation
        self.provider_id = provider_id
        self.active_profile = active_profile
        self.generated_origin_chain = generated_origin_chain or []
        self.memory_family = memory_family
        self.lifetime_region = lifetime_region
        self.capability_scope = capability_scope or {}
        self.required_proof_or_check = required_proof_or_check

    def to_diagnostic(self) -> dict[str, Any]:
        return {
            "id": self.code,
            "message": self.message,
            "provider_id": self.provider_id,
            "active_profile": self.active_profile,
            "span": self.span,
            "generated_origin_chain": self.generated_origin_chain,
            "memory_family": self.memory_family,
            "lifetime_or_region": self.lifetime_region,
            "capability_scope": self.capability_scope,
            "required_proof_or_check": self.required_proof_or_check,
            "remediation": self.remediation,
            "analyzer_stage": "alternative-memory-provider",
        }


def validate_alternative_memory_manifest_file(path: Path) -> dict[str, Any]:
    manifest = load_manifest_file(path)
    return validate_alternative_memory_manifest(manifest, str(path))


def alternative_memory_manifest_diagnostic(path: Path) -> dict[str, Any] | None:
    try:
        validate_alternative_memory_manifest_file(path)
    except AlternativeMemoryError as exc:
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


def validate_alternative_memory_manifest(manifest: dict[str, Any], source: str) -> dict[str, Any]:
    if manifest.get("kind") != "alternative-memory-provider-manifest":
        raise AlternativeMemoryError(
            "L18-PROVIDER",
            "alternative memory manifest has the wrong kind",
            span(source, ""),
            "Use kind alternative-memory-provider-manifest.",
        )
    provider = manifest.get("provider", {})
    provider_id = provider.get("id")
    active_profile = manifest.get("active_profile")
    origin_chain = manifest.get("generated_origin_chain", [])
    family = first(provider.get("families", []))

    if provider.get("kind") != ":memory-system" or active_profile not in provider.get("profiles", []):
        raise AlternativeMemoryError(
            "L18-PROVIDER",
            "no memory provider satisfies the active profile requirement",
            span(source, "/provider"),
            "Select a memory-system provider that supports the profile and target.",
            provider_id=provider_id,
            active_profile=active_profile,
            generated_origin_chain=origin_chain,
            memory_family=family,
        )

    allocation = manifest.get("allocation_strategy", {})
    if active_profile in CONSTRAINED_PROFILES and allocation.get("hidden"):
        raise AlternativeMemoryError(
            "L18-HIDDEN-ALLOC",
            "allocation occurs where the profile forbids hidden allocation",
            span(source, "/allocation_strategy"),
            "Make allocation explicit, bounded, or reject the constrained profile.",
            provider_id=provider_id,
            active_profile=active_profile,
            generated_origin_chain=origin_chain,
            memory_family=allocation.get("family", family),
            required_proof_or_check="profile allocation policy",
        )

    for index, fact in enumerate(manifest.get("lifetime_facts", [])):
        if not fact.get("valid"):
            raise AlternativeMemoryError(
                "L18-LIFETIME",
                "reference or borrow can outlive its storage",
                span(source, f"/lifetime_facts/{index}"),
                "Shorten the borrow, extend the storage lifetime, insert a runtime check, or reject the program.",
                provider_id=provider_id,
                active_profile=active_profile,
                generated_origin_chain=fact.get("generated_origin_chain", origin_chain),
                memory_family=fact.get("family", family),
                lifetime_region=fact.get("region"),
                required_proof_or_check=fact.get("proof_or_check"),
            )

    for index, fact in enumerate(manifest.get("escape_facts", [])):
        if fact.get("escapes"):
            raise AlternativeMemoryError(
                "L18-ESCAPE",
                "value escapes its region, arena, stack, or device scope",
                span(source, f"/escape_facts/{index}"),
                "Keep the value inside the allocation scope or transfer ownership through a declared boundary.",
                provider_id=provider_id,
                active_profile=active_profile,
                generated_origin_chain=fact.get("generated_origin_chain", origin_chain),
                memory_family=fact.get("family", family),
                lifetime_region=fact.get("region"),
                required_proof_or_check=fact.get("proof_or_check"),
            )

    for index, fact in enumerate(manifest.get("alias_facts", [])):
        if fact.get("violates"):
            raise AlternativeMemoryError(
                "L18-ALIAS",
                "aliasing violates ownership or mutation rules",
                span(source, f"/alias_facts/{index}"),
                "Use unique ownership, immutable sharing, atomics, or a checked borrow split.",
                provider_id=provider_id,
                active_profile=active_profile,
                generated_origin_chain=fact.get("generated_origin_chain", origin_chain),
                memory_family=fact.get("family", family),
                lifetime_region=fact.get("region"),
                required_proof_or_check=fact.get("proof_or_check"),
            )

    for index, fact in enumerate(manifest.get("initialization_facts", [])):
        if not fact.get("initialized"):
            raise AlternativeMemoryError(
                "L18-UNINIT",
                "value may be read before initialization",
                span(source, f"/initialization_facts/{index}"),
                "Initialize the full range or add a checked initialization guard before reading.",
                provider_id=provider_id,
                active_profile=active_profile,
                generated_origin_chain=fact.get("generated_origin_chain", origin_chain),
                memory_family=fact.get("family", family),
                lifetime_region=fact.get("region"),
                required_proof_or_check=fact.get("proof_or_check"),
            )

    for index, evidence in enumerate(manifest.get("release_evidence", [])):
        if evidence.get("release_count", 0) > 1:
            raise AlternativeMemoryError(
                "L18-DOUBLE-RELEASE",
                "linear resource may be released twice",
                span(source, f"/release_evidence/{index}"),
                "Ensure exactly-once release through linear ownership or a runtime state check.",
                provider_id=provider_id,
                active_profile=active_profile,
                generated_origin_chain=evidence.get("generated_origin_chain", origin_chain),
                memory_family=evidence.get("family", family),
                lifetime_region=evidence.get("region"),
                required_proof_or_check=evidence.get("proof_or_check"),
            )
        if evidence.get("requires_release") and not evidence.get("released"):
            raise AlternativeMemoryError(
                "L18-LEAK",
                "resource requiring release was not released",
                span(source, f"/release_evidence/{index}"),
                "Release the resource, transfer it, or reject the program.",
                provider_id=provider_id,
                active_profile=active_profile,
                generated_origin_chain=evidence.get("generated_origin_chain", origin_chain),
                memory_family=evidence.get("family", family),
                lifetime_region=evidence.get("region"),
                required_proof_or_check=evidence.get("proof_or_check"),
            )

    for index, access in enumerate(manifest.get("bounds_checks", [])):
        if not access.get("in_bounds"):
            raise AlternativeMemoryError(
                "L18-BOUNDS",
                "memory access may exceed its range",
                span(source, f"/bounds_checks/{index}"),
                "Prove bounds, insert a runtime check, or reject the access.",
                provider_id=provider_id,
                active_profile=active_profile,
                generated_origin_chain=access.get("generated_origin_chain", origin_chain),
                memory_family=access.get("family", family),
                lifetime_region=access.get("region"),
                required_proof_or_check=access.get("proof_or_check"),
            )

    for index, sync in enumerate(manifest.get("device_sync_records", [])):
        if not sync.get("synchronized"):
            raise AlternativeMemoryError(
                "L18-DEVICE-SYNC",
                "host/device synchronization is missing",
                span(source, f"/device_sync_records/{index}"),
                "Synchronize before crossing host/device ownership or visibility boundaries.",
                provider_id=provider_id,
                active_profile=active_profile,
                generated_origin_chain=sync.get("generated_origin_chain", origin_chain),
                memory_family=sync.get("family", ":alloc/device"),
                lifetime_region=sync.get("region"),
                capability_scope=sync.get("capability_scope", {}),
                required_proof_or_check=sync.get("proof_or_check"),
            )

    for index, mmio in enumerate(manifest.get("mmio_maps", [])):
        if not mmio.get("valid"):
            raise AlternativeMemoryError(
                "L18-MMIO",
                "MMIO address, width, volatility, or ordering is invalid",
                span(source, f"/mmio_maps/{index}"),
                "Validate device map, width, alignment, volatile semantics, and ordering.",
                provider_id=provider_id,
                active_profile=active_profile,
                generated_origin_chain=mmio.get("generated_origin_chain", origin_chain),
                memory_family=":memory/mmio",
                lifetime_region=mmio.get("device"),
                capability_scope=mmio.get("capability_scope", {}),
                required_proof_or_check=mmio.get("proof_or_check"),
            )

    for index, ffi in enumerate(manifest.get("ffi_allocator_records", [])):
        if ffi.get("allocator") != ffi.get("release_provider"):
            raise AlternativeMemoryError(
                "L18-FFI-ALLOCATOR",
                "allocation and release providers mismatch",
                span(source, f"/ffi_allocator_records/{index}"),
                "Release foreign memory through a compatible allocator identity.",
                provider_id=provider_id,
                active_profile=active_profile,
                generated_origin_chain=ffi.get("generated_origin_chain", origin_chain),
                memory_family=":alloc/foreign",
                lifetime_region=ffi.get("foreign_library"),
                required_proof_or_check=ffi.get("proof_or_check"),
            )

    for index, wrapper in enumerate(manifest.get("unsafe_boundary_audits", [])):
        if not wrapper.get("invariant") or not wrapper.get("evidence"):
            raise AlternativeMemoryError(
                "L18-UNSAFE-AUDIT",
                "safe wrapper lacks invariant evidence for unsafe internals",
                span(source, f"/unsafe_boundary_audits/{index}"),
                "State the invariant and attach proof, runtime checks, or audit artifacts.",
                provider_id=provider_id,
                active_profile=active_profile,
                generated_origin_chain=wrapper.get("generated_origin_chain", origin_chain),
                memory_family=wrapper.get("family", family),
                capability_scope=wrapper.get("capability_scope", {}),
                required_proof_or_check=wrapper.get("required_proof_or_check"),
            )

    outcomes = set(manifest.get("safe_outcomes", []))
    if not SAFE_OUTCOMES.issubset(outcomes):
        raise AlternativeMemoryError(
            "L18-PROVIDER",
            "memory provider does not expose all safe-code classification outcomes",
            span(source, "/safe_outcomes"),
            "Classify dangerous operations as proven-safe, runtime-checked, rejected, or unsafe-island.",
            provider_id=provider_id,
            active_profile=active_profile,
            generated_origin_chain=origin_chain,
            memory_family=family,
        )

    return {
        "kind": "alternative-memory-provider-artifact",
        "document": "L18",
        "provider_declaration": provider,
        "allocation_strategy_record": allocation,
        "lifetime_alias_ownership_region_escape_facts": {
            "lifetime": manifest.get("lifetime_facts", []),
            "escape": manifest.get("escape_facts", []),
            "alias": manifest.get("alias_facts", []),
            "initialization": manifest.get("initialization_facts", []),
        },
        "unsafe_boundary_audit_records": manifest.get("unsafe_boundary_audits", []),
        "layout_alignment_metadata": manifest.get("layout_alignment_metadata", []),
        "runtime_check_records": manifest.get("runtime_checks", []),
        "leak_resource_release_evidence": manifest.get("release_evidence", []),
        "device_mmio_ffi_maps": {
            "device_sync": manifest.get("device_sync_records", []),
            "mmio": manifest.get("mmio_maps", []),
            "ffi": manifest.get("ffi_allocator_records", []),
        },
        "provider_conformance_report": manifest.get("conformance", {}),
        "diagnostics": [],
    }


def first(values: list[Any]) -> Any:
    return values[0] if values else None


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
