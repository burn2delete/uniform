#!/usr/bin/env python3
"""Validate Phase 00 D3 terminology boundary artifacts and fixtures."""

from __future__ import annotations

import argparse
import json
import re
import sys
from pathlib import Path
from typing import Any


ROOT = Path(__file__).resolve().parents[1]

DEFAULT_REGISTRY = ROOT / "docs/artifacts/phase-00/diagnostic-namespace-registry.json"
REQUIRED_CONCEPTS = {
    "profile",
    "target",
    "effect",
    "capability",
    "runtime",
    "backend",
    "artifact",
    "file",
    "unsafe-island",
}
REQUIRED_BOUNDARIES = {
    "profile-target": "D3-PROFILE-TARGET-CONFLATION",
    "effect-capability": "D3-EFFECT-CAPABILITY-CONFLATION",
    "runtime-backend": "D3-AMBIGUOUS-RUNTIME",
    "artifact-file": "D3-ARTIFACT-UNSTRUCTURED",
}
REQUIRED_D3_DIAGNOSTICS = {
    "D3-AMBIGUOUS-RUNTIME",
    "D3-PROFILE-TARGET-CONFLATION",
    "D3-EFFECT-CAPABILITY-CONFLATION",
    "D3-ARTIFACT-UNSTRUCTURED",
    "D3-UNSAFE-UNTRACKED",
}
ARTIFACT_IDENTITY_FIELDS = {
    "kind",
    "schema",
    "source_hash",
    "compiler_identity",
    "profile",
    "target",
    "effects",
    "capabilities",
    "safety_status",
    "provenance",
}
DIAGNOSTIC_RE = re.compile(r"^[A-Z][A-Z0-9]*-[A-Z0-9][A-Z0-9-]*$")


def load_json(path: Path) -> Any:
    try:
        return json.loads(path.read_text(encoding="utf-8"))
    except json.JSONDecodeError as exc:
        return {"_json_error": f"{path}:{exc.lineno}:{exc.colno}: {exc.msg}"}


def stable_list(value: Any) -> list[str]:
    if not isinstance(value, list):
        return []
    return [item for item in value if isinstance(item, str)]


def is_profile(value: Any, registry: dict[str, Any]) -> bool:
    return isinstance(value, str) and value in registry.get("known_profiles", [])


def is_target(value: Any, registry: dict[str, Any]) -> bool:
    return isinstance(value, str) and value in registry.get("known_targets", [])


def is_effect(value: Any, registry: dict[str, Any]) -> bool:
    return isinstance(value, str) and value in registry.get("known_effects", [])


def is_capability(value: Any, registry: dict[str, Any]) -> bool:
    return isinstance(value, str) and value in registry.get("known_capabilities", [])


def is_backend(value: Any, registry: dict[str, Any]) -> bool:
    return isinstance(value, str) and value in registry.get("known_backends", [])


def is_runtime(value: Any, registry: dict[str, Any]) -> bool:
    return isinstance(value, str) and value in registry.get("known_runtime_families", [])


def validate_registry(registry: Any) -> list[tuple[str, str]]:
    diagnostics: list[tuple[str, str]] = []
    if isinstance(registry, dict) and "_json_error" in registry:
        return [("D3-ARTIFACT-UNSTRUCTURED", registry["_json_error"])]
    if not isinstance(registry, dict):
        return [("D3-ARTIFACT-UNSTRUCTURED", "registry must be a JSON object")]
    if registry.get("kind") != "diagnostic-namespace-registry":
        diagnostics.append(("D3-ARTIFACT-UNSTRUCTURED", "kind must be diagnostic-namespace-registry"))

    concepts = set(registry.get("concepts", {}))
    missing_concepts = sorted(REQUIRED_CONCEPTS - concepts)
    if missing_concepts:
        diagnostics.append(("D3-ARTIFACT-UNSTRUCTURED", f"missing D3 concepts: {missing_concepts}"))

    boundaries = {
        item.get("id"): item.get("diagnostic")
        for item in registry.get("terminology_boundaries", [])
        if isinstance(item, dict)
    }
    for boundary_id, diagnostic_id in REQUIRED_BOUNDARIES.items():
        if boundaries.get(boundary_id) != diagnostic_id:
            diagnostics.append(("D3-ARTIFACT-UNSTRUCTURED", f"{boundary_id} must emit {diagnostic_id}"))

    namespace_diagnostics: set[str] = set()
    for namespace in registry.get("diagnostic_namespaces", []):
        if not isinstance(namespace, dict):
            continue
        namespace_diagnostics.update(stable_list(namespace.get("diagnostics")))
    missing_diagnostics = sorted(REQUIRED_D3_DIAGNOSTICS - namespace_diagnostics)
    if missing_diagnostics:
        diagnostics.append(("D3-ARTIFACT-UNSTRUCTURED", f"missing D3 diagnostics: {missing_diagnostics}"))
    for diagnostic_id in namespace_diagnostics:
        if not DIAGNOSTIC_RE.match(diagnostic_id):
            diagnostics.append(("D3-ARTIFACT-UNSTRUCTURED", f"unstable diagnostic id {diagnostic_id!r}"))

    for path in registry.get("source_basis", []):
        if isinstance(path, str) and path.endswith(".md") and not (ROOT / path).exists():
            diagnostics.append(("D3-ARTIFACT-UNSTRUCTURED", f"source basis path does not exist: {path}"))
    return diagnostics


