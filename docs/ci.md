# CI and experimental source snapshots

## Purpose

GitHub-hosted checks replace routine laptop verification for pull
requests and the exact clean integrated main commit. They do not confer
compiler migration or release acceptance.

## Workflow matrix

| Check | PR | Main/manual/weekly | Evidence and limits |
| --- | --- | --- | --- |
| static | Always | Always | actionlint 1.7.7 workflow syntax; clj-kondo 2025.10.23 on changed Clojure/EDN files and CI tooling; cljfmt 0.13.4 on CI tooling; Git whitespace; harness failure controls. Legacy format adoption remains separate. |
| projections | Always | Always | Canonical 240-document validator, full-language roadmap schema and negative fixtures, acceptance dependency controls, every existing component task projector and its tests. |
| bootstrap | Always | Always | Fresh process requires Bootstrap without preinstalled aliases; exact decimal/abbreviation Reader regressions; language boundary; strict catalog controls and ownership inventory. No modeled aliases installed by this harness. |
| fresh-leaf-regressions / portable and / darwin | Markdown-only PRs omit leaves; otherwise full fresh catalog | Full fresh catalog | 51 portable namespaces on Ubuntu and the Darwin publication namespace on ARM macOS with its exact JDK 26.0.1 contract. Every namespace gets a fresh JVM; the partition is exhaustive and disjoint. Unknown paths fall back to all 52; no cross-run result cache. This is not the full compiler suite. |
| source-Core-readiness | Omitted | Explicitly failing unavailable gate | Genuine selected-current-Reader construction plus independent verification is not yet available on main. Never treated as a pass or simulated using mocks. |
| experimental-source-snapshot | Preview artifact | Preview artifact | Exact SHA source tarball, independently repeated Git archive hash, capability notes/changelog and provenance. No compiler binary. |
| experimental-prerelease | Never | Main push only, after all four routine checks and packaging pass | Separate contents-write job publishes the source bundle as a prerelease; Core readiness is explicitly outside this source-only claim. |

## Requirements and dependencies

The audited baseline has no checked-in Actions workflow, dependency lockfile,
formatter configuration or CODEOWNERS. `deps.edn` has test aliases and only
Clojure's standard library dependency. CI pins CLI/runtime 1.12.5.1654/1.12.5,
Temurin 21.0.8+9.0.LTS (portable checks) and 26.0.1+8 (Darwin), action revisions,
and static tool versions. It does not modify
compiler dependencies or source authentication pins. Hosted runner images can
change; record the run environment and do not claim hermetic reproduction.

The Darwin source contract declares ARM macOS and JDK 26.0.1. The current
`macos-15` hosted run is nonpassing: descriptor-relative stat returns EINVAL
with the required confinement flags. The declared host contract does not yet
establish which kernel provides those capabilities. CI records the actual
OS, kernel, SDK, FFM symbol and invalid-descriptor flag probes before running
the complete publication tests. These read-only probes diagnose ABI acceptance;
they do not prove path confinement, hardlink refusal or race handling. A newer
host must pass the full positive and hostile cases before it supplies acceptance.
No kernel flag or hostile case is removed to obtain a green check.

PR static checks verify that the checkout has exactly two parents and that its
second parent matches the event's PR head. They compare against the first
parent of that tested merge, so stale event-base metadata cannot lint incoming
main changes as PR changes. Main pushes compare against the push's `before`
SHA and test the exact integrated commit.

Cache tests retain their owner and permission checks: Java temporary
files use a runner-owned private directory, rather than Linux's root-owned
shared `/tmp`. No source security or runtime pin is relaxed for CI.

No self-hosted runners, secrets, paid infrastructure, branch protection or
access settings are created. Fork PR code receives a read-only token, no
persisted checkout credentials and no release permission. No
`pull_request_target` job runs PR source.

