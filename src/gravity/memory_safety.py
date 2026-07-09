"""SAFE2-SAFE5 memory, ownership, region, and linear-resource checks."""

from __future__ import annotations

import hashlib
import json
from pathlib import Path
from typing import Any


ALLOWED_FACT_RESULTS = {":proven", ":runtime-checked", ":unsafe-island", ":not-required"}
RAW_MEMORY_REGIMES = {":raw-memory", ":mmio", ":device-memory"}
REQUIRED_OPERATION_FIELDS = {
    "id",
    "kind",
    "source_span",
    "active_profile",
    "memory_regime",
    "provider_id",
    "safety_outcome",
    "type_facts",
    "effect_facts",
    "profile_facts",
    "required_facts",
    "fact_results",
}
FACT_DIAGNOSTICS = {
    "storage_exists": "SAFE2-USE-AFTER-RELEASE",
    "initialized": "SAFE2-UNINIT",
    "bounds": "SAFE2-BOUNDS",
    "lifetime": "SAFE2-LIFETIME",
    "aliasing": "SAFE2-ALIAS",
    "allocator": "SAFE2-ALLOCATOR",
    "profile_memory_regime": "SAFE2-PROFILE",
    "raw_policy": "SAFE2-RAW",
    "release_state": "SAFE2-DOUBLE-RELEASE",
}


class MemorySafetyError(Exception):
    def __init__(
        self,
        code: str,
        message: str,
        *,
        operation: dict[str, Any] | None = None,
        source: str,
        missing_fact: str,
        remediation: str,
        details: dict[str, Any] | None = None,
    ) -> None:
        super().__init__(message)
        self.code = code
        self.message = message
        self.operation = operation or {}
        self.source = source
        self.missing_fact = missing_fact
        self.remediation = remediation
        self.details = details or {}

    def to_diagnostic(self) -> dict[str, Any]:
        diagnostic = {
            "id": self.code,
            "message": self.message,
            "operation_id": self.operation.get("id"),
            "operation_kind": self.operation.get("kind"),
            "span": self.operation.get("source_span", {"source": self.source}),
            "generated_origin": self.operation.get("generated_origin", []),
            "active_profile": self.operation.get("active_profile"),
            "memory_regime": self.operation.get("memory_regime"),
            "provider_id": self.operation.get("provider_id"),
            "missing_fact": self.missing_fact,
            "remediation": self.remediation,
            "analyzer_stage": "memory-safety",
        }
        diagnostic.update(self.details)
        return diagnostic


def analyze_memory_safety_file(path: Path) -> dict[str, Any]:
    return analyze_memory_safety_manifest(load_manifest_file(path), str(path))


def memory_safety_diagnostic(path: Path) -> dict[str, Any] | None:
    try:
        analyze_memory_safety_file(path)
    except MemorySafetyError as exc:
        return exc.to_diagnostic()
    return None


