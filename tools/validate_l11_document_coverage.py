#!/usr/bin/env python3
"""Validate document-specific coverage for L11 concurrency model rules."""

from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / "src"))

from gravity.typed_core import check_source_diagnostic, check_source_to_typed_core_artifact  # noqa: E402


TYPED_FIXTURE_DIR = ROOT / "docs/artifacts/phase-01/fixtures/typed"
L11_FIXTURE_DIR = ROOT / "docs/artifacts/phase-01/fixtures/l11"
ACCEPTED = TYPED_FIXTURE_DIR / "accepted-typed-effected-core.gravity"
NEGATIVE_FIXTURES = {
    "rejected-data-race.gravity": "L11-DATA-RACE",
    "rejected-borrow-task.gravity": "L11-BORROW-TASK",
    "../typed/rejected-task-scope.gravity": "L11-TASK-SCOPE",
    "../typed/rejected-scheduler-profile.gravity": "L11-SCHEDULER",
    "rejected-atomic-order.gravity": "L11-ATOMIC-ORDER",
    "rejected-replay-race.gravity": "L11-REPLAY-RACE",
    "rejected-gpu-barrier.gravity": "L11-GPU-BARRIER",
}


def require(condition: bool, message: str) -> None:
    if not condition:
        raise AssertionError(message)


def validate_accepted() -> dict:
    artifact = check_source_to_typed_core_artifact(ACCEPTED.read_text(encoding="utf-8"), str(ACCEPTED.relative_to(ROOT)))
    require(artifact["concurrency_facts"], "concurrency facts missing")
    require(any(record["family"] == "structured-task" for record in artifact["concurrency_facts"]), "structured task fact missing")
    require(any(record["family"] == "structured-task-scope" for record in artifact["concurrency_facts"]), "task scope fact missing")
    return artifact


def validate_negative_fixtures() -> list[dict[str, str]]:
    diagnostics = []
    for filename, expected in NEGATIVE_FIXTURES.items():
        path = (L11_FIXTURE_DIR / filename).resolve()
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
        concurrency_artifact = validate_accepted()
        diagnostics = validate_negative_fixtures()
    except AssertionError as exc:
        print(f"L11 document coverage validation failed: {exc}", file=sys.stderr)
        return 1

    artifact = {
        "kind": "l11-document-coverage",
        "document": "L11",
        "accepted": [
            {
                "fixture": str(ACCEPTED.relative_to(ROOT)),
                "artifact_kind": concurrency_artifact["kind"],
                "coverage": ["structured task facts", "task scope graph", "scheduler capability facts"],
            }
        ],
        "rejected": diagnostics,
    }
    if args.artifact_out:
        args.artifact_out.parent.mkdir(parents=True, exist_ok=True)
        args.artifact_out.write_text(json.dumps(artifact, indent=2, sort_keys=True) + "\n", encoding="utf-8")

    print("L11 document coverage validation passed: 1 accepted artifact, 7 rejected diagnostics")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
