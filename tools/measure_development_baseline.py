#!/usr/bin/env python3
"""Measure a bounded set of development commands for local feedback.

This module intentionally produces development feedback, not benchmark or
release evidence.  A receipt has two deliberately separate parts:

``identity``
    The semantic inputs that identify a run (argv, working directory,
    runtime/platform, and redacted environment bindings).  The identity is
    content-addressed with a SHA-256 digest and contains no wall-clock data.

``measurements``
    Volatile observations for each command/sample (elapsed wall time, status,
    output digests, and best-effort peak RSS).

Commands are passed as argv vectors.  They are never interpreted by a shell.
Each child starts a new process group/session.  On timeout, and after a normal
parent exit when descendants remain, the group is terminated before the result
is returned.  Strict containment of a child that deliberately creates a
cross-session daemon is outside this small development helper; the command
policy is nevertheless recorded as ``daemonization: forbidden``.

The default plan is a cheap Python-only interpreter probe.  It is intentionally
not a Gravity gate.  Callers may provide repeatable ``--command`` options for
other bounded, local checks.
"""

from __future__ import annotations

import argparse
import dataclasses
import hashlib
import json
import math
import os
from pathlib import Path
import platform
import shutil
import signal
import subprocess
import sys
import tempfile
import threading
import time
from typing import Any, Iterable, Mapping, Sequence

try:  # ``resource`` is unavailable on standard Windows Python builds.
    import resource
except ImportError:  # pragma: no cover - exercised only on Windows
    resource = None  # type: ignore[assignment]


SCHEMA = "gravity/development-performance-baseline-v1"
SCHEMA_VERSION = 1
TOOL_NAME = "measure_development_baseline"
TOOL_VERSION = 1
AUTHORITY = "non-authoritative"
MAX_COMMANDS = 32
MAX_ARGUMENTS_PER_COMMAND = 256
MAX_SAMPLES = 100
MAX_OUTPUT_BYTES = 64 * 1024 * 1024
DEFAULT_MAX_OUTPUT_BYTES = 1024 * 1024
DEFAULT_TIMEOUT_SECONDS = 60.0
DEFAULT_TERMINATE_GRACE_SECONDS = 1.0

_EMPTY_SHA256 = hashlib.sha256(b"").hexdigest()


class BaselineError(ValueError):
    """Raised for invalid baseline plans or execution settings."""


@dataclasses.dataclass(frozen=True)
class CommandSpec:
    """A validated command and its stable ordinal in the plan."""

    command_id: str
    argv: tuple[str, ...]


class _PeakRssSampler:
    """Best-effort process RSS sampler used while a child is running."""

    def __init__(self, pid: int, interval_seconds: float = 0.01) -> None:
        self.pid = pid
        self.interval_seconds = interval_seconds
        self._stop = threading.Event()
        self._thread: threading.Thread | None = None
        self.peak_bytes: int | None = None
        self.source: str | None = None

    def start(self) -> None:
        self._thread = threading.Thread(target=self._run, name="baseline-rss", daemon=True)
        self._thread.start()

    def stop(self) -> tuple[int | None, str | None]:
        self._stop.set()
        if self._thread is not None:
            self._thread.join(timeout=0.25)
        self._sample_once()
        if self.peak_bytes is None:
            fallback = _resource_children_peak_rss()
            if fallback is not None:
                self.peak_bytes = fallback
                self.source = "resource.RUSAGE_CHILDREN"
        return self.peak_bytes, self.source

    def _run(self) -> None:
        while not self._stop.is_set():
            self._sample_once()
            self._stop.wait(self.interval_seconds)

    def _sample_once(self) -> None:
        value, source = _read_process_rss(self.pid)
        if value is not None and (self.peak_bytes is None or value > self.peak_bytes):
            self.peak_bytes = value
            self.source = source


def _sha256_bytes(value: bytes) -> str:
    return hashlib.sha256(value).hexdigest()


def _sha256_text(value: str) -> str:
    return _sha256_bytes(value.encode("utf-8", "surrogateescape"))


