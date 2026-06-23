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
counterexamples, theorem-prover scripts, zero-knowledge proof systems,
verifiable-computation proof records, and verified artifact manifests.

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
- Verifiable-computation claims must record their relation form, including
  circuit, constraint system, AIR, R1CS, or Plonkish relation identifiers.
- Zero-knowledge proofs must distinguish public inputs, private inputs, witness
  generation inputs, setup parameters, trust model, verifier key, and proof id.
- Prover and verifier cost records must be emitted for proof-producing targets.
- Recursive, folding, and IVC proof chains must preserve parent links, root
  state, step relation hashes, and stale-proof invalidation metadata.
- zkVM and proof-kernel providers must declare backend, version, trusted basis,
  supported arithmetization, and conformance fixtures.

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
- Zk relation artifact.
- Public/private input manifest.
- Witness-generation record.
- Setup and trust-model manifest.
- Prover/verifier cost report.
- Recursive/folding/IVC proof-chain manifest.
- zkVM or proof-kernel provider record.
- Source/artifact hash linkage.
- Verification conformance report.
- Formal verification diagnostics.

## Domain Manifest

```clojure
{:domain :formal-verification
 :profiles #{:formal :core}
 :backends #{:proof-kernel :mlir :zkvm}
 :artifacts #{:proof-object :assumption-manifest :certificate
              :counterexample :proof-check-report :zk-relation
              :witness-record :setup-trust-manifest
              :prover-verifier-cost :recursive-proof-chain}
 :examples #{:bounds-proof :region-escape-proof :mir-equivalence
             :math-certificate :zk-circuit-proof :ivc-chain}
 :rejects #{:claim-without-proof :stale-certificate
            :solver-assumption-hidden :eml-equality-without-proof
            :zk-proof-without-relation :hidden-setup-trust}}
```

## Replacement Scope

Gravity should replace:

- safety proof annotations,
- theorem/proof declarations,
- SMT-backed property checks,
- pass equivalence fixtures,
- math interval/certificate checks,
- circuit, constraint, AIR, R1CS, and Plonkish relation fixtures,
- public/private input and witness-generation manifests,
- setup, trust-model, prover-cost, and verifier-cost records,
- recursive, folding, and IVC proof-chain manifests,
- zkVM and proof-kernel provider records,
- artifact proof manifests,
- counterexample reporting.

External theorem provers, zkVMs, proving networks, and specialized proof
kernels remain providers unless Gravity implements the proof kernel.

## Minimum End-to-End Slice

The first complete slice is a bounds proof:

- Gravity source declares loop bounds and array access property.
- Compiler emits proof obligation tied to MIR/source hashes.
- Solver/proof provider emits proof or counterexample.
- Proof checker validates certificate.
- Optimization pass may elide bounds check only with valid proof id.

The first zk/verifiable-computation slice is a checked relation proof:

- Gravity source declares the computation, public inputs, private inputs, and
  privacy boundary.
- Compiler emits a circuit, constraint, AIR, R1CS, or Plonkish relation tied to
  source, MIR, and artifact hashes.
- Witness generation records input sources, nondeterminism, secret handling, and
  relation hash.
- Setup records transparent or trusted setup, ceremony transcript when used,
  proving key hash, verifier key hash, and provider trust assumptions.
- Prover emits proof, prover cost, verifier cost, and conformance metadata.
- Verifier or proof kernel checks proof against relation, public inputs, setup,
  and verifier key before any release or check-elision use.
- Recursive, folding, or IVC variants record step proofs, accumulator state,
  parent proof ids, and root verification result.

## Diagnostics

Formal verification diagnostics use `DOM19` identifiers:

- `DOM19-CLAIM` for claims without verifier, trusted basis, or assumptions.
- `DOM19-PROOF` for invalid or missing proof object.
- `DOM19-STALE` for proof artifacts whose source/artifact hashes changed.
- `DOM19-ASSUMPTION` for hidden solver or environment assumptions.
- `DOM19-COUNTEREXAMPLE` for counterexamples without source/artifact mapping.
- `DOM19-EML` for EML equality used without semantic proof.
- `DOM19-ELISION` for safety/optimization use without valid proof.
- `DOM19-ZK-RELATION` for missing or mismatched circuit, constraint, AIR, R1CS,
  or Plonkish relation metadata.
- `DOM19-ZK-INPUT` for missing public/private input split or witness-generation
  record.
- `DOM19-ZK-SETUP` for missing setup, trust model, proving key, verifier key, or
  provider trust assumption.
- `DOM19-ZK-COST` for missing prover or verifier cost record.
- `DOM19-ZK-CHAIN` for recursive, folding, or IVC proof chains with missing
  parent links, stale step proofs, or invalid root state.
- `DOM19-ZK-PROVIDER` for zkVM or proof-kernel provider records outside policy.
- `DOM19-CONFORMANCE` for missing proof-check fixtures.

Diagnostics must include claim id, source span, artifact hash, theorem/prover,
assumption id, proof/counterexample id, relation id, input manifest, setup id,
verifier key hash, provider id, chain edge when applicable, cost record, and
remediation.

## Rejected Designs

Gravity rejects proof-carrying claims without verification.

Gravity rejects stale certificates.

Gravity rejects hidden solver assumptions.

Gravity rejects optimization based on unverified proof artifacts.

Gravity rejects treating EML normal-form equality as semantic equality.

Gravity rejects zero-knowledge proofs without relation, input, witness, setup,
and verifier-key linkage.

Gravity rejects proving setup records without trust model and key provenance.

Gravity rejects recursive, folding, or IVC chains with stale step proofs,
missing parent links, or unverifiable root state.

Gravity rejects zkVM and proof-kernel providers without provider records and
policy-reviewed assumptions.

## Conformance Criteria

A conforming formal verification slice must demonstrate:

- claim, proof, certificate, assumption, and counterexample artifacts,
- proof checking tied to source and artifact hashes,
- stale-proof invalidation for source, relation, witness, setup, verifier key,
  provider, compiler pass, backend, and target-assumption changes,
- bounds, region, math, and MIR-equivalence examples,
- circuit, constraint, AIR, R1CS, and Plonkish relation fixtures where supported,
- public/private input manifests and witness-generation records,
- setup/trust-model records and prover/verifier cost records,
- recursive, folding, and IVC proof-chain fixtures,
- zkVM or proof-kernel provider records and policy rejection fixtures,
- counterexample source mapping,
- rejection of hidden assumptions, unverified claims, stale certificates,
  proofless check elision, stale zk proofs, hidden setup assumptions, malformed
  witnesses, and invalid recursive proof chains.
