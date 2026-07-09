"""Phase 10 schema, data, and interop artifact validation."""

from __future__ import annotations

import hashlib
import json
from pathlib import Path
from typing import Any


DOCUMENT_COMPONENTS = {
    "S1": "source_schema_ir",
    "S2": "serialization_fixture",
    "S3": "canonical_format",
    "S4": "graphql_generation",
    "S5": "openapi_generation",
    "S6": "database_mapping",
    "S7": "binary_abi_schema",
    "S8": "typed_config",
    "S9": "artifact_schema_registry",
}


class SchemaInteropError(Exception):
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
            "schema_id": self.record.get("schema_id"),
            "version": self.record.get("version") or self.record.get("schema_version"),
            "boundary": self.record.get("boundary"),
            "artifact_id": self.record.get("artifact_id") or self.record.get("id"),
            "source_span": self.record.get("source_span", {"source": self.source}),
            "missing_fact": self.missing_fact,
            "remediation": self.remediation,
            "analyzer_stage": "phase10-schema-interop-validation",
        }


def validate_schema_interop_file(path: Path) -> dict[str, Any]:
    return validate_schema_interop_manifest(load_manifest_file(path), str(path))


def schema_interop_diagnostic(path: Path) -> dict[str, Any] | None:
    try:
        validate_schema_interop_file(path)
    except SchemaInteropError as exc:
        return exc.to_diagnostic()
    return None


def validate_schema_interop_manifest(manifest: dict[str, Any], source: str) -> dict[str, Any]:
    if manifest.get("kind") != "schema-interop-input":
        raise_error("S1-MANIFEST", "schema interop input has wrong kind", {}, source, "schema-interop-input")

    source_schema = require_dict(manifest, "source_schema_ir", "S1-MANIFEST", source)
    validator = require_dict(manifest, "validator_artifact", "S1-MANIFEST", source)
    serialization = require_dict(manifest, "serialization_fixture", "S2-MANIFEST", source)
    canonical = require_dict(manifest, "canonical_format", "S3-MANIFEST", source)
    graphql = require_dict(manifest, "graphql_generation", "S4-MANIFEST", source)
    openapi = require_dict(manifest, "openapi_generation", "S5-MANIFEST", source)
    database = require_dict(manifest, "database_mapping", "S6-MANIFEST", source)
    binary_abi = require_dict(manifest, "binary_abi_schema", "S7-MANIFEST", source)
    typed_config = require_dict(manifest, "typed_config", "S8-MANIFEST", source)
    artifact_registry = require_dict(manifest, "artifact_schema_registry", "S9-MANIFEST", source)

    validate_source_schema(source_schema, source)
    validate_validator(validator, source)
    validate_serialization(serialization, source_schema, source)
    validate_canonical(canonical, source_schema, source)
    validate_graphql(graphql, source_schema, source)
    validate_openapi(openapi, source_schema, source)
    validate_database(database, source_schema, source)
    validate_binary_abi(binary_abi, source_schema, source)
    validate_typed_config(typed_config, source_schema, source)
    validate_artifact_registry(artifact_registry, source_schema, source)

    document_contracts = {
        "S1": source_schema,
        "S2": serialization,
        "S3": canonical,
        "S4": graphql,
        "S5": openapi,
        "S6": database,
        "S7": binary_abi,
        "S8": typed_config,
        "S9": artifact_registry,
    }

    return {
        "kind": "schema-interop-artifact",
        "phase": "10",
        "package": manifest.get("package"),
        "module": manifest.get("module"),
        "source_schema_ir": source_schema,
        "validator_artifact": validator,
        "serialization_fixture": serialization,
        "canonical_format": canonical,
        "graphql_generation": graphql,
        "openapi_generation": openapi,
        "database_mapping": database,
        "binary_abi_schema": binary_abi,
        "typed_config": typed_config,
        "artifact_schema_registry": artifact_registry,
        "document_contracts": document_contracts,
        "coverage_summary": {
            "documents": len(DOCUMENT_COMPONENTS),
            "schema_id": source_schema["schema_id"],
            "generated_artifacts": 8,
            "status": ":passed",
        },
        "input_hash": artifact_hash(manifest),
        "diagnostics": [],
    }


def validate_source_schema(record: dict[str, Any], source: str) -> None:
    require_fields(
        record,
        [
            "schema_id",
            "version",
            "hash",
            "source_span",
            "compatibility_mode",
            "validation_boundaries",
            "derivation_targets",
            "type_projection",
            "taint_policy",
            "effect_capability_summary",
            "semantic_diff",
            "migration_requirements",
        ],
        "S1-MANIFEST",
        source,
    )
    if record.get("weakened_projection"):
        raise_error("S1-PROJECTION", "generated projection weakens source schema", record, source, "non-weakened-projection")
    if not record.get("validation_boundaries"):
        raise_error("S1-BOUNDARY", "schema lacks validation boundary metadata", record, source, "validation-boundaries")
    if not record.get("compatibility_mode"):
        raise_error("S1-COMPATIBILITY", "schema lacks compatibility policy", record, source, "compatibility-policy")


