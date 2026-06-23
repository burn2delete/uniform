# B7 - MLIR Backend Design

Sequence: 104
Phase: 7 - Backend Architecture
Status: Manual Draft 3
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

The MLIR backend emits MLIR modules, dialect-specific IR, verifier reports,
conversion pipelines, pass logs, and downstream handoff artifacts for compiler
experimentation, accelerator lowering, affine/vector optimization, tensor and
math domains, and staged lowering into LLVM, SPIR-V, GPU, or custom targets.

MLIR is an interchange and optimization framework for Gravity. It may host
Gravity-specific dialects and consume standard dialects, but it must not become
an alternate semantics for safe Gravity. Every dialect operation must preserve or
explicitly map source, type, effect, capability, ownership, safety, proof,
profile, and diagnostic facts.

## Requirements

- Input must be verified MIR or verified domain IR accepted by `B1` and `C14`.
- The backend must declare MLIR version, dialect registry, operation coverage,
  conversion targets, legality checks, and pass pipeline.
- Gravity-specific dialects must define type, effect, memory, capability,
  source-map, proof, and profile attributes.
- Standard dialect use must include a mapping from Gravity facts into dialect
  attributes, side-effect interfaces, memory effects, symbol tables, and debug
  metadata.
- MLIR passes may rewrite IR only when they preserve Gravity semantics or emit
  invalidation and repair records accepted by the Gravity compiler.
- Conversion to LLVM, GPU, SPIR-V, affine, vector, linalg, func, arith, memref,
  scf, or custom dialects must have verifier gates.
- Loss of source spans, unsafe audit ids, capability records, numeric modes,
  alias facts, or proof references is a backend error.
- Dialect undefined behavior or verifier assumptions must not be used to justify
  safe Gravity behavior.

## Dependencies

- `B1` defines common backend input and emission rules.
- `C11`, `C12`, `C13`, and `C14` define MIR, domain IR, optimization
  invalidation, and target lowering.
- `SAFE2`, `SAFE8`, `SAFE9`, `SAFE10`, and `SAFE15` define memory,
  concurrency, numeric, capability, and proof requirements.
- `PERF8`, `PERF10`, and Phase 5 math documents define vector, check-elision,
  affine, and certificate evidence.
- `B3` and `B8` define downstream LLVM and GPU handoff constraints.

## Outputs and Artifacts

- MLIR backend manifest.
- Dialect registry manifest.
- Gravity dialect operation schema.
- MLIR module.
- Conversion target and legality report.
- Pass pipeline log.
- MLIR verifier report.
- Proof-to-dialect-attribute map.
- Source/debug map.
- Downstream handoff manifest.
- MLIR backend diagnostics.

## Backend Manifest

```clojure
{:artifact :gravity/mlir-backend-manifest
 :backend :gravity.backend/mlir
 :mlir-version "stable-recorded-version"
 :dialects #{:gravity.mir :gravity.efir :func :arith :scf :cf
             :memref :affine :vector :gpu :llvm}
 :emits #{:mlir-module :verifier-report :pass-log :handoff-manifest}
 :requires #{:dialect-registry :conversion-legality :proof-map
             :metadata-preservation-policy}
 :rejects #{:metadata-loss :unverified-effect-change :dialect-ub
            :illegal-conversion-target}}
```

The manifest names every dialect allowed in the module and every downstream
backend allowed to consume it.

## Dialect Strategy

Gravity uses MLIR in three ways:

- a Gravity MIR dialect for structured compiler experimentation,
- domain dialects for EFIR, tensor, affine, GPU, hardware, or schema-like
  fragments that already have a verified domain anchor,
- standard MLIR dialects for optimization and handoff.

Dialect definitions must expose verifier hooks for types, effects, memory
regions, ownership, capabilities, profile legality, and proof references. A
standard dialect may be used only when Gravity can encode the missing facts in
attributes or accompanying artifacts.

