#!/usr/bin/env python3
"""Validate the reporting-only artifact census for the tracked artifact roots.

The census is intentionally not an evidence admission or release authority
mechanism.  It compares the reviewed contract with the Git index and with the
current worktree bytes, assigning every tracked path under ``docs/artifacts/``
and ``target/`` to one fixed class.  Unknown paths remain an explicit,
blocking ``unclassified-artifacts`` class and the contract must remain
``status: incomplete`` and ``authority: none``.

The worktree reader is a read-only dirfd walk.  Parent components and the leaf
are opened with no-follow flags, regular files must be owned by the current
user and must not be hardlinks, and Git blob identities are hashed while
streaming.  No complete file byte string is accumulated in memory.
"""

from __future__ import annotations

import argparse
from dataclasses import dataclass
import hashlib
import json
import os
from pathlib import Path, PurePosixPath
import re
import selectors
import stat
import subprocess
import sys
import time
from typing import Any, Mapping, Sequence


ROOT = Path(__file__).resolve().parents[1]
DEFAULT_CONTRACT = ROOT / "contracts" / "artifact-census.json"
SCHEMA_VERSION = 1

# The limits are deliberately finite.  They are high enough for the current
# repository (2,254 entries and a roughly 29 MiB scope) while preventing a
# malformed index or contract from turning this read-only check into an
# unbounded memory/CPU operation.
MAX_CONTRACT_BYTES = 4 * 1024 * 1024
MAX_JSON_NODES = 250_000
MAX_JSON_DEPTH = 128
MAX_INDEX_ENTRIES = 100_000
MAX_INDEX_OUTPUT_BYTES = 64 * 1024 * 1024
MAX_INDEX_RECORD_BYTES = 16 * 1024
MAX_PATH_BYTES = 4096
MAX_BLOB_BYTES = 512 * 1024 * 1024
MAX_TOTAL_BLOB_BYTES = 8 * 1024 * 1024 * 1024
MAX_GIT_OUTPUT_BYTES = 64 * 1024 * 1024
READ_CHUNK_BYTES = 1024 * 1024

SCOPED_ROOTS = ("docs/artifacts/", "target/")
GENERATED_FULL_LANGUAGE_PATHS = frozenset(
    {
        "docs/artifacts/full-language/coverage/full-language-coverage-gaps.json",
        "docs/artifacts/full-language/coverage/full-language-coverage-matrix.json",
        "docs/artifacts/full-language/reports/full-language-coverage-matrix-report.md",
    }
)
CLASS_ORDER = (
    "generated-full-language-reporting",
    "reviewed-phase18-artifacts",
    "reviewed-phase-fixtures",
    "generated-validation-candidates",
    "generated-phase18-candidates",
    "unclassified-artifacts",
)
CLASS_DISPOSITIONS = {
    "generated-full-language-reporting": "generated-nonauthoritative",
    "reviewed-phase18-artifacts": "reviewed-record-or-attestation",
    "reviewed-phase-fixtures": "reviewed-fixture",
    "generated-validation-candidates": "generated-ephemeral-candidate",
    "generated-phase18-candidates": "generated-release-candidate",
    "unclassified-artifacts": "unclassified-blocking",
}
# These descriptions are part of the contract, rather than free-form prose.
# The phase-18 prefix takes precedence over the generic phase fixture prefix;
# that precedence is also pinned in the final rule string.
CLASS_PATH_RULES = {
    "generated-full-language-reporting": (
        "exact:{docs/artifacts/full-language/coverage/full-language-coverage-gaps.json,"
        "docs/artifacts/full-language/coverage/full-language-coverage-matrix.json,"
        "docs/artifacts/full-language/reports/full-language-coverage-matrix-report.md}"
    ),
    "reviewed-phase18-artifacts": "prefix:docs/artifacts/phase-18/** (before reviewed-phase-fixtures)",
    "reviewed-phase-fixtures": "prefix:docs/artifacts/phase-{00..17}/fixtures/** (phase-18 precedence)",
    "generated-validation-candidates": "prefix:target/validation/**",
    "generated-phase18-candidates": "prefix:target/phase-18/**",
    "unclassified-artifacts": "fallback:docs/artifacts/** or target/** after all known rules",
}
PHASE_FIXTURE_RE = re.compile(r"^docs/artifacts/phase-(?:0[0-9]|1[0-7])/fixtures/")
HEX_RE = re.compile(r"^[0-9a-f]+$")
SHA256_RE = re.compile(r"^sha256:[0-9a-f]{64}$")
OBJECT_FORMATS = {"sha1": 40, "sha256": 64}

TOP_FIELDS = {
    "schema_version",
    "contract_id",
    "kind",
    "status",
    "authority",
    "description",
    "scope",
    "identity",
    "entries",
    "classes",
    "totals",
    "nonclaims",
    "diagnostics",
}
SCOPE_FIELDS = {"roots", "source", "worktree_byte_check", "unknown_path_policy"}
IDENTITY_FIELDS = {"algorithm", "record_fields", "ordering", "encoding", "git_object_format"}
ENTRY_FIELDS = {"path", "mode", "blob_oid", "byte_count"}
CLASS_FIELDS = {"id", "disposition", "path_rule", "file_count", "byte_count", "record_sha256"}
TOTAL_FIELDS = {"file_count", "byte_count", "record_sha256", "unclassified_file_count", "unclassified_byte_count"}


