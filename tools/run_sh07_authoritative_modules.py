#!/usr/bin/env python3
"""Checkpoint fresh SH-07 authoritative module runs without aggregating authority."""

from __future__ import annotations

import argparse
import dataclasses
import datetime as dt
import fcntl
import hashlib
import json
import os
from pathlib import Path
import platform
import re
import shutil
import signal
import stat
import subprocess
import sys
import time
import uuid
from collections.abc import Callable, Sequence


SCHEMA = "gravity/sh07-authoritative-module-checkpoints-v1"
TOOL_VERSION = 1
DEFAULT_LOCK = Path("/tmp/gravity-sh07-heavy.lock")
RUNNER_NAMESPACE = "gravity.self-hosting.sh07-authoritative-runner"
MODULE_PATTERN = re.compile(r"^[A-Za-z0-9][A-Za-z0-9._-]*$")


class CheckpointError(RuntimeError):
    pass


@dataclasses.dataclass(frozen=True)
class ProcessOutcome:
    exit_code: int
    timed_out: bool
    elapsed_seconds: float


Launcher = Callable[[Sequence[str], Path, Path, Path, float], ProcessOutcome]
OutputValidator = Callable[[str, Path], bool]


def utc_now() -> str:
    return dt.datetime.now(dt.timezone.utc).isoformat().replace("+00:00", "Z")


def atomic_json_write(path: Path, value: dict[str, object]) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    temporary = path.with_name(f".{path.name}.{os.getpid()}.{uuid.uuid4().hex}.tmp")
    with temporary.open("w", encoding="utf-8") as stream:
        json.dump(value, stream, indent=2, sort_keys=True)
        stream.write("\n")
        stream.flush()
        os.fsync(stream.fileno())
    os.replace(temporary, path)
    directory_fd = os.open(path.parent, os.O_RDONLY)
    try:
        os.fsync(directory_fd)
    finally:
        os.close(directory_fd)


