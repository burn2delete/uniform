"""L12 compile-time evaluation records and diagnostics."""

from __future__ import annotations

import hashlib
import json
from dataclasses import dataclass
from typing import Any

from gravity.reader import ReaderError, read_source


BUILD_EFFECT_HEADS = {
    "read-file": ":build/read-file",
    "env/read": ":build/env",
    "network/get": ":build/network",
    "shell/exec": ":build/exec",
    "time/now": ":build/time",
    "random/bytes": ":build/random",
    "model/call": ":build/model-call",
    "tool/call": ":build/tool-call",
    "target/probe": ":build/target-probe",
}

NONDETERMINISTIC_EFFECTS = {
    ":build/network",
    ":build/exec",
    ":build/time",
    ":build/random",
    ":build/model-call",
    ":build/tool-call",
    ":build/package-index",
}

GENERATED_ILLEGAL_HEADS = {
    "unsafe",
    "raw/deref",
    "host/reflect",
    "dynamic/load",
    "unchecked-cast",
}

CONSTRAINED_PROFILES = {":core", ":kernel", ":firmware", ":hardware"}


@dataclass(frozen=True)
class BuildGrant:
    effect: str
    provider: str
    scope: str
    lifetime: str
    profile: str
    replay_policy: str

    def authorizes(self, effect: str, provider: str, scope: str, profile: str) -> bool:
        if self.effect != effect or self.provider != provider:
            return False
        if self.profile not in {profile, ":any"}:
            return False
        if self.scope == "*":
            return True
        if self.scope.endswith("/"):
            return scope.startswith(self.scope)
        return self.scope == scope

    def to_record(self) -> dict[str, str]:
        return {
            "effect": self.effect,
            "provider": self.provider,
            "scope": self.scope,
            "lifetime": self.lifetime,
            "profile": self.profile,
            "replay_policy": self.replay_policy,
        }


@dataclass(frozen=True)
class CompileValue:
    value: Any
    representation: Any
    stable: bool = True


class CompileTimeError(Exception):
    def __init__(
        self,
        code: str,
        message: str,
        span: dict[str, Any],
        remediation: str,
        *,
        profile: str,
        target: str,
        requested_effect: str | None = None,
        relevant_grant: dict[str, Any] | None = None,
        generated_origin_chain: list[dict[str, Any]] | None = None,
    ) -> None:
        super().__init__(message)
        self.code = code
        self.message = message
        self.span = span
        self.remediation = remediation
        self.profile = profile
        self.target = target
        self.requested_effect = requested_effect
        self.relevant_grant = relevant_grant
        self.generated_origin_chain = generated_origin_chain or []

    def to_diagnostic(self) -> dict[str, Any]:
        return {
            "id": self.code,
            "message": self.message,
            "active_phase": "compile-time-evaluation",
            "profile": self.profile,
            "target": self.target,
            "span": self.span,
            "generated_origin_chain": self.generated_origin_chain,
            "requested_effect": self.requested_effect,
            "relevant_grant": self.relevant_grant,
            "remediation": self.remediation,
            "analyzer_stage": "compile-time-evaluation",
        }


