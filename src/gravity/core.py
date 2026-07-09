"""L2 lowering from expanded syntax objects to Gravity core AST artifacts."""

from __future__ import annotations

import hashlib
import json
from dataclasses import dataclass, field
from typing import Any

from gravity.macros import MacroExpansionError, expand_source_to_trace


CORE_SPECIAL_FORMS = {
    "quote",
    "if",
    "do",
    "let",
    "fn",
    "loop",
    "recur",
    "def",
    "var",
    "set!",
    "try",
    "throw",
    "match",
}
SURFACE_FORMS_REQUIRING_LOWERING = {
    "case",
    "cond",
    "defagent",
    "defn",
    "defschema",
    "defworkflow",
    "when",
    "with-open",
    "with-region",
}
CONSTRAINED_THROW_PROFILES = {":core", ":kernel", ":firmware", ":hardware"}


@dataclass
class LexicalBinding:
    name: str
    mutable: bool = False


@dataclass
class LoweringContext:
    lexical_scopes: list[dict[str, LexicalBinding]] = field(default_factory=lambda: [{}])
    recur_targets: list[int] = field(default_factory=list)

    def child(self) -> "LoweringContext":
        return LoweringContext(
            lexical_scopes=[scope.copy() for scope in self.lexical_scopes],
            recur_targets=list(self.recur_targets),
        )

    def push_scope(self) -> None:
        self.lexical_scopes.append({})

    def bind(self, name: str, mutable: bool = False) -> None:
        self.lexical_scopes[-1][name] = LexicalBinding(name=name, mutable=mutable)

    def lookup(self, name: str) -> LexicalBinding | None:
        for scope in reversed(self.lexical_scopes):
            if name in scope:
                return scope[name]
        return None


class CoreLoweringError(Exception):
    def __init__(self, code: str, message: str, span: dict[str, Any], remediation: str, syntax: dict[str, Any] | None = None):
        super().__init__(message)
        self.code = code
        self.message = message
        self.span = span
        self.remediation = remediation
        self.syntax = syntax

    def to_diagnostic(self) -> dict[str, Any]:
        diagnostic = {
            "id": self.code,
            "message": self.message,
            "span": self.span,
            "remediation": self.remediation,
            "analyzer_stage": "core-lowering",
        }
        if self.syntax and self.syntax.get("generated_origin_chain"):
            diagnostic["generated_origin_chain"] = self.syntax["generated_origin_chain"]
        if self.span.get("generated"):
            diagnostic["generated_span"] = self.span
        return diagnostic


