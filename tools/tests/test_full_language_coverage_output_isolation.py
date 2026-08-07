#!/usr/bin/env python3
"""Focused output-boundary tests for the full-language coverage generator."""

from __future__ import annotations

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
    def test_custom_outputs_are_isolated_and_report_links_custom_paths(self) -> None:
        matrix = {
            "generatedOn": "2026-08-07",
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
        gaps = {"gapCount": 1, "noExecutableOwnerCount": 1}
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


if __name__ == "__main__":
    unittest.main()
