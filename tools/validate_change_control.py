#!/usr/bin/env python3
"""Validate Phase 00 change-control workflow and ambiguity-log artifacts."""

from __future__ import annotations

import argparse
import json
import re
import sys
from pathlib import Path
from typing import Any


ROOT = Path(__file__).resolve().parents[1]
DEFAULT_WORKFLOW = ROOT / "docs/artifacts/phase-00/change-control-workflow.json"

MANDATORY_CATEGORIES = {
    "language-identity",
    "safety-guarantee",
    "profile-legality",
    "artifact-provenance",
    "bootstrap-trust",
}
CATEGORY_FIELDS = {
    "id",
    "trigger",
    "governing_docs",
    "affected_surfaces",
    "reviewers",
    "required_evidence",
    "blocking_diagnostics",
}
REQUEST_FIELDS = {
    "category",
    "change_summary",
    "affected_docs",
    "affected_surfaces",
    "required_evidence",
    "review_record",
    "diagnostics",
}
AMBIGUITY_FIELDS = {
    "id",
    "status",
    "governing_docs",
    "ambiguity",
    "resolution",
    "affected_surfaces",
    "evidence",
    "review_required",
}
CORE_EVIDENCE = {
    "migration-notes",
    "compatibility-analysis",
    "conformance-updates",
    "affected-document-list",
}
REQUIRED_DIAGNOSTICS = {
    "P00-T05-MISSING-EVIDENCE",
    "P00-T05-AFFECTED-SURFACES",
    "P00-T05-UNKNOWN-CATEGORY",
    "D2-SEQUENCE-SKIP",
    "D2-SAFETY-DEFERRED",
    "D2-ARTIFACT-MISSING",
    "D3-PROFILE-TARGET-CONFLATION",
    "D8-UNCLASSIFIED-DANGER",
    "D8-CAPABILITY-SAFETY",
    "D9-PROOF-MISSING",
    "D9-BOOTSTRAP-EQUIV",
}
DIAGNOSTIC_RE = re.compile(r"^[A-Z][A-Z0-9]*-[A-Z0-9][A-Z0-9-]*$")


def load_json(path: Path) -> Any:
    try:
        return json.loads(path.read_text(encoding="utf-8"))
    except json.JSONDecodeError as exc:
        return {"_json_error": f"{path}:{exc.lineno}:{exc.colno}: {exc.msg}"}


def string_list(value: Any) -> list[str]:
    if not isinstance(value, list):
        return []
    return [item for item in value if isinstance(item, str)]


def non_empty(value: Any) -> bool:
    if isinstance(value, list):
        return bool(value)
    if isinstance(value, dict):
        return bool(value)
    if isinstance(value, str):
        return bool(value.strip())
    return value is not None


def workflow_categories(workflow: dict[str, Any]) -> dict[str, dict[str, Any]]:
    categories: dict[str, dict[str, Any]] = {}
    for category in workflow.get("change_categories", []):
        if isinstance(category, dict) and isinstance(category.get("id"), str):
            categories[category["id"]] = category
    return categories


