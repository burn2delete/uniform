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
| `bootstrap/gravity/` | Uniform language | Existing Gravity source modules are semantic implementation, despite the historical path. Relocate by coherent dependency group. |
| `bootstrap/clojure/src/gravity/` | Bootstrap replacement | Stage0 implementation, adapters, and temporary host composition. A file that implements a permanent language rule should move to the language owner before its seed wrapper is retired. |
| `bootstrap/clojure/test/` | Split by assertion | Language conformance and compiler composition tests follow their owner; stage/equivalence/retirement tests remain bootstrap-owned. |
| `docs/` | Normative documentation | Remains in place; project pages link to the governing documents. |
| `tools/` | Shared repository tooling | Tool ownership follows whether a command validates language behavior, bootstrap evidence, or repository policy. |
| `contracts/` | Shared evidence and governance | Keep existing schemas and lifecycle records; add no duplicate ledger for this split. |
| `docs/component-roadmaps/` | Component navigation | Component pages remain the detailed outcome/task owners. This map only assigns their relationship to the two delivery projects. |

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

The first candidate is `bootstrap/gravity/src/gravity/checked_core.gravity`,
whose semantic ownership and temporary Clojure transport boundary are already
documented in `docs/development-verification.md`. This candidate is a future
relocation slice; this map does not claim it has moved.

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

This map follows D0, D1, D2, D3, C1, BOOT1, BOOT3, BOOT7, BOOT8, GOV1, and the
existing component roadmaps. It is navigation and ownership metadata, not a
replacement for those contracts or for the workstream lifecycle.
