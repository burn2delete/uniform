#!/usr/bin/env python3
"""Focused tests for the Stage 0 development verification orchestrator."""

from __future__ import annotations

import json
import hashlib
import fcntl
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
    def test_direct_script_import_graph_can_sample_process_tree_rss(self) -> None:
        code = (
            "import os,sys;"
            f"sys.path.insert(0,{str(TOOLS)!r});"
            "import verify_development as v;"
            "r=v._stage3_process_tree_rss(os.getpid());"
            "print(r[0],r[3])"
        )
        result = subprocess.run(
            [sys.executable, "-I", "-c", code],
            cwd=ROOT,
            capture_output=True,
            text=True,
            check=False,
            timeout=10,
        )
        self.assertEqual(result.returncode, 0, result.stderr)
        peak, limitation = result.stdout.strip().split(" ", 1)
        self.assertGreater(int(peak), 0)
        self.assertNotIn("unavailable", limitation)
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

        for value in (True, 0, 17):
            invalid_process_reservation = json.loads(json.dumps(baseline))
            invalid_process_reservation["checks"][0]["reserved_processes"] = value
            mutations.append(
                (
                    invalid_process_reservation,
                    "reserved_processes must be a positive integer within the aggregate process limit",
                )
            )

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

    def test_production_process_reservations_are_exact_and_proportional(self) -> None:
        manifest = verifier.load_manifest(TOOLS / "development_verification_manifest.json")
        self.assertEqual(
            {
                class_name: manifest["resource_policy"]["classes"][class_name]["default_processes"]
                for class_name in ("python-cheap", "bootstrap-hosted")
            },
            {"python-cheap": 1, "bootstrap-hosted": 2},
        )
        self.assertEqual(
            {
                item["id"]: item["reserved_processes"]
                for item in manifest["checks"]
                if "reserved_processes" in item
            },
            {
                "stage0-orchestrator-unit": 6,
                "stage1-sh01-unit": 4,
                "stage2-authority-admission-unit": 4,
            },
        )

        mutations = []
        missing = json.loads(json.dumps(manifest))
        next(item for item in missing["checks"] if item["id"] == "stage0-orchestrator-unit").pop(
            "reserved_processes"
        )
        mutations.append(missing)
        wrong = json.loads(json.dumps(manifest))
        next(item for item in wrong["checks"] if item["id"] == "stage2-authority-admission-unit")[
            "reserved_processes"
        ] = 3
        mutations.append(wrong)
        extra = json.loads(json.dumps(manifest))
        next(item for item in extra["checks"] if item["id"] == "m0-docs")[
            "reserved_processes"
        ] = 1
        mutations.append(extra)
        for mutated in mutations:
            with self.assertRaisesRegex(
                verifier.ManifestError,
                "production reserved_processes overrides must equal",
            ):
                verifier.validate_manifest(mutated, require_production_contracts=True)

    def test_manifest_requires_explicit_daemonization_forbidden_policy(self) -> None:
        item = check("policy", [sys.executable, "-c", "pass"])
        item.pop("daemonization")
        with self.assertRaisesRegex(verifier.ManifestError, "daemonization='forbidden'"):
            verifier.validate_manifest(manifest_for(item))

    def test_automatic_is_optional_boolean_and_manual_checks_are_explicit_only(self) -> None:
        owner = check("owner", [sys.executable, "-c", "pass"], inputs=["src.txt"])
        manual = check(
            "manual-proof",
            [sys.executable, "-c", "pass"],
            inputs=["proof.edn"],
            depends_on=["owner"],
        )
        manual["automatic"] = False
        manifest = manifest_for(owner, manual)
        verifier.validate_manifest(manifest)

        changed_owner = verifier.select_impacted_checks(
            manifest, Path("/tmp/project"), changed_paths=["src.txt"]
        )
        self.assertIn("owner", changed_owner["selected_ids"])
        self.assertNotIn("manual-proof", changed_owner["selected_ids"])
        self.assertEqual(changed_owner["unmatched_changes"], [])

        changed_manual = verifier.select_impacted_checks(
            manifest, Path("/tmp/project"), changed_paths=["proof.edn"]
        )
        self.assertEqual(changed_manual["selected_ids"], [])
        self.assertEqual(changed_manual["unmatched_changes"], ["proof.edn"])

        explicit = verifier.select_impacted_checks(
            manifest, Path("/tmp/project"), requested_ids=["manual-proof"]
        )
        self.assertEqual(explicit["selected_ids"], ["owner", "manual-proof"])
        all_checks = verifier.select_impacted_checks(manifest, Path("/tmp/project"), all_checks=True)
        self.assertEqual(all_checks["selected_ids"], ["owner", "manual-proof"])

        manual["automatic"] = "false"
        with self.assertRaisesRegex(verifier.ManifestError, "automatic must be boolean"):
            verifier.validate_manifest(manifest)
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

    def test_parallel_exact_file_checks_ignore_unrelated_sibling_mutation(self) -> None:
        """Exact-file vnode watches must not inherit their parent's events."""

        with tempfile.TemporaryDirectory(prefix="gravity-verify-parallel-exact-") as directory:
            root = Path(directory)
            shared = root / "shared"
            shared.mkdir()
            (shared / "input.txt").write_text("stable\n", encoding="ascii")
            writer = [
                sys.executable,
                "-c",
                (
                    "from pathlib import Path; import time; "
                    "Path('shared/unrelated-output.txt').write_text('generated\\n', encoding='ascii'); "
                    "time.sleep(0.2)"
                ),
            ]
            reader = [
                sys.executable,
                "-c",
                (
                    "from pathlib import Path; import time; "
                    "Path('shared/input.txt').read_text(encoding='ascii'); "
                    "time.sleep(0.2)"
                ),
            ]
            manifest = manifest_for(
                check("exact-reader", reader, inputs=["shared/input.txt"]),
                check("unrelated-writer", writer, inputs=["shared/input.txt"]),
            )

            receipt = verifier.run_verification(manifest, root, all_checks=True, jobs=2)

            self.assertEqual(receipt["status"], "passed")
            self.assertEqual(
                {record["id"]: record["status"] for record in receipt["checks"]},
                {"exact-reader": "passed", "unrelated-writer": "passed"},
            )
            self.assertTrue((shared / "unrelated-output.txt").is_file())
            for record in receipt["checks"]:
                self.assertEqual(record["mutation_monitor"]["observations"], [])

    def test_exact_file_immediate_parent_swap_restore_is_failed_and_not_cached(self) -> None:
        with tempfile.TemporaryDirectory(prefix="gravity-verify-exact-parent-swap-") as directory:
            root = Path(directory)
            parent = root / "parent"
            parent.mkdir()
            (parent / "input.txt").write_text("A\n", encoding="ascii")
            command = [
                sys.executable,
                "-c",
                (
                    "from pathlib import Path; import shutil, time; "
                    "parent=Path('parent'); saved=Path('parent.saved'); "
                    "parent.rename(saved); parent.mkdir(); "
                    "(parent/'input.txt').write_text('B\\n', encoding='ascii'); "
                    "assert (parent/'input.txt').read_text(encoding='ascii') == 'B\\n'; "
                    "shutil.rmtree(parent); saved.rename(parent); time.sleep(0.1)"
                ),
            ]
            manifest = manifest_for(check("immediate-parent-swap", command, inputs=["parent/input.txt"]))
            cache = root / "cache.json"

            receipt = verifier.run_verification(manifest, root, all_checks=True, cache_path=cache)
            record = receipt["checks"][0]

            self.assertEqual(receipt["status"], "failed")
            self.assertEqual(record["reason"], "stale-input")
            self.assertTrue(record["mutation_observed"])
            self.assertEqual((parent / "input.txt").read_text(encoding="ascii"), "A\n")
            self.assertNotIn("immediate-parent-swap", verifier.load_cache(cache)["checks"])

    def test_exact_file_intermediate_directory_swap_restore_is_failed_and_not_cached(self) -> None:
        with tempfile.TemporaryDirectory(prefix="gravity-verify-exact-intermediate-swap-") as directory:
            root = Path(directory)
            input_path = root / "outer" / "inner" / "input.txt"
            input_path.parent.mkdir(parents=True)
            input_path.write_text("A\n", encoding="ascii")
            command = [
                sys.executable,
                "-c",
                (
                    "from pathlib import Path; import shutil, time; "
                    "outer=Path('outer'); saved=Path('outer.saved'); "
                    "outer.rename(saved); (outer/'inner').mkdir(parents=True); "
                    "(outer/'inner/input.txt').write_text('B\\n', encoding='ascii'); "
                    "assert (outer/'inner/input.txt').read_text(encoding='ascii') == 'B\\n'; "
                    "shutil.rmtree(outer); saved.rename(outer); time.sleep(0.1)"
                ),
            ]
            manifest = manifest_for(
                check("intermediate-directory-swap", command, inputs=["outer/inner/input.txt"])
            )
            cache = root / "cache.json"

            receipt = verifier.run_verification(manifest, root, all_checks=True, cache_path=cache)
            record = receipt["checks"][0]

            self.assertEqual(receipt["status"], "failed")
            self.assertEqual(record["reason"], "stale-input")
            self.assertTrue(record["mutation_observed"])
            self.assertEqual(input_path.read_text(encoding="ascii"), "A\n")
            self.assertNotIn("intermediate-directory-swap", verifier.load_cache(cache)["checks"])

    def test_missing_exact_file_create_delete_is_failed_and_not_cached(self) -> None:
        with tempfile.TemporaryDirectory(prefix="gravity-verify-missing-exact-") as directory:
            root = Path(directory)
            (root / "parent").mkdir()
            command = [
                sys.executable,
                "-c",
                (
                    "from pathlib import Path; import time; "
                    "path=Path('parent/missing.txt'); path.write_text('B\\n', encoding='ascii'); "
                    "assert path.read_text(encoding='ascii') == 'B\\n'; "
                    "path.unlink(); time.sleep(0.1)"
                ),
            ]
            manifest = manifest_for(check("missing-exact", command, inputs=["parent/missing.txt"]))
            cache = root / "cache.json"

            receipt = verifier.run_verification(manifest, root, all_checks=True, cache_path=cache)
            record = receipt["checks"][0]

            self.assertEqual(receipt["status"], "failed")
            self.assertEqual(record["reason"], "stale-input")
            self.assertTrue(record["mutation_observed"])
            self.assertFalse((root / "parent" / "missing.txt").exists())
            self.assertNotIn("missing-exact", verifier.load_cache(cache)["checks"])

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
                "bootstrap/clojure/src/gravity/*.clj",
                "bootstrap/clojure/test/gravity/*_test.clj",
                "tools/validate_project_structure.py",
            },
        )

    def test_control_plane_contract_tests_are_executable_cacheable_and_routable(self) -> None:
        manifest = verifier.load_manifest(ROOT / "tools" / "development_verification_manifest.json")
        checks = {item["id"]: item for item in manifest["checks"]}
        cases = {
            "tools/tests/test_validate_project_structure.py": {
                "id": "stage0-project-structure-unit",
                "command": [
                    "python3", "-m", "unittest",
                    "tools.tests.test_validate_project_structure", "-v",
                ],
                "resource_class": "python-cheap",
            },
            "bootstrap/clojure/test/gravity/self_hosting/sh01_ownership_test.clj": {
                "id": "stage0-sh01-ownership-control",
                "command": [
                    "clojure", "-J-Xmx256m", "-M:test", "--namespace",
                    "gravity.self-hosting.sh01-ownership-test",
                ],
                "resource_class": "leaf-jvm",
            },
            "bootstrap/clojure/test/gravity/self_hosting/sh01_development_test_runner_test.clj": {
                "id": "stage0-sh01-development-runner-control",
                "command": [
                    "clojure", "-J-Xmx256m", "-M:test", "--namespace",
                    "gravity.self-hosting.sh01-development-test-runner-test",
                ],
                "resource_class": "leaf-jvm",
            },
            "bootstrap/clojure/test/gravity/self_hosting/sh01_stage0_leaf_test_runner_test.clj": {
                "id": "stage0-sh01-leaf-runner-control",
                "command": [
                    "clojure", "-J-Xmx256m", "-M:test", "--namespace",
                    "gravity.self-hosting.sh01-stage0-leaf-test-runner-test",
                ],
                "resource_class": "leaf-jvm",
            },
        }
        for changed_path, expected in cases.items():
            with self.subTest(path=changed_path):
                item = checks[expected["id"]]
                self.assertEqual(expected["command"], item["command"])
                self.assertEqual(expected["resource_class"], item["resource_class"])
                self.assertEqual("none", item["authority"])
                self.assertFalse(item["fresh"])
                self.assertIsNone(item["lock"])
                self.assertFalse(item["exclusive"])
                self.assertIn(changed_path, item["inputs"])
                selection = verifier.select_impacted_checks(
                    manifest, ROOT, changed_paths=[changed_path]
                )
                self.assertEqual([], selection["unmatched_changes"])
                self.assertIn(expected["id"], selection["selected_ids"])
                with tempfile.TemporaryDirectory(prefix="gravity-control-plane-cache-") as directory:
                    temp_root = Path(directory)
                    target = temp_root / changed_path
                    target.parent.mkdir(parents=True, exist_ok=True)
                    shutil.copyfile(ROOT / changed_path, target)
                    baseline = verifier.cache_key(manifest, item, temp_root)
                    original = target.read_bytes()
                    target.write_bytes(original + b"\ncontrol-plane-cache-change\n")
                    self.assertNotEqual(baseline, verifier.cache_key(manifest, item, temp_root))

    def test_filesystem_enumerating_control_checks_bind_stage0_source_and_test_globs(self) -> None:
        manifest = verifier.load_manifest(ROOT / "tools" / "development_verification_manifest.json")
        checks = {item["id"]: item for item in manifest["checks"]}
        for check_id in (
            "stage0-project-structure",
            "stage0-project-structure-unit",
            "stage0-sh01-ownership-control",
        ):
            with self.subTest(check_id=check_id):
                item = checks[check_id]
                self.assertIn("bootstrap/clojure/src/gravity/*.clj", item["inputs"])
                self.assertIn("bootstrap/clojure/test/gravity/*_test.clj", item["inputs"])
                with tempfile.TemporaryDirectory(prefix="gravity-filesystem-cache-") as directory:
                    temp_root = Path(directory)
                    source = temp_root / "bootstrap/clojure/src/gravity/cache_probe.clj"
                    source.parent.mkdir(parents=True, exist_ok=True)
                    baseline = verifier.cache_key(manifest, item, temp_root)
                    source.write_text("(ns gravity.cache-probe)\n", encoding="ascii")
                    self.assertNotEqual(baseline, verifier.cache_key(manifest, item, temp_root))

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
            "bootstrap/clojure/src/gravity/pass_cache.clj": {"stage0-leaf-compiler"},
            "bootstrap/clojure/test/gravity/pass_cache_test.clj": {"stage0-leaf-compiler"},
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
        expected_counts = {"foundation-reader": 9, "c2-c3": 12, "compiler": 26}
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
        self.assertEqual(47, len(all_roots))
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
            ["clojure", "-M:dev-test",
             "--namespace", "gravity.bootstrap-compatibility.c2-test"]
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

    def test_c2_pass_cache_integration_is_a_serialized_heavy_gate(self) -> None:
        manifest = verifier.load_manifest(ROOT / "tools" / "development_verification_manifest.json")
        item = next(
            check for check in manifest["checks"]
            if check["id"] == "stage0-c2-pass-cache-integration"
        )
        qualified = [
            "gravity.c2-pass-cache-test/canonical-semantic-key-is-bounded-type-sensitive-and-path-scoped",
            "gravity.c2-pass-cache-test/bounded-source-snapshot-rejects-size-links-and-traversal",
            "gravity.c2-pass-cache-test/generic-v2-adapter-preserves-c2-results-and-ignores-v1",
            "gravity.c2-pass-cache-test/generic-v2-adapter-binds-current-c2-producer-policy",
            "gravity.c2-pass-cache-test/generic-v2-adapter-corruption-is-rejected-and-not-replaced",
            "gravity.c2-pass-cache-test/generic-v2-adapter-direct-store-mints-a-current-receipt",
            "gravity.c2-pass-cache-test/generic-v2-adapter-same-key-concurrency-executes-one-c2-producer",
            "gravity.c2-pass-cache-test/leaf-contract-is-explicitly-local-and-nonauthoritative",
            "gravity.c2-pass-cache-test/opt-in-bootstrap-integration-reuses-without-reader-execution",
        ]
        self.assertEqual(
            ["clojure", "-M:dev-test", "--namespace", "gravity.c2-pass-cache-test"]
            + [part for name in qualified for part in ("--exact", name)],
            item["command"],
        )
        self.assertEqual("heavy-candidate", item["lane"])
        self.assertEqual("memory-heavy", item["resource_class"])
        self.assertEqual(verifier.CANONICAL_HEAVY_LOCK, item["lock"])
        self.assertTrue(item["exclusive"])
        self.assertTrue(item["fresh"])
        self.assertEqual("none", item["authority"])
        self.assertEqual(1800, item["timeout_seconds"])
        for changed_path in (
            "bootstrap/clojure/src/gravity/c2_pass_cache.clj",
            "bootstrap/clojure/src/gravity/pass_cache.clj",
            "bootstrap/clojure/test/gravity/c2_pass_cache_test.clj",
        ):
            selection = verifier.select_impacted_checks(
                manifest, ROOT, changed_paths=[changed_path]
            )
            self.assertIn(item["id"], selection["selected_ids"])
            self.assertEqual([], selection["unmatched_changes"])

    def test_c2_pass_cache_envelope_profile_is_a_serialized_heavy_gate(self) -> None:
        manifest = verifier.load_manifest(ROOT / "tools" / "development_verification_manifest.json")
        item = next(
            check for check in manifest["checks"]
            if check["id"] == "stage0-c2-pass-cache-envelope-profile"
        )
        self.assertEqual(
            ["clojure", "-M:dev-test",
             "--namespace", "gravity.c2-pass-cache-test",
             "--exact",
             "gravity.c2-pass-cache-test/generic-v2-adapter-preserves-opaque-c2-size-and-depth-profile"],
            item["command"],
        )
        self.assertEqual("heavy-candidate", item["lane"])
        self.assertEqual("memory-heavy", item["resource_class"])
        self.assertEqual(verifier.CANONICAL_HEAVY_LOCK, item["lock"])
        self.assertTrue(item["exclusive"])
        self.assertTrue(item["fresh"])
        self.assertEqual("none", item["authority"])
        self.assertEqual(1800, item["timeout_seconds"])
        for changed_path in (
            "bootstrap/clojure/src/gravity/c2_pass_cache.clj",
            "bootstrap/clojure/src/gravity/pass_cache.clj",
            "bootstrap/clojure/test/gravity/c2_pass_cache_test.clj",
        ):
            selection = verifier.select_impacted_checks(
                manifest, ROOT, changed_paths=[changed_path]
            )
            self.assertIn(item["id"], selection["selected_ids"])
            self.assertEqual([], selection["unmatched_changes"])

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

    def test_profile_capability_compatibility_check_is_exact_cacheable_and_routable(self) -> None:
        manifest = verifier.load_manifest(ROOT / "tools" / "development_verification_manifest.json")
        item = next(
            check for check in manifest["checks"]
            if check["id"] == "stage0-profile-capability-compatibility"
        )
        expected = [
            "gravity.bootstrap-compatibility.profile-validation-test/profile-facades-match-head-4921fbc-reference-table",
            "gravity.bootstrap-compatibility.profile-validation-test/profile-head-reference-policy-denial-matrix-and-dynamic-seams",
            "gravity.bootstrap-compatibility.profile-validation-test/profile-downstream-caller-artifacts-retain-head-shape",
            "gravity.bootstrap-compatibility.profile-validation-test/profile-facades-preserve-public-arglists-and-exact-leaf-parity",
            "gravity.bootstrap-compatibility.profile-validation-test/profile-policy-map-redefs-reach-the-leaf-through-the-central-seam",
            "gravity.bootstrap-compatibility.profile-validation-test/profile-registry-function-seams-match-head-4921fbc-ownership",
            "gravity.bootstrap-compatibility.profile-validation-test/profile-validation-facade-preserves-central-diagnostics-and-target-gates",
            "gravity.bootstrap-compatibility.profile-validation-test/profile-captured-original-interposition-is-one-shot",
            "gravity.bootstrap-compatibility.profile-validation-test/profile-leaf-operation-interposition-is-observable-through-facade",
            "gravity.bootstrap-compatibility.capability-validation-test/capability-facades-preserve-arglists-and-explicit-pass-parity",
            "gravity.bootstrap-compatibility.capability-validation-test/capability-final-authority-narrows-trust-without-rewriting-legacy-row",
            "gravity.bootstrap-compatibility.capability-validation-test/capability-policy-and-provider-seams-remain-interposable",
            "gravity.bootstrap-compatibility.capability-validation-test/capability-diagnostic-policy-scalar-reaches-leaf-pass-contract",
            "gravity.bootstrap-compatibility.capability-validation-test/capability-diagnostics-preserve-source-context-and-stable-ids",
            "gravity.bootstrap-compatibility.capability-validation-test/capability-provider-name-matches-head-4921fbc-reference-table",
            "gravity.bootstrap-compatibility.capability-validation-test/capability-captured-original-provider-interposition-is-one-shot",
        ]
        for namespace, relative in (
            (
                "gravity.bootstrap-compatibility.profile-validation-test",
                "bootstrap/clojure/test/gravity/bootstrap_compatibility/profile_validation_test.clj",
            ),
            (
                "gravity.bootstrap-compatibility.capability-validation-test",
                "bootstrap/clojure/test/gravity/bootstrap_compatibility/capability_validation_test.clj",
            ),
        ):
            deftests = re.findall(
                r"^\(deftest\s+([^\s\)]+)",
                (ROOT / relative).read_text(encoding="utf-8"),
                flags=re.MULTILINE,
            )
            self.assertEqual(
                [f"{namespace}/{name}" for name in deftests],
                [selector for selector in expected if selector.startswith(f"{namespace}/")],
            )
        runner_source = (
            ROOT / "bootstrap/clojure/test/gravity/development_test_runner.clj"
        ).read_text(encoding="utf-8")
        self.assertEqual(
            expected,
            re.findall(
                r'"(gravity\.bootstrap-compatibility\.(?:profile|capability)-validation-test/[^\"]+)"',
                runner_source,
            ),
        )
        self.assertEqual(
            expected,
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
        for path in (
            "bootstrap/clojure/test/gravity/bootstrap_compatibility/profile_validation_test.clj",
            "bootstrap/clojure/test/gravity/bootstrap_compatibility/capability_validation_test.clj",
            "bootstrap/clojure/src/gravity/profile_validation.clj",
            "bootstrap/clojure/src/gravity/capability_validation.clj",
            "bootstrap/clojure/src/gravity/bootstrap.clj",
        ):
            self.assertIn(path, item["inputs"])
            selection = verifier.select_impacted_checks(
                manifest, ROOT, changed_paths=[path]
            )
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

    def test_development_runner_catalog_has_exact_23_static_namespaces(self) -> None:
        source = (
            ROOT / "bootstrap/clojure/test/gravity/development_test_runner.clj"
        ).read_text()
        catalog_source = source[
            source.index("(def namespace-catalog") : source.index("(def ^:private usage-text")
        ]
        observed = re.findall(r"\{:namespace '([^\s]+)\s+:path \"([^\"]+)\"", catalog_source)
        expected = [
            ("gravity.bootstrap-test", "bootstrap/clojure/test/gravity/bootstrap_test.clj"),
            (
                "gravity.bootstrap-compatibility.c2-test",
                "bootstrap/clojure/test/gravity/bootstrap_compatibility/c2_test.clj",
            ),
            (
                "gravity.c2-pass-cache-test",
                "bootstrap/clojure/test/gravity/c2_pass_cache_test.clj",
            ),
            (
                "gravity.bootstrap-compatibility.c3-test",
                "bootstrap/clojure/test/gravity/bootstrap_compatibility/c3_test.clj",
            ),
            (
                "gravity.bootstrap-compatibility.module-analysis-test",
                "bootstrap/clojure/test/gravity/bootstrap_compatibility/module_analysis_test.clj",
            ),
            (
                "gravity.bootstrap-compatibility.core-ast-lowering-test",
                "bootstrap/clojure/test/gravity/bootstrap_compatibility/core_ast_lowering_test.clj",
            ),
            (
                "gravity.bootstrap-compatibility.profile-validation-test",
                "bootstrap/clojure/test/gravity/bootstrap_compatibility/profile_validation_test.clj",
            ),
            (
                "gravity.bootstrap-compatibility.capability-validation-test",
                "bootstrap/clojure/test/gravity/bootstrap_compatibility/capability_validation_test.clj",
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
                "stage0-c2-pass-cache-integration",
                "stage0-c2-pass-cache-envelope-profile",
                "stage0-c3-compatibility",
                "stage0-foundation-compatibility",
                "stage0-c4-c6-compatibility",
                "stage0-c7-c10-compatibility",
                "stage0-c11-c18-compatibility",
                "stage0-profile-capability-compatibility",
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
                "stage0-project-structure-unit",
                "stage0-orchestrator-unit",
                "stage0-reader",
                "stage0-sh01-ownership-control",
                "stage0-sh01-development-runner-control",
                "stage0-sh01-leaf-runner-control",
                "stage0-p15-native-runtime-provider-contract-prerequisite",
                "stage0-p15-native-runtime-provider-packet-binding-prerequisite",
                "stage0-p15-native-plan-specialization-prerequisite",
                "stage0-project-structure-extraction",
                "stage0-project-structure-runner-unit",
                "stage1-sh01-unit",
                "stage2-authority-admission-unit",
                "stage3-authoritative-ho-authenticated",
                "stage3-authoritative-ho-pure",
                "stage3-coverage-census-contract",
                "stage3-fragment-size-preflight",
                "stage3-primitive-bool-authenticated",
                "stage3-primitive-pure",
                "stage3-public-c7-check",
                "stage3-recursive-authenticated",
                "stage3-recursive-pure",
                "stage3-runner-unit",
                "stage3-source-control-form-arity",
                "stage3-source-plan-contract",
                "stage4-c8-source-structural",
                "stage4-public-c8",
                "stage4-sh09-adapter",
                "stage5-c9-kernel",
                "stage5-c9-source-structural",
                "stage5-public-c9",
                "stage5-sh10-c8-adapter",
                "stage6-c10-kernel",
                "stage6-c10-source-structural",
                "stage6-public-c10",
                "stage6-sh11-c9-safety-adapter",
                "stage7-c11-source-structural",
                "stage7-public-c11",
                "stage7-sh12-c10-mir-adapter",
                "stage8-c12-source-shape",
                "stage8-public-c12",
                "stage8-sh13-c11-domain-evidence",
                "stage9-c13-source-shape",
                "stage9-sh16-c13-evidence-boundary",
            },
        )

    def test_unowned_top_level_stage0_test_routes_to_filesystem_validators(self) -> None:
        manifest = verifier.load_manifest(ROOT / "tools" / "development_verification_manifest.json")
        receipt = verifier.run_verification(
            manifest,
            ROOT,
            changed_paths=["bootstrap/clojure/test/gravity/not_registered_test.clj"],
            dry_run=True,
        )
        self.assertEqual(receipt["status"], "planned")
        self.assertEqual([], receipt["selection"]["unmatched_changes"])
        selected = set(receipt["selection"]["selected_ids"])
        self.assertIn("stage0-project-structure", selected)
        self.assertIn("stage0-project-structure-unit", selected)
        self.assertIn("stage0-sh01-ownership-control", selected)
        self.assertIn("stage0-sh01-leaf-runner-control", selected)

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

    def test_real_manifest_stage2_admission_unit_closes_only_declared_prerequisites(self) -> None:
        manifest = verifier.load_manifest(ROOT / "tools" / "development_verification_manifest.json")
        selection = verifier.select_impacted_checks(
            manifest,
            ROOT,
            requested_ids=["stage2-authority-admission-unit"],
        )
        self.assertEqual(selection["selection_mode"], "explicit-check")
        self.assertEqual(
            selection["selected_ids"],
            [
                "stage0-orchestrator-unit",
                "stage1-sh01-unit",
                "stage2-authority-admission-unit",
            ],
        )
        self.assertNotIn("stage0-clojure-suite", selection["selected_ids"])
        self.assertNotIn("stage0-bootstrap-authority", selection["selected_ids"])

    def test_declared_resource_lock_is_host_wide_and_non_blocking(self) -> None:
        with verifier._process_lock("unit-test-heavy-resource"):
            with self.assertRaises(verifier.LockUnavailable):
                with verifier._process_lock("unit-test-heavy-resource"):
                    self.fail("a second owner must not enter the shared lock")

    def test_shared_authority_lock_legacy_holder_queues_then_free_inode_migrates(self) -> None:
        lock_path = Path("/private/tmp") / f"gravity-verifier-{os.getpid()}-{time.time_ns()}.lock"
        descriptor = os.open(lock_path, os.O_RDWR | os.O_CREAT | os.O_EXCL, 0o644)
        os.write(descriptor, b"legacy verifier payload\n")
        try:
            with mock.patch.object(verifier._sh07, "DEFAULT_LOCK", lock_path):
                fcntl.flock(descriptor, fcntl.LOCK_EX | fcntl.LOCK_NB)
                with self.assertRaisesRegex(verifier.LockUnavailable, "busy"):
                    with verifier._process_lock(str(lock_path)):
                        pass
                self.assertEqual(0o644, lock_path.stat().st_mode & 0o777)
                fcntl.flock(descriptor, fcntl.LOCK_UN)
                with verifier._process_lock(str(lock_path)) as acquired:
                    self.assertEqual(lock_path, acquired)
                    self.assertEqual(0o600, lock_path.stat().st_mode & 0o777)
                self.assertEqual(b"legacy verifier payload\n", lock_path.read_bytes())
        finally:
            os.close(descriptor)
            lock_path.unlink(missing_ok=True)

    def test_shared_authority_body_error_plus_replacement_reports_lock_failure(self) -> None:
        lock_path = Path("/private/tmp") / f"gravity-verifier-{os.getpid()}-{time.time_ns()}.lock"
        descriptor = os.open(lock_path, os.O_RDWR | os.O_CREAT | os.O_EXCL, 0o600)
        os.close(descriptor)
        replacement = lock_path.with_suffix(".replacement")
        try:
            with mock.patch.object(verifier._sh07, "DEFAULT_LOCK", lock_path):
                with self.assertRaises(verifier.LockUnavailable) as captured:
                    with verifier._process_lock(str(lock_path)):
                        other = os.open(
                            replacement, os.O_RDWR | os.O_CREAT | os.O_EXCL, 0o600
                        )
                        os.close(other)
                        os.replace(replacement, lock_path)
                        raise ValueError("body failed")
            self.assertIsInstance(captured.exception.__cause__, ValueError)
        finally:
            replacement.unlink(missing_ok=True)
            lock_path.unlink(missing_ok=True)

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
        # Only checks that declare the broad bootstrap tree participate in
        # this legacy shape assertion.  The focused project-structure gate is
        # intentionally precise and must not inherit this broad ownership.
        stage0_clojure_checks = [
            item
            for item in manifest["checks"]
            if item["id"].startswith("stage0-")
            and item["command"]
            and item["command"][0] == "clojure"
            and "bootstrap/gravity/**" in item["inputs"]
        ]
        self.assertTrue(stage0_clojure_checks)
        self.assertTrue(all(item.get("fresh") is True for item in stage0_clojure_checks))
        self.assertTrue(all("bin/gravity" in item["inputs"] for item in stage0_clojure_checks))
        self.assertTrue(all("bootstrap/gravity/**" in item["inputs"] for item in stage0_clojure_checks))
        project_structure = next(
            item
            for item in manifest["checks"]
            if item["id"] == "stage0-project-structure-extraction"
        )
        self.assertEqual(project_structure["lane"], "focused")
        self.assertEqual(project_structure["cost"], "cheap")
        self.assertEqual(project_structure["authority"], "none")
        self.assertIs(project_structure["fresh"], True)
        self.assertEqual(
            project_structure["command"],
            [
                "clojure",
                "-J-Xmx512m",
                "-M:project-structure-test",
                "--exact",
                "gravity.bootstrap-test/hosted-hello-runs",
                "--exact",
                "gravity.bootstrap-test/reader-source-unit-identity-preserves-path-extension-and-options",
                "--exact",
                "gravity.bootstrap-test/reader-file-policy-rejects-extension-and-malformed-utf8",
                "--exact",
                "gravity.bootstrap-test/c2-reader-treats-cr-lf-and-crlf-as-line-terminators",
                "--fail-fast",
            ],
        )
        self.assertEqual(
            project_structure["tool_inputs"],
            [
                "deps.edn",
                "bootstrap/clojure/test/gravity/project_structure_test_runner.clj",
            ],
        )
        runner_unit = next(
            item
            for item in manifest["checks"]
            if item["id"] == "stage0-project-structure-runner-unit"
        )
        self.assertEqual(runner_unit["lane"], "focused")
        self.assertEqual(runner_unit["command"], ["clojure", "-M:project-structure-runner-unit"])
        self.assertEqual(runner_unit["depends_on"], [])
        self.assertEqual(runner_unit["tool_inputs"], ["deps.edn"])
        self.assertIn(
            "bootstrap/clojure/test/gravity/project_structure_test_runner_unit.clj",
            runner_unit["inputs"],
        )
        self.assertEqual(project_structure["depends_on"], ["stage0-project-structure-runner-unit"])
        self.assertNotIn("bin/gravity", project_structure["inputs"])
        self.assertNotIn("bootstrap/gravity/**", project_structure["inputs"])
        self.assertNotIn("bootstrap/clojure/fixtures/**", project_structure["inputs"])
        extracted_leaf_paths = {
            "bootstrap/clojure/src/gravity/source_unit.clj",
            "bootstrap/clojure/test/gravity/source_unit_test.clj",
            "bootstrap/clojure/src/gravity/source_span.clj",
            "bootstrap/clojure/test/gravity/source_span_test.clj",
            "bootstrap/clojure/src/gravity/digest.clj",
            "bootstrap/clojure/test/gravity/digest_test.clj",
        }
        self.assertTrue(extracted_leaf_paths <= set(project_structure["inputs"]))
        for changed_path in sorted(extracted_leaf_paths):
            selection = verifier.select_impacted_checks(
                manifest,
                ROOT,
                changed_paths=[changed_path],
            )
            selected = set(selection["selected_ids"])
            self.assertIn("stage0-project-structure-extraction", selection["selected_ids"])
            if changed_path.startswith("bootstrap/clojure/src/"):
                self.assertIn("stage3-public-c7-check", selected)
                self.assertNotIn("stage3-c7-proof-candidate", selected)
            else:
                self.assertNotIn("stage3-public-c7-check", selected)
            self.assertNotIn("stage0-clojure-suite", selected)
            self.assertNotIn("stage0-bootstrap-authority", selected)
            self.assertEqual(selection["unmatched_changes"], [])
        runner_selection = verifier.select_impacted_checks(
            manifest,
            ROOT,
            changed_paths=[
                "bootstrap/clojure/test/gravity/project_structure_test_runner_test.clj"
            ],
        )
        self.assertEqual(
            runner_selection["selected_ids"],
            [
                "stage0-coordinator-integration-reservations",
                "stage0-project-structure-runner-unit",
                "stage0-project-structure-extraction",
            ],
        )
        orchestrator_unit = next(
            item for item in manifest["checks"] if item["id"] == "stage0-orchestrator-unit"
        )
        self.assertEqual(orchestrator_unit["reserved_processes"], 6)
        self.assertEqual(
            verifier.check_resource_declaration(manifest, orchestrator_unit)["reserved_processes"],
            6,
        )
        ordinary_python = next(item for item in manifest["checks"] if item["id"] == "m0-docs")
        self.assertNotIn("reserved_processes", ordinary_python)
        self.assertEqual(
            verifier.check_resource_declaration(manifest, ordinary_python)["reserved_processes"],
            1,
        )
        sh01_unit = next(item for item in manifest["checks"] if item["id"] == "stage1-sh01-unit")
        self.assertEqual(sh01_unit["reserved_processes"], 4)
        self.assertEqual(sh01_unit["lane"], "preflight")
        self.assertEqual(sh01_unit["command"], ["clojure", "-M:sh01-test"])
        self.assertIs(sh01_unit["fresh"], True)
        self.assertEqual(sh01_unit["depends_on"], ["stage0-orchestrator-unit"])
        self.assertEqual(sh01_unit["tool_inputs"], ["deps.edn"])
        self.assertEqual(
            set(sh01_unit["inputs"]),
            {
                "docs/self-hosting-slice-backlog.md",
                "docs/self-hosting-slice-ownership.edn",
                "bootstrap/clojure/test/gravity/self_hosting_test_runner.clj",
                "bootstrap/clojure/test/gravity/self_hosting/sh01_impact_test_planner.clj",
                "bootstrap/clojure/test/gravity/self_hosting/sh01_parallel_test_runner.clj",
                "bootstrap/clojure/test/gravity/self_hosting/sh01_development_test_runner.clj",
                "bootstrap/clojure/test/gravity/self_hosting/**/*_test.clj",
            },
        )
        self.assertNotIn("bin/gravity", sh01_unit["inputs"])
        self.assertNotIn("bootstrap/gravity/**", sh01_unit["inputs"])
        stage2_admission_unit = next(
            item for item in manifest["checks"] if item["id"] == "stage2-authority-admission-unit"
        )
        self.assertEqual(stage2_admission_unit["lane"], "preflight")
        self.assertEqual(stage2_admission_unit["cost"], "cheap")
        self.assertIsNone(stage2_admission_unit["lock"])
        self.assertFalse(stage2_admission_unit["exclusive"])
        self.assertEqual(stage2_admission_unit["authority"], "none")
        self.assertEqual(stage2_admission_unit["daemonization"], "forbidden")
        self.assertIs(stage2_admission_unit["fresh"], True)
        self.assertEqual(stage2_admission_unit["timeout_seconds"], 120)
        self.assertEqual(
            stage2_admission_unit["command"],
            [
                "python3",
                "-m",
                "unittest",
                "tools.tests.test_stage2_authority_admission",
                "-v",
            ],
        )
        self.assertEqual(
            set(stage2_admission_unit["inputs"]),
            {
                "tools/stage2_authority_admission.py",
                "tools/run_sh07_authoritative_modules.py",
                "tools/run_with_heartbeat.py",
                "tools/verify_development.py",
                "tools/tests/test_stage2_authority_admission.py",
                "tools/tests/test_run_with_heartbeat.py",
                "tools/tests/test_run_sh07_authoritative_modules.py",
                "tools/tests/test_verify_development.py",
            },
        )
        self.assertEqual(stage2_admission_unit["depends_on"], ["stage1-sh01-unit"])
        self.assertEqual(stage2_admission_unit["reserved_processes"], 4)
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
                "stage0-profile-capability-compatibility",
                "stage0-sh01-ownership-control",
                "stage0-sh01-development-runner-control",
                "stage0-sh01-leaf-runner-control",
                "stage1-sh01-unit",
                "stage3-runner-unit",
                "stage0-project-structure-runner-unit",
                "stage0-project-structure-extraction",
                "stage0-p15-native-launcher-prerequisite",
                "stage0-p15-native-runtime-provider-contract-prerequisite",
                "stage0-p15-native-runtime-provider-packet-binding-prerequisite",
                "stage0-p15-native-plan-specialization-prerequisite",
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
        self.assertNotIn(
            "bootstrap/clojure/test/gravity/self_hosting/sh01_stage0_leaf_test_runner_test.clj",
            full_suite["inputs"],
        )
        self.assertNotIn(
            "bootstrap/clojure/test/gravity/self_hosting/sh01_development_test_runner_test.clj",
            full_suite["inputs"],
        )
        self.assertIn("contracts/stage0-clojure-components.json", full_suite["inputs"])

    def test_impact_excludes_is_per_changed_path(self) -> None:
        owned = check(
            "owned",
            ["python3", "-c", "pass"],
            inputs=["bootstrap/gravity/**", "tools/verify_development.py"],
            lane="focused",
        )
        owned["impact_excludes"] = ["bootstrap/gravity/src/gravity/compiler/c7_type_checker_engine.gravity"]
        manifest = manifest_for(owned)
        verifier.validate_manifest(manifest)
        excluded_only = verifier.select_impacted_checks(
            manifest,
            ROOT,
            changed_paths=["bootstrap/gravity/src/gravity/compiler/c7_type_checker_engine.gravity"],
        )
        self.assertEqual([], excluded_only["selected_ids"])
        mixed = verifier.select_impacted_checks(
            manifest,
            ROOT,
            changed_paths=[
                "bootstrap/gravity/src/gravity/compiler/c7_type_checker_engine.gravity",
                "tools/verify_development.py",
            ],
        )
        self.assertEqual(["owned"], mixed["selected_ids"])

    def test_command_lock_owner_requires_reviewed_stage3_shape(self) -> None:
        value = check(
            "stage3",
            ["python3", "tools/run_stage3_verification.py"],
            inputs=["tools/run_stage3_verification.py"],
            lane="heavy-candidate",
            cost="heavy",
            lock=str(verifier._stage3.CANONICAL_LOCK),
            exclusive=True,
            fresh=True,
        )
        value["lock_owner"] = "command"
        value["stage3_mode"] = verifier._stage3.MODE_PURE
        value["stage3_batch"] = "public-c7-check"
        verifier.validate_manifest(manifest_for(value))
        value["command"] = ["python3", "-c", "pass"]
        with self.assertRaises(verifier.ManifestError):
            verifier.validate_manifest(manifest_for(value))

    def test_production_stage3_nodes_require_central_runtime_inputs(self) -> None:
        manifest = verifier.load_manifest(ROOT / "tools" / "development_verification_manifest.json")
        broken = json.loads(json.dumps(manifest))
        target = next(item for item in broken["checks"] if item["id"] == "stage3-public-c7-check")
        target["tool_inputs"].remove("bootstrap/clojure/src/**")
        with self.assertRaisesRegex(verifier.ManifestError, "centralized Stage3 runtime inputs"):
            verifier.validate_manifest(broken)

        for relative in verifier._stage3.STAGE3_RUNTIME_DEPENDENCIES:
            if verifier._contains_glob(relative):
                continue
            path = ROOT / relative
            self.assertTrue(path.is_file(), relative)
            self.assertFalse(path.is_symlink(), relative)

        nonexistent = "bootstrap/clojure/test/gravity/nonexistent_stage3_helper.clj"
        broken = json.loads(json.dumps(manifest))
        for item in broken["checks"]:
            if item.get("lock_owner") == "command" and item["id"].startswith("stage3-"):
                item["tool_inputs"].append(nonexistent)
        with mock.patch.object(
            verifier._stage3,
            "STAGE3_RUNTIME_DEPENDENCIES",
            (*verifier._stage3.STAGE3_RUNTIME_DEPENDENCIES, nonexistent),
        ):
            with self.assertRaisesRegex(verifier.ManifestError, "not existing regular files"):
                verifier.validate_manifest(broken)

    def test_actual_inner_runner_bytes_change_stage3_production_identity(self) -> None:
        manifest = verifier.load_manifest(ROOT / "tools" / "development_verification_manifest.json")
        production = verifier.checks_by_id(manifest)["stage3-public-c7-check"]
        relative = "bootstrap/clojure/test/gravity/self_hosting_test_runner.clj"
        self.assertIn(relative, production["tool_inputs"])
        source = ROOT / relative
        with tempfile.TemporaryDirectory() as directory:
            temp_root = Path(directory)
            target = temp_root / relative
            target.parent.mkdir(parents=True)
            target.write_bytes(source.read_bytes())
            before = verifier.input_identities(production, temp_root)["sha256"]
            target.write_bytes(target.read_bytes() + b"\n")
            after = verifier.input_identities(production, temp_root)["sha256"]
        self.assertNotEqual(before, after)

    def test_declared_authority_cannot_use_runner_lock_owner(self) -> None:
        value = check(
            "authority",
            ["python3", "tools/run_stage3_verification.py"],
            inputs=["tools/run_stage3_verification.py"],
            lane="heavy-candidate",
            cost="heavy",
            lock=str(verifier._stage3.CANONICAL_LOCK),
            exclusive=True,
            fresh=True,
            authority="declared",
        )
        with self.assertRaises(verifier.ManifestError):
            verifier.validate_manifest(manifest_for(value))

    def test_real_manifest_stage3_fixed_graph_matches_runner_allowlist(self) -> None:
        manifest = verifier.load_manifest(ROOT / "tools" / "development_verification_manifest.json")
        by_id = verifier.checks_by_id(manifest)
        stage3 = {
            check_id: item
            for check_id, item in by_id.items()
            if check_id.startswith("stage3-") and check_id != "stage3-runner-unit"
        }
        stage4_batches = {
            "stage4-c8-source-structural",
            "stage4-sh09-adapter",
            "stage4-public-c8",
            "c8-authority",
        }
        stage5_batches = {
            "stage5-c9-source-structural",
            "stage5-c9-kernel",
            "stage5-sh10-c8-adapter",
            "stage5-public-c9",
            "c9-authority",
        }
        stage6_batches = {
            "stage6-c10-source-structural",
            "stage6-c10-kernel",
            "stage6-public-c10",
            "stage6-sh11-c9-safety-adapter",
            "c10-authority",
        }
        stage7_batches = {
            "stage7-c11-source-preflight",
            "stage7-sh12-c10-mir-adapter",
            "stage7-public-c11",
            "c11-authority",
        }
        stage7_runner_only_batches = {"stage7-c11-shape-preflight"}
        stage8_batches = {
            "stage8-c12-source-shape",
            "stage8-sh13-c11-domain-evidence",
            "stage8-public-c12",
        }
        stage9_batches = {
            "stage9-c13-source-shape",
            "stage9-sh16-c13-evidence-boundary",
        }
        self.assertEqual(
            set(verifier._stage3.FIXED_BATCHES)
            & (stage7_batches | stage7_runner_only_batches | stage8_batches | stage9_batches),
            stage7_batches | stage7_runner_only_batches | stage8_batches | stage9_batches,
        )
        stage3_fixed_batches = (
            set(verifier._stage3.FIXED_BATCHES)
            - stage4_batches
            - stage5_batches
            - stage6_batches
            - stage7_batches
            - stage7_runner_only_batches
            - stage8_batches
            - stage9_batches
        )
        self.assertTrue(stage7_batches.isdisjoint(stage3_fixed_batches))
        self.assertEqual(
            {item["stage3_batch"] for item in stage3.values()},
            stage3_fixed_batches,
        )
        self.assertEqual(
            len(stage3),
            len(stage3_fixed_batches),
        )
        self.assertEqual(len(stage3) + 1, 13)  # twelve C7 fixed batches plus runner-unit
        self.assertEqual(
            [
                item["id"]
                for item in by_id.values()
                if item["id"].startswith("stage3-")
            ],
            [
                "stage3-runner-unit",
                "stage3-source-control-form-arity",
                "stage3-coverage-census-contract",
                "stage3-fragment-size-preflight",
                "stage3-source-plan-contract",
                "stage3-primitive-pure",
                "stage3-recursive-pure",
                "stage3-authoritative-ho-pure",
                "stage3-primitive-bool-authenticated",
                "stage3-recursive-authenticated",
                "stage3-authoritative-ho-authenticated",
                "stage3-public-c7-check",
                "stage3-c7-proof-candidate",
            ],
        )
        self.assertEqual(
            tuple(verifier._stage3.STAGE3_SHARED_RUNTIME_INPUTS),
            tuple(verifier._stage3._sh07.SHARED_GRAVITY_FILES)
            + tuple(f"{tree}/**" for tree in verifier._stage3._sh07.SHARED_REPOSITORY_TREES),
        )
        self.assertEqual(
            tuple(verifier._stage3._sh07.SHARED_REPOSITORY_TREES),
            ("bootstrap/clojure/src",),
        )
        runner_unit = by_id["stage3-runner-unit"]
        self.assertEqual(runner_unit["tool_inputs"], ["deps.edn"])
        required_runtime = set(verifier._stage3.STAGE3_RUNTIME_DEPENDENCIES)
        for item in stage3.values():
            self.assertEqual(item["lane"], "heavy-candidate")
            self.assertEqual(item["cost"], "heavy")
            self.assertEqual(item["lock_owner"], "command")
            self.assertEqual(item["lock"], str(verifier._stage3.CANONICAL_LOCK))
            self.assertTrue(item["exclusive"])
            self.assertEqual(item["capacity"], 1)
            self.assertTrue(item["fresh"])
            self.assertFalse(item["resume"])
            self.assertEqual(item["command"], ["python3", "tools/run_stage3_verification.py"])
            self.assertNotIn("--namespace", item["command"])
            self.assertNotIn("--exact", item["command"])
            if item["stage3_batch"] != "authority":
                self.assertEqual(item["stage3_mode"], verifier._stage3.MODE_PURE)
            self.assertTrue(
                {
                    "deps.edn",
                    "tools/run_stage3_verification.py",
                    "tools/verify_development.py",
                    "tools/run_sh07_authoritative_modules.py",
                    "bootstrap/clojure/test/gravity/self_hosting/stage3_verification_runner.clj",
                    "bootstrap/clojure/test/gravity/self_hosting/sh07_iteration_cache_runner.clj",
                    "bootstrap/clojure/src/gravity/bootstrap.clj",
                }.issubset(item["tool_inputs"])
            )
            expected_heap = (
                verifier._stage3.batch_command(item["stage3_batch"])[1]
                if item["stage3_batch"] != "authority"
                else "-J-Xmx8g"
            )
            self.assertEqual(item["jvm_heap"], expected_heap)
            self.assertEqual(
                item["minimum_heap_bytes"],
                2147483648 if expected_heap == "-J-Xmx2g" else 8589934592,
            )
            self.assertIsInstance(item.get("automatic", True), bool)
            self.assertTrue(
                required_runtime <= set(item["inputs"]) | set(item["tool_inputs"]),
                msg=item["id"],
            )
        source_test = "bootstrap/clojure/test/gravity/self_hosting/sh07_c7_type_source_coverage_test.clj"
        for check_id in ("stage3-source-control-form-arity", "stage3-source-plan-contract"):
            self.assertIn(source_test, by_id[check_id]["inputs"])
            self.assertIn(source_test, by_id[check_id]["impact_excludes"])
        census_test = "bootstrap/clojure/test/gravity/self_hosting/sh07_authoritative_coverage_census_test.clj"
        census = by_id["stage3-coverage-census-contract"]
        self.assertTrue(
            {
                census_test,
                "bootstrap/clojure/test/gravity/self_hosting/sh07_authoritative_runner.clj",
                "bootstrap/clojure/test/gravity/self_hosting/sh07_proof_contract.edn",
            } <= set(census["inputs"])
        )
        self.assertIn(census_test, census["impact_excludes"])
        public_partial = {
            "bootstrap/clojure/test/gravity/bootstrap_test.clj",
            "bootstrap/clojure/test/gravity/cli_test.clj",
            "bootstrap/clojure/test/gravity/diagnostics_test.clj",
        }
        self.assertTrue(public_partial <= set(by_id["stage3-public-c7-check"]["inputs"]))
        self.assertTrue(public_partial <= set(by_id["stage3-public-c7-check"]["impact_excludes"]))
        proof = by_id["stage3-c7-proof-candidate"]
        self.assertEqual(proof["stage3_batch"], "authority")
        self.assertEqual(proof["stage3_mode"], verifier._stage3.MODE_PROOF_CANDIDATE)
        self.assertEqual(proof["authority"], "none")
        self.assertTrue(proof["proof_candidate"])
        self.assertTrue(proof["attestation_required"])
        self.assertTrue(proof["no_resume"])
        self.assertEqual(proof["state_dir_policy"], "new-per-invocation")
        self.assertFalse(proof["automatic"])
        self.assertTrue(
            {
                "bootstrap/clojure/test/gravity/self_hosting/sh07_authoritative_runner.clj",
                "bootstrap/clojure/test/gravity/self_hosting/sh07_proof_contract.edn",
            }.issubset(proof["tool_inputs"])
        )
        public = by_id["stage3-public-c7-check"]
        self.assertEqual(public["jvm_heap"], "-J-Xmx2g")
        self.assertGreaterEqual(public["timeout_seconds"], 600)
        self.assertTrue(public["resource_receipt"].startswith("observed-peak"))
        self.assertIn("-J-Xmx2g", verifier._stage3.batch_command("public-c7-check"))
        for old_id in (
            "stage3-recursive-integer-authenticated",
            "stage3-recursive-string-authenticated",
            "stage3-authoritative-ho-fixture-parity",
            "stage3-authoritative-ho2-authenticated",
        ):
            self.assertNotIn(old_id, by_id)
        self.assertEqual(
            verifier.checks_by_id(manifest)["stage3-runner-unit"]["depends_on"],
            ["stage2-authority-admission-unit"],
        )
        expected_batch_inputs = {
            "primitive-pure": {
                "bootstrap/clojure/fixtures/self-hosting/sh-08/accepted/function-value-typed-bool.gravity",
                "bootstrap/clojure/fixtures/self-hosting/sh-08/accepted/function-value-typed-bool.qst",
            },
            "primitive-bool-authenticated": {
                "bootstrap/clojure/test/gravity/self_hosting/sh08_function_call_type_test.clj",
                "bootstrap/clojure/fixtures/self-hosting/sh-08/accepted/function-value-typed-bool.gravity",
            },
            "recursive-pure": {
                "bootstrap/clojure/fixtures/self-hosting/sh-08/accepted/function-self-recursive-type.gravity",
                "bootstrap/clojure/fixtures/self-hosting/sh-08/accepted/function-self-recursive-type.qst",
            },
            "recursive-authenticated": {
                "bootstrap/clojure/test/gravity/self_hosting/sh08_function_call_type_test.clj",
                "bootstrap/clojure/fixtures/self-hosting/sh-08/accepted/function-self-recursive-type.gravity",
                "bootstrap/clojure/fixtures/self-hosting/sh-08/accepted/function-self-recursive-string-type.gravity",
            },
            "authoritative-ho-authenticated": {
                "bootstrap/clojure/fixtures/self-hosting/sh-08/accepted/function-value-typed-call.gravity",
                "bootstrap/clojure/fixtures/self-hosting/sh-08/accepted/function-value-typed-call.qst",
                "bootstrap/clojure/test/gravity/self_hosting/sh08_function_call_type_test.clj",
            },
        }
        for batch, paths in expected_batch_inputs.items():
            check_item = next(item for item in stage3.values() if item["stage3_batch"] == batch)
            self.assertTrue(paths <= set(check_item["inputs"]), msg=batch)

    def test_real_manifest_stage3_dependencies_put_arity_before_all_late_gates(self) -> None:
        manifest = verifier.load_manifest(ROOT / "tools" / "development_verification_manifest.json")
        order = verifier.topological_order(manifest)
        position = {check_id: index for index, check_id in enumerate(order)}
        arity = position["stage3-source-control-form-arity"]
        for check_id in (
            "stage3-source-plan-contract",
            "stage3-coverage-census-contract",
            "stage3-fragment-size-preflight",
            "stage3-public-c7-check",
            "stage3-c7-proof-candidate",
        ):
            self.assertLess(arity, position[check_id])
        self.assertLess(
            position["stage3-public-c7-check"],
            position["stage3-c7-proof-candidate"],
        )
        self.assertIn("stage3-public-c7-check", verifier.dependencies_of(
            verifier.checks_by_id(manifest)["stage3-c7-proof-candidate"]
        ))
        pure = [
            position[item]
            for item in (
                "stage3-primitive-pure",
                "stage3-recursive-pure",
                "stage3-authoritative-ho-pure",
            )
        ]
        authenticated = [
            position[item]
            for item in (
                "stage3-primitive-bool-authenticated",
                "stage3-recursive-authenticated",
                "stage3-authoritative-ho-authenticated",
            )
        ]
        self.assertLess(max(pure), min(authenticated))
        self.assertLess(
            position["stage3-source-plan-contract"],
            min(pure),
        )

    def test_real_manifest_c7_impact_selects_downstream_without_legacy_stage0_heavy(self) -> None:
        manifest = verifier.load_manifest(ROOT / "tools" / "development_verification_manifest.json")
        changed = "bootstrap/gravity/src/gravity/compiler/c7_type_checker_engine.gravity"
        selection = verifier.select_impacted_checks(manifest, ROOT, changed_paths=[changed])
        selected = set(selection["selected_ids"])
        self.assertIn("stage3-source-control-form-arity", selected)
        self.assertIn("stage3-source-plan-contract", selected)
        self.assertIn("stage3-coverage-census-contract", selected)
        self.assertIn("stage3-fragment-size-preflight", selected)
        self.assertIn("stage3-public-c7-check", selected)
        # The multi-hour proof candidate is manual-only under implicit
        # change-impact routing; an explicit --check/--all remains available.
        self.assertNotIn("stage3-c7-proof-candidate", selected)
        self.assertFalse(
            verifier.checks_by_id(manifest)["stage3-c7-proof-candidate"].get("automatic", True)
        )
        explicit = verifier.select_impacted_checks(
            manifest,
            ROOT,
            requested_ids=["stage3-c7-proof-candidate"],
        )
        self.assertIn("stage3-c7-proof-candidate", explicit["selected_ids"])
        self.assertIn("stage3-public-c7-check", explicit["selected_ids"])
        self.assertNotIn("stage0-clojure-suite", selected)
        self.assertNotIn("stage0-bootstrap-authority", selected)
        self.assertEqual(selection["unmatched_changes"], [])

    def test_real_manifest_stage3_runtime_dependencies_have_executable_owners(self) -> None:
        manifest = verifier.load_manifest(ROOT / "tools" / "development_verification_manifest.json")
        by_id = verifier.checks_by_id(manifest)
        stage3_ids = {
            check_id
            for check_id in by_id
            if check_id.startswith("stage3-")
        }
        runtime_paths = {
            "tools/run_stage3_verification.py",
            "tools/verify_development.py",
            "tools/run_sh07_authoritative_modules.py",
            "bootstrap/clojure/test/gravity/self_hosting/stage3_verification_runner.clj",
            "bootstrap/clojure/test/gravity/self_hosting/sh07_iteration_cache_runner.clj",
            "bootstrap/clojure/src/gravity/bootstrap.clj",
        }
        proof_paths = {
            "bootstrap/clojure/test/gravity/self_hosting/sh07_authoritative_runner.clj",
            "bootstrap/clojure/test/gravity/self_hosting/sh07_proof_contract.edn",
        }
        for path in sorted(runtime_paths):
            selection = verifier.select_impacted_checks(manifest, ROOT, changed_paths=[path])
            self.assertTrue(
                stage3_ids.intersection(selection["selected_ids"]),
                msg=f"runtime dependency has no Stage3 owner: {path}",
            )
        for path in sorted(proof_paths):
            selection = verifier.select_impacted_checks(manifest, ROOT, changed_paths=[path])
            self.assertIn("stage3-public-c7-check", selection["selected_ids"])
            self.assertNotIn("stage3-c7-proof-candidate", selection["selected_ids"])
            self.assertEqual(selection["unmatched_changes"], [])
        partial_stage3_paths = {
            "bootstrap/clojure/test/gravity/self_hosting/sh07_c7_type_source_coverage_test.clj",
            "bootstrap/clojure/test/gravity/self_hosting/sh07_authoritative_coverage_census_test.clj",
        }
        for path in sorted(partial_stage3_paths):
            selection = verifier.select_impacted_checks(manifest, ROOT, changed_paths=[path])
            self.assertFalse(
                any(item.startswith("stage3-") for item in selection["selected_ids"]),
                path,
            )
            self.assertEqual(selection["unmatched_changes"], [path])
        for path in (
            "bootstrap/clojure/test/gravity/bootstrap_test.clj",
            "bootstrap/clojure/test/gravity/cli_test.clj",
            "bootstrap/clojure/test/gravity/diagnostics_test.clj",
        ):
            selection = verifier.select_impacted_checks(manifest, ROOT, changed_paths=[path])
            self.assertNotIn("stage3-public-c7-check", selection["selected_ids"])
        for path in list(verifier._stage3._sh07.SHARED_GRAVITY_FILES) + [
            "bootstrap/clojure/src/gravity/diagnostics.clj",
        ]:
            selection = verifier.select_impacted_checks(manifest, ROOT, changed_paths=[path])
            selected = set(selection["selected_ids"])
            self.assertIn("stage3-public-c7-check", selected, path)
            self.assertNotIn("stage3-c7-proof-candidate", selected, path)
            self.assertNotIn("stage0-clojure-suite", selected, path)
            self.assertNotIn("stage0-bootstrap-authority", selected, path)
            self.assertEqual(selection["unmatched_changes"], [], path)
        for path in (
            "bootstrap/clojure/test/gravity/self_hosting/sh08_function_call_type_test.clj",
            "bootstrap/clojure/fixtures/self-hosting/sh-08/accepted/function-value-typed-bool.gravity",
            "bootstrap/clojure/fixtures/self-hosting/sh-08/accepted/function-self-recursive-type.gravity",
            "bootstrap/clojure/fixtures/self-hosting/sh-08/accepted/function-self-recursive-string-type.gravity",
            "bootstrap/clojure/fixtures/self-hosting/sh-08/accepted/function-value-typed-call.gravity",
            "bootstrap/clojure/fixtures/self-hosting/sh-08/accepted/function-value-typed-call.qst",
        ):
            selection = verifier.select_impacted_checks(manifest, ROOT, changed_paths=[path])
            self.assertTrue(stage3_ids.intersection(selection["selected_ids"]), path)
            self.assertNotIn("stage0-clojure-suite", selection["selected_ids"])
            self.assertNotIn("stage0-bootstrap-authority", selection["selected_ids"])

    def _valid_stage3_proof_candidate_receipt(
        self,
        root: Path,
        *,
        check_id: str,
        batch: str,
        module: str,
    ) -> tuple[dict, dict, dict]:
        """Build a bounded fixed-policy proof receipt for parent validation tests."""

        manifest = verifier.load_manifest(ROOT / "tools" / "development_verification_manifest.json")
        check_item = verifier.checks_by_id(manifest)[check_id]
        receipt_path = root / ".cpcache" / f"{check_id}.json"
        nonce = "proof-receipt-test"
        command = ["python3", "tools/run_stage3_verification.py"]
        command_hash = "sha256:" + verifier._sha256_text(verifier._canonical(command))
        digest = "sha256:" + ("a" if module == "c7-types" else "b") * 64
        evidence = {
            "state_dir": str(root / ".cpcache" / "stage3-authority" / nonce),
            "state": "completed",
            "selected_modules": [module],
            "module": module,
            "aggregate_authoritative": False,
            "authority_scope": "individual-source-bound-derived",
            "lock_path": verifier._stage3.CANONICAL_LOCK_TEXT,
            "lock_mode": "0600",
            "lock_acquired": True,
            "lock_validated": True,
            "lock_released": True,
            "manifest_sha256": digest,
            "module_record": {
                "state": "passed",
                "stdout_sha256": digest,
                "proof_contract_sha256": digest,
                "module_context_fingerprint": digest,
            },
        }
        receipt = {
            "schema": verifier._stage3.SCHEMA,
            "receipt_path": str(receipt_path),
            "root": str(root),
            "nonce": nonce,
            "check_id": check_id,
            "mode": verifier._stage3.MODE_PROOF_CANDIDATE,
            "batch": batch,
            "proof_batch": batch,
            "proof_module": module,
            "command": command,
            "command_identity_sha256": command_hash,
            "lock": {
                "path": verifier._stage3.CANONICAL_LOCK_TEXT,
                "canonical_path": verifier._stage3.CANONICAL_LOCK_TEXT,
                "protocol": verifier._sh07.SHARED_LOCK_PROTOCOL,
                "acquired": True,
                "validated": True,
                "released": True,
                "owner": "authoritative-child",
            },
            "daemonization": "forbidden",
            "no_surviving_descendants": True,
            "observed_peak_process_tree_rss_bytes": 123,
            "rss_sampling_cadence_seconds": 1.0,
            "rss_sampling_contract": "run_with_heartbeat.process_tree_metrics-v1",
            "rss_sampling_limitation": "between-sample spike may be missed",
            "child": {
                "command": command,
                "returncode": 0,
                "timed_out": False,
                "supervision_failed": False,
                "survivors": [],
                "cleanup": {"terminal_safe": True, "output_complete": True},
                "observed_peak_process_tree_rss_bytes": 123,
                "rss_sampling_cadence_seconds": 1.0,
                "rss_sampling_contract": "run_with_heartbeat.process_tree_metrics-v1",
                "rss_sampling_limitation": "between-sample spike may be missed",
            },
            "exit_code": 0,
            "status": "passed",
            "proof_candidate": True,
            "attestation_required": True,
            "authority": "none",
            "non_authoritative": True,
            "candidate_manifest_path": str(Path(evidence["state_dir"]) / "manifest.json"),
            "authority_evidence": evidence,
        }
        identities = {"command": command}
        return receipt, check_item, identities

    def test_parent_accepts_valid_fixed_proof_candidate_receipts(self) -> None:
        for check_id, batch, module in (
            ("stage3-c7-proof-candidate", "authority", "c7-types"),
            ("stage4-c8-proof-candidate", "c8-authority", "c8-effects"),
            ("stage5-c9-proof-candidate", "c9-authority", "c9-ownership"),
            ("stage6-c10-proof-candidate", "c10-authority", "c10-safety"),
            ("stage7-c11-proof-candidate", "c11-authority", "c11-mir"),
        ):
            with tempfile.TemporaryDirectory(prefix=f"gravity-{module}-receipt-") as directory:
                root = Path(directory).resolve()
                receipt, check_item, identities = self._valid_stage3_proof_candidate_receipt(
                    root,
                    check_id=check_id,
                    batch=batch,
                    module=module,
                )
                validated = verifier._validate_stage3_receipt(
                    receipt,
                    check=check_item,
                    identities=identities,
                    root=root,
                    receipt_path=Path(receipt["receipt_path"]),
                    runner_report_path=None,
                    nonce=receipt["nonce"],
                    expected_returncode=0,
                )
                self.assertEqual(validated["authority"], "proof-candidate")
                self.assertEqual(validated["proof_module"], module)
                self.assertEqual(validated["authority_evidence"]["module"], module)
                self.assertEqual(validated["authority_evidence"]["selected_modules"], [module])

    def test_parent_rejects_cross_module_proof_candidate_tampering(self) -> None:
        with tempfile.TemporaryDirectory(prefix="gravity-c9-tamper-") as directory:
            root = Path(directory).resolve()
            receipt, check_item, identities = self._valid_stage3_proof_candidate_receipt(
                root,
                check_id="stage5-c9-proof-candidate",
                batch="c9-authority",
                module="c9-ownership",
            )
            for mutate in (
                lambda value: value.update({"proof_batch": "authority"}),
                lambda value: value.update({"proof_module": "c7-types"}),
                lambda value: value["authority_evidence"].update({"module": "c7-types"}),
                lambda value: value["authority_evidence"].update({"selected_modules": ["c7-types"]}),
                lambda value: value.update({"proof_batch": "c8-authority"}),
                lambda value: value.update({"proof_module": "c8-effects"}),
                lambda value: value["authority_evidence"].update({"module": "c8-effects"}),
            ):
                candidate = json.loads(json.dumps(receipt))
                mutate(candidate)
                with self.assertRaises(verifier.VerificationError):
                    verifier._validate_stage3_receipt(
                        candidate,
                        check=check_item,
                        identities=identities,
                        root=root,
                        receipt_path=Path(candidate["receipt_path"]),
                        runner_report_path=None,
                        nonce=candidate["nonce"],
                        expected_returncode=0,
                    )

    def test_stage5_fixed_graph_matches_runner_and_resource_policy(self) -> None:
        manifest = verifier.load_manifest(ROOT / "tools" / "development_verification_manifest.json")
        by_id = verifier.checks_by_id(manifest)
        expected_ids = [
            "stage5-c9-source-structural",
            "stage5-c9-kernel",
            "stage5-sh10-c8-adapter",
            "stage5-public-c9",
            "stage5-c9-proof-candidate",
        ]
        self.assertEqual(
            [item["id"] for item in manifest["checks"] if item["id"].startswith("stage5-")],
            expected_ids,
        )
        expected_batches = {
            "stage5-c9-source-structural",
            "stage5-c9-kernel",
            "stage5-sh10-c8-adapter",
            "stage5-public-c9",
            "c9-authority",
        }
        self.assertEqual(
            {by_id[item]["stage3_batch"] for item in expected_ids},
            expected_batches,
        )
        fixed = verifier._stage3._FIXED_BATCH_SELECTORS
        expected_selectors = {
            "stage5-c9-source-structural": (
                "gravity.self-hosting.sh07-c9-ownership-source-coverage-test/sh07-b30-proof-contract-registers-c9-source-exactly",
                "gravity.self-hosting.sh07-c9-ownership-source-coverage-test/sh07-b30-c9-source-control-form-arities-are-bounded",
                "gravity.self-hosting.sh07-c9-ownership-source-coverage-test/sh07-b30-c9-source-contracts-states-and-reasons-are-exact",
                "gravity.self-hosting.sh07-c9-ownership-source-coverage-test/sh07-b30-c9-structural-limitations-remain-explicit",
            ),
            "stage5-c9-kernel": (
                "gravity.self-hosting.sh10-ownership-transition-test/sh10-source-and-fixtures-compile-as-gravity",
                "gravity.self-hosting.sh10-ownership-transition-test/sh10-accepts-initiation-borrow-move-and-bounded-lifetime-flows",
                "gravity.self-hosting.sh10-ownership-transition-test/sh10-rejects-invalid-state-transitions-structurally",
                "gravity.self-hosting.sh10-ownership-transition-test/sh10-fails-closed-on-request-event-and-result-substitution",
            ),
            "stage5-sh10-c8-adapter": (
                "gravity.self-hosting.sh10-c8-ownership-adapter-test/sh10-c8-adapter-source-api-and-policy-are-exact",
                "gravity.self-hosting.sh10-c8-ownership-adapter-test/sh10-c8-adapter-accepts-persistent-primitive-read",
                "gravity.self-hosting.sh10-c8-ownership-adapter-test/sh10-c8-adapter-accepts-primitive-type-family",
                "gravity.self-hosting.sh10-c8-ownership-adapter-test/sh10-c8-adapter-rejects-mutation-and-non-read-events",
                "gravity.self-hosting.sh10-c8-ownership-adapter-test/sh10-c8-adapter-authenticated-gravity-boundary",
            ),
            "stage5-public-c9": (
                "gravity.bootstrap-test/public-check-accepts-gravity-authored-c9-ownership-checker-engine",
            ),
        }
        for check_id in expected_ids[:-1]:
            item = by_id[check_id]
            batch = item["stage3_batch"]
            self.assertEqual(tuple(fixed[batch]), expected_selectors[check_id], batch)
            self.assertEqual(len(fixed[batch]), len(set(fixed[batch])))
            self.assertEqual(item["command"], ["python3", "tools/run_stage3_verification.py"])
            self.assertEqual(item["lock_owner"], "command")
            self.assertEqual(item["lock"], str(verifier._stage3.CANONICAL_LOCK))
            self.assertTrue(item["exclusive"])
            self.assertEqual(item["capacity"], 1)
            self.assertTrue(item["fresh"])
            self.assertFalse(item["resume"])
            self.assertEqual(item["stage3_mode"], verifier._stage3.MODE_PURE)
            expected_heap = verifier._stage3.batch_command(batch)[1]
            self.assertEqual(item["jvm_heap"], expected_heap)
            self.assertEqual(
                item["minimum_heap_bytes"],
                2147483648 if expected_heap == "-J-Xmx2g" else 8589934592,
            )
            self.assertTrue(
                set(verifier._stage3.STAGE3_RUNTIME_DEPENDENCIES)
                <= set(item["inputs"]) | set(item["tool_inputs"]),
                check_id,
            )
        source = by_id["stage5-c9-source-structural"]
        self.assertIn(
            "bootstrap/clojure/test/gravity/self_hosting/sh07_c9_ownership_source_coverage_test.clj",
            source["impact_excludes"],
        )
        self.assertEqual(len([item for item in source["inputs"] if item.startswith("docs/")]), 26)
        source_identity = verifier.input_identities(source, ROOT)
        source_record = next(
            item
            for item in source_identity["files"]
            if item["path"] == "bootstrap/gravity/src/gravity/compiler/c9_ownership_checker_engine.gravity"
        )
        self.assertEqual(source_record["size"], 71132)
        self.assertEqual(
            source_record["sha256"],
            "4f26a5ca5fdd7755016f332fc5c795f84a98b83b76cef79806b8021807897fcd",
        )
        kernel = by_id["stage5-c9-kernel"]
        self.assertTrue(
            {
                "bootstrap/clojure/fixtures/self-hosting/sh-10/accepted/ownership-transitions.gravity",
                "bootstrap/clojure/fixtures/self-hosting/sh-10/accepted/ownership-transitions.qst",
                "bootstrap/clojure/fixtures/self-hosting/sh-10/rejected/invalid-ownership-transitions.gravity",
                "bootstrap/clojure/fixtures/self-hosting/sh-10/rejected/invalid-ownership-transitions.qst",
            } <= set(kernel["inputs"])
        )
        adapter = by_id["stage5-sh10-c8-adapter"]
        self.assertTrue(
            {
                "bootstrap/gravity/src/gravity/compiler/c8_effect_checker_engine.gravity",
                "bootstrap/gravity/src/gravity/compiler/c7_type_checker_engine.gravity",
                "bootstrap/clojure/test/gravity/self_hosting/sh09_c7_effect_adapter_test.clj",
                "bootstrap/clojure/test/gravity/self_hosting/sh08_function_call_type_test.clj",
                "bootstrap/clojure/test/gravity/self_hosting/sh08_primitive_function_type_test.clj",
                "bootstrap/clojure/fixtures/self-hosting/sh-08/accepted/function-value-typed-bool.gravity",
            } <= set(adapter["inputs"])
        )
        public = by_id["stage5-public-c9"]
        self.assertEqual(public["jvm_heap"], "-J-Xmx2g")
        self.assertGreaterEqual(public["timeout_seconds"], 600)
        self.assertTrue(public["resource_receipt"].startswith("observed-peak"))
        proof = by_id["stage5-c9-proof-candidate"]
        self.assertEqual(proof["stage3_batch"], "c9-authority")
        self.assertEqual(proof["stage3_mode"], verifier._stage3.MODE_PROOF_CANDIDATE)
        self.assertFalse(proof["automatic"])
        self.assertTrue(proof["proof_candidate"])
        self.assertTrue(proof["attestation_required"])
        self.assertTrue(proof["no_resume"])
        self.assertEqual(proof["state_dir_policy"], "new-per-invocation")
        self.assertEqual(proof["jvm_heap"], "-J-Xmx8g")
        self.assertEqual(
            proof["depends_on"],
            ["stage5-public-c9", "stage5-sh10-c8-adapter"],
        )

    def test_stage5_change_impact_uses_only_fixed_c9_graph_and_manual_proof(self) -> None:
        manifest = verifier.load_manifest(ROOT / "tools" / "development_verification_manifest.json")

        def selected(path: str) -> set[str]:
            return set(verifier.select_impacted_checks(manifest, ROOT, changed_paths=[path])["selected_ids"])

        source = selected("bootstrap/gravity/src/gravity/compiler/c9_ownership_checker_engine.gravity")
        self.assertTrue(
            {
                "stage5-c9-source-structural",
                "stage5-c9-kernel",
                "stage5-public-c9",
                "stage5-sh10-c8-adapter",
            } <= source
        )
        self.assertNotIn("stage5-c9-proof-candidate", source)
        self.assertFalse(
            any(
                item.startswith("stage3-") and item != "stage3-runner-unit"
                or item.startswith("stage4-")
                for item in source
            )
        )
        self.assertNotIn("stage0-clojure-suite", source)
        self.assertNotIn("stage0-bootstrap-authority", source)

        kernel = selected("bootstrap/clojure/fixtures/self-hosting/sh-10/accepted/ownership-transitions.gravity")
        self.assertTrue({"stage5-c9-source-structural", "stage5-c9-kernel", "stage5-public-c9"} <= kernel)
        self.assertNotIn("stage5-sh10-c8-adapter", kernel)
        self.assertNotIn("stage5-c9-proof-candidate", kernel)
        self.assertNotIn("stage0-clojure-suite", kernel)
        self.assertNotIn("stage0-bootstrap-authority", kernel)

        adapter = selected("bootstrap/clojure/test/gravity/self_hosting/sh10_c8_ownership_adapter_test.clj")
        self.assertTrue({"stage5-c9-source-structural", "stage5-sh10-c8-adapter"} <= adapter)
        self.assertNotIn("stage5-c9-kernel", adapter)
        self.assertNotIn("stage5-public-c9", adapter)
        self.assertNotIn("stage5-c9-proof-candidate", adapter)
        self.assertNotIn("stage0-clojure-suite", adapter)
        self.assertNotIn("stage0-bootstrap-authority", adapter)

        c8 = selected("bootstrap/gravity/src/gravity/compiler/c8_effect_checker_engine.gravity")
        self.assertTrue({"stage5-c9-source-structural", "stage5-sh10-c8-adapter"} <= c8)
        self.assertNotIn("stage5-c9-kernel", c8)
        self.assertNotIn("stage5-public-c9", c8)

        partial = verifier.select_impacted_checks(
            manifest,
            ROOT,
            changed_paths=["bootstrap/clojure/test/gravity/self_hosting/sh07_c9_ownership_source_coverage_test.clj"],
        )
        self.assertEqual(partial["selected_ids"], [])
        self.assertEqual(
            partial["unmatched_changes"],
            ["bootstrap/clojure/test/gravity/self_hosting/sh07_c9_ownership_source_coverage_test.clj"],
        )
        explicit = verifier.select_impacted_checks(
            manifest,
            ROOT,
            requested_ids=["stage5-c9-proof-candidate"],
        )
        self.assertIn("stage5-public-c9", explicit["selected_ids"])
        self.assertIn("stage5-sh10-c8-adapter", explicit["selected_ids"])
        self.assertIn("stage5-c9-proof-candidate", explicit["selected_ids"])

    def test_stage5_resource_and_runtime_contracts_fail_closed_on_drift(self) -> None:
        manifest = verifier.load_manifest(ROOT / "tools" / "development_verification_manifest.json")
        for check_id in (
            "stage5-c9-source-structural",
            "stage5-c9-kernel",
            "stage5-sh10-c8-adapter",
            "stage5-public-c9",
            "stage5-c9-proof-candidate",
        ):
            broken = json.loads(json.dumps(manifest))
            target = next(item for item in broken["checks"] if item["id"] == check_id)
            target["tool_inputs"].remove("bootstrap/clojure/src/**")
            with self.assertRaisesRegex(verifier.ManifestError, "centralized Stage3 runtime inputs"):
                verifier.validate_manifest(broken)

        for check_id in (
            "stage5-c9-source-structural",
            "stage5-c9-kernel",
            "stage5-sh10-c8-adapter",
            "stage5-public-c9",
            "stage5-c9-proof-candidate",
        ):
            broken = json.loads(json.dumps(manifest))
            target = next(item for item in broken["checks"] if item["id"] == check_id)
            target["jvm_heap"] = "-J-Xmx8g" if target["jvm_heap"] == "-J-Xmx2g" else "-J-Xmx2g"
            target["minimum_heap_bytes"] = (
                8589934592 if target["jvm_heap"] == "-J-Xmx8g" else 2147483648
            )
            with self.assertRaisesRegex(verifier.ManifestError, "jvm_heap"):
                verifier.validate_manifest(broken)

        for check_id in (
            "stage5-c9-source-structural",
            "stage5-c9-kernel",
            "stage5-sh10-c8-adapter",
            "stage5-public-c9",
            "stage5-c9-proof-candidate",
        ):
            broken = json.loads(json.dumps(manifest))
            target = next(item for item in broken["checks"] if item["id"] == check_id)
            target["lock_owner"] = "runner"
            with self.assertRaisesRegex(verifier.ManifestError, "command-owned lock evidence"):
                verifier.validate_manifest(broken)

        broken = json.loads(json.dumps(manifest))
        broken["checks"] = [
            item for item in broken["checks"] if item["id"] != "stage5-sh10-c8-adapter"
        ]
        # The generic graph validator intentionally accepts disconnected
        # checks; this assertion protects the durable manifest contract from
        # silently dropping a Stage5 owner during review.
        stage5_ids = {item["id"] for item in broken["checks"] if item["id"].startswith("stage5-")}
        self.assertNotEqual(
            stage5_ids,
            {
                "stage5-c9-source-structural",
                "stage5-c9-kernel",
                "stage5-sh10-c8-adapter",
                "stage5-public-c9",
                "stage5-c9-proof-candidate",
            },
        )

    def test_stage6_fixed_graph_matches_runner_and_resource_policy(self) -> None:
        manifest = verifier.load_manifest(ROOT / "tools" / "development_verification_manifest.json")
        by_id = verifier.checks_by_id(manifest)
        expected_ids = [
            "stage6-c10-source-structural",
            "stage6-c10-kernel",
            "stage6-public-c10",
            "stage6-sh11-c9-safety-adapter",
            "stage6-c10-proof-candidate",
        ]
        self.assertEqual(
            [item["id"] for item in manifest["checks"] if item["id"].startswith("stage6-")],
            expected_ids,
        )
        expected_batches = {
            "stage6-c10-source-structural": "stage6-c10-source-structural",
            "stage6-c10-kernel": "stage6-c10-kernel",
            "stage6-public-c10": "stage6-public-c10",
            "stage6-sh11-c9-safety-adapter": "stage6-sh11-c9-safety-adapter",
            "stage6-c10-proof-candidate": "c10-authority",
        }
        self.assertEqual(
            {check_id: by_id[check_id]["stage3_batch"] for check_id in expected_ids},
            expected_batches,
        )
        expected_selectors = {
            "stage6-c10-source-structural": (
                "gravity.self-hosting.sh07-c10-safety-source-coverage-test/sh07-b31-c10-source-control-form-arities-are-bounded",
                "gravity.self-hosting.sh07-c10-safety-source-coverage-test/sh07-b31-c10-source-export-definitions-are-complete",
                "gravity.self-hosting.sh07-c10-safety-source-coverage-test/sh07-b31-proof-contract-registers-c10-source-exactly",
                "gravity.self-hosting.sh07-c10-safety-source-coverage-test/sh07-b31-c10-source-contracts-policy-outcomes-and-reasons-are-exact",
                "gravity.self-hosting.sh07-c10-safety-source-coverage-test/sh07-b31-c10-static-lookup-and-residual-boundaries-are-exact",
            ),
            "stage6-c10-kernel": (
                "gravity.self-hosting.sh11-numeric-safety-test/sh11-source-and-fixtures-compile-as-gravity",
                "gravity.self-hosting.sh11-numeric-safety-test/sh11-classifies-every-supported-operation-into-one-outcome",
                "gravity.self-hosting.sh11-numeric-safety-test/sh11-enforces-each-supported-mode-semantics",
                "gravity.self-hosting.sh11-numeric-safety-test/sh11-rejects-unresolved-and-invalid-numeric-safety",
                "gravity.self-hosting.sh11-numeric-safety-test/sh11-contains-i64-overflow-and-binds-index-and-shift-domains",
                "gravity.self-hosting.sh11-numeric-safety-test/sh11-fails-closed-on-schema-mode-lineage-and-structural-attacks",
                "gravity.self-hosting.sh11-numeric-safety-test/sh11-fails-closed-on-runtime-unsafe-and-result-attacks",
            ),
            "stage6-public-c10": (
                "gravity.bootstrap-test/public-check-accepts-gravity-authored-c10-safety-analysis-pipeline",
            ),
            "stage6-sh11-c9-safety-adapter": (
                "gravity.self-hosting.sh11-c9-safety-adapter-test/sh11-c9-safety-source-api-is-complete",
                "gravity.self-hosting.sh11-c9-safety-adapter-test/sh11-c9-identity-binding-is-sequential-and-exact",
                "gravity.self-hosting.sh11-c9-safety-adapter-test/sh11-c9-safety-adapter-binds-one-real-read",
                "gravity.self-hosting.sh11-c9-safety-adapter-test/sh11-generic-classifier-and-substitutions-fail-closed",
                "gravity.self-hosting.sh11-c9-safety-adapter-test/sh11-c9-safety-authenticated-gravity-boundary",
            ),
        }
        fixed = verifier._stage3._FIXED_BATCH_SELECTORS
        for check_id in expected_ids[:-1]:
            item = by_id[check_id]
            batch = item["stage3_batch"]
            self.assertEqual(tuple(fixed[batch]), expected_selectors[check_id], batch)
            self.assertEqual(item["command"], ["python3", "tools/run_stage3_verification.py"])
            self.assertEqual(item["lock_owner"], "command")
            self.assertEqual(item["lock"], str(verifier._stage3.CANONICAL_LOCK))
            self.assertTrue(item["exclusive"])
            self.assertEqual(item["capacity"], 1)
            self.assertTrue(item["fresh"])
            self.assertFalse(item["resume"])
            expected_heap = verifier._stage3.batch_command(batch)[1]
            self.assertEqual(item["jvm_heap"], expected_heap)
            self.assertEqual(
                item["minimum_heap_bytes"],
                2147483648 if expected_heap == "-J-Xmx2g" else 8589934592,
            )
            self.assertTrue(
                set(verifier._stage3.STAGE3_RUNTIME_DEPENDENCIES)
                <= set(item["inputs"]) | set(item["tool_inputs"])
            )
        source = by_id["stage6-c10-source-structural"]
        source_identity = verifier.input_identities(source, ROOT)
        source_record = next(
            item
            for item in source_identity["files"]
            if item["path"]
            == "bootstrap/gravity/src/gravity/compiler/c10_safety_analysis_pipeline.gravity"
        )
        self.assertEqual(source_record["size"], 112712)
        self.assertEqual(
            source_record["sha256"],
            "2d334872a84394acc636280796e205a74b227327aa3d646d6c19d55210bd4968",
        )
        adapter = by_id["stage6-sh11-c9-safety-adapter"]
        self.assertTrue(
            {
                "bootstrap/gravity/src/gravity/compiler/c9_ownership_checker_engine.gravity",
                "bootstrap/gravity/src/gravity/compiler/c8_effect_checker_engine.gravity",
                "bootstrap/gravity/src/gravity/compiler/c7_type_checker_engine.gravity",
                "bootstrap/clojure/fixtures/self-hosting/sh-08/accepted/function-single-bool-call.gravity",
                "bootstrap/clojure/fixtures/self-hosting/sh-08/accepted/function-single-bool-call.qst",
            }
            <= set(adapter["inputs"])
        )
        proof = by_id["stage6-c10-proof-candidate"]
        self.assertEqual(proof["stage3_mode"], verifier._stage3.MODE_PROOF_CANDIDATE)
        self.assertFalse(proof["automatic"])
        self.assertTrue(proof["proof_candidate"])
        self.assertTrue(proof["attestation_required"])
        self.assertTrue(proof["no_resume"])
        self.assertEqual(proof["state_dir_policy"], "new-per-invocation")
        self.assertEqual(
            proof["depends_on"],
            ["stage6-public-c10", "stage6-sh11-c9-safety-adapter"],
        )

    def test_stage6_change_impact_is_proportional_and_proof_is_manual(self) -> None:
        manifest = verifier.load_manifest(ROOT / "tools" / "development_verification_manifest.json")

        def selected(path: str) -> set[str]:
            return set(
                verifier.select_impacted_checks(manifest, ROOT, changed_paths=[path])["selected_ids"]
            )

        units = {
            "stage0-orchestrator-unit",
            "stage1-sh01-unit",
            "stage2-authority-admission-unit",
            "stage3-runner-unit",
        }
        source = selected(
            "bootstrap/gravity/src/gravity/compiler/c10_safety_analysis_pipeline.gravity"
        )
        self.assertEqual(
            source,
            units
            | {
                "stage6-c10-source-structural",
                "stage6-c10-kernel",
                "stage6-public-c10",
                "stage6-sh11-c9-safety-adapter",
                "stage7-c11-source-structural",
                "stage7-sh12-c10-mir-adapter",
                "stage8-c12-source-shape",
                "stage8-sh13-c11-domain-evidence",
                "stage9-c13-source-shape",
                "stage9-sh16-c13-evidence-boundary",
            },
        )
        kernel = selected(
            "bootstrap/clojure/test/gravity/self_hosting/sh11_numeric_safety_test.clj"
        )
        self.assertEqual(
            kernel,
            units
            | {
                "stage6-c10-source-structural",
                "stage6-c10-kernel",
                "stage6-public-c10",
            },
        )
        adapter = selected(
            "bootstrap/clojure/test/gravity/self_hosting/sh11_c9_safety_adapter_test.clj"
        )
        self.assertEqual(
            adapter,
            units
            | {
                "stage6-c10-source-structural",
                "stage6-sh11-c9-safety-adapter",
                "stage7-c11-source-structural",
                "stage7-sh12-c10-mir-adapter",
                "stage8-c12-source-shape",
                "stage8-sh13-c11-domain-evidence",
                "stage9-c13-source-shape",
                "stage9-sh16-c13-evidence-boundary",
            },
        )
        for upstream in (
            "bootstrap/gravity/src/gravity/compiler/c9_ownership_checker_engine.gravity",
            "bootstrap/gravity/src/gravity/compiler/c8_effect_checker_engine.gravity",
        ):
            routed = selected(upstream)
            self.assertTrue(
                {"stage6-c10-source-structural", "stage6-sh11-c9-safety-adapter"}
                <= routed
            )
            self.assertNotIn("stage6-c10-kernel", routed)
            self.assertNotIn("stage6-public-c10", routed)
            self.assertNotIn("stage6-c10-proof-candidate", routed)
        partial = verifier.select_impacted_checks(
            manifest,
            ROOT,
            changed_paths=[
                "bootstrap/clojure/test/gravity/self_hosting/sh07_c10_safety_source_coverage_test.clj"
            ],
        )
        self.assertEqual(partial["selected_ids"], [])
        self.assertEqual(len(partial["unmatched_changes"]), 1)
        explicit = verifier.select_impacted_checks(
            manifest, ROOT, requested_ids=["stage6-c10-proof-candidate"]
        )
        self.assertTrue(
            {
                "stage6-public-c10",
                "stage6-sh11-c9-safety-adapter",
                "stage6-c10-proof-candidate",
            }
            <= set(explicit["selected_ids"])
        )

    def test_stage6_resource_and_runtime_contracts_fail_closed_on_drift(self) -> None:
        manifest = verifier.load_manifest(ROOT / "tools" / "development_verification_manifest.json")
        check_ids = (
            "stage6-c10-source-structural",
            "stage6-c10-kernel",
            "stage6-public-c10",
            "stage6-sh11-c9-safety-adapter",
            "stage6-c10-proof-candidate",
        )
        for check_id in check_ids:
            broken = json.loads(json.dumps(manifest))
            target = next(item for item in broken["checks"] if item["id"] == check_id)
            target["tool_inputs"].remove("bootstrap/clojure/src/**")
            with self.assertRaisesRegex(verifier.ManifestError, "centralized Stage3 runtime inputs"):
                verifier.validate_manifest(broken)
        for check_id in check_ids:
            broken = json.loads(json.dumps(manifest))
            target = next(item for item in broken["checks"] if item["id"] == check_id)
            target["jvm_heap"] = "-J-Xmx8g" if target["jvm_heap"] == "-J-Xmx2g" else "-J-Xmx2g"
            target["minimum_heap_bytes"] = (
                8589934592 if target["jvm_heap"] == "-J-Xmx8g" else 2147483648
            )
            with self.assertRaisesRegex(verifier.ManifestError, "jvm_heap"):
                verifier.validate_manifest(broken)

    def test_real_manifest_partial_stage3_source_files_fail_closed_as_deferred(self) -> None:
        manifest = verifier.load_manifest(ROOT / "tools" / "development_verification_manifest.json")
        for changed in (
            "bootstrap/clojure/test/gravity/self_hosting/sh07_c7_type_source_coverage_test.clj",
            "bootstrap/clojure/test/gravity/self_hosting/sh07_authoritative_coverage_census_test.clj",
        ):
            selection = verifier.select_impacted_checks(manifest, ROOT, changed_paths=[changed])
            self.assertEqual(selection["selected_ids"], [])
            self.assertEqual(selection["unmatched_changes"], [changed])
            self.assertFalse(any(item.startswith("stage3-") for item in selection["selected_ids"]))

    def test_real_manifest_stage4_fixed_graph_matches_runner_policy(self) -> None:
        manifest = verifier.load_manifest(ROOT / "tools" / "development_verification_manifest.json")
        by_id = verifier.checks_by_id(manifest)
        stage4_ids = [
            "stage4-c8-source-structural",
            "stage4-sh09-adapter",
            "stage4-public-c8",
            "stage4-c8-proof-candidate",
        ]
        self.assertEqual(
            [item["id"] for item in manifest["checks"] if item["id"].startswith("stage4-")],
            stage4_ids,
        )
        self.assertEqual(
            {by_id[item]["stage3_batch"] for item in stage4_ids},
            {
                "stage4-c8-source-structural",
                "stage4-sh09-adapter",
                "stage4-public-c8",
                "c8-authority",
            },
        )
        runner_batches = verifier._stage3._FIXED_BATCH_SELECTORS
        expected_selectors = {
            "stage4-c8-source-structural": (
                "gravity.self-hosting.sh07-c8-effect-source-coverage-test/sh07-b29-proof-contract-registers-c8-source-exactly",
                "gravity.self-hosting.sh07-c8-effect-source-coverage-test/sh07-b29-c8-source-control-form-arities-are-bounded",
                "gravity.self-hosting.sh07-c8-effect-source-coverage-test/sh07-b29-c8-source-contracts-policy-and-boundaries-are-exact",
                "gravity.self-hosting.sh07-c8-effect-source-coverage-test/sh07-b29-c8-structural-limitations-remain-explicit",
            ),
            "stage4-sh09-adapter": (
                "gravity.self-hosting.sh09-c7-effect-adapter-test/sh09-c7-adapter-source-structure-and-policy-are-exact",
                "gravity.self-hosting.sh09-c7-effect-adapter-test/sh09-c7-adapter-derives-one-pure-effect-fact-per-type-fact",
                "gravity.self-hosting.sh09-c7-effect-adapter-test/sh09-c7-adapter-rejects-upstream-and-candidate-substitution",
                "gravity.self-hosting.sh09-c7-effect-adapter-test/sh09-c7-adapter-derives-declared-pure-function-call-effects",
                "gravity.self-hosting.sh09-c7-effect-adapter-test/sh09-c7-adapter-binds-ordered-effect-identities",
                "gravity.self-hosting.sh09-c7-effect-adapter-test/sh09-c7-adapter-authenticated-gravity-boundary",
            ),
            "stage4-public-c8": (
                "gravity.bootstrap-test/public-check-accepts-gravity-authored-c8-effect-checker-engine",
            ),
        }
        for check_id in stage4_ids[:-1]:
            check_item = by_id[check_id]
            batch = check_item["stage3_batch"]
            self.assertEqual(tuple(runner_batches[batch]), expected_selectors[batch], batch)
            self.assertEqual(
                check_item["command"],
                ["python3", "tools/run_stage3_verification.py"],
            )
            self.assertEqual(check_item["lock_owner"], "command")
            self.assertEqual(check_item["lock"], str(verifier._stage3.CANONICAL_LOCK))
            self.assertTrue(check_item["exclusive"])
            self.assertEqual(check_item["capacity"], 1)
            self.assertTrue(check_item["fresh"])
            self.assertFalse(check_item["resume"])
            self.assertEqual(check_item["stage3_mode"], verifier._stage3.MODE_PURE)
            expected_heap = verifier._stage3.batch_command(batch)[1]
            self.assertEqual(check_item["jvm_heap"], expected_heap)
            self.assertEqual(
                check_item["minimum_heap_bytes"],
                2147483648 if expected_heap == "-J-Xmx2g" else 8589934592,
            )
            self.assertTrue(
                set(verifier._stage3.STAGE3_RUNTIME_DEPENDENCIES)
                <= set(check_item["inputs"]) | set(check_item["tool_inputs"]),
                msg=check_id,
            )
        source = by_id["stage4-c8-source-structural"]
        self.assertEqual(
            source["inputs"][0],
            "bootstrap/gravity/src/gravity/compiler/c8_effect_checker_engine.gravity",
        )
        self.assertIn(
            "bootstrap/clojure/test/gravity/self_hosting/sh07_c8_effect_source_coverage_test.clj",
            source["impact_excludes"],
        )
        self.assertEqual(
            len(
                [item for item in source["inputs"] if item.startswith("docs/")]
            ),
            29,
        )
        self.assertTrue(
            {
                "bootstrap/clojure/test/gravity/self_hosting/sh07_proof_contract.edn",
                "bootstrap/gravity/src/gravity/compiler/c8_effect_checker_engine.gravity",
            } <= set(source["inputs"])
        )
        synthetic = by_id["stage4-sh09-adapter"]
        self.assertIn(
            "bootstrap/clojure/test/gravity/self_hosting/sh09_c7_effect_adapter_test.clj",
            synthetic["inputs"],
        )
        authenticated = by_id["stage4-sh09-adapter"]
        self.assertTrue(
            {
                "bootstrap/clojure/test/gravity/self_hosting/sh08_function_call_type_test.clj",
                "bootstrap/clojure/test/gravity/self_hosting/sh08_primitive_function_type_test.clj",
                "bootstrap/gravity/src/gravity/compiler/c7_type_checker_engine.gravity",
                "bootstrap/clojure/fixtures/self-hosting/sh-08/accepted/function-value-typed-bool.gravity",
            } <= set(authenticated["inputs"])
        )
        public = by_id["stage4-public-c8"]
        self.assertGreaterEqual(public["timeout_seconds"], 600)
        self.assertEqual(public["jvm_heap"], "-J-Xmx2g")
        self.assertTrue(
            {
                "bin/gravity",
                "target/phase-18/jvm-cli/gravity-jvm-cli.jar",
                "docs/artifacts/phase-15/bootstrap/p15-s23-final-seed-retirement-proof.edn",
            } <= set(public["inputs"])
        )
        proof = by_id["stage4-c8-proof-candidate"]
        self.assertEqual(proof["stage3_batch"], "c8-authority")
        self.assertEqual(proof["stage3_mode"], verifier._stage3.MODE_PROOF_CANDIDATE)
        self.assertFalse(proof["automatic"])
        self.assertTrue(proof["proof_candidate"])
        self.assertTrue(proof["attestation_required"])
        self.assertTrue(proof["no_resume"])
        self.assertEqual(proof["jvm_heap"], "-J-Xmx8g")
        self.assertTrue(
            {
                "bootstrap/clojure/test/gravity/self_hosting/sh07_authoritative_runner.clj",
                "bootstrap/clojure/test/gravity/self_hosting/sh07_proof_contract.edn",
            } <= set(proof["tool_inputs"])
        )
        self.assertEqual(
            proof["depends_on"],
            ["stage4-public-c8"],
        )

    def test_real_manifest_stage4_dependency_order_is_sequential_and_proof_manual(self) -> None:
        manifest = verifier.load_manifest(ROOT / "tools" / "development_verification_manifest.json")
        by_id = verifier.checks_by_id(manifest)
        order = verifier.topological_order(manifest)
        position = {check_id: index for index, check_id in enumerate(order)}
        chain = [
            "stage3-runner-unit",
            "stage4-c8-source-structural",
            "stage4-sh09-adapter",
            "stage4-public-c8",
            "stage4-c8-proof-candidate",
        ]
        self.assertEqual(chain, sorted(chain, key=position.__getitem__))
        for left, right in zip(chain, chain[1:]):
            self.assertIn(left, verifier.dependencies_of(by_id[right]))
        self.assertFalse(by_id["stage4-c8-proof-candidate"]["automatic"])
        self.assertEqual(by_id["stage4-c8-proof-candidate"]["authority"], "none")

    def test_real_manifest_stage4_change_impact_routes_and_defers_partial_coverage(self) -> None:
        manifest = verifier.load_manifest(ROOT / "tools" / "development_verification_manifest.json")
        source_path = "bootstrap/gravity/src/gravity/compiler/c8_effect_checker_engine.gravity"
        source_selection = verifier.select_impacted_checks(manifest, ROOT, changed_paths=[source_path])
        source_selected = set(source_selection["selected_ids"])
        expected_c8_change_selection = {
            "stage0-orchestrator-unit",
            "stage1-sh01-unit",
            "stage2-authority-admission-unit",
            "stage3-runner-unit",
            "stage4-c8-source-structural",
            "stage4-sh09-adapter",
            "stage4-public-c8",
            "stage5-c9-source-structural",
            "stage5-sh10-c8-adapter",
            "stage6-c10-source-structural",
            "stage6-sh11-c9-safety-adapter",
            "stage7-c11-source-structural",
            "stage7-sh12-c10-mir-adapter",
            "stage8-c12-source-shape",
            "stage8-sh13-c11-domain-evidence",
            "stage9-c13-source-shape",
            "stage9-sh16-c13-evidence-boundary",
        }
        self.assertEqual(source_selected, expected_c8_change_selection)
        self.assertTrue(
            {
                "stage4-c8-source-structural",
                "stage4-sh09-adapter",
                "stage4-public-c8",
            } <= source_selected
        )
        self.assertNotIn("stage4-c8-proof-candidate", source_selected)
        self.assertNotIn("stage0-clojure-suite", source_selected)
        self.assertNotIn("stage0-bootstrap-authority", source_selected)
        self.assertFalse(
            {
                "stage3-source-control-form-arity",
                "stage3-coverage-census-contract",
                "stage3-fragment-size-preflight",
                "stage3-source-plan-contract",
                "stage3-primitive-pure",
                "stage3-recursive-pure",
                "stage3-authoritative-ho-pure",
                "stage3-primitive-bool-authenticated",
                "stage3-recursive-authenticated",
                "stage3-authoritative-ho-authenticated",
                "stage3-public-c7-check",
                "stage3-c7-proof-candidate",
            } & source_selected
        )
        adapter_path = "bootstrap/clojure/test/gravity/self_hosting/sh09_c7_effect_adapter_test.clj"
        adapter_selection = verifier.select_impacted_checks(manifest, ROOT, changed_paths=[adapter_path])
        adapter_selected = set(adapter_selection["selected_ids"])
        self.assertEqual(adapter_selected, expected_c8_change_selection)
        self.assertTrue(
            {
                "stage4-sh09-adapter",
                "stage4-public-c8",
            } <= adapter_selected
        )
        self.assertNotIn("stage4-c8-proof-candidate", adapter_selected)
        self.assertFalse("stage3-public-c7-check" in adapter_selected)
        partial_path = "bootstrap/clojure/test/gravity/self_hosting/sh07_c8_effect_source_coverage_test.clj"
        partial_selection = verifier.select_impacted_checks(manifest, ROOT, changed_paths=[partial_path])
        self.assertEqual(partial_selection["selected_ids"], [])
        self.assertEqual(partial_selection["unmatched_changes"], [partial_path])
        explicit = verifier.select_impacted_checks(
            manifest,
            ROOT,
            requested_ids=["stage4-c8-proof-candidate"],
        )
        self.assertIn("stage4-c8-proof-candidate", explicit["selected_ids"])
        self.assertIn("stage4-public-c8", explicit["selected_ids"])
        all_selected = verifier.select_impacted_checks(
            manifest,
            ROOT,
            all_checks=True,
        )
        self.assertIn("stage4-c8-proof-candidate", all_selected["selected_ids"])

        # Every Stage3/SH07 runtime identity input is also consumed by the
        # automatic Stage4 production nodes.  A shared-runtime edit therefore
        # refreshes both fixed graphs, while the multi-hour proof remains
        # manual-only under change-impact routing.
        for runtime_path in list(verifier._stage3._sh07.SHARED_GRAVITY_FILES) + [
            "bootstrap/clojure/src/gravity/diagnostics.clj",
        ]:
            runtime_selection = verifier.select_impacted_checks(
                manifest,
                ROOT,
                changed_paths=[runtime_path],
            )
            runtime_selected = set(runtime_selection["selected_ids"])
            self.assertTrue(
                {
                    "stage4-c8-source-structural",
                    "stage4-sh09-adapter",
                    "stage4-public-c8",
                } <= runtime_selected,
                runtime_path,
            )
            self.assertNotIn("stage4-c8-proof-candidate", runtime_selected, runtime_path)

    def test_real_manifest_stage4_fixed_selector_membership_is_unique(self) -> None:
        manifest = verifier.load_manifest(ROOT / "tools" / "development_verification_manifest.json")
        expected = {
            "stage4-c8-source-structural",
            "stage4-sh09-adapter",
            "stage4-public-c8",
        }
        fixed = verifier._stage3._FIXED_BATCH_SELECTORS
        for batch in expected:
            selectors = tuple(fixed[batch])
            self.assertEqual(len(selectors), len(set(selectors)), batch)
            self.assertTrue(all("/" in selector for selector in selectors), batch)
            self.assertEqual(
                verifier.checks_by_id(manifest)[next(
                    item["id"] for item in manifest["checks"]
                    if item.get("stage3_batch") == batch
                )]["stage3_batch"],
                batch,
            )

    def test_real_manifest_stage7_fixed_graph_and_impact_are_exact(self) -> None:
        manifest = verifier.load_manifest(ROOT / "tools" / "development_verification_manifest.json")
        by_id = verifier.checks_by_id(manifest)
        stage7_ids = {
            "stage7-c11-source-structural",
            "stage7-sh12-c10-mir-adapter",
            "stage7-public-c11",
            "stage7-c11-proof-candidate",
        }
        self.assertEqual(
            {check_id for check_id in by_id if check_id.startswith("stage7-")},
            stage7_ids,
        )
        source = by_id["stage7-c11-source-structural"]
        adapter = by_id["stage7-sh12-c10-mir-adapter"]
        public = by_id["stage7-public-c11"]
        proof = by_id["stage7-c11-proof-candidate"]
        self.assertEqual(source["stage3_batch"], "stage7-c11-source-preflight")
        self.assertEqual(adapter["stage3_batch"], "stage7-sh12-c10-mir-adapter")
        self.assertEqual(public["stage3_batch"], "stage7-public-c11")
        self.assertEqual(proof["stage3_batch"], "c11-authority")
        self.assertEqual(source["jvm_heap"], "-J-Xmx512m")
        self.assertEqual(adapter["jvm_heap"], "-J-Xmx8g")
        self.assertEqual(public["jvm_heap"], "-J-Xmx2g")
        self.assertEqual(proof["jvm_heap"], "-J-Xmx8g")
        self.assertEqual(adapter["depends_on"], [source["id"]])
        self.assertEqual(public["depends_on"], [source["id"]])
        self.assertEqual(proof["depends_on"], [adapter["id"], public["id"]])
        self.assertFalse(proof["automatic"])
        self.assertTrue(proof["fresh"])
        self.assertTrue(proof["no_resume"])
        self.assertTrue(proof["proof_candidate"])
        self.assertTrue(proof["attestation_required"])
        self.assertEqual(proof["authority"], "none")
        self.assertEqual(proof["state_dir_policy"], "new-per-invocation")
        for check in (source, adapter, public, proof):
            self.assertEqual(check["lock"], "/private/tmp/gravity-sh07-heavy.lock")
            self.assertEqual(check["lock_owner"], "command")
            self.assertTrue(check["exclusive"])
            self.assertEqual(check["capacity"], 1)

        selectors = tuple(verifier._stage3._FIXED_BATCH_SELECTORS[adapter["stage3_batch"]])
        self.assertEqual(len(selectors), 6)
        self.assertEqual(len(selectors), len(set(selectors)))
        self.assertTrue(selectors[0].endswith("/sh12-c10-verification-envelope-preflight"))
        self.assertTrue(selectors[-1].endswith("/sh12-c10-authenticated-gravity-boundary"))

        units = {
            "stage0-orchestrator-unit",
            "stage1-sh01-unit",
            "stage2-authority-admission-unit",
            "stage3-runner-unit",
        }

        def selected(path: str) -> set[str]:
            return set(
                verifier.select_impacted_checks(manifest, ROOT, changed_paths=[path])["selected_ids"]
            )

        self.assertEqual(
            selected("bootstrap/gravity/src/gravity/compiler/c11_mir_specification.gravity"),
            units
            | {
                source["id"],
                adapter["id"],
                public["id"],
                "stage8-c12-source-shape",
                "stage8-sh13-c11-domain-evidence",
                "stage9-c13-source-shape",
                "stage9-sh16-c13-evidence-boundary",
            },
        )
        self.assertEqual(
            selected("bootstrap/clojure/test/gravity/self_hosting/sh12_c10_mir_adapter_test.clj"),
            units
            | {
                source["id"],
                adapter["id"],
                "stage8-c12-source-shape",
                "stage8-sh13-c11-domain-evidence",
                "stage9-c13-source-shape",
                "stage9-sh16-c13-evidence-boundary",
            },
        )
        self.assertEqual(
            selected(
                "bootstrap/clojure/test/gravity/self_hosting/sh07_c11_mir_source_preflight_test.clj"
            ),
            units | {source["id"], adapter["id"], public["id"]},
        )
        explicit = set(
            verifier.select_impacted_checks(
                manifest, ROOT, requested_ids=[proof["id"]]
            )["selected_ids"]
        )
        self.assertEqual(explicit, units | stage7_ids)

        legacy_ids = {
            "stage0-hosted-hello",
            "stage0-hosted-hello-qst",
            "stage0-selective-smoke",
            "stage0-hosted-core-app",
            "stage0-hosted-core-compiled-app",
            "stage0-clojure-suite",
            "stage0-bootstrap-authority",
        }
        c11_path = "bootstrap/gravity/src/gravity/compiler/c11_mir_specification.gravity"
        for check_id in legacy_ids:
            self.assertEqual(by_id[check_id]["impact_excludes"].count(c11_path), 1, check_id)
        for test_path in (
            "bootstrap/clojure/test/gravity/self_hosting/sh07_c11_mir_source_preflight_test.clj",
            "bootstrap/clojure/test/gravity/self_hosting/sh12_c10_mir_adapter_test.clj",
        ):
            self.assertEqual(
                by_id["stage1-sh01-unit"]["impact_excludes"].count(test_path),
                1,
                test_path,
            )

    def test_real_manifest_stage8_fixed_graph_and_impact_are_exact(self) -> None:
        manifest = verifier.load_manifest(ROOT / "tools" / "development_verification_manifest.json")
        by_id = verifier.checks_by_id(manifest)
        stage8_ids = {
            "stage8-c12-source-shape",
            "stage8-sh13-c11-domain-evidence",
            "stage8-public-c12",
        }
        self.assertEqual(
            {check_id for check_id in by_id if check_id.startswith("stage8-")},
            stage8_ids,
        )
        source = by_id["stage8-c12-source-shape"]
        adapter = by_id["stage8-sh13-c11-domain-evidence"]
        public = by_id["stage8-public-c12"]
        self.assertEqual(source["stage3_batch"], "stage8-c12-source-shape")
        self.assertEqual(adapter["stage3_batch"], "stage8-sh13-c11-domain-evidence")
        self.assertEqual(public["stage3_batch"], "stage8-public-c12")
        self.assertEqual(source["jvm_heap"], "-J-Xmx512m")
        self.assertEqual(adapter["jvm_heap"], "-J-Xmx8g")
        self.assertEqual(public["jvm_heap"], "-J-Xmx2g")
        self.assertEqual(adapter["depends_on"], [source["id"]])
        self.assertEqual(public["depends_on"], [source["id"]])
        for check in (source, adapter, public):
            self.assertEqual(check["authority"], "none")
            self.assertTrue(check["automatic"])
            self.assertEqual(check["lock"], "/private/tmp/gravity-sh07-heavy.lock")
            self.assertEqual(check["lock_owner"], "command")
            self.assertTrue(check["exclusive"])
            self.assertEqual(check["capacity"], 1)

        units = {
            "stage0-orchestrator-unit",
            "stage1-sh01-unit",
            "stage2-authority-admission-unit",
            "stage3-runner-unit",
        }

        def selected(path: str) -> set[str]:
            return set(
                verifier.select_impacted_checks(
                    manifest, ROOT, changed_paths=[path]
                )["selected_ids"]
            )

        c12_path = "bootstrap/gravity/src/gravity/compiler/c12_domain_ir_architecture.gravity"
        stage9_ids = {
            "stage9-c13-source-shape",
            "stage9-sh16-c13-evidence-boundary",
        }
        self.assertEqual(selected(c12_path), units | stage8_ids | stage9_ids)
        self.assertEqual(
            selected(
                "bootstrap/clojure/test/gravity/self_hosting/"
                "sh07_c12_domain_ir_shape_preflight_test.clj"
            ),
            units | stage8_ids,
        )
        self.assertEqual(
            selected(
                "bootstrap/clojure/test/gravity/self_hosting/"
                "sh13_c11_domain_evidence_adapter_test.clj"
            ),
            units | {source["id"], adapter["id"]} | stage9_ids,
        )
        for unrelated in (
            "bootstrap/gravity/src/gravity/compiler/c7_type_checker_engine.gravity",
            "bootstrap/clojure/test/gravity/self_hosting/sh08_function_call_type_test.clj",
            "bootstrap/clojure/test/gravity/self_hosting/sh08_primitive_function_type_test.clj",
            "bootstrap/clojure/fixtures/self-hosting/sh-08/accepted/function-single-bool-call.gravity",
            "bootstrap/clojure/fixtures/self-hosting/sh-08/accepted/function-single-bool-call.qst",
        ):
            self.assertTrue(
                stage8_ids.isdisjoint(selected(unrelated)),
                unrelated,
            )
        legacy_ids = {
            "stage0-hosted-hello",
            "stage0-hosted-hello-qst",
            "stage0-selective-smoke",
            "stage0-hosted-core-app",
            "stage0-hosted-core-compiled-app",
            "stage0-clojure-suite",
            "stage0-bootstrap-authority",
        }
        for check_id in legacy_ids:
            self.assertEqual(
                by_id[check_id]["impact_excludes"].count(c12_path), 1, check_id
            )
        for test_path in (
            "bootstrap/clojure/test/gravity/self_hosting/sh07_c12_domain_ir_shape_preflight_test.clj",
            "bootstrap/clojure/test/gravity/self_hosting/sh13_c11_domain_evidence_adapter_test.clj",
        ):
            self.assertEqual(
                by_id["stage1-sh01-unit"]["impact_excludes"].count(test_path),
                1,
                test_path,
            )
        c12_source = ROOT / c12_path
        c12_sha = "sha256:" + hashlib.sha256(c12_source.read_bytes()).hexdigest()
        self.assertEqual(
            c12_sha,
            "sha256:6d56e7a0484be3abdf395ef41d5ecae85c47f090c263c08010f08ce82a8348d9",
        )
        self.assertIn(
            c12_sha,
            (ROOT / "bootstrap/clojure/test/gravity/bootstrap_test.clj").read_text(),
        )
        self.assertFalse(any(check_id.endswith("proof-candidate") for check_id in stage8_ids))
        self.assertNotIn("c12-authority", verifier._stage3.FIXED_MODULE_POLICIES)

    def test_real_manifest_stage9_evidence_boundary_is_exact_and_proportional(self) -> None:
        manifest = verifier.load_manifest(ROOT / "tools" / "development_verification_manifest.json")
        by_id = verifier.checks_by_id(manifest)
        stage9_ids = {
            check_id for check_id in by_id if check_id.startswith("stage9-")
        }
        self.assertEqual(
            stage9_ids,
            {
                "stage9-c13-source-shape",
                "stage9-sh16-c13-evidence-boundary",
            },
        )
        shape = by_id["stage9-c13-source-shape"]
        boundary = by_id["stage9-sh16-c13-evidence-boundary"]
        self.assertEqual(shape["stage3_batch"], shape["id"])
        self.assertEqual(shape["stage3_mode"], verifier._stage3.MODE_PURE)
        self.assertEqual(shape["depends_on"], ["stage3-runner-unit"])
        self.assertEqual(shape["jvm_heap"], "-J-Xmx512m")
        self.assertEqual(shape["minimum_heap_bytes"], 536870912)
        self.assertEqual(shape["timeout_seconds"], 600)
        self.assertEqual(boundary["stage3_batch"], boundary["id"])
        self.assertEqual(boundary["stage3_mode"], verifier._stage3.MODE_PURE)
        self.assertEqual(boundary["depends_on"], [shape["id"]])
        self.assertEqual(boundary["jvm_heap"], "-J-Xmx8g")
        self.assertEqual(boundary["minimum_heap_bytes"], 8589934592)
        self.assertEqual(boundary["timeout_seconds"], 1800)
        for check in (shape, boundary):
            self.assertEqual(check["authority"], "none")
            self.assertTrue(check["automatic"])
            self.assertTrue(check["fresh"])
            self.assertFalse(check["resume"])
            self.assertEqual(check["state_dir_policy"], "new-per-invocation")
            self.assertEqual(check["lock"], "/private/tmp/gravity-sh07-heavy.lock")
            self.assertEqual(check["lock_owner"], "command")
            self.assertTrue(check["exclusive"])
            self.assertEqual(check["capacity"], 1)

        shape_selectors = verifier._stage3._FIXED_BATCH_SELECTORS[shape["stage3_batch"]]
        self.assertEqual(2, len(shape_selectors))
        self.assertEqual(2, len(set(shape_selectors)))
        selectors = verifier._stage3._FIXED_BATCH_SELECTORS[boundary["stage3_batch"]]
        self.assertEqual(4, len(selectors))
        self.assertEqual(4, len(set(selectors)))
        self.assertTrue(selectors[0].endswith("/sh16-c13-evidence-boundary-surface"))
        self.assertTrue(
            selectors[-1].endswith(
                "/sh16-c13-evidence-boundary-separates-top-level-provenance"
            )
        )

        units = {
            "stage0-orchestrator-unit",
            "stage1-sh01-unit",
            "stage2-authority-admission-unit",
            "stage3-runner-unit",
        }

        def selected(path: str) -> set[str]:
            return set(
                verifier.select_impacted_checks(
                    manifest, ROOT, changed_paths=[path]
                )["selected_ids"]
            )

        c13_path = "bootstrap/gravity/src/gravity/compiler/c13_mir_optimization_passes.gravity"
        sh16_path = (
            "bootstrap/clojure/test/gravity/self_hosting/"
            "sh16_c12_domain_evidence_boundary_test.clj"
        )
        self.assertEqual(selected(c13_path), units | stage9_ids)
        self.assertEqual(selected(sh16_path), units | stage9_ids)
        self.assertNotIn("stage8-c12-source-shape", selected(c13_path))
        self.assertNotIn("stage8-sh13-c11-domain-evidence", selected(c13_path))
        self.assertNotIn("stage8-public-c12", selected(c13_path))
        self.assertFalse(any(check_id.endswith("proof-candidate") for check_id in stage9_ids))

        legacy_ids = {
            "stage0-hosted-hello",
            "stage0-hosted-hello-qst",
            "stage0-selective-smoke",
            "stage0-hosted-core-app",
            "stage0-hosted-core-compiled-app",
            "stage0-clojure-suite",
            "stage0-bootstrap-authority",
        }
        for check_id in legacy_ids:
            self.assertEqual(
                by_id[check_id]["impact_excludes"].count(c13_path), 1, check_id
            )
        self.assertEqual(
            by_id["stage1-sh01-unit"]["impact_excludes"].count(sh16_path), 1
        )
        self.assertEqual(
            by_id["stage1-sh01-unit"]["impact_excludes"].count(
                "bootstrap/clojure/test/gravity/self_hosting/"
                "sh07_c13_mir_optimization_shape_preflight_test.clj"
            ),
            1,
        )

    def test_stage9_runtime_resource_lifecycle_and_node_drift_fail_closed(self) -> None:
        manifest = verifier.load_manifest(
            ROOT / "tools" / "development_verification_manifest.json"
        )
        wrong_same_heap_batch = {
            "stage9-c13-source-shape": "stage8-c12-source-shape",
            "stage9-sh16-c13-evidence-boundary": "stage8-sh13-c11-domain-evidence",
        }
        for check_id, policy in verifier._STAGE9_FIXED_NODE_POLICIES.items():
            mutations = (
                ("timeout_seconds", policy["timeout_seconds"] - 1),
                ("fresh", False),
                ("resume", True),
                ("automatic", False),
                ("exclusive", False),
                ("capacity", 2),
                ("state_dir_policy", "reused"),
                ("lock", "/private/tmp/gravity-stage9-wrong.lock"),
                ("lock_owner", "runner"),
                ("stage3_batch", wrong_same_heap_batch[check_id]),
                ("depends_on", []),
                ("stage3_mode", verifier._stage3.MODE_PROOF_CANDIDATE),
                ("command", [sys.executable, "-c", "pass"]),
                ("authority", "declared"),
            )
            for field, value in mutations:
                broken = json.loads(json.dumps(manifest))
                target = next(
                    item for item in broken["checks"] if item["id"] == check_id
                )
                target[field] = value
                with self.assertRaisesRegex(verifier.ManifestError, field):
                    verifier.validate_manifest(broken)
            for required_input in policy["inputs"]:
                broken = json.loads(json.dumps(manifest))
                target = next(
                    item for item in broken["checks"] if item["id"] == check_id
                )
                target["inputs"].remove(required_input)
                with self.assertRaisesRegex(verifier.ManifestError, "inputs"):
                    verifier.validate_manifest(broken)
            for field, value in (
                ("inputs", [*policy["inputs"], "README.md"]),
                ("tool_inputs", [*policy["tool_inputs"], "tools/validate_reader.py"]),
                ("impact_excludes", []),
                ("impact_excludes", [*policy["impact_excludes"], "README.md"]),
                ("impact_excludes", [*policy["impact_excludes"], policy["inputs"][0]]),
            ):
                broken = json.loads(json.dumps(manifest))
                target = next(
                    item for item in broken["checks"] if item["id"] == check_id
                )
                target[field] = list(value)
                with self.assertRaisesRegex(verifier.ManifestError, field):
                    verifier.validate_manifest(broken)

    def test_stage8_runtime_resource_lifecycle_and_node_drift_fail_closed(self) -> None:
        manifest = verifier.load_manifest(
            ROOT / "tools" / "development_verification_manifest.json"
        )
        check_ids = (
            "stage8-c12-source-shape",
            "stage8-sh13-c11-domain-evidence",
            "stage8-public-c12",
        )
        wrong_same_heap_batch = {
            "stage8-c12-source-shape": "stage7-c11-shape-preflight",
            "stage8-sh13-c11-domain-evidence": "stage7-sh12-c10-mir-adapter",
            "stage8-public-c12": "stage7-public-c11",
        }
        for check_id in check_ids:
            broken = json.loads(json.dumps(manifest))
            target = next(item for item in broken["checks"] if item["id"] == check_id)
            target["tool_inputs"].remove("bootstrap/clojure/src/**")
            with self.assertRaisesRegex(
                verifier.ManifestError, "centralized Stage3 runtime inputs"
            ):
                verifier.validate_manifest(broken)

            broken = json.loads(json.dumps(manifest))
            target = next(item for item in broken["checks"] if item["id"] == check_id)
            target["minimum_heap_bytes"] += 1
            with self.assertRaisesRegex(verifier.ManifestError, "minimum_heap_bytes"):
                verifier.validate_manifest(broken)

            broken = json.loads(json.dumps(manifest))
            target = next(item for item in broken["checks"] if item["id"] == check_id)
            target["jvm_heap"] = (
                "-J-Xmx8g" if target["jvm_heap"] != "-J-Xmx8g" else "-J-Xmx2g"
            )
            target["minimum_heap_bytes"] = (
                8589934592 if target["jvm_heap"] == "-J-Xmx8g" else 2147483648
            )
            with self.assertRaisesRegex(verifier.ManifestError, "jvm_heap"):
                verifier.validate_manifest(broken)

            for field, value, error in (
                ("timeout_seconds", target["timeout_seconds"] + 1, "timeout_seconds"),
                ("fresh", False, "fresh"),
                ("resume", True, "resume"),
                ("state_dir_policy", "reused", "state_dir_policy"),
                ("automatic", False, "automatic"),
                ("lock", "/private/tmp/gravity-stage8-wrong.lock", "lock"),
                ("lock_owner", "runner", "lock_owner"),
                ("exclusive", False, "exclusive"),
                ("capacity", 2, "capacity"),
            ):
                broken = json.loads(json.dumps(manifest))
                target = next(
                    item for item in broken["checks"] if item["id"] == check_id
                )
                target[field] = value
                with self.assertRaisesRegex(verifier.ManifestError, error):
                    verifier.validate_manifest(broken)

            for field, value in (
                ("timeout_seconds", float(target["timeout_seconds"])),
                ("fresh", 1),
                ("resume", 0),
                ("automatic", 1),
                ("exclusive", 1),
                ("capacity", True),
            ):
                broken = json.loads(json.dumps(manifest))
                confused = next(
                    item for item in broken["checks"] if item["id"] == check_id
                )
                confused[field] = value
                with self.assertRaisesRegex(verifier.ManifestError, field):
                    verifier.validate_manifest(broken)

            for field, value in (
                ("stage3_batch", wrong_same_heap_batch[check_id]),
                ("depends_on", []),
                ("depends_on", ["stage0-orchestrator-unit"]),
                ("stage3_mode", verifier._stage3.MODE_PROOF_CANDIDATE),
                ("command", [sys.executable, "-c", "pass"]),
                ("authority", "declared"),
            ):
                broken = json.loads(json.dumps(manifest))
                drifted = next(
                    item for item in broken["checks"] if item["id"] == check_id
                )
                drifted[field] = value
                with self.assertRaisesRegex(verifier.ManifestError, field):
                    verifier.validate_manifest(broken)

            broken = json.loads(json.dumps(manifest))
            missing_authority = next(
                item for item in broken["checks"] if item["id"] == check_id
            )
            missing_authority.pop("authority")
            with self.assertRaisesRegex(verifier.ManifestError, "authority"):
                verifier.validate_manifest(broken)

            for required_input in verifier._STAGE8_FIXED_NODE_POLICIES[check_id][
                "required_inputs"
            ]:
                broken = json.loads(json.dumps(manifest))
                missing_input = next(
                    item for item in broken["checks"] if item["id"] == check_id
                )
                missing_input["inputs"].remove(required_input)
                with self.assertRaisesRegex(verifier.ManifestError, "required Stage8 inputs"):
                    verifier.validate_manifest(broken)

        for missing in check_ids:
            broken = json.loads(json.dumps(manifest))
            broken["checks"] = [
                item for item in broken["checks"] if item["id"] != missing
            ]
            with self.assertRaisesRegex(verifier.ManifestError, "Stage8 fixed graph ids"):
                verifier.validate_manifest(broken)
        broken = json.loads(json.dumps(manifest))
        broken["checks"] = [
            item for item in broken["checks"] if not item["id"].startswith("stage8-")
        ]
        with self.assertRaisesRegex(verifier.ManifestError, "Stage8 fixed graph ids"):
            verifier.validate_manifest(broken, require_production_contracts=True)

    def test_real_manifest_stage3_runner_unit_owns_its_complete_clojure_test_file(self) -> None:
        manifest = verifier.load_manifest(ROOT / "tools" / "development_verification_manifest.json")
        runner = verifier.checks_by_id(manifest)["stage3-runner-unit"]
        self.assertEqual(
            runner["command"],
            [
                "clojure",
                "-J-Xmx2g",
                "-M:test",
                "--namespace",
                "gravity.self-hosting.stage3-verification-runner-test",
            ],
        )
        self.assertIn(
            "bootstrap/clojure/test/gravity/self_hosting/stage3_verification_runner_test.clj",
            runner["inputs"],
        )
        selection = verifier.select_impacted_checks(
            manifest,
            ROOT,
            changed_paths=["bootstrap/clojure/test/gravity/self_hosting/stage3_verification_runner_test.clj"],
        )
        self.assertIn("stage3-runner-unit", selection["selected_ids"])
        self.assertNotIn("stage3-c7-proof-candidate", selection["selected_ids"])

    def test_real_manifest_p15_native_plan_reservation_is_exact_and_exclusive(self) -> None:
        manifest = verifier.load_manifest(ROOT / "tools" / "development_verification_manifest.json")
        check_id = "stage0-p15-native-plan-specialization-prerequisite"
        check = verifier.checks_by_id(manifest)[check_id]
        self.assertEqual(check["command"], verifier._p15_native_plan_command())
        self.assertEqual(check["inputs"], verifier._P15_NATIVE_PLAN_INPUTS)
        self.assertEqual(check["tool_inputs"], verifier._P15_NATIVE_PLAN_TOOL_INPUTS)
        self.assertEqual(check["depends_on"], ["stage0-orchestrator-unit"])
        self.assertEqual(check["resource_class"], "memory-heavy")
        self.assertEqual(check["jvm_heap"], "-J-Xmx1g")
        self.assertEqual(check["timeout_seconds"], 2400)
        self.assertEqual(check["lock"], "/private/tmp/gravity-sh07-heavy.lock")
        self.assertEqual(check["lock_owner"], "runner")
        self.assertTrue(check["exclusive"])
        self.assertEqual(check["capacity"], 1)
        self.assertTrue(check["fresh"])
        self.assertFalse(check["resume"])
        self.assertTrue(check["no_resume"])
        self.assertEqual(check["authority"], "none")

        expected = {"stage0-orchestrator-unit", check_id}
        probes = (
            "bootstrap/clojure/src/gravity/p15_native_plan_specialization.clj",
            "bootstrap/clojure/test/gravity/p15_native_plan_specialization_test.clj",
            "bootstrap/clojure/fixtures/p15-native-plan-specialization/accepted-print.gravity",
            "bootstrap/gravity/p15_s23/native_plan_c_emitter.gravity",
        )
        for path in probes:
            with self.subTest(path=path):
                selection = verifier.select_impacted_checks(manifest, ROOT, changed_paths=[path])
                self.assertEqual(set(selection["selected_ids"]), expected)
                self.assertEqual(selection["unmatched_changes"], [])

        for label, mutate in (
            ("positive-first", lambda item: item["command"].__setitem__(9, verifier._P15_NATIVE_PLAN_TEST_VARS[-1])),
            ("extra-input", lambda item: item["inputs"].append("extra.gravity")),
            ("missing-tool", lambda item: item["tool_inputs"].pop()),
            ("wide-heap", lambda item: item.__setitem__("jvm_heap", "-J-Xmx8g")),
        ):
            broken = json.loads(json.dumps(manifest))
            candidate = next(item for item in broken["checks"] if item["id"] == check_id)
            mutate(candidate)
            with self.subTest(label=label), self.assertRaises(verifier.ManifestError):
                verifier.validate_manifest(broken)

    def test_real_manifest_p15_native_launcher_gate_contract_and_dry_runs(self) -> None:
        manifest = verifier.load_manifest(ROOT / "tools" / "development_verification_manifest.json")
        launcher = verifier.checks_by_id(manifest)["stage0-p15-native-launcher-prerequisite"]
        self.assertEqual(
            launcher["command"],
            [
                "clojure",
                "-J-Xmx1g",
                "-M:test",
                "--namespace",
                "gravity.p15-native-launcher-test",
            ],
        )
        self.assertEqual(launcher["lane"], "heavy-candidate")
        self.assertEqual(launcher["cost"], "heavy")
        self.assertEqual(launcher["timeout_seconds"], 600)
        self.assertEqual(launcher["jvm_heap"], "-J-Xmx1g")
        self.assertEqual(launcher["minimum_heap_bytes"], 1073741824)
        self.assertEqual(launcher["lock"], "/private/tmp/gravity-sh07-heavy.lock")
        self.assertEqual(launcher["lock_owner"], "runner")
        self.assertTrue(launcher["exclusive"])
        self.assertEqual(launcher["capacity"], 1)
        self.assertTrue(launcher["fresh"])
        self.assertFalse(launcher["resume"])
        self.assertTrue(launcher["no_resume"])
        self.assertEqual(launcher["authority"], "none")
        self.assertEqual(
            launcher["resource_receipt"],
            "observed-peak-process-tree-rss-and-wall-time",
        )
        self.assertEqual(launcher["depends_on"], ["stage0-orchestrator-unit"])
        self.assertEqual(
            launcher["inputs"],
            [
                "bootstrap/native/p15_public_native_launcher.c",
                "bootstrap/clojure/test/gravity/p15_native_launcher_test.clj",
                "bootstrap/clojure/fixtures/p15-native-launcher/argv_stdout.c",
                "bootstrap/clojure/fixtures/p15-native-launcher/exit_23.c",
                "bootstrap/clojure/fixtures/p15-native-launcher/leader_descendant.c",
                "bootstrap/clojure/fixtures/p15-native-launcher/marker.c",
                "bootstrap/clojure/fixtures/p15-native-launcher/timeout_group.c",
                "docs/artifacts/phase-15/native-launcher/p15-s23-darwin-launcher-primitive.edn",
            ],
        )
        self.assertEqual(
            launcher["tool_inputs"],
            [
                "deps.edn",
                "bootstrap/clojure/test/gravity/self_hosting_test_runner.clj",
            ],
        )

        expected_ids = {
            "stage0-orchestrator-unit",
            "stage0-p15-native-launcher-prerequisite",
        }
        for owned_path in launcher["inputs"]:
            with self.subTest(owned_path=owned_path):
                path_expected_ids = set(expected_ids)
                if owned_path == "bootstrap/clojure/test/gravity/p15_native_launcher_test.clj":
                    path_expected_ids.add("stage0-coordinator-integration-reservations")
                selection = verifier.select_impacted_checks(
                    manifest, ROOT, changed_paths=[owned_path]
                )
                self.assertEqual(set(selection["selected_ids"]), path_expected_ids)
                self.assertEqual(selection["unmatched_changes"], [])
                self.assertNotIn("stage0-clojure-suite", selection["selected_ids"])
                self.assertNotIn("stage0-bootstrap-authority", selection["selected_ids"])
                self.assertFalse(
                    any(
                        check_id.startswith("stage3-")
                        or check_id.startswith("stage4-")
                        or check_id.startswith("stage5-")
                        or check_id.startswith("stage6-")
                        or check_id.startswith("stage7-")
                        or check_id.endswith("-proof-candidate")
                        for check_id in selection["selected_ids"]
                    )
                )
                receipt = verifier.run_verification(
                    manifest,
                    ROOT,
                    changed_paths=[owned_path],
                    dry_run=True,
                )
                self.assertEqual(receipt["status"], "planned")
                self.assertFalse(receipt["authoritative"])
                self.assertEqual(
                    {record["id"] for record in receipt["checks"]}, path_expected_ids
                )
                record = next(
                    item
                    for item in receipt["checks"]
                    if item["id"] == "stage0-p15-native-launcher-prerequisite"
                )
                self.assertEqual(record["authority"], "non-authoritative")
                self.assertEqual(record["lock_owner"], "runner")
                self.assertEqual(record["lock"], "/private/tmp/gravity-sh07-heavy.lock")
                self.assertEqual(record["command"], launcher["command"])

        explicit = verifier.run_verification(
            manifest,
            ROOT,
            requested_ids=["stage0-p15-native-launcher-prerequisite"],
            dry_run=True,
        )
        self.assertEqual(explicit["status"], "planned")
        self.assertFalse(explicit["authoritative"])
        self.assertEqual(
            explicit["plan"]["topological_order"],
            ["stage0-orchestrator-unit", "stage0-p15-native-launcher-prerequisite"],
        )
        self.assertNotIn(
            "stage0-clojure-suite",
            {record["id"] for record in explicit["checks"]},
        )

        all_checks = verifier.select_impacted_checks(manifest, ROOT, all_checks=True)
        self.assertIn("stage0-p15-native-launcher-prerequisite", all_checks["selected_ids"])

    def test_real_manifest_p15_native_launcher_gate_rejects_contract_drift(self) -> None:
        manifest = verifier.load_manifest(ROOT / "tools" / "development_verification_manifest.json")
        check_id = "stage0-p15-native-launcher-prerequisite"
        by_id = verifier.checks_by_id(manifest)

        def check_in(manifest_value: dict) -> dict:
            return next(item for item in manifest_value["checks"] if item["id"] == check_id)

        cases = (
            ("missing source", lambda item: item["inputs"].pop(0), "inputs drifted"),
            (
                "drifted fixture",
                lambda item: item["inputs"].__setitem__(2, "bootstrap/clojure/fixtures/p15-native-launcher/argv.c"),
                "inputs drifted",
            ),
            (
                "wrong command",
                lambda item: item["command"].__setitem__(4, "gravity.p15-native-launcher-proof"),
                "exact direct Darwin launcher test command",
            ),
            (
                "wrong heap",
                lambda item: item.__setitem__("jvm_heap", "-J-Xmx2g"),
                "jvm_heap",
            ),
            (
                "minimum heap float",
                lambda item: item.__setitem__("minimum_heap_bytes", 1073741824.0),
                "minimum_heap_bytes",
            ),
            (
                "minimum heap bool",
                lambda item: item.__setitem__("minimum_heap_bytes", True),
                "minimum_heap_bytes",
            ),
            (
                "wrong lock owner",
                lambda item: item.__setitem__("lock_owner", "command"),
                "lock_owner='runner'",
            ),
            (
                "wrong resource capacity",
                lambda item: item.__setitem__("capacity", 2),
                "exclusive=true and capacity=1",
            ),
            (
                "wrong artifact",
                lambda item: item["inputs"].__setitem__(7, "docs/artifacts/phase-15/native-launcher/drift.edn"),
                "inputs drifted",
            ),
            (
                "timeout bool",
                lambda item: item.__setitem__("timeout_seconds", True),
                "timeout_seconds must be exactly 600",
            ),
            (
                "timeout 601",
                lambda item: item.__setitem__("timeout_seconds", 601),
                "timeout_seconds must be exactly 600",
            ),
            (
                "timeout NaN",
                lambda item: item.__setitem__("timeout_seconds", float("nan")),
                "timeout_seconds must be exactly 600",
            ),
            (
                "timeout infinity",
                lambda item: item.__setitem__("timeout_seconds", float("inf")),
                "timeout_seconds must be exactly 600",
            ),
            (
                "timeout missing",
                lambda item: item.pop("timeout_seconds"),
                "timeout_seconds must be exactly 600",
            ),
        )
        for label, mutate, message in cases:
            with self.subTest(case=label):
                value = json.loads(json.dumps(manifest))
                mutate(check_in(value))
                with self.assertRaisesRegex(verifier.ManifestError, message):
                    verifier.validate_manifest(value)

        self.assertEqual(by_id[check_id]["authority"], "none")

    def test_real_manifest_p15_native_runtime_provider_profiles_and_routing(self) -> None:
        manifest = verifier.load_manifest(ROOT / "tools" / "development_verification_manifest.json")
        by_id = verifier.checks_by_id(manifest)
        fast_id = "stage0-p15-native-runtime-provider-contract-prerequisite"
        auth_id = "stage0-p15-native-runtime-provider-packet-binding-prerequisite"
        orchestrator_id = "stage0-orchestrator-unit"
        fast = by_id[fast_id]
        authenticated = by_id[auth_id]
        common = verifier._P15_NATIVE_RUNTIME_COMMON_INPUTS
        old_fixtures = verifier._P15_NATIVE_RUNTIME_PROVIDER_OLD_FIXTURE_INPUTS
        new_fixtures = verifier._P15_NATIVE_RUNTIME_AUTHENTICATED_FIXTURE_INPUTS
        tool_inputs = verifier._P15_NATIVE_RUNTIME_TOOL_INPUTS

        self.assertEqual(fast["inputs"], common + old_fixtures)
        self.assertEqual(authenticated["inputs"], common + [
            "bootstrap/clojure/src/gravity/p15_native_packet_binding.clj",
        ] + new_fixtures)
        self.assertEqual(len(fast["inputs"]), 26)
        self.assertEqual(len(authenticated["inputs"]), 9)
        self.assertEqual(
            set(fast["inputs"]) | set(authenticated["inputs"]),
            set(common + old_fixtures + [
                "bootstrap/clojure/src/gravity/p15_native_packet_binding.clj",
            ] + new_fixtures),
        )
        self.assertEqual(fast["tool_inputs"], tool_inputs)
        self.assertEqual(authenticated["tool_inputs"], tool_inputs)
        self.assertEqual(fast["command"], verifier._P15_NATIVE_RUNTIME_PROVIDER_COMMAND)
        self.assertEqual(
            authenticated["command"], verifier._P15_NATIVE_RUNTIME_AUTHENTICATED_COMMAND
        )
        self.assertEqual(fast["depends_on"], [orchestrator_id])
        self.assertEqual(authenticated["depends_on"], [orchestrator_id])
        self.assertEqual(
            verifier.topological_order(manifest, [orchestrator_id, fast_id]),
            [orchestrator_id, fast_id],
        )
        self.assertEqual(
            verifier.topological_order(manifest, [orchestrator_id, fast_id, auth_id]),
            [orchestrator_id, fast_id, auth_id],
        )
        shared_selection = verifier.select_impacted_checks(
            manifest,
            ROOT,
            changed_paths=[verifier._P15_NATIVE_RUNTIME_COMMON_INPUTS[0]],
        )
        self.assertEqual(
            shared_selection["selected_ids"],
            [orchestrator_id, fast_id, auth_id],
        )
        self.assertEqual(
            verifier.parallel_ready_groups(manifest, shared_selection["selected_ids"]),
            [[orchestrator_id], [fast_id], [auth_id]],
        )
        shared_receipt = verifier.run_verification(
            manifest,
            ROOT,
            changed_paths=[verifier._P15_NATIVE_RUNTIME_COMMON_INPUTS[0]],
            dry_run=True,
        )
        self.assertEqual(
            shared_receipt["plan"]["topological_order"],
            [orchestrator_id, fast_id, auth_id],
        )
        self.assertEqual(
            shared_receipt["plan"]["parallel_ready_groups"],
            [[orchestrator_id], [fast_id], [auth_id]],
        )
        test_source = (
            ROOT / "bootstrap" / "clojure" / "test" / "gravity"
            / "p15_native_runtime_driver_test.clj"
        ).read_text(encoding="utf-8")
        fixture_set_start = test_source.index("(defn- assert-reviewed-fixture-set")
        fixture_set_end = test_source.index("(defn- arm64-darwin-toolchain-available?")
        fixture_set_helper = test_source[fixture_set_start:fixture_set_end]
        exact_branch = fixture_set_helper.index("(when exact-directory?")
        directory_scan = fixture_set_helper.index("(Files/list directory)")
        subset_reads = fixture_set_helper.rindex(
            "(doseq [relative expected-relatives]"
        )
        self.assertLess(exact_branch, directory_scan)
        self.assertLess(directory_scan, subset_reads)
        self.assertNotIn("Files/list", fixture_set_helper[subset_reads:])
        self.assertNotIn("(every?", fixture_set_helper)
        auth_start = test_source.index(
            "(defn- artifact-authenticated-packet-binding-contract!"
        )
        auth_end = test_source.index(
            "(deftest p15-native-runtime-provider-artifact-identity-and-fixture-contract"
        )
        auth_helper = test_source[auth_start:auth_end]
        shared_start = test_source.index("(defn- artifact-shared-identity!")
        shared_end = test_source.index("(defn- shared-artifact-rejected?")
        shared_helper = test_source[shared_start:shared_end]
        for required in (
            "contract-relative",
            "source-relative",
            "test-source-relative",
            ":build-command",
            ":historical-receipt",
            ":limitations",
            "reviewed-limitations",
        ):
            self.assertIn(required, shared_helper)
        self.assertNotIn("assert-reviewed-fixture-set", shared_helper)
        fast_start = test_source.index("(defn- artifact-fast-contract!")
        fast_helper = test_source[fast_start:auth_start]
        self.assertIn("artifact-shared-identity! artifact", fast_helper)
        self.assertIn("reviewed-fixture-relatives", fast_helper)
        for required in (
            "artifact-shared-identity! artifact",
            "authenticated-fixture-relatives",
            "binder-hash",
            "shared-artifact-rejected? mutated",
            ":semantic-contract-hash",
            ":provider-content-hash",
            ":test-source-content-hash",
            ":provider-build-command",
            ":limitations",
        ):
            self.assertIn(required, auth_helper)
        self.assertNotIn("reviewed-fixture-relatives", auth_helper)
        self.assertNotIn("all-fixture-relatives", auth_helper)
        self.assertNotIn("assert-reviewed-fixture-set reviewed-fixture-relatives",
                         auth_helper)
        for profile, expected in (
            (fast, ("-J-Xmx1g", 1073741824, 180)),
            (authenticated, ("-J-Xmx8g", 8589934592, 1800)),
        ):
            with self.subTest(profile=profile["id"]):
                self.assertEqual(profile["lane"], "heavy-candidate")
                self.assertEqual(profile["cost"], "heavy")
                self.assertEqual(profile["jvm_heap"], expected[0])
                self.assertEqual(profile["minimum_heap_bytes"], expected[1])
                self.assertEqual(profile["timeout_seconds"], expected[2])
                self.assertEqual(profile["lock"], "/private/tmp/gravity-sh07-heavy.lock")
                self.assertEqual(profile["lock_owner"], "runner")
                self.assertIs(profile["exclusive"], True)
                self.assertEqual(profile["capacity"], 1)
                self.assertIs(profile["fresh"], True)
                self.assertIs(profile["resume"], False)
                self.assertIs(profile["no_resume"], True)
                self.assertIs(profile["automatic"], True)
                self.assertEqual(profile["authority"], "none")
                self.assertEqual(
                    profile["resource_receipt"],
                    "observed-peak-process-tree-rss-and-wall-time",
                )
                self.assertEqual(
                    profile["env"], {"GRAVITY_P15_NATIVE_RUNTIME_REQUIRED": "1"}
                )
                self.assertTrue(all("*" not in path and "?" not in path
                                    for path in profile["inputs"] + profile["tool_inputs"]))

        shared = set(common + [
            "bootstrap/clojure/src/gravity/p15_native_packet_binding.clj",
        ])
        fast_only = set(old_fixtures)
        auth_only = set(new_fixtures)
        self.assertEqual(shared & fast_only, set())
        self.assertEqual(shared & auth_only, set())
        self.assertEqual(fast_only & auth_only, set())
        for owned_path in common + old_fixtures + [
            "bootstrap/clojure/src/gravity/p15_native_packet_binding.clj",
        ] + new_fixtures:
            with self.subTest(owned_path=owned_path):
                selection = verifier.select_impacted_checks(
                    manifest, ROOT, changed_paths=[owned_path]
                )
                direct = {
                    check_id
                    for check_id, reasons in selection["reasons"].items()
                    if any(reason.startswith("changed-input:") for reason in reasons)
                }
                expected_direct = (
                    {fast_id}
                    if owned_path in fast_only
                    else {auth_id}
                    if owned_path in auth_only
                    else {fast_id, auth_id}
                )
                if owned_path == "bootstrap/clojure/test/gravity/p15_native_runtime_driver_test.clj":
                    expected_direct.add("stage0-coordinator-integration-reservations")
                if owned_path == "bootstrap/clojure/src/gravity/p15_native_packet_binding.clj":
                    expected_direct.add("stage0-p15-native-plan-specialization-prerequisite")
                expected_ids = {orchestrator_id} | expected_direct
                self.assertEqual(set(selection["selected_ids"]), expected_ids)
                self.assertEqual(selection["unmatched_changes"], [])
                self.assertEqual(direct, expected_direct)
                broad_matches = {
                    check_id
                    for check_id, check in by_id.items()
                    if verifier._automatic_check(check)
                    and any(
                        verifier._matches_change(declared, owned_path)
                        for declared in check.get("inputs", []) + check.get("tool_inputs", [])
                    )
                    and not verifier._impact_excludes_change(check, owned_path)
                }
                self.assertTrue(
                    broad_matches
                    <= {
                        fast_id,
                        auth_id,
                        "stage0-coordinator-integration-reservations",
                        "stage0-p15-native-plan-specialization-prerequisite",
                    }
                )
                receipt = verifier.run_verification(
                    manifest, ROOT, changed_paths=[owned_path], dry_run=True
                )
                self.assertEqual(receipt["status"], "planned")
                self.assertFalse(receipt["authoritative"])
                self.assertEqual(
                    {record["id"] for record in receipt["checks"]}, expected_ids
                )

        fast_explicit = verifier.run_verification(
            manifest, ROOT, requested_ids=[fast_id], dry_run=True
        )
        self.assertEqual(fast_explicit["status"], "planned")
        self.assertEqual(fast_explicit["plan"]["topological_order"],
                         [orchestrator_id, fast_id])
        auth_explicit = verifier.run_verification(
            manifest, ROOT, requested_ids=[auth_id], dry_run=True
        )
        self.assertEqual(auth_explicit["status"], "planned")
        self.assertEqual(auth_explicit["plan"]["topological_order"],
                         [orchestrator_id, auth_id])
        all_checks = verifier.select_impacted_checks(manifest, ROOT, all_checks=True)
        self.assertTrue({fast_id, auth_id} <= set(all_checks["selected_ids"]))

        # Existing runtime consumers remain real owners of the eager namespace
        # helper closure; the new binder is the only central helper whose
        # broad Stage3-8 ownership is intentionally excluded.
        helper_consumers = {
            "bootstrap/clojure/src/gravity/bootstrap.clj": {
                "stage0-reader", "stage3-source-control-form-arity",
            },
            "bootstrap/clojure/src/gravity/cli.clj": {
                "stage1-sh01-unit", "stage3-source-control-form-arity",
            },
            "bootstrap/clojure/src/gravity/darwin_publication.clj": {
                "stage3-source-control-form-arity",
            },
            "bootstrap/clojure/src/gravity/digest.clj": {
                "stage0-project-structure-extraction", "stage3-source-control-form-arity",
            },
            "bootstrap/clojure/src/gravity/diagnostics.clj": {
                "stage3-source-control-form-arity",
            },
            "bootstrap/clojure/src/gravity/source_span.clj": {
                "stage0-project-structure-extraction", "stage3-source-control-form-arity",
            },
            "bootstrap/clojure/src/gravity/source_unit.clj": {
                "stage0-project-structure-extraction", "stage3-source-control-form-arity",
            },
        }
        for helper_path, prior_ids in helper_consumers.items():
            with self.subTest(helper_path=helper_path):
                selection = verifier.select_impacted_checks(
                    manifest, ROOT, changed_paths=[helper_path]
                )
                selected = set(selection["selected_ids"])
                self.assertTrue({fast_id, auth_id} <= selected)
                self.assertTrue(prior_ids <= selected)
                self.assertEqual(selection["unmatched_changes"], [])
                direct = {
                    check_id
                    for check_id, reasons in selection["reasons"].items()
                    if any(reason.startswith("changed-input:") for reason in reasons)
                }
                self.assertTrue({fast_id, auth_id} <= direct)

    def test_real_manifest_p15_native_runtime_provider_profiles_reject_contract_drift(self) -> None:
        manifest = verifier.load_manifest(ROOT / "tools" / "development_verification_manifest.json")
        profiles = (
            ("stage0-p15-native-runtime-provider-contract-prerequisite", 180,
             ["stage0-orchestrator-unit"]),
            ("stage0-p15-native-runtime-provider-packet-binding-prerequisite", 1800,
             ["stage0-orchestrator-unit"]),
        )
        for check_id, timeout, dependencies in profiles:
            def check_in(manifest_value: dict) -> dict:
                return next(item for item in manifest_value["checks"] if item["id"] == check_id)

            cases = (
                ("missing source", lambda item: item["inputs"].pop(0), "inputs drifted"),
                ("missing tool input", lambda item: item["tool_inputs"].pop(), "tool_inputs drifted"),
                (
                    "wrong command",
                    lambda item: item["command"].__setitem__(6, "gravity.p15-native-runtime-proof"),
                    "exact fixed P15 native runtime selector command",
                ),
                ("wrong heap", lambda item: item.__setitem__("jvm_heap", "-J-Xmx2g"), "jvm_heap"),
                ("minimum heap float", lambda item: item.__setitem__("minimum_heap_bytes", 1073741824.0), "minimum_heap_bytes"),
                ("minimum heap bool", lambda item: item.__setitem__("minimum_heap_bytes", True), "minimum_heap_bytes"),
                ("wrong lock", lambda item: item.__setitem__("lock", "/private/tmp/wrong.lock"), "canonical lock"),
                ("wrong lock owner", lambda item: item.__setitem__("lock_owner", "command"), "lock_owner='runner'"),
                ("wrong capacity", lambda item: item.__setitem__("capacity", 2), "capacity=1"),
                ("wrong dependency", lambda item: item.__setitem__("depends_on", []), "depend exactly on"),
                ("timeout bool", lambda item: item.__setitem__("timeout_seconds", True), f"timeout_seconds must be exactly {timeout}"),
                ("timeout int", lambda item: item.__setitem__("timeout_seconds", timeout + 1), f"timeout_seconds must be exactly {timeout}"),
                ("timeout float", lambda item: item.__setitem__("timeout_seconds", float(timeout)), f"timeout_seconds must be exactly {timeout}"),
                ("timeout NaN", lambda item: item.__setitem__("timeout_seconds", float("nan")), f"timeout_seconds must be exactly {timeout}"),
                ("timeout missing", lambda item: item.pop("timeout_seconds"), f"timeout_seconds must be exactly {timeout}"),
                ("wrong fresh", lambda item: item.__setitem__("fresh", False), "fresh must be exactly True"),
                ("wrong resume", lambda item: item.__setitem__("resume", True), "resume must be exactly False"),
                ("wrong no_resume", lambda item: item.__setitem__("no_resume", False), "no_resume must be exactly True"),
                ("wrong automatic", lambda item: item.__setitem__("automatic", False), "automatic must be exactly True"),
                ("wrong authority", lambda item: item.__setitem__("authority", "declared"), "authority='none'"),
                ("wrong resource receipt", lambda item: item.__setitem__("resource_receipt", "none"), "resource receipt"),
                ("wrong required environment", lambda item: item.__setitem__("env", {"GRAVITY_P15_NATIVE_RUNTIME_REQUIRED": "0"}), "exact native-runtime required environment"),
                ("missing required environment", lambda item: item.pop("env"), "exact native-runtime required environment"),
            )
            for label, mutate, message in cases:
                with self.subTest(profile=check_id, case=label):
                    value = json.loads(json.dumps(manifest))
                    mutate(check_in(value))
                    with self.assertRaisesRegex(verifier.ManifestError, message):
                        verifier.validate_manifest(value)
            value = json.loads(json.dumps(manifest))
            check_in(value)["depends_on"] = dependencies
            verifier.validate_manifest(value)

    def test_real_manifest_requires_exact_p15_native_runtime_provider_nodes_and_acyclic_graph(self) -> None:
        manifest = verifier.load_manifest(ROOT / "tools" / "development_verification_manifest.json")
        ids = (
            "stage0-p15-native-runtime-provider-contract-prerequisite",
            "stage0-p15-native-runtime-provider-packet-binding-prerequisite",
        )
        for check_id in ids:
            removed = json.loads(json.dumps(manifest))
            removed["checks"] = [item for item in removed["checks"] if item["id"] != check_id]
            with self.subTest(case="removed", check_id=check_id):
                with self.assertRaisesRegex(verifier.ManifestError, "exactly one"):
                    verifier.validate_manifest(removed, require_production_contracts=True)
            renamed = json.loads(json.dumps(manifest))
            renamed_item = next(item for item in renamed["checks"] if item["id"] == check_id)
            renamed_item["id"] = check_id + "-widened"
            with self.subTest(case="renamed", check_id=check_id):
                with self.assertRaisesRegex(verifier.ManifestError, "exactly one"):
                    verifier.validate_manifest(renamed, require_production_contracts=True)

        cyclic = json.loads(json.dumps(manifest))
        orchestrator = next(
            item for item in cyclic["checks"] if item["id"] == "stage0-orchestrator-unit"
        )
        orchestrator["depends_on"] = [
            "stage0-p15-native-runtime-provider-contract-prerequisite"
        ]
        with self.assertRaisesRegex(verifier.ManifestError, "dependency cycle"):
            verifier.validate_manifest(cyclic)

    def test_real_manifest_requires_exact_p15_native_launcher_node_once(self) -> None:
        manifest = verifier.load_manifest(ROOT / "tools" / "development_verification_manifest.json")
        check_id = "stage0-p15-native-launcher-prerequisite"

        removed = json.loads(json.dumps(manifest))
        removed["checks"] = [
            item for item in removed["checks"] if item["id"] != check_id
        ]
        with self.assertRaisesRegex(verifier.ManifestError, "exactly one check id"):
            verifier.validate_manifest(removed, require_production_contracts=True)

        renamed = json.loads(json.dumps(manifest))
        renamed_item = next(item for item in renamed["checks"] if item["id"] == check_id)
        renamed_item.update(
            {
                "id": "stage0-p15-native-launcher-prerequisite-widened",
                "lane": "focused",
                "cost": "cheap",
                "lock": None,
                "exclusive": False,
                "command": [sys.executable, "-c", "pass"],
                "inputs": ["input.txt"],
                "tool_inputs": [],
                "depends_on": [],
                "fresh": False,
                "automatic": True,
            }
        )
        with self.assertRaisesRegex(verifier.ManifestError, "exactly one check id"):
            verifier.validate_manifest(renamed, require_production_contracts=True)

    def test_production_manifest_context_cannot_be_bypassed_by_metadata_drift(self) -> None:
        manifest_path = ROOT / "tools" / "development_verification_manifest.json"
        manifest = verifier.load_manifest(manifest_path)
        drifted = json.loads(json.dumps(manifest))
        drifted["name"] = "test-development-verification"
        drifted["scope"] = {"stage": "synthetic"}
        drifted["checks"] = [
            item
            for item in drifted["checks"]
            if item["id"] != "stage0-p15-native-launcher-prerequisite"
        ]

        # Content alone does not opt a generic fixture into production-only
        # membership.  The trusted canonical load path does.
        verifier.validate_manifest(drifted)
        with tempfile.TemporaryDirectory(prefix="gravity-synthetic-manifest-") as directory:
            synthetic_path = Path(directory) / "manifest.json"
            synthetic_path.write_text(json.dumps(drifted), encoding="utf-8")
            verifier.load_manifest(synthetic_path)

        with mock.patch.object(Path, "read_text", return_value=json.dumps(drifted)):
            with self.assertRaisesRegex(verifier.ManifestError, "exactly one check id"):
                verifier.load_manifest(manifest_path)

        stage8_drifted = json.loads(json.dumps(manifest))
        stage8_drifted["name"] = "test-development-verification"
        stage8_drifted["scope"] = {"stage": "synthetic"}
        stage8_drifted["checks"] = [
            item
            for item in stage8_drifted["checks"]
            if not item["id"].startswith(("stage8-", "stage9-"))
        ]
        # A generic fixture may omit production Stage8.  Trusted explicit or
        # canonical-path validation must require the exact set regardless of
        # mutable name/scope metadata.
        verifier.validate_manifest(stage8_drifted)
        with self.assertRaisesRegex(verifier.ManifestError, "Stage8 fixed graph ids"):
            verifier.validate_manifest(
                stage8_drifted, require_production_contracts=True
            )
        with mock.patch.object(
            Path, "read_text", return_value=json.dumps(stage8_drifted)
        ):
            with self.assertRaisesRegex(verifier.ManifestError, "Stage8 fixed graph ids"):
                verifier.load_manifest(manifest_path)

        stage9_drifted = json.loads(json.dumps(manifest))
        stage9_drifted["name"] = "test-development-verification"
        stage9_drifted["scope"] = {"stage": "synthetic"}
        stage9_drifted["checks"] = [
            item
            for item in stage9_drifted["checks"]
            if not item["id"].startswith("stage9-")
        ]
        verifier.validate_manifest(stage9_drifted)
        with self.assertRaisesRegex(verifier.ManifestError, "Stage9 fixed graph ids"):
            verifier.validate_manifest(
                stage9_drifted, require_production_contracts=True
            )
        with mock.patch.object(
            Path, "read_text", return_value=json.dumps(stage9_drifted)
        ):
            with self.assertRaisesRegex(verifier.ManifestError, "Stage9 fixed graph ids"):
                verifier.load_manifest(manifest_path)

    def _run_mocked_resource_check(
        self,
        outcome: dict,
        *,
        resource_receipt: str | None = "observed-peak-process-tree-rss-and-wall-time",
    ) -> tuple[dict, mock.Mock]:
        with tempfile.TemporaryDirectory(prefix="gravity-resource-receipt-") as directory:
            root = Path(directory).resolve()
            (root / "input.txt").write_text("stable\n", encoding="ascii")
            item = check(
                "non-jvm-resource-check",
                [sys.executable, "-c", "pass"],
                inputs=["input.txt"],
            )
            if resource_receipt is not None:
                item["resource_receipt"] = resource_receipt
            identities = verifier.check_identity(item, root)
            with mock.patch.object(
                verifier,
                "_run_command",
                return_value=outcome,
            ) as run_command:
                record = verifier._run_one(item, root, identities)
            return record, run_command

    def test_resource_receipt_sampling_is_enabled_and_parent_fields_are_preserved(self) -> None:
        outcome = {
            "timed_out": False,
            "returncode": 0,
            "stdout": "",
            "stderr": "",
            "cleanup": None,
            "surviving_descendants": False,
            "supervision_failed": False,
            "observed_peak_process_tree_rss_bytes": 123,
            "rss_sampling_cadence_seconds": 1.0,
            "rss_sampling_contract": "run_with_heartbeat.process_tree_metrics-v1",
            "rss_sampling_limitation": "between-sample spikes may be missed",
        }
        record, run_command = self._run_mocked_resource_check(outcome)
        self.assertEqual(record["status"], "passed")
        self.assertEqual(record["command"][0], sys.executable)
        self.assertTrue(run_command.call_args.kwargs["sample_rss"])
        self.assertEqual(record["resource_receipt"], "observed-peak-process-tree-rss-and-wall-time")
        self.assertEqual(record["observed_peak_process_tree_rss_bytes"], 123)
        self.assertEqual(record["rss_sampling_cadence_seconds"], 1.0)
        self.assertEqual(
            record["rss_sampling_contract"],
            "run_with_heartbeat.process_tree_metrics-v1",
        )

    def test_non_jvm_mocked_positive_resource_receipt_is_recorded(self) -> None:
        """The shared receipt gate must not depend on a JVM-shaped command."""

        outcome = {
            "timed_out": False,
            "returncode": 0,
            "stdout": "",
            "stderr": "",
            "cleanup": None,
            "surviving_descendants": False,
            "supervision_failed": False,
            "observed_peak_process_tree_rss_bytes": 163463168,
            "rss_sampling_cadence_seconds": 1.0,
            "rss_sampling_contract": "run_with_heartbeat.process_tree_metrics-v1",
            "rss_sampling_limitation": "between-sample spikes may be missed",
        }
        record, run_command = self._run_mocked_resource_check(outcome)
        self.assertEqual(record["status"], "passed")
        self.assertEqual(record["observed_peak_process_tree_rss_bytes"], 163463168)
        self.assertTrue(run_command.call_args.kwargs["sample_rss"])

    def test_resource_receipt_sampling_invalid_observations_fail_closed(self) -> None:
        base = {
            "timed_out": False,
            "returncode": 0,
            "stdout": "",
            "stderr": "",
            "cleanup": None,
            "surviving_descendants": False,
            "supervision_failed": False,
            "observed_peak_process_tree_rss_bytes": 123,
            "rss_sampling_cadence_seconds": 1.0,
            "rss_sampling_contract": "run_with_heartbeat.process_tree_metrics-v1",
            "rss_sampling_limitation": "between-sample spikes may be missed",
        }
        cases = {
            "none peak": {"observed_peak_process_tree_rss_bytes": None},
            "zero peak": {"observed_peak_process_tree_rss_bytes": 0},
            "boolean peak": {"observed_peak_process_tree_rss_bytes": True},
            "zero cadence": {"rss_sampling_cadence_seconds": 0},
            "boolean cadence": {"rss_sampling_cadence_seconds": True},
            "missing cadence": {"_remove": "rss_sampling_cadence_seconds"},
            "missing contract": {"_remove": "rss_sampling_contract"},
        }
        for label, changes in cases.items():
            with self.subTest(case=label):
                outcome = dict(base)
                remove = changes.get("_remove")
                if remove is not None:
                    outcome.pop(remove)
                else:
                    outcome.update(changes)
                record, run_command = self._run_mocked_resource_check(outcome)
                self.assertTrue(run_command.call_args.kwargs["sample_rss"])
                self.assertEqual(record["status"], "failed")
                self.assertEqual(record["reason"], "invalid-resource-receipt")
                self.assertFalse(record["cacheable"])
                self.assertIn("resource receipt", record["stderr"])

    def test_commands_without_observed_resource_receipt_do_not_sample_rss(self) -> None:
        outcome = {
            "timed_out": False,
            "returncode": 0,
            "stdout": "",
            "stderr": "",
            "cleanup": None,
            "surviving_descendants": False,
            "supervision_failed": False,
            "observed_peak_process_tree_rss_bytes": None,
            "rss_sampling_cadence_seconds": None,
            "rss_sampling_contract": None,
            "rss_sampling_limitation": None,
        }
        record, run_command = self._run_mocked_resource_check(
            outcome,
            resource_receipt=None,
        )
        self.assertEqual(record["status"], "passed")
        self.assertFalse(run_command.call_args.kwargs["sample_rss"])


if __name__ == "__main__":
    unittest.main()