def _canonical_json(value: Any) -> bytes:
    return json.dumps(
        value,
        allow_nan=False,
        ensure_ascii=True,
        sort_keys=True,
        separators=(",", ":"),
    ).encode("utf-8")


def _canonical_digest(value: Any) -> str:
    return _sha256_bytes(_canonical_json(value))


def _normalise_argv(argv: Sequence[str], command_number: int) -> CommandSpec:
    if isinstance(argv, (str, bytes)):
        raise BaselineError(
            f"command {command_number} must be an argv sequence, not a shell/string command"
        )
    values = tuple(str(item) for item in argv)
    if not values:
        raise BaselineError(f"command {command_number} cannot be empty")
    if len(values) > MAX_ARGUMENTS_PER_COMMAND:
        raise BaselineError(
            f"command {command_number} has {len(values)} arguments; "
            f"the limit is {MAX_ARGUMENTS_PER_COMMAND}"
        )
    if any("\x00" in item for item in values):
        raise BaselineError(f"command {command_number} contains a NUL byte")
    return CommandSpec(f"command-{command_number:03d}", values)


def _normalise_commands(commands: Iterable[Sequence[str]] | None, cwd: Path) -> tuple[CommandSpec, ...]:
    if commands is None:
        commands = default_commands(cwd)
    try:
        raw_commands = list(commands)
    except TypeError as exc:
        raise BaselineError("commands must be an iterable of argv sequences") from exc
    if raw_commands and isinstance(raw_commands[0], str):
        # A single argv vector is convenient for the Python API.  The CLI
        # always supplies a list of vectors, so this does not make command
        # boundaries ambiguous there.
        raw_commands = [raw_commands]  # type: ignore[list-item]
    if len(raw_commands) > MAX_COMMANDS:
        raise BaselineError(f"at most {MAX_COMMANDS} commands may be measured per run")
    return tuple(_normalise_argv(item, number) for number, item in enumerate(raw_commands, 1))


def _normalise_cwd(cwd: str | os.PathLike[str] | None) -> Path:
    value = Path.cwd() if cwd is None else Path(cwd)
    try:
        resolved = value.expanduser().resolve(strict=True)
    except OSError as exc:
        raise BaselineError(f"working directory cannot be resolved: {value}") from exc
    if not resolved.is_dir():
        raise BaselineError(f"working directory is not a directory: {resolved}")
    return resolved


def _normalise_env(env: Mapping[str, str] | None) -> dict[str, str]:
    source = os.environ if env is None else env
    result: dict[str, str] = {}
    for name, value in source.items():
        if not isinstance(name, str) or not isinstance(value, str):
            raise BaselineError("environment names and values must be strings")
        if "\x00" in name or "\x00" in value:
            raise BaselineError("environment names and values cannot contain NUL bytes")
        result[name] = value
    return result


def _runtime_identity() -> dict[str, Any]:
    return {
        "python_implementation": platform.python_implementation(),
        "python_version": platform.python_version(),
        "python_executable": str(Path(sys.executable).resolve()),
        "python_compiler": platform.python_compiler(),
        "byteorder": sys.byteorder,
        "hash_seed_policy": "ambient-or-interpreter-default",
    }


def _platform_identity() -> dict[str, Any]:
    return {
        "system": platform.system(),
        "release": platform.release(),
        "version": platform.version(),
        "machine": platform.machine(),
        "processor": platform.processor(),
        "architecture": list(platform.architecture()),
    }


def _environment_identity(env: Mapping[str, str]) -> dict[str, Any]:
    bindings = [
        {"name": name, "sha256": _sha256_text(value)} for name, value in sorted(env.items())
    ]
    return {
        "binding_count": len(bindings),
        "bindings": bindings,
        "bindings_sha256": _canonical_digest(bindings),
        "values_redacted": True,
    }


