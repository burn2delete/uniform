#!/usr/bin/env python3
"""Integration checks for validator output isolation and atomic publication."""

from __future__ import annotations

import json
import importlib
import os
from pathlib import Path
import re
import subprocess
import sys
import tempfile
import unittest


ROOT = Path(__file__).resolve().parents[2]
TOOLS = ROOT / "tools"


class ValidatorOutputIsolationTests(unittest.TestCase):
    def test_all_json_validator_outputs_use_shared_publisher(self) -> None:
        migrated_files = 0
        migrated_calls = 0
        direct_pattern = re.compile(
            r"args\.(?:artifact_out|coverage_out)\.(?:write_text|parent\.mkdir)"
        )
        call_pattern = re.compile(
            r"atomic_write_json\(args\.(?:artifact_out|coverage_out),"
        )
        for path in sorted(TOOLS.glob("validate_*.py")):
            text = path.read_text(encoding="utf-8")
            self.assertIsNone(direct_pattern.search(text), path.name)
            calls = len(call_pattern.findall(text))
            if calls:
                migrated_files += 1
                migrated_calls += calls
                self.assertIn(
                    "from .output_publication import atomic_write_json", text, path.name
                )
                self.assertIn(
                    "from output_publication import atomic_write_json", text, path.name
                )
        self.assertEqual(migrated_files, 59)
        self.assertEqual(migrated_calls, 66)

    def test_validators_remain_package_importable_and_module_runnable(self) -> None:
        validator_modules = [
            f"tools.{path.stem}"
            for path in sorted(TOOLS.glob("validate_*.py"))
            if "atomic_write_json(args." in path.read_text(encoding="utf-8")
        ]
        self.assertEqual(len(validator_modules), 59)
        for module_name in validator_modules:
            with self.subTest(module=module_name):
                importlib.import_module(module_name)
        result = subprocess.run(
            [sys.executable, "-m", "tools.validate_math_system", "--help"],
            cwd=ROOT,
            env={**os.environ, "PYTHONDONTWRITEBYTECODE": "1"},
            capture_output=True,
            text=True,
            check=False,
            timeout=30,
        )
        self.assertEqual(result.returncode, 0, result.stderr)

    def test_representative_validators_publish_only_under_isolated_root(self) -> None:
        with tempfile.TemporaryDirectory(prefix="gravity-validator-isolation-") as temp:
            temporary = Path(temp).resolve()
            run_root = temporary / "run-output"
            cwd = temporary / "cwd"
            cwd.mkdir()
            environment = os.environ.copy()
            environment.update(
                {
                    "GRAVITY_OUTPUT_ROOT": str(run_root),
                    "PYTHONDONTWRITEBYTECODE": "1",
                }
            )
            cases = [
                (
                    "validate_math_system.py",
                    ["--artifact-out", "target/validation/isolation/math.json"],
                    ["target/validation/isolation/math.json"],
                ),
                (
                    "validate_phase05_document_coverage.py",
                    [
                        "--artifact-out",
                        str(ROOT / "docs/artifacts/isolation/phase05.json"),
                    ],
                    ["docs/artifacts/isolation/phase05.json"],
                ),
                (
                    "validate_providers.py",
                    [
                        "--artifact-out",
                        "target/validation/isolation/providers.json",
                        "--coverage-out",
                        "docs/artifacts/isolation/providers-coverage.json",
                    ],
                    [
                        "target/validation/isolation/providers.json",
                        "docs/artifacts/isolation/providers-coverage.json",
                    ],
                ),
            ]
            for script, arguments, expected in cases:
                with self.subTest(script=script):
                    result = subprocess.run(
                        [sys.executable, str(TOOLS / script), *arguments],
                        cwd=cwd,
                        env=environment,
                        capture_output=True,
                        text=True,
                        check=False,
                        timeout=30,
                    )
                    self.assertEqual(result.returncode, 0, result.stderr)
                    for relative in expected:
                        output = run_root / relative
                        self.assertTrue(output.is_file(), output)
                        self.assertIsInstance(
                            json.loads(output.read_text(encoding="utf-8")), dict
                        )
                        self.assertFalse((ROOT / relative).exists(), relative)
                        self.assertFalse((cwd / relative).exists(), relative)


if __name__ == "__main__":
    unittest.main()
