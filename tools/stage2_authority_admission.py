#!/usr/bin/env python3
"""Safely admit one immutable Stage2 fast-forward under the SH-07 lease."""

from __future__ import annotations

import argparse
import contextlib
import dataclasses
import fcntl
import json
import os
from pathlib import Path, PurePosixPath
import re
import stat
import subprocess
import sys
from collections.abc import Callable, Mapping, Sequence

try:
    from tools import run_sh07_authoritative_modules as sh07
except ImportError:
    import run_sh07_authoritative_modules as sh07


SCHEMA = "gravity/stage2-authority-admission-v1"
DEFAULT_LOCK = Path("/private/tmp/gravity-sh07-heavy.lock")
EXIT_ADMISSION = 75
OID = re.compile(r"[0-9a-f]{40}\Z")
REPOSITORY_OPERATION_PATHS = (
    "MERGE_HEAD", "CHERRY_PICK_HEAD", "REVERT_HEAD", "REBASE_HEAD",
    "AUTO_MERGE", "BISECT_LOG", "rebase-merge", "rebase-apply", "sequencer",
    "index.lock", "shallow.lock",
)


class AdmissionError(RuntimeError):
    def __init__(self, diagnostic: str, message: str, **details: object) -> None:
        super().__init__(message)
        self.diagnostic = diagnostic
        self.details = details


CatalogProvider = Callable[[], Mapping[str, str]]


@dataclasses.dataclass(frozen=True)
class AuthorityLease:
    path: Path
    mode_migrated: bool


def _git(root: Path, *arguments: str, check: bool = True) -> subprocess.CompletedProcess[bytes]:
    result = subprocess.run(
        ["git", "-c", "core.fsmonitor=false", *arguments],
        cwd=root, capture_output=True, check=False,
    )
    if check and result.returncode != 0:
        message = result.stderr.decode("utf-8", errors="replace").strip()
        raise AdmissionError(
            "STAGE2-ADMISSION-CANDIDATE-UNUSABLE",
            message or f"git {' '.join(arguments)} failed",
            git_arguments=list(arguments), git_exit_code=result.returncode,
        )
    return result


def _require_immutable_oid(root: Path, value: str, label: str) -> str:
    if OID.fullmatch(value) is None:
        raise AdmissionError(
            "STAGE2-ADMISSION-CANDIDATE-UNUSABLE",
            f"{label} must be a lowercase full 40-hex commit OID",
        )
    observed = _git(root, "rev-parse", "--verify", f"{value}^{{commit}}").stdout.decode("ascii").strip()
    if observed != value:
        raise AdmissionError(
            "STAGE2-ADMISSION-CANDIDATE-UNUSABLE",
            f"{label} does not identify the declared immutable commit",
        )
    return value


def _tree_oid(root: Path, commit: str) -> str:
    return _git(root, "rev-parse", "--verify", f"{commit}^{{tree}}").stdout.decode("ascii").strip()


def _head_oid(root: Path) -> str:
    return _git(root, "rev-parse", "--verify", "HEAD^{commit}").stdout.decode("ascii").strip()


def _branch_ref(root: Path) -> str:
    result = _git(root, "symbolic-ref", "-q", "HEAD", check=False)
    branch = result.stdout.decode("utf-8", errors="strict").strip()
    if result.returncode != 0 or not branch.startswith("refs/heads/"):
        raise AdmissionError(
            "STAGE2-ADMISSION-RANGE-DRIFT",
            "hard admission requires a named local coordinator branch",
        )
    return branch


def _ref_oid(root: Path, branch: str) -> str:
    return _git(root, "rev-parse", "--verify", branch).stdout.decode("ascii").strip()


def _decode_path(value: bytes) -> str:
    try:
        decoded = value.decode("utf-8")
    except UnicodeDecodeError as error:
        raise AdmissionError("STAGE2-ADMISSION-CANDIDATE-UNUSABLE", "git path is not valid UTF-8") from error
    path = PurePosixPath(decoded)
    if not decoded or path.is_absolute() or ".." in path.parts:
        raise AdmissionError("STAGE2-ADMISSION-CANDIDATE-UNUSABLE", f"unsafe git path: {decoded!r}")
    return decoded