def _hash_file(path: Path) -> str | None:
    try:
        if not path.is_file():
            return None
        digest = hashlib.sha256()
        with path.open("rb") as stream:
            for block in iter(lambda: stream.read(1024 * 1024), b""):
                digest.update(block)
        return digest.hexdigest()
    except (OSError, ValueError):
        return None


def _resolve_executable(argv0: str, cwd: Path, env: Mapping[str, str]) -> Path | None:
    if os.path.dirname(argv0):
        candidate = Path(argv0)
        if not candidate.is_absolute():
            candidate = cwd / candidate
        try:
            return candidate.resolve(strict=False)
        except OSError:
            return candidate.absolute()
    found = shutil.which(argv0, path=env.get("PATH"))
    if found is None:
        return None
    try:
        return Path(found).resolve(strict=False)
    except OSError:
        return Path(found)


def _command_identity(spec: CommandSpec, cwd: Path, env: Mapping[str, str]) -> dict[str, Any]:
    executable = _resolve_executable(spec.argv[0], cwd, env)
    executable_record: dict[str, Any] = {
        "requested": spec.argv[0],
        "resolved": None if executable is None else str(executable),
        "exists": False if executable is None else executable.exists(),
        "sha256": None if executable is None else _hash_file(executable),
    }
    return {
        "id": spec.command_id,
        "argv": list(spec.argv),
        "cwd": str(cwd),
        "executable": executable_record,
        "daemonization": "forbidden",
        "shell": False,
    }


def _build_identity(
    specs: Sequence[CommandSpec],
    cwd: Path,
    env: Mapping[str, str],
    samples: int,
    timeout_seconds: float,
    terminate_grace_seconds: float,
    output_limit_bytes: int,
    dry_run: bool,
) -> dict[str, Any]:
    identity: dict[str, Any] = {
        "tool": {"name": TOOL_NAME, "version": TOOL_VERSION},
        "cwd": str(cwd),
        "commands": [_command_identity(spec, cwd, env) for spec in specs],
        "samples": samples,
        "timeout_seconds": timeout_seconds,
        "terminate_grace_seconds": terminate_grace_seconds,
        "output_limit_bytes": output_limit_bytes,
        "dry_run": dry_run,
        "platform": _platform_identity(),
        "runtime": _runtime_identity(),
        "environment": _environment_identity(env),
    }
    return identity


def _read_process_rss(pid: int) -> tuple[int | None, str | None]:
    """Read one process's resident set size, preferring Linux procfs."""

    proc_status = Path(f"/proc/{pid}/status")
    try:
        for line in proc_status.read_text(encoding="ascii", errors="replace").splitlines():
            if line.startswith("VmRSS:"):
                fields = line.split()
                if len(fields) >= 2:
                    return int(fields[1]) * 1024, "procfs.VmRSS"
    except (OSError, ValueError):
        pass
    try:
        completed = subprocess.run(
            ["ps", "-o", "rss=", "-p", str(pid)],
            check=False,
            capture_output=True,
            text=True,
            timeout=0.2,
            shell=False,
        )
        value = completed.stdout.strip().split()
        if value:
            return int(value[0]) * 1024, "ps.rss"
    except (OSError, ValueError, subprocess.SubprocessError):
        pass
    return None, None


def _resource_children_peak_rss() -> int | None:
    if resource is None:
        return None
    try:
        value = resource.getrusage(resource.RUSAGE_CHILDREN).ru_maxrss
    except (AttributeError, OSError, ValueError):
        return None
    if not value or value < 0:
        return None
    # macOS reports bytes; Linux and the common BSDs report KiB.
    if sys.platform == "darwin":
        return int(value)
    return int(value) * 1024


def _group_exists(pgid: int) -> bool:
    if os.name != "posix":
        return False
    try:
        os.killpg(pgid, 0)
    except ProcessLookupError:
        return False
    except PermissionError:
        return True
    except OSError:
        return False
    return True


