"""L14 language facet manifest, activation, and IR validation."""

from __future__ import annotations

import copy
import json
from pathlib import Path
from typing import Any


REQUIRED_IR_FIELDS = {
    "facet_id",
    "facet_version",
    "source_span_map",
    "generated_origin_map",
    "type_effect_annotations",
    "profile_assumptions",
    "capability_requirements",
    "build_inputs",
    "generated_outputs",
    "validation_results",
    "proof_obligations",
    "serialization_format",
    "artifact_schema_version",
    "compatibility_version",
}


class FacetError(Exception):
    def __init__(
        self,
        code: str,
        message: str,
        span: dict[str, Any],
        remediation: str,
        *,
        facet_id: str | None = None,
        facet_version: str | None = None,
        active_profile: str | None = None,
        generated_origin_chain: list[dict[str, Any]] | None = None,
        requested_effects: list[str] | None = None,
        requested_capabilities: list[str] | None = None,
        domain_rule: str | None = None,
        private_value_id: str | None = None,
        witness_id: str | None = None,
        public_output_schema_id: str | None = None,
        reveal_reason: str | None = None,
        facet_edge: dict[str, Any] | None = None,
    ) -> None:
        super().__init__(message)
        self.code = code
        self.message = message
        self.span = span
        self.remediation = remediation
        self.facet_id = facet_id
        self.facet_version = facet_version
        self.active_profile = active_profile
        self.generated_origin_chain = generated_origin_chain or []
        self.requested_effects = requested_effects or []
        self.requested_capabilities = requested_capabilities or []
        self.domain_rule = domain_rule
        self.private_value_id = private_value_id
        self.witness_id = witness_id
        self.public_output_schema_id = public_output_schema_id
        self.reveal_reason = reveal_reason
        self.facet_edge = facet_edge

    def to_diagnostic(self) -> dict[str, Any]:
        return {
            "id": self.code,
            "message": self.message,
            "facet_id": self.facet_id,
            "facet_version": self.facet_version,
            "active_profile": self.active_profile,
            "span": self.span,
            "generated_origin_chain": self.generated_origin_chain,
            "requested_effects": self.requested_effects,
            "requested_capabilities": self.requested_capabilities,
            "domain_rule": self.domain_rule,
            "private_value_id": self.private_value_id,
            "witness_id": self.witness_id,
            "public_output_schema_id": self.public_output_schema_id,
            "reveal_reason": self.reveal_reason,
            "facet_edge": self.facet_edge,
            "remediation": self.remediation,
            "analyzer_stage": "facet-system",
        }


def validate_facet_manifest_file(path: Path) -> dict[str, Any]:
    manifest = load_manifest_file(path)
    return validate_facet_manifest(manifest, str(path))


def facet_manifest_diagnostic(path: Path) -> dict[str, Any] | None:
    try:
        validate_facet_manifest_file(path)
    except FacetError as exc:
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


def validate_facet_manifest(manifest: dict[str, Any], source: str) -> dict[str, Any]:
    if manifest.get("kind") != "facet-system-manifest":
        raise FacetError(
            "L14-IR-SCHEMA",
            "facet system manifest has the wrong kind",
            span(source, ""),
            "Use kind facet-system-manifest.",
        )

    namespace = manifest.get("namespace", {})
    active_profile = namespace.get("profile")
    active_facets = set(namespace.get("active_facets", []))
    declared_build_effects = set(namespace.get("build_effect_grants", []))
    declared_capabilities = set(namespace.get("capability_context", []))
    facets = {facet["id"]: facet for facet in manifest.get("facets", [])}

    activation_records = []
    for facet_id in active_facets:
        facet = facets.get(facet_id)
        if facet:
            activation_records.append(
                {
                    "namespace": namespace.get("name"),
                    "facet_id": facet_id,
                    "facet_version": facet.get("version"),
                    "profile": active_profile,
                    "lexical_scope": "namespace",
                    "source_span": namespace.get("span", span(source, "/namespace")),
                }
            )

    for index, form in enumerate(manifest.get("source_forms", [])):
        validate_source_form(form, index, facets, active_facets, active_profile, declared_build_effects, declared_capabilities, source)

    generated_forms = []
    domain_ir_records = []
    for index, output in enumerate(manifest.get("lowering_outputs", [])):
        generated, domain_ir = validate_lowering_output(output, index, facets, active_profile, source)
        if generated:
            generated_forms.append(generated)
        if domain_ir:
            domain_ir_records.append(domain_ir)

    for index, edge in enumerate(manifest.get("composition_edges", [])):
        validate_composition_edge(edge, index, source, active_profile, facets)

    return {
        "kind": "facet-system-artifact",
        "document": "L14",
        "namespace": namespace.get("name"),
        "active_profile": active_profile,
        "target": namespace.get("target"),
        "facet_manifests": [facet_record(facet) for facet in manifest.get("facets", [])],
        "facet_activation_records": activation_records,
        "generated_gravity_forms": generated_forms,
        "domain_ir_records": domain_ir_records,
        "facet_diagnostics": [],
        "composition_records": manifest.get("composition_edges", []),
        "privacy_boundary_records": [
            edge["privacy_boundary"]
            for edge in manifest.get("composition_edges", [])
            if edge.get("privacy_boundary")
        ],
        "compatibility_migration_records": manifest.get("compatibility_records", []),
    }


