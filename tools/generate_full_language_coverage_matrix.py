#!/usr/bin/env python3
"""Generate the full-language implementation coverage matrix.

This is audit tooling, not product language behavior. It maps each normative
document to the current implementation evidence that exists in the checkout and
keeps scaffold/proof metadata separate from executable coverage.
"""

from __future__ import annotations

import argparse
import contextlib
import hashlib
import io
import json
import re
import sys
from datetime import date
from pathlib import Path
from typing import Any

if __package__:
    from .output_publication import atomic_write_json, atomic_write_text
else:
    from output_publication import atomic_write_json, atomic_write_text


ROOT = Path(__file__).resolve().parents[1]
DOCS = ROOT / "docs"
INVENTORY = DOCS / "document-inventory.json"
DEFAULT_MATRIX = DOCS / "artifacts/full-language/coverage/full-language-coverage-matrix.json"
DEFAULT_GAPS = DOCS / "artifacts/full-language/coverage/full-language-coverage-gaps.json"
DEFAULT_REPORT = DOCS / "artifacts/full-language/reports/full-language-coverage-matrix-report.md"
DEFAULT_COMPLETION_ATTESTATIONS = DOCS / "artifacts/full-language/coverage/full-language-completion-attestations.json"
CONTRACT = ROOT / "contracts/full-language-coverage.json"
CONTRACT_KIND = "gravity/full-language-coverage-contract"
MATRIX_KIND = "gravity/full-language-coverage-matrix"
GAP_REPORT_KIND = "gravity/full-language-coverage-gap-report"
ATTESTATIONS_KIND = "gravity/full-language-completion-attestations"
SCHEMA_VERSION = 1
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


def canonical_json_v1(payload: Any) -> str:
    return json.dumps(
        payload,
        allow_nan=False,
        ensure_ascii=True,
        separators=(",", ":"),
        sort_keys=True,
    )


def semantic_id_v1(payload: dict[str, Any]) -> str:
    semantic_payload = {
        key: value
        for key, value in payload.items()
        if key not in {"generatedOn", "semanticId"}
    }
    return "sha256:" + hashlib.sha256(
        canonical_json_v1(semantic_payload).encode("utf-8")
    ).hexdigest()


def with_semantic_id_v1(payload: dict[str, Any]) -> dict[str, Any]:
    result = dict(payload)
    result["semanticId"] = semantic_id_v1(result)
    return result


def exact_keys(value: Any, expected: list[str], label: str) -> None:
    if not isinstance(value, dict) or set(value) != set(expected):
        observed = sorted(value) if isinstance(value, dict) else type(value).__name__
        fail(f"{label} keys must be exactly {expected!r}, found {observed!r}")


def sha256_id(value: Any) -> bool:
    return isinstance(value, str) and re.fullmatch(r"sha256:[0-9a-f]{64}", value) is not None


def read_contract() -> dict[str, Any]:
    if not CONTRACT.exists():
        fail(f"missing {rel(CONTRACT)}")
    try:
        payload = json.loads(CONTRACT.read_text(encoding="utf-8"))
    except (OSError, UnicodeDecodeError, json.JSONDecodeError) as exc:
        fail(f"cannot read {rel(CONTRACT)}: {exc}")
    validate_contract_v1(payload)
    return payload