class DuplicateKeyError(ValueError):
    """Raised when a strict JSON object repeats a key."""


@dataclass(frozen=True, order=True)
class IndexedFile:
    path: str
    mode: str
    oid: str
    size: int


def _object_no_duplicates(pairs: list[tuple[str, Any]]) -> dict[str, Any]:
    result: dict[str, Any] = {}
    for key, value in pairs:
        if key in result:
            raise DuplicateKeyError(f"duplicate JSON key {key!r}")
        result[key] = value
    return result


def _parse_json_int(token: str) -> int:
    # Counts and sizes are intentionally bounded below; rejecting giant JSON
    # integers here also avoids relying on interpreter-specific digit limits.
    value = int(token)
    if abs(value) > MAX_TOTAL_BLOB_BYTES:
        raise ValueError(f"JSON integer exceeds {MAX_TOTAL_BLOB_BYTES}")
    return value


def _reject_json_constant(value: str) -> None:
    raise ValueError(f"non-finite JSON number {value!r}")


def _bounded_json(value: Any) -> None:
    count = 0
    stack: list[tuple[Any, int]] = [(value, 1)]
    while stack:
        item, depth = stack.pop()
        count += 1
        if count > MAX_JSON_NODES:
            raise ValueError(f"JSON node count exceeds {MAX_JSON_NODES}")
        if depth > MAX_JSON_DEPTH:
            raise ValueError(f"JSON depth exceeds {MAX_JSON_DEPTH}")
        if isinstance(item, Mapping):
            stack.extend((child, depth + 1) for child in item.values())
        elif isinstance(item, list):
            stack.extend((child, depth + 1) for child in item)


def _read_bounded_file(path: Path, max_bytes: int) -> bytes:
    """Read a current-owner regular file through a bounded no-follow walk."""
    if not hasattr(os, "O_NOFOLLOW") or not hasattr(os, "O_DIRECTORY"):
        raise OSError("platform lacks required no-follow directory flags")
    absolute = Path(os.path.abspath(path))
    parts = absolute.parts
    if len(parts) < 2 or parts[0] != os.path.sep:
        raise OSError("path must be an absolute POSIX path")
    uid = getattr(os, "getuid", lambda: None)()
    directory_fd = os.open(
        os.path.sep,
        os.O_RDONLY | os.O_DIRECTORY | os.O_NOFOLLOW | getattr(os, "O_CLOEXEC", 0),
    )
    current_fd = directory_fd
    try:
        for component in parts[1:-1]:
            next_fd = os.open(
                component,
                os.O_RDONLY | os.O_DIRECTORY | os.O_NOFOLLOW | getattr(os, "O_CLOEXEC", 0),
                dir_fd=current_fd,
            )
            if current_fd != directory_fd:
                os.close(current_fd)
            current_fd = next_fd
            parent_info = os.fstat(current_fd)
            if not stat.S_ISDIR(parent_info.st_mode):
                raise OSError(f"parent component {component!r} is not a directory")
        descriptor = os.open(
            parts[-1],
            os.O_RDONLY | os.O_NOFOLLOW | getattr(os, "O_NONBLOCK", 0) | getattr(os, "O_CLOEXEC", 0),
            dir_fd=current_fd,
        )
        try:
            info = os.fstat(descriptor)
            if not stat.S_ISREG(info.st_mode):
                raise OSError("file is not regular")
            if uid is not None and info.st_uid != uid:
                raise OSError("file is not owned by the current user")
            if info.st_nlink != 1:
                raise OSError("file hardlinks are not admitted")
            if info.st_size < 0 or info.st_size > max_bytes:
                raise ValueError(f"file exceeds {max_bytes} bytes")
            output = bytearray()
            while True:
                if len(output) > max_bytes:
                    raise ValueError(f"file exceeds {max_bytes} bytes")
                chunk = os.read(descriptor, min(READ_CHUNK_BYTES, max_bytes + 1 - len(output)))
                if not chunk:
                    break
                output.extend(chunk)
            after = os.fstat(descriptor)
            if (after.st_dev, after.st_ino, after.st_mode, after.st_uid, after.st_nlink, after.st_size, after.st_mtime_ns, after.st_ctime_ns) != (
                info.st_dev,
                info.st_ino,
                info.st_mode,
                info.st_uid,
                info.st_nlink,
                info.st_size,
                info.st_mtime_ns,
                info.st_ctime_ns,
            ):
                raise OSError("file metadata changed while reading")
            return bytes(output)
        finally:
            os.close(descriptor)
    finally:
        if current_fd != directory_fd:
            os.close(current_fd)
        os.close(directory_fd)


def load_json(path: Path) -> dict[str, Any]:
    data = _read_bounded_file(path, MAX_CONTRACT_BYTES)
    value = json.loads(
        data.decode("utf-8"),
        object_pairs_hook=_object_no_duplicates,
        parse_int=_parse_json_int,
        parse_constant=_reject_json_constant,
    )
    _bounded_json(value)
    if not isinstance(value, dict):
        raise ValueError(f"{path}: top level must be an object")
    return value