def _name_status(root: Path, *arguments: str) -> list[dict[str, object]]:
    tokens = _git(root, *arguments).stdout.split(b"\0")
    if tokens and not tokens[-1]:
        tokens.pop()
    result: list[dict[str, object]] = []
    index = 0
    while index < len(tokens):
        status_text = tokens[index].decode("ascii", errors="strict")
        index += 1
        count = 2 if status_text[:1] in {"R", "C"} else 1
        if index + count > len(tokens):
            raise AdmissionError("STAGE2-ADMISSION-CANDIDATE-UNUSABLE", "malformed git name-status output")
        paths = [_decode_path(item) for item in tokens[index:index + count]]
        index += count
        result.append({"status": status_text, "paths": paths})
    return result


def _candidate_changes(root: Path, base: str, candidate: str) -> list[dict[str, object]]:
    if _git(root, "merge-base", "--is-ancestor", base, candidate, check=False).returncode != 0:
        raise AdmissionError(
            "STAGE2-ADMISSION-CANDIDATE-UNUSABLE",
            "candidate is not a fast-forward descendant of the coordinator base",
            general_merge_tree="deferred",
        )
    return _name_status(
        root, "diff", "--name-status", "-z", "-M", "--no-ext-diff", base, candidate,
    )


def _worktree_status(root: Path) -> list[str]:
    raw = _git(
        root, "status", "--porcelain=v2", "-z", "--untracked-files=all", "--ignored=matching",
    ).stdout
    return [token.decode("utf-8", errors="backslashreplace") for token in raw.split(b"\0") if token]


def _allowed_ignored_output(root: Path, entry: str) -> bool:
    if not entry.startswith("! "):
        return False
    relative = entry[2:]
    path = PurePosixPath(relative)
    if not relative or path.is_absolute() or ".." in path.parts:
        return False
    if relative.endswith(".class") or path.name in sh07.ROOT_CLASSPATH_LOAD_RESOURCES:
        return False
    if relative in sh07.SHARED_REPOSITORY_FILES or any(
        relative == tree or relative.startswith(tree + "/")
        for tree in sh07.SHARED_REPOSITORY_TREES
    ):
        return False
    allowed = (
        relative == ".cpcache" or relative.startswith(".cpcache/")
        or relative == "target/validation" or relative.startswith("target/validation/")
        or relative == "target/logs" or relative.startswith("target/logs/")
        or "__pycache__" in path.parts or relative.endswith(".pyc")
    )
    if not allowed:
        return False
    current = root
    for component in path.parts:
        current = current / component
        try:
            metadata = os.lstat(current)
        except FileNotFoundError:
            return False
        if stat.S_ISLNK(metadata.st_mode):
            return False
        if current == root / relative and not (
            stat.S_ISREG(metadata.st_mode) or stat.S_ISDIR(metadata.st_mode)
        ):
            return False
    try:
        current.relative_to(root)
    except ValueError:
        return False
    return True


def _repository_operation_state(root: Path) -> list[str]:
    active: list[str] = []
    for marker in REPOSITORY_OPERATION_PATHS:
        value = _git(root, "rev-parse", "--git-path", marker).stdout.decode("utf-8").strip()
        path = Path(value)
        if not path.is_absolute():
            path = root / path
        if path.exists():
            active.append(marker)
    return active


def _require_clean_coordinator(root: Path) -> None:
    status_entries = _worktree_status(root)
    disallowed = [
        entry for entry in status_entries if not _allowed_ignored_output(root, entry)
    ]
    operation_state = _repository_operation_state(root)
    if disallowed or operation_state:
        raise AdmissionError(
            "STAGE2-ADMISSION-DIRTY",
            "hard admission requires a completely clean coordinator and inactive git operation state",
            worktree_status=disallowed,
            allowed_ignored_outputs=sorted(set(status_entries) - set(disallowed)),
            repository_operation_state=operation_state,
        )


