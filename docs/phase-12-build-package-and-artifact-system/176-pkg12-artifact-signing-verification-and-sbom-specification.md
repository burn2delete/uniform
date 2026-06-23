# PKG12 - Artifact Signing, Verification and SBOM Specification

Sequence: 176
Phase: 12 - Build, Package and Artifact System
Status: Manual Draft
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

This specification defines signing, verification, software bills of materials,
and provenance attestations for Gravity artifacts. Signing binds artifact
content, manifest schema, provenance, attestation evidence, SBOM, safety
metadata, and policy decisions into a verifiable release record. Verification
checks that a consumed artifact is exactly what policy permits.

SBOMs and attestations are first-class artifacts because Gravity packages can
target binaries, schemas, workflows, agents, HDL modules, generated code, and
proofs; all of those need dependency, authority, and builder visibility.

## Signature Payload

A signature covers canonical data containing:

- artifact manifest hash;
- artifact content hash;
- schema version;
- package id and version;
- profile and target;
- provenance hash;
- provenance attestation hash;
- SBOM hash;
- safety metadata hash;
- build recipe hash when reproducibility is claimed;
- attestation policy level and verification track;
- transparency log or timestamp evidence hash;
- signer identity and signing policy id.
- keyless signing issuer, subject, audience, certificate, and root metadata
  hashes when identity-based signing is used.

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

## Provenance Attestations

A provenance attestation records SLSA/in-toto-style evidence for the exact
artifact subject. It is separate from the SBOM: the SBOM names what is in the
artifact graph, while the attestation says who built it, where it was built,
from which declared inputs, and under which isolation and policy claims.

An attestation records:

- attestation id and schema version;
- artifact subject hash and manifest hash;
- builder identity, signing identity, and key or workload identity;
- OIDC issuer, subject, audience, token claim summary, certificate subject
  alternative names, certificate validity window, and root metadata when
  keyless or workload-identity signing is used;
- build platform, runner image, and isolation boundary;
- source material, including repository, revision, archive hash, generated-input hashes, and patch ids;
- lockfile, dependency graph, compiler, toolchain, and build recipe hashes;
- isolated, hermetic, reproducible, or non-hermetic claims with declared exceptions;
- build start/end timestamps and freshness window;
- transparency log inclusion proof or trusted timestamp evidence when required;
- policy level, verification track, and consumer gate id;
- revocation, advisory, and expiration links.

Attestation schemas may map to external SLSA or in-toto predicates, but
Gravity's internal canonical schema remains the source of truth for signing and
verification.

## Keyless and Transparency-Log Signing

Gravity supports key-managed signing and keyless identity-based signing. A
keyless signing profile is compatible with Sigstore-style systems, but the
Gravity artifact schema records the semantics directly instead of depending on a
single external service.

A keyless signing record includes:

- signing profile id and provider family, such as `:sigstore-compatible`;
- OIDC issuer, subject, audience, issued-at and expiration claims, and selected
  provider-specific claims such as repository, workflow, revision, service
  account, SPIFFE id, or workload identity;
- ephemeral public key or certificate public key hash;
- short-lived certificate, certificate authority identity, certificate subject
  alternative names, validity window, and chain/root metadata;
- transparency log service, log entry id, integrated time, inclusion proof,
  signed entry timestamp or equivalent timestamp evidence, and log checkpoint
  or consistency proof when policy requires it;
- TUF or other root-update metadata used to select trusted certificate
  authority and log keys;
- identity-monitoring policy for detecting unexpected signatures by the same
  subject.

Verification of a keyless signature must check the artifact signature, subject
hash, certificate validity at signing time, OIDC issuer and audience, subject
pattern, certificate identity claims, transparency log inclusion, root metadata,
revocation and expiration policy, and the consumer's required identity policy.
The verifier must fail closed when it cannot determine whether the identity,
certificate, log, or root metadata satisfies policy.

Transparency-log evidence is not a substitute for policy. It proves that a
signing event was witnessed by the selected log, but the consumer still decides
whether the identity, issuer, subject, builder, and artifact are acceptable.

## Attestation Policy Levels

Attestation policy levels are:

- `:baseline` requires a signed attestation bound to artifact and source hashes;
- `:tracked` also requires verified builder identity, build platform, lockfile, and recipe hashes;
- `:isolated` also requires an isolated builder boundary and declared network, filesystem, and secret inputs;
- `:hermetic` also requires all build-time inputs to be declared and all undeclared external access to be denied;
- `:reproducible` also requires `PKG7` rebuild evidence for the same recipe and source material;
- `:certified` also requires governance-approved builders, transparency log or timestamp evidence, and conformance evidence.

Verification tracks are selected by package, profile, target, registry, or
consumer policy. A consumer may require a higher attestation level than the
publisher used, but it may not silently downgrade a required gate.

## Requirements

- Release artifacts requiring signing MUST be signed over canonical manifest data.
- Verification MUST check schema, content hash, signature, provenance, attestation, SBOM, safety metadata, revocation, and policy.
- SBOMs MUST include transitive dependencies.
- Capability and unsafe summaries MUST appear in SBOM records.
- Generated source and binary blobs MUST be visible in SBOM records.
- Required provenance attestations MUST bind to the exact artifact subject hash and manifest hash.
- Attestations MUST identify builder identity, build platform, source material, dependency lock, compiler or toolchain, and build recipe.
- Keyless signing MUST identify the OIDC issuer, subject, audience, certificate
  identity, validity window, transparency log inclusion proof, and root metadata
  required by policy.
