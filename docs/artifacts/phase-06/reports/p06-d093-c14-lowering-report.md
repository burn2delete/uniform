# P06-D093 C14 Target Lowering Proof Report

Date: 2026-06-25
Task: `P06-D093`
Status: complete (stage0 C14 target lowering document coverage)

## Governing Documents Read

- `docs/phase-06-compiler-architecture/README.md`
- `docs/phase-06-compiler-architecture/IMPLEMENTATION-ROADMAP.md`
- `docs/phase-06-compiler-architecture/093-c14-target-lowering-architecture.md`
- `docs/phase-06-compiler-architecture/092-c13-mir-optimization-passes-design.md`

## Implemented Surface

- `bootstrap/clojure/src/gravity/bootstrap.clj`
- `bootstrap/clojure/test/gravity/bootstrap_test.clj`
- `bootstrap/clojure/fixtures/accepted/compiler-c14-lowering.gravity`
- reused rejected `bootstrap/clojure/fixtures/rejected/compiler-lowering-*.gravity`
- `docs/artifacts/phase-06/compiler/stage0-p06-d093-c14-lowering-proof.edn`

The `compiler-c14-lowering` command emits
`:gravity/stage0-c14-target-lowering-artifact` from the current C13 optimized
MIR artifact. It records the lowering request, target eligibility report, ABI
manifest, runtime/provider manifest, provider selections, layout decision,
proof-to-target metadata map, source/generated-origin map, capability
preservation report, unsupported-feature/fallback records, target artifact
manifest, diagnostic catalog, conformance results, and capability-based proof.

## Validation

```text
clojure -M:gravity compiler-c14-lowering bootstrap/clojure/fixtures/accepted/compiler-c14-lowering.gravity
```

Artifact summary:

```text
{:kind :gravity/stage0-c14-target-lowering-artifact,
 :task "P06-D093",
 :status :complete,
 :provider-records 3,
 :target-metadata 3,
 :target-artifacts 1,
 :unsupported-feature-records 1,
 :diagnostics 10,
 :rejected-designs 10,
 :proof :complete}
```

Artifact hash:

```text
sha256:c641c11d84d3c059d602c365f16cf8055317a197e943d28d85b74d427a25a7b8
```

```text
clojure -M:test
Ran 66 tests containing 3554 assertions.
0 failures, 0 errors.
```

```text
clojure -M -e <phase-06 compiler EDN parse>
parsed 20 phase-06 compiler EDN proof files
```

```text
/Users/mattr/.cache/codex-runtimes/codex-primary-runtime/dependencies/python/bin/python3 tools/validate_gravity_docs.py
validation passed: 240 docs, 18 phase indexes, ASCII, no placeholders
```

```text
git diff --check
passed
```

## Rejected Diagnostics

- `C14-INPUT`
- `C14-PROFILE`
- `C14-TARGET`
- `C14-ABI`
- `C14-RUNTIME`
- `C14-PROVIDER`
- `C14-PROOF-METADATA`
- `C14-CAPABILITY`
- `C14-UNSUPPORTED`
- `C14-MANIFEST`

## Proof Records

- `docs/artifacts/phase-06/compiler/stage0-p06-d093-c14-lowering-proof.edn`

## Remaining Limits

This completes `P06-D093` for the Clojure stage0 C14 target lowering document
boundary only. It does not claim backend code generation, release readiness, or
self-hosting.
