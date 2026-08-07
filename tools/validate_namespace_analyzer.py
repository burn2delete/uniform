#!/usr/bin/env python3
"""Validate the Phase 01 L3 namespace analyzer implementation."""

from __future__ import annotations

import argparse
import sys
from pathlib import Path

if __package__:
    from .output_publication import atomic_write_json
else:
    from output_publication import atomic_write_json


ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / "src"))

from gravity.namespace import ModuleContext, analyze_source, analyze_source_diagnostic  # noqa: E402


FIXTURE_DIR = ROOT / "docs/artifacts/phase-01/fixtures/namespace"
DEFAULT_ACCEPTED = FIXTURE_DIR / "accepted-server-module.gravity"
NEGATIVE_FIXTURES = {
    "rejected-missing-ns.gravity": "L3-NS-MISSING",
    "rejected-multiple-profiles.gravity": "L3-PROFILE-MULTIPLE",
    "rejected-unknown-alias.gravity": "L3-UNKNOWN-ALIAS",
    "rejected-cross-profile.gravity": "L3-CROSS-PROFILE",
    "rejected-capability-missing.gravity": "L3-CAPABILITY-MISSING",
}


def require(condition: bool, message: str) -> None:
    if not condition:
        raise AssertionError(message)


def context() -> ModuleContext:
    return ModuleContext(
        package="acme/server",
        package_capabilities=frozenset({":network/listener", ":io/stdout", ":http/client"}),
        known_module_profiles={
            "gravity.net.http": ":native",
            "app.schema": ":native",
            "shared.checksum": ":core",
            "web.ui": ":hosted",
        },
    )


def validate_accepted(path: Path) -> dict:
    source = path.read_text(encoding="utf-8")
    artifact = analyze_source(source, str(path.relative_to(ROOT)), context=context())
    require(artifact["kind"] == "module-artifact", "module artifact kind mismatch")
    require(artifact["module"] == "app.server", "module name mismatch")
    require(artifact["profile"] == ":native", "profile not extracted")
    require(artifact["target"] == ":llvm", "target not extracted")
    require(artifact["effects"] == [":network/listen", ":io/write"], "effects not preserved")
    require(artifact["capabilities"] == [":network/listener", ":io/stdout"], "capabilities not preserved")
    require(artifact["safety"] == ":safe-optimized", "safety mode not preserved")
    require(artifact["alias_table"].get("http") == "gravity.net.http", "http alias missing")
    require(artifact["alias_table"].get("schema") == "app.schema", "schema alias missing")
    require("main" in artifact["exports"], "export main missing")
    require(artifact["source_hash"].startswith("sha256:"), "source hash missing")
    require(artifact["definitions_hash"].startswith("sha256:"), "definitions hash missing")
    require(artifact["dependency_graph"]["dependencies"], "dependency graph empty")
    require(any(item["name"] == "main" and item["visibility"] == "public" for item in artifact["definitions"]), "public main definition missing")
    return artifact


def validate_negative_fixtures() -> list[str]:
    observed: list[str] = []
    for filename, expected in NEGATIVE_FIXTURES.items():
        path = FIXTURE_DIR / filename
        diagnostic = analyze_source_diagnostic(path.read_text(encoding="utf-8"), str(path.relative_to(ROOT)), context=context())
        require(diagnostic is not None, f"{filename} did not produce a diagnostic")
        require(diagnostic["id"] == expected, f"{filename} produced {diagnostic['id']} instead of {expected}")
        for key in ["span", "remediation", "analyzer_stage"]:
            require(key in diagnostic, f"{filename} diagnostic missing {key}")
        observed.append(expected)
    return observed


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--accepted", type=Path, default=DEFAULT_ACCEPTED)
    parser.add_argument("--artifact-out", type=Path)
    args = parser.parse_args()

    try:
        artifact = validate_accepted(args.accepted)
        diagnostics = validate_negative_fixtures()
    except AssertionError as exc:
        print(f"namespace analyzer validation failed: {exc}", file=sys.stderr)
        return 1

    if args.artifact_out:
        atomic_write_json(args.artifact_out, artifact)

    print(
        "namespace analyzer validation passed: "
        f"{artifact['module']} module, {len(diagnostics)} rejected fixtures"
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
