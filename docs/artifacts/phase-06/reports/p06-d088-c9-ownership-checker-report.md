# P06-D088 C9 Ownership Checker Proof Report

Date: 2026-06-25
Task: `P06-D088`
Status: complete (stage0 C9 ownership, lifetime, and region checker document coverage)

## Governing Documents Read

- `docs/phase-06-compiler-architecture/README.md`
- `docs/phase-06-compiler-architecture/IMPLEMENTATION-ROADMAP.md`
- `docs/phase-06-compiler-architecture/088-c9-ownership-lifetime-and-region-checker-design.md`
- `docs/phase-01-core-language/020-l10-memory-model-specification.md`
- `docs/phase-02-safety/031-safe2-memory-safety-model.md`
- `docs/phase-02-safety/032-safe3-ownership-borrowing-and-lifetimes.md`
- `docs/phase-02-safety/033-safe4-region-and-arena-safety.md`
- `docs/phase-02-safety/034-safe5-linear-resource-safety.md`
- `docs/phase-02-safety/037-safe8-concurrency-and-data-race-safety.md`
- `docs/phase-06-compiler-architecture/087-c8-effect-checker-design.md`

## Implemented Surface

- `bootstrap/clojure/src/gravity/bootstrap.clj`
- `bootstrap/clojure/test/gravity/bootstrap_test.clj`
- `bootstrap/clojure/fixtures/accepted/compiler-c9-ownership-checker.gravity`
- rejected `bootstrap/clojure/fixtures/rejected/compiler-c9-*.gravity`
- `docs/artifacts/phase-06/compiler/stage0-p06-d088-c9-ownership-checker-proof.edn`

The `compiler-c9-ownership-check` command emits
`:gravity/stage0-c9-ownership-checker-artifact` from the C8 effect checker
artifact. It contains an ownership graph, borrow graph, lifetime interval map,
move and consume records, escape-analysis report, region lifetime graph, arena
generation graph, linear resource flow graph, transfer records, runtime check
records, unsafe audit references, verifier report, conformance results, and
capability-based proof.

## Validation

```text
clojure -M:gravity compiler-c9-ownership-check bootstrap/clojure/fixtures/accepted/compiler-c9-ownership-checker.gravity
```

Artifact summary:

```text
{:kind :gravity/stage0-c9-ownership-checker-artifact,
 :task "P06-D088",
 :status :complete,
 :owners 76,
 :borrow-edges 5,
 :lifetimes 9,
 :moves 1,
 :consumes 1,
 :regions 2,
 :arenas 1,
 :linear-resources 2,
 :transfers 4,
 :runtime-checks 4,
 :unsafe-audits 2,
 :rejected-designs 12,
 :proof :complete}
```

Artifact hash:

```text
sha256:936e57abb689b6b5645a77aee462071c2de9d61ba2c13ae9612a54179fb438d8
```

```text
clojure -M:test
Ran 61 tests containing 3239 assertions.
0 failures, 0 errors.
```

```text
clojure -M -e <phase-06 compiler EDN parse>
parsed 15 phase-06 compiler EDN proof files
```

```text
git diff --check
passed
```

## Rejected Diagnostics

- `C9-USE-AFTER-MOVE`
- `C9-USE-AFTER-CONSUME`
- `C9-BORROW-ESCAPE`
- `C9-MUT-ALIAS`
- `C9-MOVE-WHILE-BORROWED`
- `C9-REGION-ESCAPE`
- `C9-ARENA-GENERATION`
- `C9-LINEAR-LEAK`
- `C9-LINEAR-DOUBLE`
- `C9-TRANSFER`
- `C9-RUNTIME-CHECK`
- `C9-UNSAFE`

## Proof Records

- `docs/artifacts/phase-06/compiler/stage0-p06-d088-c9-ownership-checker-proof.edn`

## Remaining Limits

This completes `P06-D088` for the Clojure stage0 C9 ownership, lifetime, and
region checker document boundary only. It does not claim production ownership
inference for the full future language, complete safety classification, MIR
construction, backend lowering, release readiness, or self-hosting.
