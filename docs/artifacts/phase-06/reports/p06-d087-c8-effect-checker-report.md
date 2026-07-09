# P06-D087 C8 Effect Checker Proof Report

Date: 2026-06-25
Task: `P06-D087`
Status: complete (stage0 C8 effect checker document coverage)

## Governing Documents Read

- `docs/phase-06-compiler-architecture/README.md`
- `docs/phase-06-compiler-architecture/IMPLEMENTATION-ROADMAP.md`
- `docs/phase-06-compiler-architecture/087-c8-effect-checker-design.md`
- `docs/phase-01-core-language/016-l6-effect-system-specification.md`
- `docs/phase-01-core-language/025-l15-capability-provider-specification.md`
- `docs/phase-06-compiler-architecture/086-c7-type-checker-design.md`

## Implemented Surface

- `bootstrap/clojure/src/gravity/bootstrap.clj`
- `bootstrap/clojure/test/gravity/bootstrap_test.clj`
- `bootstrap/clojure/fixtures/accepted/compiler-c8-effect-checker.gravity`
- rejected `bootstrap/clojure/fixtures/rejected/compiler-c8-*.gravity`
- `docs/artifacts/phase-06/compiler/stage0-p06-d087-c8-effect-checker-proof.edn`

The `compiler-c8-effect-check` command emits
`:gravity/stage0-c8-effect-checker-artifact` from the C7 type checker artifact.
It contains an effect graph, function latent effect table, namespace effect
summary, effect legality report, capability proof records, build-effect log,
replay requirements, ordering constraints, residual effect report, verifier
report, conformance results, and capability-based proof.

## Validation

```text
clojure -M:gravity compiler-c8-effect-check bootstrap/clojure/fixtures/accepted/compiler-c8-effect-checker.gravity
```

Artifact summary:

```text
{:kind :gravity/stage0-c8-effect-checker-artifact,
 :task "P06-D087",
 :status :complete,
 :effect-nodes 76,
 :inferred-effects 4,
 :function-effect-summaries 2,
 :legality-records 4,
 :capability-proofs 4,
 :build-effects 1,
 :replay-records 1,
 :ordering-constraints 10,
 :residual-effects 3,
 :rejected-designs 9,
 :proof :complete}
```

Artifact hash:

```text
sha256:7b85db9e074425bc276dc6b7e8e6cd6147902c3c1a2b368dd20710ae13022755
```

```text
clojure -M:test
Ran 60 tests containing 3175 assertions.
0 failures, 0 errors.
```

```text
clojure -M -e <phase-06 compiler EDN parse>
parsed 14 phase-06 compiler EDN proof files
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

- `C8-UNDECLARED`
- `C8-PROFILE`
- `C8-CAPABILITY`
- `C8-BUILD`
- `C8-REPLAY`
- `C8-ORDER`
- `C8-RUNTIME`
- `C8-UNKNOWN`
- `C8-VERIFY`

## Proof Records

- `docs/artifacts/phase-06/compiler/stage0-p06-d087-c8-effect-checker-proof.edn`

## Remaining Limits

This completes `P06-D087` for the Clojure stage0 C8 effect checker document
boundary only. It does not claim production effect inference for the full future
language, ownership checking, safety classification, MIR construction, backend
lowering, release readiness, or self-hosting.