def validate_fixture(fixture: Any, registry: dict[str, Any]) -> list[tuple[str, str]]:
    diagnostics: list[tuple[str, str]] = []
    if isinstance(fixture, dict) and "_json_error" in fixture:
        return [("D3-ARTIFACT-UNSTRUCTURED", fixture["_json_error"])]
    if not isinstance(fixture, dict):
        return [("D3-ARTIFACT-UNSTRUCTURED", "fixture must be a JSON object")]
    if fixture.get("kind") != "terminology-boundary-fixture":
        diagnostics.append(("D3-ARTIFACT-UNSTRUCTURED", "kind must be terminology-boundary-fixture"))

    records = fixture.get("records")
    if not isinstance(records, list) or not records:
        return diagnostics + [("D3-ARTIFACT-UNSTRUCTURED", "fixture records must be non-empty")]

    for offset, record in enumerate(records):
        if not isinstance(record, dict):
            diagnostics.append(("D3-ARTIFACT-UNSTRUCTURED", f"record {offset} must be an object"))
            continue
        record_id = str(record.get("id", offset))

        profile = record.get("profile")
        target = record.get("target")
        if profile is not None and is_target(profile, registry):
            diagnostics.append(("D3-PROFILE-TARGET-CONFLATION", f"{record_id} uses target {profile!r} as profile"))
        if target is not None and is_profile(target, registry):
            diagnostics.append(("D3-PROFILE-TARGET-CONFLATION", f"{record_id} uses profile {target!r} as target"))

        for effect in stable_list(record.get("effects")) + stable_list(record.get("declared_effects")):
            if is_capability(effect, registry):
                diagnostics.append(("D3-EFFECT-CAPABILITY-CONFLATION", f"{record_id} uses capability {effect!r} as effect"))
        for capability in stable_list(record.get("capabilities")) + stable_list(record.get("required_capabilities")):
            if is_effect(capability, registry):
                diagnostics.append(("D3-EFFECT-CAPABILITY-CONFLATION", f"{record_id} uses effect {capability!r} as capability"))

        runtime = record.get("runtime")
        backend = record.get("backend")
        if runtime is not None and is_backend(runtime, registry):
            diagnostics.append(("D3-AMBIGUOUS-RUNTIME", f"{record_id} uses backend {runtime!r} as runtime"))
        if backend is not None and is_runtime(backend, registry):
            diagnostics.append(("D3-AMBIGUOUS-RUNTIME", f"{record_id} uses runtime family {backend!r} as backend"))
        if record.get("uses_runtime_term") is True and not record.get("runtime_family"):
            diagnostics.append(("D3-AMBIGUOUS-RUNTIME", f"{record_id} uses runtime without family"))

        if record.get("concept") == "artifact":
            missing_identity = sorted(ARTIFACT_IDENTITY_FIELDS - set(record))
            if missing_identity:
                diagnostics.append(
                    (
                        "D3-ARTIFACT-UNSTRUCTURED",
                        f"{record_id} artifact is missing identity fields: {missing_identity}",
                    )
                )

        if record.get("unsafe_behavior") is True and not (record.get("unsafe_island") or record.get("audit_artifact")):
            diagnostics.append(("D3-UNSAFE-UNTRACKED", f"{record_id} describes unsafe behavior without audit tracking"))

        for diagnostic_id in stable_list(record.get("diagnostics")):
            if diagnostic_id not in REQUIRED_D3_DIAGNOSTICS or not DIAGNOSTIC_RE.match(diagnostic_id):
                diagnostics.append(("D3-ARTIFACT-UNSTRUCTURED", f"{record_id} has unstable or unknown diagnostic {diagnostic_id!r}"))

    return diagnostics


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("artifact", type=Path)
    parser.add_argument("--registry", type=Path, default=DEFAULT_REGISTRY)
    parser.add_argument("--expect-failure")
    args = parser.parse_args()

    artifact = load_json(args.artifact)
    if isinstance(artifact, dict) and artifact.get("kind") == "diagnostic-namespace-registry":
        diagnostics = validate_registry(artifact)
        output = "terminology registry validation passed"
    else:
        registry = load_json(args.registry)
        registry_diagnostics = validate_registry(registry)
        if registry_diagnostics:
            diagnostics = registry_diagnostics
        else:
            diagnostics = validate_fixture(artifact, registry)
        record_count = len(artifact.get("records", [])) if isinstance(artifact, dict) else 0
        output = f"terminology fixture validation passed: {record_count} records"

    if args.expect_failure:
        for code, message in diagnostics:
            if code == args.expect_failure:
                print(f"expected diagnostic observed: {code}")
                print(message)
                return 0
        observed = ", ".join(code for code, _ in diagnostics) or "none"
        print(
            f"expected diagnostic {args.expect_failure} was not observed; observed: {observed}",
            file=sys.stderr,
        )
        return 1

    if diagnostics:
        for code, message in diagnostics:
            print(f"{code}: {message}", file=sys.stderr)
        return 1

    print(output)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
