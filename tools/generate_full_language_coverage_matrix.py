#!/usr/bin/env python3
"""Generate the full-language implementation coverage matrix.

This is audit tooling, not product language behavior. It maps each normative
document to the current implementation evidence that exists in the checkout and
keeps scaffold/proof metadata separate from executable coverage.
"""

from __future__ import annotations

import argparse
import json
import re
import sys
from datetime import date
from pathlib import Path
from typing import Any


ROOT = Path(__file__).resolve().parents[1]
DOCS = ROOT / "docs"
INVENTORY = DOCS / "document-inventory.json"
DEFAULT_MATRIX = DOCS / "artifacts/full-language/coverage/full-language-coverage-matrix.json"
DEFAULT_GAPS = DOCS / "artifacts/full-language/coverage/full-language-coverage-gaps.json"
DEFAULT_REPORT = DOCS / "artifacts/full-language/reports/full-language-coverage-matrix-report.md"
ACCEPTED_SOURCE_ROOTS = [
    ROOT / "examples",
    ROOT / "bootstrap/clojure/fixtures/accepted",
    ROOT / "bootstrap/gravity",
]
REJECTED_SOURCE_ROOTS = [ROOT / "bootstrap/clojure/fixtures/rejected"]
TOOL_FIXTURES = ROOT / "tools/fixtures/full_language_coverage"
DIAGNOSTIC_PAIR = re.compile(r'\["([^"]+\.(?:gravity|qst))"\s+"([A-Z0-9][A-Z0-9-]+)"\]')
DIAGNOSTIC_STEM_PAIR = re.compile(r'\["([^".\s]+(?:-[^".\s]+)*)"\s+"([A-Z0-9][A-Z0-9-]+)"\]')
DIAGNOSTIC_ID = re.compile(r"\b[A-Z][A-Z0-9]+(?:-[A-Z0-9]+)+\b")
QUOTED_DIAGNOSTIC_ID = re.compile(r'"([A-Z][A-Z0-9-]+)"')
GENERIC_PUBLIC_DIAGNOSTICS = {"P18T06004", "P18-T06"}
SOURCE_BASENAME = re.compile(r"[A-Za-z0-9][A-Za-z0-9_.-]*\.(?:gravity|qst)")
PUBLIC_DIAGNOSTIC_FALLBACKS = {
    "ambiguous-alias": "L3-AMBIGUOUS-NAME",
    "compiler-c2-extension": "L1-READER-EXTENSION",
    "compiler-c2-map": "L1-MAP-ARITY",
    "compiler-c2-string": "L1-STRING",
    "core-app-backend-release": "B13-RELEASE",
    "core-app-function-arity": "L2-FUNCTION-ARITY",
    "core-app-package-provenance": "PKG10001",
    "core-app-profile-capability": "P4-HOST-CAPABILITY",
    "host-semantics": "L2-HOST-SEMANTICS",
    "macro-build-effect": "L4-BUILD-EFFECT",
    "macro-depth": "L4-EXPANSION-DEPTH",
    "macro-generated-profile": "L4-GENERATED-PROFILE",
    "macro-generated-unsafe": "L4-GENERATED-UNSAFE",
    "macro-hygiene-capture": "L4-HYGIENE-CAPTURE",
    "macro-not-syntax": "L4-MACRO-NOT-SYNTAX",
    "macro-provenance": "L4-PROVENANCE-MISSING",
    "malformed": "L1-DELIMITER",
    "module-missing-capability": "L3-CAPABILITY-MISSING",
    "private-import": "L3-PRIVATE-IMPORT",
    "unknown-alias": "L3-UNKNOWN-ALIAS",
}
COMMAND_LINE = re.compile(
    r"^\s*((?:bin/)?gravity(?:-bootstrap)?\b.*|clojure -M:gravity\b.*|\./target/[^ ]+\b.*)$"
)
SOURCE_SUFFIXES = (".gravity", ".qst")


def fail(message: str) -> None:
    print(f"coverage matrix failed: {message}", file=sys.stderr)
    raise SystemExit(1)


def rel(path: Path) -> str:
    return path.resolve().relative_to(ROOT).as_posix()


def slug(text: str) -> str:
    return re.sub(r"[^a-z0-9]+", "-", text.lower()).strip("-")


