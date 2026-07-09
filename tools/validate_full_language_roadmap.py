#!/usr/bin/env python3
"""Validate full-language roadmap completion claims.

This validator is intentionally stricter than the historical phase roadmaps.
The phase roadmaps can preserve stage0, stage3, and proof-surface evidence.
Full-language tasks, however, cannot be marked complete unless the evidence
shows user-exercisable capability, fixtures, diagnostics, artifacts, and
provenance at the claimed stage.
"""

from __future__ import annotations

import argparse
import json
import re
import sys
from pathlib import Path
from typing import Any


ROOT = Path(__file__).resolve().parents[1]
DOCS = ROOT / "docs"
ROADMAP = DOCS / "full-language-implementation-gap-map.md"
MATRIX = DOCS / "artifacts/full-language/coverage/full-language-coverage-matrix.json"
GAPS = DOCS / "artifacts/full-language/coverage/full-language-coverage-gaps.json"
FIXTURE_ROOT = ROOT / "tools/fixtures/full_language_roadmap"
P15_FINAL_SEED_ARTIFACT = (
    DOCS / "artifacts/phase-15/bootstrap/p15-s23-final-seed-retirement-proof.edn"
)
P15_FINAL_SEED_REPORT = (
    DOCS / "artifacts/phase-15/reports/p15-s23-final-seed-retirement-proof-report.md"
)
P15_BOOTSTRAP_DOC = DOCS / "bootstrap/clojure-bootstrap.md"
P15_ROADMAP = DOCS / "phase-15-bootstrap-and-self-hosting/IMPLEMENTATION-ROADMAP.md"
P15_FINAL_SEED_REPORT_LABEL = "p15-s23 final seed-retirement report"
P15_BOOTSTRAP_DOC_LABEL = "bootstrap clojure guide"
P15_ROADMAP_LABEL = "phase 15 roadmap"

TASK_RE = re.compile(r"^- \[(?P<mark>[ xX])\] `(?P<id>FL-P\d{2}-T\d{2})` (?P<title>.+)$")
LEDGER_RE = re.compile(
    r"^\|\s*(?P<date>\d{4}-\d{2}-\d{2})\s*\|\s*[^|]+\|\s*`(?P<id>FL-P\d{2}-T\d{2})`\s*\|(?P<evidence>[^|]+)\|(?P<result>[^|]+)\|"
)
COMMAND_RE = re.compile(r"\b(gravity|bin/gravity|clojure -M:gravity|python3 tools/)[^`;|]*")
P00_REQUIRED_TOKENS = {
    "FL-P00-T00": [
        "public binary audit",
        "bin/gravity check",
        "accepted fixture audit",
        "rejected fixture audit",
        "feature-specific public diagnostics",
    ],
    "FL-P00-T01": [
        "generate_full_language_coverage_matrix.py",
        "full-language-coverage-matrix.json",
        "full-language-coverage-gaps.json",
        "--self-test",
        "--write --audit-public",
    ],
    "FL-P00-T02": [
        "validate_full_language_roadmap.py",
        "validate_gravity_docs.py",
        "--self-test",
        "overclaim",
        "reject",
    ],
}
NON_P00_REQUIRED_TERMS = [
    "accepted",
    "rejected",
    "diagnostic",
    "artifact",
    "provenance",
]
SIMULATED_ONLY_TERMS = [
    "scaffold-only",
    "simulated proof",
    "generated manifest only",
    "proof metadata only",
]
P15_FINAL_SEED_INCOMPLETE_MARKERS = [
    ":status :incomplete",
    ":full-language-compiler-self-hosted? false",
    ":clojure-seed-retired? false",
    ":clojure-seed-boundary? true",
]
P15_FINAL_SEED_FORBIDDEN_WHEN_INCOMPLETE = {
    P15_FINAL_SEED_REPORT_LABEL: [
        r"Status:\s*complete",
        r"`:full-language-compiler-self-hosted\?\s+true`",
        r"`:clojure-seed-retired\?\s+true`",
        r"`:clojure-seed-boundary\?\s+false`",
        r"emitted status `:complete`",
        r"next required\s+capability `:advance_to_phase_16`",
    ],
    P15_BOOTSTRAP_DOC_LABEL: [
        r"It emits status `:complete`, records all required P15-S23 evidence",
        r"accepts\s+the `:p15-s23-final-seed-retirement` candidate",
        r"records\s+`:full-language-compiler-self-hosted\?\s+true`,\s+"
        r"`:clojure-seed-retired\?\s+true`,\s+and "
        r"`:clojure-seed-boundary\?\s+false`",
        r"final seed-retirement proof now\s+completes the P15-S23 gate",
    ],
    P15_ROADMAP_LABEL: [
        r"Status:\s*complete;\s*fail-closed evidence gate records "
        r"whole-language compiler\s+self-hosting and Clojure seed retirement proof",
    ],
}


class ValidationError(Exception):
    """Roadmap validation failure."""


