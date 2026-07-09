"""L15 capability provider declaration, grant, and selection validation."""

from __future__ import annotations

import copy
import json
from pathlib import Path
from typing import Any


REQUIRED_PROVIDER_FIELDS = {
    "id",
    "version",
    "implements",
    "profiles",
    "targets",
    "runtime_effects",
    "build_effects",
    "contracts",
    "failure_types",
    "blocking",
    "concurrency",
    "resource_memory",
    "determinism",
    "replayable",
    "artifact_schema",
    "conformance",
    "trust",
}

REVOCATION_PROFILES = {":hosted", ":distributed"}


class ProviderError(Exception):
    def __init__(
        self,
        code: str,
        message: str,
        span: dict[str, Any],
        remediation: str,
        *,
        requested_capability: str | None = None,
        selected_provider: str | None = None,
        grant_id: str | None = None,
        scope: dict[str, Any] | None = None,
        phase: str | None = None,
        active_profile: str | None = None,
        target: str | None = None,
        nearest_valid_provider: str | None = None,
        nearest_valid_grant: str | None = None,
    ) -> None:
        super().__init__(message)
        self.code = code
        self.message = message
        self.span = span
        self.remediation = remediation
        self.requested_capability = requested_capability
        self.selected_provider = selected_provider
        self.grant_id = grant_id
        self.scope = scope or {}
        self.phase = phase
        self.active_profile = active_profile
        self.target = target
        self.nearest_valid_provider = nearest_valid_provider
        self.nearest_valid_grant = nearest_valid_grant

    def to_diagnostic(self) -> dict[str, Any]:
        return {
            "id": self.code,
            "message": self.message,
            "requested_capability": self.requested_capability,
            "selected_provider": self.selected_provider,
            "grant_id": self.grant_id,
            "scope": self.scope,
            "phase": self.phase,
            "active_profile": self.active_profile,
            "target": self.target,
            "nearest_valid_provider": self.nearest_valid_provider,
            "nearest_valid_grant": self.nearest_valid_grant,
            "span": self.span,
            "remediation": self.remediation,
            "analyzer_stage": "capability-provider",
        }


def validate_provider_manifest_file(path: Path) -> dict[str, Any]:
    manifest = load_manifest_file(path)
    return validate_provider_manifest(manifest, str(path))


def provider_manifest_diagnostic(path: Path) -> dict[str, Any] | None:
    try:
        validate_provider_manifest_file(path)
    except ProviderError as exc:
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


def validate_provider_manifest(manifest: dict[str, Any], source: str) -> dict[str, Any]:
    if manifest.get("kind") != "capability-provider-manifest":
        raise ProviderError(
            "L15-CONTRACT",
            "provider manifest has the wrong kind",
            span(source, ""),
            "Use kind capability-provider-manifest.",
        )

    providers = {provider["id"]: provider for provider in manifest.get("providers", [])}
    grants = manifest.get("grants", [])

    for index, provider in enumerate(manifest.get("providers", [])):
        validate_provider_declaration(provider, source, index)

    selection_records = []
    scope_audit_logs = []
    replay_records = []
    runtime_manifests = []

    for index, request in enumerate(manifest.get("selection_requests", [])):
        selected, grant = validate_selection_request(request, providers, grants, source, index)
        selection_records.append(
            {
                "request_id": request.get("id"),
                "capability": request.get("capability"),
                "provider": selected["id"],
                "provider_version": selected.get("version"),
                "selection_source": request.get("selection_source"),
                "selection_order": request.get("selection_order", []),
                "phase": request.get("phase"),
                "profile": request.get("active_profile"),
                "target": request.get("target"),
                "trust": selected.get("trust"),
            }
        )
        scope_audit_logs.append(
            {
                "request_id": request.get("id"),
                "grant_id": grant.get("id"),
                "principal": request.get("principal"),
                "capability": request.get("capability"),
                "provider": selected["id"],
                "scope": request.get("scope", {}),
                "phase": request.get("phase"),
                "audit": grant.get("audit"),
            }
        )
        if request.get("phase") == ":build":
            replay_records.append(
                {
                    "request_id": request.get("id"),
                    "provider": selected["id"],
                    "replayable": selected.get("replayable"),
                    "output_digest": request.get("provider_output", {}).get("digest"),
                    "redaction_policy": request.get("provider_output", {}).get("redaction_policy"),
                }
            )
        if request.get("phase") == ":runtime":
            runtime_manifests.append(
                {
                    "request_id": request.get("id"),
                    "provider": selected["id"],
                    "provider_version": selected.get("version"),
                    "scope": request.get("scope", {}),
                    "contract": selected.get("contracts", []),
                    "profile": request.get("active_profile"),
                    "target": request.get("target"),
                }
            )

    return {
        "kind": "capability-provider-artifact",
        "document": "L15",
        "provider_declaration_records": [provider_record(provider) for provider in manifest.get("providers", [])],
        "grant_records": grants,
        "provider_selection_records": selection_records,
        "capability_scope_audit_logs": scope_audit_logs,
        "compile_time_replay_records": replay_records,
        "runtime_provider_manifests": runtime_manifests,
        "provider_conformance_results": [
            {"provider": provider["id"], "suite": provider.get("conformance", {}).get("suite"), "status": provider.get("conformance", {}).get("status")}
            for provider in manifest.get("providers", [])
        ],
        "diagnostics": [],
    }


