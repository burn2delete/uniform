# MATH6 - Interval Arithmetic & Real Proof Engine

Sequence: 74
Phase: 5 - Mathematical and Elementary Function System
Status: Manual Draft 2
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

Gravity uses interval arithmetic and real proof checking to validate elementary
math rewrites, approximations, range reductions, branch conditions, and
optimization claims. The proof engine is a checker-facing layer: it must be
able to validate a claim without trusting the optimizer, synthesizer, benchmark,
or runtime provider that produced the candidate.

This document defines interval domains, rational bounds, splitting policy,
proof transcripts, unresolved-region handling, and real-proof provider
interfaces.

## Requirements

- Certified real-domain proofs must use exact rational endpoints or a checker
  format with equivalent exactness.
- Floating-point interval execution is allowed as an implementation technique
  only when the checker proves outward rounding and records the floating
  assumptions.
- Every interval proof must name its EFIR graph or EML candidate, domain,
  branch policy, numeric mode, precision contract, and claim.
- Domain splitting must record split rule, cell ordering, stopping condition,
  local assumptions, and unresolved cells.
- Proof artifacts must separate real approximation bounds from floating
  roundoff bounds.
- A proof with unresolved cells cannot certify a total claim unless the numeric
  mode permits residual runtime checks and the residual check is emitted.
- Proof provider results must be imported through the `SAFE15` certificate model
  before they can erase checks or accept approximations.

## Dependencies

- `MATH3` defines EFIR graph identity and source anchors.
- `MATH4` defines EML candidates and normalization traces.
- `MATH5` consumes interval proof results for certified approximations.
- `MATH7` defines modes that decide whether residual checks are legal.
- `MATH8` defines floating assumptions used for roundoff intervals.
- `MATH9` defines symbolic rewrite rules that may discharge interval goals.
- `SAFE15` defines proof and certificate trust policy.

## Outputs and Artifacts

The interval proof engine emits:

- Claim record.
- Interval domain map.
- Partition tree.
- Rational bound ledger.
- Monotonicity, Taylor, minimax, Lipschitz, or enclosure lemmas.
- Roundoff ledger.
- Branch coverage report.
- Unresolved-cell report.
- Checker transcript.
- `SAFE15` proof or certificate reference.

## Claim Model

```clojure
{:artifact :gravity/interval-proof
 :claim-id claim-hash
 :source {:efir graph-hash
          :eml-candidate candidate-id
          :span source-span}
 :claim {:kind :bounded-error
         :expr '(abs (- candidate reference))
         :bound 1.0e-12}
 :domain {x {:real [-1 1]}}
 :branch-policy {:log :principal
                 :complex-intermediates :forbidden}
 :numeric-mode :certified-approx
 :precision {:type :F64 :absolute-error-max 1.0e-12}
 :status :pending}
```

Claims may prove exact equivalence, bounded error, domain coverage, branch
coverage, denominator nonzero conditions, range-reduction validity, or monotonic
facts needed by another certificate.

## Interval Domain Model

Intervals are closed unless the claim explicitly states open or half-open
boundaries. Endpoints are exact rationals, named constants with rational
enclosures, or algebraic values with proof references.

Domain records include:

- variable name and numeric family,
- lower and upper endpoint,
- boundary openness,
- excluded points or subregions,
- symbolic predicates,
- complex-region projection when a real proof uses complex identities,
- source of each bound.

Empty intervals are rejected unless the proof is specifically proving that a
candidate has no usable domain.

## Arithmetic Semantics

Interval operations must be inclusion-preserving. For each operation, the result
interval must contain every possible exact real result for inputs in the input
intervals.

Required operations include:

- addition, subtraction, multiplication, division,
- integer and rational powers,
- square root with domain proof,
- exponential and logarithm with branch policy,
- trigonometric and hyperbolic functions,
- absolute value and comparisons,
- polynomial and rational evaluation,
- min, max, and piecewise selection.

Division and rational kernels require denominator nonzero proof over every
cell.

## Partitioning and Splitting

Partition trees are deterministic artifacts:

```clojure
{:partition-id partition-hash
 :strategy :adaptive
 :ordering :source-stable
 :cells [{:id :c0
          :domain {x {:real [-1 0]}}
          :status :proved
          :bounds {:approximation 3/1000000000000
                   :roundoff 2/1000000000000}}
         {:id :c1
          :domain {x {:real [0 1]}}
          :status :proved
          :bounds {:approximation 4/1000000000000
                   :roundoff 2/1000000000000}}]
 :unresolved []}
```

Splitting policy records the variable selected, split point, heuristic input,
fuel use, and stopping reason. An adaptive strategy may be heuristic during
search, but the emitted partition must replay deterministically.

## Bound Ledgers

A bound ledger records how each final bound was obtained:

- exact interval arithmetic,
- Taylor remainder,
- minimax residual,
- Bernstein polynomial enclosure,
- monotonicity proof,
- Lipschitz bound,
- symbolic simplification,
- external theorem prover result,
- floating roundoff analysis.

Each ledger entry names assumptions and invalidation conditions. If a later pass
changes target, floating mode, range reduction, coefficient rounding, table
layout, or branch policy, the affected ledger entries are invalid.

## Real Proof Providers

Real proof providers may include interval kernels, SMT solvers, theorem provers,
computer-algebra systems, and certified numeric libraries. A provider result is
usable only when imported through a Gravity checker that verifies:

- claim identity,
- input artifact digests,
- domain and branch compatibility,
- proof method support,
- trust policy,
- replay or proof-object validity,
- invalidation conditions.

Provider output that cannot be inspected may guide search but cannot certify a
rewrite or approximation.

## Residual Checks

Some modes allow residual runtime checks for cells the proof engine cannot
discharge. The proof artifact must then include:

- unresolved cell domains,
- exact runtime predicate,
- check placement,
- failure behavior,
- performance cost classification,
- `SAFE15` record explaining why the residual check is preserved.

Residual checks are illegal in modes that require total compile-time proof.

## Diagnostics

Interval and real proof diagnostics use `MATH6` identifiers:

- `MATH6-CLAIM` for malformed or unsupported claims.
- `MATH6-DOMAIN` for invalid interval domains.
- `MATH6-ROUNDING` for unproved outward rounding.
- `MATH6-BRANCH` for branch-policy mismatch.
- `MATH6-PARTITION` for unreplayable or incomplete partition trees.
- `MATH6-BOUND` for missing or insufficient bounds.
- `MATH6-UNRESOLVED` for cells that prevent certificate acceptance.
- `MATH6-PROVIDER` for provider output outside trust policy.
- `MATH6-INVALIDATED` for stale proof artifacts.

Diagnostics must include claim id, EFIR graph id, candidate id when present,
cell id when present, domain, branch policy, numeric mode, bound, checker,
provider, source span, and remediation.

## Rejected Designs

Gravity rejects proof artifacts that depend on trusting an optimizer.

Gravity rejects target-floating behavior as a real proof unless outward rounding
and target assumptions are certified.

Gravity rejects unresolved intervals hidden inside accepted certificates.

Gravity rejects provider attestations whose assumptions cannot be inspected.

Gravity rejects domain splitting that cannot be replayed deterministically.

## Conformance Criteria

A conforming interval and real proof engine must demonstrate:

- exact interval-domain parsing and validation,
- inclusion-preserving arithmetic for the required operation set,
- deterministic partition replay,
- separate real and roundoff bound ledgers,
- accepted and rejected denominator nonzero proofs,
- branch-sensitive proof rejection and acceptance,
- unresolved-cell handling for proof-required and residual-check modes,
- `SAFE15` certificate import for accepted proof results.
