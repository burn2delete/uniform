# Uniform bootstrap replacement project

This project owns the work required to replace the temporary Clojure stage0
bootstrap with a reproducible Uniform-built compiler and distribution.

It is a delivery and trust-reduction project, not a second language. It may
contain temporary seed adapters, host bridges, rebuild orchestration, stage
comparisons, provenance records, and seed-retirement evidence. It must consume
the language project's compiler interfaces and artifacts rather than define
language semantics of its own.

Thirty-five byte-identical Gravity modules now have physical homes under
`language/compiler/src/gravity/` and `language/runtime/src/gravity/`. This
project retains the Clojure seed, transport adapters, rebuild and equivalence
evidence, and mixed stage material; a path move does not retire the seed or
transfer bootstrap evidence to language ownership.

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

See [the project structure map](../docs/project-structure.md) for behavior-based
ownership, dependency direction, and the staged relocation policy. In
particular, a Clojure seed function that implements a permanent language rule
is replaced by a Uniform-owned successor; the original `.clj` remains in this
project until its seed boundary is retired.

## Roadmap and development commands

Use the [Phase 15 bootstrap contracts](../docs/phase-15-bootstrap-and-self-hosting/README.md),
[self-hosting completion plan](../docs/self-hosting-milestone-completion-plan.md),
[self-hosting slice backlog](../docs/self-hosting-slice-backlog.md),
[Phase 18 release roadmap](../docs/phase-18-binary-distribution-and-seedless-release/README.md),
[Release component outcome](../docs/component-roadmaps/release.md), and the
[development verification guide](../docs/development-verification.md). Those
pages and ledgers own task status and evidence; this entrypoint does not
duplicate them.

For the pinned-input development build, follow the [bootstrap compiler input
boundary](../docs/bootstrap/compiler-inputs.md). From the repository root, the
bounded build can print only its key identities:

```bash
clojure -Srepro -M -e '
(require (quote [gravity.bootstrap.rebuild.inputs :as inputs]))
(prn (select-keys
      (:result (inputs/run-development-build! "." "contracts/bootstrap-inputs.edn"))
      [:artifact-id :manifest-sha256 :source-revision :source-graph-sha256]))'
```

This consumes the declared compiler and seed inputs for a development check;
it is not a compiler rebuild, self-hosting proof, seed-retirement result, or
release claim. The [development verification guide](../docs/development-verification.md)
remains the authority for its focused namespace and evidence scope.

The current bootstrap-hosted and fail-closed verification commands are:

```bash
clojure -M:gravity self-host verify
clojure -M:packaged-gravity --version
clojure -M:packaged-gravity run examples/core-app.gravity
clojure -M:test --namespace gravity.self-hosting.sh01-language-boundary-test

clojure -M:bootstrap-validation-test \
  --namespace gravity.bootstrap.rebuild.inputs-test \
  --fail-fast
```

The focused input-boundary namespace currently passes 8 tests and 59
assertions. W5 selectors remain available in the development verification
guide for their recorded baseline, including known nonpassing evidence; this
focused input check does not replace that baseline.

The first command is read-only and remains fail-closed while final seed
retirement is incomplete. Use the explicit export path documented in
`docs/development-verification.md` when regeneration is intentional.
