# P01-D027 L17 Document Coverage Report

Date: 2026-06-24
Agent: Codex
Status: complete

## Governing Document Read

- `docs/phase-01-core-language/027-l17-alternative-type-system-contract.md`
- `docs/phase-01-core-language/015-l5-type-system-specification.md`
- `docs/phase-01-core-language/016-l6-effect-system-specification.md`
- `docs/phase-01-core-language/020-l10-memory-model-specification.md`
- `docs/phase-01-core-language/024-l14-language-facet-system-specification.md`
- `docs/phase-01-core-language/025-l15-capability-provider-specification.md`
- `docs/phase-01-core-language/026-l16-alternative-macro-system-contract.md`

## Implementation Surface

- `bootstrap/clojure/src/gravity/bootstrap.clj`
- `bootstrap/clojure/test/gravity/bootstrap_test.clj`
- `bootstrap/clojure/fixtures/accepted/alternative-type.gravity`
- `bootstrap/clojure/fixtures/rejected/typed-alttype-*.gravity`
- `docs/artifacts/phase-01/alternative-types/stage0-l17-document-coverage-proof.edn`

## Accepted Evidence

The accepted `alternative-type.gravity` fixture is checked through the Clojure
stage0 typed/effected core pass. It records a selected alternative type-system
provider and proves that the provider boundary lowers into the common typed core
without erasing effects, capabilities, ownership facts, domain facts,
diagnostic provenance, or proof references.

The emitted artifact records:

- alternative type provider declaration with profiles, targets, facets,
  features, build effects, capabilities, fact schema, proof schema,
  conformance suite, soundness claim, deterministic selection, typed-core
  metadata, and lockfile evidence;
- typed-core lowering rule preserving expression and binding types, function
  effects, capability requirements, panic/allocation/resource behavior,
  ownership, casts, proof refs, profile assumptions, source spans, and
  generated-code mapping;
- fact export schema covering ownership, regions, linear values,
  initialization, nullability, taint, schema, domain, and capability-value
  facts;
- proof/refinement artifact and runtime-check record for a gradual boundary;
- diagnostic mapping through generated syntax back to source and type facts;
- L5 compatibility report and profile soundness evidence;
- effect and capability preservation record for function types;
- ownership fact export for L10 memory/resource, L11 transfer, and MIR layout
  consumers;
- domain fact export through the L14 facet boundary;
- optimization proof record retaining the proof reference for an erased check;
- complete L17 alternative type conformance.

The current artifact summary is 1 provider declaration, 1 typed-core lowering
rule, 1 fact export schema, 1 proof artifact, 1 runtime-check record, 1
diagnostic mapping record, 1 compatibility report, 1 profile soundness evidence
record, 1 effect/capability preservation record, 1 ownership fact record, 1
gradual boundary record, 1 domain fact record, and 1 optimization proof record.

## Rejected Evidence

The rejected fixtures prove stable diagnostics for:

- `L17-PROVIDER`
- `L17-LOWERING`
- `L17-SOUNDNESS`
- `L17-EFFECT-ERASURE`
- `L17-CAPABILITY-ERASURE`
- `L17-OWNERSHIP-FACT`
- `L17-GRADUAL-BOUNDARY`
- `L17-UNSAFE-CAST`
- `L17-DOMAIN-FACT`
- `L17-DIAGNOSTIC-MAP`

## Validation

```bash
clojure -M:gravity typed bootstrap/clojure/fixtures/accepted/alternative-type.gravity
```

Expected artifact kind:

```text
:gravity/stage0-typed-core-artifact
```

```bash
clojure -M:test
```

Output:

```text
Testing gravity.bootstrap-test

Ran 20 tests containing 837 assertions.
0 failures, 0 errors.
clojure bootstrap validation passed: hosted hello, L1 reader artifacts, L2 core artifacts, L3 module artifacts, L4 macro artifacts, L5 typed/effected artifacts, L6 effect-system artifacts, L7 pattern-match artifacts, L8 dispatch artifacts, L9 error-handling artifacts, L10 memory-model artifacts, L11 concurrency artifacts, L12 compile-time artifacts, L13 standard-library artifacts, L14 facet artifacts, L15 provider artifacts, L16 alternative macro artifacts, L17 alternative type artifacts, and 152 rejected fixtures
```

```bash
clojure -M -e artifact-summary
```

Output:

```text
complete L17 alternative type conformance with provider, typed-core lowering, fact export, proof, runtime-check, diagnostic-map, compatibility, soundness, effect/capability, ownership, gradual-boundary, domain-fact, and optimization-proof records
```

## Residual Risks

This completes the stage0 L17 document task. It does not claim a second
production type solver, broad L5 corpus equivalence, production proof checking,
language-server integration, optimizer deployment of refinement facts, release
readiness, or self-hosting.
