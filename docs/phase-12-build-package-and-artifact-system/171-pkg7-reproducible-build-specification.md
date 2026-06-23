# PKG7 - Reproducible Build Specification

Sequence: 171
Phase: 12 - Build, Package and Artifact System
Status: Manual Draft
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

This specification defines reproducible builds for Gravity packages and
artifacts. A reproducible build is one where the same declared inputs produce
the same declared outputs under the same build recipe. Gravity does not assume
the host environment is harmless; it records and controls all inputs that can
affect artifacts.

Reproducibility supports supply-chain security, bootstrap trust reduction,
artifact signing, conformance, and governance review.

## Controlled Inputs

A reproducible build controls:

- source graph hash;
- project manifest hash;
- lockfile hash;
- compiler and toolchain identity;
- build graph and task settings;
- profile and target matrix;
- environment variables allowed into the build;
- locale, timezone, filesystem order, and timestamp policy;
- random seeds;
- network policy;
- generated-source inputs;
- external binary blobs and FFI headers.

Uncontrolled input is rejected or recorded as making the artifact
non-reproducible.

## Requirements

- Release builds MUST emit a reproducible build recipe.
- Reproducible builds MUST run with locked dependencies.
- Build timestamps MUST be fixed, normalized, or excluded from content hashes.
- Filesystem traversal order MUST be deterministic.
- Network access after dependency fetch MUST be denied unless recorded as a declared input.
- Randomness MUST be seeded or recorded.
- Host paths MUST NOT appear in output artifacts unless normalized.
- Generated source MUST carry input hashes and generator identity.
- Rebuild verification MUST compare canonical artifact manifests and content hashes.
- Non-reproducible artifacts MUST be marked as such and excluded from reproducible release claims.

## Build Recipe

The recipe records:

- package and version;
- source, project, and lockfile hashes;
- compiler and toolchain ids;
- environment profile;
- target matrix;
- build graph hash;
- allowed external inputs;
- expected artifact ids;
- output hashes;
- verification command or machine-readable equivalent.

The recipe is itself an artifact and may be signed.

## Semantic Dependencies

- `PKG1` defines project inputs.
- `PKG2` defines build graph behavior.
- `PKG3` defines artifact identity.
- `PKG5` defines locked dependency graphs.
- `PKG10` defines provenance.
- `PKG12` defines signing and SBOM links.
- `BOOT6` defines trusting-trust and reproducible bootstrap requirements.

## Outputs and Artifacts

The build emits:

- reproducible build recipe;
- controlled environment manifest;
- input digest list;
- output digest list;
- rebuild verification report;
- non-reproducibility exception list when applicable.

## Example

```clojure
(reproducible-build
  {:package acme/support-agent
   :inputs {:source "blake3:source"
            :project "blake3:project"
            :lockfile "blake3:lock"
            :compiler "gravityc:0.1.0"}
   :environment {:time :fixed
                 :locale "C"
                 :network :disabled}
   :targets [:jvm-21 :workflow-graph]
   :expected [:library :agent-manifest :workflow-graph]})
```

## Rejection Rules

- Reject release reproducibility claims without a build recipe.
- Reject unlocked dependencies.
- Reject unrecorded network reads.
- Reject unseeded random input in reproducible mode.
- Reject host path leakage into artifacts.
- Reject generated source with no generator input hash.
- Reject rebuild verification based only on filenames.
- Reject signing of artifacts marked non-reproducible when policy requires reproducibility.

## Diagnostics

- `PKG7001` reports missing build recipe.
- `PKG7002` reports unlocked dependency.
- `PKG7003` reports uncontrolled network input.
- `PKG7004` reports unseeded randomness.
- `PKG7005` reports host path leakage.
- `PKG7006` reports generated-source reproducibility gap.
- `PKG7007` reports rebuild hash mismatch.
- `PKG7008` reports invalid reproducible release claim.

Diagnostics include recipe id, input category, artifact id, target, output hash,
and environment field.

## Conformance Criteria

- Running the same locked build twice under the same recipe yields identical artifact hashes.
- Changing compiler identity invalidates the reproducible build cache key.
- A network read after dependency fetch is rejected.
- Host path leakage is detected in a fixture artifact.
- Generated source records generator identity and input hashes.
- Rebuild verification compares manifest and content hashes.
- Non-reproducible artifacts cannot satisfy a reproducible release gate.
