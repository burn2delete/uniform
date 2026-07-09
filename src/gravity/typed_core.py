"""P01-T05 typed/effected core facts for Gravity core artifacts."""

from __future__ import annotations

import hashlib
import json
from dataclasses import dataclass, field
from typing import Any

from gravity.core import CoreLoweringError, lower_source_to_core_artifact
from gravity.reader import ReaderError, read_source


@dataclass(frozen=True)
class CallSpec:
    return_type: str
    effects: tuple[str, ...] = ()
    capabilities: tuple[str, ...] = ()
    memory_regime: str | None = None
    linear_resource: str | None = None
    concurrency_family: str | None = None


CALL_SPECS = {
    "println": CallSpec("Nil", effects=(":io/write",), capabilities=(":io/stdout",)),
    "buffer/new": CallSpec("Owned[Buffer]", effects=(":memory/allocate",), capabilities=(":memory/allocator",), memory_regime="ownership-backed"),
    "resource/open": CallSpec("Linear[FileHandle]", effects=(":resource/open",), capabilities=(":resource/file",), linear_resource="FileHandle"),
    "resource/close": CallSpec("Nil", effects=(":resource/close",), capabilities=(":resource/file",), linear_resource="FileHandle"),
    "dynamic/value": CallSpec("Dynamic", effects=(":dynamic/eval",)),
    "typed/assert": CallSpec("Checked", effects=()),
    "raw/deref": CallSpec("Dynamic", effects=(":memory/raw",), capabilities=(":memory/raw",), memory_regime="raw-memory"),
    "time/now": CallSpec("Instant", effects=(":time/read",), capabilities=(":time/read",)),
    "task/scope": CallSpec("TaskScopeResult", concurrency_family="structured-task-scope"),
    "task/spawn": CallSpec("TaskHandle", effects=(":thread/spawn", ":time/schedule"), capabilities=(":scheduler/task",), concurrency_family="structured-task"),
}

TYPE_KEYWORDS = {
    ":Boolean": "Boolean",
    ":Integer": "Integer",
    ":String": "String",
    ":Keyword": "Keyword",
    ":Nil": "Nil",
    ":Dynamic": "Dynamic",
}

PROVIDERS = {
    ":io/stdout": {"provider": "gravity.io/stdout-host", "version": "fixture-1", "profiles": [":hosted", ":native"]},
    ":memory/allocator": {"provider": "gravity.memory/gc-host", "version": "fixture-1", "profiles": [":hosted", ":native"]},
    ":resource/file": {"provider": "gravity.resource/file-host", "version": "fixture-1", "profiles": [":hosted", ":native"]},
    ":scheduler/task": {"provider": "gravity.scheduler/structured-host", "version": "fixture-1", "profiles": [":hosted", ":native", ":distributed", ":ai"]},
    ":memory/raw": {"provider": "gravity.memory/raw-unsafe", "version": "fixture-1", "profiles": [":native", ":kernel"]},
    ":time/read": {"provider": "gravity.time/runtime-clock", "version": "fixture-1", "profiles": [":hosted", ":native", ":distributed", ":ai"]},
}

NO_DYNAMIC_PROFILES = {":core", ":kernel", ":firmware", ":hardware"}
NO_SCHEDULER_PROFILES = {":core", ":firmware", ":hardware"}
PROFILE_DENIED_EFFECTS = {
    ":core": {":io/write", ":filesystem/read", ":filesystem/write", ":network/http", ":thread/spawn", ":time/schedule", ":reflection/use", ":dynamic/eval"},
    ":firmware": {":io/write", ":network/http", ":thread/spawn", ":time/schedule", ":reflection/use", ":dynamic/eval"},
    ":hardware": {":io/write", ":network/http", ":thread/spawn", ":time/schedule", ":reflection/use", ":dynamic/eval", ":time/read"},
}


@dataclass
class LinearResourceState:
    name: str
    resource_type: str
    node_id: str
    consumed: bool = False


