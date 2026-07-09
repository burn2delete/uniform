"""L3 namespace and module analysis."""

from __future__ import annotations

import hashlib
from dataclasses import dataclass
from typing import Any

from gravity.reader import ReaderError, read_source


class NamespaceError(Exception):
    def __init__(self, code: str, message: str, span: dict[str, Any], remediation: str):
        super().__init__(message)
        self.code = code
        self.message = message
        self.span = span
        self.remediation = remediation

    def to_diagnostic(self) -> dict[str, Any]:
        return {
            "id": self.code,
            "message": self.message,
            "span": self.span,
            "remediation": self.remediation,
            "analyzer_stage": "namespace",
        }


@dataclass(frozen=True)
class ModuleContext:
    package: str = "fixture/package"
    package_capabilities: frozenset[str] = frozenset()
    known_module_profiles: dict[str, str] | None = None
    known_public_api: dict[str, frozenset[str]] | None = None
    allowed_cross_profile_boundaries: frozenset[tuple[str, str]] = frozenset()


def analyze_source(source: str, source_path: str, context: ModuleContext | None = None) -> dict[str, Any]:
    context = context or ModuleContext()
    try:
        forms = read_source(source, source_path=source_path)
    except ReaderError as exc:
        raise NamespaceError(exc.code, exc.message, exc.span, exc.remediation) from exc
    return analyze_forms(forms, source, source_path, context)


def analyze_forms(forms: list[dict[str, Any]], source: str, source_path: str, context: ModuleContext) -> dict[str, Any]:
    if not forms or not is_ns_form(forms[0]):
        span = forms[0]["span"] if forms else empty_span(source_path)
        raise NamespaceError("L3-NS-MISSING", "file has implementation forms but no ns declaration", span, "Start the file with an ns form.")

    ns_form = forms[0]
    clauses = parse_ns_clauses(ns_form)
    profiles = clauses.get(":profile", []) + clauses.get(":profiles", [])
    if not profiles:
        raise NamespaceError("L3-NS-MISSING", "namespace does not declare an active profile", ns_form["span"], "Add (:profile p) to implementation namespaces.")
    if len(profiles) != 1:
        raise NamespaceError("L3-PROFILE-MULTIPLE", "implementation namespace declares more than one active profile", ns_form["span"], "Use exactly one (:profile p) for implementation namespaces.")

    namespace = ns_form["value"][1]["value"]
    profile = profiles[0]
    target = single_or_none(clauses.get(":target", []))
    exports = clauses.get(":exports", [])
    effects = clauses.get(":effects", [])
    capabilities = clauses.get(":capabilities", [])
    safety = single_or_none(clauses.get(":safety", []))
    providers = clauses.get(":providers", [])
    metadata = clauses.get(":metadata", [])
    requires = parse_require_entries(clauses.get(":requires/raw", []), profile, context)
    imports = parse_require_entries(clauses.get(":imports/raw", []), profile, context, foreign=True)

    missing_capabilities = sorted(set(capabilities) - set(context.package_capabilities))
    if missing_capabilities:
        raise NamespaceError(
            "L3-CAPABILITY-MISSING",
            f"namespace requires capabilities absent from package grants: {missing_capabilities}",
            ns_form["span"],
            "Add package or deployment grants for the required capabilities.",
        )

    aliases = {entry["alias"]: entry["module"] for entry in requires + imports if entry.get("alias")}
    check_refer_conflicts(requires)
    check_alias_uses(forms[1:], aliases, namespace)
    inferred_effects = infer_namespace_effects(forms[1:])
    widened_effects = sorted(set(inferred_effects) - set(effects))
    if widened_effects:
        raise NamespaceError(
            "L3-EFFECT-WIDEN",
            f"inferred namespace effects exceed declaration: {widened_effects}",
            ns_form["span"],
            "Declare the namespace effects or remove the effectful operations.",
        )

    definitions = collect_definitions(forms[1:], exports)
    source_hash = "sha256:" + hashlib.sha256(source.encode("utf-8")).hexdigest()
    definitions_hash = "sha256:" + hashlib.sha256(
        "\n".join(f"{item['kind']}:{item['name']}:{item['visibility']}" for item in definitions).encode("utf-8")
    ).hexdigest()

    return {
        "kind": "module-artifact",
        "module": namespace,
        "package": context.package,
        "profile": profile,
        "target": target,
        "source_path": source_path,
        "source_hash": source_hash,
        "definitions_hash": definitions_hash,
        "exports": exports,
        "requires": requires,
        "imports": imports,
        "alias_table": aliases,
        "effects": effects,
        "capabilities": capabilities,
        "safety": safety,
        "providers": providers,
        "metadata": metadata,
        "definitions": definitions,
        "dependency_graph": {
            "module": namespace,
            "dependencies": [entry["module"] for entry in requires + imports],
        },
        "public_api_manifest": {
            "module": namespace,
            "exports": exports,
        },
    }


