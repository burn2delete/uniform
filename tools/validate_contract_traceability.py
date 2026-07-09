#!/usr/bin/env python3
"""Validate Phase 00 contract traceability artifacts."""

from __future__ import annotations

import argparse
import json
import re
import sys
from pathlib import Path
from typing import Any


ROOT = Path(__file__).resolve().parents[1]

EXPECTED_SOURCE_DOCS = {
    "D0": "docs/phase-00-foundation-and-thesis/001-d0-gravity-vision-and-design-thesis.md",
    "D1": "docs/phase-00-foundation-and-thesis/002-d1-system-architecture-overview.md",
    "D2": "docs/phase-00-foundation-and-thesis/003-d2-implementation-roadmap-and-milestones.md",
    "D3": "docs/phase-00-foundation-and-thesis/004-d3-terminology-and-concept-model.md",
    "D4": "docs/phase-00-foundation-and-thesis/005-d4-universal-computing-coverage-charter.md",
    "D5": "docs/phase-00-foundation-and-thesis/006-d5-language-replacement-strategy.md",
    "D6": "docs/phase-00-foundation-and-thesis/007-d6-performance-philosophy-and-charter.md",
    "D7": "docs/phase-00-foundation-and-thesis/008-d7-extensibility-philosophy.md",
    "D8": "docs/phase-00-foundation-and-thesis/009-d8-safety-philosophy-and-charter.md",
    "D9": "docs/phase-00-foundation-and-thesis/010-d9-verifiability-and-mathematical-correctness-charter.md",
}

EXPECTED_RELEASE_GATES = {f"M{index}" for index in range(9)}
REQUIRED_BOUNDARIES = {
    "profile-target",
    "effect-capability",
    "runtime-backend",
    "artifact-file",
}
REQUIRED_TRACE_FIELDS = {
    "id",
    "source_doc",
    "requirement",
    "downstream_documents",
    "diagnostics",
    "artifacts",
    "release_gates",
    "evidence_expectation",
}
DIAGNOSTIC_RE = re.compile(r"^[A-Z][A-Z0-9]*-[A-Z0-9][A-Z0-9-]*$")


def load_json(path: Path) -> Any:
    try:
        return json.loads(path.read_text(encoding="utf-8"))
    except json.JSONDecodeError as exc:
        return {
            "_json_error": f"{path}:{exc.lineno}:{exc.colno}: {exc.msg}",
        }


def require_list(value: Any) -> bool:
    return isinstance(value, list) and bool(value)