def _candidate_mode(root: Path, candidate: str, relative: str) -> str | None:
    raw = _git(root, "ls-tree", "-z", candidate, "--", relative).stdout
    if not raw:
        return None
    records = [record for record in raw.split(b"\0") if record]
    if len(records) != 1 or b"\t" not in records[0]:
        raise AdmissionError("STAGE2-ADMISSION-CANDIDATE-UNUSABLE", f"ambiguous candidate entry: {relative}")
    metadata, observed = records[0].split(b"\t", 1)
    if _decode_path(observed) != relative:
        raise AdmissionError("STAGE2-ADMISSION-CANDIDATE-UNUSABLE", f"wrong candidate entry: {relative}")
    return metadata.split(b" ", 1)[0].decode("ascii")


def _classify(
    root: Path,
    changes: Sequence[Mapping[str, object]],
    module_catalog: Mapping[str, str],
    candidate: str,
) -> dict[str, list[str]]:
    classified: dict[str, set[str]] = {name: set() for name in ("shared", "module", "unsafe", "unrelated")}
    for change in changes:
        for item in change["paths"]:
            relative = str(item)
            path_class = sh07.classify_fingerprint_path(relative, module_catalog)
            mode = _candidate_mode(root, candidate, relative)
            if mode is None and path_class == "unsafe" and relative.endswith(".class"):
                path_class = "shared"
            if mode in {"120000", "160000"} and (
                path_class in {"shared", "module"} or sh07.classpath_structural_path(relative)
            ):
                path_class = "unsafe"
            classified[path_class].add(relative)
    return {name: sorted(paths) for name, paths in classified.items()}


def _relevant(classified: Mapping[str, Sequence[str]]) -> list[str]:
    return sorted(set(classified["shared"]) | set(classified["module"]) | set(classified["unsafe"]))


def _canonical_lock_path(path: Path) -> Path:
    try:
        absolute = sh07.canonical_shared_lock_path(path)
    except sh07.CheckpointError as error:
        raise AdmissionError(
            "STAGE2-ADMISSION-LOCK-UNSAFE", str(error), lock_path=str(path)
        ) from error
    if absolute.parent != Path("/private/tmp") or absolute.name in {"", ".", ".."}:
        raise AdmissionError(
            "STAGE2-ADMISSION-LOCK-UNSAFE",
            "authority lock must be a direct child of /private/tmp", lock_path=str(absolute),
        )
    return absolute


@contextlib.contextmanager
def no_write_lock_lease(path: Path, *, _before_flock: Callable[[], None] | None = None):
    """Lease the canonical lock without writing content.

    A legacy owned 0644 inode is fchmod'd to 0600 only after exclusive flock.
    """
    canonical = _canonical_lock_path(path)
    try:
        parent = os.stat(canonical.parent, follow_symlinks=False)
        if not stat.S_ISDIR(parent.st_mode) or not stat.S_IMODE(parent.st_mode) & stat.S_ISVTX:
            raise AdmissionError("STAGE2-ADMISSION-LOCK-UNSAFE", "authority lock parent is not sticky")
        handle = sh07.open_lock_file(canonical, create=True)
    except (OSError, sh07.CheckpointError) as error:
        raise AdmissionError(
            "STAGE2-ADMISSION-LOCK-UNSAFE",
            "authority lock cannot be opened with the shared stable-inode protocol",
            expected_protocol=sh07.SHARED_LOCK_PROTOCOL,
            expected_mode="0600",
            lock_path=str(canonical),
        ) from error
    try:
        if _before_flock is not None:
            _before_flock()
        try:
            fcntl.flock(handle.descriptor, fcntl.LOCK_EX | fcntl.LOCK_NB)
        except BlockingIOError as error:
            raise AdmissionError(
                "STAGE2-ADMISSION-LOCK-BUSY",
                "shared SH-07 authority lock is busy; queue this integration",
                lock_path=str(canonical),
                lock_canonical_path=str(canonical),
                lock_mode=f"{stat.S_IMODE(os.fstat(handle.descriptor).st_mode):04o}",
                lock_mode_migrated=False,
            ) from error
        try:
            try:
                migrated = handle.migrate_legacy_mode_after_exclusive_lock()
            except sh07.CheckpointError as error:
                raise AdmissionError(
                    "STAGE2-ADMISSION-LOCK-UNSAFE",
                    "authority lock migration failed after exclusive acquisition",
                    lock_path=str(canonical), lock_canonical_path=str(canonical),
                    lock_mode_migrated=False,
                ) from error
            try:
                yield AuthorityLease(canonical, migrated)
            except BaseException as body_error:
                try:
                    handle.validate()
                except sh07.CheckpointError as validation_error:
                    raise AdmissionError(
                        "STAGE2-ADMISSION-LOCK-UNSAFE",
                        "authority lock pathname changed while the transaction failed",
                        lock_path=str(canonical), lock_canonical_path=str(canonical),
                        lock_mode_migrated=migrated,
                    ) from body_error
                raise
            else:
                try:
                    handle.validate()
                except sh07.CheckpointError as error:
                    raise AdmissionError(
                        "STAGE2-ADMISSION-LOCK-UNSAFE",
                        "authority lock pathname changed while leased",
                        lock_path=str(canonical), lock_canonical_path=str(canonical),
                        lock_mode_migrated=migrated,
                    ) from error
        finally:
            fcntl.flock(handle.descriptor, fcntl.LOCK_UN)
    finally:
        handle.close()