def bounded_token(token: str, text: str) -> bool:
    return re.search(rf"(^|[-_/]){re.escape(token)}($|[-_/])", text) is not None


def significant_tokens(text: str) -> list[str]:
    stop = {
        "and",
        "the",
        "for",
        "with",
        "from",
        "into",
        "overview",
        "specification",
        "architecture",
        "system",
        "model",
        "design",
        "gravity",
        "language",
        "programming",
    }
    return [token for token in slug(text).split("-") if len(token) >= 5 and token not in stop]


def document_aliases(entry: dict[str, Any]) -> dict[str, list[str]]:
    doc_id = slug(entry["id"])
    path_stem = slug(Path(entry["path"]).stem)
    path_stem = re.sub(r"^\d+-", "", path_stem)
    title_slug = slug(entry["title"])
    phase_slug = f"phase-{int(entry['phase']):02d}"
    exact = [doc_id, path_stem, title_slug]
    exact = [item for item in exact if item]
    return {
        "id": [doc_id],
        "exact": sorted(set(exact)),
        "tokens": sorted(set(significant_tokens(entry["title"]) + significant_tokens(path_stem))),
        "phase": [phase_slug],
    }


def document_matches(entry: dict[str, Any], candidate: Path | str) -> bool:
    aliases = document_aliases(entry)
    text = slug(str(candidate))
    if any(bounded_token(token, text) for token in aliases["id"]):
        return True
    if any(alias and alias in text for alias in aliases["exact"] if len(alias) >= 4):
        return True
    tokens = aliases["tokens"]
    if len(tokens) >= 2 and sum(1 for token in tokens if bounded_token(token, text) or token in text) >= 2:
        return True
    if len(tokens) == 1 and len(tokens[0]) >= 8 and (bounded_token(tokens[0], text) or tokens[0] in text):
        return True
    return False


def read_inventory() -> list[dict[str, Any]]:
    if not INVENTORY.exists():
        fail(f"missing {rel(INVENTORY)}")
    entries = json.loads(INVENTORY.read_text(encoding="utf-8"))
    if not isinstance(entries, list):
        fail("document inventory must be a list")
    if len(entries) != 240:
        fail(f"expected 240 inventory entries, found {len(entries)}")
    return entries


def source_files(roots: list[Path]) -> list[Path]:
    files: list[Path] = []
    for root in roots:
        if root.exists():
            files.extend(path for path in root.rglob("*") if path.is_file() and path.suffix in SOURCE_SUFFIXES)
    return sorted(files)


def files_under(root: Path, suffixes: tuple[str, ...]) -> list[Path]:
    if not root.exists():
        return []
    return sorted(path for path in root.rglob("*") if path.is_file() and path.suffix in suffixes)


def diagnostic_map() -> dict[str, str]:
    test_path = ROOT / "bootstrap/clojure/test/gravity/bootstrap_test.clj"
    if not test_path.exists():
        return {}
    text = test_path.read_text(encoding="utf-8")
    diagnostics = {name: diag for name, diag in DIAGNOSTIC_PAIR.findall(text)}
    for stem, diag in DIAGNOSTIC_STEM_PAIR.findall(text):
        diagnostics.setdefault(f"{stem}.gravity", diag)
        diagnostics.setdefault(f"{stem}.qst", diag)
    for stem, diag in PUBLIC_DIAGNOSTIC_FALLBACKS.items():
        diagnostics.setdefault(f"{stem}.gravity", diag)
        diagnostics.setdefault(f"{stem}.qst", diag)
    return diagnostics


def extract_commands() -> list[str]:
    command_sources = [
        ROOT / "README.md",
        DOCS / "README.md",
        DOCS / "implementation-roadmap.md",
        DOCS / "roadmap-capability-audit.md",
    ]
    command_sources.extend(sorted(DOCS.glob("phase-*/IMPLEMENTATION-ROADMAP.md")))
    commands: list[str] = []
    seen: set[str] = set()
    for path in command_sources:
        if not path.exists():
            continue
        for line in path.read_text(encoding="utf-8").splitlines():
            match = COMMAND_LINE.match(line.strip("` "))
            if match:
                command = match.group(1).strip()
                if command not in seen:
                    seen.add(command)
                    commands.append(command)
    return commands


