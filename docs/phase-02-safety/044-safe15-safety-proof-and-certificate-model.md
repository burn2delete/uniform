# SAFE15 - Safety Proof and Certificate Model

Sequence: 44
Phase: 2 - Safety
Status: Manual Draft 3
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

Safety proofs and certificates are durable evidence that a program operation is
safe, a runtime check can remain, an unsafe wrapper maintains its invariant, or
an optimization may erase a check. They make safety claims inspectable by
compilers, package tools, auditors, release gates, and downstream consumers.

This document defines proof records, certificate contents, trust roots,
invalidation, imported certificates, proof-carrying packages, and check-erasure
rules.

## Requirements

- Every erased safety check must reference a valid proof or certificate.
- Proof records must name claim, source span, typed artifact node, assumptions,
  method, provider, profile, target, and invalidation conditions.
- Certificates imported from packages must be verified against trust policy
  before being used as safety evidence.
- Compiler passes that invalidate assumptions must invalidate or regenerate
  affected certificates.
- Unsafe wrappers may rely on certificates only when the certificate proves the
  wrapper invariant under the active profile and target.
- Certificate artifacts must be stable enough for package publication, replay,
  conformance, and audit.

## Dependencies

- `SAFE1` defines safety outcomes and check-erasure evidence.
- `SAFE2` through `SAFE14` define specialized proof claims.
- `L5`, `L6`, `L10`, and `L11` define type, effect, memory, and concurrency
  facts used as proof assumptions.
- `L12` defines compile-time proof generation effects.
- `L15` defines proof and solver providers.
- Phase 5 math documents define EFIR and numeric proof artifacts.
- Phase 6 compiler documents define pass invalidation and IR node identity.
- Phase 12 package documents define certificate distribution and trust policy.

## Outputs and Artifacts

- Proof records.
- Safety certificates.
- Check-erasure records.
- Certificate trust records.
- Invalidation records.
- Imported certificate verification records.
- Proof provider records.
- Audit views for unsafe wrappers.
- Certificate diagnostics and conformance reports.

## Proof Record

A proof record contains:

```clojure
{:proof/id :range-analysis-4821
 :claim :bounds-safe
 :span "src/app.g:12:7"
 :artifact-node :mir/node-77
 :profile :native
 :target :x86_64-linux
 :assumptions #{:i-in-range :len-known :no-overflow}
 :method :range-analysis
 :provider gravity.compiler/range
 :inputs [:typed-core-22 :loop-invariant-8]
 :invalidated-by #{:loop-transform :integer-mode-change :target-width-change}
 :result :proven-safe}
```

Proof records can be local to a build. Certificates are proof records packaged
with stable identity, trust metadata, and verification rules.

## Certificate Contents

A certificate contains:

- Certificate id.
- Package id and version.
- Compiler id and version.
- Provider id and version.
- Claim.
- Source span or generated-origin chain.
- Artifact node id.
- Profile and target.
- Assumptions.
- Proof method.
- Inputs and digests.
- Invalidation conditions.
- Trust root or signature.
- Verification status.
- Expiration or review policy when required.

Certificates may refer to source, typed core, MIR, domain IR, backend IR, package
metadata, unsafe island records, or generated artifacts.

## Claim Families

Standard claim families include:

- Bounds safe.
- Initialized before read.
- Lifetime valid.
- No region escape.
- Linear resource consumed exactly once.
- No data race.
- Numeric overflow impossible.
- Division by zero impossible.
- Taint cleared for sink.
- Capability scope valid.
- Unsafe wrapper invariant holds.
- FFI ownership transfer valid.
- Model/tool call policy satisfied.
- Supply-chain provenance valid.
- Backend lowering preserves safety.

Each specialized SAFE document defines the exact facts required for its claim.

## Proof Methods

Allowed proof methods include:

- Type checking.
- Effect checking.
- Capability checking.
- Borrow checking.
- Escape analysis.
- Region analysis.
- Linear flow analysis.
- Range and interval analysis.
- Taint analysis.
- Model checking.
- SMT or theorem proving.
- Conformance fixtures.
- Manual review for unsafe islands.
- Provider attestation.

Manual review is never enough to erase a runtime check unless policy explicitly
accepts it for that claim family.

## Invalidation

Certificates are invalidated by changes to:

- Source.
- Macro expansion.
- Type facts.
- Effect facts.
- Profile or target.
- Numeric mode.
- Memory provider.
- Capability provider.
- Compiler pass output.
- Backend lowering.
- Package dependency.
- Build grant.
- Replay record.

Compiler passes must declare which proof assumptions they preserve and which they
invalidate. A pass that cannot preserve certificate mapping must force
rechecking.

## Imported Certificates

Packages may ship certificates. Importing a certificate requires:

- Matching package digest.
- Matching compiler or accepted compatibility range.
- Matching profile and target or accepted generalization.
- Trusted provider or signature.
- Compatible certificate schema.
- No invalidated assumptions under caller policy.

Imported certificates that fail verification are ignored or rejected according to
policy. They are never silently accepted.

## Check Erasure

A runtime safety check may be erased only when:

- The source operation has a `:proven-safe` outcome.
- A proof or certificate proves the exact condition checked.
- The proof is valid after all transformations preceding erasure.
- The backend can preserve the assumptions.
- The emitted artifact records the erased check and proof id.

If later passes invalidate the proof, the check must be restored or the program
must be rejected.

## Trust and Policy

Certificate trust levels include:

- Local build proof.
- Verified compiler proof.
- Trusted package certificate.
- Third-party signed certificate.
- Manual audit certificate.
- Experimental provider certificate.

Policy decides which trust levels are accepted for each claim family and profile.
Safety-critical profiles may reject third-party and manual certificates unless
they are independently verified.

## Diagnostics

SAFE15 diagnostics use these identifiers:

- `SAFE15-PROOF-MISSING` for a safety claim without evidence.
- `SAFE15-CERT-SCHEMA` for invalid certificate shape.
- `SAFE15-CERT-TRUST` for untrusted or unsigned certificates.
- `SAFE15-CERT-MISMATCH` for package, profile, target, compiler, or provider
  mismatch.
- `SAFE15-INVALIDATED` for use of a certificate after invalidating changes.
- `SAFE15-CHECK-ERASE` for erased checks without valid proof.
- `SAFE15-PROVIDER` for proof providers outside policy.
- `SAFE15-MANUAL` for manual review used where proof is required.
- `SAFE15-BACKEND` for backend inability to preserve proof assumptions.

Diagnostics must include claim, proof id, certificate id, artifact node, source
span, profile, target, provider, trust root, invalidated assumption, and
remediation.

## Rejected Designs

Gravity rejects check erasure without proof.

Gravity rejects opaque certificates whose assumptions cannot be inspected.

Gravity rejects imported certificates accepted solely by package name.

Gravity rejects compiler passes that discard proof mapping without invalidation.

Gravity rejects manual review as a universal substitute for formal or mechanical
evidence.

## Conformance Criteria

A conforming proof and certificate implementation must demonstrate:

- Proof records for accepted static safety claims.
- Certificate serialization and verification.
- Check erasure tied to certificate ids.
- Invalidation after source, macro, profile, target, provider, dependency, and
  backend changes.
- Imported certificate acceptance and rejection cases.
- Trust policy enforcement by claim family.
- Unsafe wrapper certificates connected to `SAFE6` audit records.
- Backend preservation or rejection for proof-dependent optimizations.
