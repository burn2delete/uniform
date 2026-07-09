"""P01-T06 validation for alternative subsystem and interop extension hooks."""

from __future__ import annotations

import copy
import json
from pathlib import Path
from typing import Any


REQUIRED_PIPELINE_ARTIFACTS = {
    "reader": "syntax-object-stream",
    "namespace": "module-artifact",
    "macro_trace": "macro-expansion-trace",
    "core_ast": "core-ast-artifact",
    "typed_core": "typed-effected-core-artifact",
}
REQUIRED_SYNTAX_GUARANTEES = {"source-span", "metadata", "hygiene", "generated-origin-chain"}
REQUIRED_MACRO_PHASES = {"read", "namespace", "load", "invoke", "validate-generated", "re-expand", "typed-core"}
REQUIRED_INTEROP_FIELDS = {
    "id",
    "foreign_source",
    "boundary_kind",
    "language",
    "abi_or_protocol",
    "type_mapping",
    "ownership",
    "effects",
    "capabilities",
    "error_mapping",
    "memory_behavior",
    "threading",
    "safety",
    "profiles",
    "provider",
    "version",
}
CONSTRAINED_MEMORY_PROFILES = {":kernel", ":firmware", ":hardware"}
HOST_LEAK_PROFILES = {":core", ":kernel", ":firmware", ":hardware"}


class ExtensionHookError(Exception):
    def __init__(self, code: str, message: str, span: dict[str, Any], remediation: str, details: dict[str, Any] | None = None):
        super().__init__(message)
        self.code = code
        self.message = message
        self.span = span
        self.remediation = remediation
        self.details = details or {}

    def to_diagnostic(self) -> dict[str, Any]:
        diagnostic = {
            "id": self.code,
            "message": self.message,
            "span": self.span,
            "remediation": self.remediation,
            "analyzer_stage": "extension-hook-validation",
        }
        diagnostic.update(self.details)
        return diagnostic


def validate_extension_manifest(manifest: dict[str, Any], root: Path) -> dict[str, Any]:
    span = manifest_span(manifest)
    if manifest.get("kind") != "phase-01-extension-hooks":
        raise ExtensionHookError(
            "P01-T06-MANIFEST",
            "extension hook manifest has the wrong kind",
            span,
            "Use kind phase-01-extension-hooks.",
        )

    pipeline_artifacts = validate_pipeline_artifacts(manifest, root)
    provider_records = []
    for provider in manifest.get("providers", []):
        kind = provider.get("kind")
        if kind == ":macro-system":
            provider_records.append(validate_macro_provider(provider))
        elif kind == ":type-system":
            provider_records.append(validate_type_provider(provider))
        elif kind == ":memory-system":
            provider_records.append(validate_memory_provider(provider))
        else:
            raise ExtensionHookError(
                "L16-PROVIDER",
                f"unsupported extension provider kind {kind}",
                provider_span(provider),
                "Declare a supported macro, type, or memory provider kind.",
                provider_details(provider),
            )

    interop_records = [validate_interop_boundary(boundary) for boundary in manifest.get("interop_boundaries", [])]
    if not provider_records:
        raise ExtensionHookError("L16-PROVIDER", "no extension providers were declared", span, "Declare at least one constrained provider.")

    return {
        "kind": "extension-hook-artifact",
        "module": manifest.get("module"),
        "active_profile": manifest.get("active_profile"),
        "pipeline_artifacts": pipeline_artifacts,
        "providers": provider_records,
        "interop_boundaries": interop_records,
        "normal_pipeline_required": list(REQUIRED_PIPELINE_ARTIFACTS),
        "diagnostics": [],
    }


def validate_pipeline_artifacts(manifest: dict[str, Any], root: Path) -> dict[str, Any]:
    span = manifest_span(manifest)
    artifacts = manifest.get("normal_pipeline_artifacts", {})
    normalized = {}
    for name, expected_kind in REQUIRED_PIPELINE_ARTIFACTS.items():
        relative = artifacts.get(name)
        if not relative:
            raise ExtensionHookError(
                "P01-T06-PIPELINE",
                f"normal pipeline artifact {name} is missing",
                span,
                "Reference the existing reader, namespace, macro, core, and typed-core artifacts.",
            )
        path = root / relative
        if not path.exists():
            raise ExtensionHookError(
                "P01-T06-PIPELINE",
                f"normal pipeline artifact {relative} does not exist",
                span,
                "Generate the upstream pipeline artifact before validating extension hooks.",
            )
        data = json.loads(path.read_text(encoding="utf-8"))
        if data.get("kind") != expected_kind:
            raise ExtensionHookError(
                "P01-T06-PIPELINE",
                f"artifact {relative} has kind {data.get('kind')} instead of {expected_kind}",
                span,
                "Point to the correct upstream artifact.",
            )
        normalized[name] = {"path": relative, "kind": data["kind"]}
    return normalized