def _terminate_process_group(pid: int, grace_seconds: float) -> dict[str, Any]:
    """Terminate a child session and report the cleanup attempt."""

    outcome: dict[str, Any] = {
        "attempted": False,
        "term_sent": False,
        "kill_sent": False,
        "platform": os.name,
    }
    if os.name == "posix":
        # ``start_new_session=True`` makes the child PID the process-group ID.
        # Keep using that saved identity even after the group leader exits;
        # ordinary descendants can outlive the leader and ``getpgid(pid)``
        # would then fail before they are cleaned up.
        pgid = pid
        outcome["attempted"] = True
        try:
            os.killpg(pgid, signal.SIGTERM)
            outcome["term_sent"] = True
        except (ProcessLookupError, PermissionError, OSError):
            pass
        deadline = time.monotonic() + max(0.0, grace_seconds)
        while _group_exists(pgid) and time.monotonic() < deadline:
            time.sleep(min(0.02, max(0.001, deadline - time.monotonic())))
        if _group_exists(pgid):
            try:
                os.killpg(pgid, signal.SIGKILL)
                outcome["kill_sent"] = True
            except (ProcessLookupError, PermissionError, OSError):
                pass
        return outcome

    # Windows has no portable process-group kill equivalent in the stdlib.
    # ``kill`` still handles the direct child; strict descendant containment is
    # intentionally not claimed by this development-only helper.
    outcome["attempted"] = True
    return outcome


class _OutputAccumulator:
    """Stream one output pipe into a bounded digest/count record.

    The full output is hashed and counted, but only ``limit_bytes`` are kept
    as an optional bounded preview.  The preview is intentionally not emitted
    in the receipt; retaining it makes the policy useful to callers that
    inspect the object in-process without allowing a noisy command to grow
    memory without bound.
    """

    def __init__(self, limit_bytes: int) -> None:
        self.limit_bytes = limit_bytes
        self._digest = hashlib.sha256()
        self.total_bytes = 0
        self._preview = bytearray()
        self.truncated = False

    def feed(self, chunk: bytes) -> None:
        self._digest.update(chunk)
        self.total_bytes += len(chunk)
        if len(self._preview) < self.limit_bytes:
            remaining = self.limit_bytes - len(self._preview)
            self._preview.extend(chunk[:remaining])
        if self.total_bytes > self.limit_bytes:
            self.truncated = True

    def record(self, *, complete: bool, error: str | None = None) -> dict[str, Any]:
        result: dict[str, Any] = {
            "bytes": self.total_bytes,
            "sha256": self._digest.hexdigest(),
            "captured_bytes": len(self._preview),
            "limit_bytes": self.limit_bytes,
            "truncated": self.truncated,
            "capture_complete": complete,
        }
        if error is not None:
            result["capture_error"] = error
        return result


def _capture_stream(
    stream: Any,
    accumulator: _OutputAccumulator,
    errors: list[str],
) -> None:
    try:
        while True:
            chunk = stream.read(64 * 1024)
            if not chunk:
                break
            accumulator.feed(chunk)
    except Exception as exc:  # pragma: no cover - only closed/detached pipe failures
        errors.append(f"{type(exc).__name__}: {exc}")
    finally:
        try:
            stream.close()
        except OSError:
            pass


