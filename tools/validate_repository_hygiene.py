#!/usr/bin/env python3
"""Reject tracked Python interpreter cache outputs.

The repository keeps reviewed evidence artifacts under ``docs/artifacts`` and
``target``.  Python bytecode is different: it is host/version-specific scratch
output, is already ignored, and must not become a shared source-tree input.
"""

from __future__ import annotations

import subprocess
import sys
from pathlib import Path, PurePosixPath
from typing import Iterable, Sequence


ROOT = Path(__file__).resolve().parents[1]


class HygieneError(RuntimeError):
    """Raised when tracked-path discovery cannot be completed safely."""


def tracked_python_cache_paths(paths: Iterable[str]) -> list[str]:
    """Return sorted tracked paths that are Python interpreter cache output."""

    rejected: set[str] = set()
    for raw_path in paths:
        path = PurePosixPath(raw_path)
        if "__pycache__" in path.parts or path.suffix in {".pyc", ".pyo"}:
            rejected.add(raw_path)
    return sorted(rejected)


def git_tracked_paths(root: Path | str = ROOT) -> list[str]:
    """Read the repository index without consulting ignored/untracked files."""

    root_path = Path(root).resolve()
    try:
        result = subprocess.run(
            ["git", "ls-files", "-z"],
            cwd=root_path,
            check=False,
            capture_output=True,
        )
    except OSError as exc:
        raise HygieneError(f"cannot start git ls-files: {exc}") from exc
    if result.returncode != 0:
        detail = result.stderr.decode("utf-8", errors="replace").strip()
        raise HygieneError(f"git ls-files failed: {detail or result.returncode}")
    try:
        return [item for item in result.stdout.decode("utf-8").split("\0") if item]
    except UnicodeDecodeError as exc:
        raise HygieneError("git index contains a non-UTF-8 path") from exc


def validate_repository(root: Path | str = ROOT) -> list[str]:
    """Return tracked Python cache violations for callers and tests."""

    return tracked_python_cache_paths(git_tracked_paths(root))


def main(argv: Sequence[str] | None = None) -> int:
    if argv:
        print("usage: validate_repository_hygiene.py", file=sys.stderr)
        return 2
    try:
        violations = validate_repository()
    except HygieneError as exc:
        print(f"repository hygiene validation failed: {exc}", file=sys.stderr)
        return 2
    if violations:
        print("repository hygiene validation failed: tracked Python cache output", file=sys.stderr)
        for path in violations:
            print(f"- {path}", file=sys.stderr)
        return 1
    print("repository hygiene validation passed: no tracked Python cache output")
    return 0


if __name__ == "__main__":
    raise SystemExit(main(sys.argv[1:]))