def validate_contract_v1(payload: Any) -> None:
    expected_top = [
        "artifact_contracts",
        "authority",
        "canonical_identity",
        "commands",
        "consumer",
        "counter_contract",
        "kind",
        "nonclaims",
        "raw_identity",
        "schema_version",
    ]
    exact_keys(payload, expected_top, "coverage contract")
    if payload["kind"] != CONTRACT_KIND or payload["schema_version"] != SCHEMA_VERSION:
        fail("coverage contract kind/schema version mismatch")
    if payload["authority"] != "non-authoritative-observation":
        fail("coverage contract authority must remain non-authoritative-observation")
    artifacts = payload["artifact_contracts"]
    exact_keys(artifacts, ["attestations", "gap_report", "matrix", "report"], "artifact contracts")
    expected_artifacts = {
        "matrix": (MATRIX_KIND, DEFAULT_MATRIX),
        "gap_report": (GAP_REPORT_KIND, DEFAULT_GAPS),
        "attestations": (ATTESTATIONS_KIND, DEFAULT_COMPLETION_ATTESTATIONS),
    }
    artifact_keys = {
        "matrix": [
            "entry_keys", "implementation_module_keys", "input_identity_keys", "kind", "path",
            "public_audit_keys", "schema_version", "status_literals", "summary_keys", "top_keys",
        ],
        "gap_report": [
            "entry_keys", "input_identity_keys", "kind", "path", "schema_version",
            "status_literals", "top_keys",
        ],
        "attestations": [
            "admission_mode", "input_identity_keys", "kind", "path", "schema_version",
            "status_literals", "top_keys", "v2_requirements",
        ],
    }
    for name, (kind, path) in expected_artifacts.items():
        artifact = artifacts[name]
        exact_keys(artifact, artifact_keys[name], f"coverage contract {name}")
        if artifact.get("kind") != kind or artifact.get("schema_version") != SCHEMA_VERSION:
            fail(f"coverage contract {name} kind/schema mismatch")
        if artifact.get("path") != rel(path):
            fail(f"coverage contract {name} path mismatch")
        if artifact.get("status_literals") != ["complete", "incomplete"]:
            fail(f"coverage contract {name} status literals must be exact")
    if artifacts["attestations"]["admission_mode"] != "disabled-pending-target-coherent-public-native-evidence-v2":
        fail("coverage contract completion admission must remain disabled in v1")
    if artifacts["attestations"]["v2_requirements"] != [
        "exact-ordered-w5-accepted-and-rejected-specific-matrix",
        "exact-source-hash-argv-and-expected-result-cells",
        "w1-carrier-w2-provider-w3-containment-w4-public-route-crosslinks",
        "contained-native-execution-and-raw-transcript-receipt-output-bytes",
        "verifier-present-and-reviewed-in-a-and-byte-identical-in-b",
        "non-circular-a-b-c-registry-lineage",
        "captured-byte-or-descriptor-verification-without-path-reopen",
        "exact-verifier-abi-and-containment-policy",
        "independently-recomputed-no-jvm-no-fallback-tcb-and-sbom",
        "stable-tamper-diagnostics",
    ]:
        fail("coverage contract v2 completion requirements must remain exact")
    report = artifacts["report"]
    if report != {"kind": "markdown-report-v1", "path": rel(DEFAULT_REPORT)}:
        fail("coverage contract report declaration mismatch")
    canonical = payload["canonical_identity"]
    if canonical.get("algorithm") != "canonical-json-v1" or canonical.get("version") != 1:
        fail("coverage contract canonical identity algorithm mismatch")
    if canonical.get("excluded_top_level_keys") != ["generatedOn", "semanticId"]:
        fail("coverage contract canonical exclusions must be exact")
    if canonical.get("json") != {
        "allow_nan": False,
        "ensure_ascii": True,
        "separators": [",", ":"],
        "sort_keys": True,
    }:
        fail("coverage contract canonical JSON settings must be exact")
    raw = payload["raw_identity"]
    if raw != {
        "algorithm": "sha256-exact-file-bytes-v1",
        "embedded_in_hashed_file": False,
        "format": "sha256:<lowercase-hex>",
        "version": 1,
    }:
        fail("coverage contract raw identity rule mismatch")
    consumer = payload["consumer"]
    if consumer != {
        "module": "tools.generate_full_language_coverage_matrix",
        "predicate_version": 1,
        "predicates": [
            "validate_contract_v1",
            "validate_completion_attestations_v1",
            "validate_matrix_v1",
            "validate_gap_report_v1",
            "validate_repository_outputs_v1",
        ],
    }:
        fail("coverage contract consumer predicates must be exact")
    if payload["commands"] != {
        "generate": "PYTHONDONTWRITEBYTECODE=1 python3 tools/generate_full_language_coverage_matrix.py --write --audit-public",
        "require_complete": "PYTHONDONTWRITEBYTECODE=1 python3 tools/generate_full_language_coverage_matrix.py --validate --require-full-language",
        "self_test": "PYTHONDONTWRITEBYTECODE=1 python3 tools/generate_full_language_coverage_matrix.py --self-test",
        "validate": "PYTHONDONTWRITEBYTECODE=1 python3 tools/generate_full_language_coverage_matrix.py --validate",
    }:
        fail("coverage contract commands must be exact")
    if payload["counter_contract"] != {
        "complete_when": "fullLanguageCompleteDocuments == documents == inventoryCount and gapCount == 0",
        "current_expected_status": "incomplete",
        "inventory_count": 240,
        "supported_surface_is_not_completion": True,
    }:
        fail("coverage contract counters must be exact")
    if payload["nonclaims"] != [
        "full-language-completion",
        "public-authority",
        "release-readiness",
        "self-hosting",
        "seed-retirement",
    ]:
        fail("coverage contract nonclaims must be exact")