def sha256_file(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as stream:
        for block in iter(lambda: stream.read(1024 * 1024), b""):
            digest.update(block)
    return f"sha256:{digest.hexdigest()}"


def relevant_files(root: Path) -> list[Path]:
    required = [
        root / "deps.edn",
        root / "bootstrap/clojure/test/gravity/self_hosting/sh07_proof_contract.edn",
        root / "bootstrap/clojure/test/gravity/self_hosting/sh07_authoritative_runner.clj",
        Path(__file__).resolve(),
    ]
    for directory in [root / "bootstrap/clojure/src", root / "bootstrap/gravity/src"]:
        if not directory.is_dir():
            raise CheckpointError(f"required fingerprint directory is absent: {directory}")
        required.extend(path for path in directory.rglob("*") if path.is_file())
    missing = [str(path) for path in required if not path.is_file()]
    if missing:
        raise CheckpointError(f"required fingerprint files are absent: {missing}")
    return sorted(set(path.resolve() for path in required), key=str)


def command_capture(
    command: Sequence[str], *, cwd: Path, timeout: float = 30
) -> dict[str, object]:
    try:
        result = subprocess.run(
            list(command), cwd=cwd, capture_output=True, text=True,
            timeout=timeout, check=False
        )
    except (OSError, subprocess.TimeoutExpired) as error:
        return {"command": list(command), "error": str(error), "complete": False}
    return {
        "command": list(command),
        "exit_code": result.returncode,
        "stdout": result.stdout,
        "stderr": result.stderr,
        "complete": result.returncode == 0,
    }


def runtime_identity(
    root: Path, base_command: Sequence[str], required: bool
) -> dict[str, object]:
    launcher = str(base_command[0])
    java_home = os.environ.get("JAVA_HOME")
    java_candidate = (
        Path(java_home) / "bin/java" if java_home else Path(shutil.which("java") or "java")
    )
    java_path = java_candidate.expanduser().resolve()
    sdescribe = command_capture([launcher, "-Sdescribe"], cwd=root)
    config_match = re.search(
        r":config-files\s+\[(.*?)\]", str(sdescribe.get("stdout", "")), re.DOTALL
    )
    config_names = re.findall(r'"([^"]+)"', config_match.group(1)) if config_match else []
    config_files = []
    for name in config_names:
        candidate = Path(name).expanduser()
        path = candidate if candidate.is_absolute() else root / candidate
        resolved = path.resolve()
        config_files.append(
            {
                "path": str(resolved),
                "sha256": sha256_file(resolved) if resolved.is_file() else None,
            }
        )
    identity = {
        "required": required,
        "operating_system": platform.system(),
        "operating_system_release": platform.release(),
        "architecture": platform.machine(),
        "java_path": str(java_path),
        "java_sha256": sha256_file(java_path) if java_path.is_file() else None,
        "java_version": command_capture([str(java_path), "-version"], cwd=root),
        "clojure_sdescribe": sdescribe,
        "clojure_classpath": command_capture([launcher, "-Spath"], cwd=root),
        "clojure_config_files": config_files,
    }
    complete = bool(
        java_path.is_file()
        and identity["java_version"]["complete"]
        and identity["clojure_sdescribe"]["complete"]
        and identity["clojure_classpath"]["complete"]
        and config_match is not None
        and all(entry["sha256"] for entry in config_files)
    )
    identity["complete"] = complete
    if required and not complete:
        raise CheckpointError("Java/Clojure runtime identity is incomplete")
    return identity


def context_fingerprint(
    root: Path, base_command: Sequence[str], *, require_runtime_identity: bool
) -> dict[str, object]:
    root = root.resolve()
    entries: list[dict[str, object]] = []
    combined = hashlib.sha256()
    for path in relevant_files(root):
        try:
            relative = path.relative_to(root).as_posix()
        except ValueError:
            relative = f"external:{path}"
        content_hash = sha256_file(path)
        size = path.stat().st_size
        entries.append({"path": relative, "size": size, "sha256": content_hash})
        combined.update(relative.encode("utf-8"))
        combined.update(b"\0")
        combined.update(content_hash.encode("ascii"))
        combined.update(b"\0")
    command = [str(value) for value in base_command]
    executable = Path(shutil.which(command[0]) or command[0]).expanduser()
    executable_hash = sha256_file(executable.resolve()) if executable.is_file() else None
    context = {
        "tool_version": TOOL_VERSION,
        "command": command,
        "resolved_executable": str(executable),
        "resolved_executable_sha256": executable_hash,
        "environment": {
            name: os.environ.get(name)
            for name in [
                "JAVA_HOME",
                "JAVA_OPTS",
                "JAVA_TOOL_OPTIONS",
                "_JAVA_OPTIONS",
                "JDK_JAVA_OPTIONS",
                "CLJ_JVM_OPTS",
                "CLJ_CONFIG",
            ]
        },
        "runtime": runtime_identity(root, command, require_runtime_identity),
        "files": entries,
    }
    combined.update(json.dumps(context, sort_keys=True, separators=(",", ":")).encode())
    context["sha256"] = f"sha256:{combined.hexdigest()}"
    return context


def default_base_command() -> list[str]:
    return [
        "clojure",
        "-J-Xmx8g",
        "-Sdeps",
        '{:paths ["bootstrap/clojure/src" "bootstrap/clojure/test"]}',
        "-M",
        "-m",
        RUNNER_NAMESPACE,
    ]


def ensure_owned_directory(path: Path) -> None:
    if path.is_symlink():
        raise CheckpointError(f"checkpoint directory cannot be a symlink: {path}")
    path.mkdir(parents=True, exist_ok=True)
    metadata = os.lstat(path)
    if not stat.S_ISDIR(metadata.st_mode) or metadata.st_uid != os.geteuid():
        raise CheckpointError(
            f"checkpoint directory must be owned by the current user: {path}"
        )


def open_output_file(path: Path):
    if not hasattr(os, "O_NOFOLLOW") or not hasattr(os, "O_DIRECTORY"):
        raise CheckpointError("platform cannot create output files without following symlinks")
    directory_fd = os.open(
        path.parent,
        os.O_RDONLY | os.O_DIRECTORY | os.O_NOFOLLOW,
    )
    try:
        descriptor = os.open(
            path.name,
            os.O_WRONLY | os.O_CREAT | os.O_NOFOLLOW,
            0o600,
            dir_fd=directory_fd,
        )
    except OSError as error:
        raise CheckpointError(f"module output cannot be opened safely: {path}") from error
    finally:
        os.close(directory_fd)
    try:
        metadata = os.fstat(descriptor)
        if (
            not stat.S_ISREG(metadata.st_mode)
            or metadata.st_uid != os.geteuid()
            or metadata.st_nlink != 1
        ):
            raise CheckpointError(
                f"module output must be one owned regular file: {path}"
            )
        os.ftruncate(descriptor, 0)
        return os.fdopen(descriptor, "wb")
    except BaseException:
        os.close(descriptor)
        raise


def default_launcher(
    command: Sequence[str],
    cwd: Path,
    stdout_path: Path,
    stderr_path: Path,
    timeout_seconds: float,
) -> ProcessOutcome:
    started = time.monotonic()
    timed_out = False
    with open_output_file(stdout_path) as stdout, open_output_file(stderr_path) as stderr:
        process = subprocess.Popen(
            list(command),
            cwd=cwd,
            stdout=stdout,
            stderr=stderr,
            start_new_session=True,
        )
        try:
            exit_code = process.wait(timeout=timeout_seconds)
        except subprocess.TimeoutExpired:
            timed_out = True
            try:
                os.killpg(process.pid, signal.SIGTERM)
            except OSError:
                process.terminate()
            try:
                process.wait(timeout=5)
            except subprocess.TimeoutExpired:
                pass
            # The group leader may exit promptly while a descendant ignores
            # SIGTERM. Kill the process group regardless of leader state so no
            # memory-heavy descendant can outlive the shared lock.
            try:
                os.killpg(process.pid, signal.SIGKILL)
            except ProcessLookupError:
                pass
            if process.poll() is None:
                process.kill()
                process.wait()
            exit_code = 124
        stdout.flush()
        stderr.flush()
        os.fsync(stdout.fileno())
        os.fsync(stderr.fileno())
    return ProcessOutcome(exit_code, timed_out, time.monotonic() - started)


EDN_OUTPUT_VALIDATOR = r"""
(require '[clojure.edn :as edn] '[clojure.java.io :as io])
(let [path (System/getenv "GRAVITY_SH07_OUTPUT_PATH")
      expected-module (System/getenv "GRAVITY_SH07_EXPECTED_MODULE")
      passed?
      (with-open [reader (java.io.PushbackReader. (io/reader path))]
        (let [value (edn/read {:eof ::eof} reader)
              trailing (edn/read {:eof ::eof} reader)
              modules (:modules value)
              result (when (= 1 (count modules)) (first modules))
              checks (:contract-checks result)]
          (and (= ::eof trailing)
               (= :gravity/sh07-authoritative-proof-run (:artifact value))
               (= 1 (:schema-version value))
               (= :passed (:status value))
               (true? (:fresh-process-required? value))
               (false? (:persistent-iteration-cache-used? value))
               (vector? modules)
               (= expected-module (:module result))
               (= :accepted (:status result))
               (= :passed (:verification-status result))
               (= :complete (:capability-proof-status result))
               (empty? (:failed-checks result))
               (map? checks)
               (seq checks)
               (every? true? (vals checks)))))]
  (when-not passed? (System/exit 1)))
"""


def output_contract_passed(
    module: str, stdout_path: Path, *, clojure_command: str, cwd: Path
) -> bool:
    try:
        result = subprocess.run(
            [
                clojure_command,
                "-Srepro",
                "-M",
                "-e",
                EDN_OUTPUT_VALIDATOR,
            ],
            cwd=cwd,
            env={
                **os.environ,
                "GRAVITY_SH07_OUTPUT_PATH": str(stdout_path.resolve()),
                "GRAVITY_SH07_EXPECTED_MODULE": module,
            },
            stdout=subprocess.DEVNULL,
            stderr=subprocess.DEVNULL,
            timeout=60,
            check=False,
        )
    except (OSError, subprocess.TimeoutExpired):
        return False
    return result.returncode == 0


def valid_module_name(value: object) -> bool:
    return isinstance(value, str) and MODULE_PATTERN.fullmatch(value) is not None


def resumable(
    module: str,
    entry: object,
    fingerprint: str,
    command: Sequence[str],
    state_dir: Path,
) -> bool:
    if not isinstance(entry, dict):
        return False
    if not (
        entry.get("state") == "passed"
        and entry.get("exit_code") == 0
        and entry.get("context_fingerprint") == fingerprint
        and entry.get("command") == list(command)
        and entry.get("output_contract_checked") is True
    ):
        return False
    for kind in ["stdout", "stderr"]:
        relative = entry.get(f"{kind}_path")
        expected = entry.get(f"{kind}_sha256")
        canonical = f"modules/{module}.{kind}.log"
        if relative != canonical or not isinstance(expected, str):
            return False
        path = state_dir / canonical
        try:
            contained = path.resolve().is_relative_to(state_dir.resolve())
        except OSError:
            return False
        if (
            not contained
            or path.is_symlink()
            or not path.is_file()
            or sha256_file(path) != expected
        ):
            return False
    return True


def load_manifest(path: Path) -> dict[str, object] | None:
    if not path.exists():
        return None
    try:
        value = json.loads(path.read_text(encoding="utf-8"))
    except (OSError, json.JSONDecodeError) as error:
        raise CheckpointError(f"checkpoint manifest is unreadable: {error}") from error
    if not isinstance(value, dict) or value.get("schema") != SCHEMA:
        raise CheckpointError("checkpoint manifest has an unsupported schema")
    return value


def open_lock_file(lock_path: Path):
    if not hasattr(os, "O_NOFOLLOW"):
        raise CheckpointError("platform cannot open the shared lock without following symlinks")
    absolute = Path(os.path.abspath(lock_path.expanduser()))
    absolute.parent.mkdir(parents=True, exist_ok=True)
    try:
        descriptor = os.open(
            absolute,
            os.O_RDWR | os.O_CREAT | os.O_NOFOLLOW,
            0o600,
        )
    except OSError as error:
        raise CheckpointError(f"shared heavy lock cannot be opened safely: {absolute}") from error
    try:
        metadata = os.fstat(descriptor)
        if not stat.S_ISREG(metadata.st_mode) or metadata.st_uid != os.geteuid():
            raise CheckpointError(
                "shared heavy lock must be a regular file owned by the current user"
            )
        return absolute, os.fdopen(descriptor, "r+", encoding="utf-8")
    except BaseException:
        os.close(descriptor)
        raise


def run_modules(
    *,
    root: Path,
    state_dir: Path,
    modules: Sequence[str],
    base_command: Sequence[str] | None = None,
    timeout_seconds: float = 21600,
    resume: bool = True,
    launcher: Launcher = default_launcher,
    output_validator: OutputValidator | None = None,
    lock_path: Path | None = DEFAULT_LOCK,
) -> tuple[int, dict[str, object]]:
    if (
        not modules
        or len(set(modules)) != len(modules)
        or not all(valid_module_name(module) for module in modules)
    ):
        raise CheckpointError("selected modules must be nonempty, unique, safe slugs")
    if timeout_seconds <= 0:
        raise CheckpointError("timeout must be positive")
    root = root.resolve()
    state_dir = Path(os.path.abspath(state_dir.expanduser()))
    ensure_owned_directory(state_dir)
    modules_dir = state_dir / "modules"
    ensure_owned_directory(modules_dir)
    manifest_path = state_dir / "manifest.json"
    base = list(base_command or default_base_command())
    context = context_fingerprint(
        root, base, require_runtime_identity=launcher is default_launcher
    )
    fingerprint = str(context["sha256"])
    validator = output_validator or (
        lambda module, output: output_contract_passed(
            module, output, clojure_command=base[0], cwd=root
        )
    )

    lock_stream = None
    if lock_path is not None:
        lock_path, lock_stream = open_lock_file(lock_path)
        try:
            fcntl.flock(lock_stream.fileno(), fcntl.LOCK_EX | fcntl.LOCK_NB)
        except BlockingIOError as error:
            lock_stream.close()
            raise CheckpointError(f"shared heavy lock is unavailable: {lock_path}") from error
        lock_stream.seek(0)
        lock_stream.truncate()
        json.dump({"pid": os.getpid(), "acquired_at": utc_now()}, lock_stream)
        lock_stream.write("\n")
        lock_stream.flush()
        os.fsync(lock_stream.fileno())

    try:
        previous = load_manifest(manifest_path)
        same_context = bool(previous and previous.get("context_fingerprint") == fingerprint)
        previous_modules = previous.get("modules", {}) if same_context and resume else {}
        if not isinstance(previous_modules, dict):
            previous_modules = {}
        manifest: dict[str, object] = {
            "schema": SCHEMA,
            "tool_version": TOOL_VERSION,
            "state": "running",
            "context_fingerprint": fingerprint,
            "context": context,
            "selected_modules": list(modules),
            "modules": dict(previous_modules),
            "aggregate_authoritative": False,
            "authority_scope": "individual-existing-runner-outputs-only",
            "started_at": utc_now(),
            "updated_at": utc_now(),
        }
        if previous and not same_context:
            manifest["invalidated_context_fingerprint"] = previous.get("context_fingerprint")
        atomic_json_write(manifest_path, manifest)

        records = manifest["modules"]
        assert isinstance(records, dict)
        for module in modules:
            command = [*base, "--fresh", module]
            prior = records.get(module)
            if (
                resume
                and same_context
                and resumable(module, prior, fingerprint, command, state_dir)
                and validator(
                    module, state_dir / f"modules/{module}.stdout.log"
                )
            ):
                continue
            stdout_path = modules_dir / f"{module}.stdout.log"
            stderr_path = modules_dir / f"{module}.stderr.log"
            relative_stdout = stdout_path.relative_to(state_dir).as_posix()
            relative_stderr = stderr_path.relative_to(state_dir).as_posix()
            records[module] = {
                "state": "running",
                "command": command,
                "context_fingerprint": fingerprint,
                "stdout_path": relative_stdout,
                "stderr_path": relative_stderr,
                "started_at": utc_now(),
            }
            manifest["updated_at"] = utc_now()
            atomic_json_write(manifest_path, manifest)
            try:
                outcome = launcher(
                    command, root, stdout_path, stderr_path, timeout_seconds
                )
            except OSError as error:
                stdout_path.touch(exist_ok=True)
                stderr_path.write_text(str(error) + "\n", encoding="utf-8")
                outcome = ProcessOutcome(127, False, 0.0)
            context_after = context_fingerprint(
                root, base, require_runtime_identity=launcher is default_launcher
            )
            context_stable = context_after["sha256"] == fingerprint
            checked = (
                context_stable
                and outcome.exit_code == 0
                and validator(module, stdout_path)
            )
            state = (
                "context-changed"
                if not context_stable
                else "timed-out"
                if outcome.timed_out
                else "passed"
                if checked
                else "failed"
            )
            raw_exit_code = outcome.exit_code
            exit_code = (
                75
                if not context_stable
                else 124
                if outcome.timed_out
                else 0
                if checked
                else 128 + abs(raw_exit_code)
                if raw_exit_code < 0
                else raw_exit_code or 1
            )
            records[module] = {
                "state": state,
                "command": command,
                "context_fingerprint": fingerprint,
                "context_stable": context_stable,
                "context_fingerprint_after": context_after["sha256"],
                "stdout_path": relative_stdout,
                "stderr_path": relative_stderr,
                "stdout_sha256": sha256_file(stdout_path),
                "stderr_sha256": sha256_file(stderr_path),
                "output_contract_checked": checked,
                "exit_code": exit_code,
                "raw_child_exit_code": raw_exit_code,
                "timed_out": outcome.timed_out,
                "elapsed_seconds": round(outcome.elapsed_seconds, 3),
                "finished_at": utc_now(),
            }
            manifest["updated_at"] = utc_now()
            if state != "passed":
                manifest["state"] = state
                manifest["stopped_at_module"] = module
                manifest["finished_at"] = utc_now()
                atomic_json_write(manifest_path, manifest)
                return exit_code, manifest
            atomic_json_write(manifest_path, manifest)

        manifest["state"] = "completed"
        manifest["finished_at"] = utc_now()
        manifest["updated_at"] = utc_now()
        atomic_json_write(manifest_path, manifest)
        return 0, manifest
    finally:
        if lock_stream is not None:
            fcntl.flock(lock_stream.fileno(), fcntl.LOCK_UN)
            lock_stream.close()


def discover_modules(root: Path, base_command: Sequence[str], timeout: float) -> list[str]:
    result = subprocess.run(
        [*base_command, "--list"],
        cwd=root,
        capture_output=True,
        text=True,
        timeout=timeout,
        check=False,
    )
    if result.returncode != 0:
        raise CheckpointError(f"module listing failed: {result.stderr.strip()}")
    modules = [line.strip() for line in result.stdout.splitlines() if line.strip()]
    if (
        not modules
        or len(set(modules)) != len(modules)
        or not all(valid_module_name(module) for module in modules)
    ):
        raise CheckpointError("module listing was empty, duplicated, or unsafe")
    return modules


def parser() -> argparse.ArgumentParser:
    value = argparse.ArgumentParser(description=__doc__)
    selection = value.add_mutually_exclusive_group(required=True)
    selection.add_argument("--list", action="store_true")
    selection.add_argument("--all", action="store_true")
    selection.add_argument("--module", action="append")
    value.add_argument(
        "--state-dir",
        type=Path,
        default=Path("target/validation/sh07-authoritative-checkpoints"),
    )
    value.add_argument("--timeout-seconds", type=float, default=21600)
    value.add_argument("--lock", type=Path, default=DEFAULT_LOCK)
    value.add_argument("--no-resume", action="store_true")
    value.add_argument("--cwd", type=Path, default=Path(__file__).resolve().parents[1])
    return value


def main(arguments: list[str] | None = None) -> int:
    values = parser().parse_args(arguments)
    base = default_base_command()
    try:
        available = discover_modules(values.cwd, base, min(values.timeout_seconds, 120))
        if values.list:
            print("\n".join(available))
            return 0
        selected = available if values.all else values.module
        assert selected is not None
        unknown = sorted(set(selected) - set(available))
        if unknown:
            raise CheckpointError(f"unknown modules: {unknown}")
        code, manifest = run_modules(
            root=values.cwd,
            state_dir=values.state_dir,
            modules=selected,
            base_command=base,
            timeout_seconds=values.timeout_seconds,
            resume=not values.no_resume,
            lock_path=values.lock,
        )
        print(json.dumps({
            "state": manifest["state"],
            "manifest": str((values.state_dir / "manifest.json").resolve()),
            "aggregate_authoritative": False,
        }, sort_keys=True))
        return code
    except (CheckpointError, subprocess.TimeoutExpired) as error:
        print(f"SH-07 checkpoint runner failed: {error}", file=sys.stderr)
        return 75 if "lock is unavailable" in str(error) else 2


if __name__ == "__main__":
    raise SystemExit(main())
