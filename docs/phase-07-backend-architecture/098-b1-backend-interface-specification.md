# B1 - Backend Interface Specification

Sequence: 98
Phase: 7 - Backend Architecture
Status: Manual Draft 2
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

The backend interface is the stable contract between verified Gravity compiler
artifacts and target emitters. Backends consume verified MIR or verified domain
IR plus profile, target, ABI, runtime, provider, safety, proof, effect, and
capability manifests. They emit target artifacts and metadata without changing
Gravity semantics.

This document defines the interface every backend implements. Concrete backend
documents define target-specific details.

## Requirements

- Backends must accept only verified MIR or verified domain IR.
- Backend input must include profile manifest, target manifest, ABI policy,
  runtime/provider selection, effect summary, capability proof summary, safety
  bundle, and source/proof maps.
- Backends must reject unsupported MIR operations, unsupported domain IR,
  unsupported profile behavior, missing proofs, and missing capabilities.
- Backends must not depend on target undefined behavior for safe Gravity code.
- Target metadata and assumptions must be justified by Gravity proof artifacts.
- Emitted artifacts must include provenance, source/debug maps where meaningful,
  profile, target, effects, capabilities, safety evidence, unsafe audit ids,
  dependency graph, and conformance metadata.
- Backend diagnostics must name MIR operation or domain anchor, source span,
  target rule, missing evidence, and remediation.

## Dependencies

- `C11` defines MIR.
- `C12` defines domain IRs.
- `C14` defines target lowering requests and proof-to-metadata maps.
- `P1` and `P13` define profile legality and compatibility.
- `SAFE1`, `SAFE15`, and `PERF10` define safety outcomes, certificates, and
  check elision evidence.
- Runtime and package phases consume backend artifacts.

## Outputs and Artifacts

- Backend implementation manifest.
- Backend input eligibility report.
- Target artifact manifest.
- ABI and layout record.
- Runtime/provider dependency record.
- Proof-to-target metadata map.
- Source/debug map.
- Unsupported-feature report.
- Backend diagnostics.
- Backend conformance record.

## Backend Manifest

```clojure
{:artifact :gravity/backend-manifest
 :backend :gravity.backend/interface-v1
 :version "1"
 :accepts #{:gravity/mir :gravity/domain-ir}
 :emits #{:object :library :bytecode :source :workflow-graph :hdl :query-plan}
 :requires #{:profile :target :abi :runtime :effects :capabilities :safety}
 :supports-profiles #{:core :hosted :native :firmware :kernel :gpu
                     :hardware :distributed :ai}
 :rejects #{:unverified-ir :unsupported-op :missing-proof
            :implicit-ub :ambient-capability :profile-violation}}
```

Concrete backends extend this manifest with target-specific supported artifact
kinds and feature matrices.

## Input Contract

A backend input packet contains:

```clojure
{:input {:kind :gravity/mir :id optimized-mir-hash}
 :profile profile-manifest-id
 :target target-manifest-id
 :abi abi-policy-id
 :runtime runtime-manifest-id
 :providers provider-selection-id
 :effects effect-summary-id
 :capabilities capability-summary-id
 :safety safety-bundle-id
 :proofs proof-table-id
 :source-map source-map-id
 :dependencies dependency-graph-id}
```

Any missing or stale field rejects backend execution.

## Eligibility

Backend eligibility checks:

- profile/backend compatibility,
- target feature support,
- runtime availability or no-runtime proof,
- ABI representability,
- layout representability,
- provider availability,
- effect and capability preservation,
- safety bundle completeness,
- proof validity for target assumptions,
- source/debug map preservation capability.

Eligibility is emitted before artifact generation so tools can explain target
matrix failures.

## Emission Contract

Emitted target artifacts must be accompanied by:

- artifact kind and digest,
- backend identity and version,
- source input digest,
- profile and target fingerprint,
- ABI/layout manifest,
- runtime/provider manifest,
- safety/proof/capability summaries,
- source/debug maps,
- unsupported-feature and fallback records,
- diagnostics,
- conformance fixture references.

Backends may emit several artifacts for one input, such as object code,
metadata, debug maps, and package manifests.

## Target Assumptions

Backends must justify assumptions such as:

- no signed overflow,
- no alias,
- dereferenceable pointers,
- alignment,
- volatile and atomic ordering,
- floating rounding and fast-math flags,
- trap/panic lowering,
- tail-call legality,
- host exception mapping,
- GC or no-GC behavior.

The proof-to-target metadata map names the Gravity proof or certificate for each
assumption.

## Diagnostics

Backend interface diagnostics use `B1` identifiers:

- `B1-INPUT` for unverified or incomplete backend inputs.
- `B1-PROFILE` for profile/backend incompatibility.
- `B1-TARGET` for unsupported target features.
- `B1-ABI` for ABI or layout gaps.
- `B1-RUNTIME` for missing or forbidden runtime services.
- `B1-PROOF` for target assumptions without proof.
- `B1-CAPABILITY` for authority not preserved by provider selection.
- `B1-UNSUPPORTED` for unsupported MIR or domain operations.
- `B1-METADATA` for missing provenance, source maps, or safety metadata.

Diagnostics must include backend id, input artifact id, MIR op or domain anchor,
source span, profile, target, missing evidence, fallback status, and remediation.

## Rejected Designs

Gravity rejects backends that accept unchecked IR.

Gravity rejects target undefined behavior as an implementation shortcut.

Gravity rejects backend metadata not backed by compiler evidence.

Gravity rejects emitted artifacts without provenance and safety metadata.

Gravity rejects backend support described only in prose.

## Conformance Criteria

A conforming backend interface implementation must demonstrate:

- manifest validation,
- verified input acceptance and unverified input rejection,
- profile and target eligibility reports,
- proof-backed target metadata,
- unsupported-operation diagnostics,
- artifact manifest emission,
- metadata preservation through emission,
- backend conformance records for positive and negative fixtures.