def _fixed_fast_forward(root: Path, candidate: str) -> None:
    result = _git(
        root, "-c", "core.hooksPath=/dev/null", "merge", "--ff-only", "--no-stat", candidate,
        check=False,
    )
    if result.returncode != 0:
        raise AdmissionError(
            "STAGE2-ADMISSION-INTEGRATION-FAILED",
            "fixed fast-forward failed under the authority lease",
            integration_exit_code=result.returncode,
            integration_stderr=result.stderr.decode("utf-8", errors="replace").strip(),
        )


def _validate_documented_command(command: Sequence[str], candidate: str) -> None:
    if list(command) != ["git", "merge", "--ff-only", candidate]:
        raise AdmissionError(
            "STAGE2-ADMISSION-COMMAND-UNSAFE",
            "hard admission accepts only: git merge --ff-only <candidate-oid>; execution is internal",
            command=list(command),
        )


def _failure(error: AdmissionError, *, base: str, candidate: str, advisory: bool) -> dict[str, object]:
    return {
        **error.details,
        "schema": SCHEMA,
        "status": "queued" if error.diagnostic == "STAGE2-ADMISSION-LOCK-BUSY" else "failed",
        "diagnostic": error.diagnostic,
        "message": str(error),
        "exit_code": EXIT_ADMISSION,
        "advisory": advisory,
        "authority_granted": False,
        "integration_admission_granted": False,
        "proof_authority_granted": False,
        "authority_scope": "none",
        "base": base,
        "candidate": candidate,
    }


