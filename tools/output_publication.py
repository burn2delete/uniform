#!/usr/bin/env python3
"""Private, development-only atomic publication for tool outputs.

Paths are logical repository-relative names.  An explicit output root (or
``GRAVITY_OUTPUT_ROOT``) changes only the physical root, never the layout.
This helper carries no release, authority, or provenance meaning.

The implementation is deliberately POSIX-only. It requires directory file
descriptors, no-follow opens, and dirfd-relative atomic replacement; unsupported
platforms fail closed instead of receiving a weaker path-based implementation.
"""

from __future__ import annotations

import errno
import inspect
import json
import os
from pathlib import Path
import stat
import uuid
from typing import Any, Union


ROOT = Path(__file__).resolve().parents[1]
PathLike = Union[str, os.PathLike[str]]
try:
    _REPLACE_PARAMETERS = inspect.signature(os.replace).parameters
    _REPLACE_HAS_DIRFDS = "src_dir_fd" in _REPLACE_PARAMETERS and "dst_dir_fd" in _REPLACE_PARAMETERS
except (TypeError, ValueError):  # pragma: no cover - platform capability probe
    _REPLACE_HAS_DIRFDS = False


class OutputPublicationError(OSError, ValueError):
    """Raised when a publication path or filesystem object is unsafe."""


def _require_posix() -> None:
    required_dir_fd = (os.open, os.mkdir, os.stat, os.unlink)
    supported = (
        os.name == "posix"
        and bool(getattr(os, "O_NOFOLLOW", 0))
        and bool(getattr(os, "O_DIRECTORY", 0))
        and all(function in os.supports_dir_fd for function in required_dir_fd)
        and os.stat in os.supports_follow_symlinks
        and _REPLACE_HAS_DIRFDS
        and hasattr(os, "fchmod")
        and hasattr(os, "getuid")
    )
    if not supported:
        raise OutputPublicationError("secure output publication requires POSIX dirfd and no-follow support")


def _string_path(value: PathLike, *, label: str) -> str:
    try:
        raw = os.fspath(value)
    except TypeError as exc:
        raise OutputPublicationError(f"{label} must be a path") from exc
    if not isinstance(raw, str) or not raw:
        raise OutputPublicationError(f"{label} must be a non-empty text path")
    if "\x00" in raw:
        raise OutputPublicationError(f"{label} contains NUL")
    return raw


def _canonical_parts(raw: str, *, label: str) -> tuple[str, ...]:
    """Validate lexical spelling and return non-special path components."""
    if "\\" in raw or "//" in raw or raw.endswith(os.sep):
        raise OutputPublicationError(f"{label} is not canonically spelled")
    lexical = raw.split(os.sep)
    if any(part in (".", "..") for part in lexical):
        raise OutputPublicationError(f"{label} contains traversal or dot components")
    path = Path(raw)
    parts = path.parts
    if not parts or parts == (os.sep,):
        raise OutputPublicationError(f"{label} must name a file")
    special = {".", ".."}
    body = parts[1:] if path.is_absolute() else parts
    if not body or any(part in special for part in body):
        raise OutputPublicationError(f"{label} contains traversal or dot components")
    return tuple(body)


def _logical_parts(logical_path: PathLike, repository_root: Path) -> tuple[str, ...]:
    raw = _string_path(logical_path, label="output path")
    _canonical_parts(raw, label="output path")
    path = Path(raw)
    if path.is_absolute():
        # Do not resolve: lexical containment is required, and symlink parents
        # are rejected later while traversing the selected root.
        try:
            relative = path.relative_to(repository_root)
        except ValueError as exc:
            raise OutputPublicationError("absolute output path is outside repository root") from exc
        raw_relative = str(relative)
        if not raw_relative or raw_relative == ".":
            raise OutputPublicationError("output path must name a file")
        return _canonical_parts(raw_relative, label="output path")
    return _canonical_parts(raw, label="output path")


def _root_path(output_root: PathLike | None, repository_root: Path) -> Path:
    chosen: PathLike | None = output_root
    if chosen is None:
        chosen = os.environ.get("GRAVITY_OUTPUT_ROOT") or None
    if chosen is None:
        return repository_root
    raw = _string_path(chosen, label="output root")
    parts = _canonical_parts(raw, label="output root")
    root = Path(raw)
    if not root.is_absolute():
        root = repository_root.joinpath(*parts)
    elif root == Path("/tmp") or Path("/tmp") in root.parents:
        # macOS exposes /tmp as a root-owned symlink to trusted sticky
        # /private/tmp. Canonicalize only this conventional system alias;
        # arbitrary selected-root symlinks remain forbidden below.
        try:
            canonical_tmp = Path(os.path.realpath("/tmp"))
            info = canonical_tmp.stat()
        except OSError as exc:
            raise OutputPublicationError("cannot validate the system temporary directory") from exc
        if (
            not stat.S_ISDIR(info.st_mode)
            or info.st_uid != 0
            or not (info.st_mode & stat.S_ISVTX)
            or not (info.st_mode & stat.S_IWOTH)
        ):
            raise OutputPublicationError(
                "the system temporary directory is not trusted sticky storage"
            )
        root = canonical_tmp / root.relative_to("/tmp")
    # This remains lexical. The dirfd walk rejects every symlink component,
    # including an explicitly selected root which is itself a symlink.
    return root


