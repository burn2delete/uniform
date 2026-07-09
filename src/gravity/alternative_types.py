"""L17 alternative type-system provider contract validation."""

from __future__ import annotations

import copy
import json
from pathlib import Path
from typing import Any


REQUIRED_TYPED_CORE = {
    "expression_types",
    "binding_types",
    "function_types",
    "capability_requirements",
    "panic_error_behavior",
    "ownership_lifetime_facts",
    "allocation_resource_facts",
    "cast_runtime_check_records",
    "proof_references",
    "profile_assumptions",
    "source_span_mapping",
}


class AlternativeTypeError(Exception):
    def __init__(
        self,
        code: str,
        message: str,
        span: dict[str, Any],
        remediation: str,
        *,
        provider_id: str | None = None,
        provider_version: str | None = None,
        active_profile: str | None = None,
        generated_origin_chain: list[dict[str, Any]] | None = None,
        type_expression: str | None = None,
        effect_set: list[str] | None = None,
        capability_set: list[str] | None = None,
        proof_id: str | None = None,
        core_rule: str | None = None,
    ) -> None:
        super().__init__(message)
        self.code = code
        self.message = message
        self.span = span
        self.remediation = remediation
        self.provider_id = provider_id
        self.provider_version = provider_version
        self.active_profile = active_profile
        self.generated_origin_chain = generated_origin_chain or []
        self.type_expression = type_expression
        self.effect_set = effect_set or []
        self.capability_set = capability_set or []
        self.proof_id = proof_id
        self.core_rule = core_rule

    def to_diagnostic(self) -> dict[str, Any]:
        return {
            "id": self.code,
            "message": self.message,
            "provider_id": self.provider_id,
            "provider_version": self.provider_version,
            "active_profile": self.active_profile,
            "span": self.span,
            "generated_origin_chain": self.generated_origin_chain,
            "type_expression": self.type_expression,
            "effect_set": self.effect_set,
            "capability_set": self.capability_set,
            "relevant_proof_id": self.proof_id,
            "core_rule": self.core_rule,
            "remediation": self.remediation,
            "analyzer_stage": "alternative-type-provider",
        }


def validate_alternative_type_manifest_file(path: Path) -> dict[str, Any]:
    manifest = load_manifest_file(path)
    return validate_alternative_type_manifest(manifest, str(path))


def alternative_type_manifest_diagnostic(path: Path) -> dict[str, Any] | None:
    try:
        validate_alternative_type_manifest_file(path)
    except AlternativeTypeError as exc:
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
    for pointer, value in data.get("append", {}).items():
        target = get_pointer(result, pointer)
        if not isinstance(target, list):
            raise ValueError(f"append target is not a list: {pointer}")
        target.append(value)
    return result


