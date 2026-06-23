# PKG3 - Artifact Model Specification

Sequence: 167
Phase: 12 - Build, Package and Artifact System
Status: Manual Draft
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

This specification defines Gravity artifacts. An artifact is any typed output
of compilation, packaging, testing, runtime recording, documentation, or
governance that another tool may consume. Binaries, libraries, schemas, MIR,
workflow graphs, HDL modules, agent manifests, proof certificates, SBOMs,
diagnostics, benchmark reports, and generated docs all use the artifact model.

Artifacts make Gravity's language thesis inspectable: target-specific outputs
remain connected to source, types, effects, profiles, capabilities, safety
evidence, dependencies, and provenance.

## Artifact Identity

An artifact identity includes:

- artifact kind;
- schema version;
- package id and version;
- source graph hash;
- project manifest hash;
- lockfile hash;
- compiler identity;
- profile and target;
- build policy hash;
- content hash;
- dependency graph hash;
- evidence summary hash.

The identity is canonicalized according to `S3` before signing or comparison.

## Requirements

- Every emitted artifact MUST have a manifest.
- Artifact manifests MUST be schema-validated.
- Artifacts derived from source MUST link to source graph and compiler identity.
- Artifacts that depend on profile or target choices MUST record those choices.
- Artifacts that can affect runtime authority MUST record capability and policy summaries.
- Artifacts that claim safety MUST link to safety evidence.
- Artifact content hashes MUST be computed over canonical bytes or a declared target format.
- Generated artifacts MUST record generator identity and inputs.
- Release artifacts MUST support signing and SBOM linkage.
- Artifact consumers MUST verify schema version and required evidence before use.

## Artifact Kinds

Required artifact kinds include:

- `:library`;
- `:executable`;
- `:object`;
- `:schema`;
- `:mir`;
- `:domain-ir`;
- `:workflow-graph`;
- `:agent-manifest`;
- `:runtime-ledger`;
- `:proof-certificate`;
- `:test-report`;
- `:benchmark-report`;
- `:documentation`;
- `:sbom`;
- `:signature`;
- `:provenance`.

Targets may define additional kinds, but each kind needs a schema and
conformance fixture.

## Semantic Dependencies

- `S9` defines artifact schema conventions.
- `B13` defines backend artifact emission.
- `C15` defines diagnostics.
- `R12` defines runtime diagnostic artifacts.
- `PKG7`, `PKG10`, and `PKG12` define reproducibility, provenance, signing, and SBOM links.
- `TEST1` through `TEST13` define test report artifacts.

## Outputs and Artifacts

This document itself requires these emitted records:

- artifact schema registry;
- artifact kind registry;
- canonical identity algorithm;
- manifest validation rules;
- consumer verification rules;
- evidence link rules;
- conformance fixtures for valid and invalid manifests.

## Example

```clojure
(artifact
  {:kind :workflow-graph
   :schema GravityWorkflowGraph/v1
   :package acme/support-agent
   :profile :ai
   :target :workflow-graph
   :source-graph "blake3:source"
   :lockfile "blake3:lock"
   :compiler "gravityc:0.1.0"
   :content "blake3:graph"
   :evidence {:types "blake3:type-report"
              :effects "blake3:effect-report"
              :safety "blake3:safety-report"}})
```

## Rejection Rules

- Reject artifacts with no manifest.
- Reject manifests with unknown schema versions.
- Reject release artifacts missing source, lockfile, compiler, profile, target, or content hash.
- Reject safety claims with no evidence link.
- Reject capability-bearing artifacts with no capability summary.
- Reject signing over noncanonical manifest data.
- Reject generated artifacts with no generator identity.
- Reject consumers that ignore required artifact evidence.

## Diagnostics

- `PKG3001` reports missing artifact manifest.
- `PKG3002` reports schema mismatch.
- `PKG3003` reports missing identity field.
- `PKG3004` reports content hash mismatch.
- `PKG3005` reports missing evidence link.
- `PKG3006` reports unknown artifact kind.
- `PKG3007` reports noncanonical signed data.
- `PKG3008` reports unverified consumer use.

Diagnostics include artifact id, kind, schema, manifest field, content hash,
consumer id, and required evidence.

## Conformance Criteria

- A valid artifact manifest round-trips through canonical form.
- An artifact missing source hash is rejected.
- A content hash mismatch is detected before consumption.
- A capability-bearing artifact exposes its capability summary.
- A release artifact links to provenance, SBOM, and signature records.
- A generated artifact records generator identity and input hashes.
- Artifact consumers fail closed on unknown schema versions.
