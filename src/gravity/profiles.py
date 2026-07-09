"""Phase 03 profile manifest and compatibility validation."""

from __future__ import annotations

import hashlib
import json
from pathlib import Path
from typing import Any


STANDARD_PROFILES = {
    ":core",
    ":meta",
    ":hosted",
    ":native",
    ":firmware",
    ":kernel",
    ":hardware",
    ":distributed",
    ":ai",
    ":gpu",
    ":formal",
}
REQUIRED_PROFILE_FIELDS = {
    "name",
    "document",
    "allowed_forms",
    "allowed_effects",
    "checked_effects",
    "forbidden_effects",
    "capabilities",
    "memory",
    "runtime",
    "unsafe_policy",
    "artifact_boundaries",
    "target_permissions",
}
PROFILE_DIAGNOSTICS = {
    ":core": {
        "effect": "P2-EFFECT",
        "capability": "P2-CAPABILITY",
        "memory": "P2-MEMORY",
        "runtime": "P2-RUNTIME",
        "unsafe": "P2-UNSAFE",
        "nondeterminism": "P2-NONDETERMINISM",
        "macro": "P2-MACRO",
        "import": "P2-IMPORT",
        "backend": "P2-BACKEND",
    },
    ":meta": {
        "build_effect": "P3-BUILD-EFFECT",
        "hermetic": "P3-HERMETIC",
        "compiler_capability": "P3-COMPILER-CAPABILITY",
        "pass_contract": "P3-PASS-CONTRACT",
        "generated_profile": "P3-GENERATED-PROFILE",
        "phase": "P3-PHASE",
    },
    ":hosted": {
        "effect": "P4-HOST-EFFECT",
        "capability": "P4-HOST-CAPABILITY",
        "reflection": "P4-REFLECTION",
        "dynamic": "P4-DYNAMIC",
        "host_object": "P4-HOST-OBJECT",
        "exception": "P4-EXCEPTION",
        "resource": "P4-RESOURCE",
        "raw_memory": "P4-RAW-MEMORY",
        "cross_import": "P4-CROSS-IMPORT",
        "sourcemap": "P4-SOURCEMAP",
    },
    ":native": {
        "alloc": "P5-ALLOC",
        "memory_provider": "P5-MEMORY-PROVIDER",
        "ffi": "P5-FFI",
        "raw_memory": "P5-RAW-MEMORY",
        "thread": "P5-THREAD",
        "atomic": "P5-ATOMIC",
        "simd": "P5-SIMD",
        "numeric": "P5-NUMERIC",
        "optimization": "P5-OPTIMIZATION",
        "runtime": "P5-RUNTIME",
    },
    ":firmware": {
        "gc": "P6-GC",
        "alloc": "P6-ALLOC",
        "stack": "P6-STACK",
        "static": "P6-STATIC",
        "mmio": "P6-MMIO",
        "interrupt": "P6-INTERRUPT",
        "latency": "P6-LATENCY",
        "host": "P6-HOST",
        "exception": "P6-EXCEPTION",
        "capability": "P6-CAPABILITY",
    },
    ":kernel": {
        "hidden_alloc": "P7-HIDDEN-ALLOC",
        "gc": "P7-GC",
        "raw_memory": "P7-RAW-MEMORY",
        "mmio": "P7-MMIO",
        "interrupt": "P7-INTERRUPT",
        "scheduler": "P7-SCHEDULER",
        "atomic": "P7-ATOMIC",
        "exception": "P7-EXCEPTION",
        "abi": "P7-ABI",
        "authority": "P7-AUTHORITY",
    },
    ":hardware": {
        "width": "P8-WIDTH",
        "clock": "P8-CLOCK",
        "reset": "P8-RESET",
        "cdc": "P8-CDC",
        "unbounded": "P8-UNBOUNDED",
        "runtime": "P8-RUNTIME",
        "port": "P8-PORT",
        "numeric": "P8-NUMERIC",
        "timing": "P8-TIMING",
        "target": "P8-TARGET",
        "capability": "P8-CAPABILITY",
        "tag": "P8-TAG",
        "compartment": "P8-COMPARTMENT",
        "temporal": "P8-TEMPORAL",
        "synthesis": "P8-SYNTHESIS",
    },
    ":distributed": {
        "replay": "P9-REPLAY",
        "schema": "P9-SCHEMA",
        "migration": "P9-MIGRATION",
        "retry": "P9-RETRY",
        "compensation": "P9-COMPENSATION",
        "capability": "P9-CAPABILITY",
        "effect": "P9-EFFECT",
        "raw": "P9-RAW",
        "service_error": "P9-SERVICE-ERROR",
        "event_log": "P9-EVENT-LOG",
    },
    ":ai": {
        "model": "P10-MODEL",
        "tool": "P10-TOOL",
        "prompt": "P10-PROMPT",
        "memory": "P10-MEMORY",
        "secret": "P10-SECRET",
        "generated": "P10-GENERATED",
        "replay": "P10-REPLAY",
        "budget": "P10-BUDGET",
        "destructive": "P10-DESTRUCTIVE",
        "raw": "P10-RAW",
    },
    ":gpu": {
        "host_effect": "P11-HOST-EFFECT",
        "device_memory": "P11-DEVICE-MEMORY",
        "transfer": "P11-TRANSFER",
        "sync": "P11-SYNC",
        "alias": "P11-ALIAS",
        "target_feature": "P11-TARGET-FEATURE",
        "launch": "P11-LAUNCH",
        "math": "P11-MATH",
        "raw": "P11-RAW",
        "boundary": "P11-BOUNDARY",
    },
    ":formal": {
        "nondeterminism": "P12-NONDETERMINISM",
        "effect": "P12-EFFECT",
        "math_mode": "P12-MATH-MODE",
        "assumption": "P12-ASSUMPTION",
        "proof": "P12-PROOF",
        "certificate": "P12-CERTIFICATE",
        "trust": "P12-TRUST",
        "unsafe": "P12-UNSAFE",
        "symbolic_lowering": "P12-SYMBOLIC-LOWERING",
        "backend": "P12-BACKEND",
    },
}
DIRECT_IMPORTS = {
    ":core": {":core"},
    ":meta": {":core", ":meta"},
    ":hosted": {":core", ":hosted"},
    ":native": {":core", ":native"},
    ":firmware": {":core", ":firmware"},
    ":kernel": {":core", ":kernel"},
    ":hardware": {":core", ":hardware"},
    ":distributed": {":core", ":distributed"},
    ":ai": {":core", ":distributed", ":ai"},
    ":gpu": {":core", ":gpu"},
    ":formal": {":core", ":formal"},
}


