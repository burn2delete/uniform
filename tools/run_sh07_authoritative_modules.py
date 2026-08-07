#!/usr/bin/env python3
"""Checkpoint fresh SH-07 authoritative module runs without aggregating authority."""

from __future__ import annotations

import argparse
import contextlib
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
from collections.abc import Callable, Mapping, Sequence


SCHEMA = "gravity/sh07-authoritative-module-checkpoints-v2"
TOOL_VERSION = 3
FINGERPRINT_POLICY_VERSION = 1
PROOF_OUTPUT_SCHEMA = 3
CENSUS_SCHEMA = 2
SOURCE_BOUND_POLICY = "source-bound-derived"
SOURCE_BOUND_ATTESTATION_SCHEMA = "gravity/sh07-source-bound-attestation-v1"
SOURCE_BOUND_UNSUPPORTED_CLAIMS = ["exact-authentic-coverage", "aggregate", "release"]
DEFAULT_LOCK = Path("/tmp/gravity-sh07-heavy.lock")
SHARED_LOCK_PROTOCOL = "gravity-sh07-heavy-flock-owned-0600-v1"
SHARED_LOCK_MODE = 0o600
RUNNER_NAMESPACE = "gravity.self-hosting.sh07-authoritative-runner"
PROOF_CONTRACT_RELATIVE = (
    "bootstrap/clojure/test/gravity/self_hosting/sh07_proof_contract.edn"
)
MODULE_PATTERN = re.compile(r"^[A-Za-z0-9][A-Za-z0-9._-]*$")
SOURCE_SHA_PATTERN = re.compile(r"^sha256:[0-9a-f]{64}$")


class CheckpointError(RuntimeError):
    pass


class SharedLockUnavailable(CheckpointError):
    pass


class SharedLockValidationError(CheckpointError):
    pass


@dataclasses.dataclass(frozen=True)
class ProcessOutcome:
    exit_code: int
    timed_out: bool
    elapsed_seconds: float


Launcher = Callable[[Sequence[str], Path, Path, Path, float], ProcessOutcome]
OutputValidator = Callable[[str, str, int, str, str, Path], bool]
CatalogProvider = Callable[[], Mapping[str, str]]
SourceContracts = Mapping[str, Mapping[str, object]]


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


def sha256_bytes(value: bytes) -> str:
    return f"sha256:{hashlib.sha256(value).hexdigest()}"


def canonical_json(value: object) -> bytes:
    return (json.dumps(value, sort_keys=True, separators=(",", ":"),
                       ensure_ascii=True) + "\n").encode("utf-8")


def source_bound_policy(root: Path) -> str:
    """Read and compare the duplicated top-level and nested policy markers."""
    contract = root / PROOF_CONTRACT_RELATIVE
    selector = r'''(do
(require '[clojure.edn :as edn] '[clojure.java.io :as io])
(let [contract (edn/read-string (slurp (System/getenv "GRAVITY_SH07_PROOF_CONTRACT")))
      top (:coverage-census-policy contract)
      nested (get-in contract [:authoritative-coverage-census :policy])]
  (when (and (= top nested)
             (contains? #{:exact-precommitted :source-bound-derived} top))
    (println (name top)))))'''
    try:
        result = subprocess.run(
            ["clojure", "-Srepro", "-M", "-e", selector],
            cwd=root,
            env={**os.environ, "GRAVITY_SH07_PROOF_CONTRACT": str(contract.resolve())},
            capture_output=True, text=True, timeout=60, check=False,
        )
    except (OSError, subprocess.TimeoutExpired) as error:
        raise CheckpointError("proof contract policy cannot be parsed") from error
    policy = result.stdout.strip()
    if result.returncode != 0 or result.stderr.strip() or policy not in {
            "exact-precommitted", SOURCE_BOUND_POLICY}:
        raise CheckpointError("proof contract has no matching recognized coverage census policy")
    return policy


def _proof_output_binding(
    path: Path, module: str, *, clojure_command: str = "clojure",
    cwd: Path | None = None,
) -> dict[str, str | int]:
    """Select one module/result/census through the EDN reader, not regexes.

    Authoritative output contains many nested records with repeated keys.  The
    helper therefore parses one EDN value and selects the exact module result
    before printing its binding fields for the Python attestation layer.
    """
    selector = r'''
(do
(require '[clojure.edn :as edn] '[clojure.java.io :as io])
(defn read-one [path]
  (with-open [reader (java.io.PushbackReader. (io/reader path))]
    (let [value (edn/read {:eof ::eof} reader)
          trailing (edn/read {:eof ::eof} reader)]
      (when (or (= ::eof value) (not= ::eof trailing))
        (throw (ex-info "expected one EDN output" {})))
      value)))
(let [value (read-one (System/getenv "GRAVITY_SH07_OUTPUT_PATH"))
      module (System/getenv "GRAVITY_SH07_EXPECTED_MODULE")
      matches (filter #(= module (:module %)) (:modules value))
      result (first matches)
      census (:coverage-census result)]
  (when-not (and (= 1 (count matches))
                 (map? census)
                 (= :source-bound-derived (:coverage-census-policy census))
                 (false? (:counts-precommitted? census))
                 (false? (:independent-count-oracle? census)))
    (System/exit 1))
  (println (str (:source-path result) "\t"
                (:source-byte-count result) "\t"
                (:source-bytes-sha256 result) "\t"
                (:source-revision-id result) "\t"
                (:artifact-id result) "\t"
                (:census-hash census)))))
'''
    try:
        result = subprocess.run(
            [clojure_command, "-Srepro", "-M", "-e", selector],
            cwd=cwd or path.parent,
            env={**os.environ,
                 "GRAVITY_SH07_OUTPUT_PATH": str(path.resolve()),
                 "GRAVITY_SH07_EXPECTED_MODULE": module},
            capture_output=True, text=True, timeout=60, check=False,
        )
    except (OSError, subprocess.TimeoutExpired) as error:
        raise CheckpointError("authoritative stdout EDN binding could not be read") from error
    if result.returncode != 0 or result.stderr.strip():
        raise CheckpointError("authoritative stdout EDN binding is invalid")
    lines = result.stdout.splitlines()
    if len(lines) != 1 or lines[0].count("\t") != 5:
        raise CheckpointError("authoritative stdout EDN binding is ambiguous")
    source_path, size, source_sha, revision, artifact_id, census_hash = lines[0].split("\t")
    if not size.isdecimal() or source_sha != revision:
        raise CheckpointError("authoritative stdout source revision is not byte-bound")
    return {
        "source_path": source_path,
        "source_byte_count": int(size),
        "source_bytes_sha256": source_sha,
        "source_revision_id": revision,
        "artifact_id": artifact_id,
        "census_hash": census_hash,
    }


def _attestation_payload(attestation: Mapping[str, object]) -> dict[str, object]:
    return {str(key): value for key, value in attestation.items()
            if key != "attestation_sha256"}


def validate_source_bound_attestation(
    root: Path,
    module: str,
    stdout_path: Path,
    attestation: Mapping[str, object],
    *,
    expected_proof_contract_sha256: str,
) -> bool:
    """Validate a reviewed, source-bound attestation and all raw-file links."""
    if not isinstance(attestation, Mapping):
        return False
    try:
        required = {
            "artifact", "schema", "module", "authority_scope", "source",
            "proof_contract_sha256", "stdout_sha256", "artifact_id", "census_hash",
            "reviewer", "reviewed_at", "method", "limitations", "decision",
            "claims", "attestation_sha256",
        }
        if set(attestation) != required:
            return False
        if (attestation["artifact"] != "gravity/sh07-source-bound-attestation"
                or attestation["schema"] != SOURCE_BOUND_ATTESTATION_SCHEMA
                or attestation["module"] != module
                or attestation["authority_scope"] != "individual-source-bound-derived"):
            return False
        if not isinstance(attestation["proof_contract_sha256"], str) or \
                attestation["proof_contract_sha256"] != expected_proof_contract_sha256:
            return False
        if sha256_file(root / PROOF_CONTRACT_RELATIVE) != expected_proof_contract_sha256:
            return False
        if source_bound_policy(root) != SOURCE_BOUND_POLICY:
            return False
        if SOURCE_SHA_PATTERN.fullmatch(str(attestation["stdout_sha256"])) is None:
            return False
        if SOURCE_SHA_PATTERN.fullmatch(str(attestation["artifact_id"])) is None or \
                SOURCE_SHA_PATTERN.fullmatch(str(attestation["census_hash"])) is None:
            return False
        source = attestation["source"]
        if not isinstance(source, Mapping) or set(source) != {
                "path", "byte_count", "bytes_sha256"}:
            return False
        relative = source["path"]
        if (not isinstance(relative, str) or Path(relative).is_absolute()
                or Path(relative).as_posix() != relative or ".." in Path(relative).parts):
            return False
        source_path = root / relative
        if source_path.is_symlink() or not source_path.is_file():
            return False
        actual_size = source_path.stat().st_size
        actual_sha = sha256_file(source_path)
        if source["byte_count"] != actual_size or source["bytes_sha256"] != actual_sha:
            return False
        if sha256_file(stdout_path) != attestation["stdout_sha256"]:
            return False
        binding = _proof_output_binding(stdout_path, module, cwd=root)
        if any(binding[key] != source[value] for key, value in (
                ("source_path", "path"),
                ("source_byte_count", "byte_count"),
                ("source_bytes_sha256", "bytes_sha256"))):
            return False
        if binding["artifact_id"] != attestation["artifact_id"] or \
                binding["census_hash"] != attestation["census_hash"]:
            return False
        reviewer = attestation["reviewer"]
        if not isinstance(reviewer, str) or not reviewer.strip():
            return False
        reviewed_at = attestation["reviewed_at"]
        if not isinstance(reviewed_at, str) or not reviewed_at.endswith("Z"):
            return False
        if not isinstance(attestation["method"], str) or not attestation["method"].strip():
            return False
        limitations = attestation["limitations"]
        if (not isinstance(limitations, list) or not limitations
                or not all(isinstance(item, str) and item.strip() for item in limitations)):
            return False
        if attestation["decision"] != "accepted-with-scope":
            return False
        claims = attestation["claims"]
        if (not isinstance(claims, Mapping)
                or set(claims) != {"counts_precommitted", "independent_count_oracle",
                                   "aggregate_authoritative", "release_authoritative",
                                   "unsupported"}
                or claims["counts_precommitted"] is not False
                or claims["independent_count_oracle"] is not False
                or claims["aggregate_authoritative"] is not False
                or claims["release_authoritative"] is not False
                or claims["unsupported"] != SOURCE_BOUND_UNSUPPORTED_CLAIMS):
            return False
        digest = attestation.get("attestation_sha256")
        return isinstance(digest, str) and digest == sha256_bytes(canonical_json(_attestation_payload(attestation)))
    except (OSError, CheckpointError, TypeError, ValueError):
        return False


