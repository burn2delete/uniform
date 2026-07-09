#!/usr/bin/env python3
"""Validate the Phase 01 P01-T06 extension-hook contracts."""

from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / "src"))

from gravity.extensions import extension_manifest_diagnostic, validate_extension_manifest_file  # noqa: E402


FIXTURE_DIR = ROOT / "docs/artifacts/phase-01/fixtures/extensions"
DEFAULT_ACCEPTED = FIXTURE_DIR / "accepted-extension-hooks.json"
NEGATIVE_FIXTURES = {
    "rejected-macro-build-effect.json": "L16-BUILD-EFFECT",
    "rejected-macro-syntax-object.json": "L16-SYNTAX-OBJECT",
    "rejected-type-effect-erasure.json": "L17-EFFECT-ERASURE",
    "rejected-type-soundness.json": "L17-SOUNDNESS",
    "rejected-memory-hidden-alloc.json": "L18-HIDDEN-ALLOC",
    "rejected-memory-unsafe-audit.json": "L18-UNSAFE-AUDIT",
    "rejected-interop-incomplete.json": "L19-BOUNDARY-INCOMPLETE",
    "rejected-interop-host-leak.json": "L19-HOST-LEAK",
}


def require(condition: bool, message: str) -> None:
    if not condition:
        raise AssertionError(message)


def validate_accepted(path: Path) -> dict:
    artifact = validate_extension_manifest_file(path, ROOT)
    require(artifact["kind"] == "extension-hook-artifact", "artifact kind mismatch")
    require(artifact["module"] == "extension.demo", "module mismatch")
    for name in ["reader", "namespace", "macro_trace", "core_ast", "typed_core"]:
        require(name in artifact["pipeline_artifacts"], f"pipeline artifact {name} missing")
    provider_kinds = {provider["kind"] for provider in artifact["providers"]}
    require(":macro-system" in provider_kinds, "macro provider missing")
    require(":type-system" in provider_kinds, "type provider missing")
    require(":memory-system" in provider_kinds, "memory provider missing")
    require(artifact["interop_boundaries"], "interop boundary missing")
    macro = next(provider for provider in artifact["providers"] if provider["kind"] == ":macro-system")
    require(macro["reference_equivalence"]["result"] == ":equivalent", "macro equivalence missing")
    require("typed-core" in macro["generated_validation"], "macro provider bypasses typed core")
    type_provider = next(provider for provider in artifact["providers"] if provider["kind"] == ":type-system")
    require(type_provider["effect_preservation"], "type provider lost effects")
    require(type_provider["capability_preservation"], "type provider lost capabilities")
    memory = next(provider for provider in artifact["providers"] if provider["kind"] == ":memory-system")
    require(":unsafe-island" in memory["safe_outcomes"], "memory provider lacks unsafe-island classification")
    return artifact


def validate_negative_fixtures() -> list[str]:
    observed: list[str] = []
    for filename, expected in NEGATIVE_FIXTURES.items():
        path = FIXTURE_DIR / filename
        diagnostic = extension_manifest_diagnostic(path, ROOT)
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
        print(f"extension hook validation failed: {exc}", file=sys.stderr)
        return 1

    if args.artifact_out:
        args.artifact_out.parent.mkdir(parents=True, exist_ok=True)
        args.artifact_out.write_text(json.dumps(artifact, indent=2, sort_keys=True) + "\n", encoding="utf-8")

    print(
        "extension hook validation passed: "
        f"{len(artifact['providers'])} providers, {len(artifact['interop_boundaries'])} interop boundaries, "
        f"{len(diagnostics)} rejected fixtures"
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
