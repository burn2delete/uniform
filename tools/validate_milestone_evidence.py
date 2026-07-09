#!/usr/bin/env python3
"""Validate Phase 00 milestone evidence system artifacts and bundles."""

from __future__ import annotations

import argparse
import json
import re
import sys
from pathlib import Path
from typing import Any


ROOT = Path(__file__).resolve().parents[1]

EXPECTED_MILESTONES = [
    ("M0", "Documentation Lock"),
    ("M1", "Reader, Syntax, and Hosted Hello"),
    ("M2", "Typed Core and Effects"),
    ("M3", "Profiles and Safe-Code Contract"),
    ("M4", "MIR and Native Lowering"),
    ("M5", "Package, Build, and Schema Spine"),
    ("M6", "Workflow and AI Vertical Slice"),
    ("M7", "Standard Library and Tooling"),
    ("M8", "Self-Hosting Ramp"),
]
REQUIRED_SYSTEM_FIELDS = {
    "id",
    "sequence",
    "name",
    "governing_documents",
    "depends_on",
    "positive_fixtures",
    "negative_fixtures",
    "diagnostics",
    "required_artifacts",
    "proof_records",
    "claim_limits",
}
REQUIRED_BUNDLE_FIELDS = {
    "milestone_id",
    "release_claim_status",
    "governing_documents",
    "positive_fixtures",
    "negative_fixtures",
    "diagnostics",
    "emitted_artifacts",
    "profile_matrix",
    "capability_matrix",
    "safety_report",
    "reproducibility_record",
    "proof_records",
    "open_risks",
}
REQUIRED_RELEASE_DIAGNOSTICS = {
    "D2-MILESTONE-EVIDENCE",
    "D2-SEQUENCE-SKIP",
    "D2-SAFETY-DEFERRED",
    "D2-ARTIFACT-MISSING",
    "D8-UNCLASSIFIED-DANGER",
    "D8-CAPABILITY-SAFETY",
    "D9-PROOF-MISSING",
    "D9-CERT-UNCHECKABLE",
}
DIAGNOSTIC_RE = re.compile(r"^[A-Z][A-Z0-9]*-[A-Z0-9][A-Z0-9-]*$")


def load_json(path: Path) -> Any:
    try:
        return json.loads(path.read_text(encoding="utf-8"))
    except json.JSONDecodeError as exc:
        return {"_json_error": f"{path}:{exc.lineno}:{exc.colno}: {exc.msg}"}


def require_non_empty(value: Any) -> bool:
    if isinstance(value, list):
        return bool(value)
    if isinstance(value, dict):
        return bool(value)
    if isinstance(value, str):
        return bool(value.strip())
    return False


def add_missing_field_diagnostics(
    diagnostics: list[tuple[str, str]],
    owner: str,
    required: set[str],
    observed: dict[str, Any],
) -> None:
    for field in sorted(required - set(observed)):
        code = "D2-MILESTONE-EVIDENCE" if field in {"positive_fixtures", "negative_fixtures"} else "D2-ARTIFACT-MISSING"
        diagnostics.append((code, f"{owner} missing required field {field}"))


def validate_diagnostic_ids(values: Any, owner: str, diagnostics: list[tuple[str, str]]) -> None:
    if not isinstance(values, list):
        diagnostics.append(("D2-MILESTONE-EVIDENCE", f"{owner} diagnostics must be a non-empty list"))
        return
    for value in values:
        if not isinstance(value, str) or not DIAGNOSTIC_RE.match(value):
            diagnostics.append(("D2-MILESTONE-EVIDENCE", f"{owner} has unstable diagnostic id {value!r}"))