def _communicate_with_monitor(
    process: subprocess.Popen[bytes],
    timeout_seconds: float,
    terminate_grace_seconds: float,
    output_limit_bytes: int,
) -> tuple[dict[str, Any], dict[str, Any], bool, dict[str, Any], list[str]]:
    """Read child pipes while noticing parent exit and enforcing a timeout.

    ``Popen.communicate(timeout=...)`` retains all output in memory.  Dedicated
    readers feed bounded accumulators instead.  The coordinator observes the
    parent PID independently, cleans its process group immediately after a
    normal parent exit, and then finishes draining the pipes.
    """

    stdout_accumulator = _OutputAccumulator(output_limit_bytes)
    stderr_accumulator = _OutputAccumulator(output_limit_bytes)
    capture_errors: list[str] = []
    readers = [
        threading.Thread(
            target=_capture_stream,
            args=(process.stdout, stdout_accumulator, capture_errors),
            name="baseline-stdout",
            daemon=True,
        ),
        threading.Thread(
            target=_capture_stream,
            args=(process.stderr, stderr_accumulator, capture_errors),
            name="baseline-stderr",
            daemon=True,
        ),
    ]
    for reader in readers:
        reader.start()
    cleanup: dict[str, Any] = {
        "attempted": False,
        "term_sent": False,
        "kill_sent": False,
        "platform": os.name,
    }
    parent_cleanup_done = False
    timed_out = False
    deadline = time.monotonic() + timeout_seconds
    while process.poll() is None:
        if time.monotonic() >= deadline:
            timed_out = True
            cleanup = _merge_cleanup(
                cleanup, _terminate_process_group(process.pid, terminate_grace_seconds)
            )
            break
        time.sleep(0.01)

    if process.poll() is not None and not parent_cleanup_done:
        cleanup = _merge_cleanup(
            cleanup, _terminate_process_group(process.pid, terminate_grace_seconds)
        )
        parent_cleanup_done = True

    if timed_out:
        # The process group should already be gone.  A direct kill is a final
        # bounded fallback for a platform with incomplete group semantics.
        try:
            process.kill()
        except OSError:
            pass
        try:
            process.wait(timeout=max(0.05, terminate_grace_seconds))
        except subprocess.TimeoutExpired:
            pass

    for reader in readers:
        reader.join(timeout=max(0.05, terminate_grace_seconds))
    if any(reader.is_alive() for reader in readers):
        # Detached processes may retain a pipe despite group cleanup.  Close
        # our descriptors and do not let this development helper daemonize a
        # reader thread indefinitely.
        if process.stdout is not None:
            process.stdout.close()
        if process.stderr is not None:
            process.stderr.close()
        for reader in readers:
            reader.join(timeout=0.25)

    capture_complete = not any(reader.is_alive() for reader in readers) and not capture_errors
    stdout_record = stdout_accumulator.record(
        complete=capture_complete,
        error=capture_errors[0] if capture_errors else None,
    )
    stderr_record = stderr_accumulator.record(
        complete=capture_complete,
        error=capture_errors[0] if capture_errors else None,
    )
    # Always give a normal parent-exit path one final cleanup attempt.  This is
    # a no-op for an empty process group, and catches descendants that appeared
    # in the small race between the poll and reader completion.
    if process.poll() is not None:
        cleanup = _merge_cleanup(
            cleanup, _terminate_process_group(process.pid, terminate_grace_seconds)
        )
    return stdout_record, stderr_record, timed_out, cleanup, capture_errors


