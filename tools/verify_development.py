#!/usr/bin/env python3
"""Run the manifest-defined development verification graph.

This is intentionally a small orchestration layer rather than another test
framework.  Checks are declared in ``development_verification_manifest.json``
with explicit inputs, commands, dependencies, lanes, and resource locks.  The
runner then selects the impacted graph, schedules independent cheap checks in
parallel, serializes checks that claim a heavy or exclusive resource, and
records a JSON receipt suitable for CI or a later resume.

The cache is conservative by design.  A result can be reused only when the
declared input identities, command/tool identity, check declaration, and
dependency results are identical.  A passing command is development evidence,
not an authority grant: this runner reports every command as
``fresh-command-pass-non-authoritative`` until a required-output artifact
validator is wired in.  This keeps development acceleration from becoming an
accidental release or bootstrap claim.
"""

from __future__ import annotations

import argparse
import concurrent.futures
import contextlib
import datetime as _datetime
import fnmatch
import hashlib
import json
import math
import os
from pathlib import Path, PurePosixPath
import platform
import select
import secrets
import signal
import shutil
import stat
import subprocess
import sys
import tempfile
import threading
import time
from typing import Any, Iterable, Mapping, Sequence

try:
    from tools import run_sh07_authoritative_modules as _sh07
except ImportError:
    import run_sh07_authoritative_modules as _sh07

try:
    from tools import run_stage3_verification as _stage3
except ImportError:  # pragma: no cover - direct execution from tools/
    import run_stage3_verification as _stage3


ROOT = Path(__file__).resolve().parents[1]
DEFAULT_MANIFEST = Path(__file__).with_name("development_verification_manifest.json")
DEFAULT_CACHE = ROOT / ".cpcache" / "development-verification-cache.json"
SCHEMA_VERSION = 1
LANES = ("preflight", "focused", "heavy-candidate")
STATUSES = ("passed", "failed", "blocked", "reused", "planned", "timeout")
_GLOB_CHARS = frozenset("*?[")
_MAX_OUTPUT_BYTES = 64 * 1024
_MUTATION_POLL_SECONDS = 0.05
_PROCESS_TERM_GRACE_SECONDS = 0.5
_PROCESS_KILL_GRACE_SECONDS = 0.5
_STAGE3_RSS_CADENCE_SECONDS = 1.0
LOCK_OWNERS = ("runner", "command")
_STAGE3_RESERVED_ENV = frozenset({
    _stage3.RECEIPT_ENV,
    _stage3.REPORT_ENV,
    _stage3.NONCE_ENV,
    _stage3.CHECK_ID_ENV,
    _stage3.ROOT_ENV,
    _stage3.MODE_ENV,
    _stage3.BATCH_ENV,
    _stage3.COMMAND_HASH_ENV,
    _stage3.EXPECTED_COMMAND_ENV,
    _stage3.TIMEOUT_ENV,
    _stage3.CANDIDATE_CHECK_ID_ENV,
    _stage3.CANDIDATE_RECEIPT_ENV,
    _stage3.CANDIDATE_RECEIPT_SHA_ENV,
    _stage3.CANDIDATE_STATE_ENV,
    _stage3.CANDIDATE_MANIFEST_ENV,
    _stage3.CANDIDATE_MANIFEST_SHA_ENV,
    _stage3.ATTESTATION_PATH_ENV,
    _stage3.ATTESTATION_SHA_ENV,
})
_LAUNCH_WRAPPER = (
    "import os,sys; "
    "fd=int(os.environ.pop('_GRAVITY_VERIFIER_LAUNCH_FD')); "
    "token=os.read(fd,1); os.close(fd); "
    "raise SystemExit(125) if not token else os.execvpe(sys.argv[1], sys.argv[1:], os.environ)"
)


class VerificationError(Exception):
    """Base error for malformed manifests and invalid verification plans."""


class ManifestError(VerificationError):
    """Raised when a manifest cannot safely describe a verification graph."""


class LockUnavailable(VerificationError):
    """Raised when another process owns a declared exclusive resource."""


def _canonical(value: Any) -> str:
    return json.dumps(value, ensure_ascii=True, sort_keys=True, separators=(",", ":"))


def _sha256_bytes(value: bytes) -> str:
    return hashlib.sha256(value).hexdigest()


def _sha256_text(value: str) -> str:
    return _sha256_bytes(value.encode("utf-8"))


def _stat_identity(info: os.stat_result) -> tuple[Any, ...]:
    return (
        info.st_dev,
        info.st_ino,
        info.st_mode,
        info.st_nlink,
        info.st_size,
        getattr(info, "st_mtime_ns", int(info.st_mtime * 1_000_000_000)),
        getattr(info, "st_ctime_ns", int(info.st_ctime * 1_000_000_000)),
    )


def _open_regular_fd(path: Path, *, root: Path | None = None, relative: str | None = None) -> int:
    """Open one regular file without following a symlink or escaping ``root``.

    Declared inputs are opened component-by-component from a directory fd.  A
    pathname may be replaced after discovery, so hashing a path and then
    calling ``stat(path)`` is not a coherent identity operation.  Traversing
    with ``O_NOFOLLOW`` and checking the descriptor itself makes the hash and
    metadata refer to the same opened object.
    """

    nofollow = getattr(os, "O_NOFOLLOW", 0)
    cloexec = getattr(os, "O_CLOEXEC", 0)
    read_flags = os.O_RDONLY | cloexec | nofollow
    if root is None:
        if not nofollow:
            try:
                link_info = os.lstat(path)
            except OSError as exc:
                raise VerificationError(f"cannot read declared identity {path}: {exc}") from exc
            if stat.S_ISLNK(link_info.st_mode):
                raise VerificationError(f"declared input symlink is not allowed: {path}")
        try:
            descriptor = os.open(path, read_flags)
        except (OSError, ValueError) as exc:
            raise VerificationError(f"cannot read declared identity {path}: {exc}") from exc
        try:
            info = os.fstat(descriptor)
            if not stat.S_ISREG(info.st_mode):
                raise VerificationError(f"declared identity is not a regular file: {path}")
            return descriptor
        except BaseException:
            os.close(descriptor)
            raise

    root_path = Path(root).resolve()
    if relative is None:
        try:
            relative_path = Path(path).resolve(strict=False).relative_to(root_path)
        except ValueError as exc:
            raise VerificationError(f"declared identity escapes repository root: {path}") from exc
    else:
        relative_path = Path(_normalise_declared_path(relative))
    if relative_path.is_absolute() or ".." in relative_path.parts or not relative_path.parts:
        raise VerificationError(f"declared identity escapes repository root: {path}")
    directory_flags = os.O_RDONLY | cloexec | nofollow | getattr(os, "O_DIRECTORY", 0)
    try:
        directory_fd = os.open(root_path, directory_flags)
    except (OSError, ValueError) as exc:
        raise VerificationError(f"cannot open repository root for declared identity {root_path}: {exc}") from exc
    current_fd = directory_fd
    try:
        for component in relative_path.parts[:-1]:
            try:
                if not nofollow:
                    link_info = os.stat(component, dir_fd=current_fd, follow_symlinks=False)
                    if stat.S_ISLNK(link_info.st_mode):
                        raise VerificationError(f"declared input symlink is not allowed: {relative_path}")
                next_fd = os.open(component, directory_flags, dir_fd=current_fd)
            except (OSError, ValueError) as exc:
                raise VerificationError(f"cannot traverse declared identity {relative_path}: {exc}") from exc
            if current_fd != directory_fd:
                os.close(current_fd)
            current_fd = next_fd
        try:
            if not nofollow:
                link_info = os.stat(relative_path.parts[-1], dir_fd=current_fd, follow_symlinks=False)
                if stat.S_ISLNK(link_info.st_mode):
                    raise VerificationError(f"declared input symlink is not allowed: {path}")
            descriptor = os.open(relative_path.parts[-1], read_flags, dir_fd=current_fd)
        except (OSError, ValueError) as exc:
            raise VerificationError(f"cannot read declared identity {path}: {exc}") from exc
        try:
            info = os.fstat(descriptor)
            if not stat.S_ISREG(info.st_mode):
                raise VerificationError(f"declared identity is not a regular file: {path}")
            return descriptor
        except BaseException:
            os.close(descriptor)
            raise
    finally:
        if current_fd != directory_fd:
            os.close(current_fd)
        os.close(directory_fd)


def _hash_open_regular_fd(descriptor: int, display_path: Path | str) -> tuple[str, os.stat_result]:
    """Hash an open descriptor and require a stable before/after fstat pair."""

    before = os.fstat(descriptor)
    if not stat.S_ISREG(before.st_mode):
        raise VerificationError(f"declared identity is not a regular file: {display_path}")
    digest = hashlib.sha256()
    try:
        os.lseek(descriptor, 0, os.SEEK_SET)
    except OSError:
        pass
    with os.fdopen(os.dup(descriptor), "rb") as stream:
        for chunk in iter(lambda: stream.read(1024 * 1024), b""):
            digest.update(chunk)
    after = os.fstat(descriptor)
    if _stat_identity(before) != _stat_identity(after):
        raise VerificationError(f"declared identity changed while being read: {display_path}")
    return digest.hexdigest(), after


def _hash_regular_file(path: Path, *, root: Path | None = None, relative: str | None = None) -> tuple[str, os.stat_result]:
    """Hash a regular file through one coherent, no-follow descriptor."""

    last_error: VerificationError | None = None
    for _attempt in range(2):
        descriptor = _open_regular_fd(path, root=root, relative=relative)
        try:
            try:
                return _hash_open_regular_fd(descriptor, path)
            except (OSError, VerificationError) as exc:
                last_error = exc if isinstance(exc, VerificationError) else VerificationError(str(exc))
        finally:
            os.close(descriptor)
    raise last_error or VerificationError(f"declared identity changed while being read: {path}")


def _sha256_file(path: Path) -> str:
    """Hash a regular file only when its descriptor snapshot is coherent."""

    return _hash_regular_file(path)[0]


def _now() -> str:
    return _datetime.datetime.now(_datetime.timezone.utc).isoformat(timespec="milliseconds").replace("+00:00", "Z")


def _relpath(root: Path, path: Path | str) -> str:
    candidate = Path(path)
    if not candidate.is_absolute():
        candidate = root / candidate
    try:
        return candidate.resolve().relative_to(root.resolve()).as_posix()
    except ValueError:
        return candidate.resolve().as_posix()


def _normalise_declared_path(value: str) -> str:
    value = value.replace("\\", "/")
    while value.startswith("./"):
        value = value[2:]
    return value


def _is_safe_relative_path(value: str) -> bool:
    path = PurePosixPath(value)
    return not path.is_absolute() and ".." not in path.parts


def _normalise_change(root: Path, value: str | Path) -> str:
    candidate = Path(value)
    if candidate.is_absolute():
        try:
            candidate = candidate.resolve().relative_to(root.resolve())
        except ValueError:
            return candidate.resolve().as_posix()
    return _normalise_declared_path(candidate.as_posix())


def _parse_command(command: Any, check_id: str) -> list[str]:
    if isinstance(command, str):
        # A string command is deliberately not passed through a shell.  A
        # manifest may use it for compactness, but tokenization remains
        # deterministic and avoids ambient shell expansion.
        import shlex

        command = shlex.split(command, posix=True)
    if not isinstance(command, list) or not command or not all(isinstance(item, str) for item in command):
        raise ManifestError(f"check {check_id!r} command must be a non-empty string list")
    return list(command)


def _check_authority(check: Mapping[str, Any], lane: str) -> str:
    raw = check.get("authority")
    if raw is None:
        raw = "none"
    if raw not in {"none", "declared"}:
        raise ManifestError(f"check {check.get('id')!r} authority must be 'none' or 'declared'")
    if raw == "declared" and lane != "heavy-candidate":
        raise ManifestError(f"check {check.get('id')!r} declares authority outside heavy-candidate lane")
    return str(raw)


def _stage3_mode(check: Mapping[str, Any]) -> str:
    """Require an explicit Stage 3 mode for command-owned nodes."""

    raw = check.get("stage3_mode")
    if raw not in _stage3.MODES:
        raise ManifestError(
            f"check {check.get('id')!r} must declare one explicit stage3_mode: "
            + ", ".join(_stage3.MODES)
        )
    mode = str(raw)
    batch = check.get("stage3_batch")
    authority = check.get("authority", "none")
    proof_batches = frozenset(getattr(_stage3, "FIXED_MODULE_POLICIES", {"authority": None}))
    if mode == _stage3.MODE_PURE and batch in proof_batches:
        raise ManifestError("pure Stage 3 mode cannot select the authority batch")
    if mode in {_stage3.MODE_PROOF_CANDIDATE, _stage3.MODE_REVIEWED_ATTESTATION} \
            and batch not in proof_batches:
        raise ManifestError(
            f"{mode} Stage 3 mode requires an exact fixed proof batch"
        )
    if mode == _stage3.MODE_REVIEWED_ATTESTATION and authority != "declared":
        raise ManifestError("reviewed-attestation mode requires authority='declared'")
    if mode != _stage3.MODE_REVIEWED_ATTESTATION and authority == "declared":
        raise ManifestError("authority='declared' requires reviewed-attestation mode")
    return mode


_STAGE3_HEAP_BYTES = {
    "-J-Xmx512m": 512 * 1024 * 1024,
    "-J-Xmx2g": 2 * 1024 * 1024 * 1024,
    "-J-Xmx8g": 8 * 1024 * 1024 * 1024,
}

_STAGE8_FIXED_NODE_POLICIES = {
    "stage8-c12-source-shape": {
        "timeout_seconds": 600,
        "stage3_batch": "stage8-c12-source-shape",
        "depends_on": ["stage3-runner-unit"],
        "required_inputs": (
            "bootstrap/gravity/src/gravity/compiler/c12_domain_ir_architecture.gravity",
            "bootstrap/clojure/test/gravity/self_hosting/sh07_c12_domain_ir_shape_preflight_test.clj",
        ),
    },
    "stage8-public-c12": {
        "timeout_seconds": 900,
        "stage3_batch": "stage8-public-c12",
        "depends_on": ["stage8-c12-source-shape"],
        "required_inputs": (
            "bootstrap/gravity/src/gravity/compiler/c12_domain_ir_architecture.gravity",
            "bootstrap/clojure/test/gravity/bootstrap_test.clj",
            "bootstrap/clojure/test/gravity/cli_test.clj",
            "bootstrap/clojure/test/gravity/diagnostics_test.clj",
            "bin/gravity",
            "target/phase-18/jvm-cli/gravity-jvm-cli.jar",
            "docs/artifacts/phase-15/bootstrap/p15-s23-final-seed-retirement-proof.edn",
        ),
    },
    "stage8-sh13-c11-domain-evidence": {
        "timeout_seconds": 1800,
        "stage3_batch": "stage8-sh13-c11-domain-evidence",
        "depends_on": ["stage8-c12-source-shape"],
        "required_inputs": (
            "bootstrap/gravity/src/gravity/compiler/c12_domain_ir_architecture.gravity",
            "bootstrap/gravity/src/gravity/compiler/c11_mir_specification.gravity",
            "bootstrap/gravity/src/gravity/compiler/c10_safety_analysis_pipeline.gravity",
            "bootstrap/gravity/src/gravity/compiler/c9_ownership_checker_engine.gravity",
            "bootstrap/gravity/src/gravity/compiler/c8_effect_checker_engine.gravity",
            "bootstrap/clojure/test/gravity/self_hosting/sh13_c11_domain_evidence_adapter_test.clj",
            "bootstrap/clojure/test/gravity/self_hosting/sh12_c10_mir_adapter_test.clj",
            "bootstrap/clojure/test/gravity/self_hosting/sh11_c9_safety_adapter_test.clj",
            "bootstrap/clojure/test/gravity/self_hosting/sh10_c8_ownership_adapter_test.clj",
            "bootstrap/clojure/test/gravity/self_hosting/sh09_c7_effect_adapter_test.clj",
        ),
    },
}


_P15_NATIVE_LAUNCHER_CHECK_ID = "stage0-p15-native-launcher-prerequisite"
_P15_NATIVE_LAUNCHER_COMMAND = [
    "clojure",
    "-J-Xmx1g",
    "-M:test",
    "--namespace",
    "gravity.p15-native-launcher-test",
]
_P15_NATIVE_LAUNCHER_INPUTS = [
    "bootstrap/native/p15_public_native_launcher.c",
    "bootstrap/clojure/test/gravity/p15_native_launcher_test.clj",
    "bootstrap/clojure/fixtures/p15-native-launcher/argv_stdout.c",
    "bootstrap/clojure/fixtures/p15-native-launcher/exit_23.c",
    "bootstrap/clojure/fixtures/p15-native-launcher/leader_descendant.c",
    "bootstrap/clojure/fixtures/p15-native-launcher/marker.c",
    "bootstrap/clojure/fixtures/p15-native-launcher/timeout_group.c",
    "docs/artifacts/phase-15/native-launcher/p15-s23-darwin-launcher-primitive.edn",
]
_P15_NATIVE_LAUNCHER_TOOL_INPUTS = [
    "deps.edn",
    "bootstrap/clojure/test/gravity/self_hosting_test_runner.clj",
]
_P15_NATIVE_RUNTIME_PROVIDER_CHECK_ID = "stage0-p15-native-runtime-provider-prerequisite"
_P15_NATIVE_RUNTIME_PROVIDER_COMMAND = [
    "clojure",
    "-J-Xmx1g",
    "-M:test",
    "--namespace",
    "gravity.p15-native-runtime-driver-test",
]
_P15_NATIVE_RUNTIME_PROVIDER_INPUTS = [
    "bootstrap/native/p15_native_runtime_driver.c",
    "bootstrap/gravity/p15_s23/native_runtime_driver.gravity",
    "bootstrap/clojure/test/gravity/p15_native_runtime_driver_test.clj",
    "bootstrap/clojure/fixtures/p15-native-runtime-driver/accepted-branch.gravity",
    "bootstrap/clojure/fixtures/p15-native-runtime-driver/accepted-branch.payload",
    "bootstrap/clojure/fixtures/p15-native-runtime-driver/accepted-print.gravity",
    "bootstrap/clojure/fixtures/p15-native-runtime-driver/accepted-print.payload",
    "bootstrap/clojure/fixtures/p15-native-runtime-driver/accepted-print.qst",
    "bootstrap/clojure/fixtures/p15-native-runtime-driver/accepted-str.gravity",
    "bootstrap/clojure/fixtures/p15-native-runtime-driver/accepted-str.payload",
    "bootstrap/clojure/fixtures/p15-native-runtime-driver/rejected-halt.payload",
    "bootstrap/clojure/fixtures/p15-native-runtime-driver/rejected-int-leading-zero.payload",
    "bootstrap/clojure/fixtures/p15-native-runtime-driver/rejected-int-negative-zero.payload",
    "bootstrap/clojure/fixtures/p15-native-runtime-driver/rejected-int-plus.payload",
    "bootstrap/clojure/fixtures/p15-native-runtime-driver/rejected-invalid-utf8-ff.payload",
    "bootstrap/clojure/fixtures/p15-native-runtime-driver/rejected-invalid-utf8-overlong.payload",
    "bootstrap/clojure/fixtures/p15-native-runtime-driver/rejected-jump-leading-zero.payload",
    "bootstrap/clojure/fixtures/p15-native-runtime-driver/rejected-jump-negative-zero.payload",
    "bootstrap/clojure/fixtures/p15-native-runtime-driver/rejected-jump-plus.payload",
    "bootstrap/clojure/fixtures/p15-native-runtime-driver/rejected-missing-halt.payload",
    "bootstrap/clojure/fixtures/p15-native-runtime-driver/rejected-operand.payload",
    "bootstrap/clojure/fixtures/p15-native-runtime-driver/rejected-output-overflow.payload",
    "bootstrap/clojure/fixtures/p15-native-runtime-driver/rejected-underflow.payload",
    "bootstrap/clojure/fixtures/p15-native-runtime-driver/rejected-unsupported.payload",
    "bootstrap/clojure/fixtures/p15-native-runtime-driver/rejected-value-overflow.payload",
    "docs/artifacts/phase-15/native-runtime/p15-s23-bounded-native-runtime-provider.edn",
]
_P15_NATIVE_RUNTIME_PROVIDER_TOOL_INPUTS = [
    "deps.edn",
    "bootstrap/clojure/test/gravity/self_hosting_test_runner.clj",
]
_P15_NATIVE_RUNTIME_REQUIRED_ENV = {
    "GRAVITY_P15_NATIVE_RUNTIME_REQUIRED": "1",
}
_OBSERVED_PROCESS_TREE_RESOURCE_RECEIPT = "observed-peak-process-tree-rss-and-wall-time"
_OBSERVED_PROCESS_TREE_RSS_CONTRACT = "run_with_heartbeat.process_tree_metrics-v1"