def _exact_fields(value: Any, expected: set[str], location: str, errors: list[str]) -> bool:
    if not isinstance(value, Mapping):
        errors.append(f"{location}: must be an object")
        return False
    missing = sorted(expected.difference(value))
    extra = sorted(set(value).difference(expected))
    if missing:
        errors.append(f"{location}: missing fields: {', '.join(missing)}")
    if extra:
        errors.append(f"{location}: unknown fields: {', '.join(extra)}")
    return not missing and not extra


def _canonical_path(path: str) -> bool:
    if not isinstance(path, str) or not path or "\\" in path or "\x00" in path:
        return False
    try:
        if len(path.encode("utf-8")) > MAX_PATH_BYTES:
            return False
    except UnicodeEncodeError:
        return False
    if any(ord(character) < 0x20 or ord(character) == 0x7F for character in path):
        return False
    value = PurePosixPath(path)
    return (
        not value.is_absolute()
        and str(value) == path
        and all(part not in ("", ".", "..") for part in value.parts)
    )


def _in_scope(path: str) -> bool:
    return _canonical_path(path) and any(path.startswith(root) for root in SCOPED_ROOTS)


def classify_path(path: str) -> str:
    """Return exactly one fixed class for a canonical in-scope path."""
    if not _in_scope(path):
        raise ValueError(f"path is outside artifact census roots: {path!r}")
    if path in GENERATED_FULL_LANGUAGE_PATHS:
        return "generated-full-language-reporting"
    if path.startswith("docs/artifacts/phase-18/"):
        return "reviewed-phase18-artifacts"
    if PHASE_FIXTURE_RE.match(path):
        return "reviewed-phase-fixtures"
    if path.startswith("target/validation/"):
        return "generated-validation-candidates"
    if path.startswith("target/phase-18/"):
        return "generated-phase18-candidates"
    return "unclassified-artifacts"


def _kill_and_reap(process: subprocess.Popen[bytes], *, timeout_seconds: float = 5.0) -> None:
    """Best-effort bounded cleanup for one Git child process."""
    try:
        if process.poll() is None:
            process.kill()
    except OSError:
        pass
    try:
        process.wait(timeout=timeout_seconds)
    except (OSError, subprocess.TimeoutExpired):
        pass


def _run_git(
    root: Path,
    args: Sequence[str],
    *,
    input_bytes: bytes | None = None,
    max_output_bytes: int = MAX_GIT_OUTPUT_BYTES,
    _timeout_seconds: float = 30.0,
) -> bytes:
    if not isinstance(_timeout_seconds, (int, float)) or isinstance(_timeout_seconds, bool):
        raise ValueError("Git timeout must be numeric")
    if _timeout_seconds <= 0 or not float(_timeout_seconds) < float("inf"):
        raise ValueError("Git timeout must be finite and positive")
    selector: selectors.BaseSelector | None = None
    input_stream = None
    input_offset = 0
    stdout_parts: list[bytes] = []
    stderr_parts: list[bytes] = []
    stdout_size = 0
    stderr_size = 0
    process = subprocess.Popen(
        ["git", *args],
        cwd=root,
        stdin=subprocess.PIPE if input_bytes is not None else subprocess.DEVNULL,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        close_fds=True,
    )
    try:
        assert process.stdout is not None and process.stderr is not None
        input_stream = process.stdin if input_bytes is not None else None
        selector = selectors.DefaultSelector()
        selector.register(process.stdout, selectors.EVENT_READ, "stdout")
        selector.register(process.stderr, selectors.EVENT_READ, "stderr")
        if input_stream is not None:
            if input_bytes:
                selector.register(input_stream, selectors.EVENT_WRITE, "stdin")
            else:
                input_stream.close()
                input_stream = None
        for stream in (process.stdout, process.stderr, input_stream):
            if stream is not None:
                os.set_blocking(stream.fileno(), False)
        deadline = time.monotonic() + float(_timeout_seconds)
        while selector.get_map():
            remaining = deadline - time.monotonic()
            if remaining <= 0:
                _kill_and_reap(process)
                raise RuntimeError(f"git {' '.join(args)} timed out")
            if process.poll() is not None and input_stream is not None:
                try:
                    selector.unregister(input_stream)
                except KeyError:
                    pass
                input_stream.close()
                input_stream = None
            events = selector.select(timeout=min(remaining, 1.0))
            for key, _ in events:
                stream = key.fileobj
                if key.data == "stdin":
                    assert input_stream is not None and input_bytes is not None
                    assert process.stdin is not None
                    try:
                        written = os.write(
                            process.stdin.fileno(),
                            input_bytes[input_offset : input_offset + 64 * 1024],
                        )
                    except BlockingIOError:
                        continue
                    except BrokenPipeError:
                        selector.unregister(stream)
                        stream.close()
                        input_stream = None
                        continue
                    input_offset += written
                    if input_offset >= len(input_bytes) or written == 0:
                        selector.unregister(stream)
                        stream.close()
                        input_stream = None
                    continue
                chunk = os.read(stream.fileno(), 64 * 1024)
                if not chunk:
                    selector.unregister(stream)
                    stream.close()
                    continue
                if key.data == "stdout":
                    stdout_size += len(chunk)
                    if stdout_size > max_output_bytes:
                        _kill_and_reap(process)
                        raise ValueError(f"git {' '.join(args)} output exceeds {max_output_bytes} bytes")
                    stdout_parts.append(chunk)
                else:
                    # Error text is diagnostic only and is bounded separately.
                    stderr_size += len(chunk)
                    if stderr_size > 1024 * 1024:
                        _kill_and_reap(process)
                        raise ValueError(f"git {' '.join(args)} stderr exceeds 1048576 bytes")
                    stderr_parts.append(chunk)
        return_code = process.wait(timeout=5)
    except BaseException:
        _kill_and_reap(process)
        raise
    finally:
        if selector is not None:
            try:
                selector.close()
            except OSError:
                pass
        for stream in (process.stdin, process.stdout, process.stderr):
            if stream is None:
                continue
            try:
                stream.close()
            except OSError:
                pass
    if return_code != 0:
        detail = b"".join(stderr_parts).decode("utf-8", "replace").strip()
        raise RuntimeError(f"git {' '.join(args)} failed: {detail}")
    return b"".join(stdout_parts)


