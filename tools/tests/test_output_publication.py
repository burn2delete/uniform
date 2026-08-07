#!/usr/bin/env python3
"""Focused tests for private atomic output publication."""

from __future__ import annotations

import errno
import math
import os
from pathlib import Path
import stat
import sys
import tempfile
import unittest
from unittest import mock

TOOLS = Path(__file__).resolve().parents[1]
if str(TOOLS) not in sys.path:
    sys.path.insert(0, str(TOOLS))

import output_publication as publication


class OutputPublicationTests(unittest.TestCase):
    def setUp(self) -> None:
        self.temporary = tempfile.TemporaryDirectory(prefix="gravity-publication-")
        self.base = Path(self.temporary.name).resolve()
        self.repository = self.base / "repository"
        self.repository.mkdir(mode=0o700)

    def tearDown(self) -> None:
        self.temporary.cleanup()

    def test_relative_and_repository_absolute_paths(self) -> None:
        relative = publication.atomic_write_text("reports/one.txt", "one", self.repository)
        absolute = publication.atomic_write_text(self.repository / "reports/two.txt", "two", self.repository)
        self.assertEqual(relative, self.repository / "reports/one.txt")
        self.assertEqual(absolute, self.repository / "reports/two.txt")
        self.assertEqual(relative.read_text(encoding="utf-8"), "one")
        self.assertEqual(absolute.read_text(encoding="utf-8"), "two")

    def test_rejects_outside_traversal_nul_and_noncanonical_paths(self) -> None:
        invalid = [
            self.base / "outside.txt", "", ".", "..", "../x", "a/../x",
            "a/./x", "a//x", "a/", "a\\x", "nul\0x",
        ]
        for path in invalid:
            with self.subTest(path=repr(path)), self.assertRaises(publication.OutputPublicationError):
                publication.atomic_write_text(path, "unsafe", self.repository)
        self.assertEqual(list(self.repository.iterdir()), [])

    def test_environment_and_explicit_isolation_preserve_layout(self) -> None:
        environment_root = self.base / "environment"
        explicit_root = self.base / "explicit"
        with mock.patch.dict(os.environ, {"GRAVITY_OUTPUT_ROOT": str(environment_root)}):
            env_path = publication.atomic_write_text(self.repository / "nested/env.txt", "env", self.repository)
            explicit_path = publication.atomic_write_text(
                "nested/explicit.txt", "explicit", self.repository, explicit_root
            )
        self.assertEqual(env_path, environment_root / "nested/env.txt")
        self.assertEqual(explicit_path, explicit_root / "nested/explicit.txt")
        self.assertFalse((self.repository / "nested").exists())

    def test_default_preserves_existing_mode_and_replaces_atomically(self) -> None:
        target = self.repository / "result.txt"
        target.write_text("old", encoding="utf-8")
        target.chmod(0o644)
        inode = target.stat().st_ino
        publication.atomic_write_text("result.txt", "new", self.repository)
        self.assertEqual(target.read_text(encoding="utf-8"), "new")
        self.assertNotEqual(target.stat().st_ino, inode)
        self.assertEqual(stat.S_IMODE(target.stat().st_mode), 0o644)

    def test_new_default_mode_and_explicit_mode(self) -> None:
        default = publication.atomic_write_text("default.txt", "new", self.repository)
        explicit = publication.atomic_write_text("explicit.txt", "new", self.repository, mode=0o640)
        self.assertEqual(stat.S_IMODE(default.stat().st_mode), 0o644)
        self.assertEqual(stat.S_IMODE(explicit.stat().st_mode), 0o640)

    def test_failure_cleans_temporary_and_does_not_publish_partial_output(self) -> None:
        real_fsync = os.fsync

        def fail_file_fsync(fd: int) -> None:
            if stat.S_ISREG(os.fstat(fd).st_mode):
                raise OSError("injected failure")
            real_fsync(fd)

        with mock.patch.object(publication.os, "fsync", side_effect=fail_file_fsync):
            with self.assertRaises(OSError):
                publication.atomic_write_text("nested/result.txt", "partial", self.repository)
        parent = self.repository / "nested"
        self.assertFalse((parent / "result.txt").exists())
        self.assertEqual(list(parent.iterdir()), [])

    def test_failed_replacement_leaves_existing_destination_intact(self) -> None:
        target = self.repository / "result.txt"
        target.write_text("old", encoding="utf-8")
        target.chmod(0o600)
        with mock.patch.object(publication.os, "replace", side_effect=OSError("injected failure")):
            with self.assertRaises(OSError):
                publication.atomic_write_text("result.txt", "new", self.repository)
        self.assertEqual(target.read_text(encoding="utf-8"), "old")
        self.assertEqual([item.name for item in self.repository.iterdir()], ["result.txt"])

    def test_rejects_symlink_parent_and_final(self) -> None:
        outside = self.base / "outside"
        outside.mkdir()
        (self.repository / "linked").symlink_to(outside, target_is_directory=True)
        with self.assertRaises(publication.OutputPublicationError):
            publication.atomic_write_text("linked/result.txt", "unsafe", self.repository)
        victim = self.base / "victim.txt"
        victim.write_text("victim", encoding="utf-8")
        (self.repository / "final.txt").symlink_to(victim)
        with self.assertRaises(publication.OutputPublicationError):
            publication.atomic_write_text("final.txt", "unsafe", self.repository)
        self.assertEqual(victim.read_text(encoding="utf-8"), "victim")

    def test_rejects_relative_root_escape_and_noncanonical_spelling(self) -> None:
        for root in (".", "..", "../outside", "a/../b", "a/./b", "a//b", "a\\b", "a/"):
            with self.subTest(root=root), self.assertRaises(publication.OutputPublicationError):
                publication.atomic_write_text("result.txt", "unsafe", self.repository, root)

    def test_rejects_selected_root_symlink(self) -> None:
        actual = self.base / "actual-root"
        actual.mkdir(mode=0o700)
        linked = self.base / "linked-root"
        linked.symlink_to(actual, target_is_directory=True)
        with self.assertRaises(publication.OutputPublicationError):
            publication.atomic_write_text("result.txt", "unsafe", self.repository, linked)
        self.assertEqual(list(actual.iterdir()), [])

    @unittest.skipUnless(Path("/tmp").is_symlink(), "system /tmp is not a symlink alias")
    def test_trusted_system_tmp_alias_is_canonicalized(self) -> None:
        with tempfile.TemporaryDirectory(prefix="gravity-output-", dir="/tmp") as temporary:
            selected = Path(temporary) / "isolated"
            result = publication.atomic_write_text(
                "target/result.txt", "safe", self.repository, selected
            )
            expected = (
                Path(os.path.realpath("/tmp"))
                / selected.relative_to("/tmp")
                / "target/result.txt"
            )
            self.assertEqual(result, expected)
            self.assertEqual(result.read_text(encoding="utf-8"), "safe")

    def test_rejects_hardlink_victim(self) -> None:
        victim = self.base / "victim.txt"
        victim.write_text("victim", encoding="utf-8")
        os.link(victim, self.repository / "final.txt")
        with self.assertRaises(publication.OutputPublicationError):
            publication.atomic_write_text("final.txt", "unsafe", self.repository)
        self.assertEqual(victim.read_text(encoding="utf-8"), "victim")

    def test_json_is_deterministic_strict_and_newline_terminated(self) -> None:
        target = publication.atomic_write_json("result.json", {"z": 1, "a": [2]}, self.repository)
        self.assertEqual(target.read_text(encoding="utf-8"), '{\n  "a": [\n    2\n  ],\n  "z": 1\n}\n')
        before = set(self.repository.iterdir())
        with self.assertRaises(ValueError):
            publication.atomic_write_json("nan.json", {"value": math.nan}, self.repository)
        self.assertEqual(set(self.repository.iterdir()), before)

    def test_parent_rename_and_recreate_never_returns_success(self) -> None:
        parent = self.repository / "nested"
        parent.mkdir(mode=0o700)
        moved = self.repository / "moved"
        real_replace = os.replace

        def replace_after_swap(src: str, dst: str, **kwargs: int) -> None:
            parent.rename(moved)
            parent.mkdir(mode=0o700)
            real_replace(src, dst, **kwargs)

        with mock.patch.object(publication.os, "replace", side_effect=replace_after_swap):
            with self.assertRaisesRegex(publication.OutputPublicationError, "commit location is uncertain.*parent changed"):
                publication.atomic_write_text("nested/result.txt", "complete", self.repository)
        self.assertFalse((parent / "result.txt").exists())
        self.assertEqual((moved / "result.txt").read_text(encoding="utf-8"), "complete")

    def test_parent_swap_during_final_directory_fsync_never_returns_success(self) -> None:
        parent = self.repository / "nested"
        parent.mkdir(mode=0o700)
        moved = self.repository / "moved"
        parent_identity = (parent.stat().st_dev, parent.stat().st_ino)
        real_fsync = os.fsync
        swapped = False

        def fsync_then_swap(fd: int) -> None:
            nonlocal swapped
            real_fsync(fd)
            info = os.fstat(fd)
            if not swapped and stat.S_ISDIR(info.st_mode) and (info.st_dev, info.st_ino) == parent_identity:
                swapped = True
                parent.rename(moved)
                parent.mkdir(mode=0o700)

        with mock.patch.object(publication.os, "fsync", side_effect=fsync_then_swap):
            with self.assertRaisesRegex(publication.OutputPublicationError, "commit location is uncertain.*parent changed"):
                publication.atomic_write_text("nested/result.txt", "complete", self.repository)
        self.assertTrue(swapped)
        self.assertFalse((parent / "result.txt").exists())
        self.assertEqual((moved / "result.txt").read_text(encoding="utf-8"), "complete")

    def test_rejects_target_replacement_between_snapshots(self) -> None:
        target = self.repository / "result.txt"
        target.write_text("original", encoding="utf-8")
        target.chmod(0o644)
        replacement = self.repository / "replacement.txt"
        replacement.write_text("replacement", encoding="utf-8")
        replacement.chmod(0o644)
        real_fsync = os.fsync
        changed = False

        def replace_after_file_fsync(fd: int) -> None:
            nonlocal changed
            real_fsync(fd)
            if not changed and stat.S_ISREG(os.fstat(fd).st_mode):
                changed = True
                replacement.replace(target)

        with mock.patch.object(publication.os, "fsync", side_effect=replace_after_file_fsync):
            with self.assertRaisesRegex(publication.OutputPublicationError, "target changed"):
                publication.atomic_write_text("result.txt", "new", self.repository)
        self.assertEqual(target.read_text(encoding="utf-8"), "replacement")

    def test_rejects_absent_target_appearing_before_replace(self) -> None:
        target = self.repository / "result.txt"
        real_fsync = os.fsync
        appeared = False

        def create_after_file_fsync(fd: int) -> None:
            nonlocal appeared
            real_fsync(fd)
            if not appeared and stat.S_ISREG(os.fstat(fd).st_mode):
                appeared = True
                target.write_text("appeared", encoding="utf-8")
                target.chmod(0o644)

        with mock.patch.object(publication.os, "fsync", side_effect=create_after_file_fsync):
            with self.assertRaisesRegex(publication.OutputPublicationError, "target changed"):
                publication.atomic_write_text("result.txt", "new", self.repository)
        self.assertEqual(target.read_text(encoding="utf-8"), "appeared")

    def test_rejects_target_mode_mutation_between_snapshots(self) -> None:
        target = self.repository / "result.txt"
        target.write_text("original", encoding="utf-8")
        target.chmod(0o644)
        real_fsync = os.fsync
        changed = False

        def chmod_after_file_fsync(fd: int) -> None:
            nonlocal changed
            real_fsync(fd)
            if not changed and stat.S_ISREG(os.fstat(fd).st_mode):
                changed = True
                target.chmod(0o600)

        with mock.patch.object(publication.os, "fsync", side_effect=chmod_after_file_fsync):
            with self.assertRaisesRegex(publication.OutputPublicationError, "target changed"):
                publication.atomic_write_text("result.txt", "new", self.repository)
        self.assertEqual(stat.S_IMODE(target.stat().st_mode), 0o600)
        self.assertEqual(target.read_text(encoding="utf-8"), "original")

    def test_rejects_existing_target_with_special_permission_bits(self) -> None:
        target = self.repository / "result.txt"
        target.write_text("original", encoding="utf-8")
        target.chmod(0o4755)
        with self.assertRaisesRegex(publication.OutputPublicationError, "special permission bits"):
            publication.atomic_write_text("result.txt", "new", self.repository)
        self.assertEqual(target.read_text(encoding="utf-8"), "original")

    def test_unsupported_directory_fsync_is_tolerated_after_commit(self) -> None:
        real_fsync = os.fsync

        def unsupported_for_directory(fd: int) -> None:
            if stat.S_ISDIR(os.fstat(fd).st_mode):
                raise OSError(errno.EINVAL, "unsupported")
            real_fsync(fd)

        with mock.patch.object(publication.os, "fsync", side_effect=unsupported_for_directory):
            target = publication.atomic_write_text("result.txt", "complete", self.repository)
        self.assertEqual(target.read_text(encoding="utf-8"), "complete")

    def test_postcommit_directory_fsync_error_is_explicit(self) -> None:
        real_fsync = os.fsync

        def fail_directory(fd: int) -> None:
            if stat.S_ISDIR(os.fstat(fd).st_mode):
                raise OSError(errno.EIO, "injected durability failure")
            real_fsync(fd)

        with mock.patch.object(publication.os, "fsync", side_effect=fail_directory):
            with self.assertRaisesRegex(publication.OutputPublicationError, "committed-but-durability-failed"):
                publication.atomic_write_text("result.txt", "complete", self.repository)
        self.assertEqual((self.repository / "result.txt").read_text(encoding="utf-8"), "complete")

    def test_new_directory_entry_is_synced_before_file_and_commit(self) -> None:
        calls: list[str] = []
        real_fsync = os.fsync

        def record_fsync(fd: int) -> None:
            calls.append("directory" if stat.S_ISDIR(os.fstat(fd).st_mode) else "file")
            real_fsync(fd)

        with mock.patch.object(publication.os, "fsync", side_effect=record_fsync):
            publication.atomic_write_text("nested/result.txt", "complete", self.repository)
        self.assertEqual(calls, ["directory", "file", "directory"])

    def test_unsupported_platform_fails_before_filesystem_changes(self) -> None:
        with mock.patch.object(publication.os, "name", "nt"):
            with self.assertRaisesRegex(publication.OutputPublicationError, "requires POSIX"):
                publication.atomic_write_text("result.txt", "unsafe", self.repository)
        self.assertEqual(list(self.repository.iterdir()), [])


if __name__ == "__main__":
    unittest.main()