def _validate_p15_native_launcher_contract(check: Mapping[str, Any]) -> None:
    """Keep the bounded Darwin launcher gate on its reviewed direct command.

    Unlike fixed Stage3 nodes, this check runs the Clojure test namespace
    directly.  It therefore keeps the parent verifier lock (``lock_owner``
    ``runner``) and must not be silently widened into a generic Stage0 suite
    or a command-owned proof boundary.
    """

    if check.get("id") != _P15_NATIVE_LAUNCHER_CHECK_ID:
        return
    check_id = _P15_NATIVE_LAUNCHER_CHECK_ID
    if check.get("lane") != "heavy-candidate":
        raise ManifestError(f"check {check_id!r} must use heavy-candidate lane")
    if check.get("cost") != "heavy":
        raise ManifestError(f"check {check_id!r} must use cost='heavy'")
    if check.get("lock") != str(_stage3.CANONICAL_LOCK):
        raise ManifestError(
            f"check {check_id!r} must use canonical lock {str(_stage3.CANONICAL_LOCK)!r}"
        )
    if check.get("lock_owner") != "runner":
        raise ManifestError(
            f"check {check_id!r} direct command must retain lock_owner='runner'"
        )
    if (
        check.get("exclusive") is not True
        or type(check.get("capacity")) is not int
        or check.get("capacity") != 1
    ):
        raise ManifestError(
            f"check {check_id!r} must declare exclusive=true and capacity=1"
        )
    if check.get("authority") != "none":
        raise ManifestError(f"check {check_id!r} must declare authority='none'")
    if check.get("proof_candidate", False) is not False or check.get("attestation_required", False) is not False:
        raise ManifestError(
            f"check {check_id!r} must remain a non-proof, non-attestation gate"
        )
    if check.get("fresh") is not True:
        raise ManifestError(f"check {check_id!r} must declare fresh=true")
    if check.get("resume") is not False or check.get("no_resume") is not True:
        raise ManifestError(
            f"check {check_id!r} must declare resume=false and no_resume=true"
        )
    timeout = check.get("timeout_seconds")
    if type(timeout) is not int or timeout != 600:
        raise ManifestError(f"check {check_id!r} timeout_seconds must be exactly 600")
    if check.get("jvm_heap") != "-J-Xmx1g":
        raise ManifestError(f"check {check_id!r} must declare jvm_heap='-J-Xmx1g'")
    if (
        type(check.get("minimum_heap_bytes")) is not int
        or check.get("minimum_heap_bytes") != 1073741824
    ):
        raise ManifestError(
            f"check {check_id!r} minimum_heap_bytes must equal 1073741824"
        )
    if check.get("resource_receipt") != _OBSERVED_PROCESS_TREE_RESOURCE_RECEIPT:
        raise ManifestError(
            f"check {check_id!r} must declare the observed process-tree resource receipt"
        )
    if _parse_command(check.get("command"), check_id) != _P15_NATIVE_LAUNCHER_COMMAND:
        raise ManifestError(
            f"check {check_id!r} command must be the exact direct Darwin launcher test command"
        )
    if check.get("inputs") != _P15_NATIVE_LAUNCHER_INPUTS:
        raise ManifestError(
            f"check {check_id!r} inputs drifted from the reviewed launcher source/fixture/artifact set"
        )
    if check.get("tool_inputs") != _P15_NATIVE_LAUNCHER_TOOL_INPUTS:
        raise ManifestError(
            f"check {check_id!r} tool_inputs drifted from deps.edn and the test runner"
        )
    dependencies = check.get("depends_on", check.get("dependencies", []))
    if dependencies != ["stage0-orchestrator-unit"]:
        raise ManifestError(
            f"check {check_id!r} must depend only on stage0-orchestrator-unit"
        )
    if check.get("automatic", True) is not True:
        raise ManifestError(f"check {check_id!r} must participate in change-impact routing")


def _validate_p15_native_runtime_provider_contract(check: Mapping[str, Any]) -> None:
    """Keep the bounded native runtime provider on its reviewed direct command.

    This is an internal prerequisite for the P15 native/runtime boundary.  It
    is intentionally a direct Clojure namespace command (not a Stage3 wrapper)
    and therefore retains the verifier-owned canonical lock and non-authority
    lifecycle used by the launcher prerequisite.
    """

    if check.get("id") != _P15_NATIVE_RUNTIME_PROVIDER_CHECK_ID:
        return
    check_id = _P15_NATIVE_RUNTIME_PROVIDER_CHECK_ID
    if check.get("lane") != "heavy-candidate":
        raise ManifestError(f"check {check_id!r} must use heavy-candidate lane")
    if check.get("cost") != "heavy":
        raise ManifestError(f"check {check_id!r} must use cost='heavy'")
    if check.get("lock") != str(_stage3.CANONICAL_LOCK):
        raise ManifestError(
            f"check {check_id!r} must use canonical lock {str(_stage3.CANONICAL_LOCK)!r}"
        )
    if check.get("lock_owner") != "runner":
        raise ManifestError(
            f"check {check_id!r} direct command must retain lock_owner='runner'"
        )
    expected_booleans = {
        "exclusive": True,
        "fresh": True,
        "resume": False,
        "no_resume": True,
        "automatic": True,
    }
    for field, expected in expected_booleans.items():
        if check.get(field) is not expected:
            raise ManifestError(
                f"check {check_id!r} {field} must be exactly {expected!r}"
            )
    if type(check.get("capacity")) is not int or check.get("capacity") != 1:
        raise ManifestError(
            f"check {check_id!r} must declare exclusive=true and capacity=1"
        )
    if check.get("authority") != "none":
        raise ManifestError(f"check {check_id!r} must declare authority='none'")
    if check.get("proof_candidate", False) is not False or check.get("attestation_required", False) is not False:
        raise ManifestError(
            f"check {check_id!r} must remain a non-proof, non-attestation gate"
        )
    if check.get("daemonization") != "forbidden":
        raise ManifestError(f"check {check_id!r} must declare daemonization='forbidden'")
    timeout = check.get("timeout_seconds")
    if type(timeout) is not int or timeout != 180:
        raise ManifestError(f"check {check_id!r} timeout_seconds must be exactly 180")
    if check.get("jvm_heap") != "-J-Xmx1g":
        raise ManifestError(f"check {check_id!r} must declare jvm_heap='-J-Xmx1g'")
    if (
        type(check.get("minimum_heap_bytes")) is not int
        or check.get("minimum_heap_bytes") != 1073741824
    ):
        raise ManifestError(
            f"check {check_id!r} minimum_heap_bytes must equal 1073741824"
        )
    if check.get("resource_receipt") != _OBSERVED_PROCESS_TREE_RESOURCE_RECEIPT:
        raise ManifestError(
            f"check {check_id!r} must declare the observed process-tree resource receipt"
        )
    if check.get("env") != _P15_NATIVE_RUNTIME_REQUIRED_ENV:
        raise ManifestError(
            f"check {check_id!r} must bind the exact native-runtime required environment"
        )
    if (
        type(check.get("command")) is not list
        or check.get("command") != _P15_NATIVE_RUNTIME_PROVIDER_COMMAND
    ):
        raise ManifestError(
            f"check {check_id!r} command must be the exact direct native runtime provider test command"
        )
    if check.get("inputs") != _P15_NATIVE_RUNTIME_PROVIDER_INPUTS:
        raise ManifestError(
            f"check {check_id!r} inputs drifted from the reviewed native runtime provider source/fixture/artifact set"
        )
    if check.get("tool_inputs") != _P15_NATIVE_RUNTIME_PROVIDER_TOOL_INPUTS:
        raise ManifestError(
            f"check {check_id!r} tool_inputs drifted from deps.edn and the test runner"
        )
    if "dependencies" in check:
        raise ManifestError(
            f"check {check_id!r} must use the exact depends_on field"
        )
    dependencies = check.get("depends_on", [])
    if dependencies != ["stage0-orchestrator-unit"]:
        raise ManifestError(
            f"check {check_id!r} must depend only on stage0-orchestrator-unit"
        )


def _resource_receipt_error(record: Mapping[str, Any]) -> str | None:
    """Return a fail-closed diagnostic for an observed process-tree receipt."""

    peak = record.get("observed_peak_process_tree_rss_bytes")
    if type(peak) is not int or peak <= 0:
        return "resource receipt requires a strict positive integer RSS peak"
    cadence = record.get("rss_sampling_cadence_seconds")
    cadence_valid = type(cadence) in {int, float} and cadence > 0
    if cadence_valid:
        try:
            cadence_valid = math.isfinite(float(cadence))
        except (OverflowError, ValueError):
            cadence_valid = False
    if not cadence_valid:
        return "resource receipt requires a positive finite sampling cadence"
    if record.get("rss_sampling_contract") != _OBSERVED_PROCESS_TREE_RSS_CONTRACT:
        return "resource receipt sampling contract mismatch"
    return None


def _is_fixed_stage_check(check_id: str) -> bool:
    """Return whether a manifest node invokes the fixed stage wrapper.

    Stage3 through Stage8 all use the same command-owned
    ``run_stage3_verification.py`` boundary.  Keep this predicate centralized
    so a newly added fixed stage cannot accidentally bypass heap, runtime
    identity, lock-owner, or receipt validation.
    """

    return check_id.startswith(
        ("stage3-", "stage4-", "stage5-", "stage6-", "stage7-", "stage8-")
    )


def _validate_stage3_resource_contract(check: Mapping[str, Any]) -> None:
    """Require a fixed heap declaration for every Stage3 process boundary."""

    check_id = str(check.get("id", ""))
    if not _is_fixed_stage_check(check_id):
        return
    declared = check.get("jvm_heap")
    if check_id == "stage3-runner-unit":
        expected = "-J-Xmx2g"
        command = _parse_command(check.get("command"), check_id)
        if len(command) < 2 or command[1] != expected:
            raise ManifestError(
                f"check {check_id!r} must pin {expected} immediately after clojure"
            )
    else:
        batch = check.get("stage3_batch")
        proof_batches = frozenset(getattr(_stage3, "FIXED_MODULE_POLICIES", {"authority": None}))
        if batch in proof_batches:
            # The proof-candidate wrapper launches the authoritative module
            # child rather than the fixed Clojure batch command.  Its reviewed
            # child contract still requires the semantic/authentication floor.
            policy = getattr(_stage3, "FIXED_MODULE_POLICIES", {}).get(batch)
            expected = str(policy.get("heap", "-J-Xmx8g")) if isinstance(policy, Mapping) else "-J-Xmx8g"
        else:
            try:
                expected = str(_stage3.batch_command(str(batch))[1])
            except Exception as exc:
                raise ManifestError(
                    f"check {check_id!r} must declare a reviewed fixed Stage3 batch"
                ) from exc
    if declared != expected or expected not in _STAGE3_HEAP_BYTES:
        raise ManifestError(
            f"check {check_id!r} jvm_heap must equal the fixed wrapper heap {expected!r}"
        )
    minimum = check.get("minimum_heap_bytes")
    if type(minimum) is not int or minimum != _STAGE3_HEAP_BYTES[expected]:
        raise ManifestError(
            f"check {check_id!r} minimum_heap_bytes must equal the declared {expected} floor"
        )


def _validate_stage3_runtime_inputs(check: Mapping[str, Any]) -> None:
    """Keep every command-owned production node on the central runtime set.

    The runner-unit is intentionally a narrow unit preflight and does not
    launch the production wrapper, so it is exempt.  Production nodes must
    carry the complete centralized set in their declared inputs/tool inputs;
    otherwise a shared runtime edit could falsely reuse a receipt.
    """

    check_id = str(check.get("id", ""))
    if (
        not _is_fixed_stage_check(check_id)
        or check_id == "stage3-runner-unit"
        or check.get("lock_owner", "runner") != "command"
    ):
        return
    required = getattr(_stage3, "STAGE3_RUNTIME_DEPENDENCIES", None)
    if not isinstance(required, (tuple, list)) or not required:
        raise ManifestError("Stage3 wrapper runtime dependency contract is unavailable")
    declared = set(check.get("inputs", [])) | set(check.get("tool_inputs", []))
    missing = sorted(set(required) - declared)
    if missing:
        raise ManifestError(
            f"check {check_id!r} omits centralized Stage3 runtime inputs: {missing}"
        )
    missing_exact: list[str] = []
    for relative in required:
        if _contains_glob(str(relative)):
            continue
        try:
            info = os.lstat(ROOT / str(relative))
        except OSError:
            missing_exact.append(str(relative))
            continue
        if not stat.S_ISREG(info.st_mode):
            missing_exact.append(str(relative))
    if missing_exact:
        raise ManifestError(
            "centralized Stage3 runtime inputs are not existing regular files: "
            f"{sorted(missing_exact)}"
        )


def _validate_stage8_node_contract(check: Mapping[str, Any]) -> None:
    """Pin the bounded Stage8 lifecycle in addition to generic fixed-stage policy."""

    check_id = str(check.get("id", ""))
    if not check_id.startswith("stage8-"):
        return
    policy = _STAGE8_FIXED_NODE_POLICIES.get(check_id)
    if policy is None:
        raise ManifestError(f"unreviewed Stage8 check id: {check_id!r}")
    timeout = check.get("timeout_seconds")
    if type(timeout) is not int or timeout != policy["timeout_seconds"]:
        raise ManifestError(
            f"check {check_id!r} timeout_seconds must equal the fixed Stage8 bound"
        )
    expected_booleans = {
        "fresh": True,
        "resume": False,
        "automatic": True,
        "exclusive": True,
    }
    for field, value in expected_booleans.items():
        if check.get(field) is not value:
            raise ManifestError(
                f"check {check_id!r} {field} must equal fixed Stage8 value {value!r}"
            )
    expected_strings = {
        "state_dir_policy": "new-per-invocation",
        "lock": "/private/tmp/gravity-sh07-heavy.lock",
        "lock_owner": "command",
    }
    for field, value in expected_strings.items():
        observed = check.get(field)
        if not isinstance(observed, str) or observed != value:
            raise ManifestError(
                f"check {check_id!r} {field} must equal fixed Stage8 value {value!r}"
            )
    capacity = check.get("capacity")
    if type(capacity) is not int or capacity != 1:
        raise ManifestError(
            f"check {check_id!r} capacity must equal fixed Stage8 value 1"
        )
    exact_values = {
        "command": ["python3", "tools/run_stage3_verification.py"],
        "stage3_mode": _stage3.MODE_PURE,
        "stage3_batch": policy["stage3_batch"],
        "depends_on": policy["depends_on"],
        "authority": "none",
    }
    for field, value in exact_values.items():
        if check.get(field) != value or type(check.get(field)) is not type(value):
            raise ManifestError(
                f"check {check_id!r} {field} must equal fixed Stage8 value {value!r}"
            )
    declared_inputs = check.get("inputs")
    if not isinstance(declared_inputs, list) or not all(
        isinstance(item, str) for item in declared_inputs
    ):
        raise ManifestError(f"check {check_id!r} inputs must be a Stage8 string list")
    missing_inputs = sorted(set(policy["required_inputs"]) - set(declared_inputs))
    if missing_inputs:
        raise ManifestError(
            f"check {check_id!r} omits required Stage8 inputs: {missing_inputs}"
        )


def _lock_owner(check: Mapping[str, Any]) -> str:
    """Return the reviewed lock owner, defaulting to the verifier runner.

    A command may own a lease only through the fixed Stage 3 wrapper.  Keeping
    this policy in manifest validation makes a plain command unable to opt out
    of the parent lock merely by adding a metadata field at run time.
    """

    raw = check.get("lock_owner", "runner")
    if raw not in LOCK_OWNERS:
        raise ManifestError(
            f"check {check.get('id')!r} lock_owner must be 'runner' or 'command'"
        )
    owner = str(raw)
    check_id = str(check.get("id"))
    if (
        _is_fixed_stage_check(check_id)
        and check_id != "stage3-runner-unit"
        and owner != "command"
    ):
        raise ManifestError(
            f"fixed stage check {check_id!r} must use command-owned lock evidence"
        )
    if owner == "runner":
        return owner

    lane = check.get("lane")
    command = _parse_command(check.get("command"), check_id)
    if lane != "heavy-candidate":
        raise ManifestError(
            f"check {check_id!r} command lock ownership requires heavy-candidate lane"
        )
    if check.get("cost", "cheap") != "heavy":
        raise ManifestError(
            f"check {check_id!r} command lock ownership requires cost='heavy'"
        )
    if check.get("fresh") is not True:
        raise ManifestError(
            f"check {check_id!r} command lock ownership requires fresh=true"
        )
    if check.get("exclusive") is not True:
        raise ManifestError(
            f"check {check_id!r} command lock ownership requires exclusive=true"
        )
    lock = check.get("lock")
    try:
        canonical_lock = _stage3.canonical_lock_path(Path(str(lock)))
    except Exception as exc:
        raise ManifestError(
            f"check {check_id!r} command lock ownership requires the canonical SH-07 lock"
        ) from exc
    if canonical_lock != _stage3.CANONICAL_LOCK:
        raise ManifestError(
            f"check {check_id!r} command lock ownership requires the canonical SH-07 lock"
        )
    expected_command = ["python3", "tools/run_stage3_verification.py"]
    if command != expected_command:
        raise ManifestError(
            f"check {check_id!r} command lock ownership requires the reviewed fixed Stage 3 command"
        )
    env = check.get("env", {})
    if not isinstance(env, Mapping):
        raise ManifestError(f"check {check_id!r} env must be an object")
    reserved = sorted(set(env) & _STAGE3_RESERVED_ENV)
    if reserved:
        raise ManifestError(
            f"check {check_id!r} cannot override verifier-bound Stage 3 environment: {reserved}"
        )
    mode = _stage3_mode(check)
    batch = check.get("stage3_batch")
    if batch not in _stage3.FIXED_BATCHES:
        raise ManifestError(
            f"check {check_id!r} stage3_batch must name a reviewed fixed Stage 3 batch"
        )
    proof_batches = frozenset(getattr(_stage3, "FIXED_MODULE_POLICIES", {"authority": None}))
    if mode in {_stage3.MODE_PROOF_CANDIDATE, _stage3.MODE_REVIEWED_ATTESTATION} \
            and batch not in proof_batches:
        raise ManifestError(f"check {check_id!r} Stage 3 authority mode requires an exact fixed proof batch")
    return owner


def load_manifest(
    path: Path | str = DEFAULT_MANIFEST,
    *,
    require_production_contracts: bool | None = None,
) -> dict[str, Any]:
    """Load and validate a JSON verification manifest.

    The repository's canonical manifest is a trusted production context: its
    fixed-node contracts cannot be disabled by editing mutable JSON metadata.
    Callers may opt other paths into the same checks, while generic fixture
    manifests remain usable without production-only nodes.
    """

    manifest_path = Path(path)
    if not manifest_path.is_file():
        raise ManifestError(f"manifest does not exist: {manifest_path}")
    try:
        manifest = json.loads(manifest_path.read_text(encoding="utf-8"))
    except (OSError, UnicodeDecodeError, json.JSONDecodeError) as exc:
        raise ManifestError(f"cannot read manifest {manifest_path}: {exc}") from exc
    canonical_production_path = manifest_path.resolve() == DEFAULT_MANIFEST.resolve()
    require_fixed_contracts = (
        canonical_production_path
        or require_production_contracts is True
    )
    validate_manifest(
        manifest,
        require_production_contracts=require_fixed_contracts,
    )
    return manifest