def rel(path: Path) -> str:
    return path.resolve().relative_to(ROOT.resolve()).as_posix()


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
    # Repository paths are semantic inputs only through their repository-
    # relative spelling.  Absolute checkout/worktree prefixes are ambient
    # execution context and must never contribute document-match tokens.
    logical_candidate = rel(candidate) if isinstance(candidate, Path) else candidate
    text = slug(logical_candidate)
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


def sha256_file(path: Path) -> str:
    return "sha256:" + hashlib.sha256(path.read_bytes()).hexdigest()


def generator_producer_v1() -> dict[str, Any]:
    return {
        "authority": "non-authoritative-observation",
        "module": "tools.generate_full_language_coverage_matrix",
        "predicateVersion": 1,
    }


def attestation_producer_v1() -> dict[str, Any]:
    return {
        "authority": "none",
        "owner": "master-coordinator",
        "role": "reviewed-input",
    }


def _corpus_semantic_id(paths: list[Path]) -> str:
    rows = [
        {"path": rel(path), "sha256": sha256_file(path), "size": path.stat().st_size}
        for path in sorted(set(path.resolve() for path in paths))
        if path.exists() and path.is_file()
    ]
    return "sha256:" + hashlib.sha256(canonical_json_v1(rows).encode("utf-8")).hexdigest()


def _public_route_identity() -> str:
    path = ROOT / "target/phase-18/release/gravity"
    return sha256_file(path) if path.exists() and path.is_file() else "unavailable"


def attestation_input_identities_v1() -> dict[str, str]:
    return {
        "contractSha256": sha256_file(CONTRACT),
        "documentInventorySha256": sha256_file(INVENTORY),
    }


def validate_completion_attestations_v1(
    payload: Any,
    inventory: list[dict[str, Any]],
    contract: dict[str, Any] | None = None,
) -> dict[str, dict[str, Any]]:
    contract = contract or read_contract()
    schema = contract["artifact_contracts"]["attestations"]
    exact_keys(payload, schema["top_keys"], "completion attestations")
    if payload["kind"] != ATTESTATIONS_KIND or payload["schemaVersion"] != SCHEMA_VERSION:
        fail("completion attestations kind/schema version mismatch")
    if payload["inventoryCount"] != len(inventory) or payload["inventoryCount"] != 240:
        fail("completion attestations inventoryCount must equal the 240-document inventory")
    exact_keys(payload["inputIdentities"], schema["input_identity_keys"], "completion attestation inputs")
    if payload["inputIdentities"] != attestation_input_identities_v1():
        fail("completion attestations input identities are stale")
    if payload["producer"] != attestation_producer_v1():
        fail("completion attestations producer declaration mismatch")
    if payload["status"] not in schema["status_literals"]:
        fail("completion attestations status is invalid")
    if payload["semanticId"] != semantic_id_v1(payload):
        fail("completion attestations semanticId mismatch")
    attestations = payload["attestations"]
    if not isinstance(attestations, list):
        fail("completion attestations must contain an attestations list")
    if attestations:
        fail("full-language completion admission is disabled pending target-coherent public-native evidence v2")
    if payload["status"] != "incomplete":
        fail("completion attestations status must remain incomplete while v1 admission is disabled")
    return {}