def _resolve_output_path(
    logical_path: PathLike,
    *,
    repository_root: PathLike = ROOT,
    output_root: PathLike | None = None,
) -> Path:
    """Return the physical destination without creating anything."""
    repo_raw = _string_path(repository_root, label="repository root")
    repo = Path(repo_raw)
    if not repo.is_absolute():
        repo = Path.cwd() / repo
    try:
        repo = repo.resolve(strict=True)
    except OSError as exc:
        raise OutputPublicationError("cannot resolve repository root") from exc
    if not repo.is_dir():
        raise OutputPublicationError("repository root is not a directory")
    parts = _logical_parts(logical_path, repo)
    selected = _root_path(output_root, repo)
    destination = selected.joinpath(*parts)
    # ``parts`` contains no traversal and selected is lexical; this check
    # protects future callers from accidentally changing either invariant.
    try:
        destination.relative_to(selected)
    except ValueError as exc:  # pragma: no cover - defensive invariant
        raise OutputPublicationError("output destination escapes selected root") from exc
    return destination


def _directory_flags() -> int:
    return os.O_RDONLY | os.O_DIRECTORY | getattr(os, "O_CLOEXEC", 0) | os.O_NOFOLLOW


_UNSUPPORTED_DIRECTORY_FSYNC = {
    errno.EINVAL,
    getattr(errno, "ENOTSUP", errno.EINVAL),
    getattr(errno, "EOPNOTSUPP", errno.EINVAL),
}


def _fsync_directory(fd: int, *, postcommit: bool) -> None:
    try:
        os.fsync(fd)
    except OSError as exc:
        if exc.errno in _UNSUPPORTED_DIRECTORY_FSYNC:
            return
        if postcommit:
            raise OutputPublicationError(
                "output committed but durability failed (committed-but-durability-failed)"
            ) from exc
        raise OutputPublicationError("cannot persist newly created output directory") from exc


def _check_directory(info: os.stat_result, *, label: str) -> None:
    if not stat.S_ISDIR(info.st_mode):
        raise OutputPublicationError(f"{label} is not a directory")
    owner = getattr(os, "getuid", lambda: None)()
    if owner is not None and info.st_uid not in (0, owner):
        raise OutputPublicationError(f"{label} has an unexpected owner")
    writable = info.st_mode & (stat.S_IWGRP | stat.S_IWOTH)
    trusted_sticky = bool(info.st_mode & stat.S_ISVTX) and info.st_uid == 0
    if writable and not trusted_sticky:
        raise OutputPublicationError(f"{label} is group/world writable")


def _open_directory_chain(path: Path, *, create: bool, label: str) -> int:
    """Open/create a directory component by component without following links."""
    absolute = Path(os.path.abspath(path))
    fd = os.open(absolute.anchor or os.curdir, _directory_flags())
    try:
        for component in absolute.parts[1:] if absolute.is_absolute() else absolute.parts:
            try:
                next_fd = os.open(component, _directory_flags(), dir_fd=fd)
            except FileNotFoundError:
                if not create:
                    raise OutputPublicationError(f"{label} does not exist: {absolute}")
                created = False
                try:
                    os.mkdir(component, 0o700, dir_fd=fd)
                    created = True
                except FileExistsError:
                    pass
                if created:
                    _fsync_directory(fd, postcommit=False)
                try:
                    next_fd = os.open(component, _directory_flags(), dir_fd=fd)
                except OSError as exc:
                    raise OutputPublicationError(f"unsafe {label} component: {component}") from exc
            except OSError as exc:
                raise OutputPublicationError(f"unsafe {label} component: {component}") from exc
            os.close(fd)
            fd = next_fd
            _check_directory(os.fstat(fd), label=f"{label} component {component}")
        _check_directory(os.fstat(fd), label=label)
        return fd
    except BaseException:
        os.close(fd)
        raise


def _check_target(
    parent_fd: int, name: str, destination: Path, *, owner: int | None
) -> os.stat_result | None:
    try:
        info = os.stat(name, dir_fd=parent_fd, follow_symlinks=False)
    except FileNotFoundError:
        return None
    except OSError as exc:
        raise OutputPublicationError(f"cannot inspect output target {destination}") from exc
    if stat.S_ISLNK(info.st_mode):
        raise OutputPublicationError(f"output target is a symlink: {destination}")
    if not stat.S_ISREG(info.st_mode):
        raise OutputPublicationError(f"output target is not a regular file: {destination}")
    if info.st_nlink != 1:
        raise OutputPublicationError(f"output target is hard-linked: {destination}")
    if owner is not None and info.st_uid != owner:
        raise OutputPublicationError(f"output target has an unexpected owner: {destination}")
    if info.st_mode & (stat.S_ISUID | stat.S_ISGID | stat.S_ISVTX):
        raise OutputPublicationError(f"output target has special permission bits: {destination}")
    return info


