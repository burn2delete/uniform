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
import os
from pathlib import Path, PurePosixPath
import platform
import select
import signal
import shutil
import stat
import subprocess
import sys
import tempfile
import threading
import time
from typing import Any, Iterable, Mapping, Sequence


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


def load_manifest(path: Path | str = DEFAULT_MANIFEST) -> dict[str, Any]:
    """Load and validate a JSON verification manifest."""

    manifest_path = Path(path)
    if not manifest_path.is_file():
        raise ManifestError(f"manifest does not exist: {manifest_path}")
    try:
        manifest = json.loads(manifest_path.read_text(encoding="utf-8"))
    except (OSError, UnicodeDecodeError, json.JSONDecodeError) as exc:
        raise ManifestError(f"cannot read manifest {manifest_path}: {exc}") from exc
    validate_manifest(manifest)
    return manifest


def validate_manifest(manifest: Mapping[str, Any]) -> None:
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
        if cost == "heavy" and lock is None and not exclusive:
            raise ManifestError(f"heavy check {check_id!r} must declare lock or exclusive=true")
        _check_authority(check, str(lane))
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
            if check_id not in allowed:
                continue
            declared = list(check["inputs"]) + list(check.get("tool_inputs", []))
            matches = [path for path in changed if any(_matches_change(item, path) for item in declared)]
            if matches:
                direct.add(check_id)
                reasons[check_id].append("changed-input:" + ",".join(matches))
    # First close downstream so changed source evidence reaches dependents.
    reverse: dict[str, list[str]] = {check_id: [] for check_id in by_id}
    for check_id, check in by_id.items():
        for dep in dependencies_of(check):
            reverse[dep].append(check_id)
    selected = set(direct) & allowed
    queue = sorted(selected)
    while queue:
        current = queue.pop(0)
        for child in sorted(reverse[current]):
            if child not in selected and child in allowed:
                selected.add(child)
                reasons[child].append("downstream-of:" + current)
                queue.append(child)
    # Then close dependencies.  Dependencies may come from another lane and
    # must be included even when a lane filter was requested.
    queue = sorted(selected)
    while queue:
        current = queue.pop(0)
        for dep in dependencies_of(by_id[current]):
            if dep not in selected:
                selected.add(dep)
                reasons[dep].append("dependency-of:" + current)
                queue.append(dep)
    for check_id in selected:
        if not reasons[check_id]:
            reasons[check_id].append("dependency-closure")
    selected_order = topological_order(manifest, selected)
    matches_by_path: dict[str, list[tuple[str, str]]] = {path: [] for path in changed}
    for check_id, check in by_id.items():
        declared = list(check["inputs"]) + list(check.get("tool_inputs", []))
        for path in changed:
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
        "changed_paths": changed,
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
            paths: dict[str, Path] = {}
            declarations = list(self.check.get("inputs", [])) + list(self.check.get("tool_inputs", []))
            for declaration in declarations:
                for index, directory in enumerate(_watch_directories_for_declaration(self.root, declaration)):
                    paths[f"<directory:{_normalise_declared_path(declaration)}:{index}>"] = directory
                matches = _input_files(self.root, declaration)
                for relative, path in matches:
                    if path is None:
                        # Missing/glob members are covered by the parent or
                        # subtree directory watches above; they are not an
                        # initialization failure.
                        continue
                    paths[relative] = path.resolve()
            executable = command_identity(self.check, self.root)["executable"].get("resolved")
            if executable is None:
                raise OSError("command executable cannot be watched coherently")
            paths["<command-executable>"] = Path(executable).resolve()
            queue = kqueue_factory()
            flags = getattr(select, "KQ_EV_ADD", 0) | getattr(select, "KQ_EV_ENABLE", 0) | getattr(select, "KQ_EV_CLEAR", 0)
            vnode_flags = 0
            for name in ("KQ_NOTE_WRITE", "KQ_NOTE_DELETE", "KQ_NOTE_EXTEND", "KQ_NOTE_ATTRIB", "KQ_NOTE_LINK", "KQ_NOTE_RENAME", "KQ_NOTE_REVOKE"):
                vnode_flags |= getattr(select, name, 0)
            if not vnode_flags:
                raise OSError("kqueue vnode mutation flags are unavailable")
            changes = []
            seen_paths: set[Path] = set()
            for label, path in sorted(paths.items()):
                path = path.resolve()
                if path in seen_paths:
                    continue
                seen_paths.add(path)
                open_flags = os.O_RDONLY | getattr(os, "O_CLOEXEC", 0) | getattr(os, "O_NOFOLLOW", 0)
                if path.is_dir():
                    open_flags |= getattr(os, "O_DIRECTORY", 0)
                descriptor = os.open(path, open_flags)
                self._watch_fds[descriptor] = label
                changes.append(select.kevent(descriptor, filter=vnode_filter, flags=flags, fflags=vnode_flags))
            queue.control(changes, 0, 0)
            self._kqueue = queue
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

    def _poll(self) -> None:
        if self._kqueue is not None:
            meaningful = self._meaningful_vnode_flags()
            while not self._stop.is_set():
                try:
                    events = self._kqueue.control(None, max(1, len(self._watch_fds)), self.interval)
                except OSError as exc:
                    self._record_unavailable(f"kqueue control failed: {exc}")
                    break
                for event in events:
                    # Executing or reading a file may update atime and produce
                    # NOTE_ATTRIB.  Ignore that benign event when the stable
                    # metadata snapshot is unchanged; writes/renames remain
                    # evidence even when bytes were restored before draining.
                    if not (int(event.fflags) & meaningful):
                        current = _monitor_snapshot(self.check, self.root)
                        if current == self._baseline:
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
            meaningful = self._meaningful_vnode_flags()
            for event in events:
                if not (int(event.fflags) & meaningful):
                    current = _monitor_snapshot(self.check, self.root)
                    if current == self._baseline:
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