def empty_span(source_path: str) -> dict[str, Any]:
    return {
        "source": source_path,
        "start_byte": 0,
        "end_byte": 0,
        "start_line": 1,
        "start_column": 1,
        "end_line": 1,
        "end_column": 1,
    }


def is_ns_form(form: dict[str, Any]) -> bool:
    return (
        form["kind"] == "list"
        and len(form["value"]) >= 2
        and form["value"][0]["kind"] == "symbol"
        and form["value"][0]["value"] == "ns"
        and form["value"][1]["kind"] == "symbol"
    )


def parse_ns_clauses(ns_form: dict[str, Any]) -> dict[str, list[Any]]:
    clauses: dict[str, list[Any]] = {}
    for clause in ns_form["value"][2:]:
        if clause["kind"] != "list" or not clause["value"]:
            raise NamespaceError("L1-NS-SHAPE", "namespace clause must be a non-empty list", clause["span"], "Use a list clause such as (:profile :hosted).")
        head = clause["value"][0]
        if head["kind"] != "keyword":
            raise NamespaceError("L1-NS-SHAPE", "namespace clause head must be a keyword", head["span"], "Use a documented namespace clause keyword.")
        key = head["value"]
        body = clause["value"][1:]
        if key in {":requires", ":imports"}:
            clauses.setdefault(f"{key}/raw", []).extend(body)
        else:
            clauses.setdefault(key, []).extend(flatten_clause_values(body))
    return clauses


def flatten_clause_values(values: list[dict[str, Any]]) -> list[Any]:
    flattened: list[Any] = []
    for value in values:
        if value["kind"] in {"set", "vector"}:
            flattened.extend(atom_value(item) for item in value["value"])
        elif value["kind"] == "map":
            flattened.append(map_value(value))
        else:
            flattened.append(atom_value(value))
    return flattened


def atom_value(form: dict[str, Any]) -> Any:
    return form["value"]


def map_value(form: dict[str, Any]) -> dict[Any, Any]:
    return {atom_value(entry["key"]): atom_value(entry["value"]) for entry in form["value"]}


def single_or_none(values: list[Any]) -> Any:
    return values[0] if values else None


def parse_require_entries(values: list[dict[str, Any]], profile: str, context: ModuleContext, foreign: bool = False) -> list[dict[str, Any]]:
    entries: list[dict[str, Any]] = []
    known_profiles = context.known_module_profiles or {}
    known_public_api = context.known_public_api or {}
    for value in values:
        groups = value["value"] if value["kind"] == "vector" else [value]
        for group in groups:
            if group["kind"] == "symbol":
                entries.append(module_entry(group["value"], None, [], profile, known_profiles, context, group["span"], foreign))
                continue
            if group["kind"] != "vector" or not group["value"] or group["value"][0]["kind"] != "symbol":
                raise NamespaceError("L1-NS-SHAPE", "requires/imports entries must be symbols or vectors", group["span"], "Use [module.name :as alias].")
            module = group["value"][0]["value"]
            alias = None
            refer: list[str] = []
            items = group["value"][1:]
            index = 0
            while index < len(items):
                item = items[index]
                next_item = items[index + 1] if index + 1 < len(items) else None
                if item["kind"] == "keyword" and item["value"] == ":as" and next_item and next_item["kind"] == "symbol":
                    alias = next_item["value"]
                    index += 2
                    continue
                if item["kind"] == "keyword" and item["value"] == ":refer" and next_item and next_item["kind"] == "vector":
                    refer = [entry["value"] for entry in next_item["value"] if entry["kind"] == "symbol"]
                    index += 2
                    continue
                index += 1
            if any(item["kind"] == "symbol" and item["value"] == "*" for item in items):
                raise NamespaceError("L3-UNKNOWN-ALIAS", "wildcard stable imports are rejected", group["span"], "Import names explicitly through an alias or public API.")
            if refer and module in known_public_api:
                private = sorted(set(refer) - set(known_public_api[module]))
                if private:
                    raise NamespaceError(
                        "L3-PRIVATE-IMPORT",
                        f"module {module} does not export referenced names: {private}",
                        group["span"],
                        "Import only public API names or expose the definition through the provider module.",
                    )
            entries.append(module_entry(module, alias, refer, profile, known_profiles, context, group["span"], foreign))
    return entries