class CompileTimeAnalyzer:
    def __init__(self, source: str, source_path: str) -> None:
        self.source = source
        self.source_path = source_path
        try:
            self.forms = read_source(source, source_path=source_path)
        except ReaderError as exc:
            raise CompileTimeError(
                exc.code,
                exc.message,
                exc.span,
                exc.remediation,
                profile=":unknown",
                target="unknown",
            ) from exc

        self.context = self.extract_context()
        self.build_policy = self.extract_build_policy()
        self.grants = self.extract_grants()
        self.runtime_bindings = self.collect_runtime_bindings()
        self.events: list[dict[str, Any]] = []
        self.constant_table: list[dict[str, Any]] = []
        self.generated_origin_chains: list[dict[str, Any]] = []
        self.generated_forms: list[dict[str, Any]] = []
        self.build_effect_log: list[dict[str, Any]] = []
        self.file_input_digests: list[dict[str, str]] = []
        self.output_artifact_digests: list[dict[str, str]] = []
        self.environment_key_reads: list[dict[str, str]] = []
        self.nondeterminism_replay_records: list[dict[str, str]] = []

    def analyze(self) -> dict[str, Any]:
        for form in self.forms:
            head = list_head(form)
            if head in {"ns", "build-policy", "build-grant"}:
                continue
            if head == "defconst":
                self.process_compile_time_binding(form, binding_kind="defconst")
            elif head == "def" and len(form["value"]) >= 3 and list_head(form["value"][2]) == "compile-time":
                self.process_compile_time_binding(form, binding_kind="def")
            elif head == "compile-time":
                self.process_compile_time_form(form)
            elif head == "derive-code":
                self.process_derive_code(form)
            elif head == "cached-result":
                self.process_cached_result(form)

        cache_key = stable_hash(
            {
                "source_hash": source_hash(self.source),
                "compiler_version": self.build_policy["compiler_version"],
                "target": self.context["target"],
                "profile": self.context["profile"],
                "facets": self.build_policy["facets"],
                "grants": [grant.to_record() for grant in self.grants],
                "file_input_digests": self.file_input_digests,
                "generated_forms": self.generated_forms,
            }
        )
        return {
            "kind": "compile-time-evaluation-record",
            "document": "L12",
            "namespace": self.context["namespace"],
            "package": self.context["package"],
            "profile": self.context["profile"],
            "target": self.context["target"],
            "compiler_version": self.build_policy["compiler_version"],
            "language_facets": self.build_policy["facets"],
            "hermetic_mode": self.build_policy["mode"] == ":hermetic",
            "source": self.source_path,
            "source_hash": source_hash(self.source),
            "ordered_compile_time_events": self.events,
            "macro_expansion_events": [],
            "generated_origin_chains": self.generated_origin_chains,
            "generated_forms": self.generated_forms,
            "constant_table": self.constant_table,
            "build_effect_log": self.build_effect_log,
            "file_input_digests": self.file_input_digests,
            "output_artifact_digests": self.output_artifact_digests,
            "environment_key_reads": self.environment_key_reads,
            "external_result_digests": [
                event
                for event in self.build_effect_log
                if event["effect"] in {":build/network", ":build/exec", ":build/model-call", ":build/tool-call", ":build/package-index"}
            ],
            "nondeterminism_replay_records": self.nondeterminism_replay_records,
            "cache_key": cache_key,
            "cache_reuse_decision": {
                "key": cache_key,
                "reuse": False,
                "reason": "no prior trace supplied",
                "policy": "strict-hermetic" if self.build_policy["mode"] == ":hermetic" else "interactive",
            },
            "diagnostics": [],
        }

    def extract_context(self) -> dict[str, Any]:
        namespace = "anonymous"
        profile = ":core"
        target = "portable-core"
        declared_effects: list[str] = []
        declared_capabilities: list[str] = []
        providers: list[str] = []
        for form in self.forms:
            if list_head(form) != "ns":
                continue
            namespace = form["value"][1]["value"]
            profile = form.get("profile_context") or profile
            for clause in form["value"][2:]:
                clause_head = list_head(clause)
                values = clause.get("value", [])[1:]
                if clause_head == ":profile" and values:
                    profile = atom_text(values[0])
                elif clause_head == ":target" and values:
                    target = atom_text(values[0])
                elif clause_head == ":effects":
                    declared_effects = [atom_text(value) for value in values]
                elif clause_head == ":capabilities":
                    declared_capabilities = [atom_text(value) for value in values]
                elif clause_head == ":providers":
                    providers = [atom_text(value) for value in values]
            break
        return {
            "namespace": namespace,
            "package": namespace.split(".")[0],
            "profile": profile,
            "target": target,
            "declared_effects": declared_effects,
            "declared_capabilities": declared_capabilities,
            "providers": providers,
        }

    def extract_build_policy(self) -> dict[str, Any]:
        policy = {
            "mode": ":hermetic",
            "compiler_version": "gravity-fixture-compiler",
            "facets": [":core"],
            "declared_inputs": [],
            "declared_env": [],
        }
        for form in self.forms:
            if list_head(form) == "build-policy":
                entries = keyword_pairs(form["value"][1:])
                policy["mode"] = atom_text(entries.get(":mode", syntax_keyword(":hermetic")))
                policy["compiler_version"] = atom_text(entries.get(":compiler-version", syntax_string("gravity-fixture-compiler")))
                policy["facets"] = list_text(entries.get(":facets", syntax_vector([syntax_keyword(":core")])))
                policy["declared_inputs"] = list_text(entries.get(":declared-inputs", syntax_vector([])))
                policy["declared_env"] = list_text(entries.get(":declared-env", syntax_vector([])))
        return policy

    def extract_grants(self) -> list[BuildGrant]:
        grants = []
        for form in self.forms:
            if list_head(form) != "build-grant":
                continue
            values = form["value"]
            if len(values) != 7:
                self.error(
                    "L12-BUILD-GRANT",
                    "build grant must name effect, provider, scope, lifetime, profile, and replay policy",
                    form["span"],
                    "Emit a complete grant record with scoped authority.",
                    requested_effect=None,
                )
            grants.append(
                BuildGrant(
                    effect=atom_text(values[1]),
                    provider=atom_text(values[2]),
                    scope=atom_text(values[3]),
                    lifetime=atom_text(values[4]),
                    profile=atom_text(values[5]),
                    replay_policy=atom_text(values[6]),
                )
            )
        return grants

    def collect_runtime_bindings(self) -> set[str]:
        bindings: set[str] = set()
        for form in self.forms:
            if list_head(form) == "def" and len(form["value"]) >= 3 and list_head(form["value"][2]) != "compile-time":
                bindings.add(atom_text(form["value"][1]))
        return bindings

    def process_compile_time_binding(self, form: dict[str, Any], binding_kind: str) -> None:
        values = form["value"]
        if len(values) < 3:
            return
        name = atom_text(values[1])
        compile_form = values[2]
        if list_head(compile_form) != "compile-time" or len(compile_form["value"]) < 2:
            return
        value = self.evaluate(compile_form["value"][1], pure=True, fuel=128)
        if not value.stable:
            self.error(
                "L12-CONST-REPRESENTATION",
                "compile-time value cannot be represented in a stable target artifact",
                compile_form["span"],
                "Return literals, data, syntax, or another declared stable representation.",
            )
        record = {
            "name": name,
            "binding_kind": binding_kind,
            "value_hash": stable_hash(value.representation),
            "representation": value.representation,
            "source_span": form["span"],
        }
        self.constant_table.append(record)
        self.events.append({"event": "constant-evaluated", "binding": name, "value_hash": record["value_hash"], "span": form["span"]})

    def process_compile_time_form(self, form: dict[str, Any]) -> None:
        if len(form["value"]) < 2:
            return
        value = self.evaluate(form["value"][1], pure=True, fuel=128)
        self.events.append({"event": "compile-time-expression", "value_hash": stable_hash(value.representation), "span": form["span"]})

    def process_derive_code(self, form: dict[str, Any]) -> None:
        effects: list[dict[str, Any]] = []
        generated_form: dict[str, Any] | None = None
        generated_digest = "sha256:missing-generated-digest"
        for clause in form["value"][3:]:
            clause_head = list_head(clause)
            values = clause.get("value", [])
            if clause_head == ":build-effect":
                if len(values) != 6:
                    self.error(
                        "L12-BUILD-GRANT",
                        "build effect clause must name effect, provider, scope, result digest, and replay policy",
                        clause["span"],
                        "Emit a complete build effect event.",
                        requested_effect=None,
                    )
                effects.append(
                    {
                        "effect": atom_text(values[1]),
                        "provider": atom_text(values[2]),
                        "scope": atom_text(values[3]),
                        "result_digest": atom_text(values[4]),
                        "replay_policy": atom_text(values[5]),
                        "span": clause["span"],
                    }
                )
            elif clause_head == ":generated-form":
                if len(values) < 3:
                    self.error(
                        "L12-GENERATED-ILLEGAL",
                        "generated form clause must include syntax and output digest",
                        clause["span"],
                        "Record generated syntax and its output digest.",
                        generated_origin_chain=[],
                    )
                generated_form = values[1]
                generated_digest = atom_text(values[2])

        for effect in effects:
            self.record_build_effect(effect)

        origin_chain = [
            {
                "generator": "derive-code",
                "input_syntax_hash": syntax_hash(form),
                "source_span": form["span"],
                "phase": "compiler-extension-execution",
                "package": self.context["package"],
                "version": self.build_policy["compiler_version"],
                "build_effects": [effect["effect"] for effect in effects],
                "output_digest": generated_digest,
            }
        ]
        if generated_form is None or generated_illegal(generated_form):
            self.error(
                "L12-GENERATED-ILLEGAL",
                "generated code failed syntax, type, effect, capability, memory, or safety validation",
                form["span"],
                "Revalidate generated code through normal Gravity checks and preserve the generation origin chain.",
                generated_origin_chain=origin_chain,
            )

        self.generated_origin_chains.extend(origin_chain)
        self.generated_forms.append(
            {
                "kind": "generated-form",
                "form_hash": syntax_hash(generated_form),
                "output_digest": generated_digest,
                "origin_chain": origin_chain,
                "source_span": generated_form["span"],
            }
        )
        self.events.append(
            {
                "event": "generated-form-emitted",
                "generator": "derive-code",
                "target": atom_text(form["value"][1]),
                "schema": atom_text(form["value"][2]),
                "output_digest": generated_digest,
                "span": form["span"],
            }
        )

    def process_cached_result(self, form: dict[str, Any]) -> None:
        entries = keyword_pairs(form["value"][1:])
        hidden_effects = list_text(entries.get(":hidden-effects", syntax_vector([])))
        created_policy = atom_text(entries.get(":created-policy", syntax_keyword(":unknown")))
        current_policy = atom_text(entries.get(":current-policy", syntax_keyword(":unknown")))
        if hidden_effects or (created_policy == ":loose" and current_policy == ":strict"):
            self.error(
                "L12-CACHE-UNSAFE",
                "cached compile-time result was created under an incompatible or hidden-effect policy",
                form["span"],
                "Invalidate the cache entry and replay under the current build policy.",
            )

    def record_build_effect(self, effect: dict[str, Any]) -> None:
        grant = self.find_grant(effect["effect"], effect["provider"], effect["scope"])
        if grant is None:
            self.error(
                "L12-BUILD-GRANT",
                "no build grant authorizes the requested compile-time effect",
                effect["span"],
                "Declare a scoped grant for the package, namespace, provider, profile, and effect.",
                requested_effect=effect["effect"],
            )

        if self.build_policy["mode"] == ":hermetic":
            if effect["effect"] == ":build/read-file" and effect["scope"] not in self.build_policy["declared_inputs"]:
                self.error(
                    "L12-HERMETIC-INPUT",
                    "hermetic compile-time evaluation observed an undeclared file input",
                    effect["span"],
                    "Declare the input in the build policy, lockfile, or target manifest before reading it.",
                    requested_effect=effect["effect"],
                    relevant_grant=grant.to_record(),
                )
            if effect["effect"] == ":build/env" and effect["scope"] not in self.build_policy["declared_env"]:
                self.error(
                    "L12-HERMETIC-INPUT",
                    "hermetic compile-time evaluation observed an undeclared environment input",
                    effect["span"],
                    "Declare the environment key and its value policy before reading it.",
                    requested_effect=effect["effect"],
                    relevant_grant=grant.to_record(),
                )

        if effect["effect"] in NONDETERMINISTIC_EFFECTS and effect["replay_policy"] in {":none", ":ambient", ""}:
            self.error(
                "L12-NONDETERMINISM",
                "nondeterministic compile-time effect has no replay policy",
                effect["span"],
                "Pin the result, provide a replay provider, or record deterministic replay data.",
                requested_effect=effect["effect"],
                relevant_grant=grant.to_record(),
            )

        if self.context["profile"] in CONSTRAINED_PROFILES and effect["effect"] in {":build/network", ":build/exec", ":build/model-call", ":build/tool-call"}:
            self.error(
                "L12-BUILD-GRANT",
                "constrained profile cannot use hosted compile-time services for this effect",
                effect["span"],
                "Move the service behind a profile-legal provider or remove the effect.",
                requested_effect=effect["effect"],
                relevant_grant=grant.to_record(),
            )

        record = {
            "effect": effect["effect"],
            "provider": effect["provider"],
            "scope": effect["scope"],
            "package": self.context["package"],
            "namespace": self.context["namespace"],
            "profile": self.context["profile"],
            "target": self.context["target"],
            "grant": grant.to_record(),
            "result_digest": effect["result_digest"],
            "replay_policy": effect["replay_policy"],
            "source_span": effect["span"],
        }
        self.build_effect_log.append(record)
        if effect["effect"] == ":build/read-file":
            self.file_input_digests.append({"path": effect["scope"], "digest": effect["result_digest"]})
        elif effect["effect"] == ":build/write-artifact":
            self.output_artifact_digests.append({"path": effect["scope"], "digest": effect["result_digest"]})
        elif effect["effect"] == ":build/env":
            self.environment_key_reads.append({"key": effect["scope"], "redaction_policy": "redacted-presence", "digest": "redacted"})
        if effect["effect"] in NONDETERMINISTIC_EFFECTS:
            self.nondeterminism_replay_records.append(
                {"effect": effect["effect"], "scope": effect["scope"], "replay_policy": effect["replay_policy"], "result_digest": effect["result_digest"]}
            )

    def find_grant(self, effect: str, provider: str, scope: str) -> BuildGrant | None:
        for grant in self.grants:
            if grant.authorizes(effect, provider, scope, self.context["profile"]):
                return grant
        return None

    def evaluate(self, form: dict[str, Any], *, pure: bool, fuel: int) -> CompileValue:
        if fuel <= 0:
            self.error(
                "L12-FUEL",
                "compile-time evaluation exceeded deterministic fuel",
                form["span"],
                "Reduce compile-time recursion or provide an accepted totality proof.",
            )
        kind = form["kind"]
        if kind in {"integer", "float", "ratio", "string", "boolean", "keyword", "nil", "character"}:
            return CompileValue(form["value"], form["value"])
        if kind == "symbol":
            symbol = atom_text(form)
            if symbol in self.runtime_bindings:
                self.error(
                    "L12-PHASE-CAPTURE",
                    "compile-time code captured a runtime-only binding",
                    form["span"],
                    "Pass serializable compile-time data instead of runtime-only values.",
                )
            return CompileValue(symbol, {"symbol": symbol})
        if kind == "vector":
            values = [self.evaluate(item, pure=pure, fuel=fuel - 1) for item in form["value"]]
            return CompileValue([value.value for value in values], [value.representation for value in values], all(value.stable for value in values))
        if kind == "map":
            entries = []
            stable = True
            for entry in form["value"]:
                key = self.evaluate(entry["key"], pure=pure, fuel=fuel - 1)
                value = self.evaluate(entry["value"], pure=pure, fuel=fuel - 1)
                stable = stable and key.stable and value.stable
                entries.append([key.representation, value.representation])
            return CompileValue(dict(entries), entries, stable)
        if kind != "list":
            return CompileValue(form.get("value"), form.get("value"))

        head = list_head(form)
        args = form["value"][1:]
        if head == "quote" and args:
            return CompileValue(form_to_data(args[0]), form_to_data(args[0]))
        if head in {"+", "*", "-"}:
            values = [self.evaluate(arg, pure=pure, fuel=fuel - 1).value for arg in args]
            if head == "+":
                result = sum(values)
            elif head == "*":
                result = 1
                for value in values:
                    result *= value
            else:
                result = values[0] - sum(values[1:]) if values else 0
            return CompileValue(result, result)
        if head in {"vector", "pure/vector"}:
            values = [self.evaluate(arg, pure=pure, fuel=fuel - 1) for arg in args]
            return CompileValue([value.value for value in values], [value.representation for value in values], all(value.stable for value in values))
        if head == "pure/generate-table":
            count = self.evaluate(args[0], pure=pure, fuel=fuel - 1).value if args else 0
            table = [index * index for index in range(int(count))]
            return CompileValue(table, table)
        if head == "pure/layout-size":
            layout = atom_text(args[0]) if args else ":word"
            sizes = {":word": 8, ":halfword": 4, ":byte": 1}
            return CompileValue(sizes.get(layout, 8), sizes.get(layout, 8))
        if head == "runtime/value":
            self.error(
                "L12-PHASE-CAPTURE",
                "compile-time evaluation attempted to capture a runtime-only value",
                form["span"],
                "Move the value into a declared compile-time input or evaluate it at runtime.",
            )
        if head == "secret/embed":
            self.error(
                "L12-SECRET-LEAK",
                "secret value would be embedded in generated output, diagnostics, or public provenance",
                form["span"],
                "Record only the secret name and a redacted presence marker in private provenance.",
            )
        if head == "unstable/handle":
            return CompileValue({"host_handle": atom_text(args[0]) if args else "unknown"}, {"host_handle": "unrepresentable"}, stable=False)
        if head == "loop-forever":
            self.error(
                "L12-FUEL",
                "compile-time evaluation exceeded deterministic fuel",
                form["span"],
                "Reduce compile-time recursion or provide an accepted totality proof.",
            )
        if head in BUILD_EFFECT_HEADS:
            requested_effect = BUILD_EFFECT_HEADS[head]
            if pure:
                self.error(
                    "L12-PURE-EFFECT",
                    "pure constant evaluation attempted a build effect",
                    form["span"],
                    "Move the operation behind an authorized build effect or keep the expression pure.",
                    requested_effect=requested_effect,
                )
            return CompileValue({"effect": requested_effect}, {"effect": requested_effect})

        return CompileValue({"call": head, "args": [form_to_data(arg) for arg in args]}, {"call": head, "args": [form_to_data(arg) for arg in args]})

    def error(
        self,
        code: str,
        message: str,
        span: dict[str, Any],
        remediation: str,
        *,
        requested_effect: str | None = None,
        relevant_grant: dict[str, Any] | None = None,
        generated_origin_chain: list[dict[str, Any]] | None = None,
    ) -> None:
        raise CompileTimeError(
            code,
            message,
            span,
            remediation,
            profile=self.context["profile"],
            target=self.context["target"],
            requested_effect=requested_effect,
            relevant_grant=relevant_grant,
            generated_origin_chain=generated_origin_chain,
        )