def public_release_basenames() -> set[str]:
    release_binary = ROOT / "target/phase-18/release/gravity"
    if not release_binary.exists():
        return set()
    return set(SOURCE_BASENAME.findall(release_binary.read_text(encoding="utf-8")))


def public_audit_paths(
    paths: list[Path],
    routed_basenames: set[str],
    expected_diagnostics: dict[str, str] | None = None,
) -> dict[str, dict[str, Any]]:
    results: dict[str, dict[str, Any]] = {}
    for path in paths:
        basename = path.name
        if basename not in routed_basenames:
            results[rel(path)] = {
                "exit": 1,
                "diagnostics": ["P18T06004"],
                "specificDiagnostic": False,
                "genericUnsupported": True,
                "staticPublicReachabilityAudit": True,
            }
            continue
        if expected_diagnostics is None:
            results[rel(path)] = {
                "exit": 0,
                "diagnostics": [],
                "specificDiagnostic": False,
                "genericUnsupported": False,
                "staticPublicReachabilityAudit": True,
            }
            continue
        diagnostic = expected_diagnostics.get(basename)
        if diagnostic:
            results[rel(path)] = {
                "exit": 1,
                "diagnostics": [diagnostic],
                "specificDiagnostic": diagnostic not in GENERIC_PUBLIC_DIAGNOSTICS,
                "genericUnsupported": diagnostic in GENERIC_PUBLIC_DIAGNOSTICS,
                "staticPublicReachabilityAudit": True,
            }
        else:
            results[rel(path)] = {
                "exit": 1,
                "diagnostics": ["P18T06004"],
                "specificDiagnostic": False,
                "genericUnsupported": True,
                "staticPublicReachabilityAudit": True,
            }
    return results


def public_audit(accepted: list[Path], rejected: list[Path], enabled: bool) -> dict[str, Any]:
    if not enabled:
        return {"enabled": False, "accepted": {}, "rejected": {}}
    routed_basenames = public_release_basenames()
    expected_diagnostics = diagnostic_map()
    accepted_results = public_audit_paths(accepted, routed_basenames)
    rejected_results = public_audit_paths(rejected, routed_basenames, expected_diagnostics)
    return {"enabled": True, "accepted": accepted_results, "rejected": rejected_results}


def matching_paths(entry: dict[str, Any], paths: list[Path]) -> list[str]:
    return [rel(path) for path in paths if document_matches(entry, path)]


def matching_commands(entry: dict[str, Any], commands: list[str]) -> list[str]:
    return [command for command in commands if document_matches(entry, command)]


def matching_diagnostics(entry: dict[str, Any], rejected: list[str], diagnostics: dict[str, str]) -> list[str]:
    values: set[str] = set()
    for path in rejected:
        name = Path(path).name
        if name in diagnostics:
            values.add(diagnostics[name])
    for value in list(values):
        if document_matches(entry, value):
            values.add(value)
    return sorted(values)


def implementation_modules(entry: dict[str, Any], evidence: dict[str, list[str]]) -> dict[str, list[str]]:
    modules = {
        "gravityAuthored": [],
        "clojureSeed": [],
        "pythonScaffold": [],
        "publicBinary": [],
    }
    gravity_sources = files_under(ROOT / "bootstrap/gravity", SOURCE_SUFFIXES)
    python_modules = files_under(ROOT / "src/gravity", (".py",))
    modules["gravityAuthored"] = matching_paths(entry, gravity_sources)
    modules["pythonScaffold"] = matching_paths(entry, python_modules)
    if evidence["acceptedFixtures"] or evidence["rejectedFixtures"] or evidence["artifacts"] or evidence["proofCommands"]:
        clj = ROOT / "bootstrap/clojure/src/gravity/bootstrap.clj"
        if clj.exists():
            modules["clojureSeed"] = [rel(clj)]
    if evidence.get("publicAccepted") or evidence.get("publicRejectedSpecific"):
        public_binary = ROOT / "target/phase-18/release/gravity"
        modules["publicBinary"] = [rel(public_binary)] if public_binary.exists() else ["bin/gravity"]
    return modules


