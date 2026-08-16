#!/usr/bin/env python3
"""Validate metadata-first Gravity wrapper dispatch and hosted-toolchain failures."""

from __future__ import annotations

import os
from pathlib import Path
import struct
import subprocess
import tempfile
import zipfile


ROOT = Path(__file__).resolve().parents[1]
WRAPPER = ROOT / "bin" / "gravity"
PACKAGED_JAR = ROOT / "target" / "phase-18" / "jvm-cli" / "gravity-jvm-cli.jar"
SYSTEM_PATH = "/usr/bin:/bin:/usr/sbin:/sbin"


def require(condition: bool, message: str) -> None:
    if not condition:
        raise SystemExit(f"validation failed: {message}")


def run_wrapper(args: list[str], env: dict[str, str]) -> subprocess.CompletedProcess[str]:
    return subprocess.run(
        [str(WRAPPER), *args],
        cwd=ROOT,
        env=env,
        text=True,
        capture_output=True,
        check=False,
    )


def packaged_class_major() -> int:
    require(PACKAGED_JAR.is_file(), f"missing packaged JAR: {PACKAGED_JAR}")
    with zipfile.ZipFile(PACKAGED_JAR) as archive:
        header = archive.read("gravity/cli/Main.class")[:8]
    require(len(header) == 8, "packaged launcher class header is truncated")
    magic, _minor, major = struct.unpack(">IHH", header)
    require(magic == 0xCAFEBABE, "packaged launcher has an invalid class header")
    return major


def write_java_stub(directory: Path, runtime_class_major: int) -> None:
    java = directory / "java"
    java.write_text(
        "#!/bin/sh\n"
        f"echo '    java.class.version = {runtime_class_major}.0' >&2\n"
        "exit 0\n",
        encoding="ascii",
    )
    java.chmod(0o755)


def write_clojure_guard(directory: Path) -> None:
    clojure = directory / "clojure"
    clojure.write_text(
        "#!/bin/sh\n"
        "echo 'validation error: clojure guard should not execute' >&2\n"
        "exit 99\n",
        encoding="ascii",
    )
    clojure.chmod(0o755)


def test_metadata_without_clojure(env: dict[str, str]) -> None:
    version = run_wrapper(["--version"], env)
    require(version.returncode == 0, "--version must not require Clojure")
    require(':phase "P18-T02"' in version.stdout, "--version must report P18-T02")
    require(":bootstrap-hosted? true" in version.stdout, "version lost bootstrap label")
    require(":packaged-jvm-cli? true" in version.stdout, "version lost package label")
    require(":seedless-release? false" in version.stdout, "version overclaimed seedless status")
    require("P18T02007" not in version.stderr, "--version reached Clojure preflight")

    help_result = run_wrapper(["help"], env)
    require(help_result.returncode == 0, "help must not require Clojure")
    require("gravity check" in help_result.stdout, "help lost the check command")
    require(":packaged-jvm-cli? true" in help_result.stdout, "help lost package label")
    require(":seedless-release? false" in help_result.stdout, "help overclaimed seedless status")

    seedless = run_wrapper(["--assert-seedless-release"], env)
    require(seedless.returncode == 1, "seedless overclaim must fail")
    require("P18T02001" in seedless.stderr, "packaged seedless diagnostic changed")
    require(":bootstrap-hosted? true" in seedless.stderr, "overclaim lost bootstrap label")
    require(":seedless-release? false" in seedless.stderr, "overclaim lost seed label")
    require("P18T02007" not in seedless.stderr, "seedless rejection reached Clojure preflight")


def test_missing_clojure(env: dict[str, str]) -> None:
    result = run_wrapper(["check", "examples/hello.gravity"], env)
    require(result.returncode == 1, "missing Clojure must fail closed")
    require("P18T02007" in result.stderr, "missing-Clojure diagnostic was not emitted")
    require("P18T02008" not in result.stderr, "compatible Java stub was rejected")
    require("command not found" not in result.stderr, "raw shell failure leaked")


def test_incompatible_java(env: dict[str, str], jar_major: int) -> None:
    result = run_wrapper(["check", "examples/hello.gravity"], env)
    require(result.returncode == 1, "incompatible Java must fail closed")
    require("P18T02008" in result.stderr, "Java class-version diagnostic was not emitted")
    require("P18T02007" not in result.stderr, "Clojure guard was not recognized")
    require(f":jar-class-major {jar_major}" in result.stderr, "JAR class major was not reported")
    require(
        f":runtime-class-major {jar_major - 1}" in result.stderr,
        "runtime class major was not reported",
    )
    require("validation error" not in result.stderr, "Clojure ran after Java preflight failed")


def main() -> None:
    jar_major = packaged_class_major()
    require(jar_major > 1, "packaged launcher class major is not testable")

    with tempfile.TemporaryDirectory(prefix="gravity-toolchain-validator-") as temp:
        temp_root = Path(temp)

        compatible_java = temp_root / "compatible-java"
        compatible_java.mkdir()
        write_java_stub(compatible_java, jar_major)
        no_clojure_env = os.environ.copy()
        no_clojure_env["PATH"] = f"{compatible_java}:{SYSTEM_PATH}"
        test_metadata_without_clojure(no_clojure_env)
        test_missing_clojure(no_clojure_env)

        incompatible_java = temp_root / "incompatible-java"
        incompatible_java.mkdir()
        write_java_stub(incompatible_java, jar_major - 1)
        write_clojure_guard(incompatible_java)
        incompatible_env = os.environ.copy()
        incompatible_env["PATH"] = f"{incompatible_java}:{SYSTEM_PATH}"
        test_incompatible_java(incompatible_env, jar_major)

    print(
        "validation passed: gravity wrapper metadata dispatch and "
        "fail-closed hosted-toolchain diagnostics"
    )


if __name__ == "__main__":
    main()