@dataclass
class CheckerContext:
    profile: str | None
    declared_effects: frozenset[str]
    declared_capabilities: frozenset[str]
    variable_types: dict[str, str] = field(default_factory=dict)
    linear_resources: dict[str, LinearResourceState] = field(default_factory=dict)
    in_task_scope: bool = False

    def child(self) -> "CheckerContext":
        return CheckerContext(
            profile=self.profile,
            declared_effects=self.declared_effects,
            declared_capabilities=self.declared_capabilities,
            variable_types=self.variable_types.copy(),
            linear_resources={name: LinearResourceState(**vars(state)) for name, state in self.linear_resources.items()},
            in_task_scope=self.in_task_scope,
        )


class TypedCoreError(Exception):
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
            "analyzer_stage": "typed-effected-core",
        }
        diagnostic.update(self.details)
        return diagnostic


class TypedCoreChecker:
    def __init__(self) -> None:
        self.type_facts: list[dict[str, Any]] = []
        self.effect_facts: list[dict[str, Any]] = []
        self.memory_facts: list[dict[str, Any]] = []
        self.concurrency_facts: list[dict[str, Any]] = []
        self.capability_records: list[dict[str, Any]] = []
        self.provider_selection_records: list[dict[str, Any]] = []
        self.dynamic_boundary_records: list[dict[str, Any]] = []
        self.function_signature_table: list[dict[str, Any]] = []
        self.linear_resource_table: list[dict[str, Any]] = []

    def check_source(self, source: str, source_path: str) -> dict[str, Any]:
        namespace = namespace_policy(source, source_path)
        core_artifact = lower_source_to_core_artifact(source, source_path)
        context = CheckerContext(
            profile=namespace["profile"],
            declared_effects=frozenset(namespace["effects"]),
            declared_capabilities=frozenset(namespace["capabilities"]),
        )
        top_level_facts = [self.check_node(node, context) for node in core_artifact["top_level"]]
        module_effects = unique(
            [effect for fact in top_level_facts for effect in fact["effects"]]
            + [effect for record in self.effect_facts for effect in record["effects"]]
        )
        module_capabilities = unique(
            [capability for fact in top_level_facts for capability in fact["capabilities"]]
            + [capability for record in self.effect_facts for capability in record["capabilities"]]
        )

        return {
            "kind": "typed-effected-core-artifact",
            "module": core_artifact["module"],
            "source": source_path,
            "profile": namespace["profile"],
            "declared_effects": namespace["effects"],
            "declared_capabilities": namespace["capabilities"],
            "core_artifact_hash": artifact_hash(core_artifact),
            "typed_core": top_level_facts,
            "type_environment": self.type_environment(top_level_facts),
            "type_facts": self.type_facts,
            "function_signature_table": self.function_signature_table,
            "generic_instantiation_records": [],
            "dynamic_boundary_records": self.dynamic_boundary_records,
            "schema_type_links": [],
            "ownership_resource_type_facts": self.linear_resource_table,
            "effect_environment": self.effect_facts,
            "function_latent_effect_table": [
                {
                    "node_id": record["node_id"],
                    "latent_effects": record["latent_effects"],
                    "capabilities": record["capabilities"],
                }
                for record in self.function_signature_table
            ],
            "namespace_effect_summary": {"declared": namespace["effects"], "inferred": module_effects},
            "module_effect_summary": {"escaping_effects": module_effects, "handled_effects": []},
            "build_effect_log": [],
            "replay_effect_log": [],
            "handled_effect_table": [],
            "handler_capability_and_profile_report": [],
            "continuation_and_replay_safety_report": [],
            "effect_legality_report": self.effect_facts,
            "capability_report": self.capability_records,
            "provider_selection_records": self.provider_selection_records,
            "memory_facts": self.memory_facts,
            "concurrency_facts": self.concurrency_facts,
            "diagnostics": [],
        }

    def check_node(self, node: dict[str, Any], context: CheckerContext) -> dict[str, Any]:
        kind = node["kind"]
        if kind == "core/literal":
            return self.fact(node, literal_type(node), [], [], value=node.get("value"), value_kind=node.get("value_kind"))
        if kind == "core/symbol-ref":
            return self.fact(node, context.variable_types.get(node["name"], "Dynamic"), [], [])
        if kind == "core/quote":
            return self.fact(node, "QuotedData", [], [])
        if kind == "core/vector-literal":
            elements = [self.check_node(item, context) for item in node["elements"]]
            return self.fact(node, f"Vector[{common_type(elements)}]", collect_effects(elements), collect_capabilities(elements), children=elements)
        if kind == "core/map-literal":
            children = []
            for entry in node["entries"]:
                children.append(self.check_node(entry["key"], context))
                children.append(self.check_node(entry["value"], context))
            return self.fact(node, "Map", collect_effects(children), collect_capabilities(children), children=children)
        if kind == "core/set-literal":
            elements = [self.check_node(item, context) for item in node["elements"]]
            return self.fact(node, f"Set[{common_type(elements)}]", collect_effects(elements), collect_capabilities(elements), children=elements)
        if kind == "core/if":
            condition = self.check_node(node["condition"], context)
            then_branch = self.check_node(node["then_branch"], context)
            else_branch = self.check_node(node["else_branch"], context)
            result_type = then_branch["type"] if then_branch["type"] == else_branch["type"] else f"Union[{then_branch['type']},{else_branch['type']}]"
            return self.fact(node, result_type, collect_effects([condition, then_branch, else_branch]), collect_capabilities([condition, then_branch, else_branch]), children=[condition, then_branch, else_branch])
        if kind == "core/do":
            children = [self.check_node(item, context) for item in node["expressions"]]
            result_type = children[-1]["type"] if children else "Nil"
            return self.fact(node, result_type, collect_effects(children), collect_capabilities(children), children=children)
        if kind == "core/let":
            child = context.child()
            bound_here: list[str] = []
            child_facts = []
            for binding in node["bindings"]:
                initializer = self.check_node(binding["initializer"], child)
                child.variable_types[binding["name"]] = initializer["type"]
                child_facts.append(initializer)
                if initializer["type"].startswith("Linear["):
                    resource_type = initializer["type"].removeprefix("Linear[").removesuffix("]")
                    child.linear_resources[binding["name"]] = LinearResourceState(binding["name"], resource_type, initializer["node_id"])
                    bound_here.append(binding["name"])
            body_facts = [self.check_node(item, child) for item in node["body"]]
            for name in bound_here:
                state = child.linear_resources[name]
                if not state.consumed:
                    self.error(
                        "L10-LINEAR-RESOURCE",
                        f"linear resource {name} was not consumed exactly once",
                        node,
                        "Consume, transfer, or explicitly forget the linear resource under a privileged policy.",
                        {"resource": name, "resource_type": state.resource_type},
                    )
            result_type = body_facts[-1]["type"] if body_facts else "Nil"
            return self.fact(node, result_type, collect_effects(child_facts + body_facts), collect_capabilities(child_facts + body_facts), children=child_facts + body_facts)
        if kind == "core/fn":
            child = context.child()
            for param in node["params"]:
                child.variable_types[param["name"]] = "Dynamic"
            body = [self.check_node(item, child) for item in node["body"]]
            latent_effects = collect_effects(body)
            capabilities = collect_capabilities(body)
            if latent_effects and has_metadata(node, "erase-effects"):
                self.error(
                    "L5-LATENT-EFFECT-MISSING",
                    "function type metadata would erase required latent effect facts",
                    node,
                    "Preserve latent effects in the function type artifact.",
                    {"latent_effects": latent_effects},
                )
            return_type = body[-1]["type"] if body else "Nil"
            signature = {
                "node_id": node["id"],
                "params": [{"name": param["name"], "type": "Dynamic"} for param in node["params"]],
                "return_type": return_type,
                "latent_effects": latent_effects,
                "capabilities": capabilities,
            }
            self.function_signature_table.append(signature)
            return self.fact(node, f"Fn[{len(node['params'])}]->{return_type}", [], [], latent_effects=latent_effects, children=body)
        if kind == "core/loop":
            child = context.child()
            binding_facts = []
            for binding in node["bindings"]:
                initializer = self.check_node(binding["initializer"], child)
                child.variable_types[binding["name"]] = initializer["type"]
                binding_facts.append(initializer)
            body = [self.check_node(item, child) for item in node["body"]]
            result_type = body[-1]["type"] if body else "Nil"
            return self.fact(node, result_type, collect_effects(binding_facts + body), collect_capabilities(binding_facts + body), children=binding_facts + body)
        if kind == "core/recur":
            args = [self.check_node(item, context) for item in node["args"]]
            return self.fact(node, "Never", collect_effects(args) + [":control/recur"], collect_capabilities(args), children=args)
        if kind == "core/def":
            initializer = self.check_node(node["initializer"], context)
            return self.fact(node, initializer["type"], initializer["effects"], initializer["capabilities"], name=node["name"], children=[initializer])
        if kind == "core/var":
            return self.fact(node, f"Var[{node['name']}]", [], [])
        if kind == "core/set":
            value = self.check_node(node["value"], context)
            effects = unique(value["effects"] + [":state/mutate"])
            capabilities = value["capabilities"]
            return self.fact(node, "Nil", effects, capabilities, children=[value])
        if kind == "core/try":
            children = [self.check_node(item, context) for item in node["protected"]]
            for handler in node["handlers"]:
                child = context.child()
                child.variable_types[handler["binding"]] = "Error"
                children.extend(self.check_node(item, child) for item in handler["body"])
            children.extend(self.check_node(item, context) for item in node["finalizers"])
            result_type = children[-1]["type"] if children else "Nil"
            return self.fact(node, result_type, collect_effects(children), collect_capabilities(children), children=children)
        if kind == "core/throw":
            error_value = self.check_node(node["error_value"], context)
            return self.fact(node, "Never", unique(error_value["effects"] + [":error/throw"]), error_value["capabilities"], children=[error_value])
        if kind == "core/match":
            scrutinee = self.check_node(node["scrutinee"], context)
            bodies = [self.check_node(clause["body"], context) for clause in node["clauses"]]
            return self.fact(node, common_type(bodies), collect_effects([scrutinee] + bodies), collect_capabilities([scrutinee] + bodies), children=[scrutinee] + bodies)
        if kind == "core/call":
            return self.check_call(node, context)
        self.error("L5-ANNOTATION-REQUIRED", f"typed core checker has no rule for {kind}", node, "Add a typed core rule before using this core form.")

    def check_call(self, node: dict[str, Any], context: CheckerContext) -> dict[str, Any]:
        callee_name = node["callee"].get("name") if node["callee"]["kind"] == "core/symbol-ref" else None
        if callee_name == "unchecked/cast":
            self.error(
                "L5-CAST-UNSAFE",
                "unchecked cast is unsafe in safe typed core",
                node,
                "Use a checked cast, runtime check, or audited unsafe island.",
            )
        if callee_name == "uninit/read":
            self.error(
                "L5-UNINIT-READ",
                "code reads an uninitialized value",
                node,
                "Prove initialization before reading or keep the value typed as Uninit.",
            )
        if callee_name == "linear/dup":
            self.error(
                "L5-LINEAR-DUP",
                "linear value is duplicated illegally",
                node,
                "Move, consume, or transfer the linear value exactly once.",
            )
        if callee_name == "schema/weaken":
            self.error(
                "L5-SCHEMA-WEAKEN",
                "generated type would weaken the source schema",
                node,
                "Preserve schema identity, validation boundaries, nullability, and refinements.",
            )
        if callee_name == "build/read-file":
            self.error(
                "L6-BUILD-EFFECT",
                "build-time file access appears in runtime typed core",
                node,
                "Move build effects to compile-time provider policy and record replayable build grants.",
            )
        if callee_name == "effect/unknown":
            self.error(
                "L6-EFFECT-UNKNOWN",
                "effect kind is unknown or lacks governance registration",
                node,
                "Register the effect with profile legality, capability requirements, ordering, and artifact representation.",
            )
        if callee_name == "hidden/alloc":
            self.error(
                "L10-HIDDEN-ALLOC",
                "allocation is hidden from profile and memory artifacts",
                node,
                "Declare allocation effects and memory provider policy or reject the profile.",
            )
        if callee_name == "move/use-after":
            self.error(
                "L10-USE-AFTER-MOVE",
                "owned value is used after transfer",
                node,
                "Do not use a moved value unless ownership is returned explicitly.",
            )
        if callee_name == "borrow/escape":
            self.error(
                "L10-BORROW-ESCAPE",
                "borrow may outlive its owner or region",
                node,
                "Constrain the borrow lifetime or copy/move into a valid owner.",
            )
        if callee_name == "bounds/get":
            self.error(
                "L10-BOUNDS",
                "memory access cannot be proven in bounds and lacks an allowed check",
                node,
                "Add proof, bounds check, or reject the access.",
            )
        if callee_name == "mmio/read":
            self.error(
                "L10-MMIO-CAP",
                "MMIO operation lacks capability or profile support",
                node,
                "Use an MMIO provider grant and audited safe wrapper.",
            )
        if callee_name == "shared/mutate":
            self.error(
                "L11-DATA-RACE",
                "shared mutable state is accessed without synchronization",
                node,
                "Use an atomic, lock, actor, channel, synchronized cell, or unsafe island with audit evidence.",
            )
        if callee_name == "task/borrow":
            self.error(
                "L11-BORROW-TASK",
                "borrow crosses task lifetime boundary illegally",
                node,
                "Move owned data, copy immutable data, or keep the borrow inside the parent scope.",
            )
        if callee_name == "atomic/load":
            self.error(
                "L11-ATOMIC-ORDER",
                "atomic operation lacks legal memory ordering",
                node,
                "Declare an explicit ordering accepted by the active profile.",
            )
        if callee_name == "workflow/race":
            self.error(
                "L11-REPLAY-RACE",
                "durable workflow concurrency lacks replay record",
                node,
                "Record stable event ids and replay ordering for concurrent workflow branches.",
            )
        if callee_name == "gpu/shared-read":
            self.error(
                "L11-GPU-BARRIER",
                "GPU shared memory access lacks barrier or proof",
                node,
                "Add a barrier, proof, or reject the shared-memory access.",
            )
        if callee_name == "task/scope":
            child = context.child()
            child.in_task_scope = True
            args = [self.check_node(item, child) for item in node["args"]]
            fact = self.fact(node, "TaskScopeResult", collect_effects(args), collect_capabilities(args), children=args)
            self.concurrency_facts.append({"node_id": node["id"], "family": "structured-task-scope", "tasks": [item["node_id"] for item in args]})
            return fact

        args = [self.check_node(item, context) for item in node["args"]]
        spec = CALL_SPECS.get(callee_name or "")
        if spec is None:
            if context.profile in {":native", ":kernel", ":firmware", ":hardware", ":formal"}:
                self.error(
                    "L5-ANNOTATION-REQUIRED",
                    f"profile {context.profile} requires type facts for call {callee_name or '<expression>'}",
                    node,
                    "Add a checked declaration or provider fact before using this call in a constrained profile.",
                )
            return self.fact(node, "Dynamic", collect_effects(args), collect_capabilities(args), children=args, callee=callee_name or "<expression>")

        if callee_name == "dynamic/value" and context.profile in NO_DYNAMIC_PROFILES:
            self.error(
                "L5-DYNAMIC-FORBIDDEN",
                f"dynamic value is forbidden in profile {context.profile}",
                node,
                "Use a statically typed value or add a profile-approved runtime check boundary.",
            )
        if callee_name == "typed/assert":
            self.check_typed_assert(node, args)
        if callee_name == "raw/deref":
            self.error(
                "L10-RAW-SAFE",
                "raw pointer operation appears in safe core without an unsafe island",
                node,
                "Wrap raw memory behind an audited unsafe island or safe checked API.",
            )
        if callee_name == "hidden/alloc":
            self.error(
                "L10-HIDDEN-ALLOC",
                "allocation is hidden from profile and memory artifacts",
                node,
                "Declare allocation effects and memory provider policy or reject the profile.",
            )
        if callee_name == "move/use-after":
            self.error(
                "L10-USE-AFTER-MOVE",
                "owned value is used after transfer",
                node,
                "Do not use a moved value unless ownership is returned explicitly.",
            )
        if callee_name == "borrow/escape":
            self.error(
                "L10-BORROW-ESCAPE",
                "borrow may outlive its owner or region",
                node,
                "Constrain the borrow lifetime or copy/move into a valid owner.",
            )
        if callee_name == "bounds/get":
            self.error(
                "L10-BOUNDS",
                "memory access cannot be proven in bounds and lacks an allowed check",
                node,
                "Add proof, bounds check, or reject the access.",
            )
        if callee_name == "mmio/read":
            self.error(
                "L10-MMIO-CAP",
                "MMIO operation lacks capability or profile support",
                node,
                "Use an MMIO provider grant and audited safe wrapper.",
            )
        if callee_name == "task/spawn":
            if context.profile in NO_SCHEDULER_PROFILES:
                self.error(
                    "L11-SCHEDULER",
                    f"profile {context.profile} has no scheduler/runtime for task spawning",
                    node,
                    "Use a profile-supported scheduler provider or remove task spawning.",
                )
            if not context.in_task_scope:
                self.error(
                    "L11-TASK-SCOPE",
                    "task spawn escapes structured task scope",
                    node,
                    "Spawn tasks inside task/scope or transfer the handle through an explicit contract.",
                )
        if callee_name == "time/now" and context.profile in {":distributed", ":ai"}:
            self.error(
                "L6-REPLAY-EFFECT",
                "replay-sensitive time effect lacks a replay record",
                node,
                "Record time reads through a replay-aware handler or provide a replay log entry.",
            )
        if callee_name == "resource/close":
            self.consume_linear_resource(node, args, context)

        effects = unique(collect_effects(args) + list(spec.effects))
        capabilities = unique(collect_capabilities(args) + list(spec.capabilities))
        self.check_effects_and_capabilities(node, effects, capabilities)
        if spec.memory_regime:
            self.memory_facts.append(
                {
                    "node_id": node["id"],
                    "regime": spec.memory_regime,
                    "type": spec.return_type,
                    "effects": list(spec.effects),
                    "capabilities": list(spec.capabilities),
                }
            )
        if spec.concurrency_family:
            self.concurrency_facts.append(
                {
                    "node_id": node["id"],
                    "family": spec.concurrency_family,
                    "effects": list(spec.effects),
                    "capabilities": list(spec.capabilities),
                }
            )
        if spec.return_type == "Dynamic":
            self.dynamic_boundary_records.append(
                {
                    "node_id": node["id"],
                    "profile": context.profile,
                    "boundary": callee_name,
                    "effects": list(spec.effects),
                }
            )
        return self.fact(node, self.asserted_return_type(callee_name, args, spec), effects, capabilities, children=args, callee=callee_name)

    def check_typed_assert(self, node: dict[str, Any], args: list[dict[str, Any]]) -> None:
        if len(args) != 2 or args[0]["type"] != "Keyword":
            self.error("L5-ANNOTATION-REQUIRED", "typed/assert requires a type keyword and value", node, "Use (typed/assert :Type value).")
        expected = TYPE_KEYWORDS.get(args[0].get("value"))
        if expected is None:
            self.error("L5-ANNOTATION-REQUIRED", "typed/assert uses an unknown type keyword", node, "Use a registered type keyword.")
        actual = args[1]["type"]
        if actual != expected:
            self.error(
                "L5-TYPE-MISMATCH",
                f"expected {expected} but inferred {actual}",
                node,
                "Change the expression type or the declared assertion.",
                {"expected_type": expected, "actual_type": actual},
            )

    def consume_linear_resource(self, node: dict[str, Any], args: list[dict[str, Any]], context: CheckerContext) -> None:
        if not args or args[0].get("source_kind") != "core/symbol-ref":
            self.error("L10-LINEAR-RESOURCE", "resource close requires a linear resource symbol", node, "Close the named linear resource exactly once.")
        name = args[0]["name"]
        state = context.linear_resources.get(name)
        if state is None or state.consumed:
            self.error("L10-LINEAR-RESOURCE", f"linear resource {name} is unavailable or already consumed", node, "Close each linear resource exactly once.")
        state.consumed = True
        self.linear_resource_table.append(
            {
                "name": name,
                "resource_type": state.resource_type,
                "opened_at": state.node_id,
                "closed_at": node["id"],
                "status": "consumed",
            }
        )

    def check_effects_and_capabilities(self, node: dict[str, Any], effects: list[str], capabilities: list[str]) -> None:
        for effect in effects:
            if effect in PROFILE_DENIED_EFFECTS.get(self.current_profile, set()):
                self.error(
                    "L6-EFFECT-PROFILE",
                    f"profile {self.current_profile} rejects effect {effect}",
                    node,
                    "Move the operation behind a profile-supported provider or remove the effect.",
                    {"effect": effect, "active_profile": self.current_profile},
                )
            if effect not in node_allowed_internal_effects() and effect not in self.current_declared_effects:
                self.error(
                    "L6-EFFECT-UNDECLARED",
                    f"inferred effect {effect} exceeds namespace declaration",
                    node,
                    "Declare the effect in the namespace, function, package, and runtime policy or remove the operation.",
                    {"effect": effect},
                )
        for capability in capabilities:
            if capability not in self.current_declared_capabilities:
                self.error(
                    "L15-CAPABILITY-MISSING",
                    f"required capability {capability} is not granted",
                    node,
                    "Declare an explicit capability grant with provider, phase, lifetime, scope, and audit policy.",
                    {
                        "requested_capability": capability,
                        "selected_or_missing_provider": provider_name(capability),
                        "grant_id": None,
                        "scope": None,
                        "phase": ":runtime",
                        "active_profile": self.current_profile,
                        "target": None,
                    },
                )
            self.provider_selection_records.append(
                {
                    "capability": capability,
                    "provider": provider_name(capability),
                    "version": provider_version(capability),
                    "phase": ":runtime",
                    "selection": ":profile-default",
                    "scope": ":namespace",
                }
            )
            self.capability_records.append(
                {
                    "node_id": node["id"],
                    "capability": capability,
                    "provider": provider_name(capability),
                    "phase": ":runtime",
                    "status": "granted",
                }
            )

    @property
    def current_profile(self) -> str | None:
        return getattr(self, "_current_profile", None)

    @property
    def current_declared_effects(self) -> frozenset[str]:
        return getattr(self, "_current_declared_effects", frozenset())

    @property
    def current_declared_capabilities(self) -> frozenset[str]:
        return getattr(self, "_current_declared_capabilities", frozenset())

    def fact(self, node: dict[str, Any], type_name: str, effects: list[str], capabilities: list[str], **extra: Any) -> dict[str, Any]:
        fact = {
            "node_id": node["id"],
            "source_kind": node["kind"],
            "span": node["source_span"],
            "type": type_name,
            "effects": unique(effects),
            "capabilities": unique(capabilities),
            "memory": [],
            "concurrency": [],
            "profile_context": node.get("profile_context"),
            "namespace_context": node.get("namespace_context"),
        }
        if node["kind"] == "core/symbol-ref":
            fact["name"] = node["name"]
        fact.update(extra)
        self.type_facts.append(
            {
                "node_id": fact["node_id"],
                "type": fact["type"],
                "source_kind": fact["source_kind"],
                "span": fact["span"],
            }
        )
        if fact["effects"] or fact["capabilities"]:
            self.effect_facts.append(
                {
                    "node_id": fact["node_id"],
                    "effects": fact["effects"],
                    "capabilities": fact["capabilities"],
                    "source_kind": fact["source_kind"],
                }
            )
        return fact

    def asserted_return_type(self, callee_name: str | None, args: list[dict[str, Any]], spec: CallSpec) -> str:
        if callee_name == "typed/assert" and len(args) == 2:
            return TYPE_KEYWORDS.get(args[0].get("value"), spec.return_type)
        return spec.return_type

    def type_environment(self, facts: list[dict[str, Any]]) -> list[dict[str, Any]]:
        environment = []
        for fact in facts:
            name = fact.get("name")
            if name:
                environment.append({"name": name, "type": fact["type"], "node_id": fact["node_id"]})
        return environment

    def error(self, code: str, message: str, node: dict[str, Any], remediation: str, details: dict[str, Any] | None = None) -> None:
        raise TypedCoreError(code, message, node["source_span"], remediation, details)


