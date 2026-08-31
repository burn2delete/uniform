# P06-D085 C6 Core Lowering Proof Report

Date: 2026-06-25
Task: `P06-D085`
Status: complete (stage0 C6 AST and core lowering document coverage)

## Governing Documents Read

- `docs/phase-06-compiler-architecture/README.md`
- `docs/phase-06-compiler-architecture/085-c6-ast-and-core-lowering-design.md`
- `docs/phase-01-core-language/011-l1-surface-syntax-specification.md`
- `docs/phase-01-core-language/012-l2-core-language-semantics.md`
- `docs/phase-01-core-language/017-l7-pattern-matching-specification.md`
- `docs/phase-01-core-language/019-l9-error-handling-specification.md`
- `docs/phase-06-compiler-architecture/083-c4-macro-expansion-engine-design.md`
- `docs/phase-06-compiler-architecture/084-c5-name-resolution-and-namespace-analyzer-design.md`
- `docs/phase-06-compiler-architecture/091-c12-domain-ir-architecture.md`

## Implemented Surface

- `bootstrap/clojure/src/gravity/bootstrap.clj`
- `bootstrap/clojure/test/gravity/bootstrap_test.clj`
- `bootstrap/clojure/fixtures/accepted/compiler-c6-core-lowering.gravity`
- rejected `bootstrap/clojure/fixtures/rejected/compiler-c6-*.gravity`
- `docs/artifacts/phase-06/compiler/stage0-p06-d085-c6-core-lowering-proof.edn`

The `compiler-c6-lowering` command emits
`:gravity/stage0-c6-core-lowering-artifact` from the C5 namespace analysis
artifact. It contains a core AST module, core-node table, surface-to-core map,
desugaring trace, evaluation-order records, domain-boundary records, core
verifier report, versioned lowering-rule invalidation record, conformance
results, and capability-based proof.

## Validation

```text
clojure -M:gravity compiler-c6-lowering bootstrap/clojure/fixtures/accepted/compiler-c6-core-lowering.gravity
```

Artifact summary:

```text
{:kind :gravity/stage0-c6-core-lowering-artifact,
 :task "P06-D085",
 :status :complete,
 :core-nodes 53,
 :core-roots 6,
 :surface-map-entries 7,
 :evaluation-order-records 24,
 :domain-boundaries 1,
 :rejected-designs 8,
 :proof :complete}
```

Artifact hash:

```text
sha256:baee4e97293095597550832e56e1a67db8a2f06428fe5bf4ae17cdfbf05a554f
```

```text
clojure -M:test
Ran 58 tests containing 3065 assertions.
0 failures, 0 errors.
```

```text
clojure -M -e <phase-06 compiler EDN parse>
parsed 12 phase-06 compiler EDN proof files
```

```text
git diff --check
passed
```

## Rejected Diagnostics

- `C6-LOWERING-GAP`
- `C6-CORE-SHAPE`
- `C6-EVAL-ORDER`
- `C6-ORIGIN`
- `C6-EFFECT-DROP`
- `C6-UNSAFE-DROP`
- `C6-DOMAIN-BOUNDARY`
- `C6-VERIFY`

## Proof Records

- `docs/artifacts/phase-06/compiler/stage0-p06-d085-c6-core-lowering-proof.edn`

## Remaining Limits

This completes `P06-D085` for the Clojure stage0 C6 AST and core lowering
document boundary only. It does not claim production lowering coverage for
every future surface form, complete type checking, MIR construction, backend
lowering, release readiness, or self-hosting.