def analyze_memory_safety_manifest(manifest: dict[str, Any], source: str) -> dict[str, Any]:
    if manifest.get("kind") != "memory-safety-input":
        raise MemorySafetyError(
            "SAFE2-PROFILE",
            "memory safety input has the wrong artifact kind",
            source=source,
            missing_fact="memory-safety-input",
            remediation="Feed the SAFE2-SAFE5 checker a memory-safety-input artifact.",
        )

    operations = manifest.get("operations", [])
    for operation in operations:
        validate_operation(operation, source)

    return {
        "kind": "memory-safety-artifact",
        "documents": ["SAFE2", "SAFE3", "SAFE4", "SAFE5"],
        "package": manifest.get("package"),
        "module": manifest.get("module"),
        "profile": manifest.get("profile"),
        "target": manifest.get("target"),
        "typed_core_artifact_hash": manifest.get("typed_core_artifact_hash"),
        "safety_artifact": manifest.get("safety_artifact"),
        "input_hash": artifact_hash(manifest),
        "memory_safety_facts": [memory_fact(operation) for operation in operations],
        "runtime_check_records": collect_records(operations, "runtime_checks"),
        "allocation_release_map": collect_records(operations, "allocation_release_records"),
        "region_lifetime_graph": collect_records(operations, "region_records"),
        "arena_generation_graph": collect_records(operations, "arena_records"),
        "escape_analysis_records": collect_records(operations, "escape_records"),
        "ownership_graph": collect_records(operations, "ownership_records"),
        "borrow_graph": collect_records(operations, "borrow_records"),
        "lifetime_interval_map": collect_records(operations, "lifetime_records"),
        "move_and_consume_records": collect_records(operations, "move_records"),
        "ownership_transfer_records": collect_records(operations, "transfer_records"),
        "linear_resource_flow_graph": collect_records(operations, "linear_flow_records"),
        "acquire_terminal_operation_records": collect_records(operations, "terminal_records"),
        "exceptional_cleanup_records": collect_records(operations, "exceptional_cleanup_records"),
        "cancellation_cleanup_records": collect_records(operations, "cancellation_cleanup_records"),
        "unsafe_memory_audit_records": collect_records(operations, "unsafe_audit_records"),
        "proof_records": collect_records(operations, "proof_records"),
        "diagnostics": [],
    }


def validate_operation(operation: dict[str, Any], source: str) -> None:
    missing = sorted(REQUIRED_OPERATION_FIELDS - set(operation))
    if missing:
        raise MemorySafetyError(
            "SAFE2-PROFILE",
            f"memory operation is missing required facts: {missing}",
            operation=operation,
            source=source,
            missing_fact=",".join(missing),
            remediation="Preserve source span, profile, provider, type, effect, and memory fact results from typed core or MIR.",
        )

    validate_profile_regime(operation, source)
    validate_check_erasure(operation, source)
    validate_raw_memory(operation, source)
    validate_required_facts(operation, source)

    kind = operation["kind"]
    if kind == ":ownership":
        validate_ownership(operation, source)
    if kind == ":region":
        validate_region(operation, source)
    if kind == ":linear-resource":
        validate_linear_resource(operation, source)


def validate_profile_regime(operation: dict[str, Any], source: str) -> None:
    allowed = operation.get("profile_facts", {}).get("allowed_memory_regimes", [])
    if operation["memory_regime"] not in allowed:
        raise MemorySafetyError(
            "SAFE2-PROFILE",
            "memory regime is unavailable in the active profile",
            operation=operation,
            source=source,
            missing_fact="profile-memory-regime",
            remediation="Select a profile-supported memory regime, provider, or safe artifact boundary.",
        )


def validate_check_erasure(operation: dict[str, Any], source: str) -> None:
    if operation.get("erased_check") and not has_proof_replacing_check(operation):
        raise MemorySafetyError(
            "SAFE2-CHECK-ERASE",
            "memory check was erased without a replacement proof artifact",
            operation=operation,
            source=source,
            missing_fact="erased-check-proof",
            remediation="Preserve the check or attach a proof record that replaces the erased memory check.",
        )


def validate_raw_memory(operation: dict[str, Any], source: str) -> None:
    if operation["memory_regime"] not in RAW_MEMORY_REGIMES:
        return
    if operation["safety_outcome"] != ":unsafe-island" or not operation.get("unsafe_audit_records"):
        raise MemorySafetyError(
            "SAFE2-RAW",
            "raw memory operation appears outside an audited unsafe island or safe wrapper",
            operation=operation,
            source=source,
            missing_fact="unsafe-memory-audit",
            remediation="Isolate raw memory behind an unsafe island with audit evidence or a checked safe wrapper.",
        )


def validate_required_facts(operation: dict[str, Any], source: str) -> None:
    results = operation["fact_results"]
    for fact in operation["required_facts"]:
        result = results.get(fact)
        if result in {None, False, ":missing", ":rejected"}:
            raise_fact_error(operation, source, fact)
        if result not in ALLOWED_FACT_RESULTS:
            raise_fact_error(operation, source, fact)
        if result == ":runtime-checked" and not has_runtime_check(operation, fact):
            raise_fact_error(operation, source, fact)
        if result == ":unsafe-island" and not operation.get("unsafe_audit_records"):
            raise_fact_error(operation, source, fact)