def analyze_compile_time_source(source: str, source_path: str) -> dict[str, Any]:
    return CompileTimeAnalyzer(source, source_path).analyze()


def compile_time_diagnostic(source: str, source_path: str) -> dict[str, Any] | None:
    try:
        analyze_compile_time_source(source, source_path)
    except CompileTimeError as exc:
        return exc.to_diagnostic()
    return None


def list_head(form: dict[str, Any]) -> str | None:
    if not isinstance(form, dict) or form.get("kind") != "list" or not form.get("value"):
        return None
    head = form["value"][0]
    if head.get("kind") in {"symbol", "keyword"}:
        return str(head.get("value"))
    return None


def atom_text(form: dict[str, Any]) -> str:
    value = form.get("value")
    if value is None:
        return "nil"
    return str(value)


def list_text(form: dict[str, Any]) -> list[str]:
    if form.get("kind") in {"vector", "list", "set"}:
        return [atom_text(item) for item in form.get("value", [])]
    if form.get("kind") == "nil":
        return []
    return [atom_text(form)]


def keyword_pairs(forms: list[dict[str, Any]]) -> dict[str, dict[str, Any]]:
    result: dict[str, dict[str, Any]] = {}
    index = 0
    while index + 1 < len(forms):
        key = forms[index]
        if key.get("kind") == "keyword":
            result[str(key["value"])] = forms[index + 1]
        index += 2
    return result


