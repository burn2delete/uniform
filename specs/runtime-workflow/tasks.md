# Runtime implementation task authority

Spec: [spec.md](spec.md) | Plan: [plan.md](plan.md) | Evidence: [evidence.md](evidence.md).

## Implementation tasks

These five records are the sole editable task authority. IDs derive from the
existing component/section/title-slug convention; the original table had titles,
not literal individual task IDs. No external dashboard object identity or new
task population is introduced.
All five remain Open. The selected `runtime/compiler-services` outcome scopes
these same tasks through its actual receipt, not a second checklist; wider
runtime-family and release acceptance remain separate and open.

<!-- runtime-implementation-tasks:start -->
- [ ] `runtime/remaining-tasks-and-evidence/implement-explicit-family-selection-and-no-runtime-rejection` Implement explicit family selection and no-runtime rejection

Status: Open

Evidence baseline: [Selection validator](../../bootstrap/clojure/src/gravity/bootstrap_impl/components/runtimes/00284-runtime-selection-validate.clj)

Closing evidence: Actual service link/load inspection for every claimed profile/target and forbidden-service rejection

- [ ] `runtime/remaining-tasks-and-evidence/finish-managednative-execution-startup-panic-and-cleanup` Finish managed/native execution, startup, panic and cleanup

Status: Open

Evidence baseline: [Development evaluator](../../bootstrap/gravity/src/gravity/runtime.gravity) and [native manifest](../../bootstrap/clojure/src/gravity/bootstrap_impl/components/foundation/00287-minimal-native-runtime-manifest.clj)

Closing evidence: Executed startup/failure/resource fixtures and manifest-to-service agreement

- [ ] `runtime/remaining-tasks-and-evidence/implement-memory-concurrency-and-typed-ffi-boundaries` Implement memory, concurrency and typed FFI boundaries

Status: Open

Evidence baseline: [Runtime contracts](../../docs/phase-08-runtime-architecture/README.md)

Closing evidence: Allocation, lifetime, transfer, race and foreign-failure conformance with explicit providers

- [ ] `runtime/remaining-tasks-and-evidence/connect-distributedai-replay-and-interactive-execution` Connect distributed/AI replay and interactive execution

Status: Open

Evidence baseline: [Runtime contracts](../../docs/phase-08-runtime-architecture/README.md)

Closing evidence: Recorded nondeterminism, replay equality, no duplicated external effect, REPL grant denial

- [ ] `runtime/remaining-tasks-and-evidence/close-enforcement-and-observability-integration` Close enforcement and observability integration

Status: Open

Evidence baseline: [Capability runtime contract](../../docs/phase-08-runtime-architecture/122-r11-runtime-capability-enforcement-design.md)

Closing evidence: Denial at actual service hook, redacted diagnostics and source-map preservation
<!-- runtime-implementation-tasks:end -->

## Historical workflow migration record

The original file below is retained unchanged as historical workflow progress.
Its checkboxes and earlier task-source statements do not duplicate the current
implementation authority or imply runtime completion.

# Tasks: Runtime workflow migration

Spec: [spec.md](spec.md) | Plan: [plan.md](plan.md) | Evidence: [evidence.md](evidence.md)
Owner: backend workflow migration owner. Input revision: `017b541d678c6acb93eb5e843b1eda5a66babc32`.

## Declared increment

These are migration-delivery tasks only. Implementation progress is exclusively
in [Runtime canonical remaining work](../../docs/component-roadmaps/runtime.md#remaining-tasks-and-evidence); do not copy its checklist here.

- [x] T001 [US1] Prepare the four owned package artifacts, preserving canonical
  contract, component and phase links. Evidence: the files in this directory.
- [x] T002 [US1] Validate all local links/anchors, ASCII, exact owned paths and
  preserved implementation status; retain the result in evidence.md.
- [ ] T003 [US2] Obtain independent review of the exact package candidate,
  including dependencies, boundary/nonclaims and semantic names.
- [ ] T004 [US1] Integrate the reviewed package and record its merge receipt.

## Dependencies and outcomes

T002 checks T001; T003 consumes the checked candidate; T004 consumes independent
acceptance. Parallel packages do not block this package's local preparation.
Component completion is not a prerequisite for its workflow migration, and
migration completion does not close the component's implementation tasks.
Partial work stays draft. No budget, task quota or repeated authorization gate.

## New-name review

Review `runtime-workflow` against the [semantic naming rule](../../.specify/semantic-names.md).
No legacy source rename or public compatibility migration is included.