def create_source_bound_attestation(
    root: Path,
    module: str,
    stdout_path: Path,
    *,
    proof_contract_sha256: str,
    reviewer: str,
    reviewed_at: str,
    method: str,
    limitations: Sequence[str],
    decision: str = "accepted-with-scope",
) -> dict[str, object]:
    policy = source_bound_policy(root)
    if policy != SOURCE_BOUND_POLICY:
        raise CheckpointError("source-bound attestation requires source-bound-derived policy")
    binding = _proof_output_binding(stdout_path, module, cwd=root)
    payload: dict[str, object] = {
        "artifact": "gravity/sh07-source-bound-attestation",
        "schema": SOURCE_BOUND_ATTESTATION_SCHEMA,
        "module": module,
        "authority_scope": "individual-source-bound-derived",
        "source": {
            "path": binding["source_path"],
            "byte_count": binding["source_byte_count"],
            "bytes_sha256": binding["source_bytes_sha256"],
        },
        "proof_contract_sha256": proof_contract_sha256,
        "stdout_sha256": sha256_file(stdout_path),
        "artifact_id": binding["artifact_id"],
        "census_hash": binding["census_hash"],
        "reviewer": reviewer,
        "reviewed_at": reviewed_at,
        "method": method,
        "limitations": list(limitations),
        "decision": decision,
        "claims": {
            "counts_precommitted": False,
            "independent_count_oracle": False,
            "aggregate_authoritative": False,
            "release_authoritative": False,
            "unsupported": SOURCE_BOUND_UNSUPPORTED_CLAIMS,
        },
    }
    payload["attestation_sha256"] = sha256_bytes(canonical_json(payload))
    if not validate_source_bound_attestation(
            root, module, stdout_path, payload,
            expected_proof_contract_sha256=proof_contract_sha256):
        raise CheckpointError("generated source-bound attestation failed self-validation")
    return payload


SHARED_GRAVITY_FILES = (
    "bootstrap/gravity/p15_s23/compiler.gravity",
    "bootstrap/gravity/p15_s23/emitter.gravity",
    "bootstrap/gravity/src/gravity/macro.gravity",
    "bootstrap/gravity/src/gravity/resolution.gravity",
    "bootstrap/gravity/src/gravity/checked_core.gravity",
)

# Static repository inputs in the shared checkpoint fingerprint.  Keep path
# classification and shared_files() derived from this single policy so an
# integration admission check cannot drift from checkpoint invalidation.
SHARED_REPOSITORY_FILES = (
    "deps.edn",
    *SHARED_GRAVITY_FILES,
    PROOF_CONTRACT_RELATIVE,
    "bootstrap/clojure/test/gravity/self_hosting/sh07_authoritative_runner.clj",
    "tools/run_sh07_authoritative_modules.py",
)
SHARED_REPOSITORY_TREES = ("bootstrap/clojure/src",)
ROOT_CLASSPATH_DIRECTORIES = (
    "bootstrap/clojure/src",
    "bootstrap/clojure/test",
)
ROOT_CLASSPATH_LOAD_RESOURCES = ("data_readers.clj", "data_readers.cljc")


def _normalized_repository_path(relative: str) -> str:
    if (
        not isinstance(relative, str)
        or not relative
        or "\t" in relative
        or "\n" in relative
    ):
        raise CheckpointError(
            f"fingerprint policy path is not normalized and relative: {relative!r}"
        )
    path = Path(relative)
    if (
        path == Path(".")
        or path.is_absolute()
        or path.as_posix() != relative
        or ".." in path.parts
    ):
        raise CheckpointError(
            f"fingerprint policy path is not normalized and relative: {relative!r}"
        )
    return relative


def classpath_structural_path(relative: str) -> bool:
    """Whether a repository path is inside a root-local classpath directory."""
    relative = _normalized_repository_path(relative)
    return any(
        relative == directory or relative.startswith(directory + "/")
        for directory in ROOT_CLASSPATH_DIRECTORIES
    )


def classify_fingerprint_path(
    relative: str, module_catalog: Mapping[str, str]
) -> str:
    """Classify one candidate path against checkpoint invalidation policy.

    Returns ``shared``, ``module``, ``unsafe``, or ``unrelated``.  Symlink and
    special-file identity remains a caller-side tree inspection; tracked AOT
    class shadows are recognizable from the path alone.
    """
    relative = _normalized_repository_path(relative)
    catalog = validated_module_catalog_paths(module_catalog)
    if classpath_structural_path(relative) and relative.endswith(".class"):
        return "unsafe"
    if relative in SHARED_REPOSITORY_FILES or any(
        relative == directory or relative.startswith(directory + "/")
        for directory in SHARED_REPOSITORY_TREES
    ):
        return "shared"
    for directory in ROOT_CLASSPATH_DIRECTORIES:
        prefix = directory + "/"
        if relative.startswith(prefix):
            nested = relative[len(prefix):]
            if nested in ROOT_CLASSPATH_LOAD_RESOURCES:
                return "shared"
    if relative in set(catalog.values()):
        return "module"
    return "unrelated"


def validated_module_catalog_paths(catalog: Mapping[str, str]) -> dict[str, str]:
    """Validate catalog spelling without reading module files from a root."""
    if not isinstance(catalog, Mapping) or not catalog:
        raise CheckpointError("authoritative module catalog is empty")
    result: dict[str, str] = {}
    paths: set[str] = set()
    for module, relative in sorted(catalog.items()):
        if not valid_module_name(module) or not isinstance(relative, str):
            raise CheckpointError("authoritative module catalog has an invalid entry")
        _normalized_repository_path(relative)
        if relative in paths:
            raise CheckpointError(f"authoritative modules cannot share a source: {relative}")
        paths.add(relative)
        result[module] = relative
    return result


def shared_files(root: Path) -> list[Path]:
    required = [
        Path(__file__).resolve()
        if relative == "tools/run_sh07_authoritative_modules.py"
        else root / relative
        for relative in SHARED_REPOSITORY_FILES
    ]
    for directory in [root / relative for relative in SHARED_REPOSITORY_TREES]:
        if not directory.is_dir():
            raise CheckpointError(f"required fingerprint directory is absent: {directory}")
        required.extend(path for path in directory.rglob("*") if path.is_file())
    missing = [str(path) for path in required if not path.is_file()]
    if missing:
        raise CheckpointError(f"required fingerprint files are absent: {missing}")
    return sorted(set(path.absolute() for path in required), key=str)


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


