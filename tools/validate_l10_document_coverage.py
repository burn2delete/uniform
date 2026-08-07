#!/usr/bin/env python3
"""Validate document-specific coverage for L10 memory model rules."""

from __future__ import annotations

import argparse
import sys
from pathlib import Path

if __package__:
    from .output_publication import atomic_write_json
else:
    from output_publication import atomic_write_json


ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / "src"))

from gravity.typed_core import check_source_diagnostic, check_source_to_typed_core_artifact  # noqa: E402


TYPED_FIXTURE_DIR = ROOT / "docs/artifacts/phase-01/fixtures/typed"
L10_FIXTURE_DIR = ROOT / "docs/artifacts/phase-01/fixtures/l10"
ACCEPTED = TYPED_FIXTURE_DIR / "accepted-typed-effected-core.gravity"
NEGATIVE_FIXTURES = {
    "rejected-hidden-alloc.gravity": "L10-HIDDEN-ALLOC",
    "rejected-use-after-move.gravity": "L10-USE-AFTER-MOVE",
    "rejected-borrow-escape.gravity": "L10-BORROW-ESCAPE",
    "../l5/rejected-uninit-read.gravity": "L5-UNINIT-READ",
    "rejected-bounds.gravity": "L10-BOUNDS",
    "../typed/rejected-raw-safe.gravity": "L10-RAW-SAFE",
    "rejected-mmio-cap.gravity": "L10-MMIO-CAP",
    "../typed/rejected-linear-resource.gravity": "L10-LINEAR-RESOURCE",
}


def require(condition: bool, message: str) -> None:
    if not condition:
        raise AssertionError(message)


def validate_accepted() -> dict:
    artifact = check_source_to_typed_core_artifact(ACCEPTED.read_text(encoding="utf-8"), str(ACCEPTED.relative_to(ROOT)))
    require(artifact["memory_facts"], "memory facts missing")
    require(artifact["ownership_resource_type_facts"], "ownership/resource facts missing")
    require(any(record["regime"] == "ownership-backed" for record in artifact["memory_facts"]), "ownership-backed memory fact missing")
    return artifact


def validate_negative_fixtures() -> list[dict[str, str]]:
    diagnostics = []
    for filename, expected in NEGATIVE_FIXTURES.items():
        path = (L10_FIXTURE_DIR / filename).resolve()
        diagnostic = check_source_diagnostic(path.read_text(encoding="utf-8"), str(path.relative_to(ROOT)))
        require(diagnostic is not None, f"{filename} did not produce a diagnostic")
        require(diagnostic["id"] == expected, f"{filename} produced {diagnostic['id']} instead of {expected}")
        diagnostics.append({"fixture": str(path.relative_to(ROOT)), "diagnostic": diagnostic["id"]})
    return diagnostics


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--artifact-out", type=Path)
    args = parser.parse_args()

    try:
        memory_artifact = validate_accepted()
        diagnostics = validate_negative_fixtures()
    except AssertionError as exc:
        print(f"L10 document coverage validation failed: {exc}", file=sys.stderr)
        return 1

    artifact = {
        "kind": "l10-document-coverage",
        "document": "L10",
        "accepted": [
            {
                "fixture": str(ACCEPTED.relative_to(ROOT)),
                "artifact_kind": memory_artifact["kind"],
                "coverage": ["memory facts", "ownership-backed allocation", "linear resource consumption"],
            }
        ],
        "rejected": diagnostics,
    }
    if args.artifact_out:
        atomic_write_json(args.artifact_out, artifact)

    print("L10 document coverage validation passed: 1 accepted artifact, 8 rejected diagnostics")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