def module_entry(
    module: str,
    alias: str | None,
    refer: list[str],
    profile: str,
    known_profiles: dict[str, str],
    context: ModuleContext,
    span: dict[str, Any],
    foreign: bool,
) -> dict[str, Any]:
    dependency_profile = known_profiles.get(module, profile)
    if dependency_profile != profile and dependency_profile != ":core":
        if (profile, module) not in context.allowed_cross_profile_boundaries:
            raise NamespaceError(
                "L3-CROSS-PROFILE",
                f"import {module} crosses from {profile} to {dependency_profile} without boundary",
                span,
                "Use a :core API, facade, schema boundary, FFI boundary, or package artifact boundary.",
            )
    return {
        "module": module,
        "alias": alias,
        "refer": refer,
        "profile": dependency_profile,
        "foreign": foreign,
        "span": span,
    }


def check_refer_conflicts(requires: list[dict[str, Any]]) -> None:
    owners: dict[str, dict[str, Any]] = {}
    for entry in requires:
        for name in entry.get("refer", []):
            if name in owners:
                raise NamespaceError(
                    "L3-AMBIGUOUS-NAME",
                    f"referred name {name} is imported from both {owners[name]['module']} and {entry['module']}",
                    entry["span"],
                    "Use aliases or explicit qualification to remove the ambiguity.",
                )
            owners[name] = entry


def check_alias_uses(forms: list[dict[str, Any]], aliases: dict[str, str], namespace: str) -> None:
    for form in walk(forms):
        if form["kind"] != "symbol":
            continue
        value = form["value"]
        if "/" not in value:
            continue
        qualifier = value.split("/", 1)[0]
        if qualifier in aliases or qualifier == namespace or "." in qualifier:
            continue
        if qualifier in {"quote", "syntax-quote"}:
            continue
        raise NamespaceError("L3-UNKNOWN-ALIAS", f"qualified symbol uses unknown alias {qualifier}", form["span"], "Declare the alias in :requires or use a fully qualified namespace.")


def collect_definitions(forms: list[dict[str, Any]], exports: list[str]) -> list[dict[str, Any]]:
    definitions: list[dict[str, Any]] = []
    export_set = set(exports)
    for form in forms:
        if form["kind"] != "list" or len(form["value"]) < 2:
            continue
        head = form["value"][0]
        name = form["value"][1]
        if head["kind"] == "symbol" and name["kind"] == "symbol" and head["value"] in {"def", "defn", "defmacro", "defschema", "defprotocol"}:
            definitions.append(
                {
                    "name": name["value"],
                    "kind": head["value"],
                    "visibility": "public" if name["value"] in export_set else "private",
                    "source_span": name["span"],
                    "latent_effects": [],
                    "required_capabilities": [],
                    "profile_restrictions": [],
                    "safety_mode": None,
                    "artifact_links": [],
                }
            )
    return definitions


def walk(forms: list[dict[str, Any]]):
    for form in forms:
        yield form
        if form["kind"] == "map":
            for entry in form["value"]:
                yield from walk([entry["key"], entry["value"]])
        elif form["kind"] == "tagged":
            yield from walk([form["value"]["form"]])
        elif isinstance(form.get("value"), list):
            yield from walk([item for item in form["value"] if isinstance(item, dict)])


def infer_namespace_effects(forms: list[dict[str, Any]]) -> list[str]:
    call_effects = {
        "println": ":io/write",
        "listen": ":network/listen",
    }
    effects: list[str] = []
    for form in walk(forms):
        if form["kind"] != "list" or not form.get("value"):
            continue
        head = form["value"][0]
        if head.get("kind") != "symbol":
            continue
        effect = call_effects.get(head.get("value"))
        if effect and effect not in effects:
            effects.append(effect)
    return effects


def analyze_source_diagnostic(source: str, source_path: str, context: ModuleContext | None = None) -> dict[str, Any] | None:
    try:
        analyze_source(source, source_path, context=context)
    except NamespaceError as exc:
        return exc.to_diagnostic()
    return None
