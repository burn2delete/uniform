from __future__ import annotations

import fcntl
import json
import os
from pathlib import Path
import shutil
import sys
import tempfile
import time
import unittest


TOOLS = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(TOOLS))

import run_sh07_authoritative_modules as runner  # noqa: E402


def fixture_output_validator(module: str, path: Path) -> bool:
    output = path.read_text(encoding="utf-8")
    return (
        output.startswith(
            "{:artifact :gravity/sh07-authoritative-proof-run "
            ":schema-version 1 :status :passed "
        )
        and f':module "{module}"' in output
        and ":verification-status :passed" in output
        and ":capability-proof-status :complete" in output
        and ":failed-checks []" in output
    )


class FakeLauncher:
    def __init__(self, outcomes: dict[str, runner.ProcessOutcome] | None = None) -> None:
        self.outcomes = outcomes or {}
        self.calls: list[str] = []

    def __call__(
        self,
        command: list[str],
        _cwd: Path,
        stdout_path: Path,
        stderr_path: Path,
        _timeout_seconds: float,
    ) -> runner.ProcessOutcome:
        module = command[-1]
        self.calls.append(module)
        outcome = self.outcomes.get(module, runner.ProcessOutcome(0, False, 0.01))
        if outcome.exit_code == 0 and not outcome.timed_out:
            stdout_path.write_text(
                "{:artifact :gravity/sh07-authoritative-proof-run "
                ":schema-version 1 :status :passed "
                ":fresh-process-required? true "
                ":persistent-iteration-cache-used? false "
                f':modules [{{:module "{module}" :status :accepted '
                ":verification-status :passed "
                ":capability-proof-status :complete :failed-checks [] "
                ":contract-checks {:ok true}}]}\n",
                encoding="utf-8",
            )
        else:
            stdout_path.write_text(
                f'{{:status :failed :modules [{{:module "{module}"}}]}}\n',
                encoding="utf-8",
            )
        stderr_path.write_text(
            "timed out\n" if outcome.timed_out else "failure\n" if outcome.exit_code else "",
            encoding="utf-8",
        )
        return outcome