def validate_manifest(
    manifest: Mapping[str, Any],
    *,
    require_production_contracts: bool = False,
) -> None:
    """Validate graph shape and resource declarations before any command runs."""

    if not isinstance(manifest, Mapping):
        raise ManifestError("manifest must be a JSON object")
    if manifest.get("schema_version") != SCHEMA_VERSION:
        raise ManifestError(f"manifest schema_version must be {SCHEMA_VERSION}")
    lanes = manifest.get("lanes")
    if not isinstance(lanes, Mapping):
        raise ManifestError("manifest lanes must be an object")
    unknown_lanes = set(lanes) - set(LANES)
    if unknown_lanes:
        raise ManifestError(f"unknown lanes: {sorted(unknown_lanes)}")
    checks = manifest.get("checks")
    if not isinstance(checks, list) or not checks:
        raise ManifestError("manifest checks must be a non-empty list")
    # Production-only membership is selected by the trusted load context, not
    # mutable manifest metadata.  Enforce presence before per-check validation
    # so removal, renaming, or replacement by a widened arbitrary command
    # cannot silently drop the focused owner.
    if require_production_contracts:
        launcher_count = sum(
            1
            for item in checks
            if isinstance(item, Mapping)
            and item.get("id") == _P15_NATIVE_LAUNCHER_CHECK_ID
        )
        if launcher_count != 1:
            raise ManifestError(
                "manifest must contain exactly one check id "
                f"{_P15_NATIVE_LAUNCHER_CHECK_ID!r}"
            )
        runtime_provider_count = sum(
            1
            for item in checks
            if isinstance(item, Mapping)
            and item.get("id") == _P15_NATIVE_RUNTIME_PROVIDER_CHECK_ID
        )
        if runtime_provider_count != 1:
            raise ManifestError(
                "manifest must contain exactly one check id "
                f"{_P15_NATIVE_RUNTIME_PROVIDER_CHECK_ID!r}"
            )
        observed_stage8_ids = {
            str(item.get("id"))
            for item in checks
            if isinstance(item, Mapping)
            and str(item.get("id", "")).startswith("stage8-")
        }
        expected_stage8_ids = set(_STAGE8_FIXED_NODE_POLICIES)
        if observed_stage8_ids != expected_stage8_ids:
            raise ManifestError(
                "Stage8 fixed graph ids must equal the reviewed production set: "
                f"expected {sorted(expected_stage8_ids)}, "
                f"observed {sorted(observed_stage8_ids)}"
            )

    ids: set[str] = set()
    dependencies: dict[str, list[str]] = {}
    for check in checks:
        if not isinstance(check, Mapping):
            raise ManifestError("each check must be an object")
        check_id = check.get("id")
        if not isinstance(check_id, str) or not check_id.strip():
            raise ManifestError("each check requires a non-empty id")
        if check_id in ids:
            raise ManifestError(f"duplicate check id: {check_id}")
        ids.add(check_id)
        lane = check.get("lane")
        if lane not in LANES:
            raise ManifestError(f"check {check_id!r} has invalid lane {lane!r}")
        _parse_command(check.get("command"), check_id)
        _validate_p15_native_launcher_contract(check)
        _validate_p15_native_runtime_provider_contract(check)
        _validate_stage3_resource_contract(check)
        _validate_stage3_runtime_inputs(check)
        _validate_stage8_node_contract(check)
        if check.get("daemonization") != "forbidden":
            raise ManifestError(
                f"check {check_id!r} must declare daemonization='forbidden'; "
                "cross-session containment is deferred to an OS job/container"
            )
        inputs = check.get("inputs")
        if not isinstance(inputs, list) or not inputs or not all(isinstance(item, str) and item for item in inputs):
            raise ManifestError(f"check {check_id!r} inputs must be a non-empty string list")
        for item in inputs:
            normalised = _normalise_declared_path(item)
            if not _is_safe_relative_path(normalised):
                raise ManifestError(f"check {check_id!r} input escapes repository root: {item!r}")
        impact_excludes = check.get("impact_excludes", [])
        if (not isinstance(impact_excludes, list)
                or not all(isinstance(item, str) and item for item in impact_excludes)):
            raise ManifestError(f"check {check_id!r} impact_excludes must be a string list")
        for item in impact_excludes:
            normalised = _normalise_declared_path(item)
            if not _is_safe_relative_path(normalised):
                raise ManifestError(
                    f"check {check_id!r} impact_excludes escapes repository root: {item!r}"
                )
        deps = check.get("depends_on", check.get("dependencies", []))
        if not isinstance(deps, list) or not all(isinstance(item, str) and item for item in deps):
            raise ManifestError(f"check {check_id!r} depends_on must be a string list")
        if check_id in deps:
            raise ManifestError(f"check {check_id!r} cannot depend on itself")
        dependencies[check_id] = list(deps)
        cost = check.get("cost", "cheap")
        if cost not in {"cheap", "heavy"}:
            raise ManifestError(f"check {check_id!r} cost must be cheap or heavy")
        lock = check.get("lock")
        if lock is not None and (not isinstance(lock, str) or not lock):
            raise ManifestError(f"check {check_id!r} lock must be a non-empty string or null")
        exclusive = check.get("exclusive", False)
        if not isinstance(exclusive, bool):
            raise ManifestError(f"check {check_id!r} exclusive must be boolean")
        if not isinstance(check.get("fresh", False), bool):
            raise ManifestError(f"check {check_id!r} fresh must be boolean")
        automatic = check.get("automatic", True)
        if not isinstance(automatic, bool):
            raise ManifestError(f"check {check_id!r} automatic must be boolean")
        env = check.get("env", {})
        if not isinstance(env, Mapping):
            raise ManifestError(f"check {check_id!r} env must be an object")
        if not all(isinstance(key, str) and isinstance(value, (str, int, float, bool)) for key, value in env.items()):
            raise ManifestError(f"check {check_id!r} env keys and values must be scalar")
        if cost == "heavy" and lock is None and not exclusive:
            raise ManifestError(f"heavy check {check_id!r} must declare lock or exclusive=true")
        authority = _check_authority(check, str(lane))
        owner = _lock_owner(check)
        if authority == "declared" and owner != "command":
            raise ManifestError(
                f"check {check_id!r} declared authority requires lock_owner='command'"
            )
        if owner == "command":
            stage3_mode = _stage3_mode(check)
            if stage3_mode == _stage3.MODE_REVIEWED_ATTESTATION:
                # Candidate/attestation artifacts are invocation-scoped.  A
                # manifest may provide reviewed defaults for local tooling,
                # while the verifier API/CLI can supply a fresh hand-off.
                # Validate every provided value here; _run_one performs the
                # complete presence check after applying invocation inputs.
                candidate_id = check.get("stage3_candidate_check_id")
                if candidate_id is not None and (
                        not isinstance(candidate_id, str) or not candidate_id.strip()
                        or candidate_id == check_id):
                    raise ManifestError(
                        f"check {check_id!r} reviewed-attestation candidate check id must be distinct"
                    )
                for field in (
                    "stage3_candidate_receipt_path", "stage3_candidate_state_dir",
                    "stage3_attestation_path",
                ):
                    if field not in check or check.get(field) in (None, ""):
                        continue
                    value = _normalise_declared_path(str(check[field]))
                    if not _is_safe_relative_path(value):
                        raise ManifestError(
                            f"check {check_id!r} {field} must be a safe repository-relative path"
                        )
                for field in (
                    "stage3_candidate_receipt_sha256",
                    "stage3_candidate_manifest_sha256",
                    "stage3_attestation_sha256",
                ):
                    if field not in check or check.get(field) in (None, ""):
                        continue
                    if _stage3._SHA256.fullmatch(str(check[field])) is None:
                        raise ManifestError(
                            f"check {check_id!r} {field} must be sha256:<64 lowercase hex>"
                        )
        timeout = check.get("timeout_seconds")
        if timeout is not None and (not isinstance(timeout, (int, float)) or timeout <= 0):
            raise ManifestError(f"check {check_id!r} timeout_seconds must be positive")
        cwd = check.get("cwd")
        if cwd is not None and (not isinstance(cwd, str) or not _is_safe_relative_path(_normalise_declared_path(cwd))):
            raise ManifestError(f"check {check_id!r} cwd must stay inside repository root")
        tool_inputs = check.get("tool_inputs", [])
        if not isinstance(tool_inputs, list) or not all(isinstance(item, str) and item for item in tool_inputs):
            raise ManifestError(f"check {check_id!r} tool_inputs must be a string list")
        for item in tool_inputs:
            if not _is_safe_relative_path(_normalise_declared_path(item)):
                raise ManifestError(f"check {check_id!r} tool input escapes repository root: {item!r}")

    stage8_ids = {check_id for check_id in ids if check_id.startswith("stage8-")}
    expected_stage8_ids = set(_STAGE8_FIXED_NODE_POLICIES)
    if (require_production_contracts or stage8_ids) and stage8_ids != expected_stage8_ids:
        raise ManifestError(
            "Stage8 fixed graph ids must equal the reviewed set: "
            f"expected {sorted(expected_stage8_ids)}, observed {sorted(stage8_ids)}"
        )

    for check_id, deps in dependencies.items():
        missing = sorted(set(deps) - ids)
        if missing:
            raise ManifestError(f"check {check_id!r} depends on unknown checks: {missing}")
    # A deterministic Kahn walk catches cycles and supplies a stable error.
    indegree = {check_id: len(deps) for check_id, deps in dependencies.items()}
    reverse: dict[str, list[str]] = {check_id: [] for check_id in ids}
    for check_id, deps in dependencies.items():
        for dep in deps:
            reverse[dep].append(check_id)
    ready = sorted(check_id for check_id, degree in indegree.items() if degree == 0)
    visited: list[str] = []
    while ready:
        current = ready.pop(0)
        visited.append(current)
        for child in sorted(reverse[current]):
            indegree[child] -= 1
            if indegree[child] == 0:
                ready.append(child)
                ready.sort()
    if len(visited) != len(ids):
        cycle_nodes = sorted(ids - set(visited))
        raise ManifestError(f"dependency cycle involving checks: {cycle_nodes}")


def checks_by_id(manifest: Mapping[str, Any]) -> dict[str, dict[str, Any]]:
    return {str(check["id"]): dict(check) for check in manifest["checks"]}


def dependencies_of(check: Mapping[str, Any]) -> list[str]:
    return list(check.get("depends_on", check.get("dependencies", [])))


def topological_order(manifest: Mapping[str, Any], selected_ids: Iterable[str] | None = None) -> list[str]:
    """Return a stable dependency-first order for the selected subgraph."""

    by_id = checks_by_id(manifest)
    selected = set(selected_ids) if selected_ids is not None else set(by_id)
    unknown = selected - set(by_id)
    if unknown:
        raise ManifestError(f"unknown selected checks: {sorted(unknown)}")
    indegree = {check_id: 0 for check_id in selected}
    children: dict[str, list[str]] = {check_id: [] for check_id in selected}
    for check_id in selected:
        for dep in dependencies_of(by_id[check_id]):
            if dep in selected:
                indegree[check_id] += 1
                children[dep].append(check_id)
            else:
                raise ManifestError(f"selected check {check_id!r} is missing dependency {dep!r}")
    ready = sorted(check_id for check_id, degree in indegree.items() if degree == 0)
    order: list[str] = []
    while ready:
        current = ready.pop(0)
        order.append(current)
        for child in sorted(children[current]):
            indegree[child] -= 1
            if indegree[child] == 0:
                ready.append(child)
                ready.sort()
    if len(order) != len(selected):
        raise ManifestError(f"selected graph is cyclic: {sorted(selected - set(order))}")
    return order


def _contains_glob(value: str) -> bool:
    return any(char in value for char in _GLOB_CHARS)


def _matches_change(declaration: str, changed: str) -> bool:
    declaration = _normalise_declared_path(declaration)
    changed = _normalise_declared_path(changed)
    if _contains_glob(declaration):
        candidates = [declaration]
        if "**/" in declaration:
            candidates.append(declaration.replace("**/", "", 1))
        return any(fnmatch.fnmatchcase(changed, candidate) for candidate in candidates)
    return changed == declaration or changed.startswith(declaration.rstrip("/") + "/")


def _impact_excludes_change(check: Mapping[str, Any], changed: str) -> bool:
    return any(
        _matches_change(str(declaration), changed)
        for declaration in check.get("impact_excludes", [])
    )


def _automatic_check(check: Mapping[str, Any]) -> bool:
    """Return whether a check participates in implicit change-impact routing.

    The opt-out is intentionally ignored by explicit ``--check`` and
    ``--all`` scopes.  Those callers have requested the exact graph scope;
    only ambient changed-path routing is allowed to defer a manual check.
    """

    return check.get("automatic", True) is not False


def select_impacted_checks(
    manifest: Mapping[str, Any],
    root: Path | str = ROOT,
    changed_paths: Iterable[str | Path] | None = None,
    lanes: Iterable[str] | None = None,
    requested_ids: Iterable[str] | None = None,
    all_checks: bool = False,
) -> dict[str, Any]:
    """Select checks and dependency closure with deterministic impact reasons.

    A changed input selects its owning check and all downstream checks because
    their evidence may have changed.  Dependencies are then added so the
    resulting plan is executable in isolation.  Unmatched changes remain in
    the receipt, making an accidental under-declaration visible to CI.
    """

    validate_manifest(manifest)
    root_path = Path(root).resolve()
    by_id = checks_by_id(manifest)
    lane_set = set(lanes or LANES)
    unknown_lanes = lane_set - set(LANES)
    if unknown_lanes:
        raise ManifestError(f"unknown requested lanes: {sorted(unknown_lanes)}")
    requested = set(requested_ids or [])
    if all_checks and requested:
        raise ManifestError("--all cannot be combined with --check; choose one selection mode")
    unknown_ids = requested - set(by_id)
    if unknown_ids:
        raise ManifestError(f"unknown requested checks: {sorted(unknown_ids)}")
    allowed = {check_id for check_id, check in by_id.items() if check["lane"] in lane_set}
    requested_outside_lane = sorted(check_id for check_id in requested if check_id not in allowed)
    changed = sorted({_normalise_change(root_path, path) for path in changed_paths or []})
    reasons: dict[str, list[str]] = {check_id: [] for check_id in by_id}
    if requested:
        direct = set(requested) & allowed
        for check_id in direct:
            reasons[check_id].append("explicit-check")
    elif all_checks or not changed:
        direct = set(allowed)
        reason = "all-checks" if all_checks else "default-selection"
        for check_id in direct:
            reasons[check_id].append(reason)
    else:
        direct = set()
        for check_id, check in by_id.items():
            if check_id not in allowed or not _automatic_check(check):
                continue
            declared = list(check["inputs"]) + list(check.get("tool_inputs", []))
            # Exclusions are path patterns, not a check-wide veto.  A broad
            # check may ignore its owned C7 path while still being selected by
            # a second changed path that it genuinely owns.
            active_changed = [path for path in changed if not _impact_excludes_change(check, path)]
            matches = [path for path in active_changed if any(_matches_change(item, path) for item in declared)]
            if matches:
                direct.add(check_id)
                reasons[check_id].append("changed-input:" + ",".join(matches))
            elif any(_impact_excludes_change(check, path) for path in changed):
                reasons[check_id].append("impact-excluded")
    # First close downstream so changed source evidence reaches dependents.
    # An explicitly named check is already the caller's complete scope; add
    # only its prerequisites below.  Expanding its downstream graph would turn
    # ``--check stage0-reader`` into an accidental full/heavy run.
    reverse: dict[str, list[str]] = {check_id: [] for check_id in by_id}
    for check_id, check in by_id.items():
        for dep in dependencies_of(check):
            reverse[dep].append(check_id)
    selected = set(direct) & allowed
    if not requested:
        queue = sorted(selected)
        while queue:
            current = queue.pop(0)
            for child in sorted(reverse[current]):
                if child not in selected and child in allowed and _automatic_check(by_id[child]):
                    selected.add(child)
                    reasons[child].append("downstream-of:" + current)
                    queue.append(child)
    # Then close dependencies.  Dependencies may come from another lane and
    # must be included even when a lane filter was requested.
    queue = sorted(selected)
    while queue:
        current = queue.pop(0)
        for dep in dependencies_of(by_id[current]):
            # Change-impact plans never revive a manual-only node through
            # dependency closure.  Explicit --check/--all retain the exact
            # prerequisite graph, including manual-only checks.
            if dep not in selected and (requested or _automatic_check(by_id[dep])):
                selected.add(dep)
                reasons[dep].append("dependency-of:" + current)
                queue.append(dep)
    for check_id in selected:
        if not reasons[check_id]:
            reasons[check_id].append("dependency-closure")
    selected_order = topological_order(manifest, selected)
    matches_by_path: dict[str, list[tuple[str, str]]] = {path: [] for path in changed}
    for check_id, check in by_id.items():
        if not _automatic_check(check):
            continue
        declared = list(check["inputs"]) + list(check.get("tool_inputs", []))
        for path in changed:
            if _impact_excludes_change(check, path):
                continue
            if any(_matches_change(item, path) for item in declared):
                matches_by_path[path].append((check_id, str(check["lane"])))
    unmatched = sorted(path for path, matches in matches_by_path.items() if not matches)
    # A lane-filtered plan must not silently turn a change owned by another
    # lane into an empty successful plan.  Keep the path-level list compact for
    # callers and retain the matching checks/lanes for actionable diagnostics.
    matched_outside_lane = sorted(
        path
        for path, matches in matches_by_path.items()
        if matches and not any(check_id in allowed for check_id, _lane in matches)
    )
    outside_lane_details = {
        path: [
            {"id": check_id, "lane": lane}
            for check_id, lane in sorted(matches)
            if check_id not in allowed
        ]
        for path, matches in matches_by_path.items()
        if path in matched_outside_lane
    }
    return {
        "selected_ids": selected_order,
        # Keep the selection mode explicit in receipts.  A caller that asks
        # for every check (or names a check directly) has supplied its own
        # scope; ambient changed paths are observation only in those modes.
        "selection_mode": (
            "all"
            if all_checks
            else "explicit-check"
            if requested
            else "change-impact"
            if changed
            else "default"
        ),
        "changed_paths": changed,
        "changed_paths_observed": changed,
        "lanes": sorted(lane_set, key=LANES.index),
        "reasons": {check_id: sorted(set(reasons[check_id])) for check_id in selected_order},
        "unmatched_changes": unmatched,
        "matched_outside_lane": matched_outside_lane,
        "matched_outside_lane_details": outside_lane_details,
        "requested_outside_lane": requested_outside_lane,
        "requested_outside_lane_details": {
            check_id: {"id": check_id, "lane": by_id[check_id]["lane"]}
            for check_id in requested_outside_lane
        },
    }


def _input_files(root: Path, declaration: str) -> list[tuple[str, Path | None]]:
    declaration = _normalise_declared_path(declaration)
    path = root / declaration
    if _contains_glob(declaration):
        static_prefix = declaration[: min(index for index, char in enumerate(declaration) if char in _GLOB_CHARS)]
        prefix_path = root / static_prefix.rstrip("/") if static_prefix.rstrip("/") else root
        _reject_symlink_components(root, prefix_path, declaration)
        matches: list[Path] = []
        for item in root.glob(declaration):
            _reject_symlink_components(root, item, declaration)
            if item.is_file():
                _assert_within_root(root, item, declaration)
                matches.append(item)
        matches.sort(key=lambda item: item.as_posix())
        return [(_relpath(root, item), item) for item in matches] or [(declaration, None)]
    _reject_symlink_components(root, path, declaration)
    if path.is_file():
        _assert_within_root(root, path, declaration)
        return [(_relpath(root, path), path)]
    if path.is_dir():
        _assert_within_root(root, path, declaration)
        matches = []
        for item in path.rglob("*"):
            _reject_symlink_components(root, item, declaration)
            if item.is_file():
                _assert_within_root(root, item, declaration)
                matches.append(item)
        matches.sort(key=lambda item: item.as_posix())
        return [(_relpath(root, item), item) for item in matches] or [(declaration, None)]
    return [(declaration, None)]


def _reject_symlink_components(root: Path, path: Path, declaration: str) -> None:
    """Reject a declared path containing any symlink component."""

    root_path = root.resolve()
    candidate = Path(path)
    if not candidate.is_absolute():
        candidate = root_path / candidate
    try:
        relative = candidate.absolute().relative_to(root_path)
    except ValueError as exc:
        raise VerificationError(
            f"declared input {declaration!r} resolves outside repository root: {path}"
        ) from exc
    current = root_path
    for component in relative.parts:
        current /= component
        try:
            info = os.lstat(current)
        except FileNotFoundError:
            break
        if stat.S_ISLNK(info.st_mode):
            raise VerificationError(f"declared input symlink is not allowed: {current}")


def _assert_within_root(root: Path, path: Path, declaration: str) -> None:
    try:
        path.resolve(strict=False).relative_to(root.resolve())
    except ValueError as exc:
        raise VerificationError(
            f"declared input {declaration!r} resolves outside repository root: {path}"
        ) from exc


def input_identities(check: Mapping[str, Any], root: Path | str = ROOT) -> dict[str, Any]:
    """Hash every file matched by the check's declared input patterns."""

    root_path = Path(root).resolve()
    records: list[dict[str, Any]] = []
    declarations = list(check.get("inputs", [])) + list(check.get("tool_inputs", []))
    seen: set[str] = set()
    for declaration in declarations:
        for relative, path in _input_files(root_path, declaration):
            if relative in seen:
                continue
            seen.add(relative)
            if path is None:
                records.append({"path": relative, "exists": False, "sha256": None})
            else:
                digest, opened = _hash_regular_file(path, root=root_path, relative=relative)
                records.append({"path": relative, "exists": True, "sha256": digest, "size": opened.st_size})
    records.sort(key=lambda item: item["path"])
    return {
        "declared": [_normalise_declared_path(item) for item in declarations],
        "files": records,
        "sha256": _sha256_text(_canonical(records)),
    }


def _expand_token(token: str, root: Path) -> str:
    return token.replace("{root}", str(root)).replace("{python}", sys.executable)


def command_identity(check: Mapping[str, Any], root: Path | str = ROOT) -> dict[str, Any]:
    """Capture command and executable identity used by resume decisions."""

    root_path = Path(root).resolve()
    command = [_expand_token(item, root_path) for item in _parse_command(check["command"], str(check["id"]))]
    executable = command[0]
    resolved = Path(executable)
    if not resolved.is_absolute():
        candidate = root_path / resolved
        resolved = candidate if candidate.is_file() else Path(shutil.which(executable) or executable)
    executable_record: dict[str, Any] = {"requested": executable}
    if resolved.is_file():
        resolved_target = resolved.resolve()
        executable_hash, executable_stat = _hash_regular_file(resolved_target)
        executable_record.update({"resolved": str(resolved_target), "sha256": executable_hash, "size": executable_stat.st_size})
    else:
        executable_record.update({"resolved": None, "sha256": None, "missing": True})
    execution_environment = {str(name): str(value) for name, value in os.environ.items()}
    execution_environment.update({str(k): str(v) for k, v in dict(check.get("env", {})).items()})
    # Bind the complete child environment so an ambient semantic flag cannot
    # silently reuse a cache entry.  Values are intentionally one-way hashed.
    runtime_environment = {
        name: {"present": True, "sha256": _sha256_text(value)}
        for name, value in sorted(execution_environment.items())
    }
    python_executable = Path(sys.executable).resolve()
    python_executable_hash = _sha256_file(python_executable) if python_executable.is_file() else None
    manifest_env = {
        name: {"present": True, "sha256": _sha256_text(value)}
        for name, value in sorted((str(k), str(v)) for k, v in dict(check.get("env", {})).items())
    }
    return {
        "root": str(root_path),
        "argv": command,
        "executable": executable_record,
        "runtime": {
            "python": platform.python_implementation(),
            "python_version": platform.python_version(),
            "platform": platform.platform(aliased=True),
            "python_executable": str(python_executable),
            "python_executable_sha256": python_executable_hash,
            "environment": runtime_environment,
        },
        "cwd": _normalise_declared_path(str(check.get("cwd", "."))),
        # Values participate in identity but are never copied into receipts or
        # cache entries; names and one-way bindings are sufficient for reuse.
        "env": manifest_env,
    }


def _supervision_marker(identity: Mapping[str, Any]) -> str:
    return _sha256_text("gravity-supervision:" + _canonical(identity))[:32]


def _marker_from_bound_identity(identity: Mapping[str, Any]) -> str:
    unbound = json.loads(_canonical(identity))
    unbound["command"]["runtime"].pop("supervision_environment", None)
    return _supervision_marker(unbound)


def check_identity(check: Mapping[str, Any], root: Path | str = ROOT) -> dict[str, Any]:
    identity = {
        "id": check["id"],
        "lane": check["lane"],
        "depends_on": dependencies_of(check),
        "command": command_identity(check, root),
        "inputs": input_identities(check, root),
        "cost": check.get("cost", "cheap"),
        "lock": check.get("lock"),
        "lock_owner": _lock_owner(check),
        "exclusive": bool(check.get("exclusive", False)),
        "authority": _check_authority(check, str(check["lane"])),
        "daemonization": check["daemonization"],
    }
    marker = _supervision_marker(identity)
    identity["command"]["runtime"]["supervision_environment"] = {
        "_GRAVITY_VERIFIER_RUN": {"present": True, "sha256": _sha256_text(marker)}
    }
    return identity


