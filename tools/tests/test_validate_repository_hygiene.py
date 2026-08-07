#!/usr/bin/env python3
"""Focused tests for repository hygiene validation."""

from __future__ import annotations

from contextlib import redirect_stderr, redirect_stdout
import io
from pathlib import Path
import sys
import unittest
from unittest import mock


TOOLS = Path(__file__).resolve().parents[1]
if str(TOOLS) not in sys.path:
    sys.path.insert(0, str(TOOLS))

import validate_repository_hygiene as hygiene


class RepositoryHygieneTests(unittest.TestCase):
    def test_classifies_only_python_interpreter_cache_outputs(self) -> None:
        paths = [
            "src/gravity/__pycache__/reader.cpython-314.pyc",
            "tools/cache/tool.pyo",
            "src/gravity/reader.py",
            "docs/artifacts/reviewed.json",
            "target/validation/compiler.log",
        ]
        self.assertEqual(
            hygiene.tracked_python_cache_paths(paths),
            [
                "src/gravity/__pycache__/reader.cpython-314.pyc",
                "tools/cache/tool.pyo",
            ],
        )

    def test_current_index_has_no_python_cache_outputs(self) -> None:
        self.assertEqual(hygiene.validate_repository(), [])

    def test_cli_reports_violations_and_discovery_errors(self) -> None:
        with mock.patch.object(hygiene, "validate_repository", return_value=["x.pyc"]):
            stderr = io.StringIO()
            with redirect_stderr(stderr):
                self.assertEqual(hygiene.main([]), 1)
            self.assertIn("x.pyc", stderr.getvalue())
        with mock.patch.object(
            hygiene, "validate_repository", side_effect=hygiene.HygieneError("broken index")
        ):
            with redirect_stderr(io.StringIO()):
                self.assertEqual(hygiene.main([]), 2)
        with redirect_stdout(io.StringIO()):
            with mock.patch.object(hygiene, "validate_repository", return_value=[]):
                self.assertEqual(hygiene.main([]), 0)

    def test_git_spawn_failure_is_reported_as_hygiene_error(self) -> None:
        with mock.patch.object(
            hygiene.subprocess, "run", side_effect=FileNotFoundError("git unavailable")
        ):
            with self.assertRaisesRegex(hygiene.HygieneError, "cannot start git"):
                hygiene.git_tracked_paths()
            with redirect_stderr(io.StringIO()):
                self.assertEqual(hygiene.main([]), 2)


if __name__ == "__main__":
    unittest.main()
