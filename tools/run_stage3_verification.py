#!/usr/bin/env python3
"""Run the reviewed Stage 3 C7 verification boundary.

The development verifier deliberately treats this file as the only command
that may own the SH-07 heavy lock.  The command is intentionally boring: its
argv is fixed by the manifest and all per-invocation state is supplied through
an environment binding created by :mod:`verify_development`.  There is no
public command passthrough or shell evaluation in this wrapper.

Three explicit modes are supported.  A public/pure batch acquires the
canonical SH-07 lease and runs one of the reviewed Clojure batches.  A
proof-candidate authority batch starts a *new* checkpoint directory and
invokes the existing SH-07 checkpoint runner with exactly ``c7-types`` and
``--no-resume``; that child, rather than this process, owns the shared lease.
A reviewed-attestation batch acquires its own current lease and promotes only
a separately supplied, hash-bound candidate.
"""

from __future__ import annotations

import argparse
import contextlib
import dataclasses
import hashlib
import json
import os
from pathlib import Path
import re
import stat
import subprocess
import sys
import tempfile
import time
import uuid
from typing import Any, Callable, Mapping, Sequence

try:
    from tools import run_sh07_authoritative_modules as _sh07
except ImportError:  # pragma: no cover - direct execution from tools/
    import run_sh07_authoritative_modules as _sh07


SCHEMA = "gravity/stage3-verification-receipt-v1"
MAX_RECEIPT_BYTES = 64 * 1024
MAX_AUTHORITY_MANIFEST_BYTES = 2 * 1024 * 1024
# A receipt contains both child streams, the fixed runner report, and
# supervision metadata.  Per-stream retention therefore has to be
# substantially below the wire-size ceiling; bounding each stream alone
# would still permit a receipt larger than MAX_RECEIPT_BYTES.
MAX_CHILD_OUTPUT_BYTES = 8 * 1024
MAX_CHILD_OUTPUT_COMBINED_BYTES = 12 * 1024
MAX_AUTHORITY_OUTPUT_BYTES = 4 * 1024 * 1024
MAX_AUTHORITY_SOURCE_BYTES = 16 * 1024 * 1024
STAGE3_RUNTIME_DEPENDENCIES = (
    "tools/run_stage3_verification.py",
    "tools/run_sh07_authoritative_modules.py",
)
DEFAULT_BATCH = "public-c7-check"
DEFAULT_MODE = "pure"
MODE_PURE = "pure"
MODE_PROOF_CANDIDATE = "proof-candidate"
MODE_REVIEWED_ATTESTATION = "reviewed-attestation"
# Candidate generation is the only authority-adjacent mode admitted by this
# reviewed slice.  The reviewed-attestation implementation remains dormant
# until its manifest graph and artifact hand-off contract are frozen.
MODES = (MODE_PURE, MODE_PROOF_CANDIDATE)

# These are reviewed names, not an extension point.  The Clojure writer may
# add aliases for the names, but adding a name here is itself a reviewable
# change.  ``fragment-size-preflight`` is intentionally included because it
# is a cheap prerequisite for the C7 source batch.
FIXED_BATCHES = (
    "primitive-pure",
    "primitive-bool-authenticated",
    "recursive-pure",
    "recursive-integer-authenticated",
    "recursive-string-authenticated",
    "authoritative-ho-pure",
    "authoritative-ho-fixture-parity",
    "authoritative-ho2-authenticated",
    "source-control-form-arity",
    "source-plan-contract",
    "coverage-census-contract",
    "fragment-size-preflight",
    "public-c7-check",
    "authority",
)

_BATCH_ALIAS = "-M:stage3-verification"
_BATCH_HEAP = {
    # Public/source-plan/fragment preflights have a reviewed 2 GiB floor;
    # cold primitive/recursive/HO batches retain the measured 8 GiB bound.
    "public-c7-check": "-J-Xmx2g",
    "fragment-size-preflight": "-J-Xmx2g",
    "source-plan-contract": "-J-Xmx2g",
    "coverage-census-contract": "-J-Xmx2g",
    "source-control-form-arity": "-J-Xmx2g",
}
_BATCH_COMMANDS: dict[str, tuple[str, ...]] = {
    # The Clojure writer owns this one alias.  Batch identity is passed only
    # as the fixed selector value; no namespace, test var, or generic test
    # runner arguments are accepted at this boundary.
    batch: ("clojure", _BATCH_HEAP.get(batch, "-J-Xmx8g"), _BATCH_ALIAS, "--batch", batch)
    for batch in FIXED_BATCHES
    if batch != "authority"
}

_FIXED_BATCH_SELECTORS: dict[str, tuple[str, ...]] = {
    "primitive-pure": (
        "gravity.self-hosting.sh08-primitive-function-type-test/sh08-primitive-family-structure-and-fixture-parity",
        "gravity.self-hosting.sh08-primitive-function-type-test/sh08-primitive-family-ho-diagonal",
        "gravity.self-hosting.sh08-primitive-function-type-test/sh08-primitive-family-ho-unsupported-is-explicit",
        "gravity.self-hosting.sh08-primitive-function-type-test/sh08-primitive-family-ho-mutation-is-not-silent",
    ),
    "primitive-bool-authenticated": (
        "gravity.self-hosting.sh08-primitive-function-type-test/sh08-primitive-family-authenticated-bool-gravity-boundary",
    ),
    "recursive-pure": (
        "gravity.self-hosting.sh08-recursive-function-type-test/sh08-recursive-source-reachability-and-structure",
        "gravity.self-hosting.sh08-recursive-function-type-test/sh08-recursive-fixture-pair-is-byte-identical",
        "gravity.self-hosting.sh08-recursive-function-type-test/sh08-recursive-pure-positive-and-monotone-fixed-point",
        "gravity.self-hosting.sh08-recursive-function-type-test/sh08-recursive-pure-hostile-matrix",
        "gravity.self-hosting.sh08-recursive-function-type-test/sh08-recursive-unsupported-external-primitive-keeps-evidence",
        "gravity.self-hosting.sh08-recursive-function-type-test/sh08-recursive-primitive-family-diagonal-and-conflicts",
        "gravity.self-hosting.sh08-recursive-function-type-test/sh08-recursive-nonconvergence-is-precise",
    ),
    "recursive-integer-authenticated": (
        "gravity.self-hosting.sh08-recursive-function-type-test/sh08-recursive-authenticated-gravity-boundary",
    ),
    "recursive-string-authenticated": (
        "gravity.self-hosting.sh08-recursive-function-type-test/sh08-recursive-authenticated-string-gravity-boundary",
    ),
    "authoritative-ho-pure": (
        "gravity.self-hosting.sh08-authoritative-higher-order-function-test/sh08-authoritative-c7-reachability-and-identity",
        "gravity.self-hosting.sh08-authoritative-higher-order-function-test/sh08-authoritative-pure-proof-and-context-matrix",
        "gravity.self-hosting.sh08-authoritative-higher-order-function-test/sh08-authoritative-first-order-record-shape-is-additive",
        "gravity.self-hosting.sh08-authoritative-higher-order-function-test/sh08-authoritative-pure-proof-rejects-substitution",
        "gravity.self-hosting.sh08-authoritative-higher-order-function-test/sh08-authoritative-pure-proof-rejects-nonfunction-capture-and-arity",
        "gravity.self-hosting.sh08-authoritative-higher-order-function-test/sh08-authoritative-rejected-proof-uses-first-order-public-fallback",
        "gravity.self-hosting.sh08-authoritative-higher-order-function-test/sh08-authoritative-higher-order-pending-is-an-exact-replacement",
    ),
    "authoritative-ho-fixture-parity": (
        "gravity.self-hosting.sh08-authoritative-higher-order-function-test/sh08-authoritative-ho2-fixtures-are-co-canonical",
    ),
    "authoritative-ho2-authenticated": (
        "gravity.self-hosting.sh08-authoritative-higher-order-function-test/sh08-authoritative-ho2-authenticated-fixture-boundary",
    ),
    "source-control-form-arity": (
        "gravity.self-hosting.sh07-c7-type-source-coverage-test/sh07-b47-c7-source-control-form-arities-are-exact",
    ),
    "source-plan-contract": (
        "gravity.self-hosting.sh07-c7-type-source-coverage-test/sh07-b47-c7-stage2-plan-identity-is-exact",
        "gravity.self-hosting.sh07-c7-type-source-coverage-test/sh07-b28-c7-source-contracts-bounds-pending-and-limitations-are-exact",
    ),
    "coverage-census-contract": (
        "gravity.self-hosting.sh07-authoritative-coverage-census-test/proof-contract-binds-the-measured-c7-census",
        "gravity.self-hosting.sh07-authoritative-coverage-census-test/source-contract-mismatch-stops-before-authoritative-proof",
    ),
    "fragment-size-preflight": (
        "gravity.self-hosting.stage3-fragment-size-preflight-test/stage3-fragment-size-preflight",
    ),
    "public-c7-check": (
        "gravity.bootstrap-test/public-check-accepts-gravity-authored-c7-type-checker-engine",
    ),
}

CANONICAL_LOCK = _sh07.canonical_shared_lock_path(_sh07.DEFAULT_LOCK)
CANONICAL_LOCK_TEXT = str(CANONICAL_LOCK)
AUTHORITY_CHILD_SCRIPT = "tools/run_sh07_authoritative_modules.py"
AUTHORITY_MODULE = "c7-types"
RECEIPT_ENV = "GRAVITY_STAGE3_RECEIPT_PATH"
REPORT_ENV = "GRAVITY_STAGE3_REPORT_PATH"
NONCE_ENV = "GRAVITY_STAGE3_RECEIPT_NONCE"
CHECK_ID_ENV = "GRAVITY_STAGE3_CHECK_ID"
ROOT_ENV = "GRAVITY_STAGE3_ROOT"
MODE_ENV = "GRAVITY_STAGE3_MODE"
BATCH_ENV = "GRAVITY_STAGE3_BATCH"
COMMAND_HASH_ENV = "GRAVITY_STAGE3_COMMAND_IDENTITY_SHA256"
EXPECTED_COMMAND_ENV = "GRAVITY_STAGE3_EXPECTED_COMMAND"
TIMEOUT_ENV = "GRAVITY_STAGE3_TIMEOUT_SECONDS"
CANDIDATE_CHECK_ID_ENV = "GRAVITY_STAGE3_CANDIDATE_CHECK_ID"
CANDIDATE_RECEIPT_ENV = "GRAVITY_STAGE3_CANDIDATE_RECEIPT_PATH"
CANDIDATE_RECEIPT_SHA_ENV = "GRAVITY_STAGE3_CANDIDATE_RECEIPT_SHA256"
CANDIDATE_STATE_ENV = "GRAVITY_STAGE3_CANDIDATE_STATE_DIR"
CANDIDATE_MANIFEST_ENV = "GRAVITY_STAGE3_CANDIDATE_MANIFEST_PATH"
CANDIDATE_MANIFEST_SHA_ENV = "GRAVITY_STAGE3_CANDIDATE_MANIFEST_SHA256"
ATTESTATION_PATH_ENV = "GRAVITY_STAGE3_ATTESTATION_PATH"
ATTESTATION_SHA_ENV = "GRAVITY_STAGE3_ATTESTATION_SHA256"
_SAFE_SLUG = re.compile(r"^[A-Za-z0-9][A-Za-z0-9_.-]{0,127}$")
_SHA256 = re.compile(r"^sha256:[0-9a-f]{64}$")


class Stage3Error(RuntimeError):
    """Raised when the fixed Stage 3 boundary cannot produce valid evidence."""


@dataclasses.dataclass(frozen=True)
class ChildResult:
    returncode: int
    stdout: str = ""
    stderr: str = ""
    timed_out: bool = False
    survivors: tuple[object, ...] = ()
    cleanup: Mapping[str, object] | None = None
    supervision_failed: bool = False
    observed_peak_process_tree_rss_bytes: int | None = None
    rss_sampling_cadence_seconds: float | None = None
    rss_sampling_contract: str | None = None
    rss_sampling_limitation: str | None = None


Launcher = Callable[[Sequence[str], Path, Mapping[str, str], float], ChildResult]


def _now() -> str:
    # UTC ``Z`` timestamps keep receipts deterministic enough for machines.
    import datetime as dt

    return dt.datetime.now(dt.timezone.utc).isoformat(timespec="milliseconds").replace(
        "+00:00", "Z"
    )


def _sha256_bytes(value: bytes) -> str:
    return hashlib.sha256(value).hexdigest()