def admit(
    *,
    root: Path,
    base: str,
    candidate: str,
    probe_only: bool,
    command: Sequence[str] = (),
    lock_path: Path = DEFAULT_LOCK,
    module_catalog: Mapping[str, str] | None = None,
    catalog_provider: CatalogProvider | None = None,
    before_lock: Callable[[], None] | None = None,
) -> dict[str, object]:
    root = root.resolve()
    try:
        base_oid = _require_immutable_oid(root, base, "base")
        candidate_oid = _require_immutable_oid(root, candidate, "candidate")
        changes = _candidate_changes(root, base_oid, candidate_oid)
        base_tree = _tree_oid(root, base_oid)
        candidate_tree = _tree_oid(root, candidate_oid)

        # Hard mode refuses all user/repository state before the lease or any
        # executable catalog provider. No post-destruction comparison is used.
        branch = None
        if not probe_only:
            _validate_documented_command(command, candidate_oid)
            _require_clean_coordinator(root)
            branch = _branch_ref(root)
            if _head_oid(root) != base_oid or _ref_oid(root, branch) != base_oid:
                raise AdmissionError("STAGE2-ADMISSION-RANGE-DRIFT", "coordinator branch is not at the declared base")
        elif command:
            raise AdmissionError("STAGE2-ADMISSION-COMMAND-UNSAFE", "advisory mode cannot execute a command")

        def load_catalog() -> Mapping[str, str]:
            if module_catalog is not None:
                return sh07.validated_module_catalog_paths(module_catalog)
            provider = catalog_provider or (
                lambda: sh07.discover_module_catalog(root, sh07.default_base_command(), 30)
            )
            return sh07.validated_module_catalog_paths(provider())

        # Advisory planning may classify before an optional nonblocking lease.
        if probe_only:
            acquired = False
            canonical: str | None = None
            if module_catalog is None:
                status_entries = _worktree_status(root)
                disallowed = [
                    entry for entry in status_entries
                    if not _allowed_ignored_output(root, entry)
                ]
                if disallowed:
                    raise AdmissionError(
                        "STAGE2-ADMISSION-DIRTY",
                        "dynamic advisory classification requires a clean coordinator",
                        worktree_status=disallowed,
                    )
                with no_write_lock_lease(lock_path) as lease:
                    acquired = True
                    canonical = str(lease.path)
                    lock_mode_migrated = lease.mode_migrated
                    catalog = load_catalog()
                    classified = _classify(root, changes, catalog, candidate_oid)
            else:
                lock_mode_migrated = False
                catalog = load_catalog()
                classified = _classify(root, changes, catalog, candidate_oid)
                if _relevant(classified):
                    with no_write_lock_lease(lock_path) as lease:
                        acquired = True
                        canonical = str(lease.path)
                        lock_mode_migrated = lease.mode_migrated
            relevant = _relevant(classified)
            if classified["unsafe"]:
                raise AdmissionError("STAGE2-ADMISSION-CANDIDATE-UNUSABLE", "candidate introduces an unsafe classpath identity", unsafe_paths=classified["unsafe"])
            return {
                "schema": SCHEMA, "status": "advisory", "diagnostic": "STAGE2-ADMISSION-PROBE-PASSED",
                "exit_code": 0, "advisory": True, "authority_granted": False,
                "integration_admission_granted": False,
                "proof_authority_granted": False, "authority_scope": "none",
                "base": base, "candidate": candidate, "base_oid": base_oid,
                "candidate_oid": candidate_oid, "base_tree": base_tree, "candidate_tree": candidate_tree,
                "changes": changes, "classified_paths": classified, "relevant_paths": relevant,
                "worktree_status": _worktree_status(root),
                "lock_required": bool(relevant) or module_catalog is None,
                "lock_acquired": acquired, "lock_path": canonical,
                "lock_canonical_path": canonical,
                "lock_mode": "0600" if acquired else None,
                "lock_mode_migrated": lock_mode_migrated,
                "general_merge_tree": "deferred-fast-forward-only",
            }

        if before_lock is not None:
            before_lock()
        with no_write_lock_lease(lock_path) as lease:
            # Recheck every precondition under the unconditional hard lease,
            # then (and only then) allow dynamic catalog discovery.
            _require_clean_coordinator(root)
            assert branch is not None
            if _head_oid(root) != base_oid or _ref_oid(root, branch) != base_oid:
                raise AdmissionError("STAGE2-ADMISSION-RANGE-DRIFT", "coordinator branch moved before fast-forward")
            if _tree_oid(root, base_oid) != base_tree or _tree_oid(root, candidate_oid) != candidate_tree:
                raise AdmissionError("STAGE2-ADMISSION-RANGE-DRIFT", "immutable integration trees changed unexpectedly")
            catalog = load_catalog()
            classified = _classify(root, changes, catalog, candidate_oid)
            relevant = _relevant(classified)
            if classified["unsafe"]:
                raise AdmissionError("STAGE2-ADMISSION-CANDIDATE-UNUSABLE", "candidate introduces an unsafe classpath identity", unsafe_paths=classified["unsafe"])
            _require_clean_coordinator(root)
            if _head_oid(root) != base_oid or _ref_oid(root, branch) != base_oid:
                raise AdmissionError("STAGE2-ADMISSION-RANGE-DRIFT", "coordinator branch moved during catalog classification")
            _fixed_fast_forward(root, candidate_oid)
            if (
                _branch_ref(root) != branch or _head_oid(root) != candidate_oid
                or _ref_oid(root, branch) != candidate_oid
                or _tree_oid(root, "HEAD") != candidate_tree
            ):
                raise AdmissionError("STAGE2-ADMISSION-RANGE-DRIFT", "fixed fast-forward did not produce the exact candidate branch/tree")
            _require_clean_coordinator(root)
            return {
                "schema": SCHEMA, "status": "admitted", "diagnostic": "STAGE2-ADMISSION-PASSED",
                "exit_code": 0, "advisory": False, "authority_granted": False,
                "integration_admission_granted": True,
                "proof_authority_granted": False,
                "authority_scope": "lock-held-fixed-fast-forward-only",
                "base": base, "candidate": candidate, "base_oid": base_oid,
                "candidate_oid": candidate_oid, "base_tree": base_tree, "candidate_tree": candidate_tree,
                "branch_ref": branch, "changes": changes, "classified_paths": classified,
                "relevant_paths": relevant, "lock_required": True, "lock_acquired": True,
                "lock_path": str(lease.path),
                "lock_canonical_path": str(lease.path),
                "lock_mode": "0600", "lock_mode_migrated": lease.mode_migrated,
                "integration_operation": "fixed-fast-forward-only",
                "general_merge_tree": "deferred-fast-forward-only",
                "lock_namespace_limit": "cooperative-canonical-path-protocol",
            }
    except AdmissionError as error:
        return _failure(error, base=base, candidate=candidate, advisory=probe_only)
    except (OSError, subprocess.SubprocessError, sh07.CheckpointError) as error:
        return _failure(
            AdmissionError("STAGE2-ADMISSION-CANDIDATE-UNUSABLE", f"authority context cannot be classified safely: {error}"),
            base=base, candidate=candidate, advisory=probe_only,
        )