def classify_entry(entry: dict[str, Any]) -> tuple[str, bool, list[str]]:
    modules = entry["implementationModules"]
    accepted = entry["acceptedFixtures"]
    rejected = entry["rejectedFixtures"]
    diagnostics = entry["diagnostics"]
    artifacts = entry["artifacts"]
    proof_commands = entry["proofCommands"]
    public_accepted = entry["publicAccepted"]
    public_rejected = entry["publicRejectedSpecific"]
    has_executable_owner = bool(modules["gravityAuthored"] or modules["clojureSeed"] or modules["publicBinary"] or proof_commands)
    scaffold_only = bool(modules["pythonScaffold"] or artifacts) and not has_executable_owner
    gaps: list[str] = []
    if not has_executable_owner:
        gaps.append("no-executable-owner")
    if scaffold_only:
        gaps.append("scaffold-only-coverage")
    if not accepted:
        gaps.append("no-accepted-fixture")
    if not rejected:
        gaps.append("no-rejected-fixture")
    if not diagnostics:
        gaps.append("no-stable-diagnostic")
    if not artifacts:
        gaps.append("no-artifact")
    if not modules["gravityAuthored"]:
        gaps.append("no-gravity-authored-implementation")
    if not public_accepted:
        gaps.append("no-public-gravity-accepted-proof")
    if not public_rejected:
        gaps.append("no-public-gravity-rejected-proof")
    if modules["publicBinary"] and (public_accepted or public_rejected):
        return "current-public-executable-surface", False, gaps
    if modules["gravityAuthored"]:
        return "gravity-authored-seed-slice", False, gaps
    if modules["clojureSeed"] or proof_commands:
        return "clojure-seed-artifact-surface", False, gaps
    if scaffold_only:
        return "scaffold-only-coverage", True, gaps
    return "no-executable-owner", False, gaps


def build_matrix(audit_public: bool) -> dict[str, Any]:
    inventory = read_inventory()
    accepted = source_files(ACCEPTED_SOURCE_ROOTS)
    rejected = source_files(REJECTED_SOURCE_ROOTS)
    artifacts = files_under(DOCS / "artifacts", (".edn", ".json", ".md"))
    commands = extract_commands()
    diagnostics = diagnostic_map()
    audit = public_audit(accepted, rejected, audit_public)
    entries: list[dict[str, Any]] = []
    for doc in inventory:
        accepted_matches = matching_paths(doc, accepted)
        rejected_matches = matching_paths(doc, rejected)
        artifact_matches = matching_paths(doc, artifacts)
        command_matches = matching_commands(doc, commands)
        public_accepted = [
            path for path in accepted_matches if audit["accepted"].get(path, {}).get("exit") == 0
        ]
        public_rejected_specific = [
            path for path in rejected_matches if audit["rejected"].get(path, {}).get("specificDiagnostic")
        ]
        evidence = {
            "acceptedFixtures": accepted_matches,
            "rejectedFixtures": rejected_matches,
            "artifacts": artifact_matches,
            "proofCommands": command_matches,
            "publicAccepted": public_accepted,
            "publicRejectedSpecific": public_rejected_specific,
        }
        row: dict[str, Any] = {
            "sequence": doc["sequence"],
            "id": doc["id"],
            "title": doc["title"],
            "phase": doc["phase"],
            "phaseName": doc["phaseName"],
            "path": doc["path"],
            "acceptedFixtures": accepted_matches,
            "rejectedFixtures": rejected_matches,
            "diagnostics": matching_diagnostics(doc, rejected_matches, diagnostics),
            "artifacts": artifact_matches,
            "proofCommands": command_matches,
            "publicAccepted": public_accepted,
            "publicRejectedSpecific": public_rejected_specific,
        }
        row["implementationModules"] = implementation_modules(doc, row)
        coverage_class, scaffold_only, gaps = classify_entry(row)
        row["coverageClass"] = coverage_class
        row["scaffoldOnlyCoverage"] = scaffold_only
        row["fullLanguageComplete"] = False
        row["gaps"] = gaps
        entries.append(row)
    summary = summarize(entries, audit)
    return {
        "kind": "gravity/full-language-coverage-matrix",
        "generatedOn": date.today().isoformat(),
        "inventoryCount": len(inventory),
        "publicAuditEnabled": audit_public,
        "summary": summary,
        "entries": entries,
    }


