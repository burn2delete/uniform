# Diagnostics

Working name: **Diagnostics** (proposed). Semantic key: `diag`.
Owner role: diagnostic infrastructure maintainer; current owner unassigned by
this proposal. Baseline: `c21068a6d39259b7110845bbbcefaf9bf6901232`.

## Purpose and scope

Carry actionable source/manifest diagnostics consistently across compiler,
runtime, CLI and editor consumers. Own record identity, ordering, redaction,
rendering contracts and source-origin presentation. Stage owners retain rule
semantics. Existing diagnostic and wire identifiers are compatibility data,
not a reason to put planning IDs into new implementation names.

## Release outcome

Every claimed stage emits stable structured diagnostics that preserve source
and generated origins, profile/target and remediation; human and machine
renderers agree, and sensitive values are redacted. Outcome open.

## Interfaces and dependencies

Inputs: rule ID, severity, human message, structured facts, spans/origins and
artifact edges. Outputs: canonical records, ordered diagnostic streams,
redacted text/JSON and explainable links. Source spans/syntax origins are shared
interfaces. Each stage integrates independently; no dependency on a completed
Build or Release system is introduced.

## Remaining tasks and evidence

Implementation task authority: [Spec Kit ledger](../../specs/diag-workflow/tasks.md#implementation-tasks).
The following table is generated; edit the ledger and run
`clojure -M tools/diag_task_projection.clj --write`.

<!-- diag-task-projection:start -->
| Task (all open) | Evidence baseline | Closing evidence |
| --- | --- | --- |
| Reconcile stage carriers with canonical record schema | [Base carrier](../../bootstrap/clojure/src/gravity/diagnostics.clj), [records](../../bootstrap/clojure/src/gravity/c15_diagnostics/records.clj) | Record/schema parity fixtures from each producer, including malformed-record rejection |
| Preserve primary/related/generated origins and stable order | [Projection](../../bootstrap/clojure/src/gravity/c15_diagnostics/projection.clj) | Macro and optimized-source diagnostics with original and generated locations |
| Close text/JSON/redaction and explain contracts | [CLI presentation](../../bootstrap/clojure/src/gravity/cli/diagnostic_presentation.clj) | Golden text/JSON equivalence, secret redaction and known-ID explanation |
| Integrate runtime/build/editor consumers without rule drift | [Tooling contracts](../phase-13-tooling-and-developer-experience/README.md) | Same underlying diagnostic identity across public consumers and failure paths Foundation requirement gravity/runtime-check-diagnostic-stability: Specify and exercise runtime checks with deterministic owning code, source/manifest span, profile/target, failed predicate and remediation; compare their structured results across optimization and supported runtime providers. Refusals: Reject missing or changed check attribution, raw host failure leakage and a check erased without surviving evidence; fixtures must trigger the actual check, not match only free text. Evidence: retain the exact source/compiler/profile/target and applicable provider/checker identities, actual positive products and negative diagnostics for this criterion. Consume these actual dependency artifacts (not whole-component completion): runtime/remaining-tasks-and-evidence/implement-explicit-family-selection-and-no-runtime-rejection - Selected runtime/provider service contracts and actual link/load/service-use evidence; legality/remaining-tasks-and-evidence/complete-safe-code-classification-and-taintunsafe-boundaries - Dangerous-operation outcomes, actual checks, proof references and audited wrapper boundaries. |
<!-- diag-task-projection:end -->

The base carrier owns only construction/throwing of bootstrap ExceptionInfo;
it explicitly excludes rule catalogs, schema, identity, ordering and rendering.
It is not a completed diagnostics stack. Architecture decision owner:
diagnostic maintainer. Recommendation: one structured record schema with
separate renderers, retaining legacy IDs through explicit compatibility maps.

## Acceptance and integration

Yes/no: are identities stable? Are origin chains preserved? Do invalid records
fail validation? Are text and JSON semantically equal? Are secrets absent?
All must be yes. Integration milestones: Reader/Syntax errors, semantic errors,
then Runtime/Build/CLI parity. This page does not own source rule completion.