def validate_macro_provider(provider: dict[str, Any]) -> dict[str, Any]:
    span = provider_span(provider)
    missing_guarantees = sorted(REQUIRED_SYNTAX_GUARANTEES - set(provider.get("syntax_object_guarantees", [])))
    if missing_guarantees:
        raise ExtensionHookError(
            "L16-SYNTAX-OBJECT",
            f"macro provider loses syntax object guarantees: {missing_guarantees}",
            span,
            "Expose source span, metadata, hygiene, and generated-origin operations.",
            provider_details(provider),
        )
    missing_phases = sorted(REQUIRED_MACRO_PHASES - set(provider.get("phase_model", [])))
    if missing_phases:
        raise ExtensionHookError(
            "L16-PHASE",
            f"macro provider does not expose required phase boundaries: {missing_phases}",
            span,
            "Emit artifacts as if the L16 phase boundaries existed.",
            provider_details(provider),
        )
    declared = set(provider.get("build_effects", []))
    granted = {grant.get("effect") for grant in provider.get("grants", [])}
    ungranted = sorted(effect for effect in declared if effect not in granted)
    if ungranted:
        raise ExtensionHookError(
            "L16-BUILD-EFFECT",
            f"macro provider build effects are not granted: {ungranted}",
            span,
            "Declare and grant every build effect with replay policy and provider identity.",
            provider_details(provider, {"build_effects": sorted(declared)}),
        )
    if any(not grant.get("replayable") for grant in provider.get("grants", [])):
        raise ExtensionHookError(
            "L16-HERMETIC",
            "macro provider grant is not replayable under hermetic policy",
            span,
            "Record replayable inputs, outputs, and redaction policy for macro build effects.",
            provider_details(provider),
        )
    equivalence = provider.get("reference_equivalence", {})
    if equivalence.get("result") != ":equivalent":
        raise ExtensionHookError(
            "L16-EQUIVALENCE",
            "macro provider lacks reference-equivalence evidence",
            span,
            "Run the L4 conformance corpus and record structural equivalence.",
            provider_details(provider, {"equivalent_l4_rule": "L4"}),
        )
    validations = set(provider.get("generated_validation", []))
    if not {"macro-trace", "core-ast", "typed-core"}.issubset(validations):
        raise ExtensionHookError(
            "L16-GENERATED",
            "macro provider does not route generated code through normal validation",
            span,
            "Validate generated syntax through macro trace, core AST, and typed core stages.",
            provider_details(provider),
        )
    return normalized_provider(provider, "L16")


def validate_type_provider(provider: dict[str, Any]) -> dict[str, Any]:
    span = provider_span(provider)
    if not provider.get("effect_preservation"):
        raise ExtensionHookError(
            "L17-EFFECT-ERASURE",
            "type provider output erases function effect information",
            span,
            "Preserve function latent effects in typed core artifacts.",
            provider_details(provider),
        )
    if not provider.get("capability_preservation"):
        raise ExtensionHookError(
            "L17-CAPABILITY-ERASURE",
            "type provider output erases capability requirements",
            span,
            "Preserve capability requirements in function and value type facts.",
            provider_details(provider),
        )
    if provider.get("soundness_claim") and not provider.get("soundness_evidence"):
        raise ExtensionHookError(
            "L17-SOUNDNESS",
            "type provider claims profile soundness without evidence",
            span,
            "Attach theorem, proof, audit, or conformance evidence for the claimed profiles.",
            provider_details(provider),
        )
    if provider.get("gradual") and "dynamic-boundary-records" not in provider.get("exported_facts", []):
        raise ExtensionHookError(
            "L17-GRADUAL-BOUNDARY",
            "gradual provider does not export dynamic boundary records",
            span,
            "Serialize dynamic checks, blame, expected type, and failure behavior.",
            provider_details(provider),
        )
    return normalized_provider(provider, "L17")