def validate_system(index: dict[str, Any]) -> list[tuple[str, str]]:
    diagnostics: list[tuple[str, str]] = []

    if index.get("kind") != "milestone-evidence-system":
        diagnostics.append(("D2-MILESTONE-EVIDENCE", "kind must be milestone-evidence-system"))

    release_diagnostics = set(index.get("release_blocking_diagnostics", []))
    missing_release_diags = sorted(REQUIRED_RELEASE_DIAGNOSTICS - release_diagnostics)
    if missing_release_diags:
        diagnostics.append(("D2-MILESTONE-EVIDENCE", f"missing release diagnostics: {missing_release_diags}"))

    milestones = index.get("milestones")
    if not isinstance(milestones, list) or len(milestones) != len(EXPECTED_MILESTONES):
        diagnostics.append(("D2-MILESTONE-EVIDENCE", "milestones must contain M0 through M8"))
        return diagnostics

    observed_ids: list[str] = []
    for position, milestone in enumerate(milestones):
        expected_id, expected_name = EXPECTED_MILESTONES[position]
        if not isinstance(milestone, dict):
            diagnostics.append(("D2-MILESTONE-EVIDENCE", f"milestone {position} must be an object"))
            continue

        owner = str(milestone.get("id", f"milestone-{position}"))
        add_missing_field_diagnostics(diagnostics, owner, REQUIRED_SYSTEM_FIELDS, milestone)
        observed_ids.append(owner)

        if milestone.get("id") != expected_id or milestone.get("name") != expected_name:
            diagnostics.append(
                (
                    "D2-SEQUENCE-SKIP",
                    f"milestone {position} must be {expected_id} - {expected_name}",
                )
            )
        if milestone.get("sequence") != position:
            diagnostics.append(("D2-SEQUENCE-SKIP", f"{owner} sequence must be {position}"))

        depends_on = milestone.get("depends_on", [])
        if position == 0:
            if depends_on != []:
                diagnostics.append(("D2-SEQUENCE-SKIP", "M0 must not depend on later milestones"))
        elif depends_on != [EXPECTED_MILESTONES[position - 1][0]]:
            diagnostics.append(("D2-SEQUENCE-SKIP", f"{owner} must depend on {EXPECTED_MILESTONES[position - 1][0]}"))

        for field in [
            "governing_documents",
            "positive_fixtures",
            "negative_fixtures",
            "diagnostics",
            "required_artifacts",
            "proof_records",
            "claim_limits",
        ]:
            if not require_non_empty(milestone.get(field)):
                code = "D2-MILESTONE-EVIDENCE" if field in {"positive_fixtures", "negative_fixtures"} else "D2-ARTIFACT-MISSING"
                diagnostics.append((code, f"{owner} requires non-empty {field}"))
        validate_diagnostic_ids(milestone.get("diagnostics"), owner, diagnostics)

    expected_ids = [milestone_id for milestone_id, _ in EXPECTED_MILESTONES]
    if observed_ids != expected_ids:
        diagnostics.append(("D2-SEQUENCE-SKIP", f"milestone order mismatch: {observed_ids}"))

    for path in index.get("source_basis", []):
        if isinstance(path, str) and path.endswith(".md") and not (ROOT / path).exists():
            diagnostics.append(("D2-ARTIFACT-MISSING", f"source basis path does not exist: {path}"))

    return diagnostics


def validate_bundle(bundle: dict[str, Any]) -> list[tuple[str, str]]:
    diagnostics: list[tuple[str, str]] = []

    if bundle.get("kind") != "milestone-evidence-bundle":
        diagnostics.append(("D2-MILESTONE-EVIDENCE", "kind must be milestone-evidence-bundle"))

    add_missing_field_diagnostics(diagnostics, "bundle", REQUIRED_BUNDLE_FIELDS, bundle)
    milestone_id = bundle.get("milestone_id")
    known_ids = {milestone_id for milestone_id, _ in EXPECTED_MILESTONES}
    if milestone_id not in known_ids:
        diagnostics.append(("D2-SEQUENCE-SKIP", f"unknown milestone_id {milestone_id!r}"))

    if bundle.get("release_claim_status") not in {"fixture-only", "review", "complete"}:
        diagnostics.append(("D2-MILESTONE-EVIDENCE", "release_claim_status must be fixture-only, review, or complete"))

    for field in REQUIRED_BUNDLE_FIELDS - {"release_claim_status"}:
        if not require_non_empty(bundle.get(field)):
            if field == "negative_fixtures":
                code = "D2-MILESTONE-EVIDENCE"
            elif field in {"safety_report", "proof_records"}:
                code = "D9-PROOF-MISSING"
            else:
                code = "D2-ARTIFACT-MISSING"
            diagnostics.append((code, f"bundle requires non-empty {field}"))

    validate_diagnostic_ids(bundle.get("diagnostics"), "bundle", diagnostics)

    if bundle.get("release_claim_status") == "complete":
        if bundle.get("sequence_status") != "upstream-complete":
            diagnostics.append(("D2-SEQUENCE-SKIP", "complete milestone claims require upstream-complete sequence_status"))
        if bundle.get("safety_status") != "classified":
            diagnostics.append(("D2-SAFETY-DEFERRED", "complete milestone claims require classified safety_status"))

    evidence_system = bundle.get("evidence_system")
    if isinstance(evidence_system, str) and not (ROOT / evidence_system).exists():
        diagnostics.append(("D2-ARTIFACT-MISSING", f"evidence_system path does not exist: {evidence_system}"))

    return diagnostics


def validate_artifact(artifact: Any) -> list[tuple[str, str]]:
    if isinstance(artifact, dict) and "_json_error" in artifact:
        return [("D2-MILESTONE-EVIDENCE", artifact["_json_error"])]
    if not isinstance(artifact, dict):
        return [("D2-MILESTONE-EVIDENCE", "artifact must be a JSON object")]

    kind = artifact.get("kind")
    if kind == "milestone-evidence-system":
        return validate_system(artifact)
    if kind == "milestone-evidence-bundle":
        return validate_bundle(artifact)
    return [("D2-MILESTONE-EVIDENCE", f"unsupported kind {kind!r}")]


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("artifact", type=Path)
    parser.add_argument("--expect-failure")
    args = parser.parse_args()

    artifact = load_json(args.artifact)
    diagnostics = validate_artifact(artifact)

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

    if isinstance(artifact, dict) and artifact.get("kind") == "milestone-evidence-system":
        print(
            "milestone evidence validation passed: "
            f"{len(artifact.get('milestones', []))} milestones, "
            f"{len(REQUIRED_BUNDLE_FIELDS)} required bundle fields"
        )
    else:
        print(f"milestone evidence bundle validation passed: {artifact.get('milestone_id')}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