def cache_key(manifest: Mapping[str, Any], check: Mapping[str, Any], root: Path | str = ROOT) -> str:
    return _cache_key_for_identity(manifest, check_identity(check, root))


def _cache_key_for_identity(manifest: Mapping[str, Any], identity: Mapping[str, Any]) -> str:
    payload = {"schema_version": manifest["schema_version"], "name": manifest.get("name"), "check": identity}
    return _sha256_text(_canonical(payload))


def _read_json(path: Path) -> Any:
    path = _canonical_safe_path(path)
    try:
        parent_descriptor, _parent_path = _open_directory_chain(path.parent, create=False, label="cache")
    except LockUnavailable:
        return None
    try:
        parent_before = os.fstat(parent_descriptor)
        flags = os.O_RDONLY | getattr(os, "O_CLOEXEC", 0) | getattr(os, "O_NOFOLLOW", 0)
        descriptor = os.open(path.name, flags, dir_fd=parent_descriptor)
        try:
            info = os.fstat(descriptor)
            if not stat.S_ISREG(info.st_mode) or info.st_nlink != 1:
                return None
            with os.fdopen(descriptor, "r", encoding="utf-8") as stream:
                descriptor = -1
                return json.load(stream)
        finally:
            if descriptor != -1:
                os.close(descriptor)
            _assert_directory_stable(parent_descriptor, parent_before, label="cache")
    except (OSError, UnicodeDecodeError, json.JSONDecodeError, LockUnavailable):
        return None
    finally:
        os.close(parent_descriptor)


def _absolute_preserving_symlink(path: Path | str) -> Path:
    """Make a path absolute without resolving its final symlink target."""

    return Path(path).expanduser().absolute()


def _canonical_safe_path(path: Path | str) -> Path:
    """Canonicalize only known system aliases, never arbitrary user symlinks."""

    absolute = _absolute_preserving_symlink(path)
    trusted = {Path("/tmp"): Path("/private/tmp"), Path("/var"): Path("/private/var")}
    for alias, target in trusted.items():
        if absolute == alias or alias in absolute.parents:
            try:
                if alias.is_symlink() and alias.resolve() == target:
                    return target / absolute.relative_to(alias)
            except OSError:
                pass
    return absolute


def _directory_identity(info: os.stat_result) -> tuple[Any, ...]:
    return (info.st_dev, info.st_ino, info.st_mode, info.st_uid)


def _validate_directory_descriptor(descriptor: int, path: Path, *, label: str) -> os.stat_result:
    info = os.fstat(descriptor)
    if not stat.S_ISDIR(info.st_mode):
        raise LockUnavailable(f"{label} parent is not a directory: {path}")
    getuid = getattr(os, "getuid", None)
    trusted = {Path("/tmp"), Path("/private/tmp"), Path("/var"), Path("/private/var")}
    if getuid is not None and info.st_uid != getuid() and path not in trusted and path != Path(path.anchor or "/"):
        # Root-owned, non-writable system ancestors are safe traversal points;
        # writable ancestors must belong to the current user.
        if stat.S_IMODE(info.st_mode) & 0o022:
            raise LockUnavailable(f"unsafe {label} parent owner: {path}")
    # Do not permit a user-controlled group/other-writable cache/lock parent.
    # System temp directories are trusted because they are sticky and shared.
    if path not in trusted and stat.S_IMODE(info.st_mode) & 0o022:
        raise LockUnavailable(f"unsafe writable {label} parent: {path}")
    return info


def _open_directory_chain(path: Path, *, create: bool, label: str) -> tuple[int, Path]:
    """Open a directory through no-follow dirfd traversal.

    Keeping the final parent descriptor means a pathname swap cannot redirect
    a subsequent lock, cache read, or atomic publication to another tree.
    """

    absolute = _canonical_safe_path(path)
    nofollow = getattr(os, "O_NOFOLLOW", 0)
    flags = os.O_RDONLY | getattr(os, "O_CLOEXEC", 0) | nofollow | getattr(os, "O_DIRECTORY", 0)
    anchor = Path(absolute.anchor or os.curdir)
    try:
        descriptor = os.open(anchor, flags)
    except OSError as exc:
        raise LockUnavailable(f"cannot open {label} parent {absolute}: {exc}") from exc
    current = descriptor
    current_path = anchor
    try:
        _validate_directory_descriptor(current, anchor, label=label)
        for component in absolute.parts[1:] if absolute.is_absolute() else absolute.parts:
            try:
                next_descriptor = os.open(component, flags, dir_fd=current)
            except FileNotFoundError:
                if not create:
                    raise LockUnavailable(f"{label} parent does not exist: {absolute}")
                try:
                    os.mkdir(component, 0o700, dir_fd=current)
                except FileExistsError:
                    pass
                next_descriptor = os.open(component, flags, dir_fd=current)
            current_path = current_path / component
            _validate_directory_descriptor(next_descriptor, current_path, label=label)
            os.close(current)
            current = next_descriptor
        return current, absolute
    except BaseException:
        os.close(current)
        raise


def _assert_directory_stable(descriptor: int, before: os.stat_result, *, label: str) -> None:
    after = os.fstat(descriptor)
    if _directory_identity(before) != _directory_identity(after):
        raise LockUnavailable(f"{label} parent changed during operation")


def _validate_safe_parent(path: Path) -> None:
    """Reject symlinked or non-directory path components before file writes."""

    absolute = _absolute_preserving_symlink(path)
    current = Path(absolute.anchor or os.curdir)
    for component in absolute.parts[1:] if absolute.is_absolute() else absolute.parts:
        current /= component
        try:
            info = os.lstat(current)
        except FileNotFoundError:
            continue
        if stat.S_ISLNK(info.st_mode):
            trusted = {Path("/tmp"): Path("/private/tmp"), Path("/var"): Path("/private/var")}
            if current in trusted and current.resolve() == trusted[current]:
                continue
            raise LockUnavailable(f"unsafe symlink path component: {current}")
        if not stat.S_ISDIR(info.st_mode):
            raise LockUnavailable(f"lock/cache parent is not a directory: {current}")


def _validate_existing_regular_owned(path: Path, *, label: str) -> None:
    """Reject symlinks, special files, and files owned by another uid."""

    try:
        info = os.lstat(path)
    except FileNotFoundError:
        return
    if stat.S_ISLNK(info.st_mode):
        raise LockUnavailable(f"unsafe {label} symlink: {path}")
    if not stat.S_ISREG(info.st_mode):
        raise LockUnavailable(f"unsafe {label} file type: {path}")
    if info.st_nlink != 1:
        raise LockUnavailable(f"unsafe {label} hardlink: {path}")
    getuid = getattr(os, "getuid", None)
    if getuid is not None and info.st_uid != getuid():
        raise LockUnavailable(f"unsafe {label} owner: {path}")


def _open_safe_regular(path: Path, *, label: str, mode: int = 0o600):
    """Open a lock/cache file without following a final symlink.

    ``lstat`` handles already-existing unsafe files and ``O_NOFOLLOW`` closes
    the creation/replacement race on hosts that provide it.  The descriptor is
    checked again with ``fstat`` so a platform without ``O_NOFOLLOW`` still
    fails closed after a race rather than truncating an arbitrary target.
    """

    path = _canonical_safe_path(path)
    parent_descriptor, parent_path = _open_directory_chain(path.parent, create=True, label=label)
    parent_before = os.fstat(parent_descriptor)
    flags = os.O_RDWR | os.O_CREAT
    nofollow = getattr(os, "O_NOFOLLOW", 0)
    if nofollow:
        flags |= nofollow
    try:
        descriptor = os.open(path.name, flags, mode, dir_fd=parent_descriptor)
    except OSError as exc:
        os.close(parent_descriptor)
        raise LockUnavailable(f"cannot safely open {label} {path}: {exc}") from exc
    try:
        info = os.fstat(descriptor)
        if not stat.S_ISREG(info.st_mode):
            raise LockUnavailable(f"unsafe {label} file type: {path}")
        if info.st_nlink != 1:
            raise LockUnavailable(f"unsafe {label} hardlink: {path}")
        getuid = getattr(os, "getuid", None)
        if getuid is not None and info.st_uid != getuid():
            raise LockUnavailable(f"unsafe {label} owner: {path}")
        if stat.S_IMODE(info.st_mode) & 0o077:
            # Existing lock files from an older runner may be broader than
            # 0600.  Narrow them through the already-validated descriptor;
            # never widen permissions or chmod an unowned file.
            try:
                os.fchmod(descriptor, mode)
            except OSError as exc:
                raise LockUnavailable(f"cannot secure {label} permissions: {path}: {exc}") from exc
            info = os.fstat(descriptor)
            if stat.S_IMODE(info.st_mode) & 0o077:
                raise LockUnavailable(f"unsafe {label} permissions: {path}")
        _assert_directory_stable(parent_descriptor, parent_before, label=label)
        # On platforms without O_NOFOLLOW, compare the descriptor's identity
        # with lstat after opening.  A replacement race is rejected before any
        # lock metadata is written or the file is truncated.
        if not nofollow:
            current = os.stat(path.name, dir_fd=parent_descriptor, follow_symlinks=False)
            if (
                (current.st_dev, current.st_ino) != (info.st_dev, info.st_ino)
                or stat.S_ISLNK(current.st_mode)
                or current.st_nlink != 1
            ):
                raise LockUnavailable(f"unsafe {label} replacement race: {path}")
        return os.fdopen(descriptor, "a+", encoding="ascii")
    except BaseException:
        os.close(descriptor)
        raise
    finally:
        os.close(parent_descriptor)


def load_cache(path: Path | str) -> dict[str, Any]:
    cache_path = _absolute_preserving_symlink(path)
    value = _read_json(cache_path)
    if not isinstance(value, Mapping) or value.get("schema_version") != SCHEMA_VERSION or not isinstance(value.get("checks"), Mapping):
        return {"schema_version": SCHEMA_VERSION, "checks": {}}
    return {"schema_version": SCHEMA_VERSION, "checks": dict(value["checks"])}


def _write_json(path: Path, value: Mapping[str, Any]) -> None:
    path = _canonical_safe_path(path)
    parent_descriptor, parent_path = _open_directory_chain(path.parent, create=True, label="cache")
    parent_before = os.fstat(parent_descriptor)
    temporary_name = f".{path.name}.{os.getpid()}.{time.monotonic_ns()}.tmp"
    temporary_descriptor = -1
    try:
        flags = os.O_WRONLY | os.O_CREAT | os.O_EXCL | getattr(os, "O_CLOEXEC", 0) | getattr(os, "O_NOFOLLOW", 0)
        temporary_descriptor = os.open(temporary_name, flags, 0o600, dir_fd=parent_descriptor)
        with os.fdopen(temporary_descriptor, "w", encoding="utf-8") as stream:
            temporary_descriptor = -1
            stream.write(json.dumps(value, ensure_ascii=True, indent=2, sort_keys=True) + "\n")
            stream.flush()
            os.fsync(stream.fileno())
        _assert_directory_stable(parent_descriptor, parent_before, label="cache")
        # Both names are resolved relative to the already-open parent fd.  A
        # pathname swap cannot redirect this atomic publication elsewhere.
        os.replace(temporary_name, path.name, src_dir_fd=parent_descriptor, dst_dir_fd=parent_descriptor)
        _assert_directory_stable(parent_descriptor, parent_before, label="cache")
    finally:
        if temporary_descriptor != -1:
            os.close(temporary_descriptor)
        try:
            os.unlink(temporary_name, dir_fd=parent_descriptor)
        except FileNotFoundError:
            pass
        os.close(parent_descriptor)


@contextlib.contextmanager
def _cache_process_lock(cache_path: Path):
    """Serialize cache writers across verifier processes, then reload/merge."""

    try:
        import fcntl
    except ImportError as exc:  # pragma: no cover - the project currently targets POSIX hosts
        raise LockUnavailable("host does not provide POSIX file locking") from exc
    cache_path = _absolute_preserving_symlink(cache_path)
    logical_identity = str(_canonical_safe_path(cache_path))
    lock_path = _cache_lock_path(cache_path)
    with _open_safe_regular(lock_path, label="cache lock") as stream:
        # Cache writes are short and must not lose another process's entries;
        # unlike check resources this lock intentionally waits for its owner.
        fcntl.flock(stream.fileno(), fcntl.LOCK_EX)
        try:
            stream.seek(0)
            stream.truncate()
            stream.write(f"pid={os.getpid()} cache={logical_identity}\n")
            stream.flush()
            yield lock_path
        finally:
            fcntl.flock(stream.fileno(), fcntl.LOCK_UN)


def _cache_lock_path(cache_path: Path | str) -> Path:
    logical_identity = str(_canonical_safe_path(cache_path))
    return Path("/private/tmp") / f"gravity-development-cache-{_sha256_text(logical_identity)[:24]}.lock"


def _resource_lock_path(lock_name: str) -> Path:
    if Path(lock_name).is_absolute():
        path = _canonical_safe_path(lock_name)
        if path.parent != Path("/private/tmp"):
            raise LockUnavailable("resource locks must be direct children of /private/tmp")
        return path
    return Path("/private/tmp") / f"gravity-development-{_sha256_text(lock_name)[:16]}.lock"


def _trim_output(value: str) -> str:
    if len(value.encode("utf-8", errors="replace")) <= _MAX_OUTPUT_BYTES:
        return value
    data = value.encode("utf-8", errors="replace")[:_MAX_OUTPUT_BYTES]
    return data.decode("utf-8", errors="replace") + "\n[output truncated]"


def _effective_lock(check: Mapping[str, Any]) -> str | None:
    if check.get("exclusive"):
        return str(check.get("lock") or "__exclusive__")
    if check.get("cost", "cheap") == "heavy":
        return str(check.get("lock") or "__heavy__")
    return str(check["lock"]) if check.get("lock") else None


@contextlib.contextmanager
def _process_lock(lock_name: str | None):
    """Acquire a host-wide non-blocking lock for a declared heavy resource."""

    if lock_name is None:
        yield None
        return
    try:
        import fcntl
    except ImportError as exc:  # pragma: no cover - the project currently targets POSIX hosts
        raise LockUnavailable("host does not provide POSIX file locking") from exc
    path = _resource_lock_path(lock_name)
    if path == _sh07.canonical_shared_lock_path(_sh07.DEFAULT_LOCK):
        handle = _sh07.open_lock_file(path)
        try:
            try:
                fcntl.flock(handle.descriptor, fcntl.LOCK_EX | fcntl.LOCK_NB)
            except BlockingIOError as exc:
                raise LockUnavailable(f"shared resource lock is busy: {lock_name}") from exc
            handle.migrate_legacy_mode_after_exclusive_lock()
            try:
                try:
                    yield handle.path
                except BaseException as body_error:
                    try:
                        handle.validate()
                    except _sh07.CheckpointError as validation_error:
                        raise LockUnavailable(str(validation_error)) from body_error
                    raise
                else:
                    handle.validate()
            finally:
                fcntl.flock(handle.descriptor, fcntl.LOCK_UN)
        except _sh07.CheckpointError as exc:
            raise LockUnavailable(str(exc)) from exc
        finally:
            handle.close()
        return
    with _open_safe_regular(path, label="resource lock") as stream:
        try:
            fcntl.flock(stream.fileno(), fcntl.LOCK_EX | fcntl.LOCK_NB)
        except BlockingIOError as exc:
            raise LockUnavailable(f"shared resource lock is busy: {lock_name}") from exc
        try:
            stream.seek(0)
            stream.truncate()
            stream.write(f"pid={os.getpid()} lock={lock_name}\n")
            stream.flush()
            yield path
        finally:
            fcntl.flock(stream.fileno(), fcntl.LOCK_UN)


def parallel_ready_groups(manifest: Mapping[str, Any], selected_ids: Iterable[str] | None = None) -> list[list[str]]:
    """Compute deterministic dependency waves honoring heavy/exclusive locks."""

    validate_manifest(manifest)
    by_id = checks_by_id(manifest)
    selected = set(selected_ids) if selected_ids is not None else set(by_id)
    order = topological_order(manifest, selected)
    remaining = set(order)
    complete: set[str] = set()
    groups: list[list[str]] = []
    while remaining:
        ready = sorted(check_id for check_id in remaining if set(dependencies_of(by_id[check_id])) <= complete)
        if not ready:
            raise ManifestError("unable to schedule selected checks; dependency graph is not executable")
        cheap = [check_id for check_id in ready if _effective_lock(by_id[check_id]) is None]
        if cheap:
            group = cheap
        else:
            group = [ready[0]]
        groups.append(group)
        complete.update(group)
        remaining.difference_update(group)
    return groups


def _stat_signature(path: Path) -> tuple[Any, ...] | None:
    """Return metadata for both a declared path and its symlink target."""

    try:
        link = os.lstat(path)
    except FileNotFoundError:
        return None
    except OSError as exc:
        return ("error", type(exc).__name__, str(exc))
    values: list[Any] = []
    for info in (link, _safe_stat(path)):
        if info is None:
            values.append(None)
            continue
        values.append(
            (
                info.st_dev,
                info.st_ino,
                info.st_mode,
                info.st_nlink,
                info.st_size,
                getattr(info, "st_mtime_ns", int(info.st_mtime * 1_000_000_000)),
                getattr(info, "st_ctime_ns", int(info.st_ctime * 1_000_000_000)),
            )
        )
    return tuple(values)


def _safe_stat(path: Path):
    try:
        return path.stat()
    except FileNotFoundError:
        return None
    except OSError as exc:
        return type("StatError", (), {"st_dev": "error", "st_ino": type(exc).__name__, "st_mode": str(exc), "st_nlink": 0, "st_size": 0, "st_mtime": 0, "st_ctime": 0})()


def _watch_directory_for_declaration(root: Path, declaration: str) -> Path:
    """Return the nearest existing directory whose events affect a declaration."""

    normalised = _normalise_declared_path(declaration)
    candidate = root / normalised
    if _contains_glob(normalised):
        first_glob = min(index for index, char in enumerate(normalised) if char in _GLOB_CHARS)
        prefix = normalised[:first_glob]
        candidate = root / (prefix.rstrip("/") if prefix.rstrip("/") else ".")
        if not candidate.is_dir():
            candidate = candidate.parent
    elif not candidate.is_dir():
        candidate = candidate.parent
    root_path = root.resolve()
    while not candidate.exists() and candidate != root_path:
        candidate = candidate.parent
    _assert_within_root(root_path, candidate, declaration)
    if candidate.is_symlink():
        raise VerificationError(f"declared input watch directory is a symlink: {candidate}")
    if not candidate.is_dir():
        raise VerificationError(f"declared input watch directory is not a directory: {candidate}")
    return candidate.resolve()


def _watch_directories_for_declaration(root: Path, declaration: str) -> list[Path]:
    """Enumerate vnode directories needed to observe glob/subtree membership."""

    normalised = _normalise_declared_path(declaration)
    base = _watch_directory_for_declaration(root, normalised)
    recursive = "**" in normalised or (root / normalised).is_dir()
    paths = [base]
    if recursive:
        for item in base.rglob("*"):
            if item.is_symlink():
                raise VerificationError(f"declared input watch directory is a symlink: {item}")
            if item.is_dir():
                _assert_within_root(root, item, normalised)
                paths.append(item.resolve())
    return sorted(set(paths), key=lambda item: item.as_posix())


def _exact_file_watch_directories(root: Path, declaration: str) -> list[Path]:
    """Return every directory component that contains an existing exact file."""

    normalised = _normalise_declared_path(declaration)
    declared_path = root / normalised
    if _contains_glob(normalised) or not declared_path.is_file():
        return []
    root_path = root.resolve()
    current = root_path
    directories = [root_path]
    for component in Path(normalised).parts[:-1]:
        current /= component
        _assert_within_root(root_path, current, declaration)
        if current.is_symlink():
            raise VerificationError(f"declared input watch directory is a symlink: {current}")
        if not current.is_dir():
            raise VerificationError(f"declared input watch directory is not a directory: {current}")
        directories.append(current.resolve())
    return directories


def _monitor_snapshot(check: Mapping[str, Any], root: Path) -> dict[str, Any]:
    """Capture bounded metadata for declared inputs and the command binary.

    The monitor uses a kernel vnode stream when available and drains it after
    the command exits.  Stable snapshots are still captured before/after the
    command for diagnostics; an event remains evidence even if bytes are
    restored before the final snapshot.
    """

    files: dict[str, Any] = {}
    declarations = list(check.get("inputs", [])) + list(check.get("tool_inputs", []))
    try:
        for declaration in declarations:
            for relative, path in _input_files(root, declaration):
                if relative not in files:
                    files[relative] = _stat_signature(path) if path is not None else None
    except OSError as exc:
        return {"error": f"input-observation-error: {exc}", "files": files, "command": None}
    try:
        executable = command_identity(check, root)["executable"].get("resolved")
        command_signature = _stat_signature(Path(executable)) if executable else None
    except OSError as exc:
        return {"error": f"command-observation-error: {exc}", "files": files, "command": None}
    return {"files": files, "command": command_signature}


