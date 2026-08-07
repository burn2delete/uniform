#!/usr/bin/env python3
"""Unit tests for the development-only performance baseline helper."""

from __future__ import annotations

import hashlib
import io
import json
from contextlib import redirect_stderr, redirect_stdout
import math
from pathlib import Path
import subprocess
import sys
import tempfile
import unittest


TOOLS = Path(__file__).resolve().parents[1]
if str(TOOLS) not in sys.path:
    sys.path.insert(0, str(TOOLS))

import measure_development_baseline as baseline


PYTHON = str(Path(sys.executable).resolve())


def command(source: str) -> list[str]:
    return [PYTHON, "-c", source]


class DevelopmentBaselineTests(unittest.TestCase):
    def test_success_records_status_digests_and_peak_rss_field(self) -> None:
        result = baseline.measure_baseline(
            [command("import sys; sys.stdout.write('ok\\n'); sys.stderr.write('warn\\n')")],
            samples=2,
            timeout_seconds=2,
        )

        self.assertEqual(result["schema"], baseline.SCHEMA)
        self.assertEqual(result["authority"], "non-authoritative")
        self.assertFalse(result["authoritative"])
        self.assertEqual(result["summary"]["status"], "passed")
        self.assertEqual(len(result["measurements"]), 2)
        first = result["measurements"][0]
        self.assertEqual(first["exit_status"], 0)
        self.assertFalse(first["timed_out"])
        self.assertEqual(first["stdout_sha256"], hashlib.sha256(b"ok\n").hexdigest())
        self.assertEqual(first["stderr_sha256"], hashlib.sha256(b"warn\n").hexdigest())
        self.assertEqual(first["stdout"]["bytes"], 3)
        self.assertEqual(first["stdout"]["captured_bytes"], 3)
        self.assertFalse(first["stdout"]["truncated"])
        self.assertTrue(first["capture_complete"])
        self.assertIn("peak_rss_bytes", first)
        self.assertIn("wall_time_seconds", first)

    def test_failure_is_recorded_and_main_returns_nonzero(self) -> None:
        result = baseline.measure_baseline([command("import sys; sys.stderr.write('bad'); sys.exit(7)")])
        measurement = result["measurements"][0]
        self.assertEqual(result["summary"]["status"], "failed")
        self.assertEqual(measurement["exit_status"], 7)
        self.assertEqual(measurement["stderr_sha256"], hashlib.sha256(b"bad").hexdigest())

    def test_output_limit_streams_full_digest_without_unbounded_capture(self) -> None:
        source = "import sys; sys.stdout.write('x' * 100); sys.stderr.write('y' * 70)"
        result = baseline.measure_baseline([command(source)], max_output_bytes=16)
        measurement = result["measurements"][0]

        self.assertEqual(measurement["stdout"]["bytes"], 100)
        self.assertEqual(measurement["stdout"]["captured_bytes"], 16)
        self.assertTrue(measurement["stdout"]["truncated"])
        self.assertEqual(measurement["stderr"]["bytes"], 70)
        self.assertEqual(measurement["stderr"]["captured_bytes"], 16)
        self.assertTrue(measurement["stderr"]["truncated"])
        self.assertTrue(measurement["output_truncated"])
        self.assertEqual(measurement["stdout_sha256"], hashlib.sha256(b"x" * 100).hexdigest())
        self.assertEqual(measurement["stderr_sha256"], hashlib.sha256(b"y" * 70).hexdigest())
        self.assertEqual(result["summary"]["output_limit_bytes"], 16)
        self.assertTrue(result["summary"]["output_limit_reached"])
        self.assertEqual(result["summary"]["truncated_measurement_count"], 1)

        output = io.StringIO()
        with redirect_stdout(output):
            code = baseline.main(["--command", PYTHON, "-c", "import sys; sys.exit(7)"])
        self.assertEqual(code, 1)
        receipt = json.loads(output.getvalue())
        self.assertEqual(receipt["measurements"][0]["exit_status"], 7)

    def test_timeout_cleans_process_group_and_records_timeout(self) -> None:
        result = baseline.measure_baseline(
            [command("import time; time.sleep(5)")],
            timeout_seconds=0.05,
            terminate_grace_seconds=0.05,
        )
        measurement = result["measurements"][0]
        self.assertEqual(result["summary"]["status"], "failed")
        self.assertTrue(measurement["timed_out"])
        self.assertLess(measurement["wall_time_seconds"], 2.0)
        self.assertTrue(measurement["cleanup"]["attempted"])
        self.assertIsNotNone(measurement["exit_status"])

    def test_identity_and_schema_are_deterministic(self) -> None:
        plan = [command("print('stable')")]
        first = baseline.measure_baseline(plan, samples=1, timeout_seconds=2)
        second = baseline.measure_baseline(plan, samples=1, timeout_seconds=2)

        self.assertEqual(first["identity"], second["identity"])
        self.assertEqual(first["identity_sha256"], second["identity_sha256"])
        self.assertEqual(
            first["identity_sha256"],
            hashlib.sha256(
                json.dumps(first["identity"], ensure_ascii=True, sort_keys=True, separators=(",", ":")).encode(
                    "utf-8"
                )
            ).hexdigest(),
        )
        self.assertEqual(first["schema_version"], 1)
        self.assertFalse(first["evidence"]["d6_benchmark_evidence"])
        self.assertFalse(first["evidence"]["release_evidence"])
        self.assertIn("D6 benchmark evidence", first["evidence"]["forbidden_interpretations"])

    def test_dry_run_does_not_start_command_and_can_write_output(self) -> None:
        with tempfile.TemporaryDirectory(prefix="gravity-baseline-test-") as directory:
            output_path = Path(directory) / "receipt.json"
            result = baseline.measure_baseline(
                [command("raise SystemExit('must not execute')")],
                dry_run=True,
            )
            self.assertEqual(result["summary"]["status"], "planned")
            self.assertEqual(result["measurements"], [])
            self.assertTrue(result["plan"]["dry_run"])
            self.assertEqual(result["plan"]["command_count"], 1)

            code = baseline.main(
                [
                    "--dry-run",
                    "--output",
                    str(output_path),
                    "--command",
                    PYTHON,
                    "-c",
                    "raise SystemExit('must not execute')",
                ]
            )
            self.assertEqual(code, 0)
            self.assertTrue(output_path.exists())
            written = json.loads(output_path.read_text(encoding="utf-8"))
            self.assertEqual(written["measurements"], [])
            self.assertEqual(written["authority"], "non-authoritative")

    def test_default_plan_is_python_only_and_does_not_contain_gravity_gate(self) -> None:
        result = baseline.measure_baseline(dry_run=True)
        self.assertTrue(result["plan"]["default_plan"])
        argv = result["plan"]["commands"][0]["argv"]
        self.assertEqual(argv[0], PYTHON)
        self.assertNotIn("clojure", " ".join(argv).lower())
        self.assertEqual(result["plan"]["default_plan_heavy_gates"], "forbidden")

    def test_non_finite_timeout_and_grace_are_rejected_by_api_and_cli(self) -> None:
        plan = [command("pass")]
        for value in (math.nan, math.inf, -math.inf):
            with self.subTest(timeout=value), self.assertRaises(baseline.BaselineError):
                baseline.measure_baseline(plan, timeout_seconds=value)
            with self.subTest(grace=value), self.assertRaises(baseline.BaselineError):
                baseline.measure_baseline(plan, terminate_grace_seconds=value)

        stderr = io.StringIO()
        with redirect_stdout(io.StringIO()):
            with redirect_stderr(stderr):
                code = baseline.main(["--dry-run", "--timeout", "nan"])
        self.assertEqual(code, 2)
        self.assertIn("timeout_seconds must be finite", stderr.getvalue())


if __name__ == "__main__":
    unittest.main()
