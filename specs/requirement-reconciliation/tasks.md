# Tasks: Requirement progress reconciliation

Spec: [spec.md](spec.md) | Plan: [plan.md](plan.md) | Evidence: [evidence.md](evidence.md).
Owner: canonical requirement reconciliation maintainer.
Input: `a1b991be499d50579a8b4366976c519ae8949224`.

## Declared increment

- [x] T001 Conserve all 240 original sources and explicit project extensions in the versioned manifest and read-only census. Evidence: independently reviewed exact-source conservation at `52bf88de94dbb8b84d91dcedc17b8e07ca81e631`; [source findings and review](evidence.md).
- [ ] T002 Reconcile actual normative clauses into nonoverlapping semantic requirement IDs with total dispositions and preserved lineage; expose unresolved scope.
- [ ] T003 Trace canonical task/evidence sources and retain historical dates/commits while qualifying current acceptance separately.
- [ ] T004 Validate source conservation, duplicate resistance, evidence qualification and rate withholding with meaningful positive/negative controls.
- [ ] T005 Obtain independent exact-candidate semantic/evidence review and integrate the validated canonical reconciliation.
- [ ] T006 Hand off independently accepted canonical identities for separately validated dashboard project patches; no direct store write here.

## Dependencies and outcomes

T001 supports T002/T003; T004 verifies the resulting scope/evidence projection;
T005 requires independent acceptance of the exact candidate. T006 follows
integrated canonical evidence. These tasks are the reconciliation increment,
not a replacement language implementation ledger or overall completion rate.

## New-name review

Use semantic requirement/tool names; preserve original contract/task IDs as
traceability. No source, schema or wire identifiers are renamed here.