class Sh07CheckpointTests(unittest.TestCase):
    def make_repository(self, root: Path) -> None:
        files = {
            "deps.edn": "{:paths []}\n",
            "bootstrap/clojure/src/gravity/bootstrap.clj": "(ns gravity.bootstrap)\n",
            "bootstrap/gravity/p15_s23/compiler.gravity": "(ns gravity.p15-s23.compiler)\n",
            "bootstrap/gravity/p15_s23/emitter.gravity": "(ns gravity.p15-s23.emitter)\n",
            "bootstrap/gravity/src/gravity/checked_core.gravity": "(ns gravity.checked-core)\n",
            "bootstrap/clojure/test/gravity/self_hosting/sh07_proof_contract.edn": "{:schema :test}\n",
            "bootstrap/clojure/test/gravity/self_hosting/sh07_authoritative_runner.clj": "(ns test.runner)\n",
        }
        for relative, content in files.items():
            path = root / relative
            path.parent.mkdir(parents=True, exist_ok=True)
            path.write_text(content, encoding="utf-8")

    def run_in_repository(
        self,
        root: Path,
        launcher: FakeLauncher,
        modules: list[str],
        *,
        resume: bool = True,
    ) -> tuple[int, dict[str, object]]:
        return runner.run_modules(
            root=root,
            state_dir=root / "checkpoints",
            modules=modules,
            base_command=["fake-runner"],
            timeout_seconds=1,
            resume=resume,
            launcher=launcher,
            output_validator=fixture_output_validator,
            lock_path=root / "heavy.lock",
        )

    def test_identical_context_resumes_only_verified_passed_modules(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            self.make_repository(root)
            launcher = FakeLauncher()
            code, first = self.run_in_repository(root, launcher, ["alpha", "beta"])
            self.assertEqual(0, code)
            self.assertEqual(["alpha", "beta"], launcher.calls)
            self.assertEqual("completed", first["state"])

            code, second = self.run_in_repository(root, launcher, ["alpha", "beta"])
            self.assertEqual(0, code)
            self.assertEqual(["alpha", "beta"], launcher.calls)
            self.assertEqual("completed", second["state"])
            self.assertFalse(second["aggregate_authoritative"])
            manifest = json.loads((root / "checkpoints/manifest.json").read_text())
            self.assertEqual(runner.SCHEMA, manifest["schema"])
            self.assertEqual("passed", manifest["modules"]["alpha"]["state"])
            self.assertTrue((root / "checkpoints/modules/alpha.stdout.log").is_file())
            self.assertFalse(list((root / "checkpoints").glob(".*.tmp")))

    def test_source_change_invalidates_every_checkpoint(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            self.make_repository(root)
            launcher = FakeLauncher()
            _, first = self.run_in_repository(root, launcher, ["alpha", "beta"])
            previous = first["context_fingerprint"]
            source = root / "bootstrap/gravity/src/gravity/checked_core.gravity"
            source.write_text("(ns gravity.checked-core)\n;; changed\n", encoding="utf-8")

            code, second = self.run_in_repository(root, launcher, ["alpha", "beta"])
            self.assertEqual(0, code)
            self.assertEqual(["alpha", "beta", "alpha", "beta"], launcher.calls)
            self.assertNotEqual(previous, second["context_fingerprint"])
            self.assertEqual(previous, second["invalidated_context_fingerprint"])

    def test_stage2_plan_source_changes_invalidate_every_checkpoint(self) -> None:
        for name in ["compiler.gravity", "emitter.gravity"]:
            with self.subTest(name=name), tempfile.TemporaryDirectory() as directory:
                root = Path(directory)
                self.make_repository(root)
                launcher = FakeLauncher()
                _, first = self.run_in_repository(root, launcher, ["alpha", "beta"])
                previous = first["context_fingerprint"]
                source = root / "bootstrap/gravity/p15_s23" / name
                source.write_text(
                    source.read_text(encoding="utf-8") + "; changed\n",
                    encoding="utf-8",
                )

                code, second = self.run_in_repository(
                    root, launcher, ["alpha", "beta"]
                )
                self.assertEqual(0, code)
                self.assertEqual(
                    ["alpha", "beta", "alpha", "beta"], launcher.calls
                )
                self.assertNotEqual(previous, second["context_fingerprint"])
                self.assertEqual(
                    previous, second["invalidated_context_fingerprint"]
                )

    def test_source_change_during_child_run_stops_the_sequence(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            self.make_repository(root)
            delegate = FakeLauncher()

            def mutating_launcher(
                command: list[str],
                cwd: Path,
                stdout_path: Path,
                stderr_path: Path,
                timeout_seconds: float,
            ) -> runner.ProcessOutcome:
                outcome = delegate(
                    command, cwd, stdout_path, stderr_path, timeout_seconds
                )
                source = root / "bootstrap/gravity/src/gravity/checked_core.gravity"
                source.write_text(
                    source.read_text(encoding="utf-8") + "; changed during run\n",
                    encoding="utf-8",
                )
                return outcome

            code, manifest = runner.run_modules(
                root=root,
                state_dir=root / "checkpoints",
                modules=["alpha", "beta"],
                base_command=["fake-runner"],
                timeout_seconds=1,
                launcher=mutating_launcher,
                output_validator=fixture_output_validator,
                lock_path=root / "heavy.lock",
            )
            self.assertEqual(75, code)
            self.assertEqual(["alpha"], delegate.calls)
            self.assertEqual("context-changed", manifest["state"])
            self.assertFalse(manifest["modules"]["alpha"]["context_stable"])

    def test_tampered_output_invalidates_only_that_module_receipt(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            self.make_repository(root)
            launcher = FakeLauncher()
            self.run_in_repository(root, launcher, ["alpha", "beta"])
            (root / "checkpoints/modules/alpha.stdout.log").write_text(
                "tampered\n", encoding="utf-8"
            )

            code, manifest = self.run_in_repository(root, launcher, ["alpha", "beta"])
            self.assertEqual(0, code)
            self.assertEqual(["alpha", "beta", "alpha"], launcher.calls)
            self.assertEqual("completed", manifest["state"])

    def test_coherently_tampered_manifest_and_output_cannot_resume(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            self.make_repository(root)
            launcher = FakeLauncher()
            self.run_in_repository(root, launcher, ["alpha"])
            stdout = root / "checkpoints/modules/alpha.stdout.log"
            stdout.write_text("not an EDN proof\n", encoding="utf-8")
            manifest_path = root / "checkpoints/manifest.json"
            manifest = json.loads(manifest_path.read_text(encoding="utf-8"))
            manifest["modules"]["alpha"]["stdout_sha256"] = runner.sha256_file(stdout)
            manifest_path.write_text(json.dumps(manifest), encoding="utf-8")

            code, _ = self.run_in_repository(root, launcher, ["alpha"])
            self.assertEqual(0, code)
            self.assertEqual(["alpha", "alpha"], launcher.calls)

    def test_manifest_log_path_cannot_escape_checkpoint_directory(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            self.make_repository(root)
            launcher = FakeLauncher()
            self.run_in_repository(root, launcher, ["alpha"])
            outside = root / "outside.log"
            outside.write_text("external\n", encoding="utf-8")
            manifest_path = root / "checkpoints/manifest.json"
            manifest = json.loads(manifest_path.read_text(encoding="utf-8"))
            entry = manifest["modules"]["alpha"]
            entry["stdout_path"] = "../../outside.log"
            entry["stdout_sha256"] = runner.sha256_file(outside)
            manifest_path.write_text(json.dumps(manifest), encoding="utf-8")

            code, _ = self.run_in_repository(root, launcher, ["alpha"])
            self.assertEqual(0, code)
            self.assertEqual(["alpha", "alpha"], launcher.calls)

    def test_failure_is_durable_and_stops_before_later_modules(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            self.make_repository(root)
            launcher = FakeLauncher(
                {"beta": runner.ProcessOutcome(7, False, 0.02)}
            )
            code, manifest = self.run_in_repository(
                root, launcher, ["alpha", "beta", "gamma"]
            )
            self.assertEqual(7, code)
            self.assertEqual(["alpha", "beta"], launcher.calls)
            self.assertEqual("failed", manifest["state"])
            self.assertEqual("beta", manifest["stopped_at_module"])
            persisted = json.loads((root / "checkpoints/manifest.json").read_text())
            self.assertEqual(7, persisted["modules"]["beta"]["exit_code"])
            self.assertNotIn("gamma", persisted["modules"])
            self.assertEqual(
                "failure\n",
                (
                    root
                    / "checkpoints"
                    / persisted["modules"]["beta"]["stderr_path"]
                ).read_text(),
            )

    def test_timeout_returns_124_and_stops(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            self.make_repository(root)
            launcher = FakeLauncher(
                {"alpha": runner.ProcessOutcome(124, True, 1.0)}
            )
            code, manifest = self.run_in_repository(root, launcher, ["alpha", "beta"])
            self.assertEqual(124, code)
            self.assertEqual(["alpha"], launcher.calls)
            self.assertEqual("timed-out", manifest["state"])
            self.assertEqual("timed-out", manifest["modules"]["alpha"]["state"])
            self.assertTrue(manifest["modules"]["alpha"]["timed_out"])

    def test_signal_exit_is_normalized_and_raw_code_is_preserved(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            self.make_repository(root)
            launcher = FakeLauncher({"alpha": runner.ProcessOutcome(-9, False, 0.1)})
            code, manifest = self.run_in_repository(root, launcher, ["alpha"])
            self.assertEqual(137, code)
            self.assertEqual(137, manifest["modules"]["alpha"]["exit_code"])
            self.assertEqual(-9, manifest["modules"]["alpha"]["raw_child_exit_code"])

    def test_exit_zero_without_runner_pass_contract_fails_closed(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            self.make_repository(root)

            def incomplete_output(
                command: list[str],
                _cwd: Path,
                stdout_path: Path,
                stderr_path: Path,
                _timeout_seconds: float,
            ) -> runner.ProcessOutcome:
                stdout_path.write_text("{:status :passed}\n", encoding="utf-8")
                stderr_path.write_text("", encoding="utf-8")
                return runner.ProcessOutcome(0, False, 0.01)

            code, manifest = runner.run_modules(
                root=root,
                state_dir=root / "checkpoints",
                modules=["alpha"],
                base_command=["fake-runner"],
                timeout_seconds=1,
                launcher=incomplete_output,
                output_validator=fixture_output_validator,
                lock_path=root / "heavy.lock",
            )
            self.assertEqual(1, code)
            self.assertEqual("failed", manifest["state"])
            self.assertFalse(manifest["modules"]["alpha"]["output_contract_checked"])

    def test_nested_pass_marker_cannot_override_failed_top_level_status(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            self.make_repository(root)

            def misleading_output(
                command: list[str],
                _cwd: Path,
                stdout_path: Path,
                stderr_path: Path,
                _timeout_seconds: float,
            ) -> runner.ProcessOutcome:
                module = command[-1]
                stdout_path.write_text(
                    "{:artifact :gravity/sh07-authoritative-proof-run "
                    ":schema-version 1 :status :failed "
                    ":fresh-process-required? true "
                    ":persistent-iteration-cache-used? false "
                    f':modules [{{:module "{module}" :status :passed}}]}}\n',
                    encoding="utf-8",
                )
                stderr_path.write_text("", encoding="utf-8")
                return runner.ProcessOutcome(0, False, 0.01)

            code, manifest = runner.run_modules(
                root=root,
                state_dir=root / "checkpoints",
                modules=["alpha"],
                base_command=["fake-runner"],
                timeout_seconds=1,
                launcher=misleading_output,
                output_validator=fixture_output_validator,
                lock_path=root / "heavy.lock",
            )
            self.assertEqual(1, code)
            self.assertEqual("failed", manifest["state"])
            self.assertFalse(manifest["modules"]["alpha"]["output_contract_checked"])

    @unittest.skipUnless(shutil.which("clojure"), "clojure CLI is required")
    def test_production_validator_parses_one_exact_edn_result(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            self.make_repository(root)
            output = root / "result.edn"
            output.write_text(
                "{:artifact :gravity/sh07-authoritative-proof-run "
                ":schema-version 1 :status :passed "
                ":fresh-process-required? true "
                ":persistent-iteration-cache-used? false "
                ":modules [{:module \"alpha\" :status :accepted "
                ":verification-status :passed "
                ":capability-proof-status :complete :failed-checks [] "
                ":contract-checks {:exact? true}}]}\n",
                encoding="utf-8",
            )
            self.assertTrue(
                runner.output_contract_passed(
                    "alpha", output, clojure_command="clojure", cwd=root
                )
            )
            output.write_text(
                output.read_text(encoding="utf-8").replace(
                    ":status :passed", ":status :failed", 1
                ),
                encoding="utf-8",
            )
            self.assertFalse(
                runner.output_contract_passed(
                    "alpha", output, clojure_command="clojure", cwd=root
                )
            )

    def test_module_names_cannot_escape_the_log_directory(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            self.make_repository(root)
            with self.assertRaisesRegex(runner.CheckpointError, "safe slugs"):
                self.run_in_repository(root, FakeLauncher(), ["../escape"])

    def test_held_shared_lock_fails_before_launching_a_module(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            self.make_repository(root)
            lock_path = root / "heavy.lock"
            launcher = FakeLauncher()
            with lock_path.open("a+", encoding="utf-8") as held:
                fcntl.flock(held.fileno(), fcntl.LOCK_EX | fcntl.LOCK_NB)
                with self.assertRaisesRegex(runner.CheckpointError, "unavailable"):
                    self.run_in_repository(root, launcher, ["alpha"])
            self.assertEqual([], launcher.calls)

    def test_symlinked_lock_is_rejected_without_touching_target(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            self.make_repository(root)
            victim = root / "victim.txt"
            victim.write_text("KEEP-ME\n", encoding="utf-8")
            (root / "heavy.lock").symlink_to(victim)
            with self.assertRaisesRegex(runner.CheckpointError, "safely"):
                self.run_in_repository(root, FakeLauncher(), ["alpha"])
            self.assertEqual("KEEP-ME\n", victim.read_text(encoding="utf-8"))

    def test_symlinked_module_output_is_rejected_without_touching_target(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            victim = root / "victim.txt"
            victim.write_text("KEEP-ME\n", encoding="utf-8")
            stdout = root / "alpha.stdout.log"
            stdout.symlink_to(victim)
            with self.assertRaisesRegex(runner.CheckpointError, "safely"):
                runner.default_launcher(
                    [sys.executable, "-c", "print('proof')"],
                    root,
                    stdout,
                    root / "alpha.stderr.log",
                    1,
                )
            self.assertEqual("KEEP-ME\n", victim.read_text(encoding="utf-8"))

    def test_hardlinked_module_output_is_rejected_before_truncation(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            victim = root / "victim.txt"
            victim.write_text("KEEP-ME\n", encoding="utf-8")
            stdout = root / "alpha.stdout.log"
            os.link(victim, stdout)
            with self.assertRaisesRegex(runner.CheckpointError, "one owned regular file"):
                runner.default_launcher(
                    [sys.executable, "-c", "print('proof')"],
                    root,
                    stdout,
                    root / "alpha.stderr.log",
                    1,
                )
            self.assertEqual("KEEP-ME\n", victim.read_text(encoding="utf-8"))

    def test_symlinked_modules_directory_is_rejected(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            self.make_repository(root)
            state = root / "checkpoints"
            state.mkdir()
            outside = root / "outside"
            outside.mkdir()
            (state / "modules").symlink_to(outside, target_is_directory=True)
            with self.assertRaisesRegex(runner.CheckpointError, "cannot be a symlink"):
                self.run_in_repository(root, FakeLauncher(), ["alpha"])

    def test_real_timeout_kills_descendants_before_returning(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            child_pid_path = root / "child.pid"
            child_code = (
                "import os,signal,time,pathlib;"
                "signal.signal(signal.SIGTERM,signal.SIG_IGN);"
                f"pathlib.Path({str(child_pid_path)!r}).write_text(str(os.getpid()));"
                "time.sleep(30)"
            )
            leader_code = (
                "import subprocess,sys,time;"
                f"subprocess.Popen([sys.executable,'-c',{child_code!r}]);"
                "time.sleep(30)"
            )
            outcome = runner.default_launcher(
                [sys.executable, "-c", leader_code],
                root,
                root / "stdout.log",
                root / "stderr.log",
                0.5,
            )
            self.assertTrue(outcome.timed_out)
            self.assertEqual(124, outcome.exit_code)
            child_pid = int(child_pid_path.read_text(encoding="utf-8"))
            alive = True
            for _ in range(40):
                try:
                    os.kill(child_pid, 0)
                except ProcessLookupError:
                    alive = False
                    break
                time.sleep(0.05)
            if alive:
                os.kill(child_pid, 9)
            self.assertFalse(alive, "timed-out descendant survived launcher return")


if __name__ == "__main__":
    unittest.main()