def _object_format(root: Path) -> str:
    value = _run_git(root, ["rev-parse", "--show-object-format"], max_output_bytes=64).decode("ascii").strip()
    if value not in OBJECT_FORMATS:
        raise ValueError(f"unsupported Git object format {value!r}")
    return value


def _valid_oid(value: str, object_format: str) -> bool:
    length = OBJECT_FORMATS.get(object_format, 0)
    return bool(length and isinstance(value, str) and len(value) == length and HEX_RE.fullmatch(value))


def _git_blob_oid(data: bytes, algorithm: str) -> str:
    """Return a Git blob identity for an already-materialized test value."""
    digest = hashlib.new(algorithm)
    digest.update(f"blob {len(data)}\0".encode("ascii"))
    digest.update(data)
    return digest.hexdigest()


def _blob_sizes(root: Path, oids: Sequence[str], object_format: str) -> dict[str, int]:
    if object_format not in OBJECT_FORMATS:
        raise ValueError(f"unsupported Git object format {object_format!r}")
    unique = sorted(set(oids))
    if len(unique) > MAX_INDEX_ENTRIES:
        raise ValueError(f"Git object request exceeds {MAX_INDEX_ENTRIES} objects")
    for oid in unique:
        if not _valid_oid(oid, object_format):
            raise ValueError(f"invalid Git object id {oid!r}")
    request = b"".join(oid.encode("ascii") + b"\n" for oid in unique)
    output = _run_git(
        root,
        ["cat-file", "--batch-check=%(objectname) %(objecttype) %(objectsize)"],
        input_bytes=request,
        max_output_bytes=MAX_INDEX_OUTPUT_BYTES,
    )
    result: dict[str, int] = {}
    total = 0
    lines = output.splitlines()
    if len(lines) != len(unique):
        raise ValueError("Git object size response did not cover the exact index object set")
    for raw_line in lines:
        try:
            parts = raw_line.decode("ascii").split(" ")
        except UnicodeDecodeError as exc:
            raise ValueError(f"non-ASCII Git object response: {raw_line!r}") from exc
        if len(parts) != 3:
            raise ValueError(f"unexpected Git object record: {raw_line!r}")
        oid, object_type, size_text = parts
        if not _valid_oid(oid, object_format) or oid not in unique:
            raise ValueError(f"unexpected Git object id in response: {oid!r}")
        if object_type != "blob" or not size_text.isdigit():
            raise ValueError(f"Git index object {oid!r} is not a regular blob")
        size = int(size_text)
        if size > MAX_BLOB_BYTES:
            raise ValueError(f"Git blob {oid!r} exceeds {MAX_BLOB_BYTES} bytes")
        total += size
        if total > MAX_TOTAL_BLOB_BYTES:
            raise ValueError(f"Git blob total exceeds {MAX_TOTAL_BLOB_BYTES} bytes")
        if oid in result:
            raise ValueError(f"duplicate Git object response {oid!r}")
        result[oid] = size
    if set(result) != set(unique):
        raise ValueError("Git object size response did not cover the exact index object set")
    return result