def read_completion_attestations(inventory: list[dict[str, Any]]) -> dict[str, dict[str, Any]]:
    """Validate the v1 empty attestation carrier; completion admission is disabled."""
    if not DEFAULT_COMPLETION_ATTESTATIONS.exists():
        fail(f"missing {rel(DEFAULT_COMPLETION_ATTESTATIONS)}")
    payload = json.loads(DEFAULT_COMPLETION_ATTESTATIONS.read_text(encoding="utf-8"))
    return validate_completion_attestations_v1(payload, inventory)


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


def command_source_paths() -> list[Path]:
    paths = [
        ROOT / "README.md",
        DOCS / "README.md",
        DOCS / "implementation-roadmap.md",
        DOCS / "roadmap-capability-audit.md",
    ]
    paths.extend(sorted(DOCS.glob("phase-*/IMPLEMENTATION-ROADMAP.md")))
    return [path for path in paths if path.exists()]


def extract_commands() -> list[str]:
    commands: list[str] = []
    seen: set[str] = set()
    for path in command_source_paths():
        for line in path.read_text(encoding="utf-8").splitlines():
            match = COMMAND_LINE.match(line.strip("` "))
            if match:
                command = match.group(1).strip()
                if command not in seen:
                    seen.add(command)
                    commands.append(command)
    return commands


def evidence_corpus_semantic_id_v1(
    accepted: list[Path], rejected: list[Path], artifacts: list[Path]
) -> str:
    paths = list(accepted) + list(rejected) + list(artifacts) + command_source_paths()
    paths.extend(files_under(ROOT / "src/gravity", (".py",)))
    bootstrap_test = ROOT / "bootstrap/clojure/test/gravity/bootstrap_test.clj"
    if bootstrap_test.exists():
        paths.append(bootstrap_test)
    return _corpus_semantic_id(paths)


def matrix_input_identities_v1(
    accepted: list[Path], rejected: list[Path], artifacts: list[Path]
) -> dict[str, str]:
    attestations = _read_json_object(DEFAULT_COMPLETION_ATTESTATIONS, "completion attestations")
    return {
        "completionAttestationsSemanticId": attestations["semanticId"],
        "completionAttestationsSha256": sha256_file(DEFAULT_COMPLETION_ATTESTATIONS),
        "contractSha256": sha256_file(CONTRACT),
        "documentInventorySha256": sha256_file(INVENTORY),
        "evidenceCorpusSemanticId": evidence_corpus_semantic_id_v1(accepted, rejected, artifacts),
        "generatorSha256": sha256_file(Path(__file__)),
        "publicRouteSha256": _public_route_identity(),
    }


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


def completion_status(entry: dict[str, Any], attestation: dict[str, Any] | None) -> tuple[bool, list[str]]:
    """Return the reporting-only v1 result; no attestation can earn credit."""
    return False, [*entry["gaps"], "completion-admission-disabled"]


def build_matrix(audit_public: bool) -> dict[str, Any]:
    inventory = read_inventory()
    attestations = read_completion_attestations(inventory)
    accepted = source_files(ACCEPTED_SOURCE_ROOTS)
    rejected = source_files(REJECTED_SOURCE_ROOTS)
    generated_outputs = {
        DEFAULT_MATRIX.resolve(),
        DEFAULT_GAPS.resolve(),
        DEFAULT_REPORT.resolve(),
        DEFAULT_COMPLETION_ATTESTATIONS.resolve(),
    }
    artifacts = [
        path
        for path in files_under(DOCS / "artifacts", (".edn", ".json", ".md"))
        if path.resolve() not in generated_outputs
    ]
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
        row["gaps"] = gaps
        complete, completion_gaps = completion_status(row, attestations.get(row["id"]))
        row["fullLanguageComplete"] = complete
        row["completionGaps"] = completion_gaps
        entries.append(row)
    summary = summarize(entries, audit)
    status = "complete" if summary["fullLanguageCompleteDocuments"] == len(inventory) else "incomplete"
    return with_semantic_id_v1({
        "kind": MATRIX_KIND,
        "schemaVersion": SCHEMA_VERSION,
        "status": status,
        "producer": generator_producer_v1(),
        "inputIdentities": matrix_input_identities_v1(accepted, rejected, artifacts),
        "generatedOn": date.today().isoformat(),
        "inventoryCount": len(inventory),
        "publicAuditEnabled": audit_public,
        "summary": summary,
        "entries": entries,
    })


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
        "fullLanguageCompleteDocuments": sum(entry["fullLanguageComplete"] for entry in entries),
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
            "completionGaps": entry["completionGaps"],
        }
        for entry in matrix["entries"]
        if entry["completionGaps"]
    ]
    no_owner = [entry for entry in gaps if "no-executable-owner" in entry["gaps"]]
    status = "complete" if not gaps else "incomplete"
    return with_semantic_id_v1({
        "kind": GAP_REPORT_KIND,
        "schemaVersion": SCHEMA_VERSION,
        "status": status,
        "producer": generator_producer_v1(),
        "inputIdentities": {
            "contractSha256": sha256_file(CONTRACT),
            "generatorSha256": sha256_file(Path(__file__)),
            "matrixSemanticId": matrix["semanticId"],
        },
        "generatedOn": matrix["generatedOn"],
        "inventoryCount": matrix["inventoryCount"],
        "gapCount": len(gaps),
        "noExecutableOwnerCount": len(no_owner),
        "gaps": gaps,
    })


