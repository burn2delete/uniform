#!/usr/bin/env python3
"""Validate L12 compile-time evaluation coverage."""

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

from gravity.compile_time import analyze_compile_time_source, compile_time_diagnostic  # noqa: E402


FIXTURE_DIR = ROOT / "docs/artifacts/phase-01/fixtures/l12"
ACCEPTED = FIXTURE_DIR / "accepted-compile-time.gravity"
NEGATIVE_FIXTURES = {
    "rejected-pure-effect.gravity": "L12-PURE-EFFECT",
    "rejected-build-grant.gravity": "L12-BUILD-GRANT",
    "rejected-hermetic-input.gravity": "L12-HERMETIC-INPUT",
    "rejected-nondeterminism.gravity": "L12-NONDETERMINISM",
    "rejected-const-representation.gravity": "L12-CONST-REPRESENTATION",
    "rejected-generated-illegal.gravity": "L12-GENERATED-ILLEGAL",
    "rejected-phase-capture.gravity": "L12-PHASE-CAPTURE",
    "rejected-cache-unsafe.gravity": "L12-CACHE-UNSAFE",
    "rejected-secret-leak.gravity": "L12-SECRET-LEAK",
    "rejected-fuel.gravity": "L12-FUEL",
}


def require(condition: bool, message: str) -> None:
    if not condition:
        raise AssertionError(message)


def validate_accepted() -> dict:
    artifact = analyze_compile_time_source(ACCEPTED.read_text(encoding="utf-8"), str(ACCEPTED.relative_to(ROOT)))
    require(artifact["kind"] == "compile-time-evaluation-record", "artifact kind mismatch")
    require(artifact["hermetic_mode"] is True, "accepted artifact must be hermetic")
    require(len(artifact["constant_table"]) == 2, "expected two compile-time constants")
    require(any(record["effect"] == ":build/read-file" for record in artifact["build_effect_log"]), "read-file build effect missing")
    require(any(record["effect"] == ":build/env" for record in artifact["build_effect_log"]), "env build effect missing")
    require(any(record["effect"] == ":build/write-artifact" for record in artifact["build_effect_log"]), "write-artifact build effect missing")
    require(artifact["file_input_digests"], "file input digest missing")
    require(artifact["output_artifact_digests"], "output artifact digest missing")
    require(artifact["environment_key_reads"][0]["digest"] == "redacted", "secret-capable environment read was not redacted")
    require(artifact["generated_origin_chains"], "generated origin chain missing")
    require(artifact["generated_forms"], "generated form record missing")
    require(artifact["cache_key"].startswith("sha256:"), "cache key missing")
    return artifact


def validate_negative_fixtures() -> list[dict[str, str]]:
    diagnostics = []
    for filename, expected in NEGATIVE_FIXTURES.items():
        path = FIXTURE_DIR / filename
        diagnostic = compile_time_diagnostic(path.read_text(encoding="utf-8"), str(path.relative_to(ROOT)))
        require(diagnostic is not None, f"{filename} did not produce a diagnostic")
        require(diagnostic["id"] == expected, f"{filename} produced {diagnostic['id']} instead of {expected}")
        for key in ["active_phase", "profile", "target", "span", "generated_origin_chain", "requested_effect", "relevant_grant", "remediation"]:
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
        print(f"L12 compile-time validation failed: {exc}", file=sys.stderr)
        return 1

    if args.artifact_out:
        atomic_write_json(args.artifact_out, artifact)

    coverage = {
        "kind": "l12-document-coverage",
        "document": "L12",
        "accepted": [
            {
                "fixture": str(ACCEPTED.relative_to(ROOT)),
                "artifact_kind": artifact["kind"],
                "coverage": [
                    "pure compile-time constants",
                    "authorized build effects with grants",
                    "hermetic file and environment inputs",
                    "generated-form provenance",
                    "secret redaction",
                    "cache key and reuse decision",
                ],
            }
        ],
        "rejected": diagnostics,
    }
    if args.coverage_out:
        atomic_write_json(args.coverage_out, coverage)

    print("L12 compile-time validation passed: 1 accepted artifact, 10 rejected diagnostics")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