def validate_source_form(
    form: dict[str, Any],
    index: int,
    facets: dict[str, dict[str, Any]],
    active_facets: set[str],
    active_profile: str,
    declared_build_effects: set[str],
    declared_capabilities: set[str],
    source: str,
) -> None:
    form_name = form.get("form")
    explicit_facet_id = form.get("facet_id")
    claiming = [
        facet
        for facet in facets.values()
        if form_name in facet.get("surface", []) and facet["id"] in active_facets
    ]
    form_span = form.get("span", span(source, f"/source_forms/{index}"))
    if explicit_facet_id:
        facet = facets.get(explicit_facet_id)
        if explicit_facet_id not in active_facets or not facet or form_name not in facet.get("surface", []):
            raise FacetError(
                "L14-FACET-NOT-ACTIVE",
                "facet form was used without namespace-scoped activation",
                form_span,
                "Activate the facet in namespace metadata or emit a nested activation with provenance.",
                facet_id=explicit_facet_id,
                facet_version=facet.get("version") if facet else None,
                active_profile=active_profile,
                domain_rule="namespace-scoped facet activation",
            )
    else:
        if not claiming:
            raise FacetError(
                "L14-FACET-NOT-ACTIVE",
                "facet form was used without an active owning facet",
                form_span,
                "Activate the owning facet or remove the facet form.",
                active_profile=active_profile,
                domain_rule="namespace-scoped facet activation",
            )
        if len(claiming) > 1:
            raise FacetError(
                "L14-FACET-AMBIGUOUS",
                "multiple active facets claim the same surface form",
                form_span,
                "Disambiguate with an explicit facet alias or activation boundary.",
                active_profile=active_profile,
                requested_effects=[],
                requested_capabilities=[],
                domain_rule="facet dispatch ambiguity",
            )
        facet = claiming[0]

    if not explicit_facet_id:
        facet = claiming[0]
    if active_profile not in facet.get("profiles", []):
        raise FacetError(
            "L14-PROFILE",
            "facet use is outside the facet's supported profile set",
            form_span,
            "Select a profile-supported facet or gate the source form.",
            facet_id=facet["id"],
            facet_version=facet.get("version"),
            active_profile=active_profile,
            domain_rule="facet profile support",
        )

    missing_effects = sorted(set(facet.get("build_effects", [])) - declared_build_effects)
    if missing_effects:
        raise FacetError(
            "L14-BUILD-EFFECT",
            "facet expansion or artifact emission requires undeclared build effects",
            form_span,
            "Declare and grant facet build effects before expansion.",
            facet_id=facet["id"],
            facet_version=facet.get("version"),
            active_profile=active_profile,
            requested_effects=missing_effects,
            requested_capabilities=facet.get("capabilities", []),
            domain_rule="facet build-effect authority",
        )

    missing_capabilities = sorted(set(facet.get("capabilities", [])) - declared_capabilities)
    if missing_capabilities:
        raise FacetError(
            "L14-CAPABILITY",
            "facet expansion or runtime use requires missing capabilities",
            form_span,
            "Provide the capability context or disable the facet.",
            facet_id=facet["id"],
            facet_version=facet.get("version"),
            active_profile=active_profile,
            requested_effects=facet.get("build_effects", []),
            requested_capabilities=missing_capabilities,
            domain_rule="facet capability authority",
        )