class _MutationMonitor:
    """Watch declared identities for transient mutations during execution.

    Darwin/BSD hosts use a kqueue vnode watch armed before the child starts;
    events remain queued even when a file is changed and restored between
    observations.  Other hosts retain a metadata poller only as a diagnostic
    fallback and mark the execution non-cacheable because polling cannot
    provide the same event guarantee.
    """

    def __init__(self, check: Mapping[str, Any], root: Path, interval: float = _MUTATION_POLL_SECONDS):
        self.check = check
        self.root = root
        self.interval = interval
        self._stop = threading.Event()
        self._thread: threading.Thread | None = None
        self._baseline = _monitor_snapshot(check, root)
        self._observations: list[dict[str, Any]] = []
        self._lock = threading.Lock()
        self._kqueue: Any = None
        self._watch_fds: dict[int, str] = {}
        self._watch_event_masks: dict[int, int] = {}
        self._mode = "unavailable"
        self._cacheable = False
        self._unavailable = False
        self._setup_kqueue()

    def _setup_kqueue(self) -> None:
        kqueue_factory = getattr(select, "kqueue", None)
        vnode_filter = getattr(select, "KQ_FILTER_VNODE", None)
        if kqueue_factory is None or vnode_filter is None:
            self._mode = "polling-fallback"
            self._record_unavailable("kqueue vnode monitoring is unavailable")
            return
        try:
            flags = getattr(select, "KQ_EV_ADD", 0) | getattr(select, "KQ_EV_ENABLE", 0) | getattr(select, "KQ_EV_CLEAR", 0)
            vnode_flags = 0
            for name in ("KQ_NOTE_WRITE", "KQ_NOTE_DELETE", "KQ_NOTE_EXTEND", "KQ_NOTE_ATTRIB", "KQ_NOTE_LINK", "KQ_NOTE_RENAME", "KQ_NOTE_REVOKE"):
                vnode_flags |= getattr(select, name, 0)
            meaningful_flags = self._meaningful_vnode_flags()
            exact_directory_flags = self._exact_directory_vnode_flags()
            if not vnode_flags or not meaningful_flags or not exact_directory_flags:
                raise OSError("kqueue vnode mutation flags are unavailable")

            watch_specs: list[tuple[str, Path, int, int]] = []
            declarations = list(self.check.get("inputs", [])) + list(self.check.get("tool_inputs", []))
            for declaration in declarations:
                matches = _input_files(self.root, declaration)
                normalised = _normalise_declared_path(declaration)
                declared_path = self.root / normalised
                exact_file = not _contains_glob(normalised) and declared_path.is_file()
                # Exact files retain a precise file-vnode watch.  Their path
                # components are also watched, but only self delete/rename/
                # revoke events count; directory WRITE/EXTEND events merely
                # report unrelated sibling membership changes.  A directory
                # declaration, glob, or missing exact path needs the broader
                # membership watch instead.
                if exact_file:
                    for index, directory in enumerate(_exact_file_watch_directories(self.root, declaration)):
                        watch_specs.append(
                            (
                                f"<exact-directory:{normalised}:{index}>",
                                directory,
                                exact_directory_flags,
                                exact_directory_flags,
                            )
                        )
                needs_directory_watch = (
                    not exact_file
                    and (
                        _contains_glob(normalised)
                        or declared_path.is_dir()
                        or not declared_path.exists()
                    )
                )
                if needs_directory_watch:
                    for index, directory in enumerate(_watch_directories_for_declaration(self.root, declaration)):
                        watch_specs.append(
                            (
                                f"<directory:{normalised}:{index}>",
                                directory,
                                vnode_flags,
                                meaningful_flags,
                            )
                        )
                for relative, path in matches:
                    if path is None:
                        # Missing exact/glob members are covered by the parent
                        # or subtree directory watches above; they are not an
                        # initialization failure.
                        continue
                    watch_specs.append((relative, path.resolve(), vnode_flags, meaningful_flags))
            executable = command_identity(self.check, self.root)["executable"].get("resolved")
            if executable is None:
                raise OSError("command executable cannot be watched coherently")
            watch_specs.append(("<command-executable>", Path(executable).resolve(), vnode_flags, meaningful_flags))
            queue = kqueue_factory()
            self._kqueue = queue
            watches_by_path: dict[Path, dict[str, Any]] = {}
            for label, path, subscription_flags, event_mask in watch_specs:
                resolved = path.resolve()
                watch = watches_by_path.setdefault(
                    resolved,
                    {"labels": set(), "subscription_flags": 0, "event_mask": 0},
                )
                watch["labels"].add(label)
                watch["subscription_flags"] |= subscription_flags
                watch["event_mask"] |= event_mask
            changes = []
            for path, watch in sorted(watches_by_path.items(), key=lambda item: item[0].as_posix()):
                open_flags = os.O_RDONLY | getattr(os, "O_CLOEXEC", 0) | getattr(os, "O_NOFOLLOW", 0)
                if path.is_dir():
                    open_flags |= getattr(os, "O_DIRECTORY", 0)
                descriptor = os.open(path, open_flags)
                self._watch_fds[descriptor] = "|".join(sorted(watch["labels"]))
                self._watch_event_masks[descriptor] = int(watch["event_mask"])
                changes.append(
                    select.kevent(
                        descriptor,
                        filter=vnode_filter,
                        flags=flags,
                        fflags=int(watch["subscription_flags"]),
                    )
                )
            queue.control(changes, 0, 0)
            self._mode = "kqueue-vnode"
            self._cacheable = True
        except (OSError, ValueError, VerificationError) as exc:
            self._record_unavailable(str(exc))
            self._close_watches()

    def _record_unavailable(self, reason: str) -> None:
        self._unavailable = True
        with self._lock:
            self._observations.append({"error": "mutation-monitor-unavailable", "detail": reason, "observed_at": _now()})

    def _close_watches(self) -> None:
        for descriptor in list(self._watch_fds):
            try:
                os.close(descriptor)
            except OSError:
                pass
        self._watch_fds.clear()
        self._watch_event_masks.clear()
        if self._kqueue is not None:
            try:
                self._kqueue.close()
            except OSError:
                pass
            self._kqueue = None

    @property
    def baseline(self) -> dict[str, Any]:
        return self._baseline

    @property
    def observations(self) -> list[dict[str, Any]]:
        with self._lock:
            return list(self._observations)

    @property
    def mode(self) -> str:
        return self._mode

    @property
    def cacheable(self) -> bool:
        return self._cacheable

    def _observe(self, snapshot: dict[str, Any]) -> None:
        if snapshot == self._baseline:
            return
        changed_files = sorted(
            path
            for path in set(self._baseline.get("files", {})) | set(snapshot.get("files", {}))
            if self._baseline.get("files", {}).get(path) != snapshot.get("files", {}).get(path)
        )
        command_changed = self._baseline.get("command") != snapshot.get("command")
        observation = {
            "changed_files": changed_files,
            "command_changed": command_changed,
            "error": snapshot.get("error"),
            "observed_at": _now(),
        }
        with self._lock:
            # One record per observed interval is useful evidence, but cap it
            # so a long-running command cannot grow the receipt without bound.
            if len(self._observations) < 32:
                self._observations.append(observation)

    @staticmethod
    def _meaningful_vnode_flags() -> int:
        flags = 0
        for name in ("KQ_NOTE_WRITE", "KQ_NOTE_DELETE", "KQ_NOTE_EXTEND", "KQ_NOTE_LINK", "KQ_NOTE_RENAME", "KQ_NOTE_REVOKE"):
            flags |= getattr(select, name, 0)
        return flags

    @staticmethod
    def _exact_directory_vnode_flags() -> int:
        flags = 0
        for name in ("KQ_NOTE_DELETE", "KQ_NOTE_RENAME", "KQ_NOTE_REVOKE"):
            flags |= getattr(select, name, 0)
        return flags

    def _vnode_event_is_mutation(self, event: Any) -> bool:
        fflags = int(event.fflags)
        event_mask = self._watch_event_masks.get(event.ident, self._meaningful_vnode_flags())
        if fflags & event_mask:
            return True
        # Exact-file containment directories deliberately ignore all events
        # outside their self delete/rename/revoke mask.  General file and
        # membership watches retain the stable-snapshot fallback for benign
        # NOTE_ATTRIB events.
        if event_mask != self._meaningful_vnode_flags():
            return False
        return _monitor_snapshot(self.check, self.root) != self._baseline

    def _poll(self) -> None:
        if self._kqueue is not None:
            while not self._stop.is_set():
                try:
                    events = self._kqueue.control(None, max(1, len(self._watch_fds)), self.interval)
                except OSError as exc:
                    self._record_unavailable(f"kqueue control failed: {exc}")
                    break
                for event in events:
                    if not self._vnode_event_is_mutation(event):
                        continue
                    with self._lock:
                        if len(self._observations) < 32:
                            self._observations.append(
                                {
                                    "event": "vnode-mutation",
                                    "path": self._watch_fds.get(event.ident, "<unknown>"),
                                    "fflags": int(event.fflags),
                                    "observed_at": _now(),
                                }
                            )
            return
        while not self._stop.wait(self.interval):
            self._observe(_monitor_snapshot(self.check, self.root))

    def start(self) -> None:
        self._thread = threading.Thread(target=self._poll, name="gravity-input-monitor", daemon=True)
        self._thread.start()

    def stop(self) -> None:
        self._stop.set()
        if self._thread is not None:
            self._thread.join(timeout=max(1.0, self.interval * 4))
        if self._kqueue is not None:
            try:
                events = self._kqueue.control(None, max(1, len(self._watch_fds)), 0)
            except OSError as exc:
                self._record_unavailable(f"kqueue drain failed: {exc}")
                events = []
            for event in events:
                if not self._vnode_event_is_mutation(event):
                    continue
                with self._lock:
                    if len(self._observations) < 32:
                        self._observations.append(
                            {
                                "event": "vnode-mutation",
                                "path": self._watch_fds.get(event.ident, "<unknown>"),
                                "fflags": int(event.fflags),
                                "observed_at": _now(),
                            }
                        )
        self._close_watches()
        # Always inspect once after process termination so a final write that
        # races the last interval is still noticed.
        self._observe(_monitor_snapshot(self.check, self.root))

    @property
    def changed(self) -> bool:
        if self._baseline.get("error"):
            return True
        return any(
            observation.get("event") == "vnode-mutation"
            or observation.get("changed_files")
            or observation.get("command_changed")
            for observation in self.observations
        )


def _output_text(value: Any) -> str:
    if value is None:
        return ""
    if isinstance(value, bytes):
        return value.decode("utf-8", errors="replace")
    return str(value)


def _bounded_stream_reader(stream: Any, maximum: int) -> tuple[threading.Thread, dict[str, Any]]:
    """Drain one text pipe in a bounded background buffer."""

    state: dict[str, Any] = {"chunks": [], "bytes": 0, "truncated": False}

    def read() -> None:
        try:
            while True:
                chunk = stream.read(4096)
                if not chunk:
                    break
                data = chunk.encode("utf-8", errors="replace") if isinstance(chunk, str) else bytes(chunk)
                if state["bytes"] < maximum:
                    remaining = maximum - state["bytes"]
                    state["chunks"].append(data[:remaining])
                state["bytes"] += len(data)
                if state["bytes"] > maximum:
                    state["truncated"] = True
        except (OSError, ValueError):
            state["truncated"] = True

    thread = threading.Thread(target=read, name="gravity-bounded-output", daemon=True)
    thread.start()
    return thread, state


def _bounded_stream_text(state: Mapping[str, Any]) -> str:
    payload = b"".join(state.get("chunks", []))
    text = payload.decode("utf-8", errors="replace")
    return text + ("\n[output truncated]" if state.get("truncated") else "")


def _process_group_alive(pid: int) -> bool:
    if os.name != "posix":
        return False
    try:
        os.killpg(pid, 0)
    except ProcessLookupError:
        return False
    except PermissionError:
        return True
    # ``killpg(..., 0)`` can report an orphaned zombie after the group leader
    # exits.  Inspect the group and treat zombie/expired entries as gone; a
    # live entry keeps the result fail-closed.
    try:
        probe = subprocess.run(
            ["ps", "-o", "pid=,stat=", "-g", str(pid)],
            capture_output=True,
            text=True,
            check=False,
            timeout=0.25,
        )
    except (OSError, subprocess.TimeoutExpired):
        return True
    rows = [line.split() for line in probe.stdout.splitlines() if line.split()]
    if not rows:
        return False
    return any(len(row) < 2 or not row[1].startswith(("Z", "X")) for row in rows)


def _target_process_record(pid: int) -> tuple[dict[str, Any] | None, str | None]:
    """Inspect one saved PID immediately before a signal is considered."""

    try:
        result = subprocess.run(
            ["ps", "eww", "-p", str(pid), "-o", "pid=,ppid=,pgid=,stat=,lstart=,command="],
            capture_output=True, text=True, check=False, timeout=1.0,
        )
    except (OSError, subprocess.TimeoutExpired):
        return None, "targeted process census failed"
    line = next((item for item in result.stdout.splitlines() if item.strip()), "")
    columns = line.split(maxsplit=9)
    if len(columns) < 9:
        return None, None
    try:
        observed_pid, ppid, pgid = map(int, columns[:3])
    except ValueError:
        return None, "targeted process census malformed"
    if observed_pid != pid:
        return None, None
    return {
        "ppid": ppid, "pgid": pgid, "stat": columns[3],
        "start_identity": " ".join(columns[4:9]),
        "command": columns[9] if len(columns) > 9 else "",
    }, None


def _marker_processes(marker: str) -> tuple[dict[int, dict[str, Any]], str | None]:
    """Run one bounded terminal marker census, never a high-frequency env scan."""

    try:
        result = subprocess.run(
            ["ps", "eww", "-axo", "pid=,ppid=,pgid=,stat=,lstart=,command="],
            capture_output=True, text=True, check=False, timeout=2.0,
        )
    except (OSError, subprocess.TimeoutExpired):
        return {}, "terminal marker census failed"
    found: dict[int, dict[str, Any]] = {}
    for line in result.stdout.splitlines():
        if marker not in line:
            continue
        columns = line.split(maxsplit=9)
        if len(columns) < 9:
            continue
        try:
            pid, ppid, pgid = map(int, columns[:3])
        except ValueError:
            continue
        found[pid] = {
            "ppid": ppid, "pgid": pgid, "stat": columns[3],
            "start_identity": " ".join(columns[4:9]),
        }
    return found, None


def _validated_saved_process(pid: int, saved: Mapping[str, Any], marker: str) -> tuple[bool, str | None]:
    record, error = _target_process_record(pid)
    if error:
        return False, error
    if record is None or str(record.get("stat", "")).startswith(("Z", "X")):
        return False, None
    if record.get("start_identity") != saved.get("start_identity"):
        return False, None
    if int(record.get("pgid", 0)) != int(saved.get("pgid", 0)):
        return False, None
    if marker not in str(record.get("command", "")):
        return False, None
    return True, None


class _ProcessSupervisor:
    """Arm the launch barrier without recurring whole-system process scans."""

    def __init__(self, root_pid: int, marker: str):
        self.root_pid = root_pid
        self.marker = marker
        self._observed: dict[int, dict[str, Any]] = {}
        self._error: str | None = None

    def start(self) -> bool:
        try:
            pgid = os.getpgid(self.root_pid)
        except OSError as exc:
            self._error = f"launch process group could not be captured: {exc}"
            return False
        self._observed[self.root_pid] = {
            "pgid": pgid,
            "start_identity": None,
        }
        return True

    def stop(self) -> dict[str, Any]:
        return {"processes": {pid: dict(value) for pid, value in self._observed.items()}, "error": self._error}


def _terminate_process_tree(
    process: subprocess.Popen[str], extra_processes: Mapping[int, Mapping[str, Any]] | None = None,
    marker: str | None = None,
) -> dict[str, Any]:
    """Terminate a process group and prove the group is gone before returning."""

    cleanup: dict[str, Any] = {
        "process_group": process.pid if os.name == "posix" else None,
        "term_sent": False,
        "kill_sent": False,
        "group_alive": False,
        "output_complete": True,
    }
    saved_processes = {
        int(pid): dict(value) for pid, value in (extra_processes or {}).items()
        if int(pid) > 0
    }
    escaped_pids = sorted(pid for pid in saved_processes if pid != process.pid)
    cleanup["escaped_pids"] = escaped_pids
    cleanup["escaped_alive"] = []
    cleanup["marker_alive"] = []
    cleanup["error"] = None
    def group_signal_is_safe() -> bool:
        for saved_pid, saved in saved_processes.items():
            valid, validation_error = _validated_saved_process(saved_pid, saved, marker or "")
            if validation_error:
                cleanup["error"] = validation_error
                continue
            if valid and int(saved.get("pgid", 0)) == process.pid:
                return True
        return False

    if os.name == "posix" and group_signal_is_safe():
        try:
            os.killpg(process.pid, signal.SIGTERM)
            cleanup["term_sent"] = True
        except ProcessLookupError:
            pass
        for pid in escaped_pids:
            valid, validation_error = _validated_saved_process(pid, saved_processes[pid], marker or "")
            if validation_error:
                cleanup["error"] = validation_error
                continue
            if not valid:
                continue
            try:
                os.kill(pid, signal.SIGTERM)
            except ProcessLookupError:
                pass
    elif os.name != "posix":  # pragma: no cover - the project currently targets POSIX hosts
        try:
            process.terminate()
            cleanup["term_sent"] = True
        except OSError:
            pass
    try:
        process.wait(timeout=_PROCESS_TERM_GRACE_SECONDS)
    except subprocess.TimeoutExpired:
        pass
    if _process_group_alive(process.pid):
        if os.name == "posix" and group_signal_is_safe():
            try:
                os.killpg(process.pid, signal.SIGKILL)
                cleanup["kill_sent"] = True
            except ProcessLookupError:
                pass
        elif os.name != "posix":  # pragma: no cover
            try:
                process.kill()
                cleanup["kill_sent"] = True
            except OSError:
                pass
    # A setsid child may have escaped the original process group.  Escalate
    # every validated PID even when the original group already disappeared.
    for pid in escaped_pids:
        valid, validation_error = _validated_saved_process(pid, saved_processes[pid], marker or "")
        if validation_error:
            cleanup["error"] = validation_error
            continue
        if not valid:
            continue
        try:
            os.kill(pid, signal.SIGKILL)
            cleanup["kill_sent"] = True
        except ProcessLookupError:
            pass
    try:
        process.wait(timeout=_PROCESS_KILL_GRACE_SECONDS)
    except subprocess.TimeoutExpired:
        pass
    deadline = time.monotonic() + _PROCESS_KILL_GRACE_SECONDS
    while _process_group_alive(process.pid) and time.monotonic() < deadline:
        time.sleep(0.02)
    cleanup["group_alive"] = _process_group_alive(process.pid)
    alive: list[int | str] = []
    for pid in escaped_pids:
        valid, validation_error = _validated_saved_process(pid, saved_processes[pid], marker or "")
        if validation_error:
            cleanup["error"] = validation_error
            alive.append("unknown")
        elif valid:
            alive.append(pid)
    cleanup["escaped_alive"] = alive
    cleanup["marker_alive"] = list(alive)
    return cleanup


def _cleanup_terminal_safe(cleanup: Mapping[str, Any] | None) -> bool:
    return cleanup is None or (
        cleanup.get("group_alive") is False
        and cleanup.get("escaped_alive") == []
        and cleanup.get("marker_alive") == []
        and cleanup.get("output_complete") is True
        and not cleanup.get("error")
        and not cleanup.get("census_error")
    )


def _stage3_process_tree_rss(root_pid: int) -> tuple[int | None, float, str, str]:
    """Sample process-tree RSS using the reviewed heartbeat metric helper."""

    try:
        if __package__:
            from .run_with_heartbeat import process_tree_metrics
        else:  # Direct ``python3 tools/verify_development.py`` import graph.
            from run_with_heartbeat import process_tree_metrics

        metrics = process_tree_metrics(root_pid)
        rss = metrics.get("rss_bytes")
        return (
            int(rss) if isinstance(rss, int) and rss >= 0 else None,
            _STAGE3_RSS_CADENCE_SECONDS,
            "run_with_heartbeat.process_tree_metrics-v1",
            "between-sample spikes may be missed",
        )
    except Exception:
        return (None, _STAGE3_RSS_CADENCE_SECONDS,
                "run_with_heartbeat.process_tree_metrics-v1",
                "RSS unavailable; between-sample spikes may be missed")