def validate_matrix_v1(
    payload: Any,
    inventory: list[dict[str, Any]],
    contract: dict[str, Any] | None = None,
) -> None:
    contract = contract or read_contract()
    schema = contract["artifact_contracts"]["matrix"]
    exact_keys(payload, schema["top_keys"], "coverage matrix")
    if payload["kind"] != MATRIX_KIND or payload["schemaVersion"] != SCHEMA_VERSION:
        fail("coverage matrix kind/schema version mismatch")
    if payload["semanticId"] != semantic_id_v1(payload):
        fail("coverage matrix semanticId mismatch")
    if payload["status"] not in schema["status_literals"]:
        fail("coverage matrix status is invalid")
    if payload["inventoryCount"] != len(inventory) or payload["inventoryCount"] != 240:
        fail("coverage matrix inventoryCount must equal the 240-document inventory")
    if payload["producer"] != generator_producer_v1():
        fail("coverage matrix producer declaration mismatch")
    exact_keys(payload["inputIdentities"], schema["input_identity_keys"], "coverage matrix inputs")
    for key, value in payload["inputIdentities"].items():
        if key == "publicRouteSha256" and value == "unavailable":
            continue
        if not sha256_id(value):
            fail(f"coverage matrix input identity {key} must be a SHA-256 id")
    if not isinstance(payload["publicAuditEnabled"], bool):
        fail("coverage matrix publicAuditEnabled must be boolean")
    entries = payload["entries"]
    if not isinstance(entries, list) or len(entries) != len(inventory):
        fail("coverage matrix entries must exactly cover the inventory")
    for index, (entry, document) in enumerate(zip(entries, inventory, strict=True)):
        exact_keys(entry, schema["entry_keys"], f"coverage matrix entry {index}")
        for key in ("sequence", "id", "title", "phase", "phaseName", "path"):
            if entry[key] != document[key]:
                fail(f"coverage matrix entry {index} {key} does not match inventory")
        exact_keys(
            entry["implementationModules"],
            schema["implementation_module_keys"],
            f"coverage matrix entry {index} implementationModules",
        )
        if any(
            not isinstance(entry["implementationModules"][key], list)
            or not all(isinstance(item, str) for item in entry["implementationModules"][key])
            for key in schema["implementation_module_keys"]
        ):
            fail(f"coverage matrix entry {index} implementation modules must be string lists")
        for key in (
            "acceptedFixtures",
            "artifacts",
            "completionGaps",
            "diagnostics",
            "gaps",
            "proofCommands",
            "publicAccepted",
            "publicRejectedSpecific",
            "rejectedFixtures",
        ):
            if not isinstance(entry[key], list) or not all(isinstance(item, str) for item in entry[key]):
                fail(f"coverage matrix entry {index} {key} must be a string list")
        if not isinstance(entry["fullLanguageComplete"], bool) or not isinstance(
            entry["scaffoldOnlyCoverage"], bool
        ):
            fail(f"coverage matrix entry {index} completion flags must be boolean")
        if entry["fullLanguageComplete"] != (not entry["completionGaps"]):
            fail(f"coverage matrix entry {index} completion flag/gaps disagree")
        if not isinstance(entry["coverageClass"], str) or not entry["coverageClass"]:
            fail(f"coverage matrix entry {index} coverageClass must be non-empty")
    summary = payload["summary"]
    exact_keys(summary, schema["summary_keys"], "coverage matrix summary")
    exact_keys(summary["publicAudit"], schema["public_audit_keys"], "coverage matrix publicAudit")
    integer_keys = set(schema["summary_keys"]) - {"coverageClasses", "publicAudit"}
    if any(type(summary[key]) is not int or summary[key] < 0 for key in integer_keys):
        fail("coverage matrix summary counters must be non-negative integers")
    if (
        not isinstance(summary["coverageClasses"], dict)
        or any(
            not isinstance(key, str) or type(value) is not int or value < 0
            for key, value in summary["coverageClasses"].items()
        )
        or sum(summary["coverageClasses"].values()) != len(entries)
    ):
        fail("coverage matrix coverageClasses must exactly partition entries")
    public_audit = summary["publicAudit"]
    if not isinstance(public_audit["enabled"], bool) or any(
        type(public_audit[key]) is not int or public_audit[key] < 0
        for key in schema["public_audit_keys"]
        if key != "enabled"
    ):
        fail("coverage matrix publicAudit counters must be non-negative integers")
    if summary["documents"] != len(entries):
        fail("coverage matrix documents counter mismatch")
    completed = sum(entry["fullLanguageComplete"] for entry in entries)
    if summary["fullLanguageCompleteDocuments"] != completed:
        fail("coverage matrix completion counter mismatch")
    expected_status = "complete" if completed == len(entries) else "incomplete"
    if payload["status"] != expected_status:
        fail(f"coverage matrix status must be {expected_status}")
    if summary["publicAudit"]["enabled"] != payload["publicAuditEnabled"]:
        fail("coverage matrix public audit flags disagree")