def discover_index(root: Path, *, object_format: str | None = None) -> list[IndexedFile]:
    """Read stage-zero regular entries and their Git blob sizes from the index."""
    fmt = object_format or _object_format(root)
    raw = _run_git(
        root,
        ["ls-files", "--stage", "-z", "--", *SCOPED_ROOTS],
        max_output_bytes=MAX_INDEX_OUTPUT_BYTES,
    )
    if raw and not raw.endswith(b"\0"):
        raise ValueError("Git index listing is not NUL terminated")
    if len(raw) > MAX_INDEX_OUTPUT_BYTES:
        raise ValueError(f"Git index listing exceeds {MAX_INDEX_OUTPUT_BYTES} bytes")
    parsed: list[tuple[str, str, int, str]] = []
    seen_paths: set[str] = set()
    for record in raw.split(b"\0"):
        if not record:
            continue
        if len(record) > MAX_INDEX_RECORD_BYTES:
            raise ValueError(f"Git index record exceeds {MAX_INDEX_RECORD_BYTES} bytes")
        try:
            header, path_bytes = record.split(b"\t", 1)
            mode_bytes, oid_bytes, stage_bytes = header.split(b" ")
            path = path_bytes.decode("utf-8", "strict")
            mode = mode_bytes.decode("ascii", "strict")
            oid = oid_bytes.decode("ascii", "strict")
            stage = int(stage_bytes)
        except (UnicodeDecodeError, ValueError) as exc:
            raise ValueError(f"malformed or non-UTF-8 Git index record: {record!r}") from exc
        if len(parsed) >= MAX_INDEX_ENTRIES:
            raise ValueError(f"Git index entry count exceeds {MAX_INDEX_ENTRIES}")
        if not _canonical_path(path) or not any(path.startswith(root_name) for root_name in SCOPED_ROOTS):
            raise ValueError(f"Git index path is noncanonical or out of scope: {path!r}")
        if path in seen_paths:
            raise ValueError(f"duplicate Git index path {path!r}")
        seen_paths.add(path)
        if stage != 0:
            raise ValueError(f"Git index path {path!r} has nonzero merge stage {stage}")
        parsed.append((mode, oid, stage, path))
    sizes = _blob_sizes(root, [oid for _, oid, _, _ in parsed], fmt)
    entry_total = 0
    for _, oid, _, _ in parsed:
        entry_total += sizes[oid]
        if entry_total > MAX_TOTAL_BLOB_BYTES:
            raise ValueError(f"Git index entry blob total exceeds {MAX_TOTAL_BLOB_BYTES} bytes")
    entries = [IndexedFile(path=path, mode=mode, oid=oid, size=sizes[oid]) for mode, oid, _, path in parsed]
    return sorted(entries, key=lambda item: (item.path, item.mode, item.oid, item.size))


def _read_worktree_digest(root: Path, entry: IndexedFile, object_format: str) -> tuple[int, str, os.stat_result]:
    """Read one regular file through a no-follow dirfd walk and hash it."""
    if not hasattr(os, "O_NOFOLLOW") or not hasattr(os, "O_DIRECTORY"):
        raise OSError("platform lacks required no-follow directory flags")
    uid = getattr(os, "getuid", lambda: None)()
    root_fd = os.open(
        root,
        os.O_RDONLY | os.O_DIRECTORY | os.O_NOFOLLOW | getattr(os, "O_CLOEXEC", 0),
    )
    current_fd = root_fd
    try:
        root_info = os.fstat(current_fd)
        if not stat.S_ISDIR(root_info.st_mode):
            raise OSError("worktree root is not a directory")
        if uid is not None and root_info.st_uid != uid:
            raise OSError("worktree root is not owned by the current user")
        parts = PurePosixPath(entry.path).parts
        for component in parts[:-1]:
            next_fd = os.open(
                component,
                os.O_RDONLY | os.O_DIRECTORY | os.O_NOFOLLOW | getattr(os, "O_CLOEXEC", 0),
                dir_fd=current_fd,
            )
            if current_fd != root_fd:
                os.close(current_fd)
            current_fd = next_fd
            parent_info = os.fstat(current_fd)
            if not stat.S_ISDIR(parent_info.st_mode):
                raise OSError(f"parent component {component!r} is not a directory")
            if uid is not None and parent_info.st_uid != uid:
                raise OSError(f"parent component {component!r} is not owned by the current user")
        # O_NONBLOCK prevents a malicious FIFO from stalling this read-only
        # check before fstat can reject the non-regular object.
        descriptor = os.open(
            parts[-1],
            os.O_RDONLY
            | os.O_NOFOLLOW
            | getattr(os, "O_NONBLOCK", 0)
            | getattr(os, "O_CLOEXEC", 0),
            dir_fd=current_fd,
        )
        try:
            info = os.fstat(descriptor)
            if not stat.S_ISREG(info.st_mode):
                raise OSError("worktree object is not a regular file")
            if uid is not None and info.st_uid != uid:
                raise OSError("worktree file is not owned by the current user")
            if info.st_nlink != 1:
                raise OSError("worktree hardlinks are not admitted")
            if info.st_size < 0 or info.st_size > MAX_BLOB_BYTES:
                raise OSError(f"worktree file exceeds {MAX_BLOB_BYTES} bytes")
            executable = bool(info.st_mode & 0o111)
            if (entry.mode == "100755") != executable:
                raise OSError("worktree executable mode differs from the index")
            hasher = hashlib.new(object_format)
            hasher.update(f"blob {entry.size}\0".encode("ascii"))
            byte_count = 0
            while True:
                chunk = os.read(descriptor, READ_CHUNK_BYTES)
                if not chunk:
                    break
                byte_count += len(chunk)
                if byte_count > MAX_BLOB_BYTES:
                    raise OSError(f"worktree file exceeds {MAX_BLOB_BYTES} bytes while reading")
                hasher.update(chunk)
            after = os.fstat(descriptor)
            if (after.st_dev, after.st_ino, after.st_mode, after.st_uid, after.st_nlink, after.st_size, after.st_mtime_ns, after.st_ctime_ns) != (
                info.st_dev,
                info.st_ino,
                info.st_mode,
                info.st_uid,
                info.st_nlink,
                info.st_size,
                info.st_mtime_ns,
                info.st_ctime_ns,
            ):
                raise OSError("worktree file metadata changed while reading")
            return byte_count, hasher.hexdigest(), after
        finally:
            os.close(descriptor)
    finally:
        if current_fd != root_fd:
            os.close(current_fd)
        os.close(root_fd)