Jobs and each child command have wall-clock bounds and explicit JVM heaps.
Timeouts and launch errors fail. Command output and EDN receipts survive other
command failures; artifacts record commit/tree, run URL, toolchain and checksums.
Runner termination may leave only GitHub's own job/step logs and partial
receipts; missing or timed-out output is never acceptance evidence.

Main source snapshots use tag `experimental-source-<full-commit-SHA>`, never
the latest production release. The generation job has read-only permission;
only the publication job receives `contents: write`, consumes the same run's
artifact, verifies SHA256SUMS and executes no checked-out source. Publication
does not overwrite existing releases on rerun. No signing credential or OIDC
authority is provisioned. Checksums and unsigned provenance establish content
identity, not trusted compiler lineage. Notes explicitly exclude seedless,
complete-language/runtime, native binary, production, compiler reproducibility,
signature, SBOM and independent release-acceptance claims. Artifacts expire
after 14 days; published prerelease assets persist under GitHub release policy.

## Outputs and acceptance

The strict leaf catalog preserves all 54 primary component records and their
48 bootstrap-free classifications. Four auxiliary helper tests have explicit
production-source, namespace and owner associations. Their actual transitive
requires include the native C3 fixture dependency and remain compiler-free.
Unknown files, metadata drift and missing ownership still fail closed. These
hosted helper tests grant no Reader authentication or compiler execution credit.

The broader ownership inventory exposed the existing unregistered
`bootstrap/clojure/src/gravity/macro_run_storage.clj` production helper. Its
explicit SH-05 registration joins the real early consumer and existing
standalone test. All original 202 owner bindings remain, with one added helper
binding. The original 54 paired components still account for 108 direct paths;
the separate production helper makes 109. No helper exclusion, fabricated
top-level test or 55th primary component is used. CI runs the strict complete
inventory and its negative controls.

The Native owner's reported genuine A baseline failed naturally at
`C6-VERIFY/:actual-selected-current-reader-products`. The corrected C3
Reader-provenance join and public v2 interface are local, unpublished work;
CI must not use them before review/integration. PR483 repaired cold Macro
helper loading; PR482 covers Reader decimal/history semantics. Cold require
and existing Reader positive/negative tests retain those regressions.

Before enabling required checks, observe stable real GitHub runs. Proposed
initial names are `static`, `projections`, `bootstrap`,
`fresh-leaf-regressions / portable`, and `fresh-leaf-regressions / darwin`.
Keep `source-Core-readiness` non-required until the
real public route is reviewed and passing. Main tests check the push SHA,
not a cherry-picked candidate or synthetic source fixture.

Existing finite task authorities remain `specs/build-workflow/tasks.md`
(`build-implementation-tasks`) and `specs/release-workflow/tasks.md`
(`release-implementation-tasks`). Relevant Build outcomes are typed graph
execution, dependency invalidation, package/provenance verification, and
reproducibility/target matrix. Release outcomes are exact component conformance,
stage equivalence, actual self-hosted execution, signed provenance/SBOM
distributions and independent candidate acceptance. CI provides partial tooling
interfaces, closes none of these tasks and creates no new Validation ledger.
Canonical requirements remain owned by the reconciliation owner; dependency
interfaces remain owned by the SH/dependency owner.

Exact Build task IDs under `build/remaining-tasks-and-evidence/` are
`execute-typed-graph-under-declared-build-effects`,
`implement-dependency-aware-caching-and-invalidation`,
`produce-and-verify-packageprovenancesigning-records`, and
`close-reproducibility-and-target-matrix`. Exact Release suffixes under
`release/remaining-tasks-and-evidence/` are
`bind-each-claimed-component-to-exact-implementation-and-conformance`,
`complete-stage-compatibilityequivalence-across-the-claimed-compiler`,
`produce-actual-self-hosted-compilerruntime-execution`,
`build-reproducible-signed-distributions-with-provenancesbom`, and
`obtain-independent-acceptance-for-the-exact-release-candidate`.
