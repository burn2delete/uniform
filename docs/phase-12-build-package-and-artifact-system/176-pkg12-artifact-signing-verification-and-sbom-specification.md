# PKG12 - Artifact Signing, Verification and SBOM Specification

Sequence: 176
Phase: 12 - Build, Package and Artifact System
Status: Manual Draft
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

This specification defines signing, verification, and software bills of
materials for Gravity artifacts. Signing binds artifact content, manifest
schema, provenance, SBOM, safety metadata, and policy decisions into a
verifiable release record. Verification checks that a consumed artifact is
exactly what policy permits.

SBOMs are first-class artifacts because Gravity packages can target binaries,
schemas, workflows, agents, HDL modules, generated code, and proofs; all of
those need dependency and authority visibility.

## Signature Payload

A signature covers canonical data containing:

- artifact manifest hash;
- artifact content hash;
- schema version;
- package id and version;
- profile and target;
- provenance hash;
- SBOM hash;
- safety metadata hash;
- build recipe hash when reproducibility is claimed;
- signer identity and signing policy id.

Signing noncanonical data is invalid.

## SBOM Contents

An SBOM records:

- package dependencies and versions;
- artifact dependencies;
- source references and hashes;
- registry source;
- licenses or policy classifications;
- capabilities and privileged effects;
- unsafe-code summary;
- FFI and binary blob summary;
- generated-source summary;
- provenance links;
- vulnerability, advisory, and revocation status.

SBOM schemas may map to external formats, but Gravity's internal schema remains
the source of truth.

## Requirements

- Release artifacts requiring signing MUST be signed over canonical manifest data.
- Verification MUST check schema, content hash, signature, provenance, SBOM, safety metadata, revocation, and policy.
- SBOMs MUST include transitive dependencies.
- Capability and unsafe summaries MUST appear in SBOM records.
- Generated source and binary blobs MUST be visible in SBOM records.
- Signature keys MUST have policy-defined scope and validity.
- Revoked signatures or signing keys MUST invalidate release verification unless historical rebuild policy permits them.
- Artifact consumers MUST verify before use when package policy requires verification.
- Verification reports MUST be artifacts.
- Signing and verification failures MUST be diagnosable without exposing secrets.

## Semantic Dependencies

- `S3` defines canonical data.
- `S9` defines artifact schema.
- `PKG3` defines artifact identity.
- `PKG7` defines reproducible build claims.
- `PKG8` defines safety metadata.
- `PKG10` defines provenance.
- `GOV4` and `GOV10` define security and ecosystem review expectations.

## Outputs and Artifacts

Signing emits:

- signature artifact;
- signed payload hash;
- signer identity reference;
- signing policy id;
- key validity proof;
- release verification input bundle.

Verification emits:

- verification report;
- SBOM validation report;
- revocation check report;
- policy decision;
- consumer acceptance or denial record.

## Example

```clojure
(sign-artifact
  {:artifact acme/support-agent:0.3.0
   :key :release-2026-q2
   :payload [:artifact-manifest :content :provenance :sbom :safety]
   :verification [:schema :signature :hash :provenance :revocation :policy]})
```

## Rejection Rules

- Reject unsigned release artifacts when signing is required.
- Reject signatures over noncanonical payloads.
- Reject content hash mismatch.
- Reject SBOMs missing transitive dependencies.
- Reject SBOMs missing capabilities, unsafe summaries, generated source, or binary blobs when present.
- Reject revoked keys, signatures, packages, or provenance inputs.
- Reject consumers using artifacts before required verification.
- Reject verification reports that omit failed checks.

## Diagnostics

- `PKG12001` reports missing signature.
- `PKG12002` reports noncanonical signature payload.
- `PKG12003` reports content hash mismatch.
- `PKG12004` reports SBOM dependency omission.
- `PKG12005` reports SBOM safety or capability omission.
- `PKG12006` reports revoked signing material.
- `PKG12007` reports unverified consumer use.
- `PKG12008` reports incomplete verification report.

Diagnostics include artifact id, signature id, signer id, SBOM id, dependency
id, failed check, and policy rule.

## Conformance Criteria

- A signed artifact verifies against canonical manifest and content hashes.
- Changing artifact content invalidates the signature.
- An SBOM missing a transitive dependency is rejected.
- Capability and unsafe summaries appear in SBOM output.
- Generated source and binary blobs are represented when present.
- Revoked signing keys invalidate verification under release policy.
- Consumers fail closed when verification is required but absent.
