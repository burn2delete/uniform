"""L16 alternative macro provider contract validation."""

from __future__ import annotations

import copy
import json
from pathlib import Path
from typing import Any


REQUIRED_SYNTAX_GUARANTEES = {
    "kind",
    "children",
    "literal-values",
    "source-span",
    "namespace-context",
    "lexical-context",
    "metadata",
    "attach-metadata",
    "fresh-identifiers",
    "hygiene-comparison",
    "explicit-capture",
    "generated-origin-chain",
    "serialization",
}

REQUIRED_PHASES = {
    "read-time-syntax",
    "namespace-resolution",
    "macro-loading",
    "macro-invocation",
    "generated-syntax-validation",
    "fixed-point-re-expansion",
    "typed-effected-validation",
}

REQUIRED_CACHE_INVALIDATIONS = {
    "source",
    "provider",
    "profile",
    "target",
    "facet",
    "grant",
    "compiler-version",
}


class AlternativeMacroError(Exception):
    def __init__(
        self,
        code: str,
        message: str,
        span: dict[str, Any],
        remediation: str,
        *,
        provider_id: str | None = None,
        provider_version: str | None = None,
        macro_symbol: str | None = None,
        expansion_phase: str | None = None,
        active_profile: str | None = None,
        generated_origin_chain: list[dict[str, Any]] | None = None,
        build_effects: list[str] | None = None,
        equivalent_l4_rule: str | None = None,
    ) -> None:
        super().__init__(message)
        self.code = code
        self.message = message
        self.span = span
        self.remediation = remediation
        self.provider_id = provider_id
        self.provider_version = provider_version
        self.macro_symbol = macro_symbol
        self.expansion_phase = expansion_phase
        self.active_profile = active_profile
        self.generated_origin_chain = generated_origin_chain or []
        self.build_effects = build_effects or []
        self.equivalent_l4_rule = equivalent_l4_rule

    def to_diagnostic(self) -> dict[str, Any]:
        return {
            "id": self.code,
            "message": self.message,
            "provider_id": self.provider_id,
            "provider_version": self.provider_version,
            "macro_symbol": self.macro_symbol,
            "expansion_phase": self.expansion_phase,
            "active_profile": self.active_profile,
            "span": self.span,
            "generated_origin_chain": self.generated_origin_chain,
            "build_effects": self.build_effects,
            "equivalent_l4_rule": self.equivalent_l4_rule,
            "remediation": self.remediation,
            "analyzer_stage": "alternative-macro-provider",
        }


def validate_alternative_macro_manifest_file(path: Path) -> dict[str, Any]:
    manifest = load_manifest_file(path)
    return validate_alternative_macro_manifest(manifest, str(path))


def alternative_macro_manifest_diagnostic(path: Path) -> dict[str, Any] | None:
    try:
        validate_alternative_macro_manifest_file(path)
    except AlternativeMacroError as exc:
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


