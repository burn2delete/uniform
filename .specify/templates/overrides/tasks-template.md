# Tasks: [change]

Spec: [spec.md] | Plan: [plan.md] | Evidence: [evidence.md]
Owner: [exact owner] | Input revision: [commit/tree]

## Existing task authority

First locate the selected outcome's canonical implementation ledger. Preserve
its stable task IDs, marked authority region, owners and historical evidence.
Carry the current status forward unless its required evidence supports a change.
Reconcile acceptance criteria with governing contracts while retaining prior
criteria and review subjects as history. Refine the existing record and dependencies;
do not replace it with T001 rows or copy it into a second editable checklist.
Keep historical workflow-migration tasks distinct from implementation authority.
If a required outcome has no task, add only the needed task at its existing
owning authority using that ledger's stable-ID convention and exact ownership.

## Declared increment

For an existing implementation ledger, retain its current row format and IDs.
Record actual behavior/artifact outcomes and the governing acceptance, refusal,
diagnostic and evidence obligations under those records. Declarations, manifests
or assigned statuses cannot substitute for required execution or consumer use.
Use only dependencies needed for starting, composition, acceptance or release;
do not make unrelated component completion a start gate.

The following T001 sample is for a new artifact-only increment with no existing
implementation authority. Story labels and [P] are optional where applicable.
Do not add generic setup phases or tests unrelated to the requested change.

- [ ] T001 [US1] [concrete owned artifact deliverable with path and evidence]

## Dependencies and outcomes

Only mark a task complete when its specified evidence exists. Attribute inherited
results. Keep review and integration separate from implementation; leave pending
work unchecked. Partial work belongs in a draft PR. No all-feature completion,
project accounting quota or repeated authorization ceremony is introduced here.

## New-name review

For each new implementation name, check subsystem/behavior wording against
`.specify/semantic-names.md`. Example task: `T002 [US1] Preserve source origins
in core/source_map.clj and cover them in core/source_map_test.clj; planning-ref:
SH07/C6`. Planning IDs stay in task metadata, never in those implementation
names. Review added filenames and new declarations in the change diff. Record
legacy migration debt and compatibility work separately; do not rename peers'
active files or add a repository-wide naming gate.