## Operation and Type Mapping

Operation mapping records:

- MIR operation or domain anchor,
- MLIR operation,
- operand/result type mapping,
- memory effect mapping,
- control-flow region mapping,
- source span and generated-origin chain,
- ownership and lifetime references,
- capability and effect references,
- safety outcome and proof references,
- diagnostic back edge.

MLIR values without a back edge to Gravity source or generated origin are
invalid in emitted artifacts except for compiler-introduced temporaries with a
recorded origin.

## Pass Pipeline

The pass pipeline record contains:

- pass name and version,
- input dialect set,
- output dialect set,
- legality conditions,
- facts preserved,
- facts invalidated,
- verifier run before and after the pass,
- repair or recomputation pass when facts are invalidated,
- downstream handoff target.

Passes that change memory effects, synchronization, numeric mode, aliasing,
capabilities, or profile legality must update the artifact manifest. Generic
MLIR cleanup passes are disabled when they erase required Gravity metadata.

## Proof and Metadata Preservation

Proof-to-dialect records map:

- range facts to affine bounds or integer attributes,
- alias facts to memref, buffer, or LLVM metadata,
- numeric mode to arith, math, vector, or LLVM attributes,
- vectorization facts to vector dialect operations,
- synchronization facts to memory effects and GPU barriers,
- capability facts to side-effect attributes,
- source/provenance facts to locations and side tables.

Gravity proof evidence remains authoritative after MLIR lowering. A downstream
backend may consume MLIR attributes only when the handoff manifest links them
back to Gravity proof records.

## Downstream Handoff

A downstream handoff manifest includes:

- destination backend,
- accepted dialects,
- remaining illegal operations,
- conversion target status,
- verifier status,
- source/proof/safety/capability map,
- ABI and runtime provider assumptions,
- target features,
- rejected or deferred constructs.

The handoff is rejected when any operation has no legal target representation
and no profile-valid fallback.

## Diagnostics

MLIR backend diagnostics use `B7` identifiers:

- `B7-DIALECT` for undeclared or unsupported dialects and operations.
- `B7-VERIFY` for MLIR verifier failure.
- `B7-CONVERSION` for illegal conversion target or remaining illegal ops.
- `B7-METADATA` for lost source, proof, safety, unsafe-audit, effect, or
  capability metadata.
- `B7-EFFECT` for unverified effect or memory-order changes.
- `B7-NUMERIC` for numeric-mode loss or unsupported math lowering.
- `B7-ALIAS` for alias or ownership facts that cannot be preserved.
- `B7-PASS` for pass pipelines that invalidate facts without repair.
- `B7-HANDOFF` for incomplete downstream manifests.
- `B7-MANIFEST` for incomplete MLIR backend artifacts.

Diagnostics must include source span, MIR operation or domain anchor, MLIR
operation, dialect, pass name when relevant, missing or invalidated fact,
downstream target, and remediation.

## Rejected Designs

Gravity rejects treating MLIR dialect verifier acceptance as proof of Gravity
semantics.

Gravity rejects pass pipelines that silently change effects, memory order,
numeric mode, or capability requirements.

Gravity rejects dialect boundaries that drop source spans, unsafe audit ids, or
proof references.

Gravity rejects downstream handoff without conversion legality evidence.

Gravity rejects encoding target-specific behavior back into generic MIR through
MLIR round trips.

## Conformance Criteria

A conforming MLIR backend must demonstrate:

- Gravity dialect operation and type schemas,
- standard dialect mapping with preserved Gravity metadata,
- MLIR verifier acceptance and rejection fixtures,
- pass pipeline logs with fact preservation and invalidation records,
- proof-to-dialect-attribute maps,
- downstream handoff to LLVM and GPU fixtures,
- rejection of metadata loss, illegal conversion, and effect-changing passes,
- differential execution or semantic equivalence against MIR/domain reference
  fixtures.
