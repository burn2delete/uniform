"""L7 pattern matching analysis over core match nodes."""

from __future__ import annotations

from typing import Any

from gravity.core import CoreLoweringError, lower_source_to_core_artifact
from gravity.reader import ReaderError
from gravity.typed_core import namespace_policy


class PatternError(Exception):
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
            "analyzer_stage": "pattern-matching",
        }
        diagnostic.update(self.details)
        return diagnostic


def analyze_patterns(source: str, source_path: str) -> dict[str, Any]:
    policy = namespace_policy(source, source_path)
    core = lower_source_to_core_artifact(source, source_path)
    matches = [node for top in core["top_level"] for node in walk_core(top) if node["kind"] == "core/match"]
    analyzer = PatternAnalyzer(policy)
    reports = [analyzer.analyze_match(match) for match in matches]
    return {
        "kind": "match-analysis-artifact",
        "module": core["module"],
        "source": source_path,
        "profile": policy["profile"],
        "match_count": len(reports),
        "decision_trees": [report["decision_tree"] for report in reports],
        "exhaustiveness_report": [report["exhaustiveness"] for report in reports],
        "branch_type_narrowing_table": [row for report in reports for row in report["narrowing"]],
        "branch_effect_summary": [row for report in reports for row in report["effects"]],
        "schema_validation_links": [row for report in reports for row in report["schema_links"]],
        "ownership_move_borrow_facts": [row for report in reports for row in report["ownership"]],
        "diagnostics": [],
    }


class PatternAnalyzer:
    def __init__(self, policy: dict[str, Any]):
        self.policy = policy

    def analyze_match(self, node: dict[str, Any]) -> dict[str, Any]:
        scrutinee_kind = scrutinee_type(node["scrutinee"])
        seen_default = False
        constructors: set[str] = set()
        decisions = []
        narrowing = []
        effects = []
        schema_links = []
        ownership = []

        for index, clause in enumerate(node["clauses"]):
            pattern = clause["pattern"]
            pattern_info = classify_pattern(pattern)
            if seen_default:
                self.error(
                    "L7-UNREACHABLE",
                    "branch appears after a wildcard/default branch",
                    pattern,
                    "Move the default branch last or remove unreachable branches.",
                    {"match_node": node["id"], "branch_index": index},
                )
            if pattern_info["kind"] == "wildcard":
                seen_default = True
            if pattern_info["kind"] == "constructor":
                constructors.add(pattern_info["constructor"])
            duplicate = duplicate_binding(pattern)
            if duplicate:
                self.error(
                    "L7-DUP-BINDING",
                    f"pattern binds {duplicate} more than once",
                    pattern,
                    "Use distinct binding names or explicit equality-pattern syntax when available.",
                    {"binding": duplicate},
                )
            if literal_incompatible(scrutinee_kind, pattern_info):
                self.error(
                    "L7-PATTERN-TYPE",
                    "pattern literal is incompatible with the scrutinee type",
                    pattern,
                    "Use a pattern compatible with the scrutinee type.",
                    {"scrutinee_type": scrutinee_kind, "pattern_kind": pattern_info["kind"]},
                )
            guard = guard_expression(pattern)
            if guard is not None:
                guard_effect = guard_effect_label(guard)
                if guard_effect and guard_effect not in self.policy["effects"]:
                    self.error(
                        "L7-GUARD-EFFECT",
                        f"guard effect {guard_effect} is not declared",
                        guard,
                        "Declare the guard effect or make the guard pure.",
                        {"effect": guard_effect},
                    )
                effects.append({"match_node": node["id"], "branch_index": index, "guard_effects": [guard_effect] if guard_effect else []})
            if scrutinee_kind == "Untrusted" and pattern_info["kind"] in {"map", "constructor", "vector"}:
                self.error(
                    "L7-UNVALIDATED-SHAPE",
                    "untrusted data is matched as a trusted closed shape",
                    pattern,
                    "Validate the data through a schema or typed boundary before closed-shape matching.",
                    {"scrutinee_type": scrutinee_kind},
                )
            if pattern_info["kind"] == "linear-move":
                self.error(
                    "L7-LINEAR-MOVE",
                    "pattern moves a linear resource without ownership evidence",
                    pattern,
                    "Move linear resources only when the branch consumes them according to ownership facts.",
                )

            decisions.append({"branch_index": index, "pattern_kind": pattern_info["kind"], "span": pattern["span"]})
            narrowing.append({"match_node": node["id"], "branch_index": index, "bindings": sorted(pattern_bindings(pattern)), "narrowed_type": narrowed_type(pattern_info)})
            ownership.extend(ownership_facts(node["id"], index, pattern))
            if scrutinee_kind == "Validated":
                schema_links.append({"match_node": node["id"], "branch_index": index, "schema": "validated-boundary"})

        if self.policy["profile"] == ":formal" and not seen_default and {"Ok", "Err"} - constructors:
            self.error(
                "L7-NONEXHAUSTIVE",
                "formal profile requires exhaustive match over closed Result constructors",
                node,
                "Add missing constructors or an explicit default branch with proof.",
                {"missing": sorted({"Ok", "Err"} - constructors)},
            )

        return {
            "decision_tree": {"match_node": node["id"], "scrutinee_type": scrutinee_kind, "branches": decisions},
            "exhaustiveness": {"match_node": node["id"], "profile": self.policy["profile"], "has_default": seen_default, "constructors": sorted(constructors)},
            "narrowing": narrowing,
            "effects": effects,
            "schema_links": schema_links,
            "ownership": ownership,
        }

    def error(self, code: str, message: str, syntax_or_node: dict[str, Any], remediation: str, details: dict[str, Any] | None = None) -> None:
        span = syntax_or_node.get("span") or syntax_or_node.get("source_span")
        raise PatternError(code, message, span, remediation, details)


