# Tasks: Expand implementation workflow

Spec: [spec.md](spec.md) | Plan: [plan.md](plan.md) | Evidence: [evidence.md](evidence.md)
Owner role: compiler expand maintainer; existing implementation reservations are unchanged.
Authority transfer baseline: `023ec47d86004fc829ae7dc5753b715ca4a653dc`.

## Implementation tasks

This is the sole editable Expand implementation task ledger. The roadmap's
[current evidence and remaining work](../../docs/component-roadmaps/expand.md#current-evidence-and-remaining-work)
is a navigation view of these records. IDs preserve the established phase/title slug convention; the original
`expand/1` through `expand/5` labels remain unchanged display text. Order is
display order, not dependency evidence. An unchecked box means not accepted complete. Check a box only with
explicit `Accepted;` status and an immutable accepted-outcome receipt,
independently reviewed by the responsible owner. Structural validation cannot
grant acceptance. No task closes through this authority transfer.

<!-- expand-implementation-tasks:start -->

- [ ] `expand/current-evidence-and-remaining-work/expand1-execute-syntax-macros` expand/1: Execute syntax macros

Status: Open; partial source evidence above is not closure

Concrete exit evidence: Implement syntax-object invocation and macro environment/version checks, validating returned graphs and compile phases.

<!-- compile-time-source-evaluate:start -->

Apply the [complete compile-time evaluation source criteria](plan.md#complete-compile-time-evaluation-source-criteria) within this existing task. Status remains Open; finite reviewed source is not current whole-source or implementation acceptance.

Preserved finite predicate conjunctions:

Decision criterion interface/language/compiletime/evaluate-authorized-expression: Evaluate an eligible compile-time expression after semantic checks under pure policy or explicitly granted build effects; emit a stable representable value and input provenance. Reject: Reject runtime-only captures/handles, ineligible or ill-typed expressions, ungranted effects and nonrepresentable constant results. Required outputs: Constant/evaluation artifact, authorized input/effect log, stable value encoding and accepted/rejected evaluation fixtures.

Complete L12 evaluation criterion (Open): Actually evaluate compile-time expressions and produce their Gravity values, constant entries or generated syntax under ordinary type/effect/capability/memory/profile/safety semantics. Cover compile-time, defconst and derive-code entry points, pure arithmetic with declared overflow, collections, quoted/syntax data, explicitly compile-time-callable pure functions, total matching over known finite values and fixed profile/target type/layout queries. A recorded return type, operator digest or declared constant table is not the computed value. Serialize actual stable target representations and reject hosted objects, open handles, mutable cells, thread objects, ambient-state closures, unrecorded foreign references and runtime-only captures. This continues the existing finite evaluator allocation under Expand1; ordinary Core/Types/layout owners supply their real facts and representation contracts rather than a second semantic evaluator. Runtime fallback requires explicit source permission and legal resulting runtime effects; never silently change the execution phase.
<!-- compile-time-source-evaluate:end -->


<!-- compile-time-source-eligibility:start -->

Apply the [complete compile-time evaluation source criteria](plan.md#complete-compile-time-evaluation-source-criteria) within this existing task. Status remains Open; finite reviewed source is not current whole-source or implementation acceptance.

Complete L12 eligibility criterion (Open): Before compile-time invocation check actual available definition and phase, fully known or checkable types, declared legal effects, legal evaluator allocation behavior, absence of runtime-only captures and declared target dependencies. Pure mode rejects file/network/environment/clock/random/process/model/host-reflection/global-mutation/pointer-inspection/target-probe access; effects and actual grants remain distinct. Native FFI is not implicitly compile-time callable: any permitted host binding uses an explicit provider recording version, ABI, real input/output digests, declared failure and replay policy. Keep actual totality evidence when independently claimed, but removed project fuel limits, fuel-only refusals and replacement mandatory totality admission are not prerequisites. Actual phase/cycle/semantic failures and physical process/watchdog restrictions remain distinct; no held host/provider/FFI action runs here.
<!-- compile-time-source-eligibility:end -->


- [ ] `expand/current-evidence-and-remaining-work/expand2-record-hygiene-and-safety` expand/2: Record hygiene and safety

Status: Open; partial source evidence above is not closure

Concrete exit evidence: Preserve definition/call/generated origins, explicit capture and unsafe declarations; reject undeclared authority capture or missing unsafe metadata.

<!-- compile-time-source-origins:start -->

Apply the [complete compile-time evaluation source criteria](plan.md#complete-compile-time-evaluation-source-criteria) within this existing task. Status remains Open; finite reviewed source is not current whole-source or implementation acceptance.

Complete L12 generated-origin criterion (Open): Preserve immutable source syntax data, lexical context, metadata, spans, expansion phase and complete generated-origin chains across macro, plugin, schema-loader, derivation and generator output. Record actual generator identity/version, input syntax/span, phase, package, build effects and output digest, and retain original versus generated locations through normalization or memoization. Diagnose hygiene/capture and public-name collision except an explicitly declared replacement rule; later artifacts may inspect earlier ones without in-place mutation. Actual origins and matched input/output products are required, not a generated or passed label. Syntax/Resolve retain their representation and binding checks; this task owns expansion origin/hygiene preservation.
<!-- compile-time-source-origins:end -->


- [ ] `expand/current-evidence-and-remaining-work/expand3-authorize-compile-time-effects` expand/3: Authorize compile-time effects

Status: Open; partial source evidence above is not closure

Concrete exit evidence: Require declared grants, redacted logs and replay inputs for effectful expansion; reject ambient host effects and unreplayable hermetic expansion.

<!-- source-effects-build:start -->

Apply the [complete source effect and handler criteria](../effects-workflow/plan.md#complete-source-effect-and-handler-criteria) within this existing task. Status remains Open; frozen finite source review does not accept whole L6 or current implementation.

Decision criterion interface/language/effects/authorize-and-record-build-effect: Authorize build/compile-time effects against hermetic package/provider/deployment policy and record each granted effect with input/output/replay identity. Reject: Reject undeclared filesystem/environment/network/shell/model/tool authority, runtime grants reused in meta context and missing required replay records. Required outputs: Build-effect grant/deny log and hermetic input/replay artifact with positive/negative phase-authority fixtures.

L6 actual build-effect boundary criterion (Open): Enforce and record the requested effects of reader extensions, macros, compiler plugins, code generators, dependency/package resolution and build scripts under their actual phase and hermetic policy. Apply L6's hermetic denials for environment reads, undeclared files, network, shell/process, model/tool calls and non-lockfile fetching; a grant alone is not a hermetic exception or evidence of a performed effect. L12's separately stated declared-key/value-policy, replayable-provider/pinned-result, recorded-clock/seed and target-manifest alternatives retain their exact premises. Record the L6 environment-read denial versus L12 declared-key exception for existing Build/Expand source-policy resolution before claiming that live hermetic behavior; do not silently choose a broader allowance. Preserve macro build behavior separately from emitted runtime or compile-time behavior, including an effectful macro producing pure code and pure generation producing effectful code. Emit actual authorized/denied phase/input/output/provider records and replay/hermetic identities and reject ambient or hidden work. Auth supplies grants, Effects supplies real inferred summaries, Build packages the log; this task owns compile-time boundary enforcement rather than another grant issuer or package executor. No held compiler/plugin/probe/provider action is performed by this source proposal.
<!-- source-effects-build:end -->


<!-- authority-source-expand:start -->

Apply the [complete provider and capability source criteria](../auth-workflow/plan.md#complete-provider-and-capability-source-criteria) within this existing task. Status remains Open; frozen finite reviews do not accept this whole-source/current implementation proposal.

Complete compile-time provider gate criterion (Open): Enforce actual selected provider contracts, build-phase grants, profile/hermetic policy and scoped action gates for reader extensions, macro program P, plugins and generated-code services before their relevant action. Runtime grants cannot authorize build work or vice versa; generated caller Q is checked afterward and cannot supply P's prior admission. Require compile-time providers to support replay or explicitly identify non-replayability; release/conformance/safety-critical builds reject non-replayable use unless the applicable replay-policy exception is recorded. That exception does not override grants, isolation or the exact L6/L12 hermetic premises and recorded environment-wording issue. Preserve actual input/output/provider/phase/replay and redaction/refusal records; no held plugin/probe/provider action runs here.
<!-- authority-source-expand:end -->


<!-- compile-time-source-authorize:start -->

Apply the [complete compile-time evaluation source criteria](plan.md#complete-compile-time-evaluation-source-criteria) within this existing task. Status remains Open; finite reviewed source is not current whole-source or implementation acceptance.

Complete L12 compile-time action criterion (Open): Authorize the actual compile-time program P before its action using ordinary semantic checks, declared effects, actual scope/principal/lifetime/profile grants, selected provider contracts and installed operation gates. Cover all thirteen source build/compiler effects, including read/write IR and package index as well as file/artifact/env/network/exec/time/random/model/tool/target-probe. Each action retains source-span or manifest request, package/namespace/provider identity, actual grant and ordered input/output/tool/result-digest/replay evidence. Source/workspace/invocation/signed-lock/local-profile/provider configuration are grant inputs, never ambient authority or permission to exceed effective policy. Build effects and generated Q runtime effects remain separate. Apply exact L12 hermetic premises conjunctively with L6's recorded source-policy issue; a grant or replay exception alone waives neither. Reject missing authority, pure effects, phase capture, hidden inputs and unreplayed nondeterminism before the actual sink; preserve all held actions.
<!-- compile-time-source-authorize:end -->


<!-- compile-time-source-record:start -->

Apply the [complete compile-time evaluation source criteria](plan.md#complete-compile-time-evaluation-source-criteria) within this existing task. Status remains Open; finite reviewed source is not current whole-source or implementation acceptance.

Complete L12 evaluation-record producer criterion (Open): Produce the real ordered per-namespace trace, computed constant-value table, generated-origin chains, requested/granted/performed/refused build-effect events and authentic replay/cache decision products from actual compile-time behavior. Bind namespace/package/profile/target/compiler/facets, file/output digests, environment keys with redaction policy, command/tool/model/network result identities, nondeterminism records and actual diagnostics. A digest of an operator/type summary, replay-policy presence or assigned validation/reuse flag cannot stand in for evaluated values, actions, replay or current-policy validation. Build owns graph persistence/archival and cache construction; this task supplies its authentic event/value inputs without a second cache or package authority. Preserve positive and refusal records without publishing secrets.
<!-- compile-time-source-record:end -->


- [ ] `expand/current-evidence-and-remaining-work/expand4-validate-reuse-and-termination` expand/4: Validate reuse and termination

Status: Open; partial source evidence above is not closure

Concrete exit evidence: Bind cache reuse to every semantic input and replay record; diagnose actual expansion cycles/phase errors without node, size or work quotas.

<!-- compile-time-source-reuse:start -->

Apply the [complete compile-time evaluation source criteria](plan.md#complete-compile-time-evaluation-source-criteria) within this existing task. Status remains Open; finite reviewed source is not current whole-source or implementation acceptance.

Complete L12 compile-time reuse consumer criterion (Open): Request reuse from the existing Build cache owner using every actual semantic input: source hashes/spans, macro/extension identities, compile-time function hash or artifact version, lock entries, grants, declared file hashes, declared environment digests or redacted presence, target/profile manifests, compiler/facets and nondeterminism replay records. Consume the real key/entry/trace and policy validation result before using a cached value; matching keys are insufficient when policy no longer permits the trace. Stricter policy invalidates affected entries, while a looser policy never legalizes hidden effects in their creation. Demonstrate unchanged reuse and refused source/function/macro/grant/target/replay/policy drift against real evaluation; this task owns compile-time reuse consumption, Build alone constructs keys and invalidates/revalidates entries. Preserve actual cycle/phase failures without node/depth/size/work/fuel allowances.
<!-- compile-time-source-reuse:end -->


- [ ] `expand/current-evidence-and-remaining-work/expand5-integrate-generated-programs` expand/5: Integrate generated programs

Status: Open; partial source evidence above is not closure

Concrete exit evidence: Feed generated core forms through the same resolution/type/effect/profile/safety path as handwritten forms and retain expansion trace comparison evidence.

<!-- compile-time-source-generated:start -->

Apply the [complete compile-time evaluation source criteria](plan.md#complete-compile-time-evaluation-source-criteria) within this existing task. Status remains Open; finite reviewed source is not current whole-source or implementation acceptance.

Preserved finite predicate conjunctions:

Decision criterion interface/language/compiletime/check-generated-program: Revalidate generated forms through ordinary syntax/binding/types/effects/capabilities/memory/concurrency/profile/safety analysis with complete source/generated origins. Reject: Reject target-profile violations, hidden allocation/reflection/unsafe authority, invalid generated facts and missing original diagnostic provenance. Required outputs: Generated-program validation report, source-origin chain, accepted generation and stage-specific refusal artifacts.

Complete L12 generated-program criterion (Open): Feed actual macro/plugin/schema/derivation/code-generator output Q through the same syntax/hygiene/binding/public-name/type/runtime-effect/capability/memory/concurrency/unsafe/profile checks as handwritten source. Preserve declared replacement rules and diagnose both generating source and generated form with the complete origin chain. Validate actual emitted declarations, protocol implementations, serialization functions, layout metadata, tests and proof obligations where claimed; neither assigned passed fields nor generator success proves their legality. Program P's prior checks and build effects remain distinct from Q's later validation and runtime behavior. Exercise real authorized schema loading and generated protocol implementation with actual input/effect/origin products and stage-specific refusals, without adopting full schema/type contracts or held provider/plugin execution.
<!-- compile-time-source-generated:end -->


<!-- compile-time-source-conformance:start -->

Apply the [complete compile-time evaluation source criteria](plan.md#complete-compile-time-evaluation-source-criteria) within this existing task. Status remains Open; finite reviewed source is not current whole-source or implementation acceptance.

Complete L12 conformance criterion (Open): Join actual eligible-expression evaluation, stable target constants, phase/origin products, authorized/refused build actions, generated-program checks, current-policy cache results and repeated hermetic typed-core comparisons. Cover all nine explicit source profiles and all thirteen source build/compiler effects through actually supported selected cases, with held/unavailable cases visibly unassessed. For core, require pure evaluation and portable typed Core. Hosted reflection/library access and dynamic loading require actual grants/traces. Native specialization/ABI/layout/linker metadata requires declared targets and no leaked hosted assumptions. Kernel, firmware and hardware require hermetic builds and reject generated GC, reflection, ambient allocation, host threads, wall clocks or dynamic loading unless the profile explicitly models the facility. Distributed retains workflow schema/retry/external-binding/replay records without a separate workflow profile. AI generation/adaptation/test/evaluation retains model identity, prompt class, actual input/output digests, retention and applicable separately supplied human-review policy, with project cost accounting removed. Meta IR inspection/transformation preserves all normal profile/type/effect/safety/artifact checks. Exercise arithmetic/collections/matching/layout positives, runtime-only/host/ambient/phase/representation failures, hidden input/nondeterminism/secret refusals, generated protocol/schema cases and changed grant/target/source/replay/policy reuse. Preserve nine active L12 diagnostic meanings and their phase/profile/target/span/origin/effect/grant/remediation context. The historical tenth L12-FUEL diagnostic is superseded project accounting, not a renamed bound or replacement proof gate. Actual independent Release harnesses remain separate from owner examples and no complete current implementation acceptance follows from records or finite historical receipts.
<!-- compile-time-source-conformance:end -->


<!-- expand-implementation-tasks:end -->

## Historical migration record

The original scaffold below is frozen history at the transfer baseline, not
implementation work or a completion denominator. Its pending review/integration
text records what that scaffold stated, not a fresh claim about its merged PR.
The implementation authority above supersedes its navigation-only rule; no
historical checkbox is reclassified.

# Tasks: Expand workflow migration

Spec: [spec.md](spec.md) | Plan: [plan.md](plan.md) | Evidence: [evidence.md](evidence.md)
Owner: Frontend Spec Kit migration maintainer.
Input revision: `017b541d678c6acb93eb5e843b1eda5a66babc32`.

## Declared increment

- [x] T001 [US1] Prepare `specs/expand-workflow/spec.md` and `plan.md` with retained contract and canonical task/acceptance links.
- [x] T002 [US2] Prepare `specs/expand-workflow/tasks.md` and `evidence.md` with migration-only checks and explicit implementation nonclaims.
- [x] T003 [US1] Validate all four artifacts, local links/anchors, ASCII and exact owned diff; record positive and negative checks in `evidence.md`.
- [ ] T004 [US2] Independently review the immutable package candidate and record scope and findings in `evidence.md`.
- [ ] T005 [US2] Integrate the reviewed documentation increment and record the verified PR/merge receipt in `evidence.md`.

## Dependencies and outcomes

T001/T002 prepare the package; T003 validates it; T004 reviews that candidate;
T005 integrates only that reviewed scope. This five-task denominator is workflow
migration, not a percentage of Expand implementation or language completion.
Implementation task status remains solely in [the canonical table](../../docs/component-roadmaps/expand.md#current-evidence-and-remaining-work).
No existing open component task becomes complete because this package exists.
Partial work remains visible in a draft PR.

## New-name review

Only semantic `expand-workflow` documentation paths are added. No code names
or compatibility IDs change; [semantic naming guidance](../../.specify/semantic-names.md)
continues to govern later implementation.