class ProfileValidationError(Exception):
    def __init__(
        self,
        code: str,
        message: str,
        *,
        namespace: dict[str, Any] | None = None,
        edge: dict[str, Any] | None = None,
        source: str,
        missing_fact: str,
        remediation: str,
    ) -> None:
        super().__init__(message)
        self.code = code
        self.message = message
        self.namespace = namespace or {}
        self.edge = edge or {}
        self.source = source
        self.missing_fact = missing_fact
        self.remediation = remediation

    def to_diagnostic(self) -> dict[str, Any]:
        consumer = self.edge.get("consumer", {})
        producer = self.edge.get("producer", {})
        return {
            "id": self.code,
            "message": self.message,
            "namespace": self.namespace.get("name") or consumer.get("namespace"),
            "active_profile": self.namespace.get("active_profile") or consumer.get("profile"),
            "target": self.namespace.get("target"),
            "source_span": self.namespace.get("source_span") or self.edge.get("source_span") or {"source": self.source},
            "generated_origin": self.namespace.get("generated_origin", []) or self.edge.get("generated_origin", []),
            "requested_effect": first(self.namespace.get("requested_effects", [])),
            "requested_capability": first(self.namespace.get("requested_capabilities", [])),
            "consumer_profile": consumer.get("profile"),
            "producer_profile": producer.get("profile"),
            "edge_kind": self.edge.get("edge"),
            "policy_layer": self.namespace.get("policy_layer") or self.edge.get("policy_layer"),
            "missing_fact": self.missing_fact,
            "remediation": self.remediation,
            "analyzer_stage": "profile-validation",
        }


def validate_profile_file(path: Path) -> dict[str, Any]:
    return validate_profile_manifest(load_manifest_file(path), str(path))


def profile_diagnostic(path: Path) -> dict[str, Any] | None:
    try:
        validate_profile_file(path)
    except ProfileValidationError as exc:
        return exc.to_diagnostic()
    return None