def validate_index(index: Any, allow_subset: bool) -> list[tuple[str, str]]:
    diagnostics: list[tuple[str, str]] = []

    if isinstance(index, dict) and "_json_error" in index:
        return [("P00-T01-JSON", index["_json_error"])]
    if not isinstance(index, dict):
        return [("P00-T01-SHAPE", "traceability artifact must be a JSON object")]

    if index.get("kind") != "contract-traceability-index":
        diagnostics.append(("P00-T01-KIND", "kind must be contract-traceability-index"))

    source_docs = index.get("source_documents")
    if not isinstance(source_docs, list) or not source_docs:
        diagnostics.append(("P00-T01-MISSING-SOURCE-DOCS", "source_documents must be a non-empty list"))
        source_doc_ids: set[str] = set()
    else:
        source_doc_ids = set()
        for entry in source_docs:
            if not isinstance(entry, dict):
                diagnostics.append(("P00-T01-SOURCE-DOC-SHAPE", "source document entries must be objects"))
                continue
            doc_id = entry.get("id")
            path = entry.get("path")
            if not isinstance(doc_id, str) or not isinstance(path, str):
                diagnostics.append(("P00-T01-SOURCE-DOC-SHAPE", "source document entries need id and path"))
                continue
            source_doc_ids.add(doc_id)
            if not (ROOT / path).exists():
                diagnostics.append(("P00-T01-SOURCE-DOC-MISSING", f"{doc_id} path does not exist: {path}"))

    if not allow_subset:
        expected_ids = set(EXPECTED_SOURCE_DOCS)
        if source_doc_ids != expected_ids:
            missing = sorted(expected_ids - source_doc_ids)
            extra = sorted(source_doc_ids - expected_ids)
            diagnostics.append(
                (
                    "P00-T01-SOURCE-DOC-COVERAGE",
                    f"source document coverage mismatch; missing={missing}; extra={extra}",
                )
            )
        for doc_id, expected_path in EXPECTED_SOURCE_DOCS.items():
            matching = [
                entry
                for entry in source_docs or []
                if isinstance(entry, dict) and entry.get("id") == doc_id
            ]
            if matching and matching[0].get("path") != expected_path:
                diagnostics.append(
                    (
                        "P00-T01-SOURCE-DOC-PATH",
                        f"{doc_id} path must be {expected_path}",
                    )
                )

        boundaries = set(index.get("terminology_boundaries", []))
        missing_boundaries = sorted(REQUIRED_BOUNDARIES - boundaries)
        if missing_boundaries:
            diagnostics.append(
                (
                    "P00-T01-TERM-BOUNDARY",
                    f"missing D3 terminology boundaries: {missing_boundaries}",
                )
            )

    release_gates = index.get("release_gates", [])
    release_gate_ids = {
        entry.get("id")
        for entry in release_gates
        if isinstance(entry, dict) and isinstance(entry.get("id"), str)
    }
    if not allow_subset and release_gate_ids != EXPECTED_RELEASE_GATES:
        diagnostics.append(
            (
                "P00-T01-RELEASE-GATE-COVERAGE",
                f"release gates must cover M0..M8; found={sorted(release_gate_ids)}",
            )
        )

    traceability = index.get("traceability")
    if not isinstance(traceability, list) or not traceability:
        diagnostics.append(("P00-T01-MISSING-TRACEABILITY", "traceability must be a non-empty list"))
        return diagnostics

    seen_ids: set[str] = set()
    covered_docs: set[str] = set()
    covered_gates: set[str] = set()
    for offset, link in enumerate(traceability):
        if not isinstance(link, dict):
            diagnostics.append(("P00-T01-LINK-SHAPE", f"traceability link {offset} must be an object"))
            continue
        missing_fields = sorted(REQUIRED_TRACE_FIELDS - set(link))
        if missing_fields:
            diagnostics.append(
                (
                    "P00-T01-LINK-FIELDS",
                    f"{link.get('id', offset)} missing fields: {missing_fields}",
                )
            )

        link_id = link.get("id")
        if not isinstance(link_id, str) or not link_id:
            diagnostics.append(("P00-T01-LINK-ID", f"traceability link {offset} needs a stable id"))
        elif link_id in seen_ids:
            diagnostics.append(("P00-T01-LINK-ID", f"duplicate traceability id {link_id}"))
        else:
            seen_ids.add(link_id)

        source_doc = link.get("source_doc")
        if source_doc not in source_doc_ids:
            diagnostics.append(
                (
                    "P00-T01-LINK-SOURCE-DOC",
                    f"{link_id or offset} references unknown source_doc {source_doc!r}",
                )
            )
        elif isinstance(source_doc, str):
            covered_docs.add(source_doc)

        for field, code in [
            ("downstream_documents", "P00-T01-MISSING-DOWNSTREAM"),
            ("diagnostics", "P00-T01-MISSING-DIAGNOSTIC"),
            ("artifacts", "P00-T01-MISSING-ARTIFACT"),
            ("release_gates", "P00-T01-MISSING-RELEASE-GATE"),
        ]:
            values = link.get(field)
            if not require_list(values):
                diagnostics.append((code, f"{link_id or offset} requires non-empty {field}"))

        for diagnostic_id in link.get("diagnostics", []):
            if not isinstance(diagnostic_id, str) or not DIAGNOSTIC_RE.match(diagnostic_id):
                diagnostics.append(
                    (
                        "P00-T01-DIAGNOSTIC-ID",
                        f"{link_id or offset} has unstable diagnostic id {diagnostic_id!r}",
                    )
                )

        for gate_id in link.get("release_gates", []):
            if gate_id not in release_gate_ids:
                diagnostics.append(
                    (
                        "P00-T01-UNKNOWN-RELEASE-GATE",
                        f"{link_id or offset} references unknown release gate {gate_id!r}",
                    )
                )
            elif isinstance(gate_id, str):
                covered_gates.add(gate_id)

    if not allow_subset:
        missing_doc_links = sorted(set(EXPECTED_SOURCE_DOCS) - covered_docs)
        if missing_doc_links:
            diagnostics.append(
                (
                    "P00-T01-SOURCE-DOC-LINKAGE",
                    f"source docs without traceability links: {missing_doc_links}",
                )
            )
        missing_gate_links = sorted(EXPECTED_RELEASE_GATES - covered_gates)
        if missing_gate_links:
            diagnostics.append(
                (
                    "P00-T01-RELEASE-GATE-LINKAGE",
                    f"release gates without traceability links: {missing_gate_links}",
                )
            )

    return diagnostics


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("artifact", type=Path)
    parser.add_argument("--allow-subset", action="store_true")
    parser.add_argument("--expect-failure")
    args = parser.parse_args()

    index = load_json(args.artifact)
    diagnostics = validate_index(index, allow_subset=args.allow_subset)

    if args.expect_failure:
        for code, message in diagnostics:
            if code == args.expect_failure:
                print(f"expected diagnostic observed: {code}")
                print(message)
                return 0
        observed = ", ".join(code for code, _ in diagnostics) or "none"
        print(
            f"expected diagnostic {args.expect_failure} was not observed; observed: {observed}",
            file=sys.stderr,
        )
        return 1

    if diagnostics:
        for code, message in diagnostics:
            print(f"{code}: {message}", file=sys.stderr)
        return 1

    source_count = len(index.get("source_documents", []))
    trace_count = len(index.get("traceability", []))
    gate_count = len(index.get("release_gates", []))
    print(
        "contract traceability validation passed: "
        f"{source_count} source docs, {trace_count} trace links, {gate_count} release gates"
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
