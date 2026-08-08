#!/usr/bin/env python3
"""Run a long verification command with durable output and status heartbeats."""

from __future__ import annotations

import argparse
import datetime as dt
import fcntl
import json
import os
from pathlib import Path
import signal
import subprocess
import sys
import threading
import time
import uuid

try:
    from tools import shared_heavy_lock as locks
    from tools.process_tree_telemetry import process_tree_metrics
except ImportError:
    import shared_heavy_lock as locks
    from process_tree_telemetry import process_tree_metrics


SCHEMA = "gravity/long-running-command-status-v1"


def utc_now() -> str:
    return dt.datetime.now(dt.timezone.utc).isoformat().replace("+00:00", "Z")


def atomic_json_write(path: Path, value: dict[str, object]) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    temporary = path.with_name(f".{path.name}.{os.getpid()}.tmp")
    with temporary.open("w", encoding="utf-8") as stream:
        json.dump(value, stream, indent=2, sort_keys=True)
        stream.write("\n")
        stream.flush()
        os.fsync(stream.fileno())
    os.replace(temporary, path)


class DurableLog:
    def __init__(self, path: Path, append: bool) -> None:
        path.parent.mkdir(parents=True, exist_ok=True)
        self.path = path
        self.stream = path.open("ab" if append else "wb")
        self.lock = threading.Lock()
        self.bytes_written = self.stream.tell()
        self.last_output_monotonic: float | None = None
        self.mirror_enabled = True

    def write(self, data: bytes, mirror: bool) -> None:
        with self.lock:
            self.stream.write(data)
            self.stream.flush()
            self.bytes_written += len(data)
            self.last_output_monotonic = time.monotonic()
        if mirror and self.mirror_enabled:
            try:
                sys.stdout.buffer.write(data)
                sys.stdout.buffer.flush()
            except BrokenPipeError:
                self.mirror_enabled = False

    def sync(self) -> None:
        with self.lock:
            self.stream.flush()
            os.fsync(self.stream.fileno())

    def close(self) -> None:
        self.sync()
        self.stream.close()


def parser() -> argparse.ArgumentParser:
    value = argparse.ArgumentParser(
        description=(
            "Run a command without a shell, tee combined output to a durable log, "
            "and atomically refresh a JSON status file."
        )
    )
    value.add_argument("--log", type=Path, required=True)
    value.add_argument("--status", type=Path)
    value.add_argument("--lock", type=Path)
    value.add_argument("--heartbeat-seconds", type=float, default=60.0)
    value.add_argument("--metrics-sample-seconds", type=float, default=1.0)
    value.add_argument("--timeout-seconds", type=float)
    value.add_argument("--terminate-grace-seconds", type=float, default=10.0)
    value.add_argument("--cwd", type=Path, default=Path.cwd())
    value.add_argument("--append", action="store_true")
    value.add_argument("--quiet", action="store_true")
    value.add_argument("command", nargs=argparse.REMAINDER)
    return value


def validated_arguments(arguments: list[str] | None) -> argparse.Namespace:
    values = parser().parse_args(arguments)
    if values.command and values.command[0] == "--":
        values.command = values.command[1:]
    if not values.command:
        parser().error("a command is required after --")
    if values.heartbeat_seconds <= 0:
        parser().error("--heartbeat-seconds must be positive")
    if values.metrics_sample_seconds <= 0:
        parser().error("--metrics-sample-seconds must be positive")
    if values.timeout_seconds is not None and values.timeout_seconds <= 0:
        parser().error("--timeout-seconds must be positive")
    if values.terminate_grace_seconds < 0:
        parser().error("--terminate-grace-seconds cannot be negative")
    values.cwd = values.cwd.resolve()
    values.log = values.log.resolve()
    values.status = (values.status or values.log.with_suffix(values.log.suffix + ".status.json")).resolve()
    values.lock = (
        locks.canonical_shared_lock_path(values.lock)
        if values.lock is not None else None
    )
    if values.log == values.status:
        parser().error("--log and --status must name different files")
    if values.lock is not None and values.lock in {values.log, values.status}:
        parser().error("--lock must differ from --log and --status")
    return values