def validate_alternative_type_manifest(manifest: dict[str, Any], source: str) -> dict[str, Any]:
    if manifest.get("kind") != "alternative-type-system-manifest":
        raise AlternativeTypeError(
            "L17-PROVIDER",
            "alternative type manifest has the wrong kind",
            span(source, ""),
            "Use kind alternative-type-system-manifest.",
        )
    provider = manifest.get("provider", {})
    provider_id = provider.get("id")
    provider_version = provider.get("version")
    active_profile = manifest.get("namespace", {}).get("profile")
    origin_chain = manifest.get("diagnostic_mapping", {}).get("generated_origin_chain", [])

    if provider.get("kind") != ":type-system" or ":type/check" not in provider.get("implements", []):
        raise AlternativeTypeError(
            "L17-PROVIDER",
            "no selected type provider can check the namespace",
            span(source, "/provider"),
            "Select a type-system provider that implements type checking and records provider metadata.",
            provider_id=provider_id,
            provider_version=provider_version,
            active_profile=active_profile,
        )

    typed_core = manifest.get("typed_core", {})
    missing_core = sorted(REQUIRED_TYPED_CORE - set(typed_core))
    if typed_core.get("lowering_status") != ":accepted" or missing_core:
        raise AlternativeTypeError(
            "L17-LOWERING",
            "provider facts cannot lower to a typed core artifact accepted by downstream passes",
            span(source, "/typed_core"),
            "Emit typed core with types, effects, capabilities, ownership, casts, proofs, profile assumptions, and source maps.",
            provider_id=provider_id,
            provider_version=provider_version,
            active_profile=active_profile,
            generated_origin_chain=origin_chain,
            core_rule="typed core artifact compatibility",
        )

    soundness = manifest.get("soundness_evidence", {})
    if active_profile in provider.get("profiles", []) and not soundness.get(active_profile):
        raise AlternativeTypeError(
            "L17-SOUNDNESS",
            "provider claims a profile without required soundness evidence",
            span(source, "/soundness_evidence"),
            "Attach theorem, proof, conformance suite, or audit evidence for every claimed profile.",
            provider_id=provider_id,
            provider_version=provider_version,
            active_profile=active_profile,
            proof_id=None,
            core_rule="profile soundness claim",
        )

    for index, function in enumerate(typed_core.get("function_types", [])):
        if function.get("required_effects") and not function.get("effects"):
            raise AlternativeTypeError(
                "L17-EFFECT-ERASURE",
                "function type output loses required effect information",
                span(source, f"/typed_core/function_types/{index}"),
                "Preserve function effects in typed core and function type artifacts.",
                provider_id=provider_id,
                provider_version=provider_version,
                active_profile=active_profile,
                generated_origin_chain=function.get("generated_origin_chain", origin_chain),
                type_expression=function.get("type_expression"),
                effect_set=function.get("effects", []),
                capability_set=function.get("capabilities", []),
                core_rule="L6 effect preservation",
            )
        if function.get("required_capabilities") and not function.get("capabilities"):
            raise AlternativeTypeError(
                "L17-CAPABILITY-ERASURE",
                "capability requirements are missing from function types",
                span(source, f"/typed_core/function_types/{index}"),
                "Preserve capability requirements as typed values and authority metadata.",
                provider_id=provider_id,
                provider_version=provider_version,
                active_profile=active_profile,
                generated_origin_chain=function.get("generated_origin_chain", origin_chain),
                type_expression=function.get("type_expression"),
                effect_set=function.get("effects", []),
                capability_set=function.get("capabilities", []),
                core_rule="L15 capability preservation",
            )

    ownership = manifest.get("fact_exports", {}).get("ownership", {})
    if provider_claims_any(provider, ["ownership", "regions", "linear"]) and not ownership.get("complete"):
        raise AlternativeTypeError(
            "L17-OWNERSHIP-FACT",
            "memory or resource facts are incomplete for a claimed ownership feature",
            span(source, "/fact_exports/ownership"),
            "Export borrow, move, region, linear, initialization, and release facts needed by memory and concurrency checks.",
            provider_id=provider_id,
            provider_version=provider_version,
            active_profile=active_profile,
            generated_origin_chain=origin_chain,
            proof_id=ownership.get("proof_id"),
            core_rule="L10 ownership/resource facts",
        )

    for index, boundary in enumerate(manifest.get("gradual_boundaries", [])):
        if not boundary.get("recorded") or boundary.get("profile_legal") is False:
            raise AlternativeTypeError(
                "L17-GRADUAL-BOUNDARY",
                "dynamic boundary is illegal or unrecorded",
                span(source, f"/gradual_boundaries/{index}"),
                "Record source span, expected type, runtime check, failure type, and blame information.",
                provider_id=provider_id,
                provider_version=provider_version,
                active_profile=active_profile,
                generated_origin_chain=boundary.get("generated_origin_chain", origin_chain),
                type_expression=boundary.get("expected_type"),
                proof_id=boundary.get("proof_id"),
                core_rule="gradual boundary record",
            )

    for index, cast in enumerate(manifest.get("unsafe_casts", [])):
        if cast.get("safe_evidence"):
            raise AlternativeTypeError(
                "L17-UNSAFE-CAST",
                "unchecked cast was treated as safe optimization evidence",
                span(source, f"/unsafe_casts/{index}"),
                "Keep unchecked casts inside unsafe boundaries and do not use them as proof for safe optimizations.",
                provider_id=provider_id,
                provider_version=provider_version,
                active_profile=active_profile,
                generated_origin_chain=cast.get("generated_origin_chain", origin_chain),
                type_expression=cast.get("type_expression"),
                proof_id=cast.get("proof_id"),
                core_rule="unsafe cast boundary",
            )

    for index, fact in enumerate(manifest.get("domain_facts", [])):
        if not fact.get("crosses_boundary"):
            raise AlternativeTypeError(
                "L17-DOMAIN-FACT",
                "facet type facts fail to cross the boundary into typed artifacts",
                span(source, f"/domain_facts/{index}"),
                "Serialize domain facts into typed core, proof, schema, or facet artifact records.",
                provider_id=provider_id,
                provider_version=provider_version,
                active_profile=active_profile,
                generated_origin_chain=fact.get("generated_origin_chain", origin_chain),
                type_expression=fact.get("type_expression"),
                proof_id=fact.get("proof_id"),
                core_rule="L14 domain fact boundary",
            )

    mapping = manifest.get("diagnostic_mapping", {})
    if not mapping.get("source_span") or not mapping.get("generated_origin_chain") or not mapping.get("type_fact_id"):
        raise AlternativeTypeError(
            "L17-DIAGNOSTIC-MAP",
            "type diagnostic cannot be mapped through generated code to source",
            span(source, "/diagnostic_mapping"),
            "Preserve source span, generated-origin chain, and type-system fact ids in diagnostics.",
            provider_id=provider_id,
            provider_version=provider_version,
            active_profile=active_profile,
            generated_origin_chain=mapping.get("generated_origin_chain", []),
            type_expression=mapping.get("type_expression"),
            proof_id=mapping.get("proof_id"),
            core_rule="diagnostic source mapping",
        )

    return {
        "kind": "alternative-type-system-artifact",
        "document": "L17",
        "provider_declaration": provider,
        "typed_core_lowering_rules": manifest.get("typed_core_lowering_rules", []),
        "typed_core": typed_core,
        "fact_export_schema": manifest.get("fact_export_schema", {}),
        "proof_refinement_artifacts": manifest.get("proof_refinement_artifacts", []),
        "cast_runtime_check_records": manifest.get("gradual_boundaries", []),
        "type_diagnostic_mapping_records": [mapping],
        "reference_compatibility_report": manifest.get("reference_compatibility_report", {}),
        "profile_soundness_evidence": soundness,
        "diagnostics": [],
    }


def provider_claims_any(provider: dict[str, Any], features: list[str]) -> bool:
    claimed = set(provider.get("features", []))
    return any(feature in claimed for feature in features)


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
