# CI and experimental source snapshots

## Purpose

GitHub-hosted Ubuntu checks replace routine laptop verification for pull
requests and the exact clean integrated main commit. They do not confer
compiler migration or release acceptance.

## Workflow matrix

| Check | PR | Main/manual/weekly | Evidence and limits |
| --- | --- | --- | --- |
| static | Always | Always | actionlint 1.7.7 workflow syntax; clj-kondo 2025.10.23 and cljfmt 0.13.4 on new CI tooling; Git whitespace; harness failure controls. No assertion that legacy source is lint-clean. |
| projections | Always | Always | Canonical 240-document validator, full-language roadmap schema and negative fixtures, acceptance dependency controls, every existing component task projector and its tests. |
| bootstrap | Always | Always | Fresh process requires Bootstrap without preinstalled aliases; exact decimal/abbreviation Reader regressions; language boundary. No modeled aliases installed by this harness. |
| fresh-leaf-regressions | Markdown-only PRs omit leaves; otherwise full fresh catalog | Full fresh catalog | Existing reviewed bootstrap-free catalog, each namespace in its own JVM. Unknown paths fall back to the full catalog; no unproven affected-test graph or cross-run result cache. This is not the full compiler suite. |
| source-Core-readiness | Omitted | Explicitly failing unavailable gate | Genuine selected-current-Reader construction plus independent verification is not yet available on main. Never treated as a pass or simulated using mocks. |

## Requirements and dependencies

The audited baseline has no checked-in Actions workflow, dependency lockfile,
formatter configuration or CODEOWNERS. `deps.edn` has test aliases and only
Clojure's standard library dependency. CI pins CLI/runtime 1.12.5.1654/1.12.5,
Temurin 21.0.8+9, action revisions, and static tool versions. It does not modify
compiler dependencies or source authentication pins. Hosted runner images can
change; record the run environment and do not claim hermetic reproduction.

No self-hosted runners, secrets, paid infrastructure, branch protection or
access settings are created. Fork PR code receives a read-only token, no
persisted checkout credentials and no release permission. No
`pull_request_target` job runs PR source.

Jobs and each child command have wall-clock bounds and explicit JVM heaps.
Timeouts and launch errors fail. Command output and EDN receipts survive other
command failures; artifacts record commit/tree, run URL, toolchain and checksums.
Runner termination may leave only GitHub's own job/step logs and partial
receipts; missing or timed-out output is never acceptance evidence.

## Outputs and acceptance

The Native owner's reported genuine A baseline failed naturally at
`C6-VERIFY/:actual-selected-current-reader-products`. The corrected C3
Reader-provenance join and public v2 interface are local, unpublished work;
CI must not use them before review/integration. PR483 repaired cold Macro
helper loading; PR482 covers Reader decimal/history semantics. Cold require
and existing Reader positive/negative tests retain those regressions.

Before enabling required checks, observe stable real GitHub runs. Proposed
initial names are `static`, `projections`, `bootstrap`, and
`fresh-leaf-regressions`. Keep `source-Core-readiness` non-required until the
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
