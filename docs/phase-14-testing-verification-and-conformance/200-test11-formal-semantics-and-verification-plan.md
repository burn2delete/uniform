# TEST11 - Formal Semantics and Verification Plan

Sequence: 200
Phase: 14 - Testing, Verification and Conformance
Status: Manual Draft
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

This plan defines formal semantics and verification work for Gravity. Formal
artifacts do not replace executable tests, but they provide checked evidence
for core semantics, type soundness, effect preservation, safety properties,
MIR/pass correctness, math approximations, and profile-specific total or
deterministic subsets.

Formal claims are useful only when assumptions, proof artifacts, checkers, and
counterexamples are recorded.

## Claim Areas

Formal work covers:

- core operational semantics;
- type soundness;
- effect preservation;
- capability noninterference;
- ownership and region safety;
- MIR transformation preservation;
- safety check elision;
- EFIR/math approximation certificates;
- formal profile totality;
- workflow replay determinism;
- compiler bootstrap equivalence claims.

## Requirements

- Formal claims MUST name assumptions and trusted basis.
- Proof artifacts MUST identify checker, version, input hash, and claim id.
- Stale proofs MUST be invalidated by source, compiler, or spec changes.
- Counterexamples MUST be stored as artifacts.
- Claims used for release gates MUST have machine-checkable evidence.
- EML or symbolic math equivalence MUST not be accepted without proof or certificate.
- Optimization proofs MUST link to the pass and input/output IR.
- Formal profile modules MUST declare proof obligations.

## Semantic Dependencies

- `D9` defines verifiability.
- `P12` defines formal profile.
- `SAFE15` defines proof certificates.
- `C18` defines pass correctness.
- `MATH5`, `MATH6`, and `MATH11` define math verification.
- `TEST10` defines differential complements.

## Outputs and Artifacts

Formal verification emits:

- semantics document artifacts;
- proof objects;
- certificate manifests;
- checker reports;
- assumption manifests;
- counterexample artifacts;
- proof invalidation reports.

## Example

```clojure
(prove-test inline-preserves-effects
  {:claim :effect-preservation
   :pass :inline-small-functions
   :input "blake3:mir-before"
   :output "blake3:mir-after"
   :checker :gravity-proof-checker})
```

## Rejection Rules

- Reject proof artifacts with unstated assumptions.
- Reject stale proofs.
- Reject release gates relying on prose-only proof claims.
- Reject math equivalence without checked certificate.
- Reject optimization proofs not linked to exact pass input and output.
- Reject formal profile modules with unmet proof obligations.
- Reject proof checker outputs with unknown checker identity.

## Diagnostics

- `TEST11001` reports missing proof assumption.
- `TEST11002` reports stale proof.
- `TEST11003` reports uncheckable proof claim.
- `TEST11004` reports missing math certificate.
- `TEST11005` reports pass proof linkage gap.
- `TEST11006` reports unmet formal profile obligation.
- `TEST11007` reports unknown checker identity.

## Conformance Criteria

- Formal claims record assumptions and trusted basis.
- Proof artifacts identify checker and input hashes.
- Stale proof detection responds to source, spec, compiler, and dependency changes.
- Counterexamples are reproducible fixtures.
- Math certificates are checked by declared checkers.
- Optimization proofs link exact before/after IR.
- Formal profile release gates require satisfied proof obligations.