def validate_memory_provider(provider: dict[str, Any]) -> dict[str, Any]:
    span = provider_span(provider)
    profiles = set(provider.get("profiles", []))
    allocation = provider.get("allocation", {})
    if allocation.get("hidden") and profiles & CONSTRAINED_MEMORY_PROFILES:
        raise ExtensionHookError(
            "L18-HIDDEN-ALLOC",
            "memory provider performs hidden allocation in a constrained profile",
            span,
            "Make allocation explicit or reject constrained profiles.",
            provider_details(provider, {"profiles": sorted(profiles)}),
        )
    outcomes = set(provider.get("safe_outcomes", []))
    if not {":proven-safe", ":runtime-checked", ":rejected", ":unsafe-island"}.issubset(outcomes):
        raise ExtensionHookError(
            "L18-PROVIDER",
            "memory provider does not expose all safe-code classification outcomes",
            span,
            "Classify dangerous operations as proven-safe, runtime-checked, rejected, or unsafe-island.",
            provider_details(provider),
        )
    for wrapper in provider.get("unsafe_wrappers", []):
        if not wrapper.get("invariants") or not wrapper.get("evidence"):
            raise ExtensionHookError(
                "L18-UNSAFE-AUDIT",
                "safe wrapper lacks invariant or evidence records",
                span,
                "Record wrapper invariants and proof or runtime-check evidence.",
                provider_details(provider, {"wrapper": wrapper.get("id")}),
            )
    return normalized_provider(provider, "L18")


def validate_interop_boundary(boundary: dict[str, Any]) -> dict[str, Any]:
    span = boundary_span(boundary)
    missing = sorted(field for field in REQUIRED_INTEROP_FIELDS if not boundary.get(field))
    if missing:
        raise ExtensionHookError(
            "L19-BOUNDARY-INCOMPLETE",
            f"foreign boundary is missing required metadata: {missing}",
            span,
            "Declare ABI or protocol, types, ownership, effects, capabilities, errors, memory, threading, safety, profile, and provider metadata.",
            boundary_details(boundary),
        )
    if boundary.get("boundary_kind") == ":host-bridge" and set(boundary.get("profiles", [])) & HOST_LEAK_PROFILES:
        raise ExtensionHookError(
            "L19-HOST-LEAK",
            "host bridge is exposed to a portable or constrained profile",
            span,
            "Restrict hosted bridge behavior to profiles that can model the host runtime.",
            boundary_details(boundary),
        )
    if boundary.get("safety") == ":safe" and not boundary.get("safe_wrapper"):
        raise ExtensionHookError(
            "L19-SAFE-WRAPPER",
            "safe foreign boundary lacks wrapper invariants",
            span,
            "Attach a safe wrapper audit record with invariant evidence.",
            boundary_details(boundary),
        )
    record = copy.deepcopy(boundary)
    record["status"] = "accepted"
    return record


def normalized_provider(provider: dict[str, Any], governing_doc: str) -> dict[str, Any]:
    record = copy.deepcopy(provider)
    record["governing_doc"] = governing_doc
    record["status"] = "accepted"
    return record


def manifest_span(manifest: dict[str, Any]) -> dict[str, Any]:
    return manifest.get("span", {"source": manifest.get("source", "<manifest>"), "start_line": 1, "start_column": 1, "end_line": 1, "end_column": 1})


def provider_span(provider: dict[str, Any]) -> dict[str, Any]:
    return provider.get("span", {"source": provider.get("id", "<provider>"), "start_line": 1, "start_column": 1, "end_line": 1, "end_column": 1})


def boundary_span(boundary: dict[str, Any]) -> dict[str, Any]:
    return boundary.get("span", {"source": boundary.get("id", "<boundary>"), "start_line": 1, "start_column": 1, "end_line": 1, "end_column": 1})


def provider_details(provider: dict[str, Any], extra: dict[str, Any] | None = None) -> dict[str, Any]:
    details = {
        "provider_id": provider.get("id"),
        "provider_version": provider.get("version"),
        "active_profile": provider.get("profiles", [None])[0] if provider.get("profiles") else None,
        "build_effects": provider.get("build_effects", []),
        "generated_origin_chain": provider.get("generated_origin_chain", []),
    }
    if extra:
        details.update(extra)
    return details


def boundary_details(boundary: dict[str, Any]) -> dict[str, Any]:
    return {
        "boundary_id": boundary.get("id"),
        "foreign_source": boundary.get("foreign_source"),
        "active_profile": boundary.get("profiles", [None])[0] if boundary.get("profiles") else None,
        "provider_id": boundary.get("provider"),
        "type_mapping": boundary.get("type_mapping"),
        "ownership": boundary.get("ownership"),
        "effects": boundary.get("effects", []),
        "capabilities": boundary.get("capabilities", []),
    }


def load_extension_manifest(path: Path) -> dict[str, Any]:
    return json.loads(path.read_text(encoding="utf-8"))


def validate_extension_manifest_file(path: Path, root: Path) -> dict[str, Any]:
    return validate_extension_manifest(load_extension_manifest(path), root)


def extension_manifest_diagnostic(path: Path, root: Path) -> dict[str, Any] | None:
    try:
        validate_extension_manifest_file(path, root)
    except ExtensionHookError as exc:
        return exc.to_diagnostic()
    return None
