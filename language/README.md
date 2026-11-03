# Uniform language project

This project owns the Uniform language and the implementation of its language
contracts. It is the permanent home for source semantics, compiler passes,
runtime services, standard-library behavior, developer tools, and language
conformance fixtures.

The language project is independent of the host currently used to execute a
compiler pass. A compiler module written in `.gravity` or `.qst` remains a
language implementation module when the temporary Clojure seed loads or runs
it. The host is an implementation detail recorded in provenance; it does not
change semantic ownership.

## Scope

- language specifications and their accepted and rejected behavior;
- compiler stages from reader through artifact emission;
- profile, effect, capability, ownership, and safety legality;
- runtime and standard-library contracts;
- diagnostics, source provenance, and compiler conformance fixtures; and
- developer tools that expose the language contracts.

The normative contracts remain in `docs/`. This directory provides project
navigation and ownership; it does not duplicate those contracts or create a
second language definition.

## Development boundary

Language development uses focused semantic tests and affected composition
tests. It may use the Clojure seed while a Uniform implementation is being
developed. A semantic change does not become a bootstrap migration merely
because the seed executes it.

The language project does not own seed retirement, stage comparison, or the
proof that a public distribution no longer depends on Clojure. Those are
bootstrap-project outcomes consumed by the release project.

## Current source paths

The permanent implementation roots are now explicit:

- `language/compiler/src/gravity/` owns the relocated compiler semantics,
  including checked core, effects, compiler contracts, and backend families.
- `language/runtime/src/gravity/` owns the relocated runtime semantics.

These roots are language-owned even when the temporary Clojure seed loads or
executes the files. Legacy filenames and APIs remain compatibility surfaces;
the host does not change semantic ownership. Frontend and reader/syntax slices
join the same language owner only as each coherent module is extracted with its
callers, fixtures, manifests, and provenance references.

The remaining bootstrap paths are mixed by behavior, not owned wholesale by a
directory:

- `bootstrap/gravity/src/gravity/bootstrap/` still contains reader, syntax,
  diagnostic, and bootstrap-adapter portions. Permanent language semantics are
  extracted separately; stage, rebuild, release, and seed-boundary portions
  remain bootstrap-owned until extracted.
- `bootstrap/gravity/src/gravity/self_hosting/` owns stage rebuild
  orchestration, equivalence, trust, provenance, and evidence, including
  `w5_stage_rebuild_orchestrator.gravity` and
  `w5_stage_equivalence_verifier.gravity`.
- `bootstrap/gravity/p15_s23/compiler.gravity` is mixed: compiler rule
  inventories require extraction from stage inventories, rebuild orchestration,
  and seed-retirement evidence before either side can own the resulting files.
- `bootstrap/clojure/` owns the temporary Clojure seed and its adapters and
  tooling. A seed function that implements a permanent language rule moves to
  its Uniform successor's ownership; the `.clj` seed file stays here until its
  boundary is actually retired.

The project structure map records the complete behavior-based classification
and relocation order. This entrypoint does not claim bulk relocation,
self-hosting, or Clojure retirement.

See [the project structure map](../docs/project-structure.md) for the current
path classification and relocation order.

## Roadmap and development commands

Use the [implementation roadmap](../docs/implementation-roadmap.md),
[full-language gap map](../docs/full-language-implementation-gap-map.md),
[component roadmaps](../docs/component-roadmaps/README.md), and [compiler
architecture phase](../docs/phase-06-compiler-architecture/README.md) for
scope and ownership. The [development verification guide](../docs/development-verification.md)
is the current command authority; its task and evidence records are not
duplicated here.

Project-scoped aliases keep focused development selection explicit. The
language and compiler catalogs are selected with `--namespace` and `--exact`:

```bash
clojure -M:language-test \
  --namespace gravity.reader.normal-dispatch-test \
  --exact exact-normal-decoder-dispatches-real-standard-handlers --fail-fast

clojure -M:compiler-composition-test \
  --namespace gravity.checked-core.pipeline-test \
  --exact request-gateway-preserves-context-and-invocation-dispatch --fail-fast
```

For the broader current-reader development command, use the existing
`:dev-test` entrypoint:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh07-var-reference-test \
  --exact sh07-b5-qualified-var-development-kernel --fail-fast
```

The [development verification guide](../docs/development-verification.md)
remains the command authority and defines the selected catalog scope; these
aliases do not create a second task or evidence ledger.

For current-reader development, use either co-canonical extension:

```bash
bin/gravity sh07-core /absolute/canonical/path/module.gravity --development-current-reader
bin/gravity sh07-core /absolute/canonical/path/module.qst --development-current-reader
```

Run the repository language-boundary and documentation checks when the changed
paths require them; their commands remain documented in the [development
guide](../docs/development-verification.md).