def validate_profile_manifest(manifest: dict[str, Any], source: str) -> dict[str, Any]:
    if manifest.get("kind") != "profile-system-input":
        raise ProfileValidationError(
            "P1-MISSING-PROFILE",
            "profile input has the wrong artifact kind",
            source=source,
            missing_fact="profile-system-input",
            remediation="Feed Phase 03 a profile-system-input manifest.",
        )
    profiles = {profile.get("name"): profile for profile in manifest.get("profiles", [])}
    missing_profiles = sorted(STANDARD_PROFILES - set(profiles))
    if missing_profiles:
        raise ProfileValidationError(
            "P1-MISSING-PROFILE",
            f"profile manifest set is missing standard profiles: {missing_profiles}",
            source=source,
            missing_fact=",".join(missing_profiles),
            remediation="Declare every standard profile manifest before validating namespaces.",
        )
    for profile in profiles.values():
        validate_profile_schema(profile, source)
    namespace_reports = [validate_namespace(namespace, profiles, source) for namespace in manifest.get("namespaces", [])]
    edge_reports = [validate_edge(edge, profiles, source) for edge in manifest.get("imports", [])]
    backend_reports = [validate_backend_report(report, profiles, source) for report in manifest.get("backend_eligibility", [])]
    fixture_results = validate_compliance_results(manifest.get("compliance_fixtures", []), source)

    return {
        "kind": "profile-system-artifact",
        "phase": "03",
        "profile_manifests": list(profiles.values()),
        "effect_permission_table": effect_table(profiles),
        "capability_permission_table": capability_table(profiles),
        "memory_regime_records": memory_records(profiles),
        "runtime_assumption_records": runtime_records(profiles),
        "cross_profile_dependency_graph": edge_reports,
        "profile_compatibility_matrix": compatibility_matrix(),
        "backend_eligibility_reports": backend_reports,
        "profile_conformance_fixture_results": fixture_results,
        "namespace_validation_reports": namespace_reports,
        "input_hash": artifact_hash(manifest),
        "diagnostics": [],
    }


def validate_profile_schema(profile: dict[str, Any], source: str) -> None:
    missing = sorted(REQUIRED_PROFILE_FIELDS - set(profile))
    if missing:
        raise ProfileValidationError(
            "P1-MISSING-PROFILE",
            f"profile manifest lacks required fields: {missing}",
            source=source,
            missing_fact=",".join(missing),
            remediation="Profile manifests must expose effects, capabilities, memory, runtime, unsafe policy, artifacts, and target permissions.",
        )
    if profile["name"] not in STANDARD_PROFILES:
        raise ProfileValidationError(
            "P1-MISSING-PROFILE",
            f"profile manifest uses unknown profile {profile['name']}",
            source=source,
            missing_fact="standard-profile-name",
            remediation="Use one of the standard Phase 03 profile names.",
        )


def validate_namespace(namespace: dict[str, Any], profiles: dict[str, dict[str, Any]], source: str) -> dict[str, Any]:
    active = namespace.get("active_profile")
    if namespace.get("active_profiles") or not active:
        code = "P1-AMBIGUOUS-PROFILE" if namespace.get("active_profiles") else "P1-MISSING-PROFILE"
        raise_ns(code, "namespace lacks exactly one active profile", namespace, source, "single-active-profile")
    if active not in profiles:
        raise_ns("P1-MISSING-PROFILE", "namespace references an undeclared profile", namespace, source, "declared-profile")
    profile = profiles[active]
    requested_effects = set(namespace.get("requested_effects", []))
    requested_capabilities = set(namespace.get("requested_capabilities", []))
    allowed_effects = set(profile.get("allowed_effects", []))
    checked_effects = set(profile.get("checked_effects", []))
    forbidden_effects = set(profile.get("forbidden_effects", []))
    denied_effects = sorted(requested_effects & forbidden_effects)
    unknown_effects = sorted(requested_effects - allowed_effects - checked_effects)
    if denied_effects or unknown_effects:
        raise_ns(profile_effect_code(active), "namespace requests effects outside its effective profile set", namespace, source, "profile-effect-set")
    missing_checked_evidence = sorted(effect for effect in requested_effects & checked_effects if effect not in namespace.get("evidence_records", []))
    if missing_checked_evidence:
        raise_ns("P1-EFFECT", "checked effects require evidence records", namespace, source, ",".join(missing_checked_evidence))
    allowed_capabilities = set(profile.get("capabilities", []))
    missing_capabilities = sorted(requested_capabilities - allowed_capabilities)
    if missing_capabilities:
        raise_ns(profile_capability_code(active), "namespace requests capabilities outside its effective profile set", namespace, source, ",".join(missing_capabilities))
    allowed_memory = set(profile.get("memory", {}).get("regimes", []))
    bad_memory = sorted(set(namespace.get("memory_regimes", [])) - allowed_memory)
    if bad_memory:
        raise_ns(profile_memory_code(active), "namespace uses memory regimes illegal in the active profile", namespace, source, ",".join(bad_memory))
    allowed_runtime = set(profile.get("runtime", {}).get("services", []))
    bad_runtime = sorted(set(namespace.get("runtime_assumptions", [])) - allowed_runtime)
    if bad_runtime:
        raise_ns(profile_runtime_code(active), "namespace assumes unavailable runtime services", namespace, source, ",".join(bad_runtime))
    validate_profile_specific_namespace(namespace, active, source)
    return {
        "namespace": namespace.get("name"),
        "active_profile": active,
        "target": namespace.get("target"),
        "effective_effects": sorted(requested_effects),
        "effective_capabilities": sorted(requested_capabilities),
        "memory_regimes": namespace.get("memory_regimes", []),
        "runtime_assumptions": namespace.get("runtime_assumptions", []),
        "status": ":accepted",
    }


