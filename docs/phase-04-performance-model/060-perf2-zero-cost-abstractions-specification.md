# PERF2 - Zero-Cost Abstractions Specification

Sequence: 60
Phase: 4 - Performance Model
Status: Manual Draft 3
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

An abstraction is zero-cost only when optimized artifacts contain no extra
allocation, boxing, synchronization, reflection, dynamic dispatch, runtime
checks, capability calls, or host services beyond the equivalent handwritten
lower-level form. When residual work remains, Gravity reports it instead of
calling the abstraction zero-cost.

This document defines the evidence required for zero-cost claims and the
abstraction families that must expose residual cost.

## Requirements

- Zero-cost claims must identify equivalent lower-level form and erased costs.
- Optimized IR must preserve source semantics, effects, capabilities, safety
  facts, and profile legality.
- Residual allocation, boxing, dispatch, synchronization, FFI, runtime checks, or
  host calls must be reported.
- Protocols, records, macros, generics, collection views, iterators, facets, and
  standard-library wrappers must publish specialization and dispatch behavior.
- Failure to erase an expected cost is a diagnostic or performance report, not a
  silent success.

## Dependencies

- `PERF1` defines global performance claim evidence.
- `L5` and `L6` define type and effect facts used for specialization.
- `L8` defines protocols and dispatch.
- `L12` defines compile-time evaluation that may erase abstraction cost.
- `SAFE15` defines proof records for erased checks.
- Compiler phases define IR inspection and pass reporting.

## Outputs and Artifacts

- Abstraction erasure report.
- Before/after IR records.
- Residual cost list.
- Allocation and boxing audit.
- Dispatch specialization report.
- Runtime check preservation or erasure report.
- Zero-cost conformance results.

## Abstraction Families

The zero-cost model applies to:

- Protocol dispatch.
- Interface calls.
- Generic functions.
- Records and field access.
- Newtype or wrapper values.
- Iterator and sequence pipelines.
- Collection views and slices.
- Pattern matching.
- Macros.
- Facet-generated forms.
- Higher-order functions.
- Error/result wrappers.
- Resource wrappers.
- Numeric mode wrappers.
- Standard-library convenience APIs.

Each family declares what cost should erase and what residual work is acceptable.

## Erasure Claim

An erasure claim is structured:

```clojure
{:optimization :abstraction-erasure
 :profile :native
 :target :llvm
 :abstraction :protocol-dispatch
 :equivalent-form :direct-call
 :erased-costs #{:dynamic-dispatch :boxing}
 :residual-costs #{}
 :semantic-proof #{:same-method-target :effects-preserved}
 :safety-proof #{:bounds-preserved}
 :artifacts [:before-mir :after-mir :dispatch-report]}
```

The claim is valid only if the artifacts prove the residual-cost set is accurate.

## Residual Costs

Residual costs include:

- Allocation.
- Boxing or unboxing.
- Dynamic dispatch.
- Reflection.
- Synchronization.
- Runtime bounds checks.
- Runtime type checks.
- Capability provider calls.
- FFI boundary calls.
- Host runtime calls.
- Iterator state machines.
- Closure allocation.
- Virtual table lookup.
- Branch guard.

Residual work may be acceptable, but it is not zero-cost unless the equivalent
handwritten form would contain the same work.

## Inspection

The compiler must expose:

- Source operation.
- Pre-optimization IR.
- Post-optimization IR.
- Erased operations.
- Residual operations.
- Proof ids.
- Profile and target assumptions.
- Target feature assumptions.

Tooling can compare expected and actual erasure for performance-sensitive APIs.

## Diagnostics

Zero-cost diagnostics use `PERF2` identifiers:

- `PERF2-CLAIM` for incomplete zero-cost claims.
- `PERF2-RESIDUAL` for residual work contradicting a zero-cost claim.
- `PERF2-ALLOCATION` for hidden allocation.
- `PERF2-BOXING` for hidden boxing or representation conversion.
- `PERF2-DISPATCH` for unerased dynamic dispatch.
- `PERF2-REFLECTION` for hidden host reflection.
- `PERF2-CHECK` for runtime checks not accounted for.
- `PERF2-PROFILE` for erasure relying on profile-illegal behavior.
- `PERF2-EVIDENCE` for missing before/after IR or proof artifacts.

Diagnostics must include abstraction kind, source span, target, profile,
expected erased costs, residual costs, and IR artifact ids.

## Rejected Designs

Gravity rejects zero-cost claims based on source appearance alone.

Gravity rejects hidden runtime costs in lower profiles.

Gravity rejects calling an abstraction zero-cost when residual work is merely
small.

Gravity rejects erasure that loses safety or capability evidence.

## Conformance Criteria

A conforming zero-cost implementation must demonstrate:

- Protocol dispatch erasure and residual dispatch reporting.
- Generic specialization and boxing audit.
- Iterator or collection pipeline erasure where expected.
- Record and wrapper representation erasure.
- Runtime check erasure only with proof.
- Residual-cost diagnostics for non-erased abstractions.
- Before/after IR inspection artifacts.

