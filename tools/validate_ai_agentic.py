#!/usr/bin/env python3
"""Validate the Phase 11 AI and agentic programming contract."""

from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / "src"))

from gravity.ai_agentic import ai_agentic_diagnostic, validate_ai_agentic_file  # noqa: E402


FIXTURE_DIR = ROOT / "docs/artifacts/phase-11/fixtures/ai"
DEFAULT_ACCEPTED = FIXTURE_DIR / "accepted-ai-agentic.json"
NEGATIVE_FIXTURES = {
    "rejected-a1-tool-authority.json": "AI004",
    "rejected-a2-provider-capability.json": "A2001",
    "rejected-a3-authority.json": "A3003",
    "rejected-a4-human-review.json": "A4005",
    "rejected-a5-eval-gate.json": "A5005",
    "rejected-a6-replay-mode.json": "A6001",
    "rejected-a7-cross-tenant.json": "A7004",
    "rejected-a8-taint-policy.json": "A8004",
    "rejected-a9-eval-gate.json": "A9001",
    "rejected-a10-payload.json": "A10005",
    "rejected-a11-tool-escalation.json": "A11002",
}


def require(condition: bool, message: str) -> None:
    if not condition:
        raise AssertionError(message)


def validate_accepted(path: Path) -> dict:
    artifact = validate_ai_agentic_file(path)
    require(artifact["kind"] == "ai-agentic-artifact", "artifact kind mismatch")
    require(artifact["coverage_summary"]["documents"] == 11, "expected A1-A11 coverage")
    for key in [
        "ai_program_manifest",
        "model_manifest",
        "prompt_artifact",
        "tool_schema",
        "agent_manifest",
        "workflow_graph",
        "memory_policy",
        "policy_manifest",
        "evaluation_report",
        "human_review_manifest",
        "injection_defense",
        "document_contracts",
    ]:
        require(artifact[key], f"{key} missing")
    require(not artifact["diagnostics"], "accepted artifact should not contain diagnostics")
    return artifact


def validate_negative_fixtures() -> list[dict[str, str]]:
    observed = []
    for filename, expected in NEGATIVE_FIXTURES.items():
        path = FIXTURE_DIR / filename
        diagnostic = ai_agentic_diagnostic(path)
        require(diagnostic is not None, f"{filename} did not produce a diagnostic")
        require(diagnostic["id"] == expected, f"{filename} produced {diagnostic['id']} instead of {expected}")
        for key in ["source_span", "missing_fact", "remediation", "analyzer_stage"]:
            require(key in diagnostic, f"{filename} diagnostic missing {key}")
        observed.append({"fixture": filename, "diagnostic": diagnostic["id"]})
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
        print(f"AI agentic validation failed: {exc}", file=sys.stderr)
        return 1

    if args.artifact_out:
        args.artifact_out.parent.mkdir(parents=True, exist_ok=True)
        args.artifact_out.write_text(json.dumps(artifact, indent=2, sort_keys=True) + "\n", encoding="utf-8")

    print(
        "AI agentic validation passed: "
        f"{artifact['coverage_summary']['documents']} documents, "
        f"{len(diagnostics)} rejected fixtures"
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
