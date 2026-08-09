# W5 B14 backend conformance verifier leaf

This directory owns the bounded W5 Wave2 B14 backend-conformance verifier. It
is a stage-owned `:meta` Gravity leaf, compiled and invoked through the existing
stage2 compiler-artifact plan after the owning integration clears it. The leaf
is static-only: it validates supplied suite, fixture, availability, lowering,
diagnostic, semantic-comparison, metadata, artifact, replay, skip, risk, and
evidence records. It does not execute a backend or infer a target from the host.

The current stage2 compiler-plan parser requires Gravity namespace declarations
to use `(:target :jvm)` as its harness boundary. That declaration is not
candidate backend evidence and is not a JVM fallback: all B14 policy,
availability, lowering, artifact, and result records remain bounded to the
exact `:llvm-x86_64-linux` candidate target, with JVM/Darwin/cross-target
inference rejected.

The engine is
`bootstrap/gravity/src/gravity/backend/w5_b14_backend_conformance_verifier.gravity`.
The accepted and rejected requests are co-canonical byte-identical `.gravity`
and `.qst` sources.

The required named Phase 7 backend matrix is recorded as
`:c`, `:llvm`, `:wasm`, `:jvm`, `:js-ts`, `:mlir`, `:gpu`, `:hdl`,
`:workflow-graph`, `:query-relational`, and `:mobile`. Only the candidate target
`:llvm-x86_64-linux` has exact availability evidence in this leaf:
Linux, `x86_64`, LLVM, ELF, and `:sysv-amd64`. The suite therefore reports
`:coverage-status :incomplete`, keeps the missing backend records explicit, and
is never release-eligible. It makes no claim that all Phase 7 backends are
supported.

The ordered unsupported target vector is
`[:darwin :darwin-arm64 :darwin-x86_64 :windows]`; every policy has
`:support :unsupported`, `:invokes-clojure? false`, `:links-jvm? false`, and
`:fallback? false`. The availability record has no available unsupported row,
no unsupported skip, no cross-target inference, and no Clojure/JVM fallback. Shared manifest and
negative checks remain runnable for named backends whose target-specific
evidence is not present.

The accepted request inventories all 30 required B14 fixture families.  The
27 currently evidenced families have positive and differential records; the
`:errors`, `:ai-tool-calls`, and `:schemas` rows are explicitly `:pending` and
cannot be inferred from another family.  Ten exact B14 diagnostic families,
metadata preservation, artifact-manifest validation, six replayable
nondeterminism classes, and the incomplete risk/evidence pack remain explicit.
Every negative span is bounded with end greater than or equal to start.
Identity input contains only logical/content identity;
actual checkout paths occur only in provenance. Alternate checkout requests
must therefore have equal identity and different provenance.

The rejected fixture exports one total mutator for each diagnostic family plus
direct substitutions for suite/identity, positive/negative/differential,
artifact, and provenance cross-links.  The canonical constructor accepts the
actual source path, compiler path, and logical source so `.gravity` and `.qst`
provenance cannot be silently conflated:

- `B14-COVERAGE` - incomplete or malformed suite/fixture coverage;
- `B14-TARGET` - candidate availability or target evidence is not exact;
- `B14-POSITIVE` - a valid lowering result fails;
- `B14-NEGATIVE` - a rejected fixture has the wrong diagnostic;
- `B14-DIFFERENTIAL` - semantic comparison does not match;
- `B14-METADATA` - source/proof/safety/effect/capability metadata is lost;
- `B14-ARTIFACT` - the artifact manifest is invalid;
- `B14-NONDETERMINISM` - replay leaves an event unrecorded;
- `B14-SKIP` - an unsupported skip is made available or Darwin is not closed;
- `B14-EVIDENCE` - the risk/evidence/result evidence is incomplete or stale.

Every rejection carries the stable B14 rule, candidate target, profile, source
span, remediation, and provenance. Global records are explicitly
`:clojure-seed-boundary? true`, `:self-hosted? false`, `:release? false`,
`:public-authority? false`, and `:authority :non-authority`. Residual boundaries
are the Clojure stage2 compiler plan, the JVM stage2 runtime, missing backend
target evidence, and independent backend execution.

The later exact command, after integration clearance, is:

```text
clojure -M:test --namespace gravity.self-hosting.w5-b14-backend-conformance-verifier-test
```

This worktree intentionally performs only static ASCII, bracket, diff, and
`.gravity`/`.qst` parity checks; it does not run that command.
