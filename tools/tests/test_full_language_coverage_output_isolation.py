#!/usr/bin/env python3
"""Focused output-boundary tests for the full-language coverage generator."""

from __future__ import annotations

import contextlib
import copy
import io
import json
import os
from pathlib import Path
import subprocess
import sys
import tempfile
import unittest
from unittest import mock


ROOT = Path(__file__).resolve().parents[2]

from tools import generate_full_language_coverage_matrix as coverage


class FullLanguageCoverageOutputIsolationTests(unittest.TestCase):
    def test_versioned_repository_outputs_validate_and_remain_incomplete(self) -> None:
        matrix, gaps = coverage.validate_repository_outputs_v1()
        self.assertEqual(matrix["kind"], coverage.MATRIX_KIND)
        self.assertEqual(matrix["schemaVersion"], 1)
        self.assertEqual(matrix["status"], "incomplete")
        self.assertEqual(matrix["summary"]["fullLanguageCompleteDocuments"], 0)
        self.assertEqual(matrix["inventoryCount"], 240)
        self.assertEqual(gaps["status"], "incomplete")
        self.assertEqual(gaps["gapCount"], 240)
        self.assertTrue(coverage.sha256_id(matrix["semanticId"]))
        self.assertTrue(coverage.sha256_id(gaps["semanticId"]))

    def test_semantic_identity_ignores_only_date_and_own_identity(self) -> None:
        payload = {
            "generatedOn": "2026-08-08",
            "kind": "example",
            "semanticId": "sha256:" + "0" * 64,
            "value": 1,
        }
        baseline = coverage.semantic_id_v1(payload)
        changed_date = dict(payload, generatedOn="2026-08-09")
        changed_identity = dict(payload, semanticId="sha256:" + "1" * 64)
        changed_value = dict(payload, value=2)
        self.assertEqual(coverage.semantic_id_v1(changed_date), baseline)
        self.assertEqual(coverage.semantic_id_v1(changed_identity), baseline)
        self.assertNotEqual(coverage.semantic_id_v1(changed_value), baseline)

    def test_exact_schema_and_identity_mutations_fail_closed(self) -> None:
        inventory = coverage.read_inventory()
        contract = coverage.read_contract()
        matrix = json.loads(coverage.DEFAULT_MATRIX.read_text(encoding="utf-8"))
        attestations = json.loads(
            coverage.DEFAULT_COMPLETION_ATTESTATIONS.read_text(encoding="utf-8")
        )
        mutations = []
        extra_matrix_key = copy.deepcopy(matrix)
        extra_matrix_key["unexpected"] = True
        mutations.append(
            lambda: coverage.validate_matrix_v1(extra_matrix_key, inventory, contract)
        )
        stale_matrix_id = copy.deepcopy(matrix)
        stale_matrix_id["semanticId"] = "sha256:" + "0" * 64
        mutations.append(
            lambda: coverage.validate_matrix_v1(stale_matrix_id, inventory, contract)
        )
        forged_attestation_status = copy.deepcopy(attestations)
        forged_attestation_status["status"] = "complete"
        forged_attestation_status["semanticId"] = coverage.semantic_id_v1(
            forged_attestation_status
        )
        mutations.append(
            lambda: coverage.validate_completion_attestations_v1(
                forged_attestation_status, inventory, contract
            )
        )
        for mutation in mutations:
            with self.subTest(mutation=mutation), contextlib.redirect_stderr(io.StringIO()):
                with self.assertRaises(SystemExit):
                    mutation()

    def test_v1_rejects_every_nonempty_completion_attestation(self) -> None:
        inventory = coverage.read_inventory()
        contract = coverage.read_contract()
        empty = json.loads(
            coverage.DEFAULT_COMPLETION_ATTESTATIONS.read_text(encoding="utf-8")
        )
        forged_entries = [
            {},
            {
                "attestationId": "sha256:" + "1" * 64,
                "documentId": inventory[0]["id"],
                "governingDocument": inventory[0]["path"],
                "governingDocumentSha256": coverage.sha256_file(
                    coverage.ROOT / inventory[0]["path"]
                ),
                "review": {
                    "reviewedBy": "independent-sol",
                    "reviewedCommit": "a" * 40,
                    "reviewedTree": "b" * 40,
                },
                "state": "complete",
            },
            {"status": "passed", "semanticId": "sha256:" + "2" * 64},
        ]
        for entry in forged_entries:
            candidate = copy.deepcopy(empty)
            candidate["attestations"] = [entry]
            candidate["semanticId"] = coverage.semantic_id_v1(candidate)
            stderr = io.StringIO()
            with self.subTest(entry=entry), contextlib.redirect_stderr(stderr):
                with self.assertRaises(SystemExit):
                    coverage.validate_completion_attestations_v1(
                        candidate, inventory, contract
                    )
            self.assertIn("completion admission is disabled", stderr.getvalue())

    def test_custom_outputs_are_isolated_and_report_links_custom_paths(self) -> None:
        matrix = {
            "generatedOn": "2026-08-07",
            "kind": coverage.MATRIX_KIND,
            "schemaVersion": 1,
            "semanticId": "sha256:" + "1" * 64,
            "status": "incomplete",
            "summary": {
                "documents": 1,
                "fullLanguageCompleteDocuments": 0,
                "documentsWithNoExecutableOwner": 1,
                "documentsWithNoAcceptedFixture": 1,
                "documentsWithNoRejectedFixture": 1,
                "documentsWithNoStableDiagnostic": 1,
                "documentsWithNoGravityAuthoredImplementation": 1,
                "publicAudit": {
                    "enabled": True,
                    "acceptedTotal": 0,
                    "acceptedPass": 0,
                    "acceptedFail": 0,
                    "rejectedTotal": 0,
                    "rejectedSpecificDiagnostic": 0,
                    "rejectedGenericUnsupported": 0,
                },
                "coverageClasses": {"incomplete": 1},
            },
        }
        gaps = {
            "gapCount": 1,
            "noExecutableOwnerCount": 1,
            "semanticId": "sha256:" + "2" * 64,
        }
        matrix_path = Path("target/coverage/custom-matrix.json")
        gaps_path = Path("target/coverage/custom-gaps.json")
        report_path = Path("target/coverage/custom-report.md")
        with tempfile.TemporaryDirectory(prefix="gravity-coverage-output-") as temporary:
            output_root = Path(temporary).resolve() / "isolated"
            with mock.patch.dict(os.environ, {"GRAVITY_OUTPUT_ROOT": str(output_root)}):
                coverage.write_json(matrix_path, matrix)
                coverage.write_json(gaps_path, gaps)
                coverage.write_report(report_path, matrix, gaps, matrix_path, gaps_path)
            self.assertEqual(
                json.loads((output_root / matrix_path).read_text(encoding="utf-8")), matrix
            )
            report = (output_root / report_path).read_text(encoding="utf-8")
            self.assertIn(f"`{matrix_path.as_posix()}`", report)
            self.assertIn(f"`{gaps_path.as_posix()}`", report)
            self.assertIn("Static Public Reachability Audit", report)
            self.assertFalse((ROOT / matrix_path).exists())

    def test_script_and_module_help_describe_static_audit(self) -> None:
        for command in (
            [sys.executable, str(ROOT / "tools/generate_full_language_coverage_matrix.py"), "--help"],
            [sys.executable, "-m", "tools.generate_full_language_coverage_matrix", "--help"],
        ):
            with self.subTest(command=command):
                result = subprocess.run(
                    command,
                    cwd=ROOT,
                    env={**os.environ, "PYTHONDONTWRITEBYTECODE": "1"},
                    capture_output=True,
                    text=True,
                    check=False,
                    timeout=30,
                )
                self.assertEqual(result.returncode, 0, result.stderr)
                self.assertIn("static public reachability audit", result.stdout)

    def test_absolute_output_links_must_remain_repository_logical(self) -> None:
        with self.assertRaisesRegex(ValueError, "outside repository root"):
            coverage.logical_output_path(Path("/outside/matrix.json"))

    def test_document_path_matching_is_checkout_path_neutral(self) -> None:
        inventory = coverage.read_inventory()
        cases = {
            "PKG9": "bootstrap/clojure/fixtures/accepted/namespace-module.gravity",
            "GOV5": "target/phase-18/release/gravity",
        }
        by_id = {entry["id"]: entry for entry in inventory}
        for document_id, relative in cases.items():
            outcomes = []
            for checkout_name in (
                "uniform",
                "gravity-python-semantic-support-compose",
                "private-target-support-worktree",
            ):
                with tempfile.TemporaryDirectory() as directory:
                    root = Path(directory) / checkout_name
                    candidate = root / relative
                    with mock.patch.object(coverage, "ROOT", root):
                        outcomes.append(
                            (
                                coverage.document_matches(
                                    by_id[document_id], candidate
                                ),
                                coverage.matching_paths(
                                    by_id[document_id], [candidate]
                                ),
                            )
                        )
            self.assertEqual(
                [(False, []), (False, []), (False, [])], outcomes, document_id
            )

    def test_document_path_matching_rejects_paths_outside_repository(self) -> None:
        entry = next(
            item for item in coverage.read_inventory() if item["id"] == "GOV5"
        )
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory) / "checkout"
            outside = Path(directory) / "target-support" / "artifact.json"
            with mock.patch.object(coverage, "ROOT", root):
                with self.assertRaises(ValueError):
                    coverage.document_matches(entry, outside)


if __name__ == "__main__":
    unittest.main()
