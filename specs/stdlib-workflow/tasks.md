# Standard Library implementation task authority

Spec: [spec.md](spec.md) | Plan: [plan.md](plan.md) | Evidence: [evidence.md](evidence.md).

## Implementation tasks

These five records are the sole editable task authority. IDs derive from the
existing component/section/title-slug convention; the original table had titles,
not literal individual IDs. No external dashboard object identity or new task
population is introduced. All five remain Open. The selected
`stdlib/compiler-support` outcome uses these same tasks and retains its finite
reachable-export scope; wider inventory and release acceptance remain separate.

<!-- stdlib-implementation-tasks:start -->
- [ ] `stdlib/remaining-tasks-and-evidence/reconcile-export-inventory-with-actual-callable-implementations` Reconcile export inventory with actual callable implementations

Status: Open

Evidence baseline: [Library source artifact](../../bootstrap/clojure/src/gravity/bootstrap_impl/components/foundation/00499-standard-library-source-artifact.clj)

Closing evidence: Per-export source/implementation/fixture map, with absent exports explicitly open; compiled module manifests agree with package metadata, documentation and conformance discovery on namespace/artifact identity, callable exports, profiles, effects, capabilities, allocation, errors, safety and stability. Reject missing or inconsistent metadata, undeclared effects, missing grants, host-dependent core exports and unaudited unsafe internals with STD1001-STD1008 as applicable. Apply the source-specific acceptance and dependency matrix in plan.md.

- [ ] `stdlib/remaining-tasks-and-evidence/complete-pure-core-collections-text-numeric-and-testing-apis` Complete pure core, collections, text, numeric and testing APIs

Status: Open

Evidence baseline: [Library architecture](../../docs/phase-16-standard-library/211-std1-standard-library-architecture.md)

Closing evidence: Compiled examples and type/arity/value rejection through real APIs; STD2-STD5 and STD14 fixtures preserve evaluation order, transfers, inert stored values, original persistent values, Unicode boundaries, explicit numeric modes and structured test evidence. Retain each operation's proven-presence/nonempty/in-range premise without invented fallback, coercion, static facts or provider guarantees. Reject invalid UTF-8, implicit locale/lossy conversion, unchecked partial operations, unproved math transformations, free-text-only diagnostic matching and ungranted live-service tests. Compile documentation under every claimed profile; require the exact outputs and negative cases in plan.md.

- [ ] `stdlib/remaining-tasks-and-evidence/complete-resourceconcurrencyionetwork-apis` Complete resource/concurrency/IO/network APIs

Status: Open

Evidence baseline: [Module inventory](../../docs/phase-16-standard-library/README.md)

Closing evidence: Provider-mediated executions, missing-grant rejection and safe resource teardown; STD6-STD9 fixtures exercise borrow/region escape, exactly-once close/transfer, child cancellation and lifetime, atomic memory order, forbidden blocking, root/symlink traversal, explicit text encoding, atomic-write durability failures, network timeout/retry/redirect/body/cancellation policy, TLS validation and secret redaction. Replay must record or isolate actual external effects and reject unrecorded nondeterminism; host delegation must preserve Gravity diagnostics and provider provenance. Runtime, Legality and Auth dependencies remain explicit in plan.md.

- [ ] `stdlib/remaining-tasks-and-evidence/complete-schemaqueryworkflowaimetacryptoui-adapters` Complete schema/query/workflow/AI/meta/crypto/UI adapters

Status: Open

Evidence baseline: [Module inventory](../../docs/phase-16-standard-library/README.md)

Closing evidence: APIs consuming corresponding domain/runtime outcomes, with boundary failures exercised; STD10-STD13, STD15, STD18 and STD19 fixtures validate schemas before trusted decoding, canonical bytes, migration compatibility, parameterized queries, transaction commit/rollback/retry/isolation, durable workflow replay and in-flight migration, AI schema/taint/tool/review gates, generated-code rechecking, syntax context and pass invalidations, crypto lifecycle/hybrid binding/redaction, and typed UI events/render trust/accessibility/routes. Require actual provider execution and artifact consumers, not catalogue names or manifest presence. Reject each source-specific boundary failure in plan.md; unresolved provider, binding and equivalence decisions remain unassessed.

- [ ] `stdlib/remaining-tasks-and-evidence/integrate-platform-modules-and-close-release-manifests` Integrate platform modules and close release manifests

Status: Open

Evidence baseline: [Platform outcome](../../docs/component-roadmaps/platform.md#release-outcome)

Closing evidence: Actual supported platform API fixtures, unsafe audits and compatibility/stability report; STD16-STD17 fixtures distinguish target/build/runtime-host facts, all clock/random kinds, process command/args/environment/stdio/timeout/working-directory policy, MMIO widths/alignment/volatile ordering, interrupt effects/reentrancy, DMA ownership/coherence/completion and clock-domain synchronization. STD20 release evidence compares source, behavior, effects, capabilities, diagnostics, artifact schemas, profiles and fixtures; exercises experimental opt-in/internal-import denial, deprecation/replacement/removal schedules and governed breaking changes; and binds signed provenance to the actual release. Platform target and product-release outcomes retain their separate scope.
<!-- stdlib-implementation-tasks:end -->

## Historical workflow migration record

The original file follows unchanged as historical workflow evidence. Its
checkboxes and earlier task-source statements do not duplicate the current
implementation authority or imply library implementation completion.

# Tasks: Standard Library workflow migration

Spec: [spec.md](spec.md) | Plan: [plan.md](plan.md) | Evidence: [evidence.md](evidence.md).
Migration owner role: documentation migration maintainer.
Input revision: `017b541d678c6acb93eb5e843b1eda5a66babc32`.

## Declared increment

- [x] T001 [US1] Prepare the four owned artifacts with retained-contract links and exact canonical task anchors.
- [x] T002 [US1] Validate local targets/anchors, semantic names, ASCII, existing document/roadmap checks and negative controls; record results in evidence.md.
- [ ] T003 [US2] Independent review accepts the exact artifact tree, mapping and nonclaims; record disposition in evidence.md.
- [ ] T004 [US2] Integrate the reviewed change and record its merge receipt without claiming implementation completion.

## Dependencies and outcomes

T002 uses prepared artifacts; T003 uses validated exact content; T004 follows accepted review. These are documentation-migration tasks only. Implementation tasks remain at the links in plan.md, not copied into this ledger. Current implementation acceptance is unassessed by this package. Existing accepted finite increments retain their original scope and evidence.

## New-name review

All new paths use `stdlib-workflow` and ordinary artifact names. No planning-ID implementation name or compatibility identifier is changed.