def classpath_directory_manifest(
    root: Path, directory: Path
) -> tuple[list[dict[str, object]], list[str]]:
    """Fingerprint only load-affecting resources and reject unsafe shadows."""
    entries: list[dict[str, object]] = []
    errors: list[str] = []
    try:
        resolved_directory = directory.resolve(strict=True)
    except OSError:
        return entries, [f"classpath directory is absent: {directory}"]

    def walk_error(error: OSError) -> None:
        errors.append(f"classpath directory cannot be traversed: {error}")

    for current, directory_names, file_names in os.walk(
        resolved_directory, topdown=True, onerror=walk_error, followlinks=False
    ):
        current_path = Path(current)
        try:
            current_metadata = os.lstat(current_path)
            current_resolved = current_path.resolve(strict=True)
        except OSError:
            errors.append(f"classpath directory disappeared: {current_path}")
            directory_names[:] = []
            continue
        if (
            stat.S_ISLNK(current_metadata.st_mode)
            or not stat.S_ISDIR(current_metadata.st_mode)
            or not current_resolved.is_relative_to(root)
            or not current_resolved.is_relative_to(resolved_directory)
        ):
            errors.append(f"classpath directory traversal escaped: {current_path}")
            directory_names[:] = []
            continue
        for name in sorted(directory_names):
            child = current_path / name
            try:
                metadata = os.lstat(child)
                resolved = child.resolve(strict=True)
            except OSError:
                errors.append(f"classpath directory entry is absent: {child}")
                continue
            if stat.S_ISLNK(metadata.st_mode):
                errors.append(f"classpath directory entry is a symlink: {child}")
            elif not stat.S_ISDIR(metadata.st_mode):
                errors.append(f"unsupported classpath directory entry: {child}")
            elif not resolved.is_relative_to(root) or not resolved.is_relative_to(
                resolved_directory
            ):
                errors.append(f"classpath directory entry escapes its root: {child}")
        for name in sorted(file_names):
            child = current_path / name
            try:
                metadata = os.lstat(child)
                resolved = child.resolve(strict=True)
            except OSError:
                errors.append(f"classpath file is absent: {child}")
                continue
            if stat.S_ISLNK(metadata.st_mode):
                errors.append(f"classpath file is a symlink: {child}")
            elif not stat.S_ISREG(metadata.st_mode):
                errors.append(f"unsupported classpath file type: {child}")
            elif not resolved.is_relative_to(root) or not resolved.is_relative_to(
                resolved_directory
            ):
                errors.append(f"classpath file escapes its directory: {child}")
            elif child.suffix == ".class":
                errors.append(f"AOT classpath shadow is forbidden: {child}")
            elif (
                current_resolved == resolved_directory
                and child.name in {"data_readers.clj", "data_readers.cljc"}
            ):
                entries.append(
                    {
                        "path": resolved.relative_to(resolved_directory).as_posix(),
                        "size": resolved.stat().st_size,
                        "sha256": sha256_file(resolved),
                    }
                )
    return sorted(entries, key=lambda entry: str(entry["path"])), errors


def runtime_identity(
    root: Path, base_command: Sequence[str], required: bool
) -> dict[str, object]:
    root = root.resolve()
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
    classpath_capture = command_capture([launcher, "-Spath"], cwd=root)
    classpath_entries: list[dict[str, object]] = []
    classpath_errors: list[str] = []
    if classpath_capture.get("complete"):
        raw_classpath = str(classpath_capture.get("stdout", "")).strip()
        if not raw_classpath:
            classpath_errors.append("Clojure classpath is empty")
        for raw_entry in raw_classpath.split(os.pathsep) if raw_classpath else []:
            candidate = Path(raw_entry).expanduser()
            path = candidate if candidate.is_absolute() else root / candidate
            try:
                metadata = os.lstat(path)
                resolved = path.resolve(strict=True)
            except OSError:
                classpath_errors.append(f"classpath entry is absent: {path}")
                continue
            if stat.S_ISLNK(metadata.st_mode):
                classpath_errors.append(f"classpath entry is a symlink: {path}")
            elif stat.S_ISREG(metadata.st_mode):
                classpath_entries.append(
                    {
                        "path": str(resolved),
                        "kind": "file",
                        "size": resolved.stat().st_size,
                        "sha256": sha256_file(resolved),
                    }
                )
            elif stat.S_ISDIR(metadata.st_mode) and resolved.is_relative_to(root):
                files, errors = classpath_directory_manifest(root, resolved)
                classpath_errors.extend(errors)
                classpath_entries.append(
                    {
                        "path": str(resolved),
                        "kind": "root-contained-directory",
                        "files": files,
                    }
                )
            elif stat.S_ISDIR(metadata.st_mode):
                classpath_errors.append(
                    f"external classpath directory is not fingerprintable: {resolved}"
                )
            else:
                classpath_errors.append(f"unsupported classpath entry type: {path}")
    elif not classpath_capture.get("error"):
        classpath_errors.append("Clojure classpath command failed")
    identity = {
        "required": required,
        "operating_system": platform.system(),
        "operating_system_release": platform.release(),
        "architecture": platform.machine(),
        "java_path": str(java_path),
        "java_sha256": sha256_file(java_path) if java_path.is_file() else None,
        "java_version": command_capture([str(java_path), "-version"], cwd=root),
        "clojure_sdescribe": sdescribe,
        "clojure_classpath": classpath_capture,
        "clojure_classpath_entries": sorted(
            classpath_entries, key=lambda entry: str(entry["path"])
        ),
        "clojure_classpath_errors": classpath_errors,
        "clojure_config_files": config_files,
    }
    complete = bool(
        java_path.is_file()
        and identity["java_version"]["complete"]
        and identity["clojure_sdescribe"]["complete"]
        and identity["clojure_classpath"]["complete"]
        and not classpath_errors
        and config_match is not None
        and all(entry["sha256"] for entry in config_files)
    )
    identity["complete"] = complete
    if required and not complete:
        raise CheckpointError("Java/Clojure runtime identity is incomplete")
    return identity


def file_entries(root: Path, paths: Sequence[Path]) -> list[dict[str, object]]:
    entries: list[dict[str, object]] = []
    for path in paths:
        if path.is_symlink() or not path.is_file():
            raise CheckpointError(f"fingerprint input must be a regular non-symlink file: {path}")
        try:
            relative = path.relative_to(root).as_posix()
        except ValueError:
            relative = f"external:{path}"
        entries.append(
            {
                "path": relative,
                "size": path.stat().st_size,
                "sha256": sha256_file(path),
            }
        )
    return sorted(entries, key=lambda entry: str(entry["path"]))


def fingerprint(value: object) -> str:
    encoded = json.dumps(value, sort_keys=True, separators=(",", ":")).encode()
    return f"sha256:{hashlib.sha256(encoded).hexdigest()}"


def trusted_shared_file_sha256(
    shared_context: Mapping[str, object], relative: str
) -> str:
    files = shared_context.get("files")
    matches = (
        [entry for entry in files if isinstance(entry, dict) and entry.get("path") == relative]
        if isinstance(files, list)
        else []
    )
    if len(matches) != 1:
        raise CheckpointError(f"trusted shared input is absent or duplicated: {relative}")
    value = matches[0].get("sha256")
    if not isinstance(value, str) or re.fullmatch(r"sha256:[0-9a-f]{64}", value) is None:
        raise CheckpointError(f"trusted shared input hash is malformed: {relative}")
    return value


def shared_context_fingerprint(
    root: Path,
    base_command: Sequence[str],
    *,
    module_catalog: Mapping[str, str],
    require_runtime_identity: bool,
) -> dict[str, object]:
    root = root.resolve()
    catalog = validated_module_catalog(root, module_catalog)
    command = [str(value) for value in base_command]
    executable = Path(shutil.which(command[0]) or command[0]).expanduser()
    executable_hash = sha256_file(executable.resolve()) if executable.is_file() else None
    context = {
        "tool_version": TOOL_VERSION,
        "fingerprint_policy_version": FINGERPRINT_POLICY_VERSION,
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
        "authoritative_module_catalog": catalog,
        "files": file_entries(root, shared_files(root)),
    }
    context["sha256"] = fingerprint(context)
    return context


def validated_module_catalog(
    root: Path, catalog: Mapping[str, str]
) -> dict[str, str]:
    root = root.resolve()
    catalog = validated_module_catalog_paths(catalog)
    validated: dict[str, str] = {}
    resolved_paths: set[Path] = set()
    for module, relative in sorted(catalog.items()):
        relative_path = Path(relative)
        path = root / relative_path
        try:
            metadata = os.lstat(path)
            resolved = path.resolve(strict=True)
        except OSError as error:
            raise CheckpointError(f"module source is absent: {relative}") from error
        if (
            stat.S_ISLNK(metadata.st_mode)
            or not stat.S_ISREG(metadata.st_mode)
            or not resolved.is_relative_to(root)
        ):
            raise CheckpointError(
                f"module source must be a root-contained regular non-symlink file: {relative}"
            )
        if resolved in resolved_paths:
            raise CheckpointError(f"authoritative modules cannot share a source: {relative}")
        resolved_paths.add(resolved)
        validated[module] = relative
    return validated


def validated_source_contracts(
    root: Path,
    catalog: Mapping[str, str],
    contracts: SourceContracts,
) -> dict[str, dict[str, object]]:
    if not isinstance(contracts, Mapping):
        raise CheckpointError("authoritative source contracts are malformed")
    validated_catalog = validated_module_catalog(root, catalog)
    result: dict[str, dict[str, object]] = {}
    for module, value in sorted(contracts.items()):
        if (
            module not in validated_catalog
            or not isinstance(value, Mapping)
            or set(value)
            != {"source_path", "source_byte_count", "source_bytes_sha256"}
        ):
            raise CheckpointError("authoritative source contract has an invalid entry")
        source_path = value.get("source_path")
        source_byte_count = value.get("source_byte_count")
        source_sha = value.get("source_bytes_sha256")
        if (
            source_path != validated_catalog[module]
            or not isinstance(source_byte_count, int)
            or isinstance(source_byte_count, bool)
            or source_byte_count < 0
            or not isinstance(source_sha, str)
            or SOURCE_SHA_PATTERN.fullmatch(source_sha) is None
        ):
            raise CheckpointError("authoritative source contract has an invalid binding")
        result[module] = {
            "source_path": source_path,
            "source_byte_count": source_byte_count,
            "source_bytes_sha256": source_sha,
        }
    return result