def _validate_entries_once(
    entries: Sequence[IndexedFile],
    *,
    root: Path | None,
    object_format: str = "sha1",
) -> list[str]:
    """Validate structure before doing any potentially large worktree reads."""
    errors: list[str] = []
    expected_oid_length = OBJECT_FORMATS.get(object_format, 0)
    if not expected_oid_length:
        return [f"unsupported Git object format {object_format!r}"]
    if len(entries) > MAX_INDEX_ENTRIES:
        errors.append(f"index entry count exceeds {MAX_INDEX_ENTRIES}")
    total_size = 0
    seen: set[str] = set()
    previous_key: tuple[str, str, str, int] | None = None
    for entry in entries:
        location = entry.path if isinstance(entry, IndexedFile) else "<invalid-entry>"
        if not isinstance(entry, IndexedFile):
            errors.append(f"{location}: index entry is not an IndexedFile")
            continue
        path_is_string = isinstance(entry.path, str)
        oid_is_string = isinstance(entry.oid, str)
        size_is_integer = type(entry.size) is int and not isinstance(entry.size, bool)
        if path_is_string and oid_is_string and size_is_integer and isinstance(entry.mode, str):
            key = (entry.path, entry.mode, entry.oid, entry.size)
            if previous_key is not None and key < previous_key:
                errors.append(f"{entry.path}: index entries are not in canonical order")
            previous_key = key
        if path_is_string and entry.path in seen:
            errors.append(f"duplicate tracked path {entry.path!r}")
        if path_is_string:
            seen.add(entry.path)
        if not _in_scope(entry.path):
            errors.append(f"noncanonical or out-of-scope tracked path {entry.path!r}")
        if entry.mode not in {"100644", "100755"}:
            errors.append(f"{entry.path}: tracked mode {entry.mode!r} is not a regular file")
        if not oid_is_string or len(entry.oid) != expected_oid_length or not HEX_RE.fullmatch(entry.oid):
            errors.append(f"{entry.path}: invalid {object_format} blob object id")
        if type(entry.size) is not int or isinstance(entry.size, bool) or entry.size < 0 or entry.size > MAX_BLOB_BYTES:
            errors.append(f"{entry.path}: invalid or oversized blob byte count")
        else:
            total_size += entry.size
            if total_size > MAX_TOTAL_BLOB_BYTES:
                errors.append(f"index blob total exceeds {MAX_TOTAL_BLOB_BYTES} bytes")
    # A malformed or over-budget index must never trigger a large worktree
    # traversal.  The caller still receives every structural diagnostic.
    if errors or root is None:
        return errors

    seen_inodes: dict[tuple[int, int], str] = {}
    for entry in entries:
        try:
            byte_count, digest, metadata = _read_worktree_digest(root, entry, object_format)
        except (OSError, ValueError) as exc:
            errors.append(f"{entry.path}: cannot read exact worktree file: {exc}")
            continue
        inode = (metadata.st_dev, metadata.st_ino)
        if inode in seen_inodes:
            errors.append(f"{entry.path}: worktree inode is already used by {seen_inodes[inode]!r}")
        else:
            seen_inodes[inode] = entry.path
        if byte_count != entry.size:
            errors.append(f"{entry.path}: worktree byte count differs from index blob")
        if digest != entry.oid:
            errors.append(f"{entry.path}: worktree bytes differ from index blob")
    return errors


def validate_entries(
    entries: Sequence[IndexedFile],
    *,
    root: Path | None,
    object_format: str = "sha1",
) -> list[str]:
    """Validate index records with a bounded two-pass worktree observation.

    The second pass catches a file that changes immediately after its first
    digest.  This is deliberately a bounded coherence check, not an atomic
    snapshot or an authority claim.
    """
    errors = _validate_entries_once(entries, root=root, object_format=object_format)
    if root is not None and not any(error.startswith("unsupported Git object format") for error in errors):
        errors.extend(_validate_entries_once(entries, root=root, object_format=object_format))
    return sorted(set(errors))


def _record_digest(entries: Sequence[IndexedFile]) -> str:
    digest = hashlib.sha256()
    digest.update(b"[")
    for index, entry in enumerate(sorted(entries, key=lambda item: (item.path, item.mode, item.oid, item.size))):
        if index:
            digest.update(b",")
        record = [entry.path, entry.mode, entry.oid, entry.size]
        digest.update(json.dumps(record, ensure_ascii=True, separators=(",", ":")).encode("utf-8"))
    digest.update(b"]")
    return "sha256:" + digest.hexdigest()


