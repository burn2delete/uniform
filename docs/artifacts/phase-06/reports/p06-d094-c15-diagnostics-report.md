# P06-D094 C15 Compiler Diagnostics Proof Report

Date: 2026-06-25
Task: `P06-D094`
Status: complete (stage0 C15 compiler diagnostics document coverage)

## Governing Documents Read

- `docs/phase-06-compiler-architecture/README.md`
- `docs/phase-06-compiler-architecture/IMPLEMENTATION-ROADMAP.md`
- `docs/phase-06-compiler-architecture/094-c15-compiler-diagnostics-specification.md`
- `docs/phase-06-compiler-architecture/093-c14-target-lowering-architecture.md`

## Implemented Surface

- `bootstrap/clojure/src/gravity/bootstrap.clj`
- `bootstrap/clojure/test/gravity/bootstrap_test.clj`
- `bootstrap/clojure/fixtures/accepted/compiler-c15-diagnostics.gravity`
- reused rejected `bootstrap/clojure/fixtures/rejected/compiler-verify-c15-*.gravity`
- `docs/artifacts/phase-06/compiler/stage0-p06-d094-c15-diagnostics-proof.edn`

The `compiler-c15-diagnostics` command emits
`:gravity/stage0-c15-compiler-diagnostics-artifact` from the current C14 target
lowering artifact. It records a diagnostic schema, deterministic diagnostic
stream, diagnostic catalog, related-span map, remediation and quick-fix records,
redaction report, CLI/IDE/CI/safety/package rendering records, golden diagnostic
fixtures, conformance results, and capability-based proof.

## Validation

```text
clojure -M:gravity compiler-c15-diagnostics bootstrap/clojure/fixtures/accepted/compiler-c15-diagnostics.gravity
```

Artifact summary:

```text
{:kind :gravity/stage0-c15-compiler-diagnostics-artifact,
 :task "P06-D094",
 :status :complete,
 :structured-diagnostics 4,
 :catalog-rules 9,
 :quick-fixes 9,
 :renderers 5,
 :golden-fixtures 9,
 :rejected-designs 9,
 :proof :complete}
```

Artifact hash:

```text
sha256:a0815c5ae472679476b6c6879fb9e749ec64015b99bc4eedc7edce52becab401
```

```text
clojure -M:test
Ran 67 tests containing 3614 assertions.
0 failures, 0 errors.
```

```text
clojure -M -e <phase-06 compiler EDN parse>
parsed 21 phase-06 compiler EDN proof files
```

```text
git diff --check
passed
```

## Rejected Diagnostics

- `C15-SCHEMA`
- `C15-ID`
- `C15-SPAN`
- `C15-ORIGIN`
- `C15-FACTS`
- `C15-REMEDIATION`
- `C15-REDACTION`
- `C15-ORDER`
- `C15-GOLDEN`

## Proof Records

- `docs/artifacts/phase-06/compiler/stage0-p06-d094-c15-diagnostics-proof.edn`

## Remaining Limits

This completes `P06-D094` for the Clojure stage0 C15 compiler diagnostics
document boundary only. It does not claim production diagnostic localization,
release readiness, or self-hosting.
