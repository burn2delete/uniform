# Types component roadmap

Working name: **Types** (proposed default, not an approved codename).
Semantic key: `types`. Status: proposed roadmap; release acceptance open.
Source baseline: `c21068a6d39259b7110845bbbcefaf9bf6901232` (2026-10-25).
Owner role: compiler types maintainer.
Current owner: not established by this source inspection; Deus assigns or confirms
the existing owner before implementation. This page does not transfer ownership.

## Purpose

Determine legal value/function types and explicit dynamic boundaries before MIR or backend execution.

## Release outcome

Typed core with constraint solutions and checked conversions for the governing type-system surface, preserving profile, effect, ownership and schema facts and rejecting unsatisfied constraints.

This is a finite component release, not perpetual improvement and not a claim
that the implementation already conforms. The [governing compiler contract](../phase-06-compiler-architecture/086-c7-type-checker-design.md)
defines the required semantic surface; this page schedules it without replacing
the specification. Future language revisions require a new outcome revision.

## Scope and non-goals

Own type environments, constraints, inference/checking, generic/protocol constraints and dynamic-boundary checks. Effects, ownership proof, safety classification and backend layout realization are distinct consumers. No project-cost budgets, function counts, token/form/depth quotas or
task-count allowances define completion. Existing diagnostic/schema/wire IDs
remain compatible unless an explicit versioned migration is approved; new
implementation names use semantic subsystem names, not planning IDs.

## Inputs, outputs and interfaces

- Inputs: Core nodes, resolved bindings, declared types, schemas, profile and target representation requirements.
- Outputs: Typed core, type environment, constraint ledger, function/type tables, conversion/check obligations and diagnostics.
- Hard prerequisites: Core and Resolve must supply actual node/binding identities. Effect and ownership annotations have versioned interfaces; separate analysis outcomes are not invented by type inference.
- Parallel interfaces: Effects and safety/ownership work can use typed-core fixtures; layout requirements can be agreed with Lower without requiring a finished backend.

## Current evidence and remaining work

Inspected source: [existing implementation](../../bootstrap/clojure/src/gravity/c7_type_checker/inference.clj).
Inspected local inference assigns Dynamic to many expressions and function parameters/returns; the ledger marks assignments solved. Those implementation records are not evidence of full constraint solving. Existing paths containing planning identifiers are historical source
references, not proposed implementation naming. No compiler test was run for
this roadmap and no source inspection or pre-existing report is credited as
release acceptance.

