# DOM19 - Formal Verification Domain Specification

Sequence: 142
Phase: 9 - Domain-Specific Computing Coverage
Status: Manual Draft 3
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

This domain proves that Gravity can cover formal verification slices normally
written in Coq, Lean, Isabelle, Dafny, F*, SMT-LIB, ACL2, TLA+, Alloy, or
tool-specific proof languages.

The replacement scope is proof-carrying claims, safety certificates, interval
proofs, EFIR approximation proofs, MIR pass equivalence, type/effect properties,
counterexamples, theorem-prover scripts, and verified artifact manifests.

## Requirements

- Verification claims must state trusted basis, theorem/prover backend,
  assumptions, source/artifact hash, profile, and checked property.
- Proof artifacts must be verified before used for check elision, optimization,
  safety claims, or release evidence.
- Stale proofs are invalid when source, schema, compiler pass, backend, or
  target assumptions change.
- Counterexamples must map back to source, MIR/domain IR, or artifact edge.
- EML syntactic normalization is not semantic equivalence without proof.
- Solver/provider assumptions must be recorded and policy-reviewed.

## Dependencies

- `P12`, Phase 5 math docs, `C18`, `SAFE15`, and testing phases define formal
  profile, math certificates, compiler proof, proof artifacts, and validation.
- `B7`, `B13`, and package/release phases consume proof artifacts.

## Outputs and Artifacts

- Formal verification domain manifest.
- Claim manifest.
- Proof object.
- Certificate.
- Assumption manifest.
- Theorem-prover script.
- Counterexample artifact.
- Source/artifact hash linkage.
- Verification conformance report.
- Formal verification diagnostics.

## Domain Manifest

```clojure
{:domain :formal-verification
 :profiles #{:formal :core}
 :backends #{:proof-kernel :mlir}
 :artifacts #{:proof-object :assumption-manifest :certificate
              :counterexample :proof-check-report}
 :examples #{:bounds-proof :region-escape-proof :mir-equivalence
             :math-certificate}
 :rejects #{:claim-without-proof :stale-certificate
            :solver-assumption-hidden :eml-equality-without-proof}}
```

## Replacement Scope

Gravity should replace:

- safety proof annotations,
- theorem/proof declarations,
- SMT-backed property checks,
- pass equivalence fixtures,
- math interval/certificate checks,
- artifact proof manifests,
- counterexample reporting.

External theorem provers remain providers unless Gravity implements the proof
kernel.

## Minimum End-to-End Slice

The first complete slice is a bounds proof:

- Gravity source declares loop bounds and array access property.
- Compiler emits proof obligation tied to MIR/source hashes.
- Solver/proof provider emits proof or counterexample.
- Proof checker validates certificate.
- Optimization pass may elide bounds check only with valid proof id.

## Diagnostics

Formal verification diagnostics use `DOM19` identifiers:

- `DOM19-CLAIM` for claims without verifier, trusted basis, or assumptions.
- `DOM19-PROOF` for invalid or missing proof object.
- `DOM19-STALE` for proof artifacts whose source/artifact hashes changed.
- `DOM19-ASSUMPTION` for hidden solver or environment assumptions.
- `DOM19-COUNTEREXAMPLE` for counterexamples without source/artifact mapping.
- `DOM19-EML` for EML equality used without semantic proof.
- `DOM19-ELISION` for safety/optimization use without valid proof.
- `DOM19-CONFORMANCE` for missing proof-check fixtures.

Diagnostics must include claim id, source span, artifact hash, theorem/prover,
assumption id, proof/counterexample id, and remediation.

## Rejected Designs

Gravity rejects proof-carrying claims without verification.

Gravity rejects stale certificates.

Gravity rejects hidden solver assumptions.

Gravity rejects optimization based on unverified proof artifacts.

Gravity rejects treating EML normal-form equality as semantic equality.

## Conformance Criteria

A conforming formal verification slice must demonstrate:

- claim, proof, certificate, assumption, and counterexample artifacts,
- proof checking tied to source and artifact hashes,
- stale-proof invalidation,
- bounds, region, math, and MIR-equivalence examples,
- counterexample source mapping,
- rejection of hidden assumptions, unverified claims, stale certificates, and
  proofless check elision.
