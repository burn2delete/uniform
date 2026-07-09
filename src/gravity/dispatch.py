"""L8 protocol, interface, and dispatch analysis."""

from __future__ import annotations

from typing import Any

from gravity.reader import ReaderError, read_source
from gravity.typed_core import namespace_policy


class DispatchError(Exception):
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
            "analyzer_stage": "dispatch",
        }
        diagnostic.update(self.details)
        return diagnostic


def analyze_dispatch(source: str, source_path: str) -> dict[str, Any]:
    policy = namespace_policy(source, source_path)
    forms = read_source(source, source_path=source_path)
    analyzer = DispatchAnalyzer(policy)
    for form in forms[1:]:
        analyzer.consume_top_level(form)
    analyzer.validate_dispatch_calls(forms[1:])
    return {
        "kind": "dispatch-analysis-artifact",
        "module": module_name(forms),
        "source": source_path,
        "profile": policy["profile"],
        "protocol_table": list(analyzer.protocols.values()),
        "implementation_table": analyzer.implementations,
        "method_signature_records": analyzer.method_signatures,
        "dispatch_mode_records": analyzer.dispatch_records,
        "multimethod_dispatch_tables": [],
        "interface_lowering_artifacts": analyzer.interface_artifacts,
        "host_interop_dispatch_records": analyzer.host_dispatch_records,
        "diagnostics": [],
    }


class DispatchAnalyzer:
    def __init__(self, policy: dict[str, Any]):
        self.policy = policy
        self.protocols: dict[str, dict[str, Any]] = {}
        self.method_to_protocol: dict[str, str] = {}
        self.implementations: list[dict[str, Any]] = []
        self.method_signatures: list[dict[str, Any]] = []
        self.dispatch_records: list[dict[str, Any]] = []
        self.interface_artifacts: list[dict[str, Any]] = []
        self.host_dispatch_records: list[dict[str, Any]] = []

    def consume_top_level(self, form: dict[str, Any]) -> None:
        if not is_call(form):
            return
        head = form["value"][0]["value"]
        if head == "defprotocol":
            self.consume_protocol(form)
        elif head == "extend":
            self.consume_extend(form)

    def consume_protocol(self, form: dict[str, Any]) -> None:
        if len(form["value"]) < 3 or form["value"][1]["kind"] != "symbol":
            self.error("L8-PROTOCOL-METHOD", "defprotocol requires a protocol name and methods", form, "Use (defprotocol Name (method [args] ...)).")
        protocol = form["value"][1]["value"]
        methods = []
        for method in form["value"][2:]:
            if not is_call(method):
                continue
            name = method["value"][0]["value"]
            params = method["value"][1] if len(method["value"]) > 1 and method["value"][1]["kind"] == "vector" else {"value": []}
            effects = clause_values(method, ":effects")
            capabilities = clause_values(method, ":capabilities")
            record = {
                "protocol": protocol,
                "method": name,
                "arity": len(params["value"]),
                "effects": effects,
                "capabilities": capabilities,
                "span": method["span"],
            }
            methods.append(record)
            self.method_to_protocol[name] = protocol
            self.method_signatures.append(record)
        self.protocols[protocol] = {"protocol": protocol, "methods": methods, "span": form["span"]}

    def consume_extend(self, form: dict[str, Any]) -> None:
        if len(form["value"]) < 4 or form["value"][1]["kind"] != "symbol" or form["value"][2]["kind"] != "symbol":
            self.error("L8-PROTOCOL-METHOD", "extend requires type, protocol, and method implementations", form, "Use (extend Type Protocol (method [args] body)).")
        target_type = form["value"][1]["value"]
        protocol = form["value"][2]["value"]
        known = {method["method"]: method for method in self.protocols.get(protocol, {}).get("methods", [])}
        if not known:
            self.error("L8-DISPATCH-MISSING", f"protocol {protocol} is not declared", form, "Declare the protocol before extending it.")
        seen_impls = {(impl["type"], impl["protocol"], impl["method"]) for impl in self.implementations}
        for method in form["value"][3:]:
            if not is_call(method):
                continue
            name = method["value"][0]["value"]
            signature = known.get(name)
            if signature is None:
                self.error("L8-PROTOCOL-METHOD", f"method {name} is not declared by protocol {protocol}", method, "Implement only declared protocol methods.")
            params = method["value"][1] if len(method["value"]) > 1 and method["value"][1]["kind"] == "vector" else {"value": []}
            if len(params["value"]) != signature["arity"]:
                self.error("L8-PROTOCOL-METHOD", f"method {name} arity does not satisfy protocol contract", method, "Match the protocol method parameter list.")
            key = (target_type, protocol, name)
            if key in seen_impls:
                self.error("L8-DISPATCH-AMBIGUOUS", f"multiple implementations for {target_type} {protocol}/{name}", method, "Add a priority rule or remove the duplicate implementation.")
            effects = infer_effects(method)
            widened = sorted(set(effects) - set(signature["effects"]))
            if widened:
                self.error("L8-METHOD-EFFECT", f"method implementation widens effects: {widened}", method, "Declare the method effects in the protocol contract.")
            record = {
                "type": target_type,
                "protocol": protocol,
                "method": name,
                "dispatch_mode": dispatch_mode(self.policy["profile"]),
                "effects": effects,
                "capabilities": signature["capabilities"],
                "span": method["span"],
            }
            self.implementations.append(record)
            self.interface_artifacts.append({"type": target_type, "protocol": protocol, "method": name, "lowering": record["dispatch_mode"]})
            seen_impls.add(key)

    def validate_dispatch_calls(self, forms: list[dict[str, Any]]) -> None:
        implemented = {impl["method"]: impl for impl in self.implementations}
        for form in walk(forms):
            if not is_call(form):
                continue
            head = form["value"][0]["value"]
            if head in {"defprotocol", "extend"}:
                continue
            if head == "dynamic-dispatch" and self.policy["profile"] in {":kernel", ":firmware", ":hardware", ":core"}:
                self.error("L8-DYNAMIC-FORBIDDEN", "active profile rejects dynamic dispatch", form, "Use static, dictionary, vtable, or proven bounded dispatch.")
            if head == "host-dispatch":
                self.error("L8-HOST-DISPATCH", "host dispatch boundary lacks null/exception/type contract", form, "Declare host dispatch as an interop boundary with contracts.")
            if head == "tool-dispatch":
                self.error("L8-TOOL-DISPATCH", "tool dispatch lacks schema or capability", form, "Attach tool schema and capability records before dispatch.")
            if head in self.method_to_protocol:
                impl = implemented.get(head)
                if impl is None:
                    self.error("L8-DISPATCH-MISSING", f"no implementation found for protocol method {head}", form, "Add an implementation or reject the call before dispatch.")
                self.dispatch_records.append(
                    {
                        "call": head,
                        "protocol": self.method_to_protocol[head],
                        "implementation": f"{impl['type']}/{head}",
                        "dispatch_mode": impl["dispatch_mode"],
                        "effects": impl["effects"],
                        "capabilities": impl["capabilities"],
                        "span": form["span"],
                    }
                )

    def error(self, code: str, message: str, form: dict[str, Any], remediation: str, details: dict[str, Any] | None = None) -> None:
        raise DispatchError(code, message, form["span"], remediation, details)