def census(entries: Sequence[IndexedFile]) -> dict[str, Any]:
    groups: dict[str, list[IndexedFile]] = {identifier: [] for identifier in CLASS_ORDER}
    for entry in entries:
        groups[classify_path(entry.path)].append(entry)
    classes = []
    for identifier in CLASS_ORDER:
        members = groups[identifier]
        classes.append(
            {
                "id": identifier,
                "disposition": CLASS_DISPOSITIONS[identifier],
                "file_count": len(members),
                "byte_count": sum(item.size for item in members),
                "record_sha256": _record_digest(members),
            }
        )
    unclassified = groups["unclassified-artifacts"]
    return {
        "classes": classes,
        "totals": {
            "file_count": len(entries),
            "byte_count": sum(item.size for item in entries),
            "record_sha256": _record_digest(entries),
            "unclassified_file_count": len(unclassified),
            "unclassified_byte_count": sum(item.size for item in unclassified),
        },
    }


def _entry_records(entries: Sequence[IndexedFile]) -> list[list[Any]]:
    return [[entry.path, entry.mode, entry.oid, entry.size] for entry in sorted(entries, key=lambda item: (item.path, item.mode, item.oid, item.size))]


def _validate_declared_entries(value: Any, errors: list[str]) -> list[IndexedFile]:
    if not isinstance(value, list):
        errors.append("contract.entries: must be a list")
        return []
    if len(value) > MAX_INDEX_ENTRIES:
        errors.append(f"contract.entries: exceeds {MAX_INDEX_ENTRIES} entries")
        return []
    result: list[IndexedFile] = []
    seen: set[str] = set()
    for index, item in enumerate(value):
        location = f"contract.entries[{index}]"
        if not _exact_fields(item, ENTRY_FIELDS, location, errors):
            continue
        assert isinstance(item, Mapping)
        path = item.get("path")
        mode = item.get("mode")
        oid = item.get("blob_oid")
        size = item.get("byte_count")
        if not _in_scope(path):
            errors.append(f"{location}.path: noncanonical or out of scope")
        path_is_string = isinstance(path, str)
        if path_is_string and path in seen:
            errors.append(f"{location}.path: duplicate path {path!r}")
        if path_is_string:
            seen.add(path)
        if mode not in {"100644", "100755"}:
            errors.append(f"{location}.mode: must be 100644 or 100755")
        if not isinstance(oid, str) or not HEX_RE.fullmatch(oid):
            errors.append(f"{location}.blob_oid: must be lowercase hexadecimal")
        if type(size) is not int or isinstance(size, bool) or size < 0 or size > MAX_BLOB_BYTES:
            errors.append(f"{location}.byte_count: must be a bounded non-negative integer")
        valid_entry = (
            isinstance(path, str)
            and _in_scope(path)
            and mode in {"100644", "100755"}
            and isinstance(oid, str)
            and bool(HEX_RE.fullmatch(oid))
            and type(size) is int
            and not isinstance(size, bool)
            and 0 <= size <= MAX_BLOB_BYTES
        )
        if valid_entry:
            result.append(IndexedFile(path=path, mode=mode, oid=oid, size=size))
    if result != sorted(result, key=lambda item: (item.path, item.mode, item.oid, item.size)):
        errors.append("contract.entries: must be sorted by path/mode/blob_oid/byte_count")
    return result


