# PKG9 - Private Registry and Latent Package Space Design

Sequence: 173
Phase: 12 - Build, Package and Artifact System
Status: Manual Draft
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

This design defines private registries and latent package spaces. A private
registry restricts package metadata and artifact access to authorized
principals. A latent package space reserves names and generated package
candidates before they become ordinary dependencies. This supports internal
packages, AI-generated package proposals, experimental APIs, and staged review
without making unresolved or unreviewed packages silently available.

Resolution must distinguish public published packages, private published
packages, reserved names, generated candidates, reviewed candidates, and
revoked candidates.

## Registry Records

A registry stores:

- package index records;
- artifact manifests and content hashes;
- signatures and revocation status;
- access policy;
- publish policy;
- yank log;
- latent package records;
- review state;
- provenance links;
- mirror state.

Index records are signed according to registry policy.

## Latent Package States

Latent package states are:

- `:reserved`;
- `:generated`;
- `:review-requested`;
- `:reviewed`;
- `:published`;
- `:rejected`;
- `:revoked`.

Only `:published` packages resolve normally. Other states require explicit
latent grants and policy allowing that state.

## Requirements

- Private registry reads MUST require registry access grants.
- Private package metadata MUST not leak through public resolution errors.
- Registry indexes MUST be signed when policy requires signatures.
- Latent package names MUST not satisfy ordinary dependency constraints.
- Generated package candidates MUST record generator identity and input hashes.
- Review state MUST be recorded before latent packages become resolvable.
- Publish operations MUST preserve provenance and safety metadata.
- Yank and revocation records MUST remain visible for reproducible historical builds.
- Mirrors MUST preserve registry signatures or record mirror attestations.
- Dependency resolution MUST record registry source for every selected package.

## Semantic Dependencies

- `PKG4` defines package manager operations.
- `PKG5` defines dependency resolution.
- `PKG8` defines safety metadata.
- `PKG10` defines generated package provenance.
- `PKG12` defines signing.
- `A11` defines generated or AI-assisted package safety implications.
- `GOV10` defines ecosystem package governance.

## Outputs and Artifacts

Registry operations emit:

- access decision record;
- index signature verification record;
- latent package state transition record;
- publish record;
- yank or revocation record;
- mirror attestation;
- registry provenance record.

## Example

```clojure
(registry
  {:name acme/private
   :visibility :organization
   :access {:read :org-member
            :publish :release-manager}
   :latent-space {:prefix acme.experimental/*
                  :resolve :explicit-grant-only
                  :publish :review-required}
   :signatures :required})
```

## Rejection Rules

- Reject private registry access with no grant.
- Reject public error messages that reveal private package existence.
- Reject unsigned registry indexes when signatures are required.
- Reject latent packages as ordinary dependencies.
- Reject generated package candidates without generator provenance.
- Reject latent publish without review state transition.
- Reject mirrors that cannot verify original signatures or emit mirror attestations.
- Reject lockfile records missing registry source.

## Diagnostics

- `PKG9001` reports private registry access denial.
- `PKG9002` reports private metadata leak.
- `PKG9003` reports registry signature failure.
- `PKG9004` reports latent package resolution denial.
- `PKG9005` reports missing generated package provenance.
- `PKG9006` reports publish without review.
- `PKG9007` reports mirror verification failure.
- `PKG9008` reports lockfile registry omission.

Diagnostics include registry id, package id, principal scope, package state,
signature id, and policy rule.

## Conformance Criteria

- A private package requires read authorization before metadata is returned.
- Public resolution does not reveal private package existence.
- A latent package cannot satisfy normal constraints.
- A generated candidate records generator identity and input hashes.
- Publishing a latent candidate requires review state.
- Mirror verification preserves signature or mirror attestation evidence.
- Lockfiles identify the registry source for every dependency.