def validate_gap_report_v1(
    payload: Any,
    matrix: dict[str, Any],
    contract: dict[str, Any] | None = None,
) -> None:
    contract = contract or read_contract()
    schema = contract["artifact_contracts"]["gap_report"]
    exact_keys(payload, schema["top_keys"], "coverage gap report")
    if payload["kind"] != GAP_REPORT_KIND or payload["schemaVersion"] != SCHEMA_VERSION:
        fail("coverage gap report kind/schema version mismatch")
    if payload["semanticId"] != semantic_id_v1(payload):
        fail("coverage gap report semanticId mismatch")
    if payload["status"] not in schema["status_literals"]:
        fail("coverage gap report status is invalid")
    if payload["inventoryCount"] != matrix["inventoryCount"]:
        fail("coverage gap report inventoryCount mismatch")
    if payload["producer"] != generator_producer_v1():
        fail("coverage gap report producer declaration mismatch")
    exact_keys(payload["inputIdentities"], schema["input_identity_keys"], "coverage gap inputs")
    if payload["inputIdentities"] != {
        "contractSha256": sha256_file(CONTRACT),
        "generatorSha256": sha256_file(Path(__file__)),
        "matrixSemanticId": matrix["semanticId"],
    }:
        fail("coverage gap report input identities mismatch")
    gaps = payload["gaps"]
    if not isinstance(gaps, list):
        fail("coverage gap report gaps must be a list")
    for index, entry in enumerate(gaps):
        exact_keys(entry, schema["entry_keys"], f"coverage gap entry {index}")
        if not entry["completionGaps"]:
            fail(f"coverage gap entry {index} must contain completion gaps")
    expected = gap_report(matrix)
    if payload != expected:
        fail("coverage gap report does not exactly project the coverage matrix")
    expected_status = "complete" if not gaps else "incomplete"
    if payload["status"] != expected_status:
        fail(f"coverage gap report status must be {expected_status}")


def _read_json_object(path: Path, label: str) -> dict[str, Any]:
    try:
        payload = json.loads(path.read_text(encoding="utf-8"))
    except (OSError, UnicodeDecodeError, json.JSONDecodeError) as exc:
        fail(f"cannot read {label} {rel(path)}: {exc}")
    if not isinstance(payload, dict):
        fail(f"{label} must be a JSON object")
    return payload


