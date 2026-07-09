"""L9 error handling analysis."""

from __future__ import annotations

from typing import Any

from gravity.reader import ReaderError, read_source
from gravity.typed_core import namespace_policy


class ErrorHandlingError(Exception):
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
            "analyzer_stage": "error-handling",
        }
        diagnostic.update(self.details)
        return diagnostic


def analyze_errors(source: str, source_path: str) -> dict[str, Any]:
    policy = namespace_policy(source, source_path)
    forms = read_source(source, source_path=source_path)
    analyzer = ErrorAnalyzer(policy)
    analyzer.walk_forms(forms[1:], in_try=False)
    return {
        "kind": "error-handling-artifact",
        "module": module_name(forms),
        "source": source_path,
        "profile": policy["profile"],
        "error_type_declarations": analyzer.error_types,
        "function_thrown_error_effect_records": analyzer.throw_records,
        "panic_lowering_records": analyzer.panic_records,
        "safety_check_failure_records": analyzer.safety_records,
        "ffi_error_mapping_artifacts": analyzer.ffi_records,
        "workflow_failure_records": analyzer.workflow_records,
        "ai_tool_error_records": analyzer.ai_records,
        "diagnostics": [],
    }


class ErrorAnalyzer:
    def __init__(self, policy: dict[str, Any]):
        self.policy = policy
        self.error_types: list[dict[str, Any]] = []
        self.throw_records: list[dict[str, Any]] = []
        self.panic_records: list[dict[str, Any]] = []
        self.safety_records: list[dict[str, Any]] = []
        self.ffi_records: list[dict[str, Any]] = []
        self.workflow_records: list[dict[str, Any]] = []
        self.ai_records: list[dict[str, Any]] = []

    def walk_forms(self, forms: list[dict[str, Any]], in_try: bool) -> None:
        for form in forms:
            self.consume(form, in_try=in_try)

    def consume(self, form: dict[str, Any], in_try: bool) -> None:
        if form["kind"] == "list" and form["value"] and form["value"][0].get("kind") == "symbol":
            head = form["value"][0]["value"]
            if head == "try":
                for child in form["value"][1:]:
                    self.consume(child, in_try=True)
                return
            if head == "throw":
                if ":error/throw" not in self.policy["effects"]:
                    self.error("L9-THROW-EFFECT", "throw is missing from declared function or namespace effects", form, "Declare :error/throw or return Result/Option data.")
                if not in_try:
                    self.error("L9-UNHANDLED", "thrown error is not handled or propagated by an enclosing boundary", form, "Wrap the throw in try/catch or expose the thrown error in the function contract.")
                self.throw_records.append({"span": form["span"], "effect": ":error/throw", "handled": in_try})
            elif head == "panic":
                if self.policy["profile"] in {":hardware", ":formal"}:
                    self.error("L9-PANIC-PROFILE", f"profile {self.policy['profile']} lacks panic lowering", form, "Use a profile-approved trap, proof of unreachable code, or explicit failure artifact.")
                self.panic_records.append({"span": form["span"], "profile": self.policy["profile"], "lowering": "profile-runtime-panic"})
            elif head == "host/throwing-call":
                self.error("L9-HOST-ERROR", "host exception or null crosses boundary without mapping", form, "Normalize host errors and nulls into typed Gravity error contracts.")
            elif head == "ffi/call":
                self.error("L9-FFI-ERROR", "FFI error convention lacks typed mapping", form, "Attach errno/exception/result mapping and cleanup behavior.")
            elif head == "workflow/fail":
                self.error("L9-WORKFLOW-ERROR", "workflow failure lacks durable replay record", form, "Record step id, schemas, retry, compensation, and replay id.")
            elif head == "tool/fail":
                self.error("L9-AI-ERROR", "AI/tool failure lacks structured policy or audit artifact", form, "Emit model/tool/schema/policy failure artifacts.")
            elif head == "safety/check":
                self.safety_records.append({"span": form["span"], "failure": "typed-profile-failure"})
            elif head == "deferror" and len(form["value"]) > 1:
                self.error_types.append({"name": form["value"][1]["value"], "span": form["value"][1]["span"]})
        if form["kind"] == "map":
            for entry in form["value"]:
                self.consume(entry["key"], in_try)
                self.consume(entry["value"], in_try)
        elif form["kind"] == "tagged":
            self.consume(form["value"]["form"], in_try)
        elif isinstance(form.get("value"), list):
            for item in form["value"]:
                if isinstance(item, dict):
                    self.consume(item, in_try)

    def error(self, code: str, message: str, form: dict[str, Any], remediation: str, details: dict[str, Any] | None = None) -> None:
        raise ErrorHandlingError(code, message, form["span"], remediation, details)


def module_name(forms: list[dict[str, Any]]) -> str:
    if forms and forms[0]["kind"] == "list" and len(forms[0]["value"]) > 1 and forms[0]["value"][1]["kind"] == "symbol":
        return forms[0]["value"][1]["value"]
    return "<unknown>"


def analyze_errors_diagnostic(source: str, source_path: str) -> dict[str, Any] | None:
    try:
        analyze_errors(source, source_path)
    except ErrorHandlingError as exc:
        return exc.to_diagnostic()
    except ReaderError as exc:
        return {
            "id": exc.code,
            "message": exc.message,
            "span": exc.span,
            "remediation": exc.remediation,
            "analyzer_stage": "error-handling-upstream",
        }
    return None