def validate_validator(record: dict[str, Any], source: str) -> None:
    require_fields(
        record,
        ["schema_id", "validator_id", "clears_taint", "retains_residuals", "failure_type", "boundary_reports"],
        "S1-MANIFEST",
        source,
    )
    if record.get("uses_weaker_rules"):
        raise_error("S1-PROJECTION", "validator is weaker than source schema", record, source, "source-equivalent-validator")


def validate_serialization(record: dict[str, Any], source_schema: dict[str, Any], source: str) -> None:
    require_same_schema(record, source_schema, "S2-MANIFEST", source)
    require_fields(
        record,
        [
            "format",
            "field_policy",
            "unknown_field_policy",
            "numeric_policy",
            "string_policy",
            "variant_policy",
            "trust_boundary",
            "canonicalization_mode",
            "round_trip_vectors",
            "compatibility_report",
        ],
        "S2-MANIFEST",
        source,
    )
    if record.get("unsafe_polymorphic"):
        raise_error("S2-POLYMORPHIC", "serialization allows unsafe polymorphic decoding", record, source, "finite-type-set")
    if record.get("lossy_numeric"):
        raise_error("S2-NUMERIC", "serialization has lossy numeric conversion without policy", record, source, "numeric-policy")
    if not record.get("taint_boundary_record"):
        raise_error("S2-TAINT", "decoded values lack taint boundary record", record, source, "taint-boundary-record")
    if not record.get("round_trip_vectors"):
        raise_error("S2-ROUNDTRIP", "serialization lacks round-trip fixtures", record, source, "round-trip-vectors")


def validate_canonical(record: dict[str, Any], source_schema: dict[str, Any], source: str) -> None:
    require_same_schema(record, source_schema, "S3-MANIFEST", source)
    require_fields(
        record,
        [
            "format_version",
            "schema_hash_included",
            "map_order",
            "set_order",
            "numeric_policy",
            "string_normalization",
            "metadata_policy",
            "reference_vectors",
            "hash_input_record",
            "signing_input_record",
        ],
        "S3-MANIFEST",
        source,
    )
    if record.get("host_order_dependent"):
        raise_error("S3-ORDER", "canonical encoding depends on host iteration order", record, source, "deterministic-order")
    if record.get("hash_over_noncanonical_bytes"):
        raise_error("S3-HASH", "hash or signature uses noncanonical bytes", record, source, "canonical-byte-input")
    if not record.get("reference_vectors"):
        raise_error("S3-VECTOR", "canonical format lacks reference vectors", record, source, "reference-vectors")


def validate_graphql(record: dict[str, Any], source_schema: dict[str, Any], source: str) -> None:
    require_same_schema(record, source_schema, "S4-MANIFEST", source)
    require_fields(
        record,
        [
            "sdl",
            "resolver_adapters",
            "typed_client",
            "query_validation",
            "auth_capability_metadata",
            "schema_diff_report",
            "source_map",
        ],
        "S4-MANIFEST",
        source,
    )
    if record.get("nullability_weakened"):
        raise_error("S4-NULLABILITY", "GraphQL nullability weakens Gravity source schema", record, source, "gravity-nullability-map")
    if record.get("hidden_resolver_effect"):
        raise_error("S4-RESOLVER", "GraphQL resolver hides effect or capability", record, source, "resolver-effect-capability")
    if not record.get("source_map"):
        raise_error("S4-SOURCEMAP", "GraphQL output lacks source provenance", record, source, "source-map")


def validate_openapi(record: dict[str, Any], source_schema: dict[str, Any], source: str) -> None:
    require_same_schema(record, source_schema, "S5-MANIFEST", source)
    require_fields(
        record,
        [
            "route_id",
            "method",
            "path",
            "error_model",
            "auth_capability_metadata",
            "taint_boundary",
            "typed_client",
            "contract_tests",
            "source_map",
        ],
        "S5-MANIFEST",
        source,
    )
    if not record.get("request_validator") or not record.get("response_validator"):
        raise_error("S5-SCHEMA", "OpenAPI route lacks request or response schemas", record, source, "request-response-schemas")
    if record.get("hidden_route_effect"):
        raise_error("S5-CAPABILITY", "OpenAPI handler effect is outside grants", record, source, "route-effect-grant")
    if record.get("unvalidated_http_input"):
        raise_error("S5-TAINT", "HTTP input reaches trusted sink without validation", record, source, "http-taint-validation")


