# P01-D015 L5 Document Coverage Report

Date: 2026-06-24
Agent: Codex
Status: complete with Clojure stage0 capability proof

## Governing Document Read

- `docs/phase-01-core-language/015-l5-type-system-specification.md`

## Implementation Surface

- `bootstrap/clojure/src/gravity/bootstrap.clj`
- `bootstrap/clojure/test/gravity/bootstrap_test.clj`
- `bootstrap/clojure/fixtures/accepted/typed-core.gravity`
- `bootstrap/clojure/fixtures/rejected/typed-type-mismatch.gravity`
- `bootstrap/clojure/fixtures/rejected/typed-dynamic-forbidden.gravity`
- `bootstrap/clojure/fixtures/rejected/typed-annotation-required.gravity`
- `bootstrap/clojure/fixtures/rejected/typed-cast-unsafe.gravity`
- `bootstrap/clojure/fixtures/rejected/typed-implicit-cast.gravity`
- `bootstrap/clojure/fixtures/rejected/typed-host-null.gravity`
- `bootstrap/clojure/fixtures/rejected/typed-uninit-read.gravity`
- `bootstrap/clojure/fixtures/rejected/typed-linear-dup.gravity`
- `bootstrap/clojure/fixtures/rejected/typed-schema-weaken.gravity`
- `bootstrap/clojure/fixtures/rejected/typed-latent-effect-missing.gravity`
- `docs/artifacts/phase-01/typed/stage0-l5-document-coverage-proof.edn`

## Accepted Evidence

The accepted fixture emits a typed/effected core artifact with 229 type facts,
all L5 required type categories covered, no missing categories, function
signatures with latent effects, dynamic boundary records, one
runtime-checked dynamic cast record, schema identity and validation-preservation
links, generic instantiation records, ownership/resource type facts, and 229
MIR type-preservation handoff records.

The MIR records are a stage0 typed-core handoff fixture. They prove L5 type
facts survive into the next artifact boundary but do not claim Phase 06
production MIR completion.

## Rejected Evidence

The stage0 checker rejects the L5 diagnostic surface implemented in Clojure:

- `L5-TYPE-MISMATCH`
- `L5-ANNOTATION-REQUIRED`
- `L5-DYNAMIC-FORBIDDEN`
- `L5-CAST-UNSAFE`
- `L5-UNINIT-READ`
- `L5-LINEAR-DUP`
- `L5-SCHEMA-WEAKEN`
- `L5-LATENT-EFFECT-MISSING`

## Validation

```bash
clojure -M:gravity typed bootstrap/clojure/fixtures/accepted/typed-core.gravity
```

Output includes:

```text
:gravity/stage0-typed-core-artifact
```

Artifact summary:

```text
229 type facts; L5 type-conformance status complete; no missing type categories;
one runtime-checked dynamic cast; schema validation fields preserved; 229 MIR
type-preservation handoff records.
```

```bash
clojure -M:test
```

Output:

```text
Testing gravity.bootstrap-test

Ran 14 tests containing 530 assertions.
0 failures, 0 errors.
clojure bootstrap validation passed: hosted hello, L1 reader artifacts, L2 core artifacts, L3 module artifacts, L4 macro artifacts, L5 typed/effected artifacts, L6 effect-system artifacts, L7 pattern-match artifacts, L8 dispatch artifacts, L9 error-handling artifacts, L10 memory-model artifacts, L11 concurrency artifacts, and 92 rejected fixtures
```

## Conformance Statement

`P01-D015` is complete for the stage0 L5 document-coverage task. This does not
claim later Phase 03 profile matrix completion, Phase 06 production MIR,
backend lowering, release safety, or self-hosting.
