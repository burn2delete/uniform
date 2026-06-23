# SAFE16 - Safety Conformance Test Plan

Sequence: 45
Phase: 2 - Safety
Status: Manual Draft 3
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

The safety conformance suite proves that an implementation enforces `SAFE1`
through `SAFE15`. It must test acceptance, rejection, runtime checks, unsafe
audit records, generated-code provenance, certificate preservation, profile
policy, backend preservation, and package safety.

This document defines the fixture format, required families, expected outcomes,
artifact inspections, profile matrix, optimization checks, and conformance
reporting.

## Requirements

- Every SAFE rule family must have positive fixtures, negative fixtures, and
  artifact-inspection fixtures.
- Fixtures must state expected outcome: accept, reject, runtime check, unsafe
  island, or certificate-backed proof.
- Diagnostics must be matched by rule id and source span, not by brittle wording.
- Runtime-check fixtures must inspect emitted check records.
- Unsafe fixtures must inspect audit records.
- Optimization fixtures must inspect `SAFE15` proof or certificate records.
- Profile fixtures must prove that behavior accepted in one profile is rejected
  or narrowed in another when appropriate.

## Dependencies

- `SAFE1` defines the expected outcome model.
- `SAFE2` through `SAFE14` define fixture families.
- `SAFE15` defines proof and certificate artifacts to inspect.
- `L1` through `L19` define source, macro, type, effect, memory, capability, and
  interop behavior used by safety fixtures.
- Profile documents define the profile matrix.
- Compiler, backend, package, and testing phases define runner integration.

## Outputs and Artifacts

- Fixture corpus.
- Expected-outcome manifests.
- Diagnostic match records.
- Runtime check inspection records.
- Unsafe audit inspection records.
- Certificate inspection records.
- Profile matrix reports.
- Backend preservation reports.
- Safety conformance summary.

## Fixture Format

Each fixture has a manifest:

```clojure
{:fixture/id :safe2/use-after-release
 :document SAFE2
 :profile :native
 :target :generic
 :source "fixtures/safe2/use-after-release.gravity"
 :expected {:verdict :reject
            :diagnostic :SAFE2-USE-AFTER-RELEASE
            :span "use-after-release.gravity:9:5"}
 :artifacts []}
```

Accepted fixtures may specify artifacts:

```clojure
{:fixture/id :safe9/bounds-check-erased
 :expected {:verdict :accept
            :proof :SAFE15
            :erased-check :bounds}}
```

The fixture `:expected :verdict` is the compiler-level decision for the whole
fixture, such as `:accept` or `:reject`. Artifact inspections still use the
`SAFE1` operation outcome tags `:proven-safe`, `:runtime-checked`,
`:rejected`, and `:unsafe-island`.

The runner compares structured diagnostics and artifacts. It does not require
exact diagnostic prose.

## Required Fixture Families

`SAFE1` fixtures cover outcome classification: proven-safe, runtime-checked,
rejected, unsafe island, missing outcome, generated-code origin, dependency
safety mode, and optimization proof.

`SAFE2` fixtures cover uninitialized read, bounds, lifetime, escape, aliasing,
allocator mismatch, use after release, double release, raw memory, and profile
memory regime rejection.

`SAFE3` fixtures cover use after move, use after consume, immutable borrow,
mutable borrow, borrow escape, task capture, FFI ownership, and runtime borrow
checks.

`SAFE4` fixtures cover region escape, nested regions, arena reset, cleanup,
task escape, and foreign retention.

`SAFE5` fixtures cover leak, double close, use after close, branch cleanup,
transfer, capture, provider mismatch, cancellation cleanup, and generated linear
duplication.

`SAFE6` fixtures cover forbidden unsafe, missing metadata, missing invariant,
safe wrapper linkage, generated unsafe code, dependency unsafe summaries, and
review invalidation.

`SAFE7` fixtures cover incomplete FFI declarations, raw calls, type mapping,
ownership, error translation, callbacks, host bridges, and generated bindings.

`SAFE8` fixtures cover data races, task capture, ownership transfer, immutable
sharing, locks, atomics, actors, channels, workflow replay, and backend lowering.

`SAFE9` fixtures cover overflow, divide by zero, shifts, narrowing, floating
modes, elementary-function domains, approximation evidence, relaxed modes, and
numeric optimization.