def validate_database(record: dict[str, Any], source_schema: dict[str, Any], source: str) -> None:
    require_same_schema(record, source_schema, "S6-MANIFEST", source)
    require_fields(
        record,
        [
            "dialect",
            "table_mapping",
            "column_types",
            "constraints",
            "indexes",
            "row_adapter",
            "schema_diff_report",
            "migration_plan",
            "data_loss_report",
            "rollback_or_forward_policy",
            "required_review_record",
            "capabilities",
            "fixture_validation",
        ],
        "S6-MANIFEST",
        source,
    )
    if record.get("destructive_without_policy"):
        raise_error("S6-DATA-LOSS", "destructive migration lacks data-loss policy or review evidence", record, source, "data-loss-policy-review")
    if record.get("runtime_without_capability"):
        raise_error("S6-CAPABILITY", "runtime migration lacks write/admin capability", record, source, "migration-capability")
    if record.get("row_adapter_weakened"):
        raise_error("S6-ADAPTER", "row adapter weakens source schema", record, source, "source-equivalent-row-adapter")


def validate_binary_abi(record: dict[str, Any], source_schema: dict[str, Any], source: str) -> None:
    require_same_schema(record, source_schema, "S7-MANIFEST", source)
    require_fields(
        record,
        [
            "field_order",
            "widths",
            "alignment",
            "padding",
            "endian",
            "variant_discriminants",
            "pointer_policy",
            "target_abi",
            "reference_vectors",
            "ffi_binding_input",
        ],
        "S7-MANIFEST",
        source,
    )
    if not record.get("ownership_lifetime_map"):
        raise_error("S7-POINTER", "pointer or handle layout lacks lifetime and ownership policy", record, source, "ownership-lifetime-map")
    if record.get("implicit_host_layout"):
        raise_error("S7-LAYOUT", "binary layout uses implicit host layout", record, source, "explicit-layout")
    if not record.get("reference_vectors"):
        raise_error("S7-FIXTURE", "binary layout lacks byte reference fixtures", record, source, "reference-byte-vectors")


def validate_typed_config(record: dict[str, Any], source_schema: dict[str, Any], source: str) -> None:
    require_same_schema(record, source_schema, "S8-MANIFEST", source)
    require_fields(
        record,
        [
            "sources",
            "precedence",
            "defaults",
            "required_fields",
            "secret_fields",
            "validation_rules",
            "effects",
            "capabilities",
            "redaction_policy",
            "build_reproducibility_record",
            "runtime_reload_policy",
        ],
        "S8-MANIFEST",
        source,
    )
    if record.get("ambient_access"):
        raise_error("S8-CAPABILITY", "config access uses ambient authority", record, source, "config-capability-grants")
    if record.get("secret_leak"):
        raise_error("S8-SECRET", "secret value leaks into public artifact or diagnostic", record, source, "redaction-policy")
    if record.get("untracked_build_env"):
        raise_error("S8-HERMETICITY", "build-time config read is not captured as reproducibility input", record, source, "build-input-record")


def validate_artifact_registry(record: dict[str, Any], source_schema: dict[str, Any], source: str) -> None:
    require_same_schema(record, source_schema, "S9-MANIFEST", source)
    require_fields(
        record,
        [
            "artifact_kinds",
            "schema_versions",
            "required_fields",
            "content_hash_schema",
            "provenance_schema",
            "release_gate_schema",
            "canonical_encoding",
            "compatibility_report",
        ],
        "S9-MANIFEST",
        source,
    )
    if not record.get("evidence_schema"):
        raise_error("S9-EVIDENCE", "artifact schema lacks evidence requirements", record, source, "evidence-schema")
    if record.get("noncanonical_hash"):
        raise_error("S9-CANONICAL", "artifact hash input is not canonical", record, source, "canonical-encoding")
    if record.get("cycle_without_bootstrap"):
        raise_error("S9-CYCLE", "artifact graph cycle lacks bootstrap provenance", record, source, "bootstrap-provenance")


def require_same_schema(record: dict[str, Any], source_schema: dict[str, Any], code: str, source: str) -> None:
    if record.get("schema_id") != source_schema.get("schema_id") or record.get("schema_version") != source_schema.get("version"):
        raise_error(code, "artifact does not reference the source schema identity", record, source, "schema-id-version")
    if record.get("schema_hash") != source_schema.get("hash"):
        raise_error(code, "artifact does not reference the source schema hash", record, source, "schema-hash")


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
    raise SchemaInteropError(
        code,
        message,
        record=record,
        source=source,
        missing_fact=missing_fact,
        remediation="Update the Phase 10 schema manifest, generated artifact, fixture, or compatibility evidence.",
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