def validate_provider_declaration(provider: dict[str, Any], source: str, index: int) -> None:
    missing = sorted(REQUIRED_PROVIDER_FIELDS - set(provider))
    if missing:
        raise ProviderError(
            "L15-CONTRACT",
            f"provider declaration is missing required fields: {missing}",
            span(source, f"/providers/{index}"),
            "Declare provider capabilities, effects, profiles, contracts, artifacts, trust, and conformance.",
            selected_provider=provider.get("id"),
        )
    if provider.get("conformance", {}).get("status") != ":passed":
        raise ProviderError(
            "L15-CONTRACT",
            "provider declaration failed its contract suite",
            span(source, f"/providers/{index}/conformance"),
            "Fix the provider or remove it from selection until conformance passes.",
            selected_provider=provider.get("id"),
            nearest_valid_provider=None,
        )


def validate_selection_request(
    request: dict[str, Any],
    providers: dict[str, dict[str, Any]],
    grants: list[dict[str, Any]],
    source: str,
    index: int,
) -> tuple[dict[str, Any], dict[str, Any]]:
    request_span = span(source, f"/selection_requests/{index}")
    capability = request.get("capability")
    selected_id = request.get("selected_provider")
    active_profile = request.get("active_profile")
    target = request.get("target")
    phase = request.get("phase")
    provider_candidates = [provider for provider in providers.values() if capability in provider.get("implements", [])]

    if selected_id:
        selected = providers.get(selected_id)
        if not selected or capability not in selected.get("implements", []):
            raise ProviderError(
                "L15-PROVIDER-MISSING",
                "no provider implements the requested capability",
                request_span,
                "Select a provider that implements the capability or add one to package metadata.",
                requested_capability=capability,
                selected_provider=selected_id,
                phase=phase,
                active_profile=active_profile,
                target=target,
                nearest_valid_provider=provider_candidates[0]["id"] if provider_candidates else None,
            )
    else:
        viable = [
            provider
            for provider in provider_candidates
            if active_profile in provider.get("profiles", []) and target in provider.get("targets", [])
        ]
        if not viable:
            raise ProviderError(
                "L15-PROVIDER-MISSING",
                "no provider implements the requested capability for the active profile and target",
                request_span,
                "Add a provider declaration or narrow the requested capability.",
                requested_capability=capability,
                phase=phase,
                active_profile=active_profile,
                target=target,
            )
        if len(viable) > 1:
            raise ProviderError(
                "L15-PROVIDER-AMBIGUOUS",
                "provider selection has multiple valid candidates and no deterministic ordering rule",
                request_span,
                "Select a provider through source annotation, package manifest, workspace policy, profile default, or compiler default.",
                requested_capability=capability,
                phase=phase,
                active_profile=active_profile,
                target=target,
                nearest_valid_provider=viable[0]["id"],
            )
        selected = viable[0]

    grant = nearest_grant(request, selected, grants)
    if grant is None:
        raise ProviderError(
            "L15-CAPABILITY-MISSING",
            "code requires authority that is not granted",
            request_span,
            "Add an explicit capability value, ambient provider declaration, or scoped grant.",
            requested_capability=capability,
            selected_provider=selected["id"],
            phase=phase,
            active_profile=active_profile,
            target=target,
            scope=request.get("scope", {}),
        )

    if active_profile not in selected.get("profiles", []) or target not in selected.get("targets", []):
        raise ProviderError(
            "L15-PROFILE",
            "selected provider is unsupported by the active profile or target",
            request_span,
            "Select a profile-compatible provider or gate the source.",
            requested_capability=capability,
            selected_provider=selected["id"],
            grant_id=grant.get("id"),
            phase=phase,
            active_profile=active_profile,
            target=target,
            nearest_valid_grant=grant.get("id"),
        )

    if grant.get("phase") != phase:
        raise ProviderError(
            "L15-PHASE",
            "build authority is used at runtime or runtime authority is used during compilation",
            request_span,
            "Use a grant whose phase matches the operation.",
            requested_capability=capability,
            selected_provider=selected["id"],
            grant_id=grant.get("id"),
            phase=phase,
            active_profile=active_profile,
            target=target,
            scope=request.get("scope", {}),
            nearest_valid_grant=grant.get("id"),
        )

    if not scope_allows(grant.get("scope", {}), request.get("scope", {})):
        raise ProviderError(
            "L15-SCOPE",
            "requested operation exceeds the grant scope",
            request_span,
            "Attenuate the request to the granted scope or issue a narrower matching grant.",
            requested_capability=capability,
            selected_provider=selected["id"],
            grant_id=grant.get("id"),
            scope=request.get("scope", {}),
            phase=phase,
            active_profile=active_profile,
            target=target,
            nearest_valid_grant=grant.get("id"),
        )

    if selected.get("trust") not in set(request.get("allowed_trust", [])):
        raise ProviderError(
            "L15-TRUST",
            "provider trust level violates policy",
            request_span,
            "Use a provider accepted by package policy or isolate the provider explicitly.",
            requested_capability=capability,
            selected_provider=selected["id"],
            grant_id=grant.get("id"),
            scope=request.get("scope", {}),
            phase=phase,
            active_profile=active_profile,
            target=target,
            nearest_valid_grant=grant.get("id"),
        )

    if phase == ":build" and request.get("requires_replay") and not selected.get("replayable"):
        raise ProviderError(
            "L15-REPLAY",
            "compile-time provider cannot satisfy replay requirements",
            request_span,
            "Use a replayable provider or record an explicit policy exception.",
            requested_capability=capability,
            selected_provider=selected["id"],
            grant_id=grant.get("id"),
            scope=request.get("scope", {}),
            phase=phase,
            active_profile=active_profile,
            target=target,
            nearest_valid_grant=grant.get("id"),
        )

    provider_output = request.get("provider_output", {})
    if provider_output.get("contains_secret") and provider_output.get("redaction_policy") != ":redacted":
        raise ProviderError(
            "L15-SECRET",
            "provider output would leak secret material",
            request_span,
            "Record only secret names and redacted presence markers in public artifacts.",
            requested_capability=capability,
            selected_provider=selected["id"],
            grant_id=grant.get("id"),
            scope=request.get("scope", {}),
            phase=phase,
            active_profile=active_profile,
            target=target,
            nearest_valid_grant=grant.get("id"),
        )

    if request.get("revocation_required") and (active_profile not in REVOCATION_PROFILES or not selected.get("revocation_supported")):
        raise ProviderError(
            "L15-REVOCATION",
            "code assumes revocation in a profile or provider that cannot provide it",
            request_span,
            "Declare a static lifetime requirement or select a revocable provider in a supported profile.",
            requested_capability=capability,
            selected_provider=selected["id"],
            grant_id=grant.get("id"),
            scope=request.get("scope", {}),
            phase=phase,
            active_profile=active_profile,
            target=target,
            nearest_valid_grant=grant.get("id"),
        )

    return selected, grant