def is_call(form: dict[str, Any]) -> bool:
    return form.get("kind") == "list" and form.get("value") and form["value"][0].get("kind") == "symbol"


def clause_values(form: dict[str, Any], keyword: str) -> list[str]:
    values = []
    items = form.get("value", [])
    for index, item in enumerate(items):
        if item.get("kind") == "keyword" and item.get("value") == keyword and index + 1 < len(items):
            next_item = items[index + 1]
            if next_item["kind"] == "set":
                values.extend(entry["value"] for entry in next_item["value"] if entry["kind"] == "keyword")
            elif next_item["kind"] == "keyword":
                values.append(next_item["value"])
    return values


def infer_effects(form: dict[str, Any]) -> list[str]:
    effects: list[str] = []
    for node in walk([form]):
        if is_call(node):
            head = node["value"][0]["value"]
            if head == "fs/close" and ":resource/close" not in effects:
                effects.append(":resource/close")
            if head == "println" and ":io/write" not in effects:
                effects.append(":io/write")
    return effects


def dispatch_mode(profile: str | None) -> str:
    if profile == ":native":
        return ":direct"
    if profile == ":hosted":
        return ":hosted-dynamic"
    return ":dictionary"


def module_name(forms: list[dict[str, Any]]) -> str:
    if forms and is_call(forms[0]) and len(forms[0]["value"]) > 1 and forms[0]["value"][1]["kind"] == "symbol":
        return forms[0]["value"][1]["value"]
    return "<unknown>"


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


def analyze_dispatch_diagnostic(source: str, source_path: str) -> dict[str, Any] | None:
    try:
        analyze_dispatch(source, source_path)
    except DispatchError as exc:
        return exc.to_diagnostic()
    except ReaderError as exc:
        return {
            "id": exc.code,
            "message": exc.message,
            "span": exc.span,
            "remediation": exc.remediation,
            "analyzer_stage": "dispatch-upstream",
        }
    return None
