# P12 - :formal Verification Profile Specification

Sequence: 57
Phase: 3 - Profile System
Status: Manual Draft 3
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

The `:formal` profile targets proof-oriented compilation, symbolic execution,
formal semantics, theorem checking, certificate validation, proof-carrying
packages, exact or interval math, and machine-checkable safety evidence. It
narrows Gravity to deterministic, proof-checkable behavior or to explicit
assumptions recorded in proof artifacts.

The profile may reject implementation conveniences that cannot be modeled by the
selected proof engine.

## Requirements

- `:formal` code must be deterministic unless nondeterminism is represented as a
  quantified input or declared assumption.
- Runtime IO, network, clocks, randomness, dynamic eval, reflection, raw memory,
  and model calls are rejected.
- External effects are allowed only as assumptions, imported proof artifacts, or
  proof-engine services with capability grants.
- Floating and elementary math must use symbolic, exact-real, interval,
  correctly-rounded, or certified approximation modes.
- Proof artifacts must name assumptions, trusted kernel, theorem, input hashes,
  profile, target, and checked conclusion.
- Unsafe code is forbidden.

## Dependencies

- `P1` defines common profile validation.
- `SAFE9` and Phase 5 math documents define exact, interval, and certified
  elementary math modes.
- `SAFE15` defines proof and certificate artifacts.
- `L5`, `L6`, and `L12` define types, effects, and compile-time proof
  generation.
- `L15` defines proof providers and trusted kernels.
- Compiler phases define symbolic IR and proof extraction.

## Outputs and Artifacts

- `:formal` profile manifest.
- Symbolic IR.
- Proof object.
- Assumption manifest.
- Trusted-kernel record.
- Checked theorem summary.
- Certificate hash chain.
- Math mode and rounding record.
- Imported proof verification record.
- Formal conformance results.

## Allowed Behavior

`:formal` may allow:

- Pure deterministic computation.
- Symbolic values and quantified variables.
- Theorem and lemma declarations.
- Refinement and pre/postcondition checks.
- Exact integers, rationals, reals, and bitvectors.
- Interval arithmetic.
- Certified floating or elementary-function approximation.
- Proof imports through trusted roots.
- Compile-time proof checking.
- Modeled memory and region facts.

## Forbidden or Checked Behavior

`:formal` rejects:

- Runtime IO, network, environment, clock, randomness, process, model, and tool
  effects.
- Dynamic eval and reflection.
- Raw memory and FFI as ordinary behavior.
- Unsafe islands.
- Host exceptions or unmodeled runtime failure.
- Floating behavior without explicit proof mode.
- Imported proofs without trust and assumption validation.

Proof import, certificate checking, solver calls, and math approximation are
checked behavior requiring providers and artifacts.

## Proof Declarations

Proof code declares claims:

```clojure
(prove
  {:forall [x :- Real]
   :where (<= -1 x 1)
   :claim (<= (abs (- (fast-sin x) (sin x))) 1e-7)
   :mode :interval
   :trusted-kernel :gravity.proof/kernel-v1})
```

The compiler emits theorem summary, assumptions, proof method, provider, and
certificate references.

## Assumptions

Assumptions are explicit:

- Target integer width.
- Floating format.
- Memory layout.
- External theorem.
- Imported package certificate.
- Hardware or backend guarantee.
- Solver axiom.

Assumptions appear in the assumption manifest and become invalidation inputs for
certificates and backend eligibility.

## Math Modes

Formal math modes include:

- Exact integer.
- Exact rational.
- Exact real.
- Bitvector.
- Interval.
- Correctly rounded float.
- Certified approximation.
- Symbolic elementary function.

A namespace may use multiple modes only when conversions and assumptions are
explicit. Relaxed or target-default math is rejected.

## Imported Proofs

Imported proofs require:

- Source digest.
- Certificate schema.
- Trusted kernel or provider signature.
- Assumption compatibility.
- Profile and target compatibility.
- Theorem statement match.
- Hash-chain verification.

Unverified imported proofs are ignored or rejected according to policy.

## Diagnostics

Formal diagnostics use `P12` identifiers:

- `P12-NONDETERMINISM` for unmodeled nondeterminism.
- `P12-EFFECT` for runtime effects not representable in proof mode.
- `P12-MATH-MODE` for missing or invalid math mode.
- `P12-ASSUMPTION` for missing, incompatible, or hidden assumptions.
- `P12-PROOF` for failed proof checking.
- `P12-CERTIFICATE` for invalid imported certificates.
- `P12-TRUST` for untrusted proof providers or kernels.
- `P12-UNSAFE` for unsafe code.
- `P12-SYMBOLIC-LOWERING` for code that cannot lower to symbolic IR.
- `P12-BACKEND` for backend assumptions not covered by proof artifacts.

Diagnostics must include theorem id, proof id, provider, trusted kernel, source
span, assumption, profile, target, and failed check.

## Rejected Designs

Gravity rejects formal claims without machine-readable assumptions.

Gravity rejects floating or elementary math proofs under hidden target defaults.

Gravity rejects unsafe code in formal namespaces.

Gravity rejects imported proof artifacts accepted by name alone.

Gravity rejects solver or proof-provider trust not represented in artifacts.

## Conformance Criteria

A conforming `:formal` implementation must demonstrate:

- Deterministic pure proof fixtures.
- Rejection of runtime effects and unmodeled nondeterminism.
- Exact, interval, bitvector, and certified approximation math modes.
- Proof object and assumption manifest emission.
- Trusted-kernel and certificate validation.
- Imported proof acceptance and rejection cases.
- Symbolic IR lowering.
- Backend assumption compatibility checks.
