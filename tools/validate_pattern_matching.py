#!/usr/bin/env python3
"""Validate the Phase 01 L7 pattern matching analyzer."""

from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / "src"))

from gravity.patterns import analyze_patterns, analyze_patterns_diagnostic  # noqa: E402


FIXTURE_DIR = ROOT / "docs/artifacts/phase-01/fixtures/l7"
ACCEPTED = FIXTURE_DIR / "accepted-pattern-match.gravity"
NEGATIVE_FIXTURES = {
    "rejected-nonexhaustive.gravity": "L7-NONEXHAUSTIVE",
    "rejected-unreachable.gravity": "L7-UNREACHABLE",
    "rejected-duplicate-binding.gravity": "L7-DUP-BINDING",
    "rejected-pattern-type.gravity": "L7-PATTERN-TYPE",
    "rejected-guard-effect.gravity": "L7-GUARD-EFFECT",
    "rejected-unvalidated-shape.gravity": "L7-UNVALIDATED-SHAPE",
    "rejected-linear-move.gravity": "L7-LINEAR-MOVE",
}


def require(condition: bool, message: str) -> None:
    if not condition:
        raise AssertionError(message)


def validate_accepted() -> dict:
    artifact = analyze_patterns(ACCEPTED.read_text(encoding="utf-8"), str(ACCEPTED.relative_to(ROOT)))
    require(artifact["kind"] == "match-analysis-artifact", "artifact kind mismatch")
    require(artifact["match_count"] >= 3, "expected several match forms")
    require(artifact["decision_trees"], "decision trees missing")
    require(artifact["exhaustiveness_report"], "exhaustiveness report missing")
    require(artifact["branch_type_narrowing_table"], "narrowing table missing")
    require(artifact["branch_effect_summary"], "branch effect summary missing")
    require(artifact["schema_validation_links"], "schema validation links missing")
    require(artifact["ownership_move_borrow_facts"], "ownership facts missing")
    return artifact


def validate_negative_fixtures() -> list[dict[str, str]]:
    diagnostics = []
    for filename, expected in NEGATIVE_FIXTURES.items():
        path = FIXTURE_DIR / filename
        diagnostic = analyze_patterns_diagnostic(path.read_text(encoding="utf-8"), str(path.relative_to(ROOT)))
        require(diagnostic is not None, f"{filename} did not produce a diagnostic")
        require(diagnostic["id"] == expected, f"{filename} produced {diagnostic['id']} instead of {expected}")
        diagnostics.append({"fixture": str(path.relative_to(ROOT)), "diagnostic": diagnostic["id"]})
    return diagnostics


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--artifact-out", type=Path)
    args = parser.parse_args()

    try:
        artifact = validate_accepted()
        diagnostics = validate_negative_fixtures()
    except AssertionError as exc:
        print(f"pattern matching validation failed: {exc}", file=sys.stderr)
        return 1

    if args.artifact_out:
        args.artifact_out.parent.mkdir(parents=True, exist_ok=True)
        args.artifact_out.write_text(json.dumps(artifact, indent=2, sort_keys=True) + "\n", encoding="utf-8")

    print(f"pattern matching validation passed: {artifact['match_count']} matches, {len(diagnostics)} rejected fixtures")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