- Isolated, hermetic, reproducible, or non-hermetic claims MUST be explicit and evidence-backed.
- Transparency log inclusion proof or trusted timestamp evidence MUST be checked when policy requires ordering, freshness, or public auditability.
- Consumer verification gates MUST evaluate attestation policy level and verification track before accepting an artifact.
- Signature keys MUST have policy-defined scope and validity.
- Revoked signatures or signing keys MUST invalidate release verification unless historical rebuild policy permits them.
- Artifact consumers MUST verify before use when package policy requires verification.
- Missing, stale, revoked, mismatched, unsigned, or policy-incompatible attestations MUST fail closed when consumer policy requires them.
- Verification reports MUST be artifacts.
- Signing and verification failures MUST be diagnosable without exposing secrets.

## Semantic Dependencies

- `S3` defines canonical data.
- `S9` defines artifact schema.
- `PKG3` defines artifact identity.
- `PKG7` defines reproducible build claims.
- `PKG8` defines safety metadata.
- `PKG10` defines provenance.
- `L12` defines hermetic build input rules.
- `GOV4` and `GOV10` define security and ecosystem review expectations.

## Outputs and Artifacts

Signing emits:

- signature artifact;
- signed payload hash;
- signer identity reference;
- signing policy id;
- provenance attestation artifact;
- attestation payload hash;
- keyless signing identity artifact when selected;
- transparency log or timestamp evidence;
- root metadata and transparency log verification bundle;
- attestation policy level and verification track;
- key validity proof;
- release verification input bundle.

Verification emits:

- verification report;
- attestation verification report;
- SBOM validation report;
- revocation check report;
- freshness and timestamp decision;
- policy decision;
- consumer acceptance or denial record.

## Example

```clojure
(sign-artifact
  {:artifact acme/support-agent:0.3.0
   :keyless {:provider :sigstore-compatible
             :issuer "https://token.actions.githubusercontent.com"
             :subject "https://github.com/acme/support/.github/workflows/release.yml@refs/heads/main"
             :audience "sigstore"}
   :payload [:artifact-manifest :content :provenance :attestation :sbom :safety]
   :attestation-level :hermetic
   :verification [:schema :signature :hash :identity :provenance
                  :attestation :transparency-log :timestamp
                  :revocation :policy]})
```

## Rejection Rules

- Reject unsigned release artifacts when signing is required.
- Reject signatures over noncanonical payloads.
- Reject content hash mismatch.
- Reject SBOMs missing transitive dependencies.
- Reject SBOMs missing capabilities, unsafe summaries, generated source, or binary blobs when present.
- Reject required attestations that are missing, unsigned, stale, revoked, or not bound to the artifact subject hash.
- Reject attestations that omit builder identity, build platform, source material, dependency lock, or build recipe.
- Reject isolated, hermetic, or reproducible claims without matching evidence.
- Reject required transparency log or timestamp evidence that is missing, unverifiable, or outside the freshness window.
- Reject keyless signatures with missing, expired, revoked, mismatched, or
  policy-incompatible certificate, OIDC issuer, subject, audience, root
  metadata, or transparency log evidence.
- Reject consumer verification that downgrades the required attestation policy level or track.
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
- `PKG12009` reports missing required provenance attestation.
- `PKG12010` reports attestation/artifact mismatch.
- `PKG12011` reports unknown or untrusted builder identity.
- `PKG12012` reports incomplete attestation source material.
- `PKG12013` reports unsupported or unproved isolation, hermetic, or reproducible claim.
- `PKG12014` reports missing or unverifiable transparency log or timestamp evidence.
- `PKG12015` reports stale attestation.
- `PKG12016` reports attestation policy level or track failure.
- `PKG12017` reports missing or invalid keyless signing identity evidence.
- `PKG12018` reports OIDC issuer, subject, audience, claim, or certificate
  identity mismatch.
- `PKG12019` reports missing, stale, or untrusted root metadata for certificate
  authority or transparency log verification.
- `PKG12020` reports transparency log inclusion, timestamp, checkpoint, or
  consistency evidence that fails policy.

Diagnostics include artifact id, signature id, signer id, SBOM id, dependency
id, attestation id, builder id, build platform id, source material hash,
timestamp evidence id, OIDC issuer, signing subject, certificate id,
transparency log entry, root metadata id, failed check, policy level,
verification track, and policy rule.

## Conformance Criteria

- A signed artifact verifies against canonical manifest and content hashes.
- Changing artifact content invalidates the signature.
- An SBOM missing a transitive dependency is rejected.
- Capability and unsafe summaries appear in SBOM output.
- Generated source and binary blobs are represented when present.
- A signed attestation bound to the artifact subject hash is verified when policy requires it.
- Keyless signing fixtures verify OIDC issuer, subject, audience, certificate
  identity, validity window, transparency log inclusion, and root metadata.
- Missing, stale, revoked, or mismatched attestations fail consumer verification gates.
- Builder identity, build platform, and source material omissions produce specific diagnostics.
- Isolated, hermetic, and reproducible claims are rejected unless the required evidence is present.
- Transparency log inclusion or timestamp evidence is checked for policy levels that require it.
- Keyless signatures with mismatched issuer, subject, audience, certificate,
  root metadata, or transparency log evidence are rejected.
- Revoked signing keys invalidate verification under release policy.
- Consumers fail closed when verification is required but absent.
