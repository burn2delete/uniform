# Effects implementation planning entry

Select one unresolved record from [tasks.md](tasks.md#implementation-tasks),
preserving the original transfer plan and existing pending source-family
amendments. Reconcile its plan against [spec.md](spec.md), complete L6/C8
semantics and the actual typed-Core, Auth, Legality and runtime/domain interface.
Resolve current semantic implementation and transport paths from
[development verification](../../docs/development-verification.md) and the
[stage ownership table](../../docs/bootstrap/stage-owned-namespaces.md).

Name actual operations and call/dispatch/recursive edges, direct versus latent
and handled/escaping facts, declarations, profile/provider/grants and replay
inputs. Supply concrete positive/negative programs for the selected fact or
handler class. Cover missing or hidden actions, residual effects, type/profile/
capability mismatch, illegal continuation transfer, invalid record/replay and
changed-input invalidation where applicable. A fake must retain original labels,
its explicit fixture capability and absence of live authority.

Use focused owner tests and proportionate Types/Auth/Legality/MIR and selected
runtime/domain composition; do not substitute declared allowances or metadata
for inference or real interpretation. Preserve all reserved writers and held
provider/replay operations. Refine the existing authoritative task only, record
exact passed/failed/unrun evidence and retain separate independent acceptance,
integration and wider-runtime/handler completion boundaries.

## Source memory-model and provider interface

Use the [complete source memory-model and provider criteria](../legality-workflow/plan.md#complete-source-memory-model-and-provider-criteria) with the existing task authority.

The existing effects3 task exposes L10/L18 regime/provider/allocation/failure/blocking/cleanup/transfer behavior and volatile/MMIO/atomic ordering before MIR. Enforce actual profile/grant and source memory/storage policy, including hidden or ambient allocation and AI/distributed schema-governed storage. Actual capacity/alignment/representation/timing remains distinct from removed compiler accounting. Keep inference separate from declarations, authority grants and provider execution; preserve the C8 criteria and their frozen source scope.

The new whole-source proposal preserves the eight finite review qualifications; current independent review, full cross-component entailment and actual implementation acceptance remain pending. No compiler/provider/native/device/callback/probe/replay/migration/review/signing/publication action runs in this planning increment.

## Complete effect-checker source criteria

Restore the five complete C8 criteria under the existing five task IDs, retaining
their short original criteria and Open statuses. The preserved
`c8-complete-current-facet-trace.edn` at
`94dfa9c535268276765b76366c5c60687fa3807a`, SHA-256
`783919a0db49d25e48c553dc8d28dcc13f8310efc212d5228c3f267d66ada199`,
binds all 103 facets, fifty occurrences, 34 semantic conjunctions and five
original owner joins. Its frozen review at `1ecdfa59` covers only those C8
source conjunctions, excluding full L6, attached foundation predicates, later
heads and actual implementation. Current independent review remains pending.

| Existing task | Implementation and decisive evidence |
| --- | --- |
| `effects1-infer-direct-effects` | Compute real typed-Core expression/function/namespace/module/generated-output actions, with direct/latent/transitive/throws/build/replay/residual facts or a diagnostic for every effectful expression. Preserve registered concrete labels and source/generated identities. Keep the three [build/runtime/package domains](../../docs/phase-06-compiler-architecture/087-c8-effect-checker-design.md#build-and-runtime-effects) separate; exercise build-effectful macros emitting pure code and pure macros emitting effectful code. Reject hidden effects and unknown names. Residual checks/dispatch/provider/capability/logging remain visible. |
| `effects2-propagate-latent-effects` | Derive call/closure/dispatch/recursive summaries to a sound fixed point from actual typed edges; declaration allowances are not inferred bodies. Preserve conservative branch unions, ordered evaluation, declared/inferred/latent/throws distinctions, handled/escaping labels, handler identity/residuals and changed-input invalidation. Exercise higher-order/recursive/branch/generated positives and missing-edge, underreported branch and false-erasure refusals. Types supplies actual call/handler request-response facts; handlers require the separate L6 conditions below. |
| `effects3-check-legality-and-ordering` | Check all applicable source-function/namespace/profile/package/build/deployment/runtime-provider/safety conditions before MIR. Consume actual Auth grant evidence with scope/principal/phase/provider; effects and capability sets stay distinct, and authority evidence is not a safety proof. Emit all seven [ordering families](../../docs/phase-06-compiler-architecture/087-c8-effect-checker-design.md#ordering-constraints). Opt consumes actual legality/order facts before reordering/fusion/elimination. Exercise resource, volatile/MMIO/atomic, workflow and AI cases with refused widening, forbidden effects, missing grants/providers or order evidence. |
| `effects4-record-replay-and-invalidation` | Emit obligations for all nine [replay-sensitive classes](../../docs/phase-06-compiler-architecture/087-c8-effect-checker-design.md#replay-and-nondeterminism), including `:ai/human-review`; record actual grants/performed build effects and invalidate changed call/type/declaration/provider/policy/replay inputs. Runtime/Workflow/AI/test owners perform actual record/replay. Reject missing/mismatched/reordered/extra records, replayed live nondeterminism, ungranted build actions and profiles forbidding replay gaps before lowering. Obligation records alone do not prove that consumers obey them. |
| `effects5-integrate-checked-core-consumers` | Deliver actual versioned graph, latent/throws and namespace/module summaries, authority evidence, build log, replay obligations, order, residuals and diagnostics to Auth/Legality/MIR and selected Opt/Lower/Runtime/Tools consumers. Exercise actual accepted/refused producer-consumer cases and reject stale/malformed/missing semantic products. Preserve all nine [C8 diagnostic meanings](../../docs/phase-06-compiler-architecture/087-c8-effect-checker-design.md#diagnostics) and core-node/span/generated-origin/function/namespace/effect/capability/profile/target/provider/grant/remediation context. |

The same task owners retain all six [L6 handler classes](../../docs/phase-01-core-language/016-l6-effect-system-specification.md#typed-effect-handlers).
Types checks request/response/result facts; Effects preserves labels, handled/
escaping summaries and residual actions; Auth checks authority; Legality checks
profile, continuation/resource/safety obligations. Runtime/domain owners execute
async/generator/resumable-error/workflow/tool/test interpretation, including
resume/cancel/close/completion, affine defaults, explicit permitted multi-shot
proofs, safe suspension transfers and record/replay rules. A fake records the
original label, fixture identity/capability and absent live authority; it never
grants a forbidden profile or hides an unexpected live effect. These interfaces
remain required without treating C8's frozen review as full L6 acceptance.

Restore two earlier source clauses that the C8 replay/handoff context depends
on, under those same tasks. The record-versus-replay clause is qualified by
[R6](../../docs/phase-08-runtime-architecture/117-r6-concurrency-runtime-design.md#replay-sensitive-concurrency),
[R7](../../docs/phase-08-runtime-architecture/118-r7-distributed-runtime-design.md#event-logs-and-replay)
and [R8](../../docs/phase-08-runtime-architecture/119-r8-ai-runtime-design.md#replay-and-budgets).
Only an operation actually permitted by L6 and the active profile can use R6's
declared replay-safe case, outside stricter R7/R8 recorded-output segments.
Graph declarations never authorize arbitrary repeated external calls. The
derived-manifest clause consumes [PKG6](../../docs/phase-12-build-package-and-artifact-system/170-pkg6-capability-and-permission-manifest-specification.md#permission-flow):
Effects supplies actual analysis/derivation and deterministic capability-summary
data; Build packages the manifest/provenance/SBOM, Auth owns grants and Runtime
enforces scoped handles. Requests, denies and deployment grants stay distinct.
Retain exact privileged/profile/target/dependency/tool/agent/FFI/unsafe/review/
handle/audit fields and actual diff/refusal cases, without duplicating the
package system or accepting whole PKG6/R6/R7/R8/foundation implementations.

Start local inference/summary/refusal fixtures against explicit typed-Core,
registry, grant, handler and runtime interfaces. Compose actual products for
acceptance; no whole dependency release or fresh migration admission is an
ordinary editing prerequisite. Do not execute held C7/C12/compiler/provider/
replay/probe/publication routes to prepare this plan. Current accounting removal
supersedes project work/token/call/fuel allowances in retained L6/R8 text;
meaningful runtime timeout/retry/failure semantics, semantic validity, real host
restrictions and external billing remain separate. Exact raw clause conservation
does not accept current implementation, independence, release or self-hosting.

## Historical task-authority-transfer plan

# Implementation plan: Effects task authority

Spec: [spec.md](spec.md) | Evidence: [evidence.md](evidence.md)
Owner role: compiler effects maintainer.
Input revision: `fcb23737be00f646a74200fc26faaa8908f77164`.

## Contract alignment

The [canonical inventory](../../docs/document-inventory.json) and owning
contracts remain normative. Follow the [constitution](../../.specify/memory/constitution.md),
[development workflow](../../docs/development-verification.md) and
[compiler architecture phase](../../docs/phase-06-compiler-architecture/README.md).
Only task editing authority changes; language and compiler semantics do not.

Retain these references for navigation. Cross-cutting contracts may also be used
by other component packages; inclusion is not exclusive ownership or semantic
conformance acceptance.

- [L6: Effect System Specification](../../docs/phase-01-core-language/016-l6-effect-system-specification.md)
- [L11: Concurrency Model Specification](../../docs/phase-01-core-language/021-l11-concurrency-model-specification.md)
- [C8: Effect Checker Design](../../docs/phase-06-compiler-architecture/087-c8-effect-checker-design.md)
- [TEST11: Formal Semantics and Verification Plan](../../docs/phase-14-testing-verification-and-conformance/200-test11-formal-semantics-and-verification-plan.md)

## Canonical work and handoffs

- Implementation work: [five authoritative records](tasks.md#implementation-tasks).
- Roadmap navigation: [sole task ledger](tasks.md#implementation-tasks).
- Completion criteria: [owning acceptance checklist](../../docs/component-roadmaps/effects.md#acceptance-criteria-yesno).
- Release outcome: [component finish line](../../docs/component-roadmaps/effects.md#release-outcome).
- Overall composition: [integration outcomes](../../docs/component-roadmaps/README.md#integration-outcomes).
- Input: Typed core, function/call graph, declared effects, macro build-effect records, profile rules and capability requirement mappings.
- Output: Effect graph, function/module summaries, ordering constraints, build/replay records, capability requirements and diagnostics.

Types, Core and Resolve supply real identities and call edges. Auth and Legality
consume summaries; MIR preserves ordering. Provider schemas and fixtures may
progress independently of full Runtime completion. Record order adds no
dependency graph or overall completion score.

## Approach and exact files

Edit the marked implementation region in `specs/effects-workflow/tasks.md`.
Each record has a stable semantic ID, title, checkbox, exact evidence/status
and concrete exit evidence. The known five-ID set prevents row loss/substitution;
the ID must agree with the retained phase/title slug. An intentional task-set
change needs explicit review, not positional renumbering.

## Validation and evidence

Run from repository root:

```bash
clojure -J-Xmx512m -M:test --namespace gravity.self-hosting.sh01-language-boundary-test
clojure -J-Xmx512m -M tools/validate_gravity_docs.clj
clojure -J-Xmx512m -M tools/validate_full_language_roadmap.clj
```

Check the five task IDs, statuses, preserved criteria and historical migration
ledger. Check the affected Markdown files for ASCII and local file/heading links;
check `git diff --check`. Run integration
preflight against the exact final base/commit/tree. Record actual results in
[evidence](evidence.md). Structural checks do not establish compiler semantics
or authoritative module admission. No compiler source or compiler test changes
belong to this task-authority migration.

## Delivery and remaining work

Publish a draft after independent current-head review of the complete product
payload and PR body. Keep implementation, validation, review and integration
distinct. Merge only after the maintainer confirms independent review and
integration disposition. Future implementation updates change the ledger; the
roadmap remains a direct navigation link. No task closes here.

## Names, callers and compatibility

Preserve existing anchors, links, semantic IDs and task text; consumers continue
following the direct task-ledger link. New semantic tool filenames/namespaces
follow Reader conventions. No caller, import, load order, runtime API, schema,
diagnostic, manifest hash or wire ID changes.

## Additional contract interfaces

The [central contract mapping](../../.specify/contract-mapping.md) records
these scoped dependencies. They add navigation and evidence obligations, not
implementation tasks, accepted outcomes or new semantic authority. Existing
task IDs, statuses, acceptance text and historical receipts retain their scope.

- [L7](../../docs/phase-01-core-language/017-l7-pattern-matching-specification.md#requirements): Include guard effects and preserve their evaluation order, including non-returning control transfer.
