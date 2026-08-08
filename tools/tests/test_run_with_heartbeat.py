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
    def private_lock_path(self) -> Path:
        path = Path("/private/tmp") / (
            f"gravity-heartbeat-{os.getpid()}-{time.time_ns()}.lock"
        )
        self.addCleanup(path.unlink, missing_ok=True)
        return path

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
            lock_path = self.private_lock_path()
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
            self.assertIn("runner_pid", status["legacy_untrusted_lock_payload"])
            self.assertNotIn("lock_owner", status)
            self.assertLess(time.monotonic() - started, 1)

    def test_lock_lease_is_no_follow_owned_0600_and_no_write(self) -> None:
        lock_path = self.private_lock_path()
        descriptor = os.open(lock_path, os.O_RDWR | os.O_CREAT | os.O_EXCL, 0o600)
        os.write(descriptor, b"preserve lock payload\n")
        os.close(descriptor)
        before = lock_path.stat()
        exit_code, _, status = self.run_in_temp(
            [sys.executable, "-c", "print('ok')"], "--lock", str(lock_path)
        )
        after = lock_path.stat()
        self.assertEqual(0, exit_code)
        self.assertEqual(b"preserve lock payload\n", lock_path.read_bytes())
        self.assertEqual(0o600, after.st_mode & 0o777)
        self.assertEqual((before.st_dev, before.st_ino), (after.st_dev, after.st_ino))
        self.assertEqual(runner.locks.SHARED_LOCK_PROTOCOL, status["lock_protocol"])
        self.assertFalse(status["durable_telemetry_authoritative"])

    def test_free_legacy_lock_is_migrated_without_replacing_or_writing(self) -> None:
        lock_path = self.private_lock_path()
        lock_path.write_bytes(b"legacy payload\n")
        os.chmod(lock_path, 0o644)
        before = lock_path.stat()
        exit_code, _, status = self.run_in_temp(
            [sys.executable, "-c", "print('ok')"], "--lock", str(lock_path)
        )
        after = lock_path.stat()
        self.assertEqual(0, exit_code)
        self.assertEqual(b"legacy payload\n", lock_path.read_bytes())
        self.assertEqual((before.st_dev, before.st_ino), (after.st_dev, after.st_ino))
        self.assertEqual(0o600, after.st_mode & 0o777)
        self.assertTrue(status["lock_mode_migrated"])

    def test_symlink_lock_is_rejected_without_touching_target(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            victim = Path(directory) / "victim.txt"
            victim.write_text("KEEP\n", encoding="utf-8")
            lock_path = self.private_lock_path()
            lock_path.symlink_to(victim)
            exit_code, _, status = self.run_in_temp(
                [sys.executable, "-c", "raise SystemExit('must not run')"],
                "--lock", str(lock_path),
            )
            self.assertEqual(75, exit_code)
            self.assertEqual("lock-unavailable", status["state"])
            self.assertEqual("KEEP\n", victim.read_text(encoding="utf-8"))

    def test_in_body_lock_replacement_rewrites_success_as_lock_unsafe(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            lock_path = self.private_lock_path()
            descriptor = os.open(lock_path, os.O_RDWR | os.O_CREAT | os.O_EXCL, 0o600)
            os.close(descriptor)
            replacement = lock_path.with_suffix(".replacement")
            self.addCleanup(replacement.unlink, missing_ok=True)
            code = (
                "import os, pathlib; target=pathlib.Path(" + repr(str(lock_path)) + "); "
                "replacement=pathlib.Path(" + repr(str(replacement)) + "); "
                "fd=os.open(replacement, os.O_RDWR|os.O_CREAT|os.O_EXCL, 0o600); "
                "os.close(fd); os.replace(replacement, target); print('ran')"
            )
            status_path = root / "status.json"
            exit_code = runner.run([
                "--log", str(root / "run.log"), "--status", str(status_path),
                "--lock", str(lock_path), "--quiet", "--",
                sys.executable, "-c", code,
            ])
            status = json.loads(status_path.read_text())
            self.assertEqual(75, exit_code)
            self.assertEqual("lock-unsafe", status["state"])
            self.assertFalse(status["proof_authority_granted"])


if __name__ == "__main__":
    unittest.main()