def validate_contract(
    contract: Mapping[str, Any],
    entries: Sequence[IndexedFile],
    *,
    root: Path | None = None,
    object_format: str = "sha1",
) -> list[str]:
    errors = validate_entries(entries, root=root, object_format=object_format)
    if not _exact_fields(contract, TOP_FIELDS, "contract", errors):
        return errors
    if contract.get("schema_version") != SCHEMA_VERSION:
        errors.append("contract.schema_version: unsupported schema version")
    if contract.get("contract_id") != "gravity-artifact-census-v1":
        errors.append("contract.contract_id: must be gravity-artifact-census-v1")
    if contract.get("kind") != "gravity/artifact-census":
        errors.append("contract.kind: must be gravity/artifact-census")
    if contract.get("status") != "incomplete":
        errors.append("contract.status: must remain incomplete")
    if contract.get("authority") != "none":
        errors.append("contract.authority: must remain none")
    if not isinstance(contract.get("description"), str) or not contract.get("description"):
        errors.append("contract.description: must be a nonempty string")

    scope = contract.get("scope")
    if _exact_fields(scope, SCOPE_FIELDS, "contract.scope", errors):
        if scope.get("roots") != list(SCOPED_ROOTS):
            errors.append("contract.scope.roots: must equal the exact artifact roots")
        if scope.get("source") != "git-index-blobs-plus-bounded-two-pass-worktree-bytes-v1":
            errors.append("contract.scope.source: unsupported census source")
        if scope.get("worktree_byte_check") is not True:
            errors.append("contract.scope.worktree_byte_check: must remain true")
        if scope.get("unknown_path_policy") != "classify-unclassified-and-fail-completion":
            errors.append("contract.scope.unknown_path_policy: must remain fail closed")

    identity = contract.get("identity")
    expected_identity = {
        "algorithm": "sha256-canonical-json-index-records-v1",
        "record_fields": ["path", "mode", "blob_oid", "byte_count"],
        "ordering": "utf8-path-mode-oid-size-lexicographic",
        "encoding": "utf-8-json-ensure-ascii-compact",
        "git_object_format": object_format,
    }
    if _exact_fields(identity, IDENTITY_FIELDS, "contract.identity", errors) and identity != expected_identity:
        errors.append("contract.identity: must equal the exact v1 identity algorithm")

    declared_entries = _validate_declared_entries(contract.get("entries"), errors)
    if declared_entries and _entry_records(declared_entries) != _entry_records(entries):
        errors.append("contract.entries: Git index path/mode/blob/size identities drifted")
    elif len(declared_entries) != len(entries):
        errors.append("contract.entries: Git index entry count drifted")

    actual = census(entries)
    expected_classes = contract.get("classes")
    if not isinstance(expected_classes, list) or len(expected_classes) != len(CLASS_ORDER):
        errors.append("contract.classes: must contain exactly the fixed classes")
    else:
        ids = [item.get("id") if isinstance(item, Mapping) else None for item in expected_classes]
        if ids != list(CLASS_ORDER):
            errors.append("contract.classes: must use the exact fixed class order")
        actual_by_id = {item["id"]: item for item in actual["classes"]}
        seen_ids: set[str] = set()
        for index, item in enumerate(expected_classes):
            location = f"contract.classes[{index}]"
            if not _exact_fields(item, CLASS_FIELDS, location, errors):
                continue
            assert isinstance(item, Mapping)
            identifier = item.get("id")
            if identifier in seen_ids:
                errors.append(f"{location}.id: duplicate class {identifier!r}")
            seen_ids.add(identifier)
            if identifier not in CLASS_DISPOSITIONS:
                errors.append(f"{location}.id: unknown class {identifier!r}")
                continue
            if item.get("disposition") != CLASS_DISPOSITIONS[identifier]:
                errors.append(f"{location}.disposition: incorrect fixed disposition")
            if item.get("path_rule") != CLASS_PATH_RULES[identifier]:
                errors.append(f"{location}.path_rule: must equal the fixed rule for {identifier}")
            for field in ("file_count", "byte_count", "record_sha256"):
                if item.get(field) != actual_by_id[identifier][field]:
                    errors.append(f"{location}.{field}: census drift")
            if not SHA256_RE.fullmatch(str(item.get("record_sha256", ""))):
                errors.append(f"{location}.record_sha256: invalid sha256 identity")

    totals = contract.get("totals")
    if _exact_fields(totals, TOTAL_FIELDS, "contract.totals", errors):
        for field, value in actual["totals"].items():
            if totals.get(field) != value:
                errors.append(f"contract.totals.{field}: census drift")

    required_nonclaims = [
        "artifact-authority",
        "artifact-validity",
        "completion",
        "release-readiness",
        "self-hosting",
        "seed-retirement",
    ]
    if contract.get("nonclaims") != required_nonclaims:
        errors.append("contract.nonclaims: must equal the exact reporting-only nonclaims")
    expected_diagnostics = {
        "schema": "ARTIFACT-CENSUS-SCHEMA",
        "drift": "ARTIFACT-CENSUS-DRIFT",
    }
    if contract.get("diagnostics") != expected_diagnostics:
        errors.append("contract.diagnostics: must equal the exact diagnostic catalog")
    if actual["totals"]["unclassified_file_count"] == 0:
        errors.append("contract: v1 cannot complete while the unclassified class is empty")
    return sorted(set(errors))


def main(argv: Sequence[str] | None = None) -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--root", type=Path, default=ROOT)
    parser.add_argument("--contract", type=Path, default=None)
    args = parser.parse_args(argv)
    # Keep the lexical root so the no-follow dirfd walk can reject a symlinked
    # root rather than silently resolving it into a different tree.
    root = Path(os.path.abspath(args.root))
    contract_path = Path(os.path.abspath(args.contract if args.contract is not None else root / "contracts" / "artifact-census.json"))
    try:
        contract = load_json(contract_path)
        object_format = _object_format(root)
        entries = discover_index(root, object_format=object_format)
        errors = validate_contract(contract, entries, root=root, object_format=object_format)
        post_entries = discover_index(root, object_format=object_format)
        if _entry_records(post_entries) != _entry_records(entries):
            errors.append("scan-stability: Git index identities changed during worktree scan")
    except (OSError, ValueError, RuntimeError, UnicodeError, subprocess.SubprocessError, json.JSONDecodeError) as exc:
        print(f"ARTIFACT-CENSUS-SCHEMA: {exc}", file=sys.stderr)
        return 1
    if errors:
        for error in errors:
            print(f"ARTIFACT-CENSUS-DRIFT: {error}", file=sys.stderr)
        return 1
    result = census(entries)
    totals = result["totals"]
    print(
        "artifact census validated: "
        f"{totals['file_count']} files, {totals['byte_count']} bytes, "
        f"{totals['unclassified_file_count']} unclassified; status incomplete, authority none"
    )
    for item in result["classes"]:
        print(
            f"  {item['id']}: {item['file_count']} files, {item['byte_count']} bytes, "
            f"{item['record_sha256']}"
        )
    print(f"  total-records: {totals['record_sha256']}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
