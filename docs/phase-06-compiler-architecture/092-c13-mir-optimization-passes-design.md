# C13 - MIR Optimization Passes Design

Sequence: 92
Phase: 6 - Compiler Architecture
Status: Manual Draft 2
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

MIR optimization improves Gravity programs while preserving checked semantics,
effects, capabilities, ownership, safety outcomes, source maps, profiles, and
proof obligations. Optimization is a verified transformation pipeline, not a
place where backend assumptions or undefined behavior enter the language.

This document defines optimization pass contracts, decision records, pass
ordering, invalidation, verifier gates, residual cost reporting, and proof
requirements.

## Requirements

- Every optimization pass must declare input IR, output IR, required analyses,
  preserved facts, invalidated facts, regenerated facts, proof obligations,
  profile constraints, target assumptions, and emitted artifacts.
- Pass order must be deterministic for a given source, compiler, profile,
  target, feature set, package graph, and build inputs.
- A pass that invalidates type, effect, ownership, capability, safety, math, or
  profile facts must regenerate them, keep residual checks, or reject the
  transformation.
- Check elision must follow `PERF10`.
- Performance ranking and benchmarks may choose between legal variants but are
  not correctness evidence.
- Optimized MIR must pass the MIR verifier after each pass that changes control
  flow, data flow, effects, safety facts, ownership facts, or domain anchors.
- Optimization records must be stable enough for diagnostics, conformance, and
  self-hosting comparison.

## Dependencies

- `C11` defines MIR and the MIR verifier.
- `C12` defines domain IR anchors.
- `PERF1` through `PERF10` define performance evidence.
- `SAFE15` defines proof and certificate artifacts.
- `MATH10` defines elementary-function optimization strategy.
- Backend documents define target feature assumptions.
- Testing and self-hosting documents consume optimization decision logs.

## Outputs and Artifacts

- Pass registry.
- Pass pipeline manifest.
- Optimization decision log.
- Invalidated-fact ledger.
- Analysis cache records.
- Proof and certificate usage records.
- Residual cost report.
- Post-pass verifier reports.
- Optimization diagnostics.

## Pass Contract

```clojure
{:artifact :gravity/mir-pass-contract
 :pass :bounds-check-elide
 :input :gravity/mir
 :output :gravity/mir
 :requires #{:dominator-tree :range-analysis :safety-outcomes}
 :preserves #{:types :effects :source-origins :profile}
 :invalidates #{:dominator-tree :data-flow-cache}
 :regenerates #{:runtime-check-table}
 :proof-obligations #{:proof-dominates-check}
 :profiles #{:native :hosted :gpu}
 :target-assumptions #{}
 :emits #{:decision-log :check-elision-record :verifier-report}}
```

Pass contracts are checked before pass execution and after pass output.

## Standard Pass Families

Standard MIR pass families include:

- constant folding and propagation,
- dead code elimination,
- common subexpression elimination,
- inlining,
- specialization and partial evaluation,
- closure conversion preparation,
- allocation sinking and escape-based stack or region placement,
- region and arena optimization,
- bounds, overflow, null, initialization, and lifetime check retention or
  elision,
- branch simplification and match decision optimization,
- protocol and generic dispatch specialization,
- loop canonicalization and unrolling,
- vectorization preparation,
- effect-aware scheduling,
- domain IR entry and exit,
- EFIR fusion and math provider selection,
- resource cleanup simplification,
- target-independent layout preparation.

Target-specific instruction selection is not a MIR optimization pass; it belongs
to target lowering.

## Decision Record

```clojure
{:artifact :gravity/optimization-decision
 :pass :inline-small-fn
 :decision-id decision-hash
 :input-mir input-hash
 :output-mir output-hash
 :changed-ops [op-1 op-2]
 :reason :size-threshold
 :preserved #{:types :effects :source-origins :safety-outcomes}
 :invalidated #{:dominator-tree}
 :proofs-used []
 :residual-checks [check-id]
 :benchmarks []
 :verifier-result :passed}
```

Every pass emits a decision record even when it makes no changes.

## Invalidation Ledger

The invalidation ledger records:

- analysis invalidated,
- facts invalidated,
- passes that must rerun,
- proofs or certificates invalidated,
- runtime checks restored,
- caches cleared,
- diagnostics affected.

Invalidation is conservative. If the compiler cannot prove a fact remains valid,
the fact is invalidated.

## Verification and Translation Validation

Post-pass verification checks MIR structure. High-risk passes also require one
of:

- local proof,
- translation validation,
- differential testing fixture,
- certificate checker,
- domain verifier,
- conformance acceptance for nonsemantic differences.

Optimization records name which evidence was used.

## Residual Cost Reporting

When a pass is expected to erase abstraction cost, it emits a residual cost
report compatible with `PERF2`. Residual work can be acceptable, but it must be
visible when a user or library claims zero-cost behavior.

## Determinism and Reproducibility

Optimization pipelines record:

- pass order,
- pass versions,
- optimization level,
- source hashes,
- profile and target,
- target feature set,
- provider set,
- random seeds if any,
- benchmark or autotuning inputs,
- proof artifacts.

Nondeterministic search must emit a replay record or be rejected in hermetic
builds.

## Diagnostics

Optimization diagnostics use `C13` identifiers:

- `C13-CONTRACT` for invalid pass contracts.
- `C13-PRESERVE` for facts claimed preserved but missing or changed.
- `C13-INVALIDATE` for missing invalidation records.
- `C13-PROOF` for transformations without required proof.
- `C13-CHECK-ELISION` for check removal outside `PERF10`.
- `C13-EFFECT` for illegal effect reordering.
- `C13-SAFETY` for stale safety outcomes after optimization.
- `C13-DOMAIN` for invalid domain anchor transformations.
- `C13-NONDETERMINISM` for unreplayable optimization choice.
- `C13-VERIFY` for post-pass verifier failure.

Diagnostics must include pass id, decision id, input and output MIR ids, source
span when available, changed operations, missing fact, proof id, profile, target,
and remediation.

## Rejected Designs

Gravity rejects optimization based on undefined behavior.

Gravity rejects pass pipelines with hidden or nondeterministic ordering.

Gravity rejects check erasure without proof records.

Gravity rejects effect reordering without effect evidence.

Gravity rejects optimization logs that cannot explain source-visible changes.

## Conformance Criteria

A conforming MIR optimizer must demonstrate:

- pass registry and contract validation,
- deterministic pass pipeline manifests,
- decision records for changed and unchanged passes,
- verifier execution after mutating passes,
- invalidation and regeneration of analyses and proofs,
- accepted and rejected check elision,
- preservation of source and generated origins,
- residual-cost reporting,
- translation validation or proof for high-risk passes,
- diagnostics for stale facts and illegal transformations.