def fail(message: str) -> None:
    print(f"full-language roadmap validation failed: {message}", file=sys.stderr)
    raise SystemExit(1)


def parse_tasks(text: str) -> dict[str, dict[str, Any]]:
    tasks: dict[str, dict[str, Any]] = {}
    for line_no, line in enumerate(text.splitlines(), start=1):
        match = TASK_RE.match(line)
        if not match:
            continue
        task_id = match.group("id")
        if task_id in tasks:
            raise ValidationError(f"duplicate task id {task_id} at line {line_no}")
        tasks[task_id] = {
            "id": task_id,
            "line": line_no,
            "complete": match.group("mark").lower() == "x",
            "title": match.group("title").strip(),
        }
    return tasks


def parse_ledger(text: str) -> dict[str, dict[str, str]]:
    rows: dict[str, dict[str, str]] = {}
    for line_no, line in enumerate(text.splitlines(), start=1):
        match = LEDGER_RE.match(line)
        if not match:
            continue
        task_id = match.group("id")
        rows[task_id] = {
            "line": str(line_no),
            "date": match.group("date"),
            "evidence": match.group("evidence").strip(),
            "result": match.group("result").strip(),
            "combined": f"{match.group('evidence')} {match.group('result')}".strip(),
        }
    return rows


def phase_number(task_id: str) -> int:
    return int(task_id.split("-")[1][1:])


def has_command_evidence(text: str) -> bool:
    return bool(COMMAND_RE.search(text))


def validate_matrix(matrix_path: Path = MATRIX, gaps_path: Path = GAPS) -> list[str]:
    errors: list[str] = []
    if not matrix_path.exists():
        return [f"missing coverage matrix {matrix_path.relative_to(ROOT)}"]
    if not gaps_path.exists():
        return [f"missing coverage gap report {gaps_path.relative_to(ROOT)}"]
    matrix = json.loads(matrix_path.read_text(encoding="utf-8"))
    gaps = json.loads(gaps_path.read_text(encoding="utf-8"))
    entries = matrix.get("entries", [])
    if matrix.get("inventoryCount") != 240 or len(entries) != 240:
        errors.append("coverage matrix must enumerate exactly 240 normative documents")
    if gaps.get("inventoryCount") != 240:
        errors.append("coverage gap report must record inventoryCount 240")
    complete_entries = [entry for entry in entries if entry.get("fullLanguageComplete")]
    summary_complete = matrix.get("summary", {}).get("fullLanguageCompleteDocuments")
    if summary_complete != len(complete_entries):
        errors.append("coverage matrix fullLanguageCompleteDocuments does not match entries")
    for entry in complete_entries:
        if entry.get("gaps"):
            errors.append(f"{entry.get('id')} is fullLanguageComplete while gaps remain")
        if entry.get("scaffoldOnlyCoverage"):
            errors.append(f"{entry.get('id')} is fullLanguageComplete from scaffold-only coverage")
        if not entry.get("publicAccepted"):
            errors.append(f"{entry.get('id')} is fullLanguageComplete without public accepted proof")
        if not entry.get("publicRejectedSpecific"):
            errors.append(f"{entry.get('id')} is fullLanguageComplete without public rejected diagnostic proof")
    return errors


def p15_final_seed_artifact_incomplete(artifact_text: str) -> bool:
    return all(marker in artifact_text for marker in P15_FINAL_SEED_INCOMPLETE_MARKERS)


def seed_retirement_truth_errors(
    artifact_text: str, documents: dict[str, str]
) -> list[str]:
    errors: list[str] = []
    if not p15_final_seed_artifact_incomplete(artifact_text):
        return errors
    for label, patterns in P15_FINAL_SEED_FORBIDDEN_WHEN_INCOMPLETE.items():
        text = documents.get(label)
        if text is None:
            errors.append(f"missing {label} for P15 final seed-retirement truth check")
            continue
        for pattern in patterns:
            if re.search(pattern, text, flags=re.MULTILINE | re.DOTALL):
                errors.append(
                    f"{label} claims P15 final seed retirement is complete "
                    "while the artifact is incomplete"
                )
                break
    return errors


def validate_seed_retirement_truth() -> list[str]:
    paths = {
        "P15 final seed-retirement artifact": P15_FINAL_SEED_ARTIFACT,
        P15_FINAL_SEED_REPORT_LABEL: P15_FINAL_SEED_REPORT,
        P15_BOOTSTRAP_DOC_LABEL: P15_BOOTSTRAP_DOC,
        P15_ROADMAP_LABEL: P15_ROADMAP,
    }
    missing = [f"missing {label} {path.relative_to(ROOT)}" for label, path in paths.items() if not path.exists()]
    if missing:
        return missing
    artifact_text = P15_FINAL_SEED_ARTIFACT.read_text(encoding="utf-8")
    documents = {
        P15_FINAL_SEED_REPORT_LABEL: P15_FINAL_SEED_REPORT.read_text(encoding="utf-8"),
        P15_BOOTSTRAP_DOC_LABEL: P15_BOOTSTRAP_DOC.read_text(encoding="utf-8"),
        P15_ROADMAP_LABEL: P15_ROADMAP.read_text(encoding="utf-8"),
    }
    return seed_retirement_truth_errors(artifact_text, documents)


