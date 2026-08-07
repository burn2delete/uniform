from __future__ import annotations

import json
import os
from pathlib import Path
import signal
import subprocess
import sys
import tempfile
import time
import unittest


TOOLS = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(TOOLS))

import run_with_heartbeat as runner  # noqa: E402


class LongRunHeartbeatTests(unittest.TestCase):
    def run_in_temp(self, command: list[str], *options: str) -> tuple[int, bytes, dict]:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            log = root / "run.log"
            status = root / "status.json"
            exit_code = runner.run(
                [
                    "--log",
                    str(log),
                    "--status",
                    str(status),
                    "--heartbeat-seconds",
                    "0.05",
                    "--quiet",
                    *options,
                    "--",
                    *command,
                ]
            )
            return exit_code, log.read_bytes(), json.loads(status.read_text())

    def test_success_persists_output_and_final_status(self) -> None:
        code, output, status = self.run_in_temp(
            [sys.executable, "-c", "print('durable-result')"]
        )
        self.assertEqual(0, code)
        self.assertEqual(b"durable-result\n", output)
        self.assertEqual(runner.SCHEMA, status["schema"])
        self.assertEqual("succeeded", status["state"])
        self.assertEqual(0, status["exit_code"])
        self.assertGreater(status["bytes_written"], 0)
        self.assertIn("finished_at", status)

    def test_failure_preserves_child_exit_code(self) -> None:
        code, output, status = self.run_in_temp(
            [sys.executable, "-c", "import sys; print('bad'); sys.exit(7)"]
        )
        self.assertEqual(7, code)
        self.assertEqual(b"bad\n", output)
        self.assertEqual("failed", status["state"])
        self.assertEqual(7, status["exit_code"])

    def test_timeout_terminates_process_group_and_returns_124(self) -> None:
        code, _, status = self.run_in_temp(
            [sys.executable, "-c", "import time; time.sleep(10)"],
            "--timeout-seconds",
            "0.15",
            "--terminate-grace-seconds",
            "0.05",
        )
        self.assertEqual(124, code)
        self.assertEqual("timed-out", status["state"])
        self.assertTrue(status["timed_out"])
        self.assertLess(status["elapsed_seconds"], 2)

    def test_running_status_is_refreshed_before_completion(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            log = root / "run.log"
            status_path = root / "status.json"
            result: list[int] = []

            thread = runner.threading.Thread(
                target=lambda: result.append(
                    runner.run(
                        [
                            "--log",
                            str(log),
                            "--status",
                            str(status_path),
                            "--heartbeat-seconds",
                            "0.05",
                            "--quiet",
                            "--",
                            sys.executable,
                            "-c",
                            "import time; print('started', flush=True); time.sleep(0.4)",
                        ]
                    )
                )
            )
            thread.start()
            deadline = time.monotonic() + 2
            observed = None
            while time.monotonic() < deadline:
                if status_path.exists():
                    candidate = json.loads(status_path.read_text())
                    if candidate["state"] == "running" and candidate["bytes_written"] > 0:
                        observed = candidate
                        break
                time.sleep(0.02)
            thread.join(timeout=2)
            self.assertIsNotNone(observed)
            self.assertEqual(0, result[0])
            self.assertIsInstance(observed["pid"], int)
            self.assertGreaterEqual(observed["process_count"], 1)

    def test_sigterm_is_forwarded_and_reported_conventionally(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            status_path = root / "status.json"
            wrapper = subprocess.Popen(
                [
                    sys.executable,
                    str(TOOLS / "run_with_heartbeat.py"),
                    "--log",
                    str(root / "run.log"),
                    "--status",
                    str(status_path),
                    "--heartbeat-seconds",
                    "0.05",
                    "--quiet",
                    "--",
                    sys.executable,
                    "-c",
                    "import time; time.sleep(10)",
                ]
            )
            deadline = time.monotonic() + 2
            while time.monotonic() < deadline:
                if status_path.exists():
                    status = json.loads(status_path.read_text())
                    if status["state"] == "running":
                        break
                time.sleep(0.02)
            else:
                wrapper.kill()
                self.fail("wrapper did not reach running state")
            os.kill(wrapper.pid, signal.SIGTERM)
            self.assertEqual(143, wrapper.wait(timeout=2))
            status = json.loads(status_path.read_text())
            self.assertEqual("signaled", status["state"])
            self.assertEqual(signal.SIGTERM, status["received_signal"])
            self.assertEqual(143, status["exit_code"])


if __name__ == "__main__":
    unittest.main()
