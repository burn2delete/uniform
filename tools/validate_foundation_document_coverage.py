#!/usr/bin/env python3
"""Validate Phase 00 D0-D9 document coverage artifacts."""

from __future__ import annotations

import argparse
import json
import re
import sys
from pathlib import Path
from typing import Any


ROOT = Path(__file__).resolve().parents[1]

EXPECTED_DOCS = {
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
ENTRY_FIELDS = {
    "id",
    "path",
    "implementation_surface",
    "accepted_behavior",
    "rejected_behavior",
    "required_artifacts",
    "diagnostics",
    "dependencies",
    "conformance_criteria",
    "evidence_refs",
}
LIST_FIELDS = ENTRY_FIELDS - {"id", "path", "implementation_surface"}
DIAGNOSTIC_RE = re.compile(r"^[A-Z][A-Z0-9]*-[A-Z0-9][A-Z0-9-]*$")


def load_json(path: Path) -> Any:
    try:
        return json.loads(path.read_text(encoding="utf-8"))
    except json.JSONDecodeError as exc:
        return {"_json_error": f"{path}:{exc.lineno}:{exc.colno}: {exc.msg}"}


def non_empty_list(value: Any) -> bool:
    return isinstance(value, list) and bool(value)


def validate_entry(entry: Any, allow_subset: bool) -> list[tuple[str, str]]:
    diagnostics: list[tuple[str, str]] = []
    if not isinstance(entry, dict):
        return [("P00-DOC-MISSING-FIELD", "document coverage entry must be an object")]
    entry_id = entry.get("id")
    if entry_id not in EXPECTED_DOCS:
        diagnostics.append(("P00-DOC-COVERAGE", f"unknown foundation doc id {entry_id!r}"))
        return diagnostics

    missing_fields = sorted(ENTRY_FIELDS - set(entry))
    if missing_fields:
        diagnostics.append(("P00-DOC-MISSING-FIELD", f"{entry_id} missing fields: {missing_fields}"))
    if entry.get("path") != EXPECTED_DOCS[entry_id]:
        diagnostics.append(("P00-DOC-COVERAGE", f"{entry_id} path must be {EXPECTED_DOCS[entry_id]}"))
    if not (ROOT / EXPECTED_DOCS[entry_id]).exists():
        diagnostics.append(("P00-DOC-SOURCE-MISSING", f"{entry_id} source doc is missing"))
    if not isinstance(entry.get("implementation_surface"), str) or not entry.get("implementation_surface"):
        diagnostics.append(("P00-DOC-MISSING-FIELD", f"{entry_id} requires implementation_surface"))

    for field in LIST_FIELDS:
        if not non_empty_list(entry.get(field)):
            code = "P00-DOC-MISSING-ARTIFACTS" if field == "required_artifacts" else "P00-DOC-MISSING-FIELD"
            diagnostics.append((code, f"{entry_id} requires non-empty {field}"))

    for diagnostic_id in entry.get("diagnostics", []):
        if not isinstance(diagnostic_id, str) or not DIAGNOSTIC_RE.match(diagnostic_id):
            diagnostics.append(("P00-DOC-DIAGNOSTIC-ID", f"{entry_id} has unstable diagnostic {diagnostic_id!r}"))

    for evidence_ref in entry.get("evidence_refs", []):
        if isinstance(evidence_ref, str) and not (ROOT / evidence_ref).exists():
            diagnostics.append(("P00-DOC-MISSING-ARTIFACTS", f"{entry_id} evidence ref does not exist: {evidence_ref}"))

    if not allow_subset and entry_id == "D0" and "root-contract" not in entry.get("dependencies", []):
        diagnostics.append(("P00-DOC-COVERAGE", "D0 dependencies must identify root-contract"))
    return diagnostics


def validate_coverage(artifact: Any, allow_subset: bool) -> list[tuple[str, str]]:
    diagnostics: list[tuple[str, str]] = []
    if isinstance(artifact, dict) and "_json_error" in artifact:
        return [("P00-DOC-MISSING-FIELD", artifact["_json_error"])]
    if not isinstance(artifact, dict):
        return [("P00-DOC-MISSING-FIELD", "coverage artifact must be a JSON object")]
    if artifact.get("kind") not in {"foundation-document-coverage", "foundation-document-coverage-fixture"}:
        diagnostics.append(("P00-DOC-MISSING-FIELD", "kind must be foundation-document-coverage or foundation-document-coverage-fixture"))

    entries = artifact.get("documents")
    if not isinstance(entries, list) or not entries:
        return diagnostics + [("P00-DOC-COVERAGE", "documents must be a non-empty list")]

    seen_ids: list[str] = []
    for entry in entries:
        diagnostics.extend(validate_entry(entry, allow_subset=allow_subset))
        if isinstance(entry, dict) and isinstance(entry.get("id"), str):
            seen_ids.append(entry["id"])

    if len(seen_ids) != len(set(seen_ids)):
        diagnostics.append(("P00-DOC-COVERAGE", f"duplicate document coverage ids: {seen_ids}"))

    if not allow_subset:
        expected_ids = list(EXPECTED_DOCS)
        if seen_ids != expected_ids:
            diagnostics.append(("P00-DOC-COVERAGE", f"document coverage order must be {expected_ids}; found {seen_ids}"))
    return diagnostics


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("artifact", type=Path)
    parser.add_argument("--allow-subset", action="store_true")
    parser.add_argument("--expect-failure")
    args = parser.parse_args()

    artifact = load_json(args.artifact)
    diagnostics = validate_coverage(artifact, allow_subset=args.allow_subset)

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

    entries = artifact.get("documents", []) if isinstance(artifact, dict) else []
    print(f"foundation document coverage validation passed: {len(entries)} documents")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