def validate_profile_specific_namespace(namespace: dict[str, Any], active: str, source: str) -> None:
    checks = namespace.get("checks", {})
    if active == ":core":
        if checks.get("unsafe") or namespace.get("unsafe_islands"):
            raise_ns("P2-UNSAFE", "core profile forbids unsafe code", namespace, source, "core-unsafe-forbidden")
        if checks.get("nondeterminism"):
            raise_ns("P2-NONDETERMINISM", "core profile forbids nondeterminism", namespace, source, "core-determinism")
    if active == ":meta":
        ungranted = sorted(set(checks.get("build_effects", [])) - set(checks.get("build_grants", [])))
        if ungranted:
            raise_ns("P3-BUILD-EFFECT", "meta build effects are undeclared or ungranted", namespace, source, ",".join(ungranted))
        if checks.get("runtime_capture"):
            raise_ns("P3-PHASE", "meta code captured runtime authority", namespace, source, "phase-separation")
        if checks.get("generated_target_violates_profile"):
            raise_ns("P3-GENERATED-PROFILE", "generated code is illegal in its target profile", namespace, source, "target-profile-validation")
    if active == ":hosted":
        flag_check(checks, "raw_memory", "P4-RAW-MEMORY", namespace, source, "hosted-raw-memory")
        flag_check(checks, "reflection_denied", "P4-REFLECTION", namespace, source, "reflection-grant")
        flag_check(checks, "host_object_untyped", "P4-HOST-OBJECT", namespace, source, "host-object-boundary")
    if active == ":native":
        flag_check(checks, "hidden_allocation", "P5-ALLOC", namespace, source, "declared-allocation")
        flag_check(checks, "raw_memory_safe", "P5-RAW-MEMORY", namespace, source, "unsafe-island")
        flag_check(checks, "target_feature_missing", "P5-SIMD", namespace, source, "target-feature")
    if active == ":firmware":
        flag_check(checks, "unbounded_allocation", "P6-ALLOC", namespace, source, "bounded-allocation")
        flag_check(checks, "missing_stack_budget", "P6-STACK", namespace, source, "stack-budget")
        flag_check(checks, "invalid_mmio", "P6-MMIO", namespace, source, "device-map")
    if active == ":kernel":
        flag_check(checks, "hidden_allocation", "P7-HIDDEN-ALLOC", namespace, source, "no-hidden-allocation")
        flag_check(checks, "invalid_scheduler", "P7-SCHEDULER", namespace, source, "scheduler-contract")
        flag_check(checks, "ambient_authority", "P7-AUTHORITY", namespace, source, "kernel-capability")
    if active == ":hardware":
        for flag, code, fact in [
            ("missing_width", "P8-WIDTH", "fixed-width"),
            ("unsafe_cdc", "P8-CDC", "cdc-proof"),
            ("unbounded_control", "P8-UNBOUNDED", "static-unroll"),
            ("tag_loss", "P8-TAG", "capability-tag-preservation"),
            ("missing_temporal", "P8-TEMPORAL", "temporal-safety-assumption"),
        ]:
            flag_check(checks, flag, code, namespace, source, fact)
    if active == ":distributed":
        flag_check(checks, "unrecorded_nondeterminism", "P9-REPLAY", namespace, source, "replay-record")
        flag_check(checks, "missing_schema", "P9-SCHEMA", namespace, source, "message-schema")
        flag_check(checks, "missing_compensation", "P9-COMPENSATION", namespace, source, "compensation-policy")
    if active == ":ai":
        flag_check(checks, "tool_without_schema", "P10-TOOL", namespace, source, "tool-schema")
        flag_check(checks, "prompt_injection", "P10-PROMPT", namespace, source, "prompt-role-policy")
        flag_check(checks, "generated_before_checks", "P10-GENERATED", namespace, source, "compiler-safety-checks")
    if active == ":gpu":
        flag_check(checks, "host_effect_in_kernel", "P11-HOST-EFFECT", namespace, source, "host-device-boundary")
        flag_check(checks, "missing_sync", "P11-SYNC", namespace, source, "synchronization-graph")
        flag_check(checks, "math_certificate_missing", "P11-MATH", namespace, source, "math-certificate")
    if active == ":formal":
        flag_check(checks, "unmodeled_nondeterminism", "P12-NONDETERMINISM", namespace, source, "quantified-input-or-assumption")
        flag_check(checks, "runtime_effect", "P12-EFFECT", namespace, source, "proof-mode-effect")
        flag_check(checks, "untrusted_proof", "P12-TRUST", namespace, source, "trusted-kernel")


