# PKG10 - Supply-Chain Security and Provenance Specification

Sequence: 174
Phase: 12 - Build, Package and Artifact System
Status: Manual Draft
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

This specification defines supply-chain security and provenance for Gravity
packages and artifacts. Provenance records how an artifact was produced, from
which source and dependencies, by which builder, with which compiler, under
which policy, and with which verification results.

Supply-chain checks prevent unsigned dependencies, unknown builders, revoked
inputs, hidden generated code, unsafe binary blobs, and policy-incompatible
packages from entering trusted builds.

## Provenance Record

A provenance record includes:

- artifact id and content hash;
- source graph hash and repository references;
- project manifest hash;
- lockfile hash;
- compiler and builder identity;
- build recipe hash;
- dependency graph hash;
- generated-source generator identities;
- external binary blob identities;
- test, safety, and reproducibility evidence;
- signing and SBOM links;
- keyless signing identity, issuer, certificate, transparency log, and root
  metadata links when identity-based signing is used;
- revocation and advisory status.

Provenance is canonicalized and may be signed separately from artifact content.

## Requirements

- Release artifacts MUST include provenance records.
- Dependencies MUST have provenance summaries in the lockfile.
- Builder identity MUST be verified for release builds.
- Generated source MUST be recorded as generated, with generator and input hashes.
- Binary blobs MUST be declared with source, hash, license, and safety policy.
- Revoked packages, builders, signatures, or attestations MUST block release unless historical rebuild policy allows them.
- Provenance records MUST be linked to SBOM and signatures.
- Keyless or identity-based signing provenance MUST record OIDC issuer, subject,
  audience, issued certificate identity, certificate validity window,
  transparency log inclusion proof, and configured root of trust.
- Model-generated code or patches MUST be tainted as generated source until compiler and review gates accept them.
- Provenance consumers MUST fail closed on unknown schema versions for release verification.
- Supply-chain policy changes MUST be captured in artifact manifests.

## Supply-Chain Policy Inputs

Policy may require:

- trusted registries;
- trusted builders;
- signed dependencies;
- keyless signing identity policy;
- accepted OIDC issuers, subjects, audiences, and workload identity scopes;
- transparency log and root-of-trust policy;
- reproducible build evidence;
- no unreviewed unsafe metadata;
- no untracked generated code;
- approved binary blobs only;
- no revoked attestations;
- accepted license set;
- minimum vulnerability review state.

## Semantic Dependencies

- `SAFE14` defines supply-chain safety.
- `PKG3` defines artifact identity.
- `PKG5` defines locked dependency graphs.
- `PKG7` defines reproducible build evidence.
- `PKG8` defines safety metadata.
- `PKG12` defines signing, verification, and SBOM.
- `BOOT6` defines bootstrap trust concerns.
- `GOV4` defines security review.

## Outputs and Artifacts

The build and package tools emit:

- provenance record;
- builder attestation;
- keyless signer identity record;
- transparency log inclusion and monitoring record;
- generated-source ledger;
- binary blob ledger;
- dependency provenance summary;
- revocation check report;
- supply-chain verification report;
- release gate decision.

## Example

```clojure
(provenance
  {:artifact acme/support-agent:0.3.0
   :source "blake3:source"
   :project "blake3:project"
   :lockfile "blake3:lock"
   :builder "gravity-builder:trusted-linux-x64"
   :compiler "gravityc:0.1.0"
   :dependencies "blake3:dependency-graph"
   :keyless-signing {:issuer "https://token.actions.githubusercontent.com"
                     :subject "https://github.com/acme/support/.github/workflows/release.yml@refs/heads/main"
                     :audience "sigstore"
                     :certificate "sha256:fulcio-cert"
                     :transparency-log "rekor:entry"}
   :evidence [:tests :safety :reproducible-build :sbom]
   :revocation-status :checked})
```

## Rejection Rules

- Reject release artifacts with no provenance.
- Reject dependencies with missing or unverifiable provenance summaries.
- Reject unknown builders under trusted-builder policy.
- Reject untracked generated source.
- Reject binary blobs lacking source, hash, or policy.
- Reject revoked signatures, builders, packages, or attestations.
- Reject keyless signing records whose issuer, subject, audience, certificate,
  root metadata, or transparency log inclusion proof does not satisfy policy.
- Reject provenance records not linked to the artifact manifest.
- Reject release verification on unknown provenance schema.

## Diagnostics

- `PKG10001` reports missing provenance.
- `PKG10002` reports unverified dependency provenance.
- `PKG10003` reports unknown builder.
- `PKG10004` reports untracked generated source.
- `PKG10005` reports undeclared binary blob.
- `PKG10006` reports revoked supply-chain input.
- `PKG10007` reports provenance/artifact mismatch.
- `PKG10008` reports unknown provenance schema.
- `PKG10009` reports incomplete or policy-incompatible keyless signing
  provenance.
- `PKG10010` reports missing or unverifiable transparency log evidence.

Diagnostics include artifact id, package id, builder id, dependency id, source
hash, signing identity, OIDC issuer, certificate id, transparency log entry,
revocation id, and policy rule.

## Conformance Criteria

- A release artifact includes provenance linked to its manifest.
- A dependency with missing provenance blocks release.
- A model-generated patch is marked generated and rechecked.
- Revoked inputs are detected before signing or publishing.
- Keyless signing provenance records issuer, subject, audience, certificate
  identity, transparency log inclusion proof, and root-of-trust metadata.
- Binary blobs are recorded with hash and policy metadata.
- Provenance verification fails closed on unknown schema versions.
- The provenance record can reconstruct the source, dependency, builder, compiler, and policy inputs for the artifact.