def enforce_source_contract(
    root: Path, module: str, contract: Mapping[str, object]
) -> None:
    source = root / str(contract["source_path"])
    if not hasattr(os, "O_NOFOLLOW"):
        raise CheckpointError("platform cannot inspect module source without following symlinks")
    try:
        resolved = source.resolve(strict=True)
        descriptor = os.open(source, os.O_RDONLY | os.O_NOFOLLOW)
    except OSError as error:
        raise CheckpointError(
            f"authoritative source contract cannot read module {module}: {error}"
        ) from error
    try:
        metadata = os.fstat(descriptor)
        if not stat.S_ISREG(metadata.st_mode) or not resolved.is_relative_to(root):
            raise CheckpointError(
                f"authoritative source contract path is unsafe for module {module}"
            )
        digest = hashlib.sha256()
        while block := os.read(descriptor, 1024 * 1024):
            digest.update(block)
        actual_size = metadata.st_size
        actual_sha = f"sha256:{digest.hexdigest()}"
    finally:
        os.close(descriptor)
    if (
        actual_size != contract["source_byte_count"]
        or actual_sha != contract["source_bytes_sha256"]
    ):
        raise CheckpointError(
            f"authoritative source contract mismatch for module {module}: "
            f"expected {contract['source_byte_count']} bytes/"
            f"{contract['source_bytes_sha256']}, got {actual_size} bytes/{actual_sha}"
        )


def module_context_fingerprint(
    root: Path, module: str, relative: str, shared_sha256: str
) -> dict[str, object]:
    root = root.resolve()
    path = root / relative
    try:
        metadata = os.lstat(path)
        resolved = path.resolve(strict=True)
    except OSError as error:
        raise CheckpointError(f"module source is absent: {relative}") from error
    if (
        stat.S_ISLNK(metadata.st_mode)
        or not stat.S_ISREG(metadata.st_mode)
        or not resolved.is_relative_to(root)
    ):
        raise CheckpointError(
            f"module source must be a root-contained regular non-symlink file: {relative}"
        )
    context = {
        "fingerprint_policy_version": FINGERPRINT_POLICY_VERSION,
        "module": module,
        "shared_context_sha256": shared_sha256,
        "files": file_entries(root, [path]),
    }
    context["sha256"] = fingerprint(context)
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
(defn canonical-value [value]
  (cond
    (map? value)
    (let [decorated
          (mapv (fn [[key item]]
                  (let [entry [(canonical-value key) (canonical-value item)]]
                    [(pr-str entry) entry]))
                value)]
      [:map (->> decorated (sort-by first) (mapv second))])
    (set? value) [:set (->> value (map canonical-value) (sort-by pr-str) vec)]
    (vector? value) [:vector (mapv canonical-value value)]
    (seq? value) [:list (mapv canonical-value value)]
    :else value))
(defn canonical-hash [value]
  (let [digest (java.security.MessageDigest/getInstance "SHA-256")
        canonical
        (binding [*print-length* nil *print-level* nil *print-meta* true]
          (pr-str (canonical-value value)))]
    (.update digest (.getBytes canonical "UTF-8"))
    (str "sha256:"
         (apply str (map #(format "%02x" (bit-and % 0xff))
                         (.digest digest))))))
(defn bytes-sha256 [bytes]
  (let [digest (java.security.MessageDigest/getInstance "SHA-256")]
    (.update digest bytes)
    (str "sha256:"
         (apply str (map #(format "%02x" (bit-and % 0xff))
                         (.digest digest))))))
(defn utf8-text [bytes]
  (let [decoder (-> (.newDecoder java.nio.charset.StandardCharsets/UTF_8)
                    (.onMalformedInput java.nio.charset.CodingErrorAction/REPORT)
                    (.onUnmappableCharacter java.nio.charset.CodingErrorAction/REPORT))]
    (str (.decode decoder (java.nio.ByteBuffer/wrap bytes)))))
(defn read-one-edn-bytes [bytes]
  (with-open [reader
              (java.io.PushbackReader.
               (java.io.StringReader. (utf8-text bytes)))]
    (let [value (edn/read {:eof ::eof} reader)
          trailing (edn/read {:eof ::eof} reader)]
      (when (or (= ::eof value) (not= ::eof trailing))
        (throw (ex-info "Expected exactly one proof contract EDN value"
                        {})))
      value)))
