#!/usr/bin/env python3
"""Run a long verification command with durable output and status heartbeats."""

from __future__ import annotations

import argparse
import datetime as dt
import json
import os
from pathlib import Path
import signal
import subprocess
import sys
import threading
import time
import uuid


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


def process_tree_metrics(root_pid: int) -> dict[str, object]:
    """Return best-effort aggregate RSS and CPU for a local process tree."""
    try:
        result = subprocess.run(
            ["ps", "-axo", "pid=,ppid=,rss=,%cpu="],
            check=True,
            capture_output=True,
            text=True,
        )
        rows: dict[int, tuple[int, int, float]] = {}
        for line in result.stdout.splitlines():
            fields = line.split()
            if len(fields) != 4:
                continue
            pid, ppid, rss_kib = map(int, fields[:3])
            rows[pid] = (ppid, rss_kib, float(fields[3]))
        descendants = {root_pid}
        changed = True
        while changed:
            changed = False
            for pid, (ppid, _, _) in rows.items():
                if pid not in descendants and ppid in descendants:
                    descendants.add(pid)
                    changed = True
        present = [rows[pid] for pid in descendants if pid in rows]
        return {
            "process_count": len(present),
            "rss_bytes": sum(row[1] for row in present) * 1024,
            "cpu_percent": round(sum(row[2] for row in present), 2),
        }
    except (OSError, subprocess.SubprocessError, ValueError):
        return {"process_count": None, "rss_bytes": None, "cpu_percent": None}


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
    value.add_argument("--heartbeat-seconds", type=float, default=60.0)
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
    if values.timeout_seconds is not None and values.timeout_seconds <= 0:
        parser().error("--timeout-seconds must be positive")
    if values.terminate_grace_seconds < 0:
        parser().error("--terminate-grace-seconds cannot be negative")
    values.cwd = values.cwd.resolve()
    values.log = values.log.resolve()
    values.status = (values.status or values.log.with_suffix(values.log.suffix + ".status.json")).resolve()
    if values.log == values.status:
        parser().error("--log and --status must name different files")
    return values


def run(arguments: list[str] | None = None) -> int:
    values = validated_arguments(arguments)
    run_id = str(uuid.uuid4())
    started_at = utc_now()
    started_monotonic = time.monotonic()
    durable_log = DurableLog(values.log, values.append)
    process: subprocess.Popen[bytes] | None = None
    timed_out = False
    received_signal: int | None = None

    status: dict[str, object] = {
        "schema": SCHEMA,
        "run_id": run_id,
        "state": "starting",
        "command": values.command,
        "cwd": str(values.cwd),
        "log_path": str(values.log),
        "status_path": str(values.status),
        "started_at": started_at,
        "updated_at": started_at,
        "heartbeat_seconds": values.heartbeat_seconds,
        "timeout_seconds": values.timeout_seconds,
        "runner_pid": os.getpid(),
        "pid": None,
        "elapsed_seconds": 0.0,
        "bytes_written": durable_log.bytes_written,
        "last_output_seconds_ago": None,
        "exit_code": None,
    }
    atomic_json_write(values.status, status)

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

        status.update(state="running", pid=process.pid, updated_at=utc_now())
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
                    **process_tree_metrics(process.pid),
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
            time.sleep(min(0.2, values.heartbeat_seconds / 4))

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
        )
        atomic_json_write(values.status, status)
        return reported_exit_code
    finally:
        for signum, handler in previous_handlers.items():
            signal.signal(signum, handler)
        durable_log.close()


def main() -> None:
    raise SystemExit(run())


if __name__ == "__main__":
    main()