def nearest_grant(request: dict[str, Any], provider: dict[str, Any], grants: list[dict[str, Any]]) -> dict[str, Any] | None:
    for grant in grants:
        if (
            grant.get("principal") == request.get("principal")
            and grant.get("capability") == request.get("capability")
            and grant.get("provider") == provider.get("id")
        ):
            return grant
    return None


def scope_allows(grant_scope: dict[str, Any], request_scope: dict[str, Any]) -> bool:
    if "paths" in grant_scope and "path" in request_scope:
        path = request_scope["path"]
        for pattern in grant_scope["paths"]:
            if pattern.endswith("*") and path.startswith(pattern[:-1]):
                return True
            if pattern == path:
                return True
        return False
    if "models" in grant_scope and "model" in request_scope:
        return request_scope["model"] in grant_scope["models"]
    if "regions" in grant_scope and "region" in request_scope:
        return request_scope["region"] in grant_scope["regions"]
    if "functions" in grant_scope and "function" in request_scope:
        return request_scope["function"] in grant_scope["functions"]
    return grant_scope == request_scope or grant_scope.get("all") is True


def provider_record(provider: dict[str, Any]) -> dict[str, Any]:
    return {
        "id": provider["id"],
        "version": provider.get("version"),
        "implements": provider.get("implements", []),
        "profiles": provider.get("profiles", []),
        "targets": provider.get("targets", []),
        "runtime_effects": provider.get("runtime_effects", []),
        "build_effects": provider.get("build_effects", []),
        "contracts": provider.get("contracts", []),
        "failure_types": provider.get("failure_types", []),
        "blocking": provider.get("blocking"),
        "concurrency": provider.get("concurrency"),
        "resource_memory": provider.get("resource_memory"),
        "determinism": provider.get("determinism"),
        "replayable": provider.get("replayable"),
        "artifact_schema": provider.get("artifact_schema"),
        "trust": provider.get("trust"),
        "revocation_supported": provider.get("revocation_supported", False),
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