def _run_one(
    spec: CommandSpec,
    sample_index: int,
    cwd: Path,
    env: Mapping[str, str],
    timeout_seconds: float,
    terminate_grace_seconds: float,
    output_limit_bytes: int,
) -> dict[str, Any]:
    started = time.monotonic()
    process: subprocess.Popen[bytes] | None = None
    sampler: _PeakRssSampler | None = None
    timed_out = False
    error: dict[str, str] | None = None
    stdout_record: dict[str, Any] = {
        "bytes": 0,
        "sha256": _EMPTY_SHA256,
        "captured_bytes": 0,
        "limit_bytes": output_limit_bytes,
        "truncated": False,
        "capture_complete": False,
    }
    stderr_record: dict[str, Any] = {
        "bytes": 0,
        "sha256": _EMPTY_SHA256,
        "captured_bytes": 0,
        "limit_bytes": output_limit_bytes,
        "truncated": False,
        "capture_complete": False,
    }
    capture_errors: list[str] = []
    cleanup: dict[str, Any] = {
        "attempted": False,
        "term_sent": False,
        "kill_sent": False,
        "platform": os.name,
    }
    try:
        process = subprocess.Popen(
            list(spec.argv),
            cwd=str(cwd),
            env=dict(env),
            stdin=subprocess.DEVNULL,
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            shell=False,
            start_new_session=True,
        )
        sampler = _PeakRssSampler(process.pid)
        sampler.start()
        stdout_record, stderr_record, timed_out, cleanup, capture_errors = _communicate_with_monitor(
            process, timeout_seconds, terminate_grace_seconds, output_limit_bytes
        )
    except (OSError, ValueError) as exc:
        error = {"type": type(exc).__name__, "message": str(exc)}
    finally:
        if sampler is not None:
            peak_rss_bytes, peak_rss_source = sampler.stop()
        else:
            peak_rss_bytes, peak_rss_source = None, None
    if process is None:
        exit_status: int | None = None
    else:
        try:
            exit_status = process.returncode
        except (AttributeError, OSError):
            exit_status = None
    elapsed = max(0.0, time.monotonic() - started)
    result: dict[str, Any] = {
        "command_id": spec.command_id,
        "sample_index": sample_index,
        "status": "timed-out" if timed_out else ("spawn-error" if error else "completed"),
        "wall_time_seconds": round(elapsed, 6),
        "elapsed_ms": round(elapsed * 1000, 3),
        "exit_status": exit_status,
        "timed_out": timed_out,
        "stdout": stdout_record,
        "stderr": stderr_record,
        "stdout_sha256": stdout_record["sha256"],
        "stderr_sha256": stderr_record["sha256"],
        "output_limit_bytes": output_limit_bytes,
        "output_truncated": bool(stdout_record["truncated"] or stderr_record["truncated"]),
        "capture_complete": bool(
            stdout_record["capture_complete"] and stderr_record["capture_complete"]
        ),
        "peak_rss_bytes": peak_rss_bytes,
        "peak_rss_source": peak_rss_source,
        "cleanup": cleanup,
    }
    if error is not None:
        result["error"] = error
    if capture_errors:
        result["capture_errors"] = capture_errors
    return result


def _merge_cleanup(first: Mapping[str, Any], second: Mapping[str, Any]) -> dict[str, Any]:
    return {
        "attempted": bool(first.get("attempted") or second.get("attempted")),
        "term_sent": bool(first.get("term_sent") or second.get("term_sent")),
        "kill_sent": bool(first.get("kill_sent") or second.get("kill_sent")),
        "platform": second.get("platform", first.get("platform", os.name)),
    }


def default_commands(cwd: str | os.PathLike[str] | None = None) -> list[list[str]]:
    """Return the cheap default plan; it never invokes a Gravity gate."""

    del cwd  # Kept in the signature so callers can describe the plan uniformly.
    return [
        [
            str(Path(sys.executable).resolve()),
            "-c",
            "import platform; print(platform.python_version())",
        ]
    ]