def _run_command(
    command: list[str],
    *,
    cwd: Path,
    env: Mapping[str, str],
    timeout: float | int | None,
    marker: str,
    sample_rss: bool = False,
) -> dict[str, Any]:
    """Run one command in an isolated process group with bounded timeout cleanup."""

    child_env = dict(env)
    child_env["_GRAVITY_VERIFIER_RUN"] = marker
    barrier_read: int | None = None
    barrier_write: int | None = None
    launch_command = list(command)
    if os.name == "posix":
        barrier_read, barrier_write = os.pipe()
        os.set_inheritable(barrier_read, True)
        child_env["_GRAVITY_VERIFIER_LAUNCH_FD"] = str(barrier_read)
        # The wrapper blocks before exec, allowing the supervisor to complete
        # its first marker/descendant census. It then execs the original argv
        # in the same process, preserving command and process-group identity.
        launch_command = [sys.executable, "-c", _LAUNCH_WRAPPER, *command]
    popen_kwargs: dict[str, Any] = {
        "cwd": str(cwd),
        "env": child_env,
        "stdout": subprocess.PIPE,
        "stderr": subprocess.PIPE,
        "text": True,
    }
    if os.name == "posix":
        popen_kwargs["start_new_session"] = True
        popen_kwargs["pass_fds"] = (barrier_read,)
    elif hasattr(subprocess, "CREATE_NEW_PROCESS_GROUP"):  # pragma: no cover
        popen_kwargs["creationflags"] = subprocess.CREATE_NEW_PROCESS_GROUP
    try:
        process = subprocess.Popen(launch_command, **popen_kwargs)
    except BaseException:
        if barrier_read is not None:
            os.close(barrier_read)
        if barrier_write is not None:
            os.close(barrier_write)
        raise
    capture_threads: list[threading.Thread] = []
    capture_states: dict[str, dict[str, Any]] = {}
    if sample_rss:
        for stream_name, stream in (("stdout", process.stdout), ("stderr", process.stderr)):
            if stream is not None:
                thread, state = _bounded_stream_reader(stream, _MAX_OUTPUT_BYTES)
                capture_threads.append(thread)
                capture_states[stream_name] = state
    if barrier_read is not None:
        os.close(barrier_read)
    supervisor = _ProcessSupervisor(process.pid, marker)
    supervisor_armed = supervisor.start()
    if barrier_write is not None and supervisor_armed:
        os.write(barrier_write, b"1")
        os.close(barrier_write)
        barrier_write = None
    if not supervisor_armed:
        # A failed first census is fail-closed: do not release the target into
        # an unsupervised run. Kill the blocked wrapper and drain its pipes.
        census = supervisor.stop()
        # The launch barrier proves this is still our blocked child. Kill and
        # reap through the live Popen handle; no PID lookup or saved identity
        # is needed, and the target command has never executed.
        process.kill()
        process.wait(timeout=_PROCESS_KILL_GRACE_SECONDS)
        cleanup = {
            "process_group": process.pid if os.name == "posix" else None,
            "term_sent": False, "kill_sent": True, "group_alive": False,
            "escaped_pids": [], "escaped_alive": [], "marker_alive": [],
            "output_complete": True, "error": census.get("error"),
        }
        if sample_rss:
            for thread in capture_threads:
                thread.join(_PROCESS_KILL_GRACE_SECONDS)
            output_complete = not any(thread.is_alive() for thread in capture_threads)
            cleanup["output_complete"] = output_complete
            stdout_text = _bounded_stream_text(capture_states.get("stdout", {}))
            stderr_text = _bounded_stream_text(capture_states.get("stderr", {}))
        else:
            try:
                stdout, stderr = process.communicate(timeout=_PROCESS_KILL_GRACE_SECONDS)
                stdout_text = _output_text(stdout)
                stderr_text = _output_text(stderr)
            except subprocess.TimeoutExpired:
                cleanup["output_complete"] = False
                stdout_text = ""
                stderr_text = ""
                for stream in (process.stdout, process.stderr):
                    if stream is not None:
                        stream.close()
        if barrier_write is not None:
            os.close(barrier_write)
        cleanup["terminal_safe"] = _cleanup_terminal_safe(cleanup)
        return {
            "returncode": process.returncode,
            "stdout": stdout_text,
            "stderr": stderr_text,
            "timed_out": False,
            "cleanup": cleanup,
            "surviving_descendants": False,
            "supervision_failed": True,
            "supervisor": census,
            "observed_peak_process_tree_rss_bytes": None,
            "rss_sampling_cadence_seconds": _STAGE3_RSS_CADENCE_SECONDS if sample_rss else None,
            "rss_sampling_contract": "run_with_heartbeat.process_tree_metrics-v1" if sample_rss else None,
            "rss_sampling_limitation": "RSS unavailable before supervised launch; between-sample spikes may be missed" if sample_rss else None,
        }
    timed_out = False
    # Wait for the leader only.  ``communicate`` waits for inherited pipe
    # descriptors held by a detached child, which would delay supervision
    # until after the child had already mutated files.
    # Keep an explicit zero when the first short-lived process exits before a
    # metric sample is available.  This is an honest nonnegative observation
    # (with the limitation string below), not an Xmx-derived estimate.
    peak_rss: int | None = 0 if sample_rss else None
    rss_contract = "run_with_heartbeat.process_tree_metrics-v1" if sample_rss else None
    rss_limitation = "between-sample spikes may be missed" if sample_rss else None
    next_sample = time.monotonic()
    deadline = None if timeout is None else time.monotonic() + max(0.0, float(timeout))
    if sample_rss:
        rss, _cadence, rss_contract, rss_limitation = _stage3_process_tree_rss(process.pid)
        if rss is not None:
            peak_rss = max(peak_rss or 0, rss)
        next_sample += _STAGE3_RSS_CADENCE_SECONDS
    while process.poll() is None:
        now = time.monotonic()
        if sample_rss and now >= next_sample:
            rss, _cadence, rss_contract, rss_limitation = _stage3_process_tree_rss(process.pid)
            if rss is not None:
                peak_rss = max(peak_rss or 0, rss)
            next_sample = now + _STAGE3_RSS_CADENCE_SECONDS
        if deadline is not None and now >= deadline:
            timed_out = True
            break
        time.sleep(min(0.05, max(0.0, (deadline - now) if deadline is not None else 0.05)))
    census = supervisor.stop()
    # A successful parent exit does not prove that its process group is gone.
    # Detached descendants can otherwise keep mutating inputs after the
    # resource lock is released.  Drain/terminate the group before returning
    # and let the caller fail the check when one was found.
    cleanup = None
    observed_processes = census.get("processes", {})
    observed_descendants = [pid for pid in observed_processes if int(pid) != process.pid]
    group_alive = _process_group_alive(process.pid)
    # Exactly one terminal whole-system marker census closes the normal-exit
    # setsid/double-fork gap without duration-scaled polling. Saved identities
    # from this census are revalidated with targeted probes before signaling.
    marker_processes, marker_error = _marker_processes(marker)
    observed_processes.update(marker_processes)
    census["error"] = census.get("error") or marker_error
    observed_descendants = [pid for pid in observed_processes if int(pid) != process.pid]
    if timed_out or observed_descendants or group_alive or census.get("error"):
        cleanup = _terminate_process_tree(process, observed_processes, marker)
        cleanup["survivors_detected"] = bool(observed_descendants or cleanup["group_alive"])
        cleanup["census_error"] = census.get("error")
    if sample_rss:
        for thread in capture_threads:
            thread.join(_PROCESS_KILL_GRACE_SECONDS)
        output_complete = not any(thread.is_alive() for thread in capture_threads)
        if cleanup is None and not output_complete:
            cleanup = {"output_complete": False}
        elif cleanup is not None:
            cleanup["output_complete"] = output_complete
        stdout_text = _bounded_stream_text(capture_states.get("stdout", {}))
        stderr_text = _bounded_stream_text(capture_states.get("stderr", {}))
    else:
        try:
            stdout, stderr = process.communicate(timeout=_PROCESS_KILL_GRACE_SECONDS)
            stdout_text = _output_text(stdout)
            stderr_text = _output_text(stderr)
        except subprocess.TimeoutExpired as exc:
            if cleanup is None:
                cleanup = _terminate_process_tree(process, observed_processes, marker)
            cleanup["output_complete"] = False
            stdout_text = _output_text(exc.stdout)
            stderr_text = _output_text(exc.stderr)
            for stream in (process.stdout, process.stderr):
                if stream is not None:
                    stream.close()
    cleanup_safe = _cleanup_terminal_safe(cleanup)
    if cleanup is not None:
        cleanup["terminal_safe"] = cleanup_safe
    return {
        "returncode": process.returncode,
        "stdout": stdout_text,
        "stderr": stderr_text,
        "timed_out": timed_out,
        "cleanup": cleanup,
        # Retain the reviewed policy signal that a descendant was observed at
        # the terminal census.  ``cleanup`` records whether it was then
        # terminated; callers use this signal to fail forbidden daemonization
        # even when no process remains alive by lease release.
        "surviving_descendants": bool(
            not timed_out
            and cleanup is not None
            and (cleanup.get("survivors_detected") or cleanup.get("census_error"))
        ),
        "supervision_failed": bool(census.get("error") or not cleanup_safe),
        "supervisor": census,
        "observed_peak_process_tree_rss_bytes": peak_rss,
        "rss_sampling_cadence_seconds": _STAGE3_RSS_CADENCE_SECONDS if sample_rss else None,
        "rss_sampling_contract": rss_contract,
        "rss_sampling_limitation": rss_limitation,
    }


def _stage3_private_directory(root: Path, check_id: str, nonce: str, *, fresh: bool) -> Path:
    """Create/validate a private 0700 directory for one Stage 3 invocation."""

    safe_check = "".join(char if char.isalnum() or char in "_.-" else "_" for char in check_id)
    if not safe_check:
        safe_check = "check"
    directory = _stage3._ensure_directory(  # type: ignore[attr-defined]
        root,
        root / ".cpcache" / "stage3-receipts" / f"{safe_check}.{nonce}",
        "Stage 3 invocation",
        mode=0o700,
        fresh=fresh,
    )
    try:
        info = os.lstat(directory)
    except OSError as exc:
        raise VerificationError("Stage 3 invocation directory cannot be inspected") from exc
    if (not stat.S_ISDIR(info.st_mode) or info.st_uid != os.geteuid()
            or stat.S_IMODE(info.st_mode) != 0o700):
        raise VerificationError("Stage 3 invocation directory is not private and owned")
    return directory


def _stage3_receipt_path(root: Path, check_id: str, nonce: str) -> Path:
    """Allocate a fresh root-contained path for one command-owned check."""

    directory = _stage3_private_directory(root, check_id, nonce, fresh=True)
    path = directory / "receipt.json"
    try:
        path.resolve(strict=False).relative_to(root.resolve())
    except ValueError as exc:
        raise VerificationError(f"Stage 3 receipt path escapes repository root: {path}") from exc
    try:
        info = os.lstat(path)
    except FileNotFoundError:
        return path
    if stat.S_ISLNK(info.st_mode) or not stat.S_ISREG(info.st_mode) or info.st_nlink != 1:
        raise VerificationError(f"Stage 3 receipt path is unsafe or pre-existing: {path}")
    raise VerificationError(f"Stage 3 receipt path already exists: {path}")


def _stage3_runner_report_path(root: Path, check_id: str, nonce: str) -> Path:
    """Allocate the root-contained fixed-runner report target."""

    directory = _stage3_private_directory(root, check_id, nonce, fresh=False)
    path = directory / "runner.json"
    try:
        path.resolve(strict=False).relative_to(root.resolve())
    except ValueError as exc:
        raise VerificationError(f"Stage 3 runner report path escapes repository root: {path}") from exc
    parent_fd, leaf = _stage3._open_parent_dirfd(root, path, "report")  # type: ignore[attr-defined]
    try:
        try:
            os.stat(leaf, dir_fd=parent_fd, follow_symlinks=False)
        except FileNotFoundError:
            return path
        raise VerificationError(f"Stage 3 runner report path already exists: {path}")
    finally:
        os.close(parent_fd)


def _read_stage3_receipt(
    path: Path, root: Path, *, with_hash: bool = False
) -> dict[str, Any] | tuple[dict[str, Any], str]:
    """Read one bounded, no-follow receipt and reject races/trailing values."""

    try:
        relative = Path(os.path.abspath(str(path))).relative_to(Path(os.path.abspath(str(root))))
    except ValueError as exc:
        raise VerificationError("Stage 3 receipt escapes repository root") from exc
    descriptor = -1
    try:
        descriptor = _open_regular_fd(path, root=root, relative=relative.as_posix())
        before = os.fstat(descriptor)
        if before.st_nlink != 1 or before.st_uid != os.geteuid():
            raise VerificationError("Stage 3 receipt is not one regular owned file")
        if before.st_size > _MAX_OUTPUT_BYTES:
            raise VerificationError("Stage 3 receipt has unsafe size")
        payload = b""
        while True:
            chunk = os.read(descriptor, _MAX_OUTPUT_BYTES + 1 - len(payload))
            payload += chunk
            if not chunk or len(payload) > _MAX_OUTPUT_BYTES:
                break
        after = os.fstat(descriptor)
    except OSError as exc:
        raise VerificationError(f"cannot read Stage 3 receipt: {exc}") from exc
    finally:
        if descriptor != -1:
            os.close(descriptor)
    if (after.st_dev, after.st_ino, after.st_nlink, after.st_size) != (
        before.st_dev, before.st_ino, before.st_nlink, before.st_size
    ):
        raise VerificationError("Stage 3 receipt changed after reading")
    if len(payload) > _MAX_OUTPUT_BYTES:
        raise VerificationError("Stage 3 receipt exceeds bounded size")
    try:
        text = payload.decode("utf-8")
        decoder = json.JSONDecoder()
        value, index = decoder.raw_decode(text)
        # Whitespace is permitted after the one JSON value, but another value
        # or arbitrary bytes are not.
        if text[index:].strip():
            raise VerificationError("Stage 3 receipt contains trailing data")
    except (UnicodeDecodeError, json.JSONDecodeError) as exc:
        raise VerificationError("Stage 3 receipt is malformed JSON") from exc
    if not isinstance(value, Mapping):
        raise VerificationError("Stage 3 receipt must be a JSON object")
    parsed = dict(value)
    if with_hash:
        return parsed, "sha256:" + hashlib.sha256(payload).hexdigest()
    return parsed


def _validate_stage3_receipt(
    receipt: Mapping[str, Any],
    *,
    check: Mapping[str, Any],
    identities: Mapping[str, Any],
    root: Path,
    receipt_path: Path,
    runner_report_path: Path | None,
    nonce: str,
    expected_returncode: int | None,
) -> dict[str, Any]:
    """Validate wrapper evidence before accepting a command-owned result."""

    errors: list[str] = []
    if receipt.get("schema") != _stage3.SCHEMA:
        errors.append("wrong receipt schema")
    if receipt.get("receipt_path") != str(receipt_path):
        errors.append("receipt path mismatch")
    if receipt.get("root") != str(root):
        errors.append("receipt root mismatch")
    if receipt.get("nonce") != nonce:
        errors.append("receipt nonce mismatch")
    if receipt.get("check_id") != check.get("id"):
        errors.append("receipt check id mismatch")
    expected_mode = _stage3_mode(check)
    expected_batch = str(check["stage3_batch"])
    proof_policy = getattr(_stage3, "FIXED_MODULE_POLICIES", {}).get(expected_batch)
    expected_proof_module = (
        proof_policy.get("module")
        if isinstance(proof_policy, Mapping) and isinstance(proof_policy.get("module"), str)
        else None
    )
    expected_proof_scope = (
        proof_policy.get("authority_scope")
        if isinstance(proof_policy, Mapping) and isinstance(proof_policy.get("authority_scope"), str)
        else None
    )
    if receipt.get("mode") != expected_mode or receipt.get("batch") != expected_batch:
        errors.append("receipt mode or fixed batch mismatch")
    if expected_mode in {_stage3.MODE_PROOF_CANDIDATE, _stage3.MODE_REVIEWED_ATTESTATION}:
        if expected_proof_module is None:
            errors.append("fixed proof policy is unavailable for receipt batch")
        if receipt.get("proof_batch") != expected_batch:
            errors.append("receipt proof batch binding mismatch")
        if receipt.get("proof_module") != expected_proof_module:
            errors.append("receipt proof module binding mismatch")
    expected_command = ["python3", "tools/run_stage3_verification.py"]
    if receipt.get("command") != expected_command:
        errors.append("receipt command mismatch")
    expected_hash = "sha256:" + _sha256_text(_canonical(identities["command"]))
    if receipt.get("command_identity_sha256") != expected_hash:
        errors.append("receipt command identity mismatch")
    if receipt.get("mode") == _stage3.MODE_PURE:
        if runner_report_path is None or receipt.get("runner_report_path") != str(runner_report_path):
            errors.append("receipt fixed-runner report path mismatch")
        if not isinstance(receipt.get("runner_report"), Mapping):
            errors.append("receipt fixed-runner report evidence is missing")
        else:
            try:
                _stage3._validate_runner_report(  # type: ignore[attr-defined]
                    receipt["runner_report"],
                    root=root,
                    report_path=runner_report_path,
                    batch=str(receipt.get("batch")),
                    nonce=nonce,
                    check_id=str(check.get("id")),
                    command_identity_sha256=str(receipt.get("command_identity_sha256")),
                )
            except Exception as report_error:
                errors.append(f"fixed-runner report revalidation failed: {report_error}")
    lock = receipt.get("lock")
    if not isinstance(lock, Mapping):
        errors.append("receipt lock evidence is missing")
    else:
        if lock.get("path") != _stage3.CANONICAL_LOCK_TEXT or lock.get("canonical_path") != _stage3.CANONICAL_LOCK_TEXT:
            errors.append("receipt lock path is not canonical")
        if lock.get("protocol") != _sh07.SHARED_LOCK_PROTOCOL:
            errors.append("receipt lock protocol mismatch")
        for field in ("acquired", "validated", "released"):
            if lock.get(field) is not True:
                errors.append(f"receipt lock lifecycle missing {field}")
        expected_owner = (
            "authoritative-child"
            if expected_mode == _stage3.MODE_PROOF_CANDIDATE
            else "reviewed-attestation-wrapper"
            if expected_mode == _stage3.MODE_REVIEWED_ATTESTATION
            else None
        )
        if expected_owner is not None and lock.get("owner") != expected_owner:
            errors.append("authority receipt does not identify the authoritative child lease owner")
        if expected_mode in {_stage3.MODE_PURE, _stage3.MODE_REVIEWED_ATTESTATION} and (
            not isinstance(lock.get("identity_before"), Mapping)
            or not isinstance(lock.get("identity_after"), Mapping)
        ):
            errors.append("pure receipt lacks before/after lock identity")
        if lock.get("identity_before") is not None and lock.get("identity_after") is not None:
            before = lock.get("identity_before")
            after = lock.get("identity_after")
            if not isinstance(before, Mapping) or not isinstance(after, Mapping):
                errors.append("receipt lock identity is malformed")
            elif any(
                type(identity.get(field)) is not int
                for identity in (before, after)
                for field in ("device", "inode", "uid", "nlink")
            ):
                errors.append("receipt lock identity fields are not strict integers")
            elif (before.get("canonical_path") != _stage3.CANONICAL_LOCK_TEXT
                  or after.get("canonical_path") != _stage3.CANONICAL_LOCK_TEXT
                  or before.get("inode") != after.get("inode")
                  or before.get("device") != after.get("device")
                  or before.get("nlink") != 1
                  or after.get("nlink") != 1
                  or before.get("mode") != "0600"
                  or after.get("mode") != "0600"):
                errors.append("receipt lock identity is not stable")
    if receipt.get("daemonization") != "forbidden":
        errors.append("receipt daemonization policy mismatch")
    top_no_survivors = receipt.get("no_surviving_descendants")
    if type(top_no_survivors) is not bool:
        errors.append("receipt descendant evidence is not a boolean")
    peak = receipt.get("observed_peak_process_tree_rss_bytes")
    cadence = receipt.get("rss_sampling_cadence_seconds")
    if type(peak) is not int or peak <= 0:
        errors.append("receipt lacks an observed process-tree RSS peak")
    cadence_valid = type(cadence) in {int, float} and cadence > 0
    if cadence_valid:
        try:
            cadence_valid = math.isfinite(float(cadence))
        except (OverflowError, ValueError):
            cadence_valid = False
    if not cadence_valid:
        errors.append("receipt RSS sampling cadence is missing")
    if receipt.get("rss_sampling_contract") != "run_with_heartbeat.process_tree_metrics-v1":
        errors.append("receipt RSS sampling contract mismatch")
    if not isinstance(receipt.get("rss_sampling_limitation"), str) \
            or "spike" not in receipt.get("rss_sampling_limitation", ""):
        errors.append("receipt RSS sampling limitation is missing")
    child = receipt.get("child")
    if not isinstance(child, Mapping):
        errors.append("receipt child evidence is missing")
    else:
        child_returncode = child.get("returncode")
        child_timed_out = child.get("timed_out")
        child_supervision_failed = child.get("supervision_failed")
        survivors = child.get("survivors")
        if type(child_returncode) is not int:
            errors.append("receipt child returncode is malformed")
        if type(child_timed_out) is not bool:
            errors.append("receipt child timeout evidence is malformed")
        if type(child_supervision_failed) is not bool:
            errors.append("receipt child supervision evidence is malformed")
        if not isinstance(survivors, list):
            errors.append("receipt child survivor evidence is malformed")
            survivors = []
        if survivors:
            errors.append("receipt child reports surviving descendants")
        child_peak = child.get("observed_peak_process_tree_rss_bytes")
        if type(child_peak) is not int or child_peak < 0 or child_peak != peak:
            errors.append("receipt child/process-tree RSS peak mismatch")
        if child.get("rss_sampling_cadence_seconds") != cadence:
            errors.append("receipt child RSS cadence mismatch")
        if child.get("rss_sampling_contract") != receipt.get("rss_sampling_contract"):
            errors.append("receipt child RSS contract mismatch")
        if child.get("rss_sampling_limitation") != receipt.get("rss_sampling_limitation"):
            errors.append("receipt child RSS limitation mismatch")
        cleanup = child.get("cleanup")
        cleanup_safe = (
            cleanup is None
            or isinstance(cleanup, Mapping) and cleanup.get("terminal_safe") is True
        )
        if cleanup is not None and not isinstance(cleanup, Mapping):
            errors.append("receipt child cleanup evidence is malformed")
        elif isinstance(cleanup, Mapping) and type(cleanup.get("terminal_safe")) is not bool:
            errors.append("receipt child terminal-safe evidence is malformed")
        elif isinstance(cleanup, Mapping) and cleanup.get("output_complete") is not True:
            errors.append("receipt child output-complete evidence is not true")
        computed_no_survivors = bool(
            not survivors and child_supervision_failed is False and cleanup_safe
        )
        if top_no_survivors is not computed_no_survivors:
            errors.append("receipt top-level descendant evidence disagrees with child cleanup")
        if receipt.get("mode") == _stage3.MODE_PURE and isinstance(receipt.get("runner_report"), Mapping):
            if receipt["runner_report"].get("exit-code") != child_returncode:
                errors.append("receipt runner report/child exit mismatch")
        if receipt.get("mode") == _stage3.MODE_PURE:
            report_path_value = receipt.get("runner_report_path")
            try:
                expected_child = _stage3.batch_command(
                    str(receipt.get("batch")),
                    report_path=Path(str(report_path_value)),
                    nonce=nonce,
                    check_id=str(check.get("id")),
                    command_identity_sha256=str(receipt.get("command_identity_sha256")),
                ) if isinstance(report_path_value, str) else None
            except Exception:
                expected_child = None
            if expected_child is None or child.get("command") != expected_child:
                errors.append("receipt fixed-runner child command mismatch")
    wrapper_exit = receipt.get("exit_code")
    wrapper_status = receipt.get("status")
    if type(wrapper_exit) is not int or wrapper_exit not in {0, 1, 75, 124}:
        errors.append("receipt exit code is malformed")
    if expected_returncode is not None and (
        type(expected_returncode) is not int or wrapper_exit != expected_returncode
    ):
        errors.append("receipt exit code mismatch")
    if wrapper_status not in {"passed", "failed"}:
        errors.append("receipt status is malformed")
    elif wrapper_status == "passed" and wrapper_exit != 0:
        errors.append("passed receipt has nonzero wrapper exit")
    elif wrapper_status == "failed" and wrapper_exit == 0:
        errors.append("failed receipt has zero wrapper exit")
    if isinstance(child, Mapping) and type(child.get("returncode")) is int \
            and type(child.get("timed_out")) is bool \
            and type(child.get("supervision_failed")) is bool:
        child_returncode = child["returncode"]
        child_timed_out = child["timed_out"]
        child_supervision_failed = child["supervision_failed"]
        child_survivors = child.get("survivors") if isinstance(child.get("survivors"), list) else []
        cleanup = child.get("cleanup")
        cleanup_safe = cleanup is None or (
            isinstance(cleanup, Mapping) and cleanup.get("terminal_safe") is True
        )
        if wrapper_exit == 0 and (
            child_returncode != 0 or child_timed_out or child_supervision_failed
            or child_survivors or not cleanup_safe
        ):
            errors.append("passed wrapper disagrees with child lifecycle")
        if child_timed_out and wrapper_exit != 124:
            errors.append("timed-out child does not map to wrapper exit 124")
        if wrapper_exit == 124 and child_timed_out is not True:
            errors.append("wrapper timeout exit lacks child timeout evidence")
        infrastructure_failed = bool(
            child_supervision_failed or child_survivors or not cleanup_safe
        )
        if infrastructure_failed and not child_timed_out and wrapper_exit != 75:
            errors.append("child supervision failure does not map to wrapper exit 75")
        if wrapper_exit == 75 and not infrastructure_failed:
            errors.append("wrapper supervision exit lacks child failure evidence")
        if wrapper_exit == 1 and (
            child_timed_out or infrastructure_failed or child_returncode != 1
        ):
            errors.append("ordinary failure receipt disagrees with child exit")
    if errors:
        raise VerificationError("invalid command-owned Stage 3 receipt: " + "; ".join(errors))
    result = dict(receipt)
    if expected_mode == _stage3.MODE_REVIEWED_ATTESTATION:
        try:
            reviewed_values = _reviewed_stage3_values(check, root)
        except VerificationError as input_error:
            raise VerificationError(str(input_error)) from input_error
        expected_bindings = {
            "candidate_check_id": reviewed_values["candidate_check_id"],
            "candidate_receipt_path": reviewed_values["candidate_receipt_path"],
            "candidate_receipt_sha256": reviewed_values["candidate_receipt_sha256"],
            "candidate_state_dir": reviewed_values["candidate_state_dir"],
            "candidate_manifest_path": reviewed_values["candidate_manifest_path"],
            "candidate_manifest_sha256": reviewed_values["candidate_manifest_sha256"],
            "attestation_path": reviewed_values["attestation_path"],
            "attestation_sha256": reviewed_values["attestation_sha256"],
        }
        for field, expected in expected_bindings.items():
            if receipt.get(field) != expected:
                raise VerificationError(f"reviewed receipt {field} binding mismatch")
        evidence = receipt.get("authority_evidence")
        if isinstance(evidence, Mapping):
            if evidence.get("state_dir") != expected_bindings["candidate_state_dir"]:
                raise VerificationError("reviewed authority state directory binding mismatch")
            if evidence.get("manifest_sha256") != expected_bindings["candidate_manifest_sha256"]:
                raise VerificationError("reviewed authority manifest hash binding mismatch")
        # A genuinely failed authoritative child is valid negative evidence:
        # preserve its failed/non-authoritative receipt for diagnostics.  An
        # exit-0 child with an invalid authority manifest is different and is
        # rejected as a mismatched receipt rather than silently downgraded.
        if receipt.get("status") == "failed" and expected_returncode not in (None, 0):
            result["authority"] = "non-authoritative"
            result["lock_owner"] = "command"
            return result
        if (receipt.get("authority") != "scoped-proof-authority"
                or receipt.get("non_authoritative") is not False
                or receipt.get("authority_scope") != "individual-source-bound-derived"):
            raise VerificationError("declared authority requires a validated scoped authority receipt")
        if receipt.get("status") != "passed" or expected_returncode != 0:
            raise VerificationError("declared authority receipt is not a matching pass")
        evidence = receipt.get("authority_evidence")
        if not isinstance(evidence, Mapping):
            raise VerificationError("declared authority receipt lacks child manifest evidence")
        if (evidence.get("state") != "completed"
                or expected_proof_module is None
                or evidence.get("module") != expected_proof_module
                or evidence.get("selected_modules") != [expected_proof_module]
                or evidence.get("aggregate_authoritative") is not False
                or evidence.get("authority_scope") != expected_proof_scope):
            raise VerificationError("declared authority manifest scope is not exact")
        if (evidence.get("lock_path") != _stage3.CANONICAL_LOCK_TEXT
                or evidence.get("lock_mode") != "0600"
                or any(evidence.get(field) is not True for field in (
                    "lock_acquired", "lock_validated", "lock_released"))):
            raise VerificationError("declared authority manifest lease lifecycle is not exact")
        record = evidence.get("module_record")
        if not isinstance(record, Mapping) or record.get("state") != "passed":
            raise VerificationError(
                f"declared authority {expected_proof_module or 'fixed-proof'} module is not structurally passed"
            )
        for field in ("stdout_sha256", "proof_contract_sha256", "module_context_fingerprint"):
            value = record.get(field)
            if not isinstance(value, str) or not value.startswith("sha256:"):
                raise VerificationError(f"declared authority evidence lacks {field}")
        state_dir = evidence.get("state_dir")
        if not isinstance(state_dir, str):
            raise VerificationError("declared authority state directory is missing")
        expected_state = Path(str(evidence.get("state_dir"))).resolve(strict=False)
        try:
            state_path = Path(state_dir)
            info = os.lstat(state_path)
            if (stat.S_ISLNK(info.st_mode) or not stat.S_ISDIR(info.st_mode)
                    or info.st_nlink < 1 or state_path.resolve(strict=False) != expected_state):
                raise VerificationError("declared authority state directory is not fresh and nonce-bound")
        except OSError as exc:
            raise VerificationError("declared authority state directory is invalid") from exc
        result["authority"] = "scoped-proof-authority"
        result["attestation_present"] = True
    elif expected_mode == _stage3.MODE_PROOF_CANDIDATE:
        if (receipt.get("status") != "passed"
                or receipt.get("proof_candidate") is not True
                or receipt.get("attestation_required") is not True
                or receipt.get("authority") != "none"
                or receipt.get("non_authoritative") is not True):
            raise VerificationError(
                "proof-candidate receipt is not a non-authoritative candidate"
            )
        evidence = receipt.get("authority_evidence")
        if not isinstance(evidence, Mapping):
            raise VerificationError("proof-candidate receipt lacks child manifest evidence")
        if (evidence.get("state") != "completed"
                or expected_proof_module is None
                or evidence.get("module") != expected_proof_module
                or evidence.get("selected_modules") != [expected_proof_module]
                or evidence.get("aggregate_authoritative") is not False
                or evidence.get("authority_scope") != expected_proof_scope
                or evidence.get("lock_path") != _stage3.CANONICAL_LOCK_TEXT
                or evidence.get("lock_mode") != "0600"
                or any(evidence.get(field) is not True for field in (
                    "lock_acquired", "lock_validated", "lock_released"))):
            raise VerificationError("proof-candidate manifest scope/lifecycle is not exact")
        candidate_record = evidence.get("module_record")
        if not isinstance(candidate_record, Mapping):
            raise VerificationError("proof-candidate module evidence is missing")
        for field in ("stdout_sha256", "proof_contract_sha256", "module_context_fingerprint"):
            value = candidate_record.get(field)
            if not isinstance(value, str) or _stage3._SHA256.fullmatch(value) is None:
                raise VerificationError(f"proof-candidate evidence lacks {field}")
        if not isinstance(evidence.get("manifest_sha256"), str) \
                or _stage3._SHA256.fullmatch(str(evidence["manifest_sha256"])) is None:
            raise VerificationError("proof-candidate manifest hash is missing")
        if not isinstance(receipt.get("candidate_manifest_path"), str):
            raise VerificationError("proof-candidate state/manifest hand-off path is missing")
        result["authority"] = "proof-candidate"
        result["proof_candidate"] = True
        result["attestation_required"] = True
    else:
        result["authority"] = "fresh-command-pass-non-authoritative" if receipt.get("status") == "passed" else "non-authoritative"
    result["lock_owner"] = "command"
    return result


