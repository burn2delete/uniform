# Bootstrap Compiler Inputs

## Purpose

The bootstrap replacement project has an explicit input boundary independent
from the Uniform language contract. The declaration at
`contracts/bootstrap-inputs.edn` names the stage0 compiler source revision,
seed/runtime identity, load order, classpath, locked inputs, fixture bytes, and
the deliberately narrow conformance scope. It is consumed by
`gravity.bootstrap.rebuild.inputs`; it is not a self-hosting certificate or a
release claim.

## Requirements

- The source language and temporary seed language are explicit.
- The compiler revision is pinned by both commit and tree identity.
- The complete declaration is content-addressed; changing its direct-input
  inventory or source-graph identity is rejected before source consumption.
- Source, tooling, lockfile, load-order, and fixture inputs are content
  addressed. The observed graph has one canonical entry per path.
- The Clojure seed jar, dependency jars, and CLI version are pinned; the run
  records the child process's observed Clojure description and resolved
  classpath identities.
- The exact `deps.edn` classpath and six bootstrap load-order files are named.
- Compiler source profiles and tested program profiles, target, supported
  compiler modules, accepted fixture bytes, and rejected fixture bytes are
  stated separately from language semantics.
- Backend, runtime, network, and residual host boundaries are explicit.
- Comparison modes and output artifact kinds are explicit.
- Missing, substituted, symlinked, malformed, or incompatible inputs fail
  before compiler execution.

## Snapshot and execution boundary

`run-development-build!` first validates the declaration, verifies that the
pinned commit resolves to the pinned tree, and materializes the declared source roots,
lockfile, load orders, tooling root, and fixture files into an isolated
temporary directory with `git archive` and the host `tar` extractor. It then
walks that snapshot and compares the complete source graph with the declared
SHA-256 identity.

The input-boundary tests are an opt-in development check, not part of ordinary
`-M:test` or default namespace selection. The CI `bootstrap-input-controls`
command invokes this namespace explicitly against the same bounded historical
snapshot. Run the focused check with:

```bash
clojure -Srepro -M:dev-test \
  --namespace gravity.bootstrap.rebuild.inputs-test \
  --fail-fast
```

The first producer boundary is a separate development component, exposed by
`gravity.bootstrap.rebuild.component/run-seed-built-component!`. It materializes
the same authenticated snapshot, compiles the fixed Gravity
`native_plan_c_emitter.gravity` source with the pinned stage0 seed, persists the
resulting executable plan, and starts a fresh child to reload that plan. The
child invokes the reloaded exported function for both an accepted hosted/JVM
`println` source fixture and a rejected vector-output fixture. Run its focused
check with:

```bash
clojure -Srepro -M:dev-test \
  --namespace gravity.bootstrap.rebuild.component-test \
  --fail-fast
```

This producer has a deliberately narrow boundary: the input source subset is
hosted/JVM `ns` metadata plus a `main` function containing a printable-string
`println`; its output is generated C11 source and a hosted expected-stdout
observation. Native C compilation/execution is unrun. The persisted plan is a
seed-hosted component artifact, not a rebuilt compiler, whole-language proof,
self-hosting result, or seed-retirement claim. A caller-supplied plan/result is
not accepted; the child plan and observation are parsed, hashed, and compared
after public output readback.

The declaration is a trusted, versioned VCS input for this bounded check. Its
source graph and direct-input digests authenticate the selected bytes; the
executor remains responsible for process, filesystem, publication, and other
host policy. The check does not refresh the d351 historical snapshot or claim
current-checkout, full-compiler, self-hosting, or release conformance.

Before any fixture runs, the child `clojure -Spath` output is parsed. Its
source directories must match the declared classpath, its Clojure jar digest
must match `:seed-artifact-id`, and every other selected jar must match the
declared dependency-jars vector. The parent JVM classpath is not used as the
seed identity.

The development build invokes the existing stage0 command
`clojure -Srepro -M:gravity core <fixture>` from that snapshot. The accepted
fixture must exit successfully and parse as the expected
`:gravity/stage0-core-artifact` with the core-lowering pass, module, expanded
syntax/core records, source map, trace, and diagnostics fields. The rejected
fixture must exit non-zero and contain the declared `L2-UNKNOWN-CORE-FORM`
diagnostic. The returned evidence records
process status, stdout/stderr hashes, an accepted artifact hash, a rejected
diagnostic hash/code, source archive identity, source graph identity, and
seed/toolchain identity. No caller-supplied executor or result identity is
trusted.

The current declaration is intentionally a bounded development check: one
`:meta` compiler-source profile with `:meta`/`:jvm` accepted program fixture,
one `:hosted` rejected core-form program fixture, and the listed
reader/syntax/expand/resolve/core modules. It does not produce a rebuilt
compiler, prove whole-language conformance, establish stage equivalence, or
retire the Clojure seed. Updating the pinned compiler revision or expanding
scope requires an explicit declaration and corresponding code/test review.

## Outputs and artifacts

`load-inputs!` returns the authenticated manifest, observed graph, graph
identity, scope, execution boundary, and comparison declaration.
`run-development-build!` returns that record plus the immutable source revision/archive,
observed toolchain, and fixture execution records. Output and diagnostic bytes
are represented by hashes rather than copied into the provenance record. For an
accepted fixture, `:artifact-id` is the canonical hash of the parsed Core EDN
artifact and `:stdout-sha256` separately identifies the process observation;
the latter is not treated as the artifact identity.
The returned `:manifest-sha256` binds the full declaration used for the run;
the run does not reread a caller-controlled declaration after snapshot
materialization.

## Conformance criteria

- The pinned source declaration authenticates from an isolated snapshot.
- A direct input digest mismatch is rejected with
  `BOOT-INPUTS-DIRECT-DIGEST-MISMATCH`.
- A changed source graph is rejected with
  `BOOT-INPUTS-SOURCE-GRAPH-MISMATCH`.
- A substituted direct-input inventory or declared graph identity is rejected
  with `BOOT-INPUTS-MALFORMED`.
- A substituted revision or seed/toolchain identity is rejected before the
  development build.
- Symlinked source inputs cannot extend the authenticated root.
- An incompatible execution target or backend is rejected with
  `BOOT-INPUTS-INCOMPATIBLE`.
- The development build executes real accepted and rejected fixtures through the existing
  stage0 CLI and records their observed output identities.

The declaration records `:network :denied` as the intended bootstrap policy.
The current host process does not enforce OS-level offline execution; the
`-Srepro` commands may still resolve from the configured local/cache boundary.
That residual host boundary is deliberately visible in the execution record.

This boundary separates replacing the temporary Clojure bootstrap from
evolving the Uniform language. It does not establish semantic compiler
correctness, stage equivalence, seed retirement, self-hosting, or release
readiness.
