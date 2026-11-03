# Uniform bootstrap replacement project

This project owns the work required to replace the temporary Clojure stage0
bootstrap with a reproducible Uniform-built compiler and distribution.

It is a delivery and trust-reduction project, not a second language. It may
contain temporary seed adapters, host bridges, rebuild orchestration, stage
comparisons, provenance records, and seed-retirement evidence. It must consume
the language project's compiler interfaces and artifacts rather than define
language semantics of its own.

## Scope

- the Clojure stage0 seed while it remains required;
- explicit adapters between seed operations and Uniform compiler modules;
- rebuild inputs, stage manifests, and reproducible build recipes;
- stage equivalence, diagnostic compatibility, and trust-boundary reports;
- bootstrap-specific fixtures and negative tamper/substitution checks; and
- the evidence and migration steps needed to retire Clojure from a declared
  compiler or release path.

Bootstrap inputs must identify the exact compiler source revision, seed
artifact, dependency closure, profile, target, runtime, host boundaries,
conformance scope, comparison mode, and produced artifacts. A bootstrap run
against the current checkout is not an accepted baseline unless those inputs
are recorded and independently reviewed.

## Development boundary

Bootstrap checks are additional evidence for an explicit migration or release
decision. They do not gate ordinary language implementation and they do not
turn seed agreement into language correctness. The seed remains a temporary
execution mechanism until the applicable bootstrap, self-hosting, and release
contracts are satisfied.

The release project consumes this project's exact rebuild and trust evidence;
bootstrap progress alone does not claim self-hosting, seed retirement, or a
seedless public release.

See [the project structure map](../docs/project-structure.md) for ownership,
dependency direction, and the staged relocation policy.
