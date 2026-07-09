#!/usr/bin/env python3
"""Validate the Phase 01 P01-T05 typed/effected core checker."""

from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / "src"))

from gravity.typed_core import check_source_diagnostic, check_source_to_typed_core_artifact  # noqa: E402


FIXTURE_DIR = ROOT / "docs/artifacts/phase-01/fixtures/typed"
DEFAULT_ACCEPTED = FIXTURE_DIR / "accepted-typed-effected-core.gravity"
NEGATIVE_FIXTURES = {
    "rejected-type-mismatch.gravity": "L5-TYPE-MISMATCH",
    "rejected-dynamic-forbidden.gravity": "L5-DYNAMIC-FORBIDDEN",
    "rejected-effect-undeclared.gravity": "L6-EFFECT-UNDECLARED",
    "rejected-capability-missing.gravity": "L15-CAPABILITY-MISSING",
    "rejected-raw-safe.gravity": "L10-RAW-SAFE",
    "rejected-linear-resource.gravity": "L10-LINEAR-RESOURCE",
    "rejected-scheduler-profile.gravity": "L11-SCHEDULER",
    "rejected-task-scope.gravity": "L11-TASK-SCOPE",
}


def require(condition: bool, message: str) -> None:
    if not condition:
        raise AssertionError(message)


def validate_accepted(path: Path) -> dict:
    source = path.read_text(encoding="utf-8")
    artifact = check_source_to_typed_core_artifact(source, str(path.relative_to(ROOT)))
    require(artifact["kind"] == "typed-effected-core-artifact", "artifact kind mismatch")
    require(artifact["module"] == "typed.demo", "module mismatch")
    require(artifact["profile"] == ":hosted", "profile mismatch")
    require(not artifact["diagnostics"], "accepted artifact should not contain diagnostics")
    require(artifact["type_facts"], "type facts missing")
    require(artifact["function_signature_table"], "function signature table missing")
    require(artifact["function_latent_effect_table"], "function latent effect table missing")
    require(artifact["effect_environment"], "effect environment missing")
    require(artifact["namespace_effect_summary"]["inferred"], "inferred effect summary missing")
    require(":io/write" in artifact["namespace_effect_summary"]["inferred"], "io write effect missing")
    require(":memory/allocate" in artifact["namespace_effect_summary"]["inferred"], "allocation effect missing")
    require(":thread/spawn" in artifact["namespace_effect_summary"]["inferred"], "thread spawn effect missing")
    require(artifact["capability_report"], "capability report missing")
    require(artifact["provider_selection_records"], "provider selection records missing")
    require(any(record["capability"] == ":io/stdout" for record in artifact["provider_selection_records"]), "stdout provider missing")
    require(artifact["dynamic_boundary_records"], "hosted dynamic boundary record missing")
    require(artifact["memory_facts"], "memory facts missing")
    require(any(record["regime"] == "ownership-backed" for record in artifact["memory_facts"]), "ownership memory fact missing")
    require(artifact["ownership_resource_type_facts"], "linear resource table missing")
    require(any(record["status"] == "consumed" for record in artifact["ownership_resource_type_facts"]), "linear resource consumption missing")
    require(artifact["concurrency_facts"], "concurrency facts missing")
    require(any(record["family"] == "structured-task" for record in artifact["concurrency_facts"]), "task concurrency fact missing")
    return artifact


def validate_negative_fixtures() -> list[str]:
    observed: list[str] = []
    for filename, expected in NEGATIVE_FIXTURES.items():
        path = FIXTURE_DIR / filename
        diagnostic = check_source_diagnostic(path.read_text(encoding="utf-8"), str(path.relative_to(ROOT)))
        require(diagnostic is not None, f"{filename} did not produce a diagnostic")
        require(diagnostic["id"] == expected, f"{filename} produced {diagnostic['id']} instead of {expected}")
        for key in ["span", "remediation", "analyzer_stage"]:
            require(key in diagnostic, f"{filename} diagnostic missing {key}")
        if expected == "L15-CAPABILITY-MISSING":
            for key in ["requested_capability", "selected_or_missing_provider", "phase", "active_profile"]:
                require(key in diagnostic, f"{filename} capability diagnostic missing {key}")
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
        print(f"typed core validation failed: {exc}", file=sys.stderr)
        return 1

    if args.artifact_out:
        args.artifact_out.parent.mkdir(parents=True, exist_ok=True)
        args.artifact_out.write_text(json.dumps(artifact, indent=2, sort_keys=True) + "\n", encoding="utf-8")

    print(
        "typed core validation passed: "
        f"{len(artifact['type_facts'])} type facts, {len(diagnostics)} rejected fixtures"
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
