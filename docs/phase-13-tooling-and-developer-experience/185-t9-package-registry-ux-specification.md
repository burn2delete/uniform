# T9 - Package Registry UX Specification

Sequence: 185
Phase: 13 - Tooling and Developer Experience
Status: Manual Draft
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

This specification defines the user experience for package registry inspection
and interaction. Registry UX must expose package capabilities, profiles,
targets, unsafe metadata, provenance, signatures, SBOMs, yanks, advisories, and
policy impact clearly enough for developers and CI to make safe dependency
choices.

Registry UX exists in CLI, editor, web, and API forms. The same structured data
feeds all views.

## Registry Views

Views include:

- package overview;
- version history;
- dependency graph;
- capability summary;
- unsafe and audit summary;
- target support matrix;
- provenance and signing status;
- SBOM view;
- vulnerability/advisory view;
- update diff;
- policy compatibility report;
- private and latent package state.

## Requirements

- Registry views MUST expose structured data for automation.
- Package pages MUST show supported profiles and targets.
- Capability summaries MUST be visible before install or update.
- Unsafe and audit metadata MUST be visible when present.
- Update diffs MUST show capability, safety, dependency, target, provenance, and license/policy changes.
- Verification views MUST show signature, SBOM, provenance, and revocation status.
- Private registry UX MUST avoid leaking package existence to unauthorized users.
- Latent package UX MUST show state and required review before resolution.
- Registry search MUST support profile, target, capability, and safety filters.
- CLI and web/API views MUST agree on package facts.

## Semantic Dependencies

- `PKG4` defines package manager operations.
- `PKG6` defines capability manifests.
- `PKG8` defines safety metadata.
- `PKG9` defines private and latent registries.
- `PKG10` defines provenance.
- `PKG11` defines target matrix.
- `PKG12` defines signing and SBOM.
- `GOV10` defines package governance.

## Outputs and Artifacts

Registry UX emits:

- package detail JSON;
- update diff report;
- verification report;
- search result records;
- access denial records;
- policy compatibility reports.

## Example

```bash
gravity registry inspect gravity/http --show-capabilities --show-sbom --format json
gravity registry diff gravity/http 2.0.0 2.1.0 --show-policy-impact
gravity registry search --profile hosted --target wasm32-wasi --deny-capability shell/exec
```

## Rejection Rules

- Reject registry views that hide capability expansion in update diffs.
- Reject install recommendations for packages incompatible with active policy.
- Reject exposing private package metadata to unauthorized users.
- Reject verification badges without machine-verifiable reports.
- Reject package search results that ignore requested profile or target filters.
- Reject latent package resolution UI without state and review evidence.

## Diagnostics

- `T9001` reports hidden capability diff.
- `T9002` reports policy-incompatible recommendation.
- `T9003` reports private metadata leak.
- `T9004` reports unverifiable verification claim.
- `T9005` reports search filter violation.
- `T9006` reports latent package state omission.

## Conformance Criteria

- Registry JSON and human views report the same package facts.
- Update diff fixtures show capability and unsafe metadata changes.
- Private package requests deny without metadata leakage.
- Search respects profile, target, capability, and safety filters.
- Verification badges link to signature, provenance, SBOM, and revocation reports.
- Latent package views show state, generator provenance, and review requirement.
- CI can consume registry compatibility reports without screen scraping.
