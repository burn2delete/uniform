# PERF10 - Performance/Safety Check Elision Rules

Sequence: 68
Phase: 4 - Performance Model
Status: Manual Draft 3
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

Runtime checks are executable specifications. Gravity may erase a runtime check
only when a proof, certificate, or equivalent compile-time validation artifact
establishes that the checked condition always holds at the operation. This
document defines exactly when performance work can remove safety checks.

The rule is conservative: if proof validity is unclear, the check remains or the
compilation mode is rejected.

## Requirements

- A check may be elided only when a named proof fact dominates the checked
  operation and remains valid after all intervening transformations.
- The emitted artifact must record the erased check, proof id, pass id, profile,
  target, and invalidation conditions.
- If an optimization invalidates a proof, it must regenerate the proof, keep the
  check, or reject no-checks compilation.
- Capability, effect, unsafe-audit, replay, and `:ai/human-approval` checks
  cannot be removed merely for speed.
- Check elision must preserve diagnostics and generated-origin mapping.

## Dependencies

- `SAFE1` defines the `:proven-safe`, `:runtime-checked`, `:rejected`, and
  `:unsafe-island` outcomes.
- `SAFE2` through `SAFE15` define check classes and proofs.
- `PERF1` defines optimization evidence.
- Compiler phases define dominance, IR nodes, and pass invalidation.
- Backend phases define preservation of proof assumptions.

## Outputs and Artifacts

- Check-elision certificate.
- Dominating proof fact list.
- Residual-check report.
- Invalidated-proof regeneration log.
- Pass decision record.
- Backend preservation record.
- Check-elision diagnostics and conformance results.

## Elision Record

```clojure
{:optimization :bounds-check-elision
 :check-class :bounds
 :operation :slice-get
 :profile :native
 :target :llvm
 :source-span "src/sum.g:8:19"
 :dominating-proof :range-analysis-42
 :proof-dominates-use true
 :invalidated-by #{}
 :residual-checks #{}
 :certificate :check-elision-certificate-42}
```

Every erased check has a record.

## Elision Rule Table

| Check class | Elidable when | Required artifact | Policy checks |
| --- | --- | --- | --- |
| Bounds | Dominating range proof shows `0 <= index < count` at the use site | `:proof/range` plus residual-check report | no |
| Integer overflow | Bit-width range proof covers the operator result, or source chose wrapping or saturating semantics | `:proof/int-range` or explicit operator record | no |
| Division by zero | Divisor nonzero proof dominates division | `:proof/nonzero` | no |
| Shift count | Shift count range proof covers target width | `:proof/shift-range` | no |
| Null/option access | Type excludes null/none or pattern match proves the case | `:proof/non-null` or match coverage record | no |
| Initialization | Definite-init analysis proves all bytes or fields read are initialized | `:proof/definite-init` | no |
| Region lifetime | Escape analysis proves value cannot outlive region | `:proof/region-non-escape` | no |
| Borrow/alias | Borrow graph proves legal alias state | `:proof/alias-exclusive` | no |
| Linear resource | Flow graph proves exactly-once terminal state | `:proof/linear-flow` | no |
| Data race | Ownership or synchronization proof proves no race | `:proof/race-free` | no |
| Taint sink | Validator proof proves sink contract | `:proof/taint-cleared` | no |
| Capability/effect | Compile-time validation proves exact grant and deployment policy | capability validation artifact | yes |
| Unsafe audit | Review policy satisfied before build or deployment | unsafe audit artifact | yes |
| Workflow replay | Step recorded, deterministic, or replay-safe | replay contract artifact | yes |
| AI approval | Approval impossible, already satisfied, or represented by workflow state | policy proof or workflow gate | yes |

Policy checks marked yes are not removed for performance alone. They can be
discharged only by equivalent policy artifacts.

## Dominance and Invalidation

The proof must dominate the operation in the relevant IR. Dominance can be:

- Control-flow dominance.
- Data-flow dominance.
- Pattern-match dominance.
- Type-refinement dominance.
- Loop-invariant dominance.
- Profile or target manifest dominance.

Invalidation occurs after transformations that change control flow, data flow,
layout, numeric mode, ownership, aliases, profile, target, provider, or backend
assumptions.

## Residual Checks

If proof covers only part of a condition, the compiler may emit narrower residual
checks. The residual-check report states what remains and why.

Example: a loop proof may eliminate lower-bound checks but keep a length guard
when slice lengths differ.

## Backend Preservation

Backends must preserve assumptions behind erased checks. If target lowering
changes width, alignment, memory order, exception behavior, or numeric mode, the
proof must be revalidated. Otherwise the check remains or compilation fails.

## Diagnostics

Check-elision diagnostics use `PERF10` identifiers:

- `PERF10-PROOF-MISSING` for erased checks without proof.
- `PERF10-DOMINANCE` for proof that does not dominate the operation.
- `PERF10-INVALIDATED` for proof invalidated by later transformation.
- `PERF10-RESIDUAL` for missing residual checks.
- `PERF10-POLICY` for policy checks removed as performance optimization.
- `PERF10-BACKEND` for backend lowering that cannot preserve proof assumptions.
- `PERF10-CERTIFICATE` for invalid check-elision certificate.
- `PERF10-SOURCEMAP` for lost source or generated-origin mapping.

Diagnostics must include check class, source span, IR node, proof id, pass id,
profile, target, invalidating pass, and remediation.

## Rejected Designs

Gravity rejects check elision by assertion.

Gravity rejects proof records that do not dominate the operation.

Gravity rejects erasing deployment policy, unsafe audit, replay, or approval
checks for speed.

Gravity rejects backend lowering that silently invalidates erased checks.

Gravity rejects no-checks build modes that ignore failed proof regeneration.

## Conformance Criteria

A conforming check-elision implementation must demonstrate:

- Bounds, overflow, division, shift, null, initialization, lifetime, borrow,
  linear, race, and taint check elision with proofs.
- Residual check reporting.
- Rejection when proof is missing, non-dominating, or invalidated.
- Policy-check discharge only through equivalent policy artifacts.
- Backend preservation or rejection.
- Check-elision certificates and source mapping.
