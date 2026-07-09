#!/usr/bin/env python3
"""Validate the Phase 01 L4 macro expander implementation."""

from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / "src"))

from gravity.macros import ExpansionConfig, expand_source_diagnostic, expand_source_to_trace  # noqa: E402


FIXTURE_DIR = ROOT / "docs/artifacts/phase-01/fixtures/macro"
DEFAULT_ACCEPTED = FIXTURE_DIR / "accepted-macro-expansion.gravity"
NEGATIVE_FIXTURES = {
    "rejected-build-effect.gravity": ("L4-BUILD-EFFECT", ExpansionConfig()),
    "rejected-generated-profile.gravity": ("L4-GENERATED-PROFILE", ExpansionConfig()),
    "rejected-generated-unsafe.gravity": ("L4-GENERATED-UNSAFE", ExpansionConfig()),
    "rejected-depth.gravity": ("L4-EXPANSION-DEPTH", ExpansionConfig(max_depth=3)),
    "rejected-provenance.gravity": ("L4-PROVENANCE-MISSING", ExpansionConfig()),
    "rejected-not-syntax.gravity": ("L4-MACRO-NOT-SYNTAX", ExpansionConfig()),
    "rejected-hygiene-capture.gravity": ("L4-HYGIENE-CAPTURE", ExpansionConfig()),
}


def require(condition: bool, message: str) -> None:
    if not condition:
        raise AssertionError(message)


def walk(form):
    yield form
    if form.get("kind") == "map":
        for entry in form["value"]:
            yield from walk(entry["key"])
            yield from walk(entry["value"])
        return
    value = form.get("value")
    if isinstance(value, list):
        for item in value:
            if isinstance(item, dict):
                yield from walk(item)


def validate_accepted(path: Path) -> dict:
    source = path.read_text(encoding="utf-8")
    trace = expand_source_to_trace(source, str(path.relative_to(ROOT)), config=ExpansionConfig())
    require(trace["kind"] == "macro-expansion-trace", "trace kind mismatch")
    require(trace["module"] == "macro.demo", "trace module mismatch")
    require(len(trace["expansions"]) >= 2, "expected nested macro expansions")
    require(not trace["diagnostics"], "accepted trace should not have diagnostics")

    macros = [entry["macro"] for entry in trace["expansions"]]
    require("gravity.core/when" in macros, "when macro expansion missing")
    for entry in trace["expansions"]:
        for key in ["macro", "macro_version", "call_span", "input_hash", "output_hash", "build_effects", "generated_spans", "diagnostics"]:
            require(key in entry, f"trace entry missing {key}")
        require(entry["input_hash"].startswith("sha256:"), "input hash missing")
        require(entry["output_hash"].startswith("sha256:"), "output hash missing")
        require(entry["generated_spans"], "generated spans missing")

    expanded_nodes = [node for form in trace["expanded_forms"] for node in walk(form)]
    generated_nodes = [node for node in expanded_nodes if node.get("generated_origin_chain")]
    require(generated_nodes, "expanded output lacks generated-origin chain")
    require(all(node.get("compile_phase") == "macro-expansion" for node in generated_nodes), "generated nodes missing compile phase")
    require(any(node.get("metadata") for node in generated_nodes), "macro expansion did not preserve metadata")
    require(any(node["kind"] == "symbol" and node["value"] == "if" for node in expanded_nodes), "when macro did not lower to if")
    return trace


def validate_negative_fixtures() -> list[str]:
    observed: list[str] = []
    for filename, (expected, config) in NEGATIVE_FIXTURES.items():
        path = FIXTURE_DIR / filename
        diagnostic = expand_source_diagnostic(path.read_text(encoding="utf-8"), str(path.relative_to(ROOT)), config=config)
        require(diagnostic is not None, f"{filename} did not produce a diagnostic")
        require(diagnostic["id"] == expected, f"{filename} produced {diagnostic['id']} instead of {expected}")
        for key in ["span", "remediation", "analyzer_stage"]:
            require(key in diagnostic, f"{filename} diagnostic missing {key}")
        if expected in {"L4-GENERATED-PROFILE", "L4-GENERATED-UNSAFE", "L4-PROVENANCE-MISSING"}:
            require("generated_span" in diagnostic, f"{filename} missing generated span")
        observed.append(expected)
    return observed


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--accepted", type=Path, default=DEFAULT_ACCEPTED)
    parser.add_argument("--artifact-out", type=Path)
    args = parser.parse_args()

    try:
        trace = validate_accepted(args.accepted)
        diagnostics = validate_negative_fixtures()
    except AssertionError as exc:
        print(f"macro expander validation failed: {exc}", file=sys.stderr)
        return 1

    if args.artifact_out:
        args.artifact_out.parent.mkdir(parents=True, exist_ok=True)
        args.artifact_out.write_text(json.dumps(trace, indent=2, sort_keys=True) + "\n", encoding="utf-8")

    print(
        "macro expander validation passed: "
        f"{len(trace['expansions'])} expansions, {len(diagnostics)} rejected fixtures"
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
