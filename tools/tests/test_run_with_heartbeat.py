from __future__ import annotations

import json
import fcntl
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
                    "--metrics-sample-seconds",
                    "0.02",
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
            self.assertGreaterEqual(
                observed["peak_process_count"], observed["process_count"]
            )
            self.assertGreaterEqual(observed["peak_rss_bytes"], observed["rss_bytes"])

    def test_final_status_retains_peak_process_metrics(self) -> None:
        code, _, status = self.run_in_temp(
            [
                sys.executable,
                "-c",
                "import time; payload = bytearray(20_000_000); time.sleep(0.2)",
            ]
        )
        self.assertEqual(0, code)
        self.assertGreater(status["peak_rss_bytes"], 20_000_000)
        self.assertGreaterEqual(status["peak_rss_bytes"], status["rss_bytes"])
        self.assertGreaterEqual(
            status["peak_process_count"], status["process_count"]
        )

    def test_peak_sampling_is_independent_from_status_heartbeat(self) -> None:
        code, _, status = self.run_in_temp(
            [
                sys.executable,
                "-c",
                (
                    "import time; time.sleep(0.05); "
                    "payload = bytearray(120_000_000); "
                    "[(payload.__setitem__(i, 1)) for i in range(0, len(payload), 4096)]; "
                    "time.sleep(0.2); "
                    "del payload; time.sleep(0.05)"
                ),
            ],
            "--heartbeat-seconds",
            "60",
        )
        self.assertEqual(0, code)
        self.assertGreater(status["peak_rss_bytes"], 120_000_000)
        self.assertEqual(0.02, status["metrics_sample_seconds"])

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

    def test_held_cross_process_lock_fails_fast(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            lock_path = root / "heavy.lock"
            with lock_path.open("a+", encoding="utf-8") as lock_stream:
                fcntl.flock(lock_stream.fileno(), fcntl.LOCK_EX | fcntl.LOCK_NB)
                lock_stream.write('{"runner_pid": 42}\n')
                lock_stream.flush()
                started = time.monotonic()
                exit_code = runner.run(
                    [
                        "--log",
                        str(root / "run.log"),
                        "--status",
                        str(root / "status.json"),
                        "--lock",
                        str(lock_path),
                        "--quiet",
                        "--",
                        sys.executable,
                        "-c",
                        "raise SystemExit('must not start')",
                    ]
                )
            status = json.loads((root / "status.json").read_text())
            self.assertEqual(75, exit_code)
            self.assertEqual("lock-unavailable", status["state"])
            self.assertIn("runner_pid", status["lock_owner"])
            self.assertLess(time.monotonic() - started, 1)


if __name__ == "__main__":
    unittest.main()