def render_human(result: Mapping[str, object]) -> str:
    lines = [
        f"stage2-authority-admission: {result['status']}",
        f"diagnostic: {result['diagnostic']}",
        "integration-admission-granted: "
        + str(bool(result.get("integration_admission_granted"))).lower(),
        "proof-authority-granted: "
        + str(bool(result.get("proof_authority_granted"))).lower(),
        f"advisory: {str(bool(result.get('advisory'))).lower()}",
    ]
    if result.get("relevant_paths"):
        lines.append("relevant-paths: " + ", ".join(result["relevant_paths"]))
    if result.get("message"):
        lines.append("message: " + str(result["message"]))
    return "\n".join(lines) + "\n"


def parser() -> argparse.ArgumentParser:
    value = argparse.ArgumentParser(description=__doc__)
    value.add_argument("--base", required=True)
    value.add_argument("--candidate", required=True)
    value.add_argument("--cwd", type=Path, default=Path.cwd())
    value.add_argument("--lock", type=Path, default=DEFAULT_LOCK)
    value.add_argument("--probe-only", action="store_true")
    value.add_argument("--human", action="store_true")
    value.add_argument("--exec", dest="integration_command", nargs=argparse.REMAINDER)
    return value


def main(arguments: list[str] | None = None) -> int:
    raw = list(sys.argv[1:] if arguments is None else arguments)
    command: list[str] = []
    if "--exec" in raw:
        position = raw.index("--exec")
        command = raw[position + 1:]
        raw = raw[:position]
        if command[:1] == ["--"]:
            command = command[1:]
    values = parser().parse_args(raw)
    if values.probe_only == bool(command):
        result = _failure(
            AdmissionError("STAGE2-ADMISSION-COMMAND-UNSAFE", "select exactly one of --probe-only or --exec -- git merge --ff-only <candidate-oid>"),
            base=values.base, candidate=values.candidate, advisory=values.probe_only,
        )
    else:
        result = admit(
            root=values.cwd, base=values.base, candidate=values.candidate,
            probe_only=values.probe_only, command=command, lock_path=values.lock,
        )
    sys.stdout.write(render_human(result) if values.human else json.dumps(result, sort_keys=True, ensure_ascii=True) + "\n")
    return int(result["exit_code"])


if __name__ == "__main__":
    raise SystemExit(main())