def measure_baseline(
    commands: Iterable[Sequence[str]] | None = None,
    *,
    cwd: str | os.PathLike[str] | None = None,
    env: Mapping[str, str] | None = None,
    samples: int = 1,
    timeout_seconds: float = DEFAULT_TIMEOUT_SECONDS,
    terminate_grace_seconds: float = DEFAULT_TERMINATE_GRACE_SECONDS,
    max_output_bytes: int = DEFAULT_MAX_OUTPUT_BYTES,
    dry_run: bool = False,
) -> dict[str, Any]:
    """Measure commands and return a deterministic, JSON-serialisable receipt."""

    root = _normalise_cwd(cwd)
    child_env = _normalise_env(env)
    if not isinstance(samples, int) or isinstance(samples, bool) or not 1 <= samples <= MAX_SAMPLES:
        raise BaselineError(f"samples must be an integer from 1 through {MAX_SAMPLES}")
    if not isinstance(timeout_seconds, (int, float)) or isinstance(timeout_seconds, bool):
        raise BaselineError("timeout_seconds must be a finite number")
    if not math.isfinite(float(timeout_seconds)):
        raise BaselineError("timeout_seconds must be finite")
    if timeout_seconds <= 0:
        raise BaselineError("timeout_seconds must be positive")
    if not isinstance(terminate_grace_seconds, (int, float)) or isinstance(
        terminate_grace_seconds, bool
    ):
        raise BaselineError("terminate_grace_seconds must be a finite number")
    if not math.isfinite(float(terminate_grace_seconds)):
        raise BaselineError("terminate_grace_seconds must be finite")
    if terminate_grace_seconds < 0:
        raise BaselineError("terminate_grace_seconds cannot be negative")
    if (
        not isinstance(max_output_bytes, int)
        or isinstance(max_output_bytes, bool)
        or not 0 <= max_output_bytes <= MAX_OUTPUT_BYTES
    ):
        raise BaselineError(
            f"max_output_bytes must be an integer from 0 through {MAX_OUTPUT_BYTES}"
        )
    specs = _normalise_commands(commands, root)
    identity = _build_identity(
        specs,
        root,
        child_env,
        samples,
        float(timeout_seconds),
        float(terminate_grace_seconds),
        max_output_bytes,
        dry_run,
    )
    identity_digest = _canonical_digest(identity)
    measurements: list[dict[str, Any]] = []
    if not dry_run:
        for spec in specs:
            for sample_index in range(1, samples + 1):
                measurements.append(
                    _run_one(
                        spec,
                        sample_index,
                        root,
                        child_env,
                        float(timeout_seconds),
                        float(terminate_grace_seconds),
                        max_output_bytes,
                    )
                )
    failed = [
        item
        for item in measurements
        if item.get("exit_status") != 0 or item.get("timed_out") or item.get("error")
    ]
    truncated_measurements = [item for item in measurements if item.get("output_truncated")]
    incomplete_measurements = [item for item in measurements if not item.get("capture_complete", True)]
    payload: dict[str, Any] = {
        "schema": SCHEMA,
        "schema_version": SCHEMA_VERSION,
        "authority": AUTHORITY,
        "authoritative": False,
        "evidence": {
            "kind": "development-performance-feedback",
            "authority": AUTHORITY,
            "d6_benchmark_evidence": False,
            "release_evidence": False,
            "forbidden_interpretations": [
                "D6 benchmark evidence",
                "release evidence",
                "performance claim or regression gate",
                "proof, conformance, or seed-retirement evidence",
            ],
        },
        "plan": {
            "default_plan": commands is None,
            "default_plan_heavy_gates": "forbidden",
            "command_count": len(specs),
            "sample_count": samples,
            "dry_run": dry_run,
            "max_output_bytes": max_output_bytes,
            "output_policy": "streaming-digest-count-bounded-preview",
            "commands": [
                {
                    "id": spec.command_id,
                    "argv": list(spec.argv),
                    "cwd": str(root),
                    "daemonization": "forbidden",
                    "shell": False,
                }
                for spec in specs
            ],
        },
        "identity": identity,
        "identity_sha256": identity_digest,
        "measurements": measurements,
        "summary": {
            "status": "planned" if dry_run else ("failed" if failed else "passed"),
            "measurement_count": len(measurements),
            "failed_count": len(failed),
            "passed_count": len(measurements) - len(failed),
            "output_limit_bytes": max_output_bytes,
            "output_limit_reached": bool(truncated_measurements),
            "truncated_measurement_count": len(truncated_measurements),
            "capture_incomplete_measurement_count": len(incomplete_measurements),
            "non_authoritative": True,
        },
    }
    return payload


def _write_receipt(path: Path, payload: Mapping[str, Any]) -> None:
    path = path.expanduser().resolve()
    path.parent.mkdir(parents=True, exist_ok=True)
    encoded = json.dumps(payload, allow_nan=False, ensure_ascii=True, indent=2, sort_keys=True) + "\n"
    temporary_name: str | None = None
    try:
        with tempfile.NamedTemporaryFile(
            mode="w", encoding="utf-8", dir=path.parent, prefix=f".{path.name}.", suffix=".tmp", delete=False
        ) as stream:
            temporary_name = stream.name
            stream.write(encoded)
            stream.flush()
            os.fsync(stream.fileno())
        os.replace(temporary_name, path)
        temporary_name = None
    finally:
        if temporary_name is not None:
            try:
                os.unlink(temporary_name)
            except OSError:
                pass