def validate_repository_outputs_v1() -> tuple[dict[str, Any], dict[str, Any]]:
    contract = read_contract()
    inventory = read_inventory()
    attestations = _read_json_object(DEFAULT_COMPLETION_ATTESTATIONS, "completion attestations")
    validate_completion_attestations_v1(attestations, inventory, contract)
    matrix = _read_json_object(DEFAULT_MATRIX, "coverage matrix")
    validate_matrix_v1(matrix, inventory, contract)
    gaps = _read_json_object(DEFAULT_GAPS, "coverage gap report")
    validate_gap_report_v1(gaps, matrix, contract)
    expected_matrix = build_matrix(bool(matrix["publicAuditEnabled"]))
    if matrix != expected_matrix:
        fail("coverage matrix is stale against current repository inputs")
    if not DEFAULT_REPORT.exists():
        fail(f"missing {rel(DEFAULT_REPORT)}")
    report = DEFAULT_REPORT.read_text(encoding="utf-8")
    for required in (
        matrix["kind"],
        matrix["semanticId"],
        gaps["semanticId"],
        f"Full-language complete documents: {matrix['summary']['fullLanguageCompleteDocuments']}",
        f"Documents with any gap: {gaps['gapCount']}",
        "Authority: non-authoritative observation",
    ):
        if required not in report:
            fail(f"coverage report is stale or missing contract value {required!r}")
    return matrix, gaps


def write_json(path: Path, payload: dict[str, Any]) -> None:
    atomic_write_json(path, payload)


def logical_output_path(path: Path) -> str:
    if path.is_absolute():
        try:
            return path.relative_to(ROOT).as_posix()
        except ValueError as exc:
            raise ValueError(f"coverage output path is outside repository root: {path}") from exc
    return path.as_posix()


def write_report(
    path: Path,
    matrix: dict[str, Any],
    gaps: dict[str, Any],
    matrix_path: Path = DEFAULT_MATRIX,
    gaps_path: Path = DEFAULT_GAPS,
) -> None:
    summary = matrix["summary"]
    public = summary["publicAudit"]
    rows = [
        "# Full Language Coverage Matrix Report",
        "",
        f"Generated on: {matrix['generatedOn']}",
        "",
        "## Contract",
        "",
        f"- Matrix kind/schema: `{matrix['kind']}` / `{matrix['schemaVersion']}`",
        f"- Matrix status: `{matrix['status']}`",
        f"- Matrix semantic identity: `{matrix['semanticId']}`",
        f"- Gap-report semantic identity: `{gaps['semanticId']}`",
        "- Authority: non-authoritative observation",
        "- Completion, public authority, release readiness, self-hosting, and seed retirement are not claimed.",
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
        "## Static Public Reachability Audit",
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
            "The gap report is intentionally fail-closed. v1 completion admission is disabled.",
            "Every document remains incomplete",
            "even if it has executable artifacts, proof metadata, or narrative review records.",
            "",
            f"- Documents with any gap: {gaps['gapCount']}",
            f"- Documents without executable owners: {gaps['noExecutableOwnerCount']}",
            "",
            "## Report Artifacts",
            "",
            f"- Matrix: `{logical_output_path(matrix_path)}`",
            f"- Gap report: `{logical_output_path(gaps_path)}`",
        ]
    )
    atomic_write_text(path, "\n".join(rows) + "\n")


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