def _run(arguments: list[str] | None = None) -> int:
    values = validated_arguments(arguments)
    run_id = str(uuid.uuid4())
    started_at = utc_now()
    started_monotonic = time.monotonic()
    durable_log = DurableLog(values.log, values.append)
    lock_handle = None
    process: subprocess.Popen[bytes] | None = None
    timed_out = False
    received_signal: int | None = None
    peak_rss_bytes: int | None = None
    peak_process_count: int | None = None

    status: dict[str, object] = {
        "schema": SCHEMA,
        "durable_telemetry_authoritative": False,
        "authority_scope": "none",
        "run_id": run_id,
        "state": "starting",
        "command": values.command,
        "cwd": str(values.cwd),
        "log_path": str(values.log),
        "status_path": str(values.status),
        "lock_path": None if values.lock is None else str(values.lock),
        "started_at": started_at,
        "updated_at": started_at,
        "heartbeat_seconds": values.heartbeat_seconds,
        "metrics_sample_seconds": values.metrics_sample_seconds,
        "timeout_seconds": values.timeout_seconds,
        "runner_pid": os.getpid(),
        "pid": None,
        "elapsed_seconds": 0.0,
        "bytes_written": durable_log.bytes_written,
        "last_output_seconds_ago": None,
        "process_count": None,
        "rss_bytes": None,
        "cpu_percent": None,
        "peak_process_count": None,
        "peak_rss_bytes": None,
        "exit_code": None,
    }
    atomic_json_write(values.status, status)

    if values.lock is not None:
        owner: str | None = None
        try:
            lock_handle = locks.open_lock_file(values.lock)
            values.lock = lock_handle.path
            status["lock_path"] = str(values.lock)
            try:
                fcntl.flock(
                    lock_handle.descriptor,
                    fcntl.LOCK_EX | fcntl.LOCK_NB,
                )
            except BlockingIOError:
                owner = os.pread(lock_handle.descriptor, 4096, 0).decode(
                    "utf-8", errors="replace"
                ).strip()
                raise locks.CheckpointError(
                    "shared heavy lock is unavailable"
                    + (f" ({owner})" if owner else "")
                )
            lock_mode_migrated = lock_handle.migrate_legacy_mode_after_exclusive_lock()
            status.update(
                lock_protocol=locks.SHARED_LOCK_PROTOCOL,
                lock_path=str(lock_handle.path),
                lock_mode="0600",
                lock_mode_migrated=lock_mode_migrated,
            )
            atomic_json_write(values.status, status)
        except locks.CheckpointError as error:
            status.update(
                state="lock-unavailable",
                updated_at=utc_now(),
                finished_at=utc_now(),
                elapsed_seconds=round(time.monotonic() - started_monotonic, 3),
                exit_code=75,
                lock_error=str(error),
                legacy_untrusted_lock_payload=owner or None,
            )
            atomic_json_write(values.status, status)
            print(f"long-run lock unavailable: {values.lock} ({error})", file=sys.stderr)
            if lock_handle is not None:
                lock_handle.close()
            durable_log.close()
            return 75

    def forward_signal(signum: int, _frame: object) -> None:
        nonlocal received_signal
        received_signal = signum
        if process is None or process.poll() is not None:
            return
        try:
            os.killpg(process.pid, signum)
        except (AttributeError, OSError):
            process.send_signal(signum)

    managed_signals = (
        (signal.SIGINT, signal.SIGTERM)
        if threading.current_thread() is threading.main_thread()
        else ()
    )
    previous_handlers = {signum: signal.getsignal(signum) for signum in managed_signals}
    for signum in previous_handlers:
        signal.signal(signum, forward_signal)

    reader: threading.Thread | None = None

    def sample_process_tree(root_pid: int) -> dict[str, object]:
        nonlocal peak_process_count, peak_rss_bytes
        metrics = process_tree_metrics(root_pid)
        process_count = metrics.get("process_count")
        rss_bytes = metrics.get("rss_bytes")
        if isinstance(process_count, int):
            peak_process_count = max(peak_process_count or 0, process_count)
        if isinstance(rss_bytes, int):
            peak_rss_bytes = max(peak_rss_bytes or 0, rss_bytes)
        return {
            **metrics,
            "peak_process_count": peak_process_count,
            "peak_rss_bytes": peak_rss_bytes,
        }

    try:
        try:
            process = subprocess.Popen(
                values.command,
                cwd=values.cwd,
                stdout=subprocess.PIPE,
                stderr=subprocess.STDOUT,
                start_new_session=True,
            )
        except OSError as error:
            status.update(
                state="launch-failed",
                updated_at=utc_now(),
                elapsed_seconds=round(time.monotonic() - started_monotonic, 3),
                exit_code=127,
                error=str(error),
            )
            atomic_json_write(values.status, status)
            print(f"long-run launch failed: {error}", file=sys.stderr)
            return 127

        latest_metrics = sample_process_tree(process.pid)
        status.update(
            state="running",
            pid=process.pid,
            updated_at=utc_now(),
            **latest_metrics,
        )
        atomic_json_write(values.status, status)

        assert process.stdout is not None

        def copy_output() -> None:
            while True:
                chunk = process.stdout.read1(65536)
                if not chunk:
                    return
                durable_log.write(chunk, mirror=not values.quiet)

        reader = threading.Thread(target=copy_output, name="long-run-output", daemon=True)
        reader.start()

        next_heartbeat = time.monotonic()
        next_metrics_sample = time.monotonic() + values.metrics_sample_seconds
        termination_started: float | None = None
        while process.poll() is None:
            now = time.monotonic()
            elapsed = now - started_monotonic
            timeout_reached = (
                values.timeout_seconds is not None
                and elapsed >= values.timeout_seconds
            )
            should_terminate = received_signal is not None or timeout_reached
            if should_terminate:
                timed_out = timed_out or timeout_reached
                if termination_started is None:
                    termination_started = now
                    if received_signal is None:
                        try:
                            os.killpg(process.pid, signal.SIGTERM)
                        except OSError:
                            process.terminate()
                elif now - termination_started >= values.terminate_grace_seconds:
                    try:
                        os.killpg(process.pid, signal.SIGKILL)
                    except OSError:
                        process.kill()
            if now >= next_metrics_sample:
                latest_metrics = sample_process_tree(process.pid)
                next_metrics_sample = now + values.metrics_sample_seconds
            if now >= next_heartbeat:
                durable_log.sync()
                last_output_age = (
                    None
                    if durable_log.last_output_monotonic is None
                    else round(now - durable_log.last_output_monotonic, 3)
                )
                status.update(
                    state="terminating" if timed_out or received_signal else "running",
                    updated_at=utc_now(),
                    elapsed_seconds=round(elapsed, 3),
                    bytes_written=durable_log.bytes_written,
                    last_output_seconds_ago=last_output_age,
                    **latest_metrics,
                )
                atomic_json_write(values.status, status)
                if not values.quiet:
                    rss = status.get("rss_bytes")
                    rss_text = "unknown" if rss is None else f"{int(rss) / (1024 ** 3):.2f} GiB"
                    print(
                        f"[heartbeat] pid={process.pid} elapsed={elapsed:.1f}s "
                        f"rss={rss_text} output={durable_log.bytes_written}B",
                        file=sys.stderr,
                        flush=True,
                    )
                next_heartbeat = now + values.heartbeat_seconds
            time.sleep(
                min(
                    0.2,
                    values.heartbeat_seconds / 4,
                    values.metrics_sample_seconds / 2,
                )
            )

        exit_code = process.wait()
        reader.join(timeout=5.0)
        process.stdout.close()
        durable_log.sync()
        finished = time.monotonic()
        reported_exit_code = (
            124
            if timed_out
            else 128 + received_signal
            if received_signal is not None
            else 128 - exit_code
            if exit_code < 0
            else exit_code
        )
        last_output_age = (
            None
            if durable_log.last_output_monotonic is None
            else round(finished - durable_log.last_output_monotonic, 3)
        )
        status.update(
            state=(
                "timed-out"
                if timed_out
                else "signaled"
                if received_signal is not None
                else "succeeded"
                if exit_code == 0
                else "failed"
            ),
            updated_at=utc_now(),
            finished_at=utc_now(),
            elapsed_seconds=round(finished - started_monotonic, 3),
            bytes_written=durable_log.bytes_written,
            last_output_seconds_ago=last_output_age,
            exit_code=reported_exit_code,
            child_exit_code=exit_code,
            received_signal=received_signal,
            timed_out=timed_out,
            **latest_metrics,
        )
        atomic_json_write(values.status, status)
        return reported_exit_code
    finally:
        for signum, handler in previous_handlers.items():
            signal.signal(signum, handler)
        lock_error: BaseException | None = None
        if lock_handle is not None:
            try:
                lock_handle.validate()
            except BaseException as error:
                lock_error = error
            finally:
                fcntl.flock(lock_handle.descriptor, fcntl.LOCK_UN)
                lock_handle.close()
        durable_log.close()
        if lock_error is not None:
            raise lock_error


def run(arguments: list[str] | None = None) -> int:
    try:
        return _run(arguments)
    except locks.CheckpointError as error:
        try:
            values = validated_arguments(arguments)
        except locks.CheckpointError:
            values = parser().parse_args(arguments)
            if values.command and values.command[0] == "--":
                values.command = values.command[1:]
            values.log = values.log.resolve()
            values.status = (
                values.status
                or values.log.with_suffix(values.log.suffix + ".status.json")
            ).resolve()
        try:
            status = json.loads(values.status.read_text(encoding="utf-8"))
        except (OSError, json.JSONDecodeError):
            status = {"schema": SCHEMA, "command": values.command}
        status.update(
            state="lock-unsafe",
            updated_at=utc_now(),
            finished_at=utc_now(),
            exit_code=75,
            durable_telemetry_authoritative=False,
            proof_authority_granted=False,
            authority_scope="none",
            lock_validation_error=str(error),
        )
        atomic_json_write(values.status, status)
        print(f"long-run lock validation failed: {error}", file=sys.stderr)
        return 75


def main() -> None:
    raise SystemExit(run())


if __name__ == "__main__":
    main()