`SAFE10` fixtures cover missing capabilities, scope denial, ambient authority,
phase mismatch, secret leaks, attenuation, revocation, and runtime authority
failure.

`SAFE11` fixtures cover tainted sinks, validator contracts, residual constraints,
parameterization, unsafe deserialization, prompt injection, secret leaks,
generated-code taint, and interop taint.

`SAFE12` fixtures cover macro build effects, generated unsafe code, hygiene,
phase separation, taint propagation, facet macro output, and alternative macro
engines.

`SAFE13` fixtures cover model effects, tool capabilities, tool schemas, prompt
injection, human-review gates, secret leaks, generated code, replay, retention, and
destructive tools.

`SAFE14` fixtures cover package manifests, build effects, runtime capabilities,
lockfiles, unsafe summaries, native dependencies, generated artifacts,
signatures, authority diffs, and postinstall execution.

`SAFE15` fixtures cover proof records, certificate schemas, trust policy,
invalidation, imported certificates, check erasure, proof providers, manual
review limits, and backend proof preservation.

## Profile Matrix

The suite runs representative fixtures under:

- `:core`
- `:hosted`
- `:native`
- `:firmware`
- `:kernel`
- `:hardware`
- `:distributed`
- `:ai`
- `:meta`

Not every fixture is valid for every profile. The manifest states whether the
profile should accept, reject, narrow, or delegate behavior.
Workflow-specific fixtures run under `:distributed` with workflow facets,
targets, and artifacts enabled; they do not introduce a separate profile.

## Artifact Inspections

The suite inspects artifacts for:

- Safety outcome records.
- Runtime checks.
- Unsafe island records.
- Source and generated-origin mappings.
- Capability and effect records.
- Taint flow records.
- Linear resource flow records.
- Region and lifetime records.
- Atomic memory-order records.
- Numeric mode records.
- Certificate records.
- Package safety metadata.

A compiler that emits correct diagnostics but wrong artifacts fails conformance.

## Optimization and Backend Tests

Optimization fixtures compile in at least two modes:

- Checks retained.
- Optimizations enabled.

The optimized mode must show proof records for erased checks and backend
preservation records for memory, numeric, atomic, capability, and FFI facts. If
the backend cannot preserve a fact, the expected outcome is rejection or retained
check.

## Reporting

Conformance reports include:

- Compiler id and version.
- Profile and target.
- Fixture id.
- Expected outcome.
- Actual outcome.
- Diagnostic ids.
- Artifact ids inspected.
- Proof or certificate ids.
- Unsafe island ids.
- Pass/fail state.

Reports are machine-readable so package publication and release gates can depend
on them.

## Diagnostics

SAFE16 diagnostics use these identifiers:

- `SAFE16-FIXTURE` for malformed fixture manifests.
- `SAFE16-OUTCOME` for expected and actual outcome mismatch.
- `SAFE16-DIAGNOSTIC` for missing or wrong diagnostic id or span.
- `SAFE16-ARTIFACT` for missing or invalid required artifacts.
- `SAFE16-PROFILE` for profile matrix mismatch.
- `SAFE16-CERTIFICATE` for missing proof or certificate inspection.
- `SAFE16-BACKEND` for backend preservation mismatch.
- `SAFE16-REPORT` for invalid conformance report output.

Diagnostics must include fixture id, document id, profile, target, expected
outcome, actual outcome, and missing artifact or diagnostic.

## Rejected Designs

Gravity rejects conformance suites that only test successful programs.

Gravity rejects diagnostics matched only by prose.

Gravity rejects safety conformance without artifact inspection.

Gravity rejects profile-agnostic safety tests for profile-specific behavior.

Gravity rejects optimization tests that do not verify proof records for erased
checks.

## Conformance Criteria

A conforming safety conformance suite must demonstrate:

- Fixture families for `SAFE1` through `SAFE15`.
- Structured expected outcomes.
- Diagnostic id and source-span matching.
- Artifact inspection for checks, unsafe islands, generated origins,
  capabilities, taint, proofs, and package metadata.
- Profile matrix execution.
- Optimization and backend preservation tests.
- Machine-readable conformance reports.