_CONTROL_OPTIONS = {
    "--command",
    "--cwd",
    "--output",
    "-o",
    "--samples",
    "--repetitions",
    "--timeout",
    "--timeout-seconds",
    "--terminate-grace",
    "--terminate-grace-seconds",
    "--max-output-bytes",
    "--dry-run",
    "--help",
    "-h",
}


def build_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(
        description=(
            "Measure a bounded argv command plan for development feedback. "
            "Results are non-authoritative and are not D6/release evidence."
        )
    )
    parser.add_argument(
        "--command",
        action="append",
        nargs="+",
        metavar="ARG",
        help="repeatable command argv; tokens continue until the next baseline option",
    )
    parser.add_argument("--cwd", type=Path, default=None, help="command working directory (default: current directory)")
    parser.add_argument("-o", "--output", type=Path, help="JSON receipt path (default: stdout)")
    parser.add_argument("--timeout", "--timeout-seconds", type=float, default=DEFAULT_TIMEOUT_SECONDS)
    parser.add_argument(
        "--terminate-grace",
        "--terminate-grace-seconds",
        type=float,
        default=DEFAULT_TERMINATE_GRACE_SECONDS,
    )
    parser.add_argument(
        "--max-output-bytes",
        type=int,
        default=DEFAULT_MAX_OUTPUT_BYTES,
        help=f"maximum retained output bytes per stream (default: {DEFAULT_MAX_OUTPUT_BYTES})",
    )
    parser.add_argument("--samples", "--repetitions", type=int, default=1)
    parser.add_argument("--dry-run", action="store_true", help="emit the plan without starting commands")
    return parser


def _split_command_arguments(argv: Sequence[str]) -> tuple[list[list[str]], list[str]]:
    """Extract repeatable command groups while allowing command flags like ``-c``."""

    commands: list[list[str]] = []
    ordinary: list[str] = []
    index = 0
    values = list(argv)
    while index < len(values):
        token = values[index]
        if token == "--command":
            index += 1
            start = index
            while index < len(values) and values[index] not in _CONTROL_OPTIONS:
                index += 1
            group = values[start:index]
            if group and group[0] == "--":
                group = group[1:]
            if not group:
                raise BaselineError("--command requires at least an executable")
            commands.append(group)
        elif token.startswith("--command="):
            value = token.split("=", 1)[1]
            if not value:
                raise BaselineError("--command= requires an executable")
            commands.append([value])
            index += 1
        else:
            ordinary.append(token)
            index += 1
    return commands, ordinary


def parse_arguments(arguments: Sequence[str] | None = None) -> tuple[argparse.Namespace, list[list[str]]]:
    raw = list(sys.argv[1:] if arguments is None else arguments)
    command_groups, ordinary = _split_command_arguments(raw)
    values = build_parser().parse_args(ordinary)
    return values, command_groups


def main(arguments: Sequence[str] | None = None) -> int:
    try:
        values, command_groups = parse_arguments(arguments)
        commands: list[list[str]] | None = command_groups or None
        payload = measure_baseline(
            commands,
            cwd=values.cwd,
            samples=values.samples,
            timeout_seconds=values.timeout,
            terminate_grace_seconds=values.terminate_grace,
            max_output_bytes=values.max_output_bytes,
            dry_run=values.dry_run,
        )
    except (BaselineError, OSError, ValueError) as exc:
        print(f"measure_development_baseline: {exc}", file=sys.stderr)
        return 2
    encoded = json.dumps(payload, allow_nan=False, ensure_ascii=True, indent=2, sort_keys=True) + "\n"
    if values.output is None:
        try:
            sys.stdout.write(encoded)
        except BrokenPipeError:
            return 1
    elif str(values.output) == "-":
        sys.stdout.write(encoded)
    else:
        _write_receipt(values.output, payload)
    return 0 if values.dry_run or payload["summary"]["failed_count"] == 0 else 1


if __name__ == "__main__":
    raise SystemExit(main())