def validate_lowering_output(
    output: dict[str, Any],
    index: int,
    facets: dict[str, dict[str, Any]],
    active_profile: str,
    source: str,
) -> tuple[dict[str, Any] | None, dict[str, Any] | None]:
    facet = facets.get(output.get("facet_id"), {})
    output_span = output.get("source_span", span(source, f"/lowering_outputs/{index}"))
    origin_chain = output.get("generated_origin_chain", [])
    if output.get("lowering_status") != ":ok":
        raise FacetError(
            "L14-LOWERING",
            "facet output cannot lower to its declared Gravity or domain IR target",
            output_span,
            "Declare the lowering target and emit checked Gravity forms or a valid domain IR.",
            facet_id=output.get("facet_id"),
            facet_version=facet.get("version"),
            active_profile=active_profile,
            generated_origin_chain=origin_chain,
            domain_rule="facet lowering target",
        )

    generated = output.get("generated_gravity")
    if generated and not generated.get("validated"):
        raise FacetError(
            "L14-GENERATED-CODE",
            "facet-generated Gravity code failed normal validation",
            output_span,
            "Route generated code through reader, macro, core, type, effect, memory, safety, and profile checks.",
            facet_id=output.get("facet_id"),
            facet_version=facet.get("version"),
            active_profile=active_profile,
            generated_origin_chain=origin_chain,
            domain_rule="generated Gravity validation",
        )

    domain_ir = output.get("domain_ir")
    if domain_ir:
        missing = sorted(REQUIRED_IR_FIELDS - set(domain_ir))
        if missing or domain_ir.get("artifact_schema_version") != facet.get("artifact_schema_version"):
            raise FacetError(
                "L14-IR-SCHEMA",
                "facet domain IR is invalid, stale, or incompatible with the manifest",
                output_span,
                "Emit versioned domain IR with source maps, generated origins, type/effect facts, and matching schema version.",
                facet_id=output.get("facet_id"),
                facet_version=facet.get("version"),
                active_profile=active_profile,
                generated_origin_chain=origin_chain,
                domain_rule="domain IR schema",
            )
        if domain_ir.get("validation_results", {}).get("status") != ":passed":
            raise FacetError(
                "L14-DOMAIN-CHECK",
                "facet-local domain validation failed",
                output_span,
                "Fix the domain rule failure or emit a rejected diagnostic at the facet boundary.",
                facet_id=output.get("facet_id"),
                facet_version=facet.get("version"),
                active_profile=active_profile,
                generated_origin_chain=origin_chain,
                domain_rule=domain_ir.get("validation_results", {}).get("rule"),
            )

    generated_record = None
    if generated:
        generated_record = {
            "facet_id": output.get("facet_id"),
            "validated": generated.get("validated"),
            "form_hash": generated.get("form_hash"),
            "generated_origin_chain": origin_chain,
            "source_span": output_span,
        }
    return generated_record, domain_ir


def validate_composition_edge(
    edge: dict[str, Any],
    index: int,
    source: str,
    active_profile: str,
    facets: dict[str, dict[str, Any]],
) -> None:
    edge_span = edge.get("span", span(source, f"/composition_edges/{index}"))
    if not edge.get("boundary_declared") or not edge.get("effects_visible") or not edge.get("capabilities_visible"):
        raise FacetError(
            "L14-COMPOSITION",
            "facet composition hides a boundary, effect, or capability requirement",
            edge_span,
            "Declare the facet boundary and preserve all effects and capabilities across the edge.",
            facet_id=edge.get("from"),
            facet_version=facets.get(edge.get("from"), {}).get("version"),
            active_profile=active_profile,
            facet_edge=edge,
            domain_rule="facet composition boundary",
        )

    boundary = edge.get("privacy_boundary")
    if boundary and (
        not boundary.get("preserved")
        or not boundary.get("private_value_id")
        or not boundary.get("public_output_schema_id")
        or not boundary.get("witness_id")
    ):
        raise FacetError(
            "L14-PRIVACY-BOUNDARY",
            "facet composition dropped a private input, witness, credential, or disclosure boundary",
            edge_span,
            "Carry privacy labels, witness provenance, reveal reason, and public-output schema across the facet edge.",
            facet_id=edge.get("from"),
            facet_version=facets.get(edge.get("from"), {}).get("version"),
            active_profile=active_profile,
            private_value_id=boundary.get("private_value_id"),
            witness_id=boundary.get("witness_id"),
            public_output_schema_id=boundary.get("public_output_schema_id"),
            reveal_reason=boundary.get("reveal_reason"),
            facet_edge=edge,
            domain_rule="privacy-preserving facet composition",
        )


def facet_record(facet: dict[str, Any]) -> dict[str, Any]:
    return {
        "id": facet["id"],
        "version": facet.get("version"),
        "surface": facet.get("surface", []),
        "profiles": facet.get("profiles", []),
        "requires_facets": facet.get("requires_facets", []),
        "runtime_effects": facet.get("runtime_effects", []),
        "build_effects": facet.get("build_effects", []),
        "capabilities": facet.get("capabilities", []),
        "lowers_to": facet.get("lowers_to", []),
        "artifacts": facet.get("artifacts", []),
        "artifact_schema_version": facet.get("artifact_schema_version"),
        "stability": facet.get("stability"),
    }


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