def validate_workflow(workflow: Any) -> list[tuple[str, str]]:
    diagnostics: list[tuple[str, str]] = []
    if isinstance(workflow, dict) and "_json_error" in workflow:
        return [("P00-T05-MISSING-EVIDENCE", workflow["_json_error"])]
    if not isinstance(workflow, dict):
        return [("P00-T05-MISSING-EVIDENCE", "workflow must be a JSON object")]
    if workflow.get("kind") != "change-control-workflow":
        diagnostics.append(("P00-T05-MISSING-EVIDENCE", "kind must be change-control-workflow"))

    categories = workflow_categories(workflow)
    missing_categories = sorted(MANDATORY_CATEGORIES - set(categories))
    if missing_categories:
        diagnostics.append(("P00-T05-UNKNOWN-CATEGORY", f"missing change categories: {missing_categories}"))

    workflow_diagnostics = set(workflow.get("diagnostics", []))
    missing_diagnostics = sorted(REQUIRED_DIAGNOSTICS - workflow_diagnostics)
    if missing_diagnostics:
        diagnostics.append(("P00-T05-MISSING-EVIDENCE", f"missing workflow diagnostics: {missing_diagnostics}"))
    for diagnostic_id in workflow_diagnostics:
        if not isinstance(diagnostic_id, str) or not DIAGNOSTIC_RE.match(diagnostic_id):
            diagnostics.append(("P00-T05-MISSING-EVIDENCE", f"unstable diagnostic id {diagnostic_id!r}"))

    for category_id, category in categories.items():
        missing_fields = sorted(CATEGORY_FIELDS - set(category))
        if missing_fields:
            diagnostics.append(("P00-T05-MISSING-EVIDENCE", f"{category_id} missing fields: {missing_fields}"))
        for field in CATEGORY_FIELDS - {"id"}:
            if not non_empty(category.get(field)):
                diagnostics.append(("P00-T05-MISSING-EVIDENCE", f"{category_id} requires non-empty {field}"))
        evidence = set(string_list(category.get("required_evidence")))
        missing_core = sorted(CORE_EVIDENCE - evidence)
        if missing_core:
            diagnostics.append(("P00-T05-MISSING-EVIDENCE", f"{category_id} missing core evidence: {missing_core}"))
        if category_id == "safety-guarantee" and "safety-review" not in evidence:
            diagnostics.append(("D2-SAFETY-DEFERRED", "safety-guarantee changes require safety-review evidence"))
        if category_id == "bootstrap-trust" and "bootstrap-equivalence-evidence" not in evidence:
            diagnostics.append(("D9-BOOTSTRAP-EQUIV", "bootstrap-trust changes require equivalence evidence"))

    for path in workflow.get("source_basis", []):
        if isinstance(path, str) and path.endswith(".md") and not (ROOT / path).exists():
            diagnostics.append(("P00-T05-MISSING-EVIDENCE", f"source basis path does not exist: {path}"))
    return diagnostics


def validate_ambiguity_log(log: Any) -> list[tuple[str, str]]:
    diagnostics: list[tuple[str, str]] = []
    if isinstance(log, dict) and "_json_error" in log:
        return [("P00-T05-MISSING-EVIDENCE", log["_json_error"])]
    if not isinstance(log, dict):
        return [("P00-T05-MISSING-EVIDENCE", "ambiguity log must be a JSON object")]
    if log.get("kind") != "cross-phase-ambiguity-log":
        diagnostics.append(("P00-T05-MISSING-EVIDENCE", "kind must be cross-phase-ambiguity-log"))
    entries = log.get("entries")
    if not isinstance(entries, list) or not entries:
        return diagnostics + [("P00-T05-MISSING-EVIDENCE", "ambiguity log entries must be non-empty")]

    covered_categories: set[str] = set()
    for entry in entries:
        if not isinstance(entry, dict):
            diagnostics.append(("P00-T05-MISSING-EVIDENCE", "ambiguity log entries must be objects"))
            continue
        entry_id = str(entry.get("id", "entry"))
        missing_fields = sorted(AMBIGUITY_FIELDS - set(entry))
        if missing_fields:
            diagnostics.append(("P00-T05-MISSING-EVIDENCE", f"{entry_id} missing fields: {missing_fields}"))
        for field in AMBIGUITY_FIELDS - {"review_required"}:
            if not non_empty(entry.get(field)):
                diagnostics.append(("P00-T05-MISSING-EVIDENCE", f"{entry_id} requires non-empty {field}"))
        category = entry.get("category")
        if isinstance(category, str):
            covered_categories.add(category)
        if entry.get("status") not in {"open", "resolved", "review-required"}:
            diagnostics.append(("P00-T05-MISSING-EVIDENCE", f"{entry_id} has invalid status {entry.get('status')!r}"))

    missing_coverage = sorted(MANDATORY_CATEGORIES - covered_categories)
    if missing_coverage:
        diagnostics.append(("P00-T05-AFFECTED-SURFACES", f"ambiguity log missing category coverage: {missing_coverage}"))
    return diagnostics