class CoreLowerer:
    def __init__(self) -> None:
        self.next_id = 1
        self.source_map: list[dict[str, Any]] = []
        self.kind_counts: dict[str, int] = {}
        self.evaluation_order_records: list[dict[str, Any]] = []
        self.latent_function_effects: list[dict[str, Any]] = []
        self.call_records: list[dict[str, Any]] = []

    def lower_source(self, source: str, source_path: str) -> dict[str, Any]:
        trace = expand_source_to_trace(source, source_path)
        module = trace["module"]
        profile = module_profile(trace["expanded_forms"])
        context = LoweringContext()
        top_level = []
        for form in trace["expanded_forms"]:
            if is_ns_form(form):
                continue
            top_level.append(self.lower_expr(form, context, position="top-level"))

        return {
            "kind": "core-ast-artifact",
            "module": module,
            "source": source_path,
            "profile": profile,
            "macro_expansion_count": len(trace["expansions"]),
            "macro_trace_hash": artifact_hash(trace),
            "top_level": top_level,
            "core_form_kind_records": [
                {"kind": kind, "count": self.kind_counts[kind]}
                for kind in sorted(self.kind_counts)
            ],
            "source_map": self.source_map,
            "evaluation_order_records": self.evaluation_order_records,
            "latent_function_effects": self.latent_function_effects,
            "call_records": self.call_records,
            "diagnostics": [],
        }

    def lower_expr(self, syntax: dict[str, Any], context: LoweringContext, position: str) -> dict[str, Any]:
        kind = syntax["kind"]
        if kind in {"nil", "boolean", "integer", "float", "ratio", "string", "character", "keyword"}:
            return self.node("core/literal", syntax, value=syntax.get("value"), value_kind=kind, evaluation_order=[])
        if kind == "symbol":
            name = syntax["value"]
            if is_host_semantics(name):
                self.error(
                    "L2-HOST-SEMANTICS",
                    "host-specific behavior is not represented in Gravity core semantics",
                    syntax,
                    "Normalize host behavior behind an explicit interop boundary before core lowering.",
                )
            return self.node("core/symbol-ref", syntax, name=name, evaluation_order=[])
        if kind == "vector":
            items = [self.lower_expr(item, context, position=f"{position}.vector[{index}]") for index, item in enumerate(syntax["value"])]
            return self.node(
                "core/vector-literal",
                syntax,
                elements=items,
                effects=combine_effects(items),
                evaluation_order=[item["id"] for item in items],
            )
        if kind == "set":
            items = [self.lower_expr(item, context, position=f"{position}.set[{index}]") for index, item in enumerate(syntax["value"])]
            return self.node(
                "core/set-literal",
                syntax,
                elements=items,
                effects=combine_effects(items),
                evaluation_order=[item["id"] for item in items],
            )
        if kind == "map":
            entries = []
            for index, entry in enumerate(syntax["value"]):
                key = self.lower_expr(entry["key"], context, position=f"{position}.map[{index}].key")
                value = self.lower_expr(entry["value"], context, position=f"{position}.map[{index}].value")
                entries.append({"key": key, "value": value})
            return self.node(
                "core/map-literal",
                syntax,
                entries=entries,
                effects=combine_effects([item for entry in entries for item in [entry["key"], entry["value"]]]),
                evaluation_order=[item["id"] for entry in entries for item in [entry["key"], entry["value"]]],
            )
        if kind != "list":
            self.error(
                "L2-LOWERING-GAP",
                f"syntax kind {kind} has no core lowering rule",
                syntax,
                "Add a declared primitive, core lowering rule, or domain IR boundary.",
            )
        if not syntax["value"]:
            return self.node("core/list-literal", syntax, elements=[], evaluation_order=[])

        head = syntax["value"][0]
        if head.get("kind") != "symbol":
            return self.lower_call(syntax, context, position)
        operator = head["value"]
        if is_namespace_form_operator(operator):
            self.error(
                "L2-LOWERING-GAP",
                "namespace declarations are module metadata and cannot appear as core expressions",
                syntax,
                "Keep namespace declarations at the top level before core lowering.",
            )
        if is_unknown_reserved_core_form(operator):
            self.error(
                "L2-UNKNOWN-CORE-FORM",
                f"reserved core operator {operator} is not an L2 core form",
                syntax,
                "Use an L2 core form or declare a domain IR boundary.",
            )
        if operator in SURFACE_FORMS_REQUIRING_LOWERING:
            self.error(
                "L2-LOWERING-GAP",
                f"surface form {operator} reached core lowering without an expansion rule",
                syntax,
                "Lower the surface form to L2 core or to a documented domain IR before this stage.",
            )
        if is_host_semantics(operator):
            self.error(
                "L2-HOST-SEMANTICS",
                "host-specific behavior is not represented in Gravity core semantics",
                syntax,
                "Normalize host behavior behind an explicit interop boundary before core lowering.",
            )
        if operator in {"unstable-reorder!", "effect/reordered"}:
            self.error(
                "L2-EVAL-ORDER",
                "transformation would change required left-to-right evaluation order",
                syntax,
                "Preserve evaluation order until effect analysis proves reordering is legal.",
            )
        if operator in CORE_SPECIAL_FORMS:
            return self.lower_special(operator, syntax, context, position)
        return self.lower_call(syntax, context, position)

    def lower_special(self, operator: str, syntax: dict[str, Any], context: LoweringContext, position: str) -> dict[str, Any]:
        parts = syntax["value"]
        if operator == "quote":
            self.require_arity(syntax, 2, "quote requires exactly one data form")
            return self.node("core/quote", syntax, quoted_syntax=parts[1], evaluation_order=[])
        if operator == "if":
            self.require_arity(syntax, 4, "if requires condition, then branch, and else branch")
            condition = self.lower_expr(parts[1], context, position=f"{position}.if.condition")
            then_branch = self.lower_expr(parts[2], context, position=f"{position}.if.then")
            else_branch = self.lower_expr(parts[3], context, position=f"{position}.if.else")
            return self.node(
                "core/if",
                syntax,
                condition=condition,
                then_branch=then_branch,
                else_branch=else_branch,
                effects=combine_effects([condition, then_branch, else_branch]),
                evaluation_order=[condition["id"], "then-or-else"],
            )
        if operator == "do":
            expressions = [self.lower_expr(item, context, position=f"{position}.do[{index}]") for index, item in enumerate(parts[1:])]
            return self.node(
                "core/do",
                syntax,
                expressions=expressions,
                effects=combine_effects(expressions),
                evaluation_order=[item["id"] for item in expressions],
            )
        if operator == "let":
            self.require_min_arity(syntax, 3, "let requires a binding vector and body")
            bindings, body_context = self.lower_bindings(parts[1], context, position=f"{position}.let")
            body = [self.lower_expr(item, body_context, position=f"{position}.let.body[{index}]") for index, item in enumerate(parts[2:])]
            return self.node(
                "core/let",
                syntax,
                bindings=bindings,
                body=body,
                effects=combine_effects([binding["initializer"] for binding in bindings] + body),
                evaluation_order=[binding["initializer"]["id"] for binding in bindings] + [item["id"] for item in body],
            )
        if operator == "fn":
            self.require_min_arity(syntax, 3, "fn requires a parameter vector and body")
            params = self.parse_params(parts[1])
            fn_context = context.child()
            fn_context.push_scope()
            for param in params:
                fn_context.bind(param["name"], mutable=False)
            fn_context.recur_targets.append(len(params))
            body = [self.lower_expr(item, fn_context, position=f"{position}.fn.body[{index}]") for index, item in enumerate(parts[2:])]
            node = self.node(
                "core/fn",
                syntax,
                params=params,
                body=body,
                closure_captures=[],
                latent_effects=combine_effects(body),
                evaluation_order=[],
            )
            self.latent_function_effects.append(
                {
                    "node_id": node["id"],
                    "params": [param["name"] for param in params],
                    "latent_effects": node["latent_effects"],
                }
            )
            return node
        if operator == "loop":
            self.require_min_arity(syntax, 3, "loop requires a binding vector and body")
            bindings, body_context = self.lower_bindings(parts[1], context, position=f"{position}.loop")
            body_context.recur_targets.append(len(bindings))
            body = [self.lower_expr(item, body_context, position=f"{position}.loop.body[{index}]") for index, item in enumerate(parts[2:])]
            return self.node(
                "core/loop",
                syntax,
                bindings=bindings,
                body=body,
                effects=combine_effects([binding["initializer"] for binding in bindings] + body),
                evaluation_order=[binding["initializer"]["id"] for binding in bindings] + [item["id"] for item in body],
                recur_arity=len(bindings),
            )
        if operator == "recur":
            if not context.recur_targets or context.recur_targets[-1] != len(parts) - 1:
                self.error(
                    "L2-RECUR-TARGET",
                    "recur has no compatible loop or function target",
                    syntax,
                    "Place recur inside a compatible loop or function body with matching arity.",
                )
            args = [self.lower_expr(item, context, position=f"{position}.recur[{index}]") for index, item in enumerate(parts[1:])]
            return self.node(
                "core/recur",
                syntax,
                args=args,
                effects=combine_effects(args) + [":control/recur"],
                evaluation_order=[item["id"] for item in args],
            )
        if operator == "def":
            self.require_arity(syntax, 3, "def requires a name and initializer")
            name = self.require_symbol(parts[1], "def name must be a symbol")
            initializer = self.lower_expr(parts[2], context, position=f"{position}.def.initializer")
            return self.node(
                "core/def",
                syntax,
                name=name,
                initializer=initializer,
                artifact_visible=True,
                effects=initializer["effects"],
                evaluation_order=[initializer["id"]],
            )
        if operator == "var":
            self.require_arity(syntax, 2, "var requires a top-level symbol")
            name = self.require_symbol(parts[1], "var operand must be a symbol")
            return self.node("core/var", syntax, name=name, evaluation_order=[])
        if operator == "set!":
            self.require_arity(syntax, 3, "set! requires a target and value")
            target = self.require_symbol(parts[1], "set! target must be a symbol")
            binding = context.lookup(target)
            if binding is None or not binding.mutable:
                self.error(
                    "L2-SET-ILLEGAL",
                    "set! target is not an explicit mutable location in this context",
                    syntax,
                    "Use an explicit mutable binding, reference cell, var permission, or profile-approved mutable location.",
                )
            value = self.lower_expr(parts[2], context, position=f"{position}.set.value")
            return self.node(
                "core/set",
                syntax,
                target=target,
                value=value,
                effects=combine_effects([value]) + [":state/mutate"],
                capabilities=[":mutation/local"],
                evaluation_order=[value["id"]],
            )
        if operator == "try":
            return self.lower_try(syntax, context, position)
        if operator == "throw":
            self.require_arity(syntax, 2, "throw requires one error value")
            profile = syntax.get("profile_context")
            if profile in CONSTRAINED_THROW_PROFILES:
                self.error(
                    "L2-THROW-ILLEGAL",
                    f"throw is not legal in profile {profile}",
                    syntax,
                    "Use a profile-approved Result, panic, or typed error lowering strategy.",
                )
            error_value = self.lower_expr(parts[1], context, position=f"{position}.throw.value")
            return self.node(
                "core/throw",
                syntax,
                error_value=error_value,
                effects=combine_effects([error_value]) + [":error/throw"],
                evaluation_order=[error_value["id"]],
            )
        if operator == "match":
            self.require_min_arity(syntax, 3, "match requires a scrutinee and clause vector")
            scrutinee = self.lower_expr(parts[1], context, position=f"{position}.match.scrutinee")
            clauses = self.lower_match_clauses(parts[2:], context, position=f"{position}.match")
            clause_nodes = [clause["body"] for clause in clauses]
            return self.node(
                "core/match",
                syntax,
                scrutinee=scrutinee,
                clauses=clauses,
                effects=combine_effects([scrutinee] + clause_nodes),
                evaluation_order=[scrutinee["id"], "one-matching-clause"],
            )
        raise AssertionError(f"unhandled core special form {operator}")

    def lower_call(self, syntax: dict[str, Any], context: LoweringContext, position: str) -> dict[str, Any]:
        parts = syntax["value"]
        operator = parts[0]
        args = parts[1:]
        callee_name = operator.get("value") if operator.get("kind") == "symbol" else None
        if callee_name and is_host_semantics(callee_name):
            self.error(
                "L2-HOST-SEMANTICS",
                "host-specific behavior is not represented in Gravity core semantics",
                syntax,
                "Normalize host behavior behind an explicit interop boundary before core lowering.",
            )
        if callee_name in {"unstable-reorder!", "effect/reordered"}:
            self.error(
                "L2-EVAL-ORDER",
                "transformation would change required left-to-right evaluation order",
                syntax,
                "Preserve evaluation order until effect analysis proves reordering is legal.",
            )
        callee = self.lower_expr(operator, context, position=f"{position}.call.operator")
        lowered_args = [self.lower_expr(item, context, position=f"{position}.call.arg[{index}]") for index, item in enumerate(args)]
        node = self.node(
            "core/call",
            syntax,
            callee=callee,
            args=lowered_args,
            effects=combine_effects([callee] + lowered_args),
            evaluation_order=[callee["id"]] + [item["id"] for item in lowered_args],
        )
        self.call_records.append(
            {
                "node_id": node["id"],
                "callee": callee_name or "<expression>",
                "arg_count": len(lowered_args),
                "evaluation_order": node["evaluation_order"],
                "effects": node["effects"],
            }
        )
        return node

    def lower_try(self, syntax: dict[str, Any], context: LoweringContext, position: str) -> dict[str, Any]:
        protected = []
        handlers = []
        finalizers = []
        for index, item in enumerate(syntax["value"][1:]):
            if is_tagged_clause(item, "catch"):
                if len(item["value"]) < 4:
                    self.error("L2-LOWERING-GAP", "catch requires error type, binding, and body", item, "Use (catch Error binding body...).")
                binding_name = self.require_symbol(item["value"][2], "catch binding must be a symbol")
                handler_context = context.child()
                handler_context.push_scope()
                handler_context.bind(binding_name, mutable=False)
                body = [self.lower_expr(body_item, handler_context, position=f"{position}.catch[{index}]") for body_item in item["value"][3:]]
                handlers.append(
                    {
                        "error_pattern": item["value"][1],
                        "binding": binding_name,
                        "body": body,
                        "effects": combine_effects(body),
                    }
                )
            elif is_tagged_clause(item, "finally"):
                finalizers.extend(self.lower_expr(body_item, context, position=f"{position}.finally[{index}]") for body_item in item["value"][1:])
            else:
                protected.append(self.lower_expr(item, context, position=f"{position}.try.protected[{index}]"))
        if not protected:
            self.error("L2-LOWERING-GAP", "try requires protected expressions", syntax, "Add protected expressions before catch or finally clauses.")
        effect_nodes = protected + [node for handler in handlers for node in handler["body"]] + finalizers
        return self.node(
            "core/try",
            syntax,
            protected=protected,
            handlers=handlers,
            finalizers=finalizers,
            effects=combine_effects(effect_nodes),
            evaluation_order=[node["id"] for node in protected] + ["matching-catch"] + [node["id"] for node in finalizers],
        )

    def lower_bindings(self, binding_syntax: dict[str, Any], context: LoweringContext, position: str) -> tuple[list[dict[str, Any]], LoweringContext]:
        if binding_syntax["kind"] != "vector" or len(binding_syntax["value"]) % 2:
            self.error(
                "L2-LOWERING-GAP",
                "binding form must be an even vector of names and initializers",
                binding_syntax,
                "Use a vector such as [name value].",
            )
        body_context = context.child()
        body_context.push_scope()
        bindings = []
        values = binding_syntax["value"]
        for index in range(0, len(values), 2):
            name_form = values[index]
            initializer_form = values[index + 1]
            name = self.require_symbol(name_form, "binding name must be a symbol")
            initializer = self.lower_expr(initializer_form, body_context, position=f"{position}.binding[{index // 2}]")
            mutable = has_metadata(name_form, "mutable")
            body_context.bind(name, mutable=mutable)
            bindings.append(
                {
                    "name": name,
                    "mutable": mutable,
                    "initializer": initializer,
                    "source_span": name_form["span"],
                }
            )
        return bindings, body_context

    def lower_match_clauses(self, clause_forms: list[dict[str, Any]], context: LoweringContext, position: str) -> list[dict[str, Any]]:
        if len(clause_forms) == 1 and clause_forms[0]["kind"] == "vector":
            values = clause_forms[0]["value"]
        else:
            values = clause_forms
        span_source = clause_forms[0] if clause_forms else {"span": {"source": "<match>", "start_line": 1, "start_column": 1, "end_line": 1, "end_column": 1}}
        if len(values) % 2:
            self.error(
                "L2-LOWERING-GAP",
                "match clauses must be an even vector of patterns and bodies",
                span_source,
                "Use [pattern body ...] pairs for the initial L2 match artifact.",
            )
        clauses = []
        for index in range(0, len(values), 2):
            body = self.lower_expr(values[index + 1], context, position=f"{position}.clause[{index // 2}]")
            clauses.append({"pattern": values[index], "body": body})
        return clauses

    def parse_params(self, params_syntax: dict[str, Any]) -> list[dict[str, Any]]:
        if params_syntax["kind"] != "vector":
            self.error("L2-LOWERING-GAP", "fn parameters must be a vector", params_syntax, "Use a vector of parameter names.")
        params = []
        for item in params_syntax["value"]:
            params.append({"name": self.require_symbol(item, "fn parameter must be a symbol"), "source_span": item["span"]})
        return params

    def node(self, kind: str, syntax: dict[str, Any], **fields: Any) -> dict[str, Any]:
        node_id = f"core-{self.next_id:05d}"
        self.next_id += 1
        effects = fields.pop("effects", [])
        evaluation_order = fields.pop("evaluation_order", [])
        capabilities = fields.pop("capabilities", [])
        node = {
            "id": node_id,
            "kind": kind,
            "source_span": syntax["span"],
            "namespace_context": syntax.get("namespace_context"),
            "profile_context": syntax.get("profile_context"),
            "reader_origin": syntax.get("reader_origin", []),
            "generated_origin_chain": syntax.get("generated_origin_chain", []),
            "metadata": syntax.get("metadata", []),
            "evaluation_order": evaluation_order,
            "effects": unique(effects),
            "capabilities": unique(capabilities),
            "safety_facts": {"status": ":unchecked", "required_by": "D8/D9 downstream safety phases"},
        }
        node.update(fields)
        self.kind_counts[kind] = self.kind_counts.get(kind, 0) + 1
        self.source_map.append(
            {
                "node_id": node_id,
                "kind": kind,
                "span": syntax["span"],
                "reader_origin": syntax.get("reader_origin", []),
                "generated_origin_chain": syntax.get("generated_origin_chain", []),
            }
        )
        self.evaluation_order_records.append(
            {
                "node_id": node_id,
                "kind": kind,
                "evaluation_order": evaluation_order,
                "effects": node["effects"],
            }
        )
        return node

    def require_arity(self, syntax: dict[str, Any], count: int, message: str) -> None:
        if len(syntax["value"]) != count:
            self.error("L2-LOWERING-GAP", message, syntax, "Use the L2 core form arity.")

    def require_min_arity(self, syntax: dict[str, Any], count: int, message: str) -> None:
        if len(syntax["value"]) < count:
            self.error("L2-LOWERING-GAP", message, syntax, "Use the L2 core form arity.")

    def require_symbol(self, syntax: dict[str, Any], message: str) -> str:
        if syntax["kind"] != "symbol":
            self.error("L2-LOWERING-GAP", message, syntax, "Use a symbol in this core position.")
        return syntax["value"]

    def error(self, code: str, message: str, syntax: dict[str, Any], remediation: str) -> None:
        raise CoreLoweringError(code, message, syntax["span"], remediation, syntax=syntax)