def _sha256_file(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as stream:
        for block in iter(lambda: stream.read(1024 * 1024), b""):
            digest.update(block)
    return digest.hexdigest()


def _trim(value: object, *, maximum: int = MAX_CHILD_OUTPUT_BYTES) -> str:
    text = value if isinstance(value, str) else ""
    if maximum <= 0:
        return ""
    data = text.encode("utf-8", errors="replace")
    if len(data) <= maximum:
        return text
    marker = b"\n[output truncated]"
    if len(marker) >= maximum:
        return marker[:maximum].decode("ascii", errors="ignore")
    # Decode with ``ignore`` after cutting at the byte boundary.  This keeps
    # the retained wire size bounded even when the child emits a stream of
    # multi-byte Unicode characters and the cut lands inside one character.
    prefix = data[: maximum - len(marker)].decode("utf-8", errors="ignore")
    result = prefix + marker.decode("ascii")
    # A decoded prefix can be shorter than its byte slice, but never longer;
    # keep this guard so the invariant remains true if the marker changes.
    while len(result.encode("utf-8")) > maximum and prefix:
        prefix = prefix[:-1]
        result = prefix + marker.decode("ascii")
    return result


def _trim_child_streams(stdout: object, stderr: object) -> tuple[str, str]:
    """Return bounded child streams with one coherent combined byte budget."""

    retained_stdout = _trim(stdout)
    retained_stderr = _trim(stderr)
    total = len(retained_stdout.encode("utf-8")) + len(retained_stderr.encode("utf-8"))
    if total <= MAX_CHILD_OUTPUT_COMBINED_BYTES:
        return retained_stdout, retained_stderr
    # Equal budgets are deliberately conservative.  They make the bound
    # independent of which stream is noisy and leave ample room for report
    # metadata in the 64 KiB receipt.
    first_budget = MAX_CHILD_OUTPUT_COMBINED_BYTES // 2
    second_budget = MAX_CHILD_OUTPUT_COMBINED_BYTES - first_budget
    return _trim(retained_stdout, maximum=first_budget), _trim(
        retained_stderr, maximum=second_budget
    )


def _canonical_json(value: object) -> str:
    return json.dumps(value, sort_keys=True, separators=(",", ":"), ensure_ascii=True)


def _safe_slug(value: object, label: str) -> str:
    if not isinstance(value, str) or _SAFE_SLUG.fullmatch(value) is None:
        raise Stage3Error(f"{label} must be a bounded safe slug")
    return value


def canonical_lock_path(path: Path | str = _sh07.DEFAULT_LOCK) -> Path:
    """Validate that ``path`` denotes exactly the reviewed SH-07 lock."""

    try:
        canonical = _sh07.canonical_shared_lock_path(Path(path))
    except Exception as exc:  # the SH-07 helper intentionally has its own error type
        raise Stage3Error(f"invalid Stage 3 lock path: {path}") from exc
    if canonical != CANONICAL_LOCK:
        raise Stage3Error(
            f"Stage 3 requires canonical SH-07 lock {CANONICAL_LOCK_TEXT}, got {canonical}"
        )
    return canonical


def batch_command(
    batch: str,
    *,
    report_path: Path | None = None,
    nonce: str | None = None,
    check_id: str | None = None,
    command_identity_sha256: str | None = None,
) -> list[str]:
    """Return one reviewed batch command with verifier-bound report args."""

    _safe_slug(batch, "batch")
    if batch not in _BATCH_COMMANDS:
        raise Stage3Error(f"unknown Stage 3 batch: {batch}")
    command = list(_BATCH_COMMANDS[batch])
    if report_path is None:
        return command
    if not (nonce and check_id and command_identity_sha256):
        raise Stage3Error("runner report binding is incomplete")
    command.extend([
        "--report-file", str(report_path),
        "--report-nonce", nonce,
        "--report-check-id", check_id,
        "--report-command-identity-sha256", command_identity_sha256,
    ])
    return command


def _normalise_child_result(value: object) -> ChildResult:
    if isinstance(value, ChildResult):
        return value
    if isinstance(value, subprocess.CompletedProcess):
        return ChildResult(
            int(value.returncode),
            _trim(value.stdout),
            _trim(value.stderr),
        )
    if isinstance(value, Mapping):
        raw_survivors = value.get("survivors")
        if raw_survivors is None:
            survivors = ("descendant",) if value.get("surviving_descendants") else ()
        elif isinstance(raw_survivors, (str, bytes)):
            survivors = (raw_survivors,)
        else:
            try:
                survivors = tuple(raw_survivors)
            except TypeError:
                survivors = ("descendant",)
        return ChildResult(
            int(value.get("returncode", value.get("exit_code", 1))),
            _trim(value.get("stdout", "")),
            _trim(value.get("stderr", "")),
            bool(value.get("timed_out", value.get("timeout", False))),
            survivors,
            value.get("cleanup") if isinstance(value.get("cleanup"), Mapping) else None,
            bool(value.get("supervision_failed", False)),
            value.get("observed_peak_process_tree_rss_bytes")
            if type(value.get("observed_peak_process_tree_rss_bytes")) is int
            and value.get("observed_peak_process_tree_rss_bytes") >= 0
            else None,
            float(value.get("rss_sampling_cadence_seconds"))
            if isinstance(value.get("rss_sampling_cadence_seconds"), (int, float))
            else None,
            str(value.get("rss_sampling_contract"))
            if value.get("rss_sampling_contract") is not None else None,
            str(value.get("rss_sampling_limitation"))
            if value.get("rss_sampling_limitation") is not None else None,
        )
    if isinstance(value, int):
        return ChildResult(value)
    raise Stage3Error("Stage 3 launcher returned an unsupported result")


def default_launcher(
    command: Sequence[str],
    cwd: Path,
    env: Mapping[str, str],
    timeout_seconds: float,
) -> ChildResult:
    """Run one fixed command through the repository process supervisor.

    Stage 3 deliberately shares the verifier's process-group/descendant
    containment implementation.  A direct ``subprocess.run`` with captured
    pipes can deadlock on an inherited descriptor and cannot prove that a
    detached child is gone before the SH-07 lease is released.
    """

    try:
        # Import lazily: ``verify_development`` imports this module to perform
        # manifest validation, while this path is used only after the wrapper
        # has been admitted and is actually launching its child.
        try:
            from tools import verify_development as verifier
        except ImportError:  # pragma: no cover - direct tools/ execution
            import verify_development as verifier

        marker = f"stage3-child-{os.getpid()}-{uuid.uuid4().hex}"
        outcome = verifier._run_command(  # type: ignore[attr-defined]
            list(command), cwd=cwd, env=env, timeout=timeout_seconds, marker=marker,
            sample_rss=True,
        )
        return _normalise_child_result(outcome)
    except OSError as exc:
        return ChildResult(127, "", str(exc))
    except Exception as exc:
        return ChildResult(125, "", f"Stage 3 child supervision failed: {exc}", supervision_failed=True)


def _safe_root(root: Path | str) -> Path:
    path = Path(root).expanduser().resolve()
    if not path.is_dir() or path.is_symlink():
        raise Stage3Error(f"Stage 3 root must be a real directory: {path}")
    return path


def _safe_root_relative(root: Path, path: Path, label: str) -> Path:
    candidate = Path(path)
    if not candidate.is_absolute():
        candidate = root / candidate
    # Resolve only trusted system aliases (not a final receipt symlink) so a
    # macOS ``/var`` temporary directory compares with the already-resolved
    # repository root.  The final target is still checked with lstat below.
    candidate = candidate.resolve(strict=False)
    try:
        relative = candidate.relative_to(root)
    except ValueError as exc:
        raise Stage3Error(f"{label} must stay inside the repository root") from exc
    if not relative.parts:
        raise Stage3Error(f"{label} cannot be the repository root")
    current = root
    for component in relative.parts[:-1]:
        current /= component
        try:
            info = os.lstat(current)
        except FileNotFoundError:
            continue
        if stat.S_ISLNK(info.st_mode) or not stat.S_ISDIR(info.st_mode):
            raise Stage3Error(f"{label} has an unsafe parent component: {current}")
    return candidate


def _unresolved_root_relative(root: Path, path: Path | str, label: str) -> tuple[Path, Path]:
    """Return an absolute path and lexical relative path without following the leaf.

    ``Path.resolve`` is useful for comparing the platform's ``/var`` alias,
    but it is unsafe when deciding whether a pre-existing receipt symlink may
    be replaced.  This helper normalises ``..`` lexically and leaves every
    filesystem component for the dirfd/O_NOFOLLOW walk below.
    """

    root_abs = Path(os.path.abspath(str(root)))
    candidate = Path(path)
    if not candidate.is_absolute():
        candidate = root_abs / candidate
    candidate = Path(os.path.abspath(str(candidate)))
    try:
        relative = candidate.relative_to(root_abs)
    except ValueError as exc:
        raise Stage3Error(f"{label} must stay inside the repository root") from exc
    if not relative.parts or any(part in {"", ".", ".."} for part in relative.parts):
        raise Stage3Error(f"{label} has an unsafe relative path")
    return candidate, relative


_DIR_FLAGS = os.O_RDONLY | getattr(os, "O_DIRECTORY", 0) | getattr(os, "O_NOFOLLOW", 0) | getattr(os, "O_CLOEXEC", 0)
_FILE_FLAGS = os.O_RDONLY | getattr(os, "O_NOFOLLOW", 0) | getattr(os, "O_CLOEXEC", 0)


def _open_parent_dirfd(root: Path, path: Path | str, label: str, *, create: bool = False) -> tuple[int, str]:
    """Open a root-contained parent directory and return ``(fd, leaf)``.

    Every component is opened relative to the held directory descriptor with
    ``O_NOFOLLOW``.  This closes the parent-swap window present in a sequence
    of path-based ``mkdir``/``lstat``/``open`` calls.
    """

    _absolute, relative = _unresolved_root_relative(root, path, label)
    root_fd = os.open(root, _DIR_FLAGS)
    current = root_fd
    opened: list[int] = [root_fd]
    try:
        for component in relative.parts[:-1]:
            try:
                next_fd = os.open(component, _DIR_FLAGS, dir_fd=current)
            except FileNotFoundError:
                if not create:
                    raise
                os.mkdir(component, mode=0o700, dir_fd=current)
                next_fd = os.open(component, _DIR_FLAGS, dir_fd=current)
            info = os.fstat(next_fd)
            if not stat.S_ISDIR(info.st_mode) or info.st_uid != os.geteuid():
                os.close(next_fd)
                raise Stage3Error(f"{label} parent component is not an owned directory: {component}")
            opened.append(next_fd)
            current = next_fd
        # Keep only the parent descriptor alive for the caller.  Closing all
        # earlier fds is safe because ``current`` is an independent open file
        # description, not a dup of its parent.
        for descriptor in opened[:-1]:
            os.close(descriptor)
        return current, relative.parts[-1]
    except BaseException:
        for descriptor in reversed(opened):
            try:
                os.close(descriptor)
            except OSError:
                pass
        raise


def _ensure_directory(root: Path, path: Path | str, label: str, *, mode: int = 0o700, fresh: bool = False) -> Path:
    """Create/validate one root-contained directory tree using dirfds."""

    absolute, relative = _unresolved_root_relative(root, path, label)
    root_fd = os.open(root, _DIR_FLAGS)
    current = root_fd
    opened = [root_fd]
    try:
        for index, component in enumerate(relative.parts):
            existed = True
            try:
                info = os.stat(component, dir_fd=current, follow_symlinks=False)
            except FileNotFoundError:
                existed = False
                os.mkdir(component, mode=mode, dir_fd=current)
                info = os.stat(component, dir_fd=current, follow_symlinks=False)
            if not stat.S_ISDIR(info.st_mode) or info.st_uid != os.geteuid():
                raise Stage3Error(f"{label} component is not an owned directory: {component}")
            if fresh and index == len(relative.parts) - 1 and existed:
                raise Stage3Error(f"{label} directory is not fresh: {absolute}")
            next_fd = os.open(component, _DIR_FLAGS, dir_fd=current)
            opened.append(next_fd)
            current = next_fd
        try:
            os.fsync(opened[-2] if len(opened) > 1 else opened[-1])
        except OSError:
            pass
        return absolute
    finally:
        for descriptor in reversed(opened):
            try:
                os.close(descriptor)
            except OSError:
                pass


def _new_receipt_path(root: Path, check_id: str, nonce: str) -> Path:
    directory = _ensure_directory(root, root / ".cpcache" / "stage3-receipts", "receipt")
    path = directory / f"{check_id}.{nonce}.json"
    parent_fd, leaf = _open_parent_dirfd(root, path, "receipt")
    try:
        try:
            os.stat(leaf, dir_fd=parent_fd, follow_symlinks=False)
        except FileNotFoundError:
            return path
        raise Stage3Error(f"Stage 3 receipt path already exists: {path}")
    finally:
        os.close(parent_fd)


def _new_runner_report_path(root: Path, check_id: str, nonce: str) -> Path:
    """Allocate the one-shot fixed-runner report beside the wrapper receipt."""

    safe_check = "".join(
        char if char.isalnum() or char in "_.-" else "_" for char in check_id
    ) or "check"
    directory = _ensure_directory(
        root,
        root / ".cpcache" / "stage3-receipts" / f"{safe_check}.{nonce}",
        "report",
        mode=0o700,
        fresh=True,
    )
    directory_info = os.stat(directory, follow_symlinks=False)
    if (not stat.S_ISDIR(directory_info.st_mode)
            or directory_info.st_uid != os.geteuid()
            or stat.S_IMODE(directory_info.st_mode) != 0o700):
        raise Stage3Error("Stage 3 runner report directory is not private and owned")
    path = directory / "runner.json"
    parent_fd, leaf = _open_parent_dirfd(root, path, "report")
    try:
        try:
            os.stat(leaf, dir_fd=parent_fd, follow_symlinks=False)
        except FileNotFoundError:
            return path
        raise Stage3Error(f"Stage 3 runner report path already exists: {path}")
    finally:
        os.close(parent_fd)


def _assert_new_regular_target(path: Path) -> None:
    try:
        info = os.lstat(path)
    except FileNotFoundError:
        return
    if stat.S_ISLNK(info.st_mode) or not stat.S_ISREG(info.st_mode) or info.st_nlink != 1:
        raise Stage3Error(f"Stage 3 receipt target is not a fresh regular file: {path}")
    raise Stage3Error(f"Stage 3 receipt target already exists: {path}")


def atomic_receipt_write(path: Path, value: Mapping[str, object], *, root: Path | None = None) -> None:
    """Publish one bounded receipt without ever replacing an existing name."""

    payload = (json.dumps(value, ensure_ascii=True, sort_keys=True, separators=(",", ":")) + "\n").encode(
        "utf-8"
    )
    if len(payload) > MAX_RECEIPT_BYTES:
        raise Stage3Error("Stage 3 receipt exceeds bounded size")
    path = path.absolute()
    root = _safe_root(root or path.parent)
    _absolute, relative = _unresolved_root_relative(root, path, "receipt")
    # Parent directories are created/validated through dirfds, and the final
    # target is inspected relative to the held parent descriptor.
    parent_descriptor, leaf = _open_parent_dirfd(root, path, "receipt", create=True)
    temporary_name = f".{leaf}.{os.getpid()}.{uuid.uuid4().hex}.tmp"
    descriptor = -1
    target_linked = False
    publication_complete = False
    try:
        descriptor = os.open(
            temporary_name,
            os.O_WRONLY | os.O_CREAT | os.O_EXCL | getattr(os, "O_NOFOLLOW", 0),
            0o600,
            dir_fd=parent_descriptor,
        )
        with os.fdopen(descriptor, "wb") as stream:
            descriptor = -1
            stream.write(payload)
            stream.flush()
            os.fsync(stream.fileno())
        # link(2) is the publication primitive: unlike replace(2), it fails
        # atomically when any stale file, symlink, or racing publisher already
        # owns the target name.  The temporary and target names must briefly
        # be the same two-link inode before the temporary name is removed.
        try:
            os.link(
                temporary_name,
                leaf,
                src_dir_fd=parent_descriptor,
                dst_dir_fd=parent_descriptor,
                follow_symlinks=False,
            )
        except FileExistsError as exc:
            raise Stage3Error(f"Stage 3 receipt target already exists: {path}") from exc
        target_linked = True
        temporary_info = os.stat(
            temporary_name, dir_fd=parent_descriptor, follow_symlinks=False
        )
        published_info = os.stat(leaf, dir_fd=parent_descriptor, follow_symlinks=False)
        if (not stat.S_ISREG(published_info.st_mode)
                or published_info.st_uid != os.geteuid()
                or published_info.st_nlink != 2
                or (temporary_info.st_dev, temporary_info.st_ino)
                != (published_info.st_dev, published_info.st_ino)):
            raise Stage3Error(f"published Stage 3 receipt is unsafe: {path}")
        os.unlink(temporary_name, dir_fd=parent_descriptor)
        published_info = os.stat(leaf, dir_fd=parent_descriptor, follow_symlinks=False)
        if (not stat.S_ISREG(published_info.st_mode)
                or published_info.st_uid != os.geteuid()
                or published_info.st_nlink != 1):
            raise Stage3Error(f"published Stage 3 receipt is not one owned file: {path}")
        publication_complete = True
        try:
            os.fsync(parent_descriptor)
        except OSError:
            pass
    finally:
        active_exception = sys.exc_info()[1]
        cleanup_error: OSError | None = None
        if descriptor != -1:
            try:
                os.close(descriptor)
            except OSError as error:
                cleanup_error = error
        # On a failed post-link validation, remove only the target name that
        # still identifies our temporary inode.  Never unlink a name an
        # adversary replaced while cleanup was in progress.
        if target_linked and not publication_complete:
            try:
                target_info = os.stat(leaf, dir_fd=parent_descriptor, follow_symlinks=False)
                temporary_info = os.stat(
                    temporary_name, dir_fd=parent_descriptor, follow_symlinks=False
                )
                if (target_info.st_dev, target_info.st_ino) == (
                    temporary_info.st_dev, temporary_info.st_ino
                ):
                    os.unlink(leaf, dir_fd=parent_descriptor)
            except FileNotFoundError:
                pass
            except OSError as error:
                cleanup_error = cleanup_error or error
        try:
            os.unlink(temporary_name, dir_fd=parent_descriptor)
        except FileNotFoundError:
            pass
        except OSError as error:
            cleanup_error = cleanup_error or error
        try:
            os.close(parent_descriptor)
        except OSError as error:
            cleanup_error = cleanup_error or error
        # Cleanup faults must not mask ThreadDeath-equivalent Python signals
        # (KeyboardInterrupt/SystemExit) or any in-flight publication error.
        if cleanup_error is not None and active_exception is None:
            raise Stage3Error(f"Stage 3 receipt cleanup failed: {cleanup_error}") from cleanup_error


def _lock_identity(handle: Any) -> dict[str, object]:
    info = os.fstat(handle.descriptor)
    return {
        "path": str(handle.path),
        "canonical_path": str(handle.path),
        "protocol": _sh07.SHARED_LOCK_PROTOCOL,
        "device": info.st_dev,
        "inode": info.st_ino,
        "uid": info.st_uid,
        "mode": f"{stat.S_IMODE(info.st_mode):04o}",
        "nlink": info.st_nlink,
    }


def _base_receipt(
    *,
    root: Path,
    receipt_path: Path,
    nonce: str,
    check_id: str,
    mode: str,
    batch: str,
    command: Sequence[str],
    command_hash: str,
) -> dict[str, object]:
    return {
        "schema": SCHEMA,
        "receipt_path": str(receipt_path),
        "root": str(root),
        "nonce": nonce,
        "check_id": check_id,
        "mode": mode,
        "batch": batch,
        "proof_candidate": mode == MODE_PROOF_CANDIDATE,
        "attestation_required": mode == MODE_PROOF_CANDIDATE,
        "candidate_receipt_path": None,
        "candidate_state_dir": None,
        "attestation_path": None,
        "attestation_sha256": None,
        "command": list(command),
        "command_identity_sha256": command_hash,
        "lock": {
            "path": CANONICAL_LOCK_TEXT,
            "canonical_path": CANONICAL_LOCK_TEXT,
            "protocol": _sh07.SHARED_LOCK_PROTOCOL,
            "acquired": False,
            "validated": False,
            "released": False,
        },
        "non_authoritative": True,
        "authority": "none",
        "status": "failed",
        "exit_code": 1,
        "no_surviving_descendants": True,
        "daemonization": "forbidden",
        "started_at": _now(),
    }


def _child_record(result: ChildResult, command: Sequence[str]) -> dict[str, object]:
    stdout, stderr = _trim_child_streams(result.stdout, result.stderr)
    return {
        "command": list(command),
        "returncode": result.returncode,
        "timed_out": result.timed_out,
        "stdout": stdout,
        "stderr": stderr,
        "survivors": list(result.survivors),
        "cleanup": dict(result.cleanup) if isinstance(result.cleanup, Mapping) else None,
        "supervision_failed": result.supervision_failed,
        "observed_peak_process_tree_rss_bytes": result.observed_peak_process_tree_rss_bytes,
        "rss_sampling_cadence_seconds": result.rss_sampling_cadence_seconds,
        "rss_sampling_contract": result.rss_sampling_contract,
        "rss_sampling_limitation": result.rss_sampling_limitation,
    }


def _finish_receipt(receipt: dict[str, object], result: ChildResult) -> None:
    cleanup_safe = (
        not isinstance(result.cleanup, Mapping)
        or result.cleanup.get("terminal_safe", True) is True
    )
    if result.timed_out:
        wrapper_exit = 124
    elif result.supervision_failed or result.survivors or not cleanup_safe:
        wrapper_exit = 75
    else:
        wrapper_exit = result.returncode
    # A malformed launcher must not smuggle booleans or a zero infrastructure
    # failure through the process boundary.
    if type(wrapper_exit) is not int or wrapper_exit == 0 and (
        result.timed_out or result.supervision_failed or result.survivors or not cleanup_safe
    ):
        wrapper_exit = 75
    receipt["exit_code"] = wrapper_exit
    receipt["observed_peak_process_tree_rss_bytes"] = result.observed_peak_process_tree_rss_bytes
    receipt["rss_sampling_cadence_seconds"] = result.rss_sampling_cadence_seconds
    receipt["rss_sampling_contract"] = result.rss_sampling_contract
    receipt["rss_sampling_limitation"] = result.rss_sampling_limitation
    receipt["no_surviving_descendants"] = bool(
        not result.survivors
        and not result.supervision_failed
        and cleanup_safe
    )
    receipt["status"] = (
        "passed"
        if wrapper_exit == 0
        and result.returncode == 0
        and not result.timed_out
        and not result.survivors
        and not result.supervision_failed
        else "failed"
    )
    receipt["finished_at"] = _now()


def _safe_load_json(path: Path, *, maximum: int = 2 * 1024 * 1024) -> object:
    try:
        # The state directory is a fresh, wrapper-created root.  Open the
        # manifest relative to its held dirfd so a child cannot swap a
        # symlink/path between the lstat and read phases.
        parent_fd, leaf = _open_parent_dirfd(path.parent, path, "authority manifest")
        try:
            descriptor = os.open(leaf, _FILE_FLAGS, dir_fd=parent_fd)
            try:
                info = os.fstat(descriptor)
                if (not stat.S_ISREG(info.st_mode) or info.st_uid != os.geteuid()
                        or info.st_nlink != 1 or info.st_size > maximum):
                    return None
                data = b""
                while len(data) <= maximum:
                    block = os.read(descriptor, min(1024 * 1024, maximum + 1 - len(data)))
                    if not block:
                        break
                    data += block
                if len(data) > maximum:
                    return None
            finally:
                os.close(descriptor)
        finally:
            os.close(parent_fd)
        return json.loads(data.decode("utf-8"))
    except (OSError, UnicodeDecodeError, json.JSONDecodeError, ValueError, Stage3Error):
        return None


def _safe_load_json_fd(parent_fd: int, leaf: str, *, maximum: int = 2 * 1024 * 1024) -> object:
    output = _read_regular_bounded_fd(parent_fd, leaf, maximum=maximum)
    if output is None:
        return None
    try:
        return json.loads(output[0].decode("utf-8"))
    except (UnicodeDecodeError, json.JSONDecodeError, ValueError):
        return None


def _read_regular_bounded(root: Path, path: Path, *, maximum: int = MAX_CHILD_OUTPUT_BYTES) -> tuple[bytes, os.stat_result] | None:
    """Read one root-contained regular file through a no-follow dirfd."""

    try:
        parent_fd, leaf = _open_parent_dirfd(root, path, "authority output")
        try:
            descriptor = os.open(leaf, _FILE_FLAGS, dir_fd=parent_fd)
            try:
                before = os.fstat(descriptor)
                if (not stat.S_ISREG(before.st_mode) or before.st_uid != os.geteuid()
                        or before.st_nlink != 1 or before.st_size > maximum):
                    return None
                payload = b""
                while len(payload) <= maximum:
                    block = os.read(descriptor, min(1024 * 1024, maximum + 1 - len(payload)))
                    if not block:
                        break
                    payload += block
                if len(payload) > maximum:
                    return None
                after = os.fstat(descriptor)
                if ((before.st_dev, before.st_ino, before.st_nlink, before.st_size)
                        != (after.st_dev, after.st_ino, after.st_nlink, after.st_size)):
                    return None
                return payload, before
            finally:
                os.close(descriptor)
        finally:
            os.close(parent_fd)
    except (OSError, Stage3Error):
        return None


def _open_regular_bounded_snapshot(
    root: Path, path: Path, *, maximum: int, label: str
) -> tuple[bytes, os.stat_result, int, int, str] | None:
    """Read and retain one no-follow file descriptor for a coherent snapshot.

    The caller owns both returned descriptors.  Keeping the file descriptor
    alive lets later structural parsing remain bound to the exact bytes whose
    size and digest were checked, regardless of pathname replacement.
    """

    parent_fd = -1
    descriptor = -1
    try:
        parent_fd, leaf = _open_parent_dirfd(root, path, label)
        descriptor = os.open(leaf, _FILE_FLAGS, dir_fd=parent_fd)
        before = os.fstat(descriptor)
        if (not stat.S_ISREG(before.st_mode) or before.st_uid != os.geteuid()
                or before.st_nlink != 1 or before.st_size > maximum):
            raise Stage3Error(f"{label} is not one bounded owned regular file")
        payload = b""
        while len(payload) <= maximum:
            block = os.read(descriptor, min(1024 * 1024, maximum + 1 - len(payload)))
            if not block:
                break
            payload += block
        after = os.fstat(descriptor)
        if (len(payload) > maximum
                or len(payload) != before.st_size
                or (before.st_dev, before.st_ino, before.st_nlink, before.st_size)
                != (after.st_dev, after.st_ino, after.st_nlink, after.st_size)):
            raise Stage3Error(f"{label} changed while being read")
        result = (payload, before, descriptor, parent_fd, leaf)
        descriptor = -1
        parent_fd = -1
        return result
    except (OSError, Stage3Error):
        return None
    finally:
        if descriptor != -1:
            os.close(descriptor)
        if parent_fd != -1:
            os.close(parent_fd)


def _read_regular_bounded_fd(parent_fd: int, leaf: str, *, maximum: int = MAX_CHILD_OUTPUT_BYTES) -> tuple[bytes, os.stat_result] | None:
    """Read one regular file relative to a parent fd held across a child."""

    descriptor = -1
    try:
        descriptor = os.open(leaf, _FILE_FLAGS, dir_fd=parent_fd)
        before = os.fstat(descriptor)
        if (not stat.S_ISREG(before.st_mode) or before.st_uid != os.geteuid()
                or before.st_nlink != 1 or before.st_size > maximum):
            return None
        payload = b""
        while len(payload) <= maximum:
            block = os.read(descriptor, min(1024 * 1024, maximum + 1 - len(payload)))
            if not block:
                break
            payload += block
        after = os.fstat(descriptor)
        if len(payload) > maximum or (
            (before.st_dev, before.st_ino, before.st_nlink, before.st_size)
            != (after.st_dev, after.st_ino, after.st_nlink, after.st_size)
        ):
            return None
        return payload, before
    except OSError:
        return None
    finally:
        if descriptor != -1:
            os.close(descriptor)


def _write_private_snapshot_fd(parent_fd: int, leaf: str, payload: bytes) -> None:
    """Create one exclusive private snapshot beneath a held directory fd."""

    flags = (os.O_WRONLY | os.O_CREAT | os.O_EXCL | os.O_NOFOLLOW
             | getattr(os, "O_CLOEXEC", 0))
    descriptor = os.open(leaf, flags, 0o600, dir_fd=parent_fd)
    try:
        info = os.fstat(descriptor)
        if not stat.S_ISREG(info.st_mode) or info.st_nlink != 1:
            raise Stage3Error("private snapshot target is not one regular file")
        offset = 0
        while offset < len(payload):
            written = os.write(descriptor, payload[offset:])
            if written <= 0:
                raise Stage3Error("private snapshot write made no progress")
            offset += written
        os.fsync(descriptor)
    finally:
        os.close(descriptor)


def _snapshot_regular_file(
    root: Path, path: Path, label: str
) -> tuple[int, str] | None:
    """Capture one owned regular file's bytes/hash through a held no-follow fd."""

    descriptor = -1
    parent_fd = -1
    try:
        parent_fd, leaf = _open_parent_dirfd(root, path, label)
        descriptor = os.open(leaf, _FILE_FLAGS, dir_fd=parent_fd)
        before = os.fstat(descriptor)
        if (not stat.S_ISREG(before.st_mode)
                or before.st_uid != os.geteuid()
                or before.st_nlink != 1):
            return None
        digest = hashlib.sha256()
        size = 0
        while True:
            block = os.read(descriptor, 1024 * 1024)
            if not block:
                break
            digest.update(block)
            size += len(block)
        after = os.fstat(descriptor)
        if (size != before.st_size
                or (before.st_dev, before.st_ino, before.st_nlink, before.st_size)
                != (after.st_dev, after.st_ino, after.st_nlink, after.st_size)):
            return None
        return size, "sha256:" + digest.hexdigest()
    except (OSError, Stage3Error):
        return None
    finally:
        if descriptor != -1:
            os.close(descriptor)
        if parent_fd != -1:
            os.close(parent_fd)


def _validate_captured_output_contract(
    *,
    root: Path,
    state_dir: Path,
    modules_fd: int,
    output_bytes: bytes,
    expected_stdout_hash: str,
    source_path: str,
    source_size: int,
    source_hash: str,
    proof_hash: str,
    validator: Callable[..., bool] | None = None,
) -> bool:
    """Structurally validate exactly the bytes captured by the held output fd.

    ``run_sh07.output_contract_passed`` accepts a pathname, so passing the
    child's original pathname would reopen a potentially swapped file.  A
    fresh one-link file in the already-held ``modules`` directory binds the
    validator to the captured bytes.  The temp is re-read through that same
    dirfd after validation and deleted relative to it, making replacement or
    pathname swaps fail closed without changing the SH-07 helper.
    """

    if modules_fd < 0 or expected_stdout_hash != "sha256:" + _sha256_bytes(output_bytes):
        return False
    try:
        modules_before = os.fstat(modules_fd)
        modules_path = state_dir / "modules"
        modules_path_before = os.lstat(modules_path)
    except OSError:
        return False
    if (
        not stat.S_ISDIR(modules_before.st_mode)
        or modules_before.st_uid != os.geteuid()
        or not stat.S_ISDIR(modules_path_before.st_mode)
        or modules_path_before.st_uid != os.geteuid()
        or (modules_before.st_dev, modules_before.st_ino)
        != (modules_path_before.st_dev, modules_path_before.st_ino)
    ):
        return False
    temporary_leaf = (
        f".stage3-captured-{os.getpid()}-{uuid.uuid4().hex}.stdout.tmp"
    )
    descriptor = -1
    valid_result = False
    cleanup_failed = False
    try:
        descriptor = os.open(
            temporary_leaf,
            os.O_WRONLY
            | os.O_CREAT
            | os.O_EXCL
            | getattr(os, "O_NOFOLLOW", 0)
            | getattr(os, "O_CLOEXEC", 0),
            0o600,
            dir_fd=modules_fd,
        )
        info = os.fstat(descriptor)
        if (not stat.S_ISREG(info.st_mode)
                or info.st_uid != os.geteuid()
                or info.st_nlink != 1):
            return False
        view = memoryview(output_bytes)
        while view:
            written = os.write(descriptor, view)
            if written <= 0:
                return False
            view = view[written:]
        os.fsync(descriptor)
        after = os.fstat(descriptor)
        if (after.st_nlink != 1
                or after.st_uid != os.geteuid()
                or after.st_size != len(output_bytes)):
            return False
        temporary_path = state_dir / "modules" / temporary_leaf
        temporary_before = os.lstat(temporary_path)
        if (
            not stat.S_ISREG(temporary_before.st_mode)
            or temporary_before.st_uid != os.geteuid()
            or temporary_before.st_nlink != 1
            or (temporary_before.st_dev, temporary_before.st_ino)
            != (after.st_dev, after.st_ino)
        ):
            return False
        validate = validator or _sh07.output_contract_passed
        try:
            valid = bool(validate(
                AUTHORITY_MODULE,
                source_path,
                source_size,
                source_hash,
                proof_hash,
                temporary_path,
                clojure_command="clojure",
                cwd=root,
                expected_stdout_sha256=expected_stdout_hash,
                expected_authority_scope="individual-source-bound-derived",
                expected_coverage_policy="source-bound-derived",
            ))
        except (OSError, TypeError, ValueError):
            valid = False
        modules_after = os.fstat(modules_fd)
        modules_path_after = os.lstat(modules_path)
        temporary_after = os.lstat(temporary_path)
        temporary_held_after = os.fstat(descriptor)
        if (
            # Directory link counts are not stable publication evidence on
            # APFS and may change while this function's own private snapshot
            # exists.  The held descriptor and textual path must instead keep
            # naming the same owned directory inode with the same type/mode.
            (modules_after.st_dev, modules_after.st_ino, modules_after.st_uid,
             stat.S_IFMT(modules_after.st_mode), stat.S_IMODE(modules_after.st_mode))
            != (modules_before.st_dev, modules_before.st_ino, modules_before.st_uid,
                stat.S_IFMT(modules_before.st_mode), stat.S_IMODE(modules_before.st_mode))
            or not stat.S_ISDIR(modules_path_after.st_mode)
            or (modules_path_after.st_dev, modules_path_after.st_ino)
            != (modules_before.st_dev, modules_before.st_ino)
            or (temporary_after.st_dev, temporary_after.st_ino, temporary_after.st_nlink)
            != (temporary_before.st_dev, temporary_before.st_ino, temporary_before.st_nlink)
            or (temporary_held_after.st_dev, temporary_held_after.st_ino,
                temporary_held_after.st_nlink)
            != (temporary_before.st_dev, temporary_before.st_ino,
                temporary_before.st_nlink)
        ):
            valid = False
        observed = _read_regular_bounded_fd(
            modules_fd, temporary_leaf, maximum=MAX_AUTHORITY_OUTPUT_BYTES
        )
        if (not valid or observed is None
                or observed[0] != output_bytes
                or "sha256:" + _sha256_bytes(observed[0]) != expected_stdout_hash):
            valid_result = False
        else:
            valid_result = True
    except OSError:
        valid_result = False
    finally:
        if descriptor != -1:
            os.close(descriptor)
        try:
            os.unlink(temporary_leaf, dir_fd=modules_fd)
        except FileNotFoundError:
            cleanup_failed = True
        except OSError:
            # Failure to remove the validation file is itself not evidence of
            # authority.  Keep that failure visible in the return value.
            cleanup_failed = True
        try:
            os.fsync(modules_fd)
        except OSError:
            pass
    return valid_result and not cleanup_failed


def _recompute_shared_context(root: Path, manifest: Mapping[str, object]) -> Mapping[str, object] | None:
    """Recompute the child-declared runtime/classpath context for acceptance.

    The production path deliberately performs the expensive runtime identity
    probe again.  Tests may replace this function with a deterministic
    provider; no arbitrary command or shell input is accepted through that
    seam.
    """

    shared = manifest.get("shared_context")
    if not isinstance(shared, Mapping):
        return None
    command = shared.get("command")
    catalog = shared.get("authoritative_module_catalog")
    # Never let manifest data choose the command or widen the catalog passed
    # to the context provider.  Resolve the reviewed command locally first;
    # this check performs no subprocess/JVM work.
    try:
        expected_command = [str(value) for value in _sh07.default_base_command()]
    except Exception:
        return None
    if command != expected_command:
        return None
    canonical_module_path = (
        "bootstrap/gravity/src/gravity/compiler/c7_type_checker_engine.gravity"
    )
    if (not isinstance(catalog, Mapping)
            or catalog.get(AUTHORITY_MODULE) != canonical_module_path
            or not all(
                isinstance(key, str)
                and _SAFE_SLUG.fullmatch(key) is not None
                and isinstance(value, str)
                and not Path(value).is_absolute()
                and Path(value).as_posix() == value
                and ".." not in Path(value).parts
                for key, value in catalog.items()
            )):
        return None
    try:
        return _sh07.shared_context_fingerprint(
            root,
            command,
            module_catalog=dict(catalog),
            require_runtime_identity=True,
        )
    except (OSError, ValueError, TypeError, _sh07.CheckpointError, subprocess.TimeoutExpired):
        return None


def _read_one_json(root: Path, path: Path, *, maximum: int = MAX_RECEIPT_BYTES) -> object | None:
    """Read exactly one bounded JSON value through a no-follow descriptor."""

    output = _read_regular_bounded(root, path, maximum=maximum)
    if output is None:
        return None
    payload = output[0]
    try:
        text = payload.decode("utf-8")
        decoder = json.JSONDecoder()
        value, index = decoder.raw_decode(text)
        if text[index:].strip():
            return None
        return value
    except (UnicodeDecodeError, json.JSONDecodeError):
        return None


def _read_one_json_fd(parent_fd: int, leaf: str, *, maximum: int = MAX_RECEIPT_BYTES) -> object | None:
    """Read a report leaf relative to a parent dirfd held across launch."""
    output = _read_regular_bounded_fd(parent_fd, leaf, maximum=maximum)
    if output is None:
        return None
    payload = output[0]
    try:
        text = payload.decode("utf-8")
        decoder = json.JSONDecoder()
        value, index = decoder.raw_decode(text)
        if text[index:].strip():
            return None
        return value
    except (UnicodeDecodeError, json.JSONDecodeError):
        return None


def _is_exact_nonnegative_int(value: object) -> bool:
    """Return true only for a JSON integer, never for a JSON boolean."""

    return type(value) is int and value >= 0


def _is_exact_bool(value: object) -> bool:
    return type(value) is bool


def _validate_runner_report(
    report: object,
    *,
    root: Path,
    report_path: Path,
    batch: str,
    nonce: str,
    check_id: str,
    command_identity_sha256: str,
) -> dict[str, object]:
    """Validate one fixed-runner JSON report before trusting child exit status."""

    if not isinstance(report, Mapping):
        raise Stage3Error("fixed Stage 3 runner report is not an object")
    required = {
        "schema", "stage", "status", "exit-code", "batch-id", "batch-name",
        "selection-order", "executed-vars", "executed", "skipped-tail",
        "skipped-vars", "counts", "cache", "elapsed-ms", "per-var-results",
        "authority", "authoritative?", "cache-authoritative?",
        "fresh-authoritative-run-required?", "report-file", "nonce", "check-id",
        "command-identity-sha256",
    }
    if set(report) != required:
        raise Stage3Error("fixed Stage 3 runner report fields are not exact")
    if report.get("schema") != "gravity/stage3-verification-receipt-v1" or report.get("stage") != "stage3":
        raise Stage3Error("fixed Stage 3 runner report schema mismatch")
    if report.get("batch-id") != batch or report.get("batch-name") != batch:
        raise Stage3Error("fixed Stage 3 runner report batch mismatch")
    if report.get("report-file") != str(report_path):
        raise Stage3Error("fixed Stage 3 runner report path binding mismatch")
    if report.get("nonce") != nonce or report.get("check-id") != check_id:
        raise Stage3Error("fixed Stage 3 runner report invocation binding mismatch")
    if report.get("command-identity-sha256") != command_identity_sha256:
        raise Stage3Error("fixed Stage 3 runner report command identity mismatch")
    if report.get("authority") != "non-authoritative" or report.get("authoritative?") is not False:
        raise Stage3Error("fixed Stage 3 runner report claims authority")
    if (report.get("cache-authoritative?") is not False
            or report.get("fresh-authoritative-run-required?") is not True):
        raise Stage3Error("fixed Stage 3 runner authority metadata is malformed")
    expected_selectors = list(_FIXED_BATCH_SELECTORS.get(batch, ()))
    selection = report.get("selection-order")
    executed = report.get("executed-vars")
    skipped = report.get("skipped-tail")
    if (not isinstance(selection, list) or selection != expected_selectors
            or not isinstance(executed, list) or not isinstance(skipped, list)
            or report.get("executed") != executed
            or report.get("skipped-vars") != skipped
            or executed != selection[: len(executed)]
            or skipped != selection[len(executed):]
            or len(executed) + len(skipped) != len(selection)):
        raise Stage3Error("fixed Stage 3 runner selection/order evidence is invalid")
    counts = report.get("counts")
    if (not isinstance(counts, Mapping)
            or set(counts) - {"type", "test", "pass", "fail", "error"}
            or not {"test", "pass", "fail", "error"}.issubset(counts)
            or counts.get("type") not in (None, "summary")
            or not all(_is_exact_nonnegative_int(counts[field])
                       for field in ("test", "pass", "fail", "error"))):
        raise Stage3Error("fixed Stage 3 runner counts are malformed")
    cache = report.get("cache")
    cache_keys = {
        "sh06-hits", "sh06-misses", "core-hits", "core-misses",
        "verification-hits", "verification-misses",
    }
    if (not isinstance(cache, Mapping)
            or set(cache) != cache_keys
            or not all(_is_exact_nonnegative_int(value) for value in cache.values())):
        raise Stage3Error("fixed Stage 3 runner cache evidence is malformed")
    per_var = report.get("per-var-results")
    if not isinstance(per_var, list) or len(per_var) != len(selection):
        raise Stage3Error("fixed Stage 3 runner per-var results are incomplete")
    if not _is_exact_nonnegative_int(report.get("elapsed-ms")):
        raise Stage3Error("fixed Stage 3 runner elapsed evidence is malformed")
    for index, entry in enumerate(per_var):
        if (not isinstance(entry, Mapping)
                or set(entry) != {
                    "test-var", "selection-index", "status", "counts", "cache",
                    "elapsed-ms", "completed?", "skipped-tail?",
                }
                or entry.get("test-var") != selection[index]
                or not _is_exact_nonnegative_int(entry.get("selection-index"))
                or entry.get("selection-index") != index
                or not _is_exact_bool(entry.get("completed?"))
                or not _is_exact_bool(entry.get("skipped-tail?"))
                or not _is_exact_nonnegative_int(entry.get("elapsed-ms"))):
            raise Stage3Error("fixed Stage 3 runner per-var identity is invalid")
        expected_skipped = index >= len(executed)
        if (entry.get("skipped-tail?") != expected_skipped
                or entry.get("completed?") != (not expected_skipped)):
            raise Stage3Error("fixed Stage 3 runner skipped-tail evidence is invalid")
        entry_counts = entry.get("counts")
        if (not isinstance(entry_counts, Mapping)
                or set(entry_counts) - {"type", "test", "pass", "fail", "error"}
                or not {"test", "pass", "fail", "error"}.issubset(entry_counts)
                or entry_counts.get("type") not in (None, "summary")
                or not all(_is_exact_nonnegative_int(entry_counts[field])
                           for field in ("test", "pass", "fail", "error"))):
            raise Stage3Error("fixed Stage 3 runner per-var counts are malformed")
        entry_cache = entry.get("cache")
        if (not isinstance(entry_cache, Mapping) or set(entry_cache) != cache_keys
                or not all(_is_exact_nonnegative_int(value) for value in entry_cache.values())):
            raise Stage3Error("fixed Stage 3 runner per-var cache is malformed")
        status = entry.get("status")
        expected_status = "skipped" if expected_skipped else (
            "failed" if entry_counts["fail"] or entry_counts["error"] else "passed"
        )
        if status != expected_status:
            raise Stage3Error("fixed Stage 3 runner per-var status is inconsistent")
        if expected_skipped:
            if any(entry_counts[field] != 0 for field in ("test", "pass", "fail", "error")) \
                    or any(entry_cache[field] != 0 for field in cache_keys) \
                    or entry.get("elapsed-ms") != 0:
                raise Stage3Error("fixed Stage 3 runner skipped entry carries execution evidence")
        elif entry_counts["test"] < 1 or entry.get("skipped-tail?"):
            raise Stage3Error("fixed Stage 3 runner completed entry is empty or skipped")
    aggregate = {
        field: sum(int(entry["counts"][field]) for entry in per_var)
        for field in ("test", "pass", "fail", "error")
    }
    if any(counts[field] != aggregate[field] for field in aggregate):
        raise Stage3Error("fixed Stage 3 runner aggregate counts disagree with per-var summaries")
    aggregate_cache = {
        field: sum(int(entry["cache"][field]) for entry in per_var)
        for field in cache_keys
    }
    if any(cache[field] != aggregate_cache[field] for field in cache_keys):
        raise Stage3Error("fixed Stage 3 runner aggregate cache disagrees with per-var summaries")
    status = report.get("status")
    if status not in {"passed", "failed"}:
        raise Stage3Error("fixed Stage 3 runner status is malformed")
    if type(report.get("exit-code")) is not int or report.get("exit-code") not in {0, 1}:
        raise Stage3Error("fixed Stage 3 runner exit evidence is malformed")
    if status == "passed" and (
        len(executed) != len(selection)
        or skipped
        or counts["fail"]
        or counts["error"]
        or report.get("exit-code") != 0
    ):
        raise Stage3Error("fixed Stage 3 runner reported an inconsistent pass")
    if status == "failed":
        # Fail-fast reports may stop at the final failing executed var and
        # then carry the exact unexecuted suffix as skipped.  Any other
        # ordering (a failure before a later executed var, multiple failures,
        # or a skipped suffix with no failure) is impossible evidence.
        failed_indices = [
            index for index in range(len(executed))
            if per_var[index].get("status") == "failed"
        ]
        if (report.get("exit-code") != 1
                or not executed
                or len(failed_indices) != 1
                or failed_indices[0] != len(executed) - 1
                or any(per_var[index].get("status") != "passed"
                       for index in range(len(executed) - 1))
                or per_var[len(executed) - 1].get("status") != "failed"
                or (per_var[len(executed) - 1]["counts"]["fail"] == 0
                    and per_var[len(executed) - 1]["counts"]["error"] == 0)
                or counts["fail"] == 0 and counts["error"] == 0):
            raise Stage3Error("fixed Stage 3 runner failure status is not one final fail-fast failure")
    return dict(report)


def _authority_manifest_valid(
    root: Path,
    state_dir: Path,
    manifest: object,
    *,
    state_fd: int | None = None,
    modules_fd: int | None = None,
    check_output_contract: bool = True,
    manifest_sha256: str | None = None,
) -> tuple[bool, dict[str, object]]:
    """Validate the exact C7 completed source-bound-derived receipt shape."""

    reasons: list[str] = []
    if not isinstance(manifest, Mapping):
        return False, {"errors": ["manifest is not an object"]}
    if manifest.get("schema") != _sh07.SCHEMA:
        reasons.append("wrong schema")
    if manifest.get("tool_version") != _sh07.TOOL_VERSION:
        reasons.append("child tool version is not the reviewed version")
    if manifest.get("fingerprint_policy_version") != _sh07.FINGERPRINT_POLICY_VERSION:
        reasons.append("child fingerprint policy version is not the reviewed version")
    if manifest.get("state") != "completed":
        reasons.append("state is not completed")
    if manifest.get("selected_modules") != [AUTHORITY_MODULE]:
        reasons.append("selected module is not exactly c7-types")
    if manifest.get("aggregate_authoritative") is not False:
        reasons.append("aggregate authority must be false")
    if manifest.get("resumed_modules") != []:
        reasons.append("authority run resumed a module")
    lock_path = manifest.get("lock_path")
    if lock_path != CANONICAL_LOCK_TEXT:
        reasons.append("child manifest lock path is not canonical")
    if manifest.get("lock_mode") != "0600":
        reasons.append("child manifest lock mode is not 0600")
    for lifecycle in ("lock_acquired", "lock_validated", "lock_released"):
        if manifest.get(lifecycle) is not True:
            reasons.append(f"child manifest lease lifecycle lacks {lifecycle}")
    if manifest.get("authority_scope") not in {
        "individual-existing-runner-outputs-only",
        "individual-source-bound-derived",
    }:
        reasons.append("authority scope is not an individual source-bound scope")
    shared_fingerprint = manifest.get("shared_context_fingerprint")
    if not isinstance(shared_fingerprint, str) or not _SHA256.fullmatch(shared_fingerprint):
        reasons.append("shared context fingerprint is missing or malformed")
    if manifest.get("shared_context_fingerprint_after") != shared_fingerprint:
        reasons.append("shared context changed after module completion")
    current_shared = _recompute_shared_context(root, manifest)
    if (not isinstance(current_shared, Mapping)
            or current_shared.get("sha256") != shared_fingerprint):
        reasons.append("current runtime/classpath context does not match child manifest")
    modules = manifest.get("modules")
    if not isinstance(modules, Mapping) or set(modules) != {AUTHORITY_MODULE}:
        reasons.append("child manifest module records are not exact")
        record: Mapping[str, object] = {}
    else:
        raw = modules.get(AUTHORITY_MODULE)
        record = raw if isinstance(raw, Mapping) else {}
    if record.get("state") != "passed":
        reasons.append("c7-types module did not pass")
    if record.get("exit_code") != 0 or record.get("raw_child_exit_code") != 0:
        reasons.append("c7-types child exit was not zero")
    if record.get("timed_out") is not False or record.get("context_stable") is not True:
        reasons.append("c7-types context is not stable")
    if record.get("output_contract_checked") is not True:
        reasons.append("c7-types output contract was not checked")
    command = record.get("command")
    try:
        expected_child_command = [
            *[str(value) for value in _sh07.default_base_command()],
            "--fresh",
            AUTHORITY_MODULE,
        ]
    except Exception:
        expected_child_command = None
    if (not isinstance(command, list)
            or expected_child_command is None
            or command != expected_child_command):
        reasons.append("c7-types was not run as one fresh module")
    module_context = record.get("module_context")
    if (not isinstance(module_context, Mapping)
            or record.get("module_context_fingerprint") != module_context.get("sha256")):
        reasons.append("module context fingerprint is not bound to its record")
    expected_contract = root / _sh07.PROOF_CONTRACT_RELATIVE
    proof_hash = record.get("proof_contract_sha256")
    if not isinstance(proof_hash, str) or not _SHA256.fullmatch(proof_hash):
        reasons.append("proof contract hash is missing or malformed")
    else:
        try:
            if proof_hash != _sh07.sha256_file(expected_contract):
                reasons.append("proof contract hash mismatch")
        except OSError:
            reasons.append("proof contract is unavailable")
    source_descriptor = -1
    source_parent_fd = -1
    source_leaf = ""
    source_bytes: bytes | None = None
    source_info: os.stat_result | None = None
    context = record.get("module_context")
    if not isinstance(context, Mapping) or context.get("module") != AUTHORITY_MODULE:
        reasons.append("module context does not bind c7-types")
    else:
        files = context.get("files")
        if not isinstance(files, list) or not files or not isinstance(files[0], Mapping):
            reasons.append("module context lacks source binding")
        else:
            source_entry = files[0]
            if source_entry.get("path") != "bootstrap/gravity/src/gravity/compiler/c7_type_checker_engine.gravity":
                reasons.append("module context source path is not c7-types")
            source_path = root / str(source_entry.get("path", ""))
            source_snapshot = _open_regular_bounded_snapshot(
                root,
                source_path,
                maximum=MAX_AUTHORITY_SOURCE_BYTES,
                label="authority source",
            )
            if source_snapshot is None:
                reasons.append("module context source binding is unavailable")
            else:
                (source_bytes, source_info, source_descriptor,
                 source_parent_fd, source_leaf) = source_snapshot
                if (source_entry.get("size") != len(source_bytes)
                        or source_entry.get("sha256") != "sha256:" + _sha256_bytes(source_bytes)):
                    reasons.append("module context source binding is stale")
        if not isinstance(context.get("sha256"), str) or not context.get("sha256"):
            reasons.append("module context fingerprint is missing")
    stdout_relative = record.get("stdout_path")
    if stdout_relative != f"modules/{AUTHORITY_MODULE}.stdout.log":
        reasons.append("stdout path is not canonical")
        stdout_path = state_dir / "invalid"
    else:
        stdout_path = state_dir / str(stdout_relative)
    owned_modules_fd = modules_fd is None
    if owned_modules_fd:
        modules_fd = -1
    if owned_modules_fd and state_fd is not None:
        try:
            modules_fd = os.open("modules", _DIR_FLAGS, dir_fd=state_fd)
            modules_info = os.fstat(modules_fd)
            if not stat.S_ISDIR(modules_info.st_mode) or modules_info.st_uid != os.geteuid():
                os.close(modules_fd)
                modules_fd = -1
        except OSError:
            modules_fd = -1
    output = (
        _read_regular_bounded_fd(modules_fd, f"{AUTHORITY_MODULE}.stdout.log", maximum=MAX_AUTHORITY_OUTPUT_BYTES)
        if modules_fd != -1 else _read_regular_bounded(
            state_dir, stdout_path, maximum=MAX_AUTHORITY_OUTPUT_BYTES
        )
    )
    if output is None:
        reasons.append("stdout receipt is not a current regular child")
    else:
        output_bytes, _output_info = output
        expected_stdout_hash = record.get("stdout_sha256")
        if (not isinstance(expected_stdout_hash, str)
                or expected_stdout_hash != "sha256:" + _sha256_bytes(output_bytes)):
            reasons.append("stdout receipt hash mismatch")
        # ``output_contract_checked`` is set only after the authoritative
        # child structurally parses the exact EDN output and verifies the
        # source-bound census.  Validate the bytes captured through the held
        # modules dirfd; never reopen the child's pathname after the snapshot.
        if check_output_contract:
            try:
                source_entry = context["files"][0]  # type: ignore[index]
                if source_bytes is None or source_info is None or source_descriptor == -1:
                    raise ValueError("authority source snapshot is unavailable")
                expected_stdout_hash = "sha256:" + _sha256_bytes(output_bytes)
                if not _validate_captured_output_contract(
                    root=root,
                    state_dir=state_dir,
                    modules_fd=modules_fd,
                    output_bytes=output_bytes,
                    expected_stdout_hash=expected_stdout_hash,
                    source_path=str(source_entry["path"]),
                    source_size=len(source_bytes),
                    source_hash="sha256:" + _sha256_bytes(source_bytes),
                    proof_hash=str(record.get("proof_contract_sha256")),
                ):
                    reasons.append("authoritative output contract did not structurally validate")
            except (KeyError, TypeError, ValueError, OSError):
                reasons.append("authoritative output contract inputs are malformed")
    stderr_relative = record.get("stderr_path")
    if stderr_relative != f"modules/{AUTHORITY_MODULE}.stderr.log":
        reasons.append("stderr path is not canonical")
    else:
        stderr_output = (
            _read_regular_bounded_fd(
                modules_fd, f"{AUTHORITY_MODULE}.stderr.log", maximum=MAX_AUTHORITY_OUTPUT_BYTES
            )
            if modules_fd != -1 else _read_regular_bounded(
                state_dir, state_dir / str(stderr_relative), maximum=MAX_AUTHORITY_OUTPUT_BYTES
            )
        )
        expected_stderr_hash = record.get("stderr_sha256")
        if (stderr_output is None or not isinstance(expected_stderr_hash, str)
                or expected_stderr_hash != "sha256:" + _sha256_bytes(stderr_output[0])):
            reasons.append("stderr receipt hash mismatch")
    if owned_modules_fd and modules_fd != -1:
        os.close(modules_fd)
    if source_descriptor != -1:
        try:
            held_after = os.fstat(source_descriptor)
            named_after = os.stat(
                source_leaf, dir_fd=source_parent_fd, follow_symlinks=False
            )
            if (source_info is None
                    or (held_after.st_dev, held_after.st_ino, held_after.st_nlink,
                        held_after.st_size)
                    != (source_info.st_dev, source_info.st_ino, source_info.st_nlink,
                        source_info.st_size)
                    or (named_after.st_dev, named_after.st_ino)
                    != (source_info.st_dev, source_info.st_ino)):
                reasons.append("authority source pathname no longer names the held snapshot")
        except OSError:
            reasons.append("authority source snapshot could not be revalidated")
        finally:
            os.close(source_descriptor)
            os.close(source_parent_fd)
    evidence = {
        "state_dir": str(state_dir),
        "manifest_schema": manifest.get("schema"),
        "tool_version": manifest.get("tool_version"),
        "fingerprint_policy_version": manifest.get("fingerprint_policy_version"),
        "selected_modules": manifest.get("selected_modules"),
        "state": manifest.get("state"),
        "aggregate_authoritative": manifest.get("aggregate_authoritative"),
        "lock_path": manifest.get("lock_path"),
        "lock_mode": manifest.get("lock_mode"),
        "lock_acquired": manifest.get("lock_acquired"),
        "lock_validated": manifest.get("lock_validated"),
        "lock_released": manifest.get("lock_released"),
        "shared_context_fingerprint": shared_fingerprint,
        "shared_context_fingerprint_after": manifest.get("shared_context_fingerprint_after"),
        "manifest_sha256": manifest_sha256,
        "current_shared_context_fingerprint": (
            current_shared.get("sha256") if isinstance(current_shared, Mapping) else None
        ),
        "authority_scope": "individual-source-bound-derived",
        "module": AUTHORITY_MODULE,
        "module_record": {
            "state": record.get("state"),
            "stdout_path": stdout_relative,
            "stdout_sha256": record.get("stdout_sha256"),
            "module_context_fingerprint": record.get("module_context_fingerprint"),
            "proof_contract_sha256": record.get("proof_contract_sha256"),
            "output_contract_checked": record.get("output_contract_checked"),
        },
        "errors": reasons,
    }
    return not reasons, evidence


def _run_pure(
    *,
    root: Path,
    receipt: dict[str, object],
    batch: str,
    nonce: str,
    check_id: str,
    command_identity_sha256: str,
    runner_report_path: Path,
    launcher: Launcher,
    timeout_seconds: float,
) -> ChildResult:
    command = batch_command(
        batch,
        report_path=runner_report_path,
        nonce=nonce,
        check_id=check_id,
        command_identity_sha256=command_identity_sha256,
    )
    env = os.environ.copy()
    result = ChildResult(75, "", "Stage 3 lock unavailable")
    report_parent_fd = -1
    report_leaf = runner_report_path.name
    try:
        report_parent_fd, report_leaf = _open_parent_dirfd(root, runner_report_path, "runner report")
        try:
            os.stat(report_leaf, dir_fd=report_parent_fd, follow_symlinks=False)
        except FileNotFoundError:
            pass
        else:
            raise Stage3Error("fixed runner report target is pre-existing")
        with _sh07.shared_lock_lease(CANONICAL_LOCK) as (handle, lease):
            lock = receipt["lock"]
            assert isinstance(lock, dict)
            lock.update(lease)
            lock["path"] = CANONICAL_LOCK_TEXT
            lock["canonical_path"] = CANONICAL_LOCK_TEXT
            lock["acquired"] = True
            lock["identity_before"] = _lock_identity(handle)
            result = _normalise_child_result(launcher(command, root, env, timeout_seconds))
            runner_report = _read_one_json_fd(report_parent_fd, report_leaf)
            if runner_report is None:
                result = dataclasses.replace(
                    result,
                    supervision_failed=True,
                    stderr=(result.stderr + "\nmissing or malformed fixed runner report").strip(),
                )
            else:
                try:
                    validated_report = _validate_runner_report(
                        runner_report,
                        root=root,
                        report_path=runner_report_path,
                        batch=batch,
                        nonce=nonce,
                        check_id=check_id,
                        command_identity_sha256=command_identity_sha256,
                    )
                    if validated_report.get("exit-code") != result.returncode:
                        raise Stage3Error("fixed runner report exit does not match child exit")
                    if result.returncode == 0 and validated_report.get("status") != "passed":
                        raise Stage3Error("fixed runner child exit zero lacks a structural pass report")
                    if result.returncode != 0 and validated_report.get("status") == "passed":
                        raise Stage3Error("fixed runner pass report disagrees with child failure")
                    receipt["runner_report"] = validated_report
                    receipt["runner_report_path"] = str(runner_report_path)
                except Stage3Error as report_error:
                    result = dataclasses.replace(
                        result,
                        supervision_failed=True,
                        stderr=(result.stderr + "\n" + str(report_error)).strip(),
                    )
            receipt["child"] = _child_record(result, command)
            handle.validate()
            lock["validated"] = True
            lock["identity_after"] = _lock_identity(handle)
    except Exception as exc:
        # Never allow a child exit-0 to survive a failed lease acquisition or
        # final-handle validation.  The receipt remains useful diagnostics,
        # but its status is an explicit infrastructure failure.
        result = ChildResult(75, result.stdout, f"{result.stderr}\n{exc}".strip(), supervision_failed=True)
        receipt["error"] = str(exc)
        receipt["child"] = _child_record(result, command)
    finally:
        lock = receipt["lock"]
        assert isinstance(lock, dict)
        if lock.get("acquired") is True:
            # The context manager's finally block releases the flock even on
            # validation/child failure.  ``validated`` remains false when the
            # final handle check failed, so the verifier can distinguish a
            # released but invalid lease from a truthful pass.
            lock["released"] = True
        if report_parent_fd != -1:
            os.close(report_parent_fd)
    return result


def _run_proof_candidate(
    *,
    root: Path,
    receipt: dict[str, object],
    nonce: str,
    launcher: Launcher,
    timeout_seconds: float,
) -> ChildResult:
    authority_parent = _ensure_directory(root, root / ".cpcache" / "stage3-authority", "state")
    state_dir = authority_parent / nonce
    _ensure_directory(root, state_dir, "state", mode=0o700, fresh=True)
    # Pre-create and hold the private modules directory before launching the
    # child.  A child-side rename/replacement of ``state/modules`` therefore
    # cannot redirect the report bytes consumed after exit.
    _ensure_directory(root, state_dir / "modules", "state modules", mode=0o700, fresh=True)
    command = [
        "python3",
        AUTHORITY_CHILD_SCRIPT,
        "--module",
        AUTHORITY_MODULE,
        "--state-dir",
        str(state_dir),
        "--lock",
        CANONICAL_LOCK_TEXT,
        "--no-resume",
        "--cwd",
        str(root),
    ]
    env = os.environ.copy()
    state_parent_fd, state_leaf = _open_parent_dirfd(root, state_dir, "state")
    state_fd = -1
    modules_fd = -1
    try:
        state_fd = os.open(state_leaf, _DIR_FLAGS, dir_fd=state_parent_fd)
        modules_fd = os.open("modules", _DIR_FLAGS, dir_fd=state_fd)
        modules_info = os.fstat(modules_fd)
        if (not stat.S_ISDIR(modules_info.st_mode)
                or modules_info.st_uid != os.geteuid()
                or stat.S_IMODE(modules_info.st_mode) != 0o700):
            raise Stage3Error("authority modules directory is not private and owned")
    finally:
        os.close(state_parent_fd)
    try:
        result = _normalise_child_result(launcher(command, root, env, timeout_seconds))
        receipt["child"] = _child_record(result, command)
        receipt["state_dir"] = str(state_dir)
        manifest_snapshot = _read_regular_bounded_fd(
            state_fd, "manifest.json", maximum=MAX_AUTHORITY_MANIFEST_BYTES
        )
        if manifest_snapshot is not None:
            try:
                manifest = json.loads(manifest_snapshot[0].decode("utf-8"))
            except (UnicodeDecodeError, json.JSONDecodeError):
                manifest = None
            receipt["candidate_manifest_path"] = str(state_dir / "manifest.json")
            receipt["candidate_manifest_sha256"] = "sha256:" + _sha256_bytes(
                manifest_snapshot[0]
            )
        else:
            manifest = None
        manifest_hash = receipt.get("candidate_manifest_sha256")
        valid, evidence = _authority_manifest_valid(
            root,
            state_dir,
            manifest,
            state_fd=state_fd,
            modules_fd=modules_fd,
            manifest_sha256=(str(manifest_hash) if isinstance(manifest_hash, str) else None),
        )
    finally:
        if modules_fd != -1:
            os.close(modules_fd)
        if state_fd != -1:
            os.close(state_fd)
    receipt["authority_evidence"] = evidence
    # The wrapper receipt itself is the hand-off artifact for a candidate.
    # Its final digest is recorded by the parent verifier after publication;
    # retaining the root-contained path and check identity here avoids any
    # reusable/latest-file discovery in reviewed-attestation mode.
    receipt["candidate_receipt_path"] = str(receipt.get("receipt_path"))
    receipt["candidate_check_id"] = receipt.get("check_id")
    lock = receipt["lock"]
    assert isinstance(lock, dict)
    # The authoritative child owns the lease; a completed manifest with its
    # exact lock path is the child-side lease evidence.  The wrapper never
    # acquires this lock itself.
    if isinstance(manifest, Mapping) and manifest.get("lock_path") == CANONICAL_LOCK_TEXT \
            and all(manifest.get(field) is True for field in (
        "lock_acquired", "lock_validated", "lock_released"
    )):
        lock.update({
            "path": CANONICAL_LOCK_TEXT,
            "canonical_path": CANONICAL_LOCK_TEXT,
            "acquired": True,
            "validated": True,
            "released": True,
            "owner": "authoritative-child",
        })
    if (valid and result.returncode == 0 and not result.timed_out
            and not result.survivors and not result.supervision_failed):
        # A source-bound-derived proof is only a candidate.  Promotion needs
        # a separate reviewed attestation bound to this exact current output;
        # the wrapper must never manufacture or infer that reviewer decision.
        receipt["non_authoritative"] = True
        receipt["authority"] = "none"
        receipt.update({
            "authority_scope": "none",
            "evidence_kind": "source-bound-derived-proof-candidate",
            "proof_candidate": True,
            "proof_candidate_status": "passed",
            "candidate_scope": "individual-source-bound-derived",
            "attestation_required": True,
            "attestation_present": False,
            "aggregate_authoritative": False,
            "release_authoritative": False,
        })
    else:
        receipt["authority"] = "none"
        receipt["non_authoritative"] = True
        receipt.update({
            "authority_scope": "none",
            "proof_candidate": False,
            "attestation_present": False,
            "aggregate_authoritative": False,
            "release_authoritative": False,
        })
        if not valid:
            result = dataclasses.replace(
                result,
                returncode=75,
                supervision_failed=True,
                stderr=(result.stderr + "\ninvalid authoritative manifest/receipt").strip(),
            )
            receipt["child"] = _child_record(result, command)
    return result


def _run_reviewed_attestation_locked(
    *,
    root: Path,
    receipt: dict[str, object],
    nonce: str,
    check_id: str,
    candidate_check_id: str | None,
    candidate_receipt_path: Path | None,
    candidate_receipt_sha256: str | None,
    candidate_state_dir: Path | None,
    candidate_manifest_path: Path | None,
    candidate_manifest_sha256: str | None,
    attestation_path: Path | None,
    attestation_sha256: str | None,
) -> ChildResult:
    """Promote one exact, human-reviewed proof candidate.

    This mode launches no child and acquires no SH-07 lease.  Every input is
    supplied explicitly by the verifier and read through root-contained
    no-follow descriptors; a stale or malformed candidate is infrastructure
    failure, never an implicit authority grant.
    """

    command = ["python3", "tools/run_stage3_verification.py"]
    rss_kwargs = {
        "observed_peak_process_tree_rss_bytes": 0,
        "rss_sampling_cadence_seconds": 1.0,
        "rss_sampling_contract": "run_with_heartbeat.process_tree_metrics-v1",
        "rss_sampling_limitation": "no child launched; candidate observation may miss between-sample spikes",
    }

    def fail(message: str) -> ChildResult:
        receipt["error"] = message
        result = ChildResult(
            75,
            "",
            message,
            cleanup={"terminal_safe": True, "output_complete": True},
            supervision_failed=True,
            **rss_kwargs,
        )
        receipt["proof_candidate"] = False
        receipt["attestation_required"] = True
        receipt["authority"] = "none"
        receipt["non_authoritative"] = True
        receipt["child"] = _child_record(result, command)
        return result

    if (candidate_check_id is None
            or candidate_receipt_path is None or candidate_receipt_sha256 is None
            or candidate_state_dir is None
            or candidate_manifest_sha256 is None or attestation_path is None
            or attestation_sha256 is None):
        return fail("reviewed-attestation requires explicit candidate and attestation paths/hashes")
    for label, value in (
        ("candidate receipt", candidate_receipt_sha256),
        ("candidate manifest", candidate_manifest_sha256),
        ("attestation", attestation_sha256),
    ):
        if _SHA256.fullmatch(value) is None:
            return fail(f"{label} hash is malformed")
    try:
        candidate_receipt_path = _safe_root_relative(root, candidate_receipt_path, "candidate receipt")
        candidate_state_dir = _safe_root_relative(root, candidate_state_dir, "candidate state")
        derived_manifest_path = candidate_state_dir / "manifest.json"
        if candidate_manifest_path is not None:
            candidate_manifest_path = _safe_root_relative(
                root, candidate_manifest_path, "candidate manifest"
            )
        if candidate_manifest_path is not None and candidate_manifest_path != derived_manifest_path:
            return fail("candidate manifest path must be candidate_state_dir/manifest.json")
        candidate_manifest_path = derived_manifest_path
        attestation_path = _safe_root_relative(root, attestation_path, "attestation")
    except Stage3Error as error:
        return fail(str(error))
    receipt["candidate_receipt_path"] = str(candidate_receipt_path)
    receipt["candidate_state_dir"] = str(candidate_state_dir)
    receipt["attestation_path"] = str(attestation_path)
    receipt["attestation_sha256"] = attestation_sha256
    candidate_snapshot = _read_regular_bounded(
        root, candidate_receipt_path, maximum=MAX_RECEIPT_BYTES
    )
    if candidate_snapshot is None:
        return fail("candidate receipt is not one bounded regular file")
    candidate_bytes, _candidate_info = candidate_snapshot
    if "sha256:" + _sha256_bytes(candidate_bytes) != candidate_receipt_sha256:
        return fail("candidate receipt hash mismatch")
    try:
        decoder = json.JSONDecoder()
        candidate, end = decoder.raw_decode(candidate_bytes.decode("utf-8"))
        if candidate_bytes.decode("utf-8")[end:].strip():
            return fail("candidate receipt contains trailing data")
    except (UnicodeDecodeError, json.JSONDecodeError):
        return fail("candidate receipt is malformed JSON")
    if (not isinstance(candidate, Mapping)
            or candidate.get("schema") != SCHEMA
            or candidate.get("mode") != MODE_PROOF_CANDIDATE
            or candidate.get("proof_candidate") is not True
            or candidate.get("status") != "passed"
            or candidate.get("authority") != "none"
            or candidate.get("non_authoritative") is not True
            or candidate.get("attestation_required") is not True
            or candidate.get("check_id") != candidate_check_id
            or candidate_check_id == check_id):
        return fail("candidate receipt is not an exact passed proof candidate")
    evidence = candidate.get("authority_evidence")
    if not isinstance(evidence, Mapping):
        return fail("candidate receipt lacks structural authority evidence")
    if evidence.get("state_dir") != str(candidate_state_dir):
        return fail("candidate state directory does not match candidate receipt")
    try:
        state_parent_fd, state_leaf = _open_parent_dirfd(root, candidate_state_dir, "candidate state")
        state_fd = os.open(state_leaf, _DIR_FLAGS, dir_fd=state_parent_fd)
        os.close(state_parent_fd)
        state_info = os.fstat(state_fd)
        if (not stat.S_ISDIR(state_info.st_mode) or state_info.st_uid != os.geteuid()
                or stat.S_IMODE(state_info.st_mode) != 0o700):
            os.close(state_fd)
            return fail("candidate state directory is not private and owned")
        # The manifest path is derived from the held candidate state fd.  Do
        # not reopen a caller-provided pathname after validation; a parent
        # swap must not redirect the bytes that are hashed and parsed.
        manifest_snapshot = _read_regular_bounded_fd(
            state_fd, "manifest.json", maximum=MAX_RECEIPT_BYTES
        )
        if manifest_snapshot is None:
            os.close(state_fd)
            return fail("candidate manifest is not one bounded regular file")
        manifest_bytes, _manifest_info = manifest_snapshot
        if "sha256:" + _sha256_bytes(manifest_bytes) != candidate_manifest_sha256:
            os.close(state_fd)
            return fail("candidate manifest hash mismatch")
        try:
            manifest_text = manifest_bytes.decode("utf-8")
            manifest_decoder = json.JSONDecoder()
            manifest, manifest_end = manifest_decoder.raw_decode(manifest_text)
            if manifest_text[manifest_end:].strip():
                raise ValueError("candidate manifest contains trailing data")
        except (UnicodeDecodeError, json.JSONDecodeError, ValueError):
            os.close(state_fd)
            return fail("candidate manifest is malformed")
        if not isinstance(manifest, Mapping):
            os.close(state_fd)
            return fail("candidate manifest is malformed")
        modules_fd = os.open("modules", _DIR_FLAGS, dir_fd=state_fd)
        valid, manifest_evidence = _authority_manifest_valid(
            root,
            candidate_state_dir,
            manifest,
            state_fd=state_fd,
            modules_fd=modules_fd,
            check_output_contract=False,
            manifest_sha256=candidate_manifest_sha256,
        )
        if not valid:
            os.close(modules_fd)
            os.close(state_fd)
            return fail("candidate manifest no longer validates: " + "; ".join(
                str(item) for item in manifest_evidence.get("errors", [])
            ))
        record = manifest.get("modules", {}).get(AUTHORITY_MODULE)  # type: ignore[union-attr]
        if not isinstance(record, Mapping):
            os.close(modules_fd)
            os.close(state_fd)
            return fail("candidate manifest c7-types record is missing")
        stdout_relative = record.get("stdout_path")
        if stdout_relative != f"modules/{AUTHORITY_MODULE}.stdout.log":
            os.close(modules_fd)
            os.close(state_fd)
            return fail("candidate stdout path is not canonical")
        stdout_path = candidate_state_dir / str(stdout_relative)
        output = _read_regular_bounded_fd(
            modules_fd, f"{AUTHORITY_MODULE}.stdout.log", maximum=MAX_AUTHORITY_OUTPUT_BYTES
        )
        expected_stdout_hash = record.get("stdout_sha256")
        if (output is None or not isinstance(expected_stdout_hash, str)
                or expected_stdout_hash != "sha256:" + _sha256_bytes(output[0])):
            os.close(modules_fd)
            os.close(state_fd)
            return fail("candidate stdout hash is not stable")
        attestation_snapshot = _read_regular_bounded(
            root, attestation_path, maximum=MAX_RECEIPT_BYTES
        )
        if attestation_snapshot is None:
            os.close(modules_fd)
            os.close(state_fd)
            return fail("reviewed attestation is not one bounded regular file")
        attestation_bytes, _attestation_info = attestation_snapshot
        if "sha256:" + _sha256_bytes(attestation_bytes) != attestation_sha256:
            os.close(modules_fd)
            os.close(state_fd)
            return fail("reviewed attestation hash mismatch")
        try:
            attestation = json.loads(attestation_bytes.decode("utf-8"))
        except (UnicodeDecodeError, json.JSONDecodeError):
            os.close(modules_fd)
            os.close(state_fd)
            return fail("reviewed attestation is malformed JSON")
        proof_hash = record.get("proof_contract_sha256")
        context = manifest.get("modules", {}).get(AUTHORITY_MODULE, {}).get("module_context")
        source_entry = None
        if isinstance(context, Mapping):
            files = context.get("files")
            if isinstance(files, list) and files and isinstance(files[0], Mapping):
                source_entry = files[0]
        source_path_value = source_entry.get("path") if isinstance(source_entry, Mapping) else None
        source_snapshot = (
            _read_regular_bounded(
                root,
                root / str(source_path_value),
                maximum=MAX_AUTHORITY_SOURCE_BYTES,
            )
            if isinstance(source_path_value, str) else None
        )
        if source_snapshot is None or not isinstance(proof_hash, str):
            os.close(modules_fd)
            os.close(state_fd)
            return fail("candidate source binding is unavailable")
        source_bytes, _source_info = source_snapshot
        source_leaf = f".stage3-attest-source-{nonce}"
        stdout_leaf = f".stage3-attest-stdout-{nonce}"
        _write_private_snapshot_fd(modules_fd, source_leaf, source_bytes)
        _write_private_snapshot_fd(modules_fd, stdout_leaf, output[0])
        source_snapshot_path = candidate_state_dir / "modules" / source_leaf
        stdout_snapshot_path = candidate_state_dir / "modules" / stdout_leaf
        try:
            attestation_valid = _sh07.validate_source_bound_attestation(
                root,
                AUTHORITY_MODULE,
                stdout_path,
                attestation,
                expected_proof_contract_sha256=proof_hash,
                expected_stdout_sha256=expected_stdout_hash,
                expected_source_path=str(source_path_value),
                expected_source_byte_count=(
                    int(source_entry.get("size"))
                    if isinstance(source_entry, Mapping) else None
                ),
                expected_source_bytes_sha256=(
                    str(source_entry.get("sha256"))
                    if isinstance(source_entry, Mapping) else None
                ),
                expected_authority_scope="individual-source-bound-derived",
                expected_coverage_policy="source-bound-derived",
                source_snapshot_path=source_snapshot_path,
                stdout_snapshot_path=stdout_snapshot_path,
            )
        finally:
            for leaf in (source_leaf, stdout_leaf):
                try:
                    os.unlink(leaf, dir_fd=modules_fd)
                except FileNotFoundError:
                    pass
        if not attestation_valid:
            os.close(modules_fd)
            os.close(state_fd)
            return fail("reviewed attestation failed source-bound validation")
        output_after = _read_regular_bounded_fd(
            modules_fd, f"{AUTHORITY_MODULE}.stdout.log", maximum=MAX_AUTHORITY_OUTPUT_BYTES
        )
        if output_after is None or output_after[0] != output[0]:
            os.close(modules_fd)
            os.close(state_fd)
            return fail("candidate stdout changed during attestation validation")
        os.close(modules_fd)
        os.close(state_fd)
    except (OSError, Stage3Error, TypeError, ValueError) as error:
        try:
            os.close(modules_fd)
        except (UnboundLocalError, OSError):
            pass
        try:
            os.close(state_fd)
        except (UnboundLocalError, OSError):
            pass
        return fail(f"reviewed candidate validation failed: {error}")
    receipt["candidate_manifest_path"] = str(candidate_manifest_path)
    receipt["candidate_manifest_sha256"] = candidate_manifest_sha256
    receipt["candidate_receipt_sha256"] = candidate_receipt_sha256
    receipt["candidate_check_id"] = candidate_check_id
    receipt["authority_evidence"] = manifest_evidence
    receipt["authority"] = "scoped-proof-authority"
    receipt["authority_scope"] = "individual-source-bound-derived"
    receipt["non_authoritative"] = False
    receipt["proof_candidate"] = False
    receipt["attestation_required"] = False
    receipt["attestation_present"] = True
    receipt["child"] = {
        "command": command,
        "returncode": 0,
        "timed_out": False,
        "stdout": "",
        "stderr": "",
        "survivors": [],
        "cleanup": {"terminal_safe": True, "output_complete": True},
        "supervision_failed": False,
        **rss_kwargs,
    }
    return ChildResult(0, **rss_kwargs)


def _run_reviewed_attestation(
    *,
    root: Path,
    receipt: dict[str, object],
    nonce: str,
    check_id: str,
    candidate_check_id: str | None,
    candidate_receipt_path: Path | None,
    candidate_receipt_sha256: str | None,
    candidate_state_dir: Path | None,
    candidate_manifest_path: Path | None,
    candidate_manifest_sha256: str | None,
    attestation_path: Path | None,
    attestation_sha256: str | None,
) -> ChildResult:
    """Acquire one current canonical lease while promoting a candidate."""

    command = ["python3", "tools/run_stage3_verification.py"]
    rss_kwargs = {
        "observed_peak_process_tree_rss_bytes": 0,
        "rss_sampling_cadence_seconds": 1.0,
        "rss_sampling_contract": "run_with_heartbeat.process_tree_metrics-v1",
        "rss_sampling_limitation": "no child launched; candidate observation may miss between-sample spikes",
    }
    lock_cm = None
    result: ChildResult | None = None
    body_error: BaseException | None = None
    try:
        lock_cm = _sh07.shared_lock_lease(CANONICAL_LOCK)
        handle, lease = lock_cm.__enter__()
        lock = receipt["lock"]
        assert isinstance(lock, dict)
        lock.update(lease)
        lock.update({"path": CANONICAL_LOCK_TEXT,
                     "canonical_path": CANONICAL_LOCK_TEXT,
                     "owner": "reviewed-attestation-wrapper",
                     "acquired": True,
                     "identity_before": _lock_identity(handle)})
        result = _run_reviewed_attestation_locked(
            root=root,
            receipt=receipt,
            nonce=nonce,
            check_id=check_id,
            candidate_check_id=candidate_check_id,
            candidate_receipt_path=candidate_receipt_path,
            candidate_receipt_sha256=candidate_receipt_sha256,
            candidate_state_dir=candidate_state_dir,
            candidate_manifest_path=candidate_manifest_path,
            candidate_manifest_sha256=candidate_manifest_sha256,
            attestation_path=attestation_path,
            attestation_sha256=attestation_sha256,
        )
        handle.validate()
        lock["validated"] = True
        lock["identity_after"] = _lock_identity(handle)
    except Exception as error:
        body_error = error
        result = ChildResult(
            75, "", str(error), cleanup={"terminal_safe": True, "output_complete": True},
            supervision_failed=True, **rss_kwargs,
        )
        receipt["error"] = str(error)
        receipt["authority"] = "none"
        receipt["non_authoritative"] = True
        receipt["proof_candidate"] = False
        receipt["attestation_required"] = True
        receipt["child"] = _child_record(result, command)
    finally:
        if lock_cm is not None:
            try:
                lock_cm.__exit__(
                    type(body_error),
                    body_error,
                    body_error.__traceback__ if body_error is not None else None,
                )
                lock = receipt.get("lock")
                if isinstance(lock, dict) and lock.get("acquired") is True:
                    lock["released"] = True
            except Exception as error:
                lock = receipt.get("lock")
                if isinstance(lock, dict):
                    receipt["error"] = str(error)
                    lock["released"] = False
                result = ChildResult(
                    75, "", str(error), cleanup={"terminal_safe": True, "output_complete": True},
                    supervision_failed=True, **rss_kwargs,
                )
                receipt["child"] = _child_record(result, command)
    if result is None:
        result = ChildResult(
            75, "", "reviewed-attestation did not produce a result",
            cleanup={"terminal_safe": True, "output_complete": True},
            supervision_failed=True, **rss_kwargs,
        )
        receipt["child"] = _child_record(result, command)
    return result


def run_stage3(
    *,
    root: Path | str,
    receipt_path: Path | str,
    nonce: str,
    check_id: str,
    runner_report_path: Path | str | None = None,
    mode: str = DEFAULT_MODE,
    batch: str = DEFAULT_BATCH,
    command_identity_sha256: str = "",
    candidate_check_id: str | None = None,
    candidate_receipt_path: Path | str | None = None,
    candidate_receipt_sha256: str | None = None,
    candidate_state_dir: Path | str | None = None,
    candidate_manifest_path: Path | str | None = None,
    candidate_manifest_sha256: str | None = None,
    attestation_path: Path | str | None = None,
    attestation_sha256: str | None = None,
    launcher: Launcher = default_launcher,
    timeout_seconds: float = 21600.0,
) -> tuple[int, dict[str, object]]:
    """Run one fixed Stage 3 transaction and publish its final receipt."""

    root_path = _safe_root(root)
    nonce = _safe_slug(nonce, "nonce")
    check_id = _safe_slug(check_id, "check_id")
    if mode not in MODES:
        raise Stage3Error(f"unknown Stage 3 mode: {mode}")
    if mode == MODE_PURE and batch == "authority":
        raise Stage3Error("authority batch requires proof-candidate or reviewed-attestation mode")
    if mode in {MODE_PROOF_CANDIDATE, MODE_REVIEWED_ATTESTATION} and batch != "authority":
        raise Stage3Error("proof-candidate/reviewed-attestation modes require the authority batch")
    command_hash = command_identity_sha256 or "sha256:" + ("0" * 64)
    if command_identity_sha256 and _SHA256.fullmatch(command_identity_sha256) is None:
        raise Stage3Error("command identity hash is malformed")
    receipt_target = _safe_root_relative(root_path, Path(receipt_path), "receipt")
    _assert_new_regular_target(receipt_target)
    runner_report_target: Path | None = None
    if mode == MODE_PURE:
        if runner_report_path is None:
            runner_report_target = _new_runner_report_path(root_path, check_id, nonce)
        else:
            runner_report_target = _safe_root_relative(root_path, Path(runner_report_path), "runner report")
            parent_fd, leaf = _open_parent_dirfd(root_path, runner_report_target, "runner report", create=True)
            try:
                try:
                    os.stat(leaf, dir_fd=parent_fd, follow_symlinks=False)
                except FileNotFoundError:
                    pass
                else:
                    raise Stage3Error(f"runner report target already exists: {runner_report_target}")
            finally:
                os.close(parent_fd)
    command = ["python3", "tools/run_stage3_verification.py"]
    receipt = _base_receipt(
        root=root_path,
        receipt_path=receipt_target,
        nonce=nonce,
        check_id=check_id,
        mode=mode,
        batch=batch,
        command=command,
        command_hash=command_hash,
    )
    started = time.monotonic()
    try:
        result = (
            _run_proof_candidate(
                root=root_path,
                receipt=receipt,
                nonce=nonce,
                launcher=launcher,
                timeout_seconds=timeout_seconds,
            )
            if mode == MODE_PROOF_CANDIDATE
            else _run_reviewed_attestation(
                root=root_path,
                receipt=receipt,
                nonce=nonce,
                check_id=check_id,
                candidate_check_id=(str(candidate_check_id) if candidate_check_id is not None else None),
                candidate_receipt_path=(
                    Path(candidate_receipt_path) if candidate_receipt_path is not None else None
                ),
                candidate_receipt_sha256=candidate_receipt_sha256,
                candidate_state_dir=(
                    Path(candidate_state_dir) if candidate_state_dir is not None else None
                ),
                candidate_manifest_path=(
                    Path(candidate_manifest_path) if candidate_manifest_path is not None else None
                ),
                candidate_manifest_sha256=candidate_manifest_sha256,
                attestation_path=(Path(attestation_path) if attestation_path is not None else None),
                attestation_sha256=attestation_sha256,
            )
            if mode == MODE_REVIEWED_ATTESTATION
            else _run_pure(
                root=root_path,
                receipt=receipt,
                batch=batch,
                nonce=nonce,
                check_id=check_id,
                command_identity_sha256=command_hash,
                runner_report_path=runner_report_target,
                launcher=launcher,
                timeout_seconds=timeout_seconds,
            )
        )
        _finish_receipt(receipt, result)
    except Exception as exc:
        result = ChildResult(1, "", str(exc))
        receipt["error"] = str(exc)
        receipt["child"] = _child_record(result, command)
        _finish_receipt(receipt, result)
    receipt["duration_ms"] = round((time.monotonic() - started) * 1000, 3)
    atomic_receipt_write(receipt_target, receipt, root=root_path)
    return int(receipt["exit_code"]), receipt


def _environment_configuration(cli: Mapping[str, object] | None = None) -> dict[str, object]:
    cli = dict(cli or {})
    root = cli.get("root") or os.environ.get(ROOT_ENV)
    receipt = cli.get("receipt_path") or os.environ.get(RECEIPT_ENV)
    report = cli.get("runner_report_path") or os.environ.get(REPORT_ENV)
    nonce = cli.get("nonce") or os.environ.get(NONCE_ENV)
    check_id = cli.get("check_id") or os.environ.get(CHECK_ID_ENV)
    candidate_check_id = cli.get("candidate_check_id") or os.environ.get(CANDIDATE_CHECK_ID_ENV)
    if not root or not receipt or not nonce or not check_id:
        raise Stage3Error(
            "Stage 3 wrapper requires verifier-bound root, receipt, nonce, and check id"
        )
    mode = cli.get("mode") or os.environ.get(MODE_ENV, DEFAULT_MODE)
    batch = cli.get("batch") or os.environ.get(
        BATCH_ENV,
        "authority" if mode in {MODE_PROOF_CANDIDATE, MODE_REVIEWED_ATTESTATION}
        else DEFAULT_BATCH,
    )
    return {
        "root": root,
        "receipt_path": receipt,
        "runner_report_path": report,
        "nonce": nonce,
        "check_id": check_id,
        "candidate_check_id": candidate_check_id,
        "mode": mode,
        "batch": batch,
        "command_identity_sha256": os.environ.get(COMMAND_HASH_ENV, ""),
        "candidate_receipt_path": cli.get("candidate_receipt_path") or os.environ.get(CANDIDATE_RECEIPT_ENV),
        "candidate_receipt_sha256": cli.get("candidate_receipt_sha256") or os.environ.get(CANDIDATE_RECEIPT_SHA_ENV),
        "candidate_state_dir": cli.get("candidate_state_dir") or os.environ.get(CANDIDATE_STATE_ENV),
        "candidate_manifest_path": cli.get("candidate_manifest_path") or os.environ.get(CANDIDATE_MANIFEST_ENV),
        "candidate_manifest_sha256": cli.get("candidate_manifest_sha256") or os.environ.get(CANDIDATE_MANIFEST_SHA_ENV),
        "attestation_path": cli.get("attestation_path") or os.environ.get(ATTESTATION_PATH_ENV),
        "attestation_sha256": cli.get("attestation_sha256") or os.environ.get(ATTESTATION_SHA_ENV),
        "timeout_seconds": float(os.environ.get(TIMEOUT_ENV, "21600")),
    }


def build_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--root", type=Path)
    parser.add_argument("--receipt", type=Path)
    parser.add_argument("--report", type=Path)
    parser.add_argument("--nonce")
    parser.add_argument("--check-id")
    parser.add_argument("--candidate-check-id", dest="candidate_check_id")
    parser.add_argument("--mode", choices=MODES)
    parser.add_argument("--batch", choices=FIXED_BATCHES)
    parser.add_argument("--candidate-receipt", dest="candidate_receipt_path", type=Path)
    parser.add_argument("--candidate-receipt-sha256", dest="candidate_receipt_sha256")
    parser.add_argument("--candidate-state-dir", dest="candidate_state_dir", type=Path)
    parser.add_argument("--candidate-manifest", dest="candidate_manifest_path", type=Path)
    parser.add_argument("--candidate-manifest-sha256", dest="candidate_manifest_sha256")
    parser.add_argument("--attestation", dest="attestation_path", type=Path)
    parser.add_argument("--attestation-sha256", dest="attestation_sha256")
    return parser


def main(argv: Sequence[str] | None = None) -> int:
    args = build_parser().parse_args(argv)
    try:
        cli_values = {
            "root": args.root,
            "receipt_path": args.receipt,
            "runner_report_path": args.report,
            "nonce": args.nonce,
            "check_id": args.check_id,
            "candidate_check_id": args.candidate_check_id,
            "mode": args.mode,
            "batch": args.batch,
            "candidate_receipt_path": args.candidate_receipt_path,
            "candidate_receipt_sha256": args.candidate_receipt_sha256,
            "candidate_state_dir": args.candidate_state_dir,
            "candidate_manifest_path": args.candidate_manifest_path,
            "candidate_manifest_sha256": args.candidate_manifest_sha256,
            "attestation_path": args.attestation_path,
            "attestation_sha256": args.attestation_sha256,
        }
        cli_values = {key: value for key, value in cli_values.items() if value is not None}
        values = _environment_configuration(cli_values)
        code, receipt = run_stage3(**values)
        print(json.dumps(receipt, ensure_ascii=True, sort_keys=True))
        return code
    except (Stage3Error, ValueError, OSError) as exc:
        print(f"Stage 3 verification failed: {exc}", file=sys.stderr)
        return 2


if __name__ == "__main__":
    raise SystemExit(main())
