"""L4 macro expansion over Gravity syntax objects."""

from __future__ import annotations

import copy
import hashlib
import json
from dataclasses import dataclass
from typing import Any, Callable

from gravity.reader import ReaderError, read_source


MacroFn = Callable[[dict[str, Any], list[dict[str, Any]], "ExpansionConfig"], Any]


@dataclass(frozen=True)
class MacroDefinition:
    name: str
    namespace: str
    version: str
    expand: MacroFn
    build_effects: frozenset[str] = frozenset()
    hygiene_policy: str = "hygienic"
    attach_provenance: bool = True


@dataclass(frozen=True)
class ExpansionConfig:
    build_grants: frozenset[str] = frozenset()
    allow_unsafe: bool = False
    max_depth: int = 16


class MacroExpansionError(Exception):
    def __init__(self, code: str, message: str, span: dict[str, Any], remediation: str, generated_span: dict[str, Any] | None = None):
        super().__init__(message)
        self.code = code
        self.message = message
        self.span = span
        self.generated_span = generated_span
        self.remediation = remediation

    def to_diagnostic(self) -> dict[str, Any]:
        diagnostic = {
            "id": self.code,
            "message": self.message,
            "span": self.span,
            "remediation": self.remediation,
            "analyzer_stage": "macro-expansion",
        }
        if self.generated_span is not None:
            diagnostic["generated_span"] = self.generated_span
        return diagnostic


class MacroExpander:
    def __init__(self, registry: dict[str, MacroDefinition] | None = None, config: ExpansionConfig | None = None):
        self.registry = registry or default_registry()
        self.config = config or ExpansionConfig()
        self.expansions: list[dict[str, Any]] = []

    def expand_source(self, source: str, source_path: str) -> dict[str, Any]:
        try:
            forms = read_source(source, source_path=source_path)
        except ReaderError as exc:
            raise MacroExpansionError(exc.code, exc.message, exc.span, exc.remediation) from exc

        module = module_name(forms)
        expanded_forms = [self.expand_form(form, caller_profile=form.get("profile_context"), depth=0) for form in forms]
        return {
            "kind": "macro-expansion-trace",
            "module": module,
            "expansions": self.expansions,
            "diagnostics": [],
            "expanded_forms": expanded_forms,
        }

    def expand_form(self, form: dict[str, Any], caller_profile: str | None, depth: int) -> dict[str, Any]:
        if form["kind"] == "map":
            result = clone(form)
            result["value"] = [
                {
                    "key": self.expand_form(entry["key"], caller_profile, depth),
                    "value": self.expand_form(entry["value"], caller_profile, depth),
                }
                for entry in form["value"]
            ]
            return result

        if form["kind"] != "list" or not form["value"]:
            return clone(form)

        head = form["value"][0]
        if head["kind"] == "symbol" and head["value"] == "quote":
            return clone(form)

        macro = self.resolve_macro(head)
        if macro is not None:
            if depth >= self.config.max_depth:
                raise MacroExpansionError(
                    "L4-EXPANSION-DEPTH",
                    "macro expansion exceeded configured depth",
                    form["span"],
                    "Reduce recursive macro expansion or raise the explicit expansion limit.",
                )
            return self.expand_macro_call(macro, form, caller_profile, depth)

        result = clone(form)
        result["value"] = [self.expand_form(item, caller_profile, depth) if isinstance(item, dict) else item for item in form["value"]]
        return result

    def resolve_macro(self, head: dict[str, Any]) -> MacroDefinition | None:
        if head.get("kind") != "symbol":
            return None
        name = head.get("value")
        return self.registry.get(name) or self.registry.get(f"gravity.core/{name}")

    def expand_macro_call(self, macro: MacroDefinition, call: dict[str, Any], caller_profile: str | None, depth: int) -> dict[str, Any]:
        ungranted = sorted(macro.build_effects - self.config.build_grants)
        if ungranted:
            raise MacroExpansionError(
                "L4-BUILD-EFFECT",
                f"macro used undeclared or ungranted build effects: {ungranted}",
                call["span"],
                "Declare and grant the macro build effects in build policy.",
            )

        input_hash = syntax_hash(call)
        output = macro.expand(call, call["value"][1:], self.config)
        if not isinstance(output, dict) or "kind" not in output:
            raise MacroExpansionError(
                "L4-MACRO-NOT-SYNTAX",
                "macro returned a non-syntax value",
                call["span"],
                "Return syntax objects from macro expansion.",
            )

        if macro.attach_provenance:
            annotate_generated(output, macro, call, list(macro.build_effects))
        copy_call_metadata(call, output)
        ensure_generated_provenance(output, call)
        ensure_profile_legality(output, caller_profile, call)
        ensure_unsafe_policy(output, self.config, call)

        output_hash = syntax_hash(output)
        self.expansions.append(
            {
                "macro": f"{macro.namespace}/{macro.name}",
                "macro_version": macro.version,
                "call_span": call["span"],
                "input_hash": input_hash,
                "output_hash": output_hash,
                "build_effects": sorted(macro.build_effects),
                "generated_spans": generated_spans(output),
                "diagnostics": [],
            }
        )
        return self.expand_form(output, caller_profile, depth + 1)


