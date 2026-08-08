#!/usr/bin/env python3
"""Stable-inode shared heavy-lock protocol for development tooling."""

from __future__ import annotations

import contextlib
import fcntl
import os
from pathlib import Path
import stat


SHARED_LOCK_PROTOCOL = "gravity-sh07-heavy-flock-owned-0600-v1"
SHARED_LOCK_MODE = 0o600


class CheckpointError(RuntimeError):
    pass


class SharedLockUnavailable(CheckpointError):
    pass


class SharedLockValidationError(CheckpointError):
    pass


class SharedLockFile:
    def __init__(
        self,
        path: Path,
        descriptor: int,
        parent_descriptor: int,
        parent_device: int,
        parent_inode: int,
    ) -> None:
        self.path = path
        self.descriptor = descriptor
        self.parent_descriptor = parent_descriptor
        self.parent_device = parent_device
        self.parent_inode = parent_inode

    def validate(self, *, allow_legacy_mode: bool = False) -> None:
        try:
            opened = os.fstat(self.descriptor)
            named = os.stat(
                self.path.name,
                dir_fd=self.parent_descriptor,
                follow_symlinks=False,
            )
            parent = os.fstat(self.parent_descriptor)
        except OSError as error:
            raise CheckpointError(
                f"shared heavy lock pathname changed: {self.path}"
            ) from error
        allowed_modes = (
            {SHARED_LOCK_MODE, 0o644}
            if allow_legacy_mode
            else {SHARED_LOCK_MODE}
        )
        if (
            not stat.S_ISREG(opened.st_mode)
            or not stat.S_ISREG(named.st_mode)
            or opened.st_uid != os.geteuid()
            or named.st_uid != os.geteuid()
            or opened.st_nlink != 1
            or named.st_nlink != 1
            or stat.S_IMODE(opened.st_mode) not in allowed_modes
            or stat.S_IMODE(named.st_mode) not in allowed_modes
            or (opened.st_dev, opened.st_ino) != (named.st_dev, named.st_ino)
            or (parent.st_dev, parent.st_ino)
            != (self.parent_device, self.parent_inode)
        ):
            raise CheckpointError(
                "shared heavy lock must be one stable owned 0600 regular file"
            )

    def close(self) -> None:
        os.close(self.descriptor)
        os.close(self.parent_descriptor)

    def migrate_legacy_mode_after_exclusive_lock(self) -> bool:
        """Promote an owned legacy 0644 inode only while this fd holds flock."""
        self.validate(allow_legacy_mode=True)
        metadata = os.fstat(self.descriptor)
        if stat.S_IMODE(metadata.st_mode) == SHARED_LOCK_MODE:
            return False
        os.fchmod(self.descriptor, SHARED_LOCK_MODE)
        self.validate()
        return True


def canonical_shared_lock_path(lock_path: Path) -> Path:
    """Return a direct child of verified canonical /private/tmp."""
    absolute = Path(os.path.abspath(lock_path.expanduser()))
    if absolute.parent == Path("/tmp") and Path("/tmp").is_symlink():
        if Path("/tmp").resolve() != Path("/private/tmp"):
            raise CheckpointError(
                "system /tmp alias does not resolve exactly to /private/tmp"
            )
        absolute = Path("/private/tmp") / absolute.name
    if absolute.parent != Path("/private/tmp") or absolute.name in {"", ".", ".."}:
        raise CheckpointError(
            "shared heavy lock must be a direct child of /private/tmp"
        )
    return absolute


def open_lock_file(lock_path: Path, *, create: bool = True) -> SharedLockFile:
    if not hasattr(os, "O_NOFOLLOW"):
        raise CheckpointError(
            "platform cannot open the shared lock without following symlinks"
        )
    absolute = canonical_shared_lock_path(lock_path)
    parent_descriptor = -1
    descriptor = -1
    try:
        parent_descriptor = os.open(
            absolute.parent,
            os.O_RDONLY | getattr(os, "O_DIRECTORY", 0) | os.O_NOFOLLOW,
        )
        parent = os.fstat(parent_descriptor)
        if not stat.S_ISDIR(parent.st_mode):
            raise CheckpointError("shared heavy lock parent must be a directory")
        flags = os.O_RDWR | os.O_NOFOLLOW | getattr(os, "O_CLOEXEC", 0)
        if create:
            flags |= os.O_CREAT
        descriptor = os.open(
            absolute.name,
            flags,
            SHARED_LOCK_MODE,
            dir_fd=parent_descriptor,
        )
    except OSError as error:
        if parent_descriptor != -1:
            os.close(parent_descriptor)
        raise CheckpointError(
            f"shared heavy lock cannot be opened safely: {absolute}"
        ) from error
    try:
        handle = SharedLockFile(
            absolute,
            descriptor,
            parent_descriptor,
            parent.st_dev,
            parent.st_ino,
        )
        handle.validate(allow_legacy_mode=True)
        return handle
    except BaseException:
        os.close(descriptor)
        os.close(parent_descriptor)
        raise


@contextlib.contextmanager
def shared_lock_lease(lock_path: Path):
    try:
        handle = open_lock_file(lock_path)
    except CheckpointError as error:
        raise SharedLockValidationError(str(error)) from error
    try:
        try:
            fcntl.flock(handle.descriptor, fcntl.LOCK_EX | fcntl.LOCK_NB)
        except BlockingIOError as error:
            raise SharedLockUnavailable(
                f"shared heavy lock is unavailable: {handle.path}"
            ) from error
        try:
            migrated = handle.migrate_legacy_mode_after_exclusive_lock()
        except CheckpointError as error:
            raise SharedLockValidationError(str(error)) from error
        try:
            try:
                yield handle, {
                    "lock_protocol": SHARED_LOCK_PROTOCOL,
                    "lock_path": str(handle.path),
                    "lock_mode": "0600",
                    "lock_mode_migrated": migrated,
                }
            except BaseException as body_error:
                try:
                    handle.validate()
                except CheckpointError as validation_error:
                    raise SharedLockValidationError(
                        str(validation_error)
                    ) from body_error
                raise
            else:
                try:
                    handle.validate()
                except CheckpointError as error:
                    raise SharedLockValidationError(str(error)) from error
        finally:
            fcntl.flock(handle.descriptor, fcntl.LOCK_UN)
    finally:
        handle.close()