def validate_request(request: Any, workflow: dict[str, Any]) -> list[tuple[str, str]]:
    diagnostics = validate_workflow(workflow)
    if diagnostics:
        return diagnostics
    if isinstance(request, dict) and "_json_error" in request:
        return [("P00-T05-MISSING-EVIDENCE", request["_json_error"])]
    if not isinstance(request, dict):
        return [("P00-T05-MISSING-EVIDENCE", "request fixture must be a JSON object")]
    if request.get("kind") != "change-control-request-fixture":
        diagnostics.append(("P00-T05-MISSING-EVIDENCE", "kind must be change-control-request-fixture"))

    missing_request_fields = sorted(REQUEST_FIELDS - set(request))
    if missing_request_fields:
        diagnostics.append(("P00-T05-MISSING-EVIDENCE", f"request missing fields: {missing_request_fields}"))
    for field in REQUEST_FIELDS:
        if not non_empty(request.get(field)):
            diagnostics.append(("P00-T05-MISSING-EVIDENCE", f"request requires non-empty {field}"))

    categories = workflow_categories(workflow)
    category_id = request.get("category")
    if category_id not in categories:
        diagnostics.append(("P00-T05-UNKNOWN-CATEGORY", f"unknown category {category_id!r}"))
        return diagnostics

    required_evidence = set(string_list(categories[category_id].get("required_evidence")))
    supplied_evidence = set(string_list(request.get("required_evidence")))
    missing_evidence = sorted(required_evidence - supplied_evidence)
    if missing_evidence:
        diagnostics.append(("P00-T05-MISSING-EVIDENCE", f"{category_id} request missing evidence: {missing_evidence}"))

    affected_surfaces = set(string_list(request.get("affected_surfaces")))
    required_surfaces = set(string_list(categories[category_id].get("affected_surfaces")))
    if not affected_surfaces.intersection(required_surfaces):
        diagnostics.append(("P00-T05-AFFECTED-SURFACES", f"{category_id} request does not name an affected governed surface"))

    if request.get("moves_release_gate") is True and not request.get("release_gate_updates"):
        diagnostics.append(("D2-SEQUENCE-SKIP", "release-gate changes require release_gate_updates"))
    if request.get("weakens_safety") is True and "safety-review" not in supplied_evidence:
        diagnostics.append(("D2-SAFETY-DEFERRED", "safety weakening requires safety-review evidence"))
    if request.get("bootstrap_trust_change") is True and "bootstrap-equivalence-evidence" not in supplied_evidence:
        diagnostics.append(("D9-BOOTSTRAP-EQUIV", "bootstrap trust changes require equivalence evidence"))
    if request.get("artifact_provenance_change") is True and "artifact-schema-updates" not in supplied_evidence:
        diagnostics.append(("D2-ARTIFACT-MISSING", "artifact provenance changes require artifact-schema-updates evidence"))

    for diagnostic_id in string_list(request.get("diagnostics")):
        if diagnostic_id not in REQUIRED_DIAGNOSTICS or not DIAGNOSTIC_RE.match(diagnostic_id):
            diagnostics.append(("P00-T05-MISSING-EVIDENCE", f"unknown request diagnostic {diagnostic_id!r}"))
    return diagnostics


def validate_artifact(artifact: Any, workflow: dict[str, Any]) -> tuple[list[tuple[str, str]], str]:
    if isinstance(artifact, dict) and artifact.get("kind") == "change-control-workflow":
        return validate_workflow(artifact), "change-control workflow validation passed"
    if isinstance(artifact, dict) and artifact.get("kind") == "cross-phase-ambiguity-log":
        return validate_ambiguity_log(artifact), "cross-phase ambiguity log validation passed"
    if isinstance(artifact, dict) and artifact.get("kind") == "change-control-request-fixture":
        return validate_request(artifact, workflow), "change-control request fixture validation passed"
    return [("P00-T05-MISSING-EVIDENCE", "unsupported change-control artifact kind")], ""


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("artifact", type=Path)
    parser.add_argument("--workflow", type=Path, default=DEFAULT_WORKFLOW)
    parser.add_argument("--expect-failure")
    args = parser.parse_args()

    artifact = load_json(args.artifact)
    if isinstance(artifact, dict) and artifact.get("kind") == "change-control-workflow":
        workflow = artifact
    else:
        workflow = load_json(args.workflow)
    diagnostics, output = validate_artifact(artifact, workflow)

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

    print(output)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