def combine_effects(nodes: list[dict[str, Any]]) -> list[str]:
    effects: list[str] = []
    for node in nodes:
        for effect in node.get("effects", []):
            if effect not in effects:
                effects.append(effect)
    return effects


def unique(values: list[str]) -> list[str]:
    result: list[str] = []
    for value in values:
        if value not in result:
            result.append(value)
    return result


def has_metadata(syntax: dict[str, Any], name: str) -> bool:
    for metadata in syntax.get("metadata", []):
        if metadata["kind"] == "keyword" and metadata["value"] == f":{name}":
            return True
        if metadata["kind"] == "symbol" and metadata["value"] == name:
            return True
    return False


def is_ns_form(form: dict[str, Any]) -> bool:
    return (
        form["kind"] == "list"
        and len(form["value"]) >= 2
        and form["value"][0].get("kind") == "symbol"
        and form["value"][0].get("value") == "ns"
    )


def is_namespace_form_operator(operator: str) -> bool:
    return operator == "ns"


def is_unknown_reserved_core_form(operator: str) -> bool:
    if operator.startswith("core/"):
        return operator.removeprefix("core/") not in CORE_SPECIAL_FORMS
    if operator.startswith("gravity.core/"):
        return operator.removeprefix("gravity.core/") not in CORE_SPECIAL_FORMS
    return False