def _run_command(
    command: list[str],
    *,
    cwd: Path,
    env: Mapping[str, str],
    timeout: float | int | None,
    marker: str,
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
        finally:
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
        }
    timed_out = False
    # Wait for the leader only.  ``communicate`` waits for inherited pipe
    # descriptors held by a detached child, which would delay supervision
    # until after the child had already mutated files.
    try:
        process.wait(timeout=timeout)
    except subprocess.TimeoutExpired:
        timed_out = True
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
        "surviving_descendants": bool(not timed_out and cleanup is not None and (cleanup.get("survivors_detected") or cleanup.get("census_error"))),
        "supervision_failed": bool(census.get("error") or not cleanup_safe),
        "supervisor": census,
    }


def _run_one(check: Mapping[str, Any], root: Path, identities: dict[str, Any]) -> dict[str, Any]:
    started = _now()
    started_clock = time.monotonic()
    command = identities["command"]["argv"]
    cwd_value = check.get("cwd", ".")
    cwd = root / _normalise_declared_path(str(cwd_value))
    env = os.environ.copy()
    env.update({str(k): str(v) for k, v in dict(check.get("env", {})).items()})
    record: dict[str, Any] = {
        "id": check["id"],
        "lane": check["lane"],
        "command": command,
        "command_identity": identities["command"],
        "inputs": identities["inputs"],
        "lock": check.get("lock"),
        "exclusive": bool(check.get("exclusive", False)),
        "cost": check.get("cost", "cheap"),
        "fresh": bool(check.get("fresh", False)),
        "authority": "non-authoritative",
        "depends_on": dependencies_of(check),
        "started_at": started,
    }
    try:
        with _process_lock(_effective_lock(check)) as lock_path:
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
                )
            finally:
                monitor.stop()
        record["returncode"] = None if outcome["timed_out"] else outcome["returncode"]
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
    if record["status"] == "passed":
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
    if selection["unmatched_changes"] or selection.get("matched_outside_lane") or selection.get("requested_outside_lane"):
        # A changed path that no check declares is unsafe to ignore.  Returning
        # a failed receipt (rather than an empty passing plan) makes missing
        # manifest coverage visible to both humans and CI.
        receipt["status"] = "failed"
        errors: list[str] = []
        if selection["unmatched_changes"]:
            errors.append("unmatched changed paths: " + ", ".join(selection["unmatched_changes"]))
        if selection.get("matched_outside_lane"):
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
        print("authority: fresh-declared-authority")
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