(let [path (System/getenv "GRAVITY_SH07_OUTPUT_PATH")
      expected-module (System/getenv "GRAVITY_SH07_EXPECTED_MODULE")
      expected-source-path (System/getenv "GRAVITY_SH07_EXPECTED_SOURCE_PATH")
      expected-source-byte-count
      (Long/parseLong (System/getenv "GRAVITY_SH07_EXPECTED_SOURCE_BYTE_COUNT"))
      expected-source-bytes-sha256
      (System/getenv "GRAVITY_SH07_EXPECTED_SOURCE_BYTES_SHA256")
      expected-proof-contract-sha256
      (System/getenv "GRAVITY_SH07_EXPECTED_PROOF_CONTRACT_SHA256")
      expected-output-sha256
      (some-> (System/getenv "GRAVITY_SH07_EXPECTED_OUTPUT_SHA256") not-empty)
      expected-authority-scope
      (some-> (System/getenv "GRAVITY_SH07_EXPECTED_AUTHORITY_SCOPE")
              not-empty keyword)
      expected-coverage-policy
      (some-> (System/getenv "GRAVITY_SH07_EXPECTED_COVERAGE_POLICY")
              not-empty keyword)
      output-file (io/file path)
      output-size (java.nio.file.Files/size (.toPath output-file))
      output-bytes
      (when (<= output-size 4194304)
        ;; Read the child output exactly once.  The expected hash is supplied
        ;; by the held descriptor snapshot in the Python authority wrapper;
        ;; a pathname swap therefore fails closed below.
        (java.nio.file.Files/readAllBytes (.toPath output-file)))
      output-sha256 (when output-bytes (bytes-sha256 output-bytes))
      output-value (when output-bytes (read-one-edn-bytes output-bytes))
      contract-bytes
      (java.nio.file.Files/readAllBytes
       (.toPath
        (io/file
         "bootstrap/clojure/test/gravity/self_hosting/sh07_proof_contract.edn")))
      contract-sha256 (bytes-sha256 contract-bytes)
      contract
      (when (= expected-proof-contract-sha256 contract-sha256)
        (read-one-edn-bytes contract-bytes))
      legacy-exact? (= :test (:schema contract))
      expected-output-schema (if legacy-exact? 2 3)
      boundary (:boundary contract)
      census-contract (:authoritative-coverage-census contract)
      module-expectation
      (get-in census-contract [:module-expectations (keyword expected-module)])
      passed?
        (let [value output-value
              modules (:modules value)
              result (when (= 1 (count modules)) (first modules))
              checks (:contract-checks result)
              census (:coverage-census result)
              request-counts (:request-counts census)
              core-counts (:core-counts census)
              frequencies (:core-form-frequencies core-counts)
              integrity (:integrity census)
              source-binding (:source-binding census)
              nonnegative-counts?
              (fn [counts]
                (and (map? counts)
                     (every? (fn [[_ count]]
                               (and (integer? count) (not (neg? count))))
                             counts)))]
          (and output-bytes
               (<= (alength output-bytes) 4194304)
               (or (nil? expected-output-sha256)
                   (= expected-output-sha256 output-sha256))
               (or (nil? expected-authority-scope)
                   (= expected-authority-scope (:authority-scope census)))
               (or (nil? expected-coverage-policy)
                   (= expected-coverage-policy (:coverage-census-policy census)))
               (= expected-proof-contract-sha256 contract-sha256)
               (map? contract)
               (or legacy-exact?
                   (contains? #{:exact-precommitted :source-bound-derived}
                              (:coverage-census-policy contract)))
               (= :gravity/sh07-authoritative-proof-run (:artifact value))
               (= expected-output-schema (:schema-version value))
               (= :passed (:status value))
               (true? (:fresh-process-required? value))
               (false? (:persistent-iteration-cache-used? value))
               (if legacy-exact?
                 true
                 (if (= :source-bound-derived (:coverage-census-policy contract))
                   (and (= :source-bound-derived
                           (:coverage-census-policy value))
                        (= :individual-source-bound-derived
                           (:authority-scope value))
                        (false? (:counts-precommitted? value))
                        (false? (:independent-count-oracle? value))
                        (false? (:aggregate-authoritative? value))
                        (= [:exact-authentic-coverage :aggregate :release]
                           (:unsupported-claims value))
                        (true? (:attestation-required? value)))
                   (and (= :exact-precommitted
                           (:coverage-census-policy value))
                        (= :individual-existing-runner-output-only
                           (:authority-scope value))
                        (true? (:counts-precommitted? value))
                        (true? (:independent-count-oracle? value))
                        (false? (:aggregate-authoritative? value))
                        (= [] (:unsupported-claims value))
                        (or (not (contains? value :attestation-required?))
                            (false? (:attestation-required? value))))))
               (vector? modules)
               (= expected-module (:module result))
               (= expected-source-path (:source-path result))
               (= expected-source-byte-count (:source-byte-count result))
               (= expected-source-bytes-sha256 (:source-bytes-sha256 result))
               (= :accepted (:status result))
               (= :passed (:verification-status result))
               (= :complete (:capability-proof-status result))
               (empty? (:failed-checks result))
               (= :gravity/sh07-authoritative-coverage-census
                  (:artifact census))
               (= (if legacy-exact?
                    #{:artifact :schema-version :authority-scope
                      :aggregate-authoritative? :module :module-namespace
                      :source-revision-id :sh07-artifact-id :sh06-status :task
                      :request-schema-version :scope :source-binding
                      :request-counts :core-counts :integrity :census-hash}
                    #{:artifact :schema-version :authority-scope
                    :aggregate-authoritative? :module :module-namespace
                    :source-revision-id :sh07-artifact-id :sh06-status :task
                    :request-schema-version :scope :source-binding
                    :request-counts :core-counts :integrity :census-hash
                    :coverage-census-policy :counts-precommitted?
                    :independent-count-oracle? :unsupported-claims})
                  (set (keys census)))
               (= (if legacy-exact? 1 2) (:schema-version census))
               (= (:schema-version census-contract) (:schema-version census))
               (if legacy-exact?
                 (= :individual-existing-runner-output-only
                    (:authority-scope census))
                 (and (= (:coverage-census-policy contract)
                         (:coverage-census-policy census))
                      (= (if (= :source-bound-derived
                                 (:coverage-census-policy contract))
                           :individual-source-bound-derived
                           :individual-existing-runner-output-only)
                         (:authority-scope census))))
               (false? (:aggregate-authoritative? census))
               (if legacy-exact?
                 true
                 (if (= :source-bound-derived (:coverage-census-policy contract))
                   (and (= false (:counts-precommitted? census))
                        (= false (:independent-count-oracle? census))
                        (= [:exact-authentic-coverage :aggregate :release]
                           (:unsupported-claims census)))
                   (and (= true (:counts-precommitted? census))
                        (= true (:independent-count-oracle? census))
                        (= [] (:unsupported-claims census)))))
               (= expected-module (:module census))
               (symbol? (:module-namespace census))
               (= (:task boundary) (:task census))
               (= (:request-schema-version boundary)
                  (:request-schema-version census))
               (= (:scope boundary) (:scope census))
               (or (nil? module-expectation)
                   (and (= (:module-namespace module-expectation)
                           (:module-namespace census))
                        (= (:source-binding module-expectation)
                           source-binding)
                        (if (or legacy-exact?
                                (= :exact-precommitted
                                   (:coverage-census-policy contract)))
                          (and (= (:request-counts module-expectation)
                                  request-counts)
                               (= (:core-counts module-expectation)
                                  core-counts))
                          (and (not (contains? module-expectation
                                                :request-counts))
                               (not (contains? module-expectation
                                                :core-counts))))))
               (= expected-source-bytes-sha256
                  (:source-revision-id result)
                  (:source-revision-id census))
               (string? (:artifact-id result))
               (= (:artifact-id result) (:sh07-artifact-id census))
               (= :accepted (:sh06-status census))
               (string? (:task census))
               (pos-int? (:request-schema-version census))
               (keyword? (:scope census))
               (= #{:source-byte-count :source-bytes-sha256}
                  (set (keys source-binding)))
               (= expected-source-byte-count
                  (:source-byte-count source-binding))
               (= expected-source-bytes-sha256
                  (:source-bytes-sha256 source-binding))
               (= #{:fragment-count :root-form-count :form-count
                    :binding-count :local-binding-count :resolution-count}
                  (set (keys request-counts)))
               (nonnegative-counts? request-counts)
               (= #{:core-node-count :definition-count :call-count
                    :reference-count :keyword-lookup-count
                    :core-form-frequencies}
                  (set (keys core-counts)))
               (nonnegative-counts?
                (dissoc core-counts :core-form-frequencies))
               (map? frequencies)
               (every? (fn [[form count]]
                         (and (keyword? form) (pos-int? count)))
                       frequencies)
               (= #{:root-form-id-order-exact? :form-id-order-exact?
                    :source-snapshot-stable? :source-revision-bound-to-bytes?
                    :target-source-reread-disabled?}
                  (set (keys integrity)))
               (every? true? (vals integrity))
               (= (:census-hash census)
                  (canonical-hash
                   {:domain (if legacy-exact?
                              :gravity/sh07-authoritative-coverage-census-v1
                              :gravity/sh07-authoritative-coverage-census-v2)
                    :census (dissoc census :census-hash)}))
               (map? checks)
               (seq checks)
               (true? (:authoritative-coverage-census-current? checks))
               (every? true? (vals checks)))))]
  (when-not passed? (System/exit 1)))
