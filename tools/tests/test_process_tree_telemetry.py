from __future__ import annotations

from pathlib import Path
import subprocess
import sys
import unittest
from unittest import mock


TOOLS = Path(__file__).resolve().parents[1]
if str(TOOLS) not in sys.path:
    sys.path.insert(0, str(TOOLS))

import process_tree_telemetry as telemetry  # noqa: E402


class ProcessTreeTelemetryTests(unittest.TestCase):
    def test_aggregates_only_the_requested_process_tree(self) -> None:
        table = "10 1 100 1.0\n11 10 200 2.5\n12 11 300 3.0\n20 1 999 9.0\n"
        completed = subprocess.CompletedProcess([], 0, stdout=table, stderr="")
        with mock.patch.object(telemetry.subprocess, "run", return_value=completed) as run:
            result = telemetry.process_tree_metrics(10)
        self.assertEqual(3, result["process_count"])
        self.assertEqual(600 * 1024, result["rss_bytes"])
        self.assertEqual(6.5, result["cpu_percent"])
        self.assertTrue(result["telemetry_available"])
        self.assertIsNone(result["telemetry_error"])
        self.assertEqual(["ps", "-axo", "pid=,ppid=,rss=,%cpu="], run.call_args.args[0])
        self.assertEqual(0.5, run.call_args.kwargs["timeout"])

    def test_failure_and_absent_root_are_explicit_not_zero_measurements(self) -> None:
        with mock.patch.object(telemetry.subprocess, "run", side_effect=OSError("no ps")):
            failed = telemetry.process_tree_metrics(10)
        self.assertFalse(failed["telemetry_available"])
        self.assertIsNone(failed["rss_bytes"])
        self.assertIn("no ps", failed["telemetry_error"])
        completed = subprocess.CompletedProcess([], 0, stdout="20 1 5 0.0\n", stderr="")
        with mock.patch.object(telemetry.subprocess, "run", return_value=completed):
            absent = telemetry.process_tree_metrics(10)
        self.assertFalse(absent["telemetry_available"])
        self.assertIn("not present", absent["telemetry_error"])

    def test_ps_timeout_is_explicitly_unavailable(self) -> None:
        error = subprocess.TimeoutExpired(["ps"], 0.5)
        with mock.patch.object(telemetry.subprocess, "run", side_effect=error):
            result = telemetry.process_tree_metrics(10)
        self.assertFalse(result["telemetry_available"])
        self.assertIsNone(result["process_count"])
        self.assertIn("TimeoutExpired", result["telemetry_error"])


if __name__ == "__main__":
    unittest.main()