def generated_illegal(form: dict[str, Any]) -> bool:
    head = list_head(form)
    if head in GENERATED_ILLEGAL_HEADS:
        return True
    if form.get("kind") == "map":
        return any(generated_illegal(entry["key"]) or generated_illegal(entry["value"]) for entry in form["value"])
    value = form.get("value")
    if isinstance(value, list):
        return any(isinstance(item, dict) and generated_illegal(item) for item in value)
    return False


def form_to_data(form: dict[str, Any]) -> Any:
    kind = form["kind"]
    if kind in {"integer", "float", "ratio", "string", "boolean", "keyword", "nil", "character", "symbol"}:
        return form["value"]
    if kind in {"vector", "list", "set"}:
        return [form_to_data(item) for item in form["value"]]
    if kind == "map":
        return [[form_to_data(entry["key"]), form_to_data(entry["value"])] for entry in form["value"]]
    return form.get("value")


def syntax_hash(form: dict[str, Any]) -> str:
    return stable_hash(form_to_data(form))


def stable_hash(value: Any) -> str:
    payload = json.dumps(value, sort_keys=True, separators=(",", ":"), default=str).encode("utf-8")
    return "sha256:" + hashlib.sha256(payload).hexdigest()


def source_hash(source: str) -> str:
    return "sha256:" + hashlib.sha256(source.encode("utf-8")).hexdigest()


def syntax_keyword(value: str) -> dict[str, Any]:
    return {"kind": "keyword", "value": value}


def syntax_string(value: str) -> dict[str, Any]:
    return {"kind": "string", "value": value}


def syntax_vector(items: list[dict[str, Any]]) -> dict[str, Any]:
    return {"kind": "vector", "value": items}