"""


def output_contract_passed(
    module: str,
    expected_source_path: str,
    expected_source_byte_count: int,
    expected_source_bytes_sha256: str,
    expected_proof_contract_sha256: str,
    stdout_path: Path,
    *,
    clojure_command: str,
    cwd: Path,
    expected_stdout_sha256: str | None = None,
    expected_authority_scope: str | None = None,
    expected_coverage_policy: str | None = None,
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
                "GRAVITY_SH07_EXPECTED_SOURCE_PATH": expected_source_path,
                "GRAVITY_SH07_EXPECTED_SOURCE_BYTE_COUNT": str(
                    expected_source_byte_count
                ),
                "GRAVITY_SH07_EXPECTED_SOURCE_BYTES_SHA256": (
                    expected_source_bytes_sha256
                ),
                "GRAVITY_SH07_EXPECTED_PROOF_CONTRACT_SHA256": (
                    expected_proof_contract_sha256
                ),
                "GRAVITY_SH07_EXPECTED_OUTPUT_SHA256": (
                    expected_stdout_sha256 or ""
                ),
                "GRAVITY_SH07_EXPECTED_AUTHORITY_SCOPE": (
                    expected_authority_scope or ""
                ),
                "GRAVITY_SH07_EXPECTED_COVERAGE_POLICY": (
                    expected_coverage_policy or ""
                ),
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
    module_fingerprint: str,
    command: Sequence[str],
    state_dir: Path,
    proof_contract_sha256: str,
) -> bool:
    if not isinstance(entry, dict):
        return False
    if not (
        entry.get("state") == "passed"
        and entry.get("exit_code") == 0
        and entry.get("module_context_fingerprint") == module_fingerprint
        and entry.get("command") == list(command)
        and entry.get("proof_contract_sha256") == proof_contract_sha256
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


@dataclasses.dataclass
class SharedLockFile:
    path: Path
    descriptor: int
    parent_descriptor: int
    parent_device: int
    parent_inode: int

    def validate(self, *, allow_legacy_mode: bool = False) -> None:
        try:
            opened = os.fstat(self.descriptor)
            named = os.stat(
                self.path.name,
                dir_fd=self.parent_descriptor,
                follow_symlinks=False,
            )
            parent = os.fstat(self.parent_descriptor)
        except OSError as error:
            raise CheckpointError(
                f"shared heavy lock pathname changed: {self.path}"
            ) from error
        if (
            not stat.S_ISREG(opened.st_mode)
            or not stat.S_ISREG(named.st_mode)
            or opened.st_uid != os.geteuid()
            or named.st_uid != os.geteuid()
            or opened.st_nlink != 1
            or named.st_nlink != 1
            or stat.S_IMODE(opened.st_mode)
            not in ({SHARED_LOCK_MODE, 0o644} if allow_legacy_mode else {SHARED_LOCK_MODE})
            or stat.S_IMODE(named.st_mode)
            not in ({SHARED_LOCK_MODE, 0o644} if allow_legacy_mode else {SHARED_LOCK_MODE})
            or (opened.st_dev, opened.st_ino) != (named.st_dev, named.st_ino)
            or (parent.st_dev, parent.st_ino)
            != (self.parent_device, self.parent_inode)
        ):
            raise CheckpointError(
                "shared heavy lock must be one stable owned 0600 regular file"
            )

    def close(self) -> None:
        os.close(self.descriptor)
        os.close(self.parent_descriptor)

    def migrate_legacy_mode_after_exclusive_lock(self) -> bool:
        """Promote an owned legacy 0644 inode only while this fd holds flock."""
        self.validate(allow_legacy_mode=True)
        metadata = os.fstat(self.descriptor)
        if stat.S_IMODE(metadata.st_mode) == SHARED_LOCK_MODE:
            return False
        os.fchmod(self.descriptor, SHARED_LOCK_MODE)
        self.validate()
        return True


def canonical_shared_lock_path(lock_path: Path) -> Path:
    """Return a direct child of verified canonical /private/tmp."""
    absolute = Path(os.path.abspath(lock_path.expanduser()))
    if absolute.parent == Path("/tmp") and Path("/tmp").is_symlink():
        if Path("/tmp").resolve() != Path("/private/tmp"):
            raise CheckpointError("system /tmp alias does not resolve exactly to /private/tmp")
        absolute = Path("/private/tmp") / absolute.name
    if absolute.parent != Path("/private/tmp") or absolute.name in {"", ".", ".."}:
        raise CheckpointError("shared heavy lock must be a direct child of /private/tmp")
    return absolute


def open_lock_file(lock_path: Path, *, create: bool = True) -> SharedLockFile:
    if not hasattr(os, "O_NOFOLLOW"):
        raise CheckpointError("platform cannot open the shared lock without following symlinks")
    absolute = canonical_shared_lock_path(lock_path)
    parent_descriptor = -1
    descriptor = -1
    try:
        parent_descriptor = os.open(
            absolute.parent,
            os.O_RDONLY | getattr(os, "O_DIRECTORY", 0) | os.O_NOFOLLOW,
        )
        parent = os.fstat(parent_descriptor)
        if not stat.S_ISDIR(parent.st_mode):
            raise CheckpointError("shared heavy lock parent must be a directory")
        flags = os.O_RDWR | os.O_NOFOLLOW | getattr(os, "O_CLOEXEC", 0)
        if create:
            flags |= os.O_CREAT
        descriptor = os.open(
            absolute.name,
            flags,
            SHARED_LOCK_MODE,
            dir_fd=parent_descriptor,
        )
    except OSError as error:
        if parent_descriptor != -1:
            os.close(parent_descriptor)
        raise CheckpointError(f"shared heavy lock cannot be opened safely: {absolute}") from error
    try:
        handle = SharedLockFile(
            path=absolute,
            descriptor=descriptor,
            parent_descriptor=parent_descriptor,
            parent_device=parent.st_dev,
            parent_inode=parent.st_ino,
        )
        # Legacy owned 0644 locks may be opened only far enough to contend on
        # the same inode.  The exclusive acquirer promotes that inode to 0600.
        handle.validate(allow_legacy_mode=True)
        return handle
    except BaseException:
        os.close(descriptor)
        os.close(parent_descriptor)
        raise


@contextlib.contextmanager
def shared_lock_lease(lock_path: Path):
    try:
        handle = open_lock_file(lock_path)
    except CheckpointError as error:
        raise SharedLockValidationError(str(error)) from error
    try:
        try:
            fcntl.flock(handle.descriptor, fcntl.LOCK_EX | fcntl.LOCK_NB)
        except BlockingIOError as error:
            raise SharedLockUnavailable(
                f"shared heavy lock is unavailable: {handle.path}"
            ) from error
        try:
            migrated = handle.migrate_legacy_mode_after_exclusive_lock()
        except CheckpointError as error:
            raise SharedLockValidationError(str(error)) from error
        lease_receipt = {
            "lock_path": str(handle.path),
            "lock_mode": "0600",
            "lock_mode_migrated": migrated,
            "lock_acquired": True,
            "lock_validated": False,
            "lock_released": False,
        }
        try:
            try:
                yield handle, lease_receipt
            except BaseException as body_error:
                try:
                    handle.validate()
                except CheckpointError as validation_error:
                    raise SharedLockValidationError(str(validation_error)) from body_error
                raise
            else:
                try:
                    handle.validate()
                except CheckpointError as error:
                    raise SharedLockValidationError(str(error)) from error
                lease_receipt["lock_validated"] = True
        finally:
            try:
                fcntl.flock(handle.descriptor, fcntl.LOCK_UN)
            except BaseException:
                raise
            else:
                lease_receipt["lock_released"] = True
    finally:
        handle.close()


def run_modules(
    *,
    root: Path,
    state_dir: Path,
    modules: Sequence[str],
    module_catalog: Mapping[str, str],
    base_command: Sequence[str] | None = None,
    timeout_seconds: float = 21600,
    resume: bool = True,
    launcher: Launcher = default_launcher,
    output_validator: OutputValidator | None = None,
    catalog_provider: CatalogProvider | None = None,
    source_contracts: SourceContracts | None = None,
    source_contract_proof_sha256: str | None = None,
    lock_path: Path | None = DEFAULT_LOCK,
    _lock_receipt: Mapping[str, object] | None = None,
) -> tuple[int, dict[str, object]]:
    if lock_path is not None:
        child_result: tuple[int, dict[str, object]] | None = None
        lease_receipt: Mapping[str, object] | None = None
        try:
            with shared_lock_lease(lock_path) as (_handle, receipt):
                lease_receipt = receipt
                child_result = run_modules(
                    root=root, state_dir=state_dir, modules=modules,
                    module_catalog=module_catalog, base_command=base_command,
                    timeout_seconds=timeout_seconds, resume=resume, launcher=launcher,
                    output_validator=output_validator, catalog_provider=catalog_provider,
                    source_contracts=source_contracts,
                    source_contract_proof_sha256=source_contract_proof_sha256,
                    lock_path=None, _lock_receipt=receipt,
                )
                _handle.validate()
        except SharedLockUnavailable:
            raise
        except SharedLockValidationError as error:
            manifest_path = Path(os.path.abspath(state_dir.expanduser())) / "manifest.json"
            existing = load_manifest(manifest_path) if manifest_path.exists() else None
            if existing is None:
                raise
            existing.update(
                state="lock-unsafe", aggregate_authoritative=False,
                authority_scope="none", resumable=False,
                lock_validation_error=str(error), finished_at=utc_now(), updated_at=utc_now(),
            )
            if isinstance(lease_receipt, Mapping):
                existing.update({
                    key: lease_receipt.get(key)
                    for key in ("lock_path", "lock_mode", "lock_mode_migrated",
                                "lock_acquired", "lock_validated", "lock_released")
                })
            atomic_json_write(manifest_path, existing)
            return 75, existing
        if child_result is None:
            raise CheckpointError("authoritative child did not return a checkpoint result")
        result_code, result_manifest = child_result
        # The inner runner writes its completed manifest while the lease is
        # still held.  Amend that same manifest only after the context exits,
        # so the lifecycle receipt cannot claim release before the flock is
        # actually unlocked.  The held outer lease receipt is immutable to
        # callers except for these final booleans.
        if isinstance(lease_receipt, Mapping):
            manifest_path = Path(os.path.abspath(state_dir.expanduser())) / "manifest.json"
            amended = dict(result_manifest)
            amended.update({
                key: lease_receipt.get(key)
                for key in ("lock_path", "lock_mode", "lock_mode_migrated",
                            "lock_acquired", "lock_validated", "lock_released")
            })
            atomic_json_write(manifest_path, amended)
            result_manifest = amended
        return result_code, result_manifest
    lock_receipt = dict(_lock_receipt or {})
    if (
        not modules
        or len(set(modules)) != len(modules)
        or not all(valid_module_name(module) for module in modules)
    ):
        raise CheckpointError("selected modules must be nonempty, unique, safe slugs")
    if timeout_seconds <= 0:
        raise CheckpointError("timeout must be positive")
    root = root.resolve()
    catalog = validated_module_catalog(root, module_catalog)
    unknown = sorted(set(modules) - set(catalog))
    if unknown:
        raise CheckpointError(f"selected modules are absent from the catalog: {unknown}")
    if source_contracts is None:
        if launcher is default_launcher:
            source_contract_proof_sha256, source_contracts = discover_source_contracts(
                root, base_command or default_base_command(), 120,
                module_catalog=catalog,
            )
        else:
            source_contracts = {}
    contracts = validated_source_contracts(root, catalog, source_contracts)
    if contracts and source_contract_proof_sha256 is None:
        raise CheckpointError(
            "authoritative source contracts lack a trusted proof contract hash"
        )
    try:
        coverage_policy = source_bound_policy(root)
    except CheckpointError:
        # Tiny unit fixtures predate the policy fields; production contracts
        # may not silently omit or mismatch them.
        contract_text = (root / PROOF_CONTRACT_RELATIVE).read_text(
            encoding="utf-8"
        )
        if ":schema :test" in contract_text:
            coverage_policy = "exact-precommitted"
        else:
            raise
    if coverage_policy == SOURCE_BOUND_POLICY:
        missing_expectations = sorted(set(modules) - set(contracts))
        if missing_expectations:
            raise CheckpointError(
                "source-bound-derived authority requires source contracts for "
                f"selected modules: {missing_expectations}"
            )
    state_dir = Path(os.path.abspath(state_dir.expanduser()))
    ensure_owned_directory(state_dir)
    modules_dir = state_dir / "modules"
    ensure_owned_directory(modules_dir)
    manifest_path = state_dir / "manifest.json"
    base = list(base_command or default_base_command())
    if catalog_provider is not None:
        provider = catalog_provider
    elif launcher is default_launcher:
        def provider() -> Mapping[str, str]:
            return discover_module_catalog(
                root, base, min(timeout_seconds, 120)
            )
    else:
        def provider() -> Mapping[str, str]:
            return dict(catalog)
    shared_context = shared_context_fingerprint(
        root,
        base,
        module_catalog=catalog,
        require_runtime_identity=launcher is default_launcher,
    )
    shared_fingerprint = str(shared_context["sha256"])
    trusted_proof_contract_sha256 = trusted_shared_file_sha256(
        shared_context, PROOF_CONTRACT_RELATIVE
    )
    if (
        source_contract_proof_sha256 is not None
        and source_contract_proof_sha256 != trusted_proof_contract_sha256
    ):
        raise CheckpointError(
            "authoritative source contracts do not match the trusted proof contract"
        )
    validator = output_validator or (
        lambda module, source_path, source_size, source_sha, contract_sha, output: output_contract_passed(
            module,
            source_path,
            source_size,
            source_sha,
            contract_sha,
            output,
            clojure_command=base[0],
            cwd=root,
        )
    )
    try:
        confirmed_catalog = validated_module_catalog(root, provider())
        confirmed_context = shared_context_fingerprint(
            root,
            base,
            module_catalog=confirmed_catalog,
            require_runtime_identity=launcher is default_launcher,
        )
    except (OSError, subprocess.TimeoutExpired) as error:
        raise CheckpointError(f"startup catalog confirmation failed: {error}") from error
    if confirmed_context["sha256"] != shared_fingerprint:
        raise CheckpointError("startup catalog/shared context was not stable")

    try:
        for module in modules:
            if module in contracts:
                enforce_source_contract(root, module, contracts[module])
    except CheckpointError as error:
        manifest: dict[str, object] = {
            "schema": SCHEMA,
            "tool_version": TOOL_VERSION,
            "fingerprint_policy_version": FINGERPRINT_POLICY_VERSION,
            "state": "source-contract-mismatch",
            "shared_context_fingerprint": shared_fingerprint,
            "shared_context": shared_context,
            "selected_modules": list(modules),
            "source_contracts": contracts,
            "modules": {},
            "aggregate_authoritative": False,
            "authority_scope": "individual-existing-runner-outputs-only",
            "preflight_error": str(error),
            "finished_at": utc_now(),
            "updated_at": utc_now(),
            **lock_receipt,
        }
        atomic_json_write(manifest_path, manifest)
        return 75, manifest

    if True:
        previous = load_manifest(manifest_path)
        same_shared_context = bool(
            previous
            and previous.get("schema") == SCHEMA
            and previous.get("tool_version") == TOOL_VERSION
            and previous.get("shared_context_fingerprint") == shared_fingerprint
        )
        previous_modules = (
            previous.get("modules", {}) if same_shared_context and resume else {}
        )
        if not isinstance(previous_modules, dict):
            previous_modules = {}
        manifest: dict[str, object] = {
            "schema": SCHEMA,
            "tool_version": TOOL_VERSION,
            "fingerprint_policy_version": FINGERPRINT_POLICY_VERSION,
            "state": "running",
            "shared_context_fingerprint": shared_fingerprint,
            "shared_context": shared_context,
            "selected_modules": list(modules),
            "source_contracts": contracts,
            "module_contexts": {},
            "modules": dict(previous_modules),
            "resumed_modules": [],
            "aggregate_authoritative": False,
            "authority_scope": "individual-existing-runner-outputs-only",
            "started_at": utc_now(),
            "updated_at": utc_now(),
            **lock_receipt,
        }
        if previous and not same_shared_context:
            manifest["invalidated_shared_context_fingerprint"] = previous.get(
                "shared_context_fingerprint"
            )
        atomic_json_write(manifest_path, manifest)

        records = manifest["modules"]
        assert isinstance(records, dict)
        module_contexts = manifest["module_contexts"]
        assert isinstance(module_contexts, dict)
        completed_fingerprints: dict[str, str] = {}

        def current_module_context(module: str) -> dict[str, object]:
            return module_context_fingerprint(
                root, module, catalog[module], shared_fingerprint
            )

        def stability(
            *, rediscover_catalog: bool = False
        ) -> tuple[bool, str | None, list[str], str | None]:
            try:
                observed_catalog = (
                    validated_module_catalog(root, provider())
                    if rediscover_catalog
                    else catalog
                )
                observed_shared = shared_context_fingerprint(
                    root,
                    base,
                    module_catalog=observed_catalog,
                    require_runtime_identity=launcher is default_launcher,
                )
                observed_shared_sha = str(observed_shared["sha256"])
                if observed_shared_sha != shared_fingerprint:
                    return False, observed_shared_sha, [], None
                stale = [
                    completed
                    for completed, expected in completed_fingerprints.items()
                    if current_module_context(completed)["sha256"] != expected
                ]
                return not stale, observed_shared_sha, stale, None
            except (CheckpointError, OSError, subprocess.TimeoutExpired) as error:
                return False, None, [], str(error)

        def stop_for_context_change(
            module: str, observed_shared: str | None, stale: Sequence[str], error: str | None
        ) -> tuple[int, dict[str, object]]:
            manifest["state"] = "context-changed"
            manifest["stopped_at_module"] = module
            manifest["stale_modules"] = list(stale)
            manifest["shared_context_fingerprint_after"] = observed_shared
            if error:
                manifest["context_error"] = error
            manifest["finished_at"] = utc_now()
            manifest["updated_at"] = utc_now()
            atomic_json_write(manifest_path, manifest)
            return 75, manifest

        for module in modules:
            stable, observed_shared, stale, context_error = stability()
            if not stable:
                return stop_for_context_change(
                    module, observed_shared, stale, context_error
                )
            if module in contracts:
                try:
                    enforce_source_contract(root, module, contracts[module])
                except CheckpointError as error:
                    return stop_for_context_change(
                        module, observed_shared, [module], str(error)
                    )
            module_context = current_module_context(module)
            module_fingerprint = str(module_context["sha256"])
            source_entry = module_context["files"][0]
            assert isinstance(source_entry, dict)
            source_size = int(source_entry["size"])
            source_sha = str(source_entry["sha256"])
            module_contexts[module] = module_context
            command = [*base, "--fresh", module]
            prior = records.get(module)
            if (
                resume
                and same_shared_context
                and resumable(
                    module, prior, module_fingerprint, command, state_dir,
                    trusted_proof_contract_sha256
                )
                and validator(
                    module,
                    catalog[module],
                    source_size,
                    source_sha,
                    trusted_proof_contract_sha256,
                    state_dir / f"modules/{module}.stdout.log",
                )
            ):
                completed_fingerprints[module] = module_fingerprint
                resumed = manifest["resumed_modules"]
                assert isinstance(resumed, list)
                resumed.append(module)
                manifest["updated_at"] = utc_now()
                atomic_json_write(manifest_path, manifest)
                continue
            stdout_path = modules_dir / f"{module}.stdout.log"
            stderr_path = modules_dir / f"{module}.stderr.log"
            relative_stdout = stdout_path.relative_to(state_dir).as_posix()
            relative_stderr = stderr_path.relative_to(state_dir).as_posix()
            records[module] = {
                "state": "running",
                "command": command,
                "module_context_fingerprint": module_fingerprint,
                "proof_contract_sha256": trusted_proof_contract_sha256,
                "module_context": module_context,
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
            completed_fingerprints[module] = module_fingerprint
            context_stable, shared_after, stale, context_error = stability()
            checked = (
                context_stable
                and outcome.exit_code == 0
                and validator(
                    module,
                    catalog[module],
                    source_size,
                    source_sha,
                    trusted_proof_contract_sha256,
                    stdout_path,
                )
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
                "module_context_fingerprint": module_fingerprint,
                "proof_contract_sha256": trusted_proof_contract_sha256,
                "module_context": module_context,
                "context_stable": context_stable,
                "shared_context_fingerprint_after": shared_after,
                "stale_modules": list(stale),
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
            if context_error:
                records[module]["context_error"] = context_error
            manifest["updated_at"] = utc_now()
            if state != "passed":
                manifest["state"] = state
                manifest["stopped_at_module"] = module
                manifest["finished_at"] = utc_now()
                atomic_json_write(manifest_path, manifest)
                return exit_code, manifest
            atomic_json_write(manifest_path, manifest)

        stable, observed_shared, stale, context_error = stability(
            rediscover_catalog=True
        )
        if not stable:
            return stop_for_context_change(
                modules[-1], observed_shared, stale, context_error
            )
        manifest["state"] = "completed"
        manifest["finished_at"] = utc_now()
        manifest["updated_at"] = utc_now()
        atomic_json_write(manifest_path, manifest)
        return 0, manifest
def discover_module_catalog(
    root: Path, base_command: Sequence[str], timeout: float
) -> dict[str, str]:
    result = subprocess.run(
        [*base_command, "--catalog"],
        cwd=root,
        capture_output=True,
        text=True,
        timeout=timeout,
        check=False,
    )
    if result.returncode != 0:
        raise CheckpointError(f"module catalog failed: {result.stderr.strip()}")
    catalog: dict[str, str] = {}
    for line in result.stdout.splitlines():
        if not line or line.count("\t") != 1:
            raise CheckpointError("module catalog output is malformed")
        module, relative = line.split("\t")
        if module in catalog:
            raise CheckpointError("module catalog output contains duplicate modules")
        catalog[module] = relative
    return validated_module_catalog(root, catalog)


def discover_source_contracts(
    root: Path,
    base_command: Sequence[str],
    timeout: float,
    *,
    module_catalog: Mapping[str, str] | None = None,
) -> tuple[str, dict[str, dict[str, object]]]:
    result = subprocess.run(
        [*base_command, "--source-contracts"],
        cwd=root,
        capture_output=True,
        text=True,
        timeout=timeout,
        check=False,
    )
    if result.returncode != 0:
        raise CheckpointError(
            f"module source contract discovery failed: {result.stderr.strip()}"
        )
    lines = result.stdout.splitlines()
    if (
        not lines
        or lines[0].count("\t") != 1
        or not lines[0].startswith("#proof-contract-sha256\t")
    ):
        raise CheckpointError("module source contract output is malformed")
    proof_contract_sha = lines[0].split("\t", 1)[1]
    if SOURCE_SHA_PATTERN.fullmatch(proof_contract_sha) is None:
        raise CheckpointError("module source contract proof hash is malformed")
    contracts: dict[str, dict[str, object]] = {}
    policy_lines = [line for line in lines[1:]
                    if line.startswith("#coverage-census-policy\t")]
    if len(policy_lines) != 1 or policy_lines[0].split("\t", 1)[1] not in {
            "exact-precommitted", SOURCE_BOUND_POLICY}:
        raise CheckpointError("module source contract policy marker is malformed")
    for line in lines[1:]:
        if line.startswith("#coverage-census-policy\t"):
            continue
        if not line or line.count("\t") != 3:
            raise CheckpointError("module source contract output is malformed")
        module, relative, byte_count_text, source_sha = line.split("\t")
        if module in contracts or not byte_count_text.isdecimal():
            raise CheckpointError("module source contract output is malformed")
        contracts[module] = {
            "source_path": relative,
            "source_byte_count": int(byte_count_text),
            "source_bytes_sha256": source_sha,
        }
    catalog = module_catalog or discover_module_catalog(root, base_command, timeout)
    return proof_contract_sha, validated_source_contracts(root, catalog, contracts)


def discover_modules(root: Path, base_command: Sequence[str], timeout: float) -> list[str]:
    """Compatibility helper returning the strict catalog's ordered names."""
    return list(discover_module_catalog(root, base_command, timeout))


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
    value.add_argument(
        "--attest", action="store_true",
        help="write a reviewed source-bound attestation for one completed module",
    )
    value.add_argument("--attestation-output", type=Path)
    value.add_argument("--reviewer")
    value.add_argument("--reviewed-at")
    value.add_argument("--method")
    value.add_argument("--limitation", action="append", default=[])
    value.add_argument("--decision", default="accepted-with-scope")
    value.add_argument("--cwd", type=Path, default=Path(__file__).resolve().parents[1])
    return value


