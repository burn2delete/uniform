# Uniform project structure

This map separates two delivery projects that currently share one checkout:
the Uniform language and the replacement of its temporary Clojure bootstrap.
The split is an ownership and dependency boundary. It does not move source,
change language semantics, or alter the 240-document inventory.

## Project boundaries

| Project | Owns | Does not own |
| --- | --- | --- |
| Uniform language | Language contracts, compiler semantics, runtime and standard-library behavior, tools, diagnostics, and conformance | Seed retirement, stage comparison, or release trust claims |
| Bootstrap replacement | Clojure seed, temporary adapters, rebuild orchestration, stage comparison, provenance, and Clojure-retirement evidence | An alternative language definition or semantic authority |
| Release | The declared public distribution assembled from accepted compiler, runtime, build, CLI, and bootstrap outcomes | Unclaimed targets, profiles, or whole-language completion |
| Normative documentation | The 240 design contracts and the Phase 18 roadmap extension | Implementation status by itself |

The same compiler module can be language-owned while bootstrap-owned tooling
executes it. Ownership follows the behavior and contract being implemented,
not the current host language or directory name.

## Dependency direction

```text
normative docs
    -> Uniform language contracts and compiler artifacts
    -> bootstrap adapters and rebuild/equivalence evidence
    -> release assembly and seedless-release decision
```

The language project may expose explicit operations for a temporary host, but
language modules must not import bootstrap orchestration or discover seed vars
implicitly. Bootstrap orchestration supplies those operations and records the
remaining host boundary. Release consumes both projects' exact artifacts and
does not grant authority that either project has not proved.

## Current path classification

| Current path | Initial owner | Notes |
| --- | --- | --- |
| `bootstrap/gravity/src/gravity/compiler/` | Uniform language | Compiler pass semantics and their Uniform-authored contracts. The directory name is historical; a pass remains language-owned when Clojure executes it. |
| `bootstrap/gravity/src/gravity/reader.gravity`, `effects.gravity`, `runtime.gravity` | Uniform language | Language and compiler behavior. Their temporary seed transport stays bootstrap-owned. |
| `language/compiler/src/gravity/macro.gravity`, `language/compiler/src/gravity/compiler/c4_macro_expansion_engine.gravity`, `language/compiler/src/gravity/resolution.gravity`, `language/compiler/src/gravity/compiler/c5_name_resolution_namespace_analyzer.gravity` | Uniform language | Relocated frontend modules retain their legacy namespaces/APIs; Clojure adapters and migration evidence remain bootstrap-owned. |
| `bootstrap/gravity/src/gravity/bootstrap/` | Mixed, by behavior | C2/C3 reader, syntax, and diagnostic semantics are language-owned; stage executors, release-attestation, and seed-retirement entrypoints in the same files remain bootstrap-owned until extracted. The directory name alone does not decide ownership. |
| `bootstrap/gravity/src/gravity/self_hosting/` | Bootstrap replacement, by behavior | Stage rebuild orchestration, compiler identity, equivalence, trust, and evidence modules such as `w5_stage_rebuild_orchestrator.gravity`, `w5_stage_equivalence_verifier.gravity`, and `w5_trust_provenance_verifier.gravity` belong to bootstrap. A module that implements a permanent compiler rule is language-owned after behavior review. |
| `bootstrap/gravity/p15_s23/compiler.gravity` | Mixed; extraction required | This source combines compiler rule inventories with stage/rebuild/seed-retirement records. Extract permanent compiler behavior to the language project and keep stage inventory, rebuild orchestration, and retirement evidence in bootstrap. Do not assign the whole file to either project. |
| `bootstrap/gravity/p15_s23/` | Mixed, by behavior | Review each file before relocation. Target lowering/runtime semantics can be language-owned; stage drivers, seed-boundary publication, and rebuild evidence remain bootstrap-owned. |
| `bootstrap/clojure/src/gravity/bootstrap_impl/` | Bootstrap replacement | Stage0 implementation, adapters, and temporary host composition. If a Clojure function embodies a permanent language rule, its ownership moves to the Uniform successor; this does not mean moving the `.clj` seed file into the language tree or hiding the remaining seed debt. |
| `bootstrap/clojure/test/` | Split by assertion | Language conformance and compiler composition tests follow their owner; stage/equivalence/retirement tests remain bootstrap-owned. |
| `docs/` | Normative documentation | Remains in place; project pages link to the governing documents. |
| `tools/` | Shared repository tooling | Tool ownership follows whether a command validates language behavior, bootstrap evidence, or repository policy. |
| `contracts/` | Shared evidence and governance | Keep existing schemas and lifecycle records; add no duplicate ledger for this split. |
| `docs/component-roadmaps/` | Component navigation | Component pages remain the detailed outcome/task owners. This map only assigns their relationship to the two delivery projects. |

The milestone completion plan and current migration cohort retain the source
paths recorded in their pinned baselines. The current physical-owner mapping
above governs this layout; it does not renew migration, self-hosting, or release
authority.

## Relocation order

Relocation is incremental and preserves runnable development paths:

1. establish ownership and test classifications;
2. move one coherent language module with its callers and fixtures;
3. keep its Clojure transport adapter in the bootstrap project;
4. update source identities, manifests, and provenance together;
5. remove the adapter only after the Uniform-built replacement consumes the
   same interface and evidence; and
6. repeat by compiler dependency order, then runtime, library, build, and
   tooling dependencies.

The first relocated candidate is
`language/compiler/src/gravity/checked_core.gravity`, whose semantic ownership
and temporary Clojure transport boundary are documented in
`docs/development-verification.md`. Its legacy bootstrap path is retired as a
current source path; historical evidence continues to retain the old path. The mixed
`bootstrap/gravity/p15_s23/compiler.gravity` and self-hosting orchestrators
must be split by behavior before they are candidates for relocation.

## Separate progress claims

The project reports these states independently:

- language behavior implemented and conformance-tested;
- compiler module owned by Uniform source;
- compiler rebuilt by a declared stage;
- public execution path free of Clojure; and
- seedless release accepted.

One state never implies the others. Historical evidence remains tied to its
original source paths and identities, and a path move requires fresh evidence
for any later migration or release claim.

## Governing contracts

This map follows [D0](phase-00-foundation-and-thesis/001-d0-gravity-vision-and-design-thesis.md),
[D1](phase-00-foundation-and-thesis/002-d1-system-architecture-overview.md),
[D2](phase-00-foundation-and-thesis/003-d2-implementation-roadmap-and-milestones.md),
[D3](phase-00-foundation-and-thesis/004-d3-terminology-and-concept-model.md),
[C1](phase-06-compiler-architecture/080-c1-compiler-architecture-overview.md),
[BOOT1](phase-15-bootstrap-and-self-hosting/203-boot1-bootstrap-strategy.md),
[BOOT3](phase-15-bootstrap-and-self-hosting/205-boot3-self-hosted-compiler-plan.md),
[BOOT7](phase-15-bootstrap-and-self-hosting/209-boot7-self-hosting-validation-and-equivalence-plan.md),
[BOOT8](phase-15-bootstrap-and-self-hosting/210-boot8-bootstrap-artifact-provenance-specification.md),
[GOV1](phase-17-governance-and-evolution/231-gov1-language-evolution-process.md),
the [component roadmaps](component-roadmaps/README.md), and the existing
[development verification guide](development-verification.md). It is navigation
and ownership metadata, not a replacement for those contracts or for the
workstream lifecycle.
