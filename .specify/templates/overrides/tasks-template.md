# Tasks: [change]

Spec: [spec.md] | Plan: [plan.md] | Evidence: [evidence.md]
Owner: [exact owner] | Input revision: [commit/tree]

## Declared increment

Use dependency-ordered checklist rows with task IDs, optional [P], story labels
where applicable, exact paths and evidence expectations. Do not add generic
setup phases or tests unrelated to the requested change.

- [ ] T001 [US1] [concrete owned deliverable with path and evidence]

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