def _main_under_lease(
    values: argparse.Namespace,
    base: Sequence[str],
    lock_receipt: Mapping[str, object],
) -> int:
    try:
        catalog = discover_module_catalog(
            values.cwd, base, min(values.timeout_seconds, 120)
        )
        source_contract_proof_sha256, source_contracts = discover_source_contracts(
            values.cwd, base, min(values.timeout_seconds, 120),
            module_catalog=catalog,
        )
        available = list(catalog)
        if values.list:
            print("\n".join(available))
            return 0
        selected = available if values.all else values.module
        assert selected is not None
        unknown = sorted(set(selected) - set(available))
        if unknown:
            raise CheckpointError(f"unknown modules: {unknown}")
        if values.attest:
            if values.all or len(selected) != 1:
                raise CheckpointError("--attest requires exactly one --module")
            module = selected[0]
            if source_bound_policy(values.cwd) != SOURCE_BOUND_POLICY:
                raise CheckpointError(
                    "--attest is available only for source-bound-derived policy"
                )
            state_dir = values.state_dir.resolve()
            manifest = load_manifest(state_dir / "manifest.json")
            record = manifest.get("modules", {}).get(module) if manifest else None
            manifest_context_current = False
            if isinstance(manifest, Mapping):
                try:
                    current_shared = shared_context_fingerprint(
                        values.cwd, base, module_catalog=catalog,
                        require_runtime_identity=True)
                    current_module = module_context_fingerprint(
                        values.cwd, module, catalog[module],
                        str(manifest.get("shared_context_fingerprint")))
                    manifest_context_current = (
                        manifest.get("shared_context_fingerprint")
                        == current_shared.get("sha256")
                        and isinstance(record, Mapping)
                        and record.get("module_context_fingerprint")
                        == current_module.get("sha256")
                    )
                except (CheckpointError, OSError, subprocess.TimeoutExpired):
                    manifest_context_current = False
            if (not isinstance(record, Mapping)
                    or not isinstance(manifest, Mapping)
                    or manifest.get("schema") != SCHEMA
                    or manifest.get("tool_version") != TOOL_VERSION
                    or manifest.get("state") != "completed"
                    or not manifest_context_current
                    or record.get("state") != "passed"
                    or record.get("output_contract_checked") is not True
                    or record.get("proof_contract_sha256")
                    != source_contract_proof_sha256):
                raise CheckpointError(
                    f"module {module} has no completed validated receipt to attest"
                )
            relative = record.get("stdout_path")
            canonical_relative = f"modules/{module}.stdout.log"
            if relative != canonical_relative or not isinstance(relative, str):
                raise CheckpointError("completed module receipt lacks stdout path")
            stdout_path = state_dir / relative
            if (stdout_path.is_symlink()
                    or not stdout_path.is_file()
                    or stdout_path.resolve().parent != (state_dir / "modules").resolve()
                    or record.get("stdout_sha256") != sha256_file(stdout_path)):
                raise CheckpointError("completed module stdout receipt is not current")
            if module not in source_contracts:
                raise CheckpointError("completed module lacks a current source contract")
            enforce_source_contract(values.cwd, module, source_contracts[module])
            if values.reviewer is None or values.reviewed_at is None or values.method is None:
                raise CheckpointError("--attest requires --reviewer, --reviewed-at, and --method")
            attestation = create_source_bound_attestation(
                values.cwd, module, stdout_path,
                proof_contract_sha256=source_contract_proof_sha256,
                reviewer=values.reviewer,
                reviewed_at=values.reviewed_at,
                method=values.method,
                limitations=values.limitation,
                decision=values.decision,
            )
            output = values.attestation_output or (
                state_dir / "attestations" / f"{module}.json"
            )
            atomic_json_write(output, attestation)
            print(json.dumps({"attestation": str(output.resolve()),
                              "authority_scope": attestation["authority_scope"],
                              "aggregate_authoritative": False}, sort_keys=True))
            return 0
        code, manifest = run_modules(
            root=values.cwd,
            state_dir=values.state_dir,
            modules=selected,
            module_catalog=catalog,
            base_command=base,
            timeout_seconds=values.timeout_seconds,
            resume=not values.no_resume,
            catalog_provider=lambda: discover_module_catalog(
                values.cwd, base, min(values.timeout_seconds, 120)
            ),
            source_contracts=source_contracts,
            source_contract_proof_sha256=source_contract_proof_sha256,
            lock_path=None,
            _lock_receipt=lock_receipt,
        )
        print(json.dumps({
            "state": manifest["state"],
            "manifest": str((values.state_dir / "manifest.json").resolve()),
            "aggregate_authoritative": False,
        }, sort_keys=True))
        return code
    except (CheckpointError, subprocess.TimeoutExpired) as error:
        print(f"SH-07 checkpoint runner failed: {error}", file=sys.stderr)
        return 75 if any(
            marker in str(error)
            for marker in ["lock is unavailable", "source contract mismatch"]
        ) else 2