def default_registry() -> dict[str, MacroDefinition]:
    definitions = [
        MacroDefinition("when", "gravity.core", "fixture-1", expand_when),
        MacroDefinition("read-env", "gravity.core", "fixture-1", expand_read_env, build_effects=frozenset({":build/env"})),
        MacroDefinition("host-reflect", "gravity.core", "fixture-1", expand_host_reflect),
        MacroDefinition("unsafe-nth", "gravity.core", "fixture-1", expand_unsafe_nth),
        MacroDefinition("recur-macro", "gravity.core", "fixture-1", expand_recur_macro),
        MacroDefinition("return-text", "gravity.core", "fixture-1", expand_return_text),
        MacroDefinition("drop-origin", "gravity.core", "fixture-1", expand_drop_origin, attach_provenance=False),
        MacroDefinition("capture-bug", "gravity.core", "fixture-1", expand_capture_bug),
    ]
    registry: dict[str, MacroDefinition] = {}
    for definition in definitions:
        registry[definition.name] = definition
        registry[f"{definition.namespace}/{definition.name}"] = definition
    return registry


def expand_when(call: dict[str, Any], args: list[dict[str, Any]], config: ExpansionConfig) -> dict[str, Any]:
    condition = clone(args[0]) if args else generated_atom("nil", None, call)
    body = [clone(arg) for arg in args[1:]]
    do_form = generated_list([generated_atom("symbol", "do", call), *body], call)
    return generated_list([generated_atom("symbol", "if", call), condition, do_form, generated_atom("nil", None, call)], call)


def expand_read_env(call: dict[str, Any], args: list[dict[str, Any]], config: ExpansionConfig) -> dict[str, Any]:
    return generated_list([generated_atom("symbol", "env/read", call), *(clone(arg) for arg in args)], call)


def expand_host_reflect(call: dict[str, Any], args: list[dict[str, Any]], config: ExpansionConfig) -> dict[str, Any]:
    return generated_list([generated_atom("symbol", "host/reflect", call), *(clone(arg) for arg in args)], call)


def expand_unsafe_nth(call: dict[str, Any], args: list[dict[str, Any]], config: ExpansionConfig) -> dict[str, Any]:
    unsafe_call = generated_list([generated_atom("symbol", "vector/get-unchecked", call), *(clone(arg) for arg in args)], call)
    return generated_list([generated_atom("symbol", "unsafe", call), unsafe_call], call)


def expand_recur_macro(call: dict[str, Any], args: list[dict[str, Any]], config: ExpansionConfig) -> dict[str, Any]:
    return clone(call)


def expand_return_text(call: dict[str, Any], args: list[dict[str, Any]], config: ExpansionConfig) -> str:
    return "not syntax"


def expand_drop_origin(call: dict[str, Any], args: list[dict[str, Any]], config: ExpansionConfig) -> dict[str, Any]:
    return generated_list([generated_atom("symbol", "do", call), *(clone(arg) for arg in args)], call)


def expand_capture_bug(call: dict[str, Any], args: list[dict[str, Any]], config: ExpansionConfig) -> dict[str, Any]:
    raise MacroExpansionError(
        "L4-HYGIENE-CAPTURE",
        "expansion would accidentally capture a caller binding",
        call["span"],
        "Use explicit capture syntax and record the capture in the macro contract.",
    )


def clone(form: dict[str, Any]) -> dict[str, Any]:
    return copy.deepcopy(form)


def generated_atom(kind: str, value: Any, call: dict[str, Any]) -> dict[str, Any]:
    return {
        "kind": kind,
        "value": value,
        "span": generated_span(call),
        "metadata": [],
        "namespace_context": call.get("namespace_context"),
        "profile_context": call.get("profile_context"),
        "reader_origin": [{"kind": "macro-generated"}],
    }


def generated_list(items: list[dict[str, Any]], call: dict[str, Any]) -> dict[str, Any]:
    return {
        "kind": "list",
        "value": items,
        "span": generated_span(call),
        "metadata": [],
        "namespace_context": call.get("namespace_context"),
        "profile_context": call.get("profile_context"),
        "reader_origin": [{"kind": "macro-generated"}],
    }


