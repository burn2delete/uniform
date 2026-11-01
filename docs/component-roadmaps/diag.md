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

Task criteria, statuses, history and closing evidence live in the [sole editable task ledger](../../specs/diag-workflow/tasks.md#implementation-tasks).

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
