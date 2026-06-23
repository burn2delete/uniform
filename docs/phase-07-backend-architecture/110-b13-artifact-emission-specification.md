# B13 - Artifact Emission Specification

Sequence: 110
Phase: 7 - Backend Architecture
Status: Manual Draft 3
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

Artifact emission is the common contract used by every backend to turn compiler
truth into inspectable build outputs. It emits target artifacts plus manifests,
hashes, provenance, source/debug maps, safety evidence, proof links,
capability/effect summaries, dependency records, reproducibility data, and
conformance evidence.

Backends may emit different artifact kinds, but they do not get different rules
for evidence. A binary, C file, LLVM module, Wasm component, JAR, JavaScript
package, GPU kernel, HDL module, workflow graph, SQL migration, or mobile bundle
must be traceable to source, profile, target, compiler passes, runtime
providers, dependencies, safety decisions, and diagnostics.

## Requirements

- Every emitted artifact must have a schema version, kind, backend, profile,
  target, content hash, source provenance, compiler provenance, dependency
  provenance, and build identity.
- Artifact manifests must include effects, capabilities, unsafe audit records,
  safety outcomes, proof/certificate references, runtime provider references,
  target features, ABI/layout records, and diagnostics.
- Source maps must preserve source spans and generated-origin chains from read
  syntax through macro expansion, core, MIR/domain IR, lowering, and backend
  emission.
- Release-grade artifacts must include reproducibility inputs and conformance
  evidence required by policy.
- Missing, stale, or contradictory evidence rejects artifact emission.
- Generated artifacts must identify the generator, input digests, pass pipeline,
  backend version, and any manual or unsafe approvals.
- Artifacts must be content-addressable before signing, packaging, deployment,
  or governance workflows consume them.

## Dependencies

- `B1` through `B12` define backend-specific artifact kinds and metadata.
- `C1`, `C11`, `C14`, and `C18` define compiler pipeline, MIR, target lowering,
  and verification records.
- `SAFE15`, `PERF10`, and Phase 5 math documents define proof and certificate
  evidence.
- Profile documents define artifact eligibility and profile compatibility.
- Runtime, package, release, tooling, and conformance phases consume emitted
  manifests.

## Outputs and Artifacts

- Gravity artifact manifest.
- Artifact graph.
- Content hash record.
- Source/debug map.
- Compiler provenance record.
- Dependency provenance record.
- Safety/proof/certificate bundle reference.
- Effect and capability summary.
- Runtime/provider summary.
- Target feature and ABI/layout summary.
- Reproducibility record.
- Conformance evidence reference.
- Artifact emission diagnostics.

## Artifact Manifest

```clojure
{:artifact :gravity/artifact-manifest
 :schema-version 1
 :kind :llvm-ir
 :backend :gravity.backend/llvm
 :profile :native
 :target target-fingerprint
 :content-hash "sha256:..."
 :inputs {:source source-digest
          :mir mir-digest
          :lowering lowering-digest}
 :evidence {:safety safety-bundle-id
            :proofs proof-bundle-id
            :capabilities capability-summary-id
            :effects effect-summary-id
            :conformance conformance-pack-id}
 :provenance {:compiler compiler-build-id
              :passes pass-pipeline-id
              :dependencies dependency-graph-id}}
```

The schema is stable enough for tools to validate without loading backend
implementation code.

## Artifact Graph

The artifact graph records:

- source files and forms,
- generated syntax and macro origins,
- checked core,
- MIR and domain IR,
- analysis artifacts,
- optimization passes,
- lowering requests,
- backend artifacts,
- runtime helper artifacts,
- package artifacts,
- conformance packs.

Edges carry digests, pass names, generator identity, profile/target identity,
and invalidation rules. Tools must be able to answer which source form and which
proof justified an emitted target construct.

## Metadata Requirements

Each artifact kind declares required metadata:

- source and generated-origin map,
- type/effect/capability summary,
- profile and compatibility record,
- target feature and ABI/layout record,
- runtime/provider dependencies,
- unsafe audit references,
- safety/proof/certificate references,
- diagnostics and warnings,
- dependency provenance,
- conformance evidence,
- reproducibility inputs.

Backend-specific manifests may add fields, but they may not remove common
fields without an explicit profile and tooling exemption.

## Reproducibility and Hashing

Hash records include:

- artifact bytes or canonical representation,
- manifest digest,
- source digest,
- compiler build id,
- pass pipeline digest,
- target toolchain digest,
- environment inputs that affect output,
- nondeterminism records,
- timestamp policy.

Artifacts that cannot be reproduced exactly must record the nondeterminism and
policy reason. Content hashes are computed before signing or packaging phases.

## Diagnostics and Evidence

Artifact emission validates that diagnostics and evidence are complete. A
backend cannot suppress a lowering diagnostic by still producing bytes. Warnings,
rejections, proof gaps, unsafe audit requirements, and target assumptions remain
attached to the emitted graph.

If evidence is intentionally unavailable for a development build, the manifest
must mark the artifact as non-release and state which downstream operations are
blocked.

## Diagnostics

Artifact emission diagnostics use `B13` identifiers:

- `B13-SCHEMA` for missing or unsupported artifact manifest schema.
- `B13-HASH` for missing, stale, or mismatched content hashes.
- `B13-PROVENANCE` for missing source, compiler, generator, pass, or dependency
  provenance.
- `B13-SOURCEMAP` for incomplete source or generated-origin maps.
- `B13-EVIDENCE` for missing safety, proof, certificate, effect, capability, or
  unsafe-audit records.
- `B13-TARGET` for missing target, ABI, layout, runtime, or provider metadata.
- `B13-CONFORMANCE` for missing conformance evidence required by policy.
- `B13-REPRODUCIBILITY` for unrecorded nondeterminism or environment inputs.
- `B13-RELEASE` for release-grade emission attempted with development-only
  evidence.
- `B13-GRAPH` for invalid or incomplete artifact graph edges.

Diagnostics must include artifact id, artifact kind, backend, profile, target,
missing or stale field, source span or generated-origin edge when available, and
remediation.

## Rejected Designs

Gravity rejects emitted bytes without typed manifests.

Gravity rejects release artifacts without content hashes, provenance, safety
evidence, and conformance references required by policy.

Gravity rejects source maps that stop before macro expansion, MIR, or backend
emission.

Gravity rejects generated artifacts whose generator identity and input digests
are not recorded.

Gravity rejects treating backend-specific manifests as replacements for the
common artifact graph.

## Conformance Criteria

A conforming artifact emitter must demonstrate:

- common manifest schema validation across all Phase 7 backend artifact kinds,
- artifact graph generation from source through backend emission,
- content hash and digest mismatch rejection,
- source/debug/generated-origin map preservation,
- safety/proof/certificate/effect/capability evidence checks,
- runtime/provider/target/ABI metadata checks,
- reproducibility records and nondeterminism diagnostics,
- release-grade evidence gating,
- downstream consumption by package, tooling, and conformance fixtures.