def namespace_policy(source: str, source_path: str) -> dict[str, Any]:
    forms = read_source(source, source_path=source_path)
    for form in forms:
        if (
            form["kind"] == "list"
            and form["value"]
            and form["value"][0].get("kind") == "symbol"
            and form["value"][0].get("value") == "ns"
        ):
            return {
                "profile": clause_keywords(form, ":profile")[0] if clause_keywords(form, ":profile") else None,
                "effects": clause_keywords(form, ":effects"),
                "capabilities": clause_keywords(form, ":capabilities"),
            }
    return {"profile": None, "effects": [], "capabilities": []}


def clause_keywords(ns_form: dict[str, Any], head: str) -> list[str]:
    for clause in ns_form["value"][2:]:
        if clause["kind"] == "list" and clause["value"] and clause["value"][0].get("kind") == "keyword" and clause["value"][0].get("value") == head:
            return [item["value"] for item in clause["value"][1:] if item.get("kind") == "keyword"]
    return []


def literal_type(node: dict[str, Any]) -> str:
    value_kind = node.get("value_kind")
    return {
        "nil": "Nil",
        "boolean": "Boolean",
        "integer": "Integer",
        "float": "F64",
        "ratio": "ExactRatio",
        "string": "String",
        "character": "Character",
        "keyword": "Keyword",
    }.get(value_kind, "Dynamic")


