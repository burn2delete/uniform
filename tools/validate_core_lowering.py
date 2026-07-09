#!/usr/bin/env python3
"""Validate the Phase 01 L2 core lowering implementation."""

from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / "src"))

from gravity.core import lower_source_diagnostic, lower_source_to_core_artifact  # noqa: E402


FIXTURE_DIR = ROOT / "docs/artifacts/phase-01/fixtures/core"
DEFAULT_ACCEPTED = FIXTURE_DIR / "accepted-core-forms.gravity"
NEGATIVE_FIXTURES = {
    "rejected-unknown-core-form.gravity": "L2-UNKNOWN-CORE-FORM",
    "rejected-eval-order.gravity": "L2-EVAL-ORDER",
    "rejected-recur-target.gravity": "L2-RECUR-TARGET",
    "rejected-set-illegal.gravity": "L2-SET-ILLEGAL",
    "rejected-throw-illegal.gravity": "L2-THROW-ILLEGAL",
    "rejected-host-semantics.gravity": "L2-HOST-SEMANTICS",
    "rejected-lowering-gap.gravity": "L2-LOWERING-GAP",
}


def require(condition: bool, message: str) -> None:
    if not condition:
        raise AssertionError(message)


def walk_core(node):
    yield node
    for key in [
        "condition",
        "then_branch",
        "else_branch",
        "initializer",
        "callee",
        "value",
        "error_value",
        "scrutinee",
    ]:
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


def validate_accepted(path: Path) -> dict:
    source = path.read_text(encoding="utf-8")
    artifact = lower_source_to_core_artifact(source, str(path.relative_to(ROOT)))
    require(artifact["kind"] == "core-ast-artifact", "core artifact kind mismatch")
    require(artifact["module"] == "core.demo", "module mismatch")
    require(artifact["profile"] == ":hosted", "profile mismatch")
    require(artifact["macro_expansion_count"] >= 1, "macro expansion was not consumed")
    require(artifact["macro_trace_hash"].startswith("sha256:"), "macro trace hash missing")
    require(not artifact["diagnostics"], "accepted artifact should not contain diagnostics")
    require(artifact["top_level"], "core artifact has no top-level forms")

    nodes = [node for top in artifact["top_level"] for node in walk_core(top)]
    kinds = {node["kind"] for node in nodes}
    expected_kinds = {
        "core/quote",
        "core/if",
        "core/do",
        "core/let",
        "core/fn",
        "core/loop",
        "core/recur",
        "core/def",
        "core/var",
        "core/set",
        "core/try",
        "core/throw",
        "core/match",
        "core/call",
    }
    missing = sorted(expected_kinds - kinds)
    require(not missing, f"accepted fixture missing core kinds: {missing}")
    require(any(node["kind"] == "core/if" and node.get("generated_origin_chain") for node in nodes), "macro-generated if lost provenance")
    require(any(node["kind"] == "core/set" and ":state/mutate" in node["effects"] for node in nodes), "set! mutation effect missing")
    require(any(node["kind"] == "core/throw" and ":error/throw" in node["effects"] for node in nodes), "throw error effect missing")
    require(any(node["kind"] == "core/fn" for node in nodes), "fn node missing")
    require(artifact["latent_function_effects"], "latent function effects missing")
    require(artifact["call_records"], "call records missing")
    require(len(artifact["source_map"]) >= len(nodes), "source map does not cover lowered nodes")
    require(artifact["evaluation_order_records"], "evaluation-order records missing")
    for record in artifact["source_map"]:
        require("node_id" in record and "span" in record and "kind" in record, "source map record incomplete")
    for record in artifact["core_form_kind_records"]:
        require(record["kind"].startswith("core/"), "core form kind record is not a core kind")
        require(record["count"] > 0, "core form kind count must be positive")
    return artifact


def validate_negative_fixtures() -> list[str]:
    observed: list[str] = []
    for filename, expected in NEGATIVE_FIXTURES.items():
        path = FIXTURE_DIR / filename
        diagnostic = lower_source_diagnostic(path.read_text(encoding="utf-8"), str(path.relative_to(ROOT)))
        require(diagnostic is not None, f"{filename} did not produce a diagnostic")
        require(diagnostic["id"] == expected, f"{filename} produced {diagnostic['id']} instead of {expected}")
        for key in ["span", "remediation", "analyzer_stage"]:
            require(key in diagnostic, f"{filename} diagnostic missing {key}")
        observed.append(expected)
    return observed


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--accepted", type=Path, default=DEFAULT_ACCEPTED)
    parser.add_argument("--artifact-out", type=Path)
    args = parser.parse_args()

    try:
        artifact = validate_accepted(args.accepted)
        diagnostics = validate_negative_fixtures()
    except AssertionError as exc:
        print(f"core lowering validation failed: {exc}", file=sys.stderr)
        return 1

    if args.artifact_out:
        args.artifact_out.parent.mkdir(parents=True, exist_ok=True)
        args.artifact_out.write_text(json.dumps(artifact, indent=2, sort_keys=True) + "\n", encoding="utf-8")

    print(
        "core lowering validation passed: "
        f"{len(artifact['top_level'])} top-level forms, {len(diagnostics)} rejected fixtures"
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
