#!/usr/bin/env python3
"""Tests for the fixed Stage2 authority integration transaction."""

from __future__ import annotations

import fcntl
import contextlib
import io
import json
import os
from pathlib import Path
import subprocess
import sys
import tempfile
import unittest
from unittest import mock
import uuid


TOOLS = Path(__file__).resolve().parents[1]
if str(TOOLS) not in sys.path:
    sys.path.insert(0, str(TOOLS))

import stage2_authority_admission as admission  # noqa: E402
import run_with_heartbeat as heartbeat  # noqa: E402
import verify_development as development  # noqa: E402


CATALOG = {
    "c7-types": "bootstrap/gravity/src/gravity/compiler/c7_type_checker_engine.gravity",
    "diagnostics": "bootstrap/gravity/src/gravity/bootstrap/diagnostics.gravity",
}


def git(root: Path, *arguments: str, check: bool = True) -> str:
    result = subprocess.run(
        ["git", "-c", "core.fsmonitor=false", *arguments],
        cwd=root, capture_output=True, text=True, check=False,
    )
    if check and result.returncode != 0:
        raise AssertionError(result.stderr)
    return result.stdout.strip()


class Repository:
    def __init__(self, root: Path) -> None:
        self.root = root
        git(root, "init", "-q", "-b", "coordinator")
        git(root, "config", "user.email", "stage2@example.invalid")
        git(root, "config", "user.name", "Stage2 Test")
        self.write("deps.edn", "{:paths [\"bootstrap/clojure/src\"]}\n")
        self.write("README.md", "base\n")
        self.write(".gitignore", "ignored.tmp\n.cpcache/\n*.class\n__pycache__/\n")
        self.write("bootstrap/clojure/src/gravity/core.clj", "(ns gravity.core)\n")
        for relative in CATALOG.values():
            self.write(relative, f"; {relative}\n")
        git(root, "add", ".")
        git(root, "commit", "-q", "-m", "base")
        self.base = git(root, "rev-parse", "HEAD")
        self.counter = 0

    def write(self, relative: str, text: str) -> None:
        path = self.root / relative
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(text, encoding="utf-8")

    def candidate(self, mutate) -> str:
        self.counter += 1
        branch = f"candidate-{self.counter}"
        git(self.root, "checkout", "-q", "-b", branch)
        mutate()
        git(self.root, "add", "-A")
        git(self.root, "commit", "-q", "-m", branch)
        candidate = git(self.root, "rev-parse", "HEAD")
        git(self.root, "checkout", "-q", "coordinator")
        return candidate


