# Implementation plan: Release workflow migration

Spec: [spec.md](spec.md). Owner: backend workflow migration owner.
Input revision: `017b541d678c6acb93eb5e843b1eda5a66babc32`.

## Contract alignment

Follow the [constitution](../../.specify/memory/constitution.md),
[development verification workflow](../../docs/development-verification.md) and
[canonical contracts](spec.md#purpose-and-canonical-contracts).
The workflow records work and evidence; it does not replace normative authority.

## Approach and exact files

Keep the four workflow artifacts, generated roadmap task region and
`tools/release_task_projection{,_test}.clj` in scope. Edit implementation
records only in [tasks.md](tasks.md#implementation-tasks); the roadmap task link
below is a generated view. Retain the entire original migration file as history.
Shared helpers remain unchanged. Shared migration indexes are owned
by the migration coordinator and are not edited by this package.

| Canonical truth | Reference | Package use |
| --- | --- | --- |
| Finite outcome | [Release outcome](../../docs/component-roadmaps/release.md#release-outcome) | Declares component scope, not migration completion |
| Implementation tasks | [Release canonical remaining work](../../docs/component-roadmaps/release.md#remaining-tasks-and-evidence) | the five remaining-task rows with selected-compiler prerequisite narrowing; retain their original status and evidence |
| Component acceptance | [Acceptance criteria](../../docs/component-roadmaps/release.md#acceptance-and-integration) | Required semantic evidence stays here |
| Phase tasks | [Phase implementation roadmap](../../docs/phase-15-bootstrap-and-self-hosting/IMPLEMENTATION-ROADMAP.md) | Historical/current phase claims retain original scope |

Component role: bootstrap/release maintainer. Interface owners: [runtime](../../docs/component-roadmaps/runtime.md#release-outcome), [stdlib](../../docs/component-roadmaps/stdlib.md#release-outcome), [build](../../docs/component-roadmaps/build.md#release-outcome), [cli](../../docs/component-roadmaps/cli.md#release-outcome), [auth](../../docs/component-roadmaps/auth.md#release-outcome).
Release admission consumes accepted component outcomes and exact candidate evidence. Ordinary development is not release admission. Runtime compiler-services, Standard Library compiler-support and selected compiler CLI outcomes bound the compiler prerequisite surface; wider platform outcomes remain separate. No seed retirement follows from this migration.

## Validation and evidence

Check ASCII, the exact seven-file set, local Markdown target/anchor resolution,
semantic package names and absence of duplicated component checklists.
Negative acceptance cases are broken/missing links, treating artifact presence
as implementation completion, or narrowing retained contracts without disposition.
Run the existing structural documentation and full-language roadmap validators.
Run focused projector, link-preservation and language-boundary checks; no
compiler/service execution suite is claimed. Record commands/results and exact review identity in evidence.md.

## Delivery and remaining work

Publish partial artifact work in a draft PR. Keep package review and integration
unaccepted until their actual receipts exist. Deus owns final integration;
repository validation cannot confer semantic component or release acceptance.
Do not add a recurring acceptance loop solely because evidence text changes.

## Names, callers and compatibility

No source names, callers, wire/schema identities, load order or historical pins
change. Retain original task identities and phase anchors. Future source work
uses subsystem/behavior names under the semantic naming rule, separately reviewed.

## Additional contract interfaces

The [central contract mapping](../../.specify/contract-mapping.md) records
these scoped dependencies. They add navigation and evidence obligations, not
implementation tasks, accepted outcomes or new semantic authority. Existing
task IDs, statuses, acceptance text and historical receipts retain their scope.

- [TEST13](../../docs/phase-14-testing-verification-and-conformance/202-test13-self-hosting-validation-plan.md#requirements): Stage admission requires declared trusted inputs, conformance, rebuild comparisons, provenance and TCB delta. Gravity source presence or a wrapper does not establish seed retirement.
- [GOV2](../../docs/phase-17-governance-and-evolution/232-gov2-compatibility-policy.md#requirements): Consume baseline/profile/target/artifact compatibility reports and migration records for release claims; Release does not approve semantic breaks.

## Governance evidence consumers

This bounded requirement-reconciliation correction binds four previously
unlinked contracts to the existing task
`release/remaining-tasks-and-evidence/bind-each-claimed-component-to-exact-implementation-and-conformance`
in [tasks.md](tasks.md#implementation-tasks). Its ID, title, Open status and
closing evidence remain byte-identical. This table explains the applicable
governance inputs to its existing positive/negative evidence obligation; it
does not amend task acceptance. The existing exact-candidate independent
acceptance task remains separate. No implementation record is added.

| Contract | Evidence consumed by Release | Required negative evidence |
| --- | --- | --- |
| [GOV1](../../docs/phase-17-governance-and-evolution/231-gov1-language-evolution-process.md#conformance-criteria) | Owned change ID, affected contracts, decision history and trace through implementation, conformance and release notes | Ownerless changes, untraceable accepted changes and omitted user-visible release notes reject admission |
| [GOV6](../../docs/phase-17-governance-and-evolution/236-gov6-rfc-process.md#conformance-criteria) | RFC decision, affected contracts, design/impact/test/migration sections, applicable specialized reviews and implementation/validation links | Missing required sections, review history or implementation/conformance links cannot support release |
| [GOV7](../../docs/phase-17-governance-and-evolution/237-gov7-experimental-feature-policy.md#conformance-criteria) | Explicit opt-in, stable default-off or governed preview-channel decision, profile availability, expiry, rollback, stabilization/removal criteria, package/build dependencies, on/off fixtures and lifecycle notes | Accidental experimental use, missing dependency metadata or expiry without a reviewed decision rejects admission |
| [GOV8](../../docs/phase-17-governance-and-evolution/238-gov8-deprecation-and-stabilization-policy.md#conformance-criteria) | Exact stabilization surfaces and compatibility/conformance/negative-fixture history; or replacement/rationale, diagnostic, schedule and migration records; tests through removal and release lifecycle notes | Evidence-free stabilization, missing migration metadata or removal before the approved window without the security-emergency records rejects admission |

These are consumer interfaces, not assignment of language-policy authority to
Release. The broader evolution workflow proposed in
[contract mapping](../../.specify/contract-mapping.md#evolution-workflow-proposal)
still needs an accountable policy owner and full lifecycle implementation.
[FL-P17-T01](../../docs/full-language-implementation-gap-map.md#phase-17---governance-and-evolution)
remains Open for real command-produced records and release-blocking behavior.
The Phase 17 stage0 history is preserved; it does not accept this wider gate.

PR445's proposed requirement population and missing-interface census are not
recomputed by adding these links. Source/facet-to-task entailment still needs
exact-manifest review; no candidate requirement gains implementation credit.

## Proposed historical-gap criteria dependencies

The following interfaces supply the concrete criterion's inputs; their entire
components need not be complete before a selected fixture is developed.
Conformance producers execute checks and emit evidence. Intermediate admission
and final release consume it independently. All affected tasks remain Open.

| Source obligation | Concrete task | Required input interfaces |
| --- | --- | --- |
| `bootstrap/release-supply-chain` | `release/remaining-tasks-and-evidence/execute-staged-compiler-validation` | `release/remaining-tasks-and-evidence/produce-actual-self-hosted-compilerruntime-execution`, `release/remaining-tasks-and-evidence/build-reproducible-signed-distributions-with-provenancesbom` |
| `bootstrap/trust-reduction` | `release/remaining-tasks-and-evidence/execute-staged-compiler-validation` | `release/remaining-tasks-and-evidence/produce-actual-self-hosted-compilerruntime-execution`, `release/remaining-tasks-and-evidence/build-reproducible-signed-distributions-with-provenancesbom` |
| `compiler/c1/self-hosting-comparison` | `release/remaining-tasks-and-evidence/complete-stage-compatibilityequivalence-across-the-claimed-compiler` | `release/remaining-tasks-and-evidence/produce-actual-self-hosted-compilerruntime-execution` |
| `testing/test1/portable-language` | `release/remaining-tasks-and-evidence/execute-language-conformance-fixtures` | `pipeline`, `release/remaining-tasks-and-evidence/execute-profile-compliance-fixtures` |
| `testing/test1/profile-separation` | `release/remaining-tasks-and-evidence/execute-language-conformance-fixtures` | `pipeline`, `release/remaining-tasks-and-evidence/execute-profile-compliance-fixtures` |
| `testing/test1/report` | `release/remaining-tasks-and-evidence/execute-language-conformance-fixtures` | `pipeline`, `release/remaining-tasks-and-evidence/execute-profile-compliance-fixtures` |
| `testing/test1/syntax-origin` | `release/remaining-tasks-and-evidence/execute-language-conformance-fixtures` | `pipeline`, `release/remaining-tasks-and-evidence/execute-profile-compliance-fixtures` |
| `testing/test10/backend-artifact` | `release/remaining-tasks-and-evidence/execute-differential-conformance-fixtures` | `backend-conformance`, `math`, `runtime/remaining-tasks-and-evidence/connect-distributedai-replay-and-interactive-execution` |
| `testing/test10/context-divergence` | `release/remaining-tasks-and-evidence/execute-differential-conformance-fixtures` | `backend-conformance`, `math`, `runtime/remaining-tasks-and-evidence/connect-distributedai-replay-and-interactive-execution` |
| `testing/test10/numeric-mode` | `release/remaining-tasks-and-evidence/execute-differential-conformance-fixtures` | `backend-conformance`, `math`, `runtime/remaining-tasks-and-evidence/connect-distributedai-replay-and-interactive-execution` |
| `testing/test10/oracle-observables` | `release/remaining-tasks-and-evidence/execute-differential-conformance-fixtures` | `backend-conformance`, `math`, `runtime/remaining-tasks-and-evidence/connect-distributedai-replay-and-interactive-execution` |
| `testing/test10/recorded-nondeterminism` | `release/remaining-tasks-and-evidence/execute-differential-conformance-fixtures` | `backend-conformance`, `math`, `runtime/remaining-tasks-and-evidence/connect-distributedai-replay-and-interactive-execution` |
| `testing/test10/report` | `release/remaining-tasks-and-evidence/execute-differential-conformance-fixtures` | `backend-conformance`, `math`, `runtime/remaining-tasks-and-evidence/connect-distributedai-replay-and-interactive-execution` |
| `testing/test11/formal-obligations` | `release/remaining-tasks-and-evidence/execute-formal-evidence-checks` | `pass-evidence`, `math`, `legality/remaining-tasks-and-evidence/complete-profile-restrictions-and-generated-form-checks` |
| `testing/test11/pass-linkage` | `release/remaining-tasks-and-evidence/execute-formal-evidence-checks` | `pass-evidence`, `math`, `legality/remaining-tasks-and-evidence/complete-profile-restrictions-and-generated-form-checks` |
| `testing/test11/proof-identity` | `release/remaining-tasks-and-evidence/execute-formal-evidence-checks` | `pass-evidence`, `math`, `legality/remaining-tasks-and-evidence/complete-profile-restrictions-and-generated-form-checks` |
| `testing/test11/report` | `release/remaining-tasks-and-evidence/execute-formal-evidence-checks` | `pass-evidence`, `math`, `legality/remaining-tasks-and-evidence/complete-profile-restrictions-and-generated-form-checks` |
| `testing/test12/ai-costs` | `release/remaining-tasks-and-evidence/execute-performance-regression-fixtures` | `tools/finite-service-outcomes-and-remaining-work/profile-performance-tool-maintainer`, `release/remaining-tasks-and-evidence/execute-differential-conformance-fixtures`, `release/remaining-tasks-and-evidence/execute-safety-conformance-fixtures` |
| `testing/test12/device-counters` | `release/remaining-tasks-and-evidence/execute-performance-regression-fixtures` | `tools/finite-service-outcomes-and-remaining-work/profile-performance-tool-maintainer`, `release/remaining-tasks-and-evidence/execute-differential-conformance-fixtures`, `release/remaining-tasks-and-evidence/execute-safety-conformance-fixtures` |
| `testing/test12/report` | `release/remaining-tasks-and-evidence/execute-performance-regression-fixtures` | `tools/finite-service-outcomes-and-remaining-work/profile-performance-tool-maintainer`, `release/remaining-tasks-and-evidence/execute-differential-conformance-fixtures`, `release/remaining-tasks-and-evidence/execute-safety-conformance-fixtures` |
| `testing/test12/semantic-safety-gate` | `release/remaining-tasks-and-evidence/execute-performance-regression-fixtures` | `tools/finite-service-outcomes-and-remaining-work/profile-performance-tool-maintainer`, `release/remaining-tasks-and-evidence/execute-differential-conformance-fixtures`, `release/remaining-tasks-and-evidence/execute-safety-conformance-fixtures` |
| `testing/test13/diagnostic-schema` | `release/remaining-tasks-and-evidence/execute-staged-compiler-validation` | `release/remaining-tasks-and-evidence/produce-actual-self-hosted-compilerruntime-execution`, `release/remaining-tasks-and-evidence/build-reproducible-signed-distributions-with-provenancesbom` |
| `testing/test13/rebuild-equivalence` | `release/remaining-tasks-and-evidence/execute-staged-compiler-validation` | `release/remaining-tasks-and-evidence/produce-actual-self-hosted-compilerruntime-execution`, `release/remaining-tasks-and-evidence/build-reproducible-signed-distributions-with-provenancesbom` |
| `testing/test13/report` | `release/remaining-tasks-and-evidence/execute-staged-compiler-validation` | `release/remaining-tasks-and-evidence/produce-actual-self-hosted-compilerruntime-execution`, `release/remaining-tasks-and-evidence/build-reproducible-signed-distributions-with-provenancesbom` |
| `testing/test13/stage-conformance` | `release/remaining-tasks-and-evidence/execute-staged-compiler-validation` | `release/remaining-tasks-and-evidence/produce-actual-self-hosted-compilerruntime-execution`, `release/remaining-tasks-and-evidence/build-reproducible-signed-distributions-with-provenancesbom` |
| `testing/test13/trusting-trust` | `release/remaining-tasks-and-evidence/execute-staged-compiler-validation` | `release/remaining-tasks-and-evidence/produce-actual-self-hosted-compilerruntime-execution`, `release/remaining-tasks-and-evidence/build-reproducible-signed-distributions-with-provenancesbom` |
| `testing/test2/cache-invalidation` | `release/remaining-tasks-and-evidence/execute-compiler-preservation-fixtures` | `pipeline`, `plugins`, `pass-evidence`, `build/remaining-tasks-and-evidence/implement-dependency-aware-caching-and-invalidation` |
| `testing/test2/development-feedback` | `release/remaining-tasks-and-evidence/execute-compiler-preservation-fixtures` | `pipeline`, `plugins`, `pass-evidence`, `build/remaining-tasks-and-evidence/implement-dependency-aware-caching-and-invalidation` |
| `testing/test2/diagnostic-match` | `release/remaining-tasks-and-evidence/execute-compiler-preservation-fixtures` | `pipeline`, `plugins`, `pass-evidence`, `build/remaining-tasks-and-evidence/implement-dependency-aware-caching-and-invalidation` |
| `testing/test2/generated-reentry` | `release/remaining-tasks-and-evidence/execute-compiler-preservation-fixtures` | `pipeline`, `plugins`, `pass-evidence`, `build/remaining-tasks-and-evidence/implement-dependency-aware-caching-and-invalidation` |
| `testing/test2/optimization-soundness` | `release/remaining-tasks-and-evidence/execute-compiler-preservation-fixtures` | `pipeline`, `plugins`, `pass-evidence`, `build/remaining-tasks-and-evidence/implement-dependency-aware-caching-and-invalidation` |
| `testing/test2/plugin-boundaries` | `release/remaining-tasks-and-evidence/execute-compiler-preservation-fixtures` | `pipeline`, `plugins`, `pass-evidence`, `build/remaining-tasks-and-evidence/implement-dependency-aware-caching-and-invalidation` |
| `testing/test2/report` | `release/remaining-tasks-and-evidence/execute-compiler-preservation-fixtures` | `pipeline`, `plugins`, `pass-evidence`, `build/remaining-tasks-and-evidence/implement-dependency-aware-caching-and-invalidation` |
| `testing/test2/stage-preservation` | `release/remaining-tasks-and-evidence/execute-compiler-preservation-fixtures` | `pipeline`, `plugins`, `pass-evidence`, `build/remaining-tasks-and-evidence/implement-dependency-aware-caching-and-invalidation` |
| `testing/test3/ai-ledger` | `release/remaining-tasks-and-evidence/execute-runtime-conformance-fixtures` | `runtime/remaining-tasks-and-evidence/implement-explicit-family-selection-and-no-runtime-rejection`, `runtime/remaining-tasks-and-evidence/implement-memory-concurrency-and-typed-ffi-boundaries`, `runtime/remaining-tasks-and-evidence/connect-distributedai-replay-and-interactive-execution`, `runtime/remaining-tasks-and-evidence/close-enforcement-and-observability-integration` |
| `testing/test3/capability-denial` | `release/remaining-tasks-and-evidence/execute-runtime-conformance-fixtures` | `runtime/remaining-tasks-and-evidence/implement-explicit-family-selection-and-no-runtime-rejection`, `runtime/remaining-tasks-and-evidence/implement-memory-concurrency-and-typed-ffi-boundaries`, `runtime/remaining-tasks-and-evidence/connect-distributedai-replay-and-interactive-execution`, `runtime/remaining-tasks-and-evidence/close-enforcement-and-observability-integration` |
| `testing/test3/concurrency` | `release/remaining-tasks-and-evidence/execute-runtime-conformance-fixtures` | `runtime/remaining-tasks-and-evidence/implement-explicit-family-selection-and-no-runtime-rejection`, `runtime/remaining-tasks-and-evidence/implement-memory-concurrency-and-typed-ffi-boundaries`, `runtime/remaining-tasks-and-evidence/connect-distributedai-replay-and-interactive-execution`, `runtime/remaining-tasks-and-evidence/close-enforcement-and-observability-integration` |
| `testing/test3/family-coverage` | `release/remaining-tasks-and-evidence/execute-runtime-conformance-fixtures` | `runtime/remaining-tasks-and-evidence/implement-explicit-family-selection-and-no-runtime-rejection`, `runtime/remaining-tasks-and-evidence/implement-memory-concurrency-and-typed-ffi-boundaries`, `runtime/remaining-tasks-and-evidence/connect-distributedai-replay-and-interactive-execution`, `runtime/remaining-tasks-and-evidence/close-enforcement-and-observability-integration` |
| `testing/test3/ffi-boundary` | `release/remaining-tasks-and-evidence/execute-runtime-conformance-fixtures` | `runtime/remaining-tasks-and-evidence/implement-explicit-family-selection-and-no-runtime-rejection`, `runtime/remaining-tasks-and-evidence/implement-memory-concurrency-and-typed-ffi-boundaries`, `runtime/remaining-tasks-and-evidence/connect-distributedai-replay-and-interactive-execution`, `runtime/remaining-tasks-and-evidence/close-enforcement-and-observability-integration` |
| `testing/test3/memory-safety` | `release/remaining-tasks-and-evidence/execute-runtime-conformance-fixtures` | `runtime/remaining-tasks-and-evidence/implement-explicit-family-selection-and-no-runtime-rejection`, `runtime/remaining-tasks-and-evidence/implement-memory-concurrency-and-typed-ffi-boundaries`, `runtime/remaining-tasks-and-evidence/connect-distributedai-replay-and-interactive-execution`, `runtime/remaining-tasks-and-evidence/close-enforcement-and-observability-integration` |
| `testing/test3/observability` | `release/remaining-tasks-and-evidence/execute-runtime-conformance-fixtures` | `runtime/remaining-tasks-and-evidence/implement-explicit-family-selection-and-no-runtime-rejection`, `runtime/remaining-tasks-and-evidence/implement-memory-concurrency-and-typed-ffi-boundaries`, `runtime/remaining-tasks-and-evidence/connect-distributedai-replay-and-interactive-execution`, `runtime/remaining-tasks-and-evidence/close-enforcement-and-observability-integration` |
| `testing/test3/replay` | `release/remaining-tasks-and-evidence/execute-runtime-conformance-fixtures` | `runtime/remaining-tasks-and-evidence/implement-explicit-family-selection-and-no-runtime-rejection`, `runtime/remaining-tasks-and-evidence/implement-memory-concurrency-and-typed-ffi-boundaries`, `runtime/remaining-tasks-and-evidence/connect-distributedai-replay-and-interactive-execution`, `runtime/remaining-tasks-and-evidence/close-enforcement-and-observability-integration` |
| `testing/test3/report` | `release/remaining-tasks-and-evidence/execute-runtime-conformance-fixtures` | `runtime/remaining-tasks-and-evidence/implement-explicit-family-selection-and-no-runtime-rejection`, `runtime/remaining-tasks-and-evidence/implement-memory-concurrency-and-typed-ffi-boundaries`, `runtime/remaining-tasks-and-evidence/connect-distributedai-replay-and-interactive-execution`, `runtime/remaining-tasks-and-evidence/close-enforcement-and-observability-integration` |
| `testing/test4/allocation-runtime` | `release/remaining-tasks-and-evidence/execute-profile-compliance-fixtures` | `legality/remaining-tasks-and-evidence/close-conformance-for-the-declared-profile-surface`, `backend-interface` |
| `testing/test4/capability-legality` | `release/remaining-tasks-and-evidence/execute-profile-compliance-fixtures` | `legality/remaining-tasks-and-evidence/close-conformance-for-the-declared-profile-surface`, `backend-interface` |
| `testing/test4/compatibility` | `release/remaining-tasks-and-evidence/execute-profile-compliance-fixtures` | `legality/remaining-tasks-and-evidence/close-conformance-for-the-declared-profile-surface`, `backend-interface` |
| `testing/test4/delegation` | `release/remaining-tasks-and-evidence/execute-profile-compliance-fixtures` | `legality/remaining-tasks-and-evidence/close-conformance-for-the-declared-profile-surface`, `backend-interface` |
| `testing/test4/profile-legality` | `release/remaining-tasks-and-evidence/execute-profile-compliance-fixtures` | `legality/remaining-tasks-and-evidence/close-conformance-for-the-declared-profile-surface`, `backend-interface` |
| `testing/test4/report` | `release/remaining-tasks-and-evidence/execute-profile-compliance-fixtures` | `legality/remaining-tasks-and-evidence/close-conformance-for-the-declared-profile-surface`, `backend-interface` |
| `testing/test5/capability-bypass` | `release/remaining-tasks-and-evidence/execute-safety-conformance-fixtures` | `legality/remaining-tasks-and-evidence/complete-safe-code-classification-and-taintunsafe-boundaries`, `legality/remaining-tasks-and-evidence/preserve-obligations-into-mir-and-runtime-execution`, `opt/current-evidence-and-remaining-work/opt3-protect-checks-and-anchors` |
| `testing/test5/check-elision` | `release/remaining-tasks-and-evidence/execute-safety-conformance-fixtures` | `legality/remaining-tasks-and-evidence/complete-safe-code-classification-and-taintunsafe-boundaries`, `legality/remaining-tasks-and-evidence/preserve-obligations-into-mir-and-runtime-execution`, `opt/current-evidence-and-remaining-work/opt3-protect-checks-and-anchors` |
| `testing/test5/report` | `release/remaining-tasks-and-evidence/execute-safety-conformance-fixtures` | `legality/remaining-tasks-and-evidence/complete-safe-code-classification-and-taintunsafe-boundaries`, `legality/remaining-tasks-and-evidence/preserve-obligations-into-mir-and-runtime-execution`, `opt/current-evidence-and-remaining-work/opt3-protect-checks-and-anchors` |
| `testing/test5/runtime-checks` | `release/remaining-tasks-and-evidence/execute-safety-conformance-fixtures` | `legality/remaining-tasks-and-evidence/complete-safe-code-classification-and-taintunsafe-boundaries`, `legality/remaining-tasks-and-evidence/preserve-obligations-into-mir-and-runtime-execution`, `opt/current-evidence-and-remaining-work/opt3-protect-checks-and-anchors` |
| `testing/test5/safe-outcomes` | `release/remaining-tasks-and-evidence/execute-safety-conformance-fixtures` | `legality/remaining-tasks-and-evidence/complete-safe-code-classification-and-taintunsafe-boundaries`, `legality/remaining-tasks-and-evidence/preserve-obligations-into-mir-and-runtime-execution`, `opt/current-evidence-and-remaining-work/opt3-protect-checks-and-anchors` |
| `testing/test5/taint` | `release/remaining-tasks-and-evidence/execute-safety-conformance-fixtures` | `legality/remaining-tasks-and-evidence/complete-safe-code-classification-and-taintunsafe-boundaries`, `legality/remaining-tasks-and-evidence/preserve-obligations-into-mir-and-runtime-execution`, `opt/current-evidence-and-remaining-work/opt3-protect-checks-and-anchors` |
| `testing/test5/unsafe-audit` | `release/remaining-tasks-and-evidence/execute-safety-conformance-fixtures` | `legality/remaining-tasks-and-evidence/complete-safe-code-classification-and-taintunsafe-boundaries`, `legality/remaining-tasks-and-evidence/preserve-obligations-into-mir-and-runtime-execution`, `opt/current-evidence-and-remaining-work/opt3-protect-checks-and-anchors` |
| `testing/test7/availability` | `release/remaining-tasks-and-evidence/execute-library-conformance-fixtures` | `stdlib/remaining-tasks-and-evidence/reconcile-export-inventory-with-actual-callable-implementations`, `stdlib/remaining-tasks-and-evidence/integrate-platform-modules-and-close-release-manifests` |
| `testing/test7/capability-denial` | `release/remaining-tasks-and-evidence/execute-library-conformance-fixtures` | `stdlib/remaining-tasks-and-evidence/reconcile-export-inventory-with-actual-callable-implementations`, `stdlib/remaining-tasks-and-evidence/integrate-platform-modules-and-close-release-manifests` |
| `testing/test7/properties` | `release/remaining-tasks-and-evidence/execute-library-conformance-fixtures` | `stdlib/remaining-tasks-and-evidence/reconcile-export-inventory-with-actual-callable-implementations`, `stdlib/remaining-tasks-and-evidence/integrate-platform-modules-and-close-release-manifests` |
| `testing/test7/public-api-coverage` | `release/remaining-tasks-and-evidence/execute-library-conformance-fixtures` | `stdlib/remaining-tasks-and-evidence/reconcile-export-inventory-with-actual-callable-implementations`, `stdlib/remaining-tasks-and-evidence/integrate-platform-modules-and-close-release-manifests` |
| `testing/test7/report` | `release/remaining-tasks-and-evidence/execute-library-conformance-fixtures` | `stdlib/remaining-tasks-and-evidence/reconcile-export-inventory-with-actual-callable-implementations`, `stdlib/remaining-tasks-and-evidence/integrate-platform-modules-and-close-release-manifests` |
| `testing/test7/stability` | `release/remaining-tasks-and-evidence/execute-library-conformance-fixtures` | `stdlib/remaining-tasks-and-evidence/reconcile-export-inventory-with-actual-callable-implementations`, `stdlib/remaining-tasks-and-evidence/integrate-platform-modules-and-close-release-manifests` |
| `testing/test7/unsafe-wrappers` | `release/remaining-tasks-and-evidence/execute-library-conformance-fixtures` | `stdlib/remaining-tasks-and-evidence/reconcile-export-inventory-with-actual-callable-implementations`, `stdlib/remaining-tasks-and-evidence/integrate-platform-modules-and-close-release-manifests` |
| `testing/test8/human-review` | `release/remaining-tasks-and-evidence/execute-agent-and-workflow-evaluations` | `ai`, `runtime/remaining-tasks-and-evidence/connect-distributedai-replay-and-interactive-execution` |
| `testing/test8/metric-release` | `release/remaining-tasks-and-evidence/execute-agent-and-workflow-evaluations` | `ai`, `runtime/remaining-tasks-and-evidence/connect-distributedai-replay-and-interactive-execution` |
| `testing/test8/report` | `release/remaining-tasks-and-evidence/execute-agent-and-workflow-evaluations` | `ai`, `runtime/remaining-tasks-and-evidence/connect-distributedai-replay-and-interactive-execution` |
| `testing/test9/live-effect-policy` | `release/remaining-tasks-and-evidence/execute-fuzz-and-property-fixtures` | `pipeline`, `mir/current-evidence-and-remaining-work/mir3-verify-graph-and-legality`, `schema`, `math`, `runtime/remaining-tasks-and-evidence/connect-distributedai-replay-and-interactive-execution` |
| `testing/test9/minimize-regressions` | `release/remaining-tasks-and-evidence/execute-fuzz-and-property-fixtures` | `pipeline`, `mir/current-evidence-and-remaining-work/mir3-verify-graph-and-legality`, `schema`, `math`, `runtime/remaining-tasks-and-evidence/connect-distributedai-replay-and-interactive-execution` |
| `testing/test9/mir-preservation` | `release/remaining-tasks-and-evidence/execute-fuzz-and-property-fixtures` | `pipeline`, `mir/current-evidence-and-remaining-work/mir3-verify-graph-and-legality`, `schema`, `math`, `runtime/remaining-tasks-and-evidence/connect-distributedai-replay-and-interactive-execution` |
| `testing/test9/property-oracle` | `release/remaining-tasks-and-evidence/execute-fuzz-and-property-fixtures` | `pipeline`, `mir/current-evidence-and-remaining-work/mir3-verify-graph-and-legality`, `schema`, `math`, `runtime/remaining-tasks-and-evidence/connect-distributedai-replay-and-interactive-execution` |
| `testing/test9/replay-identity` | `release/remaining-tasks-and-evidence/execute-fuzz-and-property-fixtures` | `pipeline`, `mir/current-evidence-and-remaining-work/mir3-verify-graph-and-legality`, `schema`, `math`, `runtime/remaining-tasks-and-evidence/connect-distributedai-replay-and-interactive-execution` |
| `testing/test9/report` | `release/remaining-tasks-and-evidence/execute-fuzz-and-property-fixtures` | `pipeline`, `mir/current-evidence-and-remaining-work/mir3-verify-graph-and-legality`, `schema`, `math`, `runtime/remaining-tasks-and-evidence/connect-distributedai-replay-and-interactive-execution` |
| `testing/test9/schema-canonicalization` | `release/remaining-tasks-and-evidence/execute-fuzz-and-property-fixtures` | `pipeline`, `mir/current-evidence-and-remaining-work/mir3-verify-graph-and-legality`, `schema`, `math`, `runtime/remaining-tasks-and-evidence/connect-distributedai-replay-and-interactive-execution` |
| `testing/test9/valid-invalid` | `release/remaining-tasks-and-evidence/execute-fuzz-and-property-fixtures` | `pipeline`, `mir/current-evidence-and-remaining-work/mir3-verify-graph-and-legality`, `schema`, `math`, `runtime/remaining-tasks-and-evidence/connect-distributedai-replay-and-interactive-execution` |