_STAGE3_REVIEW_INPUT_KEYS = {
    "candidate_check_id": "stage3_candidate_check_id",
    "candidate_receipt_path": "stage3_candidate_receipt_path",
    "candidate_receipt_sha256": "stage3_candidate_receipt_sha256",
    "candidate_state_dir": "stage3_candidate_state_dir",
    "candidate_manifest_path": "stage3_candidate_manifest_path",
    "candidate_manifest_sha256": "stage3_candidate_manifest_sha256",
    "attestation_path": "stage3_attestation_path",
    "attestation_sha256": "stage3_attestation_sha256",
}


def _apply_stage3_review_inputs(
    check: Mapping[str, Any],
    inputs: Mapping[str, object] | None,
) -> dict[str, Any]:
    """Overlay one explicit reviewed-attestation hand-off on a check copy."""

    result = dict(check)
    if not inputs:
        return result
    if _stage3_mode(result) != _stage3.MODE_REVIEWED_ATTESTATION:
        raise VerificationError(
            f"check {result.get('id')!r} does not select reviewed-attestation mode"
        )
    unknown = set(inputs) - set(_STAGE3_REVIEW_INPUT_KEYS)
    if unknown:
        raise VerificationError(f"unknown Stage 3 reviewed input(s): {sorted(unknown)}")
    for public_name, manifest_name in _STAGE3_REVIEW_INPUT_KEYS.items():
        if public_name in inputs and inputs[public_name] is not None:
            result[manifest_name] = inputs[public_name]
    return result


def _reviewed_stage3_values(check: Mapping[str, Any], root: Path) -> dict[str, str]:
    """Validate and normalize all invocation-bound reviewed artifacts."""

    required = {
        "stage3_candidate_check_id",
        "stage3_candidate_receipt_path",
        "stage3_candidate_receipt_sha256",
        "stage3_candidate_state_dir",
        "stage3_candidate_manifest_sha256",
        "stage3_attestation_path",
        "stage3_attestation_sha256",
    }
    missing = sorted(field for field in required if not check.get(field))
    if missing:
        raise VerificationError(
            f"check {check.get('id')!r} reviewed-attestation inputs are missing: {missing}"
        )
    candidate_id = str(check["stage3_candidate_check_id"])
    if candidate_id == str(check.get("id")) or _stage3._SAFE_SLUG.fullmatch(candidate_id) is None:
        raise VerificationError("reviewed-attestation candidate check id is unsafe or not distinct")
    values: dict[str, str] = {"candidate_check_id": candidate_id}
    for key in (
        "stage3_candidate_receipt_path",
        "stage3_candidate_state_dir",
        "stage3_attestation_path",
    ):
        raw = check[key]
        if not isinstance(raw, (str, Path)):
            raise VerificationError(f"{key} must be a safe repository-relative path")
        raw_path = Path(raw)
        if raw_path.is_absolute():
            try:
                normalized = raw_path.resolve(strict=False).relative_to(root.resolve()).as_posix()
            except ValueError as exc:
                raise VerificationError(f"{key} must stay inside repository root") from exc
        else:
            normalized = _normalise_declared_path(str(raw))
        if not _is_safe_relative_path(normalized):
            raise VerificationError(f"{key} must be a safe repository-relative path")
        values[key.removeprefix("stage3_")] = str(root / normalized)
    # The manifest pathname is derived from the candidate state directory;
    # callers may provide one only as an equality assertion.
    derived_manifest = Path(values["candidate_state_dir"]) / "manifest.json"
    supplied_manifest = check.get("stage3_candidate_manifest_path")
    if supplied_manifest not in (None, ""):
        manifest_path_value = Path(supplied_manifest)
        if manifest_path_value.is_absolute():
            try:
                normalized_manifest = manifest_path_value.resolve(strict=False).relative_to(root.resolve()).as_posix()
            except ValueError as exc:
                raise VerificationError("candidate manifest path must stay inside repository root") from exc
        else:
            normalized_manifest = _normalise_declared_path(str(supplied_manifest))
        if not _is_safe_relative_path(normalized_manifest) or root / normalized_manifest != derived_manifest:
            raise VerificationError("candidate manifest path must be candidate_state_dir/manifest.json")
    values["candidate_manifest_path"] = str(derived_manifest)
    for key in (
        "stage3_candidate_receipt_sha256",
        "stage3_candidate_manifest_sha256",
        "stage3_attestation_sha256",
    ):
        value = str(check[key])
        if _stage3._SHA256.fullmatch(value) is None:
            raise VerificationError(f"{key} must be sha256:<64 lowercase hex>")
        values[key.removeprefix("stage3_")] = value
    return values


def _run_one(
    check: Mapping[str, Any],
    root: Path,
    identities: dict[str, Any],
    *,
    stage3_review_inputs: Mapping[str, object] | None = None,
) -> dict[str, Any]:
    started = _now()
    started_clock = time.monotonic()
    command = identities["command"]["argv"]
    cwd_value = check.get("cwd", ".")
    cwd = root / _normalise_declared_path(str(cwd_value))
    resource_receipt = check.get("resource_receipt")
    sample_rss = resource_receipt == _OBSERVED_PROCESS_TREE_RESOURCE_RECEIPT
    env = os.environ.copy()
    env.update({str(k): str(v) for k, v in dict(check.get("env", {})).items()})
    lock_owner = _lock_owner(check)
    command_owned = lock_owner == "command"
    stage3_nonce: str | None = None
    stage3_receipt_path: Path | None = None
    stage3_runner_report_path: Path | None = None
    reviewed_values: dict[str, str] | None = None
    if command_owned:
        stage3_nonce = secrets.token_hex(16)
        stage3_receipt_path = _stage3_receipt_path(root, str(check["id"]), stage3_nonce)
        stage3_runner_report_path = _stage3_runner_report_path(root, str(check["id"]), stage3_nonce)
        stage3_mode = _stage3_mode(check)
        stage3_batch = str(check["stage3_batch"])
        if stage3_mode == _stage3.MODE_REVIEWED_ATTESTATION:
            reviewed_values = _reviewed_stage3_values(
                _apply_stage3_review_inputs(check, stage3_review_inputs), root
            )
        env.update({
            _stage3.RECEIPT_ENV: str(stage3_receipt_path),
            _stage3.REPORT_ENV: str(stage3_runner_report_path),
            _stage3.NONCE_ENV: stage3_nonce,
            _stage3.CHECK_ID_ENV: str(check["id"]),
            _stage3.ROOT_ENV: str(root.resolve()),
            _stage3.MODE_ENV: stage3_mode,
            _stage3.BATCH_ENV: stage3_batch,
            _stage3.COMMAND_HASH_ENV: "sha256:" + _sha256_text(_canonical(identities["command"])),
            _stage3.EXPECTED_COMMAND_ENV: json.dumps(command, ensure_ascii=True, separators=(",", ":")),
            _stage3.TIMEOUT_ENV: str(check.get("timeout_seconds", 21600)),
        })
        if stage3_mode == _stage3.MODE_REVIEWED_ATTESTATION:
            assert reviewed_values is not None
            env.update({
                _stage3.CANDIDATE_CHECK_ID_ENV: reviewed_values["candidate_check_id"],
                _stage3.CANDIDATE_RECEIPT_ENV: reviewed_values["candidate_receipt_path"],
                _stage3.CANDIDATE_RECEIPT_SHA_ENV: reviewed_values["candidate_receipt_sha256"],
                _stage3.CANDIDATE_STATE_ENV: reviewed_values["candidate_state_dir"],
                _stage3.CANDIDATE_MANIFEST_ENV: reviewed_values["candidate_manifest_path"],
                _stage3.CANDIDATE_MANIFEST_SHA_ENV: reviewed_values["candidate_manifest_sha256"],
                _stage3.ATTESTATION_PATH_ENV: reviewed_values["attestation_path"],
                _stage3.ATTESTATION_SHA_ENV: reviewed_values["attestation_sha256"],
            })
    record: dict[str, Any] = {
        "id": check["id"],
        "lane": check["lane"],
        "command": command,
        "command_identity": identities["command"],
        "inputs": identities["inputs"],
        "lock": check.get("lock"),
        "lock_owner": lock_owner,
        "exclusive": bool(check.get("exclusive", False)),
        "cost": check.get("cost", "cheap"),
        "fresh": bool(check.get("fresh", False)),
        "resource_receipt": resource_receipt,
        "authority": "non-authoritative",
        "depends_on": dependencies_of(check),
        "started_at": started,
    }
    try:
        lock_context = contextlib.nullcontext(None) if command_owned else _process_lock(_effective_lock(check))
        with lock_context as lock_path:
            if lock_path is not None:
                record["lock_path"] = str(lock_path)
            monitor = _MutationMonitor(check, root)
            monitor.start()
            try:
                outcome = _run_command(
                    command,
                    cwd=cwd,
                    env=env,
                    timeout=check.get("timeout_seconds"),
                    marker=_marker_from_bound_identity(identities),
                    sample_rss=sample_rss,
                )
            finally:
                monitor.stop()
        record["returncode"] = None if outcome["timed_out"] else outcome["returncode"]
        record["observed_peak_process_tree_rss_bytes"] = outcome.get("observed_peak_process_tree_rss_bytes")
        record["rss_sampling_cadence_seconds"] = outcome.get("rss_sampling_cadence_seconds")
        record["rss_sampling_contract"] = outcome.get("rss_sampling_contract")
        record["rss_sampling_limitation"] = outcome.get("rss_sampling_limitation")
        record["stdout"] = _trim_output(outcome["stdout"])
        record["stderr"] = _trim_output(outcome["stderr"])
        record["status"] = "timeout" if outcome["timed_out"] else ("passed" if outcome["returncode"] == 0 else "failed")
        if outcome.get("cleanup") is not None:
            record["timeout_cleanup" if outcome["timed_out"] else "descendant_cleanup"] = outcome["cleanup"]
        record["mutation_monitor"] = {
            "mode": monitor.mode,
            "cacheable": monitor.cacheable,
            "poll_interval_seconds": monitor.interval if monitor.mode == "polling-fallback" else None,
            "observations": monitor.observations,
        }
        record["cacheable"] = monitor.cacheable
        if outcome.get("surviving_descendants"):
            record["status"] = "failed"
            record["reason"] = "surviving-descendant"
            record["stderr"] = _trim_output(
                record.get("stderr", "")
                + ("\n" if record.get("stderr") else "")
                + "command exited with a surviving descendant; process group was terminated and result was not cached"
            )
        elif outcome.get("supervision_failed"):
            record["status"] = "failed"
            record["reason"] = "process-supervision-failed"
            record["stderr"] = _trim_output(
                record.get("stderr", "")
                + ("\n" if record.get("stderr") else "")
                + "process census failed; result was not cached"
            )
        if monitor.changed:
            record["mutation_observed"] = monitor.observations
            record["status"] = "failed"
            # Preserve a stronger process-supervision failure when both a
            # descendant and a declared-input event were observed.  The
            # command is failed and never cached in either case, but the
            # receipt must retain evidence that cleanup was required.
            if "reason" not in record:
                record["reason"] = "stale-input"
            record["authority"] = "non-authoritative"
            suffix = "declared input or command identity changed during execution; result was not cached"
            record["stderr"] = _trim_output(record.get("stderr", "") + ("\n" if record.get("stderr") else "") + suffix)
        if command_owned and record["status"] in {"passed", "failed", "timeout"}:
            assert stage3_receipt_path is not None and stage3_nonce is not None
            try:
                stage3_receipt_value = _read_stage3_receipt(
                    stage3_receipt_path, root, with_hash=True
                )
                assert isinstance(stage3_receipt_value, tuple)
                stage3_receipt, stage3_receipt_sha256 = stage3_receipt_value
                stage3_receipt = _validate_stage3_receipt(
                    stage3_receipt,
                    check=check,
                    identities=identities,
                    root=root.resolve(),
                    receipt_path=stage3_receipt_path,
                    runner_report_path=stage3_runner_report_path,
                    nonce=stage3_nonce,
                    expected_returncode=(
                        124 if outcome.get("timed_out") else record.get("returncode")
                    ),
                )
                record["stage3_receipt"] = stage3_receipt
                record["stage3_receipt_sha256"] = stage3_receipt_sha256
                record["lock_evidence"] = stage3_receipt.get("lock")
                record["child_evidence"] = stage3_receipt.get("child")
                record["observed_peak_process_tree_rss_bytes"] = stage3_receipt.get(
                    "observed_peak_process_tree_rss_bytes"
                )
                record["rss_sampling_cadence_seconds"] = stage3_receipt.get(
                    "rss_sampling_cadence_seconds"
                )
                record["rss_sampling_contract"] = stage3_receipt.get("rss_sampling_contract")
                record["rss_sampling_limitation"] = stage3_receipt.get("rss_sampling_limitation")
                if record["status"] == "passed" and stage3_receipt.get("authority") == "scoped-proof-authority":
                    record["authority"] = "scoped-proof-authority"
                elif record["status"] == "passed":
                    record["authority"] = "fresh-command-pass-non-authoritative"
            except VerificationError as exc:
                record["status"] = "failed"
                record["reason"] = "invalid-command-owned-receipt"
                record["authority"] = "non-authoritative"
                record["stderr"] = _trim_output(
                    record.get("stderr", "")
                    + ("\n" if record.get("stderr") else "")
                    + str(exc)
                )
        if record["status"] == "passed" and sample_rss:
            resource_error = _resource_receipt_error(record)
            if resource_error is not None:
                record["status"] = "failed"
                record["reason"] = "invalid-resource-receipt"
                record["cacheable"] = False
                record["authority"] = "non-authoritative"
                record["stderr"] = _trim_output(
                    record.get("stderr", "")
                    + ("\n" if record.get("stderr") else "")
                    + resource_error
                )
    except LockUnavailable as exc:
        record["returncode"] = None
        record["stdout"] = ""
        record["stderr"] = str(exc)
        record["status"] = "blocked"
        record["reason"] = "lock-busy"
    except OSError as exc:
        record["returncode"] = None
        record["stdout"] = ""
        record["stderr"] = str(exc)
        record["status"] = "failed"
    record["finished_at"] = _now()
    record["duration_ms"] = round((time.monotonic() - started_clock) * 1000, 3)
    if record["status"] == "passed" and record.get("authority") != "scoped-proof-authority":
        # Exit status alone cannot establish a required output artifact or
        # bootstrap/release claim.  Keep the result explicitly non-authoritative
        # until a dedicated artifact validator is declared by the manifest.
        record["authority"] = "fresh-command-pass-non-authoritative"
    return record