class Stage2AuthorityAdmissionTests(unittest.TestCase):
    def setUp(self) -> None:
        self.temporary = tempfile.TemporaryDirectory()
        self.repository = Repository(Path(self.temporary.name))
        self.lock_path = Path("/private/tmp") / f"gravity-stage2-{os.getpid()}-{uuid.uuid4().hex}.lock"
        descriptor = os.open(self.lock_path, os.O_RDWR | os.O_CREAT | os.O_EXCL, 0o600)
        os.write(descriptor, b"existing owner payload\n")
        os.close(descriptor)

    def tearDown(self) -> None:
        self.lock_path.unlink(missing_ok=True)
        self.temporary.cleanup()

    def call(self, candidate: str, **options):
        values = dict(
            root=self.repository.root,
            base=self.repository.base,
            candidate=candidate,
            probe_only=True,
            lock_path=self.lock_path,
            module_catalog=CATALOG,
        )
        values.update(options)
        return admission.admit(**values)

    def test_failure_details_cannot_override_reserved_receipt_invariants(self) -> None:
        error = admission.AdmissionError("MALICIOUS", "refused")
        error.details.update(
            schema="evil", status="admitted", exit_code=0,
            authority_granted=True, integration_admission_granted=True,
            proof_authority_granted=True, authority_scope="release",
            diagnostic="PASS", message="passed", advisory=False,
            base="other", candidate="other",
        )
        result = admission._failure(
            error, base=self.repository.base, candidate=self.repository.base,
            advisory=True,
        )
        self.assertEqual(admission.SCHEMA, result["schema"])
        self.assertEqual("failed", result["status"])
        self.assertEqual(75, result["exit_code"])
        self.assertEqual("MALICIOUS", result["diagnostic"])
        self.assertTrue(result["advisory"])
        self.assertFalse(result["authority_granted"])
        self.assertFalse(result["integration_admission_granted"])
        self.assertFalse(result["proof_authority_granted"])
        self.assertEqual("none", result["authority_scope"])
        self.assertEqual(self.repository.base, result["base"])
        self.assertEqual(self.repository.base, result["candidate"])

    def hard(self, candidate: str, **options):
        return self.call(
            candidate,
            probe_only=False,
            command=["git", "merge", "--ff-only", candidate],
            **options,
        )

    def plumbing_fast_forward(self, root: Path, candidate: str) -> None:
        branch = git(root, "symbolic-ref", "HEAD")
        old = git(root, "rev-parse", "HEAD")
        git(root, "read-tree", "--reset", "-u", candidate)
        git(root, "update-ref", branch, candidate, old)

    def test_hard_success_is_fixed_clean_lock_held_and_no_write(self) -> None:
        candidate = self.repository.candidate(
            lambda: self.repository.write("deps.edn", "{:paths [\"next\"]}\n")
        )
        before_bytes = self.lock_path.read_bytes()
        before_stat = self.lock_path.stat()
        blocked: list[bool] = []

        def fixed(root: Path, target: str) -> None:
            contender = os.open(self.lock_path, os.O_RDWR)
            try:
                with self.assertRaises(BlockingIOError):
                    fcntl.flock(contender, fcntl.LOCK_EX | fcntl.LOCK_NB)
                blocked.append(True)
            finally:
                os.close(contender)
            self.plumbing_fast_forward(root, target)

        with mock.patch.object(admission, "_fixed_fast_forward", side_effect=fixed):
            result = self.hard(candidate)
        after_stat = self.lock_path.stat()
        self.assertEqual(0, result["exit_code"])
        self.assertFalse(result["authority_granted"])
        self.assertTrue(result["integration_admission_granted"])
        self.assertFalse(result["proof_authority_granted"])
        self.assertFalse(result["lock_mode_migrated"])
        self.assertEqual("0600", result["lock_mode"])
        self.assertEqual(str(self.lock_path), result["lock_canonical_path"])
        self.assertEqual([True], blocked)
        self.assertEqual(candidate, git(self.repository.root, "rev-parse", "HEAD"))
        self.assertEqual(before_bytes, self.lock_path.read_bytes())
        self.assertEqual(
            (before_stat.st_dev, before_stat.st_ino, before_stat.st_size, before_stat.st_mtime_ns),
            (after_stat.st_dev, after_stat.st_ino, after_stat.st_size, after_stat.st_mtime_ns),
        )

    def test_real_fixed_fast_forward_updates_named_coordinator_branch(self) -> None:
        candidate = self.repository.candidate(lambda: self.repository.write("README.md", "real ff\n"))
        result = self.hard(candidate)
        self.assertEqual(0, result["exit_code"])
        self.assertTrue(result["integration_admission_granted"])
        self.assertFalse(result["proof_authority_granted"])
        self.assertEqual(candidate, git(self.repository.root, "rev-parse", "HEAD"))
        self.assertEqual("coordinator", git(self.repository.root, "branch", "--show-current"))

    def test_all_declared_heavy_lock_users_share_canonical_namespace(self) -> None:
        canonical = admission.sh07.canonical_shared_lock_path(admission.sh07.DEFAULT_LOCK)
        self.assertEqual(admission.DEFAULT_LOCK, canonical)
        self.assertEqual(canonical, development._resource_lock_path(str(admission.sh07.DEFAULT_LOCK)))
        values = heartbeat.validated_arguments([
            "--log", str(self.repository.root / "log"),
            "--lock", str(admission.sh07.DEFAULT_LOCK), "--",
            sys.executable, "-c", "pass",
        ])
        self.assertEqual(canonical, values.lock)
        manifest = json.loads((TOOLS / "development_verification_manifest.json").read_text())
        heavy = [item["lock"] for item in manifest["checks"] if item.get("lock")]
        self.assertTrue(heavy)
        self.assertTrue(all(
            admission.sh07.canonical_shared_lock_path(Path(item)) == canonical
            for item in heavy
        ))

    def test_busy_hard_queues_before_dynamic_provider(self) -> None:
        candidate = self.repository.candidate(lambda: self.repository.write("README.md", "next\n"))
        provider = mock.Mock(return_value=CATALOG)
        holder = os.open(self.lock_path, os.O_RDWR)
        fcntl.flock(holder, fcntl.LOCK_EX | fcntl.LOCK_NB)
        try:
            result = self.call(
                candidate, probe_only=False,
                command=["git", "merge", "--ff-only", candidate],
                module_catalog=None, catalog_provider=provider,
            )
        finally:
            fcntl.flock(holder, fcntl.LOCK_UN)
            os.close(holder)
        self.assertEqual("STAGE2-ADMISSION-LOCK-BUSY", result["diagnostic"])
        self.assertEqual("queued", result["status"])
        self.assertFalse(result["authority_granted"])
        self.assertFalse(result["lock_mode_migrated"])
        provider.assert_not_called()
        with mock.patch.object(admission, "_fixed_fast_forward", side_effect=self.plumbing_fast_forward):
            released = self.call(
                candidate, probe_only=False,
                command=["git", "merge", "--ff-only", candidate],
                module_catalog=None, catalog_provider=provider,
            )
        self.assertEqual(0, released["exit_code"])
        provider.assert_called_once_with()

    def test_busy_and_dirty_advisory_never_call_dynamic_provider(self) -> None:
        candidate = self.repository.candidate(lambda: self.repository.write("README.md", "next\n"))
        provider = mock.Mock(return_value=CATALOG)
        holder = os.open(self.lock_path, os.O_RDWR)
        fcntl.flock(holder, fcntl.LOCK_EX | fcntl.LOCK_NB)
        try:
            busy = self.call(candidate, module_catalog=None, catalog_provider=provider)
        finally:
            fcntl.flock(holder, fcntl.LOCK_UN)
            os.close(holder)
        self.assertEqual("STAGE2-ADMISSION-LOCK-BUSY", busy["diagnostic"])
        provider.assert_not_called()
        self.repository.write("user.tmp", "dirty\n")
        dirty = self.call(candidate, module_catalog=None, catalog_provider=provider)
        self.assertEqual("STAGE2-ADMISSION-DIRTY", dirty["diagnostic"])
        provider.assert_not_called()

    def test_dirty_authority_input_refuses_before_provider_or_mutation(self) -> None:
        candidate = self.repository.candidate(lambda: self.repository.write("README.md", "next\n"))
        self.repository.write("deps.edn", "dirty\n")
        provider = mock.Mock(return_value=CATALOG)
        with mock.patch.object(admission, "_fixed_fast_forward") as fixed:
            result = self.call(
                candidate, probe_only=False,
                command=["git", "merge", "--ff-only", candidate],
                module_catalog=None, catalog_provider=provider,
            )
        self.assertEqual("STAGE2-ADMISSION-DIRTY", result["diagnostic"])
        self.assertEqual("dirty\n", (self.repository.root / "deps.edn").read_text())
        provider.assert_not_called()
        fixed.assert_not_called()

    def test_tracked_untracked_symlink_and_ignored_victims_are_never_touched(self) -> None:
        candidate = self.repository.candidate(lambda: self.repository.write("README.md", "next\n"))
        cases = (
            ("tracked", lambda: self.repository.write("README.md", "user tracked\n"), "README.md"),
            ("untracked", lambda: self.repository.write("user.tmp", "user untracked\n"), "user.tmp"),
            ("ignored", lambda: self.repository.write("ignored.tmp", "user ignored\n"), "ignored.tmp"),
            ("symlink", lambda: (self.repository.root / "link.tmp").symlink_to("README.md"), "link.tmp"),
        )
        for name, setup, relative in cases:
            with self.subTest(name=name):
                git(self.repository.root, "reset", "--hard", "-q", self.repository.base)
                for extra in ("user.tmp", "ignored.tmp", "link.tmp"):
                    (self.repository.root / extra).unlink(missing_ok=True)
                setup()
                before = os.lstat(self.repository.root / relative)
                with mock.patch.object(admission, "_fixed_fast_forward") as fixed:
                    result = self.hard(candidate)
                after = os.lstat(self.repository.root / relative)
                self.assertEqual("STAGE2-ADMISSION-DIRTY", result["diagnostic"])
                self.assertEqual((before.st_dev, before.st_ino), (after.st_dev, after.st_ino))
                fixed.assert_not_called()

    def test_arbitrary_reset_clean_and_checkout_commands_refuse_before_mutation(self) -> None:
        candidate = self.repository.candidate(lambda: self.repository.write("README.md", "next\n"))
        self.repository.write("user.tmp", "preserve me\n")
        for command in (
            ["git", "reset", "--hard", candidate],
            ["git", "clean", "-fdx"],
            ["git", "checkout", candidate],
        ):
            with self.subTest(command=command):
                result = self.call(candidate, probe_only=False, command=command)
                self.assertEqual("STAGE2-ADMISSION-COMMAND-UNSAFE", result["diagnostic"])
                self.assertFalse(result["authority_granted"])
                self.assertEqual("preserve me\n", (self.repository.root / "user.tmp").read_text())

    def test_transient_shared_mutation_before_lock_refuses_without_provider(self) -> None:
        candidate = self.repository.candidate(lambda: self.repository.write("README.md", "next\n"))
        provider = mock.Mock(return_value=CATALOG)
        with mock.patch.object(admission, "_fixed_fast_forward") as fixed:
            result = self.call(
                candidate, probe_only=False,
                command=["git", "merge", "--ff-only", candidate],
                module_catalog=None, catalog_provider=provider,
                before_lock=lambda: self.repository.write("deps.edn", "transient mutation\n"),
            )
        self.assertEqual("STAGE2-ADMISSION-DIRTY", result["diagnostic"])
        provider.assert_not_called()
        fixed.assert_not_called()

    def test_provider_side_effect_is_rejected_before_fixed_operation(self) -> None:
        candidate = self.repository.candidate(lambda: self.repository.write("README.md", "next\n"))
        def provider():
            self.repository.write("ignored.tmp", "provider side effect\n")
            return CATALOG
        with mock.patch.object(admission, "_fixed_fast_forward") as fixed:
            result = self.call(
                candidate, probe_only=False,
                command=["git", "merge", "--ff-only", candidate],
                module_catalog=None, catalog_provider=provider,
            )
        self.assertEqual("STAGE2-ADMISSION-DIRTY", result["diagnostic"])
        fixed.assert_not_called()

    def test_provider_created_operational_cpcache_is_allowed_for_hard_fast_forward(self) -> None:
        candidate = self.repository.candidate(lambda: self.repository.write("README.md", "next\n"))
        self.repository.write(".cpcache/existing.edn", "{}\n")
        def provider():
            self.repository.write(".cpcache/provider.edn", "{}\n")
            return CATALOG
        with mock.patch.object(
            admission, "_fixed_fast_forward", side_effect=self.plumbing_fast_forward
        ):
            result = self.call(
                candidate, probe_only=False,
                command=["git", "merge", "--ff-only", candidate],
                module_catalog=None, catalog_provider=provider,
            )
        self.assertEqual(0, result["exit_code"])
        self.assertFalse(result["authority_granted"])
        self.assertTrue(result["integration_admission_granted"])

    def test_ignored_classpath_shadow_is_rejected(self) -> None:
        candidate = self.repository.candidate(lambda: self.repository.write("README.md", "next\n"))
        self.repository.write("bootstrap/clojure/test/gravity/Shadow.class", "bytecode\n")
        with mock.patch.object(admission, "_fixed_fast_forward") as fixed:
            result = self.hard(candidate)
        self.assertEqual("STAGE2-ADMISSION-DIRTY", result["diagnostic"])
        fixed.assert_not_called()

    def test_git_operation_and_ignored_state_fail_closed(self) -> None:
        candidate = self.repository.candidate(lambda: self.repository.write("README.md", "next\n"))
        git_dir = Path(git(self.repository.root, "rev-parse", "--absolute-git-dir"))
        (git_dir / "MERGE_HEAD").write_text(candidate + "\n", encoding="ascii")
        with mock.patch.object(admission, "_fixed_fast_forward") as fixed:
            result = self.hard(candidate)
        self.assertEqual("STAGE2-ADMISSION-DIRTY", result["diagnostic"])
        self.assertIn("MERGE_HEAD", result["repository_operation_state"])
        fixed.assert_not_called()

    def test_candidate_must_be_immutable_oid_and_branch_ref_drift_is_irrelevant(self) -> None:
        candidate = self.repository.candidate(lambda: self.repository.write("README.md", "next\n"))
        result = self.call("candidate-1")
        self.assertEqual("STAGE2-ADMISSION-CANDIDATE-UNUSABLE", result["diagnostic"])
        self.assertFalse(result["authority_granted"])
        git(self.repository.root, "branch", "-f", "candidate-1", self.repository.base)
        exact = self.call(candidate)
        self.assertEqual(0, exact["exit_code"])
        self.assertEqual(candidate, exact["candidate_oid"])

    def test_range_drift_after_plan_refuses_under_lock(self) -> None:
        candidate = self.repository.candidate(lambda: self.repository.write("README.md", "next\n"))
        def move() -> None:
            self.repository.write("local.txt", "new base\n")
            git(self.repository.root, "add", "local.txt")
            git(self.repository.root, "commit", "-q", "-m", "move")
        with mock.patch.object(admission, "_fixed_fast_forward") as fixed:
            result = self.hard(candidate, before_lock=move)
        self.assertEqual("STAGE2-ADMISSION-RANGE-DRIFT", result["diagnostic"])
        fixed.assert_not_called()

    def test_advisory_unrelated_does_not_touch_busy_lock_but_shared_queues(self) -> None:
        unrelated = self.repository.candidate(lambda: self.repository.write("README.md", "next\n"))
        shared = self.repository.candidate(lambda: self.repository.write("deps.edn", "{:paths []}\n"))
        holder = os.open(self.lock_path, os.O_RDWR)
        fcntl.flock(holder, fcntl.LOCK_EX | fcntl.LOCK_NB)
        try:
            passed = self.call(unrelated)
            queued = self.call(shared)
        finally:
            fcntl.flock(holder, fcntl.LOCK_UN)
            os.close(holder)
        self.assertEqual(0, passed["exit_code"])
        self.assertFalse(passed["lock_required"])
        self.assertEqual("STAGE2-ADMISSION-LOCK-BUSY", queued["diagnostic"])
        self.assertFalse(queued["authority_granted"])

    def test_rename_delete_and_symlink_type_are_classified_fail_closed(self) -> None:
        def rename() -> None:
            (self.repository.root / "deps.edn").rename(self.repository.root / "deps.next.edn")
        renamed = self.repository.candidate(rename)
        result = self.call(renamed)
        self.assertIn("deps.edn", result["relevant_paths"])

        def symlink() -> None:
            path = self.repository.root / "bootstrap/clojure/src/gravity/core.clj"
            path.unlink()
            path.symlink_to("replacement.clj")
        unsafe = self.repository.candidate(symlink)
        rejected = self.call(unsafe)
        self.assertEqual("STAGE2-ADMISSION-CANDIDATE-UNUSABLE", rejected["diagnostic"])

    def test_candidate_class_additions_in_src_and_test_are_unsafe(self) -> None:
        for relative in (
            "bootstrap/clojure/src/gravity/Injected.class",
            "bootstrap/clojure/test/gravity/Injected.class",
        ):
            with self.subTest(relative=relative), tempfile.TemporaryDirectory() as directory:
                repository = Repository(Path(directory))
                def add_class() -> None:
                    repository.write(relative, "bytecode\n")
                    git(repository.root, "add", "-f", relative)
                candidate = repository.candidate(add_class)
                result = admission.admit(
                    root=repository.root, base=repository.base, candidate=candidate,
                    probe_only=True, lock_path=self.lock_path, module_catalog=CATALOG,
                )
                self.assertEqual("STAGE2-ADMISSION-CANDIDATE-UNUSABLE", result["diagnostic"])

    def test_candidate_deletion_of_preexisting_class_shadow_is_relevant_cleanup(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            repository = Repository(Path(directory))
            relative = "bootstrap/clojure/test/gravity/Old.class"
            repository.write(relative, "old bytecode\n")
            git(repository.root, "add", "-f", relative)
            git(repository.root, "commit", "-q", "-m", "invalid old shadow")
            repository.base = git(repository.root, "rev-parse", "HEAD")
            candidate = repository.candidate(lambda: (repository.root / relative).unlink())
            result = admission.admit(
                root=repository.root, base=repository.base, candidate=candidate,
                probe_only=True, lock_path=self.lock_path, module_catalog=CATALOG,
            )
            self.assertEqual(0, result["exit_code"])
            self.assertIn(relative, result["relevant_paths"])

    def test_lock_replacement_between_open_and_flock_never_yields(self) -> None:
        replacement = self.lock_path.with_suffix(".replacement")
        descriptor = os.open(replacement, os.O_RDWR | os.O_CREAT | os.O_EXCL, 0o600)
        os.close(descriptor)
        yielded: list[bool] = []
        def replace() -> None:
            os.replace(replacement, self.lock_path)
        with self.assertRaises(admission.AdmissionError) as captured:
            with admission.no_write_lock_lease(self.lock_path, _before_flock=replace):
                yielded.append(True)
        self.assertEqual("STAGE2-ADMISSION-LOCK-UNSAFE", captured.exception.diagnostic)
        self.assertEqual([], yielded)
        with admission.no_write_lock_lease(self.lock_path):
            pass

    def test_body_failure_plus_lock_replacement_reports_lock_unsafe(self) -> None:
        replacement = self.lock_path.with_suffix(".body-replacement")
        descriptor = os.open(replacement, os.O_RDWR | os.O_CREAT | os.O_EXCL, 0o600)
        os.close(descriptor)
        with self.assertRaises(admission.AdmissionError) as captured:
            with admission.no_write_lock_lease(self.lock_path):
                os.replace(replacement, self.lock_path)
                raise admission.AdmissionError("BODY-FAILURE", "body failed")
        self.assertEqual("STAGE2-ADMISSION-LOCK-UNSAFE", captured.exception.diagnostic)
        self.assertIsInstance(captured.exception.__cause__, admission.AdmissionError)
        self.assertEqual("BODY-FAILURE", captured.exception.__cause__.diagnostic)

    def test_provider_catalog_error_with_stable_lock_is_not_lock_unsafe(self) -> None:
        candidate = self.repository.candidate(lambda: self.repository.write("README.md", "next\n"))
        result = self.call(
            candidate, probe_only=False,
            command=["git", "merge", "--ff-only", candidate],
            module_catalog=None, catalog_provider=lambda: {},
        )
        self.assertEqual("STAGE2-ADMISSION-CANDIDATE-UNUSABLE", result["diagnostic"])
        self.assertNotEqual("STAGE2-ADMISSION-LOCK-UNSAFE", result["diagnostic"])

    def test_custom_parent_lock_is_rejected_before_parent_swap_can_split_inode(self) -> None:
        custom = self.repository.root / "locks/heavy.lock"
        custom.parent.mkdir()
        with self.assertRaises(admission.AdmissionError) as captured:
            with admission.no_write_lock_lease(custom):
                pass
        self.assertEqual("STAGE2-ADMISSION-LOCK-UNSAFE", captured.exception.diagnostic)
        self.assertFalse(custom.exists())

    def test_free_legacy_lock_is_migrated_only_after_exclusive_acquisition(self) -> None:
        os.chmod(self.lock_path, 0o644)
        before = self.lock_path.read_bytes()
        with admission.no_write_lock_lease(self.lock_path) as lease:
            self.assertTrue(lease.mode_migrated)
            self.assertEqual(self.lock_path, lease.path)
            self.assertEqual(0o600, self.lock_path.stat().st_mode & 0o777)
        self.assertEqual(before, self.lock_path.read_bytes())

    def test_absent_canonical_lock_is_created_0600_without_content(self) -> None:
        self.lock_path.unlink()
        with admission.no_write_lock_lease(self.lock_path) as lease:
            self.assertFalse(lease.mode_migrated)
            self.assertEqual(0o600, self.lock_path.stat().st_mode & 0o777)
            self.assertEqual(b"", self.lock_path.read_bytes())

    def test_legacy_0644_held_inode_reports_busy_to_all_three_tools(self) -> None:
        os.chmod(self.lock_path, 0o644)
        holder = os.open(self.lock_path, os.O_RDWR | os.O_NOFOLLOW)
        fcntl.flock(holder, fcntl.LOCK_EX | fcntl.LOCK_NB)
        try:
            with self.assertRaises(admission.AdmissionError) as captured:
                with admission.no_write_lock_lease(self.lock_path):
                    pass
            self.assertEqual("STAGE2-ADMISSION-LOCK-BUSY", captured.exception.diagnostic)
            self.assertFalse(captured.exception.details["lock_mode_migrated"])

            root = self.repository.root
            heartbeat_exit = heartbeat.run([
                "--log", str(root / "heartbeat.log"),
                "--status", str(root / "heartbeat.json"),
                "--lock", str(self.lock_path), "--quiet", "--",
                sys.executable, "-c", "raise SystemExit('must not run')",
            ])
            self.assertEqual(75, heartbeat_exit)
            self.assertEqual("lock-unavailable", json.loads((root / "heartbeat.json").read_text())["state"])

            authoritative = admission.sh07.open_lock_file(self.lock_path, create=False)
            try:
                with self.assertRaises(BlockingIOError):
                    fcntl.flock(authoritative.descriptor, fcntl.LOCK_EX | fcntl.LOCK_NB)
            finally:
                authoritative.close()
        finally:
            fcntl.flock(holder, fcntl.LOCK_UN)
            os.close(holder)
        self.assertEqual(0o644, self.lock_path.stat().st_mode & 0o777)
        with admission.no_write_lock_lease(self.lock_path) as lease:
            self.assertTrue(lease.mode_migrated)
        self.assertEqual(0o600, self.lock_path.stat().st_mode & 0o777)

    def test_cli_documented_separator_and_unsafe_command_are_structured(self) -> None:
        candidate = self.repository.candidate(lambda: self.repository.write("README.md", "next\n"))
        command = [
            sys.executable, str(TOOLS / "stage2_authority_admission.py"),
            "--cwd", str(self.repository.root), "--base", self.repository.base,
            "--candidate", candidate, "--lock", str(self.lock_path),
            "--exec", "--", "git", "reset", "--hard", candidate,
        ]
        completed = subprocess.run(
            command, capture_output=True, text=True, check=False,
            env={**os.environ, "PYTHONDONTWRITEBYTECODE": "1"},
        )
        receipt = json.loads(completed.stdout)
        self.assertEqual(75, completed.returncode)
        self.assertEqual("STAGE2-ADMISSION-COMMAND-UNSAFE", receipt["diagnostic"])
        self.assertFalse(receipt["authority_granted"])

    def test_cli_documented_separator_reaches_admission_without_separator(self) -> None:
        candidate = self.repository.candidate(lambda: self.repository.write("README.md", "next\n"))
        receipt = {
            "schema": admission.SCHEMA, "status": "admitted", "diagnostic": "ok",
            "exit_code": 0, "advisory": False, "authority_granted": False,
            "integration_admission_granted": True,
            "proof_authority_granted": False,
        }
        output = io.StringIO()
        arguments = [
            "--cwd", str(self.repository.root), "--base", self.repository.base,
            "--candidate", candidate, "--lock", str(self.lock_path),
            "--exec", "--", "git", "merge", "--ff-only", candidate,
        ]
        with mock.patch.object(admission, "admit", return_value=receipt) as invoked:
            with contextlib.redirect_stdout(output):
                exit_code = admission.main(arguments)
        self.assertEqual(0, exit_code)
        self.assertEqual(receipt, json.loads(output.getvalue()))
        self.assertEqual(
            ["git", "merge", "--ff-only", candidate],
            invoked.call_args.kwargs["command"],
        )


if __name__ == "__main__":
    unittest.main()
