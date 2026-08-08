#!/usr/bin/env python3
"""Focused tests for the Stage 0 development verification orchestrator."""

from __future__ import annotations

import json
import os
import io
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


def manifest_for(*checks: dict) -> dict:
    return {
        "schema_version": 1,
        "name": "test-development-verification",
        "lanes": {
            "preflight": {"description": "test"},
            "focused": {"description": "test"},
            "heavy-candidate": {"description": "test"},
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
) -> dict:
    value = {
        "id": check_id,
        "lane": lane,
        "cost": cost,
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
                if argv and Path(str(argv[0])).name == "ps":
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
        clojure_checks = [item for item in manifest["checks"] if item["command"] and item["command"][0] == "clojure"]
        self.assertTrue(clojure_checks)
        self.assertTrue(all(item.get("fresh") is True for item in clojure_checks))
        self.assertTrue(all("bin/gravity" in item["inputs"] for item in clojure_checks))
        self.assertTrue(all("bootstrap/gravity/**" in item["inputs"] for item in clojure_checks))
        full_suite = next(item for item in manifest["checks"] if item["id"] == "stage0-clojure-suite")
        self.assertIn("bin/gravity-bootstrap", full_suite["inputs"])


if __name__ == "__main__":
    unittest.main()