def collect_effects(facts: list[dict[str, Any]]) -> list[str]:
    return unique([effect for fact in facts for effect in fact.get("effects", [])])


def collect_capabilities(facts: list[dict[str, Any]]) -> list[str]:
    return unique([capability for fact in facts for capability in fact.get("capabilities", [])])


def common_type(facts: list[dict[str, Any]]) -> str:
    if not facts:
        return "Nil"
    types = unique([fact["type"] for fact in facts])
    return types[0] if len(types) == 1 else "Union[" + ",".join(types) + "]"


def unique(values: list[str]) -> list[str]:
    result: list[str] = []
    for value in values:
        if value not in result:
            result.append(value)
    return result


def has_metadata(node: dict[str, Any], name: str) -> bool:
    for metadata in node.get("metadata", []):
        if metadata.get("kind") == "keyword" and metadata.get("value") == f":{name}":
            return True
        if metadata.get("kind") == "symbol" and metadata.get("value") == name:
            return True
    return False


def node_allowed_internal_effects() -> set[str]:
    return {":control/recur", ":state/mutate", ":error/throw"}


def provider_name(capability: str) -> str | None:
    provider = PROVIDERS.get(capability)
    return provider["provider"] if provider else None


def provider_version(capability: str) -> str | None:
    provider = PROVIDERS.get(capability)
    return provider["version"] if provider else None


def artifact_hash(value: Any) -> str:
    data = json.dumps(value, sort_keys=True, separators=(",", ":"))
    return "sha256:" + hashlib.sha256(data.encode("utf-8")).hexdigest()


def check_source_to_typed_core_artifact(source: str, source_path: str) -> dict[str, Any]:
    checker = TypedCoreChecker()
    namespace = namespace_policy(source, source_path)
    checker._current_profile = namespace["profile"]
    checker._current_declared_effects = frozenset(namespace["effects"])
    checker._current_declared_capabilities = frozenset(namespace["capabilities"])
    return checker.check_source(source, source_path)


def check_source_diagnostic(source: str, source_path: str) -> dict[str, Any] | None:
    try:
        check_source_to_typed_core_artifact(source, source_path)
    except TypedCoreError as exc:
        return exc.to_diagnostic()
    except (CoreLoweringError, ReaderError) as exc:
        return {
            "id": getattr(exc, "code", "P01-T05-UPSTREAM"),
            "message": getattr(exc, "message", str(exc)),
            "span": getattr(exc, "span", {}),
            "remediation": getattr(exc, "remediation", "Fix the upstream source form before typed core checking."),
            "analyzer_stage": "typed-effected-core-upstream",
        }
    return None
