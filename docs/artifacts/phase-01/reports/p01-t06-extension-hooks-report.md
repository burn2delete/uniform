# P01-T06 Extension Hook Evidence Report

Date: 2026-06-24
Agent: Codex
Status: complete

## Governing Inputs Read

- `AGENTS.md`
- `README.md`
- `docs/README.md`
- `docs/source-concepts.md`
- `docs/document-sequence.md`
- `docs/implementation-roadmap.md`
- `docs/phase-00-foundation-and-thesis/002-d1-system-architecture-overview.md`
- `docs/phase-00-foundation-and-thesis/003-d2-implementation-roadmap-and-milestones.md`
- `docs/phase-00-foundation-and-thesis/004-d3-terminology-and-concept-model.md`
- `docs/phase-00-foundation-and-thesis/009-d8-safety-philosophy-and-charter.md`
- `docs/phase-00-foundation-and-thesis/010-d9-verifiability-and-mathematical-correctness-charter.md`
- `docs/phase-01-core-language/README.md`
- `docs/phase-01-core-language/026-l16-alternative-macro-system-contract.md`
- `docs/phase-01-core-language/027-l17-alternative-type-system-contract.md`
- `docs/phase-01-core-language/028-l18-alternative-memory-model-contract.md`
- `docs/phase-01-core-language/029-l19-language-interoperability-and-migration-specification.md`

## Implementation Surface

- `bootstrap/clojure/src/gravity/bootstrap.clj`
- `bootstrap/clojure/test/gravity/bootstrap_test.clj`
- `bootstrap/clojure/fixtures/accepted/alternative-macro.gravity`
- `bootstrap/clojure/fixtures/accepted/alternative-type.gravity`
- `bootstrap/clojure/fixtures/accepted/alternative-memory.gravity`
- `bootstrap/clojure/fixtures/accepted/interop-migration.gravity`
- `bootstrap/clojure/fixtures/rejected/typed-altmacro-*.gravity`
- `bootstrap/clojure/fixtures/rejected/typed-alttype-*.gravity`
- `bootstrap/clojure/fixtures/rejected/typed-altmem-*.gravity`
- `bootstrap/clojure/fixtures/rejected/typed-interop-*.gravity`
- `docs/artifacts/phase-01/extensions/stage0-p01-t06-extension-hooks-proof.edn`

## Accepted Evidence

The Clojure stage0 typed/effected core pass now accepts one constrained
fixture for each extension family:

- alternative macro provider contract through `alternative-macro.gravity`;
- alternative type provider contract through `alternative-type.gravity`;
- alternative memory provider contract through `alternative-memory.gravity`;
- interop and migration boundary contract through `interop-migration.gravity`.

Those artifacts stay inside the normal reader, namespace, macro, core, and
typed/effected pipeline. They preserve source spans, generated-origin chains,
phase boundaries, effect and capability facts, ownership and memory facts,
safety outcomes, profile support, diagnostic mappings, and provenance records.

## Rejected Evidence

The combined L16 through L19 rejected fixture families prove 44 stable
diagnostics:

- L16 rejects provider, equivalence, syntax-object, hygiene, phase,
  build-effect, hermetic, cache, facet, and generated-code violations.
- L17 rejects provider, lowering, soundness, effect erasure, capability
  erasure, ownership fact, gradual boundary, unsafe cast, domain fact, and
  diagnostic mapping violations.
- L18 rejects provider, hidden allocation, lifetime, escape, alias,
  uninitialized read, double release, leak, bounds, device sync, MMIO,
  FFI allocator, and unsafe audit violations.
- L19 rejects incomplete boundaries, unsupported profiles, lossy type maps,
  missing ownership, missing error maps, missing capabilities, missing effects,
  unsafe wrappers without proof, schema drift, migration parity failure, and
  host leakage.

## Validation

```bash
clojure -M:test
```

Output:

```text
Testing gravity.bootstrap-test

Ran 22 tests containing 974 assertions.
0 failures, 0 errors.
clojure bootstrap validation passed: hosted hello, L1 reader artifacts, L2 core artifacts, L3 module artifacts, L4 macro artifacts, L5 typed/effected artifacts, L6 effect-system artifacts, L7 pattern-match artifacts, L8 dispatch artifacts, L9 error-handling artifacts, L10 memory-model artifacts, L11 concurrency artifacts, L12 compile-time artifacts, L13 standard-library artifacts, L14 facet artifacts, L15 provider artifacts, L16 alternative macro artifacts, L17 alternative type artifacts, L18 alternative memory artifacts, L19 interop artifacts, and 176 rejected fixtures
```

## Conformance Statement

P01-T06 is complete for the stage0 Phase 01 capability surface because every
alternative subsystem and interop extension family has an accepted typed-core
artifact, rejected diagnostics, a document-specific proof record, and automated
test coverage. The proof records are:

- `docs/artifacts/phase-01/alternative-macros/stage0-l16-document-coverage-proof.edn`
- `docs/artifacts/phase-01/alternative-types/stage0-l17-document-coverage-proof.edn`
- `docs/artifacts/phase-01/alternative-memory/stage0-l18-document-coverage-proof.edn`
- `docs/artifacts/phase-01/interop/stage0-l19-document-coverage-proof.edn`
- `docs/artifacts/phase-01/extensions/stage0-p01-t06-extension-hooks-proof.edn`

This does not claim a production plugin system, package distribution,
runtime provider loading, backend ABI lowering, governance policy, or
self-hosting. Those remain later-phase roadmap work.