def _invalidate_cli_artifacts_after_lock_failure(
    values: argparse.Namespace, error: SharedLockValidationError
) -> None:
    state_dir = values.state_dir.resolve()
    manifest_path = state_dir / "manifest.json"
    if manifest_path.exists():
        try:
            manifest = load_manifest(manifest_path)
        except CheckpointError:
            manifest = None
        if manifest is not None:
            manifest.update(
                state="lock-unsafe", aggregate_authoritative=False,
                authority_scope="none", resumable=False,
                lock_validation_error=str(error), finished_at=utc_now(), updated_at=utc_now(),
            )
            atomic_json_write(manifest_path, manifest)
    if values.attest:
        selected = values.module or []
        output = values.attestation_output
        if output is None and len(selected) == 1:
            output = state_dir / "attestations" / f"{selected[0]}.json"
        if output is not None:
            Path(output).unlink(missing_ok=True)


def main(arguments: list[str] | None = None) -> int:
    values = parser().parse_args(arguments)
    base = default_base_command()
    try:
        with shared_lock_lease(values.lock) as (_handle, receipt):
            return _main_under_lease(values, base, receipt)
    except SharedLockValidationError as error:
        _invalidate_cli_artifacts_after_lock_failure(values, error)
        print(f"SH-07 checkpoint runner failed: {error}", file=sys.stderr)
        return 75
    except CheckpointError as error:
        print(f"SH-07 checkpoint runner failed: {error}", file=sys.stderr)
        return 75 if "lock is unavailable" in str(error) else 2


if __name__ == "__main__":
    raise SystemExit(main())
