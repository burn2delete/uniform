# S9 - Artifact Schema Specification

Sequence: 153
Phase: 10 - Schema, Data and Interop
Status: Manual Draft 3
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

Artifact schemas define the machine-readable shape of every Gravity output:
binaries, libraries, firmware images, kernel modules, Wasm modules, JS bundles,
JARs, mobile apps, workflow graphs, agent manifests, schemas, migrations, HDL,
proofs, tests, packages, docs, and release evidence.

An artifact manifest is a schema-validated contract, not an informal build log.

## Requirements

- Artifact schemas must include kind, schema version, source graph, compiler
  version, profile, target, backend, runtime, effects, capabilities,
  dependencies, content hash, provenance, safety evidence, proof/certificate
  references, diagnostics, and conformance state.
- Artifact manifests must use canonical encoding when used for hashes, signing,
  replay, or release decisions.
- Release artifacts must include all evidence required by policy.
- Unsafe islands, model/tool policies, capability grants, secret redaction,
  runtime services, and generated-code provenance must appear when relevant.
- Artifact graph cycles require bootstrap/self-hosting provenance.
- Schema version changes require compatibility and migration records.

## Dependencies

- `S1` and `S3` define source schemas and canonical data.
- `B13` defines backend artifact emission.
- Package, testing, governance, tooling, AI, workflow, and release phases consume
  artifact schemas.

## Outputs and Artifacts

- Artifact schema manifest.
- Artifact manifest validator.
- Artifact graph schema.
- Content hash schema.
- Provenance schema.
- Evidence schema.
- Release gate schema.
- Artifact compatibility report.
- Artifact schema diagnostics.

## Artifact Schema

```clojure
{:artifact :gravity/artifact-schema
 :schema-version 1
 :required #{:kind :profile :target :source-hash :content-hash
             :schema-version :conformance}
 :evidence #{:types :effects :capabilities :safety :proofs :tests}
 :provenance #{:compiler-version :source-graph :dependency-lock :passes}}
```

## Artifact Kinds

Artifact kinds include source-derived schemas, MIR/domain IR, backend outputs,
runtime manifests, package manifests, conformance packs, proof objects, workflow
graphs, agent manifests, generated APIs, migrations, HDL modules, mobile
bundles, documentation, and governance records. Each kind may add required
fields but must preserve common provenance and evidence fields.

## Diagnostics

Artifact schema diagnostics use `S9` identifiers:

- `S9-SCHEMA` for missing or unsupported artifact schema version.
- `S9-REQUIRED` for missing required artifact fields.
- `S9-HASH` for missing or invalid content/source hashes.
- `S9-PROVENANCE` for incomplete source/compiler/dependency/pass provenance.
- `S9-EVIDENCE` for missing safety, proof, effect, capability, test, or
  conformance evidence.
- `S9-CANONICAL` for hash/signature inputs not using canonical encoding.
- `S9-CYCLE` for artifact graph cycles without bootstrap provenance.
- `S9-COMPATIBILITY` for schema version changes without migration policy.

Diagnostics must include artifact id, kind, schema version, missing field,
source or graph edge when available, release gate, and remediation.

## Rejected Designs

Gravity rejects untyped artifact manifests.

Gravity rejects release artifacts without required provenance and evidence.

Gravity rejects artifact hashes over noncanonical data.

Gravity rejects artifact graph cycles without bootstrap explanation.

Gravity rejects hiding unsafe, AI/tool, capability, or runtime decisions outside
the artifact schema.

## Conformance Criteria

A conforming artifact schema system must demonstrate:

- validators for all Gravity artifact kinds,
- common provenance and evidence fields,
- canonical encoding for hash/signature contexts,
- release gate validation,
- graph cycle and bootstrap provenance checks,
- schema version compatibility fixtures,
- rejection of incomplete, noncanonical, and evidence-free manifests.