def walk_core(node: dict[str, Any]):
    yield node
    for key in ["condition", "then_branch", "else_branch", "initializer", "callee", "value", "error_value", "scrutinee"]:
        value = node.get(key)
        if isinstance(value, dict) and "kind" in value:
            yield from walk_core(value)
    for key in ["expressions", "body", "args", "elements", "protected", "finalizers"]:
        for item in node.get(key, []):
            if isinstance(item, dict) and "kind" in item:
                yield from walk_core(item)
    for binding in node.get("bindings", []):
        yield from walk_core(binding["initializer"])
    for entry in node.get("entries", []):
        yield from walk_core(entry["key"])
        yield from walk_core(entry["value"])
    for handler in node.get("handlers", []):
        for item in handler.get("body", []):
            yield from walk_core(item)
    for clause in node.get("clauses", []):
        yield from walk_core(clause["body"])


def classify_pattern(pattern: dict[str, Any]) -> dict[str, Any]:
    if pattern["kind"] == "symbol" and pattern["value"] == "_":
        return {"kind": "wildcard"}
    if pattern["kind"] in {"nil", "boolean", "integer", "float", "ratio", "string", "keyword"}:
        return {"kind": "literal", "literal_kind": pattern["kind"]}
    if pattern["kind"] == "vector":
        return {"kind": "vector"}
    if pattern["kind"] == "map":
        return {"kind": "map"}
    if pattern["kind"] == "list" and pattern["value"] and pattern["value"][0]["kind"] == "symbol":
        constructor = pattern["value"][0]["value"]
        if constructor == "MoveLinear":
            return {"kind": "linear-move"}
        return {"kind": "constructor", "constructor": constructor}
    if pattern["kind"] == "symbol":
        return {"kind": "binding"}
    return {"kind": pattern["kind"]}


def scrutinee_type(node: dict[str, Any]) -> str:
    if node["kind"] == "core/literal":
        return node.get("value_kind", "literal")
    if node["kind"] == "core/call":
        callee = node.get("callee", {})
        if callee.get("name") == "network/input":
            return "Untrusted"
        if callee.get("name") == "schema/validate":
            return "Validated"
    if node["kind"] == "core/symbol-ref" and node.get("name") == "result":
        return "Result"
    return "Dynamic"


def duplicate_binding(pattern: dict[str, Any]) -> str | None:
    seen: set[str] = set()
    for name in pattern_bindings(pattern):
        if name in seen:
            return name
        seen.add(name)
    return None


def pattern_bindings(pattern: dict[str, Any]) -> list[str]:
    if pattern["kind"] == "symbol":
        return [] if pattern["value"] == "_" else [pattern["value"]]
    bindings: list[str] = []
    if pattern["kind"] == "map":
        for entry in pattern["value"]:
            if entry["key"].get("kind") == "keyword" and entry["key"].get("value") == ":when":
                continue
            bindings.extend(pattern_bindings(entry["value"]))
    elif isinstance(pattern.get("value"), list):
        items = pattern["value"][1:] if pattern["kind"] == "list" else pattern["value"]
        for item in items:
            if isinstance(item, dict):
                bindings.extend(pattern_bindings(item))
    return bindings


def literal_incompatible(scrutinee_kind: str, pattern_info: dict[str, Any]) -> bool:
    return scrutinee_kind in {"integer", "string", "keyword", "boolean"} and pattern_info.get("literal_kind") not in {scrutinee_kind, None}


def guard_expression(pattern: dict[str, Any]) -> dict[str, Any] | None:
    if pattern["kind"] != "map":
        return None
    for entry in pattern["value"]:
        if entry["key"].get("kind") == "keyword" and entry["key"].get("value") == ":when":
            return entry["value"]
    return None


def guard_effect_label(guard: dict[str, Any]) -> str | None:
    if guard["kind"] == "list" and guard["value"] and guard["value"][0].get("kind") == "symbol":
        if guard["value"][0].get("value") == "println":
            return ":io/write"
    return None


def narrowed_type(pattern_info: dict[str, Any]) -> str:
    if pattern_info["kind"] == "constructor":
        return pattern_info["constructor"]
    return pattern_info["kind"]


def ownership_facts(match_node: str, branch_index: int, pattern: dict[str, Any]) -> list[dict[str, Any]]:
    facts = []
    for name in pattern_bindings(pattern):
        facts.append({"match_node": match_node, "branch_index": branch_index, "binding": name, "mode": "borrow-or-bind"})
    return facts


def analyze_patterns_diagnostic(source: str, source_path: str) -> dict[str, Any] | None:
    try:
        analyze_patterns(source, source_path)
    except PatternError as exc:
        return exc.to_diagnostic()
    except (CoreLoweringError, ReaderError) as exc:
        return {
            "id": getattr(exc, "code", "L7-UPSTREAM"),
            "message": getattr(exc, "message", str(exc)),
            "span": getattr(exc, "span", {}),
            "remediation": getattr(exc, "remediation", "Fix upstream syntax or core lowering before pattern analysis."),
            "analyzer_stage": "pattern-matching-upstream",
        }
    return None
