#!/usr/bin/env python3
"""Validate L18 alternative memory-model coverage."""

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

from gravity.alternative_memory import alternative_memory_manifest_diagnostic, validate_alternative_memory_manifest_file  # noqa: E402


FIXTURE_DIR = ROOT / "docs/artifacts/phase-01/fixtures/l18"
ACCEPTED = FIXTURE_DIR / "accepted-alternative-memory.json"
NEGATIVE_FIXTURES = {
    "rejected-provider.json": "L18-PROVIDER",
    "rejected-hidden-alloc.json": "L18-HIDDEN-ALLOC",
    "rejected-lifetime.json": "L18-LIFETIME",
    "rejected-escape.json": "L18-ESCAPE",
    "rejected-alias.json": "L18-ALIAS",
    "rejected-uninit.json": "L18-UNINIT",
    "rejected-double-release.json": "L18-DOUBLE-RELEASE",
    "rejected-leak.json": "L18-LEAK",
    "rejected-bounds.json": "L18-BOUNDS",
    "rejected-device-sync.json": "L18-DEVICE-SYNC",
    "rejected-mmio.json": "L18-MMIO",
    "rejected-ffi-allocator.json": "L18-FFI-ALLOCATOR",
    "rejected-unsafe-audit.json": "L18-UNSAFE-AUDIT",
}


def require(condition: bool, message: str) -> None:
    if not condition:
        raise AssertionError(message)


def validate_accepted() -> dict:
    artifact = validate_alternative_memory_manifest_file(ACCEPTED)
    require(artifact["kind"] == "alternative-memory-provider-artifact", "artifact kind mismatch")
    require(artifact["provider_declaration"]["kind"] == ":memory-system", "provider declaration missing")
    require(artifact["allocation_strategy_record"], "allocation strategy missing")
    require(artifact["lifetime_alias_ownership_region_escape_facts"], "memory facts missing")
    require(artifact["unsafe_boundary_audit_records"], "unsafe audit records missing")
    require(artifact["layout_alignment_metadata"], "layout metadata missing")
    require(artifact["runtime_check_records"], "runtime checks missing")
    require(artifact["leak_resource_release_evidence"], "release evidence missing")
    require(artifact["device_mmio_ffi_maps"], "device/mmio/ffi maps missing")
    require(artifact["provider_conformance_report"]["status"] == ":passed", "conformance did not pass")
    return artifact


def validate_negative_fixtures() -> list[dict[str, str]]:
    diagnostics = []
    for filename, expected in NEGATIVE_FIXTURES.items():
        path = FIXTURE_DIR / filename
        diagnostic = alternative_memory_manifest_diagnostic(path)
        require(diagnostic is not None, f"{filename} did not produce a diagnostic")
        require(diagnostic["id"] == expected, f"{filename} produced {diagnostic['id']} instead of {expected}")
        for key in ["provider_id", "active_profile", "span", "generated_origin_chain", "memory_family", "lifetime_or_region", "capability_scope", "required_proof_or_check"]:
            require(key in diagnostic, f"{filename} diagnostic missing {key}")
        diagnostics.append({"fixture": str(path.relative_to(ROOT)), "diagnostic": diagnostic["id"]})
    return diagnostics


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--artifact-out", type=Path)
    parser.add_argument("--coverage-out", type=Path)
    args = parser.parse_args()

    try:
        artifact = validate_accepted()
        diagnostics = validate_negative_fixtures()
    except AssertionError as exc:
        print(f"L18 alternative memory validation failed: {exc}", file=sys.stderr)
        return 1

    if args.artifact_out:
        atomic_write_json(args.artifact_out, artifact)

    coverage = {
        "kind": "l18-document-coverage",
        "document": "L18",
        "accepted": [
            {
                "fixture": str(ACCEPTED.relative_to(ROOT)),
                "artifact_kind": artifact["kind"],
                "coverage": [
                    "memory provider declaration",
                    "allocation strategy",
                    "lifetime and alias facts",
                    "unsafe boundary audits",
                    "layout metadata",
                    "runtime checks",
                    "release evidence",
                    "device, MMIO, and FFI maps",
                    "conformance report",
                ],
            }
        ],
        "rejected": diagnostics,
    }
    if args.coverage_out:
        atomic_write_json(args.coverage_out, coverage)

    print("L18 alternative memory validation passed: 1 accepted artifact, 13 rejected diagnostics")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
