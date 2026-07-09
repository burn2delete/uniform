#!/usr/bin/env python3
"""Validate document-specific coverage for L3 namespace and module rules."""

from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / "src"))

from gravity.namespace import ModuleContext, analyze_source, analyze_source_diagnostic  # noqa: E402


NAMESPACE_FIXTURE_DIR = ROOT / "docs/artifacts/phase-01/fixtures/namespace"
L3_FIXTURE_DIR = ROOT / "docs/artifacts/phase-01/fixtures/l3"
ACCEPTED_MODULE = NAMESPACE_FIXTURE_DIR / "accepted-server-module.gravity"
NEGATIVE_FIXTURES = {
    "rejected-effect-widen.gravity": "L3-EFFECT-WIDEN",
    "rejected-private-import.gravity": "L3-PRIVATE-IMPORT",
    "rejected-ambiguous-name.gravity": "L3-AMBIGUOUS-NAME",
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
            "lib.one": ":native",
            "lib.two": ":native",
            "app.internal": ":native",
        },
        known_public_api={
            "lib.one": frozenset({"make"}),
            "lib.two": frozenset({"make"}),
            "app.internal": frozenset({"public-value"}),
        },
    )


def validate_accepted() -> dict:
    artifact = analyze_source(ACCEPTED_MODULE.read_text(encoding="utf-8"), str(ACCEPTED_MODULE.relative_to(ROOT)), context=context())
    require(artifact["kind"] == "module-artifact", "module artifact kind mismatch")
    require(artifact["module"] == "app.server", "module mismatch")
    require(artifact["alias_table"].get("http") == "gravity.net.http", "http alias missing")
    require(artifact["public_api_manifest"]["exports"], "public API manifest missing exports")
    require(artifact["dependency_graph"]["dependencies"], "dependency graph missing dependencies")
    require(artifact["source_hash"].startswith("sha256:"), "source hash missing")
    require(artifact["definitions_hash"].startswith("sha256:"), "definitions hash missing")
    return artifact


def validate_negative_fixtures() -> list[dict[str, str]]:
    diagnostics = []
    for filename, expected in NEGATIVE_FIXTURES.items():
        path = L3_FIXTURE_DIR / filename
        diagnostic = analyze_source_diagnostic(path.read_text(encoding="utf-8"), str(path.relative_to(ROOT)), context=context())
        require(diagnostic is not None, f"{filename} did not produce a diagnostic")
        require(diagnostic["id"] == expected, f"{filename} produced {diagnostic['id']} instead of {expected}")
        diagnostics.append({"fixture": str(path.relative_to(ROOT)), "diagnostic": diagnostic["id"]})
    return diagnostics


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--artifact-out", type=Path)
    args = parser.parse_args()

    try:
        module_artifact = validate_accepted()
        diagnostics = validate_negative_fixtures()
    except AssertionError as exc:
        print(f"L3 document coverage validation failed: {exc}", file=sys.stderr)
        return 1

    artifact = {
        "kind": "l3-document-coverage",
        "document": "L3",
        "accepted": [
            {
                "fixture": str(ACCEPTED_MODULE.relative_to(ROOT)),
                "artifact_kind": module_artifact["kind"],
                "coverage": ["module artifact", "alias table", "dependency graph", "public API manifest", "content hashes"],
            }
        ],
        "rejected": diagnostics,
    }
    if args.artifact_out:
        args.artifact_out.parent.mkdir(parents=True, exist_ok=True)
        args.artifact_out.write_text(json.dumps(artifact, indent=2, sort_keys=True) + "\n", encoding="utf-8")

    print("L3 document coverage validation passed: 1 accepted artifact, 3 rejected diagnostics")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
