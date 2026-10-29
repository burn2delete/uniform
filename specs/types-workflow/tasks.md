# Tasks: Types implementation workflow

Spec: [spec.md](spec.md) | Plan: [plan.md](plan.md) | Evidence: [evidence.md](evidence.md)
Owner role: compiler types maintainer; existing implementation reservations are unchanged.
Authority transfer baseline: `88f621ee7633be6965f34d33a43a361cbdd26157`.

## Implementation tasks

This is the sole editable Types implementation task ledger. The roadmap's
[current evidence and remaining work](../../docs/component-roadmaps/types.md#current-evidence-and-remaining-work)
is generated from these records. IDs preserve the established phase/title slug
convention; the original `types/1` through `types/5` labels remain display
text. Order is display order, not dependency evidence. An unchecked box means
not accepted complete. Check a box only with explicit `Accepted;` status and an
immutable accepted-outcome receipt independently reviewed by the responsible
owner. Structural validation cannot grant acceptance. No task closes here.

<!-- types-implementation-tasks:start -->

- [ ] `types/current-evidence-and-remaining-work/types1-solve-core-constraints` types/1: Solve core constraints

Current evidence/status: Open; partial source evidence above is not closure

Done when (yes/no): Implement literal, binding, function/application, branch, collection and error-result constraints with actionable mismatch diagnostics. Decision criterion interface/language/types/derive-private-keyword-map-domain: Derive the whole Keyword key domain of an immutable bare Map parameter only from a complete finite census of private normal entries; preserve the existing homogeneous Keyword List result for keys. Reject: Reject partial/open/public/mutable or otherwise invalid census assumptions, non-Keyword entries and widening inferred keys from mere observed subsets. Required outputs: Private-entry census and key-domain inference evidence, homogeneous List type result and incomplete/invalid-census refusal fixtures.

- [ ] `types/current-evidence-and-remaining-work/types2-check-declared-abstractions` types/2: Check declared abstractions

Current evidence/status: Open; partial source evidence above is not closure

Done when (yes/no): Validate generics, protocols/dispatch, records and schema-derived types against their declarations and resolved identities. Criterion `types/schema-static-projection/decision-interface` (Open): Input: The exact S1 expanded typed/versioned schema manifest and its source identity/refinement/constraint facts. Outputs: Static type projection artifact with schema identity; Accepted and rejected source-to-type parity fixtures. Accept: Generate static records/unions/optional/reference/recursive/refined types from the expanded schema, preserve resolved names, schema identity and constraints, and compare declared and generated type legality against the same manifest. Record target representation limitations explicitly. Refuse: Type projection from a mismatched/stale schema identity; Dropped nullability/refinement/variant constraint or unrepresentable schema family without explicit policy/rejection; Schema normalization or validator generation substituted for the static type artifact. Dependencies consume actual artifacts: Schema task supplies the expanded manifest; Types resolved declarations/constraints supply legality; downstream validators and bindings consume the projected type facts. Diagnostics: S1-PROJECTION and applicable Types mismatch diagnostics retain schema/version/span/target and remediation. These are required future products/results; no execution or implementation acceptance is claimed. Decision criterion interface/language/errors/represent-typed-outcome: Represent Option absence, Result recoverable error, denial, panic, replay and policy failure as their declared distinct typed contracts, retaining applicable origins and audit policy. Reject: Reject conflating capability denial with nil/absence, replay failure with a local exception or AI failure with an unrecorded free-text error. Required outputs: Typed outcome/interface schema and exhaustiveness cases, source/audit records and cross-kind rejection fixtures. Decision criterion interface/language/protocol/select-checked-dispatch: Resolve a declared profile/target-legal implementation set and dispatch mode with typed method/effect contracts; retain host-boundary metadata when delegated. Reject: Reject missing/ambiguous dispatch, ungranted reflection/open-world assumptions and method/type/effect/profile mismatches. Required outputs: Protocol/implementation/dispatch and target metadata, legal selection and missing/ambiguous/host-boundary refusal fixtures.

- [ ] `types/current-evidence-and-remaining-work/types3-make-dynamic-boundaries-explicit` types/3: Make dynamic boundaries explicit

Current evidence/status: Open; partial source evidence above is not closure

Done when (yes/no): Require checked casts and representability/numeric conversions where specified; reject implicit unsafe narrowing and unsupported profile-dependent dynamic behavior. Decision criterion interface/language/interop/convert-declared-foreign-value: Perform the explicitly directional Gravity/foreign type conversion under declared ownership/nullability/encoding/layout semantics; loss is explicit and failure returns Result or the declared boundary error. Reject: Reject hidden lossy conversion, invalid null-to-non-null mapping and foreign values admitted without required type/lifetime facts; declared Option absence remains legal. Required outputs: Directional type map and conversion/round-trip results, declared loss/failure records and invalid-value/ownership refusals.

- [ ] `types/current-evidence-and-remaining-work/types4-preserve-and-invalidate-facts` types/4: Preserve and invalidate facts

Current evidence/status: Open; partial source evidence above is not closure

Done when (yes/no): Serialize type solutions and layout requirements with source/profile/ownership/effect references; invalidate on changed nodes, bindings or contracts.

- [ ] `types/current-evidence-and-remaining-work/types5-integrate-typed-core` types/5: Integrate typed core

Current evidence/status: Open; partial source evidence above is not closure

Done when (yes/no): Feed Effects and MIR prerequisites from actual solutions, with accepted inferred/declared cases and rejected unsatisfied, stale and illegal-conversion cases.

<!-- types-implementation-tasks:end -->

## Historical migration record

The original scaffold below is frozen history at the transfer baseline, not
implementation work or a completion denominator. Its pending review/integration
text records what that scaffold stated, not a fresh claim about its merged PR.
The implementation authority above supersedes its navigation-only rule; no
historical checkbox is reclassified.

# Tasks: Types workflow migration

Spec: [spec.md](spec.md) | Plan: [plan.md](plan.md) | Evidence: [evidence.md](evidence.md)
Migration owner: checked-IR documentation workstream.
Input revision: `017b541d678c6acb93eb5e843b1eda5a66babc32`.

## Declared increment

- [x] T001 [US1] Prepare spec.md and plan.md with retained contract links and the canonical component task/acceptance anchors.
- [x] T002 [US2] Prepare tasks.md and evidence.md with migration-only checks and explicit implementation nonclaims.
- [x] T003 [US1] Validate the four owned artifacts, relative links/anchors, negative controls and existing document/roadmap checks; record results in evidence.md.
- [ ] T004 [US2] Independently review the exact candidate and record disposition in evidence.md or the linked PR.
- [ ] T005 [US2] Verify integration to main and record the merge receipt separately from local validation.

## Dependencies and outcomes

T003 follows artifact preparation; T004 follows successful validation; T005
follows independent acceptance. These five migration tasks are not a copy of
the [component implementation tasks](../../docs/component-roadmaps/types.md#current-evidence-and-remaining-work).
No component implementation status is changed here. Existing compiler evidence
retains its original scope and source identity.

## New-name review

The new directory is semantic; contract planning IDs remain cross-references.
No legacy implementation renaming is required for this documentation slice.