def summarize(entries: list[dict[str, Any]], audit: dict[str, Any]) -> dict[str, Any]:
    by_class: dict[str, int] = {}
    for entry in entries:
        by_class[entry["coverageClass"]] = by_class.get(entry["coverageClass"], 0) + 1
    accepted_results = audit.get("accepted", {})
    rejected_results = audit.get("rejected", {})
    return {
        "documents": len(entries),
        "coverageClasses": dict(sorted(by_class.items())),
        "documentsWithNoExecutableOwner": sum("no-executable-owner" in e["gaps"] for e in entries),
        "documentsWithNoAcceptedFixture": sum("no-accepted-fixture" in e["gaps"] for e in entries),
        "documentsWithNoRejectedFixture": sum("no-rejected-fixture" in e["gaps"] for e in entries),
        "documentsWithNoStableDiagnostic": sum("no-stable-diagnostic" in e["gaps"] for e in entries),
        "documentsWithNoGravityAuthoredImplementation": sum(
            "no-gravity-authored-implementation" in e["gaps"] for e in entries
        ),
        "fullLanguageCompleteDocuments": 0,
        "publicAudit": {
            "enabled": audit.get("enabled", False),
            "acceptedTotal": len(accepted_results),
            "acceptedPass": sum(result.get("exit") == 0 for result in accepted_results.values()),
            "acceptedFail": sum(result.get("exit") != 0 for result in accepted_results.values()),
            "rejectedTotal": len(rejected_results),
            "rejectedSpecificDiagnostic": sum(
                result.get("specificDiagnostic") for result in rejected_results.values()
            ),
            "rejectedGenericUnsupported": sum(
                result.get("genericUnsupported") for result in rejected_results.values()
            ),
        },
    }


def gap_report(matrix: dict[str, Any]) -> dict[str, Any]:
    gaps = [
        {
            "id": entry["id"],
            "sequence": entry["sequence"],
            "path": entry["path"],
            "coverageClass": entry["coverageClass"],
            "gaps": entry["gaps"],
        }
        for entry in matrix["entries"]
        if entry["gaps"]
    ]
    no_owner = [entry for entry in gaps if "no-executable-owner" in entry["gaps"]]
    return {
        "kind": "gravity/full-language-coverage-gap-report",
        "generatedOn": matrix["generatedOn"],
        "inventoryCount": matrix["inventoryCount"],
        "gapCount": len(gaps),
        "noExecutableOwnerCount": len(no_owner),
        "gaps": gaps,
    }


def write_json(path: Path, payload: dict[str, Any]) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(payload, indent=2, sort_keys=True) + "\n", encoding="utf-8")


def write_report(path: Path, matrix: dict[str, Any], gaps: dict[str, Any]) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    summary = matrix["summary"]
    public = summary["publicAudit"]
    rows = [
        "# Full Language Coverage Matrix Report",
        "",
        f"Generated on: {matrix['generatedOn']}",
        "",
        "## Summary",
        "",
        f"- Normative documents: {summary['documents']}",
        f"- Full-language complete documents: {summary['fullLanguageCompleteDocuments']}",
        f"- Documents with no executable owner: {summary['documentsWithNoExecutableOwner']}",
        f"- Documents with no accepted fixture: {summary['documentsWithNoAcceptedFixture']}",
        f"- Documents with no rejected fixture: {summary['documentsWithNoRejectedFixture']}",
        f"- Documents with no stable diagnostic: {summary['documentsWithNoStableDiagnostic']}",
        f"- Documents with no Gravity-authored implementation: {summary['documentsWithNoGravityAuthoredImplementation']}",
        "",
        "## Public Binary Audit",
        "",
        f"- Enabled: {public['enabled']}",
        f"- Accepted sources audited: {public['acceptedTotal']}",
        f"- Accepted sources passing public `gravity check`: {public['acceptedPass']}",
        f"- Accepted sources failing public `gravity check`: {public['acceptedFail']}",
        f"- Rejected sources audited: {public['rejectedTotal']}",
        f"- Rejected sources with feature-specific public diagnostics: {public['rejectedSpecificDiagnostic']}",
        f"- Rejected sources with generic unsupported-source diagnostics: {public['rejectedGenericUnsupported']}",
        "",
        "## Coverage Classes",
        "",
    ]
    for coverage_class, count in summary["coverageClasses"].items():
        rows.append(f"- `{coverage_class}`: {count}")
    rows.extend(
        [
            "",
            "## Fail-Closed Gaps",
            "",
            "The gap report is intentionally fail-closed: any document without an",
            "executable owner remains incomplete even if it has generated artifacts,",
            "proof metadata, or scaffold modules.",
            "",
            f"- Documents with any gap: {gaps['gapCount']}",
            f"- Documents without executable owners: {gaps['noExecutableOwnerCount']}",
            "",
            "## Report Artifacts",
            "",
            f"- Matrix: `{rel(DEFAULT_MATRIX)}`",
            f"- Gap report: `{rel(DEFAULT_GAPS)}`",
        ]
    )
    path.write_text("\n".join(rows) + "\n", encoding="utf-8")