def validate_ownership(operation: dict[str, Any], source: str) -> None:
    ownership = operation.get("ownership", {})
    if ownership.get("use_after_move"):
        raise MemorySafetyError(
            "SAFE3-USE-AFTER-MOVE",
            "owned value is used after move",
            operation=operation,
            source=source,
            missing_fact="available-owner",
            remediation="Use the moved destination owner or return ownership before later access.",
            details={"owner_id": ownership.get("owner_id"), "move_path": ownership.get("move_path")},
        )
    if ownership.get("use_after_consume"):
        raise MemorySafetyError(
            "SAFE3-USE-AFTER-CONSUME",
            "owned value is used after terminal consumption",
            operation=operation,
            source=source,
            missing_fact="unconsumed-owner",
            remediation="Do not access a consumed owner unless a new valid owner is returned.",
            details={"owner_id": ownership.get("owner_id")},
        )
    if ownership.get("mutable_borrow") and ownership.get("immutable_borrows"):
        raise MemorySafetyError(
            "SAFE3-MUT-ALIAS",
            "mutable borrow conflicts with active immutable aliases",
            operation=operation,
            source=source,
            missing_fact="exclusive-mutable-borrow",
            remediation="End immutable borrows before taking a mutable borrow, or split non-overlapping ranges with proof.",
            details={"owner_id": ownership.get("owner_id"), "borrow_id": ownership.get("mutable_borrow")},
        )
    if ownership.get("borrow_escape"):
        raise MemorySafetyError(
            "SAFE3-BORROW-ESCAPE",
            "borrow escapes its valid lifetime",
            operation=operation,
            source=source,
            missing_fact="bounded-borrow-lifetime",
            remediation="Copy, move ownership, or keep the borrow inside the owner lifetime.",
            details={"owner_id": ownership.get("owner_id"), "escape_path": ownership.get("borrow_escape")},
        )


def validate_region(operation: dict[str, Any], source: str) -> None:
    region = operation.get("region", {})
    if region.get("escapes"):
        first_escape = region["escapes"][0]
        raise MemorySafetyError(
            "SAFE4-REGION-ESCAPE",
            "region value escapes its allocation lifetime",
            operation=operation,
            source=source,
            missing_fact="region-bounded-escape",
            remediation="Copy into longer-lived owned storage or keep the value inside the region.",
            details={"region_id": region.get("region_id"), "escape_path": first_escape.get("path")},
        )
    arena = operation.get("arena", {})
    if arena.get("use_after_reset"):
        raise MemorySafetyError(
            "SAFE4-POST-RESET",
            "arena value is used after reset invalidated its generation",
            operation=operation,
            source=source,
            missing_fact="valid-arena-generation",
            remediation="Avoid using values from an invalidated generation or emit a profile-legal generation check.",
            details={"arena_id": arena.get("arena_id"), "generation": arena.get("generation")},
        )
    provider = operation.get("provider_contract", {})
    if provider.get("missing_fields"):
        raise MemorySafetyError(
            "SAFE4-PROVIDER",
            "region or arena provider omits required safety behavior",
            operation=operation,
            source=source,
            missing_fact=",".join(provider["missing_fields"]),
            remediation="Declare allocation, alignment, reset, failure, threading, cleanup, and runtime-check behavior.",
            details={"provider_id": operation.get("provider_id")},
        )


