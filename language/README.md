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

The initial split is an ownership boundary, not a bulk relocation. Existing
implementation paths remain valid while coherent modules are moved with their
callers, tests, manifests, and provenance references. Current Gravity modules
are classified by behavior rather than directory. Compiler passes and runtime
semantics under `bootstrap/gravity/src/gravity/` are language-owned, while
stage rebuild, trust, and equivalence modules under `src/gravity/self_hosting/`
are bootstrap-owned. The mixed `bootstrap/gravity/p15_s23/compiler.gravity`
source requires extraction before relocation; see the project structure map.

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

The focused language-development entrypoint is:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh07-var-reference-test \
  --exact sh07-b5-qualified-var-development-kernel --fail-fast
```

For current-reader development, use either co-canonical extension:

```bash
bin/gravity sh07-core /absolute/canonical/path/module.gravity --development-current-reader
bin/gravity sh07-core /absolute/canonical/path/module.qst --development-current-reader
```

Run the repository language-boundary and documentation checks when the changed
paths require them; their commands remain documented in the [development
guide](../docs/development-verification.md).
