#!/usr/bin/env python3
"""Validate the Phase 01 L1 reader implementation against fixtures."""

from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / "src"))

from gravity.reader import read_source_diagnostic, read_source_to_artifact  # noqa: E402


FIXTURE_DIR = ROOT / "docs/artifacts/phase-01/fixtures/reader"
DEFAULT_ACCEPTED = FIXTURE_DIR / "accepted-hosted-hello.gravity"
NEGATIVE_FIXTURES = {
    "rejected-malformed-map.gravity": "L1-MAP-ARITY",
    "rejected-metadata-unattached.gravity": "L1-METADATA",
    "rejected-invalid-string.gravity": "L1-STRING",
    "rejected-delimiter.gravity": "L1-DELIMITER",
    "rejected-ns-shape.gravity": "L1-NS-SHAPE",
    "rejected-reader-extension.gravity": "L1-READER-EXTENSION",
}


def require(condition: bool, message: str) -> None:
    if not condition:
        raise AssertionError(message)


def walk_forms(form):
    yield form
    if form.get("kind") == "map":
        for entry in form["value"]:
            yield from walk_forms(entry["key"])
            yield from walk_forms(entry["value"])
        return
    value = form.get("value")
    if isinstance(value, list):
        for item in value:
            if isinstance(item, dict):
                yield from walk_forms(item)


def validate_span(form: dict) -> None:
    span = form.get("span", {})
    for key in ["source", "start_byte", "end_byte", "start_line", "start_column", "end_line", "end_column"]:
        require(key in span, f"missing span key {key}")
    require(span["end_byte"] >= span["start_byte"], "span end before start")


def validate_accepted(path: Path) -> dict:
    source = path.read_text(encoding="utf-8")
    artifact = read_source_to_artifact(source, str(path.relative_to(ROOT)))
    require(artifact["kind"] == "syntax-object-stream", "artifact kind mismatch")
    require(artifact["form_count"] >= 3, "expected at least namespace, definition, and quoted data forms")

    forms = artifact["forms"]
    all_forms = [node for form in forms for node in walk_forms(form)]
    for form in all_forms:
        validate_span(form)
        require("metadata" in form, "syntax object missing metadata")
        require("reader_origin" in form, "syntax object missing reader_origin")

    require(forms[0]["kind"] == "list", "first form should be namespace list")
    require(forms[0]["namespace_context"] == "hello.main", "namespace context was not attached")
    require(forms[0]["profile_context"] == ":hosted", "profile context was not attached")

    quote_forms = [
        form
        for form in all_forms
        if form["kind"] == "list"
        and form["value"]
        and form["value"][0].get("kind") == "symbol"
        and form["value"][0].get("value") == "quote"
    ]
    require(quote_forms, "quote abbreviation did not expand to explicit quote form")
    require(any(origin.get("kind") == "abbreviation" for origin in quote_forms[0]["reader_origin"]), "quote form missing abbreviation origin")

    metadata_forms = [form for form in all_forms if form.get("metadata")]
    require(metadata_forms, "metadata did not attach to following syntax object")

    ns_text = json.dumps(forms[0], sort_keys=True)
    for preserved in [":profile", ":target", ":effects", ":capabilities", ":safety"]:
        require(preserved in ns_text, f"namespace clause {preserved} was not preserved")
    return artifact


def validate_negative_fixtures() -> list[str]:
    observed: list[str] = []
    for filename, expected in NEGATIVE_FIXTURES.items():
        path = FIXTURE_DIR / filename
        diagnostic = read_source_diagnostic(path.read_text(encoding="utf-8"), str(path.relative_to(ROOT)))
        require(diagnostic is not None, f"{filename} did not produce a diagnostic")
        require(diagnostic["id"] == expected, f"{filename} produced {diagnostic['id']} instead of {expected}")
        for key in ["span", "excerpt", "reader_state", "remediation"]:
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
        print(f"reader validation failed: {exc}", file=sys.stderr)
        return 1

    if args.artifact_out:
        args.artifact_out.parent.mkdir(parents=True, exist_ok=True)
        args.artifact_out.write_text(json.dumps(artifact, indent=2, sort_keys=True) + "\n", encoding="utf-8")

    print(
        "reader validation passed: "
        f"{artifact['form_count']} accepted forms, {len(diagnostics)} rejected fixtures"
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
