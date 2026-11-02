# Implementation plan: Emit task authority

Spec: [spec.md](spec.md) | Evidence: [evidence.md](evidence.md)
Owner role: compiler/backend maintainer.
Input revision: `176c4ee65bb3ceb545f51edfaa599902030a0cb7`.

## Contract alignment

Follow the [constitution](../../.specify/memory/constitution.md),
[development verification workflow](../../docs/development-verification.md) and
[canonical contracts](spec.md#purpose-and-canonical-contracts).
B13/B14 retain artifact/provenance and backend-conformance requirements;
workflow artifacts do not replace semantic authority.

Retain existing canonical navigation:

| Canonical truth | Reference | Package use |
| --- | --- | --- |
| Finite outcome | [Release outcome](../../docs/component-roadmaps/emit.md#release-outcome) | Declares component scope, not migration completion |
| Implementation tasks | [Emit canonical remaining work](../../docs/component-roadmaps/emit.md#current-evidence-and-remaining-work) | emit/1 through emit/5; retain their original status and evidence |
| Component acceptance | [Acceptance criteria](../../docs/component-roadmaps/emit.md#acceptance-criteria-yesno) | Required semantic evidence stays here |
| Phase tasks | [Phase implementation roadmap](../../docs/phase-07-backend-architecture/IMPLEMENTATION-ROADMAP.md) | Historical/current phase claims retain original scope |

Component role: compiler/backend maintainer. Interface owners: [lower](../../docs/component-roadmaps/lower.md#release-outcome), [runtime](../../docs/component-roadmaps/runtime.md#release-outcome), [build](../../docs/component-roadmaps/build.md#release-outcome), [backends](../../docs/component-roadmaps/backends.md#release-outcome).
Lower establishes legal backend input and Runtime supplies service/ABI contracts. Build consumes emitted records later. Preserve the selected staged Linux LLVM candidate; a hosted artifact does not establish seedless release or all-backend support.

## Approach and exact files

Move only the five real rows to the marked
[implementation region](tasks.md#implementation-tasks), retaining stable semantic
IDs, display labels, exact evidence/status and concrete exit criteria. Keep the
complete original migration task file as a frozen suffix with unchanged
checkboxes. Workflow delivery stays separate from implementation completion.

`tools/emit_task_projection.clj` uses the accepted shared pure parser and strict
HTTPS-only policy unchanged. It accepts only --check or --write and validates
all records before writing only its fixed roadmap table. No caller-selected
paths, discovery, registry or syntax allowance is added. The focused test covers
projection and structural/CLI rejection controls.

Own seven files: roadmap, four workflow artifacts and focused tool/test. No
compiler/runtime source/test, other package, mapping artifact or shared helper
is edited. The source plan blob `d62412306c4a726aa9c077a1abd0b72158c55677`
contains no Additional contract interfaces appendix; preserve B13/B14 and all
original navigation without creating an appendix. Removing only added task
navigation/markers must reconstruct the whole original roadmap, preserving
selected-target, hosted, producer/admission, accounting, conformance,
compatibility, wider-artifact and acceptance wording.

## Validation and evidence

Run from repository root:

```bash
clojure -J-Xmx512m -M tools/emit_task_projection.clj --write
clojure -J-Xmx512m -M tools/emit_task_projection.clj --check
clojure -J-Xmx512m -M tools/emit_task_projection_test.clj
clojure -J-Xmx512m -M:test --namespace gravity.self-hosting.sh01-language-boundary-test
clojure -J-Xmx512m -M tools/validate_gravity_docs.clj
clojure -J-Xmx512m -M tools/validate_full_language_roadmap.clj
```

Compare exact original/current table/cells, five derived IDs, complete historical
suffix, prior spec/plan navigation and reconstruction of the full original
roadmap. Verify B13/B14 targets and absence of an invented appendix. Check five
Markdown files for ASCII and file/heading links, including missing-file/heading
negative controls. Exercise missing/duplicate IDs and markers, malformed records,
title mismatch, explicit acceptance consistency, stale projections and unsupported
links. Confirm strict shared arrow rejection. Isolated subprocess fixtures prove
read-only checks, fixed-roadmap writes, extra-path rejection, non-dispatching
helper loads and outside-sentinel preservation. Run whitespace and clean exact-
base/head/tree integration preflight. These structural checks require no compiler
semantic or emission suite and grant no artifact acceptance.

## Delivery and remaining work

Independent review inspects all seven files and the proposed public body. Publish
a validated draft with exact identities and compact actual final-tree receipts
for maintainer current-head review. Integration follows that disposition and
repository gates. All five implementation outcomes remain Open.

## Names, callers and compatibility

`emit-task-projection` follows semantic component entrypoints. No compiler or
runtime API, wire/schema identity, diagnostic, manifest hash, target choice,
admission policy or legacy source name changes. Planning IDs remain traceability.