<!-- types-task-projection:start -->
| Task | Status | Concrete exit evidence |
| --- | --- | --- |
| types/1: Solve core constraints | Open; partial source evidence above is not closure | Implement literal, binding, function/application, branch, collection and error-result constraints with actionable mismatch diagnostics. Decision criterion interface/language/types/derive-private-keyword-map-domain: Derive the whole Keyword key domain of an immutable bare Map parameter only from a complete finite census of private normal entries; preserve the existing homogeneous Keyword List result for keys. Reject: Reject partial/open/public/mutable or otherwise invalid census assumptions, non-Keyword entries and widening inferred keys from mere observed subsets. Required outputs: Private-entry census and key-domain inference evidence, homogeneous List type result and incomplete/invalid-census refusal fixtures. |
| types/2: Check declared abstractions | Open; partial source evidence above is not closure | Validate generics, protocols/dispatch, records and schema-derived types against their declarations and resolved identities. Criterion `types/schema-static-projection/decision-interface` (Open): Input: The exact S1 expanded typed/versioned schema manifest and its source identity/refinement/constraint facts. Outputs: Static type projection artifact with schema identity; Accepted and rejected source-to-type parity fixtures. Accept: Generate static records/unions/optional/reference/recursive/refined types from the expanded schema, preserve resolved names, schema identity and constraints, and compare declared and generated type legality against the same manifest. Record target representation limitations explicitly. Refuse: Type projection from a mismatched/stale schema identity; Dropped nullability/refinement/variant constraint or unrepresentable schema family without explicit policy/rejection; Schema normalization or validator generation substituted for the static type artifact. Dependencies consume actual artifacts: Schema task supplies the expanded manifest; Types resolved declarations/constraints supply legality; downstream validators and bindings consume the projected type facts. Diagnostics: S1-PROJECTION and applicable Types mismatch diagnostics retain schema/version/span/target and remediation. These are required future products/results; no execution or implementation acceptance is claimed. Decision criterion interface/language/errors/represent-typed-outcome: Represent Option absence, Result recoverable error, denial, panic, replay and policy failure as their declared distinct typed contracts, retaining applicable origins and audit policy. Reject: Reject conflating capability denial with nil/absence, replay failure with a local exception or AI failure with an unrecorded free-text error. Required outputs: Typed outcome/interface schema and exhaustiveness cases, source/audit records and cross-kind rejection fixtures. Decision criterion interface/language/protocol/select-checked-dispatch: Resolve a declared profile/target-legal implementation set and dispatch mode with typed method/effect contracts; retain host-boundary metadata when delegated. Reject: Reject missing/ambiguous dispatch, ungranted reflection/open-world assumptions and method/type/effect/profile mismatches. Required outputs: Protocol/implementation/dispatch and target metadata, legal selection and missing/ambiguous/host-boundary refusal fixtures. |
| types/3: Make dynamic boundaries explicit | Open; partial source evidence above is not closure | Require checked casts and representability/numeric conversions where specified; reject implicit unsafe narrowing and unsupported profile-dependent dynamic behavior. Decision criterion interface/language/interop/convert-declared-foreign-value: Perform the explicitly directional Gravity/foreign type conversion under declared ownership/nullability/encoding/layout semantics; loss is explicit and failure returns Result or the declared boundary error. Reject: Reject hidden lossy conversion, invalid null-to-non-null mapping and foreign values admitted without required type/lifetime facts; declared Option absence remains legal. Required outputs: Directional type map and conversion/round-trip results, declared loss/failure records and invalid-value/ownership refusals. |
| types/4: Preserve and invalidate facts | Open; partial source evidence above is not closure | Serialize type solutions and layout requirements with source/profile/ownership/effect references; invalidate on changed nodes, bindings or contracts. |
| types/5: Integrate typed core | Open; partial source evidence above is not closure | Feed Effects and MIR prerequisites from actual solutions, with accepted inferred/declared cases and rejected unsatisfied, stale and illegal-conversion cases. |
<!-- types-task-projection:end -->

The table above is generated from the sole editable
[implementation task ledger](../../specs/types-workflow/tasks.md#implementation-tasks).
Edit that ledger, then run `clojure -M tools/types_task_projection.clj --write`;
verify with `--check`. This transfer changes no task status or release criteria.
Close a task only with the exact source revision, actual invocation/result and relevant
accepted/rejected artifacts, reviewed by the responsible owner. Do not infer
completion from assertion counts, generated status fields or filenames.

## Acceptance criteria (yes/no)

- [ ] Every emitted solution is derived from constraints, not a completeness label or unconditional Dynamic fallback.
- [ ] Required dynamic checks and numeric/layout legality are explicit and illegal conversions reject.
- [ ] Downstream facts and diagnostics retain node, binding, profile and source identities.
- [ ] Each task has source-pinned execution evidence for the claimed surface,
  with residual host boundaries explicit and independent acceptance recorded.
- [ ] The owner confirms the release outcome without claiming whole-language,
  whole-front-end migration or seed retirement from this component alone.

## Integration milestones

1. Agree the named input/output schema and positive/negative contract fixtures
   with immediate producers and consumers; implementation may proceed in parallel.
2. Typed-core handoff joins Effects and ownership/safety checks before MIR admission.
3. Record accepted component outcome here and let the [overall registry](README.md)
   reference it; the overall roadmap must not copy this task list.

## Traceability

[governing compiler contract](../phase-06-compiler-architecture/086-c7-type-checker-design.md) is normative. The foundation pipeline and milestone ordering remain
owned by [D1](../phase-00-foundation-and-thesis/002-d1-system-architecture-overview.md)
and [D2](../phase-00-foundation-and-thesis/003-d2-implementation-roadmap-and-milestones.md).
Existing self-hosting milestones are evidence/traceability references, not this
project's identity and not authority to run restricted migration actions.
