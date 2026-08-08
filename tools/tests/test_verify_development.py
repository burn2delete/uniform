#!/usr/bin/env python3
"""Focused tests for the Stage 0 development verification orchestrator."""

from __future__ import annotations

import json
import os
import io
import re
import contextlib
import hashlib
import shutil
from contextlib import redirect_stdout
from pathlib import Path
import subprocess
import sys
import tempfile
import time
import unittest
from unittest import mock

TOOLS = Path(__file__).resolve().parents[1]
ROOT = TOOLS.parent
if str(TOOLS) not in sys.path:
    sys.path.insert(0, str(TOOLS))

import verify_development as verifier


C4_C18_COMPATIBILITY_FORMS = {
    "c4_test.clj": {
        "c4-macro-evidence-compatibility-wrappers-preserve-output-and-interposition": "705f4268b8b265bd7e79cf1288a7ea4a2074047ca3d376f764b8722a641a3c70",
        "macro-expansion-compatibility-wrappers-preserve-output-and-interposition": "737de10877b86d9248384d4d8a8d5ca99897b633bdb91bf8a3f6db7a7fc47f78",
    },
    "c5_test.clj": {"c5-resolution-compatibility-wrappers-preserve-arglists-and-interposition": "73eeff6cf219d27b5de6a7b80bd2dbd0d2fcbade5f1ede24104f769d8b28042e"},
    "c6_test.clj": {"c6-lowering-compatibility-wrappers-preserve-arglists-and-interposition": "3b3861dc9fe27fc42abe0adf0b03d591ad06aba5780437518dd00709247ad4ac"},
    "c7_test.clj": {"c7-type-checker-compatibility-wrappers-preserve-interposition": "0b78a41ab70ea329ee27d8f860a5a89d997c5cf2de4eca33f865058c263fcc19"},
    "c8_test.clj": {"c8-effect-checker-compatibility-wrappers-preserve-interposition": "5a55d1772f66e6833e09fc0552b1bcde9d7dc0427d99b1f103cb150fb69f4b21"},
    "c9_test.clj": {"c9-ownership-checker-compatibility-wrappers-preserve-interposition": "0b71ec75b13ca7599c6338f984759a3617fbb6537c644be0e075d03f08b8af48"},
    "c10_test.clj": {"c10-safety-analysis-compatibility-wrappers-preserve-interposition": "119dfcc7e85daf3e05b6343944c894c1148c7574358a7cbf08f957fe58d86125"},
    "c11_test.clj": {"c11-mir-compatibility-wrappers-preserve-interposition": "850a30974d63d36c5353983094f79089aae7aab15aa07cfe3c207cb3927e81b6"},
    "c12_test.clj": {"c12-domain-ir-compatibility-wrappers-preserve-interposition": "66c368ea6860a2284abee4e87212aed078c365a3d251fb6b1b0d7e0652cb6ed9"},
    "c13_test.clj": {"c13-optimization-compatibility-wrappers-preserve-interposition": "806c1edd066063a8c7ec53dddebd909e768d34c59000b8371286626b27004607"},
    "c14_test.clj": {"c14-lowering-compatibility-wrappers-preserve-interposition": "e3080d947809166821578aa838ec8f5145b00d7dcd08d0c300c1cbd56971eafb"},
    "c15_test.clj": {"c15-diagnostics-compatibility-wrappers-preserve-interposition": "ba4e0d297f3a8b479693b85bce817be5ff5f6e2193f507d9a1d7c1009397f044"},
    "c16_test.clj": {"c16-incremental-compatibility-wrappers-preserve-interposition": "7f55bf46604118570a812826c57d776f978cf2a17bd66865e1347d782f77b327"},
    "c17_test.clj": {"c17-plugin-compatibility-wrappers-preserve-interposition": "693d0a4d846084953476126a93c1ecda1a6097d4b0435151b7f9d11e10b006a1"},
    "c18_test.clj": {"c18-verification-compatibility-wrappers-preserve-interposition": "0b0fc1689131a565f55b7b34f80fe81c0642d80f333e105723d8fa886aad9636"},
}


def clojure_deftest_source(source: str, name: str) -> str:
    start = source.index(f"(deftest {name}")
    depth = 0
    in_string = False
    escaped = False
    in_comment = False
    for index in range(start, len(source)):
        character = source[index]
        if in_comment:
            in_comment = character != "\n"
            continue
        if in_string:
            if escaped:
                escaped = False
            elif character == "\\":
                escaped = True
            elif character == '"':
                in_string = False
            continue
        if character == ";":
            in_comment = True
        elif character == '"':
            in_string = True
        elif character == "(":
            depth += 1
        elif character == ")":
            depth -= 1
            if depth == 0:
                return source[start : index + 1]
    raise AssertionError(f"unterminated deftest {name}")


def manifest_for(*checks: dict) -> dict:
    return {
        "schema_version": 1,
        "name": "test-development-verification",
        "lanes": {
            "preflight": {"description": "test"},
            "focused": {"description": "test"},
            "heavy-candidate": {"description": "test"},
        },
        "resource_policy": {
            "aggregate": {"max_rss_mb": 4096, "max_processes": 16},
            "classes": {
                "python-cheap": {
                    "max_concurrency": 4,
                    "default_rss_mb": 128,
                    "default_processes": 1,
                    "jvm_xmx_mb": None,
                    "capacity_lock": None,
                },
                "leaf-jvm": {
                    "max_concurrency": 3,
                    "default_rss_mb": 512,
                    "default_processes": 2,
                    "jvm_xmx_mb": 256,
                    "capacity_lock": "/tmp/gravity-sh07-heavy.lock",
                },
                "bootstrap-hosted": {
                    "max_concurrency": 1,
                    "default_rss_mb": 1024,
                    "default_processes": 2,
                    "jvm_xmx_mb": None,
                    "capacity_lock": "/tmp/gravity-sh07-heavy.lock",
                },
                "memory-heavy": {
                    "max_concurrency": 1,
                    "default_rss_mb": 4096,
                    "default_processes": 4,
                    "jvm_xmx_mb": None,
                    "capacity_lock": "/tmp/gravity-sh07-heavy.lock",
                },
            },
        },
        "checks": list(checks),
    }


def check(
    check_id: str,
    command: list[str],
    *,
    inputs: list[str] | None = None,
    depends_on: list[str] | None = None,
    lane: str = "focused",
    cost: str = "cheap",
    lock: str | None = None,
    exclusive: bool = False,
    authority: str = "none",
    timeout_seconds: float | None = None,
    env: dict[str, str] | None = None,
    fresh: bool = False,
    resource_class: str | None = None,
) -> dict:
    value = {
        "id": check_id,
        "lane": lane,
        "cost": cost,
        "resource_class": resource_class or ("memory-heavy" if cost == "heavy" else "python-cheap"),
        "lock": lock,
        "exclusive": exclusive,
        "authority": authority,
        "command": command,
        "inputs": inputs or ["input.txt"],
        "depends_on": depends_on or [],
        "fresh": fresh,
        "daemonization": "forbidden",
    }
    if timeout_seconds is not None:
        value["timeout_seconds"] = timeout_seconds
    if env is not None:
        value["env"] = env
    return value