def _target_snapshot(info: os.stat_result | None) -> tuple[int, ...] | None:
    if info is None:
        return None
    return (
        info.st_dev,
        info.st_ino,
        stat.S_IFMT(info.st_mode),
        info.st_uid,
        info.st_nlink,
        stat.S_IMODE(info.st_mode),
        info.st_mode & (stat.S_ISUID | stat.S_ISGID | stat.S_ISVTX),
    )


def _parent_identity(fd: int) -> tuple[int, int]:
    info = os.fstat(fd)
    return info.st_dev, info.st_ino


def _verify_parent_path(path: Path, expected: tuple[int, int], *, postcommit: bool) -> None:
    try:
        current_fd = _open_directory_chain(path, create=False, label="output parent")
    except OutputPublicationError as exc:
        if postcommit:
            raise OutputPublicationError(
                "output commit location is uncertain: parent changed after replacement"
            ) from exc
        raise OutputPublicationError("output parent changed before replacement") from exc
    try:
        changed = _parent_identity(current_fd) != expected
    finally:
        os.close(current_fd)
    if changed:
        if postcommit:
            raise OutputPublicationError(
                "output commit location is uncertain: parent changed after replacement"
            )
        raise OutputPublicationError("output parent changed before replacement")


def _write_bytes(destination: Path, data: bytes, *, mode: int | None) -> Path:
    parent_fd = _open_directory_chain(destination.parent, create=True, label="output parent")
    parent_identity = _parent_identity(parent_fd)
    temporary_name = f".{destination.name}.{os.getpid()}.{uuid.uuid4().hex}.tmp"
    temporary_fd = -1
    owner = getattr(os, "getuid", lambda: None)()
    try:
        target = _check_target(parent_fd, destination.name, destination, owner=owner)
        target_snapshot = _target_snapshot(target)
        final_mode = mode if mode is not None else stat.S_IMODE(target.st_mode) if target else 0o644
        flags = os.O_WRONLY | os.O_CREAT | os.O_EXCL | getattr(os, "O_CLOEXEC", 0) | getattr(os, "O_NOFOLLOW", 0)
        try:
            temporary_fd = os.open(temporary_name, flags, 0o600, dir_fd=parent_fd)
        except OSError as exc:
            raise OutputPublicationError(f"cannot create temporary output for {destination}") from exc
        info = os.fstat(temporary_fd)
        if not stat.S_ISREG(info.st_mode) or info.st_nlink != 1 or (owner is not None and info.st_uid != owner):
            raise OutputPublicationError(f"temporary output is unsafe: {destination}")
        with os.fdopen(temporary_fd, "wb", closefd=True) as stream:
            temporary_fd = -1
            stream.write(data)
            stream.flush()
            os.fchmod(stream.fileno(), final_mode)
            os.fsync(stream.fileno())
        current_target = _check_target(parent_fd, destination.name, destination, owner=owner)
        if _target_snapshot(current_target) != target_snapshot:
            raise OutputPublicationError("output target changed before replacement")
        _verify_parent_path(destination.parent, parent_identity, postcommit=False)
        os.replace(temporary_name, destination.name, src_dir_fd=parent_fd, dst_dir_fd=parent_fd)
        temporary_name = ""
        _verify_parent_path(destination.parent, parent_identity, postcommit=True)
        _fsync_directory(parent_fd, postcommit=True)
        _verify_parent_path(destination.parent, parent_identity, postcommit=True)
        return destination
    finally:
        if temporary_fd != -1:
            os.close(temporary_fd)
        if temporary_name:
            try:
                os.unlink(temporary_name, dir_fd=parent_fd)
            except FileNotFoundError:
                pass
            except OSError:
                pass
        os.close(parent_fd)


def _mode(mode: int | None) -> int | None:
    if mode is None:
        return None
    if isinstance(mode, bool) or not isinstance(mode, int) or mode < 0 or mode & ~0o777:
        raise OutputPublicationError("mode must be an integer permission mask")
    return mode


def atomic_write_text(
    logical_path: PathLike,
    text: str,
    repository_root: PathLike = ROOT,
    output_root: PathLike | None = None,
    mode: int | None = None,
) -> Path:
    """Atomically publish UTF-8 text using the POSIX-only safe protocol."""
    _require_posix()
    if not isinstance(text, str):
        raise TypeError("text output must be str")
    destination = _resolve_output_path(logical_path, repository_root=repository_root, output_root=output_root)
    return _write_bytes(destination, text.encode("utf-8"), mode=_mode(mode))


def atomic_write_json(
    logical_path: PathLike,
    payload: Any,
    repository_root: PathLike = ROOT,
    output_root: PathLike | None = None,
    mode: int | None = None,
) -> Path:
    """Atomically publish deterministic, strict JSON with a trailing newline."""
    _require_posix()
    encoded = json.dumps(payload, allow_nan=False, ensure_ascii=True, indent=2, sort_keys=True) + "\n"
    return atomic_write_text(logical_path, encoded, repository_root, output_root, mode)


__all__ = ["OutputPublicationError", "atomic_write_text", "atomic_write_json"]
