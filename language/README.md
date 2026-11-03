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
under `bootstrap/gravity/` are language-owned even though their present path
contains the historical bootstrap label.

See [the project structure map](../docs/project-structure.md) for the current
path classification and relocation order.