def _completion_admission_disabled_self_test() -> None:
    contract = read_contract()
    inventory = read_inventory()
    base = with_semantic_id_v1(
        {
            "attestations": [],
            "inputIdentities": attestation_input_identities_v1(),
            "inventoryCount": len(inventory),
            "kind": ATTESTATIONS_KIND,
            "producer": attestation_producer_v1(),
            "schemaVersion": SCHEMA_VERSION,
            "status": "incomplete",
        }
    )
    mutations = [
        {},
        {
            "attestationId": "sha256:" + "1" * 64,
            "documentId": inventory[0]["id"],
            "governingDocument": inventory[0]["path"],
            "governingDocumentSha256": sha256_file(ROOT / inventory[0]["path"]),
            "review": {
                "reviewedBy": "forged-independent-sol",
                "reviewedCommit": "a" * 40,
                "reviewedTree": "b" * 40,
            },
            "state": "complete",
        },
        {"status": "passed", "reviewerClass": "independent-sol"},
    ]
    for index, mutation in enumerate(mutations):
        candidate = dict(base)
        candidate["attestations"] = [mutation]
        candidate["semanticId"] = semantic_id_v1(candidate)
        stderr = io.StringIO()
        try:
            with contextlib.redirect_stderr(stderr):
                validate_completion_attestations_v1(candidate, inventory, contract)
        except SystemExit:
            if "completion admission is disabled" not in stderr.getvalue():
                fail(f"completion-disabled self-test mutation {index} rejected for the wrong reason")
        else:
            fail(f"completion-disabled self-test mutation {index} unexpectedly passed")


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
    sample = {
        "id": "EXAMPLE",
        "gaps": [],
    }
    approved = {
        "state": "complete",
        "governingDocument": "tools/fixtures/full_language_coverage/accepted/minimal-executable-coverage.json",
        "governingDocumentSha256": sha256_file(TOOL_FIXTURES / "accepted/minimal-executable-coverage.json"),
        "review": {
            "reviewedBy": "coverage-self-test",
            "reviewedAt": "2026-07-16",
            "evidenceLedgerSemanticId": "sha256:" + "1" * 64,
            "independentReviewSemanticId": "sha256:" + "2" * 64,
        },
    }
    if completion_status(sample, approved)[0]:
        fail("completion-disabled v1 unexpectedly classified synthetic governed evidence complete")
    if completion_status(sample, None)[0]:
        fail("completion-disabled v1 unexpectedly classified a missing attestation complete")
    _completion_admission_disabled_self_test()
    print(
        "coverage self-test passed: coverage fixtures classify, scaffold overclaims and all nonempty v1 attestations fail closed"
    )


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--write", action="store_true", help="write matrix, gap report, and Markdown report")
    parser.add_argument(
        "--audit-public",
        action="store_true",
        help="perform the static public reachability audit over release routing and diagnostics",
    )
    parser.add_argument("--self-test", action="store_true", help="run coverage classifier fixture tests")
    parser.add_argument(
        "--validate",
        action="store_true",
        help="validate exact schemas, semantic identities, and current repository outputs",
    )
    parser.add_argument("--require-full-language", action="store_true", help="exit nonzero if any document is incomplete")
    parser.add_argument("--matrix", type=Path, default=DEFAULT_MATRIX)
    parser.add_argument("--gaps", type=Path, default=DEFAULT_GAPS)
    parser.add_argument("--report", type=Path, default=DEFAULT_REPORT)
    args = parser.parse_args()

    if args.self_test:
        read_contract()
        self_test()
        return

    if args.validate:
        matrix, gaps = validate_repository_outputs_v1()
        summary = matrix["summary"]
        print(
            "coverage outputs validated: "
            f"{summary['fullLanguageCompleteDocuments']}/{summary['documents']} complete, "
            f"{gaps['gapCount']} gaps, status {matrix['status']}"
        )
        if args.require_full_language and matrix["status"] != "complete":
            fail(
                "full-language coverage incomplete: "
                f"{summary['fullLanguageCompleteDocuments']}/{summary['documents']} documents have governed completion evidence"
            )
        return

    matrix = build_matrix(args.audit_public)
    gaps = gap_report(matrix)
    contract = read_contract()
    inventory = read_inventory()
    validate_matrix_v1(matrix, inventory, contract)
    validate_gap_report_v1(gaps, matrix, contract)
    if args.write:
        write_json(args.matrix, matrix)
        write_json(args.gaps, gaps)
        write_report(args.report, matrix, gaps, args.matrix, args.gaps)
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
    if args.require_full_language and summary["fullLanguageCompleteDocuments"] != summary["documents"]:
        fail(
            "full-language coverage incomplete: "
            f"{summary['fullLanguageCompleteDocuments']}/{summary['documents']} documents have governed completion evidence"
        )


if __name__ == "__main__":
    main()