def generated_span(call: dict[str, Any]) -> dict[str, Any]:
    span = call["span"]
    return {
        "source": span["source"],
        "start_byte": span["start_byte"],
        "end_byte": span["end_byte"],
        "start_line": span["start_line"],
        "start_column": span["start_column"],
        "end_line": span["end_line"],
        "end_column": span["end_column"],
        "generated": True,
    }


def annotate_generated(form: dict[str, Any], macro: MacroDefinition, call: dict[str, Any], build_effects: list[str]) -> None:
    for node in walk(form):
        node.setdefault("generated_origin_chain", []).append(
            {
                "macro": f"{macro.namespace}/{macro.name}",
                "macro_version": macro.version,
                "call_span": call["span"],
            }
        )
        node.setdefault("hygiene_marks", []).append(f"{macro.namespace}/{macro.name}:{macro.version}")
        node["compile_phase"] = "macro-expansion"
        node["active_macro_namespace"] = macro.namespace
        node["build_effect_context"] = build_effects


def copy_call_metadata(call: dict[str, Any], output: dict[str, Any]) -> None:
    if call.get("metadata") and not output.get("metadata"):
        output["metadata"] = copy.deepcopy(call["metadata"])


def ensure_generated_provenance(output: dict[str, Any], call: dict[str, Any]) -> None:
    for node in walk(output):
        if not node.get("generated_origin_chain"):
            raise MacroExpansionError(
                "L4-PROVENANCE-MISSING",
                "expansion output lacks generated-origin metadata",
                call["span"],
                "Attach generated-origin metadata to macro output syntax objects.",
                generated_span=node.get("span"),
            )


def ensure_profile_legality(output: dict[str, Any], caller_profile: str | None, call: dict[str, Any]) -> None:
    if caller_profile in {":kernel", ":core", ":firmware", ":hardware"} and contains_operator(output, "host/reflect"):
        generated = first_operator_span(output, "host/reflect")
        raise MacroExpansionError(
            "L4-GENERATED-PROFILE",
            "generated code violates caller profile",
            call["span"],
            "Use a profile-legal macro or move host reflection behind an allowed interop boundary.",
            generated_span=generated,
        )


def ensure_unsafe_policy(output: dict[str, Any], config: ExpansionConfig, call: dict[str, Any]) -> None:
    if contains_operator(output, "unsafe") and not config.allow_unsafe:
        generated = first_operator_span(output, "unsafe")
        raise MacroExpansionError(
            "L4-GENERATED-UNSAFE",
            "macro generated unsafe code not allowed by policy",
            call["span"],
            "Permit unsafe macro output explicitly or use a safe wrapper macro.",
            generated_span=generated,
        )


def contains_operator(form: dict[str, Any], operator: str) -> bool:
    return first_operator_span(form, operator) is not None


def first_operator_span(form: dict[str, Any], operator: str) -> dict[str, Any] | None:
    for node in walk(form):
        if node["kind"] == "list" and node["value"]:
            head = node["value"][0]
            if isinstance(head, dict) and head.get("kind") == "symbol" and head.get("value") == operator:
                return node["span"]
    return None


def generated_spans(form: dict[str, Any]) -> list[dict[str, Any]]:
    spans = []
    for node in walk(form):
        span = node.get("span", {})
        if span.get("generated"):
            spans.append(span)
    return spans


def walk(form: dict[str, Any]):
    yield form
    if form["kind"] == "map":
        for entry in form["value"]:
            yield from walk(entry["key"])
            yield from walk(entry["value"])
        return
    value = form.get("value")
    if isinstance(value, list):
        for item in value:
            if isinstance(item, dict):
                yield from walk(item)


def syntax_hash(form: dict[str, Any]) -> str:
    data = json.dumps(form, sort_keys=True, separators=(",", ":"))
    return "sha256:" + hashlib.sha256(data.encode("utf-8")).hexdigest()


def module_name(forms: list[dict[str, Any]]) -> str:
    if forms and forms[0]["kind"] == "list" and len(forms[0]["value"]) > 1 and forms[0]["value"][1]["kind"] == "symbol":
        return forms[0]["value"][1]["value"]
    return "<unknown>"


def expand_source_to_trace(source: str, source_path: str, config: ExpansionConfig | None = None) -> dict[str, Any]:
    return MacroExpander(config=config).expand_source(source, source_path)


def expand_source_diagnostic(source: str, source_path: str, config: ExpansionConfig | None = None) -> dict[str, Any] | None:
    try:
        expand_source_to_trace(source, source_path, config=config)
    except MacroExpansionError as exc:
        return exc.to_diagnostic()
    return None