def _reused_record(check: Mapping[str, Any], identities: dict[str, Any], entry: Mapping[str, Any], key: str) -> dict[str, Any]:
    timestamp = _now()
    return {
        "id": check["id"],
        "lane": check["lane"],
        "command": identities["command"]["argv"],
        "command_identity": identities["command"],
        "inputs": identities["inputs"],
        "lock": check.get("lock"),
        "lock_owner": _lock_owner(check),
        "exclusive": bool(check.get("exclusive", False)),
        "cost": check.get("cost", "cheap"),
        "fresh": bool(check.get("fresh", False)),
        "authority": "non-authoritative",
        "depends_on": dependencies_of(check),
        "status": "reused",
        "returncode": 0,
        "stdout": "",
        "stderr": "",
        "started_at": timestamp,
        "finished_at": timestamp,
        "duration_ms": 0.0,
        "cache_key": key,
        "reused_from": entry.get("finished_at") or entry.get("recorded_at"),
    }


def _cache_dependencies_match(
    check: Mapping[str, Any],
    entry: Mapping[str, Any],
    status_by_id: Mapping[str, str],
    key_by_id: Mapping[str, str],
) -> bool:
    """Require every dependency to be reused with the same cached identity."""

    dependencies = dependencies_of(check)
    if any(status_by_id.get(dep) != "reused" for dep in dependencies):
        # A dependency that ran fresh invalidates downstream reuse even when it
        # happens to return the same exit status.
        return False
    recorded = entry.get("dependencies", {})
    if not isinstance(recorded, Mapping):
        return not dependencies
    return all(
        isinstance(recorded.get(dep), Mapping)
        and recorded[dep].get("cache_key") == key_by_id.get(dep)
        and recorded[dep].get("status") == "passed"
        for dep in dependencies
    )


def run_verification(
    manifest: Mapping[str, Any] | Path | str = DEFAULT_MANIFEST,
    root: Path | str = ROOT,
    *,
    changed_paths: Iterable[str | Path] | None = None,
    lanes: Iterable[str] | None = None,
    requested_ids: Iterable[str] | None = None,
    all_checks: bool = False,
    jobs: int | None = None,
    dry_run: bool = False,
    explain: bool = False,
    resume: bool = False,
    cache_path: Path | str | None = None,
    fail_fast: bool = True,
    candidate_check_id: str | None = None,
    candidate_receipt_path: Path | str | None = None,
    candidate_receipt_sha256: str | None = None,
    candidate_state_dir: Path | str | None = None,
    candidate_manifest_path: Path | str | None = None,
    candidate_manifest_sha256: str | None = None,
    attestation_path: Path | str | None = None,
    attestation_sha256: str | None = None,
) -> dict[str, Any]:
    """Plan and optionally execute checks, returning a JSON-serializable receipt."""

    if isinstance(manifest, (str, Path)):
        manifest_path = Path(manifest).resolve()
        manifest_value = load_manifest(manifest_path)
    else:
        manifest_value = dict(manifest)
        validate_manifest(manifest_value)
        manifest_path = None
    root_path = Path(root).resolve()
    if jobs is None:
        jobs = max(1, min(32, os.cpu_count() or 1))
    if jobs < 1:
        raise VerificationError("jobs must be at least one")
    selection = select_impacted_checks(
        manifest_value,
        root_path,
        changed_paths=changed_paths,
        lanes=lanes,
        requested_ids=requested_ids,
        all_checks=all_checks,
    )
    by_id = checks_by_id(manifest_value)
    selected_ids = selection["selected_ids"]
    explicit_review_inputs = {
        key: value for key, value in {
            "candidate_check_id": candidate_check_id,
            "candidate_receipt_path": candidate_receipt_path,
            "candidate_receipt_sha256": candidate_receipt_sha256,
            "candidate_state_dir": candidate_state_dir,
            "candidate_manifest_path": candidate_manifest_path,
            "candidate_manifest_sha256": candidate_manifest_sha256,
            "attestation_path": attestation_path,
            "attestation_sha256": attestation_sha256,
        }.items() if value is not None
    }
    reviewed_selected = [
        check_id for check_id in selected_ids
        if by_id[check_id].get("lock_owner", "runner") == "command"
        and by_id[check_id].get("stage3_mode") == _stage3.MODE_REVIEWED_ATTESTATION
    ]
    if explicit_review_inputs and not reviewed_selected:
        raise VerificationError(
            "explicit Stage 3 candidate/attestation inputs require a selected reviewed-attestation check"
        )
    if explicit_review_inputs:
        # Apply invocation-scoped hand-off values only to selected reviewed
        # nodes.  The manifest identity/cache key remains the original
        # manifest; these fields are not declared source/tool inputs.
        for check_id in reviewed_selected:
            by_id[check_id] = _apply_stage3_review_inputs(
                by_id[check_id], explicit_review_inputs
            )
    if not (dry_run or explain):
        for check_id in reviewed_selected:
            _reviewed_stage3_values(by_id[check_id], root_path)
    groups = parallel_ready_groups(manifest_value, selected_ids)
    manifest_identity = {"path": _relpath(root_path, manifest_path) if manifest_path else None, "sha256": _sha256_text(_canonical(manifest_value))}
    run_started_clock = time.monotonic()
    receipt: dict[str, Any] = {
        "schema_version": SCHEMA_VERSION,
        "kind": "development-verification-receipt",
        "manifest": manifest_identity,
        "root": str(root_path),
        "selection": selection,
        "plan": {"topological_order": topological_order(manifest_value, selected_ids), "parallel_ready_groups": groups, "jobs": jobs, "fail_fast": fail_fast},
        "checks": [],
        "started_at": _now(),
        "status": "planned" if (dry_run or explain) else "running",
        "authoritative": False,
    }
    # Changed-path ownership is fail-closed only for implicit change-impact
    # selection.  ``--all`` and ``--check`` are explicit scopes, so a dirty
    # worktree (including an unrelated generated file) is recorded in the
    # selection metadata but must not veto the requested plan.  Lane ownership
    # for an explicitly requested check remains enforced below.
    impact_selection_error = (
        selection.get("selection_mode") == "change-impact"
        and (selection["unmatched_changes"] or selection.get("matched_outside_lane"))
    )
    if impact_selection_error or selection.get("requested_outside_lane"):
        # A changed path that no check declares is unsafe to ignore.  Returning
        # a failed receipt (rather than an empty passing plan) makes missing
        # manifest coverage visible to both humans and CI.
        receipt["status"] = "failed"
        errors: list[str] = []
        if impact_selection_error and selection["unmatched_changes"]:
            errors.append("unmatched changed paths: " + ", ".join(selection["unmatched_changes"]))
        if impact_selection_error and selection.get("matched_outside_lane"):
            details = selection.get("matched_outside_lane_details", {})
            rendered = []
            for path in selection["matched_outside_lane"]:
                owners = ", ".join(
                    f"{item['id']} ({item['lane']})"
                    for item in details.get(path, [])
                )
                rendered.append(f"{path} -> {owners}")
            errors.append(
                "changed paths match checks outside requested lane(s); include the owning lane or run the full graph: "
                + "; ".join(rendered)
            )
        if selection.get("requested_outside_lane"):
            details = selection.get("requested_outside_lane_details", {})
            rendered = ", ".join(
                f"{check_id} ({details[check_id]['lane']})"
                for check_id in selection["requested_outside_lane"]
            )
            errors.append(
                "requested checks are outside requested lane(s); include the owning lane or remove the lane filter: "
                + rendered
            )
        receipt["error"] = "; ".join(errors)
        receipt["checks"] = []
        receipt["finished_at"] = _now()
        receipt["duration_ms"] = 0.0
        return receipt
    if dry_run or explain:
        receipt["checks"] = [
            {
                "id": check_id,
                "lane": by_id[check_id]["lane"],
                "command": command_identity(by_id[check_id], root_path)["argv"],
                "command_identity": command_identity(by_id[check_id], root_path),
                "inputs": input_identities(by_id[check_id], root_path),
                "depends_on": dependencies_of(by_id[check_id]),
                "lock": by_id[check_id].get("lock"),
                "lock_owner": _lock_owner(by_id[check_id]),
                "exclusive": bool(by_id[check_id].get("exclusive", False)),
                "cost": by_id[check_id].get("cost", "cheap"),
                "fresh": bool(by_id[check_id].get("fresh", False)),
                "authority": "non-authoritative",
                "status": "planned",
            }
            for check_id in selected_ids
        ]
        receipt["finished_at"] = _now()
        receipt["duration_ms"] = 0.0
        receipt["status"] = "planned"
        return receipt

    cache_file = _absolute_preserving_symlink(cache_path) if cache_path is not None else DEFAULT_CACHE if root_path == ROOT else root_path / ".cpcache" / "development-verification-cache.json"
    cache = load_cache(cache_file) if resume else {"schema_version": SCHEMA_VERSION, "checks": {}}
    cache_updates: dict[str, Any] = {}
    status_by_id: dict[str, str] = {}
    key_by_id: dict[str, str] = {}
    records: dict[str, dict[str, Any]] = {}
    pending = set(selected_ids)
    failed_seen = False
    while pending:
        # Failed prerequisites are never executed, even with keep-going.
        for check_id in sorted(list(pending)):
            failed_deps = [dep for dep in dependencies_of(by_id[check_id]) if status_by_id.get(dep) in {"failed", "timeout", "blocked"}]
            if failed_deps:
                timestamp = _now()
                records[check_id] = {
                    "id": check_id,
                    "lane": by_id[check_id]["lane"],
                    "command": command_identity(by_id[check_id], root_path)["argv"],
                    "command_identity": command_identity(by_id[check_id], root_path),
                    "inputs": input_identities(by_id[check_id], root_path),
                    "depends_on": dependencies_of(by_id[check_id]),
                    "lock": by_id[check_id].get("lock"),
                    "lock_owner": _lock_owner(by_id[check_id]),
                    "exclusive": bool(by_id[check_id].get("exclusive", False)),
                    "cost": by_id[check_id].get("cost", "cheap"),
                    "authority": "non-authoritative",
                    "status": "blocked",
                    "blocked_by": failed_deps,
                    "reason": "failed-prerequisite",
                    "started_at": timestamp,
                    "finished_at": timestamp,
                    "duration_ms": 0.0,
                }
                status_by_id[check_id] = "blocked"
                pending.remove(check_id)
        if not pending:
            break
        if failed_seen and fail_fast:
            for check_id in sorted(pending):
                timestamp = _now()
                identity = check_identity(by_id[check_id], root_path)
                records[check_id] = {
                    "id": check_id,
                    "lane": by_id[check_id]["lane"],
                    "command": identity["command"]["argv"],
                    "command_identity": identity["command"],
                    "inputs": identity["inputs"],
                    "depends_on": dependencies_of(by_id[check_id]),
                    "lock": by_id[check_id].get("lock"),
                    "lock_owner": _lock_owner(by_id[check_id]),
                    "exclusive": bool(by_id[check_id].get("exclusive", False)),
                    "cost": by_id[check_id].get("cost", "cheap"),
                    "authority": "non-authoritative",
                    "status": "blocked",
                    "reason": "fail-fast",
                    "started_at": timestamp,
                    "finished_at": timestamp,
                    "duration_ms": 0.0,
                }
                status_by_id[check_id] = "blocked"
            pending.clear()
            break
        ready = sorted(check_id for check_id in pending if all(status_by_id.get(dep) in {"passed", "reused"} for dep in dependencies_of(by_id[check_id])))
        if not ready:
            raise VerificationError("no executable checks remain; dependency status is inconsistent")
        # Reuse complete, non-authoritative checks before launching anything.
        reused_any = False
        for check_id in ready:
            check = by_id[check_id]
            identities = check_identity(check, root_path)
            key = _cache_key_for_identity(manifest_value, identities)
            entry = cache["checks"].get(check_id) if resume else None
            if (entry and entry.get("status") == "passed"
                    and entry.get("cache_key") == key
                    and entry.get("cacheable", True)
                    and not check.get("fresh", False)
                    and _cache_dependencies_match(check, entry, status_by_id, key_by_id)
                    and _check_authority(check, str(check["lane"])) != "declared"):
                record = _reused_record(check, identities, entry, key)
                records[check_id] = record
                status_by_id[check_id] = "reused"
                key_by_id[check_id] = key
                pending.remove(check_id)
                reused_any = True
        if reused_any:
            continue
        # Prefer all unlocked cheap work; a heavy/locked check occupies a
        # single wave, making the resource boundary explicit in the receipt.
        cheap = [check_id for check_id in ready if _effective_lock(by_id[check_id]) is None]
        batch = cheap[:jobs] if cheap else [ready[0]]
        identities_by_id = {check_id: check_identity(by_id[check_id], root_path) for check_id in batch}
        execution_keys = {
            check_id: _cache_key_for_identity(manifest_value, identities_by_id[check_id])
            for check_id in batch
        }
        with concurrent.futures.ThreadPoolExecutor(max_workers=len(batch), thread_name_prefix="gravity-verify") as executor:
            futures = {
                executor.submit(_run_one, by_id[check_id], root_path, identities_by_id[check_id]): check_id
                for check_id in batch
            }
            results = [future.result() for future in sorted(futures, key=lambda item: futures[item])]
            for record in results:
                check_id = str(record["id"])
                executed_key = execution_keys[check_id]
                post_identity = check_identity(by_id[check_id], root_path)
                post_key = _cache_key_for_identity(manifest_value, post_identity)
                record["cache_key"] = executed_key
                if record["status"] == "passed" and post_key != executed_key:
                    record["status"] = "failed"
                    record["reason"] = "stale-input"
                    record["authority"] = "non-authoritative"
                    record["stderr"] = _trim_output(
                        record.get("stderr", "")
                        + ("\n" if record.get("stderr") else "")
                        + "declared input or command identity changed during execution; result was not cached"
                    )
                    record["post_run_identity"] = post_identity
                records[check_id] = record
                status_by_id[check_id] = record["status"]
                key_by_id[check_id] = executed_key
                pending.remove(check_id)
                if record["status"] in {"failed", "timeout"}:
                    failed_seen = True
                if record["status"] == "passed" and record.get("cacheable", True):
                    cache_updates[check_id] = {
                        "cache_key": executed_key,
                        "status": "passed",
                        "cacheable": True,
                        "recorded_at": record.get("finished_at"),
                        "finished_at": record.get("finished_at"),
                        "authority": record.get("authority"),
                        "dependencies": {
                            dep: {
                                "cache_key": key_by_id[dep],
                                "status": "passed",
                            }
                            for dep in dependencies_of(by_id[check_id])
                        },
                    }
        if failed_seen and fail_fast:
            continue
    receipt["checks"] = [records[check_id] for check_id in selected_ids]
    receipt["finished_at"] = _now()
    receipt["duration_ms"] = round((time.monotonic() - run_started_clock) * 1000, 3)
    statuses = {record["status"] for record in receipt["checks"]}
    receipt["status"] = "failed" if statuses & {"failed", "timeout", "blocked"} else "passed"
    # No command result is an authority claim until the manifest names and
    # validates its required output artifact.
    receipt["authoritative"] = False
    if cache_updates:
        # Another verifier may have completed while this process was running.
        # Acquire a host-wide cache lock, reload the latest file, merge only
        # this run's entries, and atomically replace it.
        with _cache_process_lock(cache_file):
            latest_cache = load_cache(cache_file)
            latest_cache["checks"].update(cache_updates)
            _write_json(cache_file, latest_cache)
    return receipt


def _discover_changed_paths(root: Path) -> list[str]:
    """Return tracked and untracked paths using stable git porcelain output."""

    try:
        result = subprocess.run(
            ["git", "status", "--porcelain=v1", "--untracked-files=all"],
            cwd=str(root),
            capture_output=True,
            text=True,
            check=False,
        )
    except OSError:
        return []
    paths: set[str] = set()
    for line in result.stdout.splitlines():
        if len(line) < 4:
            continue
        value = line[3:]
        if " -> " in value:
            value = value.split(" -> ", 1)[1]
        paths.add(_normalise_change(root, value))
    return sorted(paths)


def _print_human(receipt: Mapping[str, Any]) -> None:
    print(f"{receipt['kind']}: {receipt['status']}")
    print("checks:")
    for record in receipt.get("checks", []):
        suffix = ""
        if record.get("reason"):
            suffix = f" ({record['reason']})"
        print(f"  {record['id']}: {record['status']}{suffix}")
    if receipt.get("authoritative"):
        print("authority: scoped-proof-authority")
    elif receipt.get("checks"):
        print("authority: non-authoritative")


def build_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(description="Run manifest-defined Gravity development verification checks.")
    parser.add_argument("--manifest", type=Path, default=DEFAULT_MANIFEST)
    parser.add_argument("--root", type=Path, default=ROOT)
    parser.add_argument("--lane", action="append", choices=LANES, help="restrict selection to a lane; repeatable")
    parser.add_argument("--check", dest="requested_ids", action="append", help="run a named check and its dependencies; repeatable")
    parser.add_argument("--changed", action="append", help="changed path used for impact selection; repeatable")
    parser.add_argument("--all", dest="all_checks", action="store_true", help="select every check in the requested lanes")
    parser.add_argument("--jobs", type=int, help="maximum parallel cheap checks")
    parser.add_argument("--resume", action="store_true", help="reuse matching non-authoritative cache entries")
    parser.add_argument("--cache", type=Path, help="cache path (default: .cpcache/development-verification-cache.json)")
    parser.add_argument("--receipt", type=Path, help="write the JSON receipt to this path")
    parser.add_argument("--dry-run", action="store_true", help="show the planned DAG without executing checks")
    parser.add_argument("--explain", action="store_true", help="explain impact, ordering, locks, and cache eligibility")
    parser.add_argument("--no-fail-fast", dest="fail_fast", action="store_false", help="continue independent checks after a failure")
    parser.add_argument("--candidate-check-id", help="reviewed Stage 3 candidate check id")
    parser.add_argument("--candidate-receipt", "--stage3-candidate-receipt", dest="candidate_receipt_path", type=Path)
    parser.add_argument("--candidate-receipt-sha256", dest="candidate_receipt_sha256")
    parser.add_argument("--candidate-state-dir", dest="candidate_state_dir", type=Path)
    parser.add_argument("--candidate-manifest", dest="candidate_manifest_path", type=Path)
    parser.add_argument("--candidate-manifest-sha256", dest="candidate_manifest_sha256")
    parser.add_argument("--attestation", "--stage3-attestation", dest="attestation_path", type=Path)
    parser.add_argument("--attestation-sha256", dest="attestation_sha256")
    parser.add_argument("--human", action="store_true", help="print a short human summary instead of JSON")
    parser.add_argument("--json", dest="force_json", action="store_true", help="explicitly request JSON output (the default)")
    return parser


def main(argv: Sequence[str] | None = None) -> int:
    parser = build_parser()
    args = parser.parse_args(argv)
    try:
        root = args.root.resolve()
        changed = args.changed if args.changed is not None else _discover_changed_paths(root)
        receipt = run_verification(
            args.manifest,
            root,
            changed_paths=changed,
            lanes=args.lane,
            requested_ids=args.requested_ids,
            all_checks=args.all_checks,
            jobs=args.jobs,
            dry_run=args.dry_run,
            explain=args.explain,
            resume=args.resume,
            cache_path=args.cache,
            fail_fast=args.fail_fast,
            candidate_check_id=args.candidate_check_id,
            candidate_receipt_path=args.candidate_receipt_path,
            candidate_receipt_sha256=args.candidate_receipt_sha256,
            candidate_state_dir=args.candidate_state_dir,
            candidate_manifest_path=args.candidate_manifest_path,
            candidate_manifest_sha256=args.candidate_manifest_sha256,
            attestation_path=args.attestation_path,
            attestation_sha256=args.attestation_sha256,
        )
    except (ManifestError, VerificationError, OSError) as exc:
        error = {"kind": "development-verification-receipt", "schema_version": SCHEMA_VERSION, "status": "invalid", "error": str(exc)}
        if args.human:
            print(f"verification invalid: {exc}", file=sys.stderr)
        else:
            print(json.dumps(error, ensure_ascii=True, indent=2, sort_keys=True))
        return 2
    if args.receipt:
        _write_json(args.receipt.resolve(), receipt)
    if args.human:
        _print_human(receipt)
    else:
        print(json.dumps(receipt, ensure_ascii=True, indent=2, sort_keys=True))
    return 0 if receipt["status"] in {"passed", "planned"} else 1


if __name__ == "__main__":
    raise SystemExit(main())