def is_host_semantics(operator: str) -> bool:
    return operator.startswith("host/")


def is_tagged_clause(syntax: dict[str, Any], tag: str) -> bool:
    return (
        syntax.get("kind") == "list"
        and bool(syntax.get("value"))
        and syntax["value"][0].get("kind") == "symbol"
        and syntax["value"][0].get("value") == tag
    )


def module_profile(forms: list[dict[str, Any]]) -> str | None:
    for form in forms:
        if not is_ns_form(form):
            continue
        for clause in form["value"][2:]:
            if (
                clause["kind"] == "list"
                and len(clause["value"]) >= 2
                and clause["value"][0].get("kind") == "keyword"
                and clause["value"][0].get("value") == ":profile"
                and clause["value"][1].get("kind") == "keyword"
            ):
                return clause["value"][1]["value"]
    return None


def artifact_hash(value: Any) -> str:
    data = json.dumps(value, sort_keys=True, separators=(",", ":"))
    return "sha256:" + hashlib.sha256(data.encode("utf-8")).hexdigest()


def lower_source_to_core_artifact(source: str, source_path: str) -> dict[str, Any]:
    return CoreLowerer().lower_source(source, source_path)


def lower_source_diagnostic(source: str, source_path: str) -> dict[str, Any] | None:
    try:
        lower_source_to_core_artifact(source, source_path)
    except CoreLoweringError as exc:
        return exc.to_diagnostic()
    except MacroExpansionError as exc:
        diagnostic = exc.to_diagnostic()
        diagnostic["analyzer_stage"] = "macro-expansion-before-core-lowering"
        return diagnostic
    return None