def validate_completion_claims(text: str) -> list[str]:
    errors: list[str] = []
    tasks = parse_tasks(text)
    ledger = parse_ledger(text)
    if not tasks:
        return ["no full-language tasks found"]
    for task_id, task in tasks.items():
        if not task["complete"]:
            continue
        row = ledger.get(task_id)
        if not row:
            errors.append(f"{task_id} is complete without an evidence-ledger row")
            continue
        combined = row["combined"].lower()
        if not has_command_evidence(row["combined"]):
            errors.append(f"{task_id} evidence row lacks command evidence")
        if phase_number(task_id) == 0:
            for token in P00_REQUIRED_TOKENS.get(task_id, []):
                if token.lower() not in combined:
                    errors.append(f"{task_id} evidence row lacks required audit token `{token}`")
            continue
        for term in NON_P00_REQUIRED_TERMS:
            if term not in combined:
                errors.append(f"{task_id} evidence row lacks `{term}` evidence")
        if any(term in combined for term in SIMULATED_ONLY_TERMS):
            errors.append(f"{task_id} appears to rely on scaffold/proof-only evidence")
        if task_id.startswith("FL-P18-"):
            for term in ["gravity check", "gravity run", "gravity compile"]:
                if term not in combined:
                    errors.append(f"{task_id} final release evidence lacks `{term}`")
        if task_id in {"FL-P15-T03", "FL-P18-T02"} and ":clojure-seed-boundary? false" not in combined:
            errors.append(f"{task_id} lacks final Clojure seed-boundary proof")
    return errors


def validate_text(text: str, check_matrix: bool = True) -> list[str]:
    errors = validate_completion_claims(text)
    if check_matrix:
        errors.extend(validate_matrix())
        errors.extend(validate_seed_retirement_truth())
    return errors


def validate_current() -> None:
    if not ROADMAP.exists():
        fail(f"missing {ROADMAP.relative_to(ROOT)}")
    errors = validate_text(ROADMAP.read_text(encoding="utf-8"), check_matrix=True)
    if errors:
        fail("; ".join(errors))


def self_test() -> None:
    accepted = sorted((FIXTURE_ROOT / "accepted").glob("*.md"))
    rejected = sorted((FIXTURE_ROOT / "rejected").glob("*.md"))
    if not accepted or not rejected:
        fail("full-language roadmap validation fixtures are missing")
    for path in accepted:
        errors = validate_text(path.read_text(encoding="utf-8"), check_matrix=False)
        if errors:
            fail(f"accepted fixture failed {path.relative_to(ROOT)}: {'; '.join(errors)}")
    for path in rejected:
        errors = validate_text(path.read_text(encoding="utf-8"), check_matrix=False)
        if not errors:
            fail(f"rejected fixture unexpectedly passed {path.relative_to(ROOT)}")
        if not any("scaffold" in error or "lacks" in error or "without" in error for error in errors):
            fail(f"rejected fixture failed for the wrong reason {path.relative_to(ROOT)}: {errors}")
    stale_seed_errors = seed_retirement_truth_errors(
        "\n".join(P15_FINAL_SEED_INCOMPLETE_MARKERS),
        {
            P15_FINAL_SEED_REPORT_LABEL: "Status: complete\n`:clojure-seed-retired? true`",
            P15_BOOTSTRAP_DOC_LABEL: (
                "It emits status `:complete`, records all required P15-S23 evidence, "
                "accepts the `:p15-s23-final-seed-retirement` candidate"
            ),
            P15_ROADMAP_LABEL: (
                "Status: complete; fail-closed evidence gate records whole-language compiler "
                "self-hosting and Clojure seed retirement proof"
            ),
        },
    )
    if len(stale_seed_errors) != 3:
        fail("P15 final seed-retirement overclaim self-test did not reject stale claims")
    clean_seed_errors = seed_retirement_truth_errors(
        "\n".join(P15_FINAL_SEED_INCOMPLETE_MARKERS),
        {
            P15_FINAL_SEED_REPORT_LABEL: "Status: incomplete\n`:clojure-seed-retired? false`",
            P15_BOOTSTRAP_DOC_LABEL: "The final seed-retirement artifact remains incomplete.",
            P15_ROADMAP_LABEL: "final seed-retirement proof incomplete and fail-closed",
        },
    )
    if clean_seed_errors:
        fail(f"P15 final seed-retirement clean self-test failed: {'; '.join(clean_seed_errors)}")
    print("full-language roadmap validation self-test passed: accepted audit claims pass and overclaims fail")


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--self-test", action="store_true", help="run accepted/rejected validator fixtures")
    args = parser.parse_args()
    if args.self_test:
        self_test()
        return
    validate_current()
    print("full-language roadmap validation passed")


if __name__ == "__main__":
    main()
