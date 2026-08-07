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
import shutil
import subprocess
import sys
import tempfile
import time
from typing import Any, Iterable, Mapping, Sequence


ROOT = Path(__file__).resolve().parents[1]
DEFAULT_MANIFEST = Path(__file__).with_name("development_verification_manifest.json")
DEFAULT_CACHE = ROOT / ".cpcache" / "development-verification-cache.json"
SCHEMA_VERSION = 1
LANES = ("preflight", "focused", "authoritative")
STATUSES = ("passed", "failed", "blocked", "reused", "planned", "timeout")
_GLOB_CHARS = frozenset("*?[")
_MAX_OUTPUT_BYTES = 64 * 1024


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


def _sha256_file(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as stream:
        for chunk in iter(lambda: stream.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


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
    if raw is None and "authoritative" in check:
        raw = "declared" if check["authoritative"] else "none"
    if raw is None:
        raw = "declared" if lane == "authoritative" else "none"
    if raw not in {"none", "declared"}:
        raise ManifestError(f"check {check.get('id')!r} authority must be 'none' or 'declared'")
    if raw == "declared" and lane != "authoritative":
        raise ManifestError(f"check {check.get('id')!r} declares authority outside authoritative lane")
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
    changed = sorted({_normalise_change(root_path, path) for path in changed_paths or []})
    reasons: dict[str, list[str]] = {check_id: [] for check_id in by_id}
    if requested:
        direct = set(requested)
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
    selected = set(direct) & allowed if not requested else set(direct)
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
    unmatched = sorted(path for path in changed if not any(_matches_change(item, path) for check in by_id.values() for item in list(check["inputs"]) + list(check.get("tool_inputs", []))))
    return {
        "selected_ids": selected_order,
        "changed_paths": changed,
        "lanes": sorted(lane_set, key=LANES.index),
        "reasons": {check_id: sorted(set(reasons[check_id])) for check_id in selected_order},
        "unmatched_changes": unmatched,
    }


def _input_files(root: Path, declaration: str) -> list[tuple[str, Path | None]]:
    declaration = _normalise_declared_path(declaration)
    path = root / declaration
    if _contains_glob(declaration):
        matches = sorted((item for item in root.glob(declaration) if item.is_file()), key=lambda item: item.as_posix())
        return [(_relpath(root, item), item) for item in matches] or [(declaration, None)]
    if path.is_file():
        return [(_relpath(root, path), path)]
    if path.is_dir():
        matches = sorted((item for item in path.rglob("*") if item.is_file()), key=lambda item: item.as_posix())
        return [(_relpath(root, item), item) for item in matches] or [(declaration, None)]
    return [(declaration, None)]


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
                records.append({"path": relative, "exists": True, "sha256": _sha256_file(path), "size": path.stat().st_size})
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
        executable_record.update({"resolved": str(resolved.resolve()), "sha256": _sha256_file(resolved), "size": resolved.stat().st_size})
    else:
        executable_record.update({"resolved": None, "sha256": None, "missing": True})
    return {
        "argv": command,
        "executable": executable_record,
        "runtime": {
            "python": platform.python_implementation(),
            "python_version": platform.python_version(),
            "platform": platform.platform(aliased=True),
        },
        "cwd": _normalise_declared_path(str(check.get("cwd", "."))),
        "env": dict(sorted((str(k), str(v)) for k, v in dict(check.get("env", {})).items())),
    }


def check_identity(check: Mapping[str, Any], root: Path | str = ROOT) -> dict[str, Any]:
    return {
        "id": check["id"],
        "lane": check["lane"],
        "depends_on": dependencies_of(check),
        "command": command_identity(check, root),
        "inputs": input_identities(check, root),
        "cost": check.get("cost", "cheap"),
        "lock": check.get("lock"),
        "exclusive": bool(check.get("exclusive", False)),
        "authority": _check_authority(check, str(check["lane"])),
    }


def cache_key(manifest: Mapping[str, Any], check: Mapping[str, Any], root: Path | str = ROOT) -> str:
    return _cache_key_for_identity(manifest, check_identity(check, root))


def _cache_key_for_identity(manifest: Mapping[str, Any], identity: Mapping[str, Any]) -> str:
    payload = {"schema_version": manifest["schema_version"], "name": manifest.get("name"), "check": identity}
    return _sha256_text(_canonical(payload))


def _read_json(path: Path) -> Any:
    try:
        return json.loads(path.read_text(encoding="utf-8"))
    except (OSError, UnicodeDecodeError, json.JSONDecodeError):
        return None


def load_cache(path: Path | str) -> dict[str, Any]:
    value = _read_json(Path(path))
    if not isinstance(value, Mapping) or value.get("schema_version") != SCHEMA_VERSION or not isinstance(value.get("checks"), Mapping):
        return {"schema_version": SCHEMA_VERSION, "checks": {}}
    return {"schema_version": SCHEMA_VERSION, "checks": dict(value["checks"])}


def _write_json(path: Path, value: Mapping[str, Any]) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    with tempfile.NamedTemporaryFile("w", encoding="utf-8", dir=str(path.parent), prefix=path.name + ".", delete=False) as stream:
        stream.write(json.dumps(value, ensure_ascii=True, indent=2, sort_keys=True) + "\n")
        temporary = Path(stream.name)
    os.replace(temporary, path)


@contextlib.contextmanager
def _cache_process_lock(cache_path: Path):
    """Serialize cache writers across verifier processes, then reload/merge."""

    try:
        import fcntl
    except ImportError as exc:  # pragma: no cover - the project currently targets POSIX hosts
        raise LockUnavailable("host does not provide POSIX file locking") from exc
    lock_path = cache_path.with_name(cache_path.name + ".lock")
    lock_path.parent.mkdir(parents=True, exist_ok=True)
    with lock_path.open("a+", encoding="ascii") as stream:
        # Cache writes are short and must not lose another process's entries;
        # unlike check resources this lock intentionally waits for its owner.
        fcntl.flock(stream.fileno(), fcntl.LOCK_EX)
        try:
            stream.seek(0)
            stream.truncate()
            stream.write(f"pid={os.getpid()} cache={cache_path}\n")
            stream.flush()
            yield lock_path
        finally:
            fcntl.flock(stream.fileno(), fcntl.LOCK_UN)


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
    if Path(lock_name).is_absolute():
        path = Path(lock_name)
    else:
        lock_id = _sha256_text(lock_name)[:16]
        path = Path(tempfile.gettempdir()) / f"gravity-development-{lock_id}.lock"
    path.parent.mkdir(parents=True, exist_ok=True)
    with path.open("a+", encoding="ascii") as stream:
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
            completed = subprocess.run(
                command,
                cwd=str(cwd),
                env=env,
                capture_output=True,
                text=True,
                check=False,
                timeout=check.get("timeout_seconds"),
            )
        record["returncode"] = completed.returncode
        record["stdout"] = _trim_output(completed.stdout)
        record["stderr"] = _trim_output(completed.stderr)
        record["status"] = "passed" if completed.returncode == 0 else "failed"
    except subprocess.TimeoutExpired as exc:
        record["returncode"] = None
        record["stdout"] = _trim_output((exc.stdout or "") if isinstance(exc.stdout, str) else "")
        record["stderr"] = _trim_output((exc.stderr or "") if isinstance(exc.stderr, str) else "")
        record["status"] = "timeout"
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
    if selection["unmatched_changes"]:
        # A changed path that no check declares is unsafe to ignore.  Returning
        # a failed receipt (rather than an empty passing plan) makes missing
        # manifest coverage visible to both humans and CI.
        receipt["status"] = "failed"
        receipt["error"] = "unmatched changed paths: " + ", ".join(selection["unmatched_changes"])
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

    cache_file = Path(cache_path).resolve() if cache_path is not None else DEFAULT_CACHE if root_path == ROOT else root_path / ".cpcache" / "development-verification-cache.json"
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
                if record["status"] == "passed":
                    cache_updates[check_id] = {
                        "cache_key": executed_key,
                        "status": "passed",
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