def validate_edge(edge: dict[str, Any], profiles: dict[str, dict[str, Any]], source: str) -> dict[str, Any]:
    consumer = edge.get("consumer", {})
    producer = edge.get("producer", {})
    consumer_profile = consumer.get("profile")
    producer_profile = producer.get("profile")
    if consumer_profile not in profiles or producer_profile not in profiles:
        raise_edge("P13-MATRIX", "profile edge references undeclared profile", edge, source, "declared-edge-profiles")
    edge_kind = edge.get("edge")
    if edge_kind == ":direct" and producer_profile not in DIRECT_IMPORTS.get(consumer_profile, set()):
        raise_edge("P13-DIRECT", "illegal direct cross-profile source import", edge, source, "facade-or-artifact-boundary")
    if edge_kind == ":facade-required":
        facade = edge.get("facade", {})
        required = {"effects", "capabilities", "memory_assumptions", "runtime_assumptions", "safety_evidence"}
        if not facade or not required.issubset(facade):
            raise_edge("P13-FACADE", "facade edge lacks required metadata", edge, source, "complete-facade-metadata")
    if edge_kind == ":artifact-only" and edge.get("imports_source"):
        raise_edge("P13-ARTIFACT", "artifact-only edge attempted source import", edge, source, "artifact-boundary")
    if edge_kind == ":rejected":
        raise_edge("P13-DIRECT", "rejected edge appears in accepted manifest", edge, source, "legal-edge-kind")
    return {
        "consumer": consumer,
        "producer": producer,
        "edge": edge_kind,
        "facade": edge.get("facade"),
        "artifact": edge.get("artifact"),
        "evidence": edge.get("evidence", []),
        "status": ":accepted",
    }


def validate_backend_report(report: dict[str, Any], profiles: dict[str, dict[str, Any]], source: str) -> dict[str, Any]:
    profile = report.get("profile")
    if profile not in profiles:
        raise ProfileValidationError(
            "P1-BACKEND",
            "backend report references undeclared profile",
            source=source,
            missing_fact="backend-profile",
            remediation="Emit backend eligibility only for declared profiles.",
        )
    if report.get("eligible") is not True or not report.get("required_features"):
        namespace = {"name": report.get("namespace"), "active_profile": profile, "target": report.get("target"), "source_span": report.get("source_span")}
        raise ProfileValidationError(
            profile_backend_code(profile),
            "backend cannot implement required profile features",
            namespace=namespace,
            source=source,
            missing_fact="backend-required-features",
            remediation="Select a backend that preserves the profile manifest or reject before lowering.",
        )
    output = dict(report)
    output["status"] = ":eligible"
    return output