def validate_alternative_macro_manifest(manifest: dict[str, Any], source: str) -> dict[str, Any]:
    if manifest.get("kind") != "alternative-macro-provider-manifest":
        raise AlternativeMacroError(
            "L16-PROVIDER",
            "alternative macro manifest has the wrong kind",
            span(source, ""),
            "Use kind alternative-macro-provider-manifest.",
        )

    provider = manifest.get("provider", {})
    provider_span = span(source, "/provider")
    provider_id = provider.get("id")
    provider_version = provider.get("version")
    active_profile = manifest.get("namespace", {}).get("profile")
    expansion = manifest.get("expansion_trace", {})
    macro_symbol = expansion.get("macro_symbol")
    expansion_phase = expansion.get("phase")
    origin_chain = expansion.get("generated_origin_chain", [])

    if provider.get("kind") != ":macro-system" or ":macro/expand" not in provider.get("implements", []):
        raise AlternativeMacroError(
            "L16-PROVIDER",
            "macro provider is missing, ambiguous, or unsupported",
            provider_span,
            "Select a provider with kind :macro-system that implements macro expansion.",
            provider_id=provider_id,
            provider_version=provider_version,
            active_profile=active_profile,
        )

    equivalence = manifest.get("reference_equivalence", {})
    if equivalence.get("result") != ":equivalent":
        raise AlternativeMacroError(
            "L16-EQUIVALENCE",
            "alternative expansion differs from the L4 reference contract",
            span(source, "/reference_equivalence"),
            "Run the L4 corpus and preserve expansion results up to allowed structural equivalence.",
            provider_id=provider_id,
            provider_version=provider_version,
            macro_symbol=macro_symbol,
            expansion_phase=expansion_phase,
            active_profile=active_profile,
            generated_origin_chain=origin_chain,
            equivalent_l4_rule=equivalence.get("l4_rule", "L4"),
        )

    missing_syntax = sorted(REQUIRED_SYNTAX_GUARANTEES - set(provider.get("syntax_object_guarantees", [])))
    if missing_syntax:
        raise AlternativeMacroError(
            "L16-SYNTAX-OBJECT",
            f"syntax object representation loses required observable data: {missing_syntax}",
            provider_span,
            "Expose source spans, metadata, hygiene context, generated origins, and serialization.",
            provider_id=provider_id,
            provider_version=provider_version,
            macro_symbol=macro_symbol,
            expansion_phase=expansion_phase,
            active_profile=active_profile,
            generated_origin_chain=origin_chain,
            equivalent_l4_rule="L1/L4 syntax object contract",
        )

    hygiene = manifest.get("hygiene_records", {})
    if provider.get("hygiene_mode") not in {":hygienic", ":explicit-unhygienic", ":compatibility"} or hygiene.get("hidden_capture"):
        raise AlternativeMacroError(
            "L16-HYGIENE",
            "macro engine performed hidden capture or invalid identifier comparison",
            span(source, "/hygiene_records"),
            "Use hygienic identifiers by default and record explicit capture operations.",
            provider_id=provider_id,
            provider_version=provider_version,
            macro_symbol=macro_symbol,
            expansion_phase=expansion_phase,
            active_profile=active_profile,
            generated_origin_chain=origin_chain,
            equivalent_l4_rule="L4 hygiene",
        )

    missing_phases = sorted(REQUIRED_PHASES - set(provider.get("phase_model", [])))
    if missing_phases or manifest.get("phase_records", {}).get("runtime_capture"):
        raise AlternativeMacroError(
            "L16-PHASE",
            "macro expansion phase model captured runtime-only values or omitted required phase boundaries",
            span(source, "/phase_records"),
            "Emit artifacts as if the required L16 phase boundaries exist and reject runtime capture.",
            provider_id=provider_id,
            provider_version=provider_version,
            macro_symbol=macro_symbol,
            expansion_phase=expansion_phase,
            active_profile=active_profile,
            generated_origin_chain=origin_chain,
            equivalent_l4_rule="L4 phase separation",
        )

    declared_effects = set(provider.get("build_effects", []))
    granted_effects = {grant.get("effect") for grant in provider.get("grants", [])}
    traced_effects = {event.get("effect") for event in manifest.get("build_effect_trace", [])}
    ungranted = sorted((declared_effects | traced_effects) - granted_effects)
    if ungranted:
        raise AlternativeMacroError(
            "L16-BUILD-EFFECT",
            f"macro expansion performs undeclared or ungranted build effects: {ungranted}",
            span(source, "/build_effect_trace"),
            "Declare, grant, trace, and replay every macro build effect.",
            provider_id=provider_id,
            provider_version=provider_version,
            macro_symbol=macro_symbol,
            expansion_phase=expansion_phase,
            active_profile=active_profile,
            generated_origin_chain=origin_chain,
            build_effects=ungranted,
            equivalent_l4_rule="L6/L12 build effects",
        )

    non_replayable = [
        event.get("effect")
        for event in manifest.get("build_effect_trace", [])
        if event.get("replay_policy") in {":none", ":ambient"} or not event.get("output_digest")
    ]
    if any(not grant.get("replayable") for grant in provider.get("grants", [])) or non_replayable:
        raise AlternativeMacroError(
            "L16-HERMETIC",
            "macro expansion cannot be replayed in hermetic mode",
            span(source, "/build_effect_trace"),
            "Record replayable inputs, output digests, and redaction policy for macro build effects.",
            provider_id=provider_id,
            provider_version=provider_version,
            macro_symbol=macro_symbol,
            expansion_phase=expansion_phase,
            active_profile=active_profile,
            generated_origin_chain=origin_chain,
            build_effects=sorted(traced_effects),
            equivalent_l4_rule="L12 hermetic replay",
        )

    cache = manifest.get("cache_decision", {})
    missing_invalidations = sorted(REQUIRED_CACHE_INVALIDATIONS - set(cache.get("invalidates_on", [])))
    if cache.get("reuse") and (not cache.get("inputs_match") or missing_invalidations):
        raise AlternativeMacroError(
            "L16-CACHE",
            "incremental macro cache entry was reused under incompatible inputs",
            span(source, "/cache_decision"),
            "Invalidate on source, provider, profile, target, facet, grant, and compiler-version changes.",
            provider_id=provider_id,
            provider_version=provider_version,
            macro_symbol=macro_symbol,
            expansion_phase=expansion_phase,
            active_profile=active_profile,
            generated_origin_chain=origin_chain,
            equivalent_l4_rule="L16 cache invalidation",
        )

    facet = manifest.get("facet_dispatch", {})
    if facet and not facet.get("uses_l14_pipeline"):
        raise AlternativeMacroError(
            "L16-FACET",
            "facet-aware macro dispatch bypasses the L14 facet system",
            span(source, "/facet_dispatch"),
            "Route facet forms through namespace-scoped activation, ambiguity checks, and domain IR validation.",
            provider_id=provider_id,
            provider_version=provider_version,
            macro_symbol=macro_symbol,
            expansion_phase=expansion_phase,
            active_profile=active_profile,
            generated_origin_chain=origin_chain,
            equivalent_l4_rule="L14 facet dispatch",
        )

    generated = manifest.get("generated_validation", {})
    if not all(generated.get(stage) for stage in ["syntax", "core", "typed_core", "effects", "capabilities", "memory", "profile", "safety"]):
        raise AlternativeMacroError(
            "L16-GENERATED",
            "generated syntax failed normal Gravity validation",
            span(source, "/generated_validation"),
            "Validate generated syntax through normal reader, core, type, effect, capability, memory, profile, and safety checks.",
            provider_id=provider_id,
            provider_version=provider_version,
            macro_symbol=macro_symbol,
            expansion_phase=expansion_phase,
            active_profile=active_profile,
            generated_origin_chain=origin_chain,
            equivalent_l4_rule="L4 generated-code validation",
        )

    return {
        "kind": "alternative-macro-provider-artifact",
        "document": "L16",
        "provider_declaration": provider,
        "expansion_trace": expansion,
        "syntax_object_serialization": manifest.get("syntax_object_serialization", {}),
        "hygiene_and_capture_records": hygiene,
        "build_effect_trace": manifest.get("build_effect_trace", []),
        "incremental_cache_decision": cache,
        "reference_equivalence_report": equivalence,
        "facet_dispatch_record": facet,
        "generated_validation": generated,
        "diagnostics": [],
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