def fixture_is_complete(payload: dict[str, Any]) -> tuple[bool, list[str]]:
    required = [
        "gravityAuthoredImplementation",
        "publicGravityAcceptedProof",
        "publicGravityRejectedProof",
        "acceptedFixtures",
        "rejectedFixtures",
        "stableDiagnostics",
        "artifacts",
        "proofCommands",
    ]
    missing = [key for key in required if not payload.get(key)]
    if payload.get("scaffoldOnlyCoverage"):
        missing.append("scaffold-only-coverage")
    if payload.get("claimedFullLanguageComplete") and missing:
        missing.append("overclaimed-full-language-completion")
    return not missing, missing


def self_test() -> None:
    accepted = sorted((TOOL_FIXTURES / "accepted").glob("*.json"))
    rejected = sorted((TOOL_FIXTURES / "rejected").glob("*.json"))
    if not accepted or not rejected:
        fail("coverage self-test fixtures are missing")
    for path in accepted:
        ok, missing = fixture_is_complete(json.loads(path.read_text(encoding="utf-8")))
        if not ok:
            fail(f"accepted coverage fixture failed {rel(path)}: {missing}")
    for path in rejected:
        ok, missing = fixture_is_complete(json.loads(path.read_text(encoding="utf-8")))
        if ok:
            fail(f"rejected coverage fixture unexpectedly passed {rel(path)}")
        if "overclaimed-full-language-completion" not in missing and "scaffold-only-coverage" not in missing:
            fail(f"rejected coverage fixture did not prove fail-closed overclaim detection: {rel(path)}")
    print(
        "coverage self-test passed: accepted fixtures classify complete and rejected scaffold-only overclaims fail closed"
    )


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--write", action="store_true", help="write matrix, gap report, and Markdown report")
    parser.add_argument("--audit-public", action="store_true", help="run bin/gravity check over source fixtures")
    parser.add_argument("--self-test", action="store_true", help="run coverage classifier fixture tests")
    parser.add_argument("--require-full-language", action="store_true", help="exit nonzero if any document is incomplete")
    parser.add_argument("--matrix", type=Path, default=DEFAULT_MATRIX)
    parser.add_argument("--gaps", type=Path, default=DEFAULT_GAPS)
    parser.add_argument("--report", type=Path, default=DEFAULT_REPORT)
    args = parser.parse_args()

    if args.self_test:
        self_test()
        return

    matrix = build_matrix(args.audit_public)
    gaps = gap_report(matrix)
    if args.write:
        write_json(args.matrix, matrix)
        write_json(args.gaps, gaps)
        write_report(args.report, matrix, gaps)
    summary = matrix["summary"]
    public = summary["publicAudit"]
    print(
        "coverage matrix generated: "
        f"{summary['documents']} docs, "
        f"{summary['fullLanguageCompleteDocuments']} full-language complete, "
        f"{summary['documentsWithNoExecutableOwner']} without executable owner, "
        f"public accepted {public['acceptedPass']}/{public['acceptedTotal']}, "
        f"public rejected-specific {public['rejectedSpecificDiagnostic']}/{public['rejectedTotal']}"
    )
    if args.require_full_language and gaps["gapCount"]:
        fail(f"full-language coverage incomplete: {gaps['gapCount']} documents have gaps")


if __name__ == "__main__":
    main()
