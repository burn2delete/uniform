#!/usr/bin/env python3
"""Positive and adversarial tests for the reporting-only artifact census."""

from __future__ import annotations

import copy
import hashlib
import inspect
import json
import os
from pathlib import Path
import stat
import sys
import tempfile
import threading
import unittest
from unittest import mock

TOOLS = Path(__file__).resolve().parents[1]
ROOT = TOOLS.parent
if str(TOOLS) not in sys.path:
    sys.path.insert(0, str(TOOLS))

import validate_artifact_census as census
import verify_development


def git_oid(content: bytes) -> str:
    digest = hashlib.sha1()
    digest.update(f"blob {len(content)}\0".encode("ascii"))
    digest.update(content)
    return digest.hexdigest()


def index_entry(path: str, content: bytes, mode: str = "100644") -> census.IndexedFile:
    return census.IndexedFile(path=path, mode=mode, oid=git_oid(content), size=len(content))


class ArtifactCensusTests(unittest.TestCase):
    def test_current_contract_matches_index_and_worktree(self) -> None:
        contract = census.load_json(ROOT / "contracts" / "artifact-census.json")
        fmt = census._object_format(ROOT)
        entries = census.discover_index(ROOT, object_format=fmt)
        # The preflight contract check owns the full current-worktree scan.
        # This unit check keeps the exact index/contract identity gate without
        # hashing all 29 MiB a second time in the same development graph.
        self.assertEqual(census.validate_contract(contract, entries, root=None, object_format=fmt), [])
        self.assertGreater(contract["totals"]["unclassified_file_count"], 0)
        self.assertEqual(contract["status"], "incomplete")
        self.assertEqual(contract["authority"], "none")

    def test_streaming_reader_does_not_accumulate_full_file_bytes(self) -> None:
        self.assertNotIn("data +=", inspect.getsource(census._read_worktree_digest))
        self.assertIn("os.read", inspect.getsource(census._read_worktree_digest))

    def test_bounded_second_pass_rejects_mutation_after_first_digest(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            artifact_dir = root / "target" / "validation"
            artifact_dir.mkdir(parents=True)
            path = artifact_dir / "mutable"
            path.write_bytes(b"old")
            entry = index_entry("target/validation/mutable", b"old")
            original = census._read_worktree_digest
            calls = 0

            def digest_then_mutate(worktree_root: Path, item: census.IndexedFile, fmt: str):
                nonlocal calls
                result = original(worktree_root, item, fmt)
                calls += 1
                if calls == 1:
                    path.write_bytes(b"new")
                return result

            with mock.patch.object(census, "_read_worktree_digest", side_effect=digest_then_mutate):
                errors = census.validate_entries([entry], root=root)
            self.assertTrue(any("worktree bytes differ" in error for error in errors), errors)
            self.assertGreaterEqual(calls, 2)

    def test_git_pipe_drains_output_while_writing_bounded_input(self) -> None:
        entry = census.discover_index(ROOT)[0]
        request = (entry.oid + "\n").encode("ascii") * 100_000
        output = census._run_git(
            ROOT,
            ["cat-file", "--batch-check=%(objectname) %(objecttype) %(objectsize)"],
            input_bytes=request,
            max_output_bytes=8 * 1024 * 1024,
        )
        self.assertGreater(len(output), 64 * 1024)
        with self.assertRaises(ValueError):
            census._run_git(
                ROOT,
                ["cat-file", "--batch-check=%(objectname) %(objecttype) %(objectsize)"],
                input_bytes=request,
                max_output_bytes=1024,
            )

    def test_git_output_overflow_reaps_child_and_closes_pipes(self) -> None:
        real_popen = census.subprocess.Popen

        def assert_overflow(stream_fd: int, expected: str, max_output_bytes: int) -> None:
            created: list[object] = []

            def spawn(*args: object, **kwargs: object):
                process = real_popen(
                    [
                        sys.executable,
                        "-c",
                        f"import os; os.write({stream_fd}, b'x' * (2 * 1024 * 1024))",
                    ],
                    **kwargs,
                )
                created.append(process)
                return process

            before_threads = {thread.ident for thread in threading.enumerate()}
            with self.subTest(stream_fd=stream_fd):
                with mock.patch.object(census.subprocess, "Popen", side_effect=spawn):
                    with self.assertRaisesRegex(ValueError, expected):
                        census._run_git(
                            ROOT,
                            ["overflow-probe"],
                            max_output_bytes=max_output_bytes,
                        )
                self.assertEqual(len(created), 1)
                process = created[0]
                self.assertIsNotNone(process.poll())
                for stream in (process.stdin, process.stdout, process.stderr):
                    if stream is not None:
                        self.assertTrue(stream.closed)
                self.assertEqual(
                    before_threads,
                    {thread.ident for thread in threading.enumerate()},
                )

        assert_overflow(1, "output exceeds 1024 bytes", 1024)
        assert_overflow(2, "stderr exceeds 1048576 bytes", census.MAX_GIT_OUTPUT_BYTES)

    def test_git_setup_failures_reap_child_and_close_pipes(self) -> None:
        real_popen = census.subprocess.Popen
        created: list[object] = []

        def spawn(*args: object, **kwargs: object):
            process = real_popen(*args, **kwargs)
            created.append(process)
            return process

        def assert_setup_failure(patcher: object, message: str) -> None:
            with self.subTest(message=message):
                created.clear()
                with mock.patch.object(census.subprocess, "Popen", side_effect=spawn):
                    with patcher:
                        with self.assertRaisesRegex(RuntimeError, message):
                            census._run_git(ROOT, ["--version"])
                self.assertEqual(len(created), 1)
                process = created[0]
                self.assertIsNotNone(process.poll())
                for stream in (process.stdin, process.stdout, process.stderr):
                    if stream is not None:
                        self.assertTrue(stream.closed)

        class FailingSelector:
            def register(self, *args: object, **kwargs: object) -> None:
                raise RuntimeError("register failure")

            def close(self) -> None:
                return None

        assert_setup_failure(
            mock.patch.object(census.selectors, "DefaultSelector", FailingSelector),
            "register failure",
        )
        assert_setup_failure(
            mock.patch.object(census.os, "set_blocking", side_effect=RuntimeError("set_blocking failure")),
            "set_blocking failure",
        )

    def test_fixed_classes_reject_unknown_or_overlapping_rule_laundering(self) -> None:
        contract = census.load_json(ROOT / "contracts" / "artifact-census.json")
        entries = census.discover_index(ROOT)
        mutated = copy.deepcopy(contract)
        mutated["classes"][0]["path_rule"] = "prefix:docs/artifacts/**"
        self.assertTrue(any("path_rule" in error for error in census.validate_contract(mutated, entries, root=None)))
        mutated = copy.deepcopy(contract)
        mutated["classes"][0]["id"] = "forged-class"
        self.assertTrue(any("fixed class order" in error or "unknown class" in error for error in census.validate_contract(mutated, entries, root=None)))
        self.assertEqual(
            census.classify_path("docs/artifacts/phase-18/fixtures/accepted.json"),
            "reviewed-phase18-artifacts",
        )
        self.assertEqual(
            census.classify_path("docs/artifacts/phase-99/unknown.json"),
            "unclassified-artifacts",
        )
        for phase in ("19", "99"):
            self.assertEqual(
                census.classify_path(f"docs/artifacts/phase-{phase}/fixtures/future.json"),
                "unclassified-artifacts",
            )

    def test_index_identity_drift_is_blocking(self) -> None:
        contract = census.load_json(ROOT / "contracts" / "artifact-census.json")
        entries = census.discover_index(ROOT)
        changed = list(entries)
        first = changed[0]
        changed[0] = census.IndexedFile(first.path, first.mode, "0" * len(first.oid), first.size)
        errors = census.validate_contract(contract, changed, root=None)
        self.assertTrue(any("identities drifted" in error for error in errors), errors)

    def test_status_and_authority_cannot_be_laundered(self) -> None:
        contract = census.load_json(ROOT / "contracts" / "artifact-census.json")
        entries = census.discover_index(ROOT)
        for field, value in (("status", "complete"), ("authority", "trusted")):
            mutated = copy.deepcopy(contract)
            mutated[field] = value
            errors = census.validate_contract(mutated, entries, root=None)
            self.assertTrue(any(field in error for error in errors), errors)

    def test_duplicate_json_keys_and_bounds_fail_closed(self) -> None:
        with tempfile.TemporaryDirectory(dir="/private/tmp") as directory:
            path = Path(directory) / "duplicate.json"
            path.write_text('{"schema_version": 1, "schema_version": 2}', encoding="utf-8")
            with self.assertRaises(census.DuplicateKeyError):
                census.load_json(path)
            hardlink = Path(directory) / "hardlink.json"
            hardlink.write_text("{}", encoding="utf-8")
            hardlink_alias = Path(directory) / "hardlink-alias.json"
            os.link(hardlink, hardlink_alias)
            with self.assertRaises(OSError):
                census.load_json(hardlink_alias)
        oversized = census.IndexedFile("target/validation/x", "100644", "0" * 40, census.MAX_BLOB_BYTES + 1)
        self.assertTrue(any("oversized" in error for error in census.validate_entries([oversized], root=None)))
        with self.assertRaises(ValueError):
            census._parse_json_int(str(census.MAX_TOTAL_BLOB_BYTES + 1))

    def test_over_budget_repeated_blob_skips_worktree_reads(self) -> None:
        entry_a = index_entry("target/validation/a", b"123456")
        entry_b = index_entry("target/validation/b", b"123456")
        with mock.patch.object(census, "MAX_TOTAL_BLOB_BYTES", 8), mock.patch.object(
            census, "_read_worktree_digest"
        ) as digest:
            errors = census.validate_entries(
                [entry_a, entry_b], root=Path("/nonexistent-artifact-census-root")
            )
        self.assertTrue(any("index blob total" in error for error in errors), errors)
        digest.assert_not_called()

    def test_symlink_hardlink_and_special_files_are_rejected(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            artifact_dir = root / "target" / "validation"
            artifact_dir.mkdir(parents=True)
            target = artifact_dir / "real"
            target.write_bytes(b"artifact")
            dirty = artifact_dir / "dirty"
            dirty.write_bytes(b"new-bytes")
            errors = census.validate_entries([index_entry("target/validation/dirty", b"old-bytes")], root=root)
            self.assertTrue(any("worktree bytes differ" in error for error in errors), errors)
            symlink = artifact_dir / "symlink"
            symlink.symlink_to(target.name)
            errors = census.validate_entries([index_entry("target/validation/symlink", b"artifact")], root=root)
            self.assertTrue(any("cannot read exact worktree file" in error for error in errors), errors)

            hard_a = artifact_dir / "hard-a"
            hard_b = artifact_dir / "hard-b"
            hard_a.write_bytes(b"hardlink")
            os.link(hard_a, hard_b)
            errors = census.validate_entries(
                [index_entry("target/validation/hard-a", b"hardlink"), index_entry("target/validation/hard-b", b"hardlink")],
                root=root,
            )
            self.assertTrue(any("hardlinks" in error or "inode" in error for error in errors), errors)

            fifo = artifact_dir / "fifo"
            if hasattr(os, "mkfifo"):
                os.mkfifo(fifo)
                errors = census.validate_entries([index_entry("target/validation/fifo", b"fifo")], root=root)
                self.assertTrue(any("regular file" in error for error in errors), errors)

    def test_development_manifest_registers_exact_routing_and_cache_inputs(self) -> None:
        manifest = verify_development.load_manifest(ROOT / "tools" / "development_verification_manifest.json")
        by_id = verify_development.checks_by_id(manifest)
        contract_check = by_id["artifact-census-contract"]
        unit_check = by_id["artifact-census-unit"]
        self.assertEqual(contract_check["command"], ["python3", "tools/validate_artifact_census.py"])
        self.assertEqual(
            unit_check["command"],
            ["python3", "-m", "unittest", "tools.tests.test_validate_artifact_census", "-v"],
        )
        for check in (contract_check, unit_check):
            self.assertEqual(check["lane"], "preflight")
            self.assertEqual(check["cost"], "cheap")
            self.assertEqual(check["resource_class"], "python-cheap")
            self.assertIsNone(check["lock"])
            self.assertFalse(check["exclusive"])
            self.assertEqual(check["authority"], "none")
            self.assertEqual(check["daemonization"], "forbidden")
            self.assertTrue(check.get("fresh", False))
        exact_paths = {
            "contracts/artifact-census.json",
            "tools/validate_artifact_census.py",
            "tools/tests/test_validate_artifact_census.py",
        }
        for check in (contract_check, unit_check):
            self.assertTrue(exact_paths.issubset(set(check["inputs"])))
        self.assertTrue(
            {"docs/artifacts/**", "target/**"}.issubset(set(contract_check["inputs"]))
        )
        self.assertFalse(
            {"docs/artifacts/**", "target/**"}.intersection(set(unit_check["inputs"]))
        )
        for changed_path in (
            "contracts/artifact-census.json",
            "docs/artifacts/phase-18/review.json",
            "target/validation/candidate.json",
            "target/unclassified/new.json",
        ):
            selected = verify_development.select_impacted_checks(
                manifest, ROOT, changed_paths=[changed_path]
            )
            self.assertIn("artifact-census-contract", selected["selected_ids"], changed_path)
            self.assertIn("artifact-census-unit", selected["selected_ids"], changed_path)
        original_key = verify_development.cache_key(manifest, contract_check, ROOT)
        changed = copy.deepcopy(contract_check)
        changed["inputs"] = list(changed["inputs"]) + ["tools/validate_python_tooling_contract.py"]
        self.assertNotEqual(original_key, verify_development.cache_key(manifest, changed, ROOT))


if __name__ == "__main__":
    raise SystemExit(unittest.main())