def validate_linear_resource(operation: dict[str, Any], source: str) -> None:
    resource = operation.get("linear_resource", {})
    provider_id = operation.get("provider_id")
    for path in resource.get("paths", []):
        terminals = path.get("terminal_operations", [])
        if not terminals and not path.get("transfer"):
            raise MemorySafetyError(
                "SAFE5-LEAK",
                "linear resource may lack a terminal operation",
                operation=operation,
                source=source,
                missing_fact="terminal-operation",
                remediation="Close, release, transfer, commit, roll back, or cancel the resource on every path.",
                details={"resource_id": resource.get("resource_id"), "control_flow_path": path.get("id")},
            )
        if len(terminals) > 1:
            raise MemorySafetyError(
                "SAFE5-DOUBLE-CLOSE",
                "linear resource has multiple terminal operations on one path",
                operation=operation,
                source=source,
                missing_fact="single-terminal-operation",
                remediation="Ensure every path reaches exactly one terminal resource state.",
                details={"resource_id": resource.get("resource_id"), "control_flow_path": path.get("id")},
            )
        for terminal in terminals:
            if terminal.get("provider_id") != provider_id:
                raise MemorySafetyError(
                    "SAFE5-WRONG-PROVIDER",
                    "linear resource terminal operation uses an incompatible provider",
                    operation=operation,
                    source=source,
                    missing_fact="matching-terminal-provider",
                    remediation="Release the resource through the provider that acquired it or declare a valid transfer.",
                    details={"resource_id": resource.get("resource_id"), "terminal_operation": terminal.get("operation")},
                )
    if resource.get("cleanup_covers_all_paths") is False:
        raise MemorySafetyError(
            "SAFE5-BRANCH",
            "linear resource cleanup is present on only some control-flow paths",
            operation=operation,
            source=source,
            missing_fact="all-path-cleanup",
            remediation="Make cleanup structured or prove exact terminal behavior on normal, error, panic, and cancellation paths.",
            details={"resource_id": resource.get("resource_id")},
        )
    if resource.get("generated_duplicate"):
        raise MemorySafetyError(
            "SAFE5-GENERATED",
            "generated code duplicates a linear resource",
            operation=operation,
            source=source,
            missing_fact="linear-generated-flow",
            remediation="Preserve one owner in generated code and emit linear flow records with the generated-origin chain.",
            details={"resource_id": resource.get("resource_id")},
        )


def raise_fact_error(operation: dict[str, Any], source: str, fact: str) -> None:
    code = FACT_DIAGNOSTICS.get(fact, "SAFE2-LIFETIME")
    raise MemorySafetyError(
        code,
        f"memory operation lacks required fact: {fact}",
        operation=operation,
        source=source,
        missing_fact=fact,
        remediation="Prove the fact statically, emit a profile-legal runtime check, reject the operation, or isolate it in an unsafe island.",
    )


def has_runtime_check(operation: dict[str, Any], fact: str) -> bool:
    return any(
        check.get("condition") == fact
        and check.get("profile_legal") is True
        and check.get("failure_behavior") not in {None, ":undefined"}
        for check in operation.get("runtime_checks", [])
    )


def has_proof_replacing_check(operation: dict[str, Any]) -> bool:
    return any(
        proof.get("result") == ":accepted"
        and proof.get("replaces_check") == operation.get("erased_check")
        and proof.get("checker")
        for proof in operation.get("proof_records", [])
    )


def memory_fact(operation: dict[str, Any]) -> dict[str, Any]:
    return {
        "operation_id": operation["id"],
        "operation_kind": operation["kind"],
        "source_span": operation["source_span"],
        "active_profile": operation["active_profile"],
        "memory_regime": operation["memory_regime"],
        "provider_id": operation["provider_id"],
        "safety_outcome": operation["safety_outcome"],
        "type_facts": operation["type_facts"],
        "effect_facts": operation["effect_facts"],
        "required_facts": operation["required_facts"],
        "fact_results": operation["fact_results"],
    }


def collect_records(operations: list[dict[str, Any]], key: str) -> list[dict[str, Any]]:
    records = []
    for operation in operations:
        for record in operation.get(key, []):
            output = dict(record)
            output.setdefault("operation_id", operation["id"])
            output.setdefault("source_span", operation["source_span"])
            records.append(output)
    return records


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
