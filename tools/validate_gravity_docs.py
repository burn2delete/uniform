#!/usr/bin/env python3
"""Validate the generated Gravity documentation set."""

from __future__ import annotations

import json
import re
import sys
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
DOCS = ROOT / "docs"
MANIFEST = DOCS / "document-inventory.json"
FORBIDDEN = re.compile(r"\b(TODO|TBD|FIXME|placeholder|lorem ipsum)\b", re.IGNORECASE)


def fail(message: str) -> None:
    print(f"validation failed: {message}", file=sys.stderr)
    raise SystemExit(1)


def assert_ascii(path: Path) -> None:
    try:
        path.read_text(encoding="ascii")
    except UnicodeDecodeError as exc:
        fail(f"non-ASCII text in {path}: {exc}")


def main() -> None:
    if not MANIFEST.exists():
        fail(f"missing manifest {MANIFEST}")

    inventory = json.loads(MANIFEST.read_text(encoding="utf-8"))
    if len(inventory) != 240:
        fail(f"expected 240 inventory entries, found {len(inventory)}")

    sequences = [entry["sequence"] for entry in inventory]
    if sequences != list(range(1, 241)):
        fail("document sequence must be exactly 1..240")

    ids = [entry["id"] for entry in inventory]
    if len(ids) != len(set(ids)):
        fail("document ids must be unique")

    doc_paths = [DOCS / entry["path"].removeprefix("docs/") for entry in inventory]
    for path in doc_paths:
        if not path.exists():
            fail(f"missing document {path}")
        text = path.read_text(encoding="utf-8")
        if len(text.splitlines()) < 80:
            fail(f"document is too thin: {path}")
        if FORBIDDEN.search(text):
            fail(f"forbidden placeholder marker in {path}")
        required_section_groups = [
            ("## Purpose",),
            ("## Requirements", "## Document-Specific Rules", "## Concrete Requirements"),
            ("## Dependencies", "## Semantic Dependencies"),
            ("## Outputs and Artifacts", "## Artifact Expectations", "## Required Outputs"),
            ("## Conformance Criteria", "## Conformance Checks", "## Acceptance Criteria"),
        ]
        for group in required_section_groups:
            if not any(section in text for section in group):
                fail(f"missing section equivalent to {group[0]} in {path}")

    markdown_files = sorted(DOCS.rglob("*.md")) + sorted((ROOT / "tools").glob("*.py"))
    for path in markdown_files:
        assert_ascii(path)

    phase_readmes = sorted(DOCS.glob("phase-*/README.md"))
    if len(phase_readmes) != 18:
        fail(f"expected 18 phase README files, found {len(phase_readmes)}")

    print("validation passed: 240 docs, 18 phase indexes, ASCII, no placeholders")


if __name__ == "__main__":
    main()
