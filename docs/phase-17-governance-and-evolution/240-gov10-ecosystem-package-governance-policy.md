# GOV10 - Ecosystem Package Governance Policy

Sequence: 240
Phase: 17 - Governance and Evolution
Status: Normative draft
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

The ecosystem package governance policy defines how Gravity package registries, namespaces, releases, yanks, advisories, provenance, capability manifests, safety metadata, and conformance claims are governed.
Packages are part of Gravity's artifact system.
They can introduce effects, capabilities, unsafe code, target assumptions, generated code, AI tools, and supply-chain risk.

The policy protects package users without freezing ecosystem growth.
It requires identity, provenance, reproducibility, capabilities, safety metadata, target/profile declarations, and advisory handling for registry participation.

## Requirements

- Registry packages MUST have stable identity, owner, namespace, version, license metadata, provenance, and signing status.
- Releases MUST include package manifest, lock metadata where applicable, capability manifest, profile matrix, target matrix, artifact hashes, and SBOM.
- Packages claiming conformance MUST include conformance reports tied to compiler, target, profile, and standard-library versions.
- Packages exposing unsafe code MUST include GOV9 unsafe metadata.
- Packages exposing effects or capabilities MUST make authority visible in manifests.
- Packages exposing AI tools or agents MUST include Phase 11 policy, eval, `:ai/human-review`, and prompt-injection evidence.
- Registry yanks MUST be recorded with reason, replacement guidance, and dependency-resolution behavior.
- Security advisories MUST include affected versions, severity, impact, patched versions, mitigations, and provenance links.
- Namespace transfer, abandoned package takeover, and reserved names MUST be governed.
- Registry tooling MUST reject unsigned, nonreproducible, capability-hidden, or malicious releases according to policy.

## Governed Package Surfaces

- Package identity, namespace, ownership, and versioning.
- Source archives, generated artifacts, binaries, schemas, docs, tools, workflows, and agents.
- Dependency resolution, lockfiles, registries, mirrors, yanks, and advisories.
- Capability manifests, effects, unsafe code, FFI, host delegation, and target support.
- Provenance, signatures, SBOMs, reproducibility, and build attestations.
- License metadata and policy compatibility.
- Conformance claims and test reports.
- Namespace disputes, transfers, reserved names, and abandoned packages.

## Dependencies

- `PKG1` through `PKG12` for project files, build graph, artifact identity, package manager, dependency resolution, capabilities, reproducibility, safety metadata, registries, provenance, targets, signing, and SBOMs.
- `SAFE6`, `SAFE7`, `SAFE10`, `SAFE11`, `SAFE13`, `SAFE14`, `SAFE15`, and `SAFE16` for unsafe and FFI metadata, capabilities, taint, AI tool safety, supply-chain safety, proof evidence, and conformance.
- `A1` through `A11` for AI package surfaces.
- `STD20` and `GOV3` for standard-library package governance.
- `GOV2`, `GOV4`, and `GOV9` for compatibility, security, and unsafe code.
- `TEST1`, `TEST4`, `TEST7`, and `TEST13` for conformance claims.

## Package Governance Record

```clojure
{:package "gravity/http"
 :version "1.2.0"
 :owners ["net-working-group"]
 :profiles #{:hosted :native :distributed}
 :targets #{:jvm :native-elf :wasm-component}
 :evidence [:signature :sbom :capability-manifest :conformance-report]
 :unsafe-islands []
 :advisories []}
```

The record is consumed by registries, package managers, policy engines, conformance tooling, and release auditors.

## Registry Decisions

- Accept release when identity, ownership, signature, provenance, capabilities, profile matrix, and policy checks pass.
- Reject release when package metadata is incomplete, unsigned, nonreproducible, malicious, or authority-hidden.
- Quarantine release when automated checks identify unresolved security or provenance risk.
- Yank release when a published version should not be newly selected but must remain reproducible for existing locks.
- Reserve namespace for standard-library, governance, security, or ecosystem reasons.
- Transfer namespace only with owner verification and audit record.
- Publish advisory when a vulnerability or supply-chain issue affects package consumers.

## Outputs and Artifacts

- Registry package records with identity, owners, namespace, version, profiles, targets, and evidence.
- Signed package manifests, provenance, SBOMs, and artifact hashes.
- Capability, unsafe, AI, FFI, host delegation, and target metadata.
- Conformance reports tied to compiler, runtime, profile, target, and standard-library versions.
- Yank records, advisory records, namespace-transfer records, and quarantine records.
- Malware, dependency-confusion, license-policy, and reproducibility scan results.
- Compatibility and migration records for package-breaking changes.

## Rejection Rules

- Reject unsigned packages when registry policy requires signatures.
- Reject releases without provenance, SBOM, artifact hashes, or capability manifest.
- Reject packages that hide unsafe code, FFI, AI tools, or host delegation.
- Reject conformance claims without test reports.
- Reject namespace transfers without owner verification.
- Reject packages with dependency-confusion risk not mitigated by namespace policy.
- Reject releases that violate active security advisories or yanked dependency policy.
- Reject standard-library impersonation or reserved-name abuse.

## Diagnostics

- `GOV10001` when package identity, owner, namespace, or version metadata is incomplete.
- `GOV10002` when signing, provenance, SBOM, or artifact hashes are missing.
- `GOV10003` when capability metadata is absent for effectful packages.
- `GOV10004` when unsafe, FFI, AI, or host-delegation metadata is hidden.
- `GOV10005` when conformance claims lack reports.
- `GOV10006` when yanks or advisories are missing required impact and migration data.
- `GOV10007` when namespace transfer or reserved-name use lacks governance records.
- `GOV10008` when registry policy detects dependency-confusion or malicious release risk.

## Conformance Criteria

- Registry tooling can validate package identity, signatures, provenance, SBOMs, capabilities, and profile matrices.
- Package managers can enforce yanks, advisories, compatibility metadata, and capability policy.
- Conformance claims are traceable to test reports and artifact hashes.
- Unsafe and AI package surfaces are visible before installation.
- Namespace transfers, abandoned package takeovers, and reserved names have audit records.
- Reproducible builds can recreate released package artifacts.
- Security advisories and yanks do not break existing reproducible locks without explicit emergency policy.
