#!/usr/bin/env python3
"""Validate document-specific coverage for L1 surface syntax."""

from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / "src"))

from gravity.reader import (  # noqa: E402
    ReaderExtension,
    ReaderPolicy,
    read_source_bytes_diagnostic,
    read_source_diagnostic,
    read_source_to_artifact,
)


FIXTURE_DIR = ROOT / "docs/artifacts/phase-01/fixtures/l1"
ACCEPTED_EXTENSION = FIXTURE_DIR / "accepted-reader-extension.gravity"
ACCEPTED_LATER_SEMANTIC = FIXTURE_DIR / "accepted-later-semantic-rejection.gravity"
REJECTED_EXTENSION_BUILD_EFFECT = FIXTURE_DIR / "rejected-reader-extension-build-effect.gravity"


def require(condition: bool, message: str) -> None:
    if not condition:
        raise AssertionError(message)


def extension_policy(granted: bool) -> ReaderPolicy:
    return ReaderPolicy(
        registered_extensions={
            "uuid": ReaderExtension("uuid"),
            "schema": ReaderExtension("schema", required_build_effects=frozenset({":build/read-file"})),
        },
        build_effect_grants=frozenset({":build/read-file"} if granted else set()),
    )


def walk(form):
    yield form
    if form.get("kind") == "tagged":
        yield from walk(form["value"]["form"])
        return
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


def validate_accepted_extension() -> dict:
    source = ACCEPTED_EXTENSION.read_text(encoding="utf-8")
    artifact = read_source_to_artifact(source, str(ACCEPTED_EXTENSION.relative_to(ROOT)), policy=extension_policy(granted=True))
    nodes = [node for form in artifact["forms"] for node in walk(form)]
    tagged = [node for node in nodes if node["kind"] == "tagged"]
    require(tagged, "registered reader extension did not produce tagged syntax")
    require(tagged[0]["value"]["tag"] == "uuid", "reader extension tag mismatch")
    require(tagged[0]["value"]["form"]["kind"] == "string", "reader extension payload was not preserved")
    require("uuid" in artifact["reader_extension_registry"], "reader extension registry missing from artifact")
    return artifact


def validate_later_semantic_acceptance() -> dict:
    source = ACCEPTED_LATER_SEMANTIC.read_text(encoding="utf-8")
    diagnostic = read_source_diagnostic(source, str(ACCEPTED_LATER_SEMANTIC.relative_to(ROOT)))
    require(diagnostic is None, "reader rejected syntax that should be left to later semantic phases")
    return read_source_to_artifact(source, str(ACCEPTED_LATER_SEMANTIC.relative_to(ROOT)))


def validate_rejected_extension_build_effect() -> dict:
    diagnostic = read_source_diagnostic(
        REJECTED_EXTENSION_BUILD_EFFECT.read_text(encoding="utf-8"),
        str(REJECTED_EXTENSION_BUILD_EFFECT.relative_to(ROOT)),
        policy=extension_policy(granted=False),
    )
    require(diagnostic is not None, "ungranted reader extension build effect was accepted")
    require(diagnostic["id"] == "L1-READER-EXTENSION", "wrong reader extension diagnostic")
    return diagnostic


def validate_source_encoding() -> dict:
    diagnostic = read_source_bytes_diagnostic(b"(ns invalid.\xff (:profile :hosted))", "docs/artifacts/phase-01/fixtures/l1/invalid-encoding.gravity")
    require(diagnostic is not None, "invalid source bytes were accepted")
    require(diagnostic["id"] == "L1-SOURCE-ENCODING", "wrong source encoding diagnostic")
    return diagnostic


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--artifact-out", type=Path)
    args = parser.parse_args()

    try:
        extension_artifact = validate_accepted_extension()
        semantic_artifact = validate_later_semantic_acceptance()
        extension_diagnostic = validate_rejected_extension_build_effect()
        encoding_diagnostic = validate_source_encoding()
    except AssertionError as exc:
        print(f"L1 document coverage validation failed: {exc}", file=sys.stderr)
        return 1

    artifact = {
        "kind": "l1-document-coverage",
        "document": "L1",
        "accepted": [
            {
                "fixture": str(ACCEPTED_EXTENSION.relative_to(ROOT)),
                "artifact_kind": extension_artifact["kind"],
                "coverage": ["reader extension registry", "tagged syntax object", "payload span preservation"],
            },
            {
                "fixture": str(ACCEPTED_LATER_SEMANTIC.relative_to(ROOT)),
                "artifact_kind": semantic_artifact["kind"],
                "coverage": ["syntax-valid semantic rejection forwarded to later phases"],
            },
        ],
        "rejected": [
            {
                "fixture": str(REJECTED_EXTENSION_BUILD_EFFECT.relative_to(ROOT)),
                "diagnostic": extension_diagnostic["id"],
                "coverage": "ungranted reader extension build effect",
            },
            {
                "fixture": "invalid utf-8 byte sequence",
                "diagnostic": encoding_diagnostic["id"],
                "coverage": "source encoding policy",
            },
        ],
    }
    if args.artifact_out:
        args.artifact_out.parent.mkdir(parents=True, exist_ok=True)
        args.artifact_out.write_text(json.dumps(artifact, indent=2, sort_keys=True) + "\n", encoding="utf-8")

    print("L1 document coverage validation passed: 2 accepted fixtures, 2 rejected diagnostics")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