class VerifyDevelopmentTests(unittest.TestCase):
    def test_resource_policy_and_check_declarations_fail_closed(self) -> None:
        command = [sys.executable, "-c", "pass"]
        baseline = manifest_for(check("one", command))
        mutations = []

        missing_policy = json.loads(json.dumps(baseline))
        missing_policy.pop("resource_policy")
        mutations.append((missing_policy, "resource_policy must be an object"))

        missing_class = json.loads(json.dumps(baseline))
        missing_class["checks"][0].pop("resource_class")
        mutations.append((missing_class, "resource_class must name"))

        unknown_class = json.loads(json.dumps(baseline))
        unknown_class["checks"][0]["resource_class"] = "typo"
        mutations.append((unknown_class, "resource_class must name"))

        invalid_limit = json.loads(json.dumps(baseline))
        invalid_limit["resource_policy"]["classes"]["python-cheap"]["max_concurrency"] = True
        mutations.append((invalid_limit, "max_concurrency must be a positive integer"))

        missing_field = json.loads(json.dumps(baseline))
        missing_field["resource_policy"]["classes"]["leaf-jvm"].pop("default_rss_mb")
        mutations.append((missing_field, "missing fields"))

        unknown_field = json.loads(json.dumps(baseline))
        unknown_field["resource_policy"]["aggregate"]["telemetry"] = 1
        mutations.append((unknown_field, "unknown fields"))

        missing_capacity_lock = json.loads(json.dumps(baseline))
        missing_capacity_lock["resource_policy"]["classes"]["leaf-jvm"].pop("capacity_lock")
        mutations.append((missing_capacity_lock, "missing fields"))

        unsafe_leaf_capacity = json.loads(json.dumps(baseline))
        unsafe_leaf_capacity["resource_policy"]["classes"]["leaf-jvm"]["capacity_lock"] = None
        mutations.append((unsafe_leaf_capacity, "capacity_lock must be"))

        unsafe_leaf_fanout = json.loads(json.dumps(baseline))
        unsafe_leaf_fanout["resource_policy"]["classes"]["leaf-jvm"]["max_concurrency"] = 4
        mutations.append((unsafe_leaf_fanout, "must not exceed three"))

        unsafe_hosted_fanout = json.loads(json.dumps(baseline))
        unsafe_hosted_fanout["resource_policy"]["classes"]["bootstrap-hosted"]["max_concurrency"] = 2
        mutations.append((unsafe_hosted_fanout, "max_concurrency must be one"))

        forged_class = json.loads(json.dumps(baseline))
        forged_class["resource_policy"]["classes"]["forged-cheap"] = {
            "max_concurrency": 32,
            "default_rss_mb": 1,
            "default_processes": 1,
            "jvm_xmx_mb": None,
            "capacity_lock": None,
        }
        forged_class["checks"][0]["resource_class"] = "forged-cheap"
        mutations.append((forged_class, "has unknown classes.*forged-cheap"))

        for manifest, message in mutations:
            with self.subTest(message=message), self.assertRaisesRegex(verifier.ManifestError, message):
                verifier.validate_manifest(manifest)

    def test_direct_clojure_jvm_xmx_must_match_declared_class(self) -> None:
        valid = manifest_for(
            check(
                "leaf",
                ["clojure", "-J-Xmx256m", "-M:leaf-test", "--group", "compiler"],
                resource_class="leaf-jvm",
            )
        )
        verifier.validate_manifest(valid)
        for command in (
            ["clojure", "-J-Xmx128m", "-M:leaf-test"],
            ["clojure", "-M:leaf-test"],
            ["clojure", "-J-Xmx256m", "-J-Xmx512m", "-M:leaf-test"],
        ):
            invalid = manifest_for(check("leaf", command, resource_class="leaf-jvm"))
            with self.subTest(command=command), self.assertRaisesRegex(
                verifier.ManifestError, "must declare exactly '-J-Xmx256m'"
            ):
                verifier.validate_manifest(invalid)

        wrapper = manifest_for(
            check("wrapped", ["bin/gravity-bootstrap", "--check"], resource_class="bootstrap-hosted")
        )
        verifier.validate_manifest(wrapper)

    def test_resource_class_and_aggregate_budgets_shape_deterministic_batches(self) -> None:
        command = [sys.executable, "-c", "pass"]
        class_limited = manifest_for(
            *(check(name, command) for name in ("e", "a", "d", "b", "c"))
        )
        class_limited["resource_policy"]["classes"]["python-cheap"]["max_concurrency"] = 2
        self.assertEqual(
            verifier.parallel_ready_groups(class_limited, jobs=32),
            [["a", "b"], ["c", "d"], ["e"]],
        )

        for budget, reservation, field in (
            (250, 150, "max_rss_mb"),
            (3, 2, "max_processes"),
        ):
            aggregate_limited = manifest_for(
                *(check(name, command) for name in ("c", "a", "b"))
            )
            policy = aggregate_limited["resource_policy"]
            if field == "max_rss_mb":
                policy["aggregate"][field] = budget
                for declaration in policy["classes"].values():
                    declaration["default_rss_mb"] = min(reservation, budget)
                policy["classes"]["python-cheap"]["default_rss_mb"] = reservation
            else:
                policy["aggregate"][field] = budget
                for declaration in policy["classes"].values():
                    declaration["default_processes"] = 1
                policy["classes"]["python-cheap"]["default_processes"] = reservation
            with self.subTest(field=field):
                self.assertEqual(
                    verifier.parallel_ready_groups(aggregate_limited, jobs=32),
                    [["a"], ["b"], ["c"]],
                )

    def test_resource_declaration_changes_cache_identity(self) -> None:
        with tempfile.TemporaryDirectory(prefix="gravity-resource-cache-") as directory:
            root = Path(directory)
            (root / "input.txt").write_text("stable\n", encoding="ascii")
            item = check("one", [sys.executable, "-c", "pass"])
            original = manifest_for(item)
            changed = json.loads(json.dumps(original))
            changed["resource_policy"]["classes"]["python-cheap"]["default_rss_mb"] += 1
            self.assertNotEqual(
                verifier.cache_key(original, item, root),
                verifier.cache_key(changed, changed["checks"][0], root),
            )

    def test_resource_metadata_is_in_planned_executed_reused_and_blocked_receipts(self) -> None:
        with tempfile.TemporaryDirectory(prefix="gravity-resource-receipt-") as directory:
            root = Path(directory)
            (root / "input.txt").write_text("stable\n", encoding="ascii")
            passing = manifest_for(check("pass", [sys.executable, "-c", "pass"]))
            planned = verifier.run_verification(passing, root, all_checks=True, dry_run=True)
            executed = verifier.run_verification(passing, root, all_checks=True, cache_path=root / "cache.json")
            reused = verifier.run_verification(
                passing, root, all_checks=True, resume=True, cache_path=root / "cache.json"
            )
            blocked_manifest = manifest_for(
                check("fail", [sys.executable, "-c", "raise SystemExit(1)"]),
                check("blocked", [sys.executable, "-c", "pass"], depends_on=["fail"]),
            )
            blocked = verifier.run_verification(
                blocked_manifest, root, all_checks=True, fail_fast=False
            )
            records = [
                planned["checks"][0],
                executed["checks"][0],
                reused["checks"][0],
                next(item for item in blocked["checks"] if item["id"] == "blocked"),
            ]
            self.assertEqual([item["status"] for item in records], ["planned", "passed", "reused", "blocked"])
            for receipt in (planned, executed, reused, blocked):
                self.assertEqual(
                    receipt["resource_policy"]["authority"],
                    "non-authoritative-admission-estimate",
                )
            for record in records:
                self.assertEqual(record["resource"]["class"], "python-cheap")
                self.assertEqual(record["resource"]["reserved_rss_mb"], 128)
                self.assertEqual(record["resource"]["reserved_processes"], 1)
                self.assertEqual(
                    record["resource"]["authority"],
                    "non-authoritative-admission-estimate",
                )
            self.assertEqual("process-tree-sampling", executed["checks"][0]["resource_observation"]["source"])
            self.assertEqual(0.25, executed["checks"][0]["resource_observation"]["sample_interval_seconds"])
            self.assertFalse(executed["checks"][0]["resource_observation"]["authoritative"])
            for record in (planned["checks"][0], reused["checks"][0], records[-1]):
                self.assertEqual("not-executed", record["resource_observation"]["source"])
                self.assertEqual(0, record["resource_observation"]["sample_count"])

    def test_observed_resource_exceedance_fails_and_is_not_cached(self) -> None:
        with tempfile.TemporaryDirectory(prefix="gravity-resource-exceeded-") as directory:
            root = Path(directory)
            (root / "input.txt").write_text("stable\n", encoding="ascii")
            cache = root / "cache.json"
            manifest = manifest_for(check("over", [sys.executable, "-c", "pass"]))
            measurement = {
                "process_count": 2, "rss_bytes": 129 * 1024 * 1024,
                "cpu_percent": 0.0, "telemetry_available": True,
                "telemetry_error": None,
            }
            with mock.patch.object(verifier, "process_tree_metrics", return_value=measurement):
                receipt = verifier.run_verification(manifest, root, all_checks=True, cache_path=cache)
            record = receipt["checks"][0]
            self.assertEqual("failed", record["status"])
            self.assertEqual("resource-budget-exceeded", record["reason"])
            self.assertFalse(record["cacheable"])
            self.assertTrue(record["resource_observation"]["rss_exceeded"])
            self.assertTrue(record["resource_observation"]["processes_exceeded"])
            self.assertFalse(cache.exists())

    def test_unavailable_resource_telemetry_is_explicit_and_non_authoritative(self) -> None:
        with tempfile.TemporaryDirectory(prefix="gravity-resource-unavailable-") as directory:
            root = Path(directory)
            (root / "input.txt").write_text("stable\n", encoding="ascii")
            manifest = manifest_for(check("unknown", [sys.executable, "-c", "pass"]))
            unavailable = {
                "process_count": None, "rss_bytes": None, "cpu_percent": None,
                "telemetry_available": False, "telemetry_error": "ps unavailable",
            }
            with mock.patch.object(verifier, "process_tree_metrics", return_value=unavailable):
                receipt = verifier.run_verification(manifest, root, all_checks=True)
            record = receipt["checks"][0]
            self.assertEqual("passed", record["status"])
            observation = record["resource_observation"]
            self.assertEqual(
                {
                    "source", "sample_count", "sample_interval_seconds", "peak_rss_bytes",
                    "peak_process_count", "telemetry_available", "telemetry_error",
                    "declared_reserved_rss_bytes", "declared_reserved_processes",
                    "rss_exceeded", "processes_exceeded", "authoritative",
                },
                set(observation),
            )
            self.assertFalse(observation["telemetry_available"])
            self.assertEqual("ps unavailable", observation["telemetry_error"])
            self.assertIsNone(observation["rss_exceeded"])
            self.assertIsNone(observation["processes_exceeded"])
            self.assertFalse(observation["authoritative"])
            self.assertFalse(any(thread.name == "gravity-resource-sampler" for thread in verifier.threading.enumerate()))

    def test_slow_initial_telemetry_cannot_extend_command_execution_deadline(self) -> None:
        with tempfile.TemporaryDirectory(prefix="gravity-slow-telemetry-") as directory:
            root = Path(directory)
            side_effect = root / "too-late"
            command = [
                sys.executable, "-c",
                f"import time; from pathlib import Path; time.sleep(0.2); Path({str(side_effect)!r}).write_text('bad')",
            ]

            def slow_metrics(_pid: int) -> dict[str, object]:
                time.sleep(0.3)
                return {
                    "process_count": None, "rss_bytes": None, "cpu_percent": None,
                    "telemetry_available": False, "telemetry_error": "slow test sampler",
                }

            started = time.monotonic()
            with mock.patch.object(verifier, "process_tree_metrics", side_effect=slow_metrics):
                outcome = verifier._run_command(
                    command, cwd=root, env=os.environ.copy(), timeout=0.1,
                    marker="slow-telemetry-deadline-test",
                )
            elapsed = time.monotonic() - started
            time.sleep(0.25)
            self.assertTrue(outcome["timed_out"])
            self.assertFalse(side_effect.exists())
            self.assertLess(elapsed, 1.5)
            self.assertFalse(any(thread.name == "gravity-resource-sampler" for thread in verifier.threading.enumerate()))

    @unittest.skipUnless(os.name == "posix", "process-group escalation requires POSIX")
    def test_timeout_sigterm_ignoring_group_is_killed_reaped_and_terminal_safe(self) -> None:
        with tempfile.TemporaryDirectory(prefix="gravity-ignore-term-") as directory:
            root = Path(directory)
            outcome = verifier._run_command(
                [
                    sys.executable,
                    "-c",
                    "import signal,time; signal.signal(signal.SIGTERM, signal.SIG_IGN); time.sleep(10)",
                ],
                cwd=root,
                env=os.environ.copy(),
                timeout=0.2,
                marker="ignore-term-deadline-test",
            )
            cleanup = outcome["cleanup"]
            self.assertTrue(outcome["timed_out"])
            self.assertIsNotNone(outcome["returncode"])
            self.assertTrue(cleanup["term_sent"])
            self.assertTrue(cleanup["kill_sent"])
            self.assertFalse(cleanup["group_alive"])
            self.assertEqual(cleanup["escaped_alive"], [])
            self.assertEqual(cleanup["marker_alive"], [])
            self.assertTrue(cleanup["terminal_safe"])
            self.assertFalse(verifier._process_group_alive(cleanup["process_group"]))

    def test_terminal_census_exception_joins_sampler_and_preserves_exception(self) -> None:
        with tempfile.TemporaryDirectory(prefix="gravity-census-exception-") as directory:
            root = Path(directory)
            with mock.patch.object(
                verifier,
                "_marker_processes",
                side_effect=RuntimeError("injected terminal census exception"),
            ):
                with self.assertRaisesRegex(RuntimeError, "injected terminal census exception"):
                    verifier._run_command(
                        [sys.executable, "-c", "import time; time.sleep(0.05)"],
                        cwd=root,
                        env=os.environ.copy(),
                        timeout=1.0,
                        marker="terminal-census-exception-test",
                    )
            self.assertFalse(
                any(thread.name == "gravity-resource-sampler" for thread in verifier.threading.enumerate())
            )

    def test_terminal_census_error_refreshes_cleanup_safety(self) -> None:
        with tempfile.TemporaryDirectory(prefix="gravity-census-error-") as directory:
            root = Path(directory)
            with mock.patch.object(
                verifier,
                "_marker_processes",
                return_value=({}, "injected terminal census failure"),
            ):
                outcome = verifier._run_command(
                    [sys.executable, "-c", "pass"],
                    cwd=root,
                    env=os.environ.copy(),
                    timeout=1.0,
                    marker="terminal-census-error-test",
                )
            self.assertEqual(
                outcome["cleanup"]["census_error"],
                "injected terminal census failure",
            )
            self.assertFalse(outcome["cleanup"]["terminal_safe"])
            self.assertTrue(outcome["supervision_failed"])

    def test_expired_supervisor_setup_deadline_never_releases_target(self) -> None:
        with tempfile.TemporaryDirectory(prefix="gravity-slow-supervisor-") as directory:
            root = Path(directory)
            side_effect = root / "must-not-run"
            command = [sys.executable, "-c", f"from pathlib import Path; Path({str(side_effect)!r}).write_text('bad')"]
            original_start = verifier._ProcessSupervisor.start

            def slow_start(supervisor: verifier._ProcessSupervisor) -> bool:
                time.sleep(0.1)
                return original_start(supervisor)

            with mock.patch.object(verifier._ProcessSupervisor, "start", slow_start):
                outcome = verifier._run_command(
                    command, cwd=root, env=os.environ.copy(), timeout=0.05,
                    marker="slow-supervisor-deadline-test",
                )
            self.assertTrue(outcome["timed_out"])
            self.assertFalse(outcome["supervision_failed"])
            self.assertFalse(side_effect.exists())
            self.assertEqual("not-executed", outcome["resource_sample"]["source"])

    @unittest.skipUnless(os.name == "posix", "launch barriers require POSIX")
    def test_barrier_release_exception_reaps_child_closes_fds_and_preserves_error(self) -> None:
        with tempfile.TemporaryDirectory(prefix="gravity-barrier-release-error-") as directory:
            root = Path(directory)
            side_effect = root / "must-not-run"
            command = [
                sys.executable,
                "-c",
                f"from pathlib import Path; Path({str(side_effect)!r}).write_text('bad')",
            ]
            original_pipe = os.pipe
            original_write = os.write
            original_popen = subprocess.Popen
            launch_fds: dict[str, int] = {}
            launched: list[subprocess.Popen[str]] = []

            def tracked_pipe() -> tuple[int, int]:
                read_fd, write_fd = original_pipe()
                if not launch_fds:
                    launch_fds.update(read=read_fd, write=write_fd)
                return read_fd, write_fd

            def fail_barrier_release(fd: int, data: bytes) -> int:
                if fd == launch_fds.get("write"):
                    raise BrokenPipeError("injected barrier release failure")
                return original_write(fd, data)

            def tracked_popen(*args, **kwargs):
                process = original_popen(*args, **kwargs)
                if kwargs.get("env", {}).get("_GRAVITY_VERIFIER_RUN") == "barrier-release-error-test":
                    launched.append(process)
                return process

            with (
                mock.patch.object(verifier.os, "pipe", side_effect=tracked_pipe),
                mock.patch.object(verifier.os, "write", side_effect=fail_barrier_release),
                mock.patch.object(verifier.subprocess, "Popen", side_effect=tracked_popen),
            ):
                with self.assertRaisesRegex(BrokenPipeError, "injected barrier release failure"):
                    verifier._run_command(
                        command,
                        cwd=root,
                        env=os.environ.copy(),
                        timeout=1.0,
                        marker="barrier-release-error-test",
                    )

            self.assertEqual(len(launched), 1)
            process = launched[0]
            self.assertIsNotNone(process.poll())
            self.assertTrue(process.stdout is not None and process.stdout.closed)
            self.assertTrue(process.stderr is not None and process.stderr.closed)
            for launch_fd in launch_fds.values():
                with self.assertRaises(OSError):
                    os.fstat(launch_fd)
            self.assertFalse(side_effect.exists())
            marker_processes, marker_error = verifier._marker_processes("barrier-release-error-test")
            self.assertIsNone(marker_error)
            self.assertEqual(marker_processes, {})
            self.assertFalse(
                any(thread.name == "gravity-resource-sampler" for thread in verifier.threading.enumerate())
            )

    def test_capacity_lock_busy_blocks_before_any_subprocess_launch(self) -> None:
        with tempfile.TemporaryDirectory(prefix="gravity-capacity-busy-") as directory:
            root = Path(directory)
            (root / "input.txt").write_text("stable\n", encoding="ascii")
            manifest = manifest_for(
                check(
                    "leaf-a",
                    [sys.executable, "-c", "pass"],
                    resource_class="leaf-jvm",
                ),
                check(
                    "leaf-b",
                    [sys.executable, "-c", "pass"],
                    resource_class="leaf-jvm",
                ),
            )
            @contextlib.contextmanager
            def busy_capacity_lock(lock_name: str | None):
                self.assertEqual(lock_name, verifier.CANONICAL_HEAVY_LOCK)
                raise verifier.LockUnavailable(f"shared resource lock is busy: {lock_name}")
                yield None

            with mock.patch.object(
                verifier, "_process_lock", busy_capacity_lock
            ), mock.patch.object(verifier, "_run_one") as run_one:
                receipt = verifier.run_verification(manifest, root, all_checks=True, jobs=32)
            run_one.assert_not_called()
            self.assertEqual(receipt["status"], "failed")
            self.assertEqual(
                [item["status"] for item in receipt["checks"]], ["blocked", "blocked"]
            )
            for record in receipt["checks"]:
                self.assertEqual(record["reason"], "capacity-lock-busy")
                self.assertEqual(record["capacity_lock"], verifier.CANONICAL_HEAVY_LOCK)
                self.assertEqual(
                    record["resource"]["capacity_lock"], verifier.CANONICAL_HEAVY_LOCK
                )

    def test_capacity_lock_is_acquired_once_for_leaf_batch_with_stable_lock_path(self) -> None:
        with tempfile.TemporaryDirectory(prefix="gravity-capacity-path-") as directory:
            root = Path(directory)
            (root / "input.txt").write_text("stable\n", encoding="ascii")
            manifest = manifest_for(
                check(
                    "leaf-a",
                    [sys.executable, "-c", "pass"],
                    resource_class="leaf-jvm",
                ),
                check(
                    "leaf-b",
                    [sys.executable, "-c", "pass"],
                    resource_class="leaf-jvm",
                ),
            )
            lock_calls: list[str | None] = []

            @contextlib.contextmanager
            def observed_process_lock(lock_name: str | None):
                lock_calls.append(lock_name)
                yield (
                    verifier._resource_lock_path(lock_name)
                    if lock_name is not None
                    else None
                )

            with mock.patch.object(verifier, "_process_lock", observed_process_lock):
                receipt = verifier.run_verification(manifest, root, all_checks=True, jobs=32)
            self.assertEqual(receipt["status"], "passed")
            self.assertEqual(
                [item for item in lock_calls if item == verifier.CANONICAL_HEAVY_LOCK],
                [verifier.CANONICAL_HEAVY_LOCK],
            )
            expected_path = str(verifier._resource_lock_path(verifier.CANONICAL_HEAVY_LOCK))
            for record in receipt["checks"]:
                self.assertEqual(record["capacity_lock"], verifier.CANONICAL_HEAVY_LOCK)
                self.assertEqual(record["capacity_lock_path"], expected_path)

    def test_canonical_default_jobs_respects_every_resource_limit(self) -> None:
        manifest = verifier.load_manifest(TOOLS / "development_verification_manifest.json")
        groups = verifier.parallel_ready_groups(manifest)
        policy = verifier.normalized_resource_policy(manifest)
        by_id = verifier.checks_by_id(manifest)
        for group in groups:
            resources = [verifier.check_resource_declaration(manifest, by_id[item]) for item in group]
            counts: dict[str, int] = {}
            for resource in resources:
                counts[resource["class"]] = counts.get(resource["class"], 0) + 1
            self.assertLessEqual(
                sum(resource["reserved_rss_mb"] for resource in resources),
                policy["aggregate"]["max_rss_mb"],
            )
            self.assertLessEqual(
                sum(resource["reserved_processes"] for resource in resources),
                policy["aggregate"]["max_processes"],
            )
            for class_name, count in counts.items():
                self.assertLessEqual(count, policy["classes"][class_name]["max_concurrency"])
            if any(verifier._effective_lock(by_id[item]) is not None for item in group):
                self.assertEqual(len(group), 1)

    def test_manifest_requires_explicit_daemonization_forbidden_policy(self) -> None:
        item = check("policy", [sys.executable, "-c", "pass"])
        item.pop("daemonization")
        with self.assertRaisesRegex(verifier.ManifestError, "daemonization='forbidden'"):
            verifier.validate_manifest(manifest_for(item))

    def test_manifest_rejects_boolean_and_nonfinite_timeouts(self) -> None:
        command = [sys.executable, "-c", "pass"]
        for timeout in (True, False, 0, -1, float("nan"), float("inf"), float("-inf"), 10**309):
            with self.subTest(timeout=timeout), self.assertRaisesRegex(
                verifier.ManifestError, "finite positive number"
            ):
                verifier.validate_manifest(
                    manifest_for(check("timeout", command, timeout_seconds=timeout))
                )
        huge = check("huge-timeout", command, timeout_seconds=10**309)
        with self.assertRaisesRegex(verifier.ManifestError, "finite positive number"):
            verifier.check_identity(huge, Path("/tmp"))

    def test_dag_order_and_parallel_ready_groups(self) -> None:
        command = [sys.executable, "-c", "pass"]
        manifest = manifest_for(
            check("z-dependent", command, depends_on=["a-root"]),
            check("a-root", command),
            check("c-independent", command),
            check("heavy", command, cost="heavy", lock="jvm", exclusive=True),
        )
        self.assertEqual(
            verifier.topological_order(manifest),
            ["a-root", "c-independent", "heavy", "z-dependent"],
        )
        self.assertEqual(
            verifier.parallel_ready_groups(manifest),
            [["a-root", "c-independent"], ["z-dependent"], ["heavy"]],
        )

    def test_change_impact_selects_downstream_and_dependencies_deterministically(self) -> None:
        command = [sys.executable, "-c", "pass"]
        manifest = manifest_for(
            check("preflight", command, lane="preflight", inputs=["contract.json"]),
            check("focused", command, inputs=["src/changed.txt"], depends_on=["preflight"]),
            check("downstream", command, inputs=["generated/report.json"], depends_on=["focused"]),
            check("unrelated", command, inputs=["other.txt"]),
        )
        selection = verifier.select_impacted_checks(manifest, Path("/tmp/project"), changed_paths=["src/changed.txt"])
        self.assertEqual(selection["selected_ids"], ["preflight", "focused", "downstream"])
        self.assertEqual(selection["unmatched_changes"], [])
        self.assertIn("changed-input:src/changed.txt", selection["reasons"]["focused"])
        self.assertIn("downstream-of:focused", selection["reasons"]["downstream"])

    def test_resume_requires_matching_declared_inputs_and_command_identity(self) -> None:
        command = [sys.executable, "-c", "import sys; sys.exit(0)"]
        with tempfile.TemporaryDirectory(prefix="gravity-verify-test-") as directory:
            root = Path(directory)
            (root / "input.txt").write_text("first\n", encoding="ascii")
            manifest = manifest_for(check("one", command))
            cache = root / "cache.json"
            first = verifier.run_verification(manifest, root, all_checks=True, cache_path=cache)
            self.assertEqual(first["checks"][0]["status"], "passed")
            second = verifier.run_verification(manifest, root, all_checks=True, resume=True, cache_path=cache)
            self.assertEqual(second["checks"][0]["status"], "reused")
            (root / "input.txt").write_text("changed\n", encoding="ascii")
            third = verifier.run_verification(manifest, root, all_checks=True, resume=True, cache_path=cache)
            self.assertEqual(third["checks"][0]["status"], "passed")
            self.assertFalse(third["checks"][0]["status"] == "reused")

    def test_fresh_and_timeout_are_bound_into_cache_identity(self) -> None:
        command = [sys.executable, "-c", "import sys; sys.exit(0)"]
        with tempfile.TemporaryDirectory(prefix="gravity-verify-declaration-identity-") as directory:
            root = Path(directory)
            (root / "input.txt").write_text("stable\n", encoding="ascii")
            original = check("one", command, timeout_seconds=1)
            manifest = manifest_for(original)
            original_key = verifier.cache_key(manifest, original, root)

            fresh = dict(original)
            fresh["fresh"] = True
            fresh_manifest = manifest_for(fresh)
            self.assertNotEqual(original_key, verifier.cache_key(fresh_manifest, fresh, root))

            retimed = dict(original)
            retimed["timeout_seconds"] = 2
            retimed_manifest = manifest_for(retimed)
            retimed_key = verifier.cache_key(retimed_manifest, retimed, root)
            self.assertNotEqual(original_key, retimed_key)
            self.assertEqual(verifier.check_identity(original, root)["fresh"], False)
            self.assertEqual(verifier.check_identity(original, root)["timeout_seconds"], 1.0)
            self.assertIsInstance(verifier.check_identity(original, root)["timeout_seconds"], float)

            integer_timeout = check("equivalent", command, timeout_seconds=5)
            float_timeout = check("equivalent", command, timeout_seconds=5.0)
            self.assertEqual(
                verifier.cache_key(manifest_for(integer_timeout), integer_timeout, root),
                verifier.cache_key(manifest_for(float_timeout), float_timeout, root),
            )

            cache = root / "cache.json"
            first = verifier.run_verification(manifest, root, all_checks=True, cache_path=cache)
            self.assertEqual(first["checks"][0]["status"], "passed")
            self.assertEqual(first["checks"][0]["timeout_seconds"], 1.0)
            changed = verifier.run_verification(
                retimed_manifest, root, all_checks=True, resume=True, cache_path=cache
            )
            self.assertEqual(changed["checks"][0]["status"], "passed")
            self.assertEqual(changed["checks"][0]["cache_key"], retimed_key)
            stable = verifier.run_verification(
                retimed_manifest, root, all_checks=True, resume=True, cache_path=cache
            )
            self.assertEqual(stable["checks"][0]["status"], "reused")
            self.assertEqual(stable["checks"][0]["timeout_seconds"], 2.0)

    def test_heavy_candidate_results_are_fresh_only(self) -> None:
        command = [sys.executable, "-c", "import sys; sys.exit(0)"]
        with tempfile.TemporaryDirectory(prefix="gravity-verify-heavy-candidate-") as directory:
            root = Path(directory)
            (root / "input.txt").write_text("stable\n", encoding="ascii")
            manifest = manifest_for(
                check(
                    "heavy-candidate",
                    command,
                    lane="heavy-candidate",
                    cost="heavy",
                    lock="heavy-candidate-lock",
                    exclusive=True,
                    authority="none",
                    fresh=True,
                )
            )
            cache = root / "cache.json"
            first = verifier.run_verification(manifest, root, all_checks=True, cache_path=cache)
            self.assertFalse(first["authoritative"])
            self.assertEqual(first["checks"][0]["authority"], "fresh-command-pass-non-authoritative")
            second = verifier.run_verification(manifest, root, all_checks=True, resume=True, cache_path=cache)
            self.assertEqual(second["checks"][0]["status"], "passed")
            self.assertFalse(second["authoritative"])

    def test_fresh_non_authoritative_evidence_is_not_reused(self) -> None:
        command = [sys.executable, "-c", "import sys; sys.exit(0)"]
        with tempfile.TemporaryDirectory(prefix="gravity-verify-fresh-") as directory:
            root = Path(directory)
            (root / "input.txt").write_text("stable\n", encoding="ascii")
            fresh_check = check("fresh", command)
            fresh_check["fresh"] = True
            manifest = manifest_for(fresh_check)
            cache = root / "cache.json"
            verifier.run_verification(manifest, root, all_checks=True, cache_path=cache)
            second = verifier.run_verification(
                manifest, root, all_checks=True, resume=True, cache_path=cache
            )
            self.assertEqual(second["checks"][0]["status"], "passed")
            self.assertFalse(second["authoritative"])

    def test_fresh_dependency_invalidates_downstream_resume(self) -> None:
        command = [sys.executable, "-c", "import sys; sys.exit(0)"]
        with tempfile.TemporaryDirectory(prefix="gravity-verify-dependency-") as directory:
            root = Path(directory)
            (root / "root.txt").write_text("one\n", encoding="ascii")
            (root / "child.txt").write_text("child\n", encoding="ascii")
            manifest = manifest_for(
                check("root", command, inputs=["root.txt"]),
                check("child", command, inputs=["child.txt"], depends_on=["root"]),
            )
            cache = root / "cache.json"
            first = verifier.run_verification(manifest, root, all_checks=True, cache_path=cache)
            self.assertEqual({item["status"] for item in first["checks"]}, {"passed"})
            (root / "root.txt").write_text("two\n", encoding="ascii")
            second = verifier.run_verification(manifest, root, all_checks=True, resume=True, cache_path=cache)
            statuses = {item["id"]: item["status"] for item in second["checks"]}
            self.assertEqual(statuses, {"root": "passed", "child": "passed"})

    def test_passing_command_that_mutates_declared_input_is_failed_and_not_cached(self) -> None:
        with tempfile.TemporaryDirectory(prefix="gravity-verify-stale-input-") as directory:
            root = Path(directory)
            input_path = root / "input.txt"
            input_path.write_text("before\n", encoding="ascii")
            command = [
                sys.executable,
                "-c",
                "from pathlib import Path; Path('input.txt').write_text('after\\n', encoding='ascii')",
            ]
            manifest = manifest_for(check("mutator", command))
            cache = root / "cache.json"

            first = verifier.run_verification(manifest, root, all_checks=True, cache_path=cache)
            first_record = first["checks"][0]
            self.assertEqual(first["status"], "failed")
            self.assertEqual(first_record["status"], "failed")
            self.assertEqual(first_record["reason"], "stale-input")
            self.assertNotIn("mutator", verifier.load_cache(cache)["checks"])

            second = verifier.run_verification(
                manifest, root, all_checks=True, resume=True, cache_path=cache
            )
            self.assertNotEqual(second["checks"][0]["status"], "reused")

    def test_unmatched_changed_path_fails_closed(self) -> None:
        command = [sys.executable, "-c", "import sys; sys.exit(0)"]
        with tempfile.TemporaryDirectory(prefix="gravity-verify-unmatched-") as directory:
            root = Path(directory)
            (root / "input.txt").write_text("stable\n", encoding="ascii")
            manifest = manifest_for(check("known", command, inputs=["input.txt"]))
            receipt = verifier.run_verification(manifest, root, changed_paths=["undelared/file.py"])
            self.assertEqual(receipt["status"], "failed")
            self.assertIn("undelared/file.py", receipt["error"])
            self.assertEqual(receipt["checks"], [])

    def test_all_selection_ignores_ambient_unmatched_path_but_impact_selection_fails(self) -> None:
        command = [sys.executable, "-c", "import sys; sys.exit(0)"]
        with tempfile.TemporaryDirectory(prefix="gravity-verify-all-selection-") as directory:
            root = Path(directory)
            (root / "input.txt").write_text("stable\n", encoding="ascii")
            manifest = manifest_for(
                check("preflight", command, lane="preflight", inputs=["input.txt"]),
                check("focused", command, lane="focused", inputs=["input.txt"]),
            )
            manifest_path = root / "manifest.json"
            manifest_path.write_text(json.dumps(manifest), encoding="ascii")

            # The CLI discovers this path from git in a real worktree.  It is
            # deliberately unrelated to every declared input so the regression
            # covers the coordinator's dirty-worktree behavior, not only the
            # direct Python API.
            with mock.patch.object(verifier, "_discover_changed_paths", return_value=["generated.py"]):
                dry_output = io.StringIO()
                with redirect_stdout(dry_output):
                    dry_code = verifier.main(
                        [
                            "--manifest",
                            str(manifest_path),
                            "--root",
                            str(root),
                            "--all",
                            "--lane",
                            "preflight",
                            "--lane",
                            "focused",
                            "--dry-run",
                        ]
                    )
            self.assertEqual(dry_code, 0)
            dry_receipt = json.loads(dry_output.getvalue())
            self.assertEqual(dry_receipt["status"], "planned")
            self.assertEqual(
                [item["id"] for item in dry_receipt["checks"]],
                ["focused", "preflight"],
            )
            self.assertTrue(all("timeout_seconds" in item for item in dry_receipt["checks"]))
            self.assertTrue(all(item["timeout_seconds"] is None for item in dry_receipt["checks"]))
            self.assertEqual(dry_receipt["selection"]["selection_mode"], "all")
            self.assertEqual(dry_receipt["selection"]["unmatched_changes"], ["generated.py"])

            with mock.patch.object(verifier, "_discover_changed_paths", return_value=["generated.py"]):
                run_output = io.StringIO()
                with redirect_stdout(run_output):
                    run_code = verifier.main(
                        [
                            "--manifest",
                            str(manifest_path),
                            "--root",
                            str(root),
                            "--all",
                            "--lane",
                            "preflight",
                            "--lane",
                            "focused",
                        ]
                    )
            self.assertEqual(run_code, 0)
            run_receipt = json.loads(run_output.getvalue())
            self.assertEqual(run_receipt["status"], "passed")
            self.assertEqual(
                {item["id"] for item in run_receipt["checks"]},
                {"focused", "preflight"},
            )

            # Without --all the same dirty path is an impact-selection input,
            # so unmatched ownership remains fail-closed and no check runs.
            impact = verifier.run_verification(
                manifest,
                root,
                changed_paths=["generated.py"],
                lanes=["preflight", "focused"],
                dry_run=True,
            )
            self.assertEqual(impact["status"], "failed")
            self.assertEqual(impact["selection"]["selection_mode"], "change-impact")
            self.assertEqual(impact["checks"], [])
            self.assertIn("generated.py", impact["error"])

    def test_explicit_selection_modes_observe_changed_paths_and_reject_all_check_ambiguity(self) -> None:
        command = [sys.executable, "-c", "import sys; sys.exit(0)"]
        with tempfile.TemporaryDirectory(prefix="gravity-verify-selection-modes-") as directory:
            root = Path(directory)
            (root / "input.txt").write_text("stable\n", encoding="ascii")
            manifest = manifest_for(check("focused", command, inputs=["input.txt"]))

            explicit_check = verifier.run_verification(
                manifest,
                root,
                changed_paths=["generated.py"],
                requested_ids=["focused"],
                dry_run=True,
            )
            self.assertEqual(explicit_check["status"], "planned")
            self.assertEqual(explicit_check["selection"]["selection_mode"], "explicit-check")
            self.assertEqual(explicit_check["selection"]["unmatched_changes"], ["generated.py"])
            self.assertEqual(explicit_check["checks"][0]["id"], "focused")
            self.assertIn("timeout_seconds", explicit_check["checks"][0])

            with self.assertRaisesRegex(verifier.VerificationError, "--all cannot be combined"):
                verifier.run_verification(
                    manifest,
                    root,
                    all_checks=True,
                    requested_ids=["focused"],
                    dry_run=True,
                )

    def test_lane_filtered_change_owned_only_by_excluded_lane_fails_closed(self) -> None:
        command = [sys.executable, "-c", "import sys; sys.exit(0)"]
        with tempfile.TemporaryDirectory(prefix="gravity-verify-lane-closure-") as directory:
            root = Path(directory)
            (root / "input.txt").write_text("stable\n", encoding="ascii")
            manifest = manifest_for(
                check("preflight-owner", command, lane="preflight", inputs=["bin/gravity-bootstrap"]),
                check("focused-owner", command, lane="focused", inputs=["input.txt"]),
            )
            receipt = verifier.run_verification(
                manifest,
                root,
                changed_paths=["bin/gravity-bootstrap"],
                lanes=["focused"],
            )
            self.assertEqual(receipt["status"], "failed")
            self.assertEqual(receipt["checks"], [])
            self.assertEqual(receipt["selection"]["selected_ids"], [])
            self.assertEqual(receipt["selection"]["matched_outside_lane"], ["bin/gravity-bootstrap"])
            self.assertIn("outside requested lane", receipt["error"])

    def test_change_and_restore_is_event_observed_and_not_cached(self) -> None:
        with tempfile.TemporaryDirectory(prefix="gravity-verify-restore-") as directory:
            root = Path(directory)
            input_path = root / "input.txt"
            input_path.write_text("before\n", encoding="ascii")
            command = [
                sys.executable,
                "-c",
                (
                    "from pathlib import Path; import time; "
                    "p=Path('input.txt'); old=p.read_text(); "
                    "p.write_text('transient\\n'); p.write_text(old); time.sleep(0.15)"
                ),
            ]
            manifest = manifest_for(check("restore", command))
            cache = root / "cache.json"
            receipt = verifier.run_verification(manifest, root, all_checks=True, cache_path=cache)
            record = receipt["checks"][0]
            self.assertEqual(receipt["status"], "failed")
            self.assertEqual(record["reason"], "stale-input")
            self.assertTrue(record["mutation_observed"])
            self.assertNotIn("restore", verifier.load_cache(cache)["checks"])

    def test_glob_membership_create_use_delete_is_event_observed_and_not_cached(self) -> None:
        with tempfile.TemporaryDirectory(prefix="gravity-verify-glob-membership-") as directory:
            root = Path(directory)
            (root / "input.txt").write_text("stable\n", encoding="ascii")
            command = [
                sys.executable,
                "-c",
                (
                    "from pathlib import Path; import time; "
                    "p=Path('ephemeral.txt'); p.write_text('created\\n'); "
                    "assert p.read_text() == 'created\\n'; p.unlink(); time.sleep(0.1)"
                ),
            ]
            manifest = manifest_for(check("glob", command, inputs=["*.txt"]))
            cache = root / "cache.json"
            receipt = verifier.run_verification(manifest, root, all_checks=True, cache_path=cache)
            record = receipt["checks"][0]
            self.assertEqual(receipt["status"], "failed")
            self.assertEqual(record["reason"], "stale-input")
            self.assertTrue(record["mutation_observed"])
            self.assertNotIn("glob", verifier.load_cache(cache)["checks"])

    def test_nested_glob_membership_rename_restore_is_event_observed_and_not_cached(self) -> None:
        with tempfile.TemporaryDirectory(prefix="gravity-verify-nested-glob-") as directory:
            root = Path(directory)
            nested = root / "nested" / "child"
            nested.mkdir(parents=True)
            (root / "input.txt").write_text("stable\n", encoding="ascii")
            command = [
                sys.executable,
                "-c",
                (
                    "from pathlib import Path; import time; "
                    "p=Path('nested/child/ephemeral.txt'); q=p.with_name('renamed.txt'); "
                    "p.write_text('created\\n'); p.rename(q); q.rename(p); p.unlink(); time.sleep(0.1)"
                ),
            ]
            manifest = manifest_for(check("nested-glob", command, inputs=["nested/**/*.txt"]))
            cache = root / "cache.json"
            receipt = verifier.run_verification(manifest, root, all_checks=True, cache_path=cache)
            record = receipt["checks"][0]
            self.assertEqual(receipt["status"], "failed")
            self.assertEqual(record["reason"], "stale-input")
            self.assertTrue(record["mutation_observed"])
            self.assertNotIn("nested-glob", verifier.load_cache(cache)["checks"])

    def test_manifest_environment_is_redacted_and_changes_invalidate_cache_identity(self) -> None:
        command = [sys.executable, "-c", "import sys; sys.exit(0)"]
        manifest = manifest_for(check("env", command, env={"MANIFEST_SECRET": "super-secret-value"}))
        with tempfile.TemporaryDirectory(prefix="gravity-verify-env-binding-") as directory:
            root = Path(directory)
            (root / "input.txt").write_text("stable\n", encoding="ascii")
            identity = verifier.command_identity(manifest["checks"][0], root)
            rendered = json.dumps(identity, sort_keys=True)
            self.assertNotIn("super-secret-value", rendered)
            self.assertIn("MANIFEST_SECRET", identity["runtime"]["environment"])
            ambient_manifest = manifest_for(check("ambient", command))
            with mock.patch.dict(os.environ, {"CUSTOM_SEMANTIC_FLAG": "one"}, clear=False):
                first_key = verifier.cache_key(ambient_manifest, ambient_manifest["checks"][0], root)
            with mock.patch.dict(os.environ, {"CUSTOM_SEMANTIC_FLAG": "two"}, clear=False):
                second_key = verifier.cache_key(ambient_manifest, ambient_manifest["checks"][0], root)
            self.assertNotEqual(first_key, second_key)

    def test_timeout_kills_descendant_before_releasing_lock(self) -> None:
        with tempfile.TemporaryDirectory(prefix="gravity-verify-timeout-tree-") as directory:
            root = Path(directory)
            (root / "input.txt").write_text("stable\n", encoding="ascii")
            command = [
                sys.executable,
                "-c",
                (
                    "import subprocess,sys,time; from pathlib import Path; "
                    "subprocess.Popen([sys.executable,'-c',\"import time; from pathlib import Path; time.sleep(0.8); Path('survived').write_text('yes')\"]); "
                    "time.sleep(10)"
                ),
            ]
            manifest = manifest_for(check("timeout", command, lock="timeout-resource", timeout_seconds=0.1))
            receipt = verifier.run_verification(manifest, root, all_checks=True)
            record = receipt["checks"][0]
            self.assertEqual(record["status"], "timeout")
            self.assertIn("timeout_cleanup", record)
            self.assertFalse(record["timeout_cleanup"]["group_alive"])
            time.sleep(1.0)
            self.assertFalse((root / "survived").exists())

    def test_normal_parent_exit_kills_surviving_descendant_before_lock_release(self) -> None:
        with tempfile.TemporaryDirectory(prefix="gravity-verify-descendant-") as directory:
            root = Path(directory)
            (root / "input.txt").write_text("stable\n", encoding="ascii")
            command = [
                sys.executable,
                "-c",
                (
                    "import subprocess,sys; from pathlib import Path; "
                    "subprocess.Popen([sys.executable,'-c',\"import time; from pathlib import Path; "
                    "time.sleep(0.8); Path('survived').write_text('yes')\"], "
                    "stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)"
                ),
            ]
            manifest = manifest_for(check("descendant", command, lock="descendant-resource"))
            receipt = verifier.run_verification(manifest, root, all_checks=True)
            record = receipt["checks"][0]
            self.assertEqual(receipt["status"], "failed")
            self.assertEqual(record["reason"], "surviving-descendant")
            self.assertFalse(record["descendant_cleanup"]["group_alive"])
            time.sleep(1.0)
            self.assertFalse((root / "survived").exists())

    def test_setsid_daemon_with_closed_standard_fds_is_killed_before_lock_release(self) -> None:
        with tempfile.TemporaryDirectory(prefix="gravity-verify-setsid-") as directory:
            root = Path(directory)
            (root / "input.txt").write_text("stable\n", encoding="ascii")
            command = [
                sys.executable,
                "-c",
                (
                    "import os,time; from pathlib import Path; "
                    "pid=os.fork(); "
                    "(os.setsid(), [os.dup2(os.open('/dev/null', os.O_RDWR), fd) for fd in (0,1,2)], "
                    "time.sleep(0.7), Path('escaped').write_text('yes'), os._exit(0)) if pid == 0 else os._exit(0)"
                ),
            ]
            manifest = manifest_for(check("setsid", command, lock="setsid-resource"))
            receipt = verifier.run_verification(manifest, root, all_checks=True)
            record = receipt["checks"][0]
            self.assertEqual(receipt["status"], "failed")
            self.assertEqual(record["reason"], "surviving-descendant")
            self.assertEqual(record["authority"], "non-authoritative")
            self.assertEqual(manifest["checks"][0]["daemonization"], "forbidden")
            self.assertFalse(record["descendant_cleanup"]["group_alive"])
            self.assertEqual(record["descendant_cleanup"]["escaped_alive"], [])
            self.assertEqual(record["descendant_cleanup"]["marker_alive"], [])
            self.assertTrue(record["descendant_cleanup"]["terminal_safe"])
            time.sleep(0.9)
            self.assertFalse((root / "escaped").exists())

    def test_double_fork_setsid_descendant_is_killed_before_timeout_lock_release(self) -> None:
        with tempfile.TemporaryDirectory(prefix="gravity-verify-double-fork-") as directory:
            root = Path(directory)
            (root / "input.txt").write_text("stable\n", encoding="ascii")
            command = [
                sys.executable,
                "-c",
                (
                    "import os,time; from pathlib import Path; "
                    "first=os.fork(); "
                    "(second := os.fork()) if first == 0 else time.sleep(10); "
                    "(os.setsid(), time.sleep(1.5), Path('double-escaped').write_text('yes'), os._exit(0)) if first == 0 and second == 0 else os._exit(0)"
                ),
            ]
            manifest = manifest_for(check("double-fork", command, lock="double-fork-resource", timeout_seconds=0.2))
            receipt = verifier.run_verification(manifest, root, all_checks=True)
            record = receipt["checks"][0]
            self.assertEqual(receipt["status"], "failed")
            self.assertEqual(record["status"], "timeout")
            self.assertFalse(record["timeout_cleanup"]["group_alive"])
            time.sleep(1.7)
            self.assertFalse((root / "double-escaped").exists())

    def test_supervisor_does_not_kill_unrelated_process_without_run_marker(self) -> None:
        with tempfile.TemporaryDirectory(prefix="gravity-verify-unrelated-") as directory:
            root = Path(directory)
            (root / "input.txt").write_text("stable\n", encoding="ascii")
            unrelated = subprocess.Popen(
                [sys.executable, "-c", "import time; from pathlib import Path; time.sleep(0.5); Path('unrelated').write_text('yes')"],
                cwd=root,
                stdout=subprocess.DEVNULL,
                stderr=subprocess.DEVNULL,
                start_new_session=True,
            )
            command = [sys.executable, "-c", "import sys; sys.exit(0)"]
            manifest = manifest_for(check("unrelated-check", command, lock="unrelated-resource"))
            receipt = verifier.run_verification(manifest, root, all_checks=True)
            self.assertEqual(receipt["status"], "passed")
            unrelated.wait(timeout=2.0)
            self.assertEqual(unrelated.returncode, 0)
            self.assertTrue((root / "unrelated").exists())

    def test_saved_pid_revalidation_rejects_pid_reuse_deterministically(self) -> None:
        saved = {"start_identity": "Fri Aug 7 09:00:00 2026", "pgid": 123}
        replacement = {
            "start_identity": "Fri Aug 7 09:00:01 2026",
            "pgid": 123,
            "stat": "S",
            "command": "worker _GRAVITY_VERIFIER_RUN=marker",
        }
        with mock.patch.object(verifier, "_target_process_record", return_value=(replacement, None)):
            valid, error = verifier._validated_saved_process(123, saved, "marker")
        self.assertFalse(valid)
        self.assertIsNone(error)

    def test_cleanup_terminal_policy_fails_closed_on_every_unknown_or_residual(self) -> None:
        clean = {
            "group_alive": False, "escaped_alive": [], "marker_alive": [],
            "output_complete": True, "error": None, "census_error": None,
        }
        self.assertTrue(verifier._cleanup_terminal_safe(clean))
        for field, value in (
            ("group_alive", True), ("escaped_alive", [123]),
            ("marker_alive", ["unknown"]), ("output_complete", False),
            ("error", "unknown"), ("census_error", "unknown"),
        ):
            residual = dict(clean)
            residual[field] = value
            self.assertFalse(verifier._cleanup_terminal_safe(residual), field)

    def test_supervision_marker_is_deterministic_and_bound_to_identity(self) -> None:
        command = [sys.executable, "-c", "pass"]
        with tempfile.TemporaryDirectory(prefix="gravity-marker-identity-") as directory:
            root = Path(directory)
            (root / "input.txt").write_text("stable\n", encoding="ascii")
            item = check("marker", command)
            first = verifier.check_identity(item, root)
            second = verifier.check_identity(item, root)
            self.assertEqual(verifier._marker_from_bound_identity(first), verifier._marker_from_bound_identity(second))
            binding = first["command"]["runtime"]["supervision_environment"]["_GRAVITY_VERIFIER_RUN"]
            self.assertTrue(binding["present"])
            self.assertEqual(binding["sha256"], verifier._sha256_text(verifier._marker_from_bound_identity(first)))

    def test_long_running_command_has_constant_bounded_process_census_count(self) -> None:
        with tempfile.TemporaryDirectory(prefix="gravity-census-count-") as directory:
            root = Path(directory)
            (root / "input.txt").write_text("stable\n", encoding="ascii")
            command = [sys.executable, "-c", "import time; time.sleep(0.4)"]
            manifest = manifest_for(check("long", command))
            original_run = verifier.subprocess.run
            census_calls = 0

            def counting_run(*args, **kwargs):
                nonlocal census_calls
                argv = args[0] if args else kwargs.get("args", [])
                if (
                    argv
                    and Path(str(argv[0])).name == "ps"
                    and "eww" in argv
                    and "-axo" in argv
                ):
                    census_calls += 1
                return original_run(*args, **kwargs)

            with mock.patch.object(verifier.subprocess, "run", side_effect=counting_run):
                receipt = verifier.run_verification(manifest, root, all_checks=True)
            self.assertEqual(receipt["status"], "passed")
            self.assertEqual(census_calls, 1)

    def test_resource_lock_symlink_and_hardlink_preserve_victim(self) -> None:
        lock_name = "unit-test-unsafe-resource"
        lock_path = verifier._resource_lock_path(lock_name)
        with tempfile.TemporaryDirectory(prefix="gravity-verify-lock-victim-") as directory:
            victim = Path(directory) / "victim"
            victim.write_text("victim\n", encoding="ascii")
            for kind in ("symlink", "hardlink"):
                try:
                    lock_path.unlink()
                except FileNotFoundError:
                    pass
                if kind == "symlink":
                    lock_path.symlink_to(victim)
                else:
                    os.link(victim, lock_path)
                with self.assertRaises(verifier.LockUnavailable):
                    with verifier._process_lock(lock_name):
                        pass
                self.assertEqual(victim.read_text(encoding="ascii"), "victim\n")
                lock_path.unlink()

    def test_cache_lock_symlink_and_hardlink_preserve_victim(self) -> None:
        with tempfile.TemporaryDirectory(prefix="gravity-verify-cache-victim-") as directory:
            root = Path(directory)
            cache = root / "cache.json"
            victim = root / "victim"
            victim.write_text("victim\n", encoding="ascii")
            lock_path = verifier._cache_lock_path(cache)
            for kind in ("symlink", "hardlink"):
                try:
                    lock_path.unlink()
                except FileNotFoundError:
                    pass
                if kind == "symlink":
                    lock_path.symlink_to(victim)
                else:
                    os.link(victim, lock_path)
                with self.assertRaises(verifier.LockUnavailable):
                    with verifier._cache_process_lock(cache):
                        pass
                self.assertEqual(victim.read_text(encoding="ascii"), "victim\n")
                lock_path.unlink()

    def test_resource_lock_parent_swap_stays_on_open_directory_fd(self) -> None:
        lock_parent_swap = "resource-parent-swap"
        with tempfile.TemporaryDirectory(prefix="gravity-verify-parent-swap-") as directory:
            root = Path(directory)
            parent = root / "lock-parent"
            parent.mkdir()
            victim = root / "victim"
            victim.mkdir()
            victim_file = victim / "victim.txt"
            victim_file.write_text("victim\n", encoding="ascii")
            safe_parent = root / "lock-parent-safe"
            with self.assertRaises(verifier.LockUnavailable):
                with verifier._process_lock(str(parent / lock_parent_swap)):
                    pass
            self.assertEqual(victim_file.read_text(encoding="ascii"), "victim\n")

    def test_cache_lock_parent_swap_stays_on_open_directory_fd(self) -> None:
        with tempfile.TemporaryDirectory(prefix="gravity-verify-cache-parent-swap-") as directory:
            root = Path(directory)
            parent = root / "cache-parent"
            parent.mkdir()
            cache = parent / "cache.json"
            victim = root / "victim"
            victim.mkdir()
            victim_file = victim / "victim.txt"
            victim_file.write_text("victim\n", encoding="ascii")
            safe_parent = root / "cache-parent-safe"
            try:
                lock_path = verifier._cache_lock_path(cache)
                with verifier._cache_process_lock(cache):
                    parent.rename(safe_parent)
                    parent.symlink_to(victim, target_is_directory=True)
                    self.assertEqual(verifier._cache_lock_path(cache), lock_path)
                self.assertEqual(victim_file.read_text(encoding="ascii"), "victim\n")
            finally:
                if parent.is_symlink():
                    parent.unlink()
                if safe_parent.exists():
                    safe_parent.rename(parent)

    def test_cache_lock_two_openers_remain_exclusive_across_parent_swap(self) -> None:
        with tempfile.TemporaryDirectory(prefix="gravity-cache-two-openers-") as directory:
            root = Path(directory)
            parent = root / "cache-parent"
            parent.mkdir()
            moved = root / "cache-parent-moved"
            victim = root / "victim"
            victim.mkdir()
            cache = parent / "cache.json"
            code = (
                "import sys; from pathlib import Path; "
                f"sys.path.insert(0, {str(TOOLS)!r}); import verify_development as v; "
                f"\nwith v._cache_process_lock(Path({str(cache)!r})):\n print('acquired', flush=True)"
            )
            child = None
            try:
                with verifier._cache_process_lock(cache):
                    parent.rename(moved)
                    parent.symlink_to(victim, target_is_directory=True)
                    child = subprocess.Popen([sys.executable, "-c", code], stdout=subprocess.PIPE, text=True)
                    time.sleep(0.35)
                    self.assertIsNone(child.poll())
                stdout, _ = child.communicate(timeout=2.0)
                self.assertEqual(child.returncode, 0)
                self.assertIn("acquired", stdout)
            finally:
                if child is not None and child.poll() is None:
                    child.kill()
                    child.wait()
                if parent.is_symlink():
                    parent.unlink()
                if moved.exists():
                    moved.rename(parent)

    def test_cache_load_and_write_parent_swap_stay_on_open_directory_fd(self) -> None:
        with tempfile.TemporaryDirectory(prefix="gravity-verify-cache-parent-swap-io-") as directory:
            root = Path(directory)
            parent = root / "cache-parent"
            parent.mkdir()
            cache = parent / "cache.json"
            verifier._write_json(cache, {"schema_version": verifier.SCHEMA_VERSION, "checks": {"ok": {}}})
            victim = root / "victim"
            victim.mkdir()
            victim_file = victim / "victim.txt"
            victim_file.write_text("victim\n", encoding="ascii")
            safe_parent = root / "cache-parent-safe"
            original_validate = verifier._validate_directory_descriptor
            swapped = False

            def swap_after_validate(fd, path, *, label):
                nonlocal swapped
                result = original_validate(fd, path, label=label)
                if path.resolve() == parent.resolve() and not swapped:
                    parent.rename(safe_parent)
                    parent.symlink_to(victim, target_is_directory=True)
                    swapped = True
                return result

            try:
                with mock.patch.object(verifier, "_validate_directory_descriptor", side_effect=swap_after_validate):
                    loaded = verifier.load_cache(cache)
                self.assertTrue(swapped)
                self.assertIn("ok", loaded["checks"])
                self.assertEqual(victim_file.read_text(encoding="ascii"), "victim\n")
                if parent.is_symlink():
                    parent.unlink()
                safe_parent.rename(parent)
                swapped = False
                with mock.patch.object(verifier, "_validate_directory_descriptor", side_effect=swap_after_validate):
                    verifier._write_json(cache, {"schema_version": verifier.SCHEMA_VERSION, "checks": {"new": {}}})
                self.assertTrue(swapped)
                self.assertTrue(parent.is_symlink())
                parent.unlink()
                safe_parent.rename(parent)
                self.assertIn("new", verifier.load_cache(cache)["checks"])
                self.assertEqual(victim_file.read_text(encoding="ascii"), "victim\n")
            finally:
                if parent.is_symlink():
                    parent.unlink()
                if safe_parent.exists():
                    safe_parent.rename(parent)

    def test_cache_identity_binds_root_runtime_environment_and_rejects_escape(self) -> None:
        command = [sys.executable, "-c", "import sys; sys.exit(0)"]
        manifest = manifest_for(check("one", command))
        with tempfile.TemporaryDirectory(prefix="gravity-verify-identity-") as first_directory, tempfile.TemporaryDirectory(prefix="gravity-verify-identity-") as second_directory:
            first = Path(first_directory)
            second = Path(second_directory)
            (first / "input.txt").write_text("stable\n", encoding="ascii")
            (second / "input.txt").write_text("stable\n", encoding="ascii")
            key_one = verifier.cache_key(manifest, manifest["checks"][0], first)
            key_two = verifier.cache_key(manifest, manifest["checks"][0], second)
            self.assertNotEqual(key_one, key_two)
            with mock.patch.dict(os.environ, {"TZ": "UTC"}, clear=False):
                utc_key = verifier.cache_key(manifest, manifest["checks"][0], first)
            with mock.patch.dict(os.environ, {"TZ": "Pacific/Honolulu"}, clear=False):
                honolulu_key = verifier.cache_key(manifest, manifest["checks"][0], first)
            self.assertNotEqual(utc_key, honolulu_key)
            outside = first.parent / "outside.txt"
            outside.write_text("outside\n", encoding="ascii")
            (first / "escape.txt").symlink_to(outside)
            with self.assertRaises(verifier.VerificationError):
                verifier.input_identities(check("escape", command, inputs=["escape.txt"]), first)

    def test_declared_input_symlink_is_rejected_before_hashing(self) -> None:
        command = [sys.executable, "-c", "import sys; sys.exit(0)"]
        with tempfile.TemporaryDirectory(prefix="gravity-verify-input-symlink-") as directory:
            root = Path(directory)
            outside = root.parent / "gravity-input-outside.txt"
            outside.write_text("outside\n", encoding="ascii")
            try:
                (root / "input.txt").symlink_to(outside)
                with self.assertRaises(verifier.VerificationError):
                    verifier.input_identities(check("symlink", command), root)
            finally:
                outside.unlink()

    def test_input_identity_rejects_path_swap_between_discovery_and_open(self) -> None:
        command = [sys.executable, "-c", "import sys; sys.exit(0)"]
        with tempfile.TemporaryDirectory(prefix="gravity-verify-input-swap-") as directory:
            root = Path(directory)
            input_path = root / "input.txt"
            input_path.write_text("stable\n", encoding="ascii")
            outside = root.parent / f"gravity-input-swap-outside-{root.name}.txt"
            outside.write_text("outside\n", encoding="ascii")
            original_open = verifier.os.open
            swapped = False

            def racing_open(path, *args, **kwargs):
                nonlocal swapped
                if kwargs.get("dir_fd") is not None and path == "input.txt" and not swapped:
                    input_path.unlink()
                    input_path.symlink_to(outside)
                    swapped = True
                return original_open(path, *args, **kwargs)

            try:
                with mock.patch.object(verifier.os, "open", side_effect=racing_open):
                    with self.assertRaises(verifier.VerificationError):
                        verifier.input_identities(check("swap", command), root)
                self.assertTrue(swapped)
            finally:
                input_path.unlink(missing_ok=True)
                outside.unlink(missing_ok=True)

    def test_explicit_check_outside_lane_fails_closed_with_owner_details(self) -> None:
        command = [sys.executable, "-c", "import sys; sys.exit(0)"]
        with tempfile.TemporaryDirectory(prefix="gravity-verify-requested-lane-") as directory:
            root = Path(directory)
            (root / "input.txt").write_text("stable\n", encoding="ascii")
            manifest = manifest_for(
                check("heavy", command, lane="heavy-candidate", cost="heavy", lock="heavy-resource")
            )
            receipt = verifier.run_verification(
                manifest,
                root,
                requested_ids=["heavy"],
                lanes=["preflight"],
            )
            self.assertEqual(receipt["status"], "failed")
            self.assertEqual(receipt["checks"], [])
            self.assertEqual(receipt["selection"]["requested_outside_lane"], ["heavy"])
            self.assertIn("heavy (heavy-candidate)", receipt["error"])

    def test_heavy_candidate_selection_closes_over_admission_gates(self) -> None:
        manifest = verifier.load_manifest(ROOT / "tools" / "development_verification_manifest.json")
        selection = verifier.select_impacted_checks(manifest, ROOT, lanes=["heavy-candidate"])
        selected = set(selection["selected_ids"])
        required = {
            "m0-docs",
            "m0-foundation-coverage",
            "m0-contract-traceability",
            "m0-milestone-evidence",
            "m0-terminology",
            "m0-safety-performance",
            "m0-change-control",
            "stage0-project-structure",
            "stage0-orchestrator-unit",
            "stage0-reader",
            "stage0-hosted-hello",
            "stage0-hosted-hello-qst",
            "stage0-selective-smoke",
            "stage0-hosted-core-app",
            "stage0-hosted-core-compiled-app",
        }
        self.assertTrue(required <= selected)
        self.assertIn("stage0-clojure-suite", selected)
        self.assertIn("stage0-bootstrap-authority", selected)

    def test_stage0_project_structure_check_has_contract_inputs(self) -> None:
        manifest = verifier.load_manifest(ROOT / "tools" / "development_verification_manifest.json")
        check_record = next(item for item in manifest["checks"] if item["id"] == "stage0-project-structure")
        self.assertEqual(check_record["lane"], "preflight")
        self.assertEqual(check_record["cost"], "cheap")
        self.assertIsNone(check_record["lock"])
        self.assertFalse(check_record["exclusive"])
        self.assertEqual(check_record["authority"], "none")
        self.assertEqual(
            check_record["command"],
            ["python3", "tools/validate_project_structure.py", "contracts/project-structure.json"],
        )
        self.assertEqual(
            set(check_record["inputs"]),
            {
                "contracts/project-structure.json",
                "contracts/stage0-clojure-components.json",
                "docs/self-hosting-slice-ownership.edn",
                "tools/validate_project_structure.py",
            },
        )

    def test_component_source_and_test_edits_route_to_the_matching_leaf_group(self) -> None:
        manifest = verifier.load_manifest(ROOT / "tools" / "development_verification_manifest.json")
        leaf_ids = {
            "stage0-leaf-foundation-reader",
            "stage0-leaf-c2-c3",
            "stage0-leaf-compiler",
        }
        cases = {
            "bootstrap/clojure/src/gravity/c2_artifact_identity.clj": {"stage0-leaf-c2-c3"},
            "bootstrap/clojure/test/gravity/c2_artifact_identity_test.clj": {"stage0-leaf-c2-c3"},
            "bootstrap/clojure/src/gravity/c7_type_checker.clj": {"stage0-leaf-compiler"},
            "bootstrap/clojure/test/gravity/c7_type_checker_test.clj": {"stage0-leaf-compiler"},
            "bootstrap/clojure/src/gravity/core_ast_lowering.clj": {"stage0-leaf-compiler"},
            "bootstrap/clojure/test/gravity/core_ast_lowering_test.clj": {"stage0-leaf-compiler"},
            "bootstrap/clojure/src/gravity/reader_cursor.clj": {"stage0-leaf-foundation-reader"},
            "bootstrap/clojure/test/gravity/reader_cursor_test.clj": {"stage0-leaf-foundation-reader"},
            "bootstrap/clojure/src/gravity/module_analysis.clj": {"stage0-leaf-foundation-reader"},
            "bootstrap/clojure/test/gravity/module_analysis_test.clj": {"stage0-leaf-foundation-reader"},
        }
        for changed_path, expected in cases.items():
            with self.subTest(changed_path=changed_path):
                selection = verifier.select_impacted_checks(
                    manifest,
                    ROOT,
                    changed_paths=[changed_path],
                    lanes=["focused"],
                )
                selected_leaf_ids = set(selection["selected_ids"]) & leaf_ids
                self.assertEqual(expected, selected_leaf_ids)

    def test_leaf_manifest_inputs_exactly_cover_normative_roots_and_source_closures(self) -> None:
        manifest = verifier.load_manifest(ROOT / "tools" / "development_verification_manifest.json")
        contract = json.loads(
            (ROOT / "contracts" / "stage0-clojure-components.json").read_text(encoding="utf-8")
        )
        components = {component["id"]: component for component in contract["components"]}
        checks = {item["id"]: item for item in manifest["checks"]}
        expected_counts = {"foundation-reader": 9, "c2-c3": 12, "compiler": 20}
        all_roots: set[str] = set()
        for group, expected_count in expected_counts.items():
            roots = {
                component_id
                for component_id, component in components.items()
                if component["leaf_execution_group"] == group
            }
            self.assertEqual(expected_count, len(roots), group)
            all_roots.update(roots)
            closure = set(roots)
            pending = list(roots)
            while pending:
                component_id = pending.pop()
                for dependency in components[component_id]["direct_source_dependencies"]:
                    if dependency not in closure:
                        closure.add(dependency)
                        pending.append(dependency)
            item = checks[f"stage0-leaf-{group}"]
            actual_sources = {
                path
                for path in item["inputs"]
                if path.startswith("bootstrap/clojure/src/gravity/")
            }
            actual_tests = {
                path
                for path in item["inputs"]
                if path.startswith("bootstrap/clojure/test/gravity/")
                and path.endswith("_test.clj")
            }
            expected_sources = {components[component_id]["source"]["path"] for component_id in closure}
            expected_tests = {components[component_id]["test"]["path"] for component_id in roots}
            self.assertEqual(expected_sources, actual_sources, group)
            self.assertEqual(expected_tests, actual_tests, group)
        self.assertEqual(41, len(all_roots))
        self.assertEqual(
            {
                component_id
                for component_id, component in components.items()
                if component["test"]["lane"] == "bootstrap-free"
            },
            all_roots,
        )

    def test_c2_compatibility_check_batches_exact_qualified_vars_under_host_capacity(self) -> None:
        manifest = verifier.load_manifest(ROOT / "tools" / "development_verification_manifest.json")
        item = next(check for check in manifest["checks"] if check["id"] == "stage0-c2-compatibility")
        qualified = [
            "gravity.bootstrap-compatibility.c2-test/c2-source-identity-compatibility-wrappers-preserve-interposition",
            "gravity.bootstrap-compatibility.c2-test/c2-reader-product-projection-compatibility-wrappers-preserve-interposition",
            "gravity.bootstrap-compatibility.c2-test/c2-reader-diagnostics-compatibility-wrappers-preserve-interposition",
            "gravity.bootstrap-compatibility.c2-test/c2-lexical-validation-compatibility-wrappers-preserve-interposition",
            "gravity.bootstrap-compatibility.c2-test/c2-artifact-identity-load-order-initializes-standard-reader-options",
            "gravity.bootstrap-compatibility.c2-test/c2-artifact-identity-compatibility-wrappers-preserve-interposition",
        ]
        self.assertEqual(
            ["clojure", "-M:dev-test", "--namespace", "gravity.bootstrap-compatibility.c2-test"]
            + [part for name in qualified for part in ("--exact", name)],
            item["command"],
        )
        self.assertEqual("bootstrap-hosted", item["resource_class"])
        self.assertEqual("none", item["authority"])
        self.assertFalse(item["fresh"])
        self.assertIsNone(item["lock"])
        resource = verifier.check_resource_declaration(manifest, item)
        self.assertEqual(verifier.CANONICAL_HEAVY_LOCK, resource["capacity_lock"])
        self.assertEqual(1, resource["class_max_concurrency"])

    def test_c2_compatibility_paths_route_and_invalidate_cache_identity(self) -> None:
        manifest = verifier.load_manifest(ROOT / "tools" / "development_verification_manifest.json")
        item = next(check for check in manifest["checks"] if check["id"] == "stage0-c2-compatibility")
        for changed_path in (
            "bootstrap/clojure/test/gravity/bootstrap_compatibility/c2_test.clj",
            "bootstrap/clojure/test/gravity/development_test_runner.clj",
        ):
            with self.subTest(path=changed_path):
                selection = verifier.select_impacted_checks(
                    manifest, ROOT, changed_paths=[changed_path]
                )
                self.assertIn("stage0-c2-compatibility", selection["selected_ids"])
                self.assertIn("stage0-clojure-suite", selection["selected_ids"])
                self.assertEqual([], selection["unmatched_changes"])

        with tempfile.TemporaryDirectory(prefix="gravity-c2-compat-cache-") as directory:
            temp_root = Path(directory)
            copied = [
                "deps.edn",
                "contracts/project-structure.json",
                "docs/self-hosting-slice-ownership.edn",
                "bootstrap/clojure/src/gravity/bootstrap.clj",
                "bootstrap/clojure/test/gravity/development_test_runner.clj",
                "bootstrap/clojure/test/gravity/bootstrap_compatibility/c2_test.clj",
                "tools/validate_project_structure.py",
            ]
            for relative in copied:
                target = temp_root / relative
                target.parent.mkdir(parents=True, exist_ok=True)
                shutil.copyfile(ROOT / relative, target)
            baseline = verifier.cache_key(manifest, item, temp_root)
            for relative in copied[1:]:
                target = temp_root / relative
                original = target.read_bytes()
                target.write_bytes(original + b"\ncompatibility-cache-change\n")
                try:
                    self.assertNotEqual(
                        baseline, verifier.cache_key(manifest, item, temp_root), relative
                    )
                finally:
                    target.write_bytes(original)

    def test_c3_compatibility_check_batches_exact_qualified_vars_under_host_capacity(self) -> None:
        manifest = verifier.load_manifest(ROOT / "tools" / "development_verification_manifest.json")
        item = next(check for check in manifest["checks"] if check["id"] == "stage0-c3-compatibility")
        qualified = [
            "gravity.bootstrap-compatibility.c3-test/syntax-object-stream-compatibility-wrapper-preserves-arity-and-output",
            "gravity.bootstrap-compatibility.c3-test/c3-origin-chain-compatibility-wrapper-preserves-arity-and-output",
            "gravity.bootstrap-compatibility.c3-test/c3-syntax-evidence-compatibility-wrappers-preserve-output-and-interposition",
            "gravity.bootstrap-compatibility.c3-test/c3-syntax-construction-compatibility-wrappers-preserve-interposition",
            "gravity.bootstrap-compatibility.c3-test/c3-syntax-verification-compatibility-wrappers-preserve-interposition",
            "gravity.bootstrap-compatibility.c3-test/c3-syntax-diagnostics-compatibility-wrappers-preserve-interposition",
            "gravity.bootstrap-compatibility.c3-test/c3-reader-integrity-compatibility-wrappers-preserve-interposition",
            "gravity.bootstrap-compatibility.c3-test/c3-literal-projection-compatibility-wrappers-preserve-interposition",
            "gravity.bootstrap-compatibility.c3-test/c3-artifact-identity-compatibility-wrappers-preserve-interposition",
        ]
        self.assertEqual(
            ["clojure", "-M:dev-test", "--namespace", "gravity.bootstrap-compatibility.c3-test"]
            + [part for name in qualified for part in ("--exact", name)],
            item["command"],
        )
        self.assertEqual("bootstrap-hosted", item["resource_class"])
        self.assertEqual("none", item["authority"])
        self.assertFalse(item["fresh"])
        self.assertIsNone(item["lock"])
        resource = verifier.check_resource_declaration(manifest, item)
        self.assertEqual(verifier.CANONICAL_HEAVY_LOCK, resource["capacity_lock"])
        self.assertEqual(1, resource["class_max_concurrency"])

    def test_c3_compatibility_paths_route_and_invalidate_cache_identity(self) -> None:
        manifest = verifier.load_manifest(ROOT / "tools" / "development_verification_manifest.json")
        item = next(check for check in manifest["checks"] if check["id"] == "stage0-c3-compatibility")
        for changed_path in (
            "bootstrap/clojure/test/gravity/bootstrap_compatibility/c3_test.clj",
            "bootstrap/clojure/test/gravity/development_test_runner.clj",
        ):
            with self.subTest(path=changed_path):
                selection = verifier.select_impacted_checks(
                    manifest, ROOT, changed_paths=[changed_path]
                )
                self.assertIn("stage0-c3-compatibility", selection["selected_ids"])
                self.assertIn("stage0-clojure-suite", selection["selected_ids"])
                self.assertEqual([], selection["unmatched_changes"])

        with tempfile.TemporaryDirectory(prefix="gravity-c3-compat-cache-") as directory:
            temp_root = Path(directory)
            copied = [
                "deps.edn",
                "contracts/project-structure.json",
                "docs/self-hosting-slice-ownership.edn",
                "bootstrap/clojure/src/gravity/bootstrap.clj",
                "bootstrap/clojure/src/gravity/c3_artifact_identity.clj",
                "bootstrap/clojure/src/gravity/c3_literal_projection.clj",
                "bootstrap/clojure/src/gravity/c3_reader_integrity.clj",
                "bootstrap/clojure/src/gravity/c3_syntax_construction.clj",
                "bootstrap/clojure/src/gravity/c3_syntax_diagnostics.clj",
                "bootstrap/clojure/src/gravity/c3_syntax_evidence.clj",
                "bootstrap/clojure/src/gravity/c3_syntax_verification.clj",
                "bootstrap/clojure/src/gravity/syntax_object_stream.clj",
                "bootstrap/clojure/src/gravity/syntax_origin.clj",
                "bootstrap/clojure/test/gravity/development_test_runner.clj",
                "bootstrap/clojure/test/gravity/bootstrap_compatibility/c3_test.clj",
                "tools/validate_project_structure.py",
            ]
            for relative in copied:
                target = temp_root / relative
                target.parent.mkdir(parents=True, exist_ok=True)
                shutil.copyfile(ROOT / relative, target)
            baseline = verifier.cache_key(manifest, item, temp_root)
            for relative in copied[1:]:
                target = temp_root / relative
                original = target.read_bytes()
                target.write_bytes(original + b"\ncompatibility-cache-change\n")
                try:
                    self.assertNotEqual(
                        baseline, verifier.cache_key(manifest, item, temp_root), relative
                    )
                finally:
                    target.write_bytes(original)

    def test_foundation_compatibility_check_is_exact_cacheable_and_routable(self) -> None:
        manifest = verifier.load_manifest(ROOT / "tools" / "development_verification_manifest.json")
        item = next(
            check for check in manifest["checks"]
            if check["id"] == "stage0-foundation-compatibility"
        )
        self.assertEqual(
            [
                "gravity.bootstrap-compatibility.module-analysis-test",
                "gravity.bootstrap-compatibility.core-ast-lowering-test",
            ],
            [
                item["command"][index + 1]
                for index, token in enumerate(item["command"])
                if token == "--namespace"
            ],
        )
        self.assertEqual(
            [
                "gravity.bootstrap-compatibility.module-analysis-test/"
                "module-analysis-compatibility-wrappers-preserve-arglists-output-and-interposition",
                "gravity.bootstrap-compatibility.module-analysis-test/"
                "bootstrap-owned-policy-map-redefs-reach-helpers-and-downstream-checks",
                "gravity.bootstrap-compatibility.core-ast-lowering-test/"
                "core-ast-lowering-compatibility-wrappers-preserve-arglists-output-and-interposition",
            ],
            [
                item["command"][index + 1]
                for index, token in enumerate(item["command"])
                if token == "--exact"
            ],
        )
        self.assertEqual("bootstrap-hosted", item["resource_class"])
        self.assertEqual("none", item["authority"])
        self.assertFalse(item["fresh"])
        self.assertIsNone(item["lock"])
        compatibility_paths = {
            "bootstrap/clojure/test/gravity/bootstrap_compatibility/module_analysis_test.clj",
            "bootstrap/clojure/test/gravity/bootstrap_compatibility/core_ast_lowering_test.clj",
        }
        self.assertTrue(compatibility_paths <= set(item["inputs"]))
        self.assertFalse(any(path.startswith("bootstrap/clojure/fixtures/") for path in item["inputs"]))
        for path in compatibility_paths:
            selection = verifier.select_impacted_checks(manifest, ROOT, changed_paths=[path])
            self.assertIn(item["id"], selection["selected_ids"])
            self.assertIn("stage0-clojure-suite", selection["selected_ids"])
            self.assertEqual([], selection["unmatched_changes"])

    def test_c4_c18_compatibility_forms_are_exactly_preserved_and_absent_centrally(self) -> None:
        central = (ROOT / "bootstrap/clojure/test/gravity/bootstrap_test.clj").read_text()
        observed = 0
        for file_name, expected_forms in C4_C18_COMPATIBILITY_FORMS.items():
            source = (
                ROOT / "bootstrap/clojure/test/gravity/bootstrap_compatibility" / file_name
            ).read_text()
            for name, expected_hash in expected_forms.items():
                with self.subTest(file=file_name, name=name):
                    self.assertNotIn(f"(deftest {name}", central)
                    form = clojure_deftest_source(source, name)
                    self.assertEqual(expected_hash, hashlib.sha256(form.encode()).hexdigest())
                    observed += 1
        self.assertEqual(16, observed)
        self.assertEqual(471, central.count("\n(deftest ") + central.startswith("(deftest "))

    def test_c4_c18_compatibility_batches_are_exact_routable_and_cacheable(self) -> None:
        manifest = verifier.load_manifest(ROOT / "tools" / "development_verification_manifest.json")
        checks = {item["id"]: item for item in manifest["checks"]}
        batches = {
            "stage0-c4-c6-compatibility": range(4, 7),
            "stage0-c7-c10-compatibility": range(7, 11),
            "stage0-c11-c18-compatibility": range(11, 19),
        }
        for check_id, stages in batches.items():
            item = checks[check_id]
            expected_namespaces = [f"gravity.bootstrap-compatibility.c{stage}-test" for stage in stages]
            actual_namespaces = [
                item["command"][index + 1]
                for index, token in enumerate(item["command"])
                if token == "--namespace"
            ]
            self.assertEqual(expected_namespaces, actual_namespaces, check_id)
            expected_exact = [
                f"gravity.bootstrap-compatibility.c{stage}-test/{name}"
                for stage in stages
                for name in C4_C18_COMPATIBILITY_FORMS[f"c{stage}_test.clj"]
            ]
            if check_id == "stage0-c11-c18-compatibility":
                expected_exact.insert(
                    3,
                    "gravity.bootstrap-compatibility.c13-test/"
                    "optimization-lowering-captured-facade-delegates-exactly-once",
                )
            actual_exact = [
                item["command"][index + 1]
                for index, token in enumerate(item["command"])
                if token == "--exact"
            ]
            self.assertEqual(expected_exact, actual_exact, check_id)
            self.assertEqual("bootstrap-hosted", item["resource_class"])
            self.assertEqual("none", item["authority"])
            self.assertFalse(item["fresh"])
            self.assertIsNone(item["lock"])
            self.assertEqual(verifier.CANONICAL_HEAVY_LOCK,
                             verifier.check_resource_declaration(manifest, item)["capacity_lock"])
            for stage in stages:
                path = f"bootstrap/clojure/test/gravity/bootstrap_compatibility/c{stage}_test.clj"
                self.assertIn(path, item["inputs"])
                selection = verifier.select_impacted_checks(manifest, ROOT, changed_paths=[path])
                self.assertIn(check_id, selection["selected_ids"])
                self.assertIn("stage0-clojure-suite", selection["selected_ids"])
                self.assertEqual([], selection["unmatched_changes"])
            for control_path in (
                "contracts/project-structure.json",
                "contracts/stage0-clojure-components.json",
                "docs/self-hosting-slice-ownership.edn",
                "bootstrap/clojure/test/gravity/development_test_runner.clj",
            ):
                self.assertIn(control_path, item["inputs"])
            fixture_paths = {
                f"bootstrap/clojure/fixtures/{relative}"
                for stage in stages
                for relative in re.findall(
                    r'\(fixture\s+"([^"]+)"',
                    (
                        ROOT
                        / f"bootstrap/clojure/test/gravity/bootstrap_compatibility/c{stage}_test.clj"
                    ).read_text(),
                )
            }
            declared_fixtures = {
                path for path in item["inputs"]
                if path.startswith("bootstrap/clojure/fixtures/")
            }
            self.assertEqual(fixture_paths, declared_fixtures, check_id)
            for fixture_path in fixture_paths:
                selection = verifier.select_impacted_checks(
                    manifest, ROOT, changed_paths=[fixture_path]
                )
                self.assertIn(check_id, selection["selected_ids"])
                self.assertIn("stage0-clojure-suite", selection["selected_ids"])
                self.assertEqual([], selection["unmatched_changes"])

    def test_development_runner_catalog_has_exact_20_static_namespaces(self) -> None:
        source = (
            ROOT / "bootstrap/clojure/test/gravity/development_test_runner.clj"
        ).read_text()
        catalog_source = source[
            source.index("(def namespace-catalog") : source.index("(def ^:private usage-text")
        ]
        observed = re.findall(r"\{:namespace '([^\s]+)\s+:path \"([^\"]+)\"\}", catalog_source)
        expected = [
            ("gravity.bootstrap-test", "bootstrap/clojure/test/gravity/bootstrap_test.clj"),
            *[
                (
                    f"gravity.bootstrap-compatibility.c{stage}-test",
                    f"bootstrap/clojure/test/gravity/bootstrap_compatibility/c{stage}_test.clj",
                )
                for stage in range(2, 4)
            ],
            (
                "gravity.bootstrap-compatibility.module-analysis-test",
                "bootstrap/clojure/test/gravity/bootstrap_compatibility/module_analysis_test.clj",
            ),
            (
                "gravity.bootstrap-compatibility.core-ast-lowering-test",
                "bootstrap/clojure/test/gravity/bootstrap_compatibility/core_ast_lowering_test.clj",
            ),
            *[
                (
                    f"gravity.bootstrap-compatibility.c{stage}-test",
                    f"bootstrap/clojure/test/gravity/bootstrap_compatibility/c{stage}_test.clj",
                )
                for stage in range(4, 19)
            ],
        ]
        self.assertEqual(expected, observed)

    def test_c4_c18_compatibility_cache_identity_tracks_test_runner_and_contract(self) -> None:
        manifest = verifier.load_manifest(ROOT / "tools" / "development_verification_manifest.json")
        checks = {item["id"]: item for item in manifest["checks"]}
        for check_id in (
            "stage0-c4-c6-compatibility",
            "stage0-c7-c10-compatibility",
            "stage0-c11-c18-compatibility",
        ):
            item = checks[check_id]
            with tempfile.TemporaryDirectory(prefix="gravity-c4-c18-compat-cache-") as directory:
                temp_root = Path(directory)
                copied = [path for path in item["inputs"] if "*" not in path]
                copied += ["bootstrap/clojure/src/gravity/bootstrap.clj", *item["tool_inputs"]]
                for relative in copied:
                    target = temp_root / relative
                    target.parent.mkdir(parents=True, exist_ok=True)
                    shutil.copyfile(ROOT / relative, target)
                baseline = verifier.cache_key(manifest, item, temp_root)
                fixture_paths = [
                    path for path in item["inputs"]
                    if path.startswith("bootstrap/clojure/fixtures/")
                ]
                self.assertTrue(fixture_paths, check_id)
                for relative in fixture_paths:
                    target = temp_root / relative
                    original = target.read_bytes()
                    target.write_bytes(original + b"\ncompatibility-cache-change\n")
                    try:
                        self.assertNotEqual(
                            baseline, verifier.cache_key(manifest, item, temp_root), relative
                        )
                    finally:
                        target.write_bytes(original)

    def test_dependency_source_edits_route_to_every_consuming_leaf_group(self) -> None:
        manifest = verifier.load_manifest(ROOT / "tools" / "development_verification_manifest.json")
        leaf_ids = {
            "stage0-leaf-foundation-reader",
            "stage0-leaf-c2-c3",
            "stage0-leaf-compiler",
        }
        cases = {
            "bootstrap/clojure/src/gravity/digest.clj": leaf_ids,
            "bootstrap/clojure/src/gravity/reader_primitives.clj": {
                "stage0-leaf-foundation-reader",
                "stage0-leaf-c2-c3",
            },
            "bootstrap/clojure/src/gravity/syntax_origin.clj": {
                "stage0-leaf-foundation-reader",
                "stage0-leaf-c2-c3",
            },
            "bootstrap/clojure/src/gravity/optimization_lowering.clj": {
                "stage0-leaf-compiler",
            },
            "bootstrap/clojure/src/gravity/compiler_verification_shared.clj": {
                "stage0-leaf-compiler",
            },
        }
        for changed_path, expected in cases.items():
            with self.subTest(changed_path=changed_path):
                selection = verifier.select_impacted_checks(
                    manifest,
                    ROOT,
                    changed_paths=[changed_path],
                    lanes=["focused"],
                )
                selected_leaf_ids = set(selection["selected_ids"]) & leaf_ids
                self.assertEqual(expected, selected_leaf_ids)

    def test_bootstrap_source_change_selects_hosted_and_heavy_closure(self) -> None:
        manifest = verifier.load_manifest(ROOT / "tools" / "development_verification_manifest.json")
        selection = verifier.select_impacted_checks(
            manifest,
            ROOT,
            changed_paths=["bootstrap/clojure/src/gravity/bootstrap.clj"],
        )
        self.assertEqual(
            set(selection["selected_ids"]),
            {
                "stage0-hosted-hello",
                "stage0-hosted-hello-qst",
                "stage0-selective-smoke",
                "stage0-c2-compatibility",
                "stage0-c3-compatibility",
                "stage0-foundation-compatibility",
                "stage0-c4-c6-compatibility",
                "stage0-c7-c10-compatibility",
                "stage0-c11-c18-compatibility",
                "stage0-hosted-core-app",
                "stage0-hosted-core-compiled-app",
                "stage0-clojure-suite",
                "stage0-bootstrap-authority",
                "m0-docs",
                "m0-foundation-coverage",
                "m0-contract-traceability",
                "m0-milestone-evidence",
                "m0-terminology",
                "m0-safety-performance",
                "m0-change-control",
                "stage0-project-structure",
                "stage0-orchestrator-unit",
                "stage0-reader",
            },
        )

    def test_unowned_top_level_stage0_test_fails_closed_in_plan_diagnostics(self) -> None:
        manifest = verifier.load_manifest(ROOT / "tools" / "development_verification_manifest.json")
        receipt = verifier.run_verification(
            manifest,
            ROOT,
            changed_paths=["bootstrap/clojure/test/gravity/not_registered_test.clj"],
            dry_run=True,
        )
        self.assertEqual(receipt["status"], "failed")
        self.assertEqual(receipt["checks"], [])
        self.assertEqual(
            receipt["selection"]["unmatched_changes"],
            ["bootstrap/clojure/test/gravity/not_registered_test.clj"],
        )
        self.assertIn("not_registered_test.clj", receipt["error"])

    def test_leaf_checks_are_cacheable_non_authoritative_and_bind_sources_contracts_and_runner(self) -> None:
        manifest = verifier.load_manifest(ROOT / "tools" / "development_verification_manifest.json")
        leaf_checks = [item for item in manifest["checks"] if item["id"].startswith("stage0-leaf-")]
        self.assertEqual(3, len(leaf_checks))
        for item in leaf_checks:
            with self.subTest(check=item["id"]):
                self.assertEqual(item["lane"], "focused")
                self.assertEqual(item["cost"], "cheap")
                self.assertIsNone(item["lock"])
                self.assertFalse(item["exclusive"])
                self.assertEqual(item["authority"], "none")
                self.assertFalse(item["fresh"])
                self.assertEqual(item.get("tool_inputs"), ["tools/validate_project_structure.py"])
                self.assertIn("deps.edn", item["inputs"])
                self.assertIn("contracts/project-structure.json", item["inputs"])
                self.assertIn("contracts/stage0-clojure-components.json", item["inputs"])
                self.assertIn("docs/self-hosting-slice-ownership.edn", item["inputs"])
                self.assertIn(
                    "bootstrap/clojure/test/gravity/bootstrap_free_leaf_test_runner.clj",
                    item["inputs"],
                )
                with tempfile.TemporaryDirectory(prefix="gravity-leaf-cache-identity-") as directory:
                    temp_root = Path(directory)
                    for relative in item["inputs"] + item.get("tool_inputs", []):
                        source = ROOT / relative
                        if not source.is_file():
                            continue
                        target = temp_root / relative
                        target.parent.mkdir(parents=True, exist_ok=True)
                        shutil.copyfile(source, target)
                    baseline = verifier.cache_key(manifest, item, temp_root)
                    for relative in (
                        "bootstrap/clojure/src/gravity/digest.clj",
                        "contracts/stage0-clojure-components.json",
                        "bootstrap/clojure/test/gravity/bootstrap_free_leaf_test_runner.clj",
                    ):
                        target = temp_root / relative
                        if not target.is_file() or relative not in item["inputs"]:
                            continue
                        original = target.read_bytes()
                        target.write_bytes(original + b"\ncache-identity-change\n")
                        try:
                            self.assertNotEqual(baseline, verifier.cache_key(manifest, item, temp_root))
                        finally:
                            target.write_bytes(original)

    def test_all_canonical_heavy_lock_declarations_remain_unchanged(self) -> None:
        manifest = verifier.load_manifest(ROOT / "tools" / "development_verification_manifest.json")
        expected = {
            "stage0-clojure-suite": {
                "lock": "/tmp/gravity-sh07-heavy.lock",
                "exclusive": True,
                "fresh": True,
                "authority": "none",
                "cost": "heavy",
                "resource_class": "memory-heavy",
            },
            "stage0-bootstrap-authority": {
                "lock": "/tmp/gravity-sh07-heavy.lock",
                "exclusive": True,
                "fresh": True,
                "authority": "none",
                "cost": "heavy",
                "resource_class": "memory-heavy",
            },
        }
        for check_id, fields in expected.items():
            with self.subTest(check=check_id):
                item = next(entry for entry in manifest["checks"] if entry["id"] == check_id)
                self.assertEqual({key: item[key] for key in fields}, fields)
                self.assertIsNone(verifier._batch_capacity_lock(manifest, [check_id]))

    def test_every_canonical_clojure_component_path_is_owned_by_a_check(self) -> None:
        manifest = verifier.load_manifest(ROOT / "tools" / "development_verification_manifest.json")
        contract = json.loads(
            (ROOT / "contracts" / "stage0-clojure-components.json").read_text(encoding="utf-8")
        )
        checks = manifest["checks"]
        for component in contract["components"]:
            paths = [component["source"]["path"], component["test"]["path"]]
            for path in paths:
                with self.subTest(path=path):
                    self.assertTrue(
                        any(
                            any(
                                verifier._matches_change(declaration, path)
                                for declaration in list(item.get("inputs", []))
                                + list(item.get("tool_inputs", []))
                            )
                            for item in checks
                        ),
                        path,
                    )

    def test_real_manifest_explicit_reader_check_does_not_expand_to_downstream_heavy_checks(self) -> None:
        manifest = verifier.load_manifest(ROOT / "tools" / "development_verification_manifest.json")
        selection = verifier.select_impacted_checks(
            manifest,
            ROOT,
            requested_ids=["stage0-reader"],
        )
        self.assertEqual(selection["selection_mode"], "explicit-check")
        self.assertEqual(selection["selected_ids"], ["stage0-reader"])
        self.assertNotIn("stage0-clojure-suite", selection["selected_ids"])
        self.assertNotIn("stage0-bootstrap-authority", selection["selected_ids"])

    def test_declared_resource_lock_is_host_wide_and_non_blocking(self) -> None:
        with verifier._process_lock("unit-test-heavy-resource"):
            with self.assertRaises(verifier.LockUnavailable):
                with verifier._process_lock("unit-test-heavy-resource"):
                    self.fail("a second owner must not enter the shared lock")

    def test_cache_writer_uses_lock_and_merges_existing_entries(self) -> None:
        command = [sys.executable, "-c", "import sys; sys.exit(0)"]
        with tempfile.TemporaryDirectory(prefix="gravity-verify-cache-merge-") as directory:
            root = Path(directory)
            (root / "input.txt").write_text("stable\n", encoding="ascii")
            cache = root / "cache.json"
            verifier._write_json(
                cache,
                {"schema_version": verifier.SCHEMA_VERSION, "checks": {"other": {"status": "passed"}}},
            )
            manifest = manifest_for(check("known", command))
            receipt = verifier.run_verification(manifest, root, all_checks=True, cache_path=cache)
            self.assertEqual(receipt["status"], "passed")
            merged = verifier.load_cache(cache)
            self.assertIn("other", merged["checks"])
            self.assertIn("known", merged["checks"])
            self.assertTrue(verifier._cache_lock_path(cache).exists())

    def test_failed_prerequisite_blocks_dependent_check(self) -> None:
        failing = [sys.executable, "-c", "import sys; sys.exit(7)"]
        passing = [sys.executable, "-c", "import sys; sys.exit(0)"]
        with tempfile.TemporaryDirectory(prefix="gravity-verify-failure-") as directory:
            root = Path(directory)
            (root / "input.txt").write_text("stable\n", encoding="ascii")
            manifest = manifest_for(
                check("fail", failing),
                check("dependent", passing, depends_on=["fail"], timeout_seconds=4),
                check("independent", passing),
            )
            receipt = verifier.run_verification(manifest, root, all_checks=True, fail_fast=False)
            statuses = {item["id"]: item for item in receipt["checks"]}
            self.assertEqual(statuses["fail"]["status"], "failed")
            self.assertEqual(statuses["dependent"]["status"], "blocked")
            self.assertEqual(statuses["dependent"]["reason"], "failed-prerequisite")
            self.assertEqual(statuses["dependent"]["timeout_seconds"], 4.0)
            self.assertEqual(statuses["independent"]["status"], "passed")

    def test_default_manifest_declares_stage0_lanes_and_heavy_lock(self) -> None:
        manifest = verifier.load_manifest(ROOT / "tools" / "development_verification_manifest.json")
        self.assertEqual(manifest["scope"]["stage"], "stage0")
        self.assertEqual(set(manifest["lanes"]), set(verifier.LANES))
        heavy_candidates = [item for item in manifest["checks"] if item["lane"] == "heavy-candidate"]
        self.assertTrue(heavy_candidates)
        self.assertTrue(all(item["cost"] == "heavy" and item["lock"] for item in heavy_candidates))
        self.assertTrue(all(item.get("fresh") is True for item in heavy_candidates))
        self.assertTrue(all(item.get("authority") == "none" for item in heavy_candidates))
        leaf_checks = [item for item in manifest["checks"] if item["id"].startswith("stage0-leaf-")]
        self.assertEqual(3, len(leaf_checks))
        self.assertTrue(all(item.get("fresh") is False for item in leaf_checks))
        self.assertTrue(all(item.get("authority") == "none" for item in leaf_checks))
        self.assertTrue(all(item.get("lock") is None and not item.get("exclusive") for item in leaf_checks))
        clojure_checks = [
            item
            for item in manifest["checks"]
            if item["command"]
            and item["command"][0] == "clojure"
            and not item["id"].startswith("stage0-leaf-")
            and item["id"] not in {
                "stage0-c2-compatibility",
                "stage0-c3-compatibility",
                "stage0-foundation-compatibility",
                "stage0-c4-c6-compatibility",
                "stage0-c7-c10-compatibility",
                "stage0-c11-c18-compatibility",
            }
        ]
        self.assertTrue(clojure_checks)
        self.assertTrue(all(item.get("fresh") is True for item in clojure_checks))
        self.assertTrue(all("bin/gravity" in item["inputs"] for item in clojure_checks))
        self.assertTrue(all("bootstrap/gravity/**" in item["inputs"] for item in clojure_checks))
        full_suite = next(item for item in manifest["checks"] if item["id"] == "stage0-clojure-suite")
        self.assertIn("bin/gravity-bootstrap", full_suite["inputs"])
        self.assertIn("bootstrap/clojure/src/gravity/*.clj", full_suite["inputs"])
        self.assertIn("bootstrap/clojure/test/gravity/module_analysis_test.clj", full_suite["inputs"])
        self.assertIn("bootstrap/clojure/test/gravity/core_ast_lowering_test.clj", full_suite["inputs"])
        self.assertIn(
            "bootstrap/clojure/test/gravity/bootstrap_compatibility/module_analysis_test.clj",
            full_suite["inputs"],
        )
        self.assertIn(
            "bootstrap/clojure/test/gravity/bootstrap_compatibility/core_ast_lowering_test.clj",
            full_suite["inputs"],
        )
        self.assertIn("bootstrap/clojure/test/gravity/bootstrap_free_leaf_test_runner.clj", full_suite["inputs"])
        self.assertIn(
            "bootstrap/clojure/test/gravity/self_hosting/sh01_stage0_leaf_test_runner_test.clj",
            full_suite["inputs"],
        )
        self.assertIn("contracts/stage0-clojure-components.json", full_suite["inputs"])


if __name__ == "__main__":
    unittest.main()
