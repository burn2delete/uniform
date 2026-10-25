# Tasks: paired source/Core reference pilot

Spec: [spec.md](spec.md) | Plan: [plan.md](plan.md) | Evidence: [evidence.md](evidence.md)
Pilot owner: Spec Kit adoption delegate. Input: PR356 `700e2929`.

## Declared adoption increment

- [x] T001 [US1] Inspect the immutable PR356 test path and map R1-R5 in `specs/001-paired-source-core/spec.md`; evidence.md records source observations.
- [x] T002 [US1] Create `specs/001-paired-source-core/plan.md` using the generated Uniform plan skill/template; retain exact owner boundaries.
- [x] T003 [US1] Create `specs/001-paired-source-core/tasks.md` using the generated Uniform tasks skill/template; keep reported implementation outcomes separate.
- [x] T004 [US1] Record PR356's attributed results and unrun checks in `specs/001-paired-source-core/evidence.md` without copying implementation.
- [x] T005 [US1] Verify this artifact flow and configuration through `.specify/validation/verify.clj` and record `.specify/validation/results.edn`.

## Existing implementation disposition (owned elsewhere)

- [ ] T006 [US1] PR356 owner/reviewer reconciles raw validation evidence for `bootstrap/clojure/test/gravity/self_hosting/sh07_current_public_closure_integration_test.clj`; prior results are reported, not independently validated by this pilot.
- [ ] T007 [US1] Independently reconcile PR356 final-head review/integration evidence (merge observed; historical log not authenticated here) on https://github.com/burn2delete/uniform/pull/356 .
- [ ] T008 [US1] Deus reviews adoption PR371 and records integration decision for `.specify/README.md` and this pilot; process bridge owns any shared navigation link.

## Dependencies and outcomes

T001 -> T002 -> T003 -> T004 -> T005 describes the adoption artifact flow.
T006/T007 belong to the existing implementation owners; this task does not wake
or assign reviewers, rerun compiler tests, or take over that PR. T008 is parent
review/integration, not a new routine permission gate. Checked pilot artifacts
do not establish underlying feature or SH07 completion. PR356 merge is an
external observed fact; adoption PR371 remains draft.