def validate_compliance_results(results: list[dict[str, Any]], source: str) -> list[dict[str, Any]]:
    profiles = {result.get("profile") for result in results}
    missing = sorted(STANDARD_PROFILES - profiles)
    if missing:
        raise ProfileValidationError(
            "P1-MISSING-PROFILE",
            "profile compliance fixture suite lacks standard profiles",
            source=source,
            missing_fact=",".join(missing),
            remediation="Add accepted and rejected compliance fixture results for every profile.",
        )
    for result in results:
        if not result.get("accepted_fixture") or not result.get("rejected_fixture") or not result.get("diagnostics"):
            raise ProfileValidationError(
                "P1-MISSING-PROFILE",
                "profile compliance result lacks accepted or rejected fixture evidence",
                source=source,
                missing_fact=str(result.get("profile")),
                remediation="Record accepted fixture, rejected fixture, and diagnostic id for every profile.",
            )
    return results


def effect_table(profiles: dict[str, dict[str, Any]]) -> list[dict[str, Any]]:
    return [
        {
            "profile": profile["name"],
            "allowed_effects": profile.get("allowed_effects", []),
            "checked_effects": profile.get("checked_effects", []),
            "forbidden_effects": profile.get("forbidden_effects", []),
        }
        for profile in profiles.values()
    ]


def capability_table(profiles: dict[str, dict[str, Any]]) -> list[dict[str, Any]]:
    return [{"profile": profile["name"], "capabilities": profile.get("capabilities", [])} for profile in profiles.values()]


def memory_records(profiles: dict[str, dict[str, Any]]) -> list[dict[str, Any]]:
    return [{"profile": profile["name"], "memory": profile.get("memory", {})} for profile in profiles.values()]


def runtime_records(profiles: dict[str, dict[str, Any]]) -> list[dict[str, Any]]:
    return [{"profile": profile["name"], "runtime": profile.get("runtime", {})} for profile in profiles.values()]


def compatibility_matrix() -> list[dict[str, Any]]:
    return [{"consumer": consumer, "direct_imports": sorted(producers)} for consumer, producers in sorted(DIRECT_IMPORTS.items())]


def profile_effect_code(profile: str) -> str:
    return PROFILE_DIAGNOSTICS.get(profile, {}).get("effect") or "P1-EFFECT"


def profile_capability_code(profile: str) -> str:
    return PROFILE_DIAGNOSTICS.get(profile, {}).get("capability") or "P1-CAPABILITY"


def profile_memory_code(profile: str) -> str:
    return PROFILE_DIAGNOSTICS.get(profile, {}).get("memory") or "P1-MEMORY"


def profile_runtime_code(profile: str) -> str:
    return PROFILE_DIAGNOSTICS.get(profile, {}).get("runtime") or "P1-RUNTIME"


def profile_backend_code(profile: str) -> str:
    return PROFILE_DIAGNOSTICS.get(profile, {}).get("backend") or "P1-BACKEND"


def flag_check(checks: dict[str, Any], flag: str, code: str, namespace: dict[str, Any], source: str, missing_fact: str) -> None:
    if checks.get(flag):
        raise_ns(code, f"profile-specific check failed: {flag}", namespace, source, missing_fact)


def raise_ns(code: str, message: str, namespace: dict[str, Any], source: str, missing_fact: str) -> None:
    raise ProfileValidationError(
        code,
        message,
        namespace=namespace,
        source=source,
        missing_fact=missing_fact,
        remediation="Adjust the namespace profile, effect, capability, memory, runtime, facade, or artifact boundary before lowering.",
    )


def raise_edge(code: str, message: str, edge: dict[str, Any], source: str, missing_fact: str) -> None:
    raise ProfileValidationError(
        code,
        message,
        edge=edge,
        source=source,
        missing_fact=missing_fact,
        remediation="Use a legal direct profile edge, complete facade, or artifact boundary with safety evidence.",
    )


def first(values: list[Any] | tuple[Any, ...] | set[Any]) -> Any:
    if not values:
        return None
    if isinstance(values, set):
        return sorted(values)[0]
    return values[0]


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
    for pointer, value in manifest.get("append", {}).items():
        target = get_pointer(result, pointer)
        if not isinstance(target, list):
            raise ValueError(f"append target is not a list: {pointer}")
        target.append(value)
    return result


def get_pointer(value: Any, pointer: str) -> Any:
    current = value
    for part in pointer_parts(pointer):
        current = current[int(part)] if isinstance(current, list) else current[part]
    return current


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
